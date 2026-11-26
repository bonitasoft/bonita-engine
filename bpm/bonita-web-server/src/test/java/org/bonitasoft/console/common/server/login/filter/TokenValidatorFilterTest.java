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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.bonitasoft.console.common.server.filter.MultiReadHttpServletRequest;
import org.bonitasoft.console.common.server.preferences.constants.WebBonitaConstantsUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;
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
    public void proceedWithFiltering_should_not_log_the_csrf_token_values() throws Exception {
        doReturn("session-token-value").when(httpSession).getAttribute("api_token");
        doReturn("request-token-value").when(httpRequest).getHeader("X-Bonita-API-Token");
        Logger filterLogger = (Logger) LoggerFactory.getLogger(TokenValidatorFilter.class.getName());
        ListAppender<ILoggingEvent> logs = new ListAppender<>();
        logs.start();
        filterLogger.addAppender(logs);
        // the failed validation is logged at DEBUG: pin the level so that it is emitted, and captured
        filterLogger.setLevel(Level.DEBUG);
        try {
            filter.proceedWithFiltering(httpRequest, httpResponse, chain);
        } finally {
            filterLogger.detachAppender(logs);
            // back to the level of the test configuration, so that DEBUG does not leak to other tests
            filterLogger.setLevel(null);
        }

        verify(httpResponse).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(logs.list).extracting(ILoggingEvent::getFormattedMessage)
                .isNotEmpty()
                .noneMatch(
                        message -> message.contains("session-token-value") || message.contains("request-token-value"));
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
    public void proceedWithFiltering_should_not_read_the_body_when_the_session_has_no_csrf_token()
            throws Exception {
        // no login yet: the session holds no token, so no token sent with the request can match
        MockHttpServletRequest multipartRequest = mockMultipartRequestFor(SESSION_CSRF_TOKEN);
        multipartRequest.getSession().removeAttribute("api_token");
        byte[] sentBody = multipartRequest.getContentAsByteArray();

        filter.proceedWithFiltering(multipartRequest, httpResponse, chain);

        verify(httpResponse).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(multipartRequest.getInputStream().readAllBytes()).isEqualTo(sentBody);
    }

    @Test
    public void proceedWithFiltering_should_not_read_the_multipart_body_of_a_put_request() throws Exception {
        // ServletFileUpload, MultiReadHttpServletRequest and FileUploadServlet only handle POST requests as
        // multipart: other methods send the token in the header or as a parameter
        MockHttpServletRequest multipartRequest = mockMultipartRequestFor(SESSION_CSRF_TOKEN);
        multipartRequest.setMethod("PUT");
        byte[] sentBody = multipartRequest.getContentAsByteArray();

        filter.proceedWithFiltering(multipartRequest, httpResponse, chain);

        verify(chain, never()).doFilter(any(ServletRequest.class), any(ServletResponse.class));
        verify(httpResponse).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(multipartRequest.getInputStream().readAllBytes()).isEqualTo(sentBody);
    }

    @Test
    public void proceedWithFiltering_should_check_csrf_token_from_header_of_a_put_multipart_request()
            throws Exception {
        // the multipart field is ignored (its value is wrong here): the token comes from the header
        MockHttpServletRequest multipartRequest = mockMultipartRequestFor("notAValidToken");
        multipartRequest.setMethod("PUT");
        multipartRequest.addHeader("X-Bonita-API-Token", SESSION_CSRF_TOKEN);

        filter.proceedWithFiltering(multipartRequest, httpResponse, chain);

        verify(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
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
    public void should_check_csrf_token_from_form_data_when_request_encoding_is_unsupported() throws Exception {
        // unlike fileupload2 (which rejects the request charset before parsing), commons-fileupload 1.x
        // decodes the part headers with the platform default, and getFormFieldEncoding falls back to
        // UTF-8 for the field value: the token is still compared, and matches
        MockHttpServletRequest multipartRequest = mockMultipartRequestFor(SESSION_CSRF_TOKEN);
        multipartRequest.setCharacterEncoding("not-a-real-charset");

        filter.proceedWithFiltering(multipartRequest, httpResponse, chain);

        verify(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
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
    public void proceedWithFiltering_should_read_csrf_form_data_of_an_upper_case_content_type_whatever_the_default_locale()
            throws Exception {
        // media types are case-insensitive, and with a Turkish default locale "I".toLowerCase() is a dotless i
        MockHttpServletRequest multipartRequest = mockMultipartRequestFor(SESSION_CSRF_TOKEN);
        multipartRequest.setContentType(format("MULTIPART/FORM-DATA; boundary=%s", MULTIPART_BOUNDARY));
        final Locale defaultLocale = Locale.getDefault();
        Locale.setDefault(new Locale("tr", "TR"));
        try {
            filter.proceedWithFiltering(multipartRequest, httpResponse, chain);
        } finally {
            Locale.setDefault(defaultLocale);
        }

        verify(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    public void proceedWithFiltering_should_set_401_status_when_multipart_boundary_is_missing() throws Exception {
        // the parser rejects the content type itself: token extraction must fail closed, not propagate the error
        MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
        request.setMethod("POST");
        request.getSession().setAttribute("api_token", SESSION_CSRF_TOKEN);
        request.setContentType("multipart/form-data");
        request.setContent(multipartBody(csrfTokenPart(SESSION_CSRF_TOKEN)).getBytes(StandardCharsets.UTF_8));

        filter.proceedWithFiltering(request, httpResponse, chain);

        verify(chain, never()).doFilter(any(ServletRequest.class), any(ServletResponse.class));
        verify(httpResponse).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void proceedWithFiltering_should_not_write_the_parts_to_disk_to_read_csrf_form_data() throws Exception {
        // a part above the 10 KB in-memory threshold of a disk-based multipart parser
        MockHttpServletRequest request = mockMultipartRequestWithParts(
                filePart("x".repeat(64 * 1024)),
                csrfTokenPart(SESSION_CSRF_TOKEN));
        Set<String> partFilesBefore = partFileNames();

        filter.proceedWithFiltering(request, httpResponse, chain);

        // compared by name: the folder is shared, and files of other tests or processes may come and go
        assertThat(partFileNames()).isSubsetOf(partFilesBefore);
    }

    @Test
    public void proceedWithFiltering_should_set_401_status_when_the_multipart_body_is_truncated() throws Exception {
        // the body ends inside a part: the parser fails, and token extraction must fail closed
        MockHttpServletRequest request = mockMultipartRequest("--" + MULTIPART_BOUNDARY + "\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"upload.txt\"\r\n"
                + "Content-Type: text/plain\r\n"
                + "\r\n"
                + "content of a part that never ends");

        filter.proceedWithFiltering(request, httpResponse, chain);

        verify(chain, never()).doFilter(any(ServletRequest.class), any(ServletResponse.class));
        verify(httpResponse).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void proceedWithFiltering_should_fail_when_the_body_cannot_be_copied() throws Exception {
        // an I/O error while copying the body (a full disk, for example) is a server error, not a missing token
        doReturn("POST").when(httpRequest).getMethod();
        doReturn(format("multipart/form-data; boundary=%s", MULTIPART_BOUNDARY)).when(httpRequest).getContentType();
        doThrow(new IOException("No space left on device")).when(httpRequest).getInputStream();
        int bodyCopiesBefore = countBodyCopies();

        assertThatThrownBy(() -> filter.proceedWithFiltering(httpRequest, httpResponse, chain))
                .isInstanceOf(IOException.class)
                .hasMessage("No space left on device");

        verify(chain, never()).doFilter(any(ServletRequest.class), any(ServletResponse.class));
        verify(httpResponse, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // the copy file is created before the failing copy starts: it is deleted all the same
        assertThat(countBodyCopies()).isEqualTo(bodyCopiesBefore);
    }

    @Test
    public void proceedWithFiltering_should_not_leave_a_body_copy_when_csrf_form_data_is_missing()
            throws Exception {
        MockHttpServletRequest multipartRequest = mockMultipartRequest(
                multipartBody(filePart("uploaded file content")));
        int bodyCopiesBefore = countBodyCopies();

        filter.proceedWithFiltering(multipartRequest, httpResponse, chain);

        verify(httpResponse).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(countBodyCopies()).isEqualTo(bodyCopiesBefore);
    }

    @Test
    public void proceedWithFiltering_should_not_leave_a_body_copy_after_the_chain_read_the_body()
            throws Exception {
        String sentBody = multipartBody(csrfTokenPart(SESSION_CSRF_TOKEN), filePart("uploaded file content"));
        MockHttpServletRequest multipartRequest = mockMultipartRequest(sentBody);
        List<String> bodiesReadByTheChain = new ArrayList<>();
        doAnswer(invocation -> {
            ServletRequest forwardedRequest = invocation.getArgument(0);
            bodiesReadByTheChain.add(IOUtils.toString(forwardedRequest.getInputStream(), StandardCharsets.UTF_8));
            return null;
        }).when(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
        int bodyCopiesBefore = countBodyCopies();

        filter.proceedWithFiltering(multipartRequest, httpResponse, chain);

        assertThat(bodiesReadByTheChain).containsExactly(sentBody);
        assertThat(countBodyCopies()).isEqualTo(bodyCopiesBefore);
    }

    @Test
    public void proceedWithFiltering_should_not_leave_a_body_copy_when_the_chain_fails() throws Exception {
        MockHttpServletRequest multipartRequest = mockMultipartRequest(
                multipartBody(csrfTokenPart(SESSION_CSRF_TOKEN), filePart("uploaded file content")));
        doThrow(new ServletException("endpoint failure")).when(chain).doFilter(any(ServletRequest.class),
                any(ServletResponse.class));
        int bodyCopiesBefore = countBodyCopies();

        assertThatThrownBy(() -> filter.proceedWithFiltering(multipartRequest, httpResponse, chain))
                .isInstanceOf(ServletException.class);

        assertThat(countBodyCopies()).isEqualTo(bodyCopiesBefore);
    }

    @Test
    public void proceedWithFiltering_should_pass_the_request_unwrapped_when_csrf_token_is_in_header()
            throws Exception {
        MockHttpServletRequest multipartRequest = mockMultipartRequest(
                multipartBody(filePart("uploaded file content")));
        multipartRequest.addHeader("X-Bonita-API-Token", SESSION_CSRF_TOKEN);

        filter.proceedWithFiltering(multipartRequest, httpResponse, chain);

        verify(chain).doFilter(same(multipartRequest), any(ServletResponse.class));
    }

    @Test
    public void proceedWithFiltering_should_pass_the_request_unwrapped_when_csrf_token_is_a_parameter()
            throws Exception {
        MockHttpServletRequest multipartRequest = mockMultipartRequest(
                multipartBody(filePart("uploaded file content")));
        multipartRequest.addParameter("CSRFToken", SESSION_CSRF_TOKEN);

        filter.proceedWithFiltering(multipartRequest, httpResponse, chain);

        verify(chain).doFilter(same(multipartRequest), any(ServletResponse.class));
    }

    @Test
    public void proceedWithFiltering_should_reuse_a_request_wrapped_by_an_outer_pass() throws Exception {
        // a request forwarded after a first pass of the filter (REQUEST then FORWARD dispatch)
        MultiReadHttpServletRequest wrappedByOuterPass = new MultiReadHttpServletRequest(mockMultipartRequest(
                multipartBody(csrfTokenPart(SESSION_CSRF_TOKEN), filePart("uploaded file content"))));
        try {
            filter.proceedWithFiltering(wrappedByOuterPass, httpResponse, chain);

            verify(chain).doFilter(same(wrappedByOuterPass), any(ServletResponse.class));
        } finally {
            wrappedByOuterPass.cleanMultipartTempContent();
        }
    }

    @Test
    public void proceedWithFiltering_should_leave_the_body_copy_of_an_outer_pass_to_that_pass() throws Exception {
        MultiReadHttpServletRequest wrappedByOuterPass = new MultiReadHttpServletRequest(mockMultipartRequest(
                multipartBody(csrfTokenPart(SESSION_CSRF_TOKEN), filePart("uploaded file content"))));
        int bodyCopiesBefore = countBodyCopies();
        try {
            filter.proceedWithFiltering(wrappedByOuterPass, httpResponse, chain);

            // the outer pass is still running and may read the body again: only its own copy exists
            assertThat(countBodyCopies()).isEqualTo(bodyCopiesBefore + 1);
        } finally {
            wrappedByOuterPass.cleanMultipartTempContent();
        }
    }

    @Test
    public void proceedWithFiltering_should_reuse_a_body_copy_wrapped_by_another_filter() throws Exception {
        // a filter between the two passes (BonitaOIDCFilter, for example) wraps the request of the outer pass again
        MultiReadHttpServletRequest wrappedByOuterPass = new MultiReadHttpServletRequest(mockMultipartRequest(
                multipartBody(csrfTokenPart(SESSION_CSRF_TOKEN), filePart("uploaded file content"))));
        HttpServletRequest wrappedByAnotherFilter = new HttpServletRequestWrapper(wrappedByOuterPass);
        int bodyCopiesBefore = countBodyCopies();
        try {
            filter.proceedWithFiltering(wrappedByAnotherFilter, httpResponse, chain);

            verify(chain).doFilter(same(wrappedByAnotherFilter), any(ServletResponse.class));
            assertThat(countBodyCopies()).isEqualTo(bodyCopiesBefore + 1);
        } finally {
            wrappedByOuterPass.cleanMultipartTempContent();
        }
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
        return mockMultipartRequest(multipartBody(parts));
    }

    private MockHttpServletRequest mockMultipartRequest(String body) {
        MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
        request.setMethod("POST");
        request.getSession().setAttribute("api_token", SESSION_CSRF_TOKEN);
        request.setContentType(format("multipart/form-data; boundary=%s", MULTIPART_BOUNDARY));
        request.setContent(body.getBytes(StandardCharsets.UTF_8));
        return request;
    }

    private static String csrfTokenPart(String csrfToken) {
        return "--" + MULTIPART_BOUNDARY + "\r\n"
                + "Content-Disposition: form-data; name=\"CSRFToken\"\r\n"
                + "\r\n"
                + csrfToken + "\r\n";
    }

    private static String filePart(String content) {
        return "--" + MULTIPART_BOUNDARY + "\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"upload.txt\"\r\n"
                + "Content-Type: text/plain\r\n"
                + "\r\n"
                + content + "\r\n";
    }

    private static String multipartBody(String... parts) {
        return String.join("", parts) + "--" + MULTIPART_BOUNDARY + "--";
    }

    /**
     * Number of request body copies ({@code tmp_*.part}) in the folder MultiReadHttpServletRequest writes them to.
     * Counts are stable: that folder is specific to the JVM ({@code bonita_portal_<pid@host>}), and the tests of a
     * JVM run one after another. The shared JVM temporary folder of {@link #partFileNames()} needs names instead.
     */
    private static int countBodyCopies() {
        File[] bodyCopies = WebBonitaConstantsUtils.getPlatformInstance().getTempFolder()
                .listFiles((dir, name) -> name.startsWith("tmp_") && name.endsWith(".part"));
        return bodyCopies == null ? 0 : bodyCopies.length;
    }

    /**
     * Names of the parts ({@code upload_*.tmp}) written to the JVM temporary folder by a commons-fileupload
     * DiskFileItemFactory without repository, such as the one of Spring's CommonsMultipartResolver
     */
    private static Set<String> partFileNames() {
        String[] partFiles = new File(System.getProperty("java.io.tmpdir"))
                .list((dir, name) -> name.startsWith("upload_") && name.endsWith(".tmp"));
        return partFiles == null ? Set.of() : Set.of(partFiles);
    }
}
