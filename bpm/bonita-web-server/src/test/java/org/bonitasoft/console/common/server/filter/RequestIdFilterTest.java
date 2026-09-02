/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.bonitasoft.console.common.server.login.HttpServletRequestAccessor;
import org.bonitasoft.engine.mdc.MDCConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.slf4j.MDC;

/**
 * Test class RequestIdFilter
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class RequestIdFilterTest {

    @Mock
    private FilterChain chain;

    @Mock
    private HttpServletRequestAccessor request;

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

    @Spy
    RequestIdFilter requestIdFilter;

    @BeforeEach
    void setUp() {
        doReturn(httpSession).when(request).getHttpSession();
        when(request.asHttpServletRequest()).thenReturn(httpRequest);
        when(httpRequest.getMethod()).thenReturn("POST");
        when(httpRequest.getCharacterEncoding()).thenReturn("UTF-8");
        when(httpRequest.getRequestURL()).thenReturn(new StringBuffer());
        when(servletContext.getContextPath()).thenReturn("");
        when(filterConfig.getServletContext()).thenReturn(servletContext);
        when(filterConfig.getInitParameterNames()).thenReturn(Collections.emptyEnumeration());
    }

    @Test
    void shouldDetectIdsAttachedToRequest() throws Exception {
        String correlationId = Long.toHexString(System.nanoTime() - 10000000L);
        String requestId = Long.toHexString(System.nanoTime());

        when(httpRequest.getContentType()).thenReturn("application/json");
        when(httpRequest.getAttribute("track.requestId")).thenReturn(requestId);
        when(httpRequest.getAttribute("track.correlationId")).thenReturn(correlationId);

        Map<String, String> contextMap = new HashMap<>();
        doAnswer(invocation -> {
            contextMap.putAll(MDC.getCopyOfContextMap());
            return null;
        }).when(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));

        requestIdFilter.init(filterConfig);
        requestIdFilter.doFilter(httpRequest, httpResponse, chain);

        // method chain was called once and put REQUEST_ID & CORRELATION_REQUEST_ID in context
        verify(chain, times(1)).doFilter(any(ServletRequest.class), any(ServletResponse.class));
        assertThat(contextMap).containsEntry(MDCConstants.REQUEST_ID, requestId);
        assertThat(contextMap).containsEntry(MDCConstants.CORRELATION_REQUEST_ID, correlationId);
    }

    @Test
    void shouldDetectIdsInRequestHeader() throws Exception {
        String correlationId = Long.toHexString(System.nanoTime() - 10000000L);
        String requestId = Long.toHexString(System.nanoTime());

        when(httpRequest.getContentType()).thenReturn("application/json");
        when(httpRequest.getHeader("X-Request-ID")).thenReturn(requestId);
        when(httpRequest.getHeader("X-Correlation-ID")).thenReturn(correlationId);

        Map<String, String> contextMap = new HashMap<>();
        doAnswer(invocation -> {
            contextMap.putAll(MDC.getCopyOfContextMap());
            return null;
        }).when(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));

        requestIdFilter.init(filterConfig);
        requestIdFilter.doFilter(httpRequest, httpResponse, chain);

        // method chain was called once and put REQUEST_ID & CORRELATION_REQUEST_ID in context
        verify(chain, times(1)).doFilter(any(ServletRequest.class), any(ServletResponse.class));
        assertThat(contextMap).containsEntry(MDCConstants.REQUEST_ID, requestId);
        assertThat(contextMap).containsEntry(MDCConstants.CORRELATION_REQUEST_ID, correlationId);
    }

    @Test
    void shouldDetectUserAgent() throws Exception {
        String userAgent = "PostmanRuntime/7.43.0";
        when(httpRequest.getHeader("User-Agent")).thenReturn(userAgent);

        Map<String, String> contextMap = new HashMap<>();
        doAnswer(invocation -> {
            contextMap.putAll(MDC.getCopyOfContextMap());
            return null;
        }).when(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));

        requestIdFilter.init(filterConfig);
        requestIdFilter.doFilter(httpRequest, httpResponse, chain);

        // method chain was called once and put REQUEST_USER_AGENT_MDC_KEY in context
        verify(chain, times(1)).doFilter(any(ServletRequest.class), any(ServletResponse.class));
        assertThat(contextMap).containsEntry(MDCConstants.REQUEST_USER_AGENT_MDC_KEY, userAgent);
    }

    @Test
    void shouldIgnoreNullUserAgent() throws Exception {
        when(httpRequest.getHeader("User-Agent")).thenReturn(null);

        Map<String, String> contextMap = new HashMap<>();
        doAnswer(invocation -> {
            contextMap.putAll(MDC.getCopyOfContextMap());
            return null;
        }).when(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));

        requestIdFilter.init(filterConfig);
        requestIdFilter.doFilter(httpRequest, httpResponse, chain);

        // method chain was called once and put REQUEST_USER_AGENT_MDC_KEY in context
        verify(chain, times(1)).doFilter(any(ServletRequest.class), any(ServletResponse.class));
        assertThat(contextMap).doesNotContainKey(MDCConstants.REQUEST_USER_AGENT_MDC_KEY);
    }
}
