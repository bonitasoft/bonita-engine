package org.bonitasoft.engine;

import org.bonitasoft.engine.BonitaSuiteRunner.Initializer;
import org.bonitasoft.engine.accessors.TenantAccessorTest;
import org.bonitasoft.engine.command.AdvancedStartProcessCommandIT;
import org.bonitasoft.engine.command.CommandTest;
import org.bonitasoft.engine.command.web.ExternalCommandsTest;
import org.bonitasoft.engine.event.SignalEventTest;
import org.bonitasoft.engine.identity.UserTest;
import org.bonitasoft.engine.login.LoginAPITest;
import org.bonitasoft.engine.login.PlatformLoginAPITest;
import org.bonitasoft.engine.operation.OperationTest;
import org.bonitasoft.engine.platform.command.PlatformCommandTest;
import org.bonitasoft.engine.process.ProcessManagementTest;
import org.bonitasoft.engine.profile.ProfileITest;
import org.bonitasoft.engine.search.SearchProcessInstanceTest;
import org.bonitasoft.engine.supervisor.ProcessSupervisedTest;
import org.bonitasoft.engine.test.APITestUtil;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({
        RemoteEngineTests.class,// this class is only in remote (we test that server stack trace are reported in client side)
        PlatformCommandTest.class,
        ProcessManagementTest.class,
        ProfileITest.class,
        SearchProcessInstanceTest.class,
        SignalEventTest.class,
        UserTest.class,
        LoginAPITest.class,
        PlatformLoginAPITest.class,
        CommandTest.class,
        ExternalCommandsTest.class,
        ProcessSupervisedTest.class,
        OperationTest.class,
        TenantAccessorTest.class,
        MultiThreadCallsTest.class,
        AdvancedStartProcessCommandIT.class })
@Initializer(BPMRemoteTestsForServers.class)
public class BPMRemoteTestsForServers {

    private static APITestUtil apiTestUtil = new APITestUtil();

    public static void beforeAll() throws Exception {
        System.err.println("=================== BPMRemoteTestsForServers.beforeClass()");
        apiTestUtil.createPlatformStructure();
        apiTestUtil.initializeAndStartPlatformWithDefaultTenant(true);
    }

    public static void afterAll() throws Exception {
        System.err.println("=================== BPMRemoteTestsForServers.afterClass()");
        apiTestUtil.stopAndCleanPlatformAndTenant(true);
        apiTestUtil.deletePlatformStructure();
    }
}
