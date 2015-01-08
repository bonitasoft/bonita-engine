package org.bonitasoft.engine.process;

import org.bonitasoft.engine.process.actor.ActorTests;
import org.bonitasoft.engine.process.comment.CommentIT;
import org.bonitasoft.engine.process.data.ActivityDataDefinitionIT;
import org.bonitasoft.engine.process.data.ActivityDataInstanceIT;
import org.bonitasoft.engine.process.data.ProcessDataDefinitionIT;
import org.bonitasoft.engine.process.data.ProcessDataInstanceIT;
import org.bonitasoft.engine.process.document.DocumentIT;
import org.bonitasoft.engine.process.instance.ProcessInstanceTests;
import org.bonitasoft.engine.process.task.TaskTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        ProcessResolutionIT.class,
        ProcessExecutionIT.class,
        ProcessManagementIT.class,
        ProcessDeploymentIT.class,
        ProcessDescriptionIT.class,
        GetProcessDefinitionIT.class,
        GatewayExecutionIT.class,
        ProcessCategoryIT.class,
        ProcessInstanceTests.class,
        ProcessWithExpressionIT.class,
        StartProcessWithOperationsIT.class,
        ProcessDeletionIT.class,
        EvaluateExpressionIT.class,
        TaskTests.class,
        CommentIT.class,
        DocumentIT.class,
        ActorTests.class,
        ActivityDataInstanceIT.class,
        ActivityDataDefinitionIT.class,
        ProcessDataInstanceIT.class,
        ProcessDataDefinitionIT.class
})
public class ProcessTests {

}
