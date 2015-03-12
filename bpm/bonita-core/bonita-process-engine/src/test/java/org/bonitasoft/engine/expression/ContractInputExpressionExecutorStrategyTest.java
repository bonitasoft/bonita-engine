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
package org.bonitasoft.engine.expression;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.core.contract.data.ContractDataService;
import org.bonitasoft.engine.core.contract.data.SContractDataNotFoundException;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ContractInputExpressionExecutorStrategyTest {

    @Mock
    private ContractDataService contractDataService;

    @InjectMocks
    private ContractInputExpressionExecutorStrategy strategy;

    private Map<String, Object> buildInitialContext(final long containerId) {
        final Map<String, Object> context = new HashMap<String, Object>(2);
        context.put(SExpressionContext.CONTAINER_ID_KEY, containerId);
        context.put(SExpressionContext.CONTAINER_TYPE_KEY, DataInstanceContainer.ACTIVITY_INSTANCE.name());
        return context;
    }

    private SExpression buildInputContractExpression(final String name, final Class<?> returnType) {
        return new SExpressionImpl(name, name, ExpressionExecutorStrategy.TYPE_CONTRACT_INPUT, returnType.getName(), null, null);
    }

    @Test
    public void evaluateShouldReturnTheContractInput() throws Exception {
        final Map<String, Object> inputs = new HashMap<String, Object>();
        inputs.put("comment", "No way!");
        final SExpression expression = buildInputContractExpression("comment", String.class);
        final Map<String, Object> context = buildInitialContext(465465L);
        when(contractDataService.getUserTaskDataValue(465465L, "comment")).thenReturn("No way!");

        final String result = (String) strategy.evaluate(expression, context, null, null);
        assertThat(result).isEqualTo("No way!");
    }

    @Test(expected = SExpressionEvaluationException.class)
    public void evaluateShouldThrowAnExceptionInputNotFound() throws Exception {
        final SExpression expression = buildInputContractExpression("comment", String.class);
        final Map<String, Object> context = buildInitialContext(465465L);
        when(contractDataService.getUserTaskDataValue(465465L, "comment")).thenThrow(new SContractDataNotFoundException("exception"));

        strategy.evaluate(expression, context, null, null);
    }

    @Test(expected = SExpressionEvaluationException.class)
    public void evaluateShouldThrowAnExceptionDuetoServiceFailure() throws Exception {
        final SExpression expression = buildInputContractExpression("comment", String.class);
        final Map<String, Object> context = buildInitialContext(465465L);
        when(contractDataService.getUserTaskDataValue(465465L, "comment")).thenThrow(new SBonitaReadException("exception"));

        strategy.evaluate(expression, context, null, null);
    }

    @Test
    public void getExpressionKindShouldReturnAContractInputKind() throws Exception {
        assertThat(strategy.getExpressionKind()).isEqualTo(ExpressionExecutorStrategy.KIND_CONTRACT_INPUT);
    }

    @Test
    public void validateShouldDoNothing() throws Exception {
        final SExpression expression = buildInputContractExpression("comment", String.class);
        strategy.validate(expression);

        verify(contractDataService, never()).getUserTaskDataValue(anyLong(), anyString());
    }

    @Test
    public void mustPutEvaluatedExpressionInContext() throws Exception {
        assertThat(strategy.mustPutEvaluatedExpressionInContext()).isTrue();
    }

    @Test
    public void evaluateShouldReturnTheContractInputs() throws Exception {
        final Map<String, Object> inputs = new HashMap<String, Object>();
        inputs.put("comment", "No way!");
        inputs.put("isValid", false);
        final SExpression expression1 = buildInputContractExpression("comment", String.class);
        final SExpression expression2 = buildInputContractExpression("isValid", Boolean.class);
        final List<SExpression> expressions = Arrays.asList(expression1, expression2);
        final Map<String, Object> context = buildInitialContext(465465L);
        when(contractDataService.getUserTaskDataValue(465465L, "comment")).thenReturn("No way!");
        when(contractDataService.getUserTaskDataValue(465465L, "isValid")).thenReturn(false);

        final List<Object> results = strategy.evaluate(expressions, context, null, null);
        assertThat(results).hasSize(2);
        assertThat(results.get(0)).isEqualTo("No way!");
        assertThat((Boolean) results.get(1)).isFalse();
    }

}
