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
import static org.mockito.Mockito.*;

import java.io.File;
import java.net.URISyntaxException;

import javax.servlet.http.HttpSession;

import org.bonitasoft.console.common.server.page.extension.PageResourceProviderImpl;
import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.session.APISession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Julien Mege
 */
@RunWith(MockitoJUnitRunner.class)
public class PageRendererTest {

    MockHttpServletRequest hsRequest = new MockHttpServletRequest();

    MockHttpServletResponse hsResponse = new MockHttpServletResponse();

    @Mock
    HttpSession httpSession;

    @Mock
    APISession apiSession;

    @Mock
    ResourceRenderer resourceRenderer;

    @Mock
    CustomPageService customPageService;

    @Mock
    PageResourceProviderImpl pageResourceProvider;

    @Mock
    Page page;

    @Mock
    PageAPI pageAPI;

    @Spy
    @InjectMocks
    PageRenderer pageRenderer = new PageRenderer(resourceRenderer);

    @Before
    public void beforeEach() {
        hsRequest.setSession(httpSession);
        doReturn(apiSession).when(httpSession).getAttribute("apiSession");
    }

    @Test
    public void should_display_html_page() throws Exception {
        final File indexFile = initializeHTMLFilePage();

        pageRenderer.displayCustomPage(hsRequest, hsResponse, apiSession, 42L);

        verify(customPageService, times(1)).ensurePageFolderIsUpToDate(apiSession, pageResourceProvider);
        verify(resourceRenderer, times(1)).renderFile(hsRequest, hsResponse, indexFile, true);
    }

    protected File initializeHTMLFilePage() throws URISyntaxException, BonitaException {
        final String pageName = "my_html_page_v_7";
        final File pageDir = new File(PageRenderer.class.getResource(pageName).toURI());
        final File indexFile = new File(pageDir, "resources" + File.separator + "index.html");
        doReturn(pageResourceProvider).when(pageRenderer).getPageResourceProvider(42L, apiSession);
        doReturn(pageDir).when(pageResourceProvider).getPageDirectory();
        doReturn(pageAPI).when(customPageService).getPageAPI(apiSession);
        doReturn(page).when(pageResourceProvider).getPage(pageAPI);
        doReturn(pageName).when(page).getName();
        when(customPageService.getPage(apiSession, 42L)).thenReturn(page);
        when(customPageService.getGroovyPageFile(any(File.class))).thenReturn(new File("none_existing_file"));
        return indexFile;
    }

    @Test
    public void should_display_fallback_index_if_no_index_in_resource_folder() throws Exception {

        final String pageName = "my_html_page_v_6";
        final File pageDir = new File(PageRenderer.class.getResource(pageName).toURI());
        final File indexFile = new File(pageDir, "index.html");
        doReturn(pageResourceProvider).when(pageRenderer).getPageResourceProvider(42L, apiSession);
        doReturn(pageDir).when(pageResourceProvider).getPageDirectory();
        doReturn(pageAPI).when(customPageService).getPageAPI(apiSession);
        doReturn(page).when(pageResourceProvider).getPage(pageAPI);
        doReturn(pageName).when(page).getName();
        when(customPageService.getPage(apiSession, 42L)).thenReturn(page);
        when(customPageService.getGroovyPageFile(any(File.class))).thenReturn(new File("none_existing_file"));

        pageRenderer.displayCustomPage(hsRequest, hsResponse, apiSession, 42L);

        verify(resourceRenderer, times(1)).renderFile(hsRequest, hsResponse, indexFile, true);
    }

    @Test
    public void should_get_pageResourceProvider_by_pageId() throws Exception {
        //given
        final String pageName = "pageName";
        doReturn(pageName).when(page).getName();
        doReturn(page).when(customPageService).getPage(apiSession, 42L);

        //when
        final PageResourceProviderImpl pageResourceProvider = pageRenderer.getPageResourceProvider(42L, apiSession);

        //then
        assertThat(pageResourceProvider.getPageName()).isEqualTo(pageName);
    }

    @Test
    public void should_get_pageResourceProvider_by_pageName() throws Exception {
        //given
        final String pageName = "pageName";
        doReturn(pageName).when(page).getName();

        //when
        final PageResourceProviderImpl pageResourceProvider = pageRenderer.getPageResourceProvider(pageName);

        //then
        assertThat(pageResourceProvider.getPageName()).isEqualTo(pageName);
    }

}
