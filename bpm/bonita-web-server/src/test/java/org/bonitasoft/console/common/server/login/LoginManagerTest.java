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
package org.bonitasoft.console.common.server.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.bonitasoft.console.common.server.auth.AuthenticationFailedException;
import org.bonitasoft.console.common.server.auth.AuthenticationManager;
import org.bonitasoft.console.common.server.login.credentials.Credentials;
import org.bonitasoft.console.common.server.login.credentials.StandardCredentials;
import org.bonitasoft.console.common.server.login.credentials.UserLogger;
import org.bonitasoft.console.common.server.login.filter.TokenGenerator;
import org.bonitasoft.console.common.server.utils.SessionUtil;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.user.User;
import org.bonitasoft.web.server.login.LoginFailureTracker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class LoginManagerTest {

    @Mock
    UserLogger userLogger;

    @Mock
    HttpServletRequest request;

    MockHttpServletResponse response = new MockHttpServletResponse();

    @Mock
    HttpSession session;

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    APISession apiSession;

    @Mock
    TokenGenerator tokenGenerator;

    @Mock
    LoginFailureTracker loginFailureTracker;

    @Mock
    AuthenticationAuditLogger authenticationAuditLogger;

    @Spy
    @InjectMocks
    LoginManager loginManager = new LoginManager(null);

    HttpServletRequestAccessor requestAccessor;

    @Before
    public void setUp() throws Exception {
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("/bonita");
        requestAccessor = new HttpServletRequestAccessor(request);

        doReturn("123").when(tokenGenerator).createOrLoadToken(session);
    }

    @Test
    public void login_should_initSession() throws Exception {
        final Credentials credentials = new StandardCredentials("name", "password");
        doReturn(authenticationManager).when(loginManager).getAuthenticationManager();
        doReturn(apiSession).when(userLogger).doLogin(credentials);

        loginManager.loginInternal(requestAccessor, response, userLogger, credentials);

        verify(loginManager).initSession(eq(requestAccessor), eq(apiSession), any(User.class), anyBoolean());
        verify(session).invalidate();
    }

    @Test
    public void login_should_perform_engine_login() throws Exception {
        final Credentials credentials = new StandardCredentials("name", "password");
        doReturn(authenticationManager).when(loginManager).getAuthenticationManager();
        doReturn(apiSession).when(userLogger).doLogin(credentials);

        loginManager.loginInternal(requestAccessor, response, userLogger, credentials);

        verify(userLogger).doLogin(credentials);
    }

    @Test
    public void login_should_perform_engine_login_with_credentials_map() throws Exception {
        final Credentials credentials = new StandardCredentials("name", "password");
        doReturn(authenticationManager).when(loginManager).getAuthenticationManager();
        final Map<String, Serializable> credentialsMap = new HashMap<>();
        credentialsMap.put("principal", "userId");
        doReturn(credentialsMap).when(authenticationManager).authenticate(requestAccessor, credentials);
        doReturn(apiSession).when(userLogger).doLogin(credentialsMap);

        loginManager.loginInternal(requestAccessor, response, userLogger, credentials);

        verify(userLogger).doLogin(credentialsMap);
        verify(loginManager).storeCredentials(requestAccessor, apiSession, true);
    }

    @Test
    public void login_should_perform_engine_login_with_credentials_map_without_invalidating_session() throws Exception {
        final Credentials credentials = new StandardCredentials("name", "password");
        doReturn(authenticationManager).when(loginManager).getAuthenticationManager();
        final Map<String, Serializable> credentialsMap = new HashMap<>();
        credentialsMap.put("principal", "userId");
        credentialsMap.put(AuthenticationManager.INVALIDATE_SESSION, Boolean.FALSE);
        doReturn(credentialsMap).when(authenticationManager).authenticate(requestAccessor, credentials);
        doReturn(apiSession).when(userLogger).doLogin(credentialsMap);

        loginManager.loginInternal(requestAccessor, response, userLogger, credentials);

        verify(userLogger).doLogin(credentialsMap);
        verify(loginManager).storeCredentials(requestAccessor, apiSession, false);
    }

    @Test(expected = LoginFailedException.class)
    public void login_should_throw_exception_when_login_fails() throws Exception {
        final Credentials credentials = new StandardCredentials("name", "password");
        doReturn(authenticationManager).when(loginManager).getAuthenticationManager();
        doThrow(LoginFailedException.class).when(userLogger).doLogin(credentials);

        loginManager.loginInternal(requestAccessor, response, userLogger, credentials);
    }

    @Test(expected = AuthenticationFailedException.class)
    public void login_should_throw_exception_when_authentication_fails() throws Exception {
        final Credentials credentials = new StandardCredentials("name", "password");
        doReturn(authenticationManager).when(loginManager).getAuthenticationManager();
        doThrow(AuthenticationFailedException.class).when(authenticationManager).authenticate(requestAccessor,
                credentials);

        loginManager.loginInternal(requestAccessor, response, userLogger, credentials);
    }

    @Test(expected = AuthenticationFailedException.class)
    public void login_should_throw_exception_when_no_credentials_are_passed() throws Exception {
        final Credentials credentials = new StandardCredentials(null, null);
        doReturn(authenticationManager).when(loginManager).getAuthenticationManager();
        doReturn(Collections.emptyMap()).when(authenticationManager).authenticate(requestAccessor, credentials);

        loginManager.loginInternal(requestAccessor, response, userLogger, credentials);
    }

    @Test
    public void should_store_csrf_token_in_cookies() throws Exception {
        Credentials credentials = new StandardCredentials("name", "password");
        doReturn(authenticationManager).when(loginManager).getAuthenticationManager();
        doReturn(apiSession).when(userLogger).doLogin(credentials);

        loginManager.loginInternal(requestAccessor, response, userLogger, credentials);

        verify(tokenGenerator).createOrLoadToken(session);
        verify(tokenGenerator, never()).setTokenToResponseHeader(any(HttpServletResponse.class), anyString());
        assertThat(response.getCookie(TokenGenerator.X_BONITA_API_TOKEN).getValue()).isEqualTo("123");
    }

    @Test
    public void loginInternal_should_throw_LoginFailedException_when_user_is_locked_out() throws Exception {
        final Credentials credentials = new StandardCredentials("lockedUser", "password");
        when(loginFailureTracker.isLockedOut("lockedUser")).thenReturn(true);

        assertThatThrownBy(() -> loginManager.loginInternal(requestAccessor, response, userLogger, credentials))
                .isInstanceOf(AccountLockedException.class)
                .hasMessageContaining("Too many failed login attempts");

        verify(userLogger, never()).doLogin(any(Credentials.class));
    }

    @Test
    public void loginInternal_should_record_failure_on_login_failure() throws Exception {
        final Credentials credentials = new StandardCredentials("user1", "wrongPassword");
        doReturn(authenticationManager).when(loginManager).getAuthenticationManager();
        doThrow(LoginFailedException.class).when(userLogger).doLogin(credentials);

        assertThatThrownBy(() -> loginManager.loginInternal(requestAccessor, response, userLogger, credentials))
                .isInstanceOf(LoginFailedException.class);

        verify(loginFailureTracker).recordFailure("user1");
    }

    @Test
    public void loginInternal_should_record_failure_on_authentication_failure() throws Exception {
        final Credentials credentials = new StandardCredentials("user1", "password");
        doReturn(authenticationManager).when(loginManager).getAuthenticationManager();
        doThrow(AuthenticationFailedException.class).when(authenticationManager).authenticate(requestAccessor,
                credentials);

        assertThatThrownBy(() -> loginManager.loginInternal(requestAccessor, response, userLogger, credentials))
                .isInstanceOf(AuthenticationFailedException.class);

        verify(loginFailureTracker).recordFailure("user1");
    }

    @Test
    public void loginInternal_should_reset_failures_on_successful_login() throws Exception {
        final Credentials credentials = new StandardCredentials("user1", "password");
        doReturn(authenticationManager).when(loginManager).getAuthenticationManager();
        doReturn(apiSession).when(userLogger).doLogin(credentials);

        loginManager.loginInternal(requestAccessor, response, userLogger, credentials);

        verify(loginFailureTracker).resetFailures("user1");
    }

    @Test
    public void loginInternal_should_write_a_login_success_audit_event_once_the_session_is_initialized()
            throws Exception {
        final Credentials credentials = new StandardCredentials("walter.bates", "bpm");
        doReturn(authenticationManager).when(loginManager).getAuthenticationManager();
        doReturn(apiSession).when(userLogger).doLogin(credentials);
        doReturn("walter.bates").when(apiSession).getUserName();
        doReturn(42L).when(apiSession).getId();

        loginManager.loginInternal(requestAccessor, response, userLogger, credentials);

        final InOrder inOrder = inOrder(loginManager, authenticationAuditLogger);
        inOrder.verify(loginManager).storeCredentials(requestAccessor, apiSession, true);
        inOrder.verify(authenticationAuditLogger).loginSucceeded(request, "walter.bates",
                AuthenticationAuditLogger.METHOD_STANDARD, null, 42L);
        verify(session).setAttribute(SessionUtil.AUDITED_LOGIN_METHOD_SESSION_PARAM,
                AuthenticationAuditLogger.METHOD_STANDARD);
    }

    @Test
    public void loginInternal_should_keep_the_audited_login_method_in_the_http_session_created_by_the_login()
            throws Exception {
        final Credentials credentials = new StandardCredentials("walter.bates", "bpm");
        doReturn(authenticationManager).when(loginManager).getAuthenticationManager();
        doReturn(apiSession).when(userLogger).doLogin(credentials);
        // Like a servlet container: once invalidated (session fixation protection), a new session is created
        final HttpSession newSession = mock(HttpSession.class);
        doAnswer(invocation -> doReturn(newSession).when(request).getSession()).when(session).invalidate();
        doReturn("123").when(tokenGenerator).createOrLoadToken(newSession);

        loginManager.loginInternal(requestAccessor, response, userLogger, credentials);

        verify(session).invalidate();
        verify(newSession).setAttribute(SessionUtil.AUDITED_LOGIN_METHOD_SESSION_PARAM,
                AuthenticationAuditLogger.METHOD_STANDARD);
        verify(session, never()).setAttribute(eq(SessionUtil.AUDITED_LOGIN_METHOD_SESSION_PARAM), any());
    }

    @Test
    public void loginInternal_should_not_write_a_login_success_audit_event_when_login_fails() throws Exception {
        final Credentials credentials = new StandardCredentials("walter.bates", "wrongPassword");
        doReturn(authenticationManager).when(loginManager).getAuthenticationManager();
        doThrow(LoginFailedException.class).when(userLogger).doLogin(credentials);

        assertThatThrownBy(() -> loginManager.loginInternal(requestAccessor, response, userLogger, credentials))
                .isInstanceOf(LoginFailedException.class);

        verify(authenticationAuditLogger, never()).loginSucceeded(any(), any(), any(), any(), anyLong());
    }

    @Test
    public void loginInternal_should_write_a_login_failure_audit_event_when_engine_login_fails() throws Exception {
        final Credentials credentials = new StandardCredentials("walter.bates", "wrongPassword");
        doReturn(authenticationManager).when(loginManager).getAuthenticationManager();
        doThrow(LoginFailedException.class).when(userLogger).doLogin(credentials);

        assertThatThrownBy(() -> loginManager.loginInternal(requestAccessor, response, userLogger, credentials))
                .isInstanceOf(LoginFailedException.class);

        verify(authenticationAuditLogger).loginFailed(request, "walter.bates",
                AuthenticationAuditLogger.METHOD_STANDARD, null, null);
        verifyNoMoreInteractions(authenticationAuditLogger);
    }

    @Test
    public void loginInternal_should_write_a_login_failure_audit_event_when_authentication_fails()
            throws Exception {
        final Credentials credentials = new StandardCredentials("walter.bates", "password");
        doReturn(authenticationManager).when(loginManager).getAuthenticationManager();
        doThrow(AuthenticationFailedException.class).when(authenticationManager).authenticate(requestAccessor,
                credentials);

        assertThatThrownBy(() -> loginManager.loginInternal(requestAccessor, response, userLogger, credentials))
                .isInstanceOf(AuthenticationFailedException.class);

        verify(authenticationAuditLogger).loginFailed(request, "walter.bates",
                AuthenticationAuditLogger.METHOD_STANDARD, null, null);
        verifyNoMoreInteractions(authenticationAuditLogger);
    }

    @Test
    public void loginInternal_should_write_a_locked_out_audit_event_when_user_is_locked_out() throws Exception {
        final Credentials credentials = new StandardCredentials("walter.bates", "password");
        doReturn(true).when(loginFailureTracker).isLockedOut("walter.bates");

        assertThatThrownBy(() -> loginManager.loginInternal(requestAccessor, response, userLogger, credentials))
                .isInstanceOf(AccountLockedException.class);

        verify(authenticationAuditLogger).loginFailed(request, "walter.bates",
                AuthenticationAuditLogger.METHOD_STANDARD, AuthenticationAuditLogger.REASON_LOCKED_OUT);
        verifyNoMoreInteractions(authenticationAuditLogger);
    }

    @Test
    public void loginInternal_should_not_write_any_audit_event_when_no_credentials_are_passed() throws Exception {
        final Credentials credentials = new StandardCredentials(null, null);
        doReturn(authenticationManager).when(loginManager).getAuthenticationManager();
        doReturn(Collections.emptyMap()).when(authenticationManager).authenticate(requestAccessor, credentials);

        assertThatThrownBy(() -> loginManager.loginInternal(requestAccessor, response, userLogger, credentials))
                .isInstanceOf(AuthenticationFailedException.class);

        verifyNoInteractions(authenticationAuditLogger);
    }

    @Test
    public void loginInternal_should_write_the_sso_method_in_the_login_success_audit_event() throws Exception {
        final Credentials credentials = new StandardCredentials(null, null);
        doReturn(authenticationManager).when(loginManager).getAuthenticationManager();
        final Map<String, Serializable> credentialsMap = new HashMap<>();
        credentialsMap.put(AuthenticationManager.USERNAME_CREDENTIAL, "walter.bates");
        doReturn(credentialsMap).when(authenticationManager).authenticate(requestAccessor, credentials);
        doReturn("oidc").when(authenticationManager).getAuthMethod();
        doReturn(apiSession).when(userLogger).doLogin(credentialsMap);
        doReturn("walter.bates").when(apiSession).getUserName();
        doReturn(42L).when(apiSession).getId();

        loginManager.loginInternal(requestAccessor, response, userLogger, credentials);

        verify(authenticationAuditLogger).loginSucceeded(request, "walter.bates", "oidc", null, 42L);
        verify(session).setAttribute(SessionUtil.AUDITED_LOGIN_METHOD_SESSION_PARAM, "oidc");
        verify(session, never()).setAttribute(eq(SessionUtil.AUDITED_LOGIN_PROVIDER_SESSION_PARAM), any());
    }

    @Test
    public void loginInternal_should_write_the_sso_provider_in_the_login_success_audit_event() throws Exception {
        // e.g. OIDC with several providers configured
        final Credentials credentials = new StandardCredentials(null, null);
        doReturn(authenticationManager).when(loginManager).getAuthenticationManager();
        final Map<String, Serializable> credentialsMap = new HashMap<>();
        credentialsMap.put(AuthenticationManager.USERNAME_CREDENTIAL, "walter.bates");
        doReturn(credentialsMap).when(authenticationManager).authenticate(requestAccessor, credentials);
        doReturn("oidc").when(authenticationManager).getAuthMethod();
        doReturn("google").when(authenticationManager).getAuthProvider(requestAccessor);
        doReturn(apiSession).when(userLogger).doLogin(credentialsMap);
        doReturn("walter.bates").when(apiSession).getUserName();
        doReturn(42L).when(apiSession).getId();

        loginManager.loginInternal(requestAccessor, response, userLogger, credentials);

        verify(authenticationAuditLogger).loginSucceeded(request, "walter.bates", "oidc", "google", 42L);
        verify(session).setAttribute(SessionUtil.AUDITED_LOGIN_PROVIDER_SESSION_PARAM, "google");
    }

    @Test
    public void loginInternal_should_write_the_standard_method_when_the_sso_manager_returns_no_credentials()
            throws Exception {
        // e.g. a whitelisted account using the login form while OIDC is configured
        final Credentials credentials = new StandardCredentials("midlub", "password");
        doReturn(authenticationManager).when(loginManager).getAuthenticationManager();
        lenient().doReturn("oidc").when(authenticationManager).getAuthMethod();
        lenient().doReturn("google").when(authenticationManager).getAuthProvider(requestAccessor);
        doReturn(apiSession).when(userLogger).doLogin(credentials);
        doReturn("midlub").when(apiSession).getUserName();
        doReturn(42L).when(apiSession).getId();

        loginManager.loginInternal(requestAccessor, response, userLogger, credentials);

        verify(authenticationAuditLogger).loginSucceeded(request, "midlub", AuthenticationAuditLogger.METHOD_STANDARD,
                null, 42L);
    }

    @Test
    public void loginInternal_should_write_the_sso_identity_in_the_login_failure_audit_event() throws Exception {
        // e.g. a user authenticated by the identity provider but unknown in Bonita
        final Credentials credentials = new StandardCredentials(null, null);
        doReturn(authenticationManager).when(loginManager).getAuthenticationManager();
        final Map<String, Serializable> credentialsMap = new HashMap<>();
        credentialsMap.put(AuthenticationManager.USERNAME_CREDENTIAL, "unknown.user");
        doReturn(credentialsMap).when(authenticationManager).authenticate(requestAccessor, credentials);
        doReturn("oidc").when(authenticationManager).getAuthMethod();
        doReturn("google").when(authenticationManager).getAuthProvider(requestAccessor);
        doThrow(LoginFailedException.class).when(userLogger).doLogin(credentialsMap);

        assertThatThrownBy(() -> loginManager.loginInternal(requestAccessor, response, userLogger, credentials))
                .isInstanceOf(LoginFailedException.class);

        verify(authenticationAuditLogger).loginFailed(request, "unknown.user", "oidc", "google", null);
        verifyNoMoreInteractions(authenticationAuditLogger);
    }

    @Test
    public void loginInternal_should_write_a_login_failure_audit_event_for_a_failed_sso_login_without_username()
            throws Exception {
        // e.g. a CAS ticket rejected by the CAS server
        final Credentials credentials = new StandardCredentials(null, null);
        doReturn(authenticationManager).when(loginManager).getAuthenticationManager();
        final Map<String, Serializable> credentialsMap = new HashMap<>();
        credentialsMap.put("ticket", "ST-1-invalid");
        credentialsMap.put("service", "http://localhost:8080/bonita/apps/myapp/");
        doReturn(credentialsMap).when(authenticationManager).authenticate(requestAccessor, credentials);
        doReturn("cas").when(authenticationManager).getAuthMethod();
        doThrow(LoginFailedException.class).when(userLogger).doLogin(credentialsMap);

        assertThatThrownBy(() -> loginManager.loginInternal(requestAccessor, response, userLogger, credentials))
                .isInstanceOf(LoginFailedException.class);

        verify(authenticationAuditLogger).loginFailed(request, null, "cas", null, null);
        verifyNoMoreInteractions(authenticationAuditLogger);
    }

    @Test
    public void loginInternal_should_not_write_any_audit_event_when_a_not_audited_login_succeeds() throws Exception {
        final Credentials credentials = new StandardCredentials("guest", "guest");
        doReturn(authenticationManager).when(loginManager).getAuthenticationManager();
        doReturn(apiSession).when(userLogger).doLogin(credentials);

        loginManager.loginInternal(requestAccessor, response, userLogger, credentials, false);

        verify(userLogger).doLogin(credentials);
        verifyNoInteractions(authenticationAuditLogger);
        verify(session, never()).setAttribute(eq(SessionUtil.AUDITED_LOGIN_METHOD_SESSION_PARAM), any());
    }

    @Test
    public void loginInternal_should_not_write_any_audit_event_when_a_not_audited_login_fails() throws Exception {
        final Credentials credentials = new StandardCredentials("guest", "wrongPassword");
        doReturn(authenticationManager).when(loginManager).getAuthenticationManager();
        doThrow(LoginFailedException.class).when(userLogger).doLogin(credentials);

        assertThatThrownBy(
                () -> loginManager.loginInternal(requestAccessor, response, userLogger, credentials, false))
                .isInstanceOf(LoginFailedException.class);

        verifyNoInteractions(authenticationAuditLogger);
    }

    @Test
    public void loginInternal_should_not_write_any_audit_event_when_a_not_audited_user_is_locked_out()
            throws Exception {
        final Credentials credentials = new StandardCredentials("guest", "guest");
        doReturn(true).when(loginFailureTracker).isLockedOut("guest");

        assertThatThrownBy(
                () -> loginManager.loginInternal(requestAccessor, response, userLogger, credentials, false))
                .isInstanceOf(AccountLockedException.class);

        verifyNoInteractions(authenticationAuditLogger);
    }

    @Test
    public void loginInternal_should_not_check_lockout_when_username_is_empty() throws Exception {
        LoginManager managerWithTracker = spy(new LoginManager(loginFailureTracker));
        managerWithTracker.tokenGenerator = tokenGenerator;
        managerWithTracker.portalCookies = new PortalCookies();
        final Credentials credentials = new StandardCredentials("", "password");
        doReturn(authenticationManager).when(managerWithTracker).getAuthenticationManager();
        doReturn(Collections.emptyMap()).when(authenticationManager).authenticate(requestAccessor, credentials);

        assertThatThrownBy(() -> managerWithTracker.loginInternal(requestAccessor, response, userLogger, credentials))
                .isInstanceOf(AuthenticationFailedException.class);

        verify(loginFailureTracker, never()).isLockedOut(anyString());
    }

    @Test
    public void loginInternal_should_not_NPE_when_tracker_is_null_and_login_succeeds() throws Exception {
        LoginManager managerWithoutTracker = spy(new LoginManager(null));
        managerWithoutTracker.tokenGenerator = tokenGenerator;
        managerWithoutTracker.portalCookies = new PortalCookies();
        final Credentials credentials = new StandardCredentials("user1", "password");
        doReturn(authenticationManager).when(managerWithoutTracker).getAuthenticationManager();
        doReturn(apiSession).when(userLogger).doLogin(credentials);

        managerWithoutTracker.loginInternal(requestAccessor, response, userLogger, credentials);

        verify(userLogger).doLogin(credentials);
    }

    @Test
    public void loginInternal_should_not_NPE_when_tracker_is_null_and_login_fails() throws Exception {
        LoginManager managerWithoutTracker = spy(new LoginManager(null));
        final Credentials credentials = new StandardCredentials("user1", "wrongPassword");
        doReturn(authenticationManager).when(managerWithoutTracker).getAuthenticationManager();
        doThrow(LoginFailedException.class).when(userLogger).doLogin(credentials);

        assertThatThrownBy(
                () -> managerWithoutTracker.loginInternal(requestAccessor, response, userLogger, credentials))
                .isInstanceOf(LoginFailedException.class);
    }

    @Test
    public void logout_should_log_out_from_the_engine_clean_the_session_and_write_the_logout_audit_event()
            throws Exception {
        doReturn(apiSession).when(session).getAttribute(SessionUtil.API_SESSION_PARAM_KEY);
        doReturn("oidc").when(session).getAttribute(SessionUtil.AUDITED_LOGIN_METHOD_SESSION_PARAM);
        doReturn("walter.bates").when(apiSession).getUserName();
        doReturn(42L).when(apiSession).getId();

        loginManager.logout(requestAccessor, userLogger, true);

        final InOrder inOrder = inOrder(userLogger, session, authenticationAuditLogger);
        inOrder.verify(userLogger).doLogout(apiSession);
        inOrder.verify(session).removeAttribute(SessionUtil.AUDITED_LOGIN_METHOD_SESSION_PARAM);
        inOrder.verify(session).invalidate();
        inOrder.verify(authenticationAuditLogger).loggedOut(request, "walter.bates", "oidc", null, 42L);
    }

    @Test
    public void logout_should_write_the_provider_of_the_login_in_the_logout_audit_event() throws Exception {
        doReturn(apiSession).when(session).getAttribute(SessionUtil.API_SESSION_PARAM_KEY);
        doReturn("oidc").when(session).getAttribute(SessionUtil.AUDITED_LOGIN_METHOD_SESSION_PARAM);
        doReturn("google").when(session).getAttribute(SessionUtil.AUDITED_LOGIN_PROVIDER_SESSION_PARAM);
        doReturn("walter.bates").when(apiSession).getUserName();
        doReturn(42L).when(apiSession).getId();

        loginManager.logout(requestAccessor, userLogger, false);

        verify(session).removeAttribute(SessionUtil.AUDITED_LOGIN_PROVIDER_SESSION_PARAM);
        verify(authenticationAuditLogger).loggedOut(request, "walter.bates", "oidc", "google", 42L);
    }

    @Test
    public void logout_should_keep_the_http_session_when_asked() throws Exception {
        doReturn(apiSession).when(session).getAttribute(SessionUtil.API_SESSION_PARAM_KEY);
        doReturn("oidc").when(session).getAttribute(SessionUtil.AUDITED_LOGIN_METHOD_SESSION_PARAM);

        loginManager.logout(requestAccessor, userLogger, false);

        verify(userLogger).doLogout(apiSession);
        verify(session).removeAttribute(SessionUtil.API_SESSION_PARAM_KEY);
        verify(session, never()).invalidate();
        verify(authenticationAuditLogger).loggedOut(eq(request), any(), eq("oidc"), any(), anyLong());
    }

    @Test
    public void logout_should_not_write_any_audit_event_for_a_login_that_was_not_audited() throws Exception {
        // e.g. the guest user, or a session opened before the authentication audit log existed
        doReturn(apiSession).when(session).getAttribute(SessionUtil.API_SESSION_PARAM_KEY);

        loginManager.logout(requestAccessor, userLogger, true);

        verify(userLogger).doLogout(apiSession);
        verify(session).invalidate();
        verifyNoInteractions(authenticationAuditLogger);
    }

    @Test
    public void logout_should_only_clean_the_http_session_without_bonita_session() throws Exception {
        doReturn("standard").when(session).getAttribute(SessionUtil.AUDITED_LOGIN_METHOD_SESSION_PARAM);

        loginManager.logout(requestAccessor, userLogger, true);

        verifyNoInteractions(userLogger);
        verify(session).invalidate();
        verifyNoInteractions(authenticationAuditLogger);
    }

    @Test
    public void logout_should_leave_the_http_session_untouched_when_the_engine_logout_fails() throws Exception {
        doReturn(apiSession).when(session).getAttribute(SessionUtil.API_SESSION_PARAM_KEY);
        doReturn("standard").when(session).getAttribute(SessionUtil.AUDITED_LOGIN_METHOD_SESSION_PARAM);
        doThrow(LoginFailedException.class).when(userLogger).doLogout(apiSession);

        assertThatThrownBy(() -> loginManager.logout(requestAccessor, userLogger, true))
                .isInstanceOf(LoginFailedException.class);

        verify(session, never()).invalidate();
        verifyNoInteractions(authenticationAuditLogger);
    }
}
