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

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.bonitasoft.console.common.server.auth.AuthenticationManager;
import org.bonitasoft.console.common.server.login.HttpServletRequestAccessor;
import org.bonitasoft.console.common.server.page.CustomPageRequestModifier;
import org.bonitasoft.console.common.server.page.PageRenderer;
import org.bonitasoft.console.common.server.page.ResourceRenderer;
import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.engine.business.application.ApplicationPageNotFoundException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.page.PageNotFoundException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.livingapps.exception.CreationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LivingApplicationServletTest {

    @Mock
    HttpServletRequest hsRequest;

    @Mock
    HttpServletResponse hsResponse;

    @Mock
    HttpSession httpSession;

    @Mock
    APISession session;

    @Mock
    ApplicationRouter router;

    @Mock
    ResourceRenderer resourceRenderer;

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    PageRenderer pageRenderer;

    @Mock
    CustomPageRequestModifier customPageRequestModifier;

    @Spy
    LivingApplicationServlet servlet;

    @Before
    public void beforeEach() throws Exception {
        doReturn(router).when(servlet).createApplicationRouter(any(APISession.class));
        doReturn(pageRenderer).when(servlet).getPageRenderer();
        doReturn(resourceRenderer).when(servlet).getResourceRenderer();
        doReturn(httpSession).when(hsRequest).getSession();
        doReturn(session).when(servlet).getSession(hsRequest);
        doReturn("/appToken/pageToken/").when(hsRequest).getPathInfo();
        doReturn(authenticationManager).when(servlet).getAuthenticationManager();
        servlet.customPageRequestModifier = customPageRequestModifier;
    }

    @Test
    public void should_send_error_404_when_the_application_page_is_not_found() throws Exception {
        doThrow(new ApplicationPageNotFoundException("error")).when(router).route(eq(hsRequest), eq(hsResponse),
                eq(session), eq(pageRenderer), eq(resourceRenderer), any(BonitaHomeFolderAccessor.class));

        servlet.service(hsRequest, hsResponse);

        verify(hsResponse).sendError(404, "error");
    }

    @Test
    public void should_send_error_404_when_the_custom_page_is_not_associated_to_application() throws Exception {
        doThrow(new ApplicationPageNotFoundException("error")).when(router).route(eq(hsRequest), eq(hsResponse),
                eq(session), eq(pageRenderer), eq(resourceRenderer), any(BonitaHomeFolderAccessor.class));

        servlet.service(hsRequest, hsResponse);

        verify(hsResponse).sendError(404, "error");
    }

    @Test
    public void should_send_error_404_when_the_custom_page_is_not_existing() throws Exception {
        doThrow(new PageNotFoundException("PageName")).when(router).route(eq(hsRequest), eq(hsResponse), eq(session),
                eq(pageRenderer), eq(resourceRenderer), any(BonitaHomeFolderAccessor.class));

        servlet.service(hsRequest, hsResponse);

        verify(hsResponse).sendError(404, "Unable to find page with name: " + "PageName");
    }

    @Test
    public void should_send_error_500_on_searchException() throws Exception {
        doThrow(new CreationException("error")).when(router).route(eq(hsRequest), eq(hsResponse), eq(session),
                eq(pageRenderer), eq(resourceRenderer), any(BonitaHomeFolderAccessor.class));

        servlet.service(hsRequest, hsResponse);

        verify(hsResponse).sendError(404, "error");
    }

    @Test
    public void should_send_error_500_on_BonitaHomeNotSetException() throws Exception {
        doThrow(new BonitaHomeNotSetException("error")).when(router).route(eq(hsRequest), eq(hsResponse), eq(session),
                eq(pageRenderer), eq(resourceRenderer), any(BonitaHomeFolderAccessor.class));

        servlet.service(hsRequest, hsResponse);

        verify(hsResponse).sendError(500);
    }

    @Test
    public void should_send_error_500_on_ServerAPIException() throws Exception {
        given(servlet.createApplicationRouter(session))
                .willThrow(new ServerAPIException(new Exception("")));

        servlet.service(hsRequest, hsResponse);

        verify(hsResponse).sendError(500);
    }

    @Test
    public void should_send_error_500_on_UnknownAPITypeException() throws Exception {
        given(servlet.createApplicationRouter(session))
                .willThrow(new UnknownAPITypeException("error"));

        servlet.service(hsRequest, hsResponse);

        verify(hsResponse).sendError(500);
    }

    @Test
    public void should_send_error_404_when_the_page_is_not_found() throws Exception {
        doThrow(new ApplicationPageNotFoundException("error")).when(router).route(eq(hsRequest), eq(hsResponse),
                eq(session), eq(pageRenderer), eq(resourceRenderer), any(BonitaHomeFolderAccessor.class));

        servlet.service(hsRequest, hsResponse);

        verify(hsResponse).sendError(404, "error");
    }

    @Test
    public void should_redirect_to_the_login_page_when_the_session_is_invalid() throws Exception {
        doThrow(new InvalidSessionException("error")).when(router).route(eq(hsRequest), eq(hsResponse), eq(session),
                eq(pageRenderer), eq(resourceRenderer), any(BonitaHomeFolderAccessor.class));
        doReturn("/bonita/apps/appToken/pageToken/").when(hsRequest).getRequestURI();
        String encodedRedirectURL = "%2Fbonita%2Fapps%2FappToken%2FpageToken%2F";
        String loginURL = "/bonita/login.jsp?redirectUrl=" + encodedRedirectURL;
        doReturn(loginURL).when(authenticationManager).getLoginPageURL(any(HttpServletRequestAccessor.class),
                eq(encodedRedirectURL));

        servlet.service(hsRequest, hsResponse);

        verify(hsResponse).sendRedirect(loginURL);
    }

    @Test
    public void should_redirectToValidPageUrl_on_missing_final_slash() throws Exception {
        doReturn("/appToken/pageToken").when(hsRequest).getPathInfo();

        servlet.service(hsRequest, hsResponse);

        verify(customPageRequestModifier).redirectToValidPageUrl(hsRequest, hsResponse);
    }

    @Test
    public void should_redirectToValidPageUrl_on_missing_final_slash_after_appToken() throws Exception {
        doReturn("/appToken").when(hsRequest).getPathInfo();

        servlet.service(hsRequest, hsResponse);

        verify(customPageRequestModifier).redirectToValidPageUrl(hsRequest, hsResponse);
    }

    @Test
    public void should_not_redirectToValidPageUrl_on_resource_query() throws Exception {
        doReturn("/appToken/pageToken/file.css").when(hsRequest).getPathInfo();

        servlet.service(hsRequest, hsResponse);

        verify(customPageRequestModifier, never()).redirectToValidPageUrl(hsRequest, hsResponse);
    }

}
