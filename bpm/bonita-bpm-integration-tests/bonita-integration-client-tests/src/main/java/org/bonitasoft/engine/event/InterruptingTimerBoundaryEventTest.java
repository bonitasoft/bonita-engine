package org.bonitasoft.engine.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.BoundaryEventInstance;
import org.bonitasoft.engine.bpm.flownode.CallActivityInstance;
import org.bonitasoft.engine.bpm.flownode.EventInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.LoopActivityInstance;
import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.flownode.impl.internal.MultiInstanceLoopCharacteristics;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

public class InterruptingTimerBoundaryEventTest extends AbstractTimerBoundaryEventTest {

    @Cover(classes = { EventInstance.class, BoundaryEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Boundary", "Timer",
    "Interrupting" }, story = "Execute timer boundary event triggerd.", jira = "ENGINE-500")
    @Test
    public void timerBoundaryEventTriggered() throws Exception {
        final int timerDuration = 1000;
        final ProcessDefinition processDefinition = deployProcessWithTimerBoundaryEvent(timerDuration, true, "step1", "exceptionStep", "step2");

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final ActivityInstance waitForStep1 = waitForUserTask("step1", processInstance.getId());
        assertNotNull(waitForStep1);

        // wait timer trigger
        waitForUserTaskAndExecuteIt("exceptionStep", processInstance, getUser());
        waitForProcessToFinishAndBeArchived(processInstance);
        waitForArchivedActivity(waitForStep1.getId(), TestStates.getAbortedState());

        checkFlowNodeWasntExecuted(processInstance.getId(), "step2");

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void testTimerBoundaryEventWithScriptThatFail() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("pTimerBoundary", "2.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addStartEvent("start");
        final UserTaskDefinitionBuilder userTaskDefinitionBuilder = processDefinitionBuilder.addUserTask("step1", ACTOR_NAME);
        userTaskDefinitionBuilder.addBoundaryEvent("timer", true).addTimerEventTriggerDefinition(TimerType.DURATION,
                new ExpressionBuilder().createGroovyScriptExpression("script", "throw new java.lang.RuntimeException()", Long.class.getName()));
        processDefinitionBuilder.addAutomaticTask("timerStep");
        processDefinitionBuilder.addTransition("timer", "timerStep");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, donaBenta);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final ActivityInstance waitForTaskToFail = waitForTaskToFail(processInstance);
        assertEquals("step1", waitForTaskToFail.getName());
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { EventInstance.class, CallActivityInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Boundary", "Interrupting",
            "Timer", "Call Activity" }, story = "Execute timer boundary event triggered on call activity.", jira = "ENGINE-547")
    @Test
    public void timerBoundaryEventTriggeredOnCallActivity() throws Exception {
        final int timerDuration = 2000;
        final String simpleProcessName = "targetProcess";
        final String simpleTaskName = "stepCA";
        final String parentUserTaskName = "step2";
        final String exceptionFlowTaskName = "exceptionStep";

        // deploy a simple process p1
        final ProcessDefinition targetProcessDefinition = deployAndEnableSimpleProcess(simpleProcessName, simpleTaskName);

        // deploy a process, p2, with a call activity calling p1. The call activity has an interrupting timer boundary event
        final ProcessDefinition processDefinition = deployAndEnbleProcessWithTimerBoundaryEventOnCallActivity(timerDuration, true, simpleProcessName,
                parentUserTaskName, exceptionFlowTaskName);

        // start the root process and wait for boundary event trigger
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance waitForStepCA = waitForUserTask(simpleTaskName, processInstance.getId());
        assertNotNull(waitForStepCA);
        // wait timer trigger
        // check that the exception flow was taken
        waitForUserTaskAndExecuteIt("exceptionStep", processInstance, getUser());
        waitForProcessToFinishAndBeArchived(processInstance);

        final ArchivedActivityInstance archActivityInst = getProcessAPI().getArchivedActivityInstance(waitForStepCA.getId());
        assertEquals(TestStates.getAbortedState(), archActivityInst.getState());

        checkFlowNodeWasntExecuted(processInstance.getId(), parentUserTaskName);

        disableAndDeleteProcess(processDefinition);
        disableAndDeleteProcess(targetProcessDefinition);
    }

    @Cover(classes = { EventInstance.class, MultiInstanceLoopCharacteristics.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Boundary",
            "Interrupting", "Timer", "MultiInstance", "Sequential" }, story = "Execute interrupting timer boundary event triggered on sequential multi-instance.", jira = "ENGINE-547")
    @Test
    public void timerBoundaryEventTriggeredOnSequentialMultiInstance() throws Exception {
        // deploy a process with a interrupting timer boundary event attached to a sequential multi-instance
        final int timerDuration = 1000;
        final String multiTaskName = "step1";
        final ProcessDefinition processDefinition = deployProcessMultiInstanceWithBoundaryEvent(timerDuration, true, multiTaskName, 4, true, "step2",
                "exceptionStep");

        // start the process and wait the timer to trigger
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance multiInstance = waitForUserTask(multiTaskName, processInstance.getId());
        // wait timer trigger
        // check that the exception flow was taken
        waitForUserTaskAndExecuteIt("exceptionStep", processInstance, getUser());
        waitForProcessToFinishAndBeArchived(processInstance);

        waitForArchivedActivity(multiInstance.getId(), TestStates.getAbortedState());

        checkFlowNodeWasntExecuted(processInstance.getId(), "step2");

        disableAndDeleteProcess(processDefinition.getId());
    }

    @Cover(classes = { EventInstance.class, MultiInstanceLoopCharacteristics.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Boundary", "Timer",
            "MultiInstance", "Parallel" }, story = "Execute timer boundary event triggered on parallel multi-instance.", jira = "ENGINE-547")
    @Test
    public void timerBoundaryEventTriggeredOnParallelMultiInstance() throws Exception {
        final int timerDuration = 1000;
        final int loopCardinality = 4;
        final boolean isSequential = false;

        // deploy a process with a interrupting timer boundary event attached to a parallel multi-instance
        final ProcessDefinition processDefinition = deployProcessMultiInstanceWithBoundaryEvent(timerDuration, true, "step1", loopCardinality, isSequential,
                "step2", "exceptionStep");

        // start the process and wait for process to be triggered
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance step1 = waitForUserTask("step1", processInstance);
        // wait timer trigger
        waitForUserTaskAndExecuteIt("exceptionStep", processInstance, getUser());
        waitForProcessToFinishAndBeArchived(processInstance);

        final ArchivedActivityInstance archActivityInst = getProcessAPI().getArchivedActivityInstance(step1.getId());
        assertEquals(TestStates.getAbortedState(), archActivityInst.getState());

        checkFlowNodeWasntExecuted(processInstance.getId(), "step2");

        disableAndDeleteProcess(processDefinition.getId());
    }

    @Cover(classes = { EventInstance.class, LoopActivityInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Boundary", "Interrupging",
            "Timer", "Loop activity" }, story = "Interrupting timer boundary event triggered on loop activity", jira = "ENGINE-547")
    @Test
    public void timerBoundaryEventTriggeredOnLoopActivity() throws Exception {
        final long timerDuration = 1000;
        final int loopMax = 2;
        final String loopActivityName = "step1";
        final String normalFlowStepName = "step2";
        final String exceptionFlowStepName = "exceptionStep";
        final ProcessDefinition processDefinition = deployProcessWithBoundaryEventOnLoopActivity(timerDuration, true, loopMax, loopActivityName,
                normalFlowStepName, exceptionFlowStepName);

        // start the process and wait timer to trigger
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance activityInLoop = waitForUserTask(loopActivityName, processInstance.getId());
        Thread.sleep(timerDuration); // wait timer trigger

        // verify that the exception flow was taken
        waitForUserTaskAndExecuteIt(exceptionFlowStepName, processInstance, getUser());
        waitForProcessToFinish(processInstance);

        // verify that the normal flow was aborted
        waitForArchivedActivity(activityInLoop.getId(), TestStates.getAbortedState());

        checkFlowNodeWasntExecuted(processInstance.getId(), normalFlowStepName);

        disableAndDeleteProcess(processDefinition.getId());
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.EVENTS, keywords = { "Timer boundary event", "Data", "Long", "On process", "Human task",
    "Exception" }, story = "Execute a process with a long data and a human task with a timer boundary event that throw a exception", jira = "ENGINE-1383")
    @Test
    public void timerBoundaryEventTriggeredAndLongData() throws Exception {
        final int timerDuration = 1000;
        final ProcessDefinition processDefinition = deployProcessWithTimerEventOnHumanTask(timerDuration, true);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        // Wait and execute the step1 with a timer boundary event
        final HumanTaskInstance step1 = waitForUserTask("step1", processInstance);
        getProcessAPI().assignUserTask(step1.getId(), getUser().getId());

        // wait timer trigger
        waitForUserTaskAndExecuteIt("exceptionStep", processInstance, getUser());
        waitForProcessToFinishAndBeArchived(processInstance);

        // Check that step1 is aborted
        waitForArchivedActivity(step1.getId(), TestStates.getAbortedState());

        disableAndDeleteProcess(processDefinition);
    }

    private ProcessDefinition deployProcessWithTimerEventOnHumanTask(final long timerValue, final boolean withData) throws BonitaException,
    InvalidProcessDefinitionException {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("pTimerBoundary", "2.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addStartEvent("start");
        final UserTaskDefinitionBuilder userTaskDefinitionBuilder = processDefinitionBuilder.addUserTask("step1", ACTOR_NAME);

        final Expression timerExpr;
        if (withData) {
            processDefinitionBuilder.addData("timer", Long.class.getName(), new ExpressionBuilder().createConstantLongExpression(timerValue));
            timerExpr = new ExpressionBuilder().createDataExpression("timer", Long.class.getName());
        } else {
            timerExpr = new ExpressionBuilder().createConstantLongExpression(timerValue);
        }
        userTaskDefinitionBuilder.addBoundaryEvent("Boundary timer").addTimerEventTriggerDefinition(TimerType.DURATION, timerExpr);
        userTaskDefinitionBuilder.addUserTask("exceptionStep", ACTOR_NAME);
        userTaskDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", "step1");
        processDefinitionBuilder.addTransition("step1", "step2");
        processDefinitionBuilder.addTransition("Boundary timer", "exceptionStep");
        processDefinitionBuilder.addTransition("exceptionStep", "end");

        return deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, getUser());
    }

}
