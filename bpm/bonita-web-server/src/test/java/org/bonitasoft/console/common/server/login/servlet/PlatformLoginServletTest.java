/**
 * Copyright (C) 2022 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.console.common.server.login.servlet;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.bonitasoft.console.common.server.auth.AuthenticationManager;
import org.bonitasoft.console.common.server.login.AuthenticationAuditLogger;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.platform.InvalidPlatformCredentialsException;
import org.bonitasoft.engine.platform.PlatformLoginException;
import org.bonitasoft.engine.session.PlatformSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class PlatformLoginServletTest {

    public static final String JOHN = "john";
    public static final String DOE = "doe";
    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private HttpServletResponse httpServletResponse;
    @Mock
    private PlatformLoginAPI platformLoginAPI;
    @Mock
    private HttpSession httpSession;
    @Mock
    private PlatformSession platformSession;
    @Mock
    private AuthenticationAuditLogger authenticationAuditLogger;
    @InjectMocks
    @Spy
    private PlatformLoginServlet platformLoginServlet;

    @Before
    public void before() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        doReturn(platformLoginAPI).when(platformLoginServlet).getPlatformLoginAPI();
        doReturn(JOHN).when(httpServletRequest).getParameter(PlatformLoginServlet.USERNAME_PARAM);
        doReturn(DOE).when(httpServletRequest).getParameter(PlatformLoginServlet.PASSWORD_PARAM);
    }

    @Test
    public void should_send_error_403_on_wrong_credential() throws Exception {
        //given
        doThrow(new InvalidPlatformCredentialsException("")).when(platformLoginAPI).login(JOHN, DOE);
        doReturn("true").when(httpServletRequest).getParameter(AuthenticationManager.REDIRECT_AFTER_LOGIN_PARAM_NAME);
        //when
        platformLoginServlet.doPost(httpServletRequest, httpServletResponse);
        //then
        verify(httpServletResponse).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    public void should_set_status_403_on_wrong_credential() throws Exception {
        //given
        doThrow(new InvalidPlatformCredentialsException("")).when(platformLoginAPI).login(JOHN, DOE);
        doReturn("false").when(httpServletRequest).getParameter(AuthenticationManager.REDIRECT_AFTER_LOGIN_PARAM_NAME);
        //when
        platformLoginServlet.doPost(httpServletRequest, httpServletResponse);
        //then
        verify(httpServletResponse).setStatus(eq(HttpServletResponse.SC_FORBIDDEN));
    }

    @Test(expected = ServletException.class)
    public void should_throw_exception_on_internal_error() throws Exception {
        //given
        doThrow(new PlatformLoginException("")).when(platformLoginAPI).login(JOHN, DOE);
        doReturn("false").when(httpServletRequest).getParameter(AuthenticationManager.REDIRECT_AFTER_LOGIN_PARAM_NAME);
        //when
        platformLoginServlet.doPost(httpServletRequest, httpServletResponse);
    }

    @Test
    public void doPost_should_write_a_login_success_audit_event() throws Exception {
        //given
        doReturn(platformSession).when(platformLoginAPI).login(JOHN, DOE);
        doReturn(JOHN).when(platformSession).getUserName();
        doReturn(42L).when(platformSession).getId();
        doReturn(httpSession).when(httpServletRequest).getSession();
        doReturn(authenticationAuditLogger).when(platformLoginServlet).getAuthenticationAuditLogger(httpServletRequest);
        //when
        platformLoginServlet.doPost(httpServletRequest, httpServletResponse);
        //then
        verify(authenticationAuditLogger).loginSucceeded(httpServletRequest, JOHN,
                AuthenticationAuditLogger.METHOD_PLATFORM, 42L);
    }

    @Test
    public void doPost_should_write_a_login_failure_audit_event_on_wrong_credential() throws Exception {
        //given
        doThrow(new InvalidPlatformCredentialsException("")).when(platformLoginAPI).login(JOHN, DOE);
        doReturn(authenticationAuditLogger).when(platformLoginServlet).getAuthenticationAuditLogger(httpServletRequest);
        //when
        platformLoginServlet.doPost(httpServletRequest, httpServletResponse);
        //then
        verify(authenticationAuditLogger).loginFailed(httpServletRequest, JOHN,
                AuthenticationAuditLogger.METHOD_PLATFORM, null);
        verifyNoMoreInteractions(authenticationAuditLogger);
    }

    @Test
    public void doPost_should_not_write_any_audit_event_when_no_username_is_sent() throws Exception {
        //given
        doReturn(null).when(httpServletRequest).getParameter(PlatformLoginServlet.USERNAME_PARAM);
        doThrow(new InvalidPlatformCredentialsException("")).when(platformLoginAPI).login(null, DOE);
        lenient().doReturn(authenticationAuditLogger).when(platformLoginServlet)
                .getAuthenticationAuditLogger(httpServletRequest);
        //when
        platformLoginServlet.doPost(httpServletRequest, httpServletResponse);
        //then
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verifyNoInteractions(authenticationAuditLogger);
    }

    @Test
    public void doPost_should_not_write_any_audit_event_on_internal_error() throws Exception {
        //given
        doThrow(new PlatformLoginException("")).when(platformLoginAPI).login(JOHN, DOE);
        lenient().doReturn(authenticationAuditLogger).when(platformLoginServlet)
                .getAuthenticationAuditLogger(httpServletRequest);
        //when
        assertThatThrownBy(() -> platformLoginServlet.doPost(httpServletRequest, httpServletResponse))
                .isInstanceOf(ServletException.class);
        //then
        verifyNoInteractions(authenticationAuditLogger);
    }
}
