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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.bpm.model.ConnectorDefinition;
import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.Operation;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.exception.ActivityInstanceNotFoundException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.ClassLoaderException;
import org.bonitasoft.engine.exception.ConnectorException;
import org.bonitasoft.engine.exception.InvalidEvaluationConnectorCondition;
import org.bonitasoft.engine.exception.InvalidSessionException;
import org.bonitasoft.engine.exception.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilders;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Ruiheng Fan
 */
public class ExecuteActionsAndTerminateTaskExt extends ExecuteActionsAndTerminateTask {

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        Map<Operation, Map<String, Serializable>> operationsMap = null;
        Map<ConnectorDefinition, Map<String, Map<String, Serializable>>> connectorsMap = null;
        try {
            operationsMap = (Map<Operation, Map<String, Serializable>>) parameters.get(OPERATIONS_MAP_KEY);
        } catch (final Exception e) {
            throw new SCommandParameterizationException("Mandatory parameter " + OPERATIONS_MAP_KEY + " is missing or not convertible to Map.", e);
        }

        try {
            connectorsMap = (Map<ConnectorDefinition, Map<String, Map<String, Serializable>>>) parameters.get(CONNECTORS_MAP_KEY);
        } catch (final Exception e) {
            throw new SCommandParameterizationException("Mandatory parameter " + CONNECTORS_MAP_KEY + " is missing or not convertible to Map.", e);
        }

        long sActivityInstanceID = 0L;
        try {
            sActivityInstanceID = (Long) parameters.get(ACTIVITY_INSTANCE_ID_KEY);
        } catch (final Exception e) {
            throw new SCommandParameterizationException("Mandatory parameter " + ACTIVITY_INSTANCE_ID_KEY + " is missing or not convertible to long.", e);
        }

        try {
            executeActivity(sActivityInstanceID);
            executeConnectors(sActivityInstanceID, connectorsMap, operationsMap);
            updateActivityInstanceVariables(operationsMap, sActivityInstanceID);
            executeActivity(sActivityInstanceID);
        } catch (final BonitaException e) {
            throw new SCommandExecutionException(
                    "Error executing command 'Map<String, Serializable> ExecuteActionsAndTerminate(Map<Operation, Map<String, Serializable>> operationsMap, long activityInstanceId)'",
                    e);
        }
        return null;
    }

    private void executeConnectors(final long sActivityInstanceID, final Map<ConnectorDefinition, Map<String, Map<String, Serializable>>> connectorsMap,
            final Map<Operation, Map<String, Serializable>> operationsMap) throws InvalidSessionException, ActivityInstanceNotFoundException,
            ProcessInstanceNotFoundException, ClassLoaderException, ConnectorException, InvalidEvaluationConnectorCondition {
        final Iterator<Entry<ConnectorDefinition, Map<String, Map<String, Serializable>>>> iterator = connectorsMap.entrySet().iterator();
        while (iterator.hasNext()) {
            final Entry<ConnectorDefinition, Map<String, Map<String, Serializable>>> entry = iterator.next();
            final ConnectorDefinition connectorDefinition = entry.getKey();
            final Map<String, Map<String, Serializable>> contextMap = entry.getValue();
            Map<String, Serializable> resultMap = null;
            resultMap = executeConnectorOnActivityInstance(connectorDefinition.getConnectorId(), connectorDefinition.getVersion(),
                    connectorDefinition.getInputs(), contextMap, sActivityInstanceID);
            for (final Operation operation : connectorDefinition.getOutputs()) {
                operationsMap.put(operation, resultMap);
            }
        }
    }

    private Map<String, Serializable> executeConnectorOnActivityInstance(final String connectorDefinitionId, final String connectorDefinitionVersion,
            final Map<String, Expression> connectorInputParameters, final Map<String, Map<String, Serializable>> inputValues, final long activityInstanceId)
            throws InvalidSessionException, ActivityInstanceNotFoundException, ProcessInstanceNotFoundException, ClassLoaderException, ConnectorException,
            InvalidEvaluationConnectorCondition {
        final String containerType = "ACTIVITY_INSTANCE";
        if (connectorInputParameters.size() == inputValues.size()) {
            final TenantServiceAccessor tenantAccessor = getTenantAccessor();
            final SActivityInstance activity = getActivityInstance(tenantAccessor, activityInstanceId);
            final SProcessInstance processInstance = getProcessInstance(tenantAccessor, activity.getRootContainerId());
            final long processDefinitionId = processInstance.getProcessDefinitionId();
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
            throw new InvalidEvaluationConnectorCondition(connectorInputParameters.size(), inputValues.size());
        }
    }

}
