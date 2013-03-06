package org.bonitasoft.engine.test;

import org.bonitasoft.engine.exception.BonitaException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.bonitasoft.engine.BPMSPTests;

@RunWith(Suite.class)
@SuiteClasses({
        BPMSPTests.class,
        BPMLocalSuiteTests.class,
        LocalLogTest.class,
        APIMethodSPTest.class,
        ConnectorExecutionTimeOutTest.class
})
public class LocalIntegrationTestsSP {

    @BeforeClass
    public static void beforeClass() throws BonitaException {
        System.err.println("=================== LocalIntegrationTestsSP.beforeClass()");
        APITestUtil.createPlatformStructure();
        System.setProperty("delete.job.frequency", "0/30 * * * * ?");
    }

    @AfterClass
    public static void afterClass() throws BonitaException {
        System.err.println("=================== LocalIntegrationTestsSP.afterClass()");
        APITestUtil.deletePlatformStructure();
    }

}
