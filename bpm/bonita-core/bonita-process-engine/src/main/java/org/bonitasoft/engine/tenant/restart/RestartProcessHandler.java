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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.api.utils.VisibleForTesting;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.execution.ProcessExecutor;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.execution.work.BPMWorkFactory;
import org.bonitasoft.engine.execution.work.ExecuteConnectorOfProcess;
import org.bonitasoft.engine.execution.work.RestartException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.bonitasoft.engine.work.WorkService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Restart handler for work {@link ExecuteConnectorOfProcess}
 *
 * @author Baptiste Mesta
 */
@Component
public class RestartProcessHandler implements TenantRestartHandler {

    private final Long tenantId;
    private final WorkService workService;
    private final ActivityInstanceService activityInstanceService;
    private final ProcessDefinitionService processDefinitionService;
    private final ProcessExecutor processExecutor;
    private final BPMWorkFactory workFactory;
    private final FlowNodeStateManager flowNodeStateManager;
    private final UserTransactionService transactionService;
    private final TechnicalLoggerService logger;
    private final ProcessInstanceService processInstanceService;
    //the handler is executed on one tenant only but we keep a map by tenant because this class is a singleton
    //It should not be a singleton but have a factory to create it
    private Map<Long, List<Long>> processInstancesByTenant = new HashMap<>();

    public RestartProcessHandler(@Value("${tenantId}") Long tenantId, WorkService workService,
            ActivityInstanceService activityInstanceService, ProcessDefinitionService processDefinitionService,
            ProcessExecutor processExecutor, BPMWorkFactory workFactory,
            FlowNodeStateManager flowNodeStateManager, UserTransactionService transactionService,
            @Qualifier("tenantTechnicalLoggerService") TechnicalLoggerService logger,
            ProcessInstanceService processInstanceService) {
        this.tenantId = tenantId;
        this.workService = workService;
        this.activityInstanceService = activityInstanceService;
        this.processDefinitionService = processDefinitionService;
        this.processExecutor = processExecutor;
        this.workFactory = workFactory;
        this.flowNodeStateManager = flowNodeStateManager;
        this.transactionService = transactionService;
        this.logger = logger;
        this.processInstanceService = processInstanceService;
    }

    @Override
    public void beforeServicesStart() throws RestartException {
        logInfo(logger, "Start detecting processes to restart on tenant " + tenantId + "...");
        final List<Long> ids = getAllProcessInstancesToRestart(processInstanceService);
        logInfo(logger, "Found " + ids.size() + " process to restart on tenant " + tenantId);
        processInstancesByTenant.put(tenantId, ids);
    }

    @Override
    public void afterServicesStart() {
        // get all process in initializing
        // call executeConnectors ON_ENTER on them (only case they can be in initializing)
        // get all process in completing
        // call executeConnectors ON_FINISH on them (only case they can be in completing)
        // also completed, aborted, cancelled

        final List<Long> list = processInstancesByTenant.get(tenantId);
        final Iterator<Long> iterator = list.iterator();
        logInfo(logger, "Attempting to restart " + list.size() + " processes for tenant " + tenantId);
        final ExecuteProcesses exec = new ExecuteProcesses(workService, logger, activityInstanceService,
                processDefinitionService,
                processInstanceService, processExecutor,
                flowNodeStateManager, workFactory, iterator);
        do {
            try {
                transactionService.executeInTransaction(exec);
            } catch (Exception e) {
                logger.log(getClass(), TechnicalLogSeverity.ERROR,
                        "Some processes failed to recover, they might seem stuck, a server restart is required to unlock all stuck process instances.",
                        e);
            }
        } while (iterator.hasNext());
        logInfo(logger, "All processes to be restarted on tenant " + tenantId + " have been handled");
    }

    protected void handleCompletion(final SProcessInstance processInstance, final TechnicalLoggerService logger,
            final ActivityInstanceService activityInstanceService, final WorkService workService,
            FlowNodeStateManager flowNodeStateManager, BPMWorkFactory workFactory)
            throws SBonitaException {
        // Only Error events set interruptedByEvent on SProcessInstance:
        if (!processInstance.hasBeenInterruptedByEvent()) {

            final long callerId = processInstance.getCallerId();
            // Should always be in a CallActivity:
            if (callerId > 0) {
                final SActivityInstance callActivityInstance = activityInstanceService
                        .getActivityInstance(processInstance.getCallerId());
                if (callActivityInstance.getStateId() != flowNodeStateManager.getFailedState().getId()) {
                    workService.registerWork(workFactory.createExecuteFlowNodeWorkDescriptor(callActivityInstance));
                    logInfo(logger,
                            "Restarting notification of finished process '" + processInstance.getName() + "' with id "
                                    + processInstance.getId()
                                    + " in state " + getState(processInstance.getStateId()));
                }
            }
        }
    }

    public List<Long> getAllProcessInstancesToRestart(ProcessInstanceService processInstanceService)
            throws RestartException {
        final List<Long> ids = new ArrayList<Long>();
        QueryOptions queryOptions = new QueryOptions(0, 1000, SProcessInstance.class, "id", OrderByType.ASC);
        try {
            List<SProcessInstance> processInstances;
            do {
                processInstances = processInstanceService.getProcessInstancesInStates(queryOptions,
                        ProcessInstanceState.INITIALIZING,
                        ProcessInstanceState.COMPLETING, ProcessInstanceState.COMPLETED,
                        ProcessInstanceState.ABORTED, ProcessInstanceState.CANCELLED);
                queryOptions = QueryOptions.getNextPage(queryOptions);
                for (final SProcessInstance sProcessInstance : processInstances) {
                    ids.add(sProcessInstance.getId());
                }
            } while (processInstances.size() == queryOptions.getNumberOfResults());
        } catch (final SProcessInstanceReadException e) {
            handleException(e, "Unable to restart process: can't read process instances");
        }
        return ids;
    }

    @VisibleForTesting
    void setProcessInstancesByTenant(Map<Long, List<Long>> processInstancesByTenant) {
        this.processInstancesByTenant = processInstancesByTenant;
    }

    protected void logInfo(final TechnicalLoggerService logger, final String msg) {
        if (logger.isLoggable(RestartProcessHandler.class, TechnicalLogSeverity.INFO)) {
            logger.log(RestartProcessHandler.class, TechnicalLogSeverity.INFO, msg);
        }
    }

    private void handleException(final Exception e, final String message) throws RestartException {
        throw new RestartException(message, e);
    }

    private ProcessInstanceState getState(final int stateId) {
        return ProcessInstanceState.getFromId(stateId);
    }

    private void restartConnector(final SProcessDefinition processDefinition, final SProcessInstance processInstance,
            final ConnectorEvent event,
            final ProcessExecutor processExecutor) throws SBonitaException {
        processExecutor.executeConnectors(processDefinition, processInstance, event, null);
    }

    public class ExecuteProcesses implements Callable<Object> {

        private final WorkService workService;

        private final TechnicalLoggerService logger;

        private final ActivityInstanceService activityInstanceService;

        private final ProcessDefinitionService processDefinitionService;

        private final ProcessInstanceService processInstanceService;

        private final ProcessExecutor processExecutor;

        private final FlowNodeStateManager flowNodeStateManager;
        private final BPMWorkFactory workFactory;
        private final Iterator<Long> iterator;

        public ExecuteProcesses(final WorkService workService, final TechnicalLoggerService logger,
                final ActivityInstanceService activityInstanceService,
                final ProcessDefinitionService processDefinitionService,
                final ProcessInstanceService processInstanceService,
                final ProcessExecutor processExecutor, FlowNodeStateManager flowNodeStateManager,
                BPMWorkFactory workFactory, final Iterator<Long> iterator) {
            this.workService = workService;
            this.logger = logger;
            this.activityInstanceService = activityInstanceService;
            this.processDefinitionService = processDefinitionService;
            this.processInstanceService = processInstanceService;
            this.processExecutor = processExecutor;
            this.flowNodeStateManager = flowNodeStateManager;
            this.workFactory = workFactory;
            this.iterator = iterator;
        }

        @Override
        public Object call() throws Exception {
            for (int i = 0; i < 20 && iterator.hasNext(); i++) {
                final Long processId = iterator.next();
                try {
                    final SProcessInstance processInstance = processInstanceService.getProcessInstance(processId);
                    final SProcessDefinition processDefinition = processDefinitionService
                            .getProcessDefinition(processInstance.getProcessDefinitionId());
                    final ProcessInstanceState state = getState(processInstance.getStateId());
                    switch (state) {
                        case ABORTED:
                        case CANCELLED:
                        case COMPLETED:
                            handleCompletion(processInstance, logger, activityInstanceService, workService,
                                    flowNodeStateManager, workFactory);
                            break;
                        case COMPLETING:
                            restartConnector(processDefinition, processInstance, ConnectorEvent.ON_FINISH,
                                    processExecutor);
                            break;
                        case INITIALIZING:
                            restartConnector(processDefinition, processInstance, ConnectorEvent.ON_ENTER,
                                    processExecutor);
                            break;
                        default:
                            break;
                    }
                } catch (final SProcessInstanceNotFoundException e) {
                    logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Unable to restart the process instance "
                            + processId + ", it is not found (already completed).");
                } catch (final Exception e) {
                    logger.log(getClass(), TechnicalLogSeverity.ERROR, "Unable to restart the process instance "
                            + processId + ", a server restart is required to unlock all stuck process instances.", e);
                }
            }
            return null;
        }
    }
}
