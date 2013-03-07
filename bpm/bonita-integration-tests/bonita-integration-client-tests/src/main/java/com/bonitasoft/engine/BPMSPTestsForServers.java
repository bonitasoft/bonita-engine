package com.bonitasoft.engine;

import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.test.APITestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        BPMSPTests.class
})
public class BPMSPTestsForServers {

    @BeforeClass
    public static void beforeClass() throws BonitaException {
        System.err.println("=================== BPMSPTestsForServers.beforeClass()");
        APITestUtil.createPlatformStructure();
        System.setProperty("delete.job.frequency", "0/30 * * * * ?");
    }

    @AfterClass
    public static void afterClass() throws BonitaException {
        System.err.println("=================== BPMSPTestsForServers.afterClass()");
        APITestUtil.deletePlatformStructure();
    }

}
