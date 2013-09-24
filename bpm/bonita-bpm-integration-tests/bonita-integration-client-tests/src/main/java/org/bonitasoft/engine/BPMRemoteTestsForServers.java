package org.bonitasoft.engine;

import org.bonitasoft.engine.BonitaSuiteRunner.Initializer;
import org.bonitasoft.engine.test.APITestUtil;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({ BPMRemoteTests.class })
@Initializer(BPMRemoteTestsForServers.class)
public class BPMRemoteTestsForServers {

    public static void beforeAll() throws Exception {
        System.err.println("=================== BPMRemoteTestsForServers.beforeClass()");
        APITestUtil.createPlatformStructure();
        APITestUtil.initializeAndStartPlatformWithDefaultTenant(true);
    }

    public static void afterAll() throws Exception {
        System.err.println("=================== BPMRemoteTestsForServers.afterClass()");
        APITestUtil.stopAndCleanPlatformAndTenant(true);
        APITestUtil.deletePlatformStructure();
    }
}
