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
package org.bonitasoft.engine.session.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionProvider;
import org.bonitasoft.engine.session.model.impl.SSessionImpl;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionIdNotSetException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Celine Souchet
 */
public class SessionServiceImplTest {

    public static final long SESSION_ID = 1258l;

    public static final long TENANT_ID = 2l;

    public static final String USER_NAME = "john";

    public static final String APPLICATION_NAME = "bonita";

    public static final long USER_ID = 58l;

    @Mock
    private PlatformService platformService;

    @Mock
    private TechnicalLoggerService logger;

    @Mock
    private SessionProvider sessionProvider;

    @Mock
    private ReadSessionAccessor sessionAccessor;

    @InjectMocks
    private SessionServiceImpl sessionServiceImpl;
    private SSessionImpl sSession;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        sSession = new SSessionImpl(SESSION_ID, TENANT_ID, USER_NAME, APPLICATION_NAME, USER_ID);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.session.impl.SessionServiceImpl#getDefaultSessionDuration()}.
     */
    @Test
    public final void getDefaultSessionDuration() {
        assertEquals(3600000, sessionServiceImpl.getDefaultSessionDuration());
    }

    /**
     * Test method for {@link org.bonitasoft.engine.session.impl.SessionServiceImpl#getSessionDuration()}.
     */
    @Test
    public final void getSessionDuration() {
        assertEquals(3600000, sessionServiceImpl.getSessionDuration());
    }

    /**
     * Test method for {@link org.bonitasoft.engine.session.impl.SessionServiceImpl#cleanInvalidSessions()}.
     */
    @Test
    public final void cleanInvalidSessions() {
        sessionServiceImpl.cleanInvalidSessions();
        verify(sessionProvider, times(1)).cleanInvalidSessions();
    }

    /**
     * Test method for {@link org.bonitasoft.engine.session.impl.SessionServiceImpl#deleteSessions()}.
     */
    @Test
    public final void deleteSessions() {
        sessionServiceImpl.deleteSessions();
        verify(sessionProvider, times(1)).removeSessions();
    }

    @Test
    public final void deleteSessionsOfTenant() {
        sessionServiceImpl.deleteSessionsOfTenant(12l);
        verify(sessionProvider, times(1)).deleteSessionsOfTenant(12l, false);
    }

    @Test
    public final void deleteSessionsOfTenantExceptTechnicalUser() {
        sessionServiceImpl.deleteSessionsOfTenantExceptTechnicalUser(12l);
        verify(sessionProvider, times(1)).deleteSessionsOfTenant(12l, true);
    }

    @Test
    public final void should_getLoggedUserFromSession_return_user_id_when_there_is_a_session() throws Exception {
        //given
        doReturn(SESSION_ID).when(sessionAccessor).getSessionId();
        doReturn(sSession).when(sessionProvider).getSession(SESSION_ID);
        //when
        final long loggedUserFromSession = sessionServiceImpl.getLoggedUserFromSession(sessionAccessor);
        //when
        assertThat(loggedUserFromSession).isEqualTo(USER_ID);
    }

    @Test
    public final void should_getLoggedUserFromSession_return_minus_1_when_there_is_no_session() throws SessionIdNotSetException, SSessionNotFoundException {
        //given
        doThrow(SessionIdNotSetException.class).when(sessionAccessor).getSessionId();
        doReturn(sSession).when(sessionProvider).getSession(SESSION_ID);
        //when
        final long loggedUserFromSession = sessionServiceImpl.getLoggedUserFromSession(sessionAccessor);
        //when
        assertThat(loggedUserFromSession).isEqualTo(-1);

    }

    @Test
    public final void should_getLoggedUserFromSession_return_minus_1_when_the_session_is_not_found() throws SessionIdNotSetException, SSessionNotFoundException {
        //given
        doReturn(SESSION_ID).when(sessionAccessor).getSessionId();
        doThrow(SSessionNotFoundException.class).when(sessionProvider).getSession(SESSION_ID);
        //when
        final long loggedUserFromSession = sessionServiceImpl.getLoggedUserFromSession(sessionAccessor);
        //when
        assertThat(loggedUserFromSession).isEqualTo(-1);

    }
}
