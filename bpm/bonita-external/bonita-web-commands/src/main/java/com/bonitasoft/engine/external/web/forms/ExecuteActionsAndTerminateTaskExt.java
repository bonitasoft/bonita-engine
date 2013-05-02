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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.bpm.model.ConnectorDefinition;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.Operation;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.ClassLoaderException;
import org.bonitasoft.engine.exception.NotSerializableException;
import org.bonitasoft.engine.exception.activity.ActivityInstanceNotFoundException;
import org.bonitasoft.engine.exception.connector.ConnectorException;
import org.bonitasoft.engine.exception.connector.InvalidEvaluationConnectorConditionException;
import org.bonitasoft.engine.exception.platform.InvalidSessionException;
import org.bonitasoft.engine.exception.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilders;
import org.bonitasoft.engine.external.web.forms.ExecuteActionsAndTerminateTask;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Ruiheng Fan
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class ExecuteActionsAndTerminateTaskExt extends ExecuteActionsAndTerminateTask {

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        final Map<Operation, Map<String, Object>> operationsMap = getOperations(parameters);
        final long sActivityInstanceID = getActivityInstanceId(parameters);

        final String message = "Mandatory parameter " + CONNECTORS_MAP_KEY + " is missing or not convertible to Map.";
        final Map<ConnectorDefinition, Map<String, Map<String, Serializable>>> connectorsMap = getParameter(parameters, CONNECTORS_MAP_KEY, message);

        try {
            // get the classloader of process
            final ClassLoaderService classLoaderService = serviceAccessor.getClassLoaderService();
            final ActivityInstanceService activityInstanceService = serviceAccessor.getActivityInstanceService();
            final TransactionExecutor transactionExecutor = serviceAccessor.getTransactionExecutor();
            final boolean txOpened = transactionExecutor.openTransaction();
            final ClassLoader processClassloader;
            final long processDefinitionID;
            try {
                final SFlowNodeInstance flowNodeInstance = activityInstanceService.getFlowNodeInstance(sActivityInstanceID);
                processDefinitionID = flowNodeInstance.getLogicalGroup(0);
                processClassloader = classLoaderService.getLocalClassLoader("process", processDefinitionID);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
            // set the classloader and update activity instance variable
            final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(processClassloader);
                final Map<Operation, Map<String, Object>> connectorOutputs = executeConnectors(sActivityInstanceID, connectorsMap);
                operationsMap.putAll(connectorOutputs);
                updateActivityInstanceVariables(operationsMap, sActivityInstanceID, processDefinitionID);
            } finally {
                Thread.currentThread().setContextClassLoader(contextClassLoader);
            }
            executeActivity(sActivityInstanceID);
        } catch (final BonitaException e) {
            throw new SCommandExecutionException(
                    "Error executing command 'Map<String, Serializable> ExecuteActionsAndTerminate(Map<Operation, Map<String, Serializable>> operationsMap, long activityInstanceId)'",
                    e);
        } catch (final SBonitaException e) {
            throw new SCommandExecutionException(
                    "Error executing command 'Map<String, Serializable> ExecuteActionsAndTerminate(Map<Operation, Map<String, Serializable>> operationsMap, long activityInstanceId)'",
                    e);
        }
        return null;
    }

    private Map<Operation, Map<String, Object>> executeConnectors(final long sActivityInstanceID,
            final Map<ConnectorDefinition, Map<String, Map<String, Serializable>>> connectorsMap) throws InvalidSessionException,
            ActivityInstanceNotFoundException, ProcessInstanceNotFoundException, ClassLoaderException, ConnectorException,
            InvalidEvaluationConnectorConditionException, NotSerializableException {
        if (connectorsMap == null) {
            return Collections.emptyMap();
        } else {
            final Map<Operation, Map<String, Object>> operations = new HashMap<Operation, Map<String, Object>>(connectorsMap.size());
            for (final Entry<ConnectorDefinition, Map<String, Map<String, Serializable>>> entry : connectorsMap.entrySet()) {
                final ConnectorDefinition connectorDefinition = entry.getKey();
                final Map<String, Map<String, Serializable>> contextMap = entry.getValue();
                final Map<String, Object> resultMap = executeConnectorOnActivityInstance(connectorDefinition.getConnectorId(),
                        connectorDefinition.getVersion(), connectorDefinition.getInputs(), contextMap, sActivityInstanceID);
                for (final Operation operation : connectorDefinition.getOutputs()) {
                    operations.put(operation, resultMap);
                }
            }
            return operations;
        }
    }

    private Map<String, Object> executeConnectorOnActivityInstance(final String connectorDefinitionId, final String connectorDefinitionVersion,
            final Map<String, Expression> connectorInputParameters, final Map<String, Map<String, Serializable>> inputValues, final long activityInstanceId)
            throws InvalidSessionException, ActivityInstanceNotFoundException, ProcessInstanceNotFoundException, ClassLoaderException, ConnectorException,
            InvalidEvaluationConnectorConditionException, NotSerializableException {
        final String containerType = "ACTIVITY_INSTANCE";
        if (connectorInputParameters.size() == inputValues.size()) {
            final TenantServiceAccessor tenantAccessor = getTenantAccessor();
            final long processDefinitionId = getProcessInstance(tenantAccessor, getActivityInstance(tenantAccessor, activityInstanceId).getRootContainerId())
                    .getProcessDefinitionId();
            final ClassLoader clazzLoader = getLocalClassLoader(tenantAccessor, processDefinitionId);
            final SExpressionBuilders sExpressionBuilders = tenantAccessor.getSExpressionBuilders();
            final Map<String, SExpression> connectorsExps = ModelConvertor.constructExpressions(sExpressionBuilders, connectorInputParameters);
            final SExpressionContext expcontext = new SExpressionContext();
            expcontext.setContainerId(activityInstanceId);
            expcontext.setContainerType(containerType);
            expcontext.setProcessDefinitionId(processDefinitionId);
            return executeConnector(tenantAccessor, processDefinitionId, connectorDefinitionId, connectorDefinitionVersion, connectorsExps, inputValues,
                    clazzLoader, expcontext);
        } else {
            throw new InvalidEvaluationConnectorConditionException(connectorInputParameters.size(), inputValues.size());
        }
    }

}
