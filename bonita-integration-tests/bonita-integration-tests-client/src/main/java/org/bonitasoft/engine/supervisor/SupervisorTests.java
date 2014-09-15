package org.bonitasoft.engine.supervisor;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        ProcessSupervisedTest.class,
        SupervisorTest.class
})
public class SupervisorTests {

}
