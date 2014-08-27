/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.SubProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.SubProcessDefinitionBuilder;
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

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 */
public class TimerEventSubProcessTest extends CommonAPITest {

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

    private ProcessDefinition deployAndEnableProcessWithTimerEventSubProcess(final long timerDuration) throws BonitaException {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithEventSubProcess", "1.0");
        builder.addActor("mainActor");
        builder.addStartEvent("start");
        builder.addUserTask("step1", "mainActor");
        builder.addEndEvent("end");
        builder.addTransition("start", "step1");
        builder.addTransition("step1", "end");
        final SubProcessDefinitionBuilder subProcessBuilder = builder.addSubProcess("eventSubProcess", true).getSubProcessBuilder();
        subProcessBuilder.addStartEvent("timerStart").addTimerEventTriggerDefinition(TimerType.DURATION,
                new ExpressionBuilder().createConstantLongExpression(timerDuration));
        subProcessBuilder.addUserTask("subStep", "mainActor");
        subProcessBuilder.addEndEvent("endSubProcess");
        subProcessBuilder.addTransition("timerStart", "subStep");
        subProcessBuilder.addTransition("subStep", "endSubProcess");
        final DesignProcessDefinition processDefinition = builder.done();
        return deployAndEnableProcessWithActor(processDefinition, "mainActor", john);
    }

    private ProcessDefinition deployAndEnableProcessWithCallActivity(final String processName, final String targetProcessName, final String targetVersion)
            throws BonitaException {
        final Expression targetProcessExpr = new ExpressionBuilder().createConstantStringExpression(targetProcessName);
        final Expression targetVersionExpr = new ExpressionBuilder().createConstantStringExpression(targetVersion);
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance(processName, "1.0");
        builder.addActor("mainActor");
        builder.addStartEvent("start");
        builder.addCallActivity("callActivity", targetProcessExpr, targetVersionExpr);
        builder.addUserTask("step2", "mainActor");
        builder.addEndEvent("end");
        builder.addTransition("start", "callActivity");
        builder.addTransition("callActivity", "step2");
        builder.addTransition("step2", "end");
        final DesignProcessDefinition processDefinition = builder.done();
        return deployAndEnableProcessWithActor(processDefinition, "mainActor", john);
    }

    private ProcessDefinition deployAndEnableProcessWithTimerEventSubProcessAndData(final long timerDuration) throws BonitaException {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithEventSubProcess", "1.0");
        builder.addShortTextData("content", new ExpressionBuilder().createConstantStringExpression("parentVar"));
        builder.addIntegerData("count", new ExpressionBuilder().createConstantIntegerExpression(1));
        builder.addActor("mainActor");
        builder.addStartEvent("start");
        builder.addUserTask("step1", "mainActor");
        builder.addEndEvent("end");
        builder.addTransition("start", "step1");
        builder.addTransition("step1", "end");
        final SubProcessDefinitionBuilder subProcessBuilder = builder.addSubProcess("eventSubProcess", true).getSubProcessBuilder();
        subProcessBuilder.addShortTextData("content", new ExpressionBuilder().createConstantStringExpression("childVar"));
        subProcessBuilder.addDoubleData("value", new ExpressionBuilder().createConstantDoubleExpression(10.0));
        subProcessBuilder.addStartEvent("timerStart").addTimerEventTriggerDefinition(TimerType.DURATION,
                new ExpressionBuilder().createConstantLongExpression(timerDuration));
        subProcessBuilder.addUserTask("subStep", "mainActor").addShortTextData("content",
                new ExpressionBuilder().createConstantStringExpression("childActivityVar"));
        subProcessBuilder.addEndEvent("endSubProcess");
        subProcessBuilder.addTransition("timerStart", "subStep");
        subProcessBuilder.addTransition("subStep", "endSubProcess");
        final DesignProcessDefinition processDefinition = builder.done();
        return deployAndEnableProcessWithActor(processDefinition, "mainActor", john);
    }

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "timer" }, jira = "ENGINE-536")
    @Test
    public void testTimerEventSubProcessTriggered() throws Exception {
        final int timerDuration = 2000;
        final ProcessDefinition process = deployAndEnableProcessWithTimerEventSubProcess(timerDuration);
        final long startDate = System.currentTimeMillis();
        final ProcessInstance processInstance = getProcessAPI().startProcess(process.getId());

        final ActivityInstance step1 = waitForUserTask("step1", processInstance);
        assertNotNull(step1);
        final FlowNodeInstance eventSubProcessActivity = waitForFlowNodeInExecutingState(processInstance, "eventSubProcess", false);
        final ActivityInstance subStep = waitForUserTask("subStep", processInstance);
        assertNotNull(subStep);

        final ProcessInstance subProcInst = getProcessAPI().getProcessInstance(subStep.getParentProcessInstanceId());
        assertTrue("start date=" + subProcInst.getStartDate().getTime() + " should be >= than " + (startDate + timerDuration), subProcInst.getStartDate()
                .getTime() >= startDate + timerDuration);

        waitForArchivedActivity(step1.getId(), TestStates.getAbortedState());
        assignAndExecuteStep(subStep, john.getId());
        waitForArchivedActivity(eventSubProcessActivity.getId(), TestStates.getNormalFinalState());
        waitForProcessToFinish(subProcInst);
        waitForProcessToFinish(processInstance, TestStates.getAbortedState());

        disableAndDeleteProcess(process.getId());
    }

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "timer" }, jira = "ENGINE-536")
    @Test
    public void timerEventSubProcessNotTriggered() throws Exception {
        final int timerDuration = 6000;
        final ProcessDefinition process = deployAndEnableProcessWithTimerEventSubProcess(timerDuration);
        final ProcessInstance processInstance = getProcessAPI().startProcess(process.getId());
        waitForUserTaskAndExecuteIt("step1", processInstance, john);
        waitForProcessToFinish(processInstance);
        Thread.sleep(timerDuration);

        disableAndDeleteProcess(process.getId());
    }

    // added after problems with job name
    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "timer" }, jira = "ENGINE-536")
    @Test
    public void testCreateSeveralInstances() throws Exception {
        final int timerDuration = 500;
        final ProcessDefinition process = deployAndEnableProcessWithTimerEventSubProcess(timerDuration);
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(process.getId());
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(process.getId());
        waitForUserTask("subStep", processInstance1);
        waitForUserTask("subStep", processInstance2);

        disableAndDeleteProcess(process.getId());
    }

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "timer", "parent process data" }, jira = "ENGINE-536")
    @Test
    public void testSubProcessCanAccessParentData() throws Exception {
        final int timerDuration = 2000;
        final ProcessDefinition process = deployAndEnableProcessWithTimerEventSubProcessAndData(timerDuration);
        final ProcessInstance processInstance = getProcessAPI().startProcess(process.getId());
        final ActivityInstance subStep = waitForUserTask("subStep", processInstance);
        assertNotNull(subStep);

        final ProcessInstance subProcInst = getProcessAPI().getProcessInstance(subStep.getParentProcessInstanceId());
        checkProcessDataInstance("count", subProcInst.getId(), 1);
        checkProcessDataInstance("content", subProcInst.getId(), "childVar");
        checkProcessDataInstance("value", subProcInst.getId(), 10.0);
        checkProcessDataInstance("content", processInstance.getId(), "parentVar");
        checkActivityDataInstance("content", subStep.getId(), "childActivityVar");

        assignAndExecuteStep(subStep, john.getId());
        waitForProcessToFinish(subProcInst);
        waitForProcessToFinish(processInstance, TestStates.getAbortedState());

        disableAndDeleteProcess(process.getId());
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

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "timer", "call activity" }, jira = "ENGINE-536")
    @Test
    public void timerEventSubProcInsideTargetCallActivity() throws Exception {
        final int timerDuration = 2000;
        final ProcessDefinition targetProcess = deployAndEnableProcessWithTimerEventSubProcess(timerDuration);
        final ProcessDefinition callerProcess = deployAndEnableProcessWithCallActivity("ProcessWithCallActivity", targetProcess.getName(),
                targetProcess.getVersion());
        final ProcessInstance processInstance = getProcessAPI().startProcess(callerProcess.getId());
        final ActivityInstance step1 = waitForUserTask("step1", processInstance);
        final ActivityInstance subStep = waitForUserTask("subStep", processInstance);
        final ProcessInstance calledProcInst = getProcessAPI().getProcessInstance(step1.getParentProcessInstanceId());
        final ProcessInstance subProcInst = getProcessAPI().getProcessInstance(subStep.getParentProcessInstanceId());

        waitForArchivedActivity(step1.getId(), TestStates.getAbortedState());
        assignAndExecuteStep(subStep, john.getId());
        waitForProcessToFinish(subProcInst);
        waitForProcessToFinish(calledProcInst, TestStates.getAbortedState());

        waitForUserTaskAndExecuteIt("step2", processInstance, john);
        waitForProcessToFinish(processInstance);

        disableAndDeleteProcess(callerProcess.getId());
        disableAndDeleteProcess(targetProcess.getId());
    }
}
