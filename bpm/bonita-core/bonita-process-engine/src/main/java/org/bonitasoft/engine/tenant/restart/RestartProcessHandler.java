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

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
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

    public class ExecuteProcesses implements Callable<Object> {

        private final WorkService workService;

        private final TechnicalLoggerService logger;

        private final ActivityInstanceService activityInstanceService;

        private final ProcessDefinitionService processDefinitionService;

        private final ProcessInstanceService processInstanceService;

        private final ProcessExecutor processExecutor;

        private final FlowNodeStateManager flowNodeStateManager;
        private BPMWorkFactory workFactory;
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
                            handleCompletion(processInstance, logger, activityInstanceService, workService,
                                    flowNodeStateManager, workFactory);
                            break;
                        case CANCELLED:
                            handleCompletion(processInstance, logger, activityInstanceService, workService,
                                    flowNodeStateManager, workFactory);
                            break;
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
                } catch (final SBonitaException e) {
                    throw new RestartException("Unable to restart the process " + processId, e);
                }
            }
            return null;
        }
    }

    private Long tenantId;
    private WorkService workService;
    private ActivityInstanceService activityInstanceService;
    private ProcessDefinitionService processDefinitionService;
    private ProcessExecutor processExecutor;
    private BPMWorkFactory workFactory;

    private FlowNodeStateManager flowNodeStateManager;

    private UserTransactionService transactionService;

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

    private TechnicalLoggerService logger;
    private ProcessInstanceService processInstanceService;

    //the handler is executed on one tenant only but we keep a map by tenant because this class is a singleton
    //It should not be a singleton but have a factory to create it
    private final Map<Long, List<Long>> processInstancesByTenant = new HashMap<Long, List<Long>>();

    @Override
    public void beforeServicesStart()
            throws RestartException {

        final List<Long> ids = new ArrayList<Long>();
        processInstancesByTenant.put(tenantId, ids);
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
    public void afterServicesStart()
            throws RestartException {
        // get all process in initializing
        // call executeConnectors on enter on them (only case they can be in initializing)
        // get all process in completing
        // call executeConnectors on finish on them (only case they can be in completing)

        final List<Long> list = processInstancesByTenant.get(tenantId);
        final Iterator<Long> iterator = list.iterator();
        logger.log(getClass(), TechnicalLogSeverity.INFO,
                "Restarting " + list.size() + " processes for tenant " + tenantId);
        ExecuteProcesses exec;
        try {
            do {
                exec = new ExecuteProcesses(workService, logger, activityInstanceService, processDefinitionService,
                        processInstanceService, processExecutor,
                        flowNodeStateManager, workFactory, iterator);
                transactionService.executeInTransaction(exec);
            } while (iterator.hasNext());
        } catch (final Exception e) {
            throw new RestartException("Unable to restart process instance", e);
        }

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

    private void restartConnector(final SProcessDefinition processDefinition, final SProcessInstance processInstance,
            final ConnectorEvent event,
            final ProcessExecutor processExecutor) throws SBonitaException {
        processExecutor.executeConnectors(processDefinition, processInstance, event, null);
    }

}
