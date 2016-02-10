/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import com.bonitasoft.engine.bpm.test.ParameterAndDataExpressionIntegrationTest;
import com.bonitasoft.engine.services.event.IdentityServiceUsingEventServiceTest;
import com.bonitasoft.engine.services.event.RecorderAndEventServiceTest;
import com.bonitasoft.engine.services.monitoring.MonitoringTests;
import org.bonitasoft.engine.AllBPMTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@SuiteClasses({
        ParameterAndDataExpressionIntegrationTest.class,
        AllBPMTests.class,
        IdentityServiceUsingEventServiceTest.class,
        RecorderAndEventServiceTest.class,
        // Last test suite in order to check the correct begin/complete transactions
        MonitoringTests.class,
        PlatformExtIT.class
})
@RunWith(Suite.class)
public class AllBPMSPTests {


}
