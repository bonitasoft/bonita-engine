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

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
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
import org.bonitasoft.engine.execution.work.BPMWorkFactory;
import org.bonitasoft.engine.work.WorkService;
import org.springframework.stereotype.Component;

/**
 * This class handles the continuation of unfinished process instances
 * passed as parameter in {@link #execute(RecoveryMonitor, List)} method.
 * The logic ensures that if an instance fails to continue its execution,
 * the error is logged and other instances are continued anyways.
 */
@Slf4j
@Component
public class ProcessesRecover {

    private final WorkService workService;
    private final ActivityInstanceService activityInstanceService;
    private final ProcessDefinitionService processDefinitionService;
    private final ProcessInstanceService processInstanceService;
    private final ProcessExecutor processExecutor;
    private final BPMWorkFactory workFactory;

    public ProcessesRecover(WorkService workService,
            ActivityInstanceService activityInstanceService,
            ProcessDefinitionService processDefinitionService,
            ProcessInstanceService processInstanceService,
            ProcessExecutor processExecutor,
            BPMWorkFactory workFactory) {
        this.workService = workService;
        this.activityInstanceService = activityInstanceService;
        this.processDefinitionService = processDefinitionService;
        this.processInstanceService = processInstanceService;
        this.processExecutor = processExecutor;
        this.workFactory = workFactory;
    }

    void execute(RecoveryMonitor recoveryMonitor, List<Long> ids) {
        for (Long processId : ids) {
            try {
                final SProcessInstance processInstance = processInstanceService.getProcessInstance(processId);
                final SProcessDefinition processDefinition = processDefinitionService
                        .getProcessDefinition(processInstance.getProcessDefinitionId());
                switch (ProcessInstanceState.getFromId(processInstance.getStateId())) {
                    case ABORTED:
                    case CANCELLED:
                    case COMPLETED:
                        recoveryMonitor.incrementFinishing();
                        handleCompletion(processInstance);
                        break;
                    case COMPLETING:
                        recoveryMonitor.incrementFinishing();
                        processExecutor.registerConnectorsToExecute(processDefinition, processInstance,
                                ConnectorEvent.ON_FINISH,
                                null);
                        break;
                    case INITIALIZING:
                        recoveryMonitor.incrementExecuting();
                        processExecutor.registerConnectorsToExecute(processDefinition, processInstance,
                                ConnectorEvent.ON_ENTER,
                                null);
                        break;
                    default:
                        recoveryMonitor.incrementNotExecutable();
                        break;
                }
            } catch (final SProcessInstanceNotFoundException e) {
                recoveryMonitor.incrementNotFound();
                log.debug("Unable to recover the process instance {}, it is not found (probably already completed).",
                        processId);
            } catch (final Exception e) {
                recoveryMonitor.incrementInError();
                log.warn(
                        "Unable to recover the process instance {}, it will be retry in next recovery. Because : {} ",
                        processId, e.getMessage());
                log.debug("Cause", e);
            }
        }
    }

    private void handleCompletion(final SProcessInstance processInstance)
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
                    log.info("Restarting notification of finished process '{}' with id {} in state {}",
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
