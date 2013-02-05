package com.bonitasoft.engine.activity;

import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.bpm.model.HumanTaskInstance;
import org.bonitasoft.engine.bpm.model.ProcessDefinition;
import org.bonitasoft.engine.bpm.model.ProcessInstance;
import org.bonitasoft.engine.bpm.model.TaskPriority;
import org.bonitasoft.engine.connectors.VariableStorage;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionConstants;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.process.TestStates;
import org.bonitasoft.engine.wait.CheckNbPendingTaskOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.bpm.model.ProcessDefinitionBuilder;

public class MultiInstanceTest extends CommonAPISPTest {

    private static final String JOHN = "john";

    private User john;

    @Before
    public void beforeTest() throws BonitaException {
        login();
        john = createUser(JOHN, "bpm");
        logout();
        loginWith(JOHN, "bpm");
    }

    @After
    public void afterTest() throws BonitaException {
        deleteUser(JOHN);
        VariableStorage.clearAll();
        logout();
    }

    @Test
    public void childOfRemainingInstancesAreAbortedAfterCompletionCondition() throws Exception {
        final String delivery = "Delivery men";

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder()
                .createNewInstance("remainingInstancesAreAbortedAfterCompletionCondition", "1.0");
        builder.addActor(delivery).addDescription("Delivery all day and night long");
        final int loopMax = 3;
        builder.addUserTask("step1", delivery)
                .addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(3))
                .addCompletionCondition(
                        new ExpressionBuilder().createGroovyScriptExpression("childOfRemainingInstancesAreAbortedAfterCompletionCondition",
                                ExpressionConstants.NUMBER_OF_COMPLETED_INSTANCES.getEngineName() + " == 1 ", Boolean.class.getName(),
                                new ExpressionBuilder().createEngineConstant(ExpressionConstants.NUMBER_OF_COMPLETED_INSTANCES)));

        final ProcessDefinition processDefinition = deployAndEnableWithActor(builder.done(), delivery, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        checkPendingTaskWithChildrenInParallel(loopMax, 1, processInstance);
        disableAndDelete(processDefinition);
    }

    private void checkPendingTaskWithChildrenInParallel(final int numberOfTask, final int numberOfTaskToCompleteMI, final ProcessInstance processInstance)
            throws Exception {
        final CheckNbPendingTaskOf checkNbPendingTaskOf = new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, false, numberOfTask, john);
        final boolean waitUntil = checkNbPendingTaskOf.waitUntil();
        assertTrue("expected " + numberOfTask + " pending task but was " + checkNbPendingTaskOf.getPendingHumanTaskInstances().size(), waitUntil);
        final List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, numberOfTask, null);
        for (int i = 0; i < numberOfTask; i++) {
            final HumanTaskInstance pendingTask = pendingTasks.get(i);
            getProcessAPI().assignUserTask(pendingTask.getId(), john.getId());
            getProcessAPI().addManualUserTask(pendingTask.getId(), "manual1" + i, "manual1" + i, john.getId(), "manual task", new Date(), null);
            getProcessAPI().addManualUserTask(pendingTask.getId(), "manual2" + i, "manual2" + i, john.getId(), "manual task", new Date(), TaskPriority.NORMAL);
        }

        for (int i = 0; i < numberOfTaskToCompleteMI; i++) {
            executeAssignedTaskUntilEnd(pendingTasks.get(i).getId());
        }
        Thread.sleep(200);
        assertTrue("There was still pending task but no more was expected", new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, false, 0, john).waitUntil());
        final int nbAbortedActivities = (numberOfTask - numberOfTaskToCompleteMI) * 3 + numberOfTaskToCompleteMI * 2; // parent and 2 children for non completed
                                                                                                                      // tasks + 2 children for completed one
        assertTrue("process was not finished", new WaitProcessToFinishAndBeArchived(50, 7000, false, processInstance, getProcessAPI()).waitUntil());
        final CheckNbOfArchivedActivities checkNbOfActivities = new CheckNbOfArchivedActivities(getProcessAPI(), 100, 5000, true, processInstance,
                nbAbortedActivities, TestStates.getAbortedState());
        final boolean waitUntilAborted = checkNbOfActivities.waitUntil();
        assertTrue("Expected " + nbAbortedActivities + " in the aboterd state. But was " + checkNbOfActivities.getResult().size(), waitUntilAborted);
    }
}
