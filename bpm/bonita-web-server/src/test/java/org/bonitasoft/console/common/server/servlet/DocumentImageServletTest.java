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
package org.bonitasoft.console.common.server.servlet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.bonitasoft.console.common.server.utils.BPMEngineAPIUtil;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.process.ProcessResourceNotFoundException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.session.APISession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class DocumentImageServletTest {

    @Spy
    private DocumentImageServlet servlet;

    private MockHttpServletRequest request = new MockHttpServletRequest();
    private MockHttpServletResponse response = new MockHttpServletResponse();

    @Mock
    private APISession apiSession;

    @Mock
    private BPMEngineAPIUtil bpmEngineAPIUtil;

    @Mock
    private ProcessAPI processAPI;

    @Before
    public void setUp() throws Exception {
        request.getSession().setAttribute("apiSession", apiSession);
        ReflectionTestUtils.setField(servlet, "bpmEngineAPIUtil", bpmEngineAPIUtil);
        when(bpmEngineAPIUtil.getProcessAPI(apiSession)).thenReturn(processAPI);
    }

    // Path traversal is rejected at the DB layer: the resource name is looked up by exact match,
    // so traversal paths like "../../../etc/passwd" simply won't match any stored resource name.
    @Test
    public void doGet_should_return_404_for_path_traversal_in_resourceFileName() throws Exception {
        request.setParameter("resourceFileName", "../../../etc/passwd");
        request.setParameter("process", "123");
        when(processAPI.getDocumentProcessResource(anyLong(), anyString()))
                .thenThrow(
                        new ProcessResourceNotFoundException("No resource named ../../../etc/passwd in process 123"));

        servlet.doGet(request, response);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void doGet_should_return_404_for_path_traversal_escaping_process_directory() throws Exception {
        request.setParameter("resourceFileName", "../../secret.txt");
        request.setParameter("process", "123");
        when(processAPI.getDocumentProcessResource(anyLong(), anyString()))
                .thenThrow(new ProcessResourceNotFoundException("No resource named ../../secret.txt in process 123"));

        servlet.doGet(request, response);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void doGet_should_return_400_when_no_process_instance_or_task_param() throws Exception {
        request.setParameter("resourceFileName", "valid-image.png");

        servlet.doGet(request, response);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void doGet_should_allow_valid_resource_path() throws Exception {
        request.setParameter("resourceFileName", "valid-image.png");
        request.setParameter("process", "123");
        when(processAPI.getDocumentProcessResource(anyLong(), anyString()))
                .thenReturn("fake-image-content".getBytes());

        servlet.doGet(request, response);

        assertThat(response.getContentAsByteArray()).isEqualTo("fake-image-content".getBytes());
    }

    @Test
    public void doGet_should_wrap_RetrieveException_in_ServletException() throws Exception {
        request.setParameter("resourceFileName", "valid-image.png");
        request.setParameter("process", "123");
        when(processAPI.getDocumentProcessResource(anyLong(), anyString()))
                .thenThrow(new RetrieveException("DB error"));

        assertThatThrownBy(() -> servlet.doGet(request, response))
                .isInstanceOf(ServletException.class)
                .hasCauseInstanceOf(RetrieveException.class);
    }
}
