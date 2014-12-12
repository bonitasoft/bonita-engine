package org.bonitasoft.engine.search;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        SearchProcessInstanceIT.class,
        SearchActivityInstanceIT.class,
        SearchCommentIT.class,
        SearchProcessDefinitionIT.class,
        SearchProcessDeploymentInfosCanBeStartedByUsersManagedByIT.class,
        SearchProcessDeploymentInfosCanBeStartedByIT.class,
        SearchUncategorizedProcessDeploymentInfosCanBeStartedByIT.class,
        SearchProcessDeploymentInfosWithAssignedOrPendingHumanTasksForIT.class,
        SearchProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedByIT.class,
        SearchProcessDeploymentInfosWithAssignedOrPendingHumanTasksIT.class
})
public class SearchEntitiesTests {

}
