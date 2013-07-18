/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.bpm.process.impl;

import org.bonitasoft.engine.bpm.flownode.BoundaryEventInstance;
import org.bonitasoft.engine.bpm.flownode.EventInstance;
import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.SubProcessDefinition;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class ProcessDefinitionBuilderTest {

    private final String actorInitiator = "Actor name";

    @Mock
    private DesignProcessDefinitionImpl process;

    @InjectMocks
    private ProcessDefinitionBuilder processDefinitionBuilder;

    @Test
    public final void createNewInstance() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void done() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addError() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addDocumentDefinition() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addDescription() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addDisplayName() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addDisplayDescription() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void checkExpression() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void checkName() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addConnector() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addUserTask() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addAutomaticTask() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addReceiveTask() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addSendTask() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addManualTask() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addTransitionStringString() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addTransitionStringStringExpression() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addGateway() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addStartEvent() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addEndEvent() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addIntermediateCatchEvent() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addIntermediateThrowEvent() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addCallActivity() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addSubProcess() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addIntegerData() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addLongData() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addShortTextData() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addLongTextData() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addDoubleData() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addFloatData() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addDateData() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addXMLData() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addBlobData() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addBooleanData() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addData() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addActorString() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addActorStringBoolean() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void setActorInitiator() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void addDefaultTransition() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void getProcess() throws Exception {
        // TODO : Not yet implemented
    }

    // FIXME : Split all tests after, in several unit tests
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

    // EventSubProcessDefinitionTest
    @Test(expected = InvalidProcessDefinitionException.class)
    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event subprocess" }, jira = "ENGINE-537", story = "Verify that a event sub-proces cannot have incomming transitions")
    public void eventSubProcessMusntHaveIncommingTransitions() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithEventSubProcess", "1.0");
        builder.addActor("mainActor");
        builder.addUserTask("step1", "mainActor");
        final SubProcessDefinitionBuilder subProcessBuilder = builder.addSubProcess("eventSubProcess", true).getSubProcessBuilder();
        subProcessBuilder.addStartEvent("timerStart").addTimerEventTriggerDefinition(TimerType.DURATION,
                new ExpressionBuilder().createConstantLongExpression(1000));
        builder.addTransition("step1", "eventSubProcess");
        subProcessBuilder.addUserTask("subStep", "mainActor");
        subProcessBuilder.addEndEvent("endSubProcess");
        subProcessBuilder.addTransition("timerStart", "subStep");
        subProcessBuilder.addTransition("subStep", "endSubProcess");
        builder.done();
    }

    @Test(expected = InvalidProcessDefinitionException.class)
    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event subprocess" }, jira = "ENGINE-537", story = "Verify that a event sub-proces cannot have outgoing transitions")
    public void eventSubProcessMusntHaveOutgoingTransitions() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithEventSubProcess", "1.0");
        builder.addActor("mainActor");
        builder.addUserTask("step1", "mainActor");
        builder.addUserTask("step2", "mainActor");
        final SubProcessDefinitionBuilder subProcessBuilder = builder.addSubProcess("eventSubProcess", true).getSubProcessBuilder();
        subProcessBuilder.addStartEvent("timerStart").addTimerEventTriggerDefinition(TimerType.DURATION,
                new ExpressionBuilder().createConstantLongExpression(1000));
        builder.addTransition("eventSubProcess", "step2");
        subProcessBuilder.addUserTask("subStep", "mainActor");
        subProcessBuilder.addEndEvent("endSubProcess");
        subProcessBuilder.addTransition("timerStart", "subStep");
        subProcessBuilder.addTransition("subStep", "endSubProcess");
        builder.done();
    }

    @Test(expected = InvalidProcessDefinitionException.class)
    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event subprocess" }, jira = "ENGINE-537", story = "Verify that a event sub-proces mus have a start event with a trigger")
    public void eventSubProcessMustHaveTrigger() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithEventSubProcess", "1.0");
        builder.addActor("mainActor");
        builder.addUserTask("step1", "mainActor");
        final SubProcessDefinitionBuilder subProcessBuilder = builder.addSubProcess("eventSubProcess", true).getSubProcessBuilder();
        subProcessBuilder.addStartEvent("start");
        subProcessBuilder.addUserTask("subStep", "mainActor");
        subProcessBuilder.addEndEvent("endSubProcess");
        subProcessBuilder.addTransition("start", "subStep");
        subProcessBuilder.addTransition("subStep", "endSubProcess");
        builder.done();
    }

    @Test(expected = InvalidProcessDefinitionException.class)
    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event subprocess" }, jira = "ENGINE-537", story = "Verify that a event sub-proces mus have a start event with a trigger")
    public void eventSubProcessCannotHaveMoreThanOneStartEvent() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithEventSubProcess", "1.0");
        builder.addActor("mainActor");
        builder.addUserTask("step1", "mainActor");
        final SubProcessDefinitionBuilder subProcessBuilder = builder.addSubProcess("eventSubProcess", true).getSubProcessBuilder();
        subProcessBuilder.addStartEvent("timerStart1").addTimerEventTriggerDefinition(TimerType.DURATION,
                new ExpressionBuilder().createConstantLongExpression(1000));
        subProcessBuilder.addStartEvent("timerStart2").addTimerEventTriggerDefinition(TimerType.DURATION,
                new ExpressionBuilder().createConstantLongExpression(2000));
        subProcessBuilder.addUserTask("subStep", "mainActor");
        subProcessBuilder.addEndEvent("endSubProcess");
        subProcessBuilder.addTransition("timerStart1", "subStep");
        subProcessBuilder.addTransition("timerStart2", "subStep");
        subProcessBuilder.addTransition("subStep", "endSubProcess");
        builder.done();
    }

    // FIXME remove after adding ids in the flow nodes
    @Test(expected = InvalidProcessDefinitionException.class)
    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event subprocess" }, jira = "ENGINE-537", story = "Verify that all names are unique")
    public void eventSubProcessCannotHaveTwoFlowNodesWithTheSameName() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithEventSubProcess", "1.0");
        builder.addActor("mainActor");
        builder.addUserTask("step1", "mainActor");
        final SubProcessDefinitionBuilder subProcessBuilder = builder.addSubProcess("eventSubProcess", true).getSubProcessBuilder();
        subProcessBuilder.addStartEvent("timerStart1").addTimerEventTriggerDefinition(TimerType.DURATION,
                new ExpressionBuilder().createConstantLongExpression(1000));
        subProcessBuilder.addUserTask("step1", "mainActor");
        subProcessBuilder.addEndEvent("endSubProcess");
        subProcessBuilder.addTransition("timerStart1", "step1");
        subProcessBuilder.addTransition("step1", "endSubProcess");
        builder.done();
    }

    // MessageEventsDefinitionTest
    @Cover(classes = { CatchMessageEventTriggerDefinitionBuilder.class }, concept = BPMNConcept.EVENTS, keywords = { "Correlation", "Message start event" })
    @Test(expected = InvalidProcessDefinitionException.class)
    public void testCannotHaveCorrelationInStartMessageOfRootProcess() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("proc", "1.0");
        final CatchMessageEventTriggerDefinitionBuilder messageEventTrigger = builder.addStartEvent("startMessage").addMessageEventTrigger("m1");
        messageEventTrigger.addCorrelation(new ExpressionBuilder().createConstantStringExpression("key"),
                new ExpressionBuilder().createConstantStringExpression("v1"));
        builder.addAutomaticTask("auto1");
        builder.addEndEvent("end");
        builder.addTransition("startMessage", "auto1");
        builder.addTransition("auto1", "end");
        builder.done();
    }
}
