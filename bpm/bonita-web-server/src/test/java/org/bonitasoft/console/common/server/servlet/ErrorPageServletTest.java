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
package org.bonitasoft.console.common.server.servlet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Paths;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.bonitasoft.engine.session.APISession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ErrorPageServletTest {

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    ServletContext sc;

    @Mock
    RequestDispatcher requestDispatcher;

    @Mock
    HttpSession session;

    @Mock
    APISession apiSession;

    @Spy
    ErrorPageServlet errorServlet = new ErrorPageServlet();

    StringWriter stringWriter;

    @Before
    public void setUp() throws Exception {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("apiSession")).thenReturn(apiSession);
        doReturn(true).when(errorServlet).isPlatformHealthy();
        stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
        File errorsHTMLFilePaths = Paths.get("src/main/webapp/WEB-INF/errors.html").toFile();
        InputStream errorPageInputStream = new FileInputStream(errorsHTMLFilePaths);
        when(sc.getResourceAsStream(ErrorPageServlet.ERROR_TEMPLATE_PATH)).thenReturn(errorPageInputStream);
        when(sc.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
        doReturn(sc).when(errorServlet).getServletContext();
    }

    @Test
    public void should_write_formatted_response() throws Exception {

        when(request.getPathInfo()).thenReturn("/404");
        when(request.getContextPath()).thenReturn("/bonita");

        errorServlet.doGet(request, response);

        assertThat(stringWriter.toString()).contains("Error 404");
        assertThat(stringWriter.toString()).contains(
                "src=\"/bonita/portal/resource/app/appDirectoryBonita/error-404/content/?_l=fr&amp;app=appDirectoryBonita\"");
        assertThat(stringWriter.toString()).contains("width=\"100%\"");
    }

    @Test
    public void should_write_formatted_response_with_root_contextPath() throws Exception {

        when(request.getPathInfo()).thenReturn("/500");
        when(request.getContextPath()).thenReturn("/");

        errorServlet.doGet(request, response);

        assertThat(stringWriter.toString()).contains(
                "src=\"/portal/resource/app/appDirectoryBonita/error-500/content/?_l=fr&amp;app=appDirectoryBonita\"");
    }

    @Test
    public void should_display_error_when_error_code_is_missing() throws Exception {

        when(request.getPathInfo()).thenReturn("");
        when(request.getContextPath()).thenReturn("/bonita");

        errorServlet.doGet(request, response);

        assertThat(stringWriter.toString()).contains("Status code missing from request.");
    }

    @Test
    public void should_forward_request_when_session_is_missing() throws Exception {

        when(request.getPathInfo()).thenReturn("/404");
        when(request.getContextPath()).thenReturn("/bonita");
        doReturn(false).when(errorServlet).isPlatformHealthy();

        errorServlet.doGet(request, response);

        verify(sc, times(1)).getRequestDispatcher("/404.jsp");
    }

    @Test
    public void should_forward_request_when_session_is_missing_forbidden_access() throws Exception {

        when(request.getPathInfo()).thenReturn("/403");
        when(request.getContextPath()).thenReturn("/bonita");
        doReturn(false).when(errorServlet).isPlatformHealthy();

        errorServlet.doGet(request, response);

        verify(sc, times(1)).getRequestDispatcher("/403.jsp");
    }

    @Test
    public void should_forward_request_when_platform_is_down() throws Exception {

        when(request.getPathInfo()).thenReturn("/500");
        when(request.getContextPath()).thenReturn("/bonita");
        when(session.getAttribute("apiSession")).thenReturn(null);

        errorServlet.doGet(request, response);

        verify(sc, times(1)).getRequestDispatcher("/500.jsp");
    }
}
