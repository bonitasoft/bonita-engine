package org.bonitasoft.engine.event;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.BoundaryEventDefinition;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.MessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

public class MessageBoundaryEventTest extends AbstractEventTest {

    @Test
    @Cover(classes = { MessageEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "message", "boundary",
            "event" }, jira = "ENGINE-499", story = "message sent on a user task having a boundary catch message event")
    public void messageBoundaryEventTriggered() throws Exception {
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryMessageEvent("MyMessage");

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance step1 = waitForUserTask("step1", processInstance.getId());

        getProcessAPI().sendMessage("MyMessage", new ExpressionBuilder().createConstantStringExpression("pMessageBoundary"),
                new ExpressionBuilder().createConstantStringExpression(BOUNDARY_NAME), null);
        waitForUserTaskAndExecuteIt(EXCEPTION_STEP, processInstance, donaBenta);

        waitForProcessToFinish(processInstance);

        waitForArchivedActivity(step1.getId(), TestStates.ABORTED);

        checkWasntExecuted(processInstance, "step2");

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    @Cover(classes = { MessageEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "message", "boundary",
            "event", "call activity" }, jira = "ENGINE-499", story = "message with wrong name sent on a user task having a boundary catch message event")
    public void messageBoundaryEventNotTriggered() throws Exception {
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryMessageEvent("MyMessage1");

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
    public void messageBoundaryEventOnCallActivityTriggered() throws Exception {
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryMessageEventOnCallActivity();
        final ProcessDefinition calledProcessDefinition = deployAndEnableSimpleProcess("calledProcess", "calledTask");

        try {
            final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
            final ActivityInstance calledStep = waitForUserTask("calledTask", processInstance.getId());
            final ProcessInstance calledProcessInstance = getProcessAPI().getProcessInstance(calledStep.getParentProcessInstanceId());

            getProcessAPI().sendMessage("MyMessage", new ExpressionBuilder().createConstantStringExpression("pMessageBoundary"),
                    new ExpressionBuilder().createConstantStringExpression(BOUNDARY_NAME), null);

            waitForUserTaskAndExecuteIt("exceptionStep", processInstance, donaBenta);
            waitForProcessToBeInState(calledProcessInstance, ProcessInstanceState.ABORTED);
            waitForProcessToFinish(processInstance);

            waitForArchivedActivity(calledStep.getId(), TestStates.ABORTED);

            checkWasntExecuted(processInstance, "step2");
        } finally {
            disableAndDeleteProcess(processDefinition, calledProcessDefinition);
        }
    }

    @Test
    @Cover(classes = { MessageEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "message", "boundary",
            "event", "call activity" }, jira = "ENGINE-499", story = "message sent on a call activity having a boundary catch message event")
    public void messageBoundaryEventOnCallActivityNotTriggered() throws Exception {
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryMessageEventOnCallActivity();
        final ProcessDefinition calledProcessDefinition = deployAndEnableSimpleProcess("calledProcess", "calledTask");

        try {
            final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
            final ActivityInstance calledStep = waitForUserTask("calledTask", processInstance.getId());
            final ProcessInstance calledProcessInstance = getProcessAPI().getProcessInstance(calledStep.getParentProcessInstanceId());
            assignAndExecuteStep(calledStep, donaBenta.getId());

            final ActivityInstance step2 = waitForUserTask("step2", processInstance.getId());

            getProcessAPI().sendMessage("MyMessage", new ExpressionBuilder().createConstantStringExpression("pMessageBoundary"),
                    new ExpressionBuilder().createConstantStringExpression(BOUNDARY_NAME), null);

            waitForProcessToFinish(calledProcessInstance);
            assignAndExecuteStep(step2, donaBenta.getId());
            waitForProcessToFinish(processInstance);

            checkWasntExecuted(processInstance, "exceptionStep");
        } finally {
            disableAndDeleteProcess(processDefinition, calledProcessDefinition);
        }
    }

    @Cover(classes = { MessageEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message",
            "Boundary", "Multi-instance", "Sequential" }, story = "Execute message boundary event triggered on sequential multi-instance.", jira = "ENGINE-547")
    @Test
    public void messageBoundaryEventTriggeredOnSequentialMultiInstance() throws Exception {
        final int loopCardinality = 4;
        final boolean isSequential = true;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryMessageEventOnMultiInstance(loopCardinality, isSequential);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance step1 = waitForUserTask("step1", processInstance.getId());

        getProcessAPI().sendMessage("MyMessage", new ExpressionBuilder().createConstantStringExpression("processWithBoundaryMessageEventAndMultiInstance"),
                new ExpressionBuilder().createConstantStringExpression(BOUNDARY_NAME), null);
        waitForUserTaskAndExecuteIt(EXCEPTION_STEP, processInstance, donaBenta);

        waitForProcessToFinish(processInstance);

        waitForArchivedActivity(step1.getId(), TestStates.ABORTED);

        checkWasntExecuted(processInstance, "step2");

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { MessageEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message",
            "Boundary", "Multi-instance", "Sequential" }, story = "Execute message boundary event not triggered on sequential multi-instance", jira = "ENGINE-547")
    @Test
    public void messageBoundaryEventNotTriggeredOnSequentialMultiInstance() throws Exception {
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
    public void messageBoundaryEventTriggeredOnParallelMultiInstance() throws Exception {
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
        waitForUserTaskAndExecuteIt(EXCEPTION_STEP, processInstance, donaBenta);

        waitForProcessToFinish(processInstance);

        for (final HumanTaskInstance humanTaskInstance : pendingTasks) {
            waitForArchivedActivity(humanTaskInstance.getId(), TestStates.ABORTED);
        }

        checkWasntExecuted(processInstance, "step2");

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { MessageEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message",
            "Boundary", "Multi-instance", "Sequential" }, story = "Execute message boundary event not triggered on parallel multi-instance.", jira = "ENGINE-547")
    @Test
    public void messageBoundaryEventNotTriggeredOnParallelMultiInstance() throws Exception {
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

    @Cover(classes = { MessageEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message",
            "Boundary", "Loop activity" }, story = "Execute message boundary event triggered on loop activity", jira = "ENGINE-547")
    @Test
    public void messageBoundaryEventTriggeredOnLoopActivity() throws Exception {
        final int loopMax = 3;

        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryMessageEventOnLoopActivity(loopMax);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance step1 = waitForUserTask("step1", processInstance.getId());

        getProcessAPI().sendMessage("MyMessage", new ExpressionBuilder().createConstantStringExpression("processWithLoopActivityAndBoundaryEvent"),
                new ExpressionBuilder().createConstantStringExpression(BOUNDARY_NAME), null);
        waitForUserTaskAndExecuteIt(EXCEPTION_STEP, processInstance, donaBenta);

        waitForProcessToFinish(processInstance);

        waitForArchivedActivity(step1.getId(), TestStates.ABORTED);

        checkWasntExecuted(processInstance, "step2");

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { MessageEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message",
            "Boundary", "Loop activity" }, story = "Execute message boundary event not triggered on loop activity", jira = "ENGINE-547")
    @Test
    public void messageBoundaryEventNotTriggeredOnLoopActivity() throws Exception {
        final int loopMax = 2;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryMessageEventOnLoopActivity(loopMax);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        for (int i = 0; i < loopMax; i++) {
            waitForUserTaskAndExecuteIt("step1", processInstance, donaBenta);
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
