package org.bonitasoft.engine.process.instance;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        CancelProcessInstanceIT.class,
        AbortProcessInstanceIT.class,
        ProcessInstanceIT.class
})
public class ProcessInstanceTests {

}
