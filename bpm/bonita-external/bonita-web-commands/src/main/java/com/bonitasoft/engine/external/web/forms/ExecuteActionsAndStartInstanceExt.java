/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.external.web.forms;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.bpm.model.ActivationState;
import org.bonitasoft.engine.bpm.model.ConnectorDefinition;
import org.bonitasoft.engine.bpm.model.ProcessInstance;
import org.bonitasoft.engine.classloader.ClassLoaderService;
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
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.NotSerializableException;
import org.bonitasoft.engine.exception.OperationExecutionException;
import org.bonitasoft.engine.exception.connector.ConnectorException;
import org.bonitasoft.engine.exception.connector.InvalidEvaluationConnectorConditionException;
import org.bonitasoft.engine.exception.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.exception.process.ProcessDefinitionNotEnabledException;
import org.bonitasoft.engine.exception.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.exception.process.ProcessDefinitionReadException;
import org.bonitasoft.engine.execution.ProcessExecutor;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilders;
import org.bonitasoft.engine.external.web.forms.ExecuteActionsBaseEntry;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Ruiheng Fan
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class ExecuteActionsAndStartInstanceExt extends ExecuteActionsBaseEntry {

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        Map<Operation, Map<String, Object>> operationsMap = null;
        Map<ConnectorDefinition, Map<String, Map<String, Serializable>>> connectorsInputValues = null;
        try {
            operationsMap = (HashMap<Operation, Map<String, Object>>) parameters.get(OPERATIONS_MAP_KEY);
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

        long userId;
        try {
            userId = (Long) parameters.get(USER_ID_KEY);
        } catch (final Exception e) {
            throw new SCommandParameterizationException("Mandatory parameter " + USER_ID_KEY + " is missing or not convertible to String.", e);
        }

        try {
            final ClassLoader processClassloader;
            final ClassLoaderService classLoaderService = serviceAccessor.getClassLoaderService();
            final TransactionExecutor transactionExecutor = serviceAccessor.getTransactionExecutor();
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                processClassloader = classLoaderService.getLocalClassLoader("process", sProcessDefinitionID);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
            final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(processClassloader);
                final Map<Operation, Map<String, Object>> connectorOutputs = executeConnectors(sProcessDefinitionID, connectorsInputValues);
                operationsMap.putAll(connectorOutputs);
                return startProcess(userId, sProcessDefinitionID, operationsMap).getId();
            } finally {
                Thread.currentThread().setContextClassLoader(contextClassLoader);
            }
        } catch (final BonitaException e) {
            throw new SCommandExecutionException(
                    "Error executing command 'Map<String, Serializable> ExecuteActionsAndStartInstanceExt(Map<Operation, Map<String, Serializable>> operationsMap, long processDefinitionID)'",
                    e);
        } catch (final SBonitaException e) {
            throw new SCommandExecutionException(
                    "Error executing command 'Map<String, Serializable> ExecuteActionsAndStartInstanceExt(Map<Operation, Map<String, Serializable>> operationsMap, long processDefinitionID)'",
                    e);
        }
    }

    private Map<Operation, Map<String, Object>> executeConnectors(final long processDefId,
            final Map<ConnectorDefinition, Map<String, Map<String, Serializable>>> connectorsMap) throws ClassLoaderException, ConnectorException,
            InvalidEvaluationConnectorConditionException, InvalidProcessDefinitionException, NotSerializableException {
        final Map<Operation, Map<String, Object>> operations = new HashMap<Operation, Map<String, Object>>(connectorsMap.size());
        for (final Entry<ConnectorDefinition, Map<String, Map<String, Serializable>>> entry : connectorsMap.entrySet()) {
            final ConnectorDefinition connectorDefinition = entry.getKey();
            final Map<String, Map<String, Serializable>> contextInputValues = entry.getValue();
            final Map<String, Object> resultMap = executeConnectorOnProcessDefinition(connectorDefinition.getConnectorId(), connectorDefinition.getVersion(),
                    connectorDefinition.getInputs(), contextInputValues, processDefId);
            final List<Operation> outputs = connectorDefinition.getOutputs();
            for (final Operation operation : outputs) {
                operations.put(operation, resultMap);
            }
        }
        return operations;
    }

    private ProcessInstance startProcess(long userId, final long processDefinitionId, final Map<Operation, Map<String, Object>> operations)
            throws ProcessDefinitionNotFoundException, CreationException, ProcessDefinitionReadException, ProcessDefinitionNotEnabledException,
            OperationExecutionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final ProcessExecutor processExecutor = tenantAccessor.getProcessExecutor();
        final SOperationBuilders sOperationBuilders = tenantAccessor.getSOperationBuilders();
        final SExpressionBuilders sExpressionBuilders = tenantAccessor.getSExpressionBuilders();
        if (userId == 0) {
            userId = getUserIdFromSession();
        }
        // Retrieval of the process definition:
        SProcessDefinition sDefinition;
        try {
            final GetProcessDeploymentInfo transactionContentWithResult = new GetProcessDeploymentInfo(processDefinitionId, processDefinitionService);
            transactionExecutor.execute(transactionContentWithResult);
            final SProcessDefinitionDeployInfo deployInfo = transactionContentWithResult.getResult();
            if (ActivationState.DISABLED.name().equals(deployInfo.getActivationState())) {
                throw new ProcessDefinitionNotEnabledException(deployInfo.getName(), deployInfo.getVersion(), deployInfo.getProcessId());
            }
            sDefinition = getServerProcessDefinition(transactionExecutor, processDefinitionId, processDefinitionService);
        } catch (final SProcessDefinitionNotFoundException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SBonitaException e) {
            throw new ProcessDefinitionReadException(e);
        }
        SProcessInstance startedInstance;
        try {
            final Map<SOperation, Map<String, Object>> sOperations = toSOperation(operations, sOperationBuilders, sExpressionBuilders);
            startedInstance = processExecutor.start(userId, sDefinition, sOperations);
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new CreationException(e);
        }// FIXME in case process instance creation exception -> put it in failed
        return ModelConvertor.toProcessInstance(sDefinition, startedInstance);
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

    Map<String, Object> executeConnectorOnProcessDefinition(final String connectorDefinitionId, final String connectorDefinitionVersion,
            final Map<String, Expression> connectorInputParameters, final Map<String, Map<String, Serializable>> inputValues, final long processDefinitionId)
            throws ClassLoaderException, ConnectorException, InvalidEvaluationConnectorConditionException, InvalidProcessDefinitionException,
            NotSerializableException {
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
            throw new InvalidEvaluationConnectorConditionException(connectorInputParameters.size(), inputValues.size());
        }
    }

}
