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
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

import com.bonitasoft.engine.event.SPTimerBoundaryEventTest;
import com.bonitasoft.engine.identity.SPIdentityTests;
import com.bonitasoft.engine.platform.SPPlatformLoginTest;
import com.bonitasoft.engine.platform.SPPlatformTest;
import com.bonitasoft.engine.platform.SPProcessManagementTest;
import com.bonitasoft.engine.tenant.TenantMaintenanceIT;

@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({
        TestShadesSP.class,
        LocalIntegrationTestsSP.class,

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
        PlatformTest.class,

        // SP specific slow test suites:
        SPPlatformLoginTest.class,
        SPPlatformTest.class,
        SPTimerBoundaryEventTest.class,
        SPIdentityTests.class,
        TenantMaintenanceIT.class,
        SPProcessManagementTest.class, 
})
@Initializer(TestsInitializerSP.class)
public class SlowExecutionLocalIntegrationTestsSP {

}
