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

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartHttpServletRequest;

public class TokenValidatorFilterTest {

    public static final String SESSION_CSRF_TOKEN = "csrftoken";

    @Mock
    private FilterChain chain;

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private HttpServletResponse httpResponse;

    @Mock
    private HttpSession httpSession;

    @Mock
    private FilterConfig filterConfig;

    @Mock
    private ServletContext servletContext;

    private TokenValidatorFilter filter = spy(new TokenValidatorFilter());

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        doReturn(SESSION_CSRF_TOKEN).when(httpSession).getAttribute("api_token");
        doReturn(httpSession).when(httpRequest).getSession();
        when(filter.isCsrfProtectionEnabled()).thenReturn(true);
        when(servletContext.getContextPath()).thenReturn("");
        when(filterConfig.getServletContext()).thenReturn(servletContext);
        when(filterConfig.getInitParameterNames()).thenReturn(Collections.emptyEnumeration());
        filter.init(filterConfig);
    }

    @Test
    public void should_check_csrf_token_from_request_header() throws Exception {
        doReturn(SESSION_CSRF_TOKEN).when(httpRequest).getHeader("X-Bonita-API-Token");

        filter.proceedWithFiltering(httpRequest, httpResponse, chain);

        verify(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    public void should_not_check_csrf_token_for_GET_request() throws Exception {
        doReturn("GET").when(httpRequest).getMethod();

        filter.proceedWithFiltering(httpRequest, httpResponse, chain);

        verify(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    public void should_not_check_csrf_token_for_HEAD_request() throws Exception {
        doReturn("HEAD").when(httpRequest).getMethod();

        filter.proceedWithFiltering(httpRequest, httpResponse, chain);

        verify(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    public void should_not_check_csrf_token_for_OPTIONS_request() throws Exception {
        doReturn("OPTIONS").when(httpRequest).getMethod();

        filter.proceedWithFiltering(httpRequest, httpResponse, chain);

        verify(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    public void should_set_401_status_when_csrf_request_header_is_wrong() throws Exception {
        doReturn("notAValidToken").when(httpRequest).getHeader("X-Bonita-API-Token");

        filter.proceedWithFiltering(httpRequest, httpResponse, chain);

        verify(chain, never()).doFilter(any(ServletRequest.class), any(ServletResponse.class));
        verify(httpResponse).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void should_set_401_status_when_csrf_request_header_is_not_set() throws Exception {

        filter.proceedWithFiltering(httpRequest, httpResponse, chain);

        verify(chain, never()).doFilter(any(ServletRequest.class), any(ServletResponse.class));
        verify(httpResponse).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void should_check_csrf_token_from_request_parameter() throws Exception {
        doReturn(SESSION_CSRF_TOKEN).when(httpRequest).getParameter("CSRFToken");

        filter.proceedWithFiltering(httpRequest, httpResponse, chain);

        verify(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    public void should_set_401_status_when_csrf_request_parameter_is_wrong() throws Exception {
        doReturn("notAValidToken").when(httpRequest).getParameter("X-Bonita-API-Token");

        filter.proceedWithFiltering(httpRequest, httpResponse, chain);

        verify(chain, never()).doFilter(any(ServletRequest.class), any(ServletResponse.class));
        verify(httpResponse).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void should_check_csrf_token_from_request_form_data() throws Exception {
        MockHttpServletRequest multipartRequest = mockMultipartRequestFor(SESSION_CSRF_TOKEN);

        filter.proceedWithFiltering(multipartRequest, httpResponse, chain);

        verify(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    public void should_set_401_status_when_csrf_form_data_is_wrong() throws Exception {
        MockHttpServletRequest multipartRequest = mockMultipartRequestFor("notAValidToken");

        filter.proceedWithFiltering(multipartRequest, httpResponse, chain);

        verify(chain, never()).doFilter(any(ServletRequest.class), any(ServletResponse.class));
        verify(httpResponse).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void testFilterWithExcludedURL() throws Exception {
        final String url = "test";
        when(httpRequest.getRequestURL()).thenReturn(new StringBuffer(url));
        doReturn(true).when(filter).matchExcludePatterns(url);
        filter.doFilter(httpRequest, httpResponse, chain);
        verify(filter, times(0)).proceedWithFiltering(httpRequest, httpResponse, chain);
        verify(chain, times(1)).doFilter(httpRequest, httpResponse);
    }

    @Test
    public void testMatchExcludePatterns() throws Exception {
        matchExcludePattern("http://host/bonita/portal/resource/page/API/system/session/unusedId", true);
        matchExcludePattern("http://host/bonita/apps/app/API/system/session/unusedId", true);
        matchExcludePattern("http://host/bonita/API/system/session/unusedId", true);
        matchExcludePattern("http://host/bonita/portal/resource/page/API/system/i18ntranslation", true);
        matchExcludePattern("http://host/bonita/apps/app/API/system/i18ntranslation", true);
        matchExcludePattern("http://host/bonita/API/system/i18ntranslation", true);
        matchExcludePattern("http://host/bonita/API/bpm/process", false);
        matchExcludePattern("http://host/bonita/API/bpm/process/;session", false);
        matchExcludePattern("http://host/bonita/API/bpm/process/../../../API/system/session", false);
        matchExcludePattern("http://host/bonita/API/system/i18ntranslation/../../bpm/process", false);
        matchExcludePattern("http://host/bonita/API/bpm/activity/test/../i18ntranslation/..?p=0&c=10", false);
        matchExcludePattern("http://host/bonita/API/bpm/activity/i18ntranslation/../?p=0&c=10", false);
    }

    @Test
    public void testCompileNullPattern() throws Exception {
        assertThat(filter.compilePattern(null)).isNull();
    }

    @Test
    public void testCompileWrongPattern() throws Exception {
        assertThat(filter.compilePattern("((((")).isNull();
    }

    @Test
    public void testCompileSimplePattern() throws Exception {
        final String patternToCompile = "test";
        assertThat(filter.compilePattern(patternToCompile)).isNotNull().has(new Condition<>() {

            @Override
            public boolean matches(final Pattern pattern) {
                return pattern.pattern().equalsIgnoreCase(patternToCompile);
            }
        });
    }

    @Test
    public void testCompileExcludePattern() throws Exception {
        final String patternToCompile = TokenValidatorFilter.TOKEN_VALIDATOR_FILTER_EXCLUDED_PAGES_PATTERN;
        assertThat(filter.compilePattern(patternToCompile)).isNotNull().has(new Condition<>() {

            @Override
            public boolean matches(final Pattern pattern) {
                return pattern.pattern().equalsIgnoreCase(patternToCompile);
            }
        });
    }

    private void matchExcludePattern(final String urlToMatch, final Boolean mustMatch) {
        if (filter.matchExcludePatterns(urlToMatch) != mustMatch) {
            Assertions.fail("Matching excludePattern and the Url " + urlToMatch + " must return " + mustMatch);
        }
    }

    private MockHttpServletRequest mockMultipartRequestFor(String csrfToken) {
        MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
        request.setMethod("POST");
        request.getSession().setAttribute("api_token", SESSION_CSRF_TOKEN);

        String boundary = "----WebKitFormBoundaryRmUZEc9hkTjU1FKc";
        request.setContentType(format("multipart/form-data; boundary=%s", boundary));
        request.setContent(multipartContent(boundary, csrfToken));
        return request;
    }

    private byte[] multipartContent(String boundary, String csrfToken) {
        return new StringBuilder()
                .append("--").append(boundary).append("\r\n")
                .append("Content-Disposition: form-data; name=\"CSRFToken\"").append("\r\n")
                .append("\r\n")
                .append(csrfToken).append("\r\n")
                .append("--").append(boundary).append("--")
                .toString().getBytes(StandardCharsets.UTF_8);
    }
}
