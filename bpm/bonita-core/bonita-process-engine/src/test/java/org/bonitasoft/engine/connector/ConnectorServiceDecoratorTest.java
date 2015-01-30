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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.core.connector.ConnectorResult;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.expression.EngineConstantExpressionBuilder;
import org.bonitasoft.engine.expression.model.SExpression;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectorServiceDecoratorTest {

    @Mock
    private ConnectorService connectorService;

    @InjectMocks
    private ConnectorServiceDecorator connectorServiceDecorator;

    @Test
    public final void checkIfEngineExecutionContextIsPutOnParametersWhenExecuteMutipleEvaluation() throws Exception {
        final long processDefinitionId = 6L;
        final String connectorDefinitionId = "connectorDefinitionId";
        final String connectorDefinitionVersion = "connectorDefinitionVersion";
        final Map<String, SExpression> connectorInputParameters = Collections.emptyMap();
        final Map<String, Map<String, Serializable>> inputValues = null;
        final ClassLoader classLoader = null;
        final SExpressionContext sexpContext = null;
        final Map<String, SExpression> parameters = new HashMap<String, SExpression>(connectorInputParameters);
        parameters.put("connectorApiAccessor", EngineConstantExpressionBuilder.getConnectorAPIAccessorExpression());
        parameters.put("engineExecutionContext", EngineConstantExpressionBuilder.getEngineExecutionContext());

        final Map<String, Object> result = new HashMap<String, Object>(connectorInputParameters);
        parameters.put("connectorApiAccessor", EngineConstantExpressionBuilder.getConnectorAPIAccessorExpression());
        parameters.put("engineExecutionContext", EngineConstantExpressionBuilder.getEngineExecutionContext());
        final ConnectorResult toBeReturned = new ConnectorResult(null, result);
        doReturn(toBeReturned).when(connectorService).executeMutipleEvaluation(processDefinitionId, connectorDefinitionId,
                connectorDefinitionVersion, parameters, inputValues, classLoader, sexpContext);

        final ConnectorResult connectorResult = connectorServiceDecorator.executeMutipleEvaluation(processDefinitionId, connectorDefinitionId,
                connectorDefinitionVersion, connectorInputParameters, inputValues, classLoader, sexpContext);
        assertEquals(toBeReturned, connectorResult);
    }

    @Test
    public final void checkIfEngineExecutionContextIsPutOnParametersWhenEvaluateInputParameters() throws Exception {
        final Map<String, SExpression> parameters = Collections.emptyMap();
        final SExpressionContext sExpressionContext = null;
        final Map<String, Map<String, Serializable>> inputValues = Collections.emptyMap();
        final Map<String, SExpression> newParameters = new HashMap<String, SExpression>(parameters);
        newParameters.put("connectorApiAccessor", EngineConstantExpressionBuilder.getConnectorAPIAccessorExpression());
        newParameters.put("engineExecutionContext", EngineConstantExpressionBuilder.getEngineExecutionContext());

        final Map<String, Object> toBeReturned = new HashMap<String, Object>(parameters);
        toBeReturned.put("connectorApiAccessor", EngineConstantExpressionBuilder.getConnectorAPIAccessorExpression());
        toBeReturned.put("engineExecutionContext", EngineConstantExpressionBuilder.getEngineExecutionContext());
        doReturn(toBeReturned).when(connectorService).evaluateInputParameters("connectorId", newParameters, sExpressionContext, inputValues);

        final Map<String, Object> evaluateInputParameters = connectorServiceDecorator.evaluateInputParameters("connectorId", parameters, sExpressionContext,
                inputValues);
        assertEquals(toBeReturned, evaluateInputParameters);
    }

}
