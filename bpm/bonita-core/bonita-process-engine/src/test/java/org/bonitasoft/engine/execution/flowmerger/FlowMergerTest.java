/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.execution.flowmerger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SGatewayDefinition;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Elias Ricken de Medeiros
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class FlowMergerTest {

    @Mock
    SFlowNodeInstance child;

    @Mock
    private SProcessInstance sProcessInstance;

    @Mock
    private SFlowNodeDefinition flowNode;

    @Mock
    private SGatewayDefinition gateway;

    @Mock
    private FlowNodeTransitionsWrapper transitionsDescriptor;

    @Mock
    private FlowNodeCompletionTokenProvider tokenProvider;

    @Mock
    private SFlowNodeWrapper flowNodeWrapper;

    @Mock
    private SFlowNodeWrapper transitionDependentFlowNodeWrapper;

    private FlowMerger flowNodeMerger;

    private FlowMerger transitionDependentFlowNodeMerger;

    @Before
    public void setUp() {
        flowNodeMerger = new FlowMerger(flowNodeWrapper, transitionsDescriptor, tokenProvider);
        transitionDependentFlowNodeMerger = new FlowMerger(transitionDependentFlowNodeWrapper, transitionsDescriptor, tokenProvider);
        doReturn(false).when(transitionDependentFlowNodeWrapper).isNull();
        doReturn(false).when(transitionDependentFlowNodeWrapper).isBoundaryEvent();
        doReturn(false).when(transitionDependentFlowNodeWrapper).isExclusive();
        doReturn(false).when(transitionsDescriptor).isLastFlowNode();
    }

    @Test
    public void should_not__create_token_if_flowNode_is_null() {
        doReturn(true).when(flowNodeWrapper).isNull();
        assertFalse(flowNodeMerger.mustCreateTokenOnFinish());
    }

    @Test
    public void should_not_create_token_if_it_is_a_boundary_event() {
        doReturn(true).when(flowNodeWrapper).isBoundaryEvent();

        assertFalse(flowNodeMerger.mustCreateTokenOnFinish());
    }

    @Test
    public void should_not_create_token_if_it_is_exclusive() {
        doReturn(true).when(flowNodeWrapper).isExclusive();

        assertFalse(flowNodeMerger.mustCreateTokenOnFinish());
    }

    @Test
    public void should_not_create_token_if_is_last_flow_node() {
        doReturn(true).when(transitionsDescriptor).isLastFlowNode();

        assertFalse(transitionDependentFlowNodeMerger.mustCreateTokenOnFinish());
    }

    @Test
    public void should_not_create_token_if_dont_have_multiple_outgoing_transitions() {
        doReturn(false).when(transitionsDescriptor).isLastFlowNode();
        doReturn(false).when(transitionsDescriptor).hasMultipleOutgoingTransitions();

        assertFalse(transitionDependentFlowNodeMerger.mustCreateTokenOnFinish());
    }

    @Test
    public void should_create_token_if_depend_on_transitons_and_has_multiple_outgoing_transitions() {
        doReturn(false).when(transitionsDescriptor).isLastFlowNode();
        doReturn(true).when(transitionsDescriptor).hasMultipleOutgoingTransitions();

        assertTrue(transitionDependentFlowNodeMerger.mustCreateTokenOnFinish());
    }

    @Test
    public void should_not_consume_token_if_flowNode_is_null() {
        doReturn(true).when(flowNodeWrapper).isNull();
        assertFalse(flowNodeMerger.mustConsumeInputTokenOnTakingTransition());
    }

    @Test
    public void should_not_consume_token_if_it_is_a_boundary_event_is_not_last_flow_node() {
        doReturn(true).when(flowNodeWrapper).isBoundaryEvent();

        assertFalse(flowNodeMerger.mustConsumeInputTokenOnTakingTransition());
    }

    @Test
    public void should_not_consume_token_if_it_is_exclusive() {
        doReturn(true).when(flowNodeWrapper).isExclusive();

        assertFalse(flowNodeMerger.mustConsumeInputTokenOnTakingTransition());
    }

    @Test
    public void should_not_consume_token_if_is_last_flow_node() {
        doReturn(true).when(transitionsDescriptor).isLastFlowNode();

        assertFalse(transitionDependentFlowNodeMerger.mustConsumeInputTokenOnTakingTransition());
    }

    @Test
    public void should_not_consume_token_if_doesnt_have_multiple_incoming_transitions() {
        doReturn(false).when(transitionsDescriptor).isLastFlowNode();
        doReturn(false).when(transitionsDescriptor).hasMultipleIncomingTransitions();

        assertFalse(transitionDependentFlowNodeMerger.mustConsumeInputTokenOnTakingTransition());
    }

    @Test
    public void should_consume_token_if_has_multiple_incoming_transitions_and_is_parallel_or_inclusive() {
        doReturn(false).when(transitionsDescriptor).isLastFlowNode();
        doReturn(true).when(transitionsDescriptor).hasMultipleIncomingTransitions();
        doReturn(true).when(transitionDependentFlowNodeWrapper).isParalleleOrInclusive();

        assertTrue(transitionDependentFlowNodeMerger.mustConsumeInputTokenOnTakingTransition());
    }

    @Test
    public void should_not_consume_token_if_has_multiple_incoming_transitions_is_not_parallel_or_inclusive() {
        doReturn(false).when(transitionsDescriptor).isLastFlowNode();
        doReturn(true).when(transitionsDescriptor).hasMultipleIncomingTransitions();
        doReturn(false).when(transitionDependentFlowNodeWrapper).isParalleleOrInclusive();

        assertFalse(transitionDependentFlowNodeMerger.mustConsumeInputTokenOnTakingTransition());
    }

    @Test
    public void is_not_implicite_end_if_flowNode_is_null() {
        doReturn(true).when(flowNodeWrapper).isNull();
        assertFalse(flowNodeMerger.isImplicitEnd());
    }

    @Test
    public void is_not_implicite_end_if_it_is_a_boundary_event_and_is_not_last_flow_node() {
        doReturn(true).when(flowNodeWrapper).isBoundaryEvent();

        assertFalse(flowNodeMerger.isImplicitEnd());
        doReturn(true).when(transitionsDescriptor).isLastFlowNode();
    }

    @Test
    public void is_implicite_end_if_it_is_a_boundary_event_and_is_last_flow_node() {
        doReturn(true).when(flowNodeWrapper).isBoundaryEvent();
        doReturn(true).when(transitionsDescriptor).isLastFlowNode();

        assertTrue(flowNodeMerger.isImplicitEnd());
    }

    @Test
    public void is_implicite_end_if_is_last_flow_node() {
        doReturn(true).when(transitionsDescriptor).isLastFlowNode();

        assertTrue(flowNodeMerger.isImplicitEnd());
    }

    @Test
    public void is_not_implicite_end_if_is_not_last_flow_node() {
        doReturn(false).when(transitionsDescriptor).isLastFlowNode();

        assertFalse(flowNodeMerger.isImplicitEnd());
    }

    @Test
    public void return_token_info_supplied_by_tokenProvider() throws Exception {
        TokenInfo tokenInfo = new TokenInfo(1L, 2L);
        doReturn(tokenInfo).when(tokenProvider).getOutputTokenInfo();
        assertEquals(tokenInfo, flowNodeMerger.getOutputTokenInfo());
    }

}
