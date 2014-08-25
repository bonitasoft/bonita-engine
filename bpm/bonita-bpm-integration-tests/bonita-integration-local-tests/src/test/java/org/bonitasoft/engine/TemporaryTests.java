package org.bonitasoft.engine;

import org.bonitasoft.engine.activity.UserTaskContractTest;
import org.bonitasoft.engine.process.ProcessManagementTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({
        UserTaskContractTest.class
})
public class TemporaryTests extends LocalIntegrationTests {

}
