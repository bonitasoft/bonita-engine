package org.bonitasoft.engine.event;

import static org.junit.Assert.assertFalse;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.BoundaryEventDefinition;
import org.bonitasoft.engine.bpm.flownode.LoopActivityInstance;
import org.bonitasoft.engine.bpm.flownode.SignalEventTriggerDefinition;
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
import org.bonitasoft.engine.test.wait.WaitForStep;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SignalBoundaryEventTest extends CommonAPITest {

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

    private ProcessDefinition deployProcessWithBoundaryEvent(final String signalName) throws BonitaException {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("pSignalBoundary", "2.0");
        final String actorName = "delivery";
        processDefinitionBuilder.addActor(actorName);
        processDefinitionBuilder.addStartEvent("start");
        final UserTaskDefinitionBuilder userTaskDefinitionBuilder = processDefinitionBuilder.addUserTask("step1", actorName);
        userTaskDefinitionBuilder.addBoundaryEvent("waitSignal", true).addSignalEventTrigger(signalName);
        userTaskDefinitionBuilder.addUserTask("exceptionStep", actorName);
        userTaskDefinitionBuilder.addUserTask("step2", actorName);
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", "step1");
        processDefinitionBuilder.addTransition("step1", "step2");
        processDefinitionBuilder.addTransition("waitSignal", "exceptionStep");
        return deployAndEnableProcessWithActor(processDefinitionBuilder.done(), actorName, donaBenta);
    }

    @Test
    @Cover(classes = { SignalEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "signal", "boundary",
            "event" }, jira = "ENGINE-502", story = "signal sent on a user task having a boundary catch signal event")
    public void signalBoundaryEventTriggered() throws Exception {
        final ProcessDefinition processDefinition = deployProcessWithBoundaryEvent("MySignal");

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance step1 = waitForUserTask("step1", processInstance);
        Thread.sleep(50);
        getProcessAPI().sendSignal("MySignal");

        final ActivityInstance executionStep = waitForUserTask("exceptionStep", processInstance);
        assignAndExecuteStep(executionStep, donaBenta.getId());

        waitForProcessToFinish(processInstance);
        waitForArchivedActivity(step1.getId(), TestStates.getAbortedState());

        checkWasntExecuted(processInstance, "step2");

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    @Cover(classes = { SignalEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "signal", "boundary",
            "event" }, jira = "ENGINE-502", story = "signal with wrong name sent on a user task having a boundary catch signal event")
    public void signalBoundaryEventNotTriggered() throws Exception {
        final ProcessDefinition processDefinition = deployProcessWithBoundaryEvent("MySignal1");

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt("step1", processInstance, donaBenta);
        final ActivityInstance waitForUserTask = waitForUserTask("step2", processInstance);

        // Thread.sleep(500);
        getProcessAPI().sendSignal("MySignal1");

        final WaitForStep waitForExceptionStep = new WaitForStep(50, 1000, "exceptionStep", processInstance.getId(), TestStates.getReadyState(),
                getProcessAPI());
        assertFalse(waitForExceptionStep.waitUntil());

        assignAndExecuteStep(waitForUserTask, donaBenta.getId());

        waitForProcessToFinish(processInstance);
        checkWasntExecuted(processInstance, "exceptionStep");

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    @Cover(classes = { SignalEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "signal", "boundary",
            "event" }, jira = "ENGINE-502", story = "signal sent on a call activity having a boundary catch signal event")
    public void signalBoundaryEventOnCallActivityTriggered() throws Exception {
        final String signalName = "MySignal";
        final String actorName = "delivery";
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundarySignalEventOnCallActivity(signalName, actorName);
        final ProcessDefinition calledProcessDefinition = deployAndEnableSimpleProcess(actorName);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance calledStep = waitForUserTask("calledStep", processInstance);
        final ProcessInstance calledProcessInstance = getProcessAPI().getProcessInstance(calledStep.getParentProcessInstanceId());

        getProcessAPI().sendSignal("MySignal");

        final ActivityInstance executionStep = waitForUserTask("exceptionStep", processInstance);
        assignAndExecuteStep(executionStep, donaBenta.getId());

        waitForProcessToFinish(calledProcessInstance, TestStates.getAbortedState());
        waitForProcessToFinish(processInstance);

        waitForArchivedActivity(calledStep.getId(), TestStates.getAbortedState());

        checkWasntExecuted(processInstance, "step2");

        disableAndDeleteProcess(processDefinition);
        disableAndDeleteProcess(calledProcessDefinition);
    }

    @Test
    @Cover(classes = { SignalEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "signal", "boundary",
            "event" }, jira = "ENGINE-502", story = "signal sent on a call activity having a boundary catch signal event")
    public void signalBoundaryEventOnCallActivityNotTriggered() throws Exception {
        final String signalName = "MySignal";
        final String actorName = "delivery";
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundarySignalEventOnCallActivity(signalName, actorName);
        final ProcessDefinition calledProcessDefinition = deployAndEnableSimpleProcess(actorName);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance calledStep = waitForUserTask("calledStep", processInstance);
        final ProcessInstance calledProcessInstance = getProcessAPI().getProcessInstance(calledStep.getParentProcessInstanceId());
        assignAndExecuteStep(calledStep, donaBenta.getId());

        final ActivityInstance step2 = waitForUserTask("step2", processInstance);
        waitForProcessToFinish(calledProcessInstance);

        getProcessAPI().sendSignal("MySignal");

        final WaitForStep waitForExceptionStep = new WaitForStep(50, 1000, "exceptionStep", processInstance.getId(), TestStates.getReadyState(),
                getProcessAPI());
        assertFalse(waitForExceptionStep.waitUntil());

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

    protected ProcessDefinition deployAndEnableProcessWithBoundarySignalEventOnCallActivity(final String signalName, final String actorName)
            throws BonitaException {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("pSignalBoundary", "2.0");
        processDefinitionBuilder.addActor(actorName);
        processDefinitionBuilder.addStartEvent("start");
        final CallActivityBuilder callActivityBuilder = processDefinitionBuilder.addCallActivity("step1",
                new ExpressionBuilder().createConstantStringExpression("calledProcess"), new ExpressionBuilder().createConstantStringExpression("1.0"));
        callActivityBuilder.addBoundaryEvent("signal", true).addSignalEventTrigger(signalName);
        processDefinitionBuilder.addUserTask("exceptionStep", actorName);
        processDefinitionBuilder.addUserTask("step2", actorName);
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", "step1");
        processDefinitionBuilder.addTransition("step1", "step2");
        processDefinitionBuilder.addTransition("step2", "end");
        processDefinitionBuilder.addTransition("signal", "exceptionStep");
        return deployAndEnableProcessWithActor(processDefinitionBuilder.done(), actorName, donaBenta);
    }

    private ProcessDefinition deployAndEnableProcessWithBoundarySignalEventOnMultiInstance(final int loopCardinality, final boolean isSequential)
            throws BonitaException {
        final String actorName = "delivery";

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("processWithBoundarySignalEventAndMultiInstance",
                "1.0");
        processBuilder.addActor(actorName).addStartEvent("start");

        final UserTaskDefinitionBuilder userTaskBuilder = processBuilder.addUserTask("step1", actorName);
        userTaskBuilder.addMultiInstance(isSequential, new ExpressionBuilder().createConstantIntegerExpression(loopCardinality));
        userTaskBuilder.addBoundaryEvent("signal", true).addSignalEventTrigger("MySignal");

        processBuilder.addUserTask("step2", actorName).addUserTask("exceptionStep", actorName).addEndEvent("end").addTransition("start", "step1")
                .addTransition("step1", "step2").addTransition("step2", "end").addTransition("signal", "exceptionStep");

        return deployAndEnableProcessWithActor(processBuilder.done(), actorName, donaBenta);
    }

    @Cover(classes = { SignalEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Signal",
            "Boundary", "Multi-instance", "Sequential" }, story = "Execute signal boundary event triggered on sequential multi-instance.", jira = "ENGINE-547")
    @Test
    public void signalBoundaryEventTriggeredOnSequentialMultiInstance() throws Exception {
        final int loopCardinality = 4;
        final boolean isSequential = true;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundarySignalEventOnMultiInstance(loopCardinality, isSequential);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final ActivityInstance step1 = waitForUserTask("step1", processInstance);
        Thread.sleep(50);
        getProcessAPI().sendSignal("MySignal");

        final ActivityInstance executionStep = waitForUserTask("exceptionStep", processInstance);
        assignAndExecuteStep(executionStep, donaBenta.getId());

        waitForProcessToFinish(processInstance);
        waitForArchivedActivity(step1.getId(), TestStates.getAbortedState());

        checkWasntExecuted(processInstance, "step2");

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { SignalEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Signal",
            "Boundary", "Multi-instance", "Sequential" }, story = "Execute signal boundary event not triggered on sequential multi-instance", jira = "ENGINE-547")
    @Test
    public void signalBoundaryEventNotTriggeredOnSequentialMultiInstance() throws Exception {
        final int loopCardinality = 3;
        final boolean isSequential = true;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundarySignalEventOnMultiInstance(loopCardinality, isSequential);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        for (int i = 0; i < loopCardinality; i++) {
            waitForUserTaskAndExecuteIt("step1", processInstance.getId(), donaBenta.getId());
        }
        final ActivityInstance waitForUserTask = waitForUserTask("step2", processInstance);

        getProcessAPI().sendSignal("MySignal1");

        final WaitForStep waitForExceptionStep = new WaitForStep(50, 1000, "exceptionStep", processInstance.getId(), TestStates.getReadyState(),
                getProcessAPI());
        assertFalse(waitForExceptionStep.waitUntil());

        assignAndExecuteStep(waitForUserTask, donaBenta.getId());

        waitForProcessToFinish(processInstance);
        checkWasntExecuted(processInstance, "exceptionStep");

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { SignalEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Signal",
            "Boundary", "Multi-instance", "Sequential" }, story = "Execute signal boundary event triggered on parallel multi-instance.", jira = "ENGINE-547")
    @Test
    public void signalBoundaryEventTriggeredOnParallelMultiInstance() throws Exception {
        final int loopCardinality = 4;
        final boolean isSequential = false;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundarySignalEventOnMultiInstance(loopCardinality, isSequential);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final ActivityInstance step1 = waitForUserTask("step1", processInstance);
        Thread.sleep(50);
        getProcessAPI().sendSignal("MySignal");

        final ActivityInstance executionStep = waitForUserTask("exceptionStep", processInstance);
        assignAndExecuteStep(executionStep, donaBenta.getId());

        waitForProcessToFinish(processInstance);
        waitForArchivedActivity(step1.getId(), TestStates.getAbortedState());

        checkWasntExecuted(processInstance, "step2");

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { SignalEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Signal",
            "Boundary", "Multi-instance", "Sequential" }, story = "Execute signal boundary event not triggered on parallel multi-instance.", jira = "ENGINE-547")
    @Test
    public void signalBoundaryEventNotTriggeredOnParallelMultiInstance() throws Exception {
        final int loopCardinality = 3;
        final boolean isSequential = false;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundarySignalEventOnMultiInstance(loopCardinality, isSequential);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        for (int i = 0; i < loopCardinality; i++) {
            waitForUserTaskAndExecuteIt("step1", processInstance.getId(), donaBenta.getId());
        }
        final ActivityInstance waitForUserTask = waitForUserTask("step2", processInstance);

        getProcessAPI().sendSignal("MySignal1");

        final WaitForStep waitForExceptionStep = new WaitForStep(50, 1000, "exceptionStep", processInstance.getId(), TestStates.getReadyState(),
                getProcessAPI());
        assertFalse(waitForExceptionStep.waitUntil());

        assignAndExecuteStep(waitForUserTask, donaBenta.getId());

        waitForProcessToFinish(processInstance);
        checkWasntExecuted(processInstance, "exceptionStep");

        disableAndDeleteProcess(processDefinition);
    }

    private ProcessDefinition deployAndEnableProcessWithBoundarySignalEventOnLoopActivity(final int loopMax) throws BonitaException {
        final String actorName = "delivery";
        final Expression condition = new ExpressionBuilder().createConstantBooleanExpression(true);

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("processWithMultiInstanceAndBoundaryEvent", "1.0");
        processBuilder.addActor(actorName).addStartEvent("start");

        final UserTaskDefinitionBuilder userTaskBuilder = processBuilder.addUserTask("step1", actorName);
        userTaskBuilder.addLoop(false, condition, new ExpressionBuilder().createConstantIntegerExpression(loopMax));
        userTaskBuilder.addBoundaryEvent("signal", true).addSignalEventTrigger("MySignal");

        processBuilder.addUserTask("step2", actorName).addUserTask("exceptionStep", actorName).addEndEvent("end").addTransition("start", "step1")
                .addTransition("step1", "step2").addTransition("step2", "end").addTransition("signal", "exceptionStep");

        return deployAndEnableProcessWithActor(processBuilder.done(), actorName, donaBenta);
    }

    @Cover(classes = { SignalEventTriggerDefinition.class, LoopActivityInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Signal",
            "Boundary", "Loop activity" }, story = "Execute signal boundary event triggered on loop activity", jira = "ENGINE-547")
    @Test
    public void signalBoundaryEventTriggeredOnLoopActivity() throws Exception {
        final int loopMax = 3;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundarySignalEventOnLoopActivity(loopMax);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final ActivityInstance step1 = waitForUserTask("step1", processInstance);
        Thread.sleep(50);
        getProcessAPI().sendSignal("MySignal");

        final ActivityInstance executionStep = waitForUserTask("exceptionStep", processInstance);
        assignAndExecuteStep(executionStep, donaBenta.getId());

        waitForProcessToFinish(processInstance);
        waitForArchivedActivity(step1.getId(), TestStates.getAbortedState());

        checkWasntExecuted(processInstance, "step2");

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { SignalEventTriggerDefinition.class, LoopActivityInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Signal",
            "Boundary", "Loop activity" }, story = "Execute signal boundary event not triggered on loop activity", jira = "ENGINE-547")
    @Test
    public void signalBoundaryEventNotTriggeredOnLoopActivity() throws Exception {
        final int loopMax = 2;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundarySignalEventOnLoopActivity(loopMax);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        for (int i = 0; i < loopMax; i++) {
            waitForUserTaskAndExecuteIt("step1", processInstance.getId(), donaBenta.getId());
        }
        final ActivityInstance waitForUserTask = waitForUserTask("step2", processInstance);

        getProcessAPI().sendSignal("MySignal1");

        final WaitForStep waitForExceptionStep = new WaitForStep(50, 1000, "exceptionStep", processInstance.getId(), TestStates.getReadyState(),
                getProcessAPI());
        assertFalse(waitForExceptionStep.waitUntil());

        assignAndExecuteStep(waitForUserTask, donaBenta.getId());

        waitForProcessToFinish(processInstance);
        checkWasntExecuted(processInstance, "exceptionStep");

        disableAndDeleteProcess(processDefinition);
    }

}
