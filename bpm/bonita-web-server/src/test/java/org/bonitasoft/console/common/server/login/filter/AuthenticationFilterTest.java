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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.bonitasoft.console.common.server.auth.AuthenticationManager;
import org.bonitasoft.console.common.server.login.HttpServletRequestAccessor;
import org.bonitasoft.console.common.server.login.utils.RedirectUrl;
import org.bonitasoft.console.common.server.utils.SessionUtil;
import org.bonitasoft.engine.session.APISession;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.stubbing.Answer;

public class AuthenticationFilterTest {

    @Mock
    private FilterChain chain;

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private HttpServletResponse httpResponse;

    @Mock
    private HttpSession httpSession;

    @Mock
    private APISession apiSession;

    @Mock
    private FilterConfig filterConfig;

    @Mock
    private ServletContext servletContext;

    @Spy
    AuthenticationFilter authenticationFilter;

    @Spy
    AuthenticationManager authenticationManager = new FakeAuthenticationManager();

    HttpServletRequestAccessor request;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        request = spy(new HttpServletRequestAccessor(httpRequest));
        doReturn(httpSession).when(httpRequest).getSession();
        doReturn(authenticationManager).when(authenticationFilter).getAuthenticationManager();
        when(httpRequest.getRequestURL()).thenReturn(new StringBuffer());
        when(httpRequest.getMethod()).thenReturn("GET");
        when(servletContext.getContextPath()).thenReturn("");
        when(filterConfig.getServletContext()).thenReturn(servletContext);
        when(filterConfig.getInitParameterNames()).thenReturn(Collections.emptyEnumeration());
        doReturn(false).when(authenticationFilter).isPlaformInMaintenance(request);

        //remove default rules (already logged in) as they have their own tests
        authenticationFilter.getRules().clear();
        authenticationFilter.init(filterConfig);
    }

    @Test
    public void testIfWeGoThroughFilterWhenAtLeastOneRulePass() throws Exception {
        when(httpRequest.getServletPath()).thenReturn("/apps");
        when(httpRequest.getPathInfo()).thenReturn("/app/home");

        AuthenticationRule passingRule = spy(createPassingRule());
        authenticationFilter.addRule(passingRule);
        authenticationFilter.addRule(createFailingRule());

        authenticationFilter.doAuthenticationFiltering(request, httpResponse, chain);

        verify(passingRule).proceedWithRequest(chain, httpRequest, httpResponse);
        verify(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    public void testIfWeAreNotRedirectedIfAtLeastOneRulePass() throws Exception {
        when(httpRequest.getServletPath()).thenReturn("/apps");
        when(httpRequest.getPathInfo()).thenReturn("/app/home");

        authenticationFilter.addRule(createFailingRule());
        authenticationFilter.addRule(createPassingRule());

        authenticationFilter.doAuthenticationFiltering(request, httpResponse, chain);

        verify(httpResponse, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(httpResponse, never()).sendRedirect(anyString());
    }

    @Test
    public void testIfWeAreRedirectedIfAllRulesFail() throws Exception {
        AuthenticationRule firstFailingRule = spy(createFailingRule());
        authenticationFilter.addRule(firstFailingRule);

        when(httpRequest.getServletPath()).thenReturn("/apps");
        when(httpRequest.getPathInfo()).thenReturn("/app/home");

        AuthenticationRule secondFailingRule = spy(createFailingRule());
        authenticationFilter.addRule(secondFailingRule);
        when(httpRequest.getContextPath()).thenReturn("/bonita");
        authenticationFilter.doAuthenticationFiltering(request, httpResponse, chain);

        verify(firstFailingRule, never()).proceedWithRequest(chain, httpRequest, httpResponse);
        verify(secondFailingRule, never()).proceedWithRequest(chain, httpRequest, httpResponse);
        verify(authenticationFilter).cleanHttpSession(httpSession);
        verify(httpResponse).sendRedirect(anyString());
    }

    @Test
    public void testIfSessionIsNotInvalidatedWhenCallingPlatformAPIWithAPISession() throws Exception {
        AuthenticationRule failingRule = spy(createFailingRule());
        authenticationFilter.addRule(failingRule);
        when(httpRequest.getServletPath()).thenReturn("/API");
        when(httpRequest.getPathInfo()).thenReturn("/platform/license");
        when(httpRequest.getContextPath()).thenReturn("/bonita");
        when(httpSession.getAttribute(SessionUtil.API_SESSION_PARAM_KEY)).thenReturn(apiSession);

        authenticationFilter.doAuthenticationFiltering(request, httpResponse, chain);

        verify(authenticationFilter, never()).cleanHttpSession(httpSession);
        verify(httpResponse).sendRedirect(anyString());
    }

    @Test
    public void testIfWeGet401ErrorIfAllRulesFailAndRedirectParamIsFalse() throws Exception {
        AuthenticationFilter authenticationFilterWithConfig = spy(new AuthenticationFilter());
        doReturn(authenticationManager).when(authenticationFilterWithConfig).getAuthenticationManager();
        FilterConfig filterConfig = mock(FilterConfig.class);
        when(filterConfig.getServletContext()).thenReturn(servletContext);
        List<String> initParams = new ArrayList<>();
        initParams.add(AuthenticationFilter.REDIRECT_PARAM);
        when(filterConfig.getInitParameterNames()).thenReturn(Collections.enumeration(initParams));
        when(filterConfig.getInitParameter(AuthenticationFilter.REDIRECT_PARAM)).thenReturn(Boolean.FALSE.toString());
        authenticationFilterWithConfig.init(filterConfig);
        AuthenticationRule firstFailingRule = spy(createFailingRule());
        authenticationFilterWithConfig.addRule(firstFailingRule);
        AuthenticationRule secondFailingRule = spy(createFailingRule());
        authenticationFilterWithConfig.addRule(secondFailingRule);
        when(httpRequest.getServletPath()).thenReturn("/apps");
        when(httpRequest.getPathInfo()).thenReturn("/app/home");
        when(httpRequest.getContextPath()).thenReturn("/bonita");
        authenticationFilterWithConfig.doAuthenticationFiltering(request, httpResponse, chain);

        verify(firstFailingRule, never()).proceedWithRequest(chain, httpRequest, httpResponse);
        verify(secondFailingRule, never()).proceedWithRequest(chain, httpRequest, httpResponse);
        verify(httpResponse, never()).sendRedirect(anyString());
        verify(httpResponse).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void testIfWeAreNotRedirectedIfRequestMethodIsNotGet() throws Exception {
        when(httpRequest.getMethod()).thenReturn("POST");
        authenticationFilter.addRule(createFailingRule());

        when(httpRequest.getServletPath()).thenReturn("/apps");
        when(httpRequest.getPathInfo()).thenReturn("/app/home");
        when(httpRequest.getContextPath()).thenReturn("/bonita");

        authenticationFilter.doAuthenticationFiltering(request, httpResponse, chain);

        verify(httpResponse).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(httpResponse, never()).sendRedirect(anyString());
    }

    @Test
    public void testIfWeDontGoThroughTheChainWhenRulesFails() throws Exception {
        authenticationFilter.addRule(createFailingRule());
        authenticationFilter.addRule(createFailingRule());
        when(httpRequest.getServletPath()).thenReturn("/apps");
        when(httpRequest.getPathInfo()).thenReturn("/app/home");
        when(httpRequest.getContextPath()).thenReturn("/bonita");
        authenticationFilter.doAuthenticationFiltering(request, httpResponse, chain);

        verify(chain, never()).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    public void testFilter() throws Exception {
        when(httpRequest.getSession()).thenReturn(httpSession);
        doAnswer((Answer<Object>) invocation -> null).when(authenticationFilter).doAuthenticationFiltering(
                any(HttpServletRequestAccessor.class), any(HttpServletResponse.class),
                any(FilterChain.class));
        authenticationFilter.doFilter(httpRequest, httpResponse, chain);
        verify(authenticationFilter, times(1)).doAuthenticationFiltering(any(HttpServletRequestAccessor.class),
                any(HttpServletResponse.class),
                any(FilterChain.class));
    }

    @Test
    public void testFailedLoginOnDoFiltering() throws Exception {
        when(httpRequest.getServletPath()).thenReturn("/apps");
        when(httpRequest.getPathInfo()).thenReturn("/app/home");

        EngineUserNotFoundOrInactive engineUserNotFoundOrInactive = new EngineUserNotFoundOrInactive("login failed");
        authenticationFilter.addRule(createThrowingExceptionRule(engineUserNotFoundOrInactive));

        authenticationFilter.doAuthenticationFiltering(request, httpResponse, chain);

        verify(authenticationManager, never()).getLoginPageURL(eq(request), anyString());
        verify(chain, never()).doFilter(httpRequest, httpResponse);
        verify(authenticationFilter).handleUserNotFoundOrInactiveException(request, httpResponse,
                engineUserNotFoundOrInactive);
        verify(authenticationFilter).redirectTo(request, httpResponse, AuthenticationFilter.USER_NOT_FOUND_JSP);
    }

    @Test
    public void testTenantIsPausedExceptionOnDoFiltering() throws Exception {
        when(httpRequest.getServletPath()).thenReturn("/apps");
        when(httpRequest.getPathInfo()).thenReturn("/app/home");
        PlatformUnderMaintenanceException platformUnderMaintenanceException = new PlatformUnderMaintenanceException(
                "login failed");
        authenticationFilter.addRule(createThrowingExceptionRule(platformUnderMaintenanceException));

        authenticationFilter.doAuthenticationFiltering(request, httpResponse, chain);

        verify(authenticationManager, never()).getLoginPageURL(eq(request), anyString());
        verify(chain, never()).doFilter(httpRequest, httpResponse);
        verify(authenticationFilter).handlePlatformUnderMaintenanceException(request, httpResponse,
                platformUnderMaintenanceException);
        verify(httpResponse).sendError(eq(HttpServletResponse.SC_SERVICE_UNAVAILABLE), anyString());
    }

    @Test
    public void should_redirect_to_503_error_page_in_maintenance() throws Exception {
        when(httpRequest.getServletPath()).thenReturn("/apps");
        when(httpRequest.getPathInfo()).thenReturn("/app/home");
        doReturn(true).when(authenticationFilter).isPlaformInMaintenance(request);
        authenticationFilter.addRule(createPassingRule());

        authenticationFilter.doAuthenticationFiltering(request, httpResponse, chain);

        verify(authenticationManager, never()).getLoginPageURL(eq(request), anyString());
        verify(chain, never()).doFilter(httpRequest, httpResponse);
        verify(authenticationFilter).handlePlatformUnderMaintenanceException(eq(request), eq(httpResponse),
                any(PlatformUnderMaintenanceException.class));
        verify(httpResponse).sendError(eq(HttpServletResponse.SC_SERVICE_UNAVAILABLE), anyString());
    }

    @Test
    public void should_be_able_to_display_error_pages_in_maintenance() throws Exception {
        when(httpRequest.getServletPath()).thenReturn("/portal/resource/app");
        when(httpRequest.getPathInfo()).thenReturn("/appDirectoryBonita/error-503/content/");
        doReturn(true).when(authenticationFilter).isPlaformInMaintenance(request);
        authenticationFilter.addRule(createPassingRule());

        authenticationFilter.doAuthenticationFiltering(request, httpResponse, chain);

        verify(authenticationManager, never()).getLoginPageURL(eq(request), anyString());
        verify(chain).doFilter(httpRequest, httpResponse);
        verify(httpResponse, never()).sendError(eq(HttpServletResponse.SC_SERVICE_UNAVAILABLE), anyString());
    }

    @Test
    public void should_let_technical_user_pass_when_platform_is_in_maintenance() throws Exception {
        when(httpRequest.getServletPath()).thenReturn("/apps");
        when(httpRequest.getPathInfo()).thenReturn("/app/home");
        doReturn(true).when(authenticationFilter).isPlaformInMaintenance(request);
        when(httpSession.getAttribute(SessionUtil.API_SESSION_PARAM_KEY)).thenReturn(apiSession);
        doReturn(true).when(apiSession).isTechnicalUser();
        authenticationFilter.addRule(createPassingRule());

        authenticationFilter.doAuthenticationFiltering(request, httpResponse, chain);

        verify(authenticationManager, never()).getLoginPageURL(eq(request), anyString());
        verify(chain).doFilter(httpRequest, httpResponse);
        verify(authenticationFilter, never()).handlePlatformUnderMaintenanceException(eq(request), eq(httpResponse),
                any(PlatformUnderMaintenanceException.class));
    }

    @Test
    public void testRedirectTo() throws Exception {
        final String context = "/bonita";
        when(httpRequest.getContextPath()).thenReturn(context);
        authenticationFilter.redirectTo(request, httpResponse, AuthenticationFilter.USER_NOT_FOUND_JSP);
        verify(httpResponse, times(1)).sendRedirect(context + AuthenticationFilter.USER_NOT_FOUND_JSP);
        verify(httpRequest, times(1)).getContextPath();
    }

    @Test
    public void testFilterWithExcludedURL() throws Exception {
        final String url = "test";
        when(httpRequest.getRequestURL()).thenReturn(new StringBuffer(url));
        doReturn(true).when(authenticationFilter).matchExcludePatterns(url);
        authenticationFilter.doFilter(httpRequest, httpResponse, chain);
        verify(authenticationFilter, times(0)).doAuthenticationFiltering(request, httpResponse, chain);
        verify(chain, times(1)).doFilter(httpRequest, httpResponse);
    }

    @Test
    public void testMatchExcludePatterns() {
        matchExcludePattern("http://host/bonita/portal/resource/page/API/system/session/unusedId", true);
        matchExcludePattern("http://host/bonita/apps/app/API/system/session/unusedId", true);
        matchExcludePattern("http://host/bonita/portal/resource/page/content/", false);
        matchExcludePattern("http://host/bonita/apps/app/page/", false);
    }

    @Test
    public void testMakeRedirectUrl() {
        when(httpRequest.getRequestURI()).thenReturn("/apps/appDirectoryBonita");
        final RedirectUrl redirectUrl = authenticationFilter.makeRedirectUrl(request);
        verify(httpRequest, times(1)).getRequestURI();
        assertThat(redirectUrl.getUrl()).isEqualToIgnoringCase("/apps/appDirectoryBonita");
    }

    @Test
    public void testCompileNullPattern() {
        assertThat(authenticationFilter.compilePattern(null)).isNull();
    }

    @Test
    public void testCompileWrongPattern() {
        assertThat(authenticationFilter.compilePattern("((((")).isNull();
    }

    @Test
    public void testCompileSimplePattern() {
        final String patternToCompile = "test";
        assertThat(authenticationFilter.compilePattern(patternToCompile)).isNotNull().has(new Condition<>() {

            @Override
            public boolean matches(final Pattern pattern) {
                return pattern.pattern().equalsIgnoreCase(patternToCompile);
            }
        });
    }

    @Test
    public void testCompileExcludePattern() {
        final String patternToCompile = "^/(bonita/)?(login.jsp$)|(images/)|(redirectCasToCatchHash.jsp)|(loginservice)|(serverAPI)|(maintenance.jsp$)|(API/platform/)|(platformloginservice$)|(/bonita/?$)|(logoutservice)";
        assertThat(authenticationFilter.compilePattern(patternToCompile)).isNotNull().has(new Condition<>() {

            @Override
            public boolean matches(final Pattern pattern) {
                return pattern.pattern().equalsIgnoreCase(patternToCompile);
            }
        });
    }

    private void matchExcludePattern(final String urlToMatch, final Boolean mustMatch) {
        if (authenticationFilter.matchExcludePatterns(urlToMatch) != mustMatch) {
            Assertions.fail("Matching excludePattern and the Url " + urlToMatch + " must return " + mustMatch);
        }
    }

    private AuthenticationRule createPassingRule() {
        return new AuthenticationRule() {

            @Override
            public boolean doAuthorize(final HttpServletRequestAccessor request, HttpServletResponse response) {
                return true;
            }
        };
    }

    private AuthenticationRule createFailingRule() {
        return new AuthenticationRule() {

            @Override
            public boolean doAuthorize(final HttpServletRequestAccessor request, HttpServletResponse response) {
                return false;
            }
        };
    }

    private AuthenticationRule createThrowingExceptionRule(RuntimeException e) {
        return new AuthenticationRule() {

            @Override
            public boolean doAuthorize(final HttpServletRequestAccessor request, HttpServletResponse response) {
                throw e;
            }
        };
    }

}
