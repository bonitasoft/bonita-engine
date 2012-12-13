package com.bonitasoft.engine.process;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    ProcessResolutionTest.class,
    HiddenTaskTest.class,
    ManualTasksTest.class,
    ProcessManagementTest.class,
    TaskOnDemandTest.class,
})
public class ProcessTests {

}
