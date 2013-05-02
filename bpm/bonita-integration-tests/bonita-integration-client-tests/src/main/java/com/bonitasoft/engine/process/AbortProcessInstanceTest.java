package com.bonitasoft.engine.process;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.model.ProcessDefinition;
import org.bonitasoft.engine.bpm.model.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.model.ProcessInstance;
import org.bonitasoft.engine.bpm.model.StateCategory;
import org.bonitasoft.engine.bpm.model.archive.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionConstants;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.descriptor.ArchivedFlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.check.CheckNbOfProcessInstances;
import org.bonitasoft.engine.test.wait.WaitForFlowNode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AbortProcessInstanceTest extends InterruptProcessInstanceTest {

    private ProcessDefinition deployProcessWithMultiInstanceCallActivity(final int loopCardinality, final String targetProcess, final String targetVersion)
            throws BonitaException {
        final Expression targetProcExpr = new ExpressionBuilder().createConstantStringExpression(targetProcess);
        final Expression targetVersionExpr = new ExpressionBuilder().createConstantStringExpression(targetVersion);

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder()
                .createNewInstance("RemainingInstancesAreAbortedAfterCompletionCondition", "1.0");
        builder.addStartEvent("start");
        builder.addCallActivity("callActivity", targetProcExpr, targetVersionExpr)
                .addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(loopCardinality))
                .addCompletionCondition(
                        new ExpressionBuilder().createGroovyScriptExpression("deployProcessWithMultiInstanceCallActivity",
                                ExpressionConstants.NUMBER_OF_COMPLETED_INSTANCES.getEngineName() + " == 1 ", Boolean.class.getName(),
                                new ExpressionBuilder().createEngineConstant(ExpressionConstants.NUMBER_OF_COMPLETED_INSTANCES)));
        builder.addEndEvent("end");
        builder.addTransition("start", "callActivity");
        builder.addTransition("callActivity", "end");

        return deployAndEnableProcess(builder.done());
    }

    @Test
    public void abortProcessWithGateways() throws Exception {
        final ProcessDefinition targetProcess = deployProcessWithParallelGateways();
        final int loopCardinality = 2;
        final ProcessDefinition parentProcess = deployProcessWithMultiInstanceCallActivity(loopCardinality, targetProcess.getName(), targetProcess.getVersion());

        // add breakpoint
        final String gatewayName = "gateway1";

        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("definitionId", targetProcess.getId());
        parameters.put("elementName", gatewayName);
        parameters.put("idOfTheStateToInterrupt", 2);
        parameters.put("idOfTheInterruptingState", 45);
        final Long breakpointId = (Long) getCommandAPI().execute("addBreakpoint", parameters);
        final ProcessInstance parentProcessInstance = getProcessAPI().startProcess(parentProcess.getId());

        final CheckNbOfProcessInstances checkNbOfProcessInstances = checkNbOfProcessInstances(loopCardinality + 1);
        final List<ProcessInstance> processInstances = checkNbOfProcessInstances.getResult();
        final ProcessInstance targetProcInstToBeExecuted = processInstances.get(0);
        assertEquals(targetProcess.getId(), targetProcInstToBeExecuted.getProcessDefinitionId());
        final ProcessInstance targetProcInstToBeAborted = processInstances.get(1);
        assertEquals(targetProcess.getId(), targetProcInstToBeAborted.getProcessDefinitionId());

        // wait for the breakpoint
        final Long waitForGatewayToBeExecutedId = waitForFlowNode(targetProcInstToBeExecuted.getId(), TestStates.getInterruptingState(), gatewayName, false,
                350000);
        assertNotNull(waitForGatewayToBeExecutedId);

        final Long waitForGatewayToBeAbortedId = waitForFlowNode(targetProcInstToBeAborted.getId(), TestStates.getInterruptingState(), gatewayName, false,
                350000);
        assertNotNull(waitForGatewayToBeAbortedId);

        // resume the break point for the gateway of the first child --> normal state
        getProcessAPI().executeActivity(waitForGatewayToBeExecutedId);
        waitForProcessToFinish(targetProcInstToBeExecuted);

        final WaitForFlowNode waitForAbortingCategory = new WaitForFlowNode(50, 5000, gatewayName, targetProcInstToBeAborted.getId(), StateCategory.ABORTING,
                false, getProcessAPI());
        assertTrue(waitForAbortingCategory.waitUntil());

        // resume the break point for the gateway of the second child --> aborted state
        getProcessAPI().executeActivity(waitForGatewayToBeAbortedId);

        // the target process instances that exceed the max loop must be in aborted state
        waitForProcessToFinish(targetProcInstToBeAborted, TestStates.getAbortedState());

        // the gateway not executed must be in aborted state
        final SearchOptionsBuilder searchBuilder = new SearchOptionsBuilder(0, 10);
        searchBuilder.filter(ArchivedFlowNodeInstanceSearchDescriptor.ORIGINAL_FLOW_NODE_ID, waitForGatewayToBeAbortedId);
        searchBuilder.filter(ArchivedFlowNodeInstanceSearchDescriptor.STATE_NAME, TestStates.getAbortedState());
        final SearchResult<ArchivedFlowNodeInstance> searchActivities = getProcessAPI().searchArchivedFlowNodeInstances(searchBuilder.done());
        assertEquals(1, searchActivities.getCount());

        // check the automatic tasks following the gateway weren't created
        checkWasntExecuted(targetProcInstToBeAborted, "step2");
        checkWasntExecuted(targetProcInstToBeAborted, "step3");

        // the parent process instance must finish in normal state
        waitForProcessToFinish(parentProcessInstance);
        getCommandAPI().execute("removeBreakpoint", Collections.singletonMap("breakpointId", (Serializable) breakpointId));
        disableAndDelete(parentProcess);
        disableAndDelete(targetProcess);
    }

    @Test
    public void abortProcessWithAutomaticTasks() throws Exception {
        final String taskName1 = "auto1";
        final String taskName2 = "auto2";
        final ProcessDefinition targetProcess = deployProcessWith2AutomaticTasks(taskName1, taskName2);
        final int loopCardinality = 2;
        final ProcessDefinition parentProcess = deployProcessWithMultiInstanceCallActivity(loopCardinality, targetProcess.getName(), targetProcess.getVersion());

        // add breakpoint
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("definitionId", targetProcess.getId());
        parameters.put("elementName", taskName1);
        parameters.put("idOfTheStateToInterrupt", 2);
        parameters.put("idOfTheInterruptingState", 45);
        final Long breakpointId = (Long) getCommandAPI().execute("addBreakpoint", parameters);
        final ProcessInstance parentProcessInstance = getProcessAPI().startProcess(parentProcess.getId());

        final CheckNbOfProcessInstances checkNbOfProcessInstances = checkNbOfProcessInstances(loopCardinality + 1);;
        assertTrue(checkNbOfProcessInstances.waitUntil());
        final List<ProcessInstance> processInstances = checkNbOfProcessInstances.getResult();
        final ProcessInstance targetProcInstToBeExecuted = processInstances.get(0);
        assertEquals(targetProcess.getId(), targetProcInstToBeExecuted.getProcessDefinitionId());
        final ProcessInstance targetProcInstToBeAborted = processInstances.get(1);
        assertEquals(targetProcess.getId(), targetProcInstToBeAborted.getProcessDefinitionId());

        // wait for the breakpoint
        final Long waitForFlowNodeToBeExecutedId = waitForFlowNode(targetProcInstToBeExecuted.getId(), TestStates.getInterruptingState(), taskName1, false,
                25000);
        assertNotNull(waitForFlowNodeToBeExecutedId);

        final Long waitForFlowNodeToBeAbortedId = waitForFlowNode(targetProcInstToBeAborted.getId(), TestStates.getInterruptingState(), taskName1, false, 25000);
        assertNotNull(waitForFlowNodeToBeAbortedId);

        // resume the break point for the first child --> normal state
        getProcessAPI().executeActivity(waitForFlowNodeToBeExecutedId);
        waitForProcessToFinish(targetProcInstToBeExecuted);

        final WaitForFlowNode waitForAbortingCategory = new WaitForFlowNode(50, 5000, taskName1, targetProcInstToBeAborted.getId(), StateCategory.ABORTING,
                false, getProcessAPI());
        assertTrue(waitForAbortingCategory.waitUntil());

        // resume the break point for the second child --> aborted state
        getProcessAPI().executeActivity(waitForFlowNodeToBeAbortedId);

        // the target process instances that exceed the max loop must be in aborted state
        waitForProcessToFinish(targetProcInstToBeAborted, TestStates.getAbortedState());

        // the task not executed must be in aborted state
        final SearchOptionsBuilder searchBuilder = new SearchOptionsBuilder(0, 10);
        searchBuilder.filter(ArchivedFlowNodeInstanceSearchDescriptor.ORIGINAL_FLOW_NODE_ID, waitForFlowNodeToBeAbortedId);
        searchBuilder.filter(ArchivedFlowNodeInstanceSearchDescriptor.STATE_NAME, TestStates.getAbortedState());
        final SearchResult<ArchivedFlowNodeInstance> searchActivities = getProcessAPI().searchArchivedFlowNodeInstances(searchBuilder.done());
        assertEquals(1, searchActivities.getCount());

        // check the end event wasn't executed
        checkWasntExecuted(targetProcInstToBeAborted, "end");

        // the parent process instance must finish in normal state
        waitForProcessToFinish(parentProcessInstance);

        getCommandAPI().execute("removeBreakpoint", Collections.singletonMap("breakpointId", (Serializable) breakpointId));
        disableAndDelete(parentProcess);
        disableAndDelete(targetProcess);
    }

    @Test
    public void abortProcessWithStartEvent() throws Exception {
        final String taskName1 = "auto1";
        final String taskName2 = "auto2";
        final ProcessDefinition targetProcess = deployProcessWith2AutomaticTasks(taskName1, taskName2);
        final int loopCardinality = 2;
        final ProcessDefinition parentProcess = deployProcessWithMultiInstanceCallActivity(loopCardinality, targetProcess.getName(), targetProcess.getVersion());

        // add breakpoint
        final String startEventName = "start";
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("definitionId", targetProcess.getId());
        parameters.put("elementName", startEventName);
        parameters.put("idOfTheStateToInterrupt", 2);
        parameters.put("idOfTheInterruptingState", 45);
        final Long breakpointId = (Long) getCommandAPI().execute("addBreakpoint", parameters);
        final ProcessInstance parentProcessInstance = getProcessAPI().startProcess(parentProcess.getId());

        final CheckNbOfProcessInstances checkNbOfProcessInstances = checkNbOfProcessInstances(loopCardinality + 1);;
        assertTrue(checkNbOfProcessInstances.waitUntil());
        final List<ProcessInstance> processInstances = checkNbOfProcessInstances.getResult();
        final ProcessInstance targetProcInstToBeExecuted = processInstances.get(0);
        assertEquals(targetProcess.getId(), targetProcInstToBeExecuted.getProcessDefinitionId());
        final ProcessInstance targetProcInstToBeAborted = processInstances.get(1);
        assertEquals(targetProcess.getId(), targetProcInstToBeAborted.getProcessDefinitionId());

        // wait for the breakpoint
        final Long waitForFlowNodeToBeExecutedId = waitForFlowNode(targetProcInstToBeExecuted.getId(), TestStates.getInterruptingState(), startEventName,
                false, 25000);
        assertNotNull(waitForFlowNodeToBeExecutedId);

        final Long waitForFlowNodeToBeAbortedId = waitForFlowNode(targetProcInstToBeAborted.getId(), TestStates.getInterruptingState(), startEventName, false,
                25000);
        assertNotNull(waitForFlowNodeToBeAbortedId);

        // resume the break point for the the first child --> normal state
        getProcessAPI().executeActivity(waitForFlowNodeToBeExecutedId);
        waitForProcessToFinish(targetProcInstToBeExecuted);

        final WaitForFlowNode waitForAbortingCategory = new WaitForFlowNode(50, 5000, startEventName, targetProcInstToBeAborted.getId(),
                StateCategory.ABORTING, false, getProcessAPI());
        assertTrue(waitForAbortingCategory.waitUntil());

        // resume the break point for the second child --> aborted state
        getProcessAPI().executeActivity(waitForFlowNodeToBeAbortedId);

        // the target process instances that exceed the max loop must be in aborted state
        waitForProcessToFinish(targetProcInstToBeAborted, TestStates.getAbortedState());

        // the start event not executed must be in aborted state
        // FIXME: uncomment the code below when archive mechanism deals with events
        // final SearchOptionsBuilder searchBuilder = new SearchOptionsBuilder(0, 10);
        // searchBuilder.filter(ArchivedFlowNodeInstanceSearchDescriptor.ORIGINAL_FLOW_NODE_ID, waitForFlowNodeToBeAborted.getResult().getId());
        // searchBuilder.filter(ArchivedFlowNodeInstanceSearchDescriptor.STATE_NAME, TestStates.getAbortedState());
        // final SearchResult<ArchivedFlowNodeInstance> searchActivities = getProcessAPI().searchArchivedFlowNodeInstances(searchBuilder.done());
        // assertEquals(1, searchActivities.getCount());

        checkWasntExecuted(targetProcInstToBeAborted, taskName1);
        checkWasntExecuted(targetProcInstToBeAborted, taskName2);

        // the parent process instance must finish in normal state
        waitForProcessToFinish(parentProcessInstance);
        getCommandAPI().execute("removeBreakpoint", Collections.singletonMap("breakpointId", (Serializable) breakpointId));
        disableAndDelete(parentProcess);
        disableAndDelete(targetProcess);
    }

    @Test
    public void abortProcessWithIntermediateThrowEvent() throws Exception {
        final String throwEventName = "throwSignal";
        final ProcessDefinition targetProcess = deployProcessWithIntermediateThrowSignalEvent(throwEventName, "s1");
        final int loopCardinality = 2;
        final ProcessDefinition parentProcess = deployProcessWithMultiInstanceCallActivity(loopCardinality, targetProcess.getName(), targetProcess.getVersion());

        // add breakpoint
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("definitionId", targetProcess.getId());
        parameters.put("elementName", throwEventName);
        parameters.put("idOfTheStateToInterrupt", 2);
        parameters.put("idOfTheInterruptingState", 45);
        final Long breakpointId = (Long) getCommandAPI().execute("addBreakpoint", parameters);
        final ProcessInstance parentProcessInstance = getProcessAPI().startProcess(parentProcess.getId());

        final CheckNbOfProcessInstances checkNbOfProcessInstances = checkNbOfProcessInstances(loopCardinality + 1);;
        assertTrue(checkNbOfProcessInstances.waitUntil());
        final List<ProcessInstance> processInstances = checkNbOfProcessInstances.getResult();
        final ProcessInstance targetProcInstToBeExecuted = processInstances.get(0);
        assertEquals(targetProcess.getId(), targetProcInstToBeExecuted.getProcessDefinitionId());
        final ProcessInstance targetProcInstToBeAborted = processInstances.get(1);
        assertEquals(targetProcess.getId(), targetProcInstToBeAborted.getProcessDefinitionId());

        // wait for the breakpoint
        final Long waitForFlowNodeToBeExecutedId = waitForFlowNode(targetProcInstToBeExecuted.getId(), TestStates.getInterruptingState(), throwEventName,
                false, 25000);
        assertNotNull(waitForFlowNodeToBeExecutedId);

        final Long waitForFlowNodeToBeAbortedId = waitForFlowNode(targetProcInstToBeAborted.getId(), TestStates.getInterruptingState(), throwEventName, false,
                25000);
        assertNotNull(waitForFlowNodeToBeAbortedId);

        // resume the break point for the the first child --> normal state
        getProcessAPI().executeActivity(waitForFlowNodeToBeExecutedId);
        waitForProcessToFinish(targetProcInstToBeExecuted);

        final WaitForFlowNode waitForAbortingCategory = new WaitForFlowNode(50, 5000, throwEventName, targetProcInstToBeAborted.getId(),
                StateCategory.ABORTING, false, getProcessAPI());
        assertTrue(waitForAbortingCategory.waitUntil());

        // resume the break point for the second child --> aborted state
        getProcessAPI().executeActivity(waitForFlowNodeToBeAbortedId);

        // the target process instances that exceed the max loop must be in aborted state
        waitForProcessToFinish(targetProcInstToBeAborted, TestStates.getAbortedState());

        // the event not executed must be in aborted state
        // FIXME: uncomment final the code below final when archive mechanism final deals with events
        // final SearchOptionsBuilder searchBuilder = new SearchOptionsBuilder(0, 10);
        // searchBuilder.filter(ArchivedFlowNodeInstanceSearchDescriptor.ORIGINAL_FLOW_NODE_ID, waitForFlowNodeToBeAborted.getResult().getId());
        // searchBuilder.filter(ArchivedFlowNodeInstanceSearchDescriptor.STATE_NAME, TestStates.getAbortedState());
        // final SearchResult<ArchivedFlowNodeInstance> searchActivities = getProcessAPI().searchArchivedFlowNodeInstances(searchBuilder.done());
        // assertEquals(1, searchActivities.getCount());

        // check the end event wasn't executed
        checkWasntExecuted(targetProcInstToBeAborted, "end");

        // the parent process instance must finish in normal state
        waitForProcessToFinish(parentProcessInstance);
        getCommandAPI().execute("removeBreakpoint", Collections.singletonMap("breakpointId", (Serializable) breakpointId));
        disableAndDelete(parentProcess);
        disableAndDelete(targetProcess);
    }

}
