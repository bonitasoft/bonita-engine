/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.external.web.forms;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.bpm.model.ConnectorDefinition;
import org.bonitasoft.engine.bpm.model.ProcessDefinitionStates;
import org.bonitasoft.engine.bpm.model.ProcessInstance;
import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.Operation;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.operation.model.builder.SOperationBuilders;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.ClassLoaderException;
import org.bonitasoft.engine.exception.ConnectorException;
import org.bonitasoft.engine.exception.InvalidEvaluationConnectorCondition;
import org.bonitasoft.engine.exception.InvalidProcessDefinitionException;
import org.bonitasoft.engine.exception.InvalidSessionException;
import org.bonitasoft.engine.exception.OperationExecutionException;
import org.bonitasoft.engine.exception.ProcessDefinitionNotEnabledException;
import org.bonitasoft.engine.exception.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.exception.ProcessDefinitionReadException;
import org.bonitasoft.engine.exception.ProcessInstanceCreationException;
import org.bonitasoft.engine.execution.ProcessExecutor;
import org.bonitasoft.engine.execution.state.ProcessInstanceStateManager;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilders;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Ruiheng Fan
 */
public class ExecuteActionsAndStartInstanceExt extends ExecuteActionsBaseEntry {

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        Map<Operation, Map<String, Serializable>> operationsMap = null;
        Map<ConnectorDefinition, Map<String, Map<String, Serializable>>> connectorsInputValues = null;
        try {
            operationsMap = (HashMap<Operation, Map<String, Serializable>>) parameters.get(OPERATIONS_MAP_KEY);
        } catch (final Exception e) {
            throw new SCommandParameterizationException("Mandatory parameter " + OPERATIONS_MAP_KEY + " is missing or not convertible to Map.", e);
        }

        try {
            connectorsInputValues = (Map<ConnectorDefinition, Map<String, Map<String, Serializable>>>) parameters.get(CONNECTORS_MAP_KEY);
        } catch (final Exception e) {
            throw new SCommandParameterizationException("Mandatory parameter " + CONNECTORS_MAP_KEY + " is missing or not convertible to Map.", e);
        }

        long sProcessDefinitionID = 0L;
        try {
            sProcessDefinitionID = (Long) parameters.get(PROCESS_DEFINITION_ID_KEY);
        } catch (final Exception e) {
            throw new SCommandParameterizationException("Mandatory parameter " + PROCESS_DEFINITION_ID_KEY + " is missing or not convertible to long.", e);
        }

        String userName = null;
        try {
            userName = (String) parameters.get(USER_NAME_KEY);
        } catch (final Exception e) {
            throw new SCommandParameterizationException("Mandatory parameter " + USER_NAME_KEY + " is missing or not convertible to String.", e);
        }

        try {
            executeConnectors(sProcessDefinitionID, connectorsInputValues, operationsMap);
            return startProcess(userName, sProcessDefinitionID, operationsMap).getId();
        } catch (final BonitaException e) {
            throw new SCommandExecutionException(
                    "Error executing command 'Map<String, Serializable> ExecuteActionsAndStartInstanceExt(Map<Operation, Map<String, Serializable>> operationsMap, long processDefinitionID)'",
                    e);
        }

    }

    /**
     * @param sProcessDefinitionID
     * @param connectorsMap
     * @param operationsMap
     * @throws InvalidProcessDefinitionException
     * @throws InvalidEvaluationConnectorCondition
     * @throws ConnectorException
     * @throws ClassLoaderException
     * @throws InvalidSessionException
     */
    private void executeConnectors(final long sProcessDefinitionID, final Map<ConnectorDefinition, Map<String, Map<String, Serializable>>> connectorsMap,
            final Map<Operation, Map<String, Serializable>> operationsMap) throws InvalidSessionException, ClassLoaderException, ConnectorException,
            InvalidEvaluationConnectorCondition, InvalidProcessDefinitionException {
        final Iterator<Entry<ConnectorDefinition, Map<String, Map<String, Serializable>>>> iterator = connectorsMap.entrySet().iterator();
        while (iterator.hasNext()) {
            final Entry<ConnectorDefinition, Map<String, Map<String, Serializable>>> entry = iterator.next();
            final ConnectorDefinition connectorDefinition = entry.getKey();
            final Map<String, Map<String, Serializable>> contextInputValues = entry.getValue();
            final Map<String, Serializable> resultMap = executeConnectorOnProcessDefinition(connectorDefinition.getConnectorId(),
                    connectorDefinition.getVersion(), connectorDefinition.getInputs(), contextInputValues, sProcessDefinitionID);
            for (final Operation operation : connectorDefinition.getOutputs()) {
                operationsMap.put(operation, resultMap);
            }
        }
    }

    private ProcessInstance startProcess(String userName, final long processDefinitionId, final Map<Operation, Map<String, Serializable>> operations)
            throws InvalidSessionException, ProcessDefinitionNotFoundException, ProcessInstanceCreationException, ProcessDefinitionReadException,
            ProcessDefinitionNotEnabledException, OperationExecutionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final ProcessExecutor processExecutor = tenantAccessor.getProcessExecutor();
        final ProcessInstanceStateManager processInstanceStateManager = tenantAccessor.getProcessInstanceStateManager();
        final SOperationBuilders sOperationBuilders = tenantAccessor.getSOperationBuilders();
        final SExpressionBuilders sExpressionBuilders = tenantAccessor.getSExpressionBuilders();
        if (userName == null || userName.isEmpty()) {
            userName = getUserFromSession();
        }
        // Retrieval of the process definition:
        SProcessDefinition sDefinition;
        try {
            final GetProcessDeploymentInfo transactionContentWithResult = new GetProcessDeploymentInfo(processDefinitionId, processDefinitionService);
            transactionExecutor.execute(transactionContentWithResult);
            final SProcessDefinitionDeployInfo deployInfo = transactionContentWithResult.getResult();
            if (!ProcessDefinitionStates.ENABLED.equals(deployInfo.getState())) {
                throw new ProcessDefinitionNotEnabledException(deployInfo.getName(), deployInfo.getVersion(), deployInfo.getProcessId());
            }
            sDefinition = getServerProcessDefinition(transactionExecutor, processDefinitionId, processDefinitionService);
        } catch (final SProcessDefinitionNotFoundException e) {
            log(tenantAccessor, e);
            throw new ProcessDefinitionNotFoundException(e.getMessage());
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ProcessDefinitionReadException(e.getMessage());
        }

        SProcessInstance startedInstance;
        try {
            final Map<SOperation, Map<String, Serializable>> sOperations = toSOperation(operations, sOperationBuilders, sExpressionBuilders);
            startedInstance = processExecutor.start(userName, sDefinition, sOperations);
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ProcessInstanceCreationException(e);
        }// FIXME in case process instance creation exception -> put it in failed
        return ModelConvertor.toProcessInstance(startedInstance, processInstanceStateManager);
    }

    /**
     * @author Baptiste Mesta
     */
    private final class GetProcessDeploymentInfo implements TransactionContentWithResult<SProcessDefinitionDeployInfo> {

        private final Long processDefinitionUUID;

        private final ProcessDefinitionService processDefinitionService;

        private SProcessDefinitionDeployInfo processDefinitionDI;

        private GetProcessDeploymentInfo(final Long processDefinitionUUID, final ProcessDefinitionService processDefinitionService) {
            this.processDefinitionUUID = processDefinitionUUID;
            this.processDefinitionService = processDefinitionService;
        }

        @Override
        public void execute() throws SProcessDefinitionNotFoundException, SProcessDefinitionReadException {
            processDefinitionDI = processDefinitionService.getProcessDefinitionDeployInfo(processDefinitionUUID);
        }

        @Override
        public SProcessDefinitionDeployInfo getResult() {
            return processDefinitionDI;
        }
    }

    Map<String, Serializable> executeConnectorOnProcessDefinition(final String connectorDefinitionId, final String connectorDefinitionVersion,
            final Map<String, Expression> connectorInputParameters, final Map<String, Map<String, Serializable>> inputValues, final long processDefinitionId)
            throws InvalidSessionException, ClassLoaderException, ConnectorException, InvalidEvaluationConnectorCondition, InvalidProcessDefinitionException {
        if (connectorInputParameters.size() == inputValues.size()) {
            final TenantServiceAccessor tenantAccessor = getTenantAccessor();
            final ClassLoader classLoader = getLocalClassLoader(tenantAccessor, processDefinitionId);
            final SExpressionBuilders sExpressionBuilders = tenantAccessor.getSExpressionBuilders();
            final Map<String, SExpression> connectorsExps = ModelConvertor.constructExpressions(sExpressionBuilders, connectorInputParameters);
            final SExpressionContext expcontext = new SExpressionContext();
            expcontext.setProcessDefinitionId(processDefinitionId);
            final SProcessDefinition processDef = getProcessDefinition(tenantAccessor, processDefinitionId);
            if (processDef != null) {
                expcontext.setProcessDefinition(processDef);
            }
            return executeConnector(tenantAccessor, processDefinitionId, connectorDefinitionId, connectorDefinitionVersion, connectorsExps, inputValues,
                    classLoader, expcontext);
        } else {
            throw new InvalidEvaluationConnectorCondition(connectorInputParameters.size(), inputValues.size());
        }
    }
}
