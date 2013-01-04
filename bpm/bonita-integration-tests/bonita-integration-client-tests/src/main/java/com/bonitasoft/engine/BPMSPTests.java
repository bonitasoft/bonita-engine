package com.bonitasoft.engine;

import org.bonitasoft.engine.BPMRemoteTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.bonitasoft.engine.activity.MultiInstanceTest;
import com.bonitasoft.engine.connector.RemoteConnectorExecutionTestsSP;
import com.bonitasoft.engine.event.TimerBoundaryEventTest;
import com.bonitasoft.engine.external.ExternalCommandsTestSP;
import com.bonitasoft.engine.external.SPProfileMemberCommandTest;
import com.bonitasoft.engine.identity.SPIdentityTests;
import com.bonitasoft.engine.log.LogTest;
import com.bonitasoft.engine.platform.SPProcessManagementTest;
import com.bonitasoft.engine.process.ProcessTests;

@RunWith(Suite.class)
@SuiteClasses({
  SPIdentityTests.class,
//  SPPlatformTest.class, JIRA-482
  SPProcessManagementTest.class,
  LogTest.class,
  ExternalCommandsTestSP.class,
  BPMRemoteTests.class,
  SPProfileMemberCommandTest.class,
  MultiInstanceTest.class,
  ProcessTests.class,
  TimerBoundaryEventTest.class,
  RemoteConnectorExecutionTestsSP.class
})
public class BPMSPTests {

}
