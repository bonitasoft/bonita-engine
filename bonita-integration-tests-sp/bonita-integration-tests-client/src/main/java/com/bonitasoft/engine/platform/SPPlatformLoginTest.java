/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel â€“ 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.platform;

import static org.junit.Assert.fail;

import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.platform.PlatformLoginException;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.session.SessionNotFoundException;
import org.bonitasoft.engine.session.impl.PlatformSessionImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonitasoft.engine.api.PlatformAPIAccessor;

@SuppressWarnings("javadoc")
public class SPPlatformLoginTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SPPlatformLoginTest.class);

    private static PlatformLoginAPI platformLoginAPI;

    private static PlatformSession session;

    @BeforeClass
    public static void beforeClass() throws Exception {
        platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
        logAsPlatformAdmin();
    }

    private static void logAsPlatformAdmin() throws PlatformLoginException {
        session = platformLoginAPI.login("platformAdmin", "platform");
    }

    @AfterClass
    public static void afterClass() throws BonitaException {
        platformLoginAPI.logout(session);
    }

    @Rule
    public TestRule testWatcher = new TestWatcher() {

        @Override
        public void starting(final Description d) {
            LOGGER.info("Starting test: " + getClass().getName() + "." + d.getMethodName());
        }

        @Override
        public void failed(final Throwable cause, final Description d) {
            LOGGER.info("Failed test: " + getClass().getName() + "." + d.getMethodName());
            LOGGER.info("-------------------------------------------------------------------------------------");
        }

        @Override
        public void succeeded(final Description d) {
            LOGGER.info("Succeeded test: " + getClass().getName() + "." + d.getMethodName());
            LOGGER.info("-------------------------------------------------------------------------------------");
        }

    };

    @Test(expected = PlatformLoginException.class)
    public void wrongloginOnDefaultTenantWithDefaultTechnicalLogger() throws Exception {
        try {
            platformLoginAPI.logout(session);
            platformLoginAPI.login("titi", "toto");
            fail();
        } finally {
            logAsPlatformAdmin();
        }
    }

    @Test(expected = SessionNotFoundException.class)
    public void logoutWithWrongSession() throws Exception {
        try {
            platformLoginAPI.logout(new PlatformSessionImpl(123l, null, -1l, null, -1l));
        } finally {
            platformLoginAPI.logout(session);
            logAsPlatformAdmin();
        }
    }

}
