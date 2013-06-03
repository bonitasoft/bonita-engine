package org.bonitasoft.engine;

import org.bonitasoft.engine.test.APITestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ BPMRemoteTests.class })
public class BPMRemoteTestsForServers {

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.err.println("=================== BPMRemoteTestsForServers.beforeClass()");
        APITestUtil.createPlatformStructure();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        System.err.println("=================== BPMRemoteTestsForServers.afterClass()");
        APITestUtil.deletePlatformStructure();
    }
}
