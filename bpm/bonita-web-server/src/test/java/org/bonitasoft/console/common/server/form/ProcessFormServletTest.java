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
package org.bonitasoft.console.common.server.form;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.bonitasoft.engine.bpm.flownode.ActivityInstanceNotFoundException;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstanceNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.session.APISession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProcessFormServletTest {

    @Mock
    ProcessFormService processFormService;

    @Spy
    @InjectMocks
    ProcessFormServlet formServlet;

    @Mock(answer = Answers.RETURNS_MOCKS)
    HttpServletRequest hsRequest;

    @Mock
    HttpServletResponse hsResponse;

    @Mock
    HttpSession httpSession;

    @Mock
    APISession apiSession;

    @Before
    public void beforeEach() throws Exception {
        when(hsRequest.getContextPath()).thenReturn("/bonita");
        when(hsRequest.getSession()).thenReturn(httpSession);
        when(httpSession.getAttribute("apiSession")).thenReturn(apiSession);
        when(apiSession.getUserId()).thenReturn(1L);
    }

    @Test
    public void should_get_Bad_Request_when_invalid_parameters() throws Exception {
        when(hsRequest.getPathInfo()).thenReturn("");
        when(hsRequest.getParameter(anyString())).thenReturn(null);
        when(processFormService.getProcessDefinitionId(apiSession, null, null)).thenReturn(-1L);
        formServlet.doGet(hsRequest, hsResponse);
        verify(hsResponse, times(1)).sendError(400,
                "Either process name and version are required or process instance Id (with or without task name) or task instance Id.");
    }

    @Test
    public void should_redirect_to_page_servlet_for_process() throws Exception {
        when(hsRequest.getPathInfo()).thenReturn("/process/processName/processVersion");
        when(processFormService.getProcessDefinitionId(apiSession, "processName", "processVersion")).thenReturn(1L);
        when(processFormService.ensureProcessDefinitionId(apiSession, 1L, -1L, -1L)).thenReturn(1L);
        when(processFormService.getProcessPath(apiSession, 1L)).thenReturn("processName/processVersion");

        formServlet.doGet(hsRequest, hsResponse);

        verify(formServlet, times(1)).redirectToPageServlet(hsRequest, hsResponse, apiSession, 1L, -1L, -1L, null);
        verify(hsResponse, times(1))
                .encodeRedirectURL("/bonita/portal/resource/process/processName/processVersion/content/?id=1");
        verify(hsResponse, times(1)).sendRedirect(anyString());
    }

    @Test
    public void should_redirect_to_page_servlet_for_instance() throws Exception {
        when(hsRequest.getPathInfo()).thenReturn("/processInstance/42");
        when(processFormService.ensureProcessDefinitionId(apiSession, -1L, 42L, -1L)).thenReturn(1L);
        when(processFormService.getProcessPath(apiSession, 1L)).thenReturn("processName/processVersion");

        formServlet.doGet(hsRequest, hsResponse);

        verify(formServlet, times(1)).redirectToPageServlet(hsRequest, hsResponse, apiSession, 1L, 42L, -1L, null);
        verify(hsResponse, times(1))
                .encodeRedirectURL("/bonita/portal/resource/processInstance/processName/processVersion/content/?id=42");
        verify(hsResponse, times(1)).sendRedirect(anyString());
    }

    @Test
    public void should_redirect_to_page_servlet_for_task() throws Exception {
        when(hsRequest.getPathInfo()).thenReturn("/taskInstance/42");
        when(processFormService.ensureProcessDefinitionId(apiSession, -1L, -1L, 42L)).thenReturn(1L);
        when(processFormService.getTaskName(apiSession, 42L)).thenReturn("taskName");
        when(processFormService.encodePathSegment("taskName")).thenCallRealMethod();
        when(processFormService.getProcessPath(apiSession, 1L)).thenReturn("processName/processVersion");

        formServlet.doGet(hsRequest, hsResponse);

        verify(formServlet, times(1)).redirectToPageServlet(hsRequest, hsResponse, apiSession, 1L, -1L, 42L,
                "taskName");
        verify(hsResponse, times(1)).encodeRedirectURL(
                "/bonita/portal/resource/taskInstance/processName/processVersion/taskName/content/?id=42");
        verify(hsResponse, times(1)).sendRedirect(anyString());
    }

    @Test
    public void should_redirect_to_page_servlet_for_task_from_instance() throws Exception {
        when(hsRequest.getPathInfo()).thenReturn("/processInstance/42/task/taskName");
        when(processFormService.getTaskInstanceId(apiSession, 42L, "taskName", -1L)).thenReturn(1L);
        when(processFormService.ensureProcessDefinitionId(apiSession, -1L, 42L, 1L)).thenReturn(2L);
        when(processFormService.getTaskName(apiSession, 1L)).thenReturn("taskName");
        when(processFormService.encodePathSegment("taskName")).thenCallRealMethod();
        when(processFormService.getProcessPath(apiSession, 2L)).thenReturn("processName/processVersion");

        formServlet.doGet(hsRequest, hsResponse);

        verify(formServlet, times(1)).redirectToPageServlet(hsRequest, hsResponse, apiSession, 2L, 42L, 1L, "taskName");
        verify(hsResponse, times(1)).encodeRedirectURL(
                "/bonita/portal/resource/taskInstance/processName/processVersion/taskName/content/?id=1");
        verify(hsResponse, times(1)).sendRedirect(anyString());
    }

    @Test
    public void should_redirect_to_page_servlet_for_process_with_unicode_characters() throws Exception {
        when(hsRequest.getPathInfo()).thenReturn("/process/processus+%C3%A9%2B%C3%B8/%C3%B8");
        when(processFormService.getProcessDefinitionId(apiSession, "processus é+ø", "ø")).thenReturn(1L);
        when(processFormService.ensureProcessDefinitionId(apiSession, 1L, -1L, -1L)).thenReturn(1L);
        when(processFormService.getProcessPath(apiSession, 1L)).thenReturn("processus+%C3%A9%2B%C3%B8/%C3%B8");

        formServlet.doGet(hsRequest, hsResponse);

        verify(formServlet, times(1)).redirectToPageServlet(hsRequest, hsResponse, apiSession, 1L, -1L, -1L, null);
        verify(hsResponse, times(1))
                .encodeRedirectURL("/bonita/portal/resource/process/processus+%C3%A9%2B%C3%B8/%C3%B8/content/?id=1");
        verify(hsResponse, times(1)).sendRedirect(anyString());
    }

    @Test
    public void should_redirect_to_page_servlet_for_task_with_unicode_characters() throws Exception {
        when(hsRequest.getPathInfo()).thenReturn("/taskInstance/42");
        when(processFormService.ensureProcessDefinitionId(apiSession, -1L, -1L, 42L)).thenReturn(1L);
        when(processFormService.getTaskName(apiSession, 42L)).thenReturn("taskName é+ø");
        when(processFormService.encodePathSegment("taskName é+ø")).thenCallRealMethod();
        when(processFormService.getProcessPath(apiSession, 1L)).thenReturn("processName/processVersion");

        formServlet.doGet(hsRequest, hsResponse);

        verify(formServlet, times(1)).redirectToPageServlet(hsRequest, hsResponse, apiSession, 1L, -1L, 42L,
                "taskName é+ø");
        verify(hsResponse, times(1)).encodeRedirectURL(
                "/bonita/portal/resource/taskInstance/processName/processVersion/taskName%20%C3%A9+%C3%B8/content/?id=42");
        verify(hsResponse, times(1)).sendRedirect(anyString());
    }

    @Test
    public void should_redirect_to_page_servlet_for_task_with_unicode_characters_and_slash() throws Exception {
        when(hsRequest.getPathInfo()).thenReturn("/taskInstance/42");
        when(processFormService.ensureProcessDefinitionId(apiSession, -1L, -1L, 42L)).thenReturn(1L);
        when(processFormService.getTaskName(apiSession, 42L)).thenReturn("taskName/é+ø");
        when(processFormService.encodePathSegment("taskName/é+ø")).thenCallRealMethod();
        when(processFormService.getProcessPath(apiSession, 1L)).thenReturn("processName/processVersion");

        formServlet.doGet(hsRequest, hsResponse);

        verify(formServlet, times(1)).redirectToPageServlet(hsRequest, hsResponse, apiSession, 1L, -1L, 42L,
                "taskName/é+ø");
        verify(hsResponse, times(1)).encodeRedirectURL(
                "/bonita/portal/resource/taskInstance/processName/processVersion/taskName/%C3%A9+%C3%B8/content/?id=42");
        verify(hsResponse, times(1)).sendRedirect(anyString());
    }

    @Test
    public void should_get_not_found_when_invalid_process() throws Exception {
        when(hsRequest.getPathInfo()).thenReturn("/process/processName/processVersion/");
        when(processFormService.getProcessDefinitionId(apiSession, "processName", "processVersion"))
                .thenThrow(ProcessDefinitionNotFoundException.class);

        formServlet.doGet(hsRequest, hsResponse);

        verify(hsResponse, times(1)).sendError(404, "Cannot find the process");
    }

    @Test
    public void should_get_not_found_when_invalid_processInstanceId() throws Exception {
        when(hsRequest.getPathInfo()).thenReturn("/processInstance/42/");
        when(processFormService.ensureProcessDefinitionId(apiSession, -1L, 42L, -1L))
                .thenThrow(ArchivedProcessInstanceNotFoundException.class);

        formServlet.doGet(hsRequest, hsResponse);

        verify(hsResponse, times(1)).sendError(404, "Cannot find the process instance");
    }

    @Test
    public void should_get_not_found_when_invalid_task() throws Exception {
        when(hsRequest.getPathInfo()).thenReturn("/processInstance/42/task/taskName/");
        when(processFormService.getTaskInstanceId(apiSession, 42L, "taskName", -1L)).thenReturn(-1L);
        when(processFormService.ensureProcessDefinitionId(apiSession, -1L, 42L, -1L))
                .thenThrow(ActivityInstanceNotFoundException.class);

        formServlet.doGet(hsRequest, hsResponse);

        verify(hsResponse, times(1)).sendError(404, "Cannot find the task instance");
    }
}
