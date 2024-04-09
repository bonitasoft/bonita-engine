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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CustomPageRequestModifierTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher requestDispatcher;

    @Test
    public void redirect_with_trailing_slash_should_not_encode_parameter() throws Exception {
        when(request.getContextPath()).thenReturn("bonita/");
        when(request.getServletPath()).thenReturn("apps/");
        when(request.getPathInfo()).thenReturn("myapp/mypage");
        when(request.getQueryString()).thenReturn("time=12:00");
        when(response.encodeRedirectURL("bonita/apps/myapp/mypage/?time=12:00"))
                .thenReturn("bonita/apps/myapp/mypage/?time=12:00");

        CustomPageRequestModifier customPageRequestModifier = new CustomPageRequestModifier();
        customPageRequestModifier.redirectToValidPageUrl(request, response);

        verify(response).sendRedirect("bonita/apps/myapp/mypage/?time=12:00");
    }

    @Test
    public void redirect_with_trailing_slash_should_not_add_question_mark() throws Exception {
        when(request.getContextPath()).thenReturn("bonita/");
        when(request.getServletPath()).thenReturn("apps/");
        when(request.getPathInfo()).thenReturn("myapp/mypage");
        when(response.encodeRedirectURL("bonita/apps/myapp/mypage/")).thenReturn("bonita/apps/myapp/mypage/");

        CustomPageRequestModifier customPageRequestModifier = new CustomPageRequestModifier();
        customPageRequestModifier.redirectToValidPageUrl(request, response);

        verify(response).sendRedirect("bonita/apps/myapp/mypage/");
    }

    @Test
    public void check_should_not_authorize_requests_to_other_paths() throws Exception {
        String apiPath = "/API/living/../../WEB-INF/web.xml";

        CustomPageRequestModifier customPageRequestModifier = new CustomPageRequestModifier();
        customPageRequestModifier.forwardIfRequestIsAuthorized(request, response, "/API", apiPath);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(request, never()).getRequestDispatcher(anyString());
    }

    @Test
    public void check_should_authorize_valid_requests() throws Exception {
        String apiPath = "/API/living/0";
        when(request.getRequestDispatcher(apiPath)).thenReturn(requestDispatcher);

        CustomPageRequestModifier customPageRequestModifier = new CustomPageRequestModifier();
        customPageRequestModifier.forwardIfRequestIsAuthorized(request, response, "/API", apiPath);

        verify(request).getRequestDispatcher(apiPath);
        verify(response, never()).sendError(anyInt(), anyString());
    }

}
