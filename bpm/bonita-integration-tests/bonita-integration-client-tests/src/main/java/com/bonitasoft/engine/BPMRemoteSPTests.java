package com.bonitasoft.engine;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.bonitasoft.engine.activity.MultiInstanceTest;
import com.bonitasoft.engine.connector.RemoteConnectorExecutionTestSP;
import com.bonitasoft.engine.external.ExternalCommandsTestSP;
import com.bonitasoft.engine.log.LogTest;
import com.bonitasoft.engine.monitoring.MonitoringAPITest;
import com.bonitasoft.engine.monitoring.PlatformMonitoringAPITest;
import com.bonitasoft.engine.platform.NodeAPITest;
import com.bonitasoft.engine.process.ProcessTests;
import com.bonitasoft.engine.profile.ProfileTests;
import com.bonitasoft.engine.reporting.ReportingAPIIT;
import com.bonitasoft.engine.search.SearchEntitiesTests;

@RunWith(Suite.class)
@SuiteClasses({
    // SPIdentityTests.class, // slow execution test suite only
    // SPProcessManagementTest.class, // slow execution test suite only
    NodeAPITest.class,
    LogTest.class,
    ExternalCommandsTestSP.class,
    MultiInstanceTest.class,
    ProcessTests.class,
    ProfileTests.class,
    RemoteConnectorExecutionTestSP.class,
    MonitoringAPITest.class,
    SearchEntitiesTests.class,
    ReportingAPIIT.class,
    PlatformMonitoringAPITest.class
})
public class BPMRemoteSPTests {

}
