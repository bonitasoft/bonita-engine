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
package org.bonitasoft.engine.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;



@RunWith(MockitoJUnitRunner.class)
public class QueriableLogSessionProviderImplTest {
    
    /**
     * 
     */
    private static final long SESSION_ID = 5010L;

    @Mock
    private SessionService sessionService;

    @Mock
    private ReadSessionAccessor sessionAccessor;

    @Mock
    private TechnicalLoggerService technicalLoggerService;

    @Mock
    private SSession session;

    @InjectMocks
    private QueriableLogSessionProviderImpl sessionProvider;
    
    @Before
    public void setUp() throws Exception {
        doReturn(SESSION_ID).when(sessionAccessor).getSessionId();
    }
    
    @Test
    public void getUserId_should_return_session_user_name_if_there_is_a_session() throws Exception {
        //given
        doReturn(session).when(sessionService).getSession(SESSION_ID);
        doReturn("john").when(session).getUserName();
        
        //when
        String userId = sessionProvider.getUserId();

        //then
        assertThat(userId).isEqualTo("john");
    }

    @Test
    public void getUserId_should_return_system_if_there_is_no_session() throws Exception {
        //given
        doReturn(null).when(sessionService).getSession(SESSION_ID);
        
        //when
        String userId = sessionProvider.getUserId();
        
        //then
        assertThat(userId).isEqualTo("system");
    }
    
    @Test
    public void getClusterNode_should_return_the_session_cluster_node_if_there_is_a_session() throws Exception {
        //given
        doReturn(session).when(sessionService).getSession(SESSION_ID);
        doReturn("aNode").when(session).getClusterNode();

        //when
        String clusterNode = sessionProvider.getClusterNode();

        //then
        assertThat(clusterNode).isEqualTo("aNode");
    }
    
    @Test
    public void getClusterNode_should_return_empty_string_if_there_is_no_session() throws Exception {
        //given
        doReturn(null).when(sessionService).getSession(SESSION_ID);
        
        //when
        String clusterNode = sessionProvider.getClusterNode();
        
        //then
        assertThat(clusterNode).isEmpty();
    }
    
}
