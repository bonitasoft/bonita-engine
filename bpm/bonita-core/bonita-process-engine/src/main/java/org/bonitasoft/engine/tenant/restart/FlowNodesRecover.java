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
package org.bonitasoft.engine.tenant.restart;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.api.utils.VisibleForTesting;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.execution.work.BPMWorkFactory;
import org.bonitasoft.engine.work.WorkService;
import org.springframework.stereotype.Component;

/**
 * Recover flow nodes
 * <p>
 * This class is called after the start of the engine or periodically and will recover elements given their ids.
 */
@Slf4j
@Component
public class FlowNodesRecover {

    private final WorkService workService;
    private final BPMWorkFactory workFactory;
    private final ActivityInstanceService activityInstanceService;
    private final FlowNodeStateManager flowNodeStateManager;

    public FlowNodesRecover(WorkService workService,
            ActivityInstanceService activityInstanceService,
            FlowNodeStateManager flowNodeStateManager, BPMWorkFactory workFactory) {
        this.workService = workService;
        this.workFactory = workFactory;
        this.activityInstanceService = activityInstanceService;
        this.flowNodeStateManager = flowNodeStateManager;
    }

    void execute(RecoveryMonitor recoveryMonitor, List<Long> flowNodeIds) throws SBonitaException {
        List<Long> unprocessed = new ArrayList<>(flowNodeIds);
        List<SFlowNodeInstance> flowNodeInstances = activityInstanceService.getFlowNodeInstancesByIds(flowNodeIds);
        for (SFlowNodeInstance flowNodeInstance : flowNodeInstances) {
            unprocessed.remove(flowNodeInstance.getId());
            if (flowNodeInstance.isTerminal()) {
                recoveryMonitor.incrementFinishing();
                log.debug("Restarting flow node (Notify finished...) with name = <" + flowNodeInstance.getName()
                        + ">, and id = <" + flowNodeInstance.getId()
                        + " in state = <" + flowNodeInstance.getStateName() + ">");
                workService.registerWork(workFactory.createNotifyChildFinishedWorkDescriptor(flowNodeInstance));
            } else {
                if (shouldBeRecovered(flowNodeInstance)) {
                    recoveryMonitor.incrementExecuting();
                    log.debug("Recovering flow node (Execute ...) with name = <" + flowNodeInstance.getName()
                            + ">, and id = <" + flowNodeInstance.getId()
                            + "> in state = <" + flowNodeInstance.getStateName() + ">");
                    workService.registerWork(workFactory.createExecuteFlowNodeWorkDescriptor(flowNodeInstance));
                } else {
                    recoveryMonitor.incrementNotExecutable();
                    log.debug(
                            "Flownode with name = <{}>, and id = <{}> in state = <{}> does not fulfill the recovered conditions.",
                            flowNodeInstance.getName(), flowNodeInstance.getId(), flowNodeInstance.getStateName());
                }
            }
        }
        recoveryMonitor.incrementNotFound(unprocessed.size());
    }

    @VisibleForTesting
    boolean shouldBeRecovered(final SFlowNodeInstance sFlowNodeInstance) {
        //when state category is cancelling but the state is 'stable' (e.g. boundary event in waiting but that has been cancelled)
        if ((sFlowNodeInstance.getStateCategory().equals(SStateCategory.CANCELLING)
                || sFlowNodeInstance.getStateCategory().equals(SStateCategory.ABORTING))
                && !sFlowNodeInstance.isTerminal()
                && sFlowNodeInstance.isStable()) {
            FlowNodeState state = flowNodeStateManager.getState(sFlowNodeInstance.getStateId());
            //in this case we restart it only if the state is not in this cancelling of aborting category
            //this can happen when we abort a process with a call activity:
            //the call activity is put in aborting state and wait for its children to finish, in that case we do not call execute on it
            return !state.getStateCategory().equals(sFlowNodeInstance.getStateCategory());
        }
        // Gateway is a special case :
        //   Initial state is not stable but it should probably be stable because it waits for other flowNode to be completed.
        if (SFlowNodeType.GATEWAY.equals(sFlowNodeInstance.getType())) {
            return sFlowNodeInstance.isAborting() || sFlowNodeInstance.isCanceling()
                    || ((SGatewayInstance) sFlowNodeInstance).isFinished();
        }
        return true;
    }

}
