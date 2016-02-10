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
package org.bonitasoft.engine.expression.impl.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BinaryComparatorExecutorTest {

    @Mock
    BinaryComparatorMapper mapper;

    @Mock
    BinaryComparator evaluator;

    @InjectMocks
    private BinaryComparatorExecutor manager;

    @Test
    public void evaluate_should_return_the_result_of_selected_evaluator() throws Exception {
        //given
        SExpression left = buildIntegerExpression(5);
        SExpression right = buildIntegerExpression(4);
        Map<Integer, Object> resolvedExpressions = new HashMap<Integer, Object>();
        resolvedExpressions.put(left.getDiscriminant(), 5);
        resolvedExpressions.put(right.getDiscriminant(), 4);

        SExpression comparisonExpression = buildComparisonExpression(left, right, ">");
        given(mapper.getEvaluator(">")).willReturn(evaluator);
        given(evaluator.evaluate(new BigDecimal(5), new BigDecimal(4))).willReturn(true);

        //when
        Boolean value = manager.evaluate(resolvedExpressions, comparisonExpression);

        //then
        assertThat(value).isTrue();
    }

    @Test
    public void evaluate_should_throw_SExpressionEvaluationException_when_no_evaluator_is_found() throws Exception {
        //given
        SExpression left = buildIntegerExpression(5);
        SExpression right = buildIntegerExpression(4);
        String operator = ">";
        SExpression comparisonExpression = buildComparisonExpression(left, right, operator);
        given(mapper.getEvaluator(operator)).willReturn(null);

        try {
            //when
            manager.evaluate(Collections.<Integer, Object> emptyMap(), comparisonExpression);
            fail("exception expected");
        } catch (SExpressionEvaluationException e) {
            //then
            assertThat(e.getMessage()).isEqualTo("Unable to find evaluator for operator '" + operator + "'");
        }

    }

    @Test
    public void evaluate_should_throw_SExpressionEvaluationException_when_number_there_are_not_exactly_2_dependencies() throws Exception {
        //given
        SExpression expression = mock(SExpression.class);
        String operator = ">";
        given(expression.getContent()).willReturn(operator);
        given(expression.getName()).willReturn(operator);
        given(expression.getDependencies()).willReturn(Arrays.asList(mock(SExpression.class), mock(SExpression.class), mock(SExpression.class)));

        try {
            //when
            manager.evaluate(Collections.<Integer, Object> emptyMap(), expression);
            fail("exception expected");
        } catch (SExpressionEvaluationException e) {
            //then
            assertThat(e.getMessage()).isEqualTo("The expression '" + expression.getContent() + "' has " + expression.getDependencies().size()
                    + " dependencies, but it must have exactly " + 2 + " dependencies.");
        }

    }

    @Test
    public void evaluate_should_throw_SExpressionEvaluationException_when_return_types_are_incompatible() throws Exception {
        //given

        SExpression left = buildIntegerExpression(5);
        SExpression right = buildBooleanExpression(true);
        String operator = ">";
        SExpression comparisonExpression = buildComparisonExpression(left, right, operator);

        try {
            //when
            manager.evaluate(Collections.<Integer, Object> emptyMap(), comparisonExpression);
            fail("exception expected");
        } catch (SExpressionEvaluationException e) {
            //then
            assertThat(e.getMessage()).isEqualTo("The two dependencies of expression '" + operator + "' must have the same return type.");
        }

    }

    @Test
    public void evaluate_should_throw_SExpressionEvaluationException_when_evaluator_throws_SComparisonException() throws Exception {
        //given
        String operator = ">";
        given(mapper.getEvaluator(operator)).willReturn(evaluator);
        given(evaluator.evaluate(new BigDecimal(5), new BigDecimal(5))).willThrow(new SComparisonException(""));

        SExpression left = buildIntegerExpression(5);
        SExpression right = buildIntegerExpression(5);

        Map<Integer, Object> resolvedExpressions = new HashMap<Integer, Object>();
        resolvedExpressions.put(left.getDiscriminant(), 5);
        resolvedExpressions.put(right.getDiscriminant(), 5);
        SExpression comparisonExpression = buildComparisonExpression(left, right, operator);

        try {
            //when
            manager.evaluate(resolvedExpressions, comparisonExpression);
            fail("exception expected");
        } catch (SExpressionEvaluationException e) {
            //then
            assertThat(e.getMessage()).isEqualTo("Unable to evaluate expression '" + comparisonExpression.getName() + "'");
        }

    }

    @Test
    public void areCompatible_should_return_true_when_two_parameters_has_the_same_return_type() throws Exception {
        //given

        //when
        boolean value = manager.areReturnTypeCompatible(String.class.getName(), String.class.getName(), ">");

        //then
        assertThat(value).isTrue();
    }

    @Test
    public void areCompatible_should_return_false_when_two_parameters_has_different_return_types() throws Exception {
        //given

        //when
        boolean value = manager.areReturnTypeCompatible(String.class.getName(), Integer.class.getName(), ">");

        //then
        assertThat(value).isFalse();
    }

    @Test
    public void areCompatible_should_return_true_when_two_parameters_has_different_return_types_but_operator_is_equals_to() throws Exception {
        //given

        //when
        boolean value = manager.areReturnTypeCompatible(String.class.getName(), Integer.class.getName(), "==");

        //then
        assertThat(value).isTrue();
    }

    @Test
    public void areCompatible_should_return_true_when_two_parameters_has_different_return_types_but_both_are_numeric() throws Exception {
        //given

        //when
        boolean value = manager.areReturnTypeCompatible(Long.class.getName(), Integer.class.getName(), ">");

        //then
        assertThat(value).isTrue();
    }

    @Test
    public void transtype_integer_should_return_bigDecimal() throws Exception {
        //given

        //when
        Object value = manager.transtypeIfApplicable(Integer.class.getName(), 5);

        //then
        assertThat(value).isInstanceOf(BigDecimal.class);
        assertThat(value).isEqualTo(new BigDecimal(5));
    }

    @Test
    public void transtype_long_should_return_bigDecimal() throws Exception {
        //given

        //when
        Object value = manager.transtypeIfApplicable(Long.class.getName(), 5L);

        //then
        assertThat(value).isInstanceOf(BigDecimal.class);
        assertThat(value).isEqualTo(new BigDecimal(5));
    }

    @Test
    public void transtype_float_should_return_bigDecimal() throws Exception {
        //given

        //when
        Object value = manager.transtypeIfApplicable(Float.class.getName(), 5.2f);

        //then
        assertThat(value).isInstanceOf(BigDecimal.class);
        assertThat(value).isEqualTo(new BigDecimal("5.2"));
    }

    @Test
    public void transtype_double_should_return_bigDecimal() throws Exception {
        //given

        //when
        Object value = manager.transtypeIfApplicable(Double.class.getName(), 5.2d);

        //then
        assertThat(value).isInstanceOf(BigDecimal.class);
        assertThat(value).isEqualTo(new BigDecimal("5.2"));
    }

    @Test
    public void transtype_null_should_return_null() throws Exception {
        //given

        //when
        Object value = manager.transtypeIfApplicable(Integer.class.getName(), null);

        //then
        assertThat(value).isNull();
    }

    @Test
    public void transtype_non_numeric_should_return_same_object() throws Exception {
        //given

        //when
        Object value = manager.transtypeIfApplicable(String.class.getName(), "just a string");

        //then
        assertThat(value).isInstanceOf(String.class);
        assertThat(value).isEqualTo("just a string");
    }

    private SExpression buildIntegerExpression(int value) {
        final SExpressionImpl expression = new SExpressionImpl();
        String strValue = String.valueOf(value);
        expression.setName(strValue);
        expression.setContent(strValue);
        expression.setExpressionType(SExpression.TYPE_CONSTANT);
        expression.setReturnType(Integer.class.getName());
        expression.setDependencies(Collections.<SExpression> emptyList());
        return expression;
    }

    private SExpression buildComparisonExpression(SExpression leftExpression, SExpression rightExpression, String operator) {
        final SExpressionImpl expression = new SExpressionImpl();
        expression.setName("compare");
        expression.setContent(operator);
        expression.setExpressionType(SExpression.TYPE_CONDITION);
        expression.setReturnType(Boolean.class.getName());
        expression.setDependencies(Arrays.asList(leftExpression, rightExpression));
        return expression;
    }

    private SExpression buildBooleanExpression(boolean value) {
        final SExpressionImpl expression = new SExpressionImpl();
        String strValue = String.valueOf(value);
        expression.setName(strValue);
        expression.setContent(strValue);
        expression.setExpressionType(SExpression.TYPE_CONSTANT);
        expression.setReturnType(Boolean.class.getName());
        expression.setDependencies(Collections.<SExpression> emptyList());
        return expression;
    }

}
