package org.bonitasoft.engine;

import org.bonitasoft.engine.test.ProcessArchiveTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        ProcessArchiveTest.class
})
public class TemporaryTests extends LocalIntegrationTests {

}
