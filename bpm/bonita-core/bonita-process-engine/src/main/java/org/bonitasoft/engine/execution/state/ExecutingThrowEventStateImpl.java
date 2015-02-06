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

import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityStateExecutionException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.execution.StateBehaviors;

/**
 * @author Baptiste Mesta
 */
public class ExecutingThrowEventStateImpl extends ExecutingFlowNodeStateImpl {

    private final StateBehaviors stateBehaviors;

    public ExecutingThrowEventStateImpl(final StateBehaviors stateBehaviors) {
        super(stateBehaviors, true, true);
        this.stateBehaviors = stateBehaviors;
    }

    @Override
    public int getId() {
        return 26;
    }

    @Override
    protected void beforeOnEnter(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance) throws SActivityStateExecutionException {
        stateBehaviors.handleCatchEvents(processDefinition, flowNodeInstance);
        stateBehaviors.createData(processDefinition, flowNodeInstance);
    }

    @Override
    protected void onEnterToOnFinish(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance)
            throws SActivityStateExecutionException {
        final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
        stateBehaviors.updateDisplayNameAndDescription(processDefinition, flowNodeInstance);
        stateBehaviors.mapActors(flowNodeInstance, processContainer);
        stateBehaviors.handleCallActivity(processDefinition, flowNodeInstance);
        stateBehaviors.handleThrowEvent(processDefinition, flowNodeInstance);
    }

    @Override
    protected void afterOnFinish(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance) throws SActivityStateExecutionException {
        stateBehaviors.updateDisplayDescriptionAfterCompletion(processDefinition, flowNodeInstance);
    }
}
