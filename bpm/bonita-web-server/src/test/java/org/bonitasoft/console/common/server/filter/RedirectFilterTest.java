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

import static org.mockito.Mockito.*;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class RedirectFilterTest {

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private HttpServletResponse httpResponse;

    @Mock
    private FilterChain filterChain;

    private RedirectFilter redirectFilter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        redirectFilter = new RedirectFilter();
    }

    @Test
    public void testDoFilterWithInternalRedirect() throws IOException, ServletException {
        when(httpRequest.getParameter("redirectUrl")).thenReturn("/redirectLink");
        when(httpRequest.getRequestURL()).thenReturn(new StringBuffer("http://bonita:8080/currentPath"));
        when(httpRequest.getRequestURI()).thenReturn("/currentPath");

        redirectFilter.doFilter(httpRequest, httpResponse, filterChain);

        verify(httpResponse).sendRedirect("/redirectLink");
        verify(filterChain, never()).doFilter(httpRequest, httpResponse);
    }

    @Test
    public void testDoFilterWithExternalRedirect() throws IOException, ServletException {
        // using http:// as a prefix to simulate an external redirect
        when(httpRequest.getParameter("redirectUrl")).thenReturn("http://external:9090/redirectLink");
        when(httpRequest.getRequestURL()).thenReturn(new StringBuffer("http://bonita:8080/currentPath"));
        when(httpRequest.getRequestURI()).thenReturn("/currentPath");

        redirectFilter.doFilter(httpRequest, httpResponse, filterChain);

        verify(httpResponse, never()).sendRedirect(anyString());
        verify(filterChain).doFilter(httpRequest, httpResponse);

        // using // as a prefix to simulate an external redirect
        when(httpRequest.getParameter("redirectUrl")).thenReturn("//external:9090/redirectLink");
        when(httpRequest.getRequestURL()).thenReturn(new StringBuffer("http://bonita:8080/currentPath"));
        when(httpRequest.getRequestURI()).thenReturn("/currentPath");

        redirectFilter.doFilter(httpRequest, httpResponse, filterChain);

        verify(httpResponse, never()).sendRedirect(anyString());
        verify(filterChain, times(2)).doFilter(httpRequest, httpResponse);
    }

    @Test
    public void testDoFilterWithoutRedirect() throws IOException, ServletException {
        when(httpRequest.getParameter("redirectUrl")).thenReturn(null);

        redirectFilter.doFilter(httpRequest, httpResponse, filterChain);

        verify(httpResponse, never()).sendRedirect(anyString());
        verify(filterChain).doFilter(httpRequest, httpResponse);
    }
}
