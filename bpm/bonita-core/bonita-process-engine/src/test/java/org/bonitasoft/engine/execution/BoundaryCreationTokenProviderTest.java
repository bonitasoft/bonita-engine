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
package org.bonitasoft.engine.execution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;

import org.bonitasoft.engine.core.process.definition.model.event.SBoundaryEventDefinition;
import org.bonitasoft.engine.core.process.instance.api.TokenService;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SToken;
import org.bonitasoft.engine.execution.flowmerger.TokenInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;



/**
 * @author Elias Ricken de Medeiros
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class BoundaryCreationTokenProviderTest {
    
    private static final long PARENT_TOKEN_REF_ID = 21L;

    private static final long PROCESS_INSTANCE_ID = 5L;

    private static final long FLOW_NODE_TOKEN_REF_ID = 6L;
    
    private static final long FLOW_NODE_INSTANCE_ID = 7L;
    
    private static final long BOUNDARY_DEFINITION_ID = 456L;
    
    @Mock 
    private TokenService tokenService;
    
    @Mock
    private SToken token;
    
    @Mock
    private SActivityInstance relatedActivityInstance;

    @Mock
    private SBoundaryEventDefinition boundaryDefinition;
    
    @InjectMocks
    BoundaryCreationTokenProvider tokenProvider;
    
    @Before
    public void setUp() throws Exception {
        doReturn(BOUNDARY_DEFINITION_ID).when(boundaryDefinition).getId();
        doReturn(PROCESS_INSTANCE_ID).when(relatedActivityInstance).getParentProcessInstanceId();
        doReturn(FLOW_NODE_TOKEN_REF_ID).when(relatedActivityInstance).getTokenRefId();
        doReturn(FLOW_NODE_INSTANCE_ID).when(relatedActivityInstance).getId();

        doReturn(token).when(tokenService).getToken(PROCESS_INSTANCE_ID, FLOW_NODE_TOKEN_REF_ID);
        doReturn(FLOW_NODE_TOKEN_REF_ID).when(token).getRefId();
        doReturn(PARENT_TOKEN_REF_ID).when(token).getParentRefId();
    }

    @Test
    public void getTokenInfo_returns_current_tokenInfo_for_interrupting_boundary_event() throws Exception {
        doReturn(true).when(boundaryDefinition).isInterrupting();
        
        TokenInfo tokenInfo = tokenProvider.getOutputTokenInfo();
        assertEquals(Long.valueOf(FLOW_NODE_TOKEN_REF_ID), tokenInfo.outputTokenRefId);
        assertEquals(Long.valueOf(PARENT_TOKEN_REF_ID), tokenInfo.outputParentTokenRefId);
    }

    @Test
    public void getTokenInfo_returns_new_tokenInfo_with_boundary_definition_id_for_non_interrupting_boundary_event() throws Exception {
        doReturn(false).when(boundaryDefinition).isInterrupting();
        
        TokenInfo tokenInfo = tokenProvider.getOutputTokenInfo();
        assertEquals(Long.valueOf(BOUNDARY_DEFINITION_ID), tokenInfo.outputTokenRefId);
        assertNull(tokenInfo.outputParentTokenRefId);
    }
}
