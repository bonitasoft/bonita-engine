package com.bonitasoft.engine;

import org.bonitasoft.engine.BonitaSuiteRunner;
import org.bonitasoft.engine.BonitaSuiteRunner.Initializer;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

import com.bonitasoft.engine.activity.MultiInstanceTest;
import com.bonitasoft.engine.business.data.BDRepositoryIT;
import com.bonitasoft.engine.command.ExecuteBDMQueryCommandIT;
import com.bonitasoft.engine.connector.RemoteConnectorExecutionTestSP;
import com.bonitasoft.engine.external.ExternalCommandsTestSP;
import com.bonitasoft.engine.log.LogTest;
import com.bonitasoft.engine.monitoring.MonitoringAPITest;
import com.bonitasoft.engine.monitoring.PlatformMonitoringAPITest;
import com.bonitasoft.engine.page.PageAPIIT;
import com.bonitasoft.engine.operation.OperationTest;
import com.bonitasoft.engine.platform.NodeAPITest;
import com.bonitasoft.engine.process.ProcessTests;
import com.bonitasoft.engine.profile.ProfileAllSPITest;
import com.bonitasoft.engine.reporting.ReportingAPIIT;
import com.bonitasoft.engine.reporting.ReportingSQLValidityIT;
import com.bonitasoft.engine.search.SearchEntitiesTests;
import com.bonitasoft.engine.supervisor.SupervisedTests;

@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({
        // SPIdentityTests.class, // slow execution test suite only
        // SPProcessManagementTest.class, // slow execution test suite only
        PageAPIIT.class,
        TenantRemoteTestSpITest.class,
        NodeAPITest.class,
        LogTest.class,
        ExternalCommandsTestSP.class,
        MultiInstanceTest.class,
        ProcessTests.class,
        SupervisedTests.class,
        ProfileAllSPITest.class,
        RemoteConnectorExecutionTestSP.class,
        MonitoringAPITest.class,
        SearchEntitiesTests.class,
        ReportingAPIIT.class,
        ReportingSQLValidityIT.class,
        PlatformMonitoringAPITest.class,
        TenantTest.class,
        OperationTest.class,
        BDRepositoryIT.class,
        ExecuteBDMQueryCommandIT.class
})
@Initializer(TestsInitializerSP.class)
public class BPMRemoteSPTests {

}
