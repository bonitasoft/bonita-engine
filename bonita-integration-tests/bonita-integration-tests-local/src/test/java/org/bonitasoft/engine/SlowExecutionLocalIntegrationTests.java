package org.bonitasoft.engine;

import org.bonitasoft.engine.event.InterruptingTimerBoundaryEventIT;
import org.bonitasoft.engine.event.MessageBoundaryEventIT;
import org.bonitasoft.engine.event.MessageEventSubProcessIT;
import org.bonitasoft.engine.event.MessageEventIT;
import org.bonitasoft.engine.event.NonInterruptingTimerBoundaryEventIT;
import org.bonitasoft.engine.event.TimerBoundaryEventIT;
import org.bonitasoft.engine.event.TimerEventSubProcessIT;
import org.bonitasoft.engine.event.TimerEventIT;
import org.bonitasoft.engine.platform.PlatformLoginIT;
import org.bonitasoft.engine.platform.PlatformIT;
import org.bonitasoft.engine.test.APIMethodIT;
import org.bonitasoft.engine.test.BPMLocalSuiteTests;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner.Initializer;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({
        TestShades.class,
        // // Same suites as in LocalIntegrationTests below:
        BPMLocalSuiteTests.class,
        BPMRemoteTests.class,
        APIMethodIT.class,

        // Specific slow test suites below:
        NonInterruptingTimerBoundaryEventIT.class,
        InterruptingTimerBoundaryEventIT.class,
        TimerBoundaryEventIT.class,
        TimerEventIT.class,
        MessageEventIT.class,
        MessageBoundaryEventIT.class,
        TimerEventSubProcessIT.class,
        MessageEventSubProcessIT.class,
        PlatformLoginIT.class,
        PlatformIT.class

})
@Initializer(TestsInitializer.class)
public class SlowExecutionLocalIntegrationTests extends LocalIntegrationTests {

}
