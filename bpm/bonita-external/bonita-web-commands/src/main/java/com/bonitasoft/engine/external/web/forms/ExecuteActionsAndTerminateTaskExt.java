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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.connector.ConnectorDefinition;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinitionWithInputValues;
import org.bonitasoft.engine.bpm.connector.InvalidEvaluationConnectorConditionException;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.connector.ConnectorResult;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilders;
import org.bonitasoft.engine.external.web.forms.ExecuteActionsAndTerminateTask;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.operation.Operation;
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
        final List<Operation> operationsList = getOperations(parameters);
        final Map<String, Serializable> operationsContext = getOperationsContext(parameters);
        final long sActivityInstanceID = getActivityInstanceId(parameters);

        final String message = "Mandatory parameter " + CONNECTORS_LIST_KEY + " is missing or not convertible to List.";
        final List<ConnectorDefinitionWithInputValues> connectorsList = getParameter(parameters, CONNECTORS_LIST_KEY, message);

        final TechnicalLoggerService logger = serviceAccessor.getTechnicalLoggerService();
        final ClassLoaderService classLoaderService = serviceAccessor.getClassLoaderService();
        final ActivityInstanceService activityInstanceService = serviceAccessor.getActivityInstanceService();
        final ClassLoader processClassloader;
        final long processDefinitionID;
        try {
            final SFlowNodeInstance flowNodeInstance = activityInstanceService.getFlowNodeInstance(sActivityInstanceID);
            processDefinitionID = flowNodeInstance.getLogicalGroup(0);
            processClassloader = classLoaderService.getLocalClassLoader("process", processDefinitionID);
            // set the classloader and update activity instance variable
            final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(processClassloader);

                // TODO This should not be part of the transaction.
                executeConnectors(sActivityInstanceID, processDefinitionID, connectorsList);

                updateActivityInstanceVariables(operationsList, operationsContext, sActivityInstanceID, processDefinitionID);
            } finally {
                Thread.currentThread().setContextClassLoader(contextClassLoader);
            }
            executeActivity(flowNodeInstance, logger);
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

    private void executeConnectors(final long sActivityInstanceID, final long processDefinitionID, final List<ConnectorDefinitionWithInputValues> connectors)
            throws ActivityInstanceNotFoundException, ProcessInstanceNotFoundException, InvalidEvaluationConnectorConditionException, SBonitaException {
        if (connectors == null) {
            return;
        }
        for (final ConnectorDefinitionWithInputValues connectorWithInputValues : connectors) {
            final ConnectorDefinition connectorDefinition = connectorWithInputValues.getConnectorDefinition();
            final Map<String, Map<String, Serializable>> contextMap = connectorWithInputValues.getInputValues();
            final Map<String, Expression> connectorInputParameters = connectorDefinition.getInputs();
            if (connectorInputParameters.size() != contextMap.size()) {
                throw new InvalidEvaluationConnectorConditionException(connectorInputParameters.size(), contextMap.size());
            }
            final TenantServiceAccessor tenantAccessor = getTenantAccessor();
            final long processDefinitionId = getProcessInstance(tenantAccessor, getActivityInstance(tenantAccessor, sActivityInstanceID).getRootContainerId())
                    .getProcessDefinitionId();
            final SExpressionBuilders sExpressionBuilders = tenantAccessor.getSExpressionBuilders();
            final Map<String, SExpression> connectorsExps = ModelConvertor.constructExpressions(sExpressionBuilders, connectorInputParameters);
            final String connectorDefinitionId = connectorDefinition.getConnectorId();
            final ConnectorService connectorService = tenantAccessor.getConnectorService();
            final SExpressionContext expcontext = new SExpressionContext();
            expcontext.setContainerId(sActivityInstanceID);
            expcontext.setContainerType(DataInstanceContainer.ACTIVITY_INSTANCE.name());
            expcontext.setProcessDefinitionId(processDefinitionID);
            final ConnectorResult result = connectorService.executeMutipleEvaluation(processDefinitionId, connectorDefinitionId,
                    connectorDefinition.getVersion(), connectorsExps, contextMap, Thread.currentThread().getContextClassLoader(), expcontext);
            final List<Operation> outputs = connectorDefinition.getOutputs();
            final ArrayList<SOperation> operations = new ArrayList<SOperation>(outputs.size());
            for (final Operation operation : outputs) {
                operations.add(ModelConvertor.constructSOperation(operation, tenantAccessor));
            }
            expcontext.setInputValues(result.getResult());
            connectorService.executeOutputOperation(operations, expcontext, result);
        }
    }

}
