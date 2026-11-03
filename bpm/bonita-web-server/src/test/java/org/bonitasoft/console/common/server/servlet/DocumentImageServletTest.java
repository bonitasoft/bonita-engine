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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.bonitasoft.console.common.server.utils.BPMEngineAPIUtil;
import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.process.ProcessResourceNotFoundException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.io.FileContent;
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
    public void doGet_should_download_temp_file_when_fileName_param_has_an_active_content_extension()
            throws Exception {
        //given: a text file whose name is replaced by "x.html" in the URL (CVE-68)
        request.setParameter("filePath", "tempKey");
        request.setParameter("fileName", "x.html");
        storeTempContent("notes.txt", "<script>alert(document.domain)</script>");

        //when
        servlet.doGet(request, response);

        //then
        assertThat(response.getContentType()).startsWith("application/octet-stream");
        assertThat(response.getHeader("Content-Disposition")).startsWith("attachment;");
    }

    @Test
    public void doGet_should_download_stored_xml_temp_file() throws Exception {
        //given: an XML file would run XHTML-namespaced script if rendered inline as application/xml
        request.setParameter("filePath", "tempKey");
        storeTempContent("evil.xml",
                "<x:script xmlns:x=\"http://www.w3.org/1999/xhtml\">alert(document.domain)</x:script>");

        //when
        servlet.doGet(request, response);

        //then
        assertThat(response.getContentType()).startsWith("application/octet-stream");
        assertThat(response.getHeader("Content-Disposition")).startsWith("attachment;");
    }

    @Test
    public void doGet_should_download_svg_temp_file() throws Exception {
        //given
        request.setParameter("filePath", "tempKey");
        storeTempContent("logo.svg", "<svg xmlns=\"http://www.w3.org/2000/svg\"><script>alert(1)</script></svg>");

        //when
        servlet.doGet(request, response);

        //then
        assertThat(response.getContentType()).startsWith("application/octet-stream");
        assertThat(response.getHeader("Content-Disposition")).startsWith("attachment;");
    }

    @Test
    public void doGet_should_download_document_whose_file_name_has_an_active_content_extension() throws Exception {
        //given: the document file name comes from the submitted contract, not from the uploaded file
        request.setParameter("document", "12");
        final Document document = mock(Document.class);
        when(document.getContentFileName()).thenReturn("x.html");
        when(document.getContentStorageId()).thenReturn("34");
        when(processAPI.getDocument(12L)).thenReturn(document);
        when(processAPI.getDocumentContent("34")).thenReturn("<script>alert(1)</script>".getBytes());

        //when
        servlet.doGet(request, response);

        //then
        assertThat(response.getContentType()).startsWith("application/octet-stream");
        assertThat(response.getHeader("Content-Disposition")).startsWith("attachment;");
    }

    @Test
    public void doGet_should_display_images_and_pdf_inline_with_their_content_type() throws Exception {
        //given
        final Map<String, String> expectedContentTypes = Map.of(
                "photo.png", "image/png",
                "photo.JPG", "image/jpeg",
                "photo.jpeg", "image/jpeg",
                "anim.gif", "image/gif",
                "scan.bmp", "image/bmp",
                "photo.webp", "image/webp",
                "scan.tiff", "image/tiff",
                "report.pdf", "application/pdf");
        request.setParameter("filePath", "tempKey");

        for (final Map.Entry<String, String> expected : expectedContentTypes.entrySet()) {
            final MockHttpServletResponse fileResponse = new MockHttpServletResponse();
            storeTempContent(expected.getKey(), "content");

            //when
            servlet.doGet(request, fileResponse);

            //then
            assertThat(fileResponse.getContentType()).as(expected.getKey()).startsWith(expected.getValue());
            assertThat(fileResponse.getHeader("Content-Disposition")).as(expected.getKey()).startsWith("inline;");
            assertThat(fileResponse.getContentAsString()).as(expected.getKey()).isEqualTo("content");
        }
    }

    private void storeTempContent(final String fileName, final String content) throws Exception {
        final BonitaHomeFolderAccessor bonitaHomeFolderAccessor = mock(BonitaHomeFolderAccessor.class);
        final byte[] bytes = content.getBytes();
        when(bonitaHomeFolderAccessor.retrieveUploadedTempContent(anyString())).thenReturn(
                new FileContent(fileName, new ByteArrayInputStream(bytes), "text/plain", bytes.length));
        ReflectionTestUtils.setField(servlet, "bonitaHomeFolderAccessor", bonitaHomeFolderAccessor);
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
