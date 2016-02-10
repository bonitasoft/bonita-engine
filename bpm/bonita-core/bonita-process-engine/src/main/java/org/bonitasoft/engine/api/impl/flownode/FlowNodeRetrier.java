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

package org.bonitasoft.engine.api.impl.flownode;

import org.bonitasoft.engine.api.impl.connector.ConnectorResetStrategy;
import org.bonitasoft.engine.bpm.flownode.ActivityExecutionException;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ActivityStates;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.execution.FlowNodeExecutor;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;

/**
 * @author Elias Ricken de Medeiros
 */
public class FlowNodeRetrier {

    private final ContainerRegistry containerRegistry;
    private final FlowNodeExecutor flowNodeExecutor;
    private final ActivityInstanceService activityInstanceService;
    private final FlowNodeStateManager stateManager;
    private final ConnectorResetStrategy strategy;

    public FlowNodeRetrier(ContainerRegistry containerRegistry, FlowNodeExecutor flowNodeExecutor, ActivityInstanceService activityInstanceService,
            FlowNodeStateManager stateManager, final ConnectorResetStrategy strategy) {
        this.containerRegistry = containerRegistry;
        this.flowNodeExecutor = flowNodeExecutor;
        this.activityInstanceService = activityInstanceService;
        this.stateManager = stateManager;
        this.strategy = strategy;
    }

    public void retry(long flowNodeInstanceId) throws ActivityExecutionException, ActivityInstanceNotFoundException {

        try {
            final SFlowNodeInstance flowNodeInstance = activityInstanceService.getFlowNodeInstance(flowNodeInstanceId);
            FlowNodeState previousState = stateManager.getState(flowNodeInstance.getPreviousStateId());
            validateCurrentState(flowNodeInstance);
            strategy.resetConnectorsOf(flowNodeInstanceId);
            flowNodeExecutor.setStateByStateId(flowNodeInstance.getProcessDefinitionId(), flowNodeInstanceId, flowNodeInstance.getPreviousStateId());

            if (!previousState.isTerminal()) {
                containerRegistry.executeFlowNode(flowNodeInstance.getProcessDefinitionId(), flowNodeInstance.getParentProcessInstanceId(), flowNodeInstanceId,
                        null, null);
            }
        } catch (final SFlowNodeNotFoundException e) {
            throw new ActivityInstanceNotFoundException(e);
        } catch (final SBonitaException e) {
            throw new ActivityExecutionException(e);
        }

    }

    private void validateCurrentState(final SFlowNodeInstance flowNodeInstance) throws ActivityExecutionException {
        FlowNodeState currentState = stateManager.getState(flowNodeInstance.getStateId());
        if (!ActivityStates.FAILED_STATE.equals(currentState.getName())) {
            throw new ActivityExecutionException("Unable to retry the flow node instance [name=" + flowNodeInstance.getName() + ", id="
                    + flowNodeInstance.getId() + "] because it is not in failed state. The current state for this flow node instance is '"
                    + currentState.getName() + "'");
        }
    }

}
