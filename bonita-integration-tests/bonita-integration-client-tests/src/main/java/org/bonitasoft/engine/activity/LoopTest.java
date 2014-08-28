package org.bonitasoft.engine.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.ProcessManagementAPI;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedLoopActivityInstance;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.connectors.VariableStorage;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionConstants;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.bonitasoft.engine.test.check.CheckNbPendingTaskOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Matthieu Chaffotte
 */
public class LoopTest extends CommonAPITest {

    private User john;

    @Before
    public void beforeTest() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        john = createUser(USERNAME, PASSWORD);
        logoutOnTenant();
        loginOnDefaultTenantWith(USERNAME, PASSWORD);
    }

    @After
    public void afterTest() throws BonitaException {
        deleteUser(USERNAME);
        VariableStorage.clearAll();
        logoutOnTenant();
    }

    @Test
    public void executeAStandardLoopUserTaskWhichDoesNotLoop() throws Exception {
        final Expression condition = new ExpressionBuilder().createConstantBooleanExpression(false);

        final String delivery = "Delivery men";

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAStandardLoopUserTaskWhichDoesNotLoop", "1.0");
        builder.addActor(delivery).addDescription("Delivery all day and night long");
        builder.addUserTask("step1", delivery).addLoop(true, condition);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), delivery, john);
        final ProcessInstance instance = getProcessAPI().startProcess(processDefinition.getId());
        assertTrue(waitForProcessToFinishAndBeArchived(instance));
        final List<ArchivedActivityInstance> archivedActivityInstances = getProcessAPI().getArchivedActivityInstances(instance.getId(), 0, 100,
                ActivityInstanceCriterion.NAME_ASC);
        assertEquals(2, archivedActivityInstances.size());
        for (final ArchivedActivityInstance archivedActivityInstance : archivedActivityInstances) {
            assertTrue(ArchivedLoopActivityInstance.class.isInstance(archivedActivityInstance));
        }

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void executeAStandardLoopUserTask() throws Exception {
        final Expression condition = new ExpressionBuilder().createConstantBooleanExpression(true);

        final String delivery = "Delivery men";

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAStandardLoopUserTask", "1.0");
        builder.addActor(delivery).addDescription("Delivery all day and night long");
        builder.addUserTask("step1", delivery).addLoop(false, condition);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), delivery, john);
        getProcessAPI().startProcess(processDefinition.getId());

        HumanTaskInstance step1 = waitForUserTask("step1");

        assignAndExecuteStep(step1, john.getId());

        waitForUserTask("step1");

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    @Cover(classes = { ProcessManagementAPI.class }, concept = BPMNConcept.EXPRESSIONS, keywords = { "expression context", "flow node container hierarchy" }, jira = "ENGINE-1848")
    public void evaluateExpressionsOnLoopUserTask() throws Exception {
        final String actorName = "Golf Players";

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("evaluateExpressionsOnLoopUserTask", "1.0");
        builder.addActor(actorName).addDescription("For Golf players only");
        final String activityName = "launch";
        builder.addStartEvent("dummy");
        builder.addUserTask(activityName, actorName).addLoop(false, new ExpressionBuilder().createConstantBooleanExpression(true));
        builder.addTransition("dummy", activityName);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), actorName, john);
        try {
            final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
            final ActivityInstance userTask = waitForUserTask(activityName, processInstance);
            final Map<Expression, Map<String, Serializable>> expressions = new HashMap<Expression, Map<String, Serializable>>();
            expressions.put(new ExpressionBuilder().createConstantBooleanExpression(true), new HashMap<String, Serializable>(0));
            getProcessAPI().evaluateExpressionsOnActivityInstance(userTask.getId(), expressions);
        } finally {
            disableAndDeleteProcess(processDefinition);
        }
    }

    @Test
    public void executeAStandardLoopWithMaxIteration() throws Exception {
        final Expression condition = new ExpressionBuilder().createConstantBooleanExpression(true);

        final String delivery = "Delivery men";

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAStandardLoopUserTask", "1.0");
        builder.addActor(delivery).addDescription("Delivery all day and night long");
        int loopMax = 3;
        builder.addIntegerData("loopMax", new ExpressionBuilder().createConstantIntegerExpression(loopMax));

        builder.addUserTask("step1", delivery).addLoop(false, condition, new ExpressionBuilder().createDataExpression("loopMax", Integer.class.getName()));

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), delivery, john);
        getProcessAPI().startProcess(processDefinition.getId());

        for (int i = 0; i < loopMax; i++) {
            assertTrue(new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, false, 1, john).waitUntil());
            final List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
            final HumanTaskInstance pendingTask = pendingTasks.get(0);

            assignAndExecuteStep(pendingTask, john.getId());
        }
        Thread.sleep(500);
        assertTrue(new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, false, 0, john).waitUntil());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void executeAStandardLoopWithConditionUsingLoopCounter() throws Exception {
        final Expression condition = new ExpressionBuilder().createGroovyScriptExpression("executeAStandardLoopWithConditionUsingLoopCounter",
                "loopCounter < 3", Boolean.class.getName(), Arrays.asList(new ExpressionBuilder().createEngineConstant(ExpressionConstants.LOOP_COUNTER)));

        final String delivery = "Delivery men";

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAStandardLoopUserTask", "1.0");
        builder.addActor(delivery).addDescription("Delivery all day and night long");
        final int loopMax = 3;
        builder.addUserTask("step1", delivery).addLoop(false, condition);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), delivery, john);
        getProcessAPI().startProcess(processDefinition.getId());

        for (int i = 0; i < loopMax; i++) {
            assertTrue(new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, false, 1, john).waitUntil());
            final List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
            final HumanTaskInstance pendingTask = pendingTasks.get(0);

            assignAndExecuteStep(pendingTask, john.getId());
        }
        Thread.sleep(500);
        assertTrue(new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, false, 0, john).waitUntil());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void executeAStandardLoopWithConditionUsingData() throws Exception {
        final Expression condition = new ExpressionBuilder().createGroovyScriptExpression("executeAStandardLoopWithConditionUsingData1", "myData < 3",
                Boolean.class.getName(), Arrays.asList(new ExpressionBuilder().createDataExpression("myData", Integer.class.getName())));

        final String delivery = "Delivery men";

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAStandardLoopUserTaskWithData", "1.0");
        builder.addActor(delivery).addDescription("Delivery all day and night long");
        final int loopMax = 3;
        builder.addData("myData", Integer.class.getName(), new ExpressionBuilder().createConstantIntegerExpression(0));
        builder.addUserTask("step1", delivery)
        .addLoop(false, condition)
        .addOperation(
                new LeftOperandBuilder().createNewInstance("myData").done(),
                OperatorType.ASSIGNMENT,
                "=",
                null,
                new ExpressionBuilder().createGroovyScriptExpression("executeAStandardLoopWithConditionUsingData1", "myData + 1",
                        Integer.class.getName(), Arrays.asList(new ExpressionBuilder().createDataExpression("myData", Integer.class.getName()))));

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), delivery, john);
        getProcessAPI().startProcess(processDefinition.getId());

        for (int i = 0; i < loopMax; i++) {
            assertTrue(new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, false, 1, john).waitUntil());
            final List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
            final HumanTaskInstance pendingTask = pendingTasks.get(0);

            assignAndExecuteStep(pendingTask, john.getId());
        }
        Thread.sleep(500);
        assertTrue(new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, false, 0, john).waitUntil());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void abortProcessWithActiveLoopActivity() throws Exception {
        // given
        final String loopName = "step1";
        final String userTaskName = "step2";
        final ProcessDefinition processDefinition = deployAndEnableProcessWithLoopAndUserTaskInPararallelAndTerminateEvent(loopName, userTaskName);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        waitForUserTask(loopName, processInstance.getId());
        // when
        waitForUserTaskAndExecuteIt(userTaskName, processInstance.getId(), john.getId());

        // then
        // executing the user task will terminate the process: the loop activity must be aborted
        waitForFlowNodeInState(processInstance, loopName, TestStates.getAbortedState(), true);
        waitForProcessToFinish(processInstance);
        disableAndDeleteProcess(processDefinition);
    }

    private ProcessDefinition deployAndEnableProcessWithLoopAndUserTaskInPararallelAndTerminateEvent(final String loopName, final String parallelTaskName)
            throws InvalidExpressionException, BonitaException, InvalidProcessDefinitionException {
        final Expression condition = new ExpressionBuilder().createConstantBooleanExpression(true);
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("My proc", "1.0");
        builder.addActor(delivery).addDescription("Delivery all day and night long");
        builder.addStartEvent("start");
        builder.addUserTask(loopName, delivery).addLoop(false, condition);
        builder.addUserTask(parallelTaskName, delivery);
        builder.addGateway("gateway", GatewayType.PARALLEL);
        builder.addEndEvent("terminate").addTerminateEventTrigger();
        builder.addTransition("start", "gateway");
        builder.addTransition("gateway", loopName);
        builder.addTransition("gateway", parallelTaskName);
        builder.addTransition(loopName, "terminate");
        builder.addTransition(parallelTaskName, "terminate");

        return  deployAndEnableProcessWithActor(builder.done(), delivery, john);
    }

}
