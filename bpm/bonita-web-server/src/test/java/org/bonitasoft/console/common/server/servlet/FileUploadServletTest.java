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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
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
        when(fileUploadServlet.getInitParameter(FileUploadServlet.RETURN_ORIGINAL_FILENAME_PARAM)).thenReturn("true");
        when(fileUploadServlet.getInitParameter(FileUploadServlet.RETURN_FULL_SERVER_PATH_PARAM)).thenReturn("false");
        fileUploadServlet.init();

        final String jsonResponse = fileUploadServlet.generateResponseJson("originalFileName", "application/json",
                uploadedFile);
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
        when(fileUploadServlet.getInitParameter(FileUploadServlet.RETURN_ORIGINAL_FILENAME_PARAM)).thenReturn("true");
        when(fileUploadServlet.getInitParameter(FileUploadServlet.RETURN_FULL_SERVER_PATH_PARAM)).thenReturn("false");
        fileUploadServlet.init();

        final String responseString = fileUploadServlet.generateResponseString(request, "originalFileName",
                uploadedFile);

        assertThat(responseString).isEqualTo("uploadedFile.txt::originalFileName");
    }

    @Test
    public void getExtension_should_return_proper_extension() {
        // given
        final String filename = "C:\\Users\\Desktop\\process.bar";

        // when
        final String extension = fileUploadServlet.getExtension(filename);

        // then
        assertThat(extension).isEqualTo(".bar");
    }

    @Test
    public void getExtension_should_return_an_empty_extension() {
        // given
        final String filename = "C:\\Users\\Desktop\\process";

        // when
        final String extension = fileUploadServlet.getExtension(filename);

        // then
        assertThat(extension).isEmpty();
    }

    @Test
    public void getExtension_should_return_a_proper_extension_without_taking_care_of_dots() {
        // given
        final String filename = "C:\\Users\\Deskt.op\\proc.ess.bar";

        // when
        final String extension = fileUploadServlet.getExtension(filename);

        // then
        assertThat(extension).isEqualTo(".bar");
    }

    @Test
    public void getExtension_should_return_proper_extension_for_short_filename() {
        // given
        final String filename = "process.bar";

        // when
        final String extension = fileUploadServlet.getExtension(filename);

        // then
        assertThat(extension).isEqualTo(".bar");
    }

    @Test
    public void getExtension_should_return_proper_extension_for_linux_like_paths() {
        // given
        final String filename = "/Users/Deskt.op/proc.ess.bar";

        // when
        final String extension = fileUploadServlet.getExtension(filename);

        // then
        assertThat(extension).isEqualTo(".bar");
    }

    @Test
    public void getExtension_should_return_an_empty_extension_for_parent_folder_filename() {
        // given
        final String filename = "../../../";

        // when
        final String extension = fileUploadServlet.getExtension(filename);

        // then
        assertThat(extension).isEmpty();
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
}
