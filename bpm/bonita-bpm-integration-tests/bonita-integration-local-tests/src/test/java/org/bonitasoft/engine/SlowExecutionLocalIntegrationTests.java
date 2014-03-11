package org.bonitasoft.engine;

import org.bonitasoft.engine.BonitaSuiteRunner.Initializer;
import org.bonitasoft.engine.event.InterruptingTimerBoundaryEventTest;
import org.bonitasoft.engine.event.MessageBoundaryEventTest;
import org.bonitasoft.engine.event.MessageEventSubProcessTest;
import org.bonitasoft.engine.event.MessageEventTest;
import org.bonitasoft.engine.event.NonInterruptingTimerBoundaryEventTest;
import org.bonitasoft.engine.event.TimerBoundaryEventTest;
import org.bonitasoft.engine.event.TimerEventSubProcessTest;
import org.bonitasoft.engine.event.TimerEventTest;
import org.bonitasoft.engine.platform.PlatformLoginTest;
import org.bonitasoft.engine.platform.PlatformTest;
import org.bonitasoft.engine.test.APIMethodTest;
import org.bonitasoft.engine.test.BPMLocalSuiteTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({
        TestShades.class,
        // // Same suites as in LocalIntegrationTests below:
        BPMLocalSuiteTests.class,
        BPMRemoteTests.class,
        APIMethodTest.class,

        // Specific slow test suites below:
        NonInterruptingTimerBoundaryEventTest.class,
        InterruptingTimerBoundaryEventTest.class,
        TimerBoundaryEventTest.class,
        TimerEventTest.class,
        MessageEventTest.class,
        MessageBoundaryEventTest.class,
        TimerEventSubProcessTest.class,
        MessageEventSubProcessTest.class,
        PlatformLoginTest.class,
        PlatformTest.class

})
@Initializer(TestsInitializer.class)
public class SlowExecutionLocalIntegrationTests extends LocalIntegrationTests {

}
