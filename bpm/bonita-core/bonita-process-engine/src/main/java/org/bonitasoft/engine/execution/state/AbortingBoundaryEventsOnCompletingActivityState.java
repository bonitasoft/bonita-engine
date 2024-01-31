/**
 * Copyright (C) 2019 Bonitasoft S.A.
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

import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityStateExecutionException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.api.states.StateCode;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.execution.StateBehaviors;
import org.springframework.stereotype.Component;

@Component
public class AbortingBoundaryEventsOnCompletingActivityState implements FlowNodeState {

    private final StateBehaviors stateBehaviors;

    public AbortingBoundaryEventsOnCompletingActivityState(final StateBehaviors stateBehaviors) {
        this.stateBehaviors = stateBehaviors;
    }

    @Override
    public boolean mustAddSystemComment(final SFlowNodeInstance flowNodeInstance) {
        return false;
    }

    @Override
    public String getSystemComment(final SFlowNodeInstance flowNodeInstance) {
        return "";
    }

    @Override
    public int getId() {
        return 34;
    }

    @Override
    public String getName() {
        // TODO: should be changed but has impacts client-side, as it is exposed client-side.
        return "completing activity with boundary";
    }

    @Override
    public SStateCategory getStateCategory() {
        return SStateCategory.NORMAL;
    }

    @Override
    public boolean shouldExecuteState(final SProcessDefinition processDefinition,
            final SFlowNodeInstance flowNodeInstance) {
        final SActivityDefinition activityDef = (SActivityDefinition) processDefinition.getProcessContainer()
                .getFlowNode(flowNodeInstance.getFlowNodeDefinitionId());
        return !activityDef.getBoundaryEventDefinitions().isEmpty();
    }

    @Override
    public StateCode execute(final SProcessDefinition processDefinition, final SFlowNodeInstance instance)
            throws SActivityStateExecutionException {
        final SActivityDefinition activityDef = (SActivityDefinition) processDefinition.getProcessContainer()
                .getFlowNode(instance.getFlowNodeDefinitionId());
        if (!activityDef.getBoundaryEventDefinitions().isEmpty()) {
            final SActivityInstance activityInstance = (SActivityInstance) instance;
            stateBehaviors.interruptAttachedBoundaryEvent(processDefinition, activityInstance,
                    SStateCategory.ABORTING);
        }
        return StateCode.DONE;
    }

    @Override
    public boolean notifyChildFlowNodeHasFinished(final SProcessDefinition processDefinition,
            final SFlowNodeInstance parentInstance,
            final SFlowNodeInstance childInstance) {
        return true;
    }

    @Override
    public boolean isStable() {
        return false;
    }

    @Override
    public boolean isTerminal() {
        return false;
    }
}
