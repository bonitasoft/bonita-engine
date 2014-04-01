package org.bonitasoft.engine;

import org.bonitasoft.engine.process.document.DocumentIntegrationTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        DocumentIntegrationTest.class
})
public class TemporaryTests extends LocalIntegrationTests {

}
