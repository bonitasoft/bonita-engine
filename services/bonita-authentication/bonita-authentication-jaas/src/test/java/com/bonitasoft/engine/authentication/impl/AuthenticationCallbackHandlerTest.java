package com.bonitasoft.engine.authentication.impl;

import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;

import org.bonitasoft.engine.authentication.AuthenticationConstants;
import org.junit.Test;


public class AuthenticationCallbackHandlerTest {

    AuthenticationCallbackHandler authenticationCallbackHandler;

    @Test(expected = IllegalArgumentException.class)
    public void testAuthenticationCallbackHandler() throws Exception {
        new AuthenticationCallbackHandler(null);
    }

    @Test
    public void testHandlePassword() throws Exception {
        String password = "password";
        Map<String, Serializable> credentials = new HashMap<String, Serializable>();
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, password);
        authenticationCallbackHandler = new AuthenticationCallbackHandler(credentials);
        PasswordCallback pc = mock(PasswordCallback.class);

        authenticationCallbackHandler.handlePassword(pc);
        verify(pc, times(1)).setPassword(password.toCharArray());
        
    }

    @Test
    public void testHandleUsername() throws Exception {
        String username = "username";
        Map<String, Serializable> credentials = new HashMap<String, Serializable>();
        credentials.put(AuthenticationConstants.BASIC_USERNAME, username);
        authenticationCallbackHandler = new AuthenticationCallbackHandler(credentials);
        NameCallback pc = mock(NameCallback.class);

        authenticationCallbackHandler.handleName(pc);
        verify(pc, times(1)).setName(username);

    }

}
