package org.bonitasoft.engine;

import org.bonitasoft.engine.connectors.RemoteConnectorExecutionTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        RemoteConnectorExecutionTest.class
})
public class TemporaryTests extends LocalIntegrationTests {

}
