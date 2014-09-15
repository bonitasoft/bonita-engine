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
package org.bonitasoft.engine.event;

import static org.junit.Assert.assertEquals;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.FlowNodeType;
import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.CallActivityBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.junit.After;
import org.junit.Before;

/**
 * @author Elias Ricken de Medeiros
 */
public abstract class AbstractTimerBoundaryEventTest extends CommonAPITest {

    protected User donaBenta;

    @Before
    public void beforeTest() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        donaBenta = createUser("donabenta", "bpm");
        logoutOnTenant();
        loginOnDefaultTenantWith("donabenta", "bpm");
    }

    @After
    public void afterTest() throws BonitaException {
        logoutOnTenant();
        loginOnDefaultTenantWithDefaultTechnicalUser();
        deleteUser(donaBenta.getId());
        logoutOnTenant();
    }

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
    protected ProcessDefinition deployProcessWithTimerBoundaryEvent(final long timerValue, final boolean interrupting, final String taskWithBoundaryName,
            final String exceptionTaskName, final String normalFlowTaskName) throws BonitaException {
        return deployTimerProcessWithBoundaryEvent(TimerType.DURATION, timerValue, interrupting, taskWithBoundaryName, exceptionTaskName, normalFlowTaskName);
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
    protected ProcessDefinition deployTimerProcessWithBoundaryEvent(final TimerType timerType, final long timerValue, final boolean interrupting,
            final String taskWithBoundaryName, final String exceptionTaskName, final String normalFlowTaskName) throws BonitaException {
        final Expression timerExpr = new ExpressionBuilder().createConstantLongExpression(timerValue);
        return deployProcessWithTimerBoundaryEvent(timerType, timerExpr, interrupting, taskWithBoundaryName, exceptionTaskName, normalFlowTaskName);
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
     * @param timerValue
     *        the timer value
     * @return
     * @throws BonitaException
     * @throws InvalidProcessDefinitionException
     * @since 6.0
     */
    protected ProcessDefinition deployProcessWithTimerBoundaryEvent(final TimerType timerType, final Expression timerExpr, final boolean interrupting,
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

        return deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, donaBenta);
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
     * @param userTaskName
     *        the name of user task following the call activity in the normal flow
     * @param exceptionFlowTaskName
     *        the name of the user task reached by the exception flow
     * @return
     * @throws InvalidExpressionException
     * @throws BonitaException
     * @throws InvalidProcessDefinitionException
     * @since 6.0
     */
    protected ProcessDefinition deployAndEnbleProcessWithTimerBoundaryEventOnCallActivity(final long timerDuration, final boolean interrupting,
            final String targetProcessName, final String userTaskName, final String exceptionFlowTaskName) throws BonitaException {
        final Expression timerExpr = new ExpressionBuilder().createConstantLongExpression(timerDuration);
        final Expression targetProcessNameExpr = new ExpressionBuilder().createConstantStringExpression(targetProcessName);
        final Expression targetProcessVersionExpr = new ExpressionBuilder().createConstantStringExpression("1.0");
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("processWithCallActivityAndBoundaryEvent",
                "2.0");
        processDefinitionBuilder.addActor(ACTOR_NAME).addStartEvent("start");
        final CallActivityBuilder callActivityBuilder = processDefinitionBuilder.addCallActivity("callActivity", targetProcessNameExpr,
                targetProcessVersionExpr);
        callActivityBuilder.addBoundaryEvent("timer", interrupting).addTimerEventTriggerDefinition(TimerType.DURATION, timerExpr);
        processDefinitionBuilder.addUserTask(exceptionFlowTaskName, ACTOR_NAME).addUserTask(userTaskName, ACTOR_NAME).addEndEvent("end")
                .addTransition("start", "callActivity").addTransition("callActivity", userTaskName).addTransition(userTaskName, "end")
                .addTransition("timer", exceptionFlowTaskName);

        return deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, getUser());
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
    protected ProcessDefinition deployAndEnableSimpleProcess(final String processName, final String userTaskName) throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(processName, "1.0");
        processBuilder.addActor(ACTOR_NAME).addStartEvent("startCA").addUserTask(userTaskName, ACTOR_NAME).addEndEvent("endCA")
                .addTransition("startCA", userTaskName).addTransition(userTaskName, "endCA");
        return deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, getUser());
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
    protected ProcessDefinition deployProcessMultiInstanceWithBoundaryEvent(final long timerValue, final boolean interrupting, final String multiTaskName,
            final int loopCardinality, final boolean isSequential, final String normalFlowTaskName, final String exceptionFlowTaskName) throws BonitaException {
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

        return deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, getUser());
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
    protected ProcessDefinition deployProcessWithBoundaryEventOnLoopActivity(final long timerValue, final boolean interrupting, final int loopMax,
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

        return deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, getUser());
    }

    protected void executeRemainingSequencialMultiInstancesOrLoop(final String taskName, final ProcessInstance processInstance, final int nbOfRemainingInstances)
            throws Exception {
        for (int i = 0; i < nbOfRemainingInstances; i++) {
            waitForUserTaskAndExecuteIt(taskName, processInstance.getId(), getUser());
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
            assignAndExecuteStep(activity, getUser().getId());
        }
    }

    protected User getUser() {
        return donaBenta;
    }

}
