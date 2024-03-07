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

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.PrintWriter;
import java.net.HttpURLConnection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.bonitasoft.console.common.server.preferences.properties.ConsoleProperties;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TenantFileUploadServletTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Spy
    protected TenantFileUploadServlet fileUploadServlet;
    private ConsoleProperties consoleProperties;
    private HttpServletRequest request;

    @Before
    public void setUp() throws Exception {
        request = mock(HttpServletRequest.class);
        consoleProperties = mock(ConsoleProperties.class);

        doReturn(consoleProperties).when(fileUploadServlet).getConsoleProperties();
        fileUploadServlet.checkUploadedFileSize = false;
        fileUploadServlet.checkUploadedImageSize = false;
    }

    @Test
    public void should_set_maxFileSize_with_conf_value() throws Exception {
        final ServletFileUpload serviceFileUpload = mock(ServletFileUpload.class);
        fileUploadServlet.checkUploadedFileSize = true;

        when(consoleProperties.getMaxSize()).thenReturn(1L); // 1Mb
        fileUploadServlet.setUploadMaxSize(serviceFileUpload, request);
        verify(serviceFileUpload).setFileSizeMax(FileUploadServlet.MEGABYTE);
    }

    @Test
    public void should_set_maxImageSize_with_conf_value() throws Exception {
        final ServletFileUpload serviceFileUpload = mock(ServletFileUpload.class);
        fileUploadServlet.checkUploadedImageSize = true;

        when(consoleProperties.getImageMaxSizeInKB()).thenReturn(100L); // 1Mb
        fileUploadServlet.setUploadMaxSize(serviceFileUpload, request);
        verify(serviceFileUpload).setFileSizeMax(100 * FileUploadServlet.KILOBYTE);
    }

    @Test
    public void should_set_413_status_code_with_empty_body_when_file_creates_OOMError() throws Exception {
        final ServletFileUpload serviceFileUpload = mock(ServletFileUpload.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final PrintWriter printer = mock(PrintWriter.class);

        //manage spy
        fileUploadServlet.uploadDirectoryPath = tempFolder.getRoot().getAbsolutePath();
        fileUploadServlet.checkUploadedFileSize = true;
        doNothing().when(fileUploadServlet).defineUploadDirectoryPath(request);
        doReturn(serviceFileUpload).when(fileUploadServlet).createServletFileUpload(any(FileItemFactory.class));

        when(serviceFileUpload.parseRequest(request)).thenThrow(new OutOfMemoryError());
        when(request.getMethod()).thenReturn("post");
        when(request.getContentType()).thenReturn("multipart/");
        when(response.getWriter()).thenReturn(printer);

        fileUploadServlet.doPost(request, response);

        verify(response).setStatus(HttpURLConnection.HTTP_ENTITY_TOO_LARGE);
        verify(printer, never()).print(anyString());
        verify(printer, never()).flush();
    }

    @Test
    public void should_set_413_status_code_with_empty_body_when_file_is_too_big() throws Exception {
        final ServletFileUpload serviceFileUpload = mock(ServletFileUpload.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final PrintWriter printer = mock(PrintWriter.class);

        //manage spy
        fileUploadServlet.uploadDirectoryPath = tempFolder.getRoot().getAbsolutePath();
        doNothing().when(fileUploadServlet).defineUploadDirectoryPath(request);
        doReturn(serviceFileUpload).when(fileUploadServlet).createServletFileUpload(any(FileItemFactory.class));

        final FileSizeLimitExceededException exception = new FileSizeLimitExceededException(
                format("The field %s exceeds its maximum permitted size of %s bytes.",
                        "uploadedFile.zip", Long.valueOf(0)),
                20 * FileUploadServlet.MEGABYTE, 0);
        exception.setFileName("uploadedFile.zip");
        when(serviceFileUpload.parseRequest(request)).thenThrow(exception);
        when(request.getMethod()).thenReturn("post");
        when(request.getContentType()).thenReturn("multipart/");
        when(response.getWriter()).thenReturn(printer);

        fileUploadServlet.doPost(request, response);

        verify(response).setStatus(HttpURLConnection.HTTP_ENTITY_TOO_LARGE);
        verify(printer, never()).print(anyString());
        verify(printer, never()).flush();
    }

    @Test
    public void should_set_413_status_code_with_json_body_when_file_is_too_big_and_json_is_supported()
            throws Exception {
        final ServletFileUpload serviceFileUpload = mock(ServletFileUpload.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final PrintWriter printer = mock(PrintWriter.class);

        //manage spy
        fileUploadServlet.uploadDirectoryPath = tempFolder.getRoot().getAbsolutePath();
        fileUploadServlet.checkUploadedFileSize = true;
        fileUploadServlet.responseContentType = "json";
        doNothing().when(fileUploadServlet).defineUploadDirectoryPath(request);
        doReturn(serviceFileUpload).when(fileUploadServlet).createServletFileUpload(any(FileItemFactory.class));

        final FileSizeLimitExceededException exception = new FileSizeLimitExceededException(
                format("The field %s exceeds its maximum permitted size of %s bytes.",
                        "uploadedFile.zip", Long.valueOf(0)),
                20 * FileUploadServlet.MEGABYTE, 0);
        exception.setFileName("uploadedFile.zip");
        when(serviceFileUpload.parseRequest(request)).thenThrow(exception);
        when(request.getMethod()).thenReturn("post");
        when(request.getContentType()).thenReturn("multipart/");
        when(response.getWriter()).thenReturn(printer);

        fileUploadServlet.doPost(request, response);

        verify(response).setStatus(HttpURLConnection.HTTP_ENTITY_TOO_LARGE);
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(printer).print(captor.capture());
        assertThat(captor.getValue())
                .contains("\"statusCode\":413")
                .contains("\"message\":\"uploadedFile.zip is 20971520 large, limit is set to 0Mb\"")
                .contains("\"type\":\"EntityTooLarge\"");
        verify(printer).flush();
    }
}
