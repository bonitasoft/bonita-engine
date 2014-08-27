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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.bpm.bar.xml.XMLProcessDefinition.BEntry;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.process.SubProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.CatchMessageEventTriggerDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.SubProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ThrowMessageEventTriggerBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
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
public class MessageEventSubProcessTest extends EventsAPITest {

    private static final String INT_DATA_NAME = "count";

    private static final String SHORT_DATA_NAME = "content";

    private static final String SUB_PROCESS_START_NAME = "messageStart";

    private static final String SUB_PROCESS_USER_TASK_NAME = "subStep";

    private static final String PARENT_PROCESS_USER_TASK_NAME = "step1";

    private static final String THROW_MESSAGE_TASK_NAME = "messageTask";

    private static final String MESSAGE_NAME = "canStart";

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

    private ProcessDefinition deployAndEnableProcessWithCallActivity(final String targetProcessName, final String targetVersion) throws BonitaException {
        final Expression targetProcessExpr = new ExpressionBuilder().createConstantStringExpression(targetProcessName);
        final Expression targetVersionExpr = new ExpressionBuilder().createConstantStringExpression(targetVersion);
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithCallActivity", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addStartEvent("start");
        builder.addCallActivity("callActivity", targetProcessExpr, targetVersionExpr);
        builder.addUserTask("step2", ACTOR_NAME);
        builder.addEndEvent("end");
        builder.addTransition("start", "callActivity");
        builder.addTransition("callActivity", "step2");
        builder.addTransition("step2", "end");
        final DesignProcessDefinition processDefinition = builder.done();
        return deployAndEnableProcessWithActor(processDefinition, ACTOR_NAME, john);
    }

    private ProcessDefinition deployAndEnableProcessWithMessageEventSubProcess() throws BonitaException {
        final ProcessDefinitionBuilder builder = buildParentProcess(false);
        buildSubProcess(builder, false, null);
        final DesignProcessDefinition processDefinition = builder.done();
        return deployAndEnableProcessWithActor(processDefinition, ACTOR_NAME, john);
    }

    private ProcessDefinition deployAndEnableProcessWithMessageEventSubProcessAndData() throws BonitaException {
        return deployAndEnableProcessWithMessageEventSubProcessAndData(null);
    }

    private ProcessDefinition deployAndEnableProcessWithMessageEventSubProcessAndData(final List<BEntry<Expression, Expression>> correlations)
            throws BonitaException {
        final ProcessDefinitionBuilder builder = buildParentProcess(true);
        buildSubProcess(builder, true, correlations);
        final DesignProcessDefinition processDefinition = builder.done();
        return deployAndEnableProcessWithActor(processDefinition, ACTOR_NAME, john);
    }

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
        final ProcessDefinition receiveProcess = deployAndEnableProcessWithActor(receiveProcessBuilder.done(), ACTOR_NAME, john);

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
        final ActivityInstance stepInSubProcess = waitForUserTask("stepInSubProcess");

        // data should be transmit from the message
        assertEquals("message variable OK", getProcessAPI().getActivityDataInstance("aData", stepInSubProcess.getId()).getValue());

        disableAndDeleteProcess(sendProcess, receiveProcess);

    }

    private ProcessDefinitionBuilder buildParentProcess(final boolean withDatas) throws InvalidExpressionException {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithEventSubProcess", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addStartEvent("start");
        builder.addUserTask(PARENT_PROCESS_USER_TASK_NAME, ACTOR_NAME);
        builder.addEndEvent("end");
        builder.addTransition("start", PARENT_PROCESS_USER_TASK_NAME);
        builder.addTransition(PARENT_PROCESS_USER_TASK_NAME, "end");

        if (withDatas) {
            builder.addShortTextData(SHORT_DATA_NAME, new ExpressionBuilder().createConstantStringExpression("parentVar"));
            builder.addIntegerData(INT_DATA_NAME, new ExpressionBuilder().createConstantIntegerExpression(1));
        }
        return builder;
    }

    private void buildSubProcess(final ProcessDefinitionBuilder builder, final boolean withDatas, final List<BEntry<Expression, Expression>> correlations)
            throws InvalidExpressionException {
        final SubProcessDefinitionBuilder subProcessBuilder = builder.addSubProcess("eventSubProcess", true).getSubProcessBuilder();
        final CatchMessageEventTriggerDefinitionBuilder messageEventTrigger = subProcessBuilder.addStartEvent(SUB_PROCESS_START_NAME).addMessageEventTrigger(
                MESSAGE_NAME);
        final UserTaskDefinitionBuilder userTask = subProcessBuilder.addUserTask(SUB_PROCESS_USER_TASK_NAME, ACTOR_NAME);
        subProcessBuilder.addEndEvent("endSubProcess");
        subProcessBuilder.addTransition(SUB_PROCESS_START_NAME, SUB_PROCESS_USER_TASK_NAME);
        subProcessBuilder.addTransition(SUB_PROCESS_USER_TASK_NAME, "endSubProcess");

        if (withDatas) {
            subProcessBuilder.addShortTextData(SHORT_DATA_NAME, new ExpressionBuilder().createConstantStringExpression("childVar"));
            subProcessBuilder.addDoubleData("value", new ExpressionBuilder().createConstantDoubleExpression(10.0));
            addCorrelations(messageEventTrigger, correlations);
            userTask.addShortTextData(SHORT_DATA_NAME, new ExpressionBuilder().createConstantStringExpression("childActivityVar"));
        }
    }

    private void addCorrelations(final CatchMessageEventTriggerDefinitionBuilder messageEventTrigger, final List<BEntry<Expression, Expression>> correlations) {
        if (correlations != null) {
            for (final BEntry<Expression, Expression> correlation : correlations) {
                messageEventTrigger.addCorrelation(correlation.getKey(), correlation.getValue());
            }
        }
    }

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "message" }, jira = "ENGINE-536")
    @Test
    public void messageEventSubProcessTriggered() throws Exception {
        final ProcessDefinition process = deployAndEnableProcessWithMessageEventSubProcess();
        final ProcessInstance processInstance = getProcessAPI().startProcess(process.getId());
        final ActivityInstance step1 = waitForUserTask(PARENT_PROCESS_USER_TASK_NAME, processInstance);
        List<ActivityInstance> activities = getProcessAPI().getActivities(processInstance.getId(), 0, 10);
        assertEquals(1, activities.size());
        checkNumberOfWaitingEvents(SUB_PROCESS_START_NAME, 1);

        // send message to start event sub process
        getProcessAPI().sendMessage(MESSAGE_NAME, new ExpressionBuilder().createConstantStringExpression(process.getName()),
                new ExpressionBuilder().createConstantStringExpression(SUB_PROCESS_START_NAME), null);

        final FlowNodeInstance eventSubProcessActivity = waitForFlowNodeInExecutingState(processInstance, "eventSubProcess", false);
        final ActivityInstance subStep = waitForUserTask(SUB_PROCESS_USER_TASK_NAME, processInstance);
        final ProcessInstance subProcInst = getProcessAPI().getProcessInstance(subStep.getParentProcessInstanceId());

        // the parent process instance is supposed to be aborted, so no more waiting events are expected
        checkNumberOfWaitingEvents(SUB_PROCESS_START_NAME, 0);

        waitForArchivedActivity(step1.getId(), TestStates.getAbortedState());
        assignAndExecuteStep(subStep, john.getId());
        waitForArchivedActivity(eventSubProcessActivity.getId(), TestStates.getNormalFinalState());
        waitForProcessToFinish(subProcInst);
        waitForProcessToFinish(processInstance, TestStates.getAbortedState());

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
        final ProcessDefinition senderProcessDefinition = deployAndEnableProcessWithActor(senderBuilder.done(), ACTOR_NAME, john);

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
        final ProcessDefinition receiverProcessDefinition = deployAndEnableProcessWithActor(receiverBuilder.done(), ACTOR_NAME, john);

        // Start and execute the Sender process
        final ProcessInstance processInstance = getProcessAPI().startProcess(senderProcessDefinition.getId());
        final ActivityInstance step1 = waitForUserTask(PARENT_PROCESS_USER_TASK_NAME, processInstance);
        waitForPendingTasks(john.getId(), 2);
        checkNumberOfWaitingEventsInProcess(receiverProcessName, 2);
        assignAndExecuteStep(step1.getId(), john.getId());
        waitForProcessToFinish(processInstance);
        // the parent process instance is supposed to finished, so no more waiting events are expected
        checkNumberOfWaitingEvents(startName, 1);

        // Execute the Receiver process
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.filter(ProcessInstanceSearchDescriptor.NAME, receiverProcessName);
        final ProcessInstance receiverProcessInstance = getProcessAPI().searchOpenProcessInstances(searchOptionsBuilder.done()).getResult().get(0);
        waitForUserTask(SUB_PROCESS_USER_TASK_NAME, receiverProcessInstance.getId());
        assertEquals(1, getProcessAPI().getNumberOfPendingHumanTaskInstances(john.getId()));

        // Clean-up
        disableAndDeleteProcess(senderProcessDefinition.getId());
        disableAndDeleteProcess(receiverProcessDefinition.getId());
    }

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "message" }, jira = "ENGINE-536")
    @Test
    public void messageEventSubProcessNotTriggered() throws Exception {
        final ProcessDefinition process = deployAndEnableProcessWithMessageEventSubProcess();
        final ProcessInstance processInstance = getProcessAPI().startProcess(process.getId());
        final ActivityInstance step1 = waitForUserTask(PARENT_PROCESS_USER_TASK_NAME, processInstance);
        final List<ActivityInstance> activities = getProcessAPI().getActivities(processInstance.getId(), 0, 10);
        assertEquals(1, activities.size());
        checkNumberOfWaitingEvents(SUB_PROCESS_START_NAME, 1);

        assignAndExecuteStep(step1, john.getId());

        waitForArchivedActivity(step1.getId(), TestStates.getNormalFinalState());
        waitForProcessToFinish(processInstance);

        // the parent process instance has completed, so no more waiting events are expected
        checkNumberOfWaitingEvents(SUB_PROCESS_START_NAME, 0);

        disableAndDeleteProcess(process);
    }

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "message" }, jira = "ENGINE-536")
    @Test
    public void messageEventSubProcessWithCorrelation() throws Exception {
        final Expression correlationKey = new ExpressionBuilder().createConstantStringExpression("productName");
        final Expression catchCorrelationValue = new ExpressionBuilder().createDataExpression(SHORT_DATA_NAME, String.class.getName());

        final ProcessDefinition process = deployAndEnableProcessWithMessageEventSubProcessAndData(Collections.singletonList(new BEntry<Expression, Expression>(
                correlationKey, catchCorrelationValue)));
        final ProcessInstance processInstance = getProcessAPI().startProcess(process.getId());
        waitForUserTask(PARENT_PROCESS_USER_TASK_NAME, processInstance);

        // send message to start event sub process
        final Expression throwCorrelationValue = new ExpressionBuilder().createConstantStringExpression("parentVar");// the default data value
        getProcessAPI().sendMessage(MESSAGE_NAME, new ExpressionBuilder().createConstantStringExpression(process.getName()),
                new ExpressionBuilder().createConstantStringExpression(SUB_PROCESS_START_NAME), null,
                Collections.singletonMap(correlationKey, throwCorrelationValue));

        waitForUserTask(SUB_PROCESS_USER_TASK_NAME, processInstance.getId());

        disableAndDeleteProcess(process.getId());
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

        waitForUserTask(SUB_PROCESS_USER_TASK_NAME, processInstance1);
        waitForUserTask(SUB_PROCESS_USER_TASK_NAME, processInstance2);

        disableAndDeleteProcess(process.getId());
    }

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "message", "parent process data" }, jira = "ENGINE-536")
    @Test
    public void subProcessCanAccessParentData() throws Exception {
        final ProcessDefinition process = deployAndEnableProcessWithMessageEventSubProcessAndData();
        final ProcessInstance processInstance = getProcessAPI().startProcess(process.getId());
        waitForUserTask(PARENT_PROCESS_USER_TASK_NAME, processInstance.getId());

        getProcessAPI().sendMessage(MESSAGE_NAME, new ExpressionBuilder().createConstantStringExpression(process.getName()),
                new ExpressionBuilder().createConstantStringExpression(SUB_PROCESS_START_NAME), null);

        final ActivityInstance subStep = waitForUserTask(SUB_PROCESS_USER_TASK_NAME, processInstance.getId());
        final ProcessInstance subProcInst = getProcessAPI().getProcessInstance(subStep.getParentProcessInstanceId());
        checkProcessDataInstance(INT_DATA_NAME, subProcInst.getId(), 1);
        checkProcessDataInstance(SHORT_DATA_NAME, subProcInst.getId(), "childVar");
        checkProcessDataInstance("value", subProcInst.getId(), 10.0);
        checkProcessDataInstance(SHORT_DATA_NAME, processInstance.getId(), "parentVar");
        checkActivityDataInstance(SHORT_DATA_NAME, subStep.getId(), "childActivityVar");

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

    @Cover(classes = { SubProcessDefinition.class }, concept = BPMNConcept.EVENT_SUBPROCESS, keywords = { "event sub-process", "message", "call activity" }, jira = "ENGINE-536")
    @Test
    public void messageEventSubProcInsideTargetCallActivity() throws Exception {
        final ProcessDefinition targetProcess = deployAndEnableProcessWithMessageEventSubProcess();
        final ProcessDefinition callerProcess = deployAndEnableProcessWithCallActivity(targetProcess.getName(), targetProcess.getVersion());
        final ProcessInstance processInstance = getProcessAPI().startProcess(callerProcess.getId());
        final ActivityInstance step1 = waitForUserTask(PARENT_PROCESS_USER_TASK_NAME, processInstance);

        getProcessAPI().sendMessage(MESSAGE_NAME, new ExpressionBuilder().createConstantStringExpression(targetProcess.getName()),
                new ExpressionBuilder().createConstantStringExpression(SUB_PROCESS_START_NAME), null);

        final ActivityInstance subStep = waitForUserTask(SUB_PROCESS_USER_TASK_NAME, processInstance);
        final ProcessInstance calledProcInst = getProcessAPI().getProcessInstance(step1.getParentProcessInstanceId());
        final ProcessInstance subProcInst = getProcessAPI().getProcessInstance(subStep.getParentProcessInstanceId());

        waitForArchivedActivity(step1.getId(), TestStates.getAbortedState());
        assignAndExecuteStep(subStep, john.getId());
        waitForProcessToFinish(subProcInst);
        waitForProcessToFinish(calledProcInst, TestStates.getAbortedState());

        waitForUserTaskAndExecuteIt("step2", processInstance, john);
        waitForProcessToFinish(processInstance);

        disableAndDeleteProcess(callerProcess);
        disableAndDeleteProcess(targetProcess);
    }
}
