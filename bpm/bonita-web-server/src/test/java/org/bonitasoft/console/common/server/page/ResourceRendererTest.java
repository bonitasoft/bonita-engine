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
package org.bonitasoft.console.common.server.page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.bonitasoft.engine.exception.BonitaException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Julien Mege
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourceRendererTest {

    @Mock
    private HttpServletRequest req;

    @Mock
    private HttpServletResponse res;

    @Mock
    private ServletContext servletContext;

    @Mock
    ServletOutputStream outputStream;

    @Mock
    HttpSession httpSession;

    @Spy
    @InjectMocks
    ResourceRenderer resourceRenderer;

    public ResourceRendererTest() {
    }

    @Before
    public void setup() throws IOException {
        when(req.getSession()).thenReturn(httpSession);
        when(res.getOutputStream()).thenReturn(outputStream);
        when(httpSession.getServletContext()).thenReturn(servletContext);
    }

    @Test
    public void renderFile_should_build_a_valid_response()
            throws BonitaException, URISyntaxException, IOException, IllegalAccessException {
        final File resourceFile = getResourceFile();
        final long contentLength = resourceFile.length();
        when(servletContext.getMimeType("file.css")).thenReturn("text/css");
        resourceRenderer.renderFile(req, res, resourceFile);

        verify(res).setCharacterEncoding("UTF-8");
        verify(servletContext).getMimeType("file.css");
        verify(res).setContentType("text/css");
        verify(res).setContentLength((int) contentLength);
        verify(res).setBufferSize((int) contentLength);
        verify(outputStream).write(any(byte[].class), eq(0), eq((int) contentLength));
        verify(res).flushBuffer();
        verify(outputStream).close();
    }

    private File getResourceFile() throws URISyntaxException {
        return new File(ResourceRendererTest.class.getResource("file.css").toURI());
    }

    @Test(expected = BonitaException.class)
    public void getResourceFile_should_throw_BonitaException_on_passing_null_resources_folder() throws Exception {
        resourceRenderer.renderFile(req, res, null);
    }

    @Test
    public void getResourceFile_should_sendError404_on_passing_none_existing_resources() throws Exception {
        final File noneExistingFile = new File("NoneExistingFile.css");
        resourceRenderer.renderFile(req, res, noneExistingFile);
        verify(res).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void getPathSegments_should_return_expected_token_list() throws UnsupportedEncodingException {
        when(req.getPathInfo()).thenReturn("a/b");

        final List<String> tokens = resourceRenderer.getPathSegments("a/b");
        assertThat(tokens).hasSize(2).containsExactly("a", "b");
    }

    @Test
    public void getPathSegments_should_return_expected_token_list_ondouble_slash() throws UnsupportedEncodingException {
        when(req.getPathInfo()).thenReturn("a//b");

        final List<String> tokens = resourceRenderer.getPathSegments("a//b");
        assertThat(tokens).hasSize(2).containsExactly("a", "b");
    }

    @Test
    public void getPathSegments_should_return_expected_token_list_if_no_slash() throws UnsupportedEncodingException {
        when(req.getPathInfo()).thenReturn("a");

        final List<String> tokens = resourceRenderer.getPathSegments("a");
        assertThat(tokens).hasSize(1).containsExactly("a");
    }

}
