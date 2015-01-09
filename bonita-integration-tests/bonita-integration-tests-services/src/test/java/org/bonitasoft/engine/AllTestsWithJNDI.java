package org.bonitasoft.engine;

import org.bonitasoft.engine.test.runner.BonitaSuiteRunner;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner.Initializer;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

// FIXME add paltformtest suite
// FIXME abstract impl test suites
@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({ AllTests.class })
@Initializer(TestsInitializerService.class)
public class AllTestsWithJNDI {

}
