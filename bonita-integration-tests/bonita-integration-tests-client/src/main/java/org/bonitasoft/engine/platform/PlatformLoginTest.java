package org.bonitasoft.engine.platform;

import static org.junit.Assert.fail;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.session.SessionNotFoundException;
import org.bonitasoft.engine.session.impl.PlatformSessionImpl;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlatformLoginTest extends CommonAPITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformLoginTest.class);

    private static PlatformLoginAPI platformLoginAPI;

    private static PlatformSession session;

    @BeforeClass
    public static void beforeClass() throws BonitaException {
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

    @Cover(classes = PlatformAPI.class, concept = BPMNConcept.NONE, keywords = { "Platform", "Login" }, story = "Try to log with wrong loggin.", jira = "")
    @Test(expected = PlatformLoginException.class)
    public void wrongLogin() throws BonitaException {
        try {
            platformLoginAPI.logout(session);
            platformLoginAPI.login("titi", "toto");
            fail();
        } finally {
            logAsPlatformAdmin();
        }
    }

    @Cover(classes = PlatformAPI.class, concept = BPMNConcept.NONE, keywords = { "Platform", "Login" }, story = "Try to log with wrong loggin.", jira = "")
    @Test(expected = SessionNotFoundException.class)
    public void logoutWithWrongSession() throws BonitaException {
        try {
            platformLoginAPI.logout(new PlatformSessionImpl(123l, null, -1l, null, -1l));
        } finally {
            platformLoginAPI.logout(session);
            logAsPlatformAdmin();
        }
    }

}
