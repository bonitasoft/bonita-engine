/**
 * Copyright (C) 2023 Bonitasoft S.A.
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

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bonitasoft.console.common.server.preferences.properties.SecurityProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FrameSecurityFilterTest {

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private HttpServletResponse httpResponse;

    @Mock
    private FilterConfig filterConfig;

    @Mock
    private FilterChain chain;

    @Mock
    private ServletContext servletContext;

    @Mock
    private SecurityProperties securityProperties;

    @Spy
    FrameSecurityFilter frameSecurityFilter;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        doReturn(securityProperties).when(frameSecurityFilter).getSecurityProperties();
        when(servletContext.getContextPath()).thenReturn("");
        when(filterConfig.getServletContext()).thenReturn(servletContext);
    }

    @Test
    public void should_add_default_headers() throws Exception {

        frameSecurityFilter.init(filterConfig);

        frameSecurityFilter.proceedWithFiltering(httpRequest, httpResponse, chain);

        verify(httpResponse).setHeader(FrameSecurityFilter.X_FRAME_OPTIONS_HEADER,
                FrameSecurityFilter.X_FRAME_OPTIONS_HEADER_DEFAULT);
        verify(httpResponse).setHeader(FrameSecurityFilter.CONTENT_SECURITY_POLICY_HEADER,
                FrameSecurityFilter.CONTENT_SECURITY_POLICY_HEADER_DEFAULT);
    }

    @Test
    public void should_add_configured_headers() throws Exception {

        doReturn("DENY").when(securityProperties).getXFrameOptionsHeader();
        doReturn("frame-ancestors 'none';").when(securityProperties).getContentSecurityPolicyHeader();
        frameSecurityFilter.init(filterConfig);

        frameSecurityFilter.proceedWithFiltering(httpRequest, httpResponse, chain);

        verify(httpResponse).setHeader(FrameSecurityFilter.X_FRAME_OPTIONS_HEADER, "DENY");
        verify(httpResponse).setHeader(FrameSecurityFilter.CONTENT_SECURITY_POLICY_HEADER, "frame-ancestors 'none';");
    }
}
