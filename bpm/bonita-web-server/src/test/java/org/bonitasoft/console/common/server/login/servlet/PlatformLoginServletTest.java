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

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bonitasoft.console.common.server.auth.AuthenticationManager;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.platform.InvalidPlatformCredentialsException;
import org.bonitasoft.engine.platform.PlatformLoginException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

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
}
