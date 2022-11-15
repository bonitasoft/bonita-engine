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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.bonitasoft.console.common.server.auth.AuthenticationManager;
import org.bonitasoft.console.common.server.login.HttpServletRequestAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LogoutServletTest {

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletRequestAccessor requestAccessor;

    @Mock
    AuthenticationManager authenticationManager;

    @Spy
    LogoutServlet logoutServlet = new LogoutServlet();

    @Before
    public void setup() throws Exception {
        doReturn(request).when(requestAccessor).asHttpServletRequest();
        doReturn(authenticationManager).when(logoutServlet).getAuthenticationManager();
    }

    @Test
    public void testSanitizeLoginPageUrlEmptyLoginPageUrl() {
        String loginPage = logoutServlet.sanitizeLoginPageUrl("");

        assertThat(loginPage).isEqualToIgnoringCase("");
    }

    @Test
    public void testSanitizeLoginPageUrlFromMaliciousRedirectShouldReturnBrokenUrl() {
        try {
            logoutServlet.sanitizeLoginPageUrl("http://www.test.com");
            fail("building a login page with a different host on the redirectURL should fail");
        } catch (Exception e) {
            if (!(e.getCause() instanceof URISyntaxException)) {
                fail("Exception root cause should be a URISyntaxException");
            }
        }
    }

    @Test
    public void testSanitizeLoginPageUrlFromMaliciousRedirectShouldReturnBrokenUrl2() {
        String loginPage = logoutServlet.sanitizeLoginPageUrl("test.com");

        assertThat(loginPage).isEqualToIgnoringCase("test.com");
    }

    @Test
    public void testSanitizeLoginPageUrlShouldReturnCorrectUrl() {
        String loginPage = logoutServlet.sanitizeLoginPageUrl("apps/appDirectoryBonita?p=test#poutpout");

        assertThat(loginPage).isEqualToIgnoringCase("apps/appDirectoryBonita?p=test#poutpout");
    }

    @Test
    public void testGetURLToRedirectToFromAuthenticationManagerLogout() throws Exception {
        doReturn("redirectURL").when(logoutServlet).createRedirectUrl(requestAccessor);
        doReturn("logoutURL").when(authenticationManager).getLogoutPageURL(requestAccessor, "redirectURL");

        String loginPage = logoutServlet.getURLToRedirectTo(requestAccessor);

        assertThat(loginPage).isEqualTo("logoutURL");
    }

    @Test
    public void testGetURLToRedirectToFromAuthenticationManagerLogin() throws Exception {
        doReturn("loginURL").when(authenticationManager).getLoginPageURL(eq(requestAccessor), anyString());

        String loginPage = logoutServlet.getURLToRedirectTo(requestAccessor);

        assertThat(loginPage).isEqualTo("loginURL");
    }

    @Test
    public void testGetURLToRedirectToFromRequest() throws Exception {
        doReturn("redirectURLFromRequest").when(request).getParameter(AuthenticationManager.LOGIN_URL_PARAM_NAME);

        String loginPage = logoutServlet.getURLToRedirectTo(requestAccessor);

        assertThat(loginPage).isEqualTo("redirectURLFromRequest");
    }

    @Test
    public void testCreateRedirectUrlWithDefaultRedirect() throws Exception {
        doReturn(null).when(requestAccessor).getRedirectUrl();

        String redirectURL = logoutServlet.createRedirectUrl(requestAccessor);

        assertThat(redirectURL).isEqualTo(AuthenticationManager.DEFAULT_DIRECT_URL);
    }

    @Test
    public void testCreateRedirectUrl() throws Exception {
        doReturn("redirectURL").when(requestAccessor).getRedirectUrl();

        String redirectURL = logoutServlet.createRedirectUrl(requestAccessor);

        assertThat(redirectURL).isEqualTo("redirectURL");
    }

}
