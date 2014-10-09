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

import org.assertj.core.util.Lists;
import org.bonitasoft.engine.commons.exceptions.SExceptionContext;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityExecutionException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

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
        List<STransitionDefinition> transitionDefinitions = Lists.newArrayList();
        List<STransitionDefinition> results = transitionEvaluator.evaluateOutgoingTransitions(transitionDefinitions, processDefinition, flowNodeInstance);
        assertThat(results).isEmpty();
    }

    @Test
    public void testEvaluateOutgoingTransitions_with_empty_transitions_should_return_default_transition() throws Exception {
        SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        SFlowNodeInstance flowNodeInstance = mock(SFlowNodeInstance.class);
        STransitionDefinition defaultTransition = mock(STransitionDefinition.class);
        List<STransitionDefinition> transitionDefinitions = Lists.newArrayList();
        TransitionEvaluator transitionEvaluatorSpy = spy(transitionEvaluator);

        doReturn(defaultTransition).when(transitionEvaluatorSpy).getDefaultTransition(processDefinition, flowNodeInstance);
        doReturn(null).when(transitionEvaluatorSpy).evaluateTransitionsForImplicitGateway(eq(processDefinition), eq(flowNodeInstance), eq(transitionDefinitions),
                any(SExpressionContext.class));
        when(flowNodeInstance.getStateCategory()).thenReturn(SStateCategory.NORMAL);
        List<STransitionDefinition> results = transitionEvaluatorSpy.evaluateOutgoingTransitions(transitionDefinitions, processDefinition, flowNodeInstance);

        assertThat(results).containsExactly(defaultTransition);
        verify(transitionEvaluatorSpy, times(1)).getDefaultTransition(processDefinition, flowNodeInstance);
    }



    @Test
    public void testEvaluateTransitionsForImpliciteGateway_without_valid_transitions_should_throw_SActivityExecutionException() throws Exception {

        // Given
        final String processName = "Faulty Process";
        final String processVersion = "6.3.1";
        SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        when(processDefinition.getName()).thenReturn(processName);
        when(processDefinition.getVersion()).thenReturn(processVersion);

        SFlowNodeInstance flowNodeInstance = mock(SFlowNodeInstance.class);
        when(flowNodeInstance.getParentProcessInstanceId()).thenReturn(42L);


        thrown.expect(SActivityExecutionException.class);
        thrown.expect(new BaseMatcher<Object>() {

            final long expectedProcessInstanceID = 42L;

            @Override
            public boolean matches(Object item) {


                if(item instanceof SActivityExecutionException) {
                    SActivityExecutionException exception = (SActivityExecutionException) item;

                    Map<SExceptionContext, Serializable> context = exception.getContext();
                    return (hasProcessNameInContext(processName, context) && hasProcessInstanceIDInContext(expectedProcessInstanceID, context) && hasProcessVersionInContext(processVersion, context));
                }
                return false;
            }

            private boolean hasProcessVersionInContext(String processVersion, Map<SExceptionContext, Serializable> context) {
                return processVersion.equals(context.get(SExceptionContext.PROCESS_VERSION));
            }

            private boolean hasProcessInstanceIDInContext(long processInstanceId, Map<SExceptionContext, Serializable> context) {
                return processInstanceId == (Long)context.get(SExceptionContext.PROCESS_INSTANCE_ID);
            }

            private boolean hasProcessNameInContext(final String processName, Map<SExceptionContext, Serializable> context) {
                return processName.equals(context.get(SExceptionContext.PROCESS_NAME));
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Having context containing Process Name: " + processName + " and Version: " + processVersion + " and Process Instance ID: " + expectedProcessInstanceID);

            }
        });


        TransitionEvaluator transitionEvaluatorSpy = spy(transitionEvaluator);
        doReturn(null).when(transitionEvaluatorSpy).getDefaultTransition(processDefinition, flowNodeInstance);

        // When
        transitionEvaluatorSpy.evaluateTransitionsInclusively(processDefinition, flowNodeInstance, Collections.<STransitionDefinition>emptyList(), null);

    }

    @Test
    public void testEvaluateOutgoingTransitions_with_some_transition_and_default_transition_should_not_return_default_transition() throws Exception {
        SProcessDefinition processDefinition = mock(SProcessDefinition.class);
        SFlowNodeInstance flowNodeInstance = mock(SFlowNodeInstance.class);
        STransitionDefinition defaultTransition = mock(STransitionDefinition.class);
        STransitionDefinition transition1 = mock(STransitionDefinition.class);
        STransitionDefinition transition2 = mock(STransitionDefinition.class);
        STransitionDefinition transition3 = mock(STransitionDefinition.class);
        List<STransitionDefinition> transitionDefinitions = Lists.newArrayList(transition1, transition2, transition3);
        TransitionEvaluator transitionEvaluatorSpy = spy(transitionEvaluator);

        doReturn(defaultTransition).when(transitionEvaluatorSpy).getDefaultTransition(processDefinition, flowNodeInstance);
        doReturn(transitionDefinitions).when(transitionEvaluatorSpy).evaluateTransitionsForImplicitGateway(eq(processDefinition), eq(flowNodeInstance),
                eq(transitionDefinitions),
                any(SExpressionContext.class));
        when(flowNodeInstance.getStateCategory()).thenReturn(SStateCategory.NORMAL);
        List<STransitionDefinition> results = transitionEvaluatorSpy.evaluateOutgoingTransitions(transitionDefinitions, processDefinition, flowNodeInstance);

        assertThat(results).isEqualTo(transitionDefinitions);
        verify(transitionEvaluatorSpy, times(0)).getDefaultTransition(processDefinition, flowNodeInstance);
        verify(transitionEvaluatorSpy, times(1)).evaluateTransitionsForImplicitGateway(eq(processDefinition), eq(flowNodeInstance), eq(transitionDefinitions),
                any(SExpressionContext.class));
    }

}