package org.bonitasoft.engine;

import org.bonitasoft.engine.command.web.ExternalCommandsTestSP;
import org.bonitasoft.engine.identity.UserTestSP;
import org.bonitasoft.engine.log.LogTest;
import org.bonitasoft.engine.log.LogTestSP;
import org.bonitasoft.engine.process.ProcessParameterTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
  UserTestSP.class,
  LogTest.class,
  LogTestSP.class,
  ExternalCommandsTestSP.class,
  ProcessParameterTest.class,
  BPMRemoteTests.class
})
public class BPMTestsSP {

}
