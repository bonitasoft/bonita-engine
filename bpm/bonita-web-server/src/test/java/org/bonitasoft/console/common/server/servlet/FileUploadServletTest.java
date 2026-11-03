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
import java.io.StringWriter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.bonitasoft.console.common.server.filter.MultiReadHttpServletRequest;
import org.bonitasoft.engine.api.TemporaryContentAPI;
import org.bonitasoft.engine.io.FileContent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FileUploadServletTest {

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    TemporaryContentAPI temporaryContentAPI;

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

    @Test
    public void doPost_should_accept_any_extension_when_allowed_extensions_contain_a_wildcard() throws Exception {
        //given
        doReturn(new String[] { "pdf", " * " }).when(fileUploadServlet).getDefaultAllowedExtensions();

        //when
        upload("archive.7z", "application/x-7z-compressed", "7z content".getBytes());

        //then
        verify(response, never()).setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
        verify(temporaryContentAPI).storeTempFile(any(FileContent.class));
    }

    @Test
    public void doPost_should_accept_file_without_extension_when_allowed_extensions_contain_a_wildcard()
            throws Exception {
        //given
        doReturn(new String[] { "*" }).when(fileUploadServlet).getDefaultAllowedExtensions();

        //when
        upload("README", "text/plain", "read me".getBytes());

        //then
        verify(response, never()).setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
        verify(temporaryContentAPI).storeTempFile(any(FileContent.class));
    }

    @Test
    public void doPost_should_still_check_content_of_known_extensions_when_allowed_extensions_contain_a_wildcard()
            throws Exception {
        //given
        doReturn(new String[] { "*" }).when(fileUploadServlet).getDefaultAllowedExtensions();

        //when
        upload("payslip.pdf", "application/pdf", "<html><body>evil</body></html>".getBytes());

        //then
        verify(response).setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
        verify(temporaryContentAPI, never()).storeTempFile(any(FileContent.class));
    }

    @Test
    public void doPost_should_store_and_return_usual_content_type_when_client_declares_octet_stream()
            throws Exception {
        //given
        doReturn(new String[] { "pdf" }).when(fileUploadServlet).getDefaultAllowedExtensions();
        doReturn("json").when(fileUploadServlet).getInitParameter(FileUploadServlet.RESPONSE_CONTENT_TYPE_PARAM);

        //when
        final String responseBody = upload("payslip.pdf", "application/octet-stream",
                new byte[] { 0x25, 0x50, 0x44, 0x46, '-', '1', '.', '4' });

        //then
        final ArgumentCaptor<FileContent> storedFile = ArgumentCaptor.forClass(FileContent.class);
        verify(temporaryContentAPI).storeTempFile(storedFile.capture());
        assertThat(storedFile.getValue().getMimeType()).isEqualTo("application/pdf");
        assertThat(readJson(responseBody)).containsEntry(FileUploadServlet.CONTENT_TYPE_ATTRIBUTE, "application/pdf");
    }

    @Test
    public void doPost_should_accept_real_image_declared_with_an_unlisted_type_and_store_its_usual_type()
            throws Exception {
        //given: browsers in Citrix sessions declare JPEG files as image/x-citrix-jpeg
        doReturn(new String[] { "jpg" }).when(fileUploadServlet).getDefaultAllowedExtensions();
        doReturn("json").when(fileUploadServlet).getInitParameter(FileUploadServlet.RESPONSE_CONTENT_TYPE_PARAM);

        //when
        final String responseBody = upload("photo.jpg", "image/x-citrix-jpeg",
                new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0 });

        //then
        final ArgumentCaptor<FileContent> storedFile = ArgumentCaptor.forClass(FileContent.class);
        verify(temporaryContentAPI).storeTempFile(storedFile.capture());
        assertThat(storedFile.getValue().getMimeType()).isEqualTo("image/jpeg");
        assertThat(readJson(responseBody)).containsEntry(FileUploadServlet.CONTENT_TYPE_ATTRIBUTE, "image/jpeg");
    }

    @Test
    public void doPost_should_store_script_capable_type_as_octet_stream_for_extension_without_rule()
            throws Exception {
        //given
        doReturn(new String[] { "*" }).when(fileUploadServlet).getDefaultAllowedExtensions();

        //when
        upload("page.html", "text/html", "<script>alert(1)</script>".getBytes());

        //then
        final ArgumentCaptor<FileContent> storedFile = ArgumentCaptor.forClass(FileContent.class);
        verify(temporaryContentAPI).storeTempFile(storedFile.capture());
        assertThat(storedFile.getValue().getMimeType()).isEqualTo("application/octet-stream");
    }

    @Test
    public void doPost_should_log_rejected_extension_and_return_json_error() throws Exception {
        //given
        doReturn(new String[] { "pdf" }).when(fileUploadServlet).getDefaultAllowedExtensions();
        doReturn("json").when(fileUploadServlet).getInitParameter(FileUploadServlet.RESPONSE_CONTENT_TYPE_PARAM);
        when(request.getRequestURI()).thenReturn("/bonita/API/formFileUpload");

        //when
        final String responseBody = upload("script.exe", "application/x-msdownload", "MZ".getBytes());

        //then
        verify(response).setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
        assertThat(readJson(responseBody)).containsEntry("type", "UnsupportedMediaType")
                .containsEntry("message", "Extension not supported.")
                .containsEntry("statusCode", HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
        assertThat(systemOutRule.getLog()).contains("WARN", "script.exe", "/bonita/API/formFileUpload", "pdf",
                "form.attachment.allowed.extensions");
    }

    @Test
    public void doPost_should_log_content_mismatch_and_return_json_error() throws Exception {
        //given
        doReturn(new String[] { "pdf" }).when(fileUploadServlet).getDefaultAllowedExtensions();
        doReturn("json").when(fileUploadServlet).getInitParameter(FileUploadServlet.RESPONSE_CONTENT_TYPE_PARAM);

        //when
        final String responseBody = upload("payslip.pdf", "application/pdf",
                "<html><body>evil</body></html>".getBytes());

        //then
        verify(response).setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
        assertThat(readJson(responseBody)).containsEntry("type", "UnsupportedMediaType")
                .containsEntry("message", "File content does not match its extension.");
        assertThat(systemOutRule.getLog()).contains("WARN", "payslip.pdf", "application/pdf");
    }

    @Test
    public void doPost_should_return_plain_text_error_when_servlet_does_not_answer_json() throws Exception {
        //given
        doReturn(new String[] { "pdf" }).when(fileUploadServlet).getDefaultAllowedExtensions();

        //when
        final String responseBody = upload("script.exe", "application/x-msdownload", "MZ".getBytes());

        //then
        verify(response).setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
        assertThat(responseBody).isEqualTo("Extension not supported.");
    }

    @Test
    public void doPost_should_not_hint_at_the_tenant_property_when_servlet_declares_its_own_extensions()
            throws Exception {
        //given: e.g. processUpload, which only accepts .bar files
        doReturn("bar").when(fileUploadServlet).getInitParameter(FileUploadServlet.SUPPORTED_EXTENSIONS_PARAM);

        //when
        upload("script.exe", "application/x-msdownload", "MZ".getBytes());

        //then
        assertThat(systemOutRule.getLog()).contains("script.exe", "bar")
                .doesNotContain("form.attachment.allowed.extensions");
    }

    @Test
    public void doPost_should_not_let_line_breaks_in_file_name_forge_log_lines() throws Exception {
        //given
        doReturn(new String[] { "pdf" }).when(fileUploadServlet).getDefaultAllowedExtensions();

        //when
        upload("evil\r\n|00:00:00.000|main|INFO |forged| line.exe", "application/x-msdownload", "MZ".getBytes());

        //then
        assertThat(systemOutRule.getLog()).contains("evil__|00:00:00.000|main|INFO |forged| line.exe")
                .doesNotContain("\n|00:00:00.000|main|INFO |forged|");
    }

    @Test
    public void isSupportedExtension_should_match_upper_case_extension_whatever_the_default_locale() {
        //given: in Turkish, "TIF".toLowerCase() is "tıf" (dotless i)
        doReturn(new String[] { "tif" }).when(fileUploadServlet).getDefaultAllowedExtensions();
        final Locale defaultLocale = Locale.getDefault();
        Locale.setDefault(new Locale("tr", "TR"));
        try {
            //when-then
            assertThat(fileUploadServlet.isSupportedExtension("SCAN.TIF")).isTrue();
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }

    @Test
    public void doPost_should_delete_the_multipart_temp_copy_when_upload_is_rejected() throws Exception {
        //given: a request whose body was copied to a temp file by TokenValidatorFilter
        final MultiReadHttpServletRequest multiReadRequest = mock(MultiReadHttpServletRequest.class);
        request = multiReadRequest;
        doReturn(new String[] { "pdf" }).when(fileUploadServlet).getDefaultAllowedExtensions();

        //when
        upload("script.exe", "application/x-msdownload", "MZ".getBytes());

        //then
        verify(response).setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
        verify(multiReadRequest).cleanMultipartTempContent();
    }

    /**
     * Posts one file to the servlet under test and returns the response body.
     */
    private String upload(final String fileName, final String declaredContentType, final byte[] content)
            throws Exception {
        final FileItem fileItem = mock(FileItem.class);
        final ServletFileUpload serviceFileUpload = mock(ServletFileUpload.class);
        final StringWriter responseBody = new StringWriter();
        fileUploadServlet.uploadDirectoryPath = temporaryFolder.getRoot().getAbsolutePath();
        doNothing().when(fileUploadServlet).defineUploadDirectoryPath(request);
        doReturn(serviceFileUpload).when(fileUploadServlet).createServletFileUpload(any(FileItemFactory.class));
        doReturn(temporaryContentAPI).when(fileUploadServlet).getTemporaryContentAPI();
        doReturn(mock(ServletConfig.class)).when(fileUploadServlet).getServletConfig();
        // the rejection paths stop before reading the content or storing the file
        lenient().when(fileItem.getName()).thenReturn(fileName);
        lenient().when(fileItem.getContentType()).thenReturn(declaredContentType);
        lenient().when(fileItem.getInputStream()).then(invocation -> new ByteArrayInputStream(content));
        lenient().when(temporaryContentAPI.storeTempFile(any(FileContent.class))).thenReturn("tempKey");
        when(serviceFileUpload.parseRequest(request)).thenReturn(List.of(fileItem));
        when(request.getMethod()).thenReturn("post");
        when(request.getContentType()).thenReturn("multipart/");
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));

        fileUploadServlet.init();
        fileUploadServlet.doPost(request, response);
        return responseBody.toString();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> readJson(final String json) throws Exception {
        return new ObjectMapper().readValue(json, Map.class);
    }
}
