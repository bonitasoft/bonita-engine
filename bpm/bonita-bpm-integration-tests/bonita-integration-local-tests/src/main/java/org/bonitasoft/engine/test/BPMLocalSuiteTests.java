package org.bonitasoft.engine.test;

import org.bonitasoft.engine.event.LocalTimerEventTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        BPMLocalTest.class,
        ConnectorExecutionsTestsLocal.class,
        ProcessWithExpressionTestLocal.class,
        ProcessArchiveTest.class,
        LocalTimerEventTest.class,
        DataInstanceIntegrationLocalTest.class,
        GatewayExecutionLocalTest.class
})
public class BPMLocalSuiteTests {

}
