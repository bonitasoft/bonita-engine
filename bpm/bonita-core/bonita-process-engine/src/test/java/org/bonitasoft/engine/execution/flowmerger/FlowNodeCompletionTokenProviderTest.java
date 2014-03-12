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
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;

import org.bonitasoft.engine.core.process.instance.api.TokenService;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SToken;
import org.bonitasoft.engine.core.process.instance.model.event.SBoundaryEventInstance;
import org.bonitasoft.engine.execution.TokenProvider;
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
public class FlowNodeCompletionTokenProviderTest {

    private static final long PARENT_TOKEN_REF_ID = 21L;

    private static final long PROCESS_INSTANCE_ID = 5L;

    private static final long FLOW_NODE_TOKEN_REF_ID = 6L;
    
    private static final long FLOW_NODE_INSTANCE_ID = 7L;

    @Mock
    private SFlowNodeWrapper flowNodeWrapper;
    
    @Mock 
    private TokenService tokenService;
    
    @Mock
    private FlowNodeTransitionsWrapper flowNodeTransitionsWrapper;
    
    @Mock 
    private SBoundaryEventInstance boundary;

    @Mock 
    private SGatewayInstance gateway;
    
    @Mock
    private SToken token;
    
    @Mock
    private SProcessInstance processInstance;
    
    private TokenProvider boundaryTokenProvider;

    private TokenProvider gateWayTokenProvider;
    
    @Before
    public void setUp() throws Exception {
        boundaryTokenProvider = new FlowNodeCompletionTokenProvider(boundary, processInstance, flowNodeWrapper, flowNodeTransitionsWrapper, tokenService);
        gateWayTokenProvider = new FlowNodeCompletionTokenProvider(gateway, processInstance, flowNodeWrapper, flowNodeTransitionsWrapper, tokenService);
        doReturn(PROCESS_INSTANCE_ID).when(processInstance).getId();
        
        doReturn(FLOW_NODE_TOKEN_REF_ID).when(gateway).getTokenRefId();
        doReturn(FLOW_NODE_INSTANCE_ID).when(gateway).getId();
        doReturn(PROCESS_INSTANCE_ID).when(boundary).getParentProcessInstanceId();
        doReturn(FLOW_NODE_TOKEN_REF_ID).when(boundary).getTokenRefId();
        doReturn(FLOW_NODE_INSTANCE_ID).when(boundary).getId();

        doReturn(token).when(tokenService).getToken(PROCESS_INSTANCE_ID, FLOW_NODE_TOKEN_REF_ID);
        doReturn(FLOW_NODE_TOKEN_REF_ID).when(token).getRefId();
        doReturn(PARENT_TOKEN_REF_ID).when(token).getParentRefId();
    }
    
    @Test
    public void outputTokenRefId_is_null_with_null_flowNode() throws Exception {
        doReturn(true).when(flowNodeWrapper).isNull();
        
        assertNull(boundaryTokenProvider.getOutputTokenInfo().outputTokenRefId);
        assertNull(boundaryTokenProvider.getOutputTokenInfo().outputParentTokenRefId);
    }
    
    @Test
    public void outputTokenRefId_is_null_if_is_last_flowNode() throws Exception {
        doReturn(true).when(flowNodeTransitionsWrapper).isLastFlowNode();
        
        assertNull(boundaryTokenProvider.getOutputTokenInfo().outputTokenRefId);
        assertNull(boundaryTokenProvider.getOutputTokenInfo().outputParentTokenRefId);
    }
    
    @Test
    public void getOutputTokenInfo_returns_current_token_info_for_non_interrupting_boundary_event() throws Exception {
        doReturn(true).when(flowNodeWrapper).isBoundaryEvent();
        doReturn(false).when(flowNodeWrapper).isInterrupting();
        
        assertEquals(Long.valueOf(FLOW_NODE_TOKEN_REF_ID), boundaryTokenProvider.getOutputTokenInfo().outputTokenRefId);
        assertNull(boundaryTokenProvider.getOutputTokenInfo().outputParentTokenRefId);
    }

    @Test
    public void getOutputTokenInfo_returns_current_token_info_for_interrupting_boundary_event() throws Exception {
        doReturn(true).when(flowNodeWrapper).isBoundaryEvent();
        doReturn(true).when(flowNodeWrapper).isInterrupting();
        
        assertEquals(Long.valueOf(FLOW_NODE_TOKEN_REF_ID), boundaryTokenProvider.getOutputTokenInfo().outputTokenRefId);
        assertEquals(Long.valueOf(PARENT_TOKEN_REF_ID), boundaryTokenProvider.getOutputTokenInfo().outputParentTokenRefId);
    }

    @Test
    public void transmit_token_if_exclusive() throws Exception {
        doReturn(true).when(flowNodeWrapper).isExclusive();
        
        assertEquals(Long.valueOf(FLOW_NODE_TOKEN_REF_ID), gateWayTokenProvider.getOutputTokenInfo().outputTokenRefId);
        assertNull(gateWayTokenProvider.getOutputTokenInfo().outputParentTokenRefId);
        
    }

    @Test
    public void trasmit_token_if_isSimpleMerge() throws Exception {
        doReturn(true).when(flowNodeTransitionsWrapper).isSimpleMerge();
        
        assertEquals(Long.valueOf(FLOW_NODE_TOKEN_REF_ID), gateWayTokenProvider.getOutputTokenInfo().outputTokenRefId);
        assertNull(gateWayTokenProvider.getOutputTokenInfo().outputParentTokenRefId);
    }

    @Test
    public void outputTokenRefId_is_childId_if_isSimpleToMany() throws Exception {
        doReturn(true).when(flowNodeTransitionsWrapper).isSimpleToMany();
        
        assertEquals(Long.valueOf(FLOW_NODE_INSTANCE_ID), gateWayTokenProvider.getOutputTokenInfo().outputTokenRefId);
        assertEquals(Long.valueOf(FLOW_NODE_TOKEN_REF_ID), gateWayTokenProvider.getOutputTokenInfo().outputParentTokenRefId);
    }
    
    @Test
    public void return_flownode_id_as_outpuTokenRefId_if_isManyToMany_and_isParallelOrInclusive() throws Exception {
        doReturn(true).when(flowNodeTransitionsWrapper).isManyToMany();
        doReturn(true).when(flowNodeWrapper).isParalleleOrInclusive();
        
        assertEquals(Long.valueOf(FLOW_NODE_INSTANCE_ID), gateWayTokenProvider.getOutputTokenInfo().outputTokenRefId);
        assertEquals(Long.valueOf(PARENT_TOKEN_REF_ID), gateWayTokenProvider.getOutputTokenInfo().outputParentTokenRefId);
    }

    @Test
    public void return_flownode_id_as_outpuTokenRefId_if_isManyToMany_and_is_not_ParallelOrInclusive() throws Exception {
        doReturn(true).when(flowNodeTransitionsWrapper).isManyToMany();
        doReturn(false).when(flowNodeWrapper).isParalleleOrInclusive();
        
        assertEquals(Long.valueOf(FLOW_NODE_INSTANCE_ID), gateWayTokenProvider.getOutputTokenInfo().outputTokenRefId);
        assertEquals(Long.valueOf(FLOW_NODE_TOKEN_REF_ID), gateWayTokenProvider.getOutputTokenInfo().outputParentTokenRefId);
    }
    
    @Test
    public void return_parent_ref_id_if_isManyToOne_and_is_ParalelOrInclusive() throws Exception {
        doReturn(true).when(flowNodeTransitionsWrapper).isManyToOne();
        doReturn(true).when(flowNodeWrapper).isParalleleOrInclusive();
        
        assertEquals(Long.valueOf(PARENT_TOKEN_REF_ID), gateWayTokenProvider.getOutputTokenInfo().outputTokenRefId);
        assertNull(gateWayTokenProvider.getOutputTokenInfo().outputParentTokenRefId);
    }

    @Test
    public void return_child_token_ref_id_if_isManyToOne_and_not_is_ParalelOrInclusive() throws Exception {
        doReturn(true).when(flowNodeTransitionsWrapper).isManyToOne();
        doReturn(false).when(flowNodeWrapper).isParalleleOrInclusive();
        
        assertEquals(Long.valueOf(FLOW_NODE_TOKEN_REF_ID), gateWayTokenProvider.getOutputTokenInfo().outputTokenRefId);
        assertNull(gateWayTokenProvider.getOutputTokenInfo().outputParentTokenRefId);
    }

}
