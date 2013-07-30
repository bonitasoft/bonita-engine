package org.bonitasoft.engine.process;

import org.bonitasoft.engine.process.actor.ActorTests;
import org.bonitasoft.engine.process.comment.CommentTest;
import org.bonitasoft.engine.process.data.DataInstanceIntegrationTest;
import org.bonitasoft.engine.process.document.DocumentIntegrationTest;
import org.bonitasoft.engine.process.instance.ProcessInstanceTests;
import org.bonitasoft.engine.process.task.TaskTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        ProcessResolutionTest.class,
        ProcessExecutionTest.class,
        ProcessManagementTest.class,
        ProcessDeploymentTest.class,
        ProcessDescriptionTest.class,
        GetProcessDefinitionTest.class,
        GatewayExecutionTest.class,
        ProcessCategoryTest.class,
        ProcessInstanceTests.class,
        ProcessWithExpressionTest.class,
        ProcessDeletionTest.class,
        EvaluateExpressionTest.class,
        TaskTests.class,
        CommentTest.class,
        DocumentIntegrationTest.class,
        ActorTests.class,
        DataInstanceIntegrationTest.class
})
public class ProcessTests {

}
