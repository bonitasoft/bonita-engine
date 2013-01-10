package org.bonitasoft.engine.test;

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
    public static void setTestsProperties() {
        System.setProperty("delete.job.frequency", "0/30 * * * * ?");
    }

}
