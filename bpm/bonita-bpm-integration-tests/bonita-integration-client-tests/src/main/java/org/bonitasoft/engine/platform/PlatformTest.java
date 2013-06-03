package org.bonitasoft.engine.platform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.session.SessionNotFoundException;
import org.bonitasoft.engine.session.impl.PlatformSessionImpl;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlatformTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformTest.class);

    private static PlatformAPI platformAPI;

    private static PlatformLoginAPI platformLoginAPI;

    private static PlatformSession session;

    @BeforeClass
    public static void beforeClass() throws BonitaException {
        platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
        logAsPlatformAdmin();
        try {
            platformAPI.initializePlatform();
        } catch (final CreationException e) {
            // Platform already created
        }
        platformAPI.startNode();
    }

    private static void logAsPlatformAdmin() throws PlatformLoginException, BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        session = platformLoginAPI.login("platformAdmin", "platform");
        platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
    }

    @AfterClass
    public static void afterClass() throws BonitaException {
        platformAPI.stopNode();
        platformAPI.cleanPlatform();
        platformLoginAPI.logout(session);
    }

    @Before
    public void before() throws PlatformNotFoundException, CreationException, StartNodeException {
        if (!platformAPI.isPlatformCreated()) {
            platformAPI.createAndInitializePlatform();
            platformAPI.startNode();
        }
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
        }

        @Override
        public void succeeded(final Description d) {
            LOGGER.info("Succeeded test: " + getClass().getName() + "." + d.getMethodName());
        }

    };

    @Cover(classes = PlatformAPI.class, concept = BPMNConcept.NONE, keywords = { "Platform", "Login" }, story = "Try to log with wrong loggin.")
    @Test(expected = PlatformLoginException.class)
    public void testWrongLogin() throws BonitaException {
        try {
            platformLoginAPI.logout(session);
            platformLoginAPI.login("titi", "toto");
            fail();
        } finally {
            logAsPlatformAdmin();
        }
    }

    @Cover(classes = PlatformAPI.class, concept = BPMNConcept.NONE, keywords = { "Platform", "Login" }, story = "Try to log with wrong loggin.")
    @Test(expected = SessionNotFoundException.class)
    public void testLogoutWithWrongSession() throws BonitaException {
        try {
            platformLoginAPI.logout(new PlatformSessionImpl(123l, null, -1l, null, -1l));
        } finally {
            platformLoginAPI.logout(session);
            logAsPlatformAdmin();
        }
    }

    @Cover(classes = PlatformAPI.class, concept = BPMNConcept.NONE, keywords = { "Platform" }, story = "Test if platform is created.")
    @Test
    public void isPlatformCreated() throws BonitaException {
        assertTrue(platformAPI.isPlatformCreated());
    }

    @Cover(classes = PlatformAPI.class, concept = BPMNConcept.NONE, keywords = { "Platform" }, story = "Get exception when creating platform.")
    @Test(expected = CreationException.class)
    public void createPlatformException() throws BonitaException {
        assertTrue(platformAPI.isPlatformCreated());
        platformAPI.createAndInitializePlatform();
    }

    @Cover(classes = PlatformAPI.class, concept = BPMNConcept.NONE, keywords = { "Platform" }, story = "Get platform.")
    @Test
    public void getPlatform() throws BonitaException {
        final Platform platform = platformAPI.getPlatform();

        assertNotNull("can't find the platform", platform);
        assertEquals("platformAdmin", platform.getCreatedBy());
    }

    @Cover(classes = PlatformAPI.class, concept = BPMNConcept.NONE, keywords = { "Platform" }, story = "Delete platform.")
    @Test(expected = PlatformNotFoundException.class)
    public void deletePlatform() throws BonitaException {
        platformAPI.stopNode();
        platformAPI.cleanAndDeletePlaftorm();
        platformAPI.getPlatform();
    }

    @Cover(classes = PlatformAPI.class, concept = BPMNConcept.NONE, keywords = { "Platform", "State" }, story = "Try to get platfom state.")
    @Test
    public void getPlatformState() throws Exception {
        // test started state
        PlatformState state = platformAPI.getPlatformState();
        assertEquals(PlatformState.STARTED, state);
        // test stopped state
        platformAPI.stopNode();
        state = platformAPI.getPlatformState();
        assertEquals(PlatformState.STOPPED, state);
        // test exception:PlatformNotFoundException
        platformAPI.cleanAndDeletePlaftorm();
        try {
            platformAPI.getPlatformState();
            fail();
        } catch (final PlatformNotFoundException e) {
            // ok
        }
    }

    @Cover(classes = PlatformAPI.class, concept = BPMNConcept.NONE, keywords = { "Platform", "Node" }, story = "Get exception when starting node.", jira = "ENGINE-621")
    @Test(expected = StartNodeException.class)
    public void unableToStartANodeIfTheNodeIsNotCreated() throws Exception {
        platformAPI.stopNode();
        platformAPI.cleanAndDeletePlaftorm();
        assertFalse(platformAPI.isPlatformCreated());
        platformAPI.startNode();
    }

    @Cover(classes = PlatformAPI.class, concept = BPMNConcept.NONE, keywords = { "Platform", "Node" }, story = "Get exception when stopping node.", jira = "ENGINE-621")
    @Test
    public void callStopNodeTwice() throws Exception {
        platformAPI.stopNode();
        platformAPI.stopNode();
        platformAPI.cleanAndDeletePlaftorm();
    }

}
