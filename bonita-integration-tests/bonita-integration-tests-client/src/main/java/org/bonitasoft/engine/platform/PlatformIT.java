/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.platform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.PlatformTestUtil;
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

public class PlatformIT extends CommonAPIIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformIT.class);

    private static PlatformAPI platformAPI;

    private static PlatformSession session;

    private static PlatformTestUtil platformTestUtil = new PlatformTestUtil();

    @BeforeClass
    public static void beforeClass() throws BonitaException {
        session = platformTestUtil.loginOnPlatform();
        platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
    }

    @AfterClass
    public static void afterClass() throws BonitaException {
        platformTestUtil.logoutOnPlatform(session);
        // Restore initial state:
        platformTestUtil.deployCommandsOnDefaultTenant();
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
            LOGGER.warn("Starting test: " + d.getClassName() + "." + d.getMethodName());
        }

        @Override
        public void failed(final Throwable cause, final Description d) {
            LOGGER.warn("Failed test: " + d.getClassName() + "." + d.getMethodName());
            LOGGER.warn("-------------------------------------------------------------------------------------");
        }

        @Override
        public void succeeded(final Description d) {
            LOGGER.warn("Succeeded test: " + d.getClassName() + "." + d.getMethodName());
            LOGGER.warn("-------------------------------------------------------------------------------------");
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
        platformAPI.cleanAndDeletePlatform();
        assertFalse(platformAPI.isPlatformCreated());
    }

    @Cover(classes = PlatformAPI.class, concept = BPMNConcept.NONE, keywords = { "Platform", "State" }, story = "Try to get platform state.", jira = "")
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
        platformAPI.cleanAndDeletePlatform();
        // when platform does not exists it return STOPPED now
        assertEquals(PlatformState.STOPPED, platformAPI.getPlatformState());
    }

    @Cover(classes = PlatformAPI.class, concept = BPMNConcept.NONE, keywords = { "Platform", "Node" }, story = "Get exception when starting node.", jira = "ENGINE-621")
    @Test(expected = StartNodeException.class)
    public void unableToStartANodeIfTheNodeIsNotCreated() throws Exception {
        platformAPI.stopNode();
        platformAPI.cleanAndDeletePlatform();
        assertFalse(platformAPI.isPlatformCreated());
        platformAPI.startNode();
    }

    @Cover(classes = PlatformAPI.class, concept = BPMNConcept.NONE, keywords = { "Platform", "Node" }, story = "Get exception when stopping node.", jira = "ENGINE-621")
    @Test
    public void callStopNodeTwice() throws Exception {
        platformAPI.stopNode();
        platformAPI.stopNode();
        platformAPI.cleanAndDeletePlatform();
    }

    @Cover(classes = PlatformAPI.class, concept = BPMNConcept.NONE, keywords = { "Platform", "Node" }, story = "stop node then start it with same session.", jira = "")
    @Test
    public void stopNodeAndStartNode() throws Exception {
        final LoginAPI loginAPI = TenantAPIAccessor.getLoginAPI();
        final APISession tenantSession = loginAPI.login("install", "install");
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(tenantSession);
        identityAPI.getNumberOfUsers();
        platformAPI.stopNode();
        platformAPI.startNode();
        try {
            identityAPI.getNumberOfUsers();
            fail("session should not work");
        } catch (final InvalidSessionException e) {
            // ok
        }
    }
}
