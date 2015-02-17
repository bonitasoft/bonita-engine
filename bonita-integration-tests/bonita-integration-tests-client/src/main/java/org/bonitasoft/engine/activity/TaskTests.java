package org.bonitasoft.engine.activity;

import org.bonitasoft.engine.TestsInitializer;
import org.bonitasoft.engine.filter.user.UserFilterIT;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner.Initializer;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({
        UserTaskAssignationIT.class,
        UserFilterIT.class,
        PendingTasksIT.class,
        ReceiveTasksIT.class,
        HumanTasksIT.class,
        GetPossibleUsersOfPendingHumanTaskIT.class,
        ManualTasksIT.class,
        CallActivityIT.class,
        LoopIT.class,
        MultiInstanceIT.class,
})
@Initializer(TestsInitializer.class)
public class TaskTests {

}
