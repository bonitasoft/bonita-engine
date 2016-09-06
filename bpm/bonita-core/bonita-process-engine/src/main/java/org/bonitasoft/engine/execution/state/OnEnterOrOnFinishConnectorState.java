/**
 * Copyright (C) 2016 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/

package org.bonitasoft.engine.execution.state;

import java.util.List;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceReadException;
import org.bonitasoft.engine.core.process.definition.model.SConnectorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityStateExecutionException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.api.states.StateCode;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.execution.StateBehaviors;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Baptiste Mesta
 */
public abstract class OnEnterOrOnFinishConnectorState implements FlowNodeState {

    protected final StateBehaviors stateBehaviors;

    public OnEnterOrOnFinishConnectorState(StateBehaviors stateBehaviors) {
        this.stateBehaviors = stateBehaviors;
    }

    protected abstract void beforeConnectors(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance)
            throws SActivityStateExecutionException;

    protected abstract void afterConnectors(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance)
            throws SActivityStateExecutionException;

    public StateCode execute(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance) throws SActivityStateExecutionException {
        // Retrieve the phase to execute depending on which connectors to execute and when to execute them:
        try {
            ConnectorEvent connectorEvent = getConnectorEvent();
            //get all element to check where we are in the state execution
            List<SConnectorDefinition> connectorDefinitions = stateBehaviors.getConnectorDefinitions(processDefinition, flowNodeInstance, connectorEvent);
            final SConnectorInstance connectorInstance = stateBehaviors.getNextConnectorInstance(connectorDefinitions, flowNodeInstance, connectorEvent);
            boolean noConnectorStartedOnEnter = stateBehaviors.noConnectorHasStartedInCurrentList(connectorDefinitions, connectorInstance
            );
            if (noConnectorStartedOnEnter) {
                //TODO unit test condition on this
                beforeConnectors(processDefinition, flowNodeInstance);
            }
            if (connectorInstance != null) {
                stateBehaviors.executeConnector(processDefinition, flowNodeInstance, connectorDefinitions, connectorInstance);
                return StateCode.EXECUTING;
            }
            afterConnectors(processDefinition, flowNodeInstance);
            return StateCode.DONE;
        } catch (SBonitaReadException | SConnectorInstanceReadException e) {
            throw new SActivityStateExecutionException(e);
        }
    }

    protected abstract ConnectorEvent getConnectorEvent();
}
