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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.File;
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
import org.bonitasoft.engine.api.TemporaryContentAPI;
import org.bonitasoft.engine.io.FileContent;
import org.bonitasoft.engine.session.APISession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PageUploadServletTest {

    @Mock
    HttpServletRequest request;

    @Mock
    APISession apiSession;

    @Spy
    PageUploadServlet pageUploadServlet = new PageUploadServlet();

    @Before
    public void setUp() throws Exception {
        pageUploadServlet.pageTmp = new File(getClass().getResource("/pageWithPermissions.zip").toURI());

    }

    @Test
    public void storeTempFile_should_close_the_stream_of_the_uploaded_part() throws Exception {
        //given
        final InputStream partStream = spy(new ByteArrayInputStream("page content".getBytes()));
        final FileItem item = mock(FileItem.class);
        doReturn(partStream).when(item).getInputStream();
        pageUploadServlet.temporaryContentAPI = mock(TemporaryContentAPI.class);

        //when
        storeTempFileThenDeleteItsCopy(item);

        //then: an open stream keeps the space of the deleted part allocated, and prevents its deletion on Windows
        verify(partStream).close();
    }

    @Test
    public void storeTempFile_should_close_the_stream_it_hands_to_the_engine() throws Exception {
        //given
        final FileItem item = mock(FileItem.class);
        doReturn(new ByteArrayInputStream("page content".getBytes())).when(item).getInputStream();
        final TemporaryContentAPI temporaryContentAPI = mock(TemporaryContentAPI.class);
        pageUploadServlet.temporaryContentAPI = temporaryContentAPI;

        //when
        storeTempFileThenDeleteItsCopy(item);

        //then
        final ArgumentCaptor<FileContent> storedContent = ArgumentCaptor.forClass(FileContent.class);
        verify(temporaryContentAPI).storeTempFile(storedContent.capture());
        final InputStream storedStream = storedContent.getValue().getInputStream();
        assertThatThrownBy(storedStream::read).isInstanceOf(IOException.class);
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

        final String responseString = pageUploadServlet.generateResponseString(request, "fileName",
                pageUploadServlet.pageTmp.getName());

        assertThat(responseString)
                .isEqualTo(
                        pageUploadServlet.pageTmp.getName() + "::[Organisation visualization,Organisation management]");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void should_generateResponseJson_work_with_permissions() throws Exception {
        final String[] permissions = new String[] { "Organisation visualization", "Organisation management" };
        doReturn(permissions).when(pageUploadServlet).getPermissions(request);

        final String responseString = pageUploadServlet.generateResponseJson(request, "fileName", "application/zip",
                pageUploadServlet.pageTmp.getName());

        ObjectReader reader = new ObjectMapper().readerFor(Map.class);
        Map<String, Serializable> responseMap = reader.readValue(responseString);

        //jackson deserializes array to list by default
        assertThat((List<String>) responseMap.get(PageUploadServlet.PERMISSIONS_RESPONSE_ATTRIBUTE))
                .containsExactlyInAnyOrder("Organisation visualization", "Organisation management");
    }

    /**
     * Calls storeTempFile, then deletes the page copy it made: never the test page that setUp puts in pageTmp, even
     * when storeTempFile fails before replacing it.
     */
    private void storeTempFileThenDeleteItsCopy(final FileItem item) throws Exception {
        final File testPage = pageUploadServlet.pageTmp;
        try {
            pageUploadServlet.storeTempFile("page.zip", item);
        } finally {
            if (pageUploadServlet.pageTmp != testPage) {
                pageUploadServlet.pageTmp.delete();
            }
        }
    }
}
