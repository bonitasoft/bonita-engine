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
package com.bonitasoft.engine.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.List;

import org.bonitasoft.engine.bpm.model.ActivationState;
import org.bonitasoft.engine.bpm.model.ActivityInstance;
import org.bonitasoft.engine.bpm.model.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.model.ProcessDefinition;
import org.bonitasoft.engine.bpm.model.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.model.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.model.ProcessInstance;
import org.bonitasoft.engine.bpm.model.archive.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.model.event.EventInstance;
import org.bonitasoft.engine.bpm.model.event.trigger.TimerType;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.check.CheckNbOfActivities;
import org.bonitasoft.engine.test.wait.WaitForEvent;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class CancelProcessInstanceTest extends InterruptProcessInstanceTest {

    @Test
    public void cancelProcessWithAutomaticTasksUsingBkPoints() throws Exception {
        final String taskName1 = "auto1";
        final String taskName2 = "auto2";
        final ProcessDefinition processDefinition = deployProcessWith2AutomaticTasks(taskName1, taskName2);

        // add break points
        final long breakpointId1 = getProcessAPI().addBreakpoint(processDefinition.getId(), taskName1, 2, 45);
        final long breakpointId2 = getProcessAPI().addBreakpoint(processDefinition.getId(), taskName2, 2, 45);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final CheckNbOfActivities checkNbOfInterrupted = checkNbOfActivitiesInInterruptingState(processInstance, 2);

        final Iterator<ActivityInstance> iterator = checkNbOfInterrupted.getResult().iterator();
        final ActivityInstance task1 = iterator.next();
        final ActivityInstance task2 = iterator.next();

        getProcessAPI().cancelProcessInstance(processInstance.getId());

        getProcessAPI().executeActivity(task1.getId());
        getProcessAPI().executeActivity(task2.getId());

        waitForProcessToFinish(processInstance, TestStates.getCancelledState());

        final ArchivedActivityInstance archTask1 = getProcessAPI().getArchivedActivityInstance(task1.getId());
        final ArchivedActivityInstance archTask2 = getProcessAPI().getArchivedActivityInstance(task2.getId());
        assertEquals(TestStates.getCancelledState(), archTask1.getState());
        assertEquals(TestStates.getCancelledState(), archTask2.getState());

        getProcessAPI().removeBreakpoint(breakpointId1);
        getProcessAPI().removeBreakpoint(breakpointId2);
        disableAndDelete(processDefinition);

    }

    @Test
    public void cancelStartEvent() throws Exception {
        final String taskName1 = "auto1";
        final String taskName2 = "auto2";
        final ProcessDefinition processDefinition = deployProcessWith2AutomaticTasks(taskName1, taskName2);

        // add break points
        final long breakpointId = getProcessAPI().addBreakpoint(processDefinition.getId(), "start", 2, 45);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final WaitForEvent waitForInterrupted = waitForEvent(processInstance, "start", TestStates.getInterruptingState());

        final EventInstance start = waitForInterrupted.getResult();
        assertEquals("start", start.getName());

        getProcessAPI().cancelProcessInstance(processInstance.getId());

        // resume breakpoint
        getProcessAPI().executeActivity(start.getId());

        waitForProcessToFinish(processInstance, TestStates.getCancelledState());

        final List<ArchivedActivityInstance> archivedActivityInstances = getProcessAPI().getArchivedActivityInstances(processInstance.getId(), 0, 20,
                ActivityInstanceCriterion.NAME_ASC);

        // only start even must exist and its not an activity instance
        assertEquals(0, archivedActivityInstances.size());
        getProcessAPI().removeBreakpoint(breakpointId);
        disableAndDelete(processDefinition);
    }

    @Test
    public void cancelEndEvent() throws Exception {
        final String taskName1 = "auto1";
        final String taskName2 = "auto2";
        final ProcessDefinition processDefinition = deployProcessWith2AutomaticTasks(taskName1, taskName2);

        // add break points
        final long breakpointId = getProcessAPI().addBreakpoint(processDefinition.getId(), "end1", 2, 45);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final WaitForEvent waitForInterrupted = waitForEvent(processInstance, "end1", TestStates.getInterruptingState());
        final EventInstance end = waitForInterrupted.getResult();
        assertEquals("end1", end.getName());

        getProcessAPI().cancelProcessInstance(processInstance.getId());

        // resume breakpoint
        getProcessAPI().executeActivity(end.getId());

        waitForProcessToFinish(processInstance, TestStates.getCancelledState());
        getProcessAPI().removeBreakpoint(breakpointId);
        disableAndDelete(processDefinition);
    }

    @Test
    public void cancelIntermediateThrowEvent() throws Exception {
        final String eventName = "sendMessage";
        final ProcessDefinition processDefinition = deployProcessWithIntermediateThrowMessageEvent(eventName, "m1", "p1", "receiveMessage");

        // add break points
        final long breakpointId = getProcessAPI().addBreakpoint(processDefinition.getId(), "sendMessage", 2, 45);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final WaitForEvent waitForInterrupted = waitForEvent(processInstance, "sendMessage", TestStates.getInterruptingState());
        final EventInstance event = waitForInterrupted.getResult();
        assertEquals("sendMessage", event.getName());

        getProcessAPI().cancelProcessInstance(processInstance.getId());

        // resume breakpoint
        getProcessAPI().executeActivity(event.getId());

        waitForProcessToFinish(processInstance, TestStates.getCancelledState());

        // verify that the execution does not pass through the activity after the intermediate event
        checkWasntExecuted(processInstance, "auto2");
        getProcessAPI().removeBreakpoint(breakpointId);
        disableAndDelete(processDefinition);
    }

    @Test
    public void cancelParallelMergeGatewayIntance() throws Exception {
        final ProcessDefinition processDefinition = deployProcessWithParallelGateways();
        // add break points
        final long breakpointId = getProcessAPI().addBreakpoint(processDefinition.getId(), "gateway2", 2, 45);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final Long waitForFlowNodeId = waitForFlowNode(processInstance.getId(), TestStates.getInterruptingState(), "gateway2", false,
                35000);
        assertNotNull(waitForFlowNodeId);

        getProcessAPI().cancelProcessInstance(processInstance.getId());

        // resume break point
        getProcessAPI().executeActivity(waitForFlowNodeId);
        waitForProcessToFinish(processInstance, TestStates.getCancelledState());

        // verify that the execution does not pass through the activity after the gateway
        checkWasntExecuted(processInstance, "step4");
        getProcessAPI().removeBreakpoint(breakpointId);
        disableAndDelete(processDefinition);

    }

    @Test
    public void cancelParallelSplitGatewayIntance() throws Exception {
        final ProcessDefinition processDefinition = deployProcessWithParallelGateways();
        // add break points
        final long breakpointId = getProcessAPI().addBreakpoint(processDefinition.getId(), "gateway1", 2, 45);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final Long waitForFlowNodeId = waitForFlowNode(processInstance.getId(), TestStates.getInterruptingState(), "gateway1", false,
                25000);
        assertNotNull(waitForFlowNodeId);

        getProcessAPI().cancelProcessInstance(processInstance.getId());

        // resume break point
        getProcessAPI().executeActivity(waitForFlowNodeId);
        waitForProcessToFinish(processInstance, TestStates.getCancelledState());

        // verify that the execution does not pass through the activities after the gateway
        checkWasntExecuted(processInstance, "step2");
        checkWasntExecuted(processInstance, "step3");
        getProcessAPI().removeBreakpoint(breakpointId);
        disableAndDelete(processDefinition);

    }

    @Test
    public void cancelExclusiveGatewayWithDefaultTransition() throws Exception {
        final ProcessDefinition processDefinition = deployProcessWithExclusiveSplitGateway();
        // add break points
        final long breakpointId = getProcessAPI().addBreakpoint(processDefinition.getId(), "gateway1", 2, 45);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final Long waitForFlowNodeId = waitForFlowNode(processInstance.getId(), TestStates.getInterruptingState(), "gateway1", false,
                25000);
        assertNotNull(waitForFlowNodeId);

        getProcessAPI().cancelProcessInstance(processInstance.getId());

        // resume break point
        getProcessAPI().executeActivity(waitForFlowNodeId);
        waitForProcessToFinish(processInstance, TestStates.getCancelledState());

        // verify that the execution does not pass through the activities after the gateway
        checkWasntExecuted(processInstance, "step2");
        checkWasntExecuted(processInstance, "step3");
        checkWasntExecuted(processInstance, "step4");
        getProcessAPI().removeBreakpoint(breakpointId);
        disableAndDelete(processDefinition);
    }

    private ProcessDefinition deployProcessWithTimerIntermediateCatchEvent(final TimerType timerType, final Expression timerValue, final String step1Name,
            final String step2Name) throws BonitaException {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("My Process with start event", "1.0");
        processDefinitionBuilder.addStartEvent("start");
        processDefinitionBuilder.addAutomaticTask(step1Name);
        processDefinitionBuilder.addIntermediateCatchEvent("intermediateCatchEvent").addTimerEventTriggerDefinition(timerType, timerValue);
        processDefinitionBuilder.addAutomaticTask(step2Name);
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("start", step1Name);
        processDefinitionBuilder.addTransition(step1Name, "intermediateCatchEvent");
        processDefinitionBuilder.addTransition("intermediateCatchEvent", step2Name);
        processDefinitionBuilder.addTransition(step2Name, "end");

        final ProcessDefinition definition = deployAndEnableProcess(processDefinitionBuilder.getProcess());
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());
        return definition;
    }

}
