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
package org.bonitasoft.engine.execution.work;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.execution.WaitingEventsInterrupter;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;

/**
 * @author Elias Ricken de Medeiros
 */
@Slf4j
public class FailedStateSetter {

    private final WaitingEventsInterrupter waitingEventsInterrupter;

    private final ActivityInstanceService activityInstanceService;

    private final FlowNodeStateManager flowNodeStateManager;

    public FailedStateSetter(WaitingEventsInterrupter waitingEventsInterrupter,
            final ActivityInstanceService activityInstanceService,
            final FlowNodeStateManager flowNodeStateManager) {
        this.waitingEventsInterrupter = waitingEventsInterrupter;
        this.activityInstanceService = activityInstanceService;
        this.flowNodeStateManager = flowNodeStateManager;
    }

    public void setAsFailed(long flowNodeInstanceId) throws SBonitaException {
        final SFlowNodeInstance flowNodeInstance;
        try {
            flowNodeInstance = activityInstanceService.getFlowNodeInstance(flowNodeInstanceId);

            //nothing to do if the flownode is already in failed state
            if (flowNodeInstance.getStateId() != FlowNodeState.ID_ACTIVITY_FAILED) {
                activityInstanceService.setState(flowNodeInstance,
                        flowNodeStateManager.getState(FlowNodeState.ID_ACTIVITY_FAILED));
                waitingEventsInterrupter.interruptWaitingEvents(flowNodeInstance);
            }
        } catch (SFlowNodeNotFoundException e) {
            log.debug(
                    "Impossible to put flow node instance in failed state: flow node instance with id '{}' not found.",
                    flowNodeInstanceId);
        }
    }

}
