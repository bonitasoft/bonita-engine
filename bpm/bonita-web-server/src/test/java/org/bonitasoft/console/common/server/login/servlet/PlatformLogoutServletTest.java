/**
 * Copyright (C) 2026 Bonitasoft S.A.
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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.bonitasoft.console.common.server.login.AuthenticationAuditLogger;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.platform.PlatformLogoutException;
import org.bonitasoft.engine.session.PlatformSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlatformLogoutServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private PlatformLoginAPI platformLoginAPI;

    @Mock
    private PlatformSession platformSession;

    @Mock
    private AuthenticationAuditLogger authenticationAuditLogger;

    @Spy
    private PlatformLogoutServlet platformLogoutServlet = new PlatformLogoutServlet();

    @BeforeEach
    void setUp() {
        doReturn(session).when(request).getSession();
    }

    @Test
    void logout_should_write_a_logout_audit_event_once_logged_out_from_the_platform() throws Exception {
        doReturn(platformSession).when(session).getAttribute(PlatformLoginServlet.PLATFORM_SESSION_PARAM_KEY);
        doReturn("platformAdmin").when(platformSession).getUserName();
        doReturn(42L).when(platformSession).getId();
        doReturn(platformLoginAPI).when(platformLogoutServlet).getPlatformLoginAPI();
        doReturn(authenticationAuditLogger).when(platformLogoutServlet).getAuthenticationAuditLogger(request);

        platformLogoutServlet.logout(request, response);

        final InOrder inOrder = inOrder(platformLoginAPI, authenticationAuditLogger);
        inOrder.verify(platformLoginAPI).logout(platformSession);
        inOrder.verify(authenticationAuditLogger).loggedOut(request, "platformAdmin",
                AuthenticationAuditLogger.METHOD_PLATFORM, 42L);
        verify(session).invalidate();
    }

    @Test
    void logout_should_not_write_any_audit_event_without_platform_session() throws Exception {
        lenient().doReturn(authenticationAuditLogger).when(platformLogoutServlet).getAuthenticationAuditLogger(request);

        platformLogoutServlet.logout(request, response);

        verify(session).invalidate();
        verifyNoInteractions(authenticationAuditLogger);
    }

    @Test
    void logout_should_not_write_any_audit_event_when_platform_logout_fails() throws Exception {
        doReturn(platformSession).when(session).getAttribute(PlatformLoginServlet.PLATFORM_SESSION_PARAM_KEY);
        doReturn(platformLoginAPI).when(platformLogoutServlet).getPlatformLoginAPI();
        doThrow(new PlatformLogoutException("broken")).when(platformLoginAPI).logout(platformSession);
        lenient().doReturn(authenticationAuditLogger).when(platformLogoutServlet).getAuthenticationAuditLogger(request);

        platformLogoutServlet.logout(request, response);

        verify(session).invalidate();
        verifyNoInteractions(authenticationAuditLogger);
    }
}
