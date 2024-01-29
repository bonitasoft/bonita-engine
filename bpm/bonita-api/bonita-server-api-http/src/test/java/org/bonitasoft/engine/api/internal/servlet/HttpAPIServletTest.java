/**
 * Copyright (C) 2021 Bonitasoft S.A.
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
package org.bonitasoft.engine.api.internal.servlet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class HttpAPIServletTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;

    @Rule
    public EnvironmentVariables envVar = new EnvironmentVariables();
    @Rule
    public RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    @Test
    public void should_send_403_when_disabled_using_env() throws Exception {
        HttpAPIServlet httpAPIServlet = spy(new HttpAPIServlet());
        envVar.set("HTTP_API", "false");

        httpAPIServlet.init();
        httpAPIServlet.doPost(request, response);

        verify(response).sendError(403);
    }

    @Test
    public void should_send_403_when_disabled_using_props() throws Exception {
        System.setProperty("http.api", "false");
        HttpAPIServlet httpAPIServlet = spy(new HttpAPIServlet());

        httpAPIServlet.init();
        httpAPIServlet.doPost(request, response);

        verify(response).sendError(403);
    }

    @Test
    public void should_not_send_403_when_enabled_using_props() throws Exception {
        System.setProperty("http.api", "true");
        HttpAPIServlet httpAPIServlet = spy(new HttpAPIServlet());
        doNothing().when(httpAPIServlet).callHttpApi(any(), any());

        httpAPIServlet.init();
        httpAPIServlet.doPost(request, response);

        verify(response, never()).sendError(anyInt());
    }

    @Test
    public void should_be_enabled_by_default() throws Exception {
        HttpAPIServlet httpAPIServlet = spy(new HttpAPIServlet());
        doNothing().when(httpAPIServlet).callHttpApi(any(), any());

        httpAPIServlet.init();
        httpAPIServlet.doPost(request, response);

        verify(response, never()).sendError(anyInt());
    }

}
