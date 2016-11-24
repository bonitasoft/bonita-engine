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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.ProcessManagementAPI;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.bpm.process.SubProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.SubProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.test.TestStates;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class SignalEventSubProcessIT extends AbstractWaitingEventIT {

    @Test
    public void evaluateExpressionsOnLoopUserTaskInSupProcess() throws Exception {
        final ProcessDefinition process = deployAndEnableProcessWithSignalEventSubProcess(false, true);
        final ProcessInstance processInstance = getProcessAPI().startProcess(process.getId());
        waitForUserTask(processInstance, PARENT_PROCESS_USER_TASK_NAME);
        checkNumberOfWaitingEvents(SUB_PROCESS_START_NAME, 1);

        // send signal to start event sub process
        getProcessAPI().sendSignal(SIGNAL_NAME);

        waitForFlowNodeInExecutingState(processInstance, SUB_PROCESS_NAME, false);
        final long subStepId = waitForUserTask(processInstance, SUB_PROCESS_USER_TASK_NAME);

        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<>();
        expressions.put(new ExpressionBuilder().createDataExpression(SHORT_DATA_NAME, String.class.getName()), new HashMap<String, Serializable>(0));
        final Map<String, Serializable> expressionResults = getProcessAPI().evaluateExpressionsOnActivityInstance(subStepId, expressions);
        assertEquals("childActivityVar", expressionResults.get(SHORT_DATA_NAME));

        disableAndDeleteProcess(process);
    }

    @Test
    public void signalEventSubProcessTriggered() throws Exception {
        // given
        final ProcessDefinition process = deployAndEnableProcessWithSignalEventSubProcess(false, false);

        // when
        final ProcessInstance processInstance = getProcessAPI().startProcess(process.getId());
        final long step1Id = waitForUserTask(processInstance, PARENT_PROCESS_USER_TASK_NAME);

        // then
        List<ActivityInstance> activities = getProcessAPI().getActivities(processInstance.getId(), 0, 10);
        assertThat(activities).as("should have 1 activity").hasSize(1);
        checkNumberOfWaitingEvents(SUB_PROCESS_START_NAME, 1);

        // when
        getProcessAPI().sendSignal(SIGNAL_NAME);
        waitForArchivedActivity(step1Id, TestStates.ABORTED);
        final ActivityInstance subStep = waitForUserTaskAndGetIt(processInstance, SUB_PROCESS_USER_TASK_NAME);

        // then
        checkNumberOfWaitingEvents(SUB_PROCESS_START_NAME, 0);
        activities = getProcessAPI().getActivities(processInstance.getId(), 0, 10);
        assertThat(activities).as("should have 2 activities: sub-process flow node and user task").hasSize(2);
        assertEquals(SUB_PROCESS_NAME, activities.get(0).getName());
        assertEquals(SUB_PROCESS_USER_TASK_NAME, activities.get(1).getName());

        // when
        final ProcessInstance subProcInst = getProcessAPI().getProcessInstance(subStep.getParentProcessInstanceId());
        assignAndExecuteStep(subStep, user);
        waitForProcessToFinish(subProcInst);
        waitForProcessToBeInState(processInstance, ProcessInstanceState.ABORTED);

        // then
        // check that the transition wasn't taken
        checkWasntExecuted(processInstance, PARENT_END);

        disableAndDeleteProcess(process);
    }

    @Test
    public void signalEventSubProcessTriggeredWithIntermediateThrowEvent() throws Exception {
        final ProcessDefinition process = deployAndEnableProcessWithSignalEventSubProcess(true, false);
        final ProcessInstance processInstance = getProcessAPI().startProcess(process.getId());
        final long step1Id = waitForUserTask(processInstance, PARENT_PROCESS_USER_TASK_NAME);
        checkNumberOfWaitingEvents(SUB_PROCESS_START_NAME, 1);

        assignAndExecuteStep(step1Id, user);

        waitForUserTask(processInstance, SUB_PROCESS_USER_TASK_NAME);

        disableAndDeleteProcess(process);
    }

    @Test
    public void signalEventSubProcessNotTriggered() throws Exception {
        final ProcessDefinition process = deployAndEnableProcessWithSignalEventSubProcess(false, false);
        final ProcessInstance processInstance = getProcessAPI().startProcess(process.getId());
        final long step1Id = waitForUserTask(processInstance, PARENT_PROCESS_USER_TASK_NAME);
        final List<ActivityInstance> activities = getProcessAPI().getActivities(processInstance.getId(), 0, 10);
        assertEquals(1, activities.size());
        checkNumberOfWaitingEvents(SUB_PROCESS_START_NAME, 1);

        assignAndExecuteStep(step1Id, user);

        waitForArchivedActivity(step1Id, TestStates.NORMAL_FINAL);
        waitForProcessToFinish(processInstance);

        // the parent process instance has completed, so no more waiting events are expected
        checkNumberOfWaitingEvents(SUB_PROCESS_START_NAME, 0);

        disableAndDeleteProcess(process);
    }

    @Test
    public void createSeveralInstances() throws Exception {
        final ProcessDefinition process = deployAndEnableProcessWithSignalEventSubProcess(false, false);
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(process.getId());
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(process.getId());

        waitForUserTask(processInstance1, PARENT_PROCESS_USER_TASK_NAME);
        waitForUserTask(processInstance2, PARENT_PROCESS_USER_TASK_NAME);

        // send signal to start event sub processes: one signal must start the event sub-processes in the two process instances
        getProcessAPI().sendSignal(SIGNAL_NAME);

        waitForUserTask(processInstance1, SUB_PROCESS_USER_TASK_NAME);
        waitForUserTask(processInstance2, SUB_PROCESS_USER_TASK_NAME);
        Thread.sleep(50);

        disableAndDeleteProcess(process);
    }

    @Test
    public void subProcessCanAccessParentData() throws Exception {
        final ProcessDefinition process = deployAndEnableProcessWithSignalEventSubProcess(false, true);
        final ProcessInstance processInstance = getProcessAPI().startProcess(process.getId());
        waitForUserTask(processInstance, PARENT_PROCESS_USER_TASK_NAME);

        getProcessAPI().sendSignal(SIGNAL_NAME);

        final ActivityInstance subStep = waitForUserTaskAndGetIt(processInstance, SUB_PROCESS_USER_TASK_NAME);
        final ProcessInstance subProcInst = getProcessAPI().getProcessInstance(subStep.getParentProcessInstanceId());
        checkProcessDataInstance("count", subProcInst.getId(), 1);
        checkProcessDataInstance(SHORT_DATA_NAME, subProcInst.getId(), "childVar");
        checkProcessDataInstance("value", subProcInst.getId(), 10.0);
        checkProcessDataInstance(SHORT_DATA_NAME, processInstance.getId(), "parentVar");
        checkActivityDataInstance(SHORT_DATA_NAME, subStep.getId(), "childActivityVar");

        assignAndExecuteStep(subStep, user);
        waitForProcessToFinish(subProcInst);
        waitForProcessToBeInState(processInstance, ProcessInstanceState.ABORTED);

        disableAndDeleteProcess(process);
    }

    private void checkProcessDataInstance(final String dataName, final long processInstanceId, final Serializable expectedValue) throws DataNotFoundException {
        final DataInstance processDataInstance;
        processDataInstance = getProcessAPI().getProcessDataInstance(dataName, processInstanceId);
        assertEquals(expectedValue, processDataInstance.getValue());
    }

    private void checkActivityDataInstance(final String dataName, final long activityInstanceId, final Serializable expectedValue)
            throws DataNotFoundException {
        final DataInstance activityDataInstance;
        activityDataInstance = getProcessAPI().getActivityDataInstance(dataName, activityInstanceId);
        assertEquals(expectedValue, activityDataInstance.getValue());
    }

    @Test
    public void signalEventSubProcInsideTargetCallActivity() throws Exception {
        final ProcessDefinition targetProcess = deployAndEnableProcessWithSignalEventSubProcess(false, false);
        final ProcessDefinition callerProcess = deployAndEnableProcessWithCallActivity(targetProcess.getName(), targetProcess.getVersion());
        final ProcessInstance processInstance = getProcessAPI().startProcess(callerProcess.getId());
        final ActivityInstance step1 = waitForUserTaskAndGetIt(processInstance, PARENT_PROCESS_USER_TASK_NAME);

        getProcessAPI().sendSignal(SIGNAL_NAME);

        final ActivityInstance subStep = waitForUserTaskAndGetIt(processInstance, SUB_PROCESS_USER_TASK_NAME);
        final ProcessInstance calledProcInst = getProcessAPI().getProcessInstance(step1.getParentProcessInstanceId());
        final ProcessInstance subProcInst = getProcessAPI().getProcessInstance(subStep.getParentProcessInstanceId());

        waitForArchivedActivity(step1.getId(), TestStates.ABORTED);
        assignAndExecuteStep(subStep, user);
        waitForProcessToFinish(subProcInst);
        waitForProcessToBeInState(calledProcInst, ProcessInstanceState.ABORTED);

        waitForUserTaskAndExecuteIt(processInstance, "step2", user);
        waitForProcessToFinish(processInstance);

        disableAndDeleteProcess(callerProcess.getId());
        disableAndDeleteProcess(targetProcess.getId());
    }

    @Test
    public void should_process_with_gateway_be_canceled_by_event_subprocess() throws Exception {
        //given
        ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("ProcessInterrupted", "1.0");
        processDefinitionBuilder.addActor("john", true);
        processDefinitionBuilder.addStartEvent("start");
        processDefinitionBuilder.addAutomaticTask("auto1");
        processDefinitionBuilder.addGateway("p1", GatewayType.PARALLEL);
        processDefinitionBuilder.addUserTask("user1", "john");
        processDefinitionBuilder.addAutomaticTask("auto2");
        processDefinitionBuilder.addGateway("p2", GatewayType.PARALLEL);
        processDefinitionBuilder.addEndEvent("end").addTerminateEventTrigger();
        processDefinitionBuilder.addTransition("start", "auto1");
        processDefinitionBuilder.addTransition("auto1", "p1");
        processDefinitionBuilder.addTransition("p1", "auto2");
        processDefinitionBuilder.addTransition("p1", "user1");
        processDefinitionBuilder.addTransition("auto2", "p2");
        processDefinitionBuilder.addTransition("user1", "p2");
        processDefinitionBuilder.addTransition("p2", "end");
        SubProcessDefinitionBuilder eventSub = processDefinitionBuilder.addSubProcess("eventSub", true).getSubProcessBuilder();
        eventSub.addStartEvent("signalStart").addSignalEventTrigger("bip_bip");
        eventSub.addAutomaticTask("subAuto1");
        eventSub.addEndEvent("subEnd").addTerminateEventTrigger();
        eventSub.addTransition("signalStart", "subAuto1").addTransition("subAuto1", "subEnd");
        ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), "john", user);
        //when
        ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance.getId(), "user1");
        getProcessAPI().sendSignal("bip_bip");
        //then
        waitForProcessToBeInState(processInstance, ProcessInstanceState.ABORTED);

        disableAndDeleteProcess(processDefinition);
    }

}
