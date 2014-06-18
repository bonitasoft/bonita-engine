package com.bonitasoft.engine.authentication.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.bonitasoft.engine.authentication.AuthenticationConstants;
import org.bonitasoft.engine.authentication.AuthenticationException;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JAASGenericAuthenticationServiceImplTest {

    JAASGenericAuthenticationServiceImpl jaasGenericAuthenticationServiceImpl;

    private static final String JAVA_SECURITY_AUTH_LOGIN_CONFIG = "java.security.auth.login.config";

    @Mock
    AuthenticationCallbackHandler authenticationCallbackHandler;

    @Mock
    TechnicalLoggerService logger;

    @Mock
    ReadSessionAccessor sessionAccessor;

    @Before
    public void setup() {
        jaasGenericAuthenticationServiceImpl = new JAASGenericAuthenticationServiceImpl(logger, sessionAccessor);
    }

    @BeforeClass
    public static void classSetUp() {
        System.setProperty(JAVA_SECURITY_AUTH_LOGIN_CONFIG, "src/test/resources/jaas-test.cfg");
    }

    @AfterClass
    public static void classTearDown() {
        System.clearProperty(JAVA_SECURITY_AUTH_LOGIN_CONFIG);
    }

    @Test(expected = AuthenticationException.class)
    public void testExtractUserFromSubjetWithNoCallPrincipal() throws AuthenticationException {
        final LoginContext lc = mock(LoginContext.class);
        final Subject subject = new Subject();
        when(lc.getSubject()).thenReturn(subject);
        final Set<Principal> principals = subject.getPrincipals();
        final Principal principalUnknown = mock(Principal.class);
        final Principal principalCaller = mock(Principal.class);

        when(principalCaller.getName()).thenReturn(JAASGenericAuthenticationServiceImpl.CALLER_PRINCIPAL);

        principals.add(principalUnknown);
        principals.add(principalCaller);

        jaasGenericAuthenticationServiceImpl.extractUserFromSubjet(lc);
    }

    @Test
    public void testExtractUserFromSubject() throws AuthenticationException {

        final String username = "install";
        final LoginContext lc = mock(LoginContext.class);
        final Subject subject = new Subject();

        when(lc.getSubject()).thenReturn(subject);

        final Set<Principal> principals = subject.getPrincipals();

        final Principal principalUnknown = mock(Principal.class);
        final Group principalCaller = mock(Group.class);
        final Principal principalUser = mock(Principal.class);
        when(principalUser.getName()).thenReturn(username);
        final Enumeration enumeration = mock(Enumeration.class);

        when(enumeration.hasMoreElements()).thenReturn(true, false, false, false);
        when(enumeration.nextElement()).thenReturn(principalUser);

        when(principalCaller.getName()).thenReturn(JAASGenericAuthenticationServiceImpl.CALLER_PRINCIPAL);
        when(principalCaller.members()).thenReturn(enumeration);

        principals.add(principalUnknown);
        principals.add(principalCaller);

        final String result = jaasGenericAuthenticationServiceImpl.extractUserFromSubjet(lc);
        verify(principalCaller, times(1)).getName();
        assertThat(result).isSameAs(username);
    }

    @Test
    public void testIsGroupPrincipalWithPrincipal() {
        assertThat(jaasGenericAuthenticationServiceImpl.isGroupPrincipal(mock(Principal.class))).isFalse();
    }

    @Test
    public void testIsGroupPrincipalWithGroup() {
        assertThat(jaasGenericAuthenticationServiceImpl.isGroupPrincipal(mock(Group.class))).isTrue();
    }

    @Test
    public void testCreateContextWithNullTenantId() {
        try {
            jaasGenericAuthenticationServiceImpl.createContext(authenticationCallbackHandler);
        } catch (final AuthenticationException e) {
            assertThat(e).hasCauseExactlyInstanceOf(LoginException.class);
            return;
        }
        fail("Context contains a null tenant identifier");
    }

    @Test
    public void testCreateContext() throws Exception {
        when(sessionAccessor.getTenantId()).thenReturn(1L);
        final LoginContext lc = jaasGenericAuthenticationServiceImpl.createContext(authenticationCallbackHandler);
        assertThat(lc).isNotNull();
    }

    @Test
    public void testLogin() throws Exception {
        final LoginContext loginContext = mock(LoginContext.class);
        jaasGenericAuthenticationServiceImpl.login(loginContext);
        verify(loginContext, times(1)).login();
    }

    @Test
    public void testTryToAuthenticate() {
        final Map<String, Serializable> credentials = new HashMap<String, Serializable>();
        jaasGenericAuthenticationServiceImpl.tryToAuthenticate(credentials);
    }

    @Test
    public void canLoginWithCorrectUsernamePassword() throws Exception {
        final String userName = "admin";
        final Map<String, Serializable> credentials = new HashMap<String, Serializable>();
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, "bpm");
        credentials.put(AuthenticationConstants.BASIC_USERNAME, userName);
        when(sessionAccessor.getTenantId()).thenReturn(1L);

        assertThat(jaasGenericAuthenticationServiceImpl.checkUserCredentials(credentials)).isEqualTo(userName);
    }

}
