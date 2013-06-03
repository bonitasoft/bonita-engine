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
package org.bonitasoft.engine.restart;

import java.util.List;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.execution.ProcessExecutor;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.transaction.STransactionException;
import org.bonitasoft.engine.work.WorkRegisterException;

/**
 * @author Baptiste Mesta
 */
public class RestartProcessHandler implements TenantRestartHandler {

    @Override
    public void handleRestart(final PlatformServiceAccessor platformServiceAccessor, final TenantServiceAccessor tenantServiceAccessor) throws RestartException {
        final TransactionExecutor transactionExecutor = tenantServiceAccessor.getTransactionExecutor();
        QueryOptions queryOptions = QueryOptions.defaultQueryOptions();
        List<SProcessInstance> processInstances;
        final ProcessExecutor processExecutor = tenantServiceAccessor.getProcessExecutor();
        final ProcessInstanceService processInstanceService = tenantServiceAccessor.getProcessInstanceService();
        final ProcessDefinitionService processDefinitionService = tenantServiceAccessor.getProcessDefinitionService();
        final ConnectorService connectorService = tenantServiceAccessor.getConnectorService();
        try {
            boolean txOpened = transactionExecutor.openTransaction();
            // get all process in initializing
            // call executeConnectors on enter on them (only case they can be in initializing)
            // get all process in completing
            // call executeConnectors on finish on them (only case they can be in completing)
            try {
                do {
                    processInstances = processInstanceService.getProcessInstancesInState(queryOptions, ProcessInstanceState.INITIALIZING);
                    queryOptions = QueryOptions.getNextPage(queryOptions);
                    for (final SProcessInstance processInstance : processInstances) {
                        final SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(processInstance.getProcessDefinitionId());
                        processExecutor.executeConnectors(processDefinition, processInstance, ConnectorEvent.ON_ENTER, connectorService);
                    }
                } while (processInstances.size() == queryOptions.getNumberOfResults());
                do {
                    processInstances = processInstanceService.getProcessInstancesInState(queryOptions, ProcessInstanceState.COMPLETING);
                    queryOptions = QueryOptions.getNextPage(queryOptions);
                    for (final SProcessInstance processInstance : processInstances) {
                        final SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(processInstance.getProcessDefinitionId());
                        processExecutor.executeConnectors(processDefinition, processInstance, ConnectorEvent.ON_ENTER, connectorService);
                    }
                } while (processInstances.size() == queryOptions.getNumberOfResults());
            } catch (final WorkRegisterException e) {
                handleException(transactionExecutor, e, "Unable to restart flowNodes: can't register work");
            } catch (final SProcessInstanceReadException e) {
                handleException(transactionExecutor, e, "Unable to restart flowNodes: can't read process instances");
            } catch (final SProcessDefinitionNotFoundException e) {
                handleException(transactionExecutor, e, "Unable to restart flowNodes: can't find process definition");
            } catch (final SProcessDefinitionReadException e) {
                handleException(transactionExecutor, e, "Unable to restart flowNodes: can't read process definition");
            } catch (final SBonitaException e) {
                handleException(transactionExecutor, e, "Unable to restart flowNodes: can't execute connectors");
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new RestartException("Unable to restart transitions: issue with transaction", e);
        }

    }

    private void handleException(final TransactionExecutor transactionExecutor, final Exception e, final String message) throws STransactionException,
            RestartException {
        transactionExecutor.setTransactionRollback();
        throw new RestartException(message, e);
    }
}
