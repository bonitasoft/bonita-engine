package org.bonitasoft.engine.core.login;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.authentication.AuthenticationConstants;
import org.bonitasoft.engine.authentication.AuthenticationService;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class SecuredLoginServiceImplTest {

    SecuredLoginServiceImpl securedLoginServiceImpl;

    AuthenticationService authenticationService;

    SessionService sessionService;

    SessionAccessor sessionAccessor;

    IdentityService identityService;

    String formerBonitaHome = null;

    @Before
    public void setUp() {
        formerBonitaHome = System.getProperty("bonita.home");
        System.setProperty("bonita.home", "src/test/resources/bonita");
        authenticationService = mock(AuthenticationService.class);
        sessionService = mock(SessionService.class);
        sessionAccessor = mock(SessionAccessor.class);
        identityService = mock(IdentityService.class);
        this.securedLoginServiceImpl = new SecuredLoginServiceImpl(authenticationService, sessionService, sessionAccessor, identityService);
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
    public void testSecuredLoginServiceWithNullCredentials() throws Exception {
        try {
            this.securedLoginServiceImpl.login(null);
            fail();
        } catch (SLoginException e) {
            assertThat(e.getMessage()).isEqualToIgnoringCase("invalid credentials, map is null");
        }
    }

    @Test
    public void testSecuredLoginServiceWithNullLogin() throws Exception {
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
    public void testSecuredLoginServiceWithWrongCredentials() throws Exception {
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
    public void testSecuredLoginServiceWithPlatformCredentials() throws Exception {
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

        verify(authenticationService, times(1)).checkUserCredentials(credentials);
        verify(sessionAccessor, times(1)).deleteSessionId();
        verify(sessionService, times(1)).createSession(tenantId, userId, login, true);
        assertThat(sSessionResult).isSameAs(sSession);
    }

    @Test
    public void testSecuredLoginServiceWithStandardUserCredentials() throws Exception {
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
        when(authenticationService.checkUserCredentials(credentials)).thenReturn(true);
        when(sessionService.createSession(tenantId, userId, login, false)).thenReturn(sSession);
        when(identityService.getUserByUserName(login)).thenReturn(sUser);

        SSession sSessionResult = this.securedLoginServiceImpl.login(credentials);

        verify(authenticationService, times(1)).checkUserCredentials(credentials);
        verify(sessionAccessor, times(1)).deleteSessionId();
        verify(sessionService, times(1)).createSession(tenantId, userId, login, false);
        assertThat(sSessionResult).isSameAs(sSession);
    }

}
