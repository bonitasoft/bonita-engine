package org.bonitasoft.engine.platform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.APITestUtil;
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

    private static PlatformSession session;

    private static APITestUtil apiTestUtil = new APITestUtil();

    @BeforeClass
    public static void beforeClass() throws BonitaException {
        session = apiTestUtil.loginPlatform();
        platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
    }

    @AfterClass
    public static void afterClass() throws BonitaException {
        apiTestUtil.logoutPlatform(session);
    }

    @Before
    public void before() throws BonitaException {
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
            LOGGER.info("-------------------------------------------------------------------------------------");
        }

        @Override
        public void succeeded(final Description d) {
            LOGGER.info("Succeeded test: " + getClass().getName() + "." + d.getMethodName());
            LOGGER.info("-------------------------------------------------------------------------------------");
        }

    };

    @Cover(classes = PlatformAPI.class, concept = BPMNConcept.NONE, keywords = { "Platform" }, story = "Test if platform is created.", jira = "")
    @Test
    public void isPlatformCreated() throws BonitaException {
        assertTrue(platformAPI.isPlatformCreated());
    }

    @Cover(classes = PlatformAPI.class, concept = BPMNConcept.NONE, keywords = { "Platform" }, story = "Get exception when creating platform.", jira = "")
    @Test(expected = CreationException.class)
    public void createPlatformException() throws BonitaException {
        assertTrue(platformAPI.isPlatformCreated());
        platformAPI.createAndInitializePlatform();
    }

    @Cover(classes = PlatformAPI.class, concept = BPMNConcept.NONE, keywords = { "Platform" }, story = "Delete platform.", jira = "")
    @Test
    public void deletePlatform() throws BonitaException {
        platformAPI.stopNode();
        platformAPI.cleanAndDeletePlaftorm();
        assertFalse(platformAPI.isPlatformCreated());
    }

    @Cover(classes = PlatformAPI.class, concept = BPMNConcept.NONE, keywords = { "Platform", "State" }, story = "Try to get platfom state.", jira = "")
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
        // when platform does not exists it return STOPPED now
        assertEquals(PlatformState.STOPPED, platformAPI.getPlatformState());
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

    @Cover(classes = PlatformAPI.class, concept = BPMNConcept.NONE, keywords = { "Platform", "Node" }, story = "stop node then start it with same session.", jira = "")
    @Test
    public void stopNodeAndStartNode() throws Exception {
        LoginAPI loginAPI = TenantAPIAccessor.getLoginAPI();
        APISession tenantSession = loginAPI.login("install", "install");
        IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(tenantSession);
        identityAPI.getNumberOfUsers();
        platformAPI.stopNode();
        platformAPI.startNode();
        try {
            identityAPI.getNumberOfUsers();
            fail("session should not work");
        } catch (InvalidSessionException e) {
            // ok
            e.printStackTrace();
        }
    }

    @Cover(classes = PlatformAPI.class, concept = BPMNConcept.NONE, keywords = { "Platform", "Node" }, story = "all thread must be stopped after calling stop node.", jira = "BS-2353")
    @Test
    public void should_stopNode_stop_all_engine_threads() throws Exception {
        LoginAPI loginAPI = TenantAPIAccessor.getLoginAPI();
        APISession tenantSession = loginAPI.login("install", "install");
        ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(tenantSession);
        // this will add something in process cache
        ProcessDefinition processDefinition = processAPI.deployAndEnableProcess(new ProcessDefinitionBuilder().createNewInstance("aProcess", "plop").done());
        platformAPI.stopNode();
        // wait for thread to stop
        checkThreadsAreStopped();
        platformAPI.startNode();
        loginAPI = TenantAPIAccessor.getLoginAPI();
        tenantSession = loginAPI.login("install", "install");
        processAPI = TenantAPIAccessor.getProcessAPI(tenantSession);
        // check cache still works
        processAPI.getProcessDefinition(processDefinition.getId());
        processAPI.disableAndDeleteProcessDefinition(processDefinition.getId());
    }

    private void checkThreadsAreStopped() throws InterruptedException {
        Set<Thread> keySet = Thread.getAllStackTraces().keySet();
        Iterator<Thread> iterator = keySet.iterator();
        ArrayList<Thread> list = new ArrayList<Thread>();
        while (iterator.hasNext()) {
            Thread thread = iterator.next();
            if (isEngine(thread)) {
                // wait for the thread to die
                thread.join(5000);
                // if still alive print it
                if (thread.isAlive()) {
                    list.add(thread);
                }
            }
        }
        if (!list.isEmpty()) {
            throw new IllegalStateException("some threads are still active" + list);
        }
    }

    private boolean isEngine(final Thread thread) {
        String name = thread.getName();
        ThreadGroup threadGroup = thread.getThreadGroup();
        if (threadGroup != null && threadGroup.getName().equals("system")) {
            return false;
        }
        List<String> startWithFilter = Arrays.asList("H2 ", "BoneCP", "bitronix", "main", "Reference Handler", "Signal Dispatcher", "Finalizer",
                "com.google.common.base.internal.Finalizer"/* guava, used by bonecp */, "process reaper", "ReaderThread",
                "Abandoned connection cleanup thread"/* bonecp related */, "hz."/*
                                                                                 * hazelcast
                                                                                 * related
                                                                                 */);
        for (String prefix : startWithFilter) {
            if (name.startsWith(prefix)) {
                return false;
            }
        }
        return true;
    }
}
