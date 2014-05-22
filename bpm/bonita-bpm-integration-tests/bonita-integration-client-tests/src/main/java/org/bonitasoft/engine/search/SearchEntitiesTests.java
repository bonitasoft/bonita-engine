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
        SearchProcessDeploymentInfosUsersManagedByCanStartTest.class,
        SearchProcessDeploymentInfosUserCanStartTest.class,
        SearchUncategorizedProcessDeploymentInfosUserCanStartTest.class
})
public class SearchEntitiesTests {

}
