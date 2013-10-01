package org.bonitasoft.engine;

import java.io.IOException;

import org.bonitasoft.engine.BonitaSuiteRunner.Initializer;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.test.APIMethodTest;
import org.bonitasoft.engine.test.BPMLocalSuiteTests;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({
        BPMLocalSuiteTests.class,
        BPMRemoteTests.class,
        APIMethodTest.class })
@Initializer(TestsInitializer.class)
public class LocalIntegrationTests {

    @BeforeClass
    public static void beforeClass() throws BonitaException, IOException {
        System.err.println("=================== LocalIntegrationTests setup");
    }

    @AfterClass
    public static void afterClass() throws BonitaException, IOException {
        System.err.println("=================== LocalIntegrationTests teardown");
    }

}
