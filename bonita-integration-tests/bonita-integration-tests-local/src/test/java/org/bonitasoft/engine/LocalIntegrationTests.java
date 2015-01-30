package org.bonitasoft.engine;

import org.bonitasoft.engine.business.application.ApplicationAPIITs;
import org.bonitasoft.engine.test.APIMethodIT;
import org.bonitasoft.engine.test.BPMLocalSuiteTests;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner.Initializer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({
        BPMLocalSuiteTests.class,
        BPMRemoteTests.class,
        ApplicationAPIITs.class,
        APIMethodIT.class })
@Initializer(TestsInitializer.class)
public class LocalIntegrationTests {

    @BeforeClass
    public static void beforeClass() {
        System.err.println("=================== LocalIntegrationTests setup");
    }

    @AfterClass
    public static void afterClass() {
        System.err.println("=================== LocalIntegrationTests afterClass");
    }

}
