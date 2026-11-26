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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.commons.fileupload.FileItem;
import org.bonitasoft.console.common.server.page.CustomPageService;
import org.bonitasoft.engine.api.TemporaryContentAPI;
import org.bonitasoft.engine.exception.InvalidPageZipContentException;
import org.bonitasoft.engine.session.APISession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

@RunWith(MockitoJUnitRunner.class)
public class PageUploadServletTest {

    @Mock
    HttpServletRequest request;

    @Mock
    APISession apiSession;

    @Mock
    TemporaryContentAPI temporaryContentAPI;

    @Mock
    CustomPageService customPageService;

    @Spy
    PageUploadServlet pageUploadServlet = new PageUploadServlet();

    @Test
    public void storeTempFile_should_close_the_stream_of_the_uploaded_part() throws Exception {
        //given
        final InputStream partStream = spy(new ByteArrayInputStream("page content".getBytes()));
        final FileItem item = mock(FileItem.class);
        doReturn(partStream).when(item).getInputStream();
        doReturn(temporaryContentAPI).when(pageUploadServlet).getTemporaryContentAPI();

        //when
        pageUploadServlet.storeTempFile(new MockHttpServletRequest(), "page.zip", item);

        //then: an open stream keeps the space of the deleted part allocated, and prevents its deletion on Windows
        verify(partStream).close();
    }

    @Test
    public void getPagePermissions_should_read_the_page_uploaded_with_the_same_request() throws Exception {
        //given: two page uploads in progress at the same time, served by the same servlet instance
        doReturn(temporaryContentAPI).when(pageUploadServlet).getTemporaryContentAPI();
        doReturn(customPageService).when(pageUploadServlet).getCustomPageService();
        doReturn(apiSession).when(pageUploadServlet).getAPISession(any(HttpServletRequest.class));
        final MockHttpServletRequest firstRequest = new MockHttpServletRequest();
        final MockHttpServletRequest secondRequest = new MockHttpServletRequest();
        pageUploadServlet.storeTempFile(firstRequest, "first.zip", uploadedPart("content of the first page"));
        pageUploadServlet.storeTempFile(secondRequest, "second.zip", uploadedPart("content of the second page"));

        //when
        pageUploadServlet.getPagePermissions(firstRequest, false);

        //then
        verify(customPageService).getPageProperties(apiSession, "content of the first page".getBytes(), false, null);
    }

    @Test
    public void getPagePermissions_should_fail_when_no_page_was_uploaded_with_the_request() {
        assertThatThrownBy(() -> pageUploadServlet.getPagePermissions(new MockHttpServletRequest(), false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No page was uploaded with this request");
    }

    @Test
    public void generateResponseString_should_return_the_error_name_when_the_page_is_invalid() throws Exception {
        //given: a page whose permissions cannot be computed
        doReturn(temporaryContentAPI).when(pageUploadServlet).getTemporaryContentAPI();
        doReturn(customPageService).when(pageUploadServlet).getCustomPageService();
        doReturn(apiSession).when(pageUploadServlet).getAPISession(any(HttpServletRequest.class));
        doThrow(new InvalidPageZipContentException("no page.properties")).when(customPageService)
                .getPageProperties(any(APISession.class), any(byte[].class), anyBoolean(), any());
        final MockHttpServletRequest uploadRequest = new MockHttpServletRequest();

        //when
        pageUploadServlet.storeTempFile(uploadRequest, "invalid.zip", uploadedPart("not a page"));
        final String response = pageUploadServlet.generateResponseString(uploadRequest, "invalid.zip", "tempKey");

        //then
        assertThat(response).isEqualTo("tempKey::" + InvalidPageZipContentException.class.getSimpleName());
    }

    @Test
    public void should_getPermissions_work_with_valid_zip() throws Exception {
        doReturn("edit").when(request).getParameter("action");
        final Set<String> permissionsSet = new HashSet<>();
        permissionsSet.add("Organisation visualization");
        permissionsSet.add("Organisation management");
        doReturn(permissionsSet).when(pageUploadServlet).getPagePermissions(request, false);

        final String[] permissions = pageUploadServlet.getPermissions(request);

        assertThat(permissions).containsExactlyInAnyOrder("Organisation visualization", "Organisation management");
    }

    @Test
    public void should_getPermissions_work_with_invalid_zip() throws Exception {
        doReturn("add").when(request).getParameter("action");
        final Set<String> permissionsSet = new HashSet<>();
        doReturn(permissionsSet).when(pageUploadServlet).getPagePermissions(request, true);

        final String[] permissions = pageUploadServlet.getPermissions(request);

        assertThat(permissions).isNotNull().isEmpty();
    }

    @Test
    public void should_generateResponseString_work_with_permissions() throws Exception {
        final String[] permissions = new String[] { "Organisation visualization", "Organisation management" };
        doReturn(permissions).when(pageUploadServlet).getPermissions(request);

        final String responseString = pageUploadServlet.generateResponseString(request, "fileName", "tempKey");

        assertThat(responseString).isEqualTo("tempKey::[Organisation visualization,Organisation management]");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void should_generateResponseJson_work_with_permissions() throws Exception {
        final String[] permissions = new String[] { "Organisation visualization", "Organisation management" };
        doReturn(permissions).when(pageUploadServlet).getPermissions(request);

        final String responseString = pageUploadServlet.generateResponseJson(request, "fileName", "application/zip",
                "tempKey", false);

        ObjectReader reader = new ObjectMapper().readerFor(Map.class);
        Map<String, Serializable> responseMap = reader.readValue(responseString);

        //jackson deserializes array to list by default
        assertThat((List<String>) responseMap.get(PageUploadServlet.PERMISSIONS_RESPONSE_ATTRIBUTE))
                .containsExactlyInAnyOrder("Organisation visualization", "Organisation management");
    }

    /**
     * An uploaded part with the given content, as commons-fileupload parses it
     */
    private static FileItem uploadedPart(final String content) throws IOException {
        final FileItem item = mock(FileItem.class);
        // the page is stored first, and its content read again only when its permissions are computed
        lenient().when(item.getInputStream()).thenReturn(new ByteArrayInputStream(content.getBytes()));
        lenient().when(item.get()).thenReturn(content.getBytes());
        return item;
    }
}
