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
package org.bonitasoft.engine.expression.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.expression.ContainerState;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.impl.condition.BinaryComparatorExecutor;
import org.bonitasoft.engine.expression.impl.condition.LogicalComplementExecutor;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class ConditionExpressionExecutorStrategyTest {

    @InjectMocks
    private ConditionExpressionExecutorStrategy strategy;

    private ArrayList<SExpression> dependFirstLtSecond;

    private ArrayList<SExpression> dependFirstGtSecond;

    private ArrayList<SExpression> dependEquals;

    private HashMap<Integer, Object> resolvedDependencies;

    @Mock
    private LogicalComplementExecutor logicalComplementExecutor;

    @Mock
    private BinaryComparatorExecutor binaryComparatorExecutor;

    @Before
    public void initialiseDependencies() {

        final SExpression constExpr1 = buildExpression("5", SExpression.TYPE_CONSTANT, Integer.class.getName(), null, null);
        final SExpression constExpr2 = buildExpression("7", SExpression.TYPE_CONSTANT, Integer.class.getName(), null, null);
        final SExpression constExpr3 = buildExpression("2", SExpression.TYPE_CONSTANT, Integer.class.getName(), null, null);

        dependFirstLtSecond = new ArrayList<SExpression>(2);
        dependFirstLtSecond.add(constExpr1);
        dependFirstLtSecond.add(constExpr2);

        dependFirstGtSecond = new ArrayList<SExpression>(2);
        dependFirstGtSecond.add(constExpr2);
        dependFirstGtSecond.add(constExpr1);

        dependEquals = new ArrayList<SExpression>(2);
        dependEquals.add(constExpr1);
        dependEquals.add(constExpr1);

        resolvedDependencies = new HashMap<Integer, Object>(2);
        resolvedDependencies.put(constExpr1.getDiscriminant(), 5);
        resolvedDependencies.put(constExpr2.getDiscriminant(), 7);
        resolvedDependencies.put(constExpr3.getDiscriminant(), 2);
    }

    private SExpression buildExpression(final String content, final String expressionType, final String returnType, final String interpreter,
            final List<SExpression> dependencies) {
        final SExpressionImpl eb = new SExpressionImpl();
        eb.setName(content);
        eb.setContent(content);
        eb.setExpressionType(expressionType);
        eb.setInterpreter(interpreter);
        eb.setReturnType(returnType);
        eb.setDependencies(dependencies);
        return eb;
    }

    protected List<Object> evaluate(final List<SExpression> expression, final Map<Integer, Object> resolvedExpressions, final ContainerState containerState)
            throws SExpressionEvaluationException {
        return strategy.evaluate(expression, new HashMap<String, Object>(0), resolvedExpressions, containerState);
    }

    @Test
    public void evaluate_lstOfExpressions_should_return_list_of_results() throws Exception {
        //given
        final SExpression expr1 = buildExpression("!=", SExpression.TYPE_CONDITION, Boolean.class.getName(), null, dependFirstGtSecond);
        final SExpression expr2 = buildExpression("!=", SExpression.TYPE_CONDITION, Boolean.class.getName(), null, dependFirstLtSecond);
        final SExpression expr3 = buildExpression("!=", SExpression.TYPE_CONDITION, Boolean.class.getName(), null, dependEquals);

        Map<String, Object> context = Collections.emptyMap();
        ConditionExpressionExecutorStrategy mockedStrategy = spy(strategy);
        given(mockedStrategy.evaluate(expr1, context, resolvedDependencies, ContainerState.ACTIVE)).willReturn(true);
        given(mockedStrategy.evaluate(expr2, context, resolvedDependencies, ContainerState.ACTIVE)).willReturn(false);
        given(mockedStrategy.evaluate(expr3, context, resolvedDependencies, ContainerState.ACTIVE)).willReturn(null);

        //when
        List<Object> resolvedExpressions = mockedStrategy.evaluate(Arrays.asList(expr1, expr2, expr3), context, resolvedDependencies, ContainerState.ACTIVE);

        //then
        assertThat(resolvedExpressions).containsExactly(true, false, null);
    }

    @Test
    public void evaluate_should_return_result_of_LogicalComplementExecutor_when_content_is_logical_complement_operator() throws Exception {
        //given
        Map<Integer, Object> resolvedExpressions = Collections.emptyMap();
        SExpression expression = mock(SExpression.class);
        given(expression.getContent()).willReturn(ConditionExpressionExecutorStrategy.LOGICAL_COMPLEMENT_OPERATOR);

        given(logicalComplementExecutor.evaluate(resolvedExpressions, expression)).willReturn(true);

        //when
        Object value = strategy.evaluate(expression, new HashMap<String, Object>(0), resolvedExpressions, ContainerState.ACTIVE);

        //then
        assertThat(value).isEqualTo(true);
    }

    @Test
    public void evaluate_should_return_result_of_BinaryComparatorExecutor_when_content_is_a_binary_operator() throws Exception {
        //given
        Map<Integer, Object> resolvedExpressions = Collections.emptyMap();
        SExpression expression = mock(SExpression.class);
        given(expression.getContent()).willReturn(ConditionExpressionExecutorStrategy.GREATER_THAN_COMPARATOR);

        given(binaryComparatorExecutor.evaluate(resolvedExpressions, expression)).willReturn(true);

        //when
        Object value = strategy.evaluate(expression, new HashMap<String, Object>(0), resolvedExpressions, ContainerState.ACTIVE);

        //then
        assertThat(value).isEqualTo(true);
    }

    @Test
    public void mustPutEvaluatedExpressionInContext_should_return_false() throws Exception {
        //given

        //when
        boolean value = strategy.mustPutEvaluatedExpressionInContext();

        //then
        assertThat(value).isFalse();
    }

    @Test
    public void getExpressionKind_should_return() throws Exception {
        //given

        //when
        ExpressionKind kind = strategy.getExpressionKind();

        //then
        assertThat(kind.getInterpreter()).isEqualTo(ExpressionKind.NONE);
        assertThat(kind.getType()).isEqualTo(SExpression.TYPE_CONDITION);
    }

    @Test(expected = SInvalidExpressionException.class)
    public void validate_should_throw_exception_if_expression_is_null() throws Exception {
        //given

        //when
        strategy.validate(null);

        //then exception
    }

    @Test(expected = SInvalidExpressionException.class)
    public void validate_should_throw_exception_if_expression_content_is_null() throws Exception {
        //given
        SExpression expression = mock(SExpression.class);
        given(expression.getContent()).willReturn(null);

        //when
        strategy.validate(expression);

        //then exception
    }

    @Test(expected = SInvalidExpressionException.class)
    public void validate_should_throw_exception_if_expression_content_is_an_invalid_operator() throws Exception {
        //given
        SExpression expression = mock(SExpression.class);
        given(expression.getContent()).willReturn("^");

        //when
        strategy.validate(expression);

        //then exception
    }

}
