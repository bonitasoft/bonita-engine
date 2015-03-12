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
package org.bonitasoft.engine.core.platform.login.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.core.platform.login.SPlatformLoginException;
import org.bonitasoft.engine.platform.authentication.PlatformAuthenticationService;
import org.bonitasoft.engine.platform.authentication.SInvalidPasswordException;
import org.bonitasoft.engine.platform.authentication.SInvalidUserException;
import org.bonitasoft.engine.platform.session.PlatformSessionService;
import org.bonitasoft.engine.platform.session.SSessionException;
import org.bonitasoft.engine.platform.session.SSessionNotFoundException;
import org.bonitasoft.engine.platform.session.model.SPlatformSession;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Celine Souchet
 * 
 */
public class PlatformLoginServiceImplTest {

    @Mock
    private PlatformAuthenticationService authenticationService;

    @Mock
    private PlatformSessionService sessionService;

    @InjectMocks
    private PlatformLoginServiceImpl platformLoginServiceImpl;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.platform.login.impl.PlatformLoginServiceImpl#login(java.lang.String, java.lang.String)}.
     */
    @Test
    public final void login() throws SPlatformLoginException, SSessionException, SInvalidUserException, SInvalidPasswordException {
        final String userName = "userName";
        final String password = "pwd";
        doNothing().when(authenticationService).checkUserCredentials(userName, password);
        final SPlatformSession sPlatformSession = mock(SPlatformSession.class);
        doReturn(sPlatformSession).when(sessionService).createSession(userName);

        assertEquals(sPlatformSession, platformLoginServiceImpl.login(userName, password));
    }

    @Test(expected = SPlatformLoginException.class)
    public final void loginThrowSInvalidUserException() throws SPlatformLoginException, SInvalidUserException, SInvalidPasswordException {
        final String userName = "userName";
        final String password = "pwd";
        doThrow(new SInvalidUserException("")).when(authenticationService).checkUserCredentials(userName, password);

        platformLoginServiceImpl.login(userName, password);
    }

    @Test(expected = SPlatformLoginException.class)
    public final void loginThrowSInvalidPasswordException() throws SPlatformLoginException, SInvalidUserException, SInvalidPasswordException {
        final String userName = "userName";
        final String password = "pwd";
        doThrow(new SInvalidPasswordException("")).when(authenticationService).checkUserCredentials(userName, password);

        platformLoginServiceImpl.login(userName, password);
    }

    @Test(expected = SPlatformLoginException.class)
    public final void loginThrowSSessionException() throws SPlatformLoginException, SSessionException, SInvalidUserException, SInvalidPasswordException {
        final String userName = "userName";
        final String password = "pwd";
        doNothing().when(authenticationService).checkUserCredentials(userName, password);
        final SPlatformSession sPlatformSession = mock(SPlatformSession.class);
        when(sessionService.createSession(userName)).thenReturn(sPlatformSession);
        doThrow(new SSessionException("")).when(sessionService).createSession(userName);

        assertEquals(sPlatformSession, platformLoginServiceImpl.login(userName, password));
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.platform.login.impl.PlatformLoginServiceImpl#logout(long)}.
     */
    @Test
    public final void logout() throws SSessionNotFoundException {
        final long sessionId = 415L;
        doNothing().when(sessionService).deleteSession(sessionId);

        platformLoginServiceImpl.logout(sessionId);
    }

    @Test(expected = SSessionNotFoundException.class)
    public final void logoutThrowSSessionException() throws SSessionNotFoundException {
        final long sessionId = 415L;
        doThrow(new SSessionNotFoundException("")).when(sessionService).deleteSession(sessionId);

        platformLoginServiceImpl.logout(sessionId);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.platform.login.impl.PlatformLoginServiceImpl#isValid(long)}.
     */
    @Test
    public final void isValid() throws SSessionNotFoundException {
        final long sessionId = 415L;
        doReturn(true).when(sessionService).isValid(sessionId);

        assertTrue(platformLoginServiceImpl.isValid(sessionId));
    }

    @Test
    public final void isNotValid() throws SSessionNotFoundException {
        final long sessionId = 415L;
        doReturn(false).when(sessionService).isValid(sessionId);

        assertFalse(platformLoginServiceImpl.isValid(sessionId));
    }

    @Test
    public final void isValidThrowException() throws SSessionNotFoundException {
        final long sessionId = 415L;
        doThrow(new SSessionNotFoundException("")).when(sessionService).isValid(sessionId);

        assertFalse(platformLoginServiceImpl.isValid(sessionId));
    }
}
