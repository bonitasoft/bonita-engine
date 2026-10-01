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
package org.bonitasoft.console.common.server.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.quality.Strictness.LENIENT;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ReadListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

/**
 * No {@code excludePattern} init-param is ever stubbed here on purpose: these tests exercise the filter's
 * own {@link SanitizerFilter#getDefaultExcludedPages()}, so they are behavioural rather than
 * change-detecting, and they cover the three deployment shapes — a hardcoded pattern in the descriptor's
 * init-param slot would silently stop matching on a renamed webapp, because
 * {@code URLExcludePattern} only context-substitutes the code-supplied default.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class SanitizerFilterExpressionEndpointExclusionTest {

    private static final String GROOVY_CONTENT = "List<Object> list = new ArrayList<>();";

    @Mock
    private FilterChain chain;

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private HttpServletResponse httpResponse;

    @Mock
    private FilterConfig filterConfig;

    @Mock
    private ServletContext servletContext;

    @Spy
    SanitizerFilter sanitizerFilter;

    @BeforeEach
    void setUp() {
        when(filterConfig.getServletContext()).thenReturn(servletContext);
        when(httpRequest.getMethod()).thenReturn("PUT");
        when(httpRequest.getContentType()).thenReturn("application/json");
        when(httpRequest.getCharacterEncoding()).thenReturn("UTF-8");
        doReturn(true).when(sanitizerFilter).isSanitizerEnabled();
        doReturn(Collections.emptyList()).when(sanitizerFilter).getAttributesExcluded();
    }

    @Test
    void doFilter_should_leave_groovy_script_content_untouched_when_updating_a_process_expression()
            throws Exception {
        deployedAt("/bonita");
        requesting("http://localhost:8080/bonita/API/bpm/process/123/expression/456");

        sanitizerFilter.doFilter(httpRequest, httpResponse, chain);

        assertThat(forwardedContent()).isEqualTo(GROOVY_CONTENT);
    }

    @Test
    void doFilter_should_leave_groovy_script_content_untouched_when_webapp_is_deployed_at_root()
            throws Exception {
        deployedAt("");
        requesting("http://localhost:8080/API/bpm/process/123/expression/456");

        sanitizerFilter.doFilter(httpRequest, httpResponse, chain);

        assertThat(forwardedContent()).isEqualTo(GROOVY_CONTENT);
    }

    @Test
    void doFilter_should_leave_groovy_script_content_untouched_when_webapp_is_renamed()
            throws Exception {
        deployedAt("/myportal");
        requesting("http://localhost:8080/myportal/API/bpm/process/123/expression/456");

        sanitizerFilter.doFilter(httpRequest, httpResponse, chain);

        assertThat(forwardedContent()).isEqualTo(GROOVY_CONTENT);
    }

    @Test
    void doFilter_should_still_sanitize_a_different_process_sub_resource() throws Exception {
        deployedAt("/bonita");
        requesting("http://localhost:8080/bonita/API/bpm/process/123/variable/456");

        sanitizerFilter.doFilter(httpRequest, httpResponse, chain);

        assertThat(forwardedContent()).isNotEqualTo(GROOVY_CONTENT).doesNotContain("<Object>");
    }

    @Test
    void doFilter_should_still_sanitize_when_the_expression_id_is_not_numeric() throws Exception {
        deployedAt("/bonita");
        requesting("http://localhost:8080/bonita/API/bpm/process/123/expression/not-an-id");

        sanitizerFilter.doFilter(httpRequest, httpResponse, chain);

        assertThat(forwardedContent()).isNotEqualTo(GROOVY_CONTENT).doesNotContain("<Object>");
    }

    @Test
    void doFilter_should_still_sanitize_when_the_process_id_is_not_numeric() throws Exception {
        deployedAt("/bonita");
        requesting("http://localhost:8080/bonita/API/bpm/process/abc/expression/456");

        sanitizerFilter.doFilter(httpRequest, httpResponse, chain);

        assertThat(forwardedContent()).isNotEqualTo(GROOVY_CONTENT).doesNotContain("<Object>");
    }

    /**
     * Pins the leading {@code ^} anchor. Without it the pattern would also match the custom-page route,
     * which really exists in this product and reaches the very same REST resource — so dropping the anchor
     * would silently widen the relaxation to a second URL shape.
     */
    @Test
    void doFilter_should_still_sanitize_when_the_api_path_is_not_at_the_root_of_the_context()
            throws Exception {
        deployedAt("/bonita");
        requesting("http://localhost:8080/bonita/portal/custom-page/API/bpm/process/123/expression/456");

        sanitizerFilter.doFilter(httpRequest, httpResponse, chain);

        assertThat(forwardedContent()).isNotEqualTo(GROOVY_CONTENT).doesNotContain("<Object>");
    }

    /**
     * Pins the trailing {@code $} anchor: a longer path that merely starts like the excluded one must stay
     * sanitized.
     */
    @Test
    void doFilter_should_still_sanitize_when_the_expression_url_has_a_trailing_segment() throws Exception {
        deployedAt("/bonita");
        requesting("http://localhost:8080/bonita/API/bpm/process/123/expression/456/extra");

        sanitizerFilter.doFilter(httpRequest, httpResponse, chain);

        assertThat(forwardedContent()).isNotEqualTo(GROOVY_CONTENT).doesNotContain("<Object>");
    }

    /**
     * A path-parameter traversal that resolves away from the excluded endpoint must not be excluded. This
     * only became security-relevant with the fix: before it, the exclusion branch was unreachable for this
     * filter, so a match had no consequence.
     */
    @Test
    void doFilter_should_still_sanitize_when_the_url_traverses_out_of_the_expression_endpoint()
            throws Exception {
        deployedAt("/bonita");
        requesting("http://localhost:8080/bonita/API/bpm/process/123/expression/456/..;/identity/user/1");

        sanitizerFilter.doFilter(httpRequest, httpResponse, chain);

        assertThat(forwardedContent()).isNotEqualTo(GROOVY_CONTENT).doesNotContain("<Object>");
    }

    private void deployedAt(String contextPath) throws ServletException {
        when(servletContext.getContextPath()).thenReturn(contextPath);
        sanitizerFilter.init(filterConfig);
    }

    private void requesting(String url) throws IOException {
        when(httpRequest.getRequestURL()).thenReturn(new StringBuffer(url));
        when(httpRequest.getInputStream()).thenReturn(asServletInputStream(
                new ObjectMapper().writeValueAsString(
                        Map.of("id", "456", "processDefinitionId", "123", "content", GROOVY_CONTENT))));
    }

    private String forwardedContent() throws IOException, ServletException {
        ArgumentCaptor<ServletRequest> requestCaptor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(chain, times(1)).doFilter(requestCaptor.capture(), any(ServletResponse.class));
        byte[] forwardedBody = requestCaptor.getValue().getInputStream().readAllBytes();
        return new ObjectMapper().readTree(forwardedBody).get("content").asText();
    }

    private static ServletInputStream asServletInputStream(String body) {
        var is = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
        return new ServletInputStream() {

            @Override
            public int read() throws IOException {
                return is.read();
            }

            @Override
            public boolean isFinished() {
                return is.available() == 0;
            }

            @Override
            public boolean isReady() {
                return !isFinished();
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                throw new UnsupportedOperationException("Unimplemented method 'setReadListener'");
            }
        };
    }
}
