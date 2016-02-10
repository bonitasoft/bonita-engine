/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.authentication.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.authentication.AuthenticationConstants;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class NoAuthenticationServiceImplTest {

    @Mock
    private TechnicalLoggerService logger;

    @Mock
    private IdentityService identityService;

    @InjectMocks
    private NoAuthenticationServiceImpl authenticationService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(logger.isLoggable(NoAuthenticationServiceImpl.class, TechnicalLogSeverity.TRACE)).thenReturn(true);
    }

    @Test
    public void authOKWithAnExistingUser() throws Exception {
        when(identityService.getUserByUserName("matti")).thenReturn(mock(SUser.class));
        Map<String, Serializable> credentials = new HashMap<String, Serializable>();
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, "bpm");
        credentials.put(AuthenticationConstants.BASIC_USERNAME, "matti");
        assertNotNull(authenticationService.checkUserCredentials(credentials));
    }

    @Test
    public void authKOWithAnUnknownUser() throws Exception {
        when(identityService.getUserByUserName("hannu")).thenThrow(new SUserNotFoundException("plop"));
        Map<String, Serializable> credentials = new HashMap<String, Serializable>();
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, "bpm");
        credentials.put(AuthenticationConstants.BASIC_USERNAME, "hannu");
        assertNull(authenticationService.checkUserCredentials(credentials));
    }

}
