/*******************************************************************************
 * Copyright (C) 2009-2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import org.bonitasoft.engine.BonitaSuiteRunner;
import org.bonitasoft.engine.BonitaSuiteRunner.Initializer;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

import com.bonitasoft.engine.activity.ActivityTests;
import com.bonitasoft.engine.business.application.ApplicationAPIIT;
import com.bonitasoft.engine.business.data.BDRepositoryIT;
import com.bonitasoft.engine.command.ExecuteBDMQueryCommandIT;
import com.bonitasoft.engine.connector.RemoteConnectorExecutionTestSP;
import com.bonitasoft.engine.external.ExternalCommandsTestSP;
import com.bonitasoft.engine.log.LogTest;
import com.bonitasoft.engine.monitoring.MonitoringAPITest;
import com.bonitasoft.engine.monitoring.PlatformMonitoringAPITest;
import com.bonitasoft.engine.operation.OperationTest;
import com.bonitasoft.engine.page.PageAPIIT;
import com.bonitasoft.engine.platform.NodeAPITest;
import com.bonitasoft.engine.process.ProcessTests;
import com.bonitasoft.engine.profile.ProfileAllSPITest;
import com.bonitasoft.engine.reporting.ReportingAPIIT;
import com.bonitasoft.engine.reporting.ReportingSQLValidityIT;
import com.bonitasoft.engine.search.SearchEntitiesTests;
import com.bonitasoft.engine.supervisor.SupervisedTests;
import com.bonitasoft.engine.tenant.TenantIT;

@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({
    PageAPIIT.class,
    ApplicationAPIIT.class,
    TenantIT.class,
    NodeAPITest.class,
    LogTest.class,
    ExternalCommandsTestSP.class,
    ActivityTests.class,
    ProcessTests.class,
    SupervisedTests.class,
    ProfileAllSPITest.class,
    RemoteConnectorExecutionTestSP.class,
    MonitoringAPITest.class,
    SearchEntitiesTests.class,
    ReportingAPIIT.class,
    ReportingSQLValidityIT.class,
    PlatformMonitoringAPITest.class,
    TenantIT.class,
    OperationTest.class,
    BDRepositoryIT.class,
    ExecuteBDMQueryCommandIT.class
})
@Initializer(TestsInitializerSP.class)
public class BPMRemoteSPTests {

}
