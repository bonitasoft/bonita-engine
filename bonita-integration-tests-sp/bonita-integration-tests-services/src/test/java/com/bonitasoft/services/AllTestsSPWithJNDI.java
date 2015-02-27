/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.services;

import org.bonitasoft.engine.AllTests;
import org.bonitasoft.engine.TestsInitializerService;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner.Initializer;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

import com.bonitasoft.services.event.handler.IdentityServiceUsingEventServiceTest;
import com.bonitasoft.services.event.handler.RecorderAndEventServiceTest;
import com.bonitasoft.services.monitoring.MonitoringTests;

@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({
        AllTests.class,
        IdentityServiceUsingEventServiceTest.class,
        RecorderAndEventServiceTest.class,
        // Last test suite in order to check the correct begin/complete transactions
        MonitoringTests.class
})
@Initializer(TestsInitializerService.class)
public class AllTestsSPWithJNDI {

}
