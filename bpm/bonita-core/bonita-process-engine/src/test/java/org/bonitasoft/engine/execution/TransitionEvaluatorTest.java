/*
 *
 * Copyright (C) 2014 BonitaSoft S.A.
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
 *
 */

package org.bonitasoft.engine.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.assertj.core.util.Lists;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.execution.flowmerger.FlowNodeTransitionsWrapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransitionEvaluatorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private ExpressionResolverService expressionResolverService;

    @InjectMocks
    private TransitionEvaluator transitionEvaluator;

    @Test(expected = NullPointerException.class)
    public void testEvaluateOutgoingTransitions_should_throw_NPE() throws Exception {
        transitionEvaluator.evaluateOutgoingTransitions(null, null, null);
    }

    @Test
    public void testEvaluateOutgoingTransitions_with_empty_params_should_return_empty_list() throws Exception {
        SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        SFlowNodeInstance flowNodeInstance = mock(SFlowNodeInstance.class);
        FlowNodeTransitionsWrapper transitions = new FlowNodeTransitionsWrapper();
        transitions.setAllOutgoingTransitionDefinitions(Lists.<STransitionDefinition> newArrayList());

        List<STransitionDefinition> results = transitionEvaluator.evaluateOutgoingTransitions(transitions, processDefinition, flowNodeInstance);
        assertThat(results).isEmpty();
    }

}
