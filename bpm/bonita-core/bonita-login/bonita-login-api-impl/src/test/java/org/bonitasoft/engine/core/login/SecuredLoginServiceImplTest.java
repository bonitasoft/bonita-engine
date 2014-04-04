package org.bonitasoft.engine.core.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.authentication.AuthenticationConstants;
import org.bonitasoft.engine.authentication.AuthenticationException;
import org.bonitasoft.engine.authentication.AuthenticationService;
import org.bonitasoft.engine.authentication.GenericAuthenticationService;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SecuredLoginServiceImplTest {

    private SecuredLoginServiceImpl securedLoginServiceImpl;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private GenericAuthenticationService genericAuthenticationService;

    @Mock
    private SessionService sessionService;

    @Mock
    private SessionAccessor sessionAccessor;

    @Mock
    private IdentityService identityService;

    private String formerBonitaHome = null;

    @Before
    public void setUp() {
        formerBonitaHome = System.getProperty("bonita.home");
        System.setProperty("bonita.home", "src/test/resources/bonita");
    }

    @After
    public void tearDown() {
        if (formerBonitaHome != null) {
            System.setProperty("bonita.home", formerBonitaHome);
        } else {
            System.clearProperty("bonita.home");
        }
    }

    @Test
    public void testSecuredLoginServiceWithNullCredentials() {
        this.securedLoginServiceImpl = new SecuredLoginServiceImpl(authenticationService, sessionService, sessionAccessor, identityService);
        try {
            this.securedLoginServiceImpl.login(null);
            fail();
        } catch (SLoginException e) {
            assertThat(e.getMessage()).isEqualToIgnoringCase("invalid credentials, map is null");
        }
    }

    @Test
    public void testSecuredLoginServiceWithNullLogin() {
        this.securedLoginServiceImpl = new SecuredLoginServiceImpl(authenticationService, sessionService, sessionAccessor, identityService);
        try {
            Map<String, Serializable> credentials = new HashMap<String, Serializable>();
            Long tenantId = new Long(1);
            credentials.put(AuthenticationConstants.BASIC_TENANT_ID, tenantId);
            this.securedLoginServiceImpl.login(credentials);
            fail();
        } catch (SLoginException e) {
            assertThat(e.getMessage()).isEqualToIgnoringCase("invalid credentials, username is blank");
        }
    }

    @Test
    public void testSecuredLoginServiceWithWrongCredentials() {
        this.securedLoginServiceImpl = new SecuredLoginServiceImpl(authenticationService, sessionService, sessionAccessor, identityService);
        try {
            Map<String, Serializable> credentials = new HashMap<String, Serializable>();
            Long tenantId = new Long(1);
            String login = "login";
            String password = "password";
            credentials.put(AuthenticationConstants.BASIC_TENANT_ID, tenantId);
            credentials.put(AuthenticationConstants.BASIC_USERNAME, login);
            credentials.put(AuthenticationConstants.BASIC_PASSWORD, password);
            this.securedLoginServiceImpl.login(credentials);
            fail();
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualToIgnoringCase("User name or password is not valid!");
        }
    }

    @Test
    public void testSecuredLoginServiceWithInvalidPlatformCredentials() throws Exception {
        this.securedLoginServiceImpl = new SecuredLoginServiceImpl(genericAuthenticationService, sessionService, sessionAccessor, identityService);
        Map<String, Serializable> credentials = new HashMap<String, Serializable>();
        Long tenantId = new Long(1);
        Long userId = new Long(-1);
        String login = "install";
        String password = "poutpout";
        credentials.put(AuthenticationConstants.BASIC_TENANT_ID, tenantId);
        credentials.put(AuthenticationConstants.BASIC_USERNAME, login);
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, password);

        SSession sSession = mock(SSession.class);
        when(sessionService.createSession(tenantId, userId, login, true)).thenReturn(sSession);
        try {
            this.securedLoginServiceImpl.login(credentials);
            fail();
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualToIgnoringCase("User name or password is not valid!");
        }
    }

    @Test
    public void testSecuredLoginServiceWithInvalidPlatformCredentialsWithGenericAuthenticationService() throws Exception {
        this.securedLoginServiceImpl = new SecuredLoginServiceImpl(genericAuthenticationService, sessionService, sessionAccessor, identityService);
        Map<String, Serializable> credentials = new HashMap<String, Serializable>();
        Long tenantId = new Long(1);
        Long userId = new Long(-1);
        String login = "julien";
        String password = "julien";
        credentials.put(AuthenticationConstants.BASIC_TENANT_ID, tenantId);
        credentials.put(AuthenticationConstants.BASIC_USERNAME, login);
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, password);
        when(genericAuthenticationService.checkUserCredentials(any(Map.class))).thenThrow(new AuthenticationException());

        SSession sSession = mock(SSession.class);
        when(sessionService.createSession(tenantId, userId, login, true)).thenReturn(sSession);

        try {
            this.securedLoginServiceImpl.login(credentials);
        } catch (SLoginException e) {
            verify(genericAuthenticationService, times(1)).checkUserCredentials(any(Map.class));
            verify(sessionAccessor, times(1)).deleteSessionId();
            verify(sessionService, times(0)).createSession(tenantId, userId, login, true);
            assertThat(e).hasRootCauseExactlyInstanceOf(AuthenticationException.class);
            return;
        }
        fail();

    }

    @Test
    public void testSecuredLoginServiceWithPlatformCredentialsWithGenericAuthenticationService() throws Exception {
        this.securedLoginServiceImpl = new SecuredLoginServiceImpl(genericAuthenticationService, sessionService, sessionAccessor, identityService);
        Map<String, Serializable> credentials = new HashMap<String, Serializable>();
        Long tenantId = new Long(1);
        Long userId = new Long(-1);
        String login = "install";
        String password = "install";
        credentials.put(AuthenticationConstants.BASIC_TENANT_ID, tenantId);
        credentials.put(AuthenticationConstants.BASIC_USERNAME, login);
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, password);
        when(genericAuthenticationService.checkUserCredentials(any(Map.class))).thenThrow(new AuthenticationException());

        SSession sSession = mock(SSession.class);
        when(sessionService.createSession(tenantId, userId, login, true)).thenReturn(sSession);

        SSession sSessionResult = this.securedLoginServiceImpl.login(credentials);

        verify(genericAuthenticationService, times(0)).checkUserCredentials(any(Map.class));
        verify(sessionAccessor, times(1)).deleteSessionId();
        verify(sessionService, times(1)).createSession(tenantId, userId, login, true);
        assertThat(sSessionResult).isSameAs(sSession);
    }

    @Test
    public void testSecuredLoginServiceWithPlatformCredentials() throws Exception {
        this.securedLoginServiceImpl = new SecuredLoginServiceImpl(authenticationService, sessionService, sessionAccessor, identityService);
        Map<String, Serializable> credentials = new HashMap<String, Serializable>();
        Long tenantId = new Long(1);
        Long userId = new Long(-1);
        String login = "install";
        String password = "install";
        credentials.put(AuthenticationConstants.BASIC_TENANT_ID, tenantId);
        credentials.put(AuthenticationConstants.BASIC_USERNAME, login);
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, password);

        SSession sSession = mock(SSession.class);
        when(sessionService.createSession(tenantId, userId, login, true)).thenReturn(sSession);

        SSession sSessionResult = this.securedLoginServiceImpl.login(credentials);

        verify(authenticationService, times(0)).checkUserCredentials(login, password);
        verify(sessionAccessor, times(1)).deleteSessionId();
        verify(sessionService, times(1)).createSession(tenantId, userId, login, true);
        assertThat(sSessionResult).isSameAs(sSession);
    }

    @Test
    public void testSecuredLoginServiceWithStandardUserCredentials() throws Exception {
        this.securedLoginServiceImpl = new SecuredLoginServiceImpl(genericAuthenticationService, sessionService, sessionAccessor, identityService);
        Map<String, Serializable> credentials = new HashMap<String, Serializable>();
        Long tenantId = new Long(1);
        Long userId = new Long(112345);
        String login = "julien";
        String password = "julien";
        credentials.put(AuthenticationConstants.BASIC_TENANT_ID, tenantId);
        credentials.put(AuthenticationConstants.BASIC_USERNAME, login);
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, password);

        SSession sSession = mock(SSession.class);
        SUser sUser = mock(SUser.class);

        when(sUser.getId()).thenReturn(userId);
        when(genericAuthenticationService.checkUserCredentials(credentials)).thenReturn(login);
        when(sessionService.createSession(tenantId, userId, login, false)).thenReturn(sSession);
        when(identityService.getUserByUserName(login)).thenReturn(sUser);

        SSession sSessionResult = this.securedLoginServiceImpl.login(credentials);

        verify(genericAuthenticationService, times(1)).checkUserCredentials(credentials);
        verify(sessionAccessor, times(1)).deleteSessionId();
        verify(sessionService, times(1)).createSession(tenantId, userId, login, false);
        assertThat(sSessionResult).isSameAs(sSession);
    }

    @Test
    public void testRetrievePasswordFromCredentials() throws Exception {
        this.securedLoginServiceImpl = new SecuredLoginServiceImpl(genericAuthenticationService, sessionService, sessionAccessor, identityService);
        Map<String, Serializable> credentials = new HashMap<String, Serializable>();
        String password = "julien";
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, password);

        assertThat(this.securedLoginServiceImpl.retrievePasswordFromCredentials(credentials)).isEqualTo(password);
    }

    @Test
    public void testRetrievePasswordFromEmptyCredentials() {
        this.securedLoginServiceImpl = new SecuredLoginServiceImpl(genericAuthenticationService, sessionService, sessionAccessor, identityService);
        Map<String, Serializable> credentials = new HashMap<String, Serializable>();
        try {
            this.securedLoginServiceImpl.retrievePasswordFromCredentials(credentials);
            fail();
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualToIgnoringCase("invalid credentials, password is absent");
        }
    }

    @Test
    public void testRetrievePasswordFromNullPassword() {
        this.securedLoginServiceImpl = new SecuredLoginServiceImpl(genericAuthenticationService, sessionService, sessionAccessor, identityService);
        Map<String, Serializable> credentials = new HashMap<String, Serializable>();
        String password = null;
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, password);
        try {
            this.securedLoginServiceImpl.retrievePasswordFromCredentials(credentials);
            fail();
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualToIgnoringCase("invalid credentials, password is absent");
        }
    }

    @Test
    public void testRetrievePasswordFromNullCredentials() {
        this.securedLoginServiceImpl = new SecuredLoginServiceImpl(genericAuthenticationService, sessionService, sessionAccessor, identityService);
        try {
            this.securedLoginServiceImpl.retrievePasswordFromCredentials(null);
            fail();
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualToIgnoringCase("invalid credentials, password is absent");
        }
    }

    @Test
    public void testRetrieveUsernameFromCredentials() throws Exception {
        this.securedLoginServiceImpl = new SecuredLoginServiceImpl(genericAuthenticationService, sessionService, sessionAccessor, identityService);
        Map<String, Serializable> credentials = new HashMap<String, Serializable>();
        String username = "julien";
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, username);

        assertThat(this.securedLoginServiceImpl.retrievePasswordFromCredentials(credentials)).isEqualTo(username);
    }

    @Test
    public void testRetrieveUserNameFromEmptyCredentials() {
        this.securedLoginServiceImpl = new SecuredLoginServiceImpl(genericAuthenticationService, sessionService, sessionAccessor, identityService);
        Map<String, Serializable> credentials = new HashMap<String, Serializable>();
        try {
            this.securedLoginServiceImpl.retrieveUsernameFromCredentials(credentials);
            fail();
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualToIgnoringCase("invalid credentials, username is blank");
        }
    }

    @Test
    public void testRetrieveUserNameFromBlankPassword() {
        this.securedLoginServiceImpl = new SecuredLoginServiceImpl(genericAuthenticationService, sessionService, sessionAccessor, identityService);
        Map<String, Serializable> credentials = new HashMap<String, Serializable>();
        String username = "   ";
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, username);
        try {
            this.securedLoginServiceImpl.retrieveUsernameFromCredentials(credentials);
            fail();
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualToIgnoringCase("invalid credentials, username is blank");
        }
    }

    @Test
    public void testRetrieveUserNameFromNullPassword() {
        this.securedLoginServiceImpl = new SecuredLoginServiceImpl(genericAuthenticationService, sessionService, sessionAccessor, identityService);
        Map<String, Serializable> credentials = new HashMap<String, Serializable>();
        String username = null;
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, username);
        try {
            this.securedLoginServiceImpl.retrieveUsernameFromCredentials(credentials);
            fail();
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualToIgnoringCase("invalid credentials, username is blank");
        }
    }

    @Test
    public void testRetrieveUserNameFromNullCredentials() {
        this.securedLoginServiceImpl = new SecuredLoginServiceImpl(genericAuthenticationService, sessionService, sessionAccessor, identityService);
        try {
            this.securedLoginServiceImpl.retrieveUsernameFromCredentials(null);
            fail();
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualToIgnoringCase("invalid credentials, username is blank");
        }
    }

    @Test
    public void testLoginChoosingGenericAuthenticationService() throws Exception {
        this.securedLoginServiceImpl = new SecuredLoginServiceImpl(genericAuthenticationService, sessionService, sessionAccessor, identityService);
        Map<String, Serializable> credentials = new HashMap<String, Serializable>();

        String login = "julien";
        String password = "julien";
        credentials.put(AuthenticationConstants.BASIC_USERNAME, login);
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, password);
        when(genericAuthenticationService.checkUserCredentials(credentials)).thenReturn(login);
        String result = this.securedLoginServiceImpl.loginChoosingAppropriateAuthenticationService(credentials);
        verify(genericAuthenticationService, times(1)).checkUserCredentials(credentials);
        assertThat(result).isSameAs(login);
    }

    @Test
    public void testLoginChoosingAuthenticationService() throws Exception {
        this.securedLoginServiceImpl = new SecuredLoginServiceImpl(authenticationService, sessionService, sessionAccessor, identityService);
        Map<String, Serializable> credentials = new HashMap<String, Serializable>();

        String login = "julien";
        String password = "julien";
        credentials.put(AuthenticationConstants.BASIC_USERNAME, login);
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, password);
        when(authenticationService.checkUserCredentials(login, password)).thenReturn(true);
        String result = this.securedLoginServiceImpl.loginChoosingAppropriateAuthenticationService(credentials);
        verify(authenticationService, times(1)).checkUserCredentials(login, password);
        assertThat(result).isSameAs(login);
    }

    @Test
    public void testLoginChoosingAuthenticationServiceFails() throws Exception {
        this.securedLoginServiceImpl = new SecuredLoginServiceImpl(authenticationService, sessionService, sessionAccessor, identityService);
        Map<String, Serializable> credentials = new HashMap<String, Serializable>();

        String login = "julien";
        String password = "julien";
        credentials.put(AuthenticationConstants.BASIC_USERNAME, login);
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, password);
        when(authenticationService.checkUserCredentials(login, password)).thenReturn(false);
        String result = this.securedLoginServiceImpl.loginChoosingAppropriateAuthenticationService(credentials);
        verify(authenticationService, times(1)).checkUserCredentials(login, password);
        assertThat(result).isNull();
    }

    @Test
    public void testLoginChoosingNullAuthenticationService() throws Exception {
        this.securedLoginServiceImpl = new SecuredLoginServiceImpl((AuthenticationService) null, sessionService, sessionAccessor, identityService);
        try {
            this.securedLoginServiceImpl.loginChoosingAppropriateAuthenticationService(null);
        } catch (AuthenticationException e) {
            assertThat(e.getMessage()).isEqualTo("no implementation of authentication supplied");
            return;
        }
        fail();
    }

    @Test
    public void testLoginChoosingNullGenericAuthenticationService() throws Exception {
        this.securedLoginServiceImpl = new SecuredLoginServiceImpl((AuthenticationService) null, sessionService, sessionAccessor, identityService);
        try {
            this.securedLoginServiceImpl.loginChoosingAppropriateAuthenticationService(null);
        } catch (AuthenticationException e) {
            assertThat(e.getMessage()).isEqualTo("no implementation of authentication supplied");
            return;
        }
        fail();

    }

    @Test
    public void should_login_with_technical_user__return_technical_session() throws Exception {
        securedLoginServiceImpl = spy(new SecuredLoginServiceImpl(authenticationService, sessionService, sessionAccessor, identityService));
        doReturn(new TechnicalUser("john", "bpm")).when(securedLoginServiceImpl).getTechnicalUser(1);

        securedLoginServiceImpl.login(1, "john", "bpm");

        verify(sessionService).createSession(1, -1, "john", true);
    }

}
