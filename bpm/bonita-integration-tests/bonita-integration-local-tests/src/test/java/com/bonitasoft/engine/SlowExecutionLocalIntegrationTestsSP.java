package com.bonitasoft.engine;

import org.bonitasoft.engine.BonitaSuiteRunner;
import org.bonitasoft.engine.event.InterruptingTimerBoundaryEventTest;
import org.bonitasoft.engine.event.MessageBoundaryEventTest;
import org.bonitasoft.engine.event.MessageEventSubProcessTest;
import org.bonitasoft.engine.event.MessageEventTest;
import org.bonitasoft.engine.event.NonInterruptingTimerBoundaryEventTest;
import org.bonitasoft.engine.event.TimerEventSubProcessTest;
import org.bonitasoft.engine.event.TimerEventTest;
import org.bonitasoft.engine.platform.PlatformLoginTest;
import org.bonitasoft.engine.platform.PlatformTest;
import org.bonitasoft.engine.test.BPMLocalSuiteTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

import com.bonitasoft.engine.event.SPTimerBoundaryEventTest;
import com.bonitasoft.engine.identity.SPIdentityTests;
import com.bonitasoft.engine.platform.SPPlatformLoginTest;
import com.bonitasoft.engine.platform.SPPlatformTest;
import com.bonitasoft.engine.platform.SPProcessManagementTest;

@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({

        // Same suites as in LocalIntegrationTestsSP below:
        BPMSPTests.class,
        BPMLocalSuiteTests.class,
        LocalLogTest.class,
        APIMethodSPTest.class,
        ConnectorExecutionTimeOutTest.class,
        ConnectorImplementationLocalSPTest.class,

        // Specific slow test suites below:
        NonInterruptingTimerBoundaryEventTest.class,
        InterruptingTimerBoundaryEventTest.class,
        TimerEventTest.class,
        MessageEventTest.class,
        MessageBoundaryEventTest.class,
        TimerEventSubProcessTest.class,
        MessageEventSubProcessTest.class,
        PlatformLoginTest.class,
        PlatformTest.class,

        // SP specific slow test suites:
        SPPlatformLoginTest.class,
        SPPlatformTest.class,
        SPTimerBoundaryEventTest.class,
        SPIdentityTests.class,
        SPProcessManagementTest.class
})
public class SlowExecutionLocalIntegrationTestsSP extends LocalIntegrationTestsSP {

}
