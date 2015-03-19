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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.impl.ConditionExpressionExecutorStrategy;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LogicalComplementExecutorTest {

    @InjectMocks
    private LogicalComplementExecutor logicalComplementExecutor;

    @Test
    public void evaluate_should_return_true_when_the_resolution_of_first_dependency_is_false() throws Exception {
        //given
        SExpression sourceExpression = buildBooleanExpression(false);
        Map<Integer, Object> resolvedExpressions = Collections.<Integer, Object> singletonMap(sourceExpression.getDiscriminant(), false);
        SExpression logicalComplementExpression = buildLogicalComplementExpression(sourceExpression);

        //when
        Boolean value = logicalComplementExecutor.evaluate(resolvedExpressions, logicalComplementExpression);

        //then
        assertThat(value).isTrue();
    }

    @Test
    public void evaluate_should_return_false_when_the_resolution_of_first_dependency_is_true() throws Exception {
        //given
        SExpression sourceExpression = buildBooleanExpression(true);
        Map<Integer, Object> resolvedExpressions = Collections.<Integer, Object> singletonMap(sourceExpression.getDiscriminant(), true);
        SExpression logicalComplementExpression = buildLogicalComplementExpression(sourceExpression);

        //when
        Boolean value = logicalComplementExecutor.evaluate(resolvedExpressions, logicalComplementExpression);

        //then
        assertThat(value).isFalse();
    }

    @Test
    public void evaluate_should_return_null_when_the_resolution_of_first_dependency_is_null() throws Exception {
        //given
        SExpression sourceExpression = buildBooleanExpression(true);
        Map<Integer, Object> resolvedExpressions = Collections.emptyMap();
        SExpression logicalComplementExpression = buildLogicalComplementExpression(sourceExpression);

        //when
        Boolean value = logicalComplementExecutor.evaluate(resolvedExpressions, logicalComplementExpression);

        //then
        assertThat(value).isNull();
    }

    @Test
    public void evaluate_should_throws_SExpressionEvaluationException_when_dependencies_has_size_different_of_one() throws Exception {
        //given

        List<SExpression> dependencies = Arrays.asList(mock(SExpression.class), mock(SExpression.class));

        SExpression expression = mock(SExpression.class);
        given(expression.getName()).willReturn("my expr");
        given(expression.getDependencies()).willReturn(dependencies);

        try {
            //when
            logicalComplementExecutor.evaluate(Collections.<Integer, Object> emptyMap(), expression);
            fail("Exception expected");
        } catch (SExpressionEvaluationException e) {
            //then
            assertThat(e.getMessage()).isEqualTo(
                    "The expression '" + ConditionExpressionExecutorStrategy.LOGICAL_COMPLEMENT_OPERATOR + "' must have exactly 1 dependency.");
            assertThat(e.getExpressionName()).isEqualTo("my expr");
        }

    }

    @Test
    public void evaluate_should_throws_SExpressionEvaluationException_when_dependency_does_not_return_a_boolean() throws Exception {
        //given
        SExpression sourceExpression = mock(SExpression.class);
        given(sourceExpression.getReturnType()).willReturn(Integer.class.getName());

        SExpression expression = mock(SExpression.class);
        given(expression.getName()).willReturn("my expr");
        given(expression.getDependencies()).willReturn(Collections.singletonList(sourceExpression));

        try {
            //when
            logicalComplementExecutor.evaluate(Collections.<Integer, Object> emptyMap(), expression);
            fail("Exception expected");
        } catch (SExpressionEvaluationException e) {
            //then
            assertThat(e.getMessage()).isEqualTo(
                    "The dependency of expression '!' must have the return type java.lang.Boolean, but java.lang.Integer was found.");
            assertThat(e.getExpressionName()).isEqualTo("my expr");
        }

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

    private SExpression buildLogicalComplementExpression(SExpression sourceExpression) {
        final SExpressionImpl expression = new SExpressionImpl();
        expression.setName("not");
        expression.setContent(ConditionExpressionExecutorStrategy.LOGICAL_COMPLEMENT_OPERATOR);
        expression.setExpressionType(SExpression.TYPE_CONDITION);
        expression.setReturnType(Boolean.class.getName());
        expression.setDependencies(Collections.singletonList(sourceExpression));
        return expression;
    }

}
