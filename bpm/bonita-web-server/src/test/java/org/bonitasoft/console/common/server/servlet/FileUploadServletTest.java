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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.bonitasoft.engine.api.TemporaryContentAPI;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FileUploadServletTest {

    @Mock
    HttpServletRequest request;

    @Spy
    FileUploadServlet fileUploadServlet = new TenantFileUploadServlet();

    @Test
    public void generateResponseJson_should_return_valid_json() throws Exception {
        final File uploadedFile = mock(File.class);
        when(uploadedFile.getName()).thenReturn("uploadedFile.txt");
        when(fileUploadServlet.getServletConfig()).thenReturn(mock(ServletConfig.class));
        doReturn(mock(TemporaryContentAPI.class)).when(fileUploadServlet).getTemporaryContentAPI();
        when(fileUploadServlet.getInitParameter(FileUploadServlet.RETURN_ORIGINAL_FILENAME_PARAM)).thenReturn("true");
        fileUploadServlet.init();

        final String jsonResponse = fileUploadServlet.generateResponseJson(request, "originalFileName",
                "application/json",
                uploadedFile.getName());
        ObjectMapper mapper = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, String> jsonResponseMap = mapper.readValue(jsonResponse, Map.class);

        assertThat(jsonResponseMap).containsEntry(FileUploadServlet.FILE_NAME_RESPONSE_ATTRIBUTE, "originalFileName")
                .containsEntry(FileUploadServlet.TEMP_PATH_RESPONSE_ATTRIBUTE, "uploadedFile.txt")
                .containsEntry(FileUploadServlet.CONTENT_TYPE_ATTRIBUTE, "application/json");
    }

    @Test
    public void generateResponseString_should_return_valid_text() throws Exception {
        final File uploadedFile = mock(File.class);
        when(uploadedFile.getName()).thenReturn("uploadedFile.txt");
        when(fileUploadServlet.getServletConfig()).thenReturn(mock(ServletConfig.class));
        doReturn(mock(TemporaryContentAPI.class)).when(fileUploadServlet).getTemporaryContentAPI();
        when(fileUploadServlet.getInitParameter(FileUploadServlet.RETURN_ORIGINAL_FILENAME_PARAM)).thenReturn("true");
        fileUploadServlet.init();

        final String responseString = fileUploadServlet.generateResponseString(request, "originalFileName",
                uploadedFile.getName());

        assertThat(responseString).isEqualTo("uploadedFile.txt::originalFileName");
    }

    @Test
    public void getFilenameLastSegment_should_return_proper_filename() {
        // given
        final String filename = "C:\\Users\\Desktop\\process.bar";

        // when
        final String filenameLastSegment = fileUploadServlet.getFilenameLastSegment(filename);

        // then
        assertThat(filenameLastSegment).isEqualTo("process.bar");
    }

    @Test
    public void getFilenameLastSegment_should_return_proper_filename_for_linux_paths() {
        // given
        final String filename = "/Users/Deskt.op/process.bar";

        // when
        final String filenameLastSegment = fileUploadServlet.getFilenameLastSegment(filename);

        // then
        assertThat(filenameLastSegment).isEqualTo("process.bar");
    }

    @Test
    public void getFilenameLastSegment_should_return_an_empty_filename_for_parent_folder_filename() {
        // given
        final String filename = "../../../";

        // when
        final String filenameLastSegment = fileUploadServlet.getFilenameLastSegment(filename);

        // then
        assertThat(filenameLastSegment).isEmpty();
    }

    @Test
    public void doPost_should_reject_file_with_extension_outside_default_whitelist() throws Exception {
        // given
        final FileItem fileItem = mock(FileItem.class);
        final ServletFileUpload serviceFileUpload = mock(ServletFileUpload.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final PrintWriter printer = mock(PrintWriter.class);
        final File tempFolder = File.createTempFile("upload", "");
        tempFolder.mkdir();

        fileUploadServlet.uploadDirectoryPath = tempFolder.getAbsolutePath();
        doNothing().when(fileUploadServlet).defineUploadDirectoryPath(request);
        doReturn(serviceFileUpload).when(fileUploadServlet).createServletFileUpload(any(FileItemFactory.class));
        doReturn(mock(TemporaryContentAPI.class)).when(fileUploadServlet).getTemporaryContentAPI();
        doReturn(new String[] { "pdf" }).when(fileUploadServlet).getDefaultAllowedExtensions();

        when(fileItem.getName()).thenReturn("script.exe");
        when(serviceFileUpload.parseRequest(request)).thenReturn(List.of(fileItem));
        when(request.getMethod()).thenReturn("post");
        when(request.getContentType()).thenReturn("multipart/");
        when(response.getWriter()).thenReturn(printer);
        when(fileUploadServlet.getServletConfig()).thenReturn(mock(ServletConfig.class));

        // when
        fileUploadServlet.init();
        fileUploadServlet.doPost(request, response);

        // then
        verify(response).setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
        verify(fileUploadServlet, never()).storeTempFile(anyString(), any());
    }

    @Test
    public void doPost_should_reject_file_whose_actual_content_does_not_match_its_extension() throws Exception {
        // given
        final FileItem fileItem = mock(FileItem.class);
        final ServletFileUpload serviceFileUpload = mock(ServletFileUpload.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final PrintWriter printer = mock(PrintWriter.class);
        final File tempFolder = File.createTempFile("upload", "");
        tempFolder.mkdir();

        fileUploadServlet.uploadDirectoryPath = tempFolder.getAbsolutePath();
        doNothing().when(fileUploadServlet).defineUploadDirectoryPath(request);
        doReturn(serviceFileUpload).when(fileUploadServlet).createServletFileUpload(any(FileItemFactory.class));
        doReturn(mock(TemporaryContentAPI.class)).when(fileUploadServlet).getTemporaryContentAPI();
        doReturn(new String[] { "pdf" }).when(fileUploadServlet).getDefaultAllowedExtensions();

        // an HTML file renamed with a whitelisted ".pdf" extension, with a spoofed Content-Type
        final byte[] htmlBytes = "<html><body>evil</body></html>".getBytes();
        when(fileItem.getInputStream()).then(invoc -> new ByteArrayInputStream(htmlBytes));
        when(fileItem.getName()).thenReturn("payslip.pdf");
        when(fileItem.getContentType()).thenReturn("application/pdf");
        when(serviceFileUpload.parseRequest(request)).thenReturn(List.of(fileItem));
        when(request.getMethod()).thenReturn("post");
        when(request.getContentType()).thenReturn("multipart/");
        when(response.getWriter()).thenReturn(printer);
        when(fileUploadServlet.getServletConfig()).thenReturn(mock(ServletConfig.class));

        // when
        fileUploadServlet.init();
        fileUploadServlet.doPost(request, response);

        // then
        verify(response).setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
        verify(fileUploadServlet, never()).storeTempFile(anyString(), any());
    }

    @Test
    public void getDefaultAllowedExtensions_should_not_allow_everything_when_a_subclass_does_not_override_it() {
        // PlatformFileUploadServlet has no tenant ConsoleProperties to fall back on, and must still
        // not re-open CVE-68 if ever mapped to an endpoint without a SupportedExtensions init-param
        assertThat(new PlatformFileUploadServlet().getDefaultAllowedExtensions()).isNotEmpty();
    }
}
