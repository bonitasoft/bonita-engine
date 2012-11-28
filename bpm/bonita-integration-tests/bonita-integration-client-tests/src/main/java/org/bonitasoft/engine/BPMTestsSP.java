package org.bonitasoft.engine;

import org.bonitasoft.engine.command.web.ExternalCommandsTestSP;
import org.bonitasoft.engine.external.SPProfileMemberCommandTest;
import org.bonitasoft.engine.identity.SPIdentityTests;
import org.bonitasoft.engine.log.LogTest;
import org.bonitasoft.engine.log.LogTestSP;
import org.bonitasoft.engine.platform.SPPlatformTest;
import org.bonitasoft.engine.platform.SPProcessManagementTest;
import org.bonitasoft.engine.process.ProcessParameterTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
  SPIdentityTests.class,
 // SPPlatformTest.class,
  SPProcessManagementTest.class,
//  LogTest.class,
  LogTestSP.class,
  ExternalCommandsTestSP.class,
  ProcessParameterTest.class,
  BPMRemoteTests.class,
  SPProfileMemberCommandTest.class
})
public class BPMTestsSP {

}
