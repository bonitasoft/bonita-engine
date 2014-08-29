/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.ProcessManagementAPI;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.SubProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.SubProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class SignalEventSubProcessTest extends EventsAPITest {

    private static final String AFTER_SIGNAL = "after_signal";

    private static final String PARENT_END = "end";

    private static final String PARENT_STEP = "step1";

    private static final String SUB_STEP = "subStep";

    private static final String SUB_PROCESS_NAME = "eventSubProcess";

    private static final String SIGNAL_NAME = "canStart";

    private static final String SUB_PROCESS_START = "signalStart";

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
        deleteUser(john);
        logoutOnTenant();
    }

    private ProcessDefinition deployAndEnableProcessWithSignalEventSubProcess(final boolean withIntermediateThrowEvent, final boolean withData)
            throws BonitaException {
        final ProcessDefinitionBuilder builder = buildParentProcessDefinition(withIntermediateThrowEvent, withData);
        buildSubProcessDefinition(builder, withData);
        final DesignProcessDefinition processDefinition = builder.done();
        return deployAndEnableProcessWithActor(processDefinition, ACTOR_NAME, john);
    }

    private ProcessDefinitionBuilder buildParentProcessDefinition(final boolean withIntermediateThrowEvent, final boolean withData)
            throws InvalidExpressionException {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithEventSubProcess", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addStartEvent("start");
        builder.addUserTask(PARENT_STEP, ACTOR_NAME);
        builder.addEndEvent(PARENT_END);
        builder.addTransition("start", PARENT_STEP);

        if (withIntermediateThrowEvent) {
            builder.addIntermediateThrowEvent(SIGNAL_NAME).addSignalEventTrigger(SIGNAL_NAME);
            builder.addUserTask(AFTER_SIGNAL, ACTOR_NAME);
            builder.addTransition(PARENT_STEP, SIGNAL_NAME);
            builder.addTransition(SIGNAL_NAME, AFTER_SIGNAL);
            builder.addTransition(SIGNAL_NAME, PARENT_END);
        } else {
            builder.addTransition(PARENT_STEP, PARENT_END);
        }

        if (withData) {
            builder.addShortTextData("content", new ExpressionBuilder().createConstantStringExpression("parentVar"));
            builder.addIntegerData("count", new ExpressionBuilder().createConstantIntegerExpression(1));
        }
        return builder;
    }

    private void buildSubProcessDefinition(final ProcessDefinitionBuilder builder, final boolean withData) throws InvalidExpressionException {
        final SubProcessDefinitionBuilder subProcessBuilder = builder.addSubProcess(SUB_PROCESS_NAME, true).getSubProcessBuilder();
        subProcessBuilder.addStartEvent(SUB_PROCESS_START).addSignalEventTrigger(SIGNAL_NAME);
        final UserTaskDefinitionBuilder userTask = subProcessBuilder.addUserTask(SUB_STEP, ACTOR_NAME);
        subProcessBuilder.addEndEvent("endSubProcess");
        subProcessBuilder.addTransition(SUB_PROCESS_START, SUB_STEP);
        subProcessBuilder.addTransition(SUB_STEP, "endSubProcess");

        if (withData) {
            subProcessBuilder.addShortTextData("content", new ExpressionBuilder().createConstantStringExpression("childVar"));
            subProcessBuilder.addDoubleData("value", new ExpressionBuilder().createConstantDoubleExpression(10.0));
            userTask.addShortTextData("content", new ExpressionBuilder().createConstantStringExpression("childActivityVar"));
        }
    }

    private ProcessDefinition deployAndEnableProcessWithCallActivity(final String processName, final String targetProcessName, final String targetVersion)
            throws BonitaException {
        final Expression targetProcessExpr = new ExpressionBuilder().createConstantStringExpression(targetProcessName);
        final Expression targetVersionExpr = new ExpressionBuilder().createConstantStringExpression(targetVersion);
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance(processName, "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addStartEvent("start");
        builder.addCallActivity("callActivity", targetProcessExpr, targetVersionExpr);
        builder.addUserTask("step2", ACTOR_NAME);
        builder.addEndEvent(PARENT_END);
        builder.addTransition("start", "callActivity");
        builder.addTransition("callActivity", "step2");
        builder.addTransition("step2", PARENT_END);
        final DesignProcessDefinition processDefinition = builder.done();
        return deployAndEnableProcessWithActor(processDefinition, ACTOR_NAME, john);
    }

    @Test
    @Cover(classes = { ProcessManagementAPI.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "expression context", "flow node container hierarchy" }, jira = "ENGINE-1848")
    public void evaluateExpressionsOnLoopUserTaskInSupProcess() throws Exception {
        final ProcessDefinition process = deployAndEnableProcessWithSignalEventSubProcess(false, true);
        final ProcessInstance processInstance = getProcessAPI().startProcess(process.getId());
        waitForUserTask(PARENT_STEP, processInstance);
        checkNumberOfWaitingEvents(SUB_PROCESS_START, 1);

        // send signal to start event sub process
        getProcessAPI().sendSignal(SIGNAL_NAME);

        waitForFlowNodeInExecutingState(processInstance, SUB_PROCESS_NAME, false);
        final ActivityInstance subStep = waitForUserTask(SUB_STEP, processInstance);

        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<Expression, Map<String, Serializable>>();
        final String dataName = "content";
        expressions.put(new ExpressionBuilder().createDataExpression(dataName, String.class.getName()), new HashMap<String, Serializable>(0));
        final Map<String, Serializable> expressionResults = getProcessAPI().evaluateExpressionsOnActivityInstance(subStep.getId(), expressions);
        assertEquals("childActivityVar", expressionResults.get(dataName));

        disableAndDeleteProcess(process);
    }

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "signal" }, jira = "ENGINE-536")
    @Test
    public void signalEventSubProcessTriggered() throws Exception {
        final ProcessDefinition process = deployAndEnableProcessWithSignalEventSubProcess(false, false);
        final ProcessInstance processInstance = getProcessAPI().startProcess(process.getId());
        final ActivityInstance step1 = waitForUserTask(PARENT_STEP, processInstance);
        List<ActivityInstance> activities = getProcessAPI().getActivities(processInstance.getId(), 0, 10);
        assertEquals(1, activities.size());
        checkNumberOfWaitingEvents(SUB_PROCESS_START, 1);

        // send signal to start event sub process
        Thread.sleep(50);
        getProcessAPI().sendSignal(SIGNAL_NAME);

        final FlowNodeInstance eventSubProcessActivity = waitForFlowNodeInExecutingState(processInstance, SUB_PROCESS_NAME, false);
        final ActivityInstance subStep = waitForUserTask(SUB_STEP, processInstance);
        waitForArchivedActivity(step1.getId(), TestStates.getAbortedState());
        // the parent process instance is supposed to be aborted, so no more waiting events are expected
        checkNumberOfWaitingEvents(SUB_PROCESS_START, 0);

        activities = getProcessAPI().getActivities(processInstance.getId(), 0, 10);
        assertEquals(2, activities.size());
        assertEquals(SUB_PROCESS_NAME, activities.get(0).getName());
        assertEquals(SUB_STEP, activities.get(1).getName());

        final ProcessInstance subProcInst = getProcessAPI().getProcessInstance(subStep.getParentProcessInstanceId());
        assignAndExecuteStep(subStep, john.getId());
        waitForArchivedActivity(eventSubProcessActivity.getId(), TestStates.getNormalFinalState());
        waitForProcessToFinish(subProcInst);
        waitForProcessToFinish(processInstance, TestStates.getAbortedState());

        // check that the transition wasn't taken
        checkWasntExecuted(processInstance, PARENT_END);

        disableAndDeleteProcess(process);
    }

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "signal",
            "intermediateThrowEvent" }, jira = "ENGINE-1408")
    @Test
    public void signalEventSubProcessTriggeredWithIntermediateThrowEvent() throws Exception {
        final ProcessDefinition process = deployAndEnableProcessWithSignalEventSubProcess(true, false);
        final ProcessInstance processInstance = getProcessAPI().startProcess(process.getId());
        final ActivityInstance step1 = waitForUserTask(PARENT_STEP, processInstance);
        checkNumberOfWaitingEvents(SUB_PROCESS_START, 1);

        assignAndExecuteStep(step1.getId(), john.getId());

        waitForUserTask(SUB_STEP, processInstance);

        disableAndDeleteProcess(process);
    }

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "signal" }, jira = "ENGINE-536")
    @Test
    public void signalEventSubProcessNotTriggered() throws Exception {
        final ProcessDefinition process = deployAndEnableProcessWithSignalEventSubProcess(false, false);
        final ProcessInstance processInstance = getProcessAPI().startProcess(process.getId());
        final ActivityInstance step1 = waitForUserTask(PARENT_STEP, processInstance);
        final List<ActivityInstance> activities = getProcessAPI().getActivities(processInstance.getId(), 0, 10);
        assertEquals(1, activities.size());
        checkNumberOfWaitingEvents(SUB_PROCESS_START, 1);

        assignAndExecuteStep(step1, john.getId());

        waitForArchivedActivity(step1.getId(), TestStates.getNormalFinalState());
        waitForProcessToFinish(processInstance);

        // the parent process instance has completed, so no more waiting events are expected
        checkNumberOfWaitingEvents(SUB_PROCESS_START, 0);

        disableAndDeleteProcess(process);
    }

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "signal" }, jira = "ENGINE-536")
    @Test
    public void createSeveralInstances() throws Exception {
        final ProcessDefinition process = deployAndEnableProcessWithSignalEventSubProcess(false, false);
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(process.getId());
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(process.getId());

        waitForUserTask(PARENT_STEP, processInstance1);
        waitForUserTask(PARENT_STEP, processInstance2);

        // send signal to start event sub processes: one signal must start the event sub-processes in the two process instances
        getProcessAPI().sendSignal(SIGNAL_NAME);

        waitForUserTask(SUB_STEP, processInstance1);
        waitForUserTask(SUB_STEP, processInstance2);
        Thread.sleep(50);

        disableAndDeleteProcess(process);
    }

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "signal", "parent process data" }, jira = "ENGINE-536")
    @Test
    public void subProcessCanAccessParentData() throws Exception {
        final ProcessDefinition process = deployAndEnableProcessWithSignalEventSubProcess(false, true);
        final ProcessInstance processInstance = getProcessAPI().startProcess(process.getId());
        waitForUserTask(PARENT_STEP, processInstance);

        getProcessAPI().sendSignal(SIGNAL_NAME);

        final ActivityInstance subStep = waitForUserTask(SUB_STEP, processInstance);
        final ProcessInstance subProcInst = getProcessAPI().getProcessInstance(subStep.getParentProcessInstanceId());
        checkProcessDataInstance("count", subProcInst.getId(), 1);
        checkProcessDataInstance("content", subProcInst.getId(), "childVar");
        checkProcessDataInstance("value", subProcInst.getId(), 10.0);
        checkProcessDataInstance("content", processInstance.getId(), "parentVar");
        checkActivityDataInstance("content", subStep.getId(), "childActivityVar");

        assignAndExecuteStep(subStep, john.getId());
        waitForProcessToFinish(subProcInst);
        waitForProcessToFinish(processInstance, TestStates.getAbortedState());

        disableAndDeleteProcess(process);
    }

    private void checkProcessDataInstance(final String dataName, final long processInstanceId, final Serializable expectedValue) throws DataNotFoundException {
        final DataInstance processDataInstance;
        processDataInstance = getProcessAPI().getProcessDataInstance(dataName, processInstanceId);
        assertEquals(expectedValue, processDataInstance.getValue());
    }

    private void checkActivityDataInstance(final String dataName, final long activityInstanceId, final Serializable expectedValue) throws DataNotFoundException {
        final DataInstance activityDataInstance;
        activityDataInstance = getProcessAPI().getActivityDataInstance(dataName, activityInstanceId);
        assertEquals(expectedValue, activityDataInstance.getValue());
    }

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "signal", "call activity" }, jira = "ENGINE-536")
    @Test
    public void signalEventSubProcInsideTargetCallActivity() throws Exception {
        final ProcessDefinition targetProcess = deployAndEnableProcessWithSignalEventSubProcess(false, false);
        final ProcessDefinition callerProcess = deployAndEnableProcessWithCallActivity("ProcessWithCallActivity", targetProcess.getName(),
                targetProcess.getVersion());
        final ProcessInstance processInstance = getProcessAPI().startProcess(callerProcess.getId());
        final ActivityInstance step1 = waitForUserTask(PARENT_STEP, processInstance);

        getProcessAPI().sendSignal(SIGNAL_NAME);

        final ActivityInstance subStep = waitForUserTask(SUB_STEP, processInstance);
        final ProcessInstance calledProcInst = getProcessAPI().getProcessInstance(step1.getParentProcessInstanceId());
        final ProcessInstance subProcInst = getProcessAPI().getProcessInstance(subStep.getParentProcessInstanceId());

        waitForArchivedActivity(step1.getId(), TestStates.getAbortedState());
        assignAndExecuteStep(subStep, john.getId());
        waitForProcessToFinish(subProcInst);
        waitForProcessToFinish(calledProcInst, TestStates.getAbortedState());

        waitForUserTaskAndExecuteIt("step2", processInstance.getId(), john.getId());
        waitForProcessToFinish(processInstance);

        disableAndDeleteProcess(callerProcess.getId());
        disableAndDeleteProcess(targetProcess.getId());
    }
}
