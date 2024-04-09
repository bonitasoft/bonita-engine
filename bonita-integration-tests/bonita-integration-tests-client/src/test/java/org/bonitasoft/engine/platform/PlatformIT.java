/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;

import java.time.Duration;

import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.PrintTestsStatusRule;
import org.bonitasoft.engine.api.APIClient;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.PlatformTestUtil;
import org.bonitasoft.engine.util.FunctionalMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlatformIT extends CommonAPIIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformIT.class);

    private static PlatformAPI platformAPI;

    private static PlatformSession session;

    private static final PlatformTestUtil platformTestUtil = new PlatformTestUtil();

    @After
    public void after() throws BonitaException {
        if (!platformAPI.isNodeStarted()) {
            platformAPI.startNode();
        }
        platformTestUtil.logoutOnPlatform(session);
        try {
            platformTestUtil.deployCommandsOnDefaultTenant();
        } catch (AlreadyExistsException ignored) {

        }
    }

    @Before
    public void before() throws BonitaException {
        session = platformTestUtil.loginOnPlatform();
        platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        if (!platformAPI.isNodeStarted()) {
            platformAPI.startNode();
        }
    }

    @Rule
    public TestRule testWatcher = new PrintTestsStatusRule(LOGGER) {

        @Override
        public void clean() {
        }
    };

    @Test
    public void isPlatformCreated() throws BonitaException {
        assertTrue(platformAPI.isPlatformCreated());
    }

    @Test
    public void isPlatformInitialized() throws BonitaException {
        assertTrue(platformAPI.isPlatformInitialized());
    }

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
    }

    @Test
    public void callStopNodeTwice() throws Exception {
        platformAPI.stopNode();
        platformAPI.stopNode();
    }

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

    @Test
    public void should_have_processes_with_duration_timer_still_works_after_start() throws Exception {
        APIClient apiClient = new APIClient();
        apiClient.login("install", "install");
        ProcessDefinition wait1Sec = apiClient.getProcessAPI()
                .deployAndEnableProcess(new ProcessDefinitionBuilder()
                        .createNewInstance("a process with 1 sec intermediate timer", "1.0")
                        .addIntermediateCatchEvent("wait1Sec").addTimerEventTriggerDefinition(TimerType.DURATION,
                                new ExpressionBuilder().createConstantLongExpression(1000))
                        .getProcess());
        for (int i = 0; i < 100; i++) {
            apiClient.getProcessAPI().startProcess(wait1Sec.getId());
        }

        Thread.sleep(800);
        stopNodeAndStartNode();
        apiClient.login("install", "install");
        await().atMost(Duration.ofMinutes(2)).until(() -> apiClient.getProcessAPI().getNumberOfProcessInstances(),
                (FunctionalMatcher<Long>) it -> it == 0);
    }
}
