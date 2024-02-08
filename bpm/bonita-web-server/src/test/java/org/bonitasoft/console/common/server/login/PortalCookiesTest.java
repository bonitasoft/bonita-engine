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
package org.bonitasoft.console.common.server.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PortalCookiesTest {

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    private PortalCookies portalCookies = spy(new PortalCookies());

    @Test
    public void should_add_csrf_cookie_to_response_with_context_path() throws Exception {

        doReturn(true).when(portalCookies).isCSRFTokenCookieSecure();
        doReturn("/bonita").when(request).getContextPath();

        portalCookies.addCSRFTokenCookieToResponse(request, response, "apiTokenFromClient");

        ArgumentCaptor<Cookie> argument = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(argument.capture());
        assertThat(argument.getValue().getValue()).isEqualTo("apiTokenFromClient");
        assertThat(argument.getValue().getPath()).isEqualTo("/bonita");
        assertThat(argument.getValue().getSecure()).isTrue();
    }

    @Test
    public void should_add_csrf_cookie_to_response_with_empty_context_path() throws Exception {

        doReturn(false).when(portalCookies).isCSRFTokenCookieSecure();
        doReturn("").when(request).getContextPath();

        portalCookies.addCSRFTokenCookieToResponse(request, response, "apiTokenFromClient");

        ArgumentCaptor<Cookie> argument = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(argument.capture());
        assertThat(argument.getValue().getValue()).isEqualTo("apiTokenFromClient");
        assertThat(argument.getValue().getPath()).isEqualTo("/");
        assertThat(argument.getValue().getSecure()).isFalse();
    }

    @Test
    public void should_add_csrf_cookie_to_response_with_specified_path() throws Exception {

        try {
            System.setProperty("bonita.csrf.cookie.path", "/");
            doReturn(false).when(portalCookies).isCSRFTokenCookieSecure();
            doReturn("/bonita").when(request).getContextPath();

            portalCookies.addCSRFTokenCookieToResponse(request, response, "apiTokenFromClient");

            ArgumentCaptor<Cookie> argument = ArgumentCaptor.forClass(Cookie.class);
            verify(response).addCookie(argument.capture());
            assertThat(argument.getValue().getValue()).isEqualTo("apiTokenFromClient");
            assertThat(argument.getValue().getPath()).isEqualTo("/");
            assertThat(argument.getValue().getSecure()).isFalse();
        } finally {
            System.clearProperty("bonita.csrf.cookie.path");
        }
    }
}
