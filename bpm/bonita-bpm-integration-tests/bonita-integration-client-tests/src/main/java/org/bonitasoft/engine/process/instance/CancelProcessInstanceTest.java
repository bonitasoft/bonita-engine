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
package org.bonitasoft.engine.process.instance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.flownode.WaitingEvent;
import org.bonitasoft.engine.bpm.flownode.WaitingEventSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceCriterion;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.check.CheckNbOfActivities;
import org.bonitasoft.engine.test.check.CheckNbPendingTaskOf;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class CancelProcessInstanceTest extends AbstractProcessInstanceTest {

    private static final String SEARCH_WAITING_EVENTS_COMMAND = "searchWaitingEventsCommand";

    private static final String SEARCH_OPTIONS_KEY = "searchOptions";

    @Test
    public void cancelProcessInstanceWithHumanTasks() throws Exception {
        final String actorName = "delivery";
        final String taskName1 = "userTask1";
        final String taskName2 = "userTask2";
        final ProcessDefinition processDefinition = deployProcessWith2UserTasksAnd1AutoTask(actorName, taskName1, taskName2, "auto1");

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final CheckNbPendingTaskOf checkNbPendingTaskOf = new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, true, 2, pedro);
        assertTrue("Expected 2 pending tasks", checkNbPendingTaskOf.waitUntil());

        getProcessAPI().cancelProcessInstance(processInstance.getId());
        waitForProcessToFinish(processInstance, TestStates.getCancelledState());

        final List<HumanTaskInstance> pendingTasks = checkNbPendingTaskOf.getPendingHumanTaskInstances();
        final ArchivedActivityInstance archivedTask1 = getProcessAPI().getArchivedActivityInstance(pendingTasks.get(0).getId());
        assertEquals(TestStates.getCancelledState(), archivedTask1.getState());

        final ArchivedActivityInstance archivedTask2 = getProcessAPI().getArchivedActivityInstance(pendingTasks.get(1).getId());
        assertEquals(TestStates.getCancelledState(), archivedTask2.getState());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void cancelCallActivity() throws Exception {
        final String actorName = "delivery";
        final String taskName1 = "userTask1";
        final String taskName2 = "userTask2";
        final String autoTaskName = "auto1";
        final ProcessDefinition targetProcessDef = deployProcessWith2UserTasksAnd1AutoTask(actorName, taskName1, taskName2, autoTaskName);
        final ProcessDefinition callActivityProcDef = deployProcessWithCallActivity(taskName1, "callActivity", targetProcessDef.getName(),
                targetProcessDef.getVersion(), taskName2);
        final ProcessInstance parentProcessInstance = getProcessAPI().startProcess(callActivityProcDef.getId());

        final FlowNodeInstance waitForFlowNode = waitForFlowNodeInExecutingState(parentProcessInstance, "callActivity", false);
        assertNotNull("Expected call activity in executing state", waitForFlowNode);

        checkNbOfProcessInstances(2, ProcessInstanceCriterion.NAME_DESC);
        final List<ProcessInstance> processInstances = getProcessAPI().getProcessInstances(0, 10, ProcessInstanceCriterion.NAME_ASC);
        assertEquals(2, processInstances.size());
        final ProcessInstance targetProcessInstance = processInstances.get(0);
        assertEquals(targetProcessDef.getId(), targetProcessInstance.getProcessDefinitionId());

        final CheckNbOfActivities checkNbOfActivities = new CheckNbOfActivities(getProcessAPI(), 50, 5000, true, parentProcessInstance, 2,
                TestStates.getReadyState());
        assertTrue(checkNbOfActivities.waitUntil());

        getProcessAPI().cancelProcessInstance(parentProcessInstance.getId());

        waitForProcessToFinish(parentProcessInstance, TestStates.getCancelledState());
        waitForProcessToFinish(targetProcessInstance, TestStates.getCancelledState());

        checkWasntExecuted(targetProcessInstance, autoTaskName);
        checkWasntExecuted(parentProcessInstance, taskName2);

        disableAndDeleteProcess(callActivityProcDef);
        disableAndDeleteProcess(targetProcessDef);
    }

    private ProcessDefinition deployProcessWithTimerIntermediateCatchEvent(final TimerType timerType, final Expression timerValue, final String stepName)
            throws BonitaException {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("My Process with start event", "1.0");
        processDefinitionBuilder.addIntermediateCatchEvent("intermediateCatchEvent").addTimerEventTriggerDefinition(timerType, timerValue);
        processDefinitionBuilder.addAutomaticTask(stepName);
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("intermediateCatchEvent", stepName);
        processDefinitionBuilder.addTransition(stepName, "end");

        final ProcessDefinition definition = deployAndEnableProcess(processDefinitionBuilder.getProcess());
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());
        return definition;
    }

    @Test
    public void cancelProcessInstanceWithTimerIntermediateCatchEvent() throws Exception {
        final int timerTrigger = 2000; // the timer intermediate catch event will wait 2
        final Expression timerExpression = new ExpressionBuilder().createConstantLongExpression(timerTrigger);
        // seconds
        final ProcessDefinition definition = deployProcessWithTimerIntermediateCatchEvent(TimerType.DURATION, timerExpression, "step");

        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());
        waitForEventInWaitingState(processInstance, "intermediateCatchEvent");

        getProcessAPI().cancelProcessInstance(processInstance.getId());

        // FIXME: the exception only will be available in the logs. It will be necessary to add a new method in the API to get the list of available jobs
        Thread.sleep(timerTrigger); // wait to be sure the timer will not be triggered.

        waitForProcessToFinish(processInstance, TestStates.getCancelledState());

        // FIXME: comment out the lines below when intermediate catch events are archived
        // final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        // searchOptionsBuilder.filter(ArchivedFlowNodeInstanceSearchDescriptor.ORIGINAL_FLOW_NODE_ID, waitForEvent.getResult().getId());
        // searchOptionsBuilder.filter(ArchivedFlowNodeInstanceSearchDescriptor.STATE_NAME, TestStates.getCancelledState());
        // final SearchResult<ArchivedFlowNodeInstance> searchArchivedFlowNodeInstances = getProcessAPI().searchArchivedFlowNodeInstances(
        // searchOptionsBuilder.done());
        // assertEquals(1, searchArchivedFlowNodeInstances.getCount());

        checkWasntExecuted(processInstance, "step");

        disableAndDeleteProcess(definition);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void cancelProcessInstanceWithIntermediateCatchMessageEvent() throws Exception {
        final String catchMessageEvent = "receiveMessage";
        final String previousStep = "auto1";
        final String nextStep = "auto2";
        final ProcessDefinition receiveProcess = deployProcessWithIntermediateCatchMessageEvent(catchMessageEvent, "m1", previousStep, nextStep);

        final ProcessInstance receiveProcessInstance = getProcessAPI().startProcess(receiveProcess.getId());
        waitForEventInWaitingState(receiveProcessInstance, catchMessageEvent);

        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.filter(WaitingEventSearchDescriptor.FLOW_NODE_NAME, catchMessageEvent);

        final Map<String, Serializable> parameters = new HashMap<String, Serializable>(1);
        parameters.put(SEARCH_OPTIONS_KEY, searchOptionsBuilder.done());

        SearchResult<WaitingEvent> searchResult = (SearchResult<WaitingEvent>) getCommandAPI().execute(SEARCH_WAITING_EVENTS_COMMAND, parameters);
        assertEquals(1, searchResult.getCount());

        getProcessAPI().cancelProcessInstance(receiveProcessInstance.getId());
        waitForProcessToFinish(receiveProcessInstance, TestStates.getCancelledState());

        searchResult = (SearchResult<WaitingEvent>) getCommandAPI().execute(SEARCH_WAITING_EVENTS_COMMAND, parameters);
        assertEquals(0, searchResult.getCount());

        checkWasntExecuted(receiveProcessInstance, nextStep);

        disableAndDeleteProcess(receiveProcess);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void cancelProcessInstanceWithIntermediateCatchSignalEvent() throws Exception {
        final String catchMessageEvent = "receiveSignal";
        final String previousStep = "auto1";
        final String nextStep = "auto2";
        final ProcessDefinition receiveProcess = deployProcessWithIntermediateCatchSignalEvent(catchMessageEvent, "s1", previousStep, nextStep);

        final ProcessInstance receiveProcessInstance = getProcessAPI().startProcess(receiveProcess.getId());
        waitForEventInWaitingState(receiveProcessInstance, catchMessageEvent);

        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.filter(WaitingEventSearchDescriptor.FLOW_NODE_NAME, catchMessageEvent);

        final Map<String, Serializable> parameters = new HashMap<String, Serializable>(1);
        parameters.put(SEARCH_OPTIONS_KEY, searchOptionsBuilder.done());

        SearchResult<WaitingEvent> searchResult = (SearchResult<WaitingEvent>) getCommandAPI().execute(SEARCH_WAITING_EVENTS_COMMAND, parameters);
        assertEquals(1, searchResult.getCount());

        getProcessAPI().cancelProcessInstance(receiveProcessInstance.getId());
        waitForProcessToFinish(receiveProcessInstance, TestStates.getCancelledState());

        searchResult = (SearchResult<WaitingEvent>) getCommandAPI().execute(SEARCH_WAITING_EVENTS_COMMAND, parameters);
        assertEquals(0, searchResult.getCount());

        checkWasntExecuted(receiveProcessInstance, nextStep);

        disableAndDeleteProcess(receiveProcess);
    }

    @Test(expected = ProcessInstanceNotFoundException.class)
    public void cancelUnknownProcessInstance() throws Exception {
        getProcessAPI().cancelProcessInstance(45);
    }

}
