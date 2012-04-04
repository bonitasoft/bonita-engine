package org.bonitasoft.engine;

import org.bonitasoft.engine.identity.UserTestSP;
import org.bonitasoft.engine.log.LogTestSP;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Elias Ricken de Medeiros
 *
 */
@RunWith(Suite.class)
@SuiteClasses({
  BPMLocalTests.class,
  UserTestSP.class,
  LogTestSP.class
})
public class BPMTestsSP {

}
