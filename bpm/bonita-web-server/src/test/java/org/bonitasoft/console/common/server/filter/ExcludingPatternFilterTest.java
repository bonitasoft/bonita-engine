/**
 * Copyright (C) 2025 Bonitasoft S.A.
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
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

public class ExcludingPatternFilterTest {

    private int proceedCount;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;
    private final Map<String, Object> attributes = new HashMap<>();

    @Before
    public void setUp() {
        proceedCount = 0;
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
    }

    @Test
    public void should_execute_filter_logic_on_first_call() throws Exception {
        var filter = createFilter("MyFilter");

        filter.doFilter(request, response, chain);

        assertThat(proceedCount).isEqualTo(1);
        verifyNoInteractions(chain);
    }

    @Test
    public void should_skip_filter_logic_on_second_call() throws Exception {
        var filter = createFilter("MyFilter");

        filter.doFilter(request, response, chain);
        filter.doFilter(request, response, chain);

        assertThat(proceedCount).isEqualTo(1);
        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    public void should_allow_different_filter_instances_to_each_execute() throws Exception {
        var filterA = createFilter("FilterA");
        var filterB = createFilter("FilterB");

        filterA.doFilter(request, response, chain);
        filterB.doFilter(request, response, chain);

        assertThat(proceedCount).isEqualTo(2);
        verifyNoInteractions(chain);
    }

    @Test
    public void should_set_request_attribute_with_filter_name() throws Exception {
        var filter = createFilter("TokenValidatorFilter");

        filter.doFilter(request, response, chain);

        assertThat(attributes).containsKey("TokenValidatorFilter.FILTERED");
        assertThat(attributes.get("TokenValidatorFilter.FILTERED")).isEqualTo(Boolean.TRUE);
    }

    private ExcludingPatternFilter createFilter(String filterName) throws ServletException {
        ExcludingPatternFilter filter = new ExcludingPatternFilter() {

            @Override
            public void proceedWithFiltering(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws ServletException, IOException {
                proceedCount++;
            }

            @Override
            public String getDefaultExcludedPages() {
                return null;
            }
        };
        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getContextPath()).thenReturn("");
        FilterConfig config = mock(FilterConfig.class);
        when(config.getFilterName()).thenReturn(filterName);
        when(config.getInitParameterNames()).thenReturn(Collections.emptyEnumeration());
        when(config.getServletContext()).thenReturn(servletContext);
        filter.init(config);
        return filter;
    }
}
