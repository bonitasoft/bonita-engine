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

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityStateExecutionException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.api.states.StateCode;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.builder.SFlowNodeInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.SUserTaskInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.event.SBoundaryEventInstance;
import org.bonitasoft.engine.execution.ContainerRegistry;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class ExecutingBoundaryEventStateImpl implements FlowNodeState {

    private final ActivityInstanceService activityInstanceService;

    private final ContainerRegistry containerRegistry;

    public ExecutingBoundaryEventStateImpl(final ActivityInstanceService activityInstanceService, final ContainerRegistry containerRegistry) {
        this.activityInstanceService = activityInstanceService;
        this.containerRegistry = containerRegistry;
    }

    @Override
    public boolean shouldExecuteState(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance) {
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
    public StateCode execute(final SProcessDefinition processDefinition, final SFlowNodeInstance instance) throws SActivityStateExecutionException {
        final SBoundaryEventInstance boundaryEventInstance = (SBoundaryEventInstance) instance;
        if (boundaryEventInstance.isInterrupting()) {
            abortRelatedActivity(boundaryEventInstance);
        }

        return StateCode.DONE;
    }

    private void abortRelatedActivity(final SBoundaryEventInstance boundaryEventInstance) throws SActivityStateExecutionException {
        final SFlowNodeInstanceBuilderFactory flowNodeKeyProvider = BuilderFactory.get(SUserTaskInstanceBuilderFactory.class);
        if (SStateCategory.NORMAL.equals(boundaryEventInstance.getStateCategory())) {
            try {
                final SActivityInstance relatedActivityInst = activityInstanceService.getActivityInstance(boundaryEventInstance.getActivityInstanceId());
                activityInstanceService.setStateCategory(relatedActivityInst, SStateCategory.ABORTING);
                activityInstanceService.setAbortedByBoundaryEvent(relatedActivityInst, boundaryEventInstance.getId());
                if (relatedActivityInst.isStable() || relatedActivityInst.isStateExecuting()) {
                    containerRegistry
                            .executeFlowNode(relatedActivityInst.getProcessDefinitionId(),
                                    boundaryEventInstance.getLogicalGroup(flowNodeKeyProvider.getParentProcessInstanceIndex()), relatedActivityInst.getId(),
                                    null, null);
                }
            } catch (final SBonitaException e) {
                throw new SActivityStateExecutionException(e);
            }
        }
    }

    @Override
    public boolean hit(final SProcessDefinition processDefinition, final SFlowNodeInstance parentInstance, final SFlowNodeInstance childInstance) {
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
    public boolean isInterrupting() {
        return false;
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
