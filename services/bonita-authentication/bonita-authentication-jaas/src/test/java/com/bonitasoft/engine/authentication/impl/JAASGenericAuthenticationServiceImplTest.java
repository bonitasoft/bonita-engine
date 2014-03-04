package com.bonitasoft.engine.authentication.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.junit.Before;
import org.junit.Test;


public class JAASGenericAuthenticationServiceImplTest {

    JAASGenericAuthenticationServiceImpl jaasGenericAuthenticationServiceImpl;

    @Before
    public void setup() {

        jaasGenericAuthenticationServiceImpl = new JAASGenericAuthenticationServiceImpl(mock(TechnicalLoggerService.class), mock(ReadSessionAccessor.class));

    }

    @Test
    public void testExtractUserFromSubjetWithNoCallPrincipal() throws Exception {
        LoginContext lc = mock(LoginContext.class);
        Subject subject = new Subject();
        when(lc.getSubject()).thenReturn(subject);
        Set<Principal> principals = subject.getPrincipals();
        Principal principalUnknown = mock(Principal.class);
        Principal principalCaller = mock(Principal.class);

        when(principalCaller.getName()).thenReturn(JAASGenericAuthenticationServiceImpl.CALLER_PRINCIPAL);

        principals.add(principalUnknown);
        principals.add(principalCaller);

        String result = jaasGenericAuthenticationServiceImpl.extractUserFromSubjet(lc);
        assertThat(result).isNull();
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testExtractUserFromSubjet() throws Exception {
        // jaasGenericAuthenticationServiceImpl = spy(jaasGenericAuthenticationServiceImpl);
        String username = "install";
        LoginContext lc = mock(LoginContext.class);
        Subject subject = new Subject();

        when(lc.getSubject()).thenReturn(subject);

        Set<Principal> principals = subject.getPrincipals();

        Principal principalUnknown = mock(Principal.class);
        Group principalCaller = mock(Group.class);
        Principal principalUser = mock(Principal.class);
        when(principalUser.getName()).thenReturn(username);
        // when(principalUser.getClass()).thenAnswer(new Answer<Principal>() {
        //
        // Group answer(InvocationOnMock invocation) {
        // Object[] args = invocation.getArguments();
        // Object mock = invocation.getMock();
        // return Group.class;
        // }
        // });
        Enumeration enumeration = mock(Enumeration.class);
        when(enumeration.hasMoreElements()).thenReturn(true, false, false, false);
        when(enumeration.nextElement()).thenReturn(principalUser);


        when(principalCaller.getName()).thenReturn(JAASGenericAuthenticationServiceImpl.CALLER_PRINCIPAL);
        when(principalCaller.members()).thenReturn(enumeration);

        principals.add(principalUnknown);
        principals.add(principalCaller);

        // when(jaasGenericAuthenticationServiceImpl.isGroupPrincipal(principalCaller)).thenReturn(true);

        String result = jaasGenericAuthenticationServiceImpl.extractUserFromSubjet(lc);
        verify(principalCaller, times(1)).getName();
        assertThat(result).isSameAs(username);
    }

    @Test
    public void testIsGroupPrincipal() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

}
