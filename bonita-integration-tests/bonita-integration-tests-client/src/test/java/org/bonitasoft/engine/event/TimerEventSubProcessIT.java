/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.StartEventDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.SubProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.test.TestStates;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 */
public class TimerEventSubProcessIT extends AbstractEventIT {

    @Test
    public void timerEventSubProcessTriggered() throws Exception {
        // given
        final int timerDuration = 2000;
        final ProcessDefinition process = deployAndEnableProcessWithTimerEventSubProcess(timerDuration);
        final ProcessInstance processInstance = getProcessAPI().startProcess(process.getId());

        // when
        final ActivityInstance subStep = waitForUserTaskAndGetIt(processInstance, "subStep");
        final ProcessInstance subProcInst = getProcessAPI().getProcessInstance(subStep.getParentProcessInstanceId());

        final Date processStartDate = processInstance.getStartDate();
        assertThat(subProcInst.getStartDate()).as(
                String.format("process started at %s should trigger subprocess at %s (+ %d ms) ",
                        formatedDate(processStartDate),
                        formatedDate(subProcInst.getStartDate()), timerDuration))
                .isAfter(processStartDate);

        // cleanup
        assignAndExecuteStep(subStep, user);
        waitForProcessToFinish(subProcInst);
        disableAndDeleteProcess(process.getId());
    }

    private String formatedDate(final Date date) {
        final SimpleDateFormat dt = new SimpleDateFormat(DATE_FORMAT_WITH_MS);
        return dt.format(date);
    }

    @Test
    public void timerEventSubProcessNotTriggered() throws Exception {
        final int timerDuration = 6000;
        final ProcessDefinition process = deployAndEnableProcessWithTimerEventSubProcess(timerDuration);
        final ProcessInstance processInstance = getProcessAPI().startProcess(process.getId());
        waitForUserTaskAndExecuteIt(processInstance, "step1", user);
        waitForProcessToFinish(processInstance);
        Thread.sleep(timerDuration);

        disableAndDeleteProcess(process.getId());
    }

    // added after problems with job name
    @Test
    public void testCreateSeveralInstances() throws Exception {
        final int timerDuration = 500;
        final ProcessDefinition process = deployAndEnableProcessWithTimerEventSubProcess(timerDuration);
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(process.getId());
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(process.getId());
        waitForUserTask(processInstance1, "subStep");
        waitForUserTask(processInstance2, "subStep");

        disableAndDeleteProcess(process.getId());
    }

    @Test
    public void subProcessCanAccessParentData() throws Exception {
        final int timerDuration = 2000;
        final ProcessDefinition process = deployAndEnableProcessWithTimerEventSubProcessAndData(timerDuration);
        final ProcessInstance processInstance = getProcessAPI().startProcess(process.getId());
        final ActivityInstance subStep = waitForUserTaskAndGetIt(processInstance, "subStep");

        final ProcessInstance subProcInst = getProcessAPI().getProcessInstance(subStep.getParentProcessInstanceId());
        checkProcessDataInstance("count", subProcInst.getId(), 1);
        checkProcessDataInstance("content", subProcInst.getId(), "childVar");
        checkProcessDataInstance("value", subProcInst.getId(), 10.0);
        checkProcessDataInstance("content", processInstance.getId(), "parentVar");
        checkActivityDataInstance("content", subStep.getId(), "childActivityVar");

        assignAndExecuteStep(subStep, user.getId());
        waitForProcessToFinish(subProcInst);
        waitForProcessToBeInState(processInstance, ProcessInstanceState.ABORTED);

        disableAndDeleteProcess(process.getId());
    }

    private void checkProcessDataInstance(final String dataName, final long processInstanceId,
            final Serializable expectedValue) throws DataNotFoundException {
        final DataInstance processDataInstance;
        processDataInstance = getProcessAPI().getProcessDataInstance(dataName, processInstanceId);
        assertEquals(expectedValue, processDataInstance.getValue());
    }

    private void checkActivityDataInstance(final String dataName, final long activityInstanceId,
            final Serializable expectedValue)
            throws DataNotFoundException {
        final DataInstance activityDataInstance;
        activityDataInstance = getProcessAPI().getActivityDataInstance(dataName, activityInstanceId);
        assertEquals(expectedValue, activityDataInstance.getValue());
    }

    @Test
    public void timerEventSubProcInsideTargetCallActivity() throws Exception {
        final ProcessDefinition targetProcess = deployAndEnableProcessWithTimerEventSubProcess(2000);
        final ProcessDefinition callerProcess = deployAndEnableProcessWithCallActivity(targetProcess.getName(),
                targetProcess.getVersion());
        final ProcessInstance processInstance = getProcessAPI().startProcess(callerProcess.getId());
        final ActivityInstance step1 = waitForUserTaskAndGetIt(processInstance, "step1");
        final ActivityInstance subStep = waitForUserTaskAndGetIt(processInstance, "subStep");
        final ProcessInstance calledProcInst = getProcessAPI().getProcessInstance(step1.getParentProcessInstanceId());
        final ProcessInstance subProcInst = getProcessAPI().getProcessInstance(subStep.getParentProcessInstanceId());

        waitForFlowNodeInState(processInstance, "step1", TestStates.ABORTED, true);
        assignAndExecuteStep(subStep, user);
        waitForProcessToFinish(subProcInst);
        waitForProcessToBeInState(calledProcInst, ProcessInstanceState.ABORTED);

        waitForUserTaskAndExecuteIt(processInstance, "step2", user);
        waitForProcessToFinish(processInstance);

        disableAndDeleteProcess(callerProcess.getId());
        disableAndDeleteProcess(targetProcess.getId());
    }

    @Test
    public void should_be_able_to_init_timer_from_data_in_parent() throws Exception {
        //given
        ProcessDefinitionBuilder parentProcessBuilder = new ProcessDefinitionBuilder()
                .createNewInstance("ProcessWithESPTimerBasedOnData", "1.0");
        parentProcessBuilder.addActor(ACTOR_NAME);
        parentProcessBuilder.addUserTask("userTask", ACTOR_NAME);
        parentProcessBuilder.addLongData("timeToWait", new ExpressionBuilder().createConstantLongExpression(1000));
        //construct sub process
        SubProcessDefinitionBuilder subProcessBuilder = parentProcessBuilder
                .addSubProcess("subProcessToStartWithTimer", true).getSubProcessBuilder();
        StartEventDefinitionBuilder startEventDefinitionBuilder = subProcessBuilder.addStartEvent("timerStart");
        startEventDefinitionBuilder.addTimerEventTriggerDefinition(TimerType.DURATION,
                new ExpressionBuilder().createDataExpression("timeToWait", Long.class.getName()));
        subProcessBuilder.addUserTask("userTaskInSubProcess", ACTOR_NAME);
        subProcessBuilder.addEndEvent("endSubProcess");
        subProcessBuilder.addTransition("timerStart", "userTaskInSubProcess");
        subProcessBuilder.addTransition("userTaskInSubProcess", "endSubProcess");
        DesignProcessDefinition processDefinition1 = parentProcessBuilder.done();
        BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive()
                .setProcessDefinition(processDefinition1);
        ProcessDefinition processDefinition = deployAndEnableProcessWithActor(businessArchiveBuilder.done(), ACTOR_NAME,
                user);
        ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        //when
        waitForUserTask("userTaskInSubProcess");
        //then
        disableAndDeleteProcess(processDefinition);
    }
}
