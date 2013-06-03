package org.bonitasoft.engine.process.instance;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        CancelProcessInstanceTest.class,
        AbortProcessInstanceTest.class,
        ProcessInstanceTest.class
})
public class ProcessInstanceTests {

}
