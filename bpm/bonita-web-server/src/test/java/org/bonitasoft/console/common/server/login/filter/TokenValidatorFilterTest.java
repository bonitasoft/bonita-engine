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
import static org.mockito.ArgumentMatchers.any;
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

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartHttpServletRequest;

public class TokenValidatorFilterTest {

    public static final String SESSION_CSRF_TOKEN = "csrftoken";

    private static final String MULTIPART_BOUNDARY = "----WebKitFormBoundaryRmUZEc9hkTjU1FKc";

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
    public void should_not_check_csrf_token_for_bearer_request() throws Exception {
        doReturn("true").when(httpSession).getAttribute(TokenValidatorFilter.BEARER_HEADER_VERIFIED_ATTRIBUTE);
        doReturn("POST").when(httpRequest).getMethod();

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
    public void should_check_csrf_token_from_form_data_placed_after_a_file_part() throws Exception {
        // realistic browser ordering: the file part comes BEFORE the CSRFToken field, so the
        // iterator has to stream past the file part to reach the token
        MockHttpServletRequest request = mockMultipartRequestWithParts(
                filePart("x".repeat(64 * 1024)),
                csrfTokenPart(SESSION_CSRF_TOKEN));

        filter.proceedWithFiltering(request, httpResponse, chain);

        verify(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    public void should_set_401_status_when_request_encoding_is_unsupported() throws Exception {
        // fileupload2 validates the request charset before parsing: an unsupported encoding
        // makes the parser throw, and token extraction fails closed (no token -> 401) before
        // the getFormFieldEncoding UTF-8 fallback is ever consulted
        MockHttpServletRequest multipartRequest = mockMultipartRequestFor(SESSION_CSRF_TOKEN);
        multipartRequest.setCharacterEncoding("not-a-real-charset");

        filter.proceedWithFiltering(multipartRequest, httpResponse, chain);

        verify(chain, never()).doFilter(any(ServletRequest.class), any(ServletResponse.class));
        verify(httpResponse).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void should_set_401_status_when_csrf_form_data_is_oversized() throws Exception {
        // a crafted CSRFToken form field way beyond the buffering cap must be rejected
        // (truncated read -> token mismatch -> 401) without buffering it fully in memory
        String oversizedToken = SESSION_CSRF_TOKEN + "x".repeat(1024 * 1024);
        MockHttpServletRequest multipartRequest = mockMultipartRequestFor(oversizedToken);

        filter.proceedWithFiltering(multipartRequest, httpResponse, chain);

        verify(chain, never()).doFilter(any(ServletRequest.class), any(ServletResponse.class));
        verify(httpResponse).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void should_set_401_status_when_multipart_body_is_not_parseable() throws Exception {
        // a request advertising multipart/form-data whose body contains no boundary at all:
        // token extraction must fail closed (no token -> 401), not propagate the parse error
        MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
        request.setMethod("POST");
        request.getSession().setAttribute("api_token", SESSION_CSRF_TOKEN);
        request.setContentType("multipart/form-data; boundary=----expectedBoundary");
        request.setContent("this is not a valid multipart body".getBytes(StandardCharsets.UTF_8));

        filter.proceedWithFiltering(request, httpResponse, chain);

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
    public void testSemicolonTraversalUrlsMustNotBeExcluded() throws Exception {
        // Semicolon traversal patterns must NOT bypass the CSRF token validation filter
        matchExcludePattern("http://host/bonita/apps/FAKE/API/system/session/..;/..;/..;/serverAPI/something", false);
        matchExcludePattern("http://host/bonita/apps/FAKE/API/..;/..;/serverAPI/something", false);
        matchExcludePattern(
                "http://host/bonita/portal/resource/page/API/system/session/..;/..;/..;/serverAPI/something", false);
    }

    @Test
    public void testPercentEncodedSemicolonTraversalUrlsMustNotBeExcluded() throws Exception {
        // Percent-encoded semicolons (%3b / %3B) must also NOT bypass the CSRF token validation filter
        matchExcludePattern("http://host/bonita/apps/FAKE/API/system/session/..%3b/..%3b/..%3b/serverAPI/something",
                false);
        matchExcludePattern("http://host/bonita/apps/FAKE/API/..%3b/..%3b/serverAPI/something", false);
        matchExcludePattern(
                "http://host/bonita/portal/resource/page/API/system/session/..%3B/..%3B/..%3B/serverAPI/something",
                false);
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
        return mockMultipartRequestWithParts(csrfTokenPart(csrfToken));
    }

    private MockHttpServletRequest mockMultipartRequestWithParts(String... parts) {
        MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
        request.setMethod("POST");
        request.getSession().setAttribute("api_token", SESSION_CSRF_TOKEN);
        request.setContentType(format("multipart/form-data; boundary=%s", MULTIPART_BOUNDARY));
        request.setContent((String.join("", parts) + "--" + MULTIPART_BOUNDARY + "--")
                .getBytes(StandardCharsets.UTF_8));
        return request;
    }

    private String csrfTokenPart(String csrfToken) {
        return "--" + MULTIPART_BOUNDARY + "\r\n"
                + "Content-Disposition: form-data; name=\"CSRFToken\"\r\n"
                + "\r\n"
                + csrfToken + "\r\n";
    }

    private String filePart(String content) {
        return "--" + MULTIPART_BOUNDARY + "\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"data.bin\"\r\n"
                + "Content-Type: application/octet-stream\r\n"
                + "\r\n"
                + content + "\r\n";
    }
}
