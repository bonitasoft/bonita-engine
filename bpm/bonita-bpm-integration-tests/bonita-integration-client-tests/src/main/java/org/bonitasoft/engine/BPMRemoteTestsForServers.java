package org.bonitasoft.engine;

import org.bonitasoft.engine.BonitaSuiteRunner.Initializer;
import org.bonitasoft.engine.test.APITestUtil;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({
        RemoteEngineTests.class,// this class is only in remote (we test that server stack trace are reported in client side)
        BPMRemoteTests.class })
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
