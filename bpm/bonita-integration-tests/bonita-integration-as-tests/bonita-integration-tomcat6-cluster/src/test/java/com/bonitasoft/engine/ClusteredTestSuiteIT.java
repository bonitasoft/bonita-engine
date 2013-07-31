package com.bonitasoft.engine;

import org.bonitasoft.engine.test.APITestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ ClusterTests.class })
public class ClusteredTestSuiteIT {

    @BeforeClass
    public static void beforeBeforeClass() throws Exception {
        System.err.println("=================== BPMRemoteTestsForServers.beforeClass()");
        APITestUtil.createPlatformStructure();
    }

    @AfterClass
    public static void afterAfterClass() throws Exception {
        System.err.println("=================== BPMRemoteTestsForServers.afterClass()");
        APITestUtil.deletePlatformStructure();
    }
}
