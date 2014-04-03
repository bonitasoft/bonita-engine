package org.bonitasoft.engine.process.task;

import org.bonitasoft.engine.BonitaSuiteRunner;
import org.bonitasoft.engine.BonitaSuiteRunner.Initializer;
import org.bonitasoft.engine.TestsInitializer;
import org.bonitasoft.engine.filter.user.UserFilterTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({
        HiddenTaskTest.class,
        UserTaskAssignationTest.class,
        UserFilterTest.class,
        PendingTasksTest.class,
        ReceiveTasksTest.class,
        HumanTasksTest.class
})
@Initializer(TestsInitializer.class)
public class TaskTests {

}
