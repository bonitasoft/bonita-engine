package org.bonitasoft.engine.event;

import org.bonitasoft.engine.bpm.flownode.BoundaryEventInstance;
import org.bonitasoft.engine.bpm.flownode.EventInstance;
import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.SubProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

public class BoundaryEventsDefinitionTest {

    @Cover(classes = { EventInstance.class, BoundaryEventInstance.class }, exceptions = InvalidProcessDefinitionException.class, concept = BPMNConcept.EVENTS, keywords = {
            "Event", "Boundary event", "Timer event", "Automatic task", "Outgoing transition" }, story = "Check that a boundary event musy have an outgoing transition.")
    @Test(expected = InvalidProcessDefinitionException.class)
    public void boundaryEventMustHaveOutgoingTransition() throws Exception {
        final Expression timerExpr = new ExpressionBuilder().createConstantLongExpression(100);
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("invalid", "1.0");
        processDefinitionBuilder.addStartEvent("start");
        processDefinitionBuilder.addUserTask("step1", "actor").addBoundaryEvent("b1", true).addTimerEventTriggerDefinition(TimerType.DURATION, timerExpr);
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", "step1");
        processDefinitionBuilder.addTransition("step1", "end");
        processDefinitionBuilder.done();
    }

    @Test(expected = InvalidProcessDefinitionException.class)
    public void boundaryEventsAreAlsoValidatedInsideSubProcess() throws Exception {
        final Expression timerExpr = new ExpressionBuilder().createConstantLongExpression(100);
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("invalid", "1.0");
        processDefinitionBuilder.addStartEvent("start");
        processDefinitionBuilder.addAutomaticTask("auto1");
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", "auto1");
        processDefinitionBuilder.addTransition("auto1", "end");
        final SubProcessDefinitionBuilder subProcessBuilder = processDefinitionBuilder.addSubProcess("subProc", true).getSubProcessBuilder();
        subProcessBuilder.addStartEvent("startSub").addSignalEventTrigger("go");
        subProcessBuilder.addUserTask("step1", "actor").addBoundaryEvent("b1").addTimerEventTriggerDefinition(TimerType.DURATION, timerExpr);
        subProcessBuilder.addEndEvent("endSub");
        subProcessBuilder.addTransition("startSub", "step1");
        subProcessBuilder.addTransition("step1", "endSub");
        processDefinitionBuilder.done();
    }

    @Cover(classes = { EventInstance.class, BoundaryEventInstance.class }, exceptions = InvalidProcessDefinitionException.class, concept = BPMNConcept.EVENTS, keywords = {
            "Event", "Boundary event", "Timer event", "Automatic task", "Outgoing transition" }, story = "Check that a boundary event must have an outgoing transition without condition.")
    @Test(expected = InvalidProcessDefinitionException.class)
    public void boundaryEventMustHaveOutgoingTransitionWithoutCondition() throws Exception {
        final Expression timerExpr = new ExpressionBuilder().createConstantLongExpression(100);
        final Expression trueExpr = new ExpressionBuilder().createConstantBooleanExpression(true);
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("invalid", "1.0");
        final String taskWithBoundary = "step1";
        processDefinitionBuilder.addStartEvent("start");
        processDefinitionBuilder.addUserTask(taskWithBoundary, "actor").addBoundaryEvent("b1", true)
                .addTimerEventTriggerDefinition(TimerType.DURATION, timerExpr);
        processDefinitionBuilder.addAutomaticTask("auto2");
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", taskWithBoundary);
        processDefinitionBuilder.addTransition(taskWithBoundary, "end");
        processDefinitionBuilder.addTransition("b1", "auto2", trueExpr);
        processDefinitionBuilder.done();
    }

    @Cover(classes = { EventInstance.class, BoundaryEventInstance.class }, exceptions = InvalidProcessDefinitionException.class, concept = BPMNConcept.EVENTS, keywords = {
            "Event", "Boundary Event", "Trigger", "Automatic task" }, story = "Check that a boundary event must have a trigger")
    @Test(expected = InvalidProcessDefinitionException.class)
    public void boundaryEventMustHaveATrigger() throws Exception {
        final String taskWithBoundary = "step1";
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("invalid", "1.0");
        processDefinitionBuilder.addStartEvent("start");
        processDefinitionBuilder.addUserTask(taskWithBoundary, "actor").addBoundaryEvent("b1", true);
        processDefinitionBuilder.addAutomaticTask("auto2");
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", taskWithBoundary);
        processDefinitionBuilder.addTransition(taskWithBoundary, "end");
        processDefinitionBuilder.addTransition("b1", "auto2");
        processDefinitionBuilder.done();
    }

    @Cover(classes = { EventInstance.class, BoundaryEventInstance.class }, exceptions = InvalidProcessDefinitionException.class, concept = BPMNConcept.EVENTS, keywords = {
            "Event", "Boundary Event", "Trigger", "Automatic task" }, story = "Check that a boundary event must have a trigger")
    @Test(expected = InvalidProcessDefinitionException.class)
    public void timerBoundaryEventCannotHasTypeCycle() throws Exception {
        final Expression timerValue = new ExpressionBuilder().createConstantStringExpression("0/5 * * * * ?");
        final String taskWithBoundary = "step1";
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("invalid", "1.0");
        processDefinitionBuilder.addStartEvent("start");
        processDefinitionBuilder.addUserTask(taskWithBoundary, "actor").addBoundaryEvent("b1").addTimerEventTriggerDefinition(TimerType.CYCLE, timerValue);
        processDefinitionBuilder.addAutomaticTask("auto2");
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", taskWithBoundary);
        processDefinitionBuilder.addTransition(taskWithBoundary, "end");
        processDefinitionBuilder.addTransition("b1", "auto2");
        processDefinitionBuilder.done();
    }

    // this test must be deleted after implementation of non-interrupting signal event
    @Test(expected = InvalidProcessDefinitionException.class)
    public void nonInterruptingSignalBoundaryEventIsNotSupported() throws Exception {
        final String taskWithBoundary = "step1";
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("invalid", "1.0");
        processDefinitionBuilder.addStartEvent("start");
        processDefinitionBuilder.addUserTask(taskWithBoundary, "actor").addBoundaryEvent("b1", false).addSignalEventTrigger("go");
        processDefinitionBuilder.addAutomaticTask("auto2");
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", taskWithBoundary);
        processDefinitionBuilder.addTransition(taskWithBoundary, "end");
        processDefinitionBuilder.addTransition("b1", "auto2");
        processDefinitionBuilder.done();
    }

    // this test must be deleted after implementation of non-interrupting message event
    @Test(expected = InvalidProcessDefinitionException.class)
    public void nonInterruptingMessageBoundaryEventIsNotSupported() throws Exception {
        final String taskWithBoundary = "step1";
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("invalid", "1.0");
        processDefinitionBuilder.addStartEvent("start");
        processDefinitionBuilder.addUserTask(taskWithBoundary, "actor").addBoundaryEvent("b1", false).addMessageEventTrigger("m1");
        processDefinitionBuilder.addAutomaticTask("auto2");
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", taskWithBoundary);
        processDefinitionBuilder.addTransition(taskWithBoundary, "end");
        processDefinitionBuilder.addTransition("b1", "auto2");
        processDefinitionBuilder.done();
    }

    // an error event is always interrupting
    @Test(expected = InvalidProcessDefinitionException.class)
    public void nonInterruptingErrorBoundaryEventIsNotSupported() throws Exception {
        final String taskWithBoundary = "step1";
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("invalid", "1.0");
        processDefinitionBuilder.addStartEvent("start");
        processDefinitionBuilder.addUserTask(taskWithBoundary, "actor").addBoundaryEvent("b1", false).addErrorEventTrigger("e1");
        processDefinitionBuilder.addAutomaticTask("auto2");
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", taskWithBoundary);
        processDefinitionBuilder.addTransition(taskWithBoundary, "end");
        processDefinitionBuilder.addTransition("b1", "auto2");
        processDefinitionBuilder.done();
    }

    @Cover(classes = { EventInstance.class, BoundaryEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Boundary", "Automatic task", "Timer" }, story = "Timer boundary events are not supported on automatic tasks")
    @Test(expected = InvalidProcessDefinitionException.class)
    public void timerBoundaryEventsOnAutomaticTasksAreNotSupported() throws Exception {
        final String taskWithBoundary = "step1";
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("invalid", "1.0");
        processDefinitionBuilder.addStartEvent("start");
        processDefinitionBuilder.addAutomaticTask(taskWithBoundary).addBoundaryEvent("b1")
                .addTimerEventTriggerDefinition(TimerType.DURATION, new ExpressionBuilder().createConstantLongExpression(1000));
        processDefinitionBuilder.addAutomaticTask("auto2");
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", taskWithBoundary);
        processDefinitionBuilder.addTransition(taskWithBoundary, "end");
        processDefinitionBuilder.addTransition("b1", "auto2");
        processDefinitionBuilder.done();
    }

    @Cover(classes = { EventInstance.class, BoundaryEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Boundary", "Automatic task", "Signal" }, story = "Signal boundary events are not supported on automatic tasks")
    @Test(expected = InvalidProcessDefinitionException.class)
    public void signalBoundaryEventsOnAutomaticTasksAreNotSupported() throws Exception {
        final String taskWithBoundary = "step1";
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("invalid", "1.0");
        processDefinitionBuilder.addStartEvent("start");
        processDefinitionBuilder.addAutomaticTask(taskWithBoundary).addBoundaryEvent("b1").addSignalEventTrigger("go");
        processDefinitionBuilder.addAutomaticTask("auto2");
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", taskWithBoundary);
        processDefinitionBuilder.addTransition(taskWithBoundary, "end");
        processDefinitionBuilder.addTransition("b1", "auto2");
        processDefinitionBuilder.done();
    }

    @Cover(classes = { EventInstance.class, BoundaryEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Boundary", "Automatic task", "Message" }, story = "Message boundary events are not supported on automatic tasks")
    @Test(expected = InvalidProcessDefinitionException.class)
    public void messageBoundaryEventsOnAutomaticTasksAreNotSupported() throws Exception {
        final String taskWithBoundary = "step1";
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("invalid", "1.0");
        processDefinitionBuilder.addStartEvent("start");
        processDefinitionBuilder.addAutomaticTask(taskWithBoundary).addBoundaryEvent("b1").addMessageEventTrigger("m1");
        processDefinitionBuilder.addAutomaticTask("auto2");
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", taskWithBoundary);
        processDefinitionBuilder.addTransition(taskWithBoundary, "end");
        processDefinitionBuilder.addTransition("b1", "auto2");
        processDefinitionBuilder.done();
    }

    @Cover(classes = { EventInstance.class, BoundaryEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Boundary", "Receive task", "Timer" }, story = "Timer boundary events are not supported on receive tasks")
    @Test(expected = InvalidProcessDefinitionException.class)
    public void timerBoundaryEventsOnReceiveTasksAreNotSupported() throws Exception {
        final String taskWithBoundary = "step1";
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("invalid", "1.0");
        processDefinitionBuilder.addStartEvent("start");
        processDefinitionBuilder.addReceiveTask(taskWithBoundary, "m1").addBoundaryEvent("b1")
                .addTimerEventTriggerDefinition(TimerType.DURATION, new ExpressionBuilder().createConstantLongExpression(1000));
        processDefinitionBuilder.addAutomaticTask("auto2");
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", taskWithBoundary);
        processDefinitionBuilder.addTransition(taskWithBoundary, "end");
        processDefinitionBuilder.addTransition("b1", "auto2");
        processDefinitionBuilder.done();
    }

    @Cover(classes = { EventInstance.class, BoundaryEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Boundary", "Receive task", "Signal" }, story = "Signal boundary events are not supported on receive tasks")
    @Test(expected = InvalidProcessDefinitionException.class)
    public void signalBoundaryEventsOnReceiveTasksAreNotSupported() throws Exception {
        final String taskWithBoundary = "step1";
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("invalid", "1.0");
        processDefinitionBuilder.addStartEvent("start");
        processDefinitionBuilder.addReceiveTask(taskWithBoundary, "m1").addBoundaryEvent("b1").addSignalEventTrigger("go");
        processDefinitionBuilder.addAutomaticTask("auto2");
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", taskWithBoundary);
        processDefinitionBuilder.addTransition(taskWithBoundary, "end");
        processDefinitionBuilder.addTransition("b1", "auto2");
        processDefinitionBuilder.done();
    }

    @Cover(classes = { EventInstance.class, BoundaryEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Boundary", "Receive task", "Message" }, story = "Message boundary events are not supported on receive tasks")
    @Test(expected = InvalidProcessDefinitionException.class)
    public void messageBoundaryEventsOnReceiveTasksAreNotSupported() throws Exception {
        final String taskWithBoundary = "step1";
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("invalid", "1.0");
        processDefinitionBuilder.addStartEvent("start");
        processDefinitionBuilder.addReceiveTask(taskWithBoundary, "m1").addBoundaryEvent("b1").addMessageEventTrigger("m1");
        processDefinitionBuilder.addAutomaticTask("auto2");
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", taskWithBoundary);
        processDefinitionBuilder.addTransition(taskWithBoundary, "end");
        processDefinitionBuilder.addTransition("b1", "auto2");
        processDefinitionBuilder.done();
    }

    @Cover(classes = { EventInstance.class, BoundaryEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Boundary", "Send task", "Timer" }, story = "Timer boundary events are not supported on send tasks")
    @Test(expected = InvalidProcessDefinitionException.class)
    public void timerBoundaryEventsOnSendTasksAreNotSupported() throws Exception {
        final String taskWithBoundary = "step1";
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("invalid", "1.0");
        processDefinitionBuilder.addStartEvent("start");
        processDefinitionBuilder.addSendTask(taskWithBoundary, "m1", new ExpressionBuilder().createConstantStringExpression("p1")).addBoundaryEvent("b1")
                .addTimerEventTriggerDefinition(TimerType.DURATION, new ExpressionBuilder().createConstantLongExpression(1000));
        processDefinitionBuilder.addAutomaticTask("auto2");
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", taskWithBoundary);
        processDefinitionBuilder.addTransition(taskWithBoundary, "end");
        processDefinitionBuilder.addTransition("b1", "auto2");
        processDefinitionBuilder.done();
    }

    @Cover(classes = { EventInstance.class, BoundaryEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Boundary", "Send task", "Signal" }, story = "Signal boundary events are not supported on send tasks")
    @Test(expected = InvalidProcessDefinitionException.class)
    public void signalBoundaryEventsOnSendTasksAreNotSupported() throws Exception {
        final String taskWithBoundary = "step1";
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("invalid", "1.0");
        processDefinitionBuilder.addStartEvent("start");
        processDefinitionBuilder.addSendTask(taskWithBoundary, "m1", new ExpressionBuilder().createConstantStringExpression("p1")).addBoundaryEvent("b1")
                .addSignalEventTrigger("go");
        processDefinitionBuilder.addAutomaticTask("auto2");
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", taskWithBoundary);
        processDefinitionBuilder.addTransition(taskWithBoundary, "end");
        processDefinitionBuilder.addTransition("b1", "auto2");
        processDefinitionBuilder.done();
    }

    @Cover(classes = { EventInstance.class, BoundaryEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Boundary", "Send task", "Message" }, story = "Message boundary events are not supported on send tasks")
    @Test(expected = InvalidProcessDefinitionException.class)
    public void messageBoundaryEventsOnSendTasksAreNotSupported() throws Exception {
        final String taskWithBoundary = "step1";
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("invalid", "1.0");
        processDefinitionBuilder.addStartEvent("start");
        processDefinitionBuilder.addSendTask(taskWithBoundary, "m1", new ExpressionBuilder().createConstantStringExpression("p1")).addBoundaryEvent("b1")
                .addMessageEventTrigger("m1");
        processDefinitionBuilder.addAutomaticTask("auto2");
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", taskWithBoundary);
        processDefinitionBuilder.addTransition(taskWithBoundary, "end");
        processDefinitionBuilder.addTransition("b1", "auto2");
        processDefinitionBuilder.done();
    }

}
