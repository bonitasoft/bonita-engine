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
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityStateExecutionException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.api.states.StateCode;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.event.SBoundaryEventInstance;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.execution.StateBehaviors;
import org.springframework.stereotype.Component;

@Component
public class ExecutingBoundaryEventState implements FlowNodeState {

    private final ActivityInstanceService activityInstanceService;

    private final ContainerRegistry containerRegistry;

    private final StateBehaviors stateBehaviors;

    public ExecutingBoundaryEventState(ActivityInstanceService activityInstanceService,
            ContainerRegistry containerRegistry,
            StateBehaviors stateBehaviors) {
        this.activityInstanceService = activityInstanceService;
        this.containerRegistry = containerRegistry;
        this.stateBehaviors = stateBehaviors;
    }

    @Override
    public boolean shouldExecuteState(final SProcessDefinition processDefinition,
            final SFlowNodeInstance flowNodeInstance) {
        return true;
    }

    @Override
    public boolean mustAddSystemComment(final SFlowNodeInstance flowNodeInstance) {
        return false;
    }

    @Override
    public String getSystemComment(final SFlowNodeInstance flowNodeInstance) {
        return "executing boundary event";
    }

    @Override
    public StateCode execute(final SProcessDefinition processDefinition, final SFlowNodeInstance boundary)
            throws SActivityStateExecutionException {
        final SBoundaryEventInstance boundaryEventInstance = (SBoundaryEventInstance) boundary;
        if (boundaryEventInstance.isInterrupting()) {
            abortRelatedActivity(boundaryEventInstance);
            // Also abort the other boundary events on the same flow node instance:
            try {
                stateBehaviors.interruptAttachedBoundaryEvent(processDefinition,
                        activityInstanceService.getActivityInstance(boundaryEventInstance.getActivityInstanceId()),
                        SStateCategory.ABORTING);
            } catch (SActivityInstanceNotFoundException | SActivityReadException e) {
                throw new SActivityStateExecutionException(e);
            }
        }

        return StateCode.DONE;
    }

    private void abortRelatedActivity(final SBoundaryEventInstance boundaryEventInstance)
            throws SActivityStateExecutionException {
        if (SStateCategory.NORMAL.equals(boundaryEventInstance.getStateCategory())) {
            try {
                final SActivityInstance relatedActivityInst = activityInstanceService
                        .getActivityInstance(boundaryEventInstance.getActivityInstanceId());

                activityInstanceService.setStateCategory(relatedActivityInst, SStateCategory.ABORTING);
                activityInstanceService.setAbortedByBoundaryEvent(relatedActivityInst, boundaryEventInstance.getId());
                containerRegistry
                        .executeFlowNode(relatedActivityInst);
            } catch (final SBonitaException e) {
                throw new SActivityStateExecutionException(e);
            }
        }
    }

    @Override
    public boolean notifyChildFlowNodeHasFinished(final SProcessDefinition processDefinition,
            final SFlowNodeInstance parentInstance,
            final SFlowNodeInstance childInstance) {
        return true;
    }

    @Override
    public int getId() {
        return 65;
    }

    @Override
    public String getName() {
        return "executing";
    }

    @Override
    public boolean isStable() {
        return false;
    }

    @Override
    public boolean isTerminal() {
        return false;
    }

    @Override
    public SStateCategory getStateCategory() {
        return SStateCategory.NORMAL;
    }

}
