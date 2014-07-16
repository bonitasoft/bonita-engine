package org.bonitasoft.engine;

import org.bonitasoft.engine.process.ProcessManagementTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({
        ProcessManagementTest.class
})
public class TemporaryTests extends LocalIntegrationTests {

}
