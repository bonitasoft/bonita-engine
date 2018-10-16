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

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bonitasoft.engine.authentication.AuthenticationConstants;
import org.bonitasoft.engine.authentication.AuthenticationException;
import org.bonitasoft.engine.authentication.GenericAuthenticationService;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.impl.SUserImpl;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.model.impl.SProfileImpl;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
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
    private IdentityService identityService;
    @Mock
    private TechnicalLoggerService logger;
    @Mock
    private ProfileService profileService;

    @Before
    public void setUp() throws Exception {
        securedLoginServiceImpl = new SecuredLoginServiceImpl(genericAuthenticationService, sessionService,
                identityService, logger, new TechnicalUser(TECH_USER_NAME, TECH_USER_PASS), profileService);
        //return a session with given arguments
        when(sessionService.createSession(anyLong(), anyLong(), anyString(), anyBoolean(), anyList()))
                .thenAnswer(invok -> SSession.builder()
                        .id(UUID.randomUUID().getLeastSignificantBits())
                        .applicationName("myApp")
                        .tenantId(invok.getArgument(0))
                        .userId(invok.getArgument(1))
                        .userName(invok.getArgument(2))
                        .technicalUser(invok.getArgument(3))
                        .profiles(invok.getArgument(4))
                        .build());
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
    public void testSecuredLoginServiceWithInvalidPlatformCredentials() {
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
        final Long tenantId = 1L;
        final Long userId = -1L;
        final String login = "julien";
        final String password = "julien";
        credentials.put(AuthenticationConstants.BASIC_TENANT_ID, tenantId);
        credentials.put(AuthenticationConstants.BASIC_USERNAME, login);
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, password);
        when(genericAuthenticationService.checkUserCredentials(anyMap())).thenThrow(new AuthenticationException());

        try {
            securedLoginServiceImpl.login(credentials);
        } catch (final SLoginException e) {
            verify(genericAuthenticationService, times(1)).checkUserCredentials(anyMap());
            verify(sessionService, times(0)).createSession(tenantId, userId, login, true);
            assertThat(e).hasRootCauseExactlyInstanceOf(AuthenticationException.class);
            return;
        }
        fail();

    }

    @Test
    public void testSecuredLoginServiceWithPlatformCredentialsWithGenericAuthenticationService() throws Exception {
        final Map<String, Serializable> credentials = credentials(TECH_USER_NAME, TECH_USER_PASS, TENANT_ID);
        final SSession sSession = mock(SSession.class);
        when(sessionService.createSession(TENANT_ID, -1L, TECH_USER_NAME, true, emptyList())).thenReturn(sSession);

        final SSession sSessionResult = securedLoginServiceImpl.login(credentials);

        verify(genericAuthenticationService, times(0)).checkUserCredentials(anyMap());
        verify(sessionService, times(1)).createSession(1L, -1L, TECH_USER_NAME, true, emptyList());
        assertThat(sSessionResult).isSameAs(sSession);
    }

    @Test
    public void testSecuredLoginServiceWithPlatformCredentials() throws Exception {
        final Map<String, Serializable> credentials = credentials(TECH_USER_NAME, TECH_USER_PASS, TENANT_ID);

        final SSession sSession = mock(SSession.class);
        when(sessionService.createSession(TENANT_ID, USER_ID, TECH_USER_NAME, true, emptyList())).thenReturn(sSession);

        final SSession sSessionResult = securedLoginServiceImpl.login(credentials);

        verify(genericAuthenticationService, never()).checkUserCredentials(credentials);
        verify(sessionService).createSession(TENANT_ID, USER_ID, TECH_USER_NAME, true, emptyList());
        assertThat(sSessionResult).isSameAs(sSession);
    }

    @Test
    public void testSecuredLoginServiceWithStandardUserCredentials() throws Exception {
        final Map<String, Serializable> credentials = credentials("julien", "julien", TENANT_ID);

        final SSession sSession = mock(SSession.class);
        final SUser sUser = mock(SUser.class);
        doReturn(true).when(sUser).isEnabled();

        when(sUser.getId()).thenReturn(112345L);
        when(genericAuthenticationService.checkUserCredentials(credentials)).thenReturn("julien");
        when(sessionService.createSession(TENANT_ID, 112345L, "julien", false, emptyList())).thenReturn(sSession);
        when(identityService.getUserByUserName("julien")).thenReturn(sUser);

        final SSession sSessionResult = securedLoginServiceImpl.login(credentials);

        verify(genericAuthenticationService, times(1)).checkUserCredentials(credentials);
        verify(sessionService, times(1)).createSession(TENANT_ID, 112345L, "julien", false, emptyList());
        assertThat(sSessionResult).isSameAs(sSession);
    }

    @Test
    public void should_fail_if_no_password_is_provided() {
        final Map<String, Serializable> credentials = new HashMap<>();
        final String password = null;
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, password);
        try {
            securedLoginServiceImpl.login(credentials);
            fail();
        } catch (final Exception e) {
            assertThat(e.getMessage()).isEqualToIgnoringCase("User name or password is not valid!");
        }
    }


    @Test
    public void should_fail_if_credentials_are_empty() {
        final Map<String, Serializable> credentials = new HashMap<>();
        try {
            securedLoginServiceImpl.login(credentials);
            fail();
        } catch (final Exception e) {
            assertThat(e.getMessage()).isEqualToIgnoringCase("User name or password is not valid!");
        }
    }

    @Test
    public void should_fail_if_username_is_blank() throws Exception {
        havingUser("   ", "password", 1L);
        try {
            securedLoginServiceImpl.login(credentials("   ", "password", 1L));
            fail();
        } catch (final Exception e) {
            assertThat(e.getMessage()).isEqualToIgnoringCase("User name or password is not valid!");
        }
    }

    @Test
    public void should_fail_if_password_does_not_match() throws Exception {
        havingUser("a", "password1", 1L);
        try {
            securedLoginServiceImpl.login(credentials("a", "password2", 1L));
            fail();
        } catch (final Exception e) {
            assertThat(e.getMessage()).isEqualToIgnoringCase("User name or password is not valid!");
        }
    }


    @Test
    public void should_fail_when_given_null_credentials() {
        try {
            securedLoginServiceImpl.login(null);
            fail();
        } catch (final Exception e) {
            assertThat(e.getMessage()).isEqualToIgnoringCase("invalid credentials, map is null");
        }
    }


    @Test
    public void should_login_with_technical_user() throws Exception {
        SSession session = securedLoginServiceImpl.login(credentials(TECH_USER_NAME, TECH_USER_PASS, 1L));

        assertThat(session).hasFieldOrPropertyWithValue("tenantId", 1L)
                .hasFieldOrPropertyWithValue("userName", TECH_USER_NAME)
                .hasFieldOrPropertyWithValue("userId", -1L)
                .hasFieldOrPropertyWithValue("technicalUser", true);
    }

    @Test
    public void should_login_with_existing_user() throws Exception {
        SUserImpl user = havingUser("john", "bpm", 42L);

        SSession session = securedLoginServiceImpl.login(credentials("john", "bpm", 42));

        assertThat(session).hasFieldOrPropertyWithValue("tenantId", 42L)
                .hasFieldOrPropertyWithValue("userName", "john")
                .hasFieldOrPropertyWithValue("userId", user.getId())
                .hasFieldOrPropertyWithValue("technicalUser", false);
    }

    @Test
    public void should_fail_if_user_is_disabled() throws Exception {
        SUserImpl user = havingUser("john", "bpm", 42L);
        user.setEnabled(false);
        try {
            securedLoginServiceImpl.login(credentials("john", "bpm", 42L));
            fail();
        } catch (SLoginException e) {
            assertThat(e.getMessage()).isEqualToIgnoringCase("Unable to login : the user is disable.");
        }
    }

    @Test
    public void should_update_last_connection_date_when_successfully_connected() throws Exception {
        SUserImpl user = havingUser("john", "bpm", 42L);

        securedLoginServiceImpl.login(credentials("john", "bpm", 42));

        verify(identityService).updateUser(eq(user), argThat(e -> e.getFields().keySet().equals(Collections.singleton("lastConnection"))));
    }

    @Test
    public void should_have_profiles_in_session() throws Exception {
        SUserImpl user = havingUser("myUser", "myPass", 43L);
        doReturn(profiles("User", "Administrator")).when(profileService).getProfilesOfUser(user.getId());

        SSession session = securedLoginServiceImpl.login(credentials("myUser", "myPass", 43L));

        assertThat(session.getProfiles()).containsExactlyInAnyOrder("User", "Administrator");
    }

    private List<SProfile> profiles(String... profiles) {
        return Arrays.stream(profiles)
                .map(this::profile)
                .collect(Collectors.toList());
    }

    private SProfile profile(String name) {
        SProfileImpl sProfile = new SProfileImpl();
        sProfile.setName(name);
        return sProfile;
    }

    private Map<String, Serializable> credentials(String username, String password, long tenantId) {
        final Map<String, Serializable> credentials = new HashMap<>();
        credentials.put(AuthenticationConstants.BASIC_TENANT_ID, tenantId);
        credentials.put(AuthenticationConstants.BASIC_USERNAME, username);
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, password);
        return credentials;
    }


    private SUserImpl havingUser(String username, String password, long tenantId) throws Exception {
        SUserImpl user = new SUserImpl();
        user.setId(UUID.randomUUID().getLeastSignificantBits());
        user.setUserName(username);
        user.setPassword(password);
        user.setEnabled(true);
        doReturn(user).when(identityService).getUserByUserName(username);


        doReturn(username).when(genericAuthenticationService).checkUserCredentials(eq(credentials(username, password, tenantId)));

        return user;
    }

}
