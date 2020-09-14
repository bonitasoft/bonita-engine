/**
 * Copyright (C) 2020 Bonitasoft S.A.
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

import java.util.List;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.commons.CollectionUtil;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.execution.ProcessExecutor;
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
 * This class handles the continuation of unfinished process instances
 * passed as parameter in {@link #execute(List)} method.
 * The logic ensure that if an instance fails to continue its execution,
 * the error is logged and other instances are continued anyways.
 */
@Component
public class ExecuteProcesses {

    private final WorkService workService;
    private final TechnicalLogger logger;
    private final ActivityInstanceService activityInstanceService;
    private final ProcessDefinitionService processDefinitionService;
    private final ProcessInstanceService processInstanceService;
    private final ProcessExecutor processExecutor;
    private final FlowNodeStateManager flowNodeStateManager;
    private final BPMWorkFactory workFactory;
    private final UserTransactionService userTransactionService;
    private final int batchRestartSize;

    public ExecuteProcesses(WorkService workService,
            @Qualifier("tenantTechnicalLoggerService") TechnicalLoggerService logger,
            ActivityInstanceService activityInstanceService,
            ProcessDefinitionService processDefinitionService,
            ProcessInstanceService processInstanceService,
            ProcessExecutor processExecutor,
            FlowNodeStateManager flowNodeStateManager,
            BPMWorkFactory workFactory,
            UserTransactionService userTransactionService,
            @Value("${bonita.tenant.work.batch_restart_size:1000}") int batchRestartSize) {
        this.workService = workService;
        this.logger = logger.asLogger(ExecuteProcesses.class);
        this.activityInstanceService = activityInstanceService;
        this.processDefinitionService = processDefinitionService;
        this.processInstanceService = processInstanceService;
        this.processExecutor = processExecutor;
        this.flowNodeStateManager = flowNodeStateManager;
        this.workFactory = workFactory;
        this.userTransactionService = userTransactionService;
        this.batchRestartSize = batchRestartSize;
    }

    public void execute(List<Long> processInstanceIdsToRestart) {

        for (List<Long> ids : CollectionUtil.split(processInstanceIdsToRestart, batchRestartSize)) {
            try {
                userTransactionService.executeInTransaction(() -> restartBatch(ids));
            } catch (Exception e) {
                logger.error(
                        "Some processes failed to recover, they might seem stuck, a server restart is required to unlock all stuck process instances.",
                        e);
            }
        }
    }

    public Object restartBatch(List<Long> ids) {
        for (Long processId : ids) {
            try {
                final SProcessInstance processInstance = processInstanceService.getProcessInstance(processId);
                final SProcessDefinition processDefinition = processDefinitionService
                        .getProcessDefinition(processInstance.getProcessDefinitionId());
                switch (ProcessInstanceState.getFromId(processInstance.getStateId())) {
                    case ABORTED:
                    case CANCELLED:
                    case COMPLETED:
                        handleCompletion(processInstance);
                        break;
                    case COMPLETING:
                        processExecutor.registerConnectorsToExecute(processDefinition, processInstance,
                                ConnectorEvent.ON_FINISH,
                                null);
                        break;
                    case INITIALIZING:
                        processExecutor.registerConnectorsToExecute(processDefinition, processInstance,
                                ConnectorEvent.ON_ENTER,
                                null);
                        break;
                    default:
                        break;
                }
            } catch (final SProcessInstanceNotFoundException e) {
                logger.debug("Unable to restart the process instance "
                        + processId + ", it is not found (already completed).");
            } catch (final Exception e) {
                logger.error("Unable to restart the process instance "
                        + processId + ", a server restart is required to unlock all stuck process instances.", e);
            }
        }
        return null;
    }

    protected void handleCompletion(final SProcessInstance processInstance)
            throws SBonitaException {
        // Only Error events set interruptedByEvent on SProcessInstance:
        if (!processInstance.hasBeenInterruptedByEvent()) {

            final long callerId = processInstance.getCallerId();
            // Should always be in a CallActivity:
            if (callerId > 0) {
                final SActivityInstance callActivityInstance = activityInstanceService
                        .getActivityInstance(processInstance.getCallerId());
                if (callActivityInstance.getStateId() != FlowNodeState.ID_ACTIVITY_FAILED) {
                    workService.registerWork(workFactory.createExecuteFlowNodeWorkDescriptor(callActivityInstance));
                    logger.info("Restarting notification of finished process '{}' with id {} in state {}",
                            processInstance.getName(), processInstance.getId(),
                            ProcessInstanceState.getFromId(processInstance.getStateId()));
                }
            }
            // No need to handle completion of process instance in state COMPLETED here,
            // as it can never happen, because when a process instance goes into COMPLETED state, it is archived
            // directly in the same transaction (in ArchiveProcessInstanceHandler)
        }
    }
}
