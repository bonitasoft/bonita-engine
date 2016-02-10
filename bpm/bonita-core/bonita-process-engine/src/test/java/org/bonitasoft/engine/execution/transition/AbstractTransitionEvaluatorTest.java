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
package org.bonitasoft.engine.execution.transition;

import static org.mockito.BDDMockito.given;

import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.STransitionDefinitionImpl;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.expression.model.SExpression;
import org.junit.Before;
import org.mockito.Mock;

/**
 * @author Elias Ricken de Medeiros
 */
public class AbstractTransitionEvaluatorTest {

    @Mock
    protected TransitionConditionEvaluator conditionEvaluator;

    @Mock
    protected DefaultTransitionGetter defaultTransitionGetter;

    @Mock
    protected SExpression trueExpression;

    @Mock
    protected SExpression falseExpression;

    @Mock
    protected SProcessDefinition processDefinition;

    @Mock
    protected SFlowNodeInstance flowNodeInstance;

    @Mock
    protected SExpressionContext context;

    protected STransitionDefinition unConditionalTransition;

    protected STransitionDefinition defaultTransition;

    protected STransitionDefinition trueTransition1;

    protected STransitionDefinition trueTransition2;

    protected STransitionDefinition falseTransition;

    protected STransitionDefinition nullTransition;

    @Before
    public void setUp() throws Exception {
        unConditionalTransition = new STransitionDefinitionImpl("nonConditional");
        defaultTransition = new STransitionDefinitionImpl("default");
        trueTransition1 = buildTransition("trueT1", trueExpression);
        trueTransition2 = buildTransition("truT2", trueExpression);
        falseTransition = buildTransition("falseT1", falseExpression);
        nullTransition = buildTransition("falseT2", falseExpression);

        given(conditionEvaluator.evaluateCondition(trueTransition1, context)).willReturn(true);
        given(conditionEvaluator.evaluateCondition(trueTransition2, context)).willReturn(true);
        given(conditionEvaluator.evaluateCondition(falseTransition, context)).willReturn(false);
        given(conditionEvaluator.evaluateCondition(nullTransition, context)).willReturn(null);
    }

    STransitionDefinition buildTransition(String name, SExpression expression) {
        STransitionDefinitionImpl transition = new STransitionDefinitionImpl(name);
        transition.setCondition(expression);
        return transition;
    }

}
