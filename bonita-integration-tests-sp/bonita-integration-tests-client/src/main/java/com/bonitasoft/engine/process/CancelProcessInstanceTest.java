/*******************************************************************************
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.EventInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
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

        // add breakpoints
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("definitionId", processDefinition.getId());
        parameters.put("elementName", taskName1);
        parameters.put("idOfTheStateToInterrupt", 2);
        parameters.put("idOfTheInterruptingState", 45);
        final Long breakpointId1 = (Long) getCommandAPI().execute("addBreakpoint", parameters);

        final Map<String, Serializable> parameters2 = new HashMap<String, Serializable>();
        parameters2.put("definitionId", processDefinition.getId());
        parameters2.put("elementName", taskName2);
        parameters2.put("idOfTheStateToInterrupt", 2);
        parameters2.put("idOfTheInterruptingState", 45);
        final Long breakpointId2 = (Long) getCommandAPI().execute("addBreakpoint", parameters2);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final CheckNbOfActivities checkNbOfInterrupted = checkNbOfActivitiesInInterruptingState(processInstance, 2);

        final Iterator<ActivityInstance> iterator = checkNbOfInterrupted.getResult().iterator();
        final ActivityInstance task1 = iterator.next();
        final ActivityInstance task2 = iterator.next();

        getProcessAPI().cancelProcessInstance(processInstance.getId());

        getProcessAPI().executeFlowNode(task1.getId());
        getProcessAPI().executeFlowNode(task2.getId());

        waitForProcessToFinish(processInstance, TestStates.getCancelledState());
        waitForArchivedActivity(task1.getId(), TestStates.getCancelledState());
        waitForArchivedActivity(task2.getId(), TestStates.getCancelledState());

        getCommandAPI().execute("removeBreakpoint", Collections.singletonMap("breakpointId", (Serializable) breakpointId1));
        getCommandAPI().execute("removeBreakpoint", Collections.singletonMap("breakpointId", (Serializable) breakpointId2));
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void cancelStartEvent() throws Exception {
        final String taskName1 = "auto1";
        final String taskName2 = "auto2";
        final ProcessDefinition processDefinition = deployProcessWith2AutomaticTasks(taskName1, taskName2);

        // add breakpoint
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("definitionId", processDefinition.getId());
        parameters.put("elementName", "start");
        parameters.put("idOfTheStateToInterrupt", 2);
        parameters.put("idOfTheInterruptingState", 45);
        final Long breakpointId = (Long) getCommandAPI().execute("addBreakpoint", parameters);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final WaitForEvent waitForInterrupted = waitForEvent(processInstance, "start", TestStates.getInterruptingState());

        final EventInstance start = waitForInterrupted.getResult();
        assertEquals("start", start.getName());

        getProcessAPI().cancelProcessInstance(processInstance.getId());

        // resume breakpoint
        getProcessAPI().executeFlowNode(start.getId());

        waitForProcessToFinish(processInstance, TestStates.getCancelledState());

        final List<ArchivedActivityInstance> archivedActivityInstances = getProcessAPI().getArchivedActivityInstances(processInstance.getId(), 0, 20,
                ActivityInstanceCriterion.NAME_ASC);

        // only start even must exist and its not an activity instance
        assertEquals(0, archivedActivityInstances.size());
        getCommandAPI().execute("removeBreakpoint", Collections.singletonMap("breakpointId", (Serializable) breakpointId));
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void cancelEndEvent() throws Exception {
        final String taskName1 = "auto1";
        final String taskName2 = "auto2";
        final ProcessDefinition processDefinition = deployProcessWith2AutomaticTasks(taskName1, taskName2);

        // add breakpoint
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("definitionId", processDefinition.getId());
        parameters.put("elementName", "end1");
        parameters.put("idOfTheStateToInterrupt", 2);
        parameters.put("idOfTheInterruptingState", 45);
        final Long breakpointId = (Long) getCommandAPI().execute("addBreakpoint", parameters);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final WaitForEvent waitForInterrupted = waitForEvent(processInstance, "end1", TestStates.getInterruptingState());
        final EventInstance end = waitForInterrupted.getResult();
        assertEquals("end1", end.getName());

        getProcessAPI().cancelProcessInstance(processInstance.getId());

        // resume breakpoint
        getProcessAPI().executeFlowNode(end.getId());

        waitForProcessToFinish(processInstance, TestStates.getCancelledState());
        getCommandAPI().execute("removeBreakpoint", Collections.singletonMap("breakpointId", (Serializable) breakpointId));
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void cancelIntermediateThrowEvent() throws Exception {
        final String eventName = "sendMessage";
        final ProcessDefinition processDefinition = deployProcessWithIntermediateThrowMessageEvent(eventName, "m1", "p1", "receiveMessage");

        // add breakpoint
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("definitionId", processDefinition.getId());
        parameters.put("elementName", "sendMessage");
        parameters.put("idOfTheStateToInterrupt", 2);
        parameters.put("idOfTheInterruptingState", 45);
        final Long breakpointId = (Long) getCommandAPI().execute("addBreakpoint", parameters);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final WaitForEvent waitForInterrupted = waitForEvent(processInstance, "sendMessage", TestStates.getInterruptingState());
        final EventInstance event = waitForInterrupted.getResult();
        assertEquals("sendMessage", event.getName());

        getProcessAPI().cancelProcessInstance(processInstance.getId());

        // resume breakpoint
        getProcessAPI().executeFlowNode(event.getId());

        waitForProcessToFinish(processInstance, TestStates.getCancelledState());

        // verify that the execution does not pass through the activity after the intermediate event
        checkWasntExecuted(processInstance, "auto2");
        getCommandAPI().execute("removeBreakpoint", Collections.singletonMap("breakpointId", (Serializable) breakpointId));
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void cancelParallelMergeGatewayIntance() throws Exception {
        final ProcessDefinition processDefinition = deployProcessWithParallelGateways();
        // add breakpoint
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("definitionId", processDefinition.getId());
        parameters.put("elementName", "gateway2");
        parameters.put("idOfTheStateToInterrupt", 2);
        parameters.put("idOfTheInterruptingState", 45);
        final Long breakpointId = (Long) getCommandAPI().execute("addBreakpoint", parameters);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final FlowNodeInstance waitForFlowNode = waitForFlowNodeInInterruptingState(processInstance, "gateway2", false);

        getProcessAPI().cancelProcessInstance(processInstance.getId());

        // resume break point
        getProcessAPI().executeFlowNode(waitForFlowNode.getId());
        waitForProcessToFinish(processInstance, TestStates.getCancelledState());

        // verify that the execution does not pass through the activity after the gateway
        checkWasntExecuted(processInstance, "step4");
        getCommandAPI().execute("removeBreakpoint", Collections.singletonMap("breakpointId", (Serializable) breakpointId));
        disableAndDeleteProcess(processDefinition);

    }

    @Test
    public void cancelParallelSplitGatewayIntance() throws Exception {
        final ProcessDefinition processDefinition = deployProcessWithParallelGateways();
        // add breakpoint
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("definitionId", processDefinition.getId());
        parameters.put("elementName", "gateway1");
        parameters.put("idOfTheStateToInterrupt", 2);
        parameters.put("idOfTheInterruptingState", 45);
        final Long breakpointId = (Long) getCommandAPI().execute("addBreakpoint", parameters);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final FlowNodeInstance waitForFlowNode = waitForFlowNodeInInterruptingState(processInstance, "gateway1", false);
        assertNotNull(waitForFlowNode);

        getProcessAPI().cancelProcessInstance(processInstance.getId());

        // resume break point
        getProcessAPI().executeFlowNode(waitForFlowNode.getId());
        waitForProcessToFinish(processInstance, TestStates.getCancelledState());

        // verify that the execution does not pass through the activities after the gateway
        checkWasntExecuted(processInstance, "step2");
        checkWasntExecuted(processInstance, "step3");
        getCommandAPI().execute("removeBreakpoint", Collections.singletonMap("breakpointId", (Serializable) breakpointId));
        disableAndDeleteProcess(processDefinition);

    }

    @Test
    public void cancelExclusiveGatewayWithDefaultTransition() throws Exception {
        final ProcessDefinition processDefinition = deployProcessWithExclusiveSplitGateway();
        // add breakpoint
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("definitionId", processDefinition.getId());
        parameters.put("elementName", "gateway1");
        parameters.put("idOfTheStateToInterrupt", 2);
        parameters.put("idOfTheInterruptingState", 45);
        final Long breakpointId = (Long) getCommandAPI().execute("addBreakpoint", parameters);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final FlowNodeInstance waitForFlowNode = waitForFlowNodeInInterruptingState(processInstance, "gateway1", false);

        getProcessAPI().cancelProcessInstance(processInstance.getId());

        // resume break point
        getProcessAPI().executeFlowNode(waitForFlowNode.getId());
        waitForProcessToFinish(processInstance, TestStates.getCancelledState());

        // verify that the execution does not pass through the activities after the gateway
        checkWasntExecuted(processInstance, "step2");
        checkWasntExecuted(processInstance, "step3");
        checkWasntExecuted(processInstance, "step4");
        getCommandAPI().execute("removeBreakpoint", Collections.singletonMap("breakpointId", (Serializable) breakpointId));
        disableAndDeleteProcess(processDefinition);
    }

}
