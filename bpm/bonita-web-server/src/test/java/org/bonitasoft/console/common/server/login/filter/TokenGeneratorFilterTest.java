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
package org.bonitasoft.console.common.server.login.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

@RunWith(MockitoJUnitRunner.class)
public class TokenGeneratorFilterTest {

    @Mock
    private FilterChain filterChain;
    private final HttpSession session = new MockHttpSession();
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    final String contextPath = "bonitaTest";
    final String bonitaTokenName = "X-Bonita-API-Token";
    final String bonitaTokenValue = "sdfsdfjhvèzv";

    private final TokenGeneratorFilter tokenGeneratorFilter = new TokenGeneratorFilter();

    @Before
    public void setUp() {
        request.setSession(session);
        request.setContextPath(contextPath);
    }

    @Test
    public void should_add_security_token_in_headers_and_in_cookies_when_token_already_exists() throws Exception {
        session.setAttribute(TokenGenerator.API_TOKEN, bonitaTokenValue);
        response.addHeader(bonitaTokenName, bonitaTokenValue);

        tokenGeneratorFilter.doFilter(request, response, filterChain);

        final Cookie csrfCookie = response.getCookie(bonitaTokenName);

        assertThat(csrfCookie.getName()).isEqualTo(bonitaTokenName);
        assertThat(csrfCookie.getPath()).isEqualTo(contextPath);
        assertThat(csrfCookie.getValue()).isEqualTo(bonitaTokenValue);

        assertThat(session.getAttribute(TokenGenerator.API_TOKEN)).isEqualTo(bonitaTokenValue);
        assertThat(response.getHeader(bonitaTokenName)).isEqualTo(bonitaTokenValue);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void should_add_security_token_in_headers_and_in_cookies_when_no_previous_call() throws Exception {
        tokenGeneratorFilter.doFilter(request, response, filterChain);

        final Cookie csrfCookie = response.getCookie(bonitaTokenName);

        assertThat(csrfCookie.getName()).isEqualTo(bonitaTokenName);
        assertThat(csrfCookie.getPath()).isEqualTo(contextPath);
        assertThat(csrfCookie.getValue()).isNotEqualTo(bonitaTokenValue);

        assertThat(session.getAttribute(TokenGenerator.API_TOKEN)).isEqualTo(csrfCookie.getValue());
        assertThat(response.getHeader(bonitaTokenName)).isEqualTo(csrfCookie.getValue());

        verify(filterChain).doFilter(request, response);
    }
}
