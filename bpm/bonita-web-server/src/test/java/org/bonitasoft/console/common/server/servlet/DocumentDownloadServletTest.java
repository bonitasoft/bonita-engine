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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;

import java.io.File;
import java.nio.file.Files;
import java.util.Date;

import javax.servlet.ServletException;

import org.bonitasoft.engine.session.APISession;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class DocumentDownloadServletTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Spy
    private DocumentDownloadServlet servlet;

    private MockHttpServletRequest request = new MockHttpServletRequest();
    private MockHttpServletResponse response = new MockHttpServletResponse();

    @Mock
    private APISession apiSession;

    private File processDir;

    @Before
    public void setUp() throws Exception {
        processDir = temporaryFolder.newFolder("test-process");
        File documentsDir = new File(processDir, DocumentDownloadServlet.BUSINESS_ARCHIVE_RESOURCES_DIRECTORY);
        documentsDir.mkdirs();
        Files.write(new File(documentsDir, "valid-resource.txt").toPath(), "test content".getBytes());

        request.getSession().setAttribute("apiSession", apiSession);

        doReturn(null).when(servlet).getMigrationDate(any(), anyLong());
        doReturn(new Date()).when(servlet).getProcessDefinitionDate(any(), anyLong());
        doReturn(processDir).when(servlet).getProcessResourceDir(any(), anyLong(), any());
    }

    @Test
    public void doGet_should_reject_path_traversal_in_resourceFileName() {
        request.setParameter("resourceFileName", "../../../etc/passwd");
        request.setParameter("process", "123");

        assertThatThrownBy(() -> servlet.doGet(request, response))
                .isInstanceOf(ServletException.class)
                .hasMessageContaining("security");
    }

    @Test
    public void doGet_should_reject_path_traversal_escaping_process_directory() {
        // ../../secret.txt resolves to <parent-of-processDir>/secret.txt, escaping processDir
        request.setParameter("resourceFileName", "../../secret.txt");
        request.setParameter("process", "123");

        assertThatThrownBy(() -> servlet.doGet(request, response))
                .isInstanceOf(ServletException.class)
                .hasMessageContaining("security");
    }

    @Test
    public void doGet_should_allow_valid_resource_path() throws Exception {
        request.setParameter("resourceFileName", "valid-resource.txt");
        request.setParameter("process", "123");

        servlet.doGet(request, response);

        assertThat(response.getContentAsByteArray()).isEqualTo("test content".getBytes());
    }
}
