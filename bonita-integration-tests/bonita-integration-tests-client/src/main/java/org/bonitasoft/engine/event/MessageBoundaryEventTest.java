package org.bonitasoft.engine.event;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.BoundaryEventDefinition;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.MessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.CallActivityBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MessageBoundaryEventTest extends CommonAPITest {

    private static final String BOUNDARY_NAME = "waitMessage";

    private User donaBenta;

    @Before
    public void beforeTest() throws BonitaException {
         loginOnDefaultTenantWithDefaultTechnicalUser();
        donaBenta = createUser(USERNAME, PASSWORD);
        logoutOnTenant();
        loginOnDefaultTenantWith(USERNAME, PASSWORD);
    }

    @After
    public void afterTest() throws BonitaException {
        logoutOnTenant();
         loginOnDefaultTenantWithDefaultTechnicalUser();
        deleteUser(donaBenta.getId());
        logoutOnTenant();
    }

    private ProcessDefinition deployProcessWithBoundaryEvent(final String message) throws BonitaException {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("pMessageBoundary", "2.0");
        final String actorName = "delivery";
        processDefinitionBuilder.addActor(actorName);
        processDefinitionBuilder.addStartEvent("start");
        final UserTaskDefinitionBuilder userTaskDefinitionBuilder = processDefinitionBuilder.addUserTask("step1", actorName);
        userTaskDefinitionBuilder.addBoundaryEvent(BOUNDARY_NAME, true).addMessageEventTrigger(message);
        userTaskDefinitionBuilder.addUserTask("exceptionStep", actorName);
        userTaskDefinitionBuilder.addUserTask("step2", actorName);
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", "step1");
        processDefinitionBuilder.addTransition("step1", "step2");
        processDefinitionBuilder.addTransition(BOUNDARY_NAME, "exceptionStep");

        return deployAndEnableProcessWithActor(processDefinitionBuilder.done(), actorName, donaBenta);
    }

    @Test
    @Cover(classes = { MessageEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "message", "boundary",
            "event" }, jira = "ENGINE-499", story = "message sent on a user task having a boundary catch message event")
    public void testMessageBoundaryEventTriggered() throws Exception {
        final ProcessDefinition processDefinition = deployProcessWithBoundaryEvent("MyMessage");

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance step1 = waitForUserTask("step1", processInstance.getId());

        getProcessAPI().sendMessage("MyMessage", new ExpressionBuilder().createConstantStringExpression("pMessageBoundary"),
                new ExpressionBuilder().createConstantStringExpression(BOUNDARY_NAME), null);
        final ActivityInstance executionStep = waitForUserTask("exceptionStep", processInstance.getId());
        assignAndExecuteStep(executionStep, donaBenta.getId());

        waitForProcessToFinish(processInstance);

        waitForArchivedActivity(step1.getId(), TestStates.getAbortedState());

        checkWasntExecuted(processInstance, "step2");

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    @Cover(classes = { MessageEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "message", "boundary",
            "event", "call activity" }, jira = "ENGINE-499", story = "message with wrong name sent on a user task having a boundary catch message event")
    public void testMessageBoundaryEventNotTriggered() throws Exception {
        final ProcessDefinition processDefinition = deployProcessWithBoundaryEvent("MyMessage1");

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt("step1", processInstance, donaBenta);
        final ActivityInstance waitForUserTask = waitForUserTask("step2", processInstance.getId());

        getProcessAPI().sendMessage("MyMessage1", new ExpressionBuilder().createConstantStringExpression("pMessageBoundary"),
                new ExpressionBuilder().createConstantStringExpression("step1"), null);

        assignAndExecuteStep(waitForUserTask, donaBenta.getId());
        checkWasntExecuted(processInstance, "exceptionStep");

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    @Cover(classes = { MessageEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "message", "boundary",
            "event", "call activity" }, jira = "ENGINE-499", story = "message sent on a call activity having a boundary catch message event")
    public void testMessageBoundaryEventOnCallActivityTriggered() throws Exception {
        final String actorName = "delivery";
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryOnCallActivity(actorName);
        final ProcessDefinition calledProcessDefinition = deployAndEnableSimpleProcess(actorName);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance calledStep = waitForUserTask("calledStep", processInstance.getId());
        final ProcessInstance calledProcessInstance = getProcessAPI().getProcessInstance(calledStep.getParentProcessInstanceId());

        getProcessAPI().sendMessage("MyMessage", new ExpressionBuilder().createConstantStringExpression("pMessageBoundary"),
                new ExpressionBuilder().createConstantStringExpression(BOUNDARY_NAME), null);

        waitForUserTaskAndExecuteIt("exceptionStep", processInstance, donaBenta);
        waitForProcessToFinish(calledProcessInstance, TestStates.getAbortedState());
        waitForProcessToFinish(processInstance);

        waitForArchivedActivity(calledStep.getId(), TestStates.getAbortedState());

        checkWasntExecuted(processInstance, "step2");

        disableAndDeleteProcess(processDefinition);
        disableAndDeleteProcess(calledProcessDefinition);
    }

    @Test
    @Cover(classes = { MessageEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "message", "boundary",
            "event", "call activity" }, jira = "ENGINE-499", story = "message sent on a call activity having a boundary catch message event")
    public void testMessageBoundaryEventOnCallActivityNotTriggered() throws Exception {
        final String actorName = "delivery";
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryOnCallActivity(actorName);
        final ProcessDefinition calledProcessDefinition = deployAndEnableSimpleProcess(actorName);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance calledStep = waitForUserTask("calledStep", processInstance.getId());
        final ProcessInstance calledProcessInstance = getProcessAPI().getProcessInstance(calledStep.getParentProcessInstanceId());
        assignAndExecuteStep(calledStep, donaBenta.getId());

        final ActivityInstance step2 = waitForUserTask("step2", processInstance.getId());

        getProcessAPI().sendMessage("MyMessage", new ExpressionBuilder().createConstantStringExpression("pMessageBoundary"),
                new ExpressionBuilder().createConstantStringExpression(BOUNDARY_NAME), null);

        waitForProcessToFinish(calledProcessInstance);
        assignAndExecuteStep(step2, donaBenta.getId());
        waitForProcessToFinish(processInstance);

        checkWasntExecuted(processInstance, "exceptionStep");

        disableAndDeleteProcess(processDefinition);
        disableAndDeleteProcess(calledProcessDefinition);
    }

    protected ProcessDefinition deployAndEnableSimpleProcess(final String actorName) throws BonitaException {
        final ProcessDefinitionBuilder calledProcess = new ProcessDefinitionBuilder().createNewInstance("calledProcess", "1.0");
        calledProcess.addActor(actorName);
        calledProcess.addStartEvent("start");
        calledProcess.addUserTask("calledStep", actorName);
        calledProcess.addEndEvent("end");
        calledProcess.addTransition("start", "calledStep");
        calledProcess.addTransition("calledStep", "end");
        return deployAndEnableProcessWithActor(calledProcess.done(), actorName, donaBenta);
    }

    protected ProcessDefinition deployAndEnableProcessWithBoundaryOnCallActivity(final String actorName) throws BonitaException {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("pMessageBoundary", "2.0");
        processDefinitionBuilder.addActor(actorName);
        processDefinitionBuilder.addStartEvent("start");
        final CallActivityBuilder callActivityBuilder = processDefinitionBuilder.addCallActivity("step1",
                new ExpressionBuilder().createConstantStringExpression("calledProcess"), new ExpressionBuilder().createConstantStringExpression("1.0"));
        callActivityBuilder.addBoundaryEvent(BOUNDARY_NAME, true).addMessageEventTrigger("MyMessage");
        processDefinitionBuilder.addUserTask("exceptionStep", actorName);
        processDefinitionBuilder.addUserTask("step2", actorName);
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", "step1");
        processDefinitionBuilder.addTransition("step1", "step2");
        processDefinitionBuilder.addTransition(BOUNDARY_NAME, "exceptionStep");
        return deployAndEnableProcessWithActor(processDefinitionBuilder.done(), actorName, donaBenta);
    }

    private ProcessDefinition deployAndEnableProcessWithBoundaryMessageEventOnMultiInstance(final int loopCardinality, final boolean isSequential)
            throws BonitaException {
        final String actorName = "delivery";

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("processWithBoundaryMessageEventAndMultiInstance",
                "1.0");
        processBuilder.addActor(actorName).addStartEvent("start");

        final UserTaskDefinitionBuilder userTaskBuilder = processBuilder.addUserTask("step1", actorName);
        userTaskBuilder.addMultiInstance(isSequential, new ExpressionBuilder().createConstantIntegerExpression(loopCardinality));
        userTaskBuilder.addBoundaryEvent(BOUNDARY_NAME, true).addMessageEventTrigger("MyMessage");

        processBuilder.addUserTask("step2", actorName).addUserTask("exceptionStep", actorName).addEndEvent("end").addTransition("start", "step1")
                .addTransition("step1", "step2").addTransition("step2", "end").addTransition(BOUNDARY_NAME, "exceptionStep");

        return deployAndEnableProcessWithActor(processBuilder.done(), actorName, donaBenta);
    }

    @Cover(classes = { MessageEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message",
            "Boundary", "Multi-instance", "Sequential" }, story = "Execute message boundary event triggered on sequential multi-instance.", jira = "ENGINE-547")
    @Test
    public void testMessageBoundaryEventTriggeredOnSequentialMultiInstance() throws Exception {
        final int loopCardinality = 4;
        final boolean isSequential = true;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryMessageEventOnMultiInstance(loopCardinality, isSequential);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance step1 = waitForUserTask("step1", processInstance.getId());

        getProcessAPI().sendMessage("MyMessage", new ExpressionBuilder().createConstantStringExpression("processWithBoundaryMessageEventAndMultiInstance"),
                new ExpressionBuilder().createConstantStringExpression(BOUNDARY_NAME), null);
        final ActivityInstance executionStep = waitForUserTask("exceptionStep", processInstance.getId());
        assignAndExecuteStep(executionStep, donaBenta.getId());

        waitForProcessToFinish(processInstance);

        waitForArchivedActivity(step1.getId(), TestStates.getAbortedState());

        checkWasntExecuted(processInstance, "step2");

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { MessageEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message",
            "Boundary", "Multi-instance", "Sequential" }, story = "Execute message boundary event not triggered on sequential multi-instance", jira = "ENGINE-547")
    @Test
    public void testMessageBoundaryEventNotTriggeredOnSequentialMultiInstance() throws Exception {
        final int loopCardinality = 3;
        final boolean isSequential = true;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryMessageEventOnMultiInstance(loopCardinality, isSequential);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        for (int i = 0; i < loopCardinality; i++) {
            waitForUserTaskAndExecuteIt("step1", processInstance, donaBenta);
        }
        final ActivityInstance waitForUserTask = waitForUserTask("step2", processInstance.getId());

        getProcessAPI().sendMessage("MyMessage1", new ExpressionBuilder().createConstantStringExpression("pMessageBoundary"),
                new ExpressionBuilder().createConstantStringExpression("step1"), null);

        assignAndExecuteStep(waitForUserTask, donaBenta.getId());
        waitForProcessToFinish(processInstance);
        checkWasntExecuted(processInstance, "exceptionStep");

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { MessageEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message",
            "Boundary", "Multi-instance", "Sequential" }, story = "Execute message boundary event triggered on parallel multi-instance.", jira = "ENGINE-547")
    @Test
    public void testMessageBoundaryEventTriggeredOnParallelMultiInstance() throws Exception {
        final int loopCardinality = 4;
        final boolean isSequential = false;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryMessageEventOnMultiInstance(loopCardinality, isSequential);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final List<HumanTaskInstance> pendingTasks = waitForPendingTasks(donaBenta, loopCardinality);
        for (final HumanTaskInstance humanTaskInstance : pendingTasks) {
            assertEquals("step1", humanTaskInstance.getName());
        }

        getProcessAPI().sendMessage("MyMessage", new ExpressionBuilder().createConstantStringExpression("processWithBoundaryMessageEventAndMultiInstance"),
                new ExpressionBuilder().createConstantStringExpression(BOUNDARY_NAME), null);
        final ActivityInstance executionStep = waitForUserTask("exceptionStep", processInstance.getId());
        assignAndExecuteStep(executionStep, donaBenta.getId());

        waitForProcessToFinish(processInstance);

        for (final HumanTaskInstance humanTaskInstance : pendingTasks) {
            waitForArchivedActivity(humanTaskInstance.getId(), TestStates.getAbortedState());
        }

        checkWasntExecuted(processInstance, "step2");

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { MessageEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message",
            "Boundary", "Multi-instance", "Sequential" }, story = "Execute message boundary event not triggered on parallel multi-instance.", jira = "ENGINE-547")
    @Test
    public void testMessageBoundaryEventNotTriggeredOnParallelMultiInstance() throws Exception {
        final int loopCardinality = 3;
        final boolean isSequential = false;

        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryMessageEventOnMultiInstance(loopCardinality, isSequential);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        for (int i = 0; i < loopCardinality; i++) {
            waitForUserTaskAndExecuteIt("step1", processInstance, donaBenta);
        }
        final ActivityInstance waitForUserTask = waitForUserTask("step2", processInstance.getId());

        getProcessAPI().sendMessage("MyMessage1", new ExpressionBuilder().createConstantStringExpression("processWithMultiInstanceAndBoundaryEvent"),
                new ExpressionBuilder().createConstantStringExpression("step1"), null);

        assignAndExecuteStep(waitForUserTask, donaBenta.getId());
        checkWasntExecuted(processInstance, "exceptionStep");

        waitForProcessToFinish(processInstance);

        disableAndDeleteProcess(processDefinition);
    }

    private ProcessDefinition deployAndEnableProcessWithBoundaryMessageEventOnLoopActivity(final int loopMax) throws BonitaException,
            InvalidProcessDefinitionException {
        final String actorName = "delivery";
        final Expression condition = new ExpressionBuilder().createConstantBooleanExpression(true);

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("processWithLoopActivityAndBoundaryEvent", "1.0");
        processBuilder.addActor(actorName).addStartEvent("start");

        final UserTaskDefinitionBuilder userTaskBuilder = processBuilder.addUserTask("step1", actorName);
        userTaskBuilder.addLoop(false, condition, new ExpressionBuilder().createConstantIntegerExpression(loopMax));
        userTaskBuilder.addBoundaryEvent(BOUNDARY_NAME, true).addMessageEventTrigger("MyMessage");

        processBuilder.addUserTask("step2", actorName).addUserTask("exceptionStep", actorName).addEndEvent("end").addTransition("start", "step1")
                .addTransition("step1", "step2").addTransition("step2", "end").addTransition(BOUNDARY_NAME, "exceptionStep");

        return deployAndEnableProcessWithActor(processBuilder.done(), actorName, donaBenta);
    }

    @Cover(classes = { MessageEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message",
            "Boundary", "Loop activity" }, story = "Execute message boundary event triggered on loop activity", jira = "ENGINE-547")
    @Test
    public void testMessageBoundaryEventTriggeredOnLoopActivity() throws Exception {
        final int loopMax = 3;

        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryMessageEventOnLoopActivity(loopMax);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance step1 = waitForUserTask("step1", processInstance.getId());

        getProcessAPI().sendMessage("MyMessage", new ExpressionBuilder().createConstantStringExpression("processWithLoopActivityAndBoundaryEvent"),
                new ExpressionBuilder().createConstantStringExpression(BOUNDARY_NAME), null);
        final ActivityInstance executionStep = waitForUserTask("exceptionStep", processInstance.getId());
        assignAndExecuteStep(executionStep, donaBenta.getId());

        waitForProcessToFinish(processInstance);

        waitForArchivedActivity(step1.getId(), TestStates.getAbortedState());

        checkWasntExecuted(processInstance, "step2");

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { MessageEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message",
            "Boundary", "Loop activity" }, story = "Execute message boundary event not triggered on loop activity", jira = "ENGINE-547")
    @Test
    public void testMessageBoundaryEventNotTriggeredOnLoopActivity() throws Exception {
        final int loopMax = 2;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryMessageEventOnLoopActivity(loopMax);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        for (int i = 0; i < loopMax; i++) {
            waitForUserTaskAndExecuteIt("step1", processInstance.getId(), donaBenta.getId());
        }
        final ActivityInstance waitForUserTask = waitForUserTask("step2", processInstance.getId());

        getProcessAPI().sendMessage("MyMessage1", new ExpressionBuilder().createConstantStringExpression("processWithLoopActivityAndBoundaryEvent"),
                new ExpressionBuilder().createConstantStringExpression("step1"), null);

        assignAndExecuteStep(waitForUserTask, donaBenta.getId());
        waitForProcessToFinish(processInstance);
        checkWasntExecuted(processInstance, "exceptionStep");

        disableAndDeleteProcess(processDefinition);
    }

}
