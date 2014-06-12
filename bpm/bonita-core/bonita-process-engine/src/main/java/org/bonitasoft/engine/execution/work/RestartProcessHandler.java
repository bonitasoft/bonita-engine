/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.execution.work;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.execution.ProcessExecutor;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.work.WorkService;

/**
 * Restart handler for work {@link ExecuteConnectorOfProcess}
 * 
 * @author Baptiste Mesta
 */
public class RestartProcessHandler implements TenantRestartHandler {

    public class ExecuteProcesses implements Callable<Object> {

        private final WorkService workService;
        private final TechnicalLoggerService logger;
        private final ActivityInstanceService activityInstanceService;
        private final ProcessDefinitionService processDefinitionService;
        private final ProcessInstanceService processInstanceService;
        private final ProcessExecutor processExecutor;
        private final Iterator<Long> iterator;

        public ExecuteProcesses(final WorkService workService, final TechnicalLoggerService logger, final ActivityInstanceService activityInstanceService,
                final ProcessDefinitionService processDefinitionService, final ProcessInstanceService processInstanceService,
                final ProcessExecutor processExecutor, final Iterator<Long> iterator) {
            this.workService = workService;
            this.logger = logger;
            this.activityInstanceService = activityInstanceService;
            this.processDefinitionService = processDefinitionService;
            this.processInstanceService = processInstanceService;
            this.processExecutor = processExecutor;
            this.iterator = iterator;
        }

        @Override
        public Object call() throws Exception {

            for (int i = 0; i < QueryOptions.DEFAULT_NUMBER_OF_RESULTS && iterator.hasNext(); i++) {
                Long processId = iterator.next();
                try {
                    SProcessInstance processInstance = processInstanceService.getProcessInstance(processId);
                    SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(processInstance.getProcessDefinitionId());
                    ProcessInstanceState state = getState(processInstance.getStateId());
                    switch (state) {
                        case ABORTED:
                            handleCompletion(processInstance, state, logger, activityInstanceService, workService);
                            break;
                        case CANCELLED:
                            handleCompletion(processInstance, state, logger, activityInstanceService, workService);
                            break;
                        case COMPLETED:
                            handleCompletion(processInstance, state, logger, activityInstanceService, workService);
                            break;
                        case COMPLETING:
                            restartConnector(processDefinition, processInstance, ConnectorEvent.ON_FINISH, processExecutor);
                            break;
                        case INITIALIZING:
                            restartConnector(processDefinition, processInstance, ConnectorEvent.ON_ENTER, processExecutor);
                            break;
                        default:
                            break;
                    }
                } catch (SBonitaException e) {
                    throw new RestartException("Unable to restart the process " + processId, e);
                }
            }
            return null;
        }

    }

    private final Map<Long, List<Long>> processInstancesByTenant = new HashMap<Long, List<Long>>();

    @Override
    public void beforeServicesStart(final PlatformServiceAccessor platformServiceAccessor, final TenantServiceAccessor tenantServiceAccessor)
            throws RestartException {
        QueryOptions queryOptions = null;
        List<SProcessInstance> processInstances;
        final ProcessInstanceService processInstanceService = tenantServiceAccessor.getProcessInstanceService();
        final TechnicalLoggerService logger = tenantServiceAccessor.getTechnicalLoggerService();
        long tenantId = tenantServiceAccessor.getTenantId();

        List<Long> ids = new ArrayList<Long>();
        processInstancesByTenant.put(tenantId, ids);
        queryOptions = new QueryOptions(0, 1000, null);
        try {
            do {
                processInstances = processInstanceService.getProcessInstancesInStates(queryOptions, ProcessInstanceState.INITIALIZING,
                        ProcessInstanceState.COMPLETING, ProcessInstanceState.COMPLETED,
                        ProcessInstanceState.ABORTED, ProcessInstanceState.CANCELLED);
                queryOptions = QueryOptions.getNextPage(queryOptions);
                for (SProcessInstance sProcessInstance : processInstances) {
                    ids.add(sProcessInstance.getId());
                }
            } while (processInstances.size() == queryOptions.getNumberOfResults());
            logInfo(logger, "Found " + ids.size() + " process to restart on tenant " + tenantId);

        } catch (final SProcessInstanceReadException e) {
            handleException(e, "Unable to restart process: can't read process instances");
        }

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

    @Override
    public void afterServicesStart(final PlatformServiceAccessor platformServiceAccessor, final TenantServiceAccessor tenantServiceAccessor)
            throws RestartException {
        // get all process in initializing
        // call executeConnectors on enter on them (only case they can be in initializing)
        // get all process in completing
        // call executeConnectors on finish on them (only case they can be in completing)
        TransactionService transactionService = platformServiceAccessor.getTransactionService();
        long tenantId = tenantServiceAccessor.getTenantId();
        ProcessDefinitionService processDefinitionService = tenantServiceAccessor.getProcessDefinitionService();
        ProcessInstanceService processInstanceService = tenantServiceAccessor.getProcessInstanceService();
        ProcessExecutor processExecutor = tenantServiceAccessor.getProcessExecutor();
        TechnicalLoggerService logger = tenantServiceAccessor.getTechnicalLoggerService();
        ActivityInstanceService activityInstanceService = tenantServiceAccessor.getActivityInstanceService();
        WorkService workService = tenantServiceAccessor.getWorkService();

        Iterator<Long> iterator = processInstancesByTenant.get(tenantId).iterator();
        ExecuteProcesses exec = null;
        try {
            do {
                exec = new ExecuteProcesses(workService, logger, activityInstanceService, processDefinitionService, processInstanceService, processExecutor,
                        iterator);
                transactionService.executeInTransaction(exec);
            } while (iterator.hasNext());
        } catch (Exception e) {
            throw new RestartException("Unable to restart process instance", e);
        }

    }

    /**
     * @param processInstance
     * @param state
     * @param logger
     * @param activityInstanceService
     * @param workService
     * @throws SBonitaException
     * @throws SActivityReadException
     */
    private void handleCompletion(final SProcessInstance processInstance, final ProcessInstanceState state, final TechnicalLoggerService logger,
            final ActivityInstanceService activityInstanceService, final WorkService workService) throws SActivityReadException, SBonitaException {
        logInfo(logger, "Restarting notification of finished process '" + processInstance.getName()
                + "' with id " + processInstance.getId() + " in state " + state);
        // Only Error events set interruptedByEvent on SProcessInstance:
        if (!processInstance.hasBeenInterruptedByEvent()) {

            final long callerId = processInstance.getCallerId();
            // Should always be in a CallActivity:
            if (callerId > 0) {
                final SActivityInstance callActivityInstance = activityInstanceService.getActivityInstance(processInstance.getCallerId());
                workService.registerWork(WorkFactory.createExecuteFlowNodeWork(callActivityInstance.getProcessDefinitionId(),
                        callActivityInstance.getParentProcessInstanceId(), callActivityInstance.getId(), null, null));
            }
        }
    }

    /**
     * @param processDefinition
     * @param processInstance
     * @param processExecutor
     * @param onFinish
     * @throws SBonitaException
     */
    private void restartConnector(final SProcessDefinition processDefinition, final SProcessInstance processInstance, final ConnectorEvent event,
            final ProcessExecutor processExecutor) throws SBonitaException {
        processExecutor.executeConnectors(processDefinition, processInstance, event);

    }

}
