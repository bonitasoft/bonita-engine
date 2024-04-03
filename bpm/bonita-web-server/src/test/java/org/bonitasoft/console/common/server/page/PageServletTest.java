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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpHeaders;
import org.bonitasoft.console.common.server.page.extension.PageResourceProviderImpl;
import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.exception.UnauthorizedAccessException;
import org.bonitasoft.engine.page.PageNotFoundException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PageServletTest {

    public static final long PAGE_ID = 42L;
    @Mock
    PageRenderer pageRenderer;

    @Mock
    ResourceRenderer resourceRenderer;

    @Mock
    PageMappingService pageMappingService;

    @Mock
    CustomPageService customPageService;

    @Mock
    BonitaHomeFolderAccessor bonitaHomeFolderAccessor;

    @Spy
    @InjectMocks
    PageServlet pageServlet;

    @Mock(answer = Answers.RETURNS_MOCKS)
    HttpServletRequest hsRequest;

    @Mock
    HttpServletResponse hsResponse;

    @Mock
    HttpSession httpSession;

    @Mock
    APISession apiSession;

    Locale locale;

    @Before
    public void beforeEach() throws Exception {
        when(hsRequest.getMethod()).thenReturn("GET");
        when(hsRequest.getContextPath()).thenReturn("/bonita");
        when(hsRequest.getSession()).thenReturn(httpSession);
        when(httpSession.getAttribute("apiSession")).thenReturn(apiSession);
        locale = new Locale("en");
        when(pageRenderer.getCurrentLocale(hsRequest)).thenReturn(new Locale("en"));
    }

    @Test
    public void should_get_Forbidden_Status_when_unauthorized() throws Exception {
        when(hsRequest.getPathInfo()).thenReturn("/process/processName/processVersion/content/");
        doThrow(UnauthorizedAccessException.class).when(pageMappingService).getPage(hsRequest, apiSession,
                "process/processName/processVersion", locale, true);

        pageServlet.service(hsRequest, hsResponse);

        verify(hsResponse, times(1)).sendError(403, "User not Authorized");
    }

    @Test
    public void should_get_Bad_Request_when_invalid_parameters() throws Exception {
        when(hsRequest.getPathInfo()).thenReturn("");
        pageServlet.service(hsRequest, hsResponse);
        verify(hsResponse, times(1)).sendError(400,
                "/content or /theme is expected in the URL after the page mapping key");
    }

    @Test
    public void should_display_externalPage() throws Exception {
        when(hsRequest.getPathInfo()).thenReturn("/process/processName/processVersion/content/");
        when(pageMappingService.getPage(hsRequest, apiSession, "process/processName/processVersion", locale, true))
                .thenReturn(
                        new PageReference(null, "/externalPage"));

        pageServlet.service(hsRequest, hsResponse);

        verify(pageServlet, times(1)).displayExternalPage(hsResponse, "/externalPage");
        verify(hsResponse, times(1)).encodeRedirectURL("/externalPage");
        verify(hsResponse, times(1)).sendRedirect(any());
    }

    @Test
    public void should_display_customPage() throws Exception {
        when(hsRequest.getPathInfo()).thenReturn("/process/processName/processVersion/content/");
        when(pageMappingService.getPage(hsRequest, apiSession, "process/processName/processVersion", locale, true))
                .thenReturn(new PageReference(PAGE_ID, null));

        pageServlet.service(hsRequest, hsResponse);

        verify(pageRenderer, times(1)).displayCustomPage(hsRequest, hsResponse, apiSession, PAGE_ID, locale);
    }

    @Test
    public void should_display_customPage_resource() throws Exception {
        //given
        when(hsRequest.getPathInfo()).thenReturn("/process/processName/processVersion/content/path/of/resource.css");
        final PageReference pageReference = new PageReference(PAGE_ID, null);
        when(pageMappingService.getPage(hsRequest, apiSession, "process/processName/processVersion", locale, false))
                .thenReturn(pageReference);

        final PageResourceProviderImpl pageResourceProvider = mock(PageResourceProviderImpl.class);
        final File resourceFile = mock(File.class);
        when(pageResourceProvider.getResourceAsFile("resources" + File.separator + "path/of/resource.css"))
                .thenReturn(resourceFile);
        when(pageRenderer.getPageResourceProvider(PAGE_ID, apiSession)).thenReturn(pageResourceProvider);
        when(bonitaHomeFolderAccessor.isInFolder(resourceFile, null)).thenReturn(true);

        //when
        pageServlet.service(hsRequest, hsResponse);

        //then
        verify(pageServlet, times(1)).displayPageOrResource(hsRequest, hsResponse, apiSession, PAGE_ID,
                "path/of/resource.css", locale);
        verify(pageServlet, times(1)).getResourceFile(hsResponse, apiSession, PAGE_ID, "path/of/resource.css");
        verify(pageRenderer, times(1)).ensurePageFolderIsPresent(apiSession, pageResourceProvider);
        verify(resourceRenderer, times(1)).renderFile(hsRequest, hsResponse, resourceFile);
    }

    @Test
    public void should_get_not_found_when_engine_throw_not_found() throws Exception {
        final String key = "process/processName/processVersion";
        when(hsRequest.getPathInfo()).thenReturn("/" + key + "/content/");
        doThrow(NotFoundException.class).when(pageMappingService).getPage(hsRequest, apiSession, key, locale, true);

        pageServlet.service(hsRequest, hsResponse);

        verify(hsResponse, times(1)).sendError(404, "Form mapping not found");
    }

    @Test
    public void should_not_get_not_found_when_empty_mapping() throws Exception {
        when(hsRequest.getPathInfo()).thenReturn("/process/processName/processVersion/content/");
        final PageReference pageReference = new PageReference(null, null);
        doReturn(pageReference).when(pageMappingService).getPage(hsRequest, apiSession,
                "process/processName/processVersion", locale, true);

        pageServlet.service(hsRequest, hsResponse);

        verify(hsResponse, never()).sendError(anyInt(), anyString());
    }

    @Test
    public void should_get_not_found_if_the_page_does_not_exist() throws Exception {
        final String key = "process/processName/processVersion";
        when(hsRequest.getPathInfo()).thenReturn("/" + key + "/content/");
        when(pageMappingService.getPage(hsRequest, apiSession, key, locale, true))
                .thenReturn(new PageReference(PAGE_ID, null));
        doThrow(PageNotFoundException.class).when(pageRenderer).displayCustomPage(hsRequest, hsResponse, apiSession,
                PAGE_ID, locale);

        pageServlet.service(hsRequest, hsResponse);

        verify(hsResponse, times(1)).sendError(404, "Page not found");
    }

    @Test
    public void should_redirect_for_missing_slash() throws Exception {
        when(hsRequest.getPathInfo()).thenReturn("/process/processName/processVersion/content");
        when(hsRequest.getContextPath()).thenReturn("/bonita");
        when(hsRequest.getServletPath()).thenReturn("/portal/resource");

        pageServlet.service(hsRequest, hsResponse);

        verify(hsResponse, times(1))
                .encodeRedirectURL("/bonita/portal/resource/process/processName/processVersion/content/");
        verify(hsResponse, times(1)).sendRedirect(any());
    }

    @Test
    public void should_get_server_error_when_issue_with_customPage() throws Exception {
        when(hsRequest.getPathInfo()).thenReturn("/process/processName/processVersion/content/");
        when(pageMappingService.getPage(hsRequest, apiSession, "process/processName/processVersion", locale, true))
                .thenReturn(new PageReference(PAGE_ID, null));
        final InstantiationException instantiationException = new InstantiationException("instatiation exception");
        doThrow(instantiationException).when(pageRenderer).displayCustomPage(hsRequest, hsResponse, apiSession, PAGE_ID,
                locale);

        pageServlet.service(hsRequest, hsResponse);

        verify(pageServlet).handleException(hsRequest, hsResponse, "process/processName/processVersion", true,
                instantiationException);
        verify(hsResponse, times(1)).sendError(500, "instatiation exception");
    }

    @Test
    public void should_get_bad_request_when_issue_with_parameters() throws Exception {
        when(hsRequest.getPathInfo()).thenReturn("/process/processName/processVersion/content/");
        final IllegalArgumentException illegalArgumentException = new IllegalArgumentException();
        doThrow(illegalArgumentException).when(pageMappingService).getPage(hsRequest, apiSession,
                "process/processName/processVersion", locale, true);

        pageServlet.service(hsRequest, hsResponse);

        verify(pageServlet).handleException(hsRequest, hsResponse, "process/processName/processVersion", true,
                illegalArgumentException);
        verify(hsResponse, times(1)).sendError(400, "Invalid Request.");
    }

    @Test
    public void should_get_unauthorized_when_engine_session_is_invalid() throws Exception {
        when(hsRequest.getPathInfo()).thenReturn("/process/processName/processVersion/content/");
        when(pageMappingService.getPage(hsRequest, apiSession, "process/processName/processVersion", locale, true))
                .thenReturn(new PageReference(PAGE_ID, null));
        final InvalidSessionException invalidSessionException = new InvalidSessionException("Invalid session");
        doThrow(invalidSessionException).when(pageRenderer).displayCustomPage(hsRequest, hsResponse, apiSession,
                PAGE_ID, locale);

        pageServlet.service(hsRequest, hsResponse);

        verify(pageServlet).displayPageOrResource(hsRequest, hsResponse, apiSession, PAGE_ID, null, locale);
        verify(pageServlet).handleException(hsRequest, hsResponse, "process/processName/processVersion", true,
                invalidSessionException);
        verify(hsResponse).sendError(401, "Invalid Bonita engine session.");
    }

    @Test
    public void should_forward_when_API_call() throws Exception {
        when(hsRequest.getPathInfo()).thenReturn("/process/processName/processVersion/API/bpm/process/1");

        pageServlet.service(hsRequest, hsResponse);

        verify(hsRequest, times(1)).getRequestDispatcher("/API/bpm/process/1");
    }

    @Test
    public void should_forward_when_THEME_call_and_no_app_in_refered() throws Exception {
        when(hsRequest.getPathInfo()).thenReturn("/process/Test/1.0/theme/theme.css");
        when(hsRequest.getParameter("app")).thenReturn(null);
        when(pageServlet.getAppFromReferer(hsRequest)).thenReturn(null);

        pageServlet.service(hsRequest, hsResponse);

        verify(hsRequest, times(1)).getRequestDispatcher("/theme/theme.css");
    }

    @Test
    public void should_display_theme_resource_with_app_param() throws Exception {
        when(hsRequest.getPathInfo()).thenReturn("/process/Test/1.0/theme/theme.css");
        when(hsRequest.getParameter("app")).thenReturn("myApp");
        doReturn(12L).when(pageServlet).getThemeId(apiSession, "myApp");

        final PageResourceProviderImpl pageResourceProvider = mock(PageResourceProviderImpl.class);
        final File resourceFile = mock(File.class);
        when(pageResourceProvider.getResourceAsFile("resources" + File.separator + "theme.css"))
                .thenReturn(resourceFile);
        when(pageRenderer.getPageResourceProvider(12L, apiSession)).thenReturn(pageResourceProvider);
        when(bonitaHomeFolderAccessor.isInFolder(resourceFile, null)).thenReturn(true);

        pageServlet.service(hsRequest, hsResponse);

        verify(resourceRenderer, times(1)).renderFile(hsRequest, hsResponse, resourceFile);
    }

    @Test
    public void should_redirect_theme_resource_wihout_app_param() throws Exception {
        when(hsRequest.getPathInfo()).thenReturn("/resource/process/Test/1.0/theme/theme.css");
        when(hsRequest.getParameter("app")).thenReturn(null);
        when(hsRequest.getQueryString()).thenReturn(null);
        when(hsRequest.getHeader(HttpHeaders.REFERER))
                .thenReturn("/bonita/resource/process/Test/1.0/content/?app=myApp");

        pageServlet.service(hsRequest, hsResponse);

        verify(hsResponse, times(1)).sendRedirect("?app=myApp");
    }

    @Test
    public void should_not_redirect_theme_resource_wihout_app_param_for_images() throws Exception {
        when(hsRequest.getPathInfo()).thenReturn("/process/Test/1.0/theme/icon.png");
        when(hsRequest.getParameter("app")).thenReturn(null);
        when(hsRequest.getHeader(HttpHeaders.REFERER))
                .thenReturn("/bonita/resource/process/Test/1.0/theme/theme.css?app=myApp");
        doReturn(12L).when(pageServlet).getThemeId(apiSession, "myApp");

        final PageResourceProviderImpl pageResourceProvider = mock(PageResourceProviderImpl.class);
        final File resourceFile = mock(File.class);
        when(pageResourceProvider.getResourceAsFile("resources" + File.separator + "icon.png"))
                .thenReturn(resourceFile);
        when(pageRenderer.getPageResourceProvider(12L, apiSession)).thenReturn(pageResourceProvider);
        when(bonitaHomeFolderAccessor.isInFolder(resourceFile, null)).thenReturn(true);

        pageServlet.service(hsRequest, hsResponse);

        verify(hsResponse, never()).sendRedirect("?app=myApp");
        verify(resourceRenderer, times(1)).renderFile(hsRequest, hsResponse, resourceFile);
    }

    @Test
    public void should_not_authorize_API_requests_to_other_paths() throws Exception {
        String unauthorizedPath = "/API/living/../../WEB-INF/web.xml";
        when(hsRequest.getPathInfo()).thenReturn("/process/Test/1.0" + unauthorizedPath);

        pageServlet.service(hsRequest, hsResponse);

        verify(hsResponse).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(hsRequest, never()).getRequestDispatcher(anyString());
    }

    @Test
    public void should_not_authorize_theme_requests_to_other_paths() throws Exception {
        when(hsRequest.getParameter("app")).thenReturn(null);
        when(hsRequest.getHeader(HttpHeaders.REFERER)).thenReturn("/bonita/resource/process/Test/1.0/content/");

        String unauthorizedPath = "/theme/../WEB-INF/web.xml";
        when(hsRequest.getPathInfo()).thenReturn("/process/Test/1.0" + unauthorizedPath);

        pageServlet.service(hsRequest, hsResponse);

        verify(hsResponse).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(hsRequest, never()).getRequestDispatcher(anyString());
    }

}
