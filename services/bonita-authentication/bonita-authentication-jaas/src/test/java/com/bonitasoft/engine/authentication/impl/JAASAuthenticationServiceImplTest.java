package com.bonitasoft.engine.authentication.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class JAASAuthenticationServiceImplTest {

    /**
     * 
     */
    private static final String JAVA_SECURITY_AUTH_LOGIN_CONFIG = "java.security.auth.login.config";

    private JAASAuthenticationServiceImpl jaasAuthService;

    @Mock
    TechnicalLoggerService logger;

    @Mock
    ReadSessionAccessor sessionAccessor;

    @BeforeClass
    public static void classSetUp() {
        System.setProperty(JAVA_SECURITY_AUTH_LOGIN_CONFIG, "src/test/resources/jaas-test.cfg");
    }

    @AfterClass
    public static void classTearDown() {
        System.clearProperty(JAVA_SECURITY_AUTH_LOGIN_CONFIG);
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        jaasAuthService = new JAASAuthenticationServiceImpl(logger, sessionAccessor);

        when(sessionAccessor.getTenantId()).thenReturn(1L);
    }

    @Test
    public void canLoginWithCorrectUsernamePassword() throws Exception {
        final boolean valid = jaasAuthService.checkUserCredentials("admin", "bpm");
        assertTrue(valid);
    }

    @Test
    public void cannotLoginWithWrongPassword() throws Exception {
        final boolean valid = jaasAuthService.checkUserCredentials("admin", "wrongPassWord");
        assertFalse(valid);
    }

}
