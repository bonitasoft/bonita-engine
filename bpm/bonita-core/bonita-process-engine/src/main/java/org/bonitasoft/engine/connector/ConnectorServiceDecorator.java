/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
package org.bonitasoft.engine.connector;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.core.connector.ConnectorResult;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.connector.exception.SConnectorException;
import org.bonitasoft.engine.core.connector.exception.SInvalidConnectorImplementationException;
import org.bonitasoft.engine.core.connector.parser.SConnectorImplementationDescriptor;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.expression.EngineConstantExpressionBuilder;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SExpressionTypeUnknownException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.persistence.OrderByType;

/**
 * This service wraps the connector service and add engine variables like apiAccessor, engineExecutionContext.
 * 
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class ConnectorServiceDecorator implements ConnectorService {

    private final ConnectorService connectorService;

    public ConnectorServiceDecorator(final ConnectorService connectorService) {
        super();
        this.connectorService = connectorService;
    }

    @Override
    public ConnectorResult executeMutipleEvaluation(final long processDefinitionId, final String connectorDefinitionId,
            final String connectorDefinitionVersion, final Map<String, SExpression> connectorInputParameters,
            final Map<String, Map<String, Serializable>> inputValues, final ClassLoader classLoader, final SExpressionContext sexpContext)
            throws SConnectorException {
        final Map<String, SExpression> parameters = new HashMap<String, SExpression>(connectorInputParameters);
        parameters.put("connectorApiAccessor", EngineConstantExpressionBuilder.getConnectorAPIAccessorExpression());
        parameters.put("engineExecutionContext", EngineConstantExpressionBuilder.getEngineExecutionContext());
        return connectorService.executeMutipleEvaluation(processDefinitionId, connectorDefinitionId, connectorDefinitionVersion, parameters, inputValues,
                classLoader, sexpContext);
    }

    @Override
    public boolean loadConnectors(final SProcessDefinition sDefinition, final long tenantId) throws SConnectorException {
        return connectorService.loadConnectors(sDefinition, tenantId);
    }

    @Override
    public void setConnectorImplementation(final SProcessDefinition sProcessDefinition, final long tenantId, final String connectorId,
            final String connectorVersion, final byte[] connectorImplementationArchive) throws SConnectorException, SInvalidConnectorImplementationException {
        connectorService.setConnectorImplementation(sProcessDefinition, tenantId, connectorId, connectorVersion, connectorImplementationArchive);
    }

    @Override
    public List<SConnectorImplementationDescriptor> getConnectorImplementations(final long processDefinitionId, final long tenantId, final int fromIndex,
            final int numberPerPage, final String field, final OrderByType order) throws SConnectorException {
        return connectorService.getConnectorImplementations(processDefinitionId, tenantId, fromIndex, numberPerPage, field, order);
    }

    @Override
    public SConnectorImplementationDescriptor getConnectorImplementation(final long processDefinitionId, final String connectorId,
            final String connectorVersion, final long tenantId) throws SConnectorException {
        return connectorService.getConnectorImplementation(processDefinitionId, connectorId, connectorVersion, tenantId);
    }

    @Override
    public Map<String, Object> evaluateInputParameters(final String connectorId, final Map<String, SExpression> parameters,
            final SExpressionContext sExpressionContext,
            final Map<String, Map<String, Serializable>> inputValues) throws SExpressionTypeUnknownException, SExpressionEvaluationException,
            SExpressionDependencyMissingException, SInvalidExpressionException {
        SExpression apiAccessorExpression = EngineConstantExpressionBuilder.getConnectorAPIAccessorExpression();
        SExpression engineExecutionContext = EngineConstantExpressionBuilder.getEngineExecutionContext();
        final Map<String, SExpression> newParameters = new HashMap<String, SExpression>(parameters);
        newParameters.put("connectorApiAccessor", apiAccessorExpression);
        newParameters.put("engineExecutionContext", engineExecutionContext);
        return connectorService.evaluateInputParameters(connectorId, newParameters, sExpressionContext, inputValues);
    }

    @Override
    public void executeOutputOperation(final List<SOperation> outputs, final SExpressionContext expressionContext, final ConnectorResult connectorOutput)
            throws SConnectorException {
        connectorService.executeOutputOperation(outputs, expressionContext, connectorOutput);
    }

    @Override
    public ConnectorResult executeConnector(final long rootDefinitionId, final SConnectorInstance sConnectorInstance, final ClassLoader classLoader,
            final Map<String, Object> inputParameters) throws SConnectorException {
        return connectorService.executeConnector(rootDefinitionId, sConnectorInstance, classLoader, inputParameters);
    }

    @Override
    public void disconnect(final ConnectorResult result) throws SConnectorException {
        connectorService.disconnect(result);
    }

    @Override
    public Long getNumberOfConnectorImplementations(final long processDefinitionId, final long tenantId) throws SConnectorException {
        return connectorService.getNumberOfConnectorImplementations(processDefinitionId, tenantId);
    }
}
