/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import org.bonitasoft.engine.BonitaSuiteRunner;
import org.bonitasoft.engine.BonitaSuiteRunner.Initializer;
import org.bonitasoft.engine.platform.PlatformTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({
        // TestShadesSP.class,
        // // Same suites as in LocalIntegrationTestsSP below:
        // BPMSPTests.class,
        // BPMLocalSuiteTests.class,
        // LocalLogTest.class,
        // APIMethodSPTest.class,
        // ConnectorExecutionTimeOutTest.class,
        // ConnectorImplementationLocalSPTest.class,
        //
        // // Specific slow test suites below:
        // NonInterruptingTimerBoundaryEventTest.class,
        // InterruptingTimerBoundaryEventTest.class,
        // TimerBoundaryEventTest.class,
        // TimerEventTest.class,
        // MessageEventTest.class,
        // MessageBoundaryEventTest.class,
        // TimerEventSubProcessTest.class,
        // MessageEventSubProcessTest.class,
        // PlatformLoginTest.class,
        PlatformTest.class

// // SP specific slow test suites:
// SPPlatformLoginTest.class,
// SPPlatformTest.class,
// SPTimerBoundaryEventTest.class,
// SPIdentityTests.class,
// SPProcessManagementTest.class
})
@Initializer(TestsInitializerSP.class)
public class SlowExecutionLocalIntegrationTestsSP extends LocalIntegrationTestsSP {

}
