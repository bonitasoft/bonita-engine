package org.bonitasoft.engine.search;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        SearchProcessInstanceTest.class,
        SearchActivityInstanceTest.class,
        SearchCommentTest.class,
        SearchProcessDefinitionTest.class,
        SearchProcessDeploymentInfosCanBeStartedByUsersManagedByIT.class,
        SearchProcessDeploymentInfosCanBeStartedByIT.class,
        SearchUncategorizedProcessDeploymentInfosCanBeStartedByIT.class,
        SearchProcessDeploymentInfosWithAssignedOrPendingHumanTasksForUserIT.class
})
public class SearchEntitiesTests {

}
