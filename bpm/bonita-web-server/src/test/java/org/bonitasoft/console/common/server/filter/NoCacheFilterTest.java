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

import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

public class NoCacheFilterTest {

    private NoCacheFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;
    private final Map<String, Object> attributes = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        attributes.clear();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/API/bpm/process"));
        when(request.getAttribute(anyString())).thenAnswer(inv -> attributes.get(inv.getArgument(0)));
        doAnswer(inv -> {
            attributes.put(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(request).setAttribute(anyString(), any());

        filter = new NoCacheFilter();
        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getContextPath()).thenReturn("");
        FilterConfig config = mock(FilterConfig.class);
        when(config.getFilterName()).thenReturn("NoCacheFilter");
        when(config.getInitParameterNames()).thenReturn(Collections.emptyEnumeration());
        when(config.getServletContext()).thenReturn(servletContext);
        filter.init(config);
    }

    @Test
    public void should_set_cache_control_headers() throws Exception {
        filter.doFilter(request, response, chain);

        verify(response).setHeader("Cache-Control", "no-store, no-cache, must-revalidate, proxy-revalidate");
        verify(chain).doFilter(request, response);
    }

    @Test
    public void should_not_reapply_headers_on_second_dispatch() throws Exception {
        filter.doFilter(request, response, chain);
        filter.doFilter(request, response, chain);

        // Headers set only once (first pass); second pass skips filter logic but still forwards
        verify(response, times(1)).setHeader("Cache-Control", "no-store, no-cache, must-revalidate, proxy-revalidate");
        verify(chain, times(2)).doFilter(request, response);
    }
}
