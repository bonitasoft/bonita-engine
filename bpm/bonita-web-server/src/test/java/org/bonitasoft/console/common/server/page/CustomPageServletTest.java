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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.bonitasoft.console.common.server.page.extension.PageResourceProviderImpl;
import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.engine.session.APISession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class CustomPageServletTest {

    MockHttpServletRequest hsRequest = new MockHttpServletRequest();

    @Mock
    MockHttpServletResponse hsResponse = new MockHttpServletResponse();

    @Mock
    HttpSession httpSession;

    @Mock
    APISession apiSession;

    @Mock
    CustomPageAuthorizationsHelper customPageAuthorizationsHelper;

    @Mock
    PageRenderer pageRenderer;

    @Mock
    ResourceRenderer resourceRenderer;

    @Mock
    PageResourceProviderImpl pageResourceProvider;

    @Mock
    BonitaHomeFolderAccessor bonitaHomeFolderAccessor;

    @Mock
    CustomPageRequestModifier customPageRequestModifier;

    @Spy
    @InjectMocks
    CustomPageServlet servlet;

    @Before
    public void beforeEach() throws Exception {
        hsRequest.setSession(httpSession);
        doReturn(apiSession).when(httpSession).getAttribute("apiSession");
        doReturn(customPageAuthorizationsHelper).when(servlet).getCustomPageAuthorizationsHelper(apiSession);
    }

    @Test
    public void should_get_Forbidden_Status_when_page_unAuthorize() throws Exception {
        hsRequest.setPathInfo("/pageToken/");
        hsRequest.setParameter("appToken", "myApp");
        given(resourceRenderer.getPathSegments("/pageToken/")).willReturn(Arrays.asList("pageToken"));
        doReturn(false).when(apiSession).isTechnicalUser();
        given(customPageAuthorizationsHelper.isPageAuthorized("myApp", "pageToken")).willReturn(false);

        servlet.doGet(hsRequest, hsResponse);

        verify(hsResponse).sendError(403, "User not Authorized");
    }

    @Test
    public void should_get_badRequest_Status_when_page_name_is_not_set() throws Exception {
        hsRequest.setPathInfo("/");

        servlet.doGet(hsRequest, hsResponse);

        verify(hsResponse).sendError(400, "The name of the page is required.");
    }

    @Test
    public void should_redirect_to_valide_url_on_missing_slash() throws Exception {
        hsRequest.setRequestURI("/bonita/portal/custom-page/custompage_htmlexample?anyparam=paramvalue");
        hsRequest.setPathInfo("/custompage_htmlexample");

        servlet.doGet(hsRequest, hsResponse);

        verify(customPageRequestModifier).redirectToValidPageUrl(hsRequest, hsResponse);
    }

    @Test
    public void getPage_should_call_the_page_renderer() throws Exception {
        testPageIsWellCalled("custompage_htmlexample1", "/custompage_htmlexample1/",
                Arrays.asList("custompage_htmlexample1"));
        testPageIsWellCalled("custompage_htmlexample2", "/custompage_htmlexample2/index",
                Arrays.asList("custompage_htmlexample2", "index"));
        testPageIsWellCalled("custompage_htmlexample3", "/custompage_htmlexample3/Index",
                Arrays.asList("custompage_htmlexample3", "Index"));
        testPageIsWellCalled("custompage_htmlexample4", "/custompage_htmlexample4/index.html",
                Arrays.asList("custompage_htmlexample4", "index.html"));
        testPageIsWellCalled("custompage_htmlexample5", "/custompage_htmlexample5/index.groovy",
                Arrays.asList("custompage_htmlexample5", "index.groovy"));
    }

    private void testPageIsWellCalled(final String token, final String path, final List<String> pathSegment)
            throws Exception {
        hsRequest.setPathInfo(path);
        given(resourceRenderer.getPathSegments(path)).willReturn(pathSegment);
        given(customPageAuthorizationsHelper.isPageAuthorized(null, token)).willReturn(true);

        servlet.doGet(hsRequest, hsResponse);

        verify(pageRenderer, times(1)).displayCustomPage(hsRequest, hsResponse, apiSession, token);
    }

    @Test
    public void getResource_should_call_the_resource_renderer() throws Exception {
        hsRequest.setPathInfo("/custompage_htmlexample/css/file.css");
        final File pageDir = new File("/pageDir");
        final String pageName = "custompage_htmlexample";
        given(resourceRenderer.getPathSegments("/custompage_htmlexample/css/file.css"))
                .willReturn(Arrays.asList(pageName, "css", "file.css"));
        doReturn(pageResourceProvider).when(pageRenderer).getPageResourceProvider(pageName);
        doReturn(pageDir).when(pageResourceProvider).getPageDirectory();
        doReturn(true).when(bonitaHomeFolderAccessor).isInFolder(any(File.class), any(File.class));
        given(customPageAuthorizationsHelper.isPageAuthorized(null, "custompage_htmlexample")).willReturn(true);

        servlet.doGet(hsRequest, hsResponse);

        verify(resourceRenderer, times(1)).renderFile(hsRequest, hsResponse,
                new File(pageDir, File.separator + "resources" + File.separator + "css" + File.separator + "file.css"));
    }

    @Test
    public void should_get_Forbidden_Status_when_we_try_to_access_to_unAuthorize_file_with_pathSegment()
            throws Exception {
        hsRequest.setPathInfo("/custompage_htmlexample/css/../../../file.css");
        final File pageDir = new File(".");
        given(resourceRenderer.getPathSegments("/custompage_htmlexample/css/../../../file.css")).willReturn(
                Arrays.asList("custompage_htmlexample", "css", "..", "..", "..", "file.css"));
        doReturn(pageResourceProvider).when(pageRenderer).getPageResourceProvider("custompage_htmlexample");
        given(pageResourceProvider.getPageDirectory()).willReturn(pageDir);
        given(customPageAuthorizationsHelper.isPageAuthorized(null, "custompage_htmlexample")).willReturn(true);
        // folder we wants to access is not authorized
        doReturn(false).when(bonitaHomeFolderAccessor).isInFolder(any(File.class), any(File.class));

        servlet.doGet(hsRequest, hsResponse);
        verify(hsResponse).sendError(403, "User not Authorized");
    }

}
