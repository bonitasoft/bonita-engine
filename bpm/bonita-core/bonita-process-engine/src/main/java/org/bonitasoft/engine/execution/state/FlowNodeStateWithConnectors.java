/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import static org.bonitasoft.engine.execution.StateBehaviors.AFTER_ON_FINISH;
import static org.bonitasoft.engine.execution.StateBehaviors.BEFORE_ON_ENTER;
import static org.bonitasoft.engine.execution.StateBehaviors.BEFORE_ON_FINISH;
import static org.bonitasoft.engine.execution.StateBehaviors.DURING_ON_ENTER;
import static org.bonitasoft.engine.execution.StateBehaviors.DURING_ON_FINISH;

import org.bonitasoft.engine.bpm.bar.xml.XMLProcessDefinition.BEntry;
import org.bonitasoft.engine.core.process.definition.model.SConnectorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityStateExecutionException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.api.states.StateCode;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.execution.StateBehaviors;

/**
 * @author Baptiste Mesta
 */
public abstract class FlowNodeStateWithConnectors implements FlowNodeState {

    private final StateBehaviors stateBehaviors;

    private final boolean executeConnectorsOnEnter;

    private final boolean executeConnectorsOnFinish;

    public FlowNodeStateWithConnectors(final StateBehaviors stateBehaviors, final boolean executeOnEnter, final boolean executeOnFinish) {
        this.stateBehaviors = stateBehaviors;
        executeConnectorsOnEnter = executeOnEnter;
        executeConnectorsOnFinish = executeOnFinish;
    }

    protected abstract void beforeOnEnter(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance)
            throws SActivityStateExecutionException;

    protected abstract void onEnterToOnFinish(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance)
            throws SActivityStateExecutionException;

    protected abstract void afterOnFinish(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance)
            throws SActivityStateExecutionException;

    @Override
    public StateCode execute(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance) throws SActivityStateExecutionException {
        // Retrieve the phase to execute depending on which connectors to execute and when to execute them:
        final BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> entry = stateBehaviors.getConnectorToExecuteAndFlag(processDefinition,
                flowNodeInstance, executeConnectorsOnEnter, executeConnectorsOnFinish);
        final Integer phase = entry.getKey();
        if ((phase & BEFORE_ON_ENTER) != 0) {
            beforeOnEnter(processDefinition, flowNodeInstance);
        }
        if ((phase & DURING_ON_ENTER) != 0 && executeConnectorsOnEnter) {
            stateBehaviors.executeConnectorInWork(processDefinition.getId(), flowNodeInstance.getParentProcessInstanceId(), flowNodeInstance.getFlowNodeDefinitionId(), flowNodeInstance.getId(), entry
                    .getValue().getKey(), entry.getValue().getValue());
            return StateCode.EXECUTING;
        }
        if ((phase & BEFORE_ON_FINISH) != 0) {
            onEnterToOnFinish(processDefinition, flowNodeInstance);
        }
        if ((phase & DURING_ON_FINISH) != 0 && executeConnectorsOnFinish) {
            stateBehaviors.executeConnectorInWork(processDefinition.getId(), flowNodeInstance.getParentProcessInstanceId(), flowNodeInstance.getFlowNodeDefinitionId(), flowNodeInstance.getId(), entry
                    .getValue().getKey(), entry.getValue().getValue());
            return StateCode.EXECUTING;
        }
        if ((phase & AFTER_ON_FINISH) != 0) {
            afterOnFinish(processDefinition, flowNodeInstance);
        }
        return StateCode.DONE;
    }

}
