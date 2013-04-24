/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import org.bonitasoft.engine.BPMRemoteTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.bonitasoft.engine.activity.MultiInstanceTest;
import com.bonitasoft.engine.breakpoint.BreakpointsTest;
import com.bonitasoft.engine.connector.RemoteConnectorExecutionTestSP;
import com.bonitasoft.engine.event.TimerBoundaryEventTest;
import com.bonitasoft.engine.external.ExternalCommandsTestSP;
import com.bonitasoft.engine.external.SPProfileMemberCommandTest;
import com.bonitasoft.engine.identity.SPIdentityTests;
import com.bonitasoft.engine.log.LogTest;
import com.bonitasoft.engine.migration.MigrationTest;
import com.bonitasoft.engine.monitoring.MonitoringAPITest;
import com.bonitasoft.engine.monitoring.PlatformMonitoringAPITest;
import com.bonitasoft.engine.platform.NodeAPITest;
import com.bonitasoft.engine.platform.SPProcessManagementTest;
import com.bonitasoft.engine.process.ProcessTests;
import com.bonitasoft.engine.search.SearchEntitiesTests;

@RunWith(Suite.class)
@SuiteClasses({
        SPIdentityTests.class,
        // SPPlatformTest.class, JIRA-482
        SPProcessManagementTest.class,
        NodeAPITest.class ,
        LogTest.class,
        ExternalCommandsTestSP.class,
        BPMRemoteTests.class,
        SPProfileMemberCommandTest.class,
        MultiInstanceTest.class,
        ProcessTests.class,
        TimerBoundaryEventTest.class,
        RemoteConnectorExecutionTestSP.class,
        PlatformMonitoringAPITest.class,
        MonitoringAPITest.class,
        MigrationTest.class,
        BreakpointsTest.class,
        SearchEntitiesTests.class
})
public class BPMSPTests {

}
