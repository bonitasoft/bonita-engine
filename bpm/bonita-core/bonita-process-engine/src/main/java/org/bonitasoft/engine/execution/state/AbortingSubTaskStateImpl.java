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

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityExecutionException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.api.states.StateCode;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.execution.StateBehaviors;
import org.springframework.stereotype.Component;

@Component
public class AbortingSubTaskStateImpl implements FlowNodeState {

    private final StateBehaviors stateBehaviors;

    public AbortingSubTaskStateImpl(final StateBehaviors stateBehaviors) {
        super();
        this.stateBehaviors = stateBehaviors;
    }

    @Override
    public StateCode execute(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance) {
        return StateCode.DONE;
    }

    @Override
    public int getId() {
        return 13;
    }

    @Override
    public boolean isInterrupting() {
        return false;
    }

    @Override
    public boolean isStable() {
        return true;
    }

    @Override
    public boolean isTerminal() {
        return false;
    }

    @Override
    public String getName() {
        // TODO: should be changed but has impacts client-side, as it is exposed in
        // org.bonitasoft.engine.bpm.flownode.ActivityStates.CANCELLING_SUBTASKS_STATE
        return "cancelling subtasks";
    }

    @Override
    public boolean notifyChildFlowNodeHasFinished(final SProcessDefinition processDefinition,
            final SFlowNodeInstance parentInstance,
            final SFlowNodeInstance childInstance) {
        final SHumanTaskInstance sHumanTaskInstance = (SHumanTaskInstance) parentInstance;
        return sHumanTaskInstance.getTokenCount() == 0;
    }

    @Override
    public boolean shouldExecuteState(final SProcessDefinition processDefinition,
            final SFlowNodeInstance flowNodeInstance) throws SActivityExecutionException {
        final SHumanTaskInstance sHumanTaskInstance = (SHumanTaskInstance) flowNodeInstance;
        final boolean hasTokens = sHumanTaskInstance.getTokenCount() > 0;
        if (hasTokens) {
            try {
                stateBehaviors.interruptSubActivities(flowNodeInstance, SStateCategory.ABORTING);
            } catch (final SBonitaException e) {
                throw new SActivityExecutionException(e);
            }
        }
        return hasTokens;
    }

    @Override
    public SStateCategory getStateCategory() {
        return SStateCategory.NORMAL;
    }

    @Override
    public boolean mustAddSystemComment(final SFlowNodeInstance flowNodeInstance) {
        return false;
    }

    @Override
    public String getSystemComment(final SFlowNodeInstance flowNodeInstance) {
        return "";
    }

}
