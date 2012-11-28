package org.bonitasoft.engine.test;

import org.bonitasoft.engine.BPMTestsSP;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    BPMTestsSP.class,
    BPMLocalSuiteTests.class,
    LocalLogTest.class,
    APIMethodSPTest.class
})
public class LocalIntegrationTestsSP {

    @BeforeClass
    public static void setTestsProperties() {
        System.setProperty("delete.job.frequency", "0/30 * * * * ?");
    }

}
