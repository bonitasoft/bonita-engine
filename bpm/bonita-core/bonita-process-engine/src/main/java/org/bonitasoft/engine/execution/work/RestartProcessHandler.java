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

import java.util.List;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.execution.ProcessExecutor;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.work.WorkService;

/**
 * Restart handler for work {@link ExecuteConnectorOfProcess}
 * 
 * @author Baptiste Mesta
 */
public class RestartProcessHandler implements TenantRestartHandler {

    @Override
    public void handleRestart(final PlatformServiceAccessor platformServiceAccessor, final TenantServiceAccessor tenantServiceAccessor) throws RestartException {
        QueryOptions queryOptions = null;
        List<SProcessInstance> processInstances;
        final ProcessExecutor processExecutor = tenantServiceAccessor.getProcessExecutor();
        final ProcessInstanceService processInstanceService = tenantServiceAccessor.getProcessInstanceService();
        final ActivityInstanceService activityInstanceService = tenantServiceAccessor.getActivityInstanceService();
        final WorkService workService = tenantServiceAccessor.getWorkService();
        final ProcessDefinitionService processDefinitionService = tenantServiceAccessor.getProcessDefinitionService();
        final TechnicalLoggerService logger = tenantServiceAccessor.getTechnicalLoggerService();
        // get all process in initializing
        // call executeConnectors on enter on them (only case they can be in initializing)
        // get all process in completing
        // call executeConnectors on finish on them (only case they can be in completing)
        final boolean isInfo = logger.isLoggable(getClass(), TechnicalLogSeverity.INFO);
        try {
            logInfo(logger, isInfo, "Restarting connectors of process...");
            queryOptions = new QueryOptions(0, 100, SProcessInstance.class, "id", OrderByType.ASC);
            do {
                processInstances = processInstanceService.getProcessInstancesInState(queryOptions, ProcessInstanceState.INITIALIZING);
                queryOptions = QueryOptions.getNextPage(queryOptions);
                for (final SProcessInstance processInstance : processInstances) {
                    final SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(processInstance.getProcessDefinitionId());
                    if (isInfo) {
                        logger.log(getClass(), TechnicalLogSeverity.INFO, "Executing 'on enter' connectors of process " + processInstance.getName());
                    }
                    processExecutor.executeConnectors(processDefinition, processInstance, ConnectorEvent.ON_ENTER);
                }
            } while (processInstances.size() == queryOptions.getNumberOfResults());

            queryOptions = new QueryOptions(0, 100, SProcessInstance.class, "id", OrderByType.ASC);
            do {
                processInstances = processInstanceService.getProcessInstancesInState(queryOptions, ProcessInstanceState.COMPLETING);
                queryOptions = QueryOptions.getNextPage(queryOptions);
                for (final SProcessInstance processInstance : processInstances) {
                    final SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(processInstance.getProcessDefinitionId());
                    if (isInfo) {
                        logger.log(getClass(), TechnicalLogSeverity.INFO, "Executing 'on finish' connectors of process " + processInstance.getName());
                    }
                    processExecutor.executeConnectors(processDefinition, processInstance, ConnectorEvent.ON_FINISH);
                }
            } while (processInstances.size() == queryOptions.getNumberOfResults());

            logInfo(logger, isInfo, "Restarting notification of finished sub-processes of Call-Activities...");
            queryOptions = new QueryOptions(0, 100, SProcessInstance.class, "id", OrderByType.ASC);
            do {
                processInstances = processInstanceService.getProcessInstancesInStates(queryOptions, ProcessInstanceState.COMPLETED,
                        ProcessInstanceState.ABORTED, ProcessInstanceState.CANCELLED);
                queryOptions = QueryOptions.getNextPage(queryOptions);
                for (final SProcessInstance processInstance : processInstances) {
                    ProcessInstanceState state = getState(processInstance.getStateId());
                    if (isInfo) {
                        logger.log(getClass(), TechnicalLogSeverity.INFO, "Restarting notification of finished process '" + processInstance.getName()
                                + "' with id " + processInstance.getId() + " in state " + state);
                    }
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
            } while (processInstances.size() == queryOptions.getNumberOfResults());

        } catch (final SProcessInstanceReadException e) {
            handleException(e, "Unable to restart process: can't read process instances");
        } catch (final SProcessDefinitionNotFoundException e) {
            handleException(e, "Unable to restart process: can't find process definition");
        } catch (final SProcessDefinitionReadException e) {
            handleException(e, "Unable to restart process: can't read process definition");
        } catch (final SBonitaException e) {
            handleException(e, "Unable to restart process: can't execute connectors");
        }

    }

    protected void logInfo(final TechnicalLoggerService logger, final boolean isInfo, final String msg) {
        if (isInfo) {
            logger.log(getClass(), TechnicalLogSeverity.INFO, msg);
        }
    }

    private void handleException(final Exception e, final String message) throws RestartException {
        throw new RestartException(message, e);
    }

    private ProcessInstanceState getState(final int stateId) {
        return ProcessInstanceState.getFromId(stateId);
    }

}
