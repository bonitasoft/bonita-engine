/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.execution.transition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Collections;

import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.definition.model.impl.STransitionDefinitionImpl;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransitionConditionEvaluatorTest {

    @Mock
    private ExpressionResolverService expressionResolverService;

    @InjectMocks
    private TransitionConditionEvaluator evaluator;

    @Mock
    private SExpressionContext context;

    @Test
    public void evaluateCondition_should_return_result_of_expressionResolverService_evaluate() throws Exception {
        //given
        STransitionDefinitionImpl transition = new STransitionDefinitionImpl("t1");
        SExpression condition = buildBooleanExpression(true);
        transition.setCondition(condition);
        given(expressionResolverService.evaluate(condition, context)).willReturn(true);

        //when
        Boolean value = evaluator.evaluateCondition(transition, context);

        //then
        assertThat(value).isTrue();
    }

    @Test
    public void evaluateCondition_should_return_null_when_there_is_no_condition() throws Exception {
        //given
        STransitionDefinitionImpl transition = new STransitionDefinitionImpl("t1");

        //when
        Boolean value = evaluator.evaluateCondition(transition, context);

        //then
        assertThat(value).isNull();

        verifyNoInteractions(expressionResolverService);
    }

    @Test
    public void evaluateCondition_should_throw_STransitionConditionEvaluationException_when_expression_return_type_is_not_a_boolean()
            throws Exception {
        //given
        SExpression condition = mock(SExpression.class);
        given(condition.getReturnType()).willReturn(String.class.getName());
        given(condition.getName()).willReturn("isTrue");
        STransitionDefinitionImpl transition = new STransitionDefinitionImpl("t1");
        transition.setCondition(condition);

        var exception = assertThrows(STransitionConditionEvaluationException.class, () -> {
            evaluator.evaluateCondition(transition, context);
        });

        //then
        assertThat(exception).hasMessage("TRANSITION_NAME=t1 | Condition expression must return a boolean");
        assertThat(exception.getCause()).isInstanceOf(SExpressionEvaluationException.class);
        var cause = (SExpressionEvaluationException) exception.getCause();
        assertThat(cause.getExpressionName()).isEqualTo("isTrue");

        verifyNoInteractions(expressionResolverService);
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
