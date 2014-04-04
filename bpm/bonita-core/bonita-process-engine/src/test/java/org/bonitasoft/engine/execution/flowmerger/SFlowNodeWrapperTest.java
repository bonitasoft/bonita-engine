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
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SGatewayDefinition;
import org.bonitasoft.engine.core.process.definition.model.SGatewayType;
import org.bonitasoft.engine.core.process.definition.model.SSubProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SCatchEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SStartEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerDefinition;
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
public class SFlowNodeWrapperTest {

    @Mock
    private SFlowNodeDefinition flowNode;

    @Mock
    private SGatewayDefinition gateway;

    @Mock
    private SSubProcessDefinition subProcess;

    @Mock
    private SCatchEventDefinition catchEvent;

    @Mock
    private SStartEventDefinition startEvent;

    @Mock
    private SEventTriggerDefinition eventTrigger;

    private SFlowNodeWrapper flowNodeWrapper;

    private SFlowNodeWrapper gatewayWrapper;

    private SFlowNodeWrapper catchEventWrapper;

    private SFlowNodeWrapper subProcessWrapper;

    private SFlowNodeWrapper nullFlowNodewrapper;

    @Before
    public void setUp() {
        flowNodeWrapper = new SFlowNodeWrapper(flowNode);
        gatewayWrapper = new SFlowNodeWrapper(gateway);
        catchEventWrapper = new SFlowNodeWrapper(catchEvent);
        subProcessWrapper = new SFlowNodeWrapper(subProcess);
        nullFlowNodewrapper = new SFlowNodeWrapper(null);

        doReturn(SFlowNodeType.GATEWAY).when(gateway).getType();
        doReturn(SFlowNodeType.SUB_PROCESS).when(subProcess).getType();
        doReturn(SFlowNodeType.START_EVENT).when(startEvent).getType();
    }

    @Test
    public void not_exclusive_if_flow_node_is_null() {
        assertFalse(nullFlowNodewrapper.isExclusive());
    }

    @Test
    public void not_exclusive_if_not_gateway() {
        doReturn(SFlowNodeType.USER_TASK).when(flowNode).getType();

        assertFalse(flowNodeWrapper.isExclusive());
    }

    @Test
    public void not_exclusive_if_parallel_gateway() {
        doReturn(SGatewayType.PARALLEL).when(gateway).getGatewayType();

        assertFalse(gatewayWrapper.isExclusive());
    }

    @Test
    public void not_exclusive_if_inclusive_gateway() {
        doReturn(SGatewayType.INCLUSIVE).when(gateway).getGatewayType();

        assertFalse(gatewayWrapper.isExclusive());
    }

    @Test
    public void exclusive_if_exclusive_gateway() {
        doReturn(SGatewayType.EXCLUSIVE).when(gateway).getGatewayType();

        assertTrue(gatewayWrapper.isExclusive());
    }

    @Test
    public void not_parallelOrInclusive_if_flow_node_is_null() {
        assertFalse(nullFlowNodewrapper.isParalleleOrInclusive());
    }

    @Test
    public void not_parallelOrInclusive_if_not_gateway() {
        doReturn(SFlowNodeType.USER_TASK).when(flowNode).getType();

        assertFalse(flowNodeWrapper.isParalleleOrInclusive());

    }

    @Test
    public void parallelOrInclusive_if_parallel_gateway() {
        doReturn(SGatewayType.PARALLEL).when(gateway).getGatewayType();

        assertTrue(gatewayWrapper.isParalleleOrInclusive());

    }

    @Test
    public void parallelOrInclusive_if_inclusive_gateway() {
        doReturn(SGatewayType.INCLUSIVE).when(gateway).getGatewayType();

        assertTrue(gatewayWrapper.isParalleleOrInclusive());

    }

    @Test
    public void not_parallelOrInclusive_if_exclusive_gateway() {
        doReturn(SGatewayType.EXCLUSIVE).when(gateway).getGatewayType();

        assertFalse(gatewayWrapper.isParalleleOrInclusive());

    }

    @Test
    public void not_boundary_if_null() {
        assertFalse(nullFlowNodewrapper.isBoundaryEvent());
    }

    @Test
    public void boundary_if_is_a_boundary() {
        doReturn(SFlowNodeType.BOUNDARY_EVENT).when(flowNode).getType();
        assertTrue(flowNodeWrapper.isBoundaryEvent());
    }

    @Test
    public void is_not_interrupting_if_null() {
        assertFalse(nullFlowNodewrapper.isInterrupting());
    }

    @Test
    public void is_not_interrupting_if_not_catch_event() {
        assertFalse(flowNodeWrapper.isInterrupting());
    }

    @Test
    public void is_interrupting_if_interrupting_catch_event() {
        doReturn(true).when(catchEvent).isInterrupting();
        assertTrue(catchEventWrapper.isInterrupting());
    }

    @Test
    public void is_not_interrupting_if_non_interrupting_catch_event() {
        doReturn(false).when(catchEvent).isInterrupting();
        assertFalse(catchEventWrapper.isInterrupting());
    }

    @Test
    public void hasIncommingTransitions_return_true_if_flownode_hasIncommingTransitions() {
        doReturn(true).when(flowNode).hasIncomingTransitions();
        assertTrue(flowNodeWrapper.hasIncomingTransitions());
    }

    @Test
    public void hasIncommingTransitions_return_false_if_flownode_doesnt_have_IncommingTransitions() {
        doReturn(false).when(flowNode).hasIncomingTransitions();
        assertFalse(flowNodeWrapper.hasIncomingTransitions());
    }

    @Test
    public void isEventSubProcess_return_true_if_is_sub_process_triggered_by_event() {
        doReturn(true).when(subProcess).isTriggeredByEvent();
        assertTrue(subProcessWrapper.isEventSubProcess());
    }

    @Test
    public void isEventSubProcess_return_false_if_is_not_sub_process() {
        assertFalse(flowNodeWrapper.isEventSubProcess());
    }

    @Test
    public void isEventSubProcess_return_false_if_is_sub_process_not_triggered_by_event() {
        doReturn(false).when(subProcess).isTriggeredByEvent();
        assertFalse(subProcessWrapper.isEventSubProcess());
    }

    @Test
    public void isEventSubProcess_return_false_if_flownode_is_null() {
        assertFalse(nullFlowNodewrapper.isEventSubProcess());
    }

    @Test
    public void getFlowNode() {
        assertEquals(flowNode, flowNodeWrapper.getFlowNode());
    }
}
