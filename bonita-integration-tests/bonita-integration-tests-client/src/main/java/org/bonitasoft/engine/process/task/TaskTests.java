package org.bonitasoft.engine.process.task;

import org.bonitasoft.engine.TestsInitializer;
import org.bonitasoft.engine.filter.user.UserFilterIT;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner.Initializer;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({
        HiddenTaskIT.class,
        UserTaskAssignationIT.class,
        UserFilterIT.class,
        PendingTasksIT.class,
        ReceiveTasksIT.class,
        HumanTasksIT.class,
        GetPossibleUsersOfPendingHumanTaskIT.class
})
@Initializer(TestsInitializer.class)
public class TaskTests {

}
