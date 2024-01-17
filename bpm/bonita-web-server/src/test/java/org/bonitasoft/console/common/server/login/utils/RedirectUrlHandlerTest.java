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
package org.bonitasoft.console.common.server.login.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;

import javax.servlet.http.HttpServletRequest;

import org.bonitasoft.console.common.server.auth.AuthenticationManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RedirectUrlHandlerTest {

    @Mock
    HttpServletRequest req;

    @Test
    public void should_not_redirect_after_login_when_redirect_parameter_is_set_to_false() throws Exception {
        doReturn("false").when(req).getParameter(AuthenticationManager.REDIRECT_AFTER_LOGIN_PARAM_NAME);
        doReturn("anyurl").when(req).getParameter(AuthenticationManager.REDIRECT_URL);

        assertThat("should not redirect", !RedirectUrlHandler.shouldRedirectAfterLogin(req));
    }

    @Test
    public void should_redirect_after_login_when_redirect_url_is_set_in_request() throws Exception {
        doReturn("anyurl").when(req).getParameter(AuthenticationManager.REDIRECT_URL);

        assertThat("should redirect", RedirectUrlHandler.shouldRedirectAfterLogin(req));
    }

    @Test
    public void should_redirect_after_login_when_redirect_parameter_is_set_to_true() throws Exception {
        doReturn("true").when(req).getParameter(AuthenticationManager.REDIRECT_AFTER_LOGIN_PARAM_NAME);

        assertThat("should redirect", RedirectUrlHandler.shouldRedirectAfterLogin(req));
    }

    @Test
    public void should_not_redirect_after_logout_when_redirect_parameter_is_set_to_false() throws Exception {
        doReturn("false").when(req).getParameter(AuthenticationManager.REDIRECT_AFTER_LOGIN_PARAM_NAME);
        doReturn("anyurl").when(req).getParameter(AuthenticationManager.REDIRECT_URL);
        doReturn("anyurl").when(req).getParameter(AuthenticationManager.LOGIN_URL_PARAM_NAME);

        assertThat("should not redirect", !RedirectUrlHandler.shouldRedirectAfterLogout(req));
    }

    @Test
    public void should_redirect_after_logout_when_redirect_url_is_set_in_request() throws Exception {
        doReturn("anyurl").when(req).getParameter(AuthenticationManager.REDIRECT_URL);

        assertThat("should redirect", RedirectUrlHandler.shouldRedirectAfterLogout(req));
    }

    @Test
    public void should_redirect_after_logout_when_login_url_is_set_in_request() throws Exception {
        doReturn("anyurl").when(req).getParameter(AuthenticationManager.LOGIN_URL_PARAM_NAME);

        assertThat("should redirect", RedirectUrlHandler.shouldRedirectAfterLogout(req));
    }

    @Test
    public void should_redirect_after_logout_when_redirect_parameter_is_set_to_true() throws Exception {
        doReturn("true").when(req).getParameter(AuthenticationManager.REDIRECT_AFTER_LOGIN_PARAM_NAME);

        assertThat("should redirect", RedirectUrlHandler.shouldRedirectAfterLogout(req));
    }
}
