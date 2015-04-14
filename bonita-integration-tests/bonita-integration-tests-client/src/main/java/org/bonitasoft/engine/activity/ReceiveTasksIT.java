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
package org.bonitasoft.engine.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Arrays;
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
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ArchivedReceiveTaskInstance;
import org.bonitasoft.engine.bpm.flownode.EventInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeType;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.ReceiveTaskInstance;
import org.bonitasoft.engine.bpm.flownode.WaitingEvent;
import org.bonitasoft.engine.bpm.flownode.WaitingEventSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ReceiveTaskDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ThrowMessageEventTriggerBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.operation.LeftOperand;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

/**
 * @author Julien Molinaro
 */
public class ReceiveTasksIT extends TestWithUser {

    private static final String SEARCH_WAITING_EVENTS_COMMAND = "searchWaitingEventsCommand";

    private static final String SEARCH_OPTIONS_KEY = "searchOptions";

    /*
     * 1 receiveProcess, no message sent
     * dynamic -> deployAndEnable(receiveProcess), startProcess(receiveProcess)
     * checks : receiveProcess wait on receive task and don't and halt on the user task.
     */
    @SuppressWarnings("unchecked")
    @Cover(classes = { EventInstance.class, ReceiveTaskInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message event", "Receive task",
            "Send", "Receive" }, jira = "")
    @Test
    public void noMessageSentSoReceiveProcessIsWaiting() throws Exception {
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithReceivedTask("receiveMessageProcess", "waitForMessage", "userTask1",
                "delivery", user, "m1", null, null, null);

        final ProcessInstance receiveMessageProcessInstance = getProcessAPI().startProcess(receiveMessageProcess.getId());
        waitForFlowNodeInState(receiveMessageProcessInstance, "waitForMessage", TestStates.WAITING, true);

        // we check after that that the waiting event is still here
        forceMatchingOfEvents();

        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.filter(WaitingEventSearchDescriptor.ROOT_PROCESS_INSTANCE_ID, receiveMessageProcessInstance.getId());

        final Map<String, Serializable> parameters = new HashMap<String, Serializable>(1);
        parameters.put(SEARCH_OPTIONS_KEY, searchOptionsBuilder.done());

        final SearchResult<WaitingEvent> searchResult = (SearchResult<WaitingEvent>) getCommandAPI().execute(SEARCH_WAITING_EVENTS_COMMAND, parameters);
        assertEquals(1, searchResult.getCount());

        disableAndDeleteProcess(receiveMessageProcess);
    }

    /*
     * 1 receiveProcess, 1 sendProcess, Message goes from EndEvent to ReceiveTask
     * dynamic -> deployAndEnable(sendProcess), deployAndEnable(receiveProcess), startProcess(receiveProcess), startProcess(sendProcess)
     * checks : receiveProcess wait on receive task, sendProcess is finished, receiveProcess continue and halt on the user task, receive task is archived
     */
    @SuppressWarnings("unchecked")
    @Cover(classes = { EventInstance.class, ReceiveTaskInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message event", "Receive task",
            "Send", "Receive" }, jira = "")
    @Test
    public void receiveMessageSentAfterReceiveProcessIsWaiting() throws Exception {
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithEndMessageEvent("sendMessageProcess", "m2", "receiveMessageProcess",
                "waitForMessage", null, null, null, null);
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithReceivedTask("receiveMessageProcess", "waitForMessage", "userTask1",
                "delivery", user, "m2", null, null, null);

        final ProcessInstance receiveMessageProcessInstance = getProcessAPI().startProcess(receiveMessageProcess.getId());
        waitForFlowNodeInState(receiveMessageProcessInstance, "waitForMessage", TestStates.WAITING, true);

        SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.filter(WaitingEventSearchDescriptor.ROOT_PROCESS_INSTANCE_ID, receiveMessageProcessInstance.getId());

        final Map<String, Serializable> parameters = new HashMap<String, Serializable>(1);
        parameters.put(SEARCH_OPTIONS_KEY, searchOptionsBuilder.done());

        SearchResult<WaitingEvent> searchResult = (SearchResult<WaitingEvent>) getCommandAPI().execute(SEARCH_WAITING_EVENTS_COMMAND, parameters);
        assertEquals(1, searchResult.getCount());

        final ProcessInstance sendMessageProcessInstance = getProcessAPI().startProcess(sendMessageProcess.getId());
        waitForProcessToFinish(sendMessageProcessInstance);
        forceMatchingOfEvents();
        waitForUserTask(receiveMessageProcessInstance, "userTask1");

        searchResult = (SearchResult<WaitingEvent>) getCommandAPI().execute(SEARCH_WAITING_EVENTS_COMMAND, parameters);
        assertEquals(0, searchResult.getCount());

        searchOptionsBuilder = new SearchOptionsBuilder(0, 20);
        searchOptionsBuilder.filter(ArchivedActivityInstanceSearchDescriptor.ROOT_PROCESS_INSTANCE_ID, receiveMessageProcessInstance.getId());
        searchOptionsBuilder.filter(ArchivedActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.RECEIVE_TASK);
        final SearchResult<ArchivedActivityInstance> archivedActivityInstancesSearch = getProcessAPI().searchArchivedActivities(searchOptionsBuilder.done());
        assertEquals(1, archivedActivityInstancesSearch.getCount());
        assertTrue(archivedActivityInstancesSearch.getResult().get(0) instanceof ArchivedReceiveTaskInstance);

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);
    }

    /*
     * Verify receiveProcess receive message targeting it, even if sent before its existence.
     * 1 receiveProcess, 1 sendProcess, Message goes from EndEvent to ReceiveTask
     * dynamic -> deployAndEnable(sendProcess), startProcess(sendProcess), deployAndEnable(receiveProcess), startProcess(receiveProcess)
     * checks : sendProcess is finished, receiveProcess goes through receive task (found message sent by sendProcess) and reaches user task.
     */
    @Cover(classes = { EventInstance.class, ReceiveTaskInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message event", "Receive task",
            "Send", "Receive" }, jira = "")
    @Test
    public void receiveMessageSentBeforeReceiveProcessIsEnabled() throws Exception {
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithEndMessageEvent("sendMessageProcess", "m3", "receiveMessageProcess",
                "waitForMessage", null, null, null, null);

        final ProcessInstance sendMessageProcessInstance = getProcessAPI().startProcess(sendMessageProcess.getId());
        waitForProcessToFinish(sendMessageProcessInstance);

        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithReceivedTask("receiveMessageProcess", "waitForMessage", "userTask1",
                "delivery", user, "m3", null, null, null);

        final ProcessInstance receiveMessageProcessInstance = getProcessAPI().startProcess(receiveMessageProcess.getId());
        forceMatchingOfEvents();
        waitForUserTask(receiveMessageProcessInstance, "userTask1");

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);
    }

    /*
     * 1 receiveProcess, 2 sendProcess, 2 Messages go from EndEvent to ReceiveTask
     * dynamic -> deployAndEnable(sendProcesses), startProcess(sendProcesses), deployAndEnable(receiveProcess), startProcess(receiveProcess)
     * checks : sendProcesses are finished, receiveProcess goes through receive task (found one message sent by sendProcess) and reaches user task.
     */
    @Cover(classes = { EventInstance.class, ReceiveTaskInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message event", "Receive task",
            "Send", "Receive" }, jira = "")
    @Test
    public void receiveMessageSentTwice() throws Exception {
        ProcessDefinition sendMessageProcess1 = null;
        ProcessDefinition sendMessageProcess2 = null;
        ProcessDefinition receiveMessageProcess = null;
        try {
            sendMessageProcess1 = deployAndEnableProcessWithEndMessageEvent("sendMessageProcess1", "m4", "receiveMessageProcess",
                    "waitForMessage", null, null, null, null);
            sendMessageProcess2 = deployAndEnableProcessWithEndMessageEvent("sendMessageProcess2", "m4", "receiveMessageProcess",
                    "waitForMessage", null, null, null, null);
            final ProcessInstance sendMessageProcessInstance1 = getProcessAPI().startProcess(sendMessageProcess1.getId());
            final ProcessInstance sendMessageProcessInstance2 = getProcessAPI().startProcess(sendMessageProcess2.getId());
            waitForProcessToFinish(sendMessageProcessInstance1);
            waitForProcessToFinish(sendMessageProcessInstance2);

            receiveMessageProcess = deployAndEnableProcessWithReceivedTask("receiveMessageProcess", "waitForMessage", "userTask1",
                    "delivery", user, "m4", null, null, null);
            final ProcessInstance receiveMessageProcessInstance = getProcessAPI().startProcess(receiveMessageProcess.getId());
            waitForTaskInState(receiveMessageProcessInstance, "waitForMessage", TestStates.WAITING);
            forceMatchingOfEvents();
            waitForUserTask(receiveMessageProcessInstance, "userTask1");
            final ProcessInstance receiveMessageProcessInstance2 = getProcessAPI().startProcess(receiveMessageProcess.getId());
            waitForTaskInState(receiveMessageProcessInstance2, "waitForMessage", TestStates.WAITING);
            forceMatchingOfEvents();
            waitForUserTask(receiveMessageProcessInstance2, "userTask1");
        } finally {
            disableAndDeleteProcess(sendMessageProcess1);
            disableAndDeleteProcess(sendMessageProcess2);
            disableAndDeleteProcess(receiveMessageProcess);
        }
    }

    /*
     * 1 receiveProcess, 1 sendProcess, Message contains datas goes from EndEvent to ReceiveTask
     * dynamic -> deployAndEnable(sendProcess), deployAndEnable(receiveProcess), startProcess(receiveProcess), startProcess(sendProcess)
     * checks : receiveProcess wait on receive task, sendProcess is finished, receiveProcess goes through receive task (found message sent by
     * sendProcess) and reaches user task, data is transmitted to
     * the receiveProcess.
     */
    @Cover(classes = { EventInstance.class, ReceiveTaskInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message event", "Receive task",
            "Send", "Receive" }, jira = "")
    @Test
    public void receiveMessageWithData() throws Exception {
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithEndMessageEvent("sendMessageProcess", "m5", "receiveMessageProcess",
                "waitForMessage", null, Collections.singletonMap("lastName", String.class.getName()),
                Collections.singletonMap("lName", String.class.getName()), Collections.singletonMap("lName", "lastName"));
        final List<Operation> receiveMessageOperations = Collections.singletonList(buildAssignOperation("name", "lName", String.class.getName(),
                ExpressionType.TYPE_VARIABLE));
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithReceivedTask("receiveMessageProcess", "waitForMessage", "userTask1",
                "delivery", user, "m5", null, Collections.singletonMap("name", String.class.getName()), receiveMessageOperations);

        final ProcessInstance receiveMessageProcessInstance = getProcessAPI().startProcess(receiveMessageProcess.getId());
        waitForFlowNodeInState(receiveMessageProcessInstance, "waitForMessage", TestStates.WAITING, true);

        final ProcessInstance sendMessageProcessInstance = getProcessAPI().startProcess(sendMessageProcess.getId(),
                Arrays.asList(buildAssignOperation("lastName", "Doe", String.class.getName(), ExpressionType.TYPE_CONSTANT)), null);
        waitForProcessToFinish(sendMessageProcessInstance);
        forceMatchingOfEvents();
        final HumanTaskInstance step1 = waitForUserTaskAndGetIt(receiveMessageProcessInstance, "userTask1");

        final DataInstance dataInstance = getProcessAPI().getProcessDataInstance("name", step1.getRootContainerId());
        assertEquals("Doe", dataInstance.getValue());

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);
    }

    /*
     * dynamic -> deployAndEnable(receiveProcess), startProcess(receiveProcess), cancelProcessInstance(receiveProcess)
     * checks : receiveProcess wait on receive task, 1 waiting event, receiveProcess is cancelled, receiveProcess is archived, no more waiting event
     */
    @SuppressWarnings("unchecked")
    @Cover(classes = { EventInstance.class, ReceiveTaskInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message event", "Receive task",
            "Send", "Receive" }, jira = "")
    @Test
    public void cancelInstanceShouldDeleteWaitingEvents() throws Exception {
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithReceivedTask("receiveMessageProcess", "waitForMessage", "userTask1",
                "delivery", user, "m1", null, null, null);

        final ProcessInstance receiveMessageProcessInstance = getProcessAPI().startProcess(receiveMessageProcess.getId());
        waitForFlowNodeInState(receiveMessageProcessInstance, "waitForMessage", TestStates.WAITING, true);

        SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.filter(WaitingEventSearchDescriptor.ROOT_PROCESS_INSTANCE_ID, receiveMessageProcessInstance.getId());

        final Map<String, Serializable> parameters = new HashMap<String, Serializable>(1);
        parameters.put(SEARCH_OPTIONS_KEY, searchOptionsBuilder.done());

        SearchResult<WaitingEvent> searchResult = (SearchResult<WaitingEvent>) getCommandAPI().execute(SEARCH_WAITING_EVENTS_COMMAND, parameters);
        assertEquals(1, searchResult.getCount());

        getProcessAPI().cancelProcessInstance(receiveMessageProcessInstance.getId());
        waitForProcessToBeInState(receiveMessageProcessInstance, ProcessInstanceState.CANCELLED);

        searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.filter(ArchivedActivityInstanceSearchDescriptor.ROOT_PROCESS_INSTANCE_ID, receiveMessageProcessInstance.getId());
        searchOptionsBuilder.filter(ArchivedActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.RECEIVE_TASK);
        final SearchResult<ArchivedActivityInstance> archivedActivityInstancesSearch = getProcessAPI().searchArchivedActivities(searchOptionsBuilder.done());
        assertEquals(1, archivedActivityInstancesSearch.getCount());
        assertTrue(archivedActivityInstancesSearch.getResult().get(0) instanceof ArchivedReceiveTaskInstance);
        assertEquals(TestStates.CANCELLED.getStateName(), archivedActivityInstancesSearch.getResult().get(0).getState());

        searchResult = (SearchResult<WaitingEvent>) getCommandAPI().execute(SEARCH_WAITING_EVENTS_COMMAND, parameters);
        assertEquals(0, searchResult.getCount());

        disableAndDeleteProcess(receiveMessageProcess);
    }

    private ProcessDefinition deployAndEnableProcessWithEndMessageEvent(final String processName, final String messageName, final String targetProcess,
            final String targetFlowNode, final List<BEntry<Expression, Expression>> correlations, final Map<String, String> processData,
            final Map<String, String> messageData, final Map<String, String> dataInputMapping) throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder();
        processBuilder.createNewInstance(processName, "1.0");
        addProcessData(processData, processBuilder);
        processBuilder.addStartEvent("startEvent");
        processBuilder.addAutomaticTask("auto1");
        // create expression for target process/flowNode
        final Expression targetProcessExpression = new ExpressionBuilder().createConstantStringExpression(targetProcess);
        final Expression targetFlowNodeExpression = new ExpressionBuilder().createConstantStringExpression(targetFlowNode);
        final ThrowMessageEventTriggerBuilder throwMessageEventTriggerBuilder = processBuilder.addEndEvent("endEvent").addMessageEventTrigger(messageName,
                targetProcessExpression, targetFlowNodeExpression);
        addCorrelations(correlations, throwMessageEventTriggerBuilder);
        addMessageData(messageData, dataInputMapping, throwMessageEventTriggerBuilder);
        processBuilder.addTransition("startEvent", "auto1");
        processBuilder.addTransition("auto1", "endEvent");
        final DesignProcessDefinition designProcessDefinition = processBuilder.done();

        final ProcessDefinition sendMessageProcess = deployAndEnableProcess(designProcessDefinition);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(sendMessageProcess.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        return sendMessageProcess;
    }

    private ProcessDefinition deployAndEnableProcessWithReceivedTask(final String processName, final String receiveTaskName, final String userTaskName,
            final String actorName, final User user, final String messageName, final List<BEntry<Expression, Expression>> correlations,
            final Map<String, String> processData, final List<Operation> operations) throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder();
        processBuilder.createNewInstance(processName, "1.0");
        addProcessData(processData, processBuilder);
        processBuilder.addStartEvent("startEvent");
        final ReceiveTaskDefinitionBuilder receiveTaskBuilder = processBuilder.addReceiveTask(receiveTaskName, messageName);
        if (correlations != null) {
            for (final Entry<Expression, Expression> entry : correlations) {
                receiveTaskBuilder.addCorrelation(entry.getKey(), entry.getValue());
            }
        }
        if (operations != null) {
            for (final Operation operation : operations) {
                receiveTaskBuilder.addMessageOperation(operation);
            }
        }

        processBuilder.addActor(actorName);
        processBuilder.addUserTask(userTaskName, actorName);
        processBuilder.addEndEvent("endEvent");
        processBuilder.addTransition("startEvent", receiveTaskName);
        processBuilder.addTransition(receiveTaskName, userTaskName);
        processBuilder.addTransition(userTaskName, "endEvent");
        final DesignProcessDefinition designProcessDefinition = processBuilder.done();

        final BusinessArchiveBuilder archiveBuilder = new BusinessArchiveBuilder();
        archiveBuilder.createNewBusinessArchive().setProcessDefinition(designProcessDefinition);
        final BusinessArchive receiveMessaceArchive = archiveBuilder.done();
        final ProcessDefinition receiveMessageProcess = deployProcess(receiveMessaceArchive);

        final List<ActorInstance> actors = getProcessAPI().getActors(receiveMessageProcess.getId(), 0, 1, ActorCriterion.NAME_ASC);
        getProcessAPI().addUserToActor(actors.get(0).getId(), user.getId());

        getProcessAPI().enableProcess(receiveMessageProcess.getId());

        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(receiveMessageProcess.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        return receiveMessageProcess;
    }

    private void addMessageData(final Map<String, String> messageData, final Map<String, String> dataInputMapping,
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

    private void addProcessData(final Map<String, String> data, final ProcessDefinitionBuilder processBuilder) {
        if (data != null) {
            for (final Entry<String, String> entry : data.entrySet()) {
                processBuilder.addData(entry.getKey(), entry.getValue(), null);
            }
        }
    }

    private void addCorrelations(final List<BEntry<Expression, Expression>> correlations, final ThrowMessageEventTriggerBuilder throwMessageEventTriggerBuilder) {
        if (correlations != null) {
            for (final Entry<Expression, Expression> entry : correlations) {
                throwMessageEventTriggerBuilder.addCorrelation(entry.getKey(), entry.getValue());
            }
        }
    }

    private Operation buildAssignOperation(final String dataInstanceName, final String newConstantValue, final String className,
            final ExpressionType expressionType) throws InvalidExpressionException {
        final LeftOperand leftOperand = new LeftOperandBuilder().createNewInstance().setName(dataInstanceName).done();
        final Expression expression = new ExpressionBuilder().createNewInstance(dataInstanceName).setContent(newConstantValue)
                .setExpressionType(expressionType.name()).setReturnType(className).done();
        final Operation operation;
        operation = new OperationBuilder().createNewInstance().setOperator("=").setLeftOperand(leftOperand).setType(OperatorType.ASSIGNMENT)
                .setRightOperand(expression).done();
        return operation;
    }

}
