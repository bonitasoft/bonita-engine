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

import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.commons.exceptions.SObjectReadException;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SGatewayDefinition;
import org.bonitasoft.engine.core.process.definition.model.SGatewayType;
import org.bonitasoft.engine.core.process.definition.model.SSubProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SCatchEventDefinition;
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
    private SSubProcessDefinition subprocess;

    @Mock
    private SCatchEventDefinition catchEvent;
    
    private SFlowNodeWrapper flowNodeWrapper;
    
    private SFlowNodeWrapper gatewayWrapper;

    private SFlowNodeWrapper catchEventWrapper;

    private SFlowNodeWrapper subProcessWrapper;

    private SFlowNodeWrapper nullFlowNodewrapper;
    
    @Before
    public void setUp() throws SObjectReadException, SObjectNotFoundException {
        flowNodeWrapper = new SFlowNodeWrapper(flowNode);
        gatewayWrapper = new SFlowNodeWrapper(gateway);
        catchEventWrapper = new SFlowNodeWrapper(catchEvent);
        subProcessWrapper = new SFlowNodeWrapper(subprocess);
        nullFlowNodewrapper = new SFlowNodeWrapper(null);
        
        doReturn(SFlowNodeType.GATEWAY).when(gateway).getType();
        doReturn(SFlowNodeType.SUB_PROCESS).when(subprocess).getType();
    }
    
    @Test
    public void not_exclusive_if_flow_node_is_null() throws Exception {
        assertFalse(nullFlowNodewrapper.isExclusive());
    }

    @Test
    public void not_exclusive_if_not_gateway() throws Exception {
        doReturn(SFlowNodeType.USER_TASK).when(flowNode).getType();
        
        assertFalse(flowNodeWrapper.isExclusive());
    }

    @Test
    public void not_exclusive_if_parallel_gateway() throws Exception {
        doReturn(SGatewayType.PARALLEL).when(gateway).getGatewayType();
        
        assertFalse(gatewayWrapper.isExclusive());
    }

    @Test
    public void not_exclusive_if_inclusive_gateway() throws Exception {
        doReturn(SGatewayType.INCLUSIVE).when(gateway).getGatewayType();
        
        assertFalse(gatewayWrapper.isExclusive());
    }

    @Test
    public void exclusive_if_ecllusive_gateway() throws Exception {
        doReturn(SGatewayType.EXCLUSIVE).when(gateway).getGatewayType();
        
        assertTrue(gatewayWrapper.isExclusive());
    }
    
    @Test
    public void not_parallelOrInclusive_if_flow_node_is_null() throws Exception {
        assertFalse(nullFlowNodewrapper.isParalleleOrInclusive());
    }

    @Test
    public void not_parallelOrInclusive_if_not_gateway() throws Exception {
        doReturn(SFlowNodeType.USER_TASK).when(flowNode).getType();
        
        assertFalse(flowNodeWrapper.isParalleleOrInclusive());
        
    }

    @Test
    public void parallelOrInclusive_if_parallel_gateway() throws Exception {
        doReturn(SGatewayType.PARALLEL).when(gateway).getGatewayType();
        
        assertTrue(gatewayWrapper.isParalleleOrInclusive());
        
    }

    @Test
    public void parallelOrInclusive_if_inclusive_gateway() throws Exception {
        doReturn(SGatewayType.INCLUSIVE).when(gateway).getGatewayType();
        
        assertTrue(gatewayWrapper.isParalleleOrInclusive());
        
    }

    @Test
    public void not_parallelOrInclusive_if_exclusive_gateway() throws Exception {
        doReturn(SGatewayType.EXCLUSIVE).when(gateway).getGatewayType();
        
        assertFalse(gatewayWrapper.isParalleleOrInclusive());
        
    }
    
    @Test
    public void not_boundary_if_null() throws Exception {
        assertFalse(nullFlowNodewrapper.isBoundaryEvent());
    }
    
    @Test
    public void boundary_if_is_a_boundary() throws Exception {
        doReturn(SFlowNodeType.BOUNDARY_EVENT).when(flowNode).getType();
        assertTrue(flowNodeWrapper.isBoundaryEvent());
    }
    
    @Test
    public void is_not_interrupting_if_null() throws Exception {
        assertFalse(nullFlowNodewrapper.isInterrupting());
    }

    @Test
    public void is_not_interrupting_if_not_catch_event() throws Exception {
        assertFalse(flowNodeWrapper.isInterrupting());
    }

    @Test
    public void is_interrupting_if_interrupting_catch_event() throws Exception {
        doReturn(true).when(catchEvent).isInterrupting();
        assertTrue(catchEventWrapper.isInterrupting());
    }

    @Test
    public void is_not_interrupting_if_non_interrupting_catch_event() throws Exception {
        doReturn(false).when(catchEvent).isInterrupting();
        assertFalse(catchEventWrapper.isInterrupting());
    }
    
    @Test
    public void hasIncommingTransitions_return_true_if_flownode_hasIncommingTransitions() throws Exception {
        doReturn(true).when(flowNode).hasIncomingTransitions();
        assertTrue(flowNodeWrapper.hasIncomingTransitions());
    }

    @Test
    public void hasIncommingTransitions_return_false_if_flownode_doesnt_have_IncommingTransitions() throws Exception {
        doReturn(false).when(flowNode).hasIncomingTransitions();
        assertFalse(flowNodeWrapper.hasIncomingTransitions());
    }
    
    @Test
    public void isEventSubProcess_return_true_if_is_subp_process_triggered_by_event() {
        doReturn(true).when(subprocess).isTriggeredByEvent();
        assertTrue(subProcessWrapper.isEventSubProcess());
    }

    @Test
    public void isEventSubProcess_return_false_if_is_not_subprocess() {
        assertFalse(flowNodeWrapper.isEventSubProcess());
    }

    @Test
    public void isEventSubProcess_return_false_if_is_subprocess_not_triggered_by_event() {
        doReturn(false).when(subprocess).isTriggeredByEvent();
        assertFalse(subProcessWrapper.isEventSubProcess());
    }

    @Test
    public void isEventSubProcess_return_false_if_flownode_is_null() {
        assertFalse(nullFlowNodewrapper.isEventSubProcess());
    }
    
    @Test
    public void getFlowNode() throws Exception {
        assertEquals(flowNode, flowNodeWrapper.getFlowNode());
    }

}
