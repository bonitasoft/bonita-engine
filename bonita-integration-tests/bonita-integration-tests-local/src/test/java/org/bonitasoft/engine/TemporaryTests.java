package org.bonitasoft.engine;

import org.bonitasoft.engine.event.InterruptingTimerBoundaryEventIT;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({
        InterruptingTimerBoundaryEventIT.class })
@Initializer(TestsInitializer.class)
public class TemporaryTests extends LocalIntegrationTests {

}
