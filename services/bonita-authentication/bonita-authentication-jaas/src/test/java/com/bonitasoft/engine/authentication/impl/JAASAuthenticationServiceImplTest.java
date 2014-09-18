/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.authentication.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.bonitasoft.engine.authentication.AuthenticationConstants;
import org.bonitasoft.engine.authentication.AuthenticationException;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class JAASAuthenticationServiceImplTest {

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
        Map<String, Serializable> credentials = new HashMap<String, Serializable>();
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, "bpm");
        credentials.put(AuthenticationConstants.BASIC_USERNAME, "admin");
        assertNotNull(jaasAuthService.checkUserCredentials(credentials));
    }

    @Test
    public void cannotLoginWithWrongPassword() {
        try {
            Map<String, Serializable> credentials = new HashMap<String, Serializable>();
            credentials.put(AuthenticationConstants.BASIC_PASSWORD, "wrongPassword");
            credentials.put(AuthenticationConstants.BASIC_USERNAME, "admin");
            jaasAuthService.checkUserCredentials(credentials);
        } catch (AuthenticationException e) {
            assertThat(e).hasCauseExactlyInstanceOf(LoginException.class);
            return;
        }
        fail();
    }

}
