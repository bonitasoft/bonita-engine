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
package org.bonitasoft.livingapps;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bonitasoft.console.common.server.page.PageRenderer;
import org.bonitasoft.console.common.server.page.ResourceRenderer;
import org.bonitasoft.console.common.server.page.extension.PageResourceProviderImpl;
import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.engine.session.APISession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationRouterTest {

    public static final String LAYOUT_PAGE_NAME = "layoutPageName";
    @Mock(answer = Answers.RETURNS_MOCKS)
    HttpServletRequest hsRequest;

    @Mock
    HttpServletResponse hsResponse;

    @Mock
    APISession apiSession;

    @Mock
    ApplicationModelFactory applicationModelFactory;

    @Mock
    ApplicationModel applicationModel;

    @Mock
    PageRenderer pageRenderer;

    @Mock
    PageResourceProviderImpl pageResourceProvider;

    @Spy
    @InjectMocks
    ResourceRenderer resourceRenderer;

    @Mock
    BonitaHomeFolderAccessor bonitaHomeFolderAccessor;

    @InjectMocks
    ApplicationRouter applicationRouter;

    @Before
    public void beforeEach() throws Exception {
        given(hsRequest.getMethod()).willReturn("GET");
        given(hsRequest.getContextPath()).willReturn("/bonita");
        given(hsRequest.getServletPath()).willReturn("/apps");
    }

    @Test
    public void should_redirect_to_home_page_when_accessing_living_application_root() throws Exception {
        given(applicationModel.getApplicationHomePage()).willReturn("home/");
        given(applicationModel.hasProfileMapped()).willReturn(true);
        given(applicationModelFactory.createApplicationModel("HumanResources")).willReturn(applicationModel);
        given(hsRequest.getPathInfo()).willReturn("/HumanResources");
        given(resourceRenderer.getPathSegments("/HumanResources")).willReturn(Arrays.asList("HumanResources"));

        applicationRouter.route(hsRequest, hsResponse, apiSession, pageRenderer, resourceRenderer,
                bonitaHomeFolderAccessor);
        verify(hsResponse).sendRedirect("home/");
    }

    @Test
    public void should_redirect_to_home_page_with_query_parameters() throws Exception {
        given(applicationModel.getApplicationHomePage()).willReturn("home/");
        given(applicationModel.hasProfileMapped()).willReturn(true);
        given(applicationModelFactory.createApplicationModel("HumanResources")).willReturn(applicationModel);
        given(hsRequest.getQueryString()).willReturn("time=12:00");
        given(hsRequest.getPathInfo()).willReturn("/HumanResources");
        given(resourceRenderer.getPathSegments("/HumanResources")).willReturn(Arrays.asList("HumanResources"));

        applicationRouter.route(hsRequest, hsResponse, apiSession, pageRenderer, resourceRenderer,
                bonitaHomeFolderAccessor);
        verify(hsResponse).sendRedirect("home/?time=12:00");
    }

    @Test(expected = RuntimeException.class)
    public void should_throw_an_error_when_the_uri_is_malformed() throws Exception {

        applicationRouter.route(hsRequest, hsResponse, apiSession, pageRenderer, resourceRenderer,
                bonitaHomeFolderAccessor);
    }

    @Test
    public void should_display_layout_page() throws Exception {
        accessAuthorizedPage("HumanResources", "leavingRequests");

        applicationRouter.route(hsRequest, hsResponse, apiSession, pageRenderer, resourceRenderer,
                bonitaHomeFolderAccessor);

        verify(pageRenderer).displayCustomPage(hsRequest, hsResponse, apiSession,
                applicationModel.getApplicationLayoutName());
    }

    @Test
    public void should_access_Layout_resource() throws Exception {
        accessAuthorizedPage("HumanResources", "layout/css/file.css");
        final File layoutFolder = new File("layout");
        final String customPageLayoutName = "custompage_layout";
        given(applicationModel.getApplicationLayoutName()).willReturn(customPageLayoutName);

        given(pageRenderer.getPageResourceProvider(customPageLayoutName)).willReturn(pageResourceProvider);
        given(pageResourceProvider.getPageDirectory()).willReturn(layoutFolder);
        given(bonitaHomeFolderAccessor.isInFolder(any(File.class), any(File.class))).willReturn(true);

        applicationRouter.route(hsRequest, hsResponse, apiSession, pageRenderer, resourceRenderer,
                bonitaHomeFolderAccessor);

        verify(pageRenderer).ensurePageFolderIsPresent(apiSession, pageResourceProvider);
        verify(resourceRenderer).renderFile(hsRequest, hsResponse, new File("layout/resources/css/file.css"));
    }

    @Test
    public void should_access_Theme_resource() throws Exception {
        accessAuthorizedPage("HumanResources", "theme/css/file.css");
        final File themeFolder = new File("theme");
        final String customPageThemeName = "custompage_theme";
        given(applicationModel.getApplicationThemeName()).willReturn(customPageThemeName);

        given(pageRenderer.getPageResourceProvider(customPageThemeName)).willReturn(pageResourceProvider);
        given(pageResourceProvider.getPageDirectory()).willReturn(themeFolder);
        given(bonitaHomeFolderAccessor.isInFolder(any(File.class), any(File.class))).willReturn(true);

        applicationRouter.route(hsRequest, hsResponse, apiSession, pageRenderer, resourceRenderer,
                bonitaHomeFolderAccessor);

        verify(pageRenderer).ensurePageFolderIsPresent(apiSession, pageResourceProvider);
        verify(resourceRenderer).renderFile(hsRequest, hsResponse, new File("theme/resources/css/file.css"));
    }

    @Test
    public void should_not_forward_to_the_application_page_template_when_the_page_is_not_in_the_application()
            throws Exception {
        accessUnknownPage("HumanResources", "leavingRequests");

        applicationRouter.route(hsRequest, hsResponse, apiSession, pageRenderer, resourceRenderer,
                bonitaHomeFolderAccessor);

        verify(hsResponse).sendError(HttpServletResponse.SC_NOT_FOUND,
                "There is no page " + "leavingRequests" + " in the application " + "HumanResources");
        verify(pageRenderer, never()).displayCustomPage(hsRequest, hsResponse, apiSession, LAYOUT_PAGE_NAME);
    }

    @Test
    public void should_not_forward_to_the_application_page_template_when_user_is_not_authorized() throws Exception {
        accessUnauthorizedPage("HumanResources", "leavingRequests");

        applicationRouter.route(hsRequest, hsResponse, apiSession, pageRenderer, resourceRenderer,
                bonitaHomeFolderAccessor);

        verify(hsResponse).sendError(HttpServletResponse.SC_FORBIDDEN,
                "Unauthorized access for the page " + "leavingRequests" + " of the application " + "HumanResources");
        verify(pageRenderer, never()).displayCustomPage(hsRequest, hsResponse, apiSession, LAYOUT_PAGE_NAME);
    }

    @Test
    public void should_send_not_found_response_when_no_profile_is_mapped_to_application() throws Exception {
        accessAuthorizedPage("HumanResources", "leavingRequests");
        given(applicationModel.hasProfileMapped()).willReturn(false);

        applicationRouter.route(hsRequest, hsResponse, apiSession, pageRenderer, resourceRenderer,
                bonitaHomeFolderAccessor);

        verify(hsResponse).sendError(HttpServletResponse.SC_NOT_FOUND, "No profile mapped to living application");
        verify(pageRenderer, never()).displayCustomPage(hsRequest, hsResponse, apiSession, LAYOUT_PAGE_NAME);
    }

    @Test
    public void should_not_authorize_API_requests_to_other_paths() throws Exception {
        accessAuthorizedPage("HumanResources", "API");
        String unauthorizedPath = "/API/living/../../WEB-INF/web.xml";
        given(hsRequest.getPathInfo()).willReturn("/HumanResources" + unauthorizedPath);

        applicationRouter.route(hsRequest, hsResponse, apiSession, pageRenderer, resourceRenderer,
                bonitaHomeFolderAccessor);

        verify(hsResponse).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(hsRequest, never()).getRequestDispatcher(anyString());
    }

    private void accessAuthorizedPage(final String applicationToken, final String pageToken) throws Exception {
        accessPage(applicationToken, pageToken, true, true);
    }

    private void accessUnauthorizedPage(final String applicationToken, final String pageToken) throws Exception {
        accessPage(applicationToken, pageToken, true, false);
    }

    private void accessUnknownPage(final String applicationToken, final String pageToken) throws Exception {
        accessPage(applicationToken, pageToken, false, false);
    }

    private void accessPage(final String applicationToken, final String pageToken, final boolean hasPage,
            final boolean isAuthorized) throws Exception {
        given(applicationModel.hasPage(pageToken)).willReturn(hasPage);
        given(applicationModel.authorize(apiSession)).willReturn(isAuthorized);
        given(applicationModel.hasProfileMapped()).willReturn(true);
        given(applicationModelFactory.createApplicationModel(applicationToken)).willReturn(applicationModel);
        given(hsRequest.getPathInfo()).willReturn("/" + applicationToken + "/" + pageToken + "/");
    }
}
