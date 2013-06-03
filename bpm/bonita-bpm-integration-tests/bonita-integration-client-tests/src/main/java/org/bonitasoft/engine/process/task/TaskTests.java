package org.bonitasoft.engine.process.task;

import org.bonitasoft.engine.filter.user.UserFilterTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        HiddenTaskTest.class,
        UserTaskAssignationTest.class,
        UserFilterTest.class,
        PendingTasksTest.class,
        ReceiveTasksTest.class,
        HumanTasksTest.class
})
public class TaskTests {

}
