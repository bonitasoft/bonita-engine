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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
import org.mockito.ArgumentMatcher;
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
                uploadedFile.getName(),
                false);
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
    public void doPost_should_return_jarlessBar_indicator() throws Exception {
        // given
        // a bar zip file with a .jarless file inside
        final File barFile = File.createTempFile("app", ".bar");
        try (FileOutputStream fos = new FileOutputStream(barFile); ZipOutputStream zos = new ZipOutputStream(fos)) {
            ZipEntry jarlessEntry = new ZipEntry(".jarless");
            zos.putNextEntry(jarlessEntry);
            zos.closeEntry();
        }

        final FileItem fileItem = mock(FileItem.class);
        final ServletFileUpload serviceFileUpload = mock(ServletFileUpload.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final PrintWriter printer = mock(PrintWriter.class);
        final File tempFolder = File.createTempFile("upload", "");
        tempFolder.mkdir();

        //manage spy
        fileUploadServlet.uploadDirectoryPath = tempFolder.getAbsolutePath();
        doNothing().when(fileUploadServlet).defineUploadDirectoryPath(request);
        doReturn(serviceFileUpload).when(fileUploadServlet).createServletFileUpload(any(FileItemFactory.class));
        TemporaryContentAPI tempContentApi = mock(TemporaryContentAPI.class);
        doReturn("key").when(fileUploadServlet).storeTempFile(anyString(), any());
        doReturn(tempContentApi).when(fileUploadServlet).getTemporaryContentAPI();

        when(fileItem.getInputStream()).then(invoc -> new FileInputStream(barFile));
        when(fileItem.getName()).then(invoc -> barFile.getName());
        when(serviceFileUpload.parseRequest(request)).thenReturn(List.of(fileItem));
        when(request.getMethod()).thenReturn("post");
        when(request.getContentType()).thenReturn("multipart/");
        when(response.getWriter()).thenReturn(printer);

        when(fileUploadServlet.getServletConfig()).thenReturn(mock(ServletConfig.class));
        when(fileUploadServlet.getInitParameter(FileUploadServlet.RESPONSE_CONTENT_TYPE_PARAM))
                .thenReturn(FileUploadServlet.JSON_CONTENT_TYPE);
        when(fileUploadServlet.getInitParameter(FileUploadServlet.SUPPORTED_EXTENSIONS_PARAM))
                .thenReturn("bar");

        // when
        fileUploadServlet.init();
        fileUploadServlet.doPost(request, response);

        // then
        verify(response, never()).setStatus(anyInt());
        ArgumentMatcher<String> isStringWithJarless = s -> s
                .contains(String.format("\"%s\":true", FileUploadServlet.JARLESS_BAR_ATTRIBUTE));
        verify(printer).print(argThat(isStringWithJarless));
    }
}
