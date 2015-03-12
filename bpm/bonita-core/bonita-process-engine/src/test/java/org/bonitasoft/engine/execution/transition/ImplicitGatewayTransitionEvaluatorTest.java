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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.execution.flowmerger.FlowNodeTransitionsWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ImplicitGatewayTransitionEvaluatorTest extends AbstractTransitionEvaluatorTest {

    @InjectMocks
    private ImplicitGatewayTransitionEvaluator implicitGatewayTransitionEvaluator;

    @Test
    public void evaluateTransitions_should_return_all_unconditional_transitions_and_transitions_evaluated_to_true() throws Exception {
        //given
        FlowNodeTransitionsWrapper transitions = new FlowNodeTransitionsWrapper();
        transitions.setAllOutgoingTransitionDefinitions(new ArrayList<STransitionDefinition>(Arrays.asList(unConditionalTransition, trueTransition1,
                trueTransition2, falseTransition, nullTransition)));

        //when
        List<STransitionDefinition> chosenTransitions = implicitGatewayTransitionEvaluator.evaluateTransitions(processDefinition, flowNodeInstance, transitions, context);

        //then
        assertThat(chosenTransitions).containsExactly(unConditionalTransition, trueTransition1, trueTransition2);
    }

    @Test
    public void evaluateTransitions_should_return_unconditional_transitions_and_default_transition_when_all_conditional_transitions_are_evaluated_to_false() throws Exception {
        //given
        FlowNodeTransitionsWrapper transitions = new FlowNodeTransitionsWrapper();
        transitions.setAllOutgoingTransitionDefinitions(new ArrayList<STransitionDefinition>(Arrays.asList(unConditionalTransition, falseTransition, nullTransition)));
        given(defaultTransitionGetter.getDefaultTransition(transitions, processDefinition, flowNodeInstance)).willReturn(defaultTransition);

        //when
        List<STransitionDefinition> chosenTransitions = implicitGatewayTransitionEvaluator.evaluateTransitions(processDefinition, flowNodeInstance, transitions, context);

        //then
        assertThat(chosenTransitions).containsExactly(unConditionalTransition, defaultTransition);
    }

    @Test
    public void evaluateTransitions_should_not_return_default_transition_when_there_are_no_conditional_transitions() throws Exception {
        //given
        FlowNodeTransitionsWrapper transitions = new FlowNodeTransitionsWrapper();
        transitions.setAllOutgoingTransitionDefinitions(new ArrayList<STransitionDefinition>(Arrays.asList(unConditionalTransition)));
        given(defaultTransitionGetter.getDefaultTransition(transitions, processDefinition, flowNodeInstance)).willReturn(defaultTransition);

        //when
        List<STransitionDefinition> chosenTransitions = implicitGatewayTransitionEvaluator.evaluateTransitions(processDefinition, flowNodeInstance, transitions, context);

        //then
        assertThat(chosenTransitions).containsExactly(unConditionalTransition);
    }

}
