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
package org.bonitasoft.console.common.server.login.servlet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.bonitasoft.console.common.server.auth.AuthenticationFailedException;
import org.bonitasoft.console.common.server.auth.AuthenticationManager;
import org.bonitasoft.console.common.server.auth.AuthenticationManagerNotFoundException;
import org.bonitasoft.console.common.server.auth.impl.standard.StandardAuthenticationManagerImpl;
import org.bonitasoft.console.common.server.login.AccountLockedException;
import org.bonitasoft.console.common.server.login.LoginFailedException;
import org.bonitasoft.console.common.server.login.LoginManager;
import org.bonitasoft.console.common.server.utils.SessionUtil;
import org.bonitasoft.engine.exception.TenantStatusException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.server.login.LoginFailureTracker;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Created by Vincent Elcrin
 * Date: 10/09/13
 * Time: 15:18
 */
@RunWith(MockitoJUnitRunner.class)
public class LoginServletTest {

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();
    @Mock
    HttpServletRequest req;

    @Mock
    HttpServletResponse resp;

    @Mock
    HttpSession httpSession;

    @Mock
    APISession apiSession;

    @Before
    public void setup() {
        doReturn("application/x-www-form-urlencoded").when(req).getContentType();
    }

    @After
    public void tearDown() {
        System.clearProperty(LoginServlet.ENABLE_DEV_SUITE_LOGIN);
    }

    @Test
    public void testPasswordIsDroppedWhenParameterIsLast() throws Exception {
        final String cleanQueryString = LoginServlet.dropPassword("?username=walter.bates&password=bpm");

        assertThat(cleanQueryString, is("?username=walter.bates"));
    }

    @Test
    public void testPasswordIsDroppedWhenParameterIsBeforeHash() throws Exception {
        final String cleanQueryString = LoginServlet.dropPassword("?username=walter.bates&password=bpm#hash");

        assertThat(cleanQueryString, is("?username=walter.bates#hash"));
    }

    @Test
    public void testUrlIsDroppedWhenParameterIsFirstAndBeforeHash() throws Exception {
        final String cleanQueryString = LoginServlet.dropPassword("?username=walter.bates&password=bpm#hash");

        assertThat(cleanQueryString, is("?username=walter.bates#hash"));
    }

    @Test
    public void testUrlStayTheSameIfNoPasswordArePresent() throws Exception {
        final String cleanQueryString = LoginServlet.dropPassword("?param=value#dhash");

        assertThat(cleanQueryString, is("?param=value#dhash"));
    }

    @Test
    public void testPasswordIsDroppedEvenIfQueryMarkUpIsntThere() throws Exception {
        final String cleanQueryString = LoginServlet.dropPassword("password=bpm#dhash1&dhash2");

        assertThat(cleanQueryString, is("#dhash1&dhash2"));
    }

    @Test
    public void testUrlStayEmptyIfParameterIsEmpty() throws Exception {
        final String cleanQueryString = LoginServlet.dropPassword("");

        assertThat(cleanQueryString, is(""));
    }

    @Test
    public void testDropPasswordOnRealUrl() throws Exception {
        final String cleanUrl = LoginServlet
                .dropPassword(
                        "?username=walter.bates&password=bpm&redirectUrl=http%3A%2F%2Flocalhost%3A8080%2Fbonita%2Fapps%2FappDirectoryBonita%3Flocale%3Den%23form%3DPool-\n"
                                +
                                "-1.0%24entry%26process%3D8506394779365952706%26mode%3Dapp");

        assertThat(cleanUrl,
                is("?username=walter.bates&redirectUrl=http%3A%2F%2Flocalhost%3A8080%2Fbonita%2Fapps%2FappDirectoryBonita%3Flocale%3Den%23form%3DPool-\n"
                        +
                        "-1.0%24entry%26process%3D8506394779365952706%26mode%3Dapp"));
    }

    @Test
    public void testDoGetShouldDropPassowrdWhenLoggingQueryString() throws Exception {
        //given
        System.setProperty(LoginServlet.ENABLE_DEV_SUITE_LOGIN, "true");
        final LoginServlet servlet = spy(new LoginServlet());
        doReturn("password=123&username=john").when(req).getQueryString();
        doNothing().when(servlet).doPost(req, resp);

        systemOutRule.clearLog();
        //when
        servlet.doGet(req, resp);

        //then
        assertThat(systemOutRule.getLog())
                .containsPattern(".*TRACE.*.username=john")
                .doesNotContain("password")
                .doesNotContain("123");
    }

    @Test
    public void testDoGetShouldfailWhenSysPropNotSet() throws Exception {
        //given
        final LoginServlet servlet = spy(new LoginServlet());

        //when
        servlet.doGet(req, resp);

        //then
        verify(servlet, never()).doPost(req, resp);
        verify(resp).setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    public void testDoPostShouldNotUseQueryString() throws Exception {

        //given
        final LoginServlet servlet = spy(new LoginServlet());
        doReturn(httpSession).when(req).getSession();
        doReturn(apiSession).when(httpSession).getAttribute(SessionUtil.API_SESSION_PARAM_KEY);
        doReturn("query string").when(req).getQueryString();
        doReturn(null).when(req).getParameter(AuthenticationManager.REDIRECT_AFTER_LOGIN_PARAM_NAME);
        doNothing().when(servlet).doLogin(req, resp);

        //when
        systemOutRule.clearLog();
        servlet.doPost(req, resp);

        //then
        assertThat(systemOutRule.getLog()).doesNotContain(req.getQueryString());
    }

    @Test
    public void should_send_error_401_when_login_with_wrong_credentials_and_no_redirect() throws Exception {
        final LoginServlet servlet = spy(new LoginServlet());
        doThrow(new LoginFailedException("")).when(servlet).doLogin(req, resp);

        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void should_send_error_415_when_login_with_wrong_content_type() throws Exception {
        final LoginServlet servlet = spy(new LoginServlet());
        doReturn("application/json").when(req).getContentType();

        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    public void should_login_with_no_content_type() throws Exception {
        final LoginServlet servlet = spy(new LoginServlet());
        final LoginManager loginManager = mock(LoginManager.class);
        doReturn(null).when(req).getContentType();
        doReturn(httpSession).when(req).getSession();
        doReturn(apiSession).when(httpSession).getAttribute(SessionUtil.API_SESSION_PARAM_KEY);
        doReturn(loginManager).when(servlet).getLoginManager();
        doNothing().when(loginManager).login(req, resp);

        servlet.doPost(req, resp);

        verify(loginManager).login(req, resp);
        verify(resp).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    public void should_login_with_content_type_containing_charset() throws Exception {
        final LoginServlet servlet = spy(new LoginServlet());
        final LoginManager loginManager = mock(LoginManager.class);
        doReturn("application/x-www-form-urlencoded; charset=UTF-8").when(req).getContentType();
        doReturn(httpSession).when(req).getSession();
        doReturn(apiSession).when(httpSession).getAttribute(SessionUtil.API_SESSION_PARAM_KEY);
        doReturn(loginManager).when(servlet).getLoginManager();
        doNothing().when(loginManager).login(req, resp);

        servlet.doPost(req, resp);

        verify(loginManager).login(req, resp);
        verify(resp, never()).setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    public void should_not_redirect_after_login_when_redirect_parameter_is_set_to_false_in_request() throws Exception {
        final LoginServlet servlet = spy(new LoginServlet());
        final LoginManager loginManager = mock(LoginManager.class);
        doReturn("false").when(req).getParameter(AuthenticationManager.REDIRECT_AFTER_LOGIN_PARAM_NAME);
        doReturn("anyurl").when(req).getParameter(AuthenticationManager.REDIRECT_URL);
        doReturn(httpSession).when(req).getSession();
        doReturn(apiSession).when(httpSession).getAttribute(SessionUtil.API_SESSION_PARAM_KEY);
        doReturn(loginManager).when(servlet).getLoginManager();
        doNothing().when(loginManager).login(req, resp);

        servlet.doPost(req, resp);

        verify(resp, never()).sendRedirect(anyString());
    }

    @Test
    public void should_not_redirect_after_login_when_user_has_no_profile() throws Exception {
        final LoginServlet servlet = spy(new LoginServlet());
        final LoginManager loginManager = mock(LoginManager.class);
        final ServletContext servletContext = mock(ServletContext.class);
        RequestDispatcher requestDispatcher = mock(RequestDispatcher.class);
        doReturn(new StandardAuthenticationManagerImpl()).when(servlet).getAuthenticationManager();
        doReturn("/bonita").when(req).getContextPath();
        doReturn("true").when(req).getParameter(AuthenticationManager.REDIRECT_AFTER_LOGIN_PARAM_NAME);
        doReturn("anyurl").when(req).getParameter(AuthenticationManager.REDIRECT_URL);
        doReturn(httpSession).when(req).getSession();
        doReturn(apiSession).when(httpSession).getAttribute(SessionUtil.API_SESSION_PARAM_KEY);
        doReturn(loginManager).when(servlet).getLoginManager();
        doReturn(servletContext).when(servlet).getServletContext();
        doReturn(requestDispatcher).when(servletContext).getRequestDispatcher(anyString());
        doNothing().when(loginManager).login(req, resp);
        doReturn(false).when(servlet).hasProfile(apiSession);

        servlet.doPost(req, resp);

        verify(resp, never()).sendRedirect(anyString());
        verify(servletContext).getRequestDispatcher(AuthenticationManager.LOGIN_PAGE);
        verify(requestDispatcher).forward(req, resp);
    }

    @Test
    public void should_redirect_after_login_when_redirect_url_is_set_in_request() throws Exception {
        final LoginServlet servlet = spy(new LoginServlet());
        final LoginManager loginManager = mock(LoginManager.class);
        doReturn("anyurl").when(req).getParameter(AuthenticationManager.REDIRECT_URL);
        doReturn(httpSession).when(req).getSession();
        doReturn(apiSession).when(httpSession).getAttribute(SessionUtil.API_SESSION_PARAM_KEY);
        doReturn(loginManager).when(servlet).getLoginManager();
        doNothing().when(loginManager).login(req, resp);
        doReturn(true).when(servlet).hasProfile(apiSession);

        servlet.doPost(req, resp);

        verify(resp).sendRedirect(startsWith("anyurl"));
    }

    @Test
    public void should_redirect_after_login_when_redirect_parameter_is_set_to_true_in_request() throws Exception {
        final LoginServlet servlet = spy(new LoginServlet());
        final LoginManager loginManager = mock(LoginManager.class);
        doReturn("true").when(req).getParameter(AuthenticationManager.REDIRECT_AFTER_LOGIN_PARAM_NAME);
        doReturn(httpSession).when(req).getSession();
        doReturn(apiSession).when(httpSession).getAttribute(SessionUtil.API_SESSION_PARAM_KEY);
        doReturn(loginManager).when(servlet).getLoginManager();
        doNothing().when(loginManager).login(req, resp);
        doReturn(true).when(servlet).hasProfile(apiSession);

        servlet.doPost(req, resp);

        verify(resp).sendRedirect(startsWith(AuthenticationManager.DEFAULT_DIRECT_URL));
    }

    @Test(expected = ServletException.class)
    public void should_send_servlet_exception_when_login_with_wrong_credentials_and_redirect() throws Exception {
        final LoginServlet servlet = spy(new LoginServlet());
        doReturn("true").when(req).getParameter(AuthenticationManager.REDIRECT_AFTER_LOGIN_PARAM_NAME);
        doThrow(new LoginFailedException("")).when(servlet).doLogin(req, resp);

        servlet.doPost(req, resp);
    }

    @Test(expected = ServletException.class)
    public void should_throw_tenant_status_exception() throws Exception {
        final LoginServlet servlet = spy(new LoginServlet());
        doReturn("true").when(req).getParameter(AuthenticationManager.REDIRECT_AFTER_LOGIN_PARAM_NAME);
        doThrow(new TenantStatusException("")).when(servlet).doLogin(req, resp);

        servlet.doPost(req, resp);
    }

    @Test(expected = ServletException.class)
    public void should_return_servlet_exception_when_throw_authentication_failed_exception() throws Exception {
        final LoginServlet servlet = spy(new LoginServlet());
        doReturn("true").when(req).getParameter(AuthenticationManager.REDIRECT_AFTER_LOGIN_PARAM_NAME);
        doThrow(new AuthenticationFailedException("")).when(servlet).doLogin(req, resp);

        servlet.doPost(req, resp);
    }

    @Test(expected = ServletException.class)
    public void should_return_servlet_exception_when_authentication_manager_not_found_exception() throws Exception {
        final LoginServlet servlet = spy(new LoginServlet());
        doReturn("true").when(req).getParameter(AuthenticationManager.REDIRECT_AFTER_LOGIN_PARAM_NAME);
        doThrow(new AuthenticationManagerNotFoundException("")).when(servlet).doLogin(req, resp);

        servlet.doPost(req, resp);
    }

    @Test
    public void should_sanitize_semicolon_traversal_in_loginUrl_on_login_failure() throws Exception {
        final LoginServlet servlet = spy(new LoginServlet());
        final ServletContext servletContext = mock(ServletContext.class);
        RequestDispatcher requestDispatcher = mock(RequestDispatcher.class);
        doReturn("").when(req).getContextPath();
        doReturn("true").when(req).getParameter(AuthenticationManager.REDIRECT_AFTER_LOGIN_PARAM_NAME);
        // Malicious loginUrl using semicolon-based path traversal
        doReturn("/login.jsp;/..;/serverAPI").when(req).getParameter(AuthenticationManager.LOGIN_URL_PARAM_NAME);
        doThrow(new LoginFailedException("")).when(servlet).doLogin(req, resp);

        servlet.doPost(req, resp);

        // Verify the dispatched path has semicolons stripped and is normalized.
        // After sanitization: /login.jsp;/..;/serverAPI -> /login.jsp/../serverAPI
        // After normalization: /login.jsp/../serverAPI -> /serverAPI
        // The HttpAPIServlet FORWARD does not happen if it does not start with context path + login.jsp
        verify(servletContext, never()).getRequestDispatcher(anyString());
        verify(resp).setStatus(HttpServletResponse.SC_UNAUTHORIZED, "loginFailMessage");
    }

    @Test
    public void should_fallback_to_login_page_when_loginUrl_has_invalid_uri_chars() throws Exception {
        final LoginServlet servlet = spy(new LoginServlet());
        final ServletContext servletContext = mock(ServletContext.class);
        RequestDispatcher requestDispatcher = mock(RequestDispatcher.class);
        doReturn("true").when(req).getParameter(AuthenticationManager.REDIRECT_AFTER_LOGIN_PARAM_NAME);
        // loginUrl with characters invalid for URI (curly braces trigger URISyntaxException)
        doReturn("/apps/{invalid}/page").when(req).getParameter(AuthenticationManager.LOGIN_URL_PARAM_NAME);
        doReturn(servletContext).when(servlet).getServletContext();
        doReturn(requestDispatcher).when(servletContext).getRequestDispatcher(anyString());
        doThrow(new LoginFailedException("")).when(servlet).doLogin(req, resp);

        servlet.doPost(req, resp);

        // Should fall back to the default login page instead of crashing
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(servletContext).getRequestDispatcher(pathCaptor.capture());
        assertThat(pathCaptor.getValue()).startsWith(AuthenticationManager.LOGIN_PAGE);
    }

    @Test
    public void should_set_account_locked_message_when_AccountLockedException_on_redirect() throws Exception {
        final LoginServlet servlet = spy(new LoginServlet());
        final ServletContext servletContext = mock(ServletContext.class);
        RequestDispatcher requestDispatcher = mock(RequestDispatcher.class);
        doReturn("true").when(req).getParameter(AuthenticationManager.REDIRECT_AFTER_LOGIN_PARAM_NAME);
        doReturn(servletContext).when(servlet).getServletContext();
        doReturn(requestDispatcher).when(servletContext).getRequestDispatcher(anyString());
        doThrow(new AccountLockedException("Too many failed login attempts. Please try again later.")).when(servlet)
                .doLogin(req, resp);

        servlet.doPost(req, resp);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(req).setAttribute(eq(LoginServlet.LOGIN_FAIL_MESSAGE),
                messageCaptor.capture());
        assertThat(messageCaptor.getValue()).isEqualTo("accountLockedMessage");
    }

    @Test
    public void should_return_429_with_retry_after_header_when_AccountLockedException_and_no_redirect()
            throws Exception {
        final LoginServlet servlet = spy(new LoginServlet());
        LoginFailureTracker tracker = mock(LoginFailureTracker.class);
        doReturn(600L).when(tracker).getLockoutDurationSeconds();
        setField(servlet, "loginFailureTracker", tracker);
        doThrow(new AccountLockedException("Too many failed login attempts. Please try again later.")).when(servlet)
                .doLogin(req, resp);

        servlet.doPost(req, resp);

        verify(resp).setStatus(429);
        verify(resp).setHeader("Retry-After", "600");
    }

    @Test
    public void should_init_resolve_tracker_from_spring_context_and_pass_to_login_manager() throws Exception {
        // given
        final LoginServlet servlet = spy(new LoginServlet());
        final ServletContext servletContext = mock(ServletContext.class);
        final LoginFailureTracker tracker = mock(LoginFailureTracker.class);

        // Mock the ServletConfig to return our mock ServletContext
        javax.servlet.ServletConfig servletConfig = mock(javax.servlet.ServletConfig.class);
        doReturn(servletContext).when(servletConfig).getServletContext();

        // Mock Spring context lookup
        org.springframework.web.context.WebApplicationContext springContext = mock(
                org.springframework.web.context.WebApplicationContext.class);
        doReturn(springContext).when(servletContext).getAttribute(
                org.springframework.web.context.WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        doReturn(tracker).when(springContext).getBean(LoginFailureTracker.class);

        // when
        servlet.init(servletConfig);

        // then — the tracker resolved from Spring context is stored in the servlet
        assertThat(getField(servlet, "loginFailureTracker")).isSameAs(tracker);
    }

    @Test
    public void should_set_login_fail_message_when_LoginFailedException_on_redirect() throws Exception {
        final LoginServlet servlet = spy(new LoginServlet());
        final ServletContext servletContext = mock(ServletContext.class);
        RequestDispatcher requestDispatcher = mock(RequestDispatcher.class);
        doReturn("true").when(req).getParameter(AuthenticationManager.REDIRECT_AFTER_LOGIN_PARAM_NAME);
        doReturn(servletContext).when(servlet).getServletContext();
        doReturn(requestDispatcher).when(servletContext).getRequestDispatcher(anyString());
        doThrow(new LoginFailedException("wrong credentials")).when(servlet).doLogin(req, resp);

        servlet.doPost(req, resp);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(req).setAttribute(eq(LoginServlet.LOGIN_FAIL_MESSAGE),
                messageCaptor.capture());
        assertThat(messageCaptor.getValue()).isEqualTo("loginFailMessage");
    }
}
