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
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.user.User;
import org.bonitasoft.web.server.login.LoginFailureTracker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
}
