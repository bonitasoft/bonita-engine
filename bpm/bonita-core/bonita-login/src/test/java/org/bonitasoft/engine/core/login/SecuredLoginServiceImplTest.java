/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.core.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.authentication.AuthenticationConstants;
import org.bonitasoft.engine.authentication.AuthenticationException;
import org.bonitasoft.engine.authentication.GenericAuthenticationService;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SecuredLoginServiceImplTest {

    private static final String TECH_USER_NAME = "install";
    private static final String TECH_USER_PASS = "install";
    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = (long) -1;
    private SecuredLoginServiceImpl securedLoginServiceImpl;
    @Mock
    private GenericAuthenticationService genericAuthenticationService;
    @Mock
    private SessionService sessionService;
    @Mock
    private SessionAccessor sessionAccessor;
    @Mock
    private IdentityService identityService;
    @Mock
    private TechnicalLoggerService logger;

    @Before
    public void setUp() throws IOException, BonitaHomeNotSetException, SLoginException {
        securedLoginServiceImpl = spy(new SecuredLoginServiceImpl(genericAuthenticationService, sessionService,
                sessionAccessor, identityService, logger));
        doReturn(new TechnicalUser(TECH_USER_NAME, TECH_USER_PASS)).when(securedLoginServiceImpl).getTechnicalUser(1L);
    }

    @Test
    public void testSecuredLoginServiceWithNullCredentials() throws SUserNotFoundException {
        try {
            securedLoginServiceImpl.login(null);
            fail();
        } catch (final SLoginException e) {
            assertThat(e.getMessage()).isEqualToIgnoringCase("invalid credentials, map is null");
        }
    }

    @Test
    public void testSecuredLoginServiceWithNullLogin() throws SUserNotFoundException {
        try {
            final Map<String, Serializable> credentials = new HashMap<>();
            credentials.put(AuthenticationConstants.BASIC_TENANT_ID, TENANT_ID);
            securedLoginServiceImpl.login(credentials);
            fail();
        } catch (final SLoginException e) {
            assertThat(e.getMessage()).isEqualToIgnoringCase("User name or password is not valid!");
        }
    }

    @Test
    public void testSecuredLoginServiceWithWrongCredentials() {
        try {
            final Map<String, Serializable> credentials = new HashMap<>();
            final String login = "login";
            final String password = "password";
            credentials.put(AuthenticationConstants.BASIC_TENANT_ID, TENANT_ID);
            credentials.put(AuthenticationConstants.BASIC_USERNAME, login);
            credentials.put(AuthenticationConstants.BASIC_PASSWORD, password);
            securedLoginServiceImpl.login(credentials);
            fail();
        } catch (final Exception e) {
            assertThat(e.getMessage()).isEqualToIgnoringCase("User name or password is not valid!");
        }
    }

    @Test(expected = SUserNotFoundException.class)
    public void testSecuredLoginServiceWithUnknownUserThrowSUserNotFoundException() throws Exception {
        final Map<String, Serializable> credentials = new HashMap<>();
        final String login = "login";
        final String password = "password";
        credentials.put(AuthenticationConstants.BASIC_TENANT_ID, TENANT_ID);
        credentials.put(AuthenticationConstants.BASIC_USERNAME, login);
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, password);

        when(genericAuthenticationService.checkUserCredentials(credentials)).thenReturn(login);
        when(identityService.getUserByUserName(login)).thenThrow(new SUserNotFoundException(login));

        securedLoginServiceImpl.login(credentials);
    }

    @Test
    public void testSecuredLoginServiceWithInvalidPlatformCredentials() throws Exception {
        final Map<String, Serializable> credentials = new HashMap<>();
        final String password = "poutpout";
        credentials.put(AuthenticationConstants.BASIC_TENANT_ID, TENANT_ID);
        credentials.put(AuthenticationConstants.BASIC_USERNAME, TECH_USER_NAME);
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, password);
        try {
            securedLoginServiceImpl.login(credentials);
            fail();
        } catch (final Exception e) {
            assertThat(e.getMessage()).isEqualToIgnoringCase("User name or password is not valid!");
        }
    }

    @Test
    public void testSecuredLoginServiceWithInvalidPlatformCredentialsWithGenericAuthenticationService() throws Exception {
        final Map<String, Serializable> credentials = new HashMap<>();
        final Long tenantId = new Long(1);
        final Long userId = new Long(-1);
        final String login = "julien";
        final String password = "julien";
        credentials.put(AuthenticationConstants.BASIC_TENANT_ID, tenantId);
        credentials.put(AuthenticationConstants.BASIC_USERNAME, login);
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, password);
        when(genericAuthenticationService.checkUserCredentials(anyMapOf(String.class, Serializable.class))).thenThrow(new AuthenticationException());

        try {
            securedLoginServiceImpl.login(credentials);
        } catch (final SLoginException e) {
            verify(genericAuthenticationService, times(1)).checkUserCredentials(anyMapOf(String.class, Serializable.class));
            verify(sessionAccessor, times(1)).deleteSessionId();
            verify(sessionService, times(0)).createSession(tenantId, userId, login, true);
            assertThat(e).hasRootCauseExactlyInstanceOf(AuthenticationException.class);
            return;
        }
        fail();

    }

    @Test
    public void testSecuredLoginServiceWithPlatformCredentialsWithGenericAuthenticationService() throws Exception {
        final Map<String, Serializable> credentials = new HashMap<>();
        final Long tenantId = new Long(1);
        final Long userId = new Long(-1);
        final String login = TECH_USER_NAME;
        final String password = TECH_USER_PASS;
        credentials.put(AuthenticationConstants.BASIC_TENANT_ID, tenantId);
        credentials.put(AuthenticationConstants.BASIC_USERNAME, login);
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, password);
        final SSession sSession = mock(SSession.class);
        when(sessionService.createSession(tenantId, userId, login, true)).thenReturn(sSession);

        final SSession sSessionResult = securedLoginServiceImpl.login(credentials);

        verify(genericAuthenticationService, times(0)).checkUserCredentials(anyMapOf(String.class, Serializable.class));
        verify(sessionAccessor, times(1)).deleteSessionId();
        verify(sessionService, times(1)).createSession(tenantId, userId, login, true);
        assertThat(sSessionResult).isSameAs(sSession);
    }

    @Test
    public void testSecuredLoginServiceWithPlatformCredentials() throws Exception {
        final Map<String, Serializable> credentials = new HashMap<>();
        credentials.put(AuthenticationConstants.BASIC_TENANT_ID, TENANT_ID);
        credentials.put(AuthenticationConstants.BASIC_USERNAME, TECH_USER_NAME);
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, TECH_USER_PASS);

        final SSession sSession = mock(SSession.class);
        when(sessionService.createSession(TENANT_ID, USER_ID, TECH_USER_NAME, true)).thenReturn(sSession);

        final SSession sSessionResult = securedLoginServiceImpl.login(credentials);

        verify(genericAuthenticationService, never()).checkUserCredentials(credentials);
        verify(securedLoginServiceImpl).getTechnicalUser(TENANT_ID);
        verify(sessionAccessor).deleteSessionId();
        verify(sessionService).createSession(TENANT_ID, USER_ID, TECH_USER_NAME, true);
        assertThat(sSessionResult).isSameAs(sSession);
    }

    @Test
    public void testSecuredLoginServiceWithStandardUserCredentials() throws Exception {
        final Map<String, Serializable> credentials = new HashMap<>();
        final Long tenantId = 1L;
        final Long userId = 112345L;
        final String login = "julien";
        final String password = "julien";
        credentials.put(AuthenticationConstants.BASIC_TENANT_ID, tenantId);
        credentials.put(AuthenticationConstants.BASIC_USERNAME, login);
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, password);

        final SSession sSession = mock(SSession.class);
        final SUser sUser = mock(SUser.class);

        when(sUser.getId()).thenReturn(userId);
        when(genericAuthenticationService.checkUserCredentials(credentials)).thenReturn(login);
        when(sessionService.createSession(tenantId, userId, login, false)).thenReturn(sSession);
        when(identityService.getUserByUserName(login)).thenReturn(sUser);

        final SSession sSessionResult = securedLoginServiceImpl.login(credentials);

        verify(genericAuthenticationService, times(1)).checkUserCredentials(credentials);
        verify(sessionAccessor, times(1)).deleteSessionId();
        verify(sessionService, times(1)).createSession(tenantId, userId, login, false);
        assertThat(sSessionResult).isSameAs(sSession);
    }

    @Test
    public void testRetrievePasswordFromCredentials() throws Exception {
        final Map<String, Serializable> credentials = new HashMap<>();
        final String password = "julien";
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, password);

        assertThat(securedLoginServiceImpl.retrievePasswordFromCredentials(credentials)).isEqualTo(password);
    }

    @Test
    public void testRetrievePasswordFromEmptyCredentials() {
        final Map<String, Serializable> credentials = new HashMap<>();
        try {
            securedLoginServiceImpl.retrievePasswordFromCredentials(credentials);
            fail();
        } catch (final Exception e) {
            assertThat(e.getMessage()).isEqualToIgnoringCase("invalid credentials, password is absent");
        }
    }

    @Test
    public void testRetrievePasswordFromNullPassword() {
        final Map<String, Serializable> credentials = new HashMap<>();
        final String password = null;
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, password);
        try {
            securedLoginServiceImpl.retrievePasswordFromCredentials(credentials);
            fail();
        } catch (final Exception e) {
            assertThat(e.getMessage()).isEqualToIgnoringCase("invalid credentials, password is absent");
        }
    }

    @Test
    public void testRetrievePasswordFromNullCredentials() {
        try {
            securedLoginServiceImpl.retrievePasswordFromCredentials(null);
            fail();
        } catch (final Exception e) {
            assertThat(e.getMessage()).isEqualToIgnoringCase("invalid credentials, password is absent");
        }
    }

    @Test
    public void testRetrieveUsernameFromCredentials() throws Exception {
        final Map<String, Serializable> credentials = new HashMap<>();
        final String username = "julien";
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, username);

        assertThat(securedLoginServiceImpl.retrievePasswordFromCredentials(credentials)).isEqualTo(username);
    }

    @Test
    public void testRetrieveUserNameFromEmptyCredentials() {
        final Map<String, Serializable> credentials = new HashMap<>();
        try {
            securedLoginServiceImpl.retrieveUsernameFromCredentials(credentials);
            fail();
        } catch (final Exception e) {
            assertThat(e.getMessage()).isEqualToIgnoringCase("invalid credentials, username is blank");
        }
    }

    @Test
    public void testRetrieveUserNameFromBlankPassword() {
        final Map<String, Serializable> credentials = new HashMap<>();
        final String username = "   ";
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, username);
        try {
            securedLoginServiceImpl.retrieveUsernameFromCredentials(credentials);
            fail();
        } catch (final Exception e) {
            assertThat(e.getMessage()).isEqualToIgnoringCase("invalid credentials, username is blank");
        }
    }

    @Test
    public void testRetrieveUserNameFromNullPassword() {
        final Map<String, Serializable> credentials = new HashMap<>();
        final String username = null;
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, username);
        try {
            securedLoginServiceImpl.retrieveUsernameFromCredentials(credentials);
            fail();
        } catch (final Exception e) {
            assertThat(e.getMessage()).isEqualToIgnoringCase("invalid credentials, username is blank");
        }
    }

    @Test
    public void testRetrieveUserNameFromNullCredentials() {
        try {
            securedLoginServiceImpl.retrieveUsernameFromCredentials(null);
            fail();
        } catch (final Exception e) {
            assertThat(e.getMessage()).isEqualToIgnoringCase("invalid credentials, username is blank");
        }
    }

    @Test
    public void should_login_with_technical_user__return_technical_session() throws Exception {
        SecuredLoginServiceImpl spy = spy(securedLoginServiceImpl);
        final Long tenantId = new Long(1);
        final String username = "john";
        final String password = "bpm";
        doReturn(new TechnicalUser(username, password)).when(spy).getTechnicalUser(1);
        final Map<String, Serializable> credentials = new HashMap<>();
        credentials.put(AuthenticationConstants.BASIC_TENANT_ID, tenantId);
        credentials.put(AuthenticationConstants.BASIC_USERNAME, username);
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, password);

        spy.login(credentials);

        verify(sessionService).createSession(1, -1, "john", true);
    }

}
