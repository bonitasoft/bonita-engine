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
import static org.mockito.Matchers.*;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
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

    @Spy
    @InjectMocks
    LoginManager loginManager = new LoginManager();

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
}
