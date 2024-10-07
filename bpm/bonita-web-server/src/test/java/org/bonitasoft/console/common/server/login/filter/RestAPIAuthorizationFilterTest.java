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
package org.bonitasoft.console.common.server.login.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.regex.Pattern;

import javax.servlet.*;
import javax.servlet.http.HttpServletMapping;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.bonitasoft.console.common.server.login.servlet.PlatformLoginServlet;
import org.bonitasoft.console.common.server.utils.SessionUtil;
import org.bonitasoft.engine.api.permission.APICallContext;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RestAPIAuthorizationFilterTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain chain;
    @Mock
    private APISession apiSession;
    @Mock
    private HttpSession httpSession;
    @Mock
    private FilterConfig filterConfig;
    @Mock
    private ServletContext servletContext;
    @Mock
    private HttpServletMapping httpServletMapping;

    private final RestAPIAuthorizationFilter restAPIAuthorizationFilter = new RestAPIAuthorizationFilter();

    @Before
    public void setUp() throws Exception {
        doReturn(httpServletMapping).when(request).getHttpServletMapping();
        doReturn(httpSession).when(request).getSession();
        doReturn("").when(request).getQueryString();
        doReturn(apiSession).when(httpSession).getAttribute(SessionUtil.API_SESSION_PARAM_KEY);
        doReturn(false).when(apiSession).isTechnicalUser();
        when(servletContext.getContextPath()).thenReturn("");
        when(filterConfig.getServletContext()).thenReturn(servletContext);
        restAPIAuthorizationFilter.init(filterConfig);
    }

    private void initSpy(final RestAPIAuthorizationFilter restAPIAuthorizationFilterSpy) throws ServletException {
        doReturn("GET").when(request).getMethod();
        doReturn("").when(restAPIAuthorizationFilterSpy).getRequestBody(request);
    }

    @Test
    public void should_checkPermissions_call_engine_check_if_security_is_enabled() throws Exception {
        final RestAPIAuthorizationFilter restAPIAuthorizationFilterSpy = spy(restAPIAuthorizationFilter);
        initSpy(restAPIAuthorizationFilterSpy);
        doReturn(true).when(restAPIAuthorizationFilterSpy).isApiAuthorizationsCheckEnabled();
        doReturn(true).when(restAPIAuthorizationFilterSpy)
                .enginePermissionsCheck(new APICallContext("GET", "bpm", "case", null, "", ""), apiSession);

        //when
        final boolean isAuthorized = restAPIAuthorizationFilterSpy.checkPermissions(request, "bpm", "case", null);

        //then
        assertThat(isAuthorized).isTrue();
        verify(restAPIAuthorizationFilterSpy)
                .enginePermissionsCheck(new APICallContext("GET", "bpm", "case", null, "", ""), apiSession);
    }

    @Test
    public void should_not_call_engine_check_if_secu_is_enabled_but_session_call_is_always_authorized()
            throws Exception {
        final RestAPIAuthorizationFilter restAPIAuthorizationFilterSpy = spy(restAPIAuthorizationFilter);
        doReturn("GET").when(request).getMethod();
        doReturn(true).when(restAPIAuthorizationFilterSpy).isApiAuthorizationsCheckEnabled();
        doReturn("").when(restAPIAuthorizationFilterSpy).getRequestBody(any());
        //when
        final boolean isAuthorized = restAPIAuthorizationFilterSpy.checkPermissions(request, "system", "session", null);

        //then
        assertThat(isAuthorized).isTrue();
        verify(restAPIAuthorizationFilterSpy).isAlwaysAuthorizedResource(any(APICallContext.class));
        verify(restAPIAuthorizationFilterSpy, never()).enginePermissionsCheck(any(APICallContext.class),
                any(APISession.class));
    }

    @Test
    public void should_checkPermissions_do_not_call_check_if_technical() throws Exception {
        doReturn(true).when(apiSession).isTechnicalUser();
        final RestAPIAuthorizationFilter restAPIAuthorizationFilterSpy = spy(restAPIAuthorizationFilter);
        initSpy(restAPIAuthorizationFilterSpy);
        doReturn(true).when(restAPIAuthorizationFilterSpy).isApiAuthorizationsCheckEnabled();

        //when
        final boolean isAuthorized = restAPIAuthorizationFilterSpy.checkPermissions(request, "bpm", "case", null);

        //then
        assertThat(isAuthorized).isTrue();
        verify(restAPIAuthorizationFilterSpy, never()).enginePermissionsCheck(any(APICallContext.class),
                any(APISession.class));
    }

    @Test
    public void should_checkPermissions_do_nothing_if_secu_is_disabled() throws Exception {
        final RestAPIAuthorizationFilter restAPIAuthorizationFilterSpy = spy(restAPIAuthorizationFilter);
        initSpy(restAPIAuthorizationFilterSpy);
        doReturn(false).when(restAPIAuthorizationFilterSpy).isApiAuthorizationsCheckEnabled();

        //when
        final boolean isAuthorized = restAPIAuthorizationFilterSpy.checkPermissions(request, "bpm", "case", null);

        //then
        assertThat(isAuthorized).isTrue();
        verify(restAPIAuthorizationFilterSpy, never()).enginePermissionsCheck(any(APICallContext.class),
                any(APISession.class));
    }

    @Test
    public void should_checkPermissions_parse_the_request() throws Exception {
        doReturn("/bpm/case/15").when(request).getPathInfo();
        final RestAPIAuthorizationFilter restAPIAuthorizationFilterSpy = spy(restAPIAuthorizationFilter);
        doReturn(true).when(restAPIAuthorizationFilterSpy).checkPermissions(eq(request), eq("bpm"), eq("case"),
                eq(APIID.makeAPIID(15L)));

        //when
        restAPIAuthorizationFilterSpy.checkPermissions(request);

        //then
        verify(restAPIAuthorizationFilterSpy).checkPermissions(eq(request), eq("bpm"), eq("case"),
                eq(APIID.makeAPIID(15L)));
    }

    @Test
    public void should_checkValidCondition_check_session_is_platform() throws Exception {
        doReturn("/API").when(request).getServletPath();
        doReturn("/platform/plop").when(request).getPathInfo();
        doReturn(mock(PlatformSession.class)).when(httpSession)
                .getAttribute(PlatformLoginServlet.PLATFORM_SESSION_PARAM_KEY);
        //when
        restAPIAuthorizationFilter.proceedWithFiltering(request, response, chain);

        verify(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    public void should_checkValidCondition_check_session_is_platform_with_API_toolkit() throws Exception {
        doReturn("/APIToolkit").when(request).getServletPath();
        doReturn("/platform/plop").when(request).getPathInfo();
        doReturn(mock(PlatformSession.class)).when(httpSession)
                .getAttribute(PlatformLoginServlet.PLATFORM_SESSION_PARAM_KEY);
        //when
        restAPIAuthorizationFilter.proceedWithFiltering(request, response, chain);

        verify(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    public void should_checkValidCondition_check_unauthorized_if_no_platform_session() throws Exception {
        doReturn("/API").when(request).getServletPath();
        doReturn("/platform/plop").when(request).getPathInfo();
        doReturn(null).when(httpSession)
                .getAttribute(PlatformLoginServlet.PLATFORM_SESSION_PARAM_KEY);
        //when
        restAPIAuthorizationFilter.proceedWithFiltering(request, response, chain);

        verify(chain, never()).doFilter(any(ServletRequest.class), any(ServletResponse.class));
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void should_checkValidCondition_check_unauthorized_if_no_tenant_session() throws Exception {
        doReturn(null).when(httpSession).getAttribute(SessionUtil.API_SESSION_PARAM_KEY);
        doReturn("/API").when(request).getServletPath();
        doReturn("/bpm/case/15").when(request).getPathInfo();
        //when
        restAPIAuthorizationFilter.proceedWithFiltering(request, response, chain);

        verify(chain, never()).doFilter(any(ServletRequest.class), any(ServletResponse.class));
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void should_checkValidCondition_check_unauthorized_if_session_is_invalid() throws Exception {
        final RestAPIAuthorizationFilter restAPIAuthorizationFilterSpy = spy(restAPIAuthorizationFilter);
        doReturn("/API").when(request).getServletPath();
        doThrow(InvalidSessionException.class).when(restAPIAuthorizationFilterSpy)
                .checkPermissions(any(HttpServletRequest.class));
        //when
        restAPIAuthorizationFilterSpy.proceedWithFiltering(request, response, chain);

        verify(chain, never()).doFilter(any(ServletRequest.class), any(ServletResponse.class));
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void should_checkValidCondition_check_permission_if_is_tenant_is_forbidden() throws Exception {
        final RestAPIAuthorizationFilter restAPIAuthorizationFilterSpy = spy(restAPIAuthorizationFilter);
        doReturn("/API").when(request).getServletPath();
        doReturn(false).when(restAPIAuthorizationFilterSpy).checkPermissions(any(HttpServletRequest.class));

        //when
        restAPIAuthorizationFilterSpy.proceedWithFiltering(request, response, chain);

        verify(chain, never()).doFilter(any(ServletRequest.class), any(ServletResponse.class));
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    public void should_checkValidCondition_check_permission_if_is_tenant_is_ok() throws Exception {
        final RestAPIAuthorizationFilter restAPIAuthorizationFilterSpy = spy(restAPIAuthorizationFilter);
        doReturn("/API").when(request).getServletPath();
        doReturn("/bpm/case/15").when(request).getPathInfo();
        doReturn(true).when(restAPIAuthorizationFilterSpy).checkPermissions(any(HttpServletRequest.class));

        //when
        restAPIAuthorizationFilterSpy.proceedWithFiltering(request, response, chain);

        verify(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    public void should_checkValidCondition_check_permission_if_pathInfo_is_null() throws Exception {
        final RestAPIAuthorizationFilter restAPIAuthorizationFilterSpy = spy(restAPIAuthorizationFilter);
        doReturn("/API").when(request).getServletPath();
        doReturn(null).when(request).getPathInfo();
        doReturn(true).when(restAPIAuthorizationFilterSpy).checkPermissions(any(HttpServletRequest.class));

        //when
        restAPIAuthorizationFilterSpy.proceedWithFiltering(request, response, chain);

        verify(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test(expected = ServletException.class)
    public void should_checkValidCondition_catch_runtime() throws ServletException {
        doThrow(new RuntimeException()).when(request).getServletPath();

        //when
        restAPIAuthorizationFilter.proceedWithFiltering(request, response, chain);
    }

    @Test
    public void testFilterWithExcludedURL() throws Exception {
        final RestAPIAuthorizationFilter restAPIAuthorizationFilterSpy = spy(restAPIAuthorizationFilter);
        final String url = "test";
        when(request.getRequestURL()).thenReturn(new StringBuffer(url));
        doReturn(true).when(restAPIAuthorizationFilterSpy).matchExcludePatterns(url);
        restAPIAuthorizationFilterSpy.doFilter(request, response, chain);
        verify(restAPIAuthorizationFilterSpy, times(0)).proceedWithFiltering(request, response, chain);
        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    public void testMatchExcludePatterns() {
        matchExcludePattern("http://host/bonita/portal/resource/page/API/system/i18ntranslation", true);
        matchExcludePattern("http://host/bonita/apps/app/API/system/i18ntranslation", true);
        matchExcludePattern("http://host/bonita/API/system/i18ntranslation", true);
        matchExcludePattern("http://host/bonita/API/bpm/process", false);
        matchExcludePattern("http://host/bonita/API/bpm/process/;i18ntranslation", false);
        matchExcludePattern("http://host/bonita/API/bpm/process/../../../API/system/i18ntranslation", false);
        matchExcludePattern("http://host/bonita/API/system/i18ntranslation/../../bpm/process", false);
        matchExcludePattern("http://host/bonita/API/bpm/activity/test/../i18ntranslation/..?p=0&c=10", false);
        matchExcludePattern("http://host/bonita/API/bpm/activity/i18ntranslation/../?p=0&c=10", false);
    }

    @Test
    public void testCompileNullPattern() {
        assertThat(restAPIAuthorizationFilter.compilePattern(null)).isNull();
    }

    @Test
    public void testCompileWrongPattern() {
        assertThat(restAPIAuthorizationFilter.compilePattern("((((")).isNull();
    }

    @Test
    public void testCompileSimplePattern() {
        final String patternToCompile = "test";
        assertThat(restAPIAuthorizationFilter.compilePattern(patternToCompile)).isNotNull().has(new Condition<>() {

            @Override
            public boolean matches(final Pattern pattern) {
                return pattern.pattern().equalsIgnoreCase(patternToCompile);
            }
        });
    }

    @Test
    public void testCompileExcludePattern() {
        final String patternToCompile = RestAPIAuthorizationFilter.AUTHORIZATION_FILTER_EXCLUDED_PAGES_PATTERN;
        assertThat(restAPIAuthorizationFilter.compilePattern(patternToCompile)).isNotNull().has(new Condition<>() {

            @Override
            public boolean matches(final Pattern pattern) {
                return pattern.pattern().equalsIgnoreCase(patternToCompile);
            }
        });
    }

    private void matchExcludePattern(final String urlToMatch, final Boolean mustMatch) {
        if (restAPIAuthorizationFilter.matchExcludePatterns(urlToMatch) != mustMatch) {
            Assertions.fail("Matching excludePattern and the Url " + urlToMatch + " must return " + mustMatch);
        }
    }
}
