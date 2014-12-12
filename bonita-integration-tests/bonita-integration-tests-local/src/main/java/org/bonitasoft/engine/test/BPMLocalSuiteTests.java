package org.bonitasoft.engine.test;

import org.bonitasoft.engine.TestsInitializer;
import org.bonitasoft.engine.event.LocalInterruptingTimerBoundaryEventIT;
import org.bonitasoft.engine.event.LocalTimerEventIT;
import org.bonitasoft.engine.job.JobExecutionIT;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner.Initializer;
import org.bonitasoft.engine.theme.ThemeIT;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({
        JobExecutionIT.class,
        BPMLocalIT.class,
        ConnectorExecutionsLocalIT.class,
        ProcessWithExpressionLocalIT.class,
        ProcessArchiveIT.class,
        LocalTimerEventIT.class,
        LocalInterruptingTimerBoundaryEventIT.class,
        DataInstanceIntegrationLocalIT.class,
        GatewayExecutionLocalIT.class,
        ThemeIT.class,
        PermissionAPIIT.class
})
@Initializer(TestsInitializer.class)
public class BPMLocalSuiteTests {

}
