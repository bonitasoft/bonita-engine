package com.bonitasoft.engine.authentication.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(logger.isLoggable(NoAuthenticationServiceImpl.class, TechnicalLogSeverity.TRACE)).thenReturn(true);
    }

    @Test
    public void authOKWithAnExistingUser() throws Exception {
        when(identityService.getUserByUserName("matti")).thenReturn(mock(SUser.class));
        assertTrue(authenticationService.checkUserCredentials("matti", "bpm"));
    }

    @Test
    public void authKOWithAnUnknownUser() throws Exception {
        when(identityService.getUserByUserName("hannu")).thenThrow(SUserNotFoundException.class);
        assertFalse(authenticationService.checkUserCredentials("hannu", "bpm"));
    }

}
