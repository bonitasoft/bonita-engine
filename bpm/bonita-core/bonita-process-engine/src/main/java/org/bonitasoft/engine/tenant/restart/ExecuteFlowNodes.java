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

import static org.bonitasoft.engine.commons.CollectionUtil.split;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.GatewayInstanceService;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.execution.work.BPMWorkFactory;
import org.bonitasoft.engine.log.technical.TechnicalLogger;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.bonitasoft.engine.work.WorkService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Restart flow nodes
 * <p>
 * This class is called after the start of the engine and will restart elements given their ids.
 * <p>
 * It will restart these elements using multiple transaction using a batch size configured by the property
 * `bonita.tenant.work.batch_restart_size`
 */
@Component
public class ExecuteFlowNodes {

    private final WorkService workService;
    private final BPMWorkFactory workFactory;
    private final TechnicalLogger logger;
    private final ActivityInstanceService activityInstanceService;
    private final GatewayInstanceService gatewayInstanceService;
    private final ProcessDefinitionService processDefinitionService;
    private final FlowNodeStateManager flowNodeStateManager;
    private final UserTransactionService userTransactionService;
    private final int batchRestartSize;

    public ExecuteFlowNodes(WorkService workService,
            @Qualifier("tenantTechnicalLoggerService") TechnicalLoggerService logger,
            ActivityInstanceService activityInstanceService, GatewayInstanceService gatewayInstanceService,
            ProcessDefinitionService processDefinitionService,
            FlowNodeStateManager flowNodeStateManager, BPMWorkFactory workFactory,
            UserTransactionService userTransactionService,
            @Value("${bonita.tenant.work.batch_restart_size:1000}") int batchRestartSize) {
        this.workService = workService;
        this.workFactory = workFactory;
        this.logger = logger.asLogger(ExecuteFlowNodes.class);
        this.activityInstanceService = activityInstanceService;
        this.gatewayInstanceService = gatewayInstanceService;
        this.processDefinitionService = processDefinitionService;
        this.flowNodeStateManager = flowNodeStateManager;
        this.userTransactionService = userTransactionService;
        this.batchRestartSize = batchRestartSize;
    }

    public void executeFlowNodes(List<Long> flowNodeIds) {
        ExecutionMonitor executionMonitor = new ExecutionMonitor(flowNodeIds.size());
        for (List<Long> batchedFlowNodeIds : split(flowNodeIds, batchRestartSize)) {
            try {
                userTransactionService.executeInTransaction(() -> {
                    executeBatch(executionMonitor, batchedFlowNodeIds);
                    return null;
                });
            } catch (Exception e) {
                logger.error(
                        "Error processing batch of flow nodes to restart, the following flow nodes might need to be restarted manually: {}",
                        batchedFlowNodeIds, e);
            }
            executionMonitor.printProgress();
        }
        executionMonitor.printSummary();
    }

    public void executeBatch(ExecutionMonitor executionMonitor, List<Long> flowNodeIds) throws SBonitaException {
        List<Long> unprocessed = new ArrayList<>(flowNodeIds);
        List<SFlowNodeInstance> flowNodeInstances = activityInstanceService.getFlowNodeInstancesByIds(flowNodeIds);
        for (SFlowNodeInstance flowNodeInstance : flowNodeInstances) {
            try {
                unprocessed.remove(flowNodeInstance.getId());
                if (flowNodeInstance.isTerminal()) {
                    executionMonitor.finishing++;
                    logger.debug("Restarting flow node (Notify finished...) with name = <" + flowNodeInstance.getName()
                            + ">, and id = <" + flowNodeInstance.getId()
                            + " in state = <" + flowNodeInstance.getStateName() + ">");
                    workService.registerWork(workFactory.createNotifyChildFinishedWorkDescriptor(flowNodeInstance));
                } else {
                    if (shouldExecuteFlownode(flowNodeInstance)) {
                        executionMonitor.executing++;
                        logger.debug("Restarting flow node (Execute ...) with name = <" + flowNodeInstance.getName()
                                + ">, and id = <" + flowNodeInstance.getId()
                                + "> in state = <" + flowNodeInstance.getStateName() + ">");
                        workService.registerWork(workFactory.createExecuteFlowNodeWorkDescriptor(flowNodeInstance));
                    } else {
                        executionMonitor.notExecutable++;
                        logger.debug(
                                "Flownode with name = <{}>, and id = <{}> in state = <{}> does not fulfill the restart conditions.",
                                flowNodeInstance.getName(), flowNodeInstance.getId(), flowNodeInstance.getStateName());
                    }
                }
            } catch (Exception e) {
                logger.error("Error restarting flow node {}", flowNodeInstance.getId(), e);
                executionMonitor.inError++;
            }
        }
        executionMonitor.notFound += unprocessed.size();
    }

    /**
     * Determines if the found flownode should be relaunched at restart or not.
     * <ul>
     * <li>Gateways should only be started when they are 'merged'.</li>
     * <li>Elements in state category cancelling or aborting must be restart only if the current state is not part of
     * this statecategory.</li>
     * </ul>
     *
     * @param sFlowNodeInstance the flownode to check
     * @return true if the flownode should be relaunched because it has not finished its work in progress, false
     *         otherwise.
     * @throws SBonitaException in case of error.
     */
    boolean shouldExecuteFlownode(final SFlowNodeInstance sFlowNodeInstance) throws SBonitaException {
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
        if (SFlowNodeType.GATEWAY.equals(sFlowNodeInstance.getType())) {
            SProcessDefinition processDefinition = processDefinitionService
                    .getProcessDefinition(sFlowNodeInstance.getProcessDefinitionId());
            return sFlowNodeInstance.isAborting() || sFlowNodeInstance.isCanceling() ||
                    gatewayInstanceService.checkMergingCondition(processDefinition,
                            (SGatewayInstance) sFlowNodeInstance);
        }
        return true;
    }

    private class ExecutionMonitor {

        long finishing;
        long executing;
        long notExecutable;
        long notFound;
        long inError;
        private final long startTime;
        private final int numberOfElementsToProcess;

        public ExecutionMonitor(int numberOfElementsToProcess) {
            this.numberOfElementsToProcess = numberOfElementsToProcess;
            startTime = System.currentTimeMillis();
        }

        public void printProgress() {
            logger.info("Restarting elements...Processed "
                    + (finishing + executing + notExecutable + notFound + inError) + " of "
                    + numberOfElementsToProcess +
                    " flow nodes to be restarted in " + Duration.ofMillis(System.currentTimeMillis() - startTime));
        }

        public void printSummary() {
            logger.info("Restart of flow nodes completed.");
            logger.info("Processed {} flow nodes to be restarted in {}",
                    (finishing + executing + notExecutable + notFound + inError),
                    Duration.ofMillis(System.currentTimeMillis() - startTime));
            logger.info("Found {} flow nodes to be executed", executing);
            logger.info("Found {} flow nodes to be completed", finishing);
            logger.info("Found {} flow nodes that were not executable (e.g. unmerged gateway)", notExecutable);
            if (notFound > 0) {
                logger.info(notFound + " flow nodes were not found (might have been manually executed)");
            }
            if (inError > 0) {
                logger.info("Found {} flow nodes in error (see stacktrace for reason)", inError);
            }
        }
    }

}
