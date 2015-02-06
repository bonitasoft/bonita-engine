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

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.bpm.bar.xml.XMLProcessDefinition.BEntry;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.bpm.process.SubProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.SubProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ThrowMessageEventTriggerBuilder;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class MessageEventSubProcessIT extends AbstractWaitingEventIT {

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "message" }, jira = "ENGINE-1841", story = "transmit data to start message event of eventsubprocess")
    @Test
    public void messageEventSubProcessTransmitData() throws Exception {
        // create a process with a user step and an event subprocess that start with a start message event having an operation updating the data
        final ProcessDefinitionBuilder receiveProcessBuilder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithEventSubProcess", "1.0");
        receiveProcessBuilder.addActor(ACTOR_NAME);
        receiveProcessBuilder.addShortTextData("aData", new ExpressionBuilder().createConstantStringExpression("defaultValue"));
        receiveProcessBuilder.addUserTask("waitHere", ACTOR_NAME);
        final SubProcessDefinitionBuilder subProcessBuilder = receiveProcessBuilder.addSubProcess("startWithMessage", true).getSubProcessBuilder();
        subProcessBuilder.addUserTask("stepInSubProcess", ACTOR_NAME);
        subProcessBuilder
                .addStartEvent("start")
                .addMessageEventTrigger("msg")
                .addOperation(
                        new OperationBuilder().createSetDataOperation("aData", new ExpressionBuilder().createDataExpression("msgData", String.class.getName())));
        subProcessBuilder.addTransition("start", "stepInSubProcess");
        final ProcessDefinition receiveProcess = deployAndEnableProcessWithActor(receiveProcessBuilder.done(), ACTOR_NAME, user);

        // create an other process that send a message
        final ProcessDefinitionBuilder sendProcessBuilder = new ProcessDefinitionBuilder().createNewInstance("SendMsgProcess", "1.0");
        final ThrowMessageEventTriggerBuilder addMessageEventTrigger = sendProcessBuilder.addIntermediateThrowEvent("send").addMessageEventTrigger("msg",
                new ExpressionBuilder().createConstantStringExpression("ProcessWithEventSubProcess"));
        addMessageEventTrigger.addMessageContentExpression(new ExpressionBuilder().createConstantStringExpression("msgData"),
                new ExpressionBuilder().createGroovyScriptExpression("msgVariable", "\"message variable OK\"", String.class.getName()));
        final ProcessDefinition sendProcess = deployAndEnableProcess(sendProcessBuilder.done());

        getProcessAPI().startProcess(receiveProcess.getId());
        waitForUserTask("waitHere");

        getProcessAPI().startProcess(sendProcess.getId());
        final long stepInSubProcessId = waitForUserTask("stepInSubProcess");

        // data should be transmit from the message
        assertEquals("message variable OK", getProcessAPI().getActivityDataInstance("aData", stepInSubProcessId).getValue());

        disableAndDeleteProcess(sendProcess, receiveProcess);
    }

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "message" }, jira = "ENGINE-536")
    @Test
    public void messageEventSubProcessTriggered() throws Exception {
        final ProcessDefinition process = deployAndEnableProcessWithMessageEventSubProcess();
        final ProcessInstance processInstance = getProcessAPI().startProcess(process.getId());
        final long step1Id = waitForUserTask(processInstance, PARENT_PROCESS_USER_TASK_NAME);
        final List<ActivityInstance> activities = getProcessAPI().getActivities(processInstance.getId(), 0, 10);
        assertEquals(1, activities.size());
        checkNumberOfWaitingEvents(SUB_PROCESS_START_NAME, 1);

        // send message to start event sub process
        getProcessAPI().sendMessage(MESSAGE_NAME, new ExpressionBuilder().createConstantStringExpression(process.getName()),
                new ExpressionBuilder().createConstantStringExpression(SUB_PROCESS_START_NAME), null);

        final FlowNodeInstance eventSubProcessActivity = waitForFlowNodeInExecutingState(processInstance, "eventSubProcess", false);
        final ActivityInstance subStep = waitForUserTaskAndGetIt(processInstance, SUB_PROCESS_USER_TASK_NAME);
        final ProcessInstance subProcInst = getProcessAPI().getProcessInstance(subStep.getParentProcessInstanceId());

        checkNumberOfWaitingEvents("The parent process instance is supposed to be aborted, so no more waiting events are expected.", SUB_PROCESS_START_NAME, 0);

        waitForArchivedActivity(step1Id, TestStates.ABORTED);
        assignAndExecuteStep(subStep, user);
        waitForArchivedActivity(eventSubProcessActivity.getId(), TestStates.NORMAL_FINAL);
        waitForProcessToFinish(subProcInst);
        waitForProcessToBeInState(processInstance, ProcessInstanceState.ABORTED);

        // check that the transition wasn't taken
        checkWasntExecuted(processInstance, "end");

        disableAndDeleteProcess(process.getId());
    }

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "message",
            "intermediateThrowEvent" }, jira = "ENGINE-1406")
    @Test
    public void messageEventSubProcessTriggeredWithIntermediateThrowEvent() throws Exception {
        final String receiverProcessName = "ReceiverEndMessageEvent";
        final String startName = "start";
        final String endName = "end";

        // Create and deploy Sender process
        final Expression targetReceiverProcessExpression = new ExpressionBuilder().createConstantStringExpression(receiverProcessName);
        final ProcessDefinitionBuilder senderBuilder = new ProcessDefinitionBuilder().createNewInstance("SenderEndMessageEvent", "1.0");
        senderBuilder.addActor(ACTOR_NAME);
        senderBuilder.addStartEvent(startName);
        senderBuilder.addIntermediateThrowEvent(THROW_MESSAGE_TASK_NAME).addMessageEventTrigger(MESSAGE_NAME, targetReceiverProcessExpression);
        senderBuilder.addUserTask(PARENT_PROCESS_USER_TASK_NAME, ACTOR_NAME);
        senderBuilder.addEndEvent(endName).addMessageEventTrigger(MESSAGE_NAME + 1, targetReceiverProcessExpression);
        senderBuilder.addTransition(startName, THROW_MESSAGE_TASK_NAME);
        senderBuilder.addTransition(THROW_MESSAGE_TASK_NAME, PARENT_PROCESS_USER_TASK_NAME);
        senderBuilder.addTransition(PARENT_PROCESS_USER_TASK_NAME, endName);
        final ProcessDefinition senderProcessDefinition = deployAndEnableProcessWithActor(senderBuilder.done(), ACTOR_NAME, user);

        // Create and deploy Receiver process with SubProcess
        final ProcessDefinitionBuilder receiverBuilder = new ProcessDefinitionBuilder().createNewInstance(receiverProcessName, "1.0");
        receiverBuilder.addActor(ACTOR_NAME);
        receiverBuilder.addStartEvent(startName).addMessageEventTrigger(MESSAGE_NAME);
        receiverBuilder.addUserTask(receiverProcessName + PARENT_PROCESS_USER_TASK_NAME, ACTOR_NAME);
        receiverBuilder.addTransition(startName, receiverProcessName + PARENT_PROCESS_USER_TASK_NAME);

        final SubProcessDefinitionBuilder subProcessBuilder = receiverBuilder.addSubProcess("EventSubProcess", true).getSubProcessBuilder();
        subProcessBuilder.addStartEvent(SUB_PROCESS_START_NAME).addMessageEventTrigger(MESSAGE_NAME + 1);
        subProcessBuilder.addUserTask(SUB_PROCESS_USER_TASK_NAME, ACTOR_NAME);
        subProcessBuilder.addTransition(SUB_PROCESS_START_NAME, SUB_PROCESS_USER_TASK_NAME);
        final ProcessDefinition receiverProcessDefinition = deployAndEnableProcessWithActor(receiverBuilder.done(), ACTOR_NAME, user);

        // Start and execute the Sender process
        final ProcessInstance processInstance = getProcessAPI().startProcess(senderProcessDefinition.getId());
        final long step1Id = waitForUserTask(processInstance, PARENT_PROCESS_USER_TASK_NAME);
        waitForPendingTasks(user.getId(), 2);
        checkNumberOfWaitingEventsInProcess(receiverProcessName, 2);
        assignAndExecuteStep(step1Id, user);
        waitForProcessToFinish(processInstance);
        checkNumberOfWaitingEvents("The parent process instance is supposed to finished, so no more waiting events are expected.", startName, 1);

        // Execute the Receiver process
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.filter(ProcessInstanceSearchDescriptor.NAME, receiverProcessName);
        final ProcessInstance receiverProcessInstance = getProcessAPI().searchOpenProcessInstances(searchOptionsBuilder.done()).getResult().get(0);
        waitForUserTask(receiverProcessInstance.getId(), SUB_PROCESS_USER_TASK_NAME);
        assertEquals(1, getProcessAPI().getNumberOfPendingHumanTaskInstances(user.getId()));

        // Clean-up
        disableAndDeleteProcess(senderProcessDefinition.getId());
        disableAndDeleteProcess(receiverProcessDefinition.getId());
    }

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "message" }, jira = "ENGINE-536")
    @Test
    public void messageEventSubProcessNotTriggered() throws Exception {
        final ProcessDefinition process = deployAndEnableProcessWithMessageEventSubProcess();
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

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "message" }, jira = "ENGINE-536")
    @Test
    public void messageEventSubProcessTriggeredWithCorrelation() throws Exception {
        final Expression correlationKey = new ExpressionBuilder().createConstantStringExpression("productName");
        final Expression catchCorrelationValue = new ExpressionBuilder().createDataExpression(SHORT_DATA_NAME, String.class.getName());

        final ProcessDefinition process = deployAndEnableProcessWithMessageEventSubProcessAndData(Collections.singletonList(new BEntry<Expression, Expression>(
                correlationKey, catchCorrelationValue)));
        final ProcessInstance processInstance = getProcessAPI().startProcess(process.getId());
        waitForUserTask(processInstance, PARENT_PROCESS_USER_TASK_NAME);

        // send message to start event sub process
        final Expression throwCorrelationValue = new ExpressionBuilder().createConstantStringExpression("parentVar");// the default data value
        getProcessAPI().sendMessage(MESSAGE_NAME, new ExpressionBuilder().createConstantStringExpression(process.getName()),
                new ExpressionBuilder().createConstantStringExpression(SUB_PROCESS_START_NAME), null,
                Collections.singletonMap(correlationKey, throwCorrelationValue));

        waitForUserTask(processInstance.getId(), SUB_PROCESS_USER_TASK_NAME);

        disableAndDeleteProcess(process.getId());
    }

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "message",
            "intermediate throw event" }, jira = "BS-11096")
    @Test
    public void messageEventSubProcessTriggeredWithCorrelationAndIntermediateThrowEvent() throws Exception {
        // Correlation
        final Expression correlationKey = new ExpressionBuilder().createConstantStringExpression("productName");
        final Expression correlationValue = new ExpressionBuilder().createDataExpression(SHORT_DATA_NAME, String.class.getName());

        // Sender
        final Expression targetReceiverProcessExpression = new ExpressionBuilder().createConstantStringExpression("ProcessWithEventSubProcess");
        final ProcessDefinitionBuilder senderBuilder = new ProcessDefinitionBuilder().createNewInstance("SenderEndMessageEvent", "1.0");
        senderBuilder.addActor(ACTOR_NAME).addShortTextData(SHORT_DATA_NAME, new ExpressionBuilder().createConstantStringExpression("parentVar"));
        senderBuilder.addIntermediateThrowEvent(THROW_MESSAGE_TASK_NAME).addMessageEventTrigger(MESSAGE_NAME, targetReceiverProcessExpression)
                .addCorrelation(correlationKey, correlationValue);
        final ProcessDefinition senderProcessDefinition = deployAndEnableProcessWithActor(senderBuilder.done(), ACTOR_NAME, user);

        // Receiver
        final ProcessDefinition receiverProcessDefinition = deployAndEnableProcessWithMessageEventSubProcessAndData(Collections
                .singletonList(new BEntry<Expression, Expression>(correlationKey, correlationValue)));
        final ProcessInstance receiverProcessInstance = getProcessAPI().startProcess(receiverProcessDefinition.getId());
        waitForUserTask(receiverProcessInstance, PARENT_PROCESS_USER_TASK_NAME);

        // send message to start event sub process
        getProcessAPI().startProcess(senderProcessDefinition.getId());
        waitForUserTask(receiverProcessInstance.getId(), SUB_PROCESS_USER_TASK_NAME);

        disableAndDeleteProcess(senderProcessDefinition, receiverProcessDefinition);
    }

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "message" }, jira = "ENGINE-536")
    @Test
    public void createSeveralInstances() throws Exception {
        final ProcessDefinition process = deployAndEnableProcessWithMessageEventSubProcess();
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(process.getId());
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(process.getId());

        // start the first event sub-process
        getProcessAPI().sendMessage(MESSAGE_NAME, new ExpressionBuilder().createConstantStringExpression(process.getName()),
                new ExpressionBuilder().createConstantStringExpression(SUB_PROCESS_START_NAME), null);
        // start the second event sub-process
        getProcessAPI().sendMessage(MESSAGE_NAME, new ExpressionBuilder().createConstantStringExpression(process.getName()),
                new ExpressionBuilder().createConstantStringExpression(SUB_PROCESS_START_NAME), null);

        waitForUserTask(processInstance1, SUB_PROCESS_USER_TASK_NAME);
        waitForUserTask(processInstance2, SUB_PROCESS_USER_TASK_NAME);

        disableAndDeleteProcess(process.getId());
    }

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "message", "parent process data" }, jira = "ENGINE-536")
    @Test
    public void subProcessCanAccessParentData() throws Exception {
        final ProcessDefinition process = deployAndEnableProcessWithMessageEventSubProcessAndData(null);
        final ProcessInstance processInstance = getProcessAPI().startProcess(process.getId());
        waitForUserTask(processInstance.getId(), PARENT_PROCESS_USER_TASK_NAME);

        getProcessAPI().sendMessage(MESSAGE_NAME, new ExpressionBuilder().createConstantStringExpression(process.getName()),
                new ExpressionBuilder().createConstantStringExpression(SUB_PROCESS_START_NAME), null);

        final ActivityInstance subStep = waitForUserTaskAndGetIt(processInstance.getId(), SUB_PROCESS_USER_TASK_NAME);
        final ProcessInstance subProcInst = getProcessAPI().getProcessInstance(subStep.getParentProcessInstanceId());
        checkProcessDataInstance(INT_DATA_NAME, subProcInst.getId(), 1);
        checkProcessDataInstance(SHORT_DATA_NAME, subProcInst.getId(), "childVar");
        checkProcessDataInstance("value", subProcInst.getId(), 10.0);
        checkProcessDataInstance(SHORT_DATA_NAME, processInstance.getId(), "parentVar");
        checkActivityDataInstance(SHORT_DATA_NAME, subStep.getId(), "childActivityVar");

        assignAndExecuteStep(subStep, user);
        waitForProcessToFinish(subProcInst);
        waitForProcessToBeInState(processInstance, ProcessInstanceState.ABORTED);

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

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "message", "call activity" }, jira = "ENGINE-536")
    @Test
    public void messageEventSubProcInsideTargetCallActivity() throws Exception {
        final ProcessDefinition targetProcess = deployAndEnableProcessWithMessageEventSubProcess();
        final ProcessDefinition callerProcess = deployAndEnableProcessWithCallActivity(targetProcess.getName(), targetProcess.getVersion());
        final ProcessInstance processInstance = getProcessAPI().startProcess(callerProcess.getId());
        try {
            final ActivityInstance step1 = waitForUserTaskAndGetIt(processInstance, PARENT_PROCESS_USER_TASK_NAME);

            getProcessAPI().sendMessage(MESSAGE_NAME, new ExpressionBuilder().createConstantStringExpression(targetProcess.getName()),
                    new ExpressionBuilder().createConstantStringExpression(SUB_PROCESS_START_NAME), null);

            final ActivityInstance subStep = waitForUserTaskAndGetIt(processInstance, SUB_PROCESS_USER_TASK_NAME);
            final ProcessInstance calledProcInst = getProcessAPI().getProcessInstance(step1.getParentProcessInstanceId());
            final ProcessInstance subProcInst = getProcessAPI().getProcessInstance(subStep.getParentProcessInstanceId());

            waitForArchivedActivity(step1.getId(), TestStates.ABORTED);
            assignAndExecuteStep(subStep, user.getId());
            waitForProcessToFinish(subProcInst);
            waitForProcessToBeInState(calledProcInst, ProcessInstanceState.ABORTED);

            waitForUserTaskAndExecuteIt(processInstance, "step2", user);
            waitForProcessToFinish(processInstance);
        } finally {
            disableAndDeleteProcess(callerProcess);
            disableAndDeleteProcess(targetProcess);
        }
    }
}
