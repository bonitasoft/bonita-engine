/**
 * Copyright (C) 2015 BonitaSoft S.A.
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

import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Celine Souchet
 */
public class ProcessDefinitionBuilderTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

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
    @Test(expected = InvalidProcessDefinitionException.class)
    public void cannotHaveCorrelationInStartMessageOfRootProcess() throws Exception {
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

    @Test
    public void canStartOnIntermediateCatchEventMessage() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder();
        processBuilder.createNewInstance("processName", "1.0");
        processBuilder.addIntermediateCatchEvent("name").addMessageEventTrigger("messageName");
        processBuilder.done();
    }

    @Test(expected = InvalidProcessDefinitionException.class)
    public void eventSubProcessCannotHaveTwoFlowNodesWithTheSameName2() throws Exception {
        final Expression expression = new ExpressionBuilder().createConstantDoubleExpression(45d);
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithEventSubProcess", "1.0");
        builder.addActor("mainActor");
        builder.addUserTask("step1", "mainActor")
                .addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand("myAddress"), OperatorType.ASSIGNMENT, null, null, expression)
                .addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand("myAddress"), OperatorType.DELETION, null, null, null);
        builder.done();
    }

    @Test
    public void eventSubProcessCannotHaveTwoFlowNodesWithTheSameName3() throws Exception {
        final Expression expression = new ExpressionBuilder().createConstantDoubleExpression(45d);

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithEventSubProcess", "1.0");
        builder.addActor("mainActor");
        builder.addUserTask("step1", "mainActor")
                .addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand("myAddress1"), OperatorType.ASSIGNMENT, null, null, expression)
                .addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand("myAddress"), OperatorType.DELETION, null, null, null);
        builder.done();
    }

    @Test
    public void eventSubProcessCannotHaveTwoFlowNodesWithTheSameName43() throws Exception {
        final Expression expression = new ExpressionBuilder().createConstantDoubleExpression(45d);

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithEventSubProcess", "1.0");
        builder.addActor("mainActor");
        builder.addUserTask("step1", "mainActor")
                .addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand("myAddress1"), OperatorType.ASSIGNMENT, null, null, expression)
                .addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand("myAddress"), OperatorType.JAVA_METHOD, null, null, expression);
        builder.done();
    }

    @Test(expected = InvalidProcessDefinitionException.class)
    public void cant_create_process_definition_with_actor_initiator_without_actor() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("Double-hyphen -- test", "any");
        builder.setActorInitiator("toto");
        builder.done();
    }

    @Test
    public void userTask_with_valid_contract() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithBadContract", "1.0");
        builder.addActor("mainActor");

        //given
        builder.addUserTask("step1", "mainActor")
                .addContract()
                .addInput("innput", Type.TEXT, "should fail")
                .addConstraint("firstConstraint", "input != null", "mandatory", "input");

        //when
        builder.done();

        //then no exceptions
    }

    @Test(expected = InvalidProcessDefinitionException.class)
    public void userTask_with_invalid_contract_should_throw_exception() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithBadContract", "1.0");
        builder.addActor("mainActor");

        //given
        builder.addUserTask("step1", "mainActor").addContract()
                .addComplexInput("expenseLine", "expense report line")
                .addInput("date", Type.DATE, "expense date")
                .addInput("amount", Type.DECIMAL, "expense amount")
                .addComplexInput("date", "expense date")
                .addInput("expenseType", Type.TEXT, "describe expense type")
                .addInput("bad input", Type.TEXT, "should fail");

        //when
        builder.done();

        //then exception
    }

    @Test(expected = InvalidProcessDefinitionException.class)
    public void userTask_with_null_constraint_name_of_contract_should_throw_exception() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithBadContract", "1.0");
        builder.addActor("mainActor");
        builder.addUserTask("step1", "mainActor").addContract()
                .addInput("input", Type.TEXT, "").addConstraint(null, "input != null", "mandatory", "input");

        builder.done();
    }

    @Test(expected = InvalidProcessDefinitionException.class)
    public void userTask_with_null_constraint_condition_of_contract_should_throw_exception() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithBadContract", "1.0");
        builder.addActor("mainActor");
        builder.addUserTask("step1", "mainActor").addContract()
                .addInput("input", Type.TEXT, "").addConstraint("firstConstraint", null, "mandatory", "input");

        builder.done();
    }

    @Test
    public void validate_contract_with_no_type() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("test", "1");

        builder.addContract().addComplexInput("name", "desc");

        expectedException.expect(InvalidProcessDefinitionException.class);
        expectedException.expectMessage("Type not set on the contract input <name> on the process-level contract");
        builder.done();
    }

    @Test
    public void validate_contract_with_no_type_on_user_task() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("test", "1");

        builder.addActor("john").addUserTask("step1","john").addContract().addComplexInput("name", "desc");

        expectedException.expect(InvalidProcessDefinitionException.class);
        expectedException.expectMessage("Type not set on the contract input <name> on the task-level contract for task <step1>");
        builder.done();
    }

    @Test
    public void validate_contract_with_no_type_sub_children() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("test", "1");

        builder.addContract().addComplexInput("name", "desc").addComplexInput("name", "desc");

        expectedException.expect(InvalidProcessDefinitionException.class);
        expectedException.expectMessage("Type not set on the contract input <name> on the process-level contract");
        builder.done();
    }

}
