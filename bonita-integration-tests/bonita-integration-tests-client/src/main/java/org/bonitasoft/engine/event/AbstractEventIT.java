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
package org.bonitasoft.engine.event;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.bpm.actor.ActorCriterion;
import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.xml.XMLProcessDefinition.BEntry;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.FlowNodeType;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.CallActivityBuilder;
import org.bonitasoft.engine.bpm.process.impl.CatchMessageEventTriggerDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.IntermediateThrowEventDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.StartEventDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.SubProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ThrowMessageEventTriggerBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.connectors.TestConnectorThatThrowException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.BuildTestUtil;

/**
 * @author Celine Souchet
 * @version 6.4.0
 * @since 6.4.0
 */
public abstract class AbstractEventIT extends TestWithUser {

    public static final String EXCEPTION_STEP = "exceptionStep";

    public static final String DATE_FORMAT_WITH_MS = "yyyyy-mm-dd hh:mm:ss SSSSS";

    public static final String BOUNDARY_NAME = "waitMessage";

    public static final String INT_DATA_NAME = "count";

    public static final String SHORT_DATA_NAME = "short_data_name";

    public static final String SUB_PROCESS_START_NAME = "subProcessStart";

    public static final String SUB_PROCESS_USER_TASK_NAME = "subStep";

    public static final String SUB_PROCESS_NAME = "eventSubProcess";

    public static final String PARENT_PROCESS_USER_TASK_NAME = "step1";

    public static final String PARENT_END = "end";

    public static final String THROW_MESSAGE_TASK_NAME = "messageTask";

    public static final String MESSAGE_NAME = "canStart";

    public static final String START_WITH_MESSAGE_STEP1_NAME = "userStart1";

    public static final String START_WITH_MESSAGE_PROCESS_NAME = "Start from message";

    public static final String CATCH_EVENT_NAME = "waitForMessage";

    public static final String CATCH_MESSAGE_PROCESS_NAME = "Catch a message";

    public static final String CATCH_MESSAGE_STEP1_NAME = "step1";

    public static final String SEND_MESSAGE_PROCESS_NAME = "Send a message";

    public static final String AFTER_SIGNAL = "after_signal";

    public static final String SIGNAL_NAME = "canStart";

    public static final String START_EVENT_NAME = "startEvent";

    /**
     * Deploy and enable a process with a human task having a timer boundary event followed by another human task without boundary
     *
     * @param timerValue
     *        after how long time the boundary will be triggered
     * @param interrupting
     *        define whether the boundary is interrupting or not
     * @param taskWithBoundaryName
     *        the name of user task containing the boundary event
     * @param exceptionTaskName
     *        the name of human task reached by exception flow
     * @param normalFlowTaskName
     *        name of human task following the task containing the boundary (normal flow)
     * @return
     * @throws BonitaException
     * @throws InvalidProcessDefinitionException
     */
    public ProcessDefinition deployAndEnableProcessWithBoundaryTimerEvent(final long timerValue, final boolean interrupting, final String taskWithBoundaryName,
            final String exceptionTaskName, final String normalFlowTaskName) throws BonitaException {
        return deployAndEnableProcessWithBoundaryTimerEvent(TimerType.DURATION, timerValue, interrupting, taskWithBoundaryName, exceptionTaskName,
                normalFlowTaskName);
    }

    /**
     * Deploy and enable a process with a human task having a timer boundary event followed by another human task without boundary
     *
     * @param timerType
     *        the timer time
     * @param timerValue
     *        the timer value
     * @param interrupting
     *        define whether the boundary is interrupting or not
     * @param taskWithBoundaryName
     *        the name of user task containing the boundary event
     * @param exceptionTaskName
     *        the name of human task reached by exception flow
     * @param normalFlowTaskName
     *        name of human task following the task containing the boundary (normal flow)
     * @return
     * @throws BonitaException
     * @throws InvalidProcessDefinitionException
     * @since 6.0
     */
    public ProcessDefinition deployAndEnableProcessWithBoundaryTimerEvent(final TimerType timerType, final long timerValue, final boolean interrupting,
            final String taskWithBoundaryName, final String exceptionTaskName, final String normalFlowTaskName) throws BonitaException {
        final Expression timerExpr = new ExpressionBuilder().createConstantLongExpression(timerValue);
        return deployAndEnableProcessWithBoundaryTimerEvent(timerType, timerExpr, interrupting, taskWithBoundaryName, exceptionTaskName, normalFlowTaskName);
    }

    /**
     * Deploy and enable a process with a human task having a timer boundary event followed by another human task without boundary
     *
     * @param timerType
     *        the timer time
     * @param timerExpr
     *        the timer value
     * @param interrupting
     *        define whether the boundary is interrupting or not
     * @param taskWithBoundaryName
     *        the name of user task containing the boundary event
     * @param exceptionTaskName
     *        the name of human task reached by exception flow
     * @param normalFlowTaskName
     *        name of human task following the task containing the boundary (normal flow)
     * @return
     * @throws BonitaException
     * @throws InvalidProcessDefinitionException
     * @since 6.0
     */
    public ProcessDefinition deployAndEnableProcessWithBoundaryTimerEvent(final TimerType timerType, final Expression timerExpr, final boolean interrupting,
            final String taskWithBoundaryName, final String exceptionTaskName, final String normalFlowTaskName) throws BonitaException {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("pTimerBoundary", "2.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addStartEvent("start");
        final UserTaskDefinitionBuilder userTaskDefinitionBuilder = processDefinitionBuilder.addUserTask(taskWithBoundaryName, ACTOR_NAME);
        userTaskDefinitionBuilder.addBoundaryEvent("timer", interrupting).addTimerEventTriggerDefinition(timerType, timerExpr);
        processDefinitionBuilder.addUserTask(exceptionTaskName, ACTOR_NAME);
        processDefinitionBuilder.addUserTask(normalFlowTaskName, ACTOR_NAME);
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", taskWithBoundaryName);
        processDefinitionBuilder.addTransition(taskWithBoundaryName, normalFlowTaskName);
        processDefinitionBuilder.addTransition("timer", exceptionTaskName);

        return deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, user);
    }

    public ProcessDefinition deployAndEnableProcessWithBoundaryTimerEventOnHumanTask(final long timerValue, final boolean withData) throws BonitaException {
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
        userTaskDefinitionBuilder.addUserTask(EXCEPTION_STEP, ACTOR_NAME);
        userTaskDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", "step1");
        processDefinitionBuilder.addTransition("step1", "step2");
        processDefinitionBuilder.addTransition("Boundary timer", EXCEPTION_STEP);
        processDefinitionBuilder.addTransition(EXCEPTION_STEP, "end");

        return deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, user);
    }

    /**
     * Deploy and enable a process containing a timer boundary event attached to a call activity
     *
     * @param timerDuration
     *        after how long time the boundary will be triggered
     * @param interrupting
     *        define whether the boundary is interrupting or not
     * @param targetProcessName
     *        the name of called process
     * @return
     * @throws InvalidExpressionException
     * @throws BonitaException
     * @throws InvalidProcessDefinitionException
     * @since 6.0
     */
    public ProcessDefinition deployAndEnableProcessWithBoundaryTimerEventOnCallActivity(final long timerDuration, final boolean interrupting,
            final String targetProcessName) throws BonitaException {
        final Expression timerExpr = new ExpressionBuilder().createConstantLongExpression(timerDuration);
        final Expression targetProcessNameExpr = new ExpressionBuilder().createConstantStringExpression(targetProcessName);
        final Expression targetProcessVersionExpr = new ExpressionBuilder().createConstantStringExpression("1.0");
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("processWithCallActivityAndBoundaryEvent",
                "2.0");
        processDefinitionBuilder.addActor(ACTOR_NAME).addStartEvent("start");
        final CallActivityBuilder callActivityBuilder = processDefinitionBuilder.addCallActivity("callActivity", targetProcessNameExpr,
                targetProcessVersionExpr);
        callActivityBuilder.addBoundaryEvent("timer", interrupting).addTimerEventTriggerDefinition(TimerType.DURATION, timerExpr);
        processDefinitionBuilder.addUserTask(EXCEPTION_STEP, ACTOR_NAME).addUserTask(PARENT_PROCESS_USER_TASK_NAME, ACTOR_NAME).addEndEvent("end")
                .addTransition("start", "callActivity").addTransition("callActivity", PARENT_PROCESS_USER_TASK_NAME)
                .addTransition(PARENT_PROCESS_USER_TASK_NAME, "end").addTransition("timer", EXCEPTION_STEP);

        return deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, user);
    }

    /**
     * Deploy and enable a process with a timer boundary event attached to a multi-instance
     *
     * @param timerValue
     *        after how long time the boundary will be triggered
     * @param interrupting
     *        define whether the boundary is interrupting or not
     * @param multiTaskName
     *        the multi-instance name
     * @param loopCardinality
     *        the multi-instance cardinality
     * @param isSequential
     *        define whether the multi-instance is sequential or parallel
     * @param normalFlowTaskName
     *        the name of user task following the multi-instance in the normal flow
     * @param exceptionFlowTaskName
     *        the name of the user task reached by the exception flow
     * @return
     * @throws BonitaException
     * @throws InvalidProcessDefinitionException
     * @since 6.0
     */
    public ProcessDefinition deployAndEnableProcessMultiInstanceWithBoundaryEvent(final long timerValue, final boolean interrupting,
            final String multiTaskName, final int loopCardinality, final boolean isSequential, final String normalFlowTaskName,
            final String exceptionFlowTaskName) throws BonitaException {
        final Expression timerExpr = new ExpressionBuilder().createConstantLongExpression(timerValue);

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("processWithMultiInstanceAndBoundaryEvent", "1.0");
        processBuilder.addActor(ACTOR_NAME).addStartEvent("start");

        final UserTaskDefinitionBuilder userTaskBuilder = processBuilder.addUserTask(multiTaskName, ACTOR_NAME);
        userTaskBuilder.addMultiInstance(isSequential, new ExpressionBuilder().createConstantIntegerExpression(loopCardinality));
        userTaskBuilder.addBoundaryEvent("timer", interrupting).addTimerEventTriggerDefinition(TimerType.DURATION, timerExpr);

        processBuilder.addUserTask(normalFlowTaskName, ACTOR_NAME).addUserTask(exceptionFlowTaskName, ACTOR_NAME).addEndEvent("end");
        processBuilder.addTransition("start", multiTaskName);
        processBuilder.addTransition(multiTaskName, normalFlowTaskName);
        processBuilder.addTransition(normalFlowTaskName, "end");
        processBuilder.addTransition("timer", exceptionFlowTaskName);

        return deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, user);
    }

    /**
     * Deploy and enable a process with a timer boundary event attached to a loop activity
     *
     * @param timerValue
     *        after how long time the boundary will be triggered
     * @param interrupting
     *        define whether the boundary is interrupting or not
     * @param loopMax
     *        how many loops will be executed
     * @param loopActivityName
     *        the name of the loop activity
     * @param normalFlowStepName
     *        the name of the user task following the loop activity in the normal flow
     * @param exceptionFlowStepName
     *        the name of the user task reached by the exception flow
     * @return
     * @throws BonitaException
     * @throws InvalidProcessDefinitionException
     * @since 6.0
     */
    public ProcessDefinition deployAndEnableProcessWithBoundaryTimerEventOnLoopActivity(final long timerValue, final boolean interrupting, final int loopMax,
            final String loopActivityName, final String normalFlowStepName, final String exceptionFlowStepName) throws BonitaException {
        final Expression timerExpr = new ExpressionBuilder().createConstantLongExpression(timerValue);
        final Expression condition = new ExpressionBuilder().createConstantBooleanExpression(true);

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("processWithMultiInstanceAndBoundaryEvent", "1.0");
        processBuilder.addActor(ACTOR_NAME).addStartEvent("start");

        final UserTaskDefinitionBuilder userTaskBuilder = processBuilder.addUserTask(loopActivityName, ACTOR_NAME);
        userTaskBuilder.addLoop(false, condition, new ExpressionBuilder().createConstantIntegerExpression(loopMax));
        userTaskBuilder.addBoundaryEvent("timer", interrupting).addTimerEventTriggerDefinition(TimerType.DURATION, timerExpr);

        processBuilder.addUserTask(normalFlowStepName, ACTOR_NAME).addUserTask(exceptionFlowStepName, ACTOR_NAME).addEndEvent("end")
                .addTransition("start", loopActivityName).addTransition(loopActivityName, normalFlowStepName).addTransition(normalFlowStepName, "end")
                .addTransition("timer", exceptionFlowStepName);

        return deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, user);
    }

    protected void waitForUserTasksAndExecuteIt(final String taskName, final ProcessInstance processInstance, final int nbOfRemainingInstances)
            throws Exception {
        for (int i = 0; i < nbOfRemainingInstances; i++) {
            waitForUserTaskAndExecuteIt(processInstance, taskName, user);
        }
    }

    protected void executeRemainingParallelMultiInstances(final String taskName, final ProcessInstance processInstance, final int nbOfRemainingInstances)
            throws Exception {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, Math.max(10, nbOfRemainingInstances));
        builder.filter(ActivityInstanceSearchDescriptor.NAME, taskName);
        builder.filter(ActivityInstanceSearchDescriptor.PROCESS_INSTANCE_ID, processInstance.getId());
        builder.filter(ActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.USER_TASK);
        final SearchResult<ActivityInstance> searchResult = getProcessAPI().searchActivities(builder.done());
        assertEquals(nbOfRemainingInstances, searchResult.getCount());
        for (final ActivityInstance activity : searchResult.getResult()) {
            assignAndExecuteStep(activity, user.getId());
        }
    }

    public ProcessDefinition deployAndEnableProcessWithInterruptingAndNonInterruptingTimer(final long interruptTimer, final long nonInterruptingTimer,
            final String taskWithBoundaryName, final String interruptExceptionTaskName, final String nonInterruptExceptionTaskName,
            final String interruptTimerName,
            final String nonInterruptTimerName) throws BonitaException {
        final String normalFlowTaskName = "normalFlow";
        final Expression interruptTimerExpr = new ExpressionBuilder().createConstantLongExpression(interruptTimer);
        final Expression nonInterruptTimerExpr = new ExpressionBuilder().createConstantLongExpression(nonInterruptingTimer);
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("pTimerBoundary", "2.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addStartEvent("start");
        final UserTaskDefinitionBuilder userTaskDefinitionBuilder = processDefinitionBuilder.addUserTask(taskWithBoundaryName, ACTOR_NAME);
        userTaskDefinitionBuilder.addBoundaryEvent(interruptTimerName, true).addTimerEventTriggerDefinition(TimerType.DURATION, interruptTimerExpr);
        userTaskDefinitionBuilder.addBoundaryEvent(nonInterruptTimerName, false).addTimerEventTriggerDefinition(TimerType.DURATION, nonInterruptTimerExpr);
        processDefinitionBuilder.addUserTask(interruptExceptionTaskName, ACTOR_NAME);
        processDefinitionBuilder.addUserTask(nonInterruptExceptionTaskName, ACTOR_NAME);
        processDefinitionBuilder.addUserTask(normalFlowTaskName, ACTOR_NAME);
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", taskWithBoundaryName);
        processDefinitionBuilder.addTransition(taskWithBoundaryName, normalFlowTaskName);
        processDefinitionBuilder.addTransition(interruptTimerName, interruptExceptionTaskName);
        processDefinitionBuilder.addTransition(nonInterruptTimerName, nonInterruptExceptionTaskName);

        return deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, user);
    }

    public ProcessDefinition deployAndEnableProcessWithEndThrowErrorEvent(final String processName, final String errorCode)
            throws BonitaException {
        final ProcessDefinitionBuilder calledProcess = new ProcessDefinitionBuilder().createNewInstance(processName, "1.0");
        calledProcess.addActor(ACTOR_NAME);
        calledProcess.addStartEvent("start");
        calledProcess.addUserTask("calledStep1", ACTOR_NAME);
        calledProcess.addUserTask("calledStep2", ACTOR_NAME);
        calledProcess.addEndEvent("endError").addErrorEventTrigger(errorCode);
        calledProcess.addEndEvent("end").addTerminateEventTrigger();
        calledProcess.addTransition("start", "calledStep1");
        calledProcess.addTransition("start", "calledStep2");
        calledProcess.addTransition("calledStep1", "endError");
        calledProcess.addTransition("calledStep2", "end");
        return deployAndEnableProcessWithActor(calledProcess.done(), ACTOR_NAME, user);
    }

    public ProcessDefinition deployAndEnableProcessWithBoundaryErrorEventOnCallActivity(final String processName, final String targetProcessName,
            final String callActivityName, final String errorCode, final String ACTOR_NAME) throws BonitaException {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(processName, "1.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addStartEvent("start");
        final CallActivityBuilder callActivityBuilder = processDefinitionBuilder.addCallActivity(callActivityName,
                new ExpressionBuilder().createConstantStringExpression(targetProcessName), new ExpressionBuilder().createConstantStringExpression("1.0"));
        callActivityBuilder.addBoundaryEvent("error", true).addErrorEventTrigger(errorCode);
        processDefinitionBuilder.addUserTask(EXCEPTION_STEP, ACTOR_NAME);
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", callActivityName);
        processDefinitionBuilder.addTransition(callActivityName, "step2");
        processDefinitionBuilder.addTransition("error", EXCEPTION_STEP);
        processDefinitionBuilder.addTransition(EXCEPTION_STEP, "end");
        processDefinitionBuilder.addTransition("step2", "end");
        return deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, user);
    }

    public ProcessDefinition deployAndEnableSubProcessWhichThrowsAnErrorEvent(final String processName, final String errorCode)
            throws BonitaException {
        final ProcessDefinitionBuilder process = new ProcessDefinitionBuilder().createNewInstance(processName, "1.0");
        process.addStartEvent("start");
        process.addEndEvent("end").addErrorEventTrigger(errorCode);
        process.addTransition("start", "end");
        return deployAndEnableProcess(process.done());
    }

    public ProcessDefinition deployAndEnableMidProcessWhichContainsACallActivity(final String processName, final String targetProcessName)
            throws BonitaException {
        final ProcessDefinitionBuilder process = new ProcessDefinitionBuilder().createNewInstance(processName, "1.0");
        process.addStartEvent("start");
        process.addCallActivity("ca",
                new ExpressionBuilder().createConstantStringExpression(targetProcessName), new ExpressionBuilder().createConstantStringExpression("1.0"));
        process.addEndEvent("end");
        process.addTransition("start", "ca");
        process.addTransition("ca", "end");
        return deployAndEnableProcess(process.done());
    }

    public ProcessDefinition deployAndEnableProcessWithBoundaryErrorEventOnMICallActivity(final String processName, final String targetProcessName,
            final String errorCode, final String ACTOR_NAME) throws BonitaException {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(processName, "1.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addStartEvent("start");
        final CallActivityBuilder callActivityBuilder = processDefinitionBuilder.addCallActivity("step1",
                new ExpressionBuilder().createConstantStringExpression(targetProcessName), new ExpressionBuilder().createConstantStringExpression("1.0"));
        callActivityBuilder.addBoundaryEvent("error", true).addErrorEventTrigger(errorCode);
        callActivityBuilder.addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(1));
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addUserTask(EXCEPTION_STEP, ACTOR_NAME);
        processDefinitionBuilder.addTransition("start", "step1");
        processDefinitionBuilder.addTransition("step1", "step2");
        processDefinitionBuilder.addTransition("error", EXCEPTION_STEP);
        return deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, user);
    }

    public ProcessDefinition deployAndEnableProcessWithTimerEventSubProcess(final long timerDuration) throws BonitaException {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithEventSubProcess", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addStartEvent("start");
        builder.addUserTask("step1", ACTOR_NAME);
        builder.addEndEvent("end");
        builder.addTransition("start", "step1");
        builder.addTransition("step1", "end");
        final SubProcessDefinitionBuilder subProcessBuilder = builder.addSubProcess("eventSubProcess", true).getSubProcessBuilder();
        subProcessBuilder.addStartEvent("timerStart").addTimerEventTriggerDefinition(TimerType.DURATION,
                new ExpressionBuilder().createConstantLongExpression(timerDuration));
        subProcessBuilder.addUserTask("subStep", ACTOR_NAME);
        subProcessBuilder.addEndEvent("endSubProcess");
        subProcessBuilder.addTransition("timerStart", "subStep");
        subProcessBuilder.addTransition("subStep", "endSubProcess");
        return deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);
    }

    public ProcessDefinition deployAndEnableProcessWithTimerEventSubProcessAndData(final long timerDuration) throws BonitaException {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithEventSubProcess", "1.0");
        builder.addShortTextData("content", new ExpressionBuilder().createConstantStringExpression("parentVar"));
        builder.addIntegerData("count", new ExpressionBuilder().createConstantIntegerExpression(1));
        builder.addActor(ACTOR_NAME);
        builder.addStartEvent("start");
        builder.addUserTask("step1", ACTOR_NAME);
        builder.addEndEvent("end");
        builder.addTransition("start", "step1");
        builder.addTransition("step1", "end");
        final SubProcessDefinitionBuilder subProcessBuilder = builder.addSubProcess("eventSubProcess", true).getSubProcessBuilder();
        subProcessBuilder.addShortTextData("content", new ExpressionBuilder().createConstantStringExpression("childVar"));
        subProcessBuilder.addDoubleData("value", new ExpressionBuilder().createConstantDoubleExpression(10.0));
        subProcessBuilder.addStartEvent("timerStart").addTimerEventTriggerDefinition(TimerType.DURATION,
                new ExpressionBuilder().createConstantLongExpression(timerDuration));
        subProcessBuilder.addUserTask("subStep", ACTOR_NAME).addShortTextData("content",
                new ExpressionBuilder().createConstantStringExpression("childActivityVar"));
        subProcessBuilder.addEndEvent("endSubProcess");
        subProcessBuilder.addTransition("timerStart", "subStep");
        subProcessBuilder.addTransition("subStep", "endSubProcess");
        final DesignProcessDefinition processDefinition = builder.done();
        return deployAndEnableProcessWithActor(processDefinition, ACTOR_NAME, user);
    }

    public ProcessDefinition deployAndEnableProcessWithErrorEventSubProcessAndData(final String catchErrorCode, final String throwErroCode,
            final String subProcStartEventName) throws BonitaException {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithEventSubProcess", "1.0");
        builder.addShortTextData("content", new ExpressionBuilder().createConstantStringExpression("parentVar"));
        builder.addIntegerData("count", new ExpressionBuilder().createConstantIntegerExpression(1));
        builder.addActor(ACTOR_NAME);
        builder.addStartEvent("start");
        builder.addUserTask("step1", ACTOR_NAME);
        builder.addUserTask("step2", ACTOR_NAME);
        builder.addEndEvent("end");
        builder.addEndEvent("endError").addErrorEventTrigger(throwErroCode);
        builder.addTransition("start", "step1");
        builder.addTransition("start", "step2");
        builder.addTransition("step1", "end");
        builder.addTransition("step2", "endError");
        final SubProcessDefinitionBuilder subProcessBuilder = builder.addSubProcess(BuildTestUtil.EVENT_SUB_PROCESS_NAME, true).getSubProcessBuilder();
        subProcessBuilder.addShortTextData("content", new ExpressionBuilder().createConstantStringExpression("childVar"));
        subProcessBuilder.addDoubleData("value", new ExpressionBuilder().createConstantDoubleExpression(10.0));
        subProcessBuilder.addStartEvent(subProcStartEventName).addErrorEventTrigger(catchErrorCode);
        subProcessBuilder.addUserTask("subStep", ACTOR_NAME).addShortTextData("content",
                new ExpressionBuilder().createConstantStringExpression("childActivityVar"));
        subProcessBuilder.addEndEvent("endSubProcess");
        subProcessBuilder.addTransition(subProcStartEventName, "subStep");
        subProcessBuilder.addTransition("subStep", "endSubProcess");
        return deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);
    }

    public ProcessDefinition deployAndEnableProcessWithErrorEventSubProcessAndDataOnlyInRoot(final String errorCode, final String subProcStartEventName,
            final String rootUserTaskName, final String subProcUserTaskName, final String dataName, final String dataValue) throws BonitaException {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithEventSubProcess", "1.0");
        builder.addShortTextData(dataName, new ExpressionBuilder().createConstantStringExpression(dataValue));
        builder.addActor(ACTOR_NAME);
        builder.addStartEvent("start");
        builder.addUserTask(rootUserTaskName, ACTOR_NAME);
        builder.addEndEvent("end");
        builder.addEndEvent("endError").addErrorEventTrigger(errorCode);
        builder.addTransition("start", rootUserTaskName);
        builder.addTransition(rootUserTaskName, "endError");
        final SubProcessDefinitionBuilder subProcessBuilder = builder.addSubProcess(BuildTestUtil.EVENT_SUB_PROCESS_NAME, true).getSubProcessBuilder();
        subProcessBuilder.addStartEvent(subProcStartEventName).addErrorEventTrigger(errorCode);
        subProcessBuilder.addUserTask(subProcUserTaskName, ACTOR_NAME);
        subProcessBuilder.addEndEvent("endSubProcess");
        subProcessBuilder.addTransition(subProcStartEventName, subProcUserTaskName);
        subProcessBuilder.addTransition(subProcUserTaskName, "endSubProcess");
        return deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);
    }

    public ProcessDefinition deployAndEnableProcessWithErrorEventSubProcessAndDataOnlyInSubProc(final String errorCode, final String subProcStartEventName,
            final String rootUserTaskName, final String subProcUserTaskName, final String dataName, final String dataValue) throws BonitaException {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithEventSubProcess", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addStartEvent("start");
        builder.addUserTask(rootUserTaskName, ACTOR_NAME);
        builder.addEndEvent("end");
        builder.addEndEvent("endError").addErrorEventTrigger(errorCode);
        builder.addTransition("start", rootUserTaskName);
        builder.addTransition(rootUserTaskName, "endError");
        final SubProcessDefinitionBuilder subProcessBuilder = builder.addSubProcess(BuildTestUtil.EVENT_SUB_PROCESS_NAME, true).getSubProcessBuilder();
        subProcessBuilder.addShortTextData(dataName, new ExpressionBuilder().createConstantStringExpression(dataValue));
        subProcessBuilder.addStartEvent(subProcStartEventName).addErrorEventTrigger(errorCode);
        subProcessBuilder.addUserTask(subProcUserTaskName, ACTOR_NAME);
        subProcessBuilder.addEndEvent("endSubProcess");
        subProcessBuilder.addTransition(subProcStartEventName, subProcUserTaskName);
        subProcessBuilder.addTransition(subProcUserTaskName, "endSubProcess");
        return deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);
    }

    public ProcessDefinition deployAndEnableProcessWithErrorEventSubProcess(final String catchErrorCode, final String throwErrorCode,
            final String subProcStartEventName) throws BonitaException {
        final Expression transitionCondition = new ExpressionBuilder().createDataExpression("throwException", Boolean.class.getName());
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithEventSubProcess", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addBooleanData("throwException", new ExpressionBuilder().createConstantBooleanExpression(true));
        builder.addStartEvent("start");
        builder.addUserTask("step1", ACTOR_NAME);
        builder.addUserTask("step2", ACTOR_NAME);
        builder.addEndEvent("end");
        builder.addEndEvent("endError").addErrorEventTrigger(throwErrorCode);
        builder.addTransition("start", "step1");
        builder.addTransition("start", "step2");
        builder.addTransition("step1", "end");
        builder.addTransition("step2", "endError", transitionCondition);
        builder.addDefaultTransition("step2", "end");
        final SubProcessDefinitionBuilder subProcessBuilder = builder.addSubProcess(BuildTestUtil.EVENT_SUB_PROCESS_NAME, true).getSubProcessBuilder();
        if (catchErrorCode == null) {
            subProcessBuilder.addStartEvent(subProcStartEventName).addErrorEventTrigger();
        } else {
            subProcessBuilder.addStartEvent(subProcStartEventName).addErrorEventTrigger(catchErrorCode);
        }
        subProcessBuilder.addUserTask("subStep", ACTOR_NAME);
        subProcessBuilder.addEndEvent("endSubProcess");
        subProcessBuilder.addTransition(subProcStartEventName, "subStep");
        subProcessBuilder.addTransition("subStep", "endSubProcess");
        return deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);
    }

    public ProcessDefinition deployAndEnableProcessWithTestConnectorThatThrowException(final ProcessDefinitionBuilder processDefinitionBuilder)
            throws BonitaException, IOException {
        return deployAndEnableProcessWithActorAndConnector(processDefinitionBuilder, ACTOR_NAME, user, "TestConnectorThatThrowException.impl",
                TestConnectorThatThrowException.class, "TestConnectorThatThrowException.jar");
    }

    public ProcessDefinition deployAndEnableProcessWithBoundaryMessageEvent(final String message) throws BonitaException {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("pMessageBoundary", "2.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addStartEvent("start");
        final UserTaskDefinitionBuilder userTaskDefinitionBuilder = processDefinitionBuilder.addUserTask("step1", ACTOR_NAME);
        userTaskDefinitionBuilder.addBoundaryEvent(BOUNDARY_NAME, true).addMessageEventTrigger(message);
        userTaskDefinitionBuilder.addUserTask(EXCEPTION_STEP, ACTOR_NAME);
        userTaskDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", "step1");
        processDefinitionBuilder.addTransition("step1", "step2");
        processDefinitionBuilder.addTransition(BOUNDARY_NAME, EXCEPTION_STEP);

        return deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, user);
    }

    public ProcessDefinition deployAndEnableProcessWithBoundarySignalEvent(final String signalName) throws BonitaException {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("pSignalBoundary", "2.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addStartEvent("start");
        final UserTaskDefinitionBuilder userTaskDefinitionBuilder = processDefinitionBuilder.addUserTask("step1", ACTOR_NAME);
        userTaskDefinitionBuilder.addBoundaryEvent("waitSignal", true).addSignalEventTrigger(signalName);
        userTaskDefinitionBuilder.addUserTask(EXCEPTION_STEP, ACTOR_NAME);
        userTaskDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", "step1");
        processDefinitionBuilder.addTransition("step1", "step2");
        processDefinitionBuilder.addTransition("waitSignal", EXCEPTION_STEP);
        return deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, user);
    }

    public ProcessDefinition deployAndEnableProcessWithBoundaryMessageEventOnLoopActivity(final int loopMax) throws BonitaException {
        final Expression condition = new ExpressionBuilder().createConstantBooleanExpression(true);

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("processWithLoopActivityAndBoundaryEvent", "1.0");
        processBuilder.addActor(ACTOR_NAME).addStartEvent("start");

        final UserTaskDefinitionBuilder userTaskBuilder = processBuilder.addUserTask("step1", ACTOR_NAME);
        userTaskBuilder.addLoop(false, condition, new ExpressionBuilder().createConstantIntegerExpression(loopMax));
        userTaskBuilder.addBoundaryEvent(BOUNDARY_NAME, true).addMessageEventTrigger("MyMessage");

        processBuilder.addUserTask("step2", ACTOR_NAME).addUserTask(EXCEPTION_STEP, ACTOR_NAME).addEndEvent("end").addTransition("start", "step1")
                .addTransition("step1", "step2").addTransition("step2", "end").addTransition(BOUNDARY_NAME, EXCEPTION_STEP);

        return deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, user);
    }

    /**
     * Deploy and enable a simple process: start event -> user task -> end event
     *
     * @param processName
     *        the process name
     * @param userTaskName
     *        the user task name
     * @return
     * @throws BonitaException
     * @throws InvalidProcessDefinitionException
     * @since 6.0
     */
    public ProcessDefinition deployAndEnableSimpleProcess(final String processName, final String userTaskName) throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(processName, "1.0");
        processBuilder.addActor(ACTOR_NAME).addStartEvent("startCA").addUserTask(userTaskName, ACTOR_NAME).addEndEvent("endCA")
                .addTransition("startCA", userTaskName).addTransition(userTaskName, "endCA");
        return deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, user);
    }

    public ProcessDefinition deployAndEnableProcessWithBoundaryMessageEventOnCallActivity() throws BonitaException {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("pMessageBoundary", "2.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addStartEvent("start");
        final CallActivityBuilder callActivityBuilder = processDefinitionBuilder.addCallActivity("step1",
                new ExpressionBuilder().createConstantStringExpression("calledProcess"), new ExpressionBuilder().createConstantStringExpression("1.0"));
        callActivityBuilder.addBoundaryEvent(BOUNDARY_NAME, true).addMessageEventTrigger("MyMessage");
        processDefinitionBuilder.addUserTask(EXCEPTION_STEP, ACTOR_NAME);
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", "step1");
        processDefinitionBuilder.addTransition("step1", "step2");
        processDefinitionBuilder.addTransition(BOUNDARY_NAME, EXCEPTION_STEP);
        return deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, user);
    }

    public ProcessDefinition deployAndEnableProcessWithBoundaryMessageEventOnMultiInstance(final int loopCardinality, final boolean isSequential)
            throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("processWithBoundaryMessageEventAndMultiInstance",
                "1.0");
        processBuilder.addActor(ACTOR_NAME).addStartEvent("start");

        final UserTaskDefinitionBuilder userTaskBuilder = processBuilder.addUserTask("step1", ACTOR_NAME);
        userTaskBuilder.addMultiInstance(isSequential, new ExpressionBuilder().createConstantIntegerExpression(loopCardinality));
        userTaskBuilder.addBoundaryEvent(BOUNDARY_NAME, true).addMessageEventTrigger("MyMessage");

        processBuilder.addUserTask("step2", ACTOR_NAME).addUserTask(EXCEPTION_STEP, ACTOR_NAME).addEndEvent("end").addTransition("start", "step1")
                .addTransition("step1", "step2").addTransition("step2", "end").addTransition(BOUNDARY_NAME, EXCEPTION_STEP);

        return deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, user);
    }

    public ProcessDefinition deployAndEnableProcessWithCallActivity(final String targetProcessName, final String targetVersion) throws BonitaException {
        final Expression targetProcessExpr = new ExpressionBuilder().createConstantStringExpression(targetProcessName);
        final Expression targetVersionExpr = new ExpressionBuilder().createConstantStringExpression(targetVersion);
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithCallActivity", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addStartEvent("start");
        builder.addCallActivity("callActivity", targetProcessExpr, targetVersionExpr);
        builder.addUserTask("step2", ACTOR_NAME);
        builder.addEndEvent(PARENT_END);
        builder.addTransition("start", "callActivity");
        builder.addTransition("callActivity", "step2");
        builder.addTransition("step2", PARENT_END);
        return deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);
    }

    public ProcessDefinition deployAndEnableProcessWithMessageEventSubProcess() throws BonitaException {
        final ProcessDefinitionBuilder builder = buildParentProcessDefinition(false, false);
        buildSubProcessDefinition(builder, false, null, false);
        return deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);
    }

    public ProcessDefinition deployAndEnableProcessWithMessageEventSubProcessAndData(final List<BEntry<Expression, Expression>> correlations)
            throws BonitaException {
        final ProcessDefinitionBuilder builder = buildParentProcessDefinition(false, true);
        buildSubProcessDefinition(builder, true, correlations, false);
        return deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);
    }

    public ProcessDefinitionBuilder buildParentProcessDefinition(final boolean withIntermediateThrowEvent, final boolean withData)
            throws InvalidExpressionException {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithEventSubProcess", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addStartEvent("start");
        builder.addUserTask(PARENT_PROCESS_USER_TASK_NAME, ACTOR_NAME);
        builder.addEndEvent(PARENT_END);
        builder.addTransition("start", PARENT_PROCESS_USER_TASK_NAME);

        if (withIntermediateThrowEvent) {
            builder.addIntermediateThrowEvent(SIGNAL_NAME).addSignalEventTrigger(SIGNAL_NAME);
            builder.addUserTask(AFTER_SIGNAL, ACTOR_NAME);
            builder.addTransition(PARENT_PROCESS_USER_TASK_NAME, SIGNAL_NAME);
            builder.addTransition(SIGNAL_NAME, AFTER_SIGNAL);
            builder.addTransition(SIGNAL_NAME, PARENT_END);
        } else {
            builder.addTransition(PARENT_PROCESS_USER_TASK_NAME, PARENT_END);
        }

        if (withData) {
            builder.addShortTextData(SHORT_DATA_NAME, new ExpressionBuilder().createConstantStringExpression("parentVar"));
            builder.addIntegerData(INT_DATA_NAME, new ExpressionBuilder().createConstantIntegerExpression(1));
        }
        return builder;
    }

    public void buildSubProcessDefinition(final ProcessDefinitionBuilder builder, final boolean withData,
            final List<BEntry<Expression, Expression>> correlations, final boolean isSignal) throws InvalidExpressionException {
        final SubProcessDefinitionBuilder subProcessBuilder = builder.addSubProcess(SUB_PROCESS_NAME, true).getSubProcessBuilder();
        final StartEventDefinitionBuilder startEventDefinitionBuilder = subProcessBuilder.addStartEvent(SUB_PROCESS_START_NAME);
        if (isSignal) {
            startEventDefinitionBuilder.addSignalEventTrigger(SIGNAL_NAME);
        } else {
            final CatchMessageEventTriggerDefinitionBuilder messageEventTrigger = startEventDefinitionBuilder.addMessageEventTrigger(MESSAGE_NAME);
            if (withData) {
                addCorrelations(messageEventTrigger, correlations);
            }
        }

        final UserTaskDefinitionBuilder userTask = subProcessBuilder.addUserTask(SUB_PROCESS_USER_TASK_NAME, ACTOR_NAME);
        subProcessBuilder.addEndEvent("endSubProcess");
        subProcessBuilder.addTransition(SUB_PROCESS_START_NAME, SUB_PROCESS_USER_TASK_NAME);
        subProcessBuilder.addTransition(SUB_PROCESS_USER_TASK_NAME, "endSubProcess");

        if (withData) {
            subProcessBuilder.addShortTextData(SHORT_DATA_NAME, new ExpressionBuilder().createConstantStringExpression("childVar"));
            subProcessBuilder.addDoubleData("value", new ExpressionBuilder().createConstantDoubleExpression(10.0));
            userTask.addShortTextData(SHORT_DATA_NAME, new ExpressionBuilder().createConstantStringExpression("childActivityVar"));
        }
    }

    public void addCorrelations(final CatchMessageEventTriggerDefinitionBuilder messageEventTrigger, final List<BEntry<Expression, Expression>> correlations) {
        if (correlations != null) {
            for (final BEntry<Expression, Expression> correlation : correlations) {
                messageEventTrigger.addCorrelation(correlation.getKey(), correlation.getValue());
            }
        }
    }

    public void addCorrelations(final ThrowMessageEventTriggerBuilder throwMessageEventTriggerBuilder, final List<BEntry<Expression, Expression>> correlations) {
        if (correlations != null) {
            for (final Entry<Expression, Expression> entry : correlations) {
                throwMessageEventTriggerBuilder.addCorrelation(entry.getKey(), entry.getValue());
            }
        }
    }

    public ProcessDefinition deployAndEnableProcessWithEndMessageEvent(final String targetProcess, final String targetFlowNode) throws BonitaException {
        return deployAndEnableProcessWithEndMessageEvent(targetProcess, targetFlowNode, null, null, null, null);
    }

    public ProcessDefinition deployAndEnableProcessWithEndMessageEvent(final String targetProcess, final String targetFlowNode,
            final List<BEntry<Expression, Expression>> correlations, final Map<String, String> processData, final Map<String, String> messageData,
            final Map<String, String> dataInputMapping) throws BonitaException {
        return deployAndEnableProcessWithEndMessageEvent("Send message in the end", MESSAGE_NAME, targetProcess, targetFlowNode, correlations, processData,
                messageData, dataInputMapping);
    }

    public ProcessDefinition deployAndEnableProcessWithEndMessageEvent(final String processName, final String messageName, final String targetProcess,
            final String targetFlowNode, final List<BEntry<Expression, Expression>> correlations, final Map<String, String> processData,
            final Map<String, String> messageData, final Map<String, String> dataInputMapping) throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder();
        processBuilder.createNewInstance(processName, PROCESS_VERSION);
        addProcessData(processData, processBuilder);
        processBuilder.addStartEvent(START_EVENT_NAME);
        processBuilder.addAutomaticTask("auto1");
        // create expression for target process/flowNode
        final Expression targetProcessExpression = new ExpressionBuilder().createConstantStringExpression(targetProcess);
        final ThrowMessageEventTriggerBuilder throwMessageEventTriggerBuilder;
        if (targetFlowNode != null) {
            final Expression targetFlowNodeExpression = new ExpressionBuilder().createConstantStringExpression(targetFlowNode);
            throwMessageEventTriggerBuilder = processBuilder.addEndEvent("endEvent").addMessageEventTrigger(messageName, targetProcessExpression,
                    targetFlowNodeExpression);
        } else {
            throwMessageEventTriggerBuilder = processBuilder.addEndEvent("endEvent").addMessageEventTrigger(messageName, targetProcessExpression);
        }

        addCorrelations(throwMessageEventTriggerBuilder, correlations);
        addMessageData(messageData, dataInputMapping, throwMessageEventTriggerBuilder);
        processBuilder.addTransition(START_EVENT_NAME, "auto1");
        processBuilder.addTransition("auto1", "endEvent");
        final DesignProcessDefinition designProcessDefinition = processBuilder.done();

        final ProcessDefinition sendMessageProcess = deployAndEnableProcess(designProcessDefinition);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(sendMessageProcess.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        return sendMessageProcess;
    }

    public void addMessageData(final Map<String, String> messageData, final Map<String, String> dataInputMapping,
            final ThrowMessageEventTriggerBuilder throwMessageEventTriggerBuilder) throws InvalidExpressionException {
        if (messageData != null) {
            for (final Entry<String, String> entry : messageData.entrySet()) {
                final Expression displayName = new ExpressionBuilder().createConstantStringExpression(entry.getKey());

                Expression defaultValue = null;
                if (dataInputMapping.containsKey(entry.getKey())) {
                    defaultValue = new ExpressionBuilder().createDataExpression(dataInputMapping.get(entry.getKey()), entry.getValue());
                }
                throwMessageEventTriggerBuilder.addMessageContentExpression(displayName, defaultValue);
            }
        }

    }

    public void addProcessData(final Map<String, String> data, final ProcessDefinitionBuilder processBuilder) {
        if (data != null) {
            for (final Entry<String, String> entry : data.entrySet()) {
                processBuilder.addData(entry.getKey(), entry.getValue(), null);
            }
        }
    }

    public ProcessDefinition deployAndEnableProcessWithIntermediateThrowMessageEvent(final String targetProcess, final String targetFlowNode)
            throws BonitaException {
        return deployAndEnableProcessWithIntermediateThrowMessageEvent(Collections.singletonList(MESSAGE_NAME), Collections.singletonList(targetProcess),
                Collections.singletonList(targetFlowNode));
    }

    public ProcessDefinition deployAndEnableProcessWithIntermediateThrowMessageEvent(final List<String> messageNames, final List<String> targetProcesses,
            final List<String> targetFlowNodes) throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder();
        processBuilder.createNewInstance(SEND_MESSAGE_PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addStartEvent(START_EVENT_NAME);
        processBuilder.addAutomaticTask("auto1");

        final IntermediateThrowEventDefinitionBuilder intermediateThrowEvent = processBuilder.addIntermediateThrowEvent("sendMessage");
        for (final String targetProcess : targetProcesses) {
            // create expression for target process/flowNode
            final Expression targetProcessExpression = new ExpressionBuilder().createConstantStringExpression(targetProcess);
            final int indexOfTargetProcess = targetProcesses.indexOf(targetProcess);
            final Expression targetFlowNodeExpression = new ExpressionBuilder().createConstantStringExpression(targetFlowNodes.get(indexOfTargetProcess));
            intermediateThrowEvent.addMessageEventTrigger(messageNames.get(indexOfTargetProcess), targetProcessExpression, targetFlowNodeExpression);
        }
        processBuilder.addEndEvent("endEvent");
        processBuilder.addTransition(START_EVENT_NAME, "auto1");
        processBuilder.addTransition("auto1", "sendMessage");
        processBuilder.addTransition("sendMessage", "endEvent");
        final DesignProcessDefinition designProcessDefinition = processBuilder.done();

        final ProcessDefinition sendMessageProcess = deployAndEnableProcess(designProcessDefinition);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(sendMessageProcess.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        return sendMessageProcess;
    }

    public ProcessDefinition deployAndEnableProcessWithStartMessageEvent(final Map<String, String> data, final List<Operation> catchMessageOperations)
            throws BonitaException {
        return deployAndEnableProcessWithStartMessageEvent(START_WITH_MESSAGE_PROCESS_NAME, MESSAGE_NAME, data, catchMessageOperations);
    }

    public ProcessDefinition deployAndEnableProcessWithStartMessageEvent(final String processName, final String messageName, final Map<String, String> data,
            final List<Operation> catchMessageOperations) throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder();
        processBuilder.createNewInstance(processName, PROCESS_VERSION);
        addProcessData(data, processBuilder);
        final CatchMessageEventTriggerDefinitionBuilder messageEventTrigger = processBuilder.addStartEvent(START_EVENT_NAME)
                .addMessageEventTrigger(messageName);
        addOperations(messageEventTrigger, catchMessageOperations);
        processBuilder.addActor(ACTOR_NAME);
        processBuilder.addUserTask(START_WITH_MESSAGE_STEP1_NAME, ACTOR_NAME);
        processBuilder.addEndEvent("endEvent");
        processBuilder.addTransition(START_EVENT_NAME, START_WITH_MESSAGE_STEP1_NAME);
        processBuilder.addTransition(START_WITH_MESSAGE_STEP1_NAME, "endEvent");
        final DesignProcessDefinition designProcessDefinition = processBuilder.done();

        final BusinessArchiveBuilder archiveBuilder = new BusinessArchiveBuilder();
        archiveBuilder.createNewBusinessArchive().setProcessDefinition(designProcessDefinition);
        final BusinessArchive receiveMessageArchive = archiveBuilder.done();
        final ProcessDefinition receiveMessageProcess = deployProcess(receiveMessageArchive);

        final List<ActorInstance> actors = getProcessAPI().getActors(receiveMessageProcess.getId(), 0, 1, ActorCriterion.NAME_ASC);
        getProcessAPI().addUserToActor(actors.get(0).getId(), user.getId());

        getProcessAPI().enableProcess(receiveMessageProcess.getId());

        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(receiveMessageProcess.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        return receiveMessageProcess;
    }

    public ProcessDefinition deployAndEnableProcessWithStartMessageEvent(final String processName, final String messageName) throws BonitaException {
        return deployAndEnableProcessWithStartMessageEvent(processName, messageName, Collections.<String, String> emptyMap(),
                Collections.<Operation> emptyList());
    }

    public void addOperations(final CatchMessageEventTriggerDefinitionBuilder messageEventTrigger, final List<Operation> catchMessageOperations) {
        if (catchMessageOperations != null) {
            for (final Operation operation : catchMessageOperations) {
                messageEventTrigger.addOperation(operation);
            }
        }
    }

    public ProcessDefinition deployAndEnableProcessWithIntermediateCatchMessageEvent(final List<BEntry<Expression, Expression>> correlations,
            final Map<String, String> processData, final List<Operation> operations) throws BonitaException {
        return deployAndEnableProcessWithIntermediateCatchMessageEvent(CATCH_MESSAGE_PROCESS_NAME, MESSAGE_NAME, correlations, processData, operations);
    }

    public ProcessDefinition deployAndEnableProcessWithIntermediateCatchMessageEvent(final String processName, final String messageName,
            final List<BEntry<Expression, Expression>> correlations, final Map<String, String> processData, final List<Operation> operations)
            throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder();
        processBuilder.createNewInstance(processName, PROCESS_VERSION);
        addProcessData(processData, processBuilder);
        processBuilder.addStartEvent(START_EVENT_NAME);
        processBuilder.addAutomaticTask("auto1");
        final CatchMessageEventTriggerDefinitionBuilder catchMessageEventTriggerDefinitionBuilder = processBuilder.addIntermediateCatchEvent(CATCH_EVENT_NAME)
                .addMessageEventTrigger(messageName);
        addCorrelations(catchMessageEventTriggerDefinitionBuilder, correlations);
        addOperations(catchMessageEventTriggerDefinitionBuilder, operations);
        processBuilder.addActor(ACTOR_NAME);
        processBuilder.addUserTask(CATCH_MESSAGE_STEP1_NAME, ACTOR_NAME);
        processBuilder.addEndEvent("endEvent");
        processBuilder.addTransition(START_EVENT_NAME, "auto1");
        processBuilder.addTransition("auto1", CATCH_EVENT_NAME);
        processBuilder.addTransition(CATCH_EVENT_NAME, CATCH_MESSAGE_STEP1_NAME);
        processBuilder.addTransition(CATCH_MESSAGE_STEP1_NAME, "endEvent");
        final DesignProcessDefinition designProcessDefinition = processBuilder.done();

        final BusinessArchiveBuilder archiveBuilder = new BusinessArchiveBuilder();
        archiveBuilder.createNewBusinessArchive().setProcessDefinition(designProcessDefinition);
        final BusinessArchive receiveMessaceArchive = archiveBuilder.done();

        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithActor(receiveMessaceArchive, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(receiveMessageProcess.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        return receiveMessageProcess;
    }

    public ProcessDefinition deployAndEnableProcessWithIntraMessageEvent(final String targetProcess, final String targetFlowNode) throws BonitaException {
        return deployAndEnableProcessWithIntraMessageEvent("sendAndReceiveMessageProcess", MESSAGE_NAME, targetProcess, targetFlowNode);
    }

    public ProcessDefinition deployAndEnableProcessWithIntraMessageEvent(final String processName, final String messageName, final String targetProcess,
            final String targetFlowNode) throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder();
        // create expression for target process/flowNode
        final Expression targetProcessExpression = new ExpressionBuilder().createConstantStringExpression(targetProcess);
        final Expression targetFlowNodeExpression = new ExpressionBuilder().createConstantStringExpression(targetFlowNode);
        processBuilder.createNewInstance(processName, PROCESS_VERSION);
        processBuilder.addStartEvent(START_EVENT_NAME);
        processBuilder.addAutomaticTask("auto1");
        processBuilder.addGateway("gateway1", GatewayType.PARALLEL);
        processBuilder.addIntermediateThrowEvent("sendMessage").addMessageEventTrigger(messageName, targetProcessExpression, targetFlowNodeExpression);
        processBuilder.addIntermediateCatchEvent(targetFlowNode).addMessageEventTrigger(messageName);
        processBuilder.addGateway("gateway2", GatewayType.PARALLEL);
        processBuilder.addActor(ACTOR_NAME);
        processBuilder.addUserTask("userTask1", ACTOR_NAME).addUserTask("userTask2", ACTOR_NAME).addUserTask("userTask3", ACTOR_NAME);
        processBuilder.addEndEvent("endEvent");
        processBuilder.addTransition(START_EVENT_NAME, "auto1");
        processBuilder.addTransition("auto1", "gateway1");
        processBuilder.addTransition("gateway1", "userTask2");
        processBuilder.addTransition("userTask2", "sendMessage");
        processBuilder.addTransition("sendMessage", "gateway2");
        processBuilder.addTransition("gateway1", "userTask1");
        processBuilder.addTransition("userTask1", "gateway2");
        processBuilder.addTransition("gateway2", "endEvent");
        processBuilder.addTransition("auto1", targetFlowNode);
        processBuilder.addTransition(targetFlowNode, "userTask3");
        processBuilder.addTransition("userTask3", "endEvent");
        final DesignProcessDefinition designProcessDefinition = processBuilder.done();

        final BusinessArchiveBuilder archiveBuilder = new BusinessArchiveBuilder();
        archiveBuilder.createNewBusinessArchive().setProcessDefinition(designProcessDefinition);
        final BusinessArchive sendAndReceiveMessaceArchive = archiveBuilder.done();
        final ProcessDefinition receiveMessageProcess = getProcessAPI().deploy(sendAndReceiveMessaceArchive);

        final List<ActorInstance> actors = getProcessAPI().getActors(receiveMessageProcess.getId(), 0, 1, ActorCriterion.NAME_ASC);
        getProcessAPI().addUserToActor(actors.get(0).getId(), user.getId());

        getProcessAPI().enableProcess(receiveMessageProcess.getId());

        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(receiveMessageProcess.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        return receiveMessageProcess;
    }

    public ProcessDefinition deployAndEnableProcessWithIntermediateCatchMessageEventAnd1Correlation() throws BonitaException {
        final Map<String, String> data = new HashMap<String, String>();
        data.put("docRef", Integer.class.getName());
        final ArrayList<BEntry<Expression, Expression>> correlations = new ArrayList<BEntry<Expression, Expression>>(1);
        final Expression docCorrelationKey = new ExpressionBuilder().createConstantStringExpression("docKey");
        final Expression docCorrelationValue = new ExpressionBuilder().createDataExpression("docRef", Integer.class.getName());
        correlations.add(new BEntry<Expression, Expression>(docCorrelationKey, docCorrelationValue));
        return deployAndEnableProcessWithIntermediateCatchMessageEvent(correlations, data, null);
    }

    public ProcessDefinition deployAndEnableProcessWithIntermediateCatchMessageEventAnd2Correlations() throws BonitaException {
        final Map<String, String> data = new HashMap<String, String>();
        data.put("docRef", Integer.class.getName());
        data.put("name", String.class.getName());
        final Expression docCorrelationKey = new ExpressionBuilder().createConstantStringExpression("docKey");
        final Expression docCorrelationValue = new ExpressionBuilder().createDataExpression("docRef", Integer.class.getName());
        final Expression nameCorrelationKey = new ExpressionBuilder().createConstantStringExpression("nameKey");
        final Expression nameCorrelationValue = new ExpressionBuilder().createDataExpression("name", String.class.getName());
        final ArrayList<BEntry<Expression, Expression>> correlations = new ArrayList<BEntry<Expression, Expression>>(2);
        correlations.add(new BEntry<Expression, Expression>(docCorrelationKey, docCorrelationValue));
        correlations.add(new BEntry<Expression, Expression>(nameCorrelationKey, nameCorrelationValue));
        return deployAndEnableProcessWithIntermediateCatchMessageEvent(correlations, data, null);
    }

    public ProcessDefinition deployAndEnableProcessWithEndMessageEventAndCorrelation() throws BonitaException {
        final Map<String, String> data = new HashMap<String, String>();
        data.put("docNumber", Integer.class.getName());
        data.put("lastName", String.class.getName());

        final ArrayList<BEntry<Expression, Expression>> correlations = new ArrayList<BEntry<Expression, Expression>>(2);
        final Expression docCorrelationKey = new ExpressionBuilder().createConstantStringExpression("docKey");
        final Expression docCorrelationValue = new ExpressionBuilder().createDataExpression("docNumber", Integer.class.getName());
        final Expression nameCorrelationKey = new ExpressionBuilder().createConstantStringExpression("nameKey");
        final Expression nameCorrelationValue = new ExpressionBuilder().createDataExpression("lastName", String.class.getName());
        correlations.add(new BEntry<Expression, Expression>(docCorrelationKey, docCorrelationValue));
        correlations.add(new BEntry<Expression, Expression>(nameCorrelationKey, nameCorrelationValue));

        return deployAndEnableProcessWithEndMessageEvent(CATCH_MESSAGE_PROCESS_NAME, CATCH_EVENT_NAME, correlations, data, null, null);
    }

    public ProcessDefinition deployAndEnableProcessWithBoundarySignalEventOnLoopActivity(final int loopMax) throws BonitaException {
        final Expression condition = new ExpressionBuilder().createConstantBooleanExpression(true);

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("processWithMultiInstanceAndBoundaryEvent", "1.0");
        processBuilder.addActor(ACTOR_NAME).addStartEvent("start");

        final UserTaskDefinitionBuilder userTaskBuilder = processBuilder.addUserTask("step1", ACTOR_NAME);
        userTaskBuilder.addLoop(false, condition, new ExpressionBuilder().createConstantIntegerExpression(loopMax));
        userTaskBuilder.addBoundaryEvent("signal", true).addSignalEventTrigger("MySignal");

        processBuilder.addUserTask("step2", ACTOR_NAME).addUserTask(EXCEPTION_STEP, ACTOR_NAME).addEndEvent("end").addTransition("start", "step1")
                .addTransition("step1", "step2").addTransition("step2", "end").addTransition("signal", EXCEPTION_STEP);

        return deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, user);
    }

    public ProcessDefinition deployAndEnableProcessWithBoundarySignalEventOnCallActivity(final String signalName)
            throws BonitaException {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("pSignalBoundary", "2.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addStartEvent("start");
        final CallActivityBuilder callActivityBuilder = processDefinitionBuilder.addCallActivity("step1",
                new ExpressionBuilder().createConstantStringExpression("calledProcess"), new ExpressionBuilder().createConstantStringExpression("1.0"));
        callActivityBuilder.addBoundaryEvent("signal", true).addSignalEventTrigger(signalName);
        processDefinitionBuilder.addUserTask(EXCEPTION_STEP, ACTOR_NAME);
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", "step1");
        processDefinitionBuilder.addTransition("step1", "step2");
        processDefinitionBuilder.addTransition("step2", "end");
        processDefinitionBuilder.addTransition("signal", EXCEPTION_STEP);
        return deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, user);
    }

    public ProcessDefinition deployAndEnableProcessWithBoundarySignalEventOnMultiInstance(final int loopCardinality, final boolean isSequential)
            throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("processWithBoundarySignalEventAndMultiInstance",
                "1.0");
        processBuilder.addActor(ACTOR_NAME).addStartEvent("start");

        final UserTaskDefinitionBuilder userTaskBuilder = processBuilder.addUserTask("step1", ACTOR_NAME);
        userTaskBuilder.addMultiInstance(isSequential, new ExpressionBuilder().createConstantIntegerExpression(loopCardinality));
        userTaskBuilder.addBoundaryEvent("signal", true).addSignalEventTrigger("MySignal");

        processBuilder.addUserTask("step2", ACTOR_NAME).addUserTask(EXCEPTION_STEP, ACTOR_NAME).addEndEvent("end").addTransition("start", "step1")
                .addTransition("step1", "step2").addTransition("step2", "end").addTransition("signal", EXCEPTION_STEP);

        return deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, user);
    }

    public ProcessDefinition deployAndEnableProcessWithSignalEventSubProcess(final boolean withIntermediateThrowEvent, final boolean withData)
            throws BonitaException {
        final ProcessDefinitionBuilder builder = buildParentProcessDefinition(withIntermediateThrowEvent, withData);
        buildSubProcessDefinition(builder, withData, null, true);
        final DesignProcessDefinition processDefinition = builder.done();
        return deployAndEnableProcessWithActor(processDefinition, ACTOR_NAME, user);
    }

    public ProcessDefinition deployAndEnableProcessWithIntermediateCatchTimerEventAndUserTask(final TimerType timerType, final Expression timerValue,
            final String step1Name, final String step2Name) throws BonitaException {
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("My Process with start event", "1.0")
                .addActor(ACTOR_NAME).addDescription("Delivery all day and night long").addStartEvent(START_EVENT_NAME).addUserTask(step1Name, ACTOR_NAME)
                .addIntermediateCatchEvent("intermediateCatchEvent").addTimerEventTriggerDefinition(timerType, timerValue).addUserTask(step2Name, ACTOR_NAME)
                .addEndEvent("endEvent").addTransition(START_EVENT_NAME, step1Name).addTransition(step1Name, "intermediateCatchEvent")
                .addTransition("intermediateCatchEvent", step2Name).addTransition(step2Name, "endEvent").getProcess();
        final ProcessDefinition definition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());
        return definition;
    }

    public ProcessDefinition deployAndEnableProcessWithStartTimerEventAndUserTask(final TimerType timerType, final Expression timerValue, final String stepName)
            throws BonitaException {
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("My Process with start event", "1.0")
                .addActor(ACTOR_NAME).addDescription("Delivery all day and night long").addStartEvent(START_EVENT_NAME)
                .addTimerEventTriggerDefinition(timerType, timerValue).addUserTask(stepName, ACTOR_NAME).addEndEvent("endEvent")
                .addTransition(START_EVENT_NAME, stepName).addTransition(stepName, "endEvent").getProcess();

        final ProcessDefinition definition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());
        return definition;
    }

    public ProcessDefinition deployAndEnableProcessSendingMessageUsingVariableAsTarget(final String targetProcessName, final String targetFlowNode,
            final String messageName) throws BonitaException {
        Expression targetProcessExpr = new ExpressionBuilder().createConstantStringExpression(targetProcessName);
        Expression targetFlowNodeExpr = new ExpressionBuilder().createConstantStringExpression(targetFlowNode);
        Expression targetProcessVarExpr = new ExpressionBuilder().createDataExpression("targetProcess", String.class.getName());
        Expression targetFlowNodeVarExpr = new ExpressionBuilder().createDataExpression("targetFlowNode", String.class.getName());
        ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("sendMsgProcess", "5.1");
        builder.addShortTextData("targetProcess", targetProcessExpr);
        builder.addShortTextData("targetFlowNode", targetFlowNodeExpr);
        builder.addStartEvent("start");
        builder.addIntermediateThrowEvent("sendMsg").addMessageEventTrigger(messageName, targetProcessVarExpr, targetFlowNodeVarExpr);
        builder.addEndEvent("end");
        builder.addTransition("start", "sendMsg");
        builder.addTransition("sendMsg", "end");

        return deployAndEnableProcess(builder.done());
    }

}
