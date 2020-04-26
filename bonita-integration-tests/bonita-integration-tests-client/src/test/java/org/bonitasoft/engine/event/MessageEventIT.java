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

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.bonitasoft.engine.bar.BEntry;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.ActivityStates;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.SendEventException;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.CatchMessageEventTriggerDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ThrowMessageEventTriggerBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionConstants;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.operation.LeftOperand;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.test.StartProcessUntilStep;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.wait.WaitForEvent;
import org.bonitasoft.engine.test.wait.WaitForStep;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class MessageEventIT extends AbstractEventIT {

    /*
     * 1 receiveProcess, 1 sendProcess, Message goes from EndEvent to StartEvent
     * dynamic -> deployAndEnable(sendProcess), deployAndEnable(receiveProcess), startProcess(sendProcess)
     * check receiveProcess has started and halt on the user task.
     */
    @Test
    public void messageStartEventMessageSentAfterEnable() throws Exception {
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithEndMessageEvent(
                START_WITH_MESSAGE_PROCESS_NAME, "startEvent");
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithStartMessageEvent(
                Collections.<String, String> emptyMap(),
                Collections.<Operation> emptyList());

        final ProcessInstance sendMessageProcessInstance = getProcessAPI().startProcess(sendMessageProcess.getId());
        waitForProcessToFinish(sendMessageProcessInstance);
        waitForUserTask(START_WITH_MESSAGE_STEP1_NAME);

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);
    }

    /*
     * 1 receiveProcess, 1 sendProcess, Message goes from EndEvent to StartEvent
     * dynamic -> deployAndEnable(sendProcess), deployAndEnable(receiveProcess), startProcess(sendProcess)
     * check receiveProcess has started and halt on the user task.
     */
    @Test
    public void messageStartEventMessageSentAfterEnableWithNoTargetFlowNode() throws Exception {
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithEndMessageEvent(
                START_WITH_MESSAGE_PROCESS_NAME, null);
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithStartMessageEvent(
                Collections.<String, String> emptyMap(),
                Collections.<Operation> emptyList());

        final ProcessInstance sendMessageProcessInstance = getProcessAPI().startProcess(sendMessageProcess.getId());
        waitForProcessToFinish(sendMessageProcessInstance);
        waitForUserTask(START_WITH_MESSAGE_STEP1_NAME);

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);
    }

    /*
     * Differs only by the dynamic with messageStartEventMessageSentAfterEnable (Before instead of After)
     * dynamic -> deployAndEnable(sendProcess), startProcess(sendProcess), deployAndEnable(receiveProcess)
     */
    @Test
    public void messageStartEventMessageSentBeforeEnable() throws Exception {
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithEndMessageEvent(
                START_WITH_MESSAGE_PROCESS_NAME, "startEvent");
        // the message will be send before the target process is deployed
        final ProcessInstance sendMessageProcessInstance = getProcessAPI().startProcess(sendMessageProcess.getId());
        waitForProcessToFinish(sendMessageProcessInstance);

        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithStartMessageEvent(
                Collections.<String, String> emptyMap(),
                Collections.<Operation> emptyList());
        waitForUserTask(START_WITH_MESSAGE_STEP1_NAME);

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);
    }

    /*
     * 1 receiveProcess, 1 sendProcess, Message goes from EndEvent to IntermediateEvent
     * dynamic -> deployAndEnable(sendProcess), deployAndEnable(receiveProcess), startProcess(receiveProcess),
     * startProcess(sendProcess)
     * checks : receiveProcess stop on catchEvent, sendProcess is finished, receiveProcess continue and reaches user
     * task.
     */
    @Test
    public void messageIntermediateCatchEventMessageSentAfterCatch() throws Exception {
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithEndMessageEvent(
                CATCH_MESSAGE_PROCESS_NAME, CATCH_EVENT_NAME);
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithIntermediateCatchMessageEvent(null,
                null, null);

        final ProcessInstance receiveMessageProcessInstance = getProcessAPI()
                .startProcess(receiveMessageProcess.getId());
        waitForEventInWaitingState(receiveMessageProcessInstance, CATCH_EVENT_NAME);

        final ProcessInstance sendMessageProcessInstance = getProcessAPI().startProcess(sendMessageProcess.getId());
        waitForProcessToFinish(sendMessageProcessInstance);
        waitForUserTask(CATCH_MESSAGE_STEP1_NAME);

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);
    }

    @Test
    public void messageIntermediateCatchEventMessageSentBeforeCatch() throws Exception {
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithEndMessageEvent(
                CATCH_MESSAGE_PROCESS_NAME, CATCH_EVENT_NAME);
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithIntermediateCatchMessageEvent(null,
                null, null);
        final ProcessInstance sendMessageProcessInstance = getProcessAPI().startProcess(sendMessageProcess.getId());
        waitForProcessToFinish(sendMessageProcessInstance);

        final ProcessInstance receiveMessageProcessInstance = getProcessAPI()
                .startProcess(receiveMessageProcess.getId());
        Thread.sleep(100);// small sleep but don't wait for the event to be waiting, it might happen that that event is already matched at this point
        waitForUserTask(receiveMessageProcessInstance, CATCH_MESSAGE_STEP1_NAME);

        disableAndDeleteProcess(receiveMessageProcess);
        disableAndDeleteProcess(sendMessageProcess);
    }

    /*
     * Verify that correlation to determine the targeted process work well (Both receiveProcess are instance of the same
     * processDefinition)
     * 2 receiveProcess, 2 sendProcess, sendProcess1[1, Doe] -> receiveProcess1[1], sendProcess2[2,Doe Doe] ->
     * receiveProcess2[2]
     * checks : receiveProcesses stop on catchEvent, sendProcess1 is finished.
     */
    @Test
    public void messageIntermediateCatchEventWithCorrelations() throws Exception {
        final Map<String, String> data = new HashMap<>();
        data.put("docNumber", Integer.class.getName());
        data.put("lastName", String.class.getName());

        final ArrayList<BEntry<Expression, Expression>> correlations = new ArrayList<>(1);
        final Expression docCorrelationKey = new ExpressionBuilder().createConstantStringExpression("docKey");
        final Expression docCorrelationValue = new ExpressionBuilder().createDataExpression("docNumber",
                Integer.class.getName());
        correlations.add(new BEntry<>(docCorrelationKey, docCorrelationValue));

        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithEndMessageEvent(
                CATCH_MESSAGE_PROCESS_NAME, CATCH_EVENT_NAME, correlations,
                data, null, null);
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithIntermediateCatchMessageEventAnd1Correlation();

        // start two instances of a receive message process
        final ProcessInstance receiveMessageProcessInstance1 = getProcessAPI().startProcess(
                receiveMessageProcess.getId(),
                Arrays.asList(
                        buildAssignOperation("docRef", "1", Integer.class.getName(), ExpressionType.TYPE_CONSTANT)),
                null);
        final ProcessInstance receiveMessageProcessInstance2 = getProcessAPI().startProcess(
                receiveMessageProcess.getId(),
                Arrays.asList(
                        buildAssignOperation("docRef", "2", Integer.class.getName(), ExpressionType.TYPE_CONSTANT)),
                null);

        // wait the event node instance
        waitForEvent(receiveMessageProcessInstance1, CATCH_EVENT_NAME, TestStates.WAITING);
        waitForEvent(receiveMessageProcessInstance2, CATCH_EVENT_NAME, TestStates.WAITING);

        // instantiate a process containing correlations matching with receiveMessageProcessInstance1
        final ProcessInstance sendMessageProcessInstance1 = getProcessAPI().startProcess(
                sendMessageProcess.getId(),
                Arrays.asList(
                        buildAssignOperation("docNumber", "1", Integer.class.getName(), ExpressionType.TYPE_CONSTANT),
                        buildAssignOperation("lastName", "Doe", String.class.getName(), ExpressionType.TYPE_CONSTANT)),
                null);
        waitForProcessToFinish(sendMessageProcessInstance1);
        assertNotNull(waitForUserTask(receiveMessageProcessInstance1.getId(), CATCH_MESSAGE_STEP1_NAME));
        waitForEventInWaitingState(receiveMessageProcessInstance2, CATCH_EVENT_NAME);

        // instantiate a process containing correlations matching with receiveMessageProcessInstance2
        final ProcessInstance sendMessageProcessInstance2 = getProcessAPI().startProcess(
                sendMessageProcess.getId(),
                Arrays.asList(
                        buildAssignOperation("docNumber", "2", Integer.class.getName(), ExpressionType.TYPE_CONSTANT),
                        buildAssignOperation("lastName", "Doe Doe", String.class.getName(),
                                ExpressionType.TYPE_CONSTANT)),
                null);
        waitForProcessToFinish(sendMessageProcessInstance2);

        assertNotNull(waitForUserTask(receiveMessageProcessInstance2.getId(), CATCH_MESSAGE_STEP1_NAME));

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);

    }

    /*
     * Verify that if a sendProcess has for targets 2 instances of the same ProcessDefinition and no correlation key is
     * defined
     * (equivalent to matching keys), exactly one of the receiveProcess catches the message.
     * 2 receiveProcess, 1 sendProcess, sendProcess1 -> receiveProcess1, sendProcess2 -> receiveProcess2[2]
     * checks : receiveProcesses stop on catchEvent, sendProcess1 is finished.
     */
    @Test
    public void messageIntermediateCatchEventWithoutCorrelations() throws Exception {
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithEndMessageEvent(
                CATCH_MESSAGE_PROCESS_NAME, CATCH_EVENT_NAME);
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithIntermediateCatchMessageEvent(null,
                null, null);

        // start two instances of a receive message process
        final ProcessInstance receiveMessageProcessInstance1 = getProcessAPI()
                .startProcess(receiveMessageProcess.getId());
        final ProcessInstance receiveMessageProcessInstance2 = getProcessAPI()
                .startProcess(receiveMessageProcess.getId());

        // wait the event node instance
        waitForEvent(receiveMessageProcessInstance1, CATCH_EVENT_NAME, TestStates.WAITING);
        waitForEvent(receiveMessageProcessInstance2, CATCH_EVENT_NAME, TestStates.WAITING);

        // instantiate a process containing whom the targetProcess is of the ProcessDefinition receiveMessageProcess
        final ProcessInstance sendMessageProcessInstance1 = getProcessAPI().startProcess(sendMessageProcess.getId());
        waitForProcessToFinish(sendMessageProcessInstance1);

        final HumanTaskInstance waitForUserTask = waitForUserTaskAndGetIt(CATCH_MESSAGE_STEP1_NAME);
        final long processInstance = waitForUserTask.getRootContainerId();
        assertTrue(processInstance == receiveMessageProcessInstance1.getId()
                || processInstance == receiveMessageProcessInstance2.getId());
        assertEquals(1, getProcessAPI()
                .getPendingHumanTaskInstances(user.getId(), 0, 10, ActivityInstanceCriterion.DEFAULT).size());

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);
    }

    /*
     * Verify that even if matching correlations keys are not in the same order in the receiveProcess and sendProcess,
     * the message is transmitted.
     * 1 receiveProcess [value1,value2], 1 sendProcesss[value1,value2], message goes from endEvent to intermediateEvent
     * checks : receiveProcess stop on catchEvent , sendProcess is finished, receiveProcess continues and reaches user
     * task.
     */
    @Test
    public void correlationKeyInWrongOrderShouldWork() throws Exception {
        final ArrayList<BEntry<Expression, Expression>> correlations = new ArrayList<>(2);
        correlations.add(
                new BEntry<>(new ExpressionBuilder().createConstantStringExpression("aKey"), new ExpressionBuilder()
                        .createConstantStringExpression("value1")));
        correlations.add(
                new BEntry<>(new ExpressionBuilder().createConstantStringExpression("bKey"), new ExpressionBuilder()
                        .createConstantStringExpression("value2")));

        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithEndMessageEvent(
                CATCH_MESSAGE_PROCESS_NAME, CATCH_EVENT_NAME,
                correlations, null, null, null);

        final ArrayList<BEntry<Expression, Expression>> correlationsReceive = new ArrayList<>(2);
        correlationsReceive.add(
                new BEntry<>(new ExpressionBuilder().createConstantStringExpression("bKey"), new ExpressionBuilder()
                        .createConstantStringExpression("value2")));
        correlationsReceive.add(
                new BEntry<>(new ExpressionBuilder().createConstantStringExpression("aKey"), new ExpressionBuilder()
                        .createConstantStringExpression("value1")));

        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithIntermediateCatchMessageEvent(
                correlationsReceive, null, null);

        // start two instances of a receive message process
        final ProcessInstance receiveMessageProcessInstance1 = getProcessAPI()
                .startProcess(receiveMessageProcess.getId());

        // wait the event node instance
        waitForEvent(receiveMessageProcessInstance1, CATCH_EVENT_NAME, TestStates.WAITING);

        // instantiate a process containing correlations matching with receiveMessageProcessInstance1
        final ProcessInstance sendMessageProcessInstance1 = getProcessAPI().startProcess(sendMessageProcess.getId());
        waitForProcessToFinish(sendMessageProcessInstance1);

        waitForUserTask(receiveMessageProcessInstance1, CATCH_MESSAGE_STEP1_NAME);
        // waitForStep(100, 5000, "userTask1", receiveMessageProcessInstance1);

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);
    }

    /*
     * Verify that a sendProcess must have at least all correlation keys of the receiveProcess for the message to be
     * transmitted.
     * 1 receiveProcess, 2 sendProcess, receiveProcess1, sendProcess1, sendProcess2
     * dynamic -> deployAndEnable(sendProcess), deployAndEnable(receiveProcess), startProcess(sendProcess)
     * checks : sendProcess is finished, , receiveProcess start and stop on user task, data is transmitted to the
     * receiveProcess
     */
    @Test
    public void multipleCorrelationsKeys() throws Exception {
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithEndMessageEventAndCorrelation();
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithIntermediateCatchMessageEventAnd2Correlations();

        // start a instance of a receive message process
        final ProcessInstance receiveMessageProcessInstance1 = getProcessAPI().startProcess(
                receiveMessageProcess.getId(),
                Arrays.asList(
                        buildAssignOperation("docRef", "1", Integer.class.getName(), ExpressionType.TYPE_CONSTANT),
                        buildAssignOperation("name", "Doe Doe", String.class.getName(), ExpressionType.TYPE_CONSTANT)),
                null);

        // wait the event node instance
        waitForEvent(receiveMessageProcessInstance1, CATCH_EVENT_NAME, TestStates.WAITING);

        checkUserHasNoPendingTasks();

        // instantiate a process having one one correlation key matching, the process must not go further
        final ProcessInstance sendMessageProcessInstance1 = getProcessAPI().startProcess(
                sendMessageProcess.getId(),
                Arrays.asList(
                        buildAssignOperation("docNumber", "1", Integer.class.getName(), ExpressionType.TYPE_CONSTANT),
                        buildAssignOperation("lastName", "Doe 2", String.class.getName(),
                                ExpressionType.TYPE_CONSTANT)),
                null);
        waitForProcessToFinish(sendMessageProcessInstance1);
        // 1 sec because it's an assert false and we forced matching of event
        assertFalse(new WaitForStep(DEFAULT_REPEAT_EACH, 500, CATCH_MESSAGE_STEP1_NAME,
                receiveMessageProcessInstance1.getId(), getProcessAPI()).waitUntil());

        // instantiate a process having both two correlation keys matching, the process must go further
        final ProcessInstance sendMessageProcessInstance2 = getProcessAPI().startProcess(
                sendMessageProcess.getId(),
                Arrays.asList(
                        buildAssignOperation("docNumber", "1", Integer.class.getName(), ExpressionType.TYPE_CONSTANT),
                        buildAssignOperation("lastName", "Doe Doe", String.class.getName(),
                                ExpressionType.TYPE_CONSTANT)),
                null);
        waitForProcessToFinish(sendMessageProcessInstance2);
        waitForUserTask(receiveMessageProcessInstance1, CATCH_MESSAGE_STEP1_NAME);

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);
    }

    private Operation buildAssignOperation(final String dataInstanceName, final String newConstantValue,
            final String className,
            final ExpressionType expressionType) throws InvalidExpressionException {
        final LeftOperand leftOperand = new LeftOperandBuilder().createNewInstance().setName(dataInstanceName).done();
        final Expression expression = new ExpressionBuilder().createNewInstance(dataInstanceName)
                .setContent(newConstantValue)
                .setExpressionType(expressionType.name()).setReturnType(className).done();
        final Operation operation;
        operation = new OperationBuilder().createNewInstance().setOperator("=").setLeftOperand(leftOperand)
                .setType(OperatorType.ASSIGNMENT)
                .setRightOperand(expression).done();
        return operation;
    }

    /*
     * 1 receiveProcess, 1 sendProcess, Message goes from IntermediateEvent to StartEvent
     * dynamic -> deployAndEnable(sendProcess), deployAndEnable(receiveProcess), startProcess(sendProcess)
     * checks : sendProcess is finished, receiveProcess start and stop on user task.
     */
    @Test
    public void messageIntermediateThrowEventMessageSentAfterEnable() throws Exception {
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithIntermediateThrowMessageEvent(
                START_WITH_MESSAGE_PROCESS_NAME, "startEvent");
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithStartMessageEvent(
                Collections.<String, String> emptyMap(),
                Collections.<Operation> emptyList());

        final ProcessInstance sendMessageProcessInstance = getProcessAPI().startProcess(sendMessageProcess.getId());
        waitForProcessToFinish(sendMessageProcessInstance);
        waitForUserTask(START_WITH_MESSAGE_STEP1_NAME);

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);
    }

    @Test
    public void messageIntermediateThrow2EventMessages() throws Exception {
        final List<String> messages = new ArrayList<>();
        messages.add("catchMessage");
        messages.add("startMessage");
        final List<String> targetProcesses = new ArrayList<>();
        targetProcesses.add(CATCH_MESSAGE_PROCESS_NAME);
        targetProcesses.add(START_WITH_MESSAGE_PROCESS_NAME);
        final List<String> targetFlowNodes = new ArrayList<>();
        targetFlowNodes.add(CATCH_EVENT_NAME);
        targetFlowNodes.add("startEvent");
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithIntermediateThrowMessageEvent(messages,
                targetProcesses, targetFlowNodes);

        final ProcessDefinition startWithMessageProcess = deployAndEnableProcessWithStartMessageEvent(
                START_WITH_MESSAGE_PROCESS_NAME, "startMessage", null,
                null);
        final ProcessDefinition catchMessageProcess = deployAndEnableProcessWithIntermediateCatchMessageEvent(
                CATCH_MESSAGE_PROCESS_NAME, "catchMessage", null,
                null, null);

        final ProcessInstance catchMessageProcessInstance = getProcessAPI().startProcess(catchMessageProcess.getId());
        waitForEventInWaitingState(catchMessageProcessInstance, CATCH_EVENT_NAME);

        final ProcessInstance sendMessageProcessInstance = getProcessAPI().startProcess(sendMessageProcess.getId());
        waitForProcessToFinish(sendMessageProcessInstance);
        checkNbPendingTaskOf(2, user);

        final List<HumanTaskInstance> taskInstances = getProcessAPI()
                .getPendingHumanTaskInstances(user.getId(), 0, 10, ActivityInstanceCriterion.NAME_ASC);
        assertEquals(2, taskInstances.size());
        assertEquals(CATCH_MESSAGE_STEP1_NAME, taskInstances.get(0).getName());
        assertEquals(START_WITH_MESSAGE_STEP1_NAME, taskInstances.get(1).getName());

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(startWithMessageProcess);
        disableAndDeleteProcess(catchMessageProcess);
    }

    /*
     * 1 receiveProcess, 1 sendProcess, Message contains datas goes from EndEvent to StartEvent
     * dynamic -> deployAndEnable(sendProcess), deployAndEnable(receiveProcess), startProcess(sendProcess)
     * checks : sendProcess is finished, , receiveProcess start and stop on user task, data is transmitted to the
     * receiveProcess
     */
    @Test
    public void dataTransferFromMessageEndEventToStartMessageEvent() throws Exception {
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithEndMessageEvent(
                START_WITH_MESSAGE_PROCESS_NAME, "startEvent", null,
                Collections.singletonMap("lastName", String.class.getName()),
                Collections.singletonMap("lName", String.class.getName()),
                Collections.singletonMap("lName", "lastName"));
        final List<Operation> catchMessageOperations = Collections
                .singletonList(buildAssignOperation("name", "lName", String.class.getName(),
                        ExpressionType.TYPE_VARIABLE));
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithStartMessageEvent(
                Collections.singletonMap("name", String.class.getName()),
                catchMessageOperations);

        final ProcessInstance sendMessageProcessInstance = getProcessAPI().startProcess(sendMessageProcess.getId(),
                Arrays.asList(
                        buildAssignOperation("lastName", "Doe", String.class.getName(), ExpressionType.TYPE_CONSTANT)),
                null);
        waitForProcessToFinish(sendMessageProcessInstance);

        // at the first test some time the cron job time some time before executing
        final HumanTaskInstance userTask = waitForUserTaskAndGetIt(START_WITH_MESSAGE_STEP1_NAME);

        final DataInstance dataInstance = getProcessAPI().getProcessDataInstance("name", userTask.getRootContainerId());
        assertEquals("Doe", dataInstance.getValue());

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);
    }

    /*
     * 1 receiveProcess, 1 sendProcess, Message contains datas goes from EndEvent to IntermediateEvent
     * dynamic -> deployAndEnable(sendProcess), deployAndEnable(receiveProcess), startProcess(sendProcess)
     * checks : receiveProcess start and stop on catchEvent, sendProcess is finished, , receiveProcess continues and
     * reaches user task , data is transmitted to
     * the receiveProcess.
     */
    @Test
    public void dataTransferFromMessageEndEventToMessageIntermediateCatchEvent() throws Exception {
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithEndMessageEvent(
                CATCH_MESSAGE_PROCESS_NAME, CATCH_EVENT_NAME, null,
                Collections.singletonMap("lastName", String.class.getName()),
                Collections.singletonMap("lName", String.class.getName()),
                Collections.singletonMap("lName", "lastName"));

        final List<Operation> catchMessageOperations = Collections
                .singletonList(buildAssignOperation("name", "lName", String.class.getName(),
                        ExpressionType.TYPE_VARIABLE));
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithIntermediateCatchMessageEvent(null,
                Collections.singletonMap("name", String.class.getName()), catchMessageOperations);

        // start a instance of a receive message process
        final ProcessInstance receiveMessageProcessInstance = getProcessAPI()
                .startProcess(receiveMessageProcess.getId());

        // wait the event node instance
        waitForEventInWaitingState(receiveMessageProcessInstance, CATCH_EVENT_NAME);

        DataInstance dataInstance = getProcessAPI().getProcessDataInstance("name",
                receiveMessageProcessInstance.getId());
        assertNull("Data is not null", dataInstance.getValue());

        final ProcessInstance sendMessageProcessInstance = getProcessAPI().startProcess(sendMessageProcess.getId(),
                Arrays.asList(
                        buildAssignOperation("lastName", "Doe", String.class.getName(), ExpressionType.TYPE_CONSTANT)),
                null);
        waitForProcessToFinish(sendMessageProcessInstance);
        waitForUserTask(receiveMessageProcessInstance, CATCH_MESSAGE_STEP1_NAME);

        dataInstance = getProcessAPI().getProcessDataInstance("name", receiveMessageProcessInstance.getId());
        assertEquals("Doe", dataInstance.getValue());

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);
    }

    /*
     * Verify receiveProcess receive message targeting it, even if sent before its existence.
     * 1 receiveProcess, 1 sendProcess, Message goes from IntermediateEvent to IntermediateEvent
     * dynamic -> deployAndEnable(sendProcess), startProcess(sendProcess), deployAndEnable(receiveProcess),
     * startProcess(receiveProcess)
     * checks : sendProcess is finished, receiveProcess reaches catchEvent and continue (found message sent by
     * sendProcess) and reaches user task.
     */
    @Test
    public void messageSentProcessFinishBeforeReceiveProcessIsEnabled() throws Exception {
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithIntermediateThrowMessageEvent(
                CATCH_MESSAGE_PROCESS_NAME, CATCH_EVENT_NAME);
        final ProcessInstance sendMessageProcessInstance = getProcessAPI().startProcess(sendMessageProcess.getId());
        waitForProcessToFinish(sendMessageProcessInstance);

        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithIntermediateCatchMessageEvent(null,
                null, null);

        final ProcessInstance receiveMessageProcessInstance = getProcessAPI()
                .startProcess(receiveMessageProcess.getId());
        Thread.sleep(100);// small sleep but don't wait for the event to be waiting, it might happen that that event is already matched at this point
        waitForUserTask(receiveMessageProcessInstance, CATCH_MESSAGE_STEP1_NAME);

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);
    }

    /*
     * Test where the message goes from a throwEvent to a catchEvent belonging to the same process hence the same pool
     * (forbidden by BPMN 2.0)
     * But the studio forbid this case, so this should never happen in the Engine.
     */
    @Test
    public void messageEventIntraProcess() throws Exception {
        final ProcessDefinition sendAndReceiveMessageProcess = deployAndEnableProcessWithIntraMessageEvent(
                "sendAndReceiveMessageProcess", CATCH_EVENT_NAME);
        final ProcessInstance sendAndReceiveMessageProcessInstance = getProcessAPI()
                .startProcess(sendAndReceiveMessageProcess.getId());

        final long step2Id = waitForUserTask(sendAndReceiveMessageProcessInstance, "userTask2");
        waitForEventInWaitingState(sendAndReceiveMessageProcessInstance, CATCH_EVENT_NAME);
        assignAndExecuteStep(step2Id, user);
        waitForUserTask(sendAndReceiveMessageProcessInstance, "userTask3");

        disableAndDeleteProcess(sendAndReceiveMessageProcess);
    }

    /*
     * 1 receiveProcess, 2 sendProcess, 2 Messages go from EndEvent to IntermediateEvent
     * dynamic -> deployAndEnable(sendProcess), deployAndEnable(receiveProcess), startProcess(sendProcess),
     * startProcess(receiveProcess)
     * checks : sendProcess is finished, receiveProcess reaches catchEvent and continue (found message sent by
     * sendProcess) and reaches user task.
     */
    @Test
    public void messageIntermediateCatchEventMessageMultiSend() throws Exception {
        final ProcessDefinition sendMessageProcess1 = deployAndEnableProcessWithEndMessageEvent("sendMessageProcess1",
                MESSAGE_NAME,
                CATCH_MESSAGE_PROCESS_NAME, CATCH_EVENT_NAME, null, null, null, null);
        final ProcessDefinition sendMessageProcess2 = deployAndEnableProcessWithEndMessageEvent("sendMessageProcess2",
                MESSAGE_NAME,
                CATCH_MESSAGE_PROCESS_NAME, CATCH_EVENT_NAME, null, null, null, null);
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithIntermediateCatchMessageEvent(null,
                null, null);

        final ProcessInstance receiveMessageProcessInstance = getProcessAPI()
                .startProcess(receiveMessageProcess.getId());

        waitForEventInWaitingState(receiveMessageProcessInstance, CATCH_EVENT_NAME);

        final ProcessInstance sendMessageProcessInstance1 = getProcessAPI().startProcess(sendMessageProcess1.getId());
        waitForProcessToFinish(sendMessageProcessInstance1);
        waitForUserTask(CATCH_MESSAGE_STEP1_NAME);

        disableAndDeleteProcess(sendMessageProcess1);
        disableAndDeleteProcess(sendMessageProcess2);
        disableAndDeleteProcess(receiveMessageProcess);
    }

    @Test
    public void deleteProcessInstanceShouldDeleteWaitingEvents() throws Exception {
        final ProcessDefinition processDefinition = deployAndEnableProcessWithIntermediateCatchMessageEvent(null, null,
                null);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        assumeNotNull(processInstance);

        waitForEventInWaitingState(processInstance, CATCH_EVENT_NAME);

        final long processInstanceId = processInstance.getId();
        getProcessAPI().deleteProcessInstance(processInstanceId);
        assertThat(new WaitForEvent(50, 1000, CATCH_EVENT_NAME, processInstanceId, getProcessAPI()).waitUntil(),
                is(false));

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void sendMessageViaAPIToStartMessageEvent() throws Exception {
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithStartMessageEvent(
                Collections.<String, String> emptyMap(),
                Collections.<Operation> emptyList());

        // send message
        sendMessage(MESSAGE_NAME, START_WITH_MESSAGE_PROCESS_NAME, "startEvent", null);
        waitForUserTask(START_WITH_MESSAGE_STEP1_NAME);

        disableAndDeleteProcess(receiveMessageProcess);
    }

    protected void sendMessage(final String messageName, final String targetProcessName,
            final String targetFlowNodeName,
            final Map<Expression, Expression> messageContent) throws BonitaException {
        final Expression targetProcessExpression = new ExpressionBuilder()
                .createConstantStringExpression(targetProcessName);
        final Expression targetFlowNodeExpression = new ExpressionBuilder()
                .createConstantStringExpression(targetFlowNodeName);
        getProcessAPI().sendMessage(messageName, targetProcessExpression, targetFlowNodeExpression, messageContent);
    }

    private void sendMessage(final String messageName, final String targetProcessName, final String targetFlowNodeName,
            final Map<Expression, Expression> messageContent, final Map<Expression, Expression> correlations)
            throws BonitaException {
        final Expression targetProcessExpression = new ExpressionBuilder()
                .createConstantStringExpression(targetProcessName);
        final Expression targetFlowNodeExpression = new ExpressionBuilder()
                .createConstantStringExpression(targetFlowNodeName);
        getProcessAPI().sendMessage(messageName, targetProcessExpression, targetFlowNodeExpression, messageContent,
                correlations);
    }

    @Test
    public void sendMessageViaAPIToIntermediateMessageEvent() throws Exception {
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithIntermediateCatchMessageEvent(null,
                null, null);

        final ProcessInstance receiveMessageProcessInstance = getProcessAPI()
                .startProcess(receiveMessageProcess.getId());
        waitForEventInWaitingState(receiveMessageProcessInstance, CATCH_EVENT_NAME);

        checkUserHasNoPendingTasks();

        // send message
        sendMessage(MESSAGE_NAME, CATCH_MESSAGE_PROCESS_NAME, CATCH_EVENT_NAME, null);
        waitForUserTask(CATCH_MESSAGE_STEP1_NAME);

        disableAndDeleteProcess(receiveMessageProcess);
    }

    @Test
    public void sendMessageWithDataViaAPIToStartMessageEvent() throws Exception {
        final Expression lastNameDisplay = new ExpressionBuilder().createConstantStringExpression("lName");
        final Expression lastNameValue = new ExpressionBuilder().createConstantStringExpression("Doe");

        final List<Operation> catchMessageOperations = Collections
                .singletonList(buildAssignOperation("name", "lName", String.class.getName(),
                        ExpressionType.TYPE_VARIABLE));
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithStartMessageEvent(
                Collections.singletonMap("name", String.class.getName()),
                catchMessageOperations);

        checkUserHasNoPendingTasks();

        sendMessage(MESSAGE_NAME, START_WITH_MESSAGE_PROCESS_NAME, "startEvent",
                Collections.singletonMap(lastNameDisplay, lastNameValue));
        // at the first test some time the cron job time some time before executing
        final HumanTaskInstance taskInstance = waitForUserTaskAndGetIt(START_WITH_MESSAGE_STEP1_NAME);

        final DataInstance dataInstance = getProcessAPI().getProcessDataInstance("name",
                taskInstance.getRootContainerId());
        assertEquals("Doe", dataInstance.getValue());

        // Clean up
        disableAndDeleteProcess(receiveMessageProcess);
    }

    @Test
    public void sendMessageTwiceTriggersTwoStartMessageEvents() throws Exception {
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithStartMessageEvent(
                Collections.<String, String> emptyMap(),
                Collections.<Operation> emptyList());
        sendMessage(MESSAGE_NAME, START_WITH_MESSAGE_PROCESS_NAME, "startEvent",
                Collections.<Expression, Expression> emptyMap());
        final ActivityInstance taskFirstProcInst = waitForUserTaskAndGetIt(START_WITH_MESSAGE_STEP1_NAME);

        sendMessage(MESSAGE_NAME, START_WITH_MESSAGE_PROCESS_NAME, "startEvent",
                Collections.<Expression, Expression> emptyMap());
        final ActivityInstance taskSecondProcInst = waitForUserTaskAndGetIt(START_WITH_MESSAGE_STEP1_NAME);
        assertNotEquals(taskFirstProcInst.getId(), taskSecondProcInst.getId());
        assertNotEquals(taskFirstProcInst.getParentProcessInstanceId(),
                taskSecondProcInst.getParentProcessInstanceId());

        disableAndDeleteProcess(receiveMessageProcess);
    }

    private void checkUserHasNoPendingTasks() {
        final List<HumanTaskInstance> taskInstances = getProcessAPI()
                .getPendingHumanTaskInstances(user.getId(), 0, 10, ActivityInstanceCriterion.NAME_ASC);
        assertEquals(0, taskInstances.size());
    }

    @Test
    public void sendMessageWithCorrelationViaAPIToIntermediateMessageEvent() throws Exception {
        final Expression docCorrelationKey = new ExpressionBuilder().createConstantStringExpression("docKey");
        final Expression docCorrelationValue = new ExpressionBuilder().createConstantIntegerExpression(1);
        final Expression nameCorrelationKey = new ExpressionBuilder().createConstantStringExpression("nameKey");
        final Expression nameCorrelationValue1 = new ExpressionBuilder().createConstantStringExpression("Doe 2");
        final Expression nameCorrelationValue2 = new ExpressionBuilder().createConstantStringExpression("Doe Doe");

        final Map<Expression, Expression> correlations1 = new HashMap<>(2);
        correlations1.put(docCorrelationKey, docCorrelationValue);
        correlations1.put(nameCorrelationKey, nameCorrelationValue1); // don't match

        final Map<Expression, Expression> correlations2 = new HashMap<>(2);
        correlations2.put(docCorrelationKey, docCorrelationValue);
        correlations2.put(nameCorrelationKey, nameCorrelationValue2);

        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithIntermediateCatchMessageEventAnd2Correlations();

        // start a instance of a receive message process
        final ProcessInstance receiveMessageProcessInstance1 = getProcessAPI().startProcess(
                receiveMessageProcess.getId(),
                Arrays.asList(
                        buildAssignOperation("docRef", "1", Integer.class.getName(), ExpressionType.TYPE_CONSTANT),
                        buildAssignOperation("name", "Doe Doe", String.class.getName(), ExpressionType.TYPE_CONSTANT)),
                null);

        // wait the event node instance
        waitForEvent(receiveMessageProcessInstance1, CATCH_EVENT_NAME, TestStates.WAITING);

        // send a message having only one correlation key matching, the process must not go further
        sendMessage(MESSAGE_NAME, CATCH_MESSAGE_PROCESS_NAME, CATCH_EVENT_NAME,
                Collections.<Expression, Expression> emptyMap(), correlations1);
        assertFalse(new WaitForStep(50, 1000, "userTask1", receiveMessageProcessInstance1.getId(), getProcessAPI())
                .waitUntil());

        // send a message having both two correlations keys matching, the process must go further
        sendMessage(MESSAGE_NAME, CATCH_MESSAGE_PROCESS_NAME, CATCH_EVENT_NAME,
                Collections.<Expression, Expression> emptyMap(), correlations2);
        waitForUserTask(receiveMessageProcessInstance1, CATCH_MESSAGE_STEP1_NAME);

        disableAndDeleteProcess(receiveMessageProcess);
    }

    @Test
    public void sendMessageWithDataViaAPIToIntermediateCatchMessageEvent() throws Exception {
        final List<Operation> catchMessageOperations = Collections
                .singletonList(buildAssignOperation("name", "lName", String.class.getName(),
                        ExpressionType.TYPE_VARIABLE));
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithIntermediateCatchMessageEvent(null,
                Collections.singletonMap("name", String.class.getName()), catchMessageOperations);

        // start a instance of a receive message process
        final ProcessInstance receiveMessageProcessInstance = getProcessAPI()
                .startProcess(receiveMessageProcess.getId());

        // wait the event node instance
        waitForEventInWaitingState(receiveMessageProcessInstance, CATCH_EVENT_NAME);

        DataInstance dataInstance = getProcessAPI().getProcessDataInstance("name",
                receiveMessageProcessInstance.getId());
        assertNull("Data is not null", dataInstance.getValue());

        final Expression lastNameDisplay = new ExpressionBuilder().createConstantStringExpression("lName");
        final Expression lastNameValue = new ExpressionBuilder().createConstantStringExpression("Doe");
        sendMessage(MESSAGE_NAME, CATCH_MESSAGE_PROCESS_NAME, CATCH_EVENT_NAME,
                Collections.singletonMap(lastNameDisplay, lastNameValue));
        waitForUserTask(receiveMessageProcessInstance, CATCH_MESSAGE_STEP1_NAME);

        dataInstance = getProcessAPI().getProcessDataInstance("name", receiveMessageProcessInstance.getId());
        assertEquals("Doe", dataInstance.getValue());

        disableAndDeleteProcess(receiveMessageProcess);
    }

    @Test
    public void should_be_able_to_send_a_good_message_even_after_sending_a_bad_one() throws Exception {
        DesignProcessDefinition process = new ProcessDefinitionBuilder()
                .createNewInstance(CATCH_MESSAGE_PROCESS_NAME, PROCESS_VERSION)
                .addActor(ACTOR_NAME)
                .addShortTextData("processData", null)
                .addIntermediateCatchEvent("waitingMessage")
                .addMessageEventTrigger("aMessage")
                .addOperation(new OperationBuilder().createSetDataOperation("processData",
                        new ExpressionBuilder().createDataExpression("messageData", String.class.getName())))
                .addUserTask("step1", ACTOR_NAME)
                .addTransition("waitingMessage", "step1").getProcess();

        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithActor(process, ACTOR_NAME, user);
        final ProcessInstance receiveMessageProcessInstance = getProcessAPI()
                .startProcess(receiveMessageProcess.getId());

        // send a message that match the waiting message but with missing data
        sendMessage("aMessage", CATCH_MESSAGE_PROCESS_NAME, "waitingMessage", emptyMap());
        Thread.sleep(200);
        // the message should not be handled (no way to check that using the API)
        Assertions.assertThat(getProcessAPI().getOpenActivityInstances(receiveMessageProcessInstance.getId(), 0, 10,
                ActivityInstanceCriterion.DEFAULT)).isEmpty();

        // send the same message but  with the missing data
        sendMessage("aMessage", CATCH_MESSAGE_PROCESS_NAME, "waitingMessage",
                singletonMap(string("messageData"), string("Doe")));
        // the waiting message is now correctly triggered
        waitForUserTask(receiveMessageProcessInstance, CATCH_MESSAGE_STEP1_NAME);

        // we verify that it was really the second message that matched
        DataInstance dataInstance = getProcessAPI().getProcessDataInstance("processData",
                receiveMessageProcessInstance.getId());
        Assertions.assertThat(dataInstance.getValue()).isEqualTo("Doe");

        disableAndDeleteProcess(receiveMessageProcess);
    }

    private Expression string(String messageData) throws InvalidExpressionException {
        return new ExpressionBuilder().createConstantStringExpression(messageData);
    }

    @Test(expected = SendEventException.class)
    public void sendMessageWithTooManyCorrelations() throws Exception {
        final Map<Expression, Expression> correlations = new HashMap<>(6);
        correlations.put(new ExpressionBuilder().createConstantStringExpression("key1"),
                new ExpressionBuilder().createConstantIntegerExpression(1));
        correlations.put(new ExpressionBuilder().createConstantStringExpression("key2"),
                new ExpressionBuilder().createConstantStringExpression("label"));
        correlations.put(new ExpressionBuilder().createConstantStringExpression("key3"),
                new ExpressionBuilder().createConstantStringExpression("2"));
        correlations.put(new ExpressionBuilder().createConstantStringExpression("key4"),
                new ExpressionBuilder().createConstantStringExpression("Doe 2"));
        correlations.put(new ExpressionBuilder().createConstantStringExpression("key5"),
                new ExpressionBuilder().createConstantStringExpression("Doe 2"));
        correlations.put(new ExpressionBuilder().createConstantStringExpression("key6"),
                new ExpressionBuilder().createConstantStringExpression("Doe 2"));

        final Expression targetProcessExpression = new ExpressionBuilder().createConstantStringExpression("p1");
        final Expression targetFlowNodeExpression = new ExpressionBuilder().createConstantStringExpression("step1");

        getProcessAPI().sendMessage(MESSAGE_NAME, targetProcessExpression, targetFlowNodeExpression, null,
                correlations);
    }

    @Test
    public void sendMessageToTerminateProcessWithLoop() throws Exception {
        ProcessDefinition processToKillDefinition = null;
        ProcessDefinition killerProcessDefinition = null;
        try {
            // Process to kill
            final Expression falseExpr = new ExpressionBuilder().createConstantBooleanExpression(false);
            final Expression dataExpr = new ExpressionBuilder().createDataExpression("endtask",
                    Boolean.class.getName());
            final Expression condition = new ExpressionBuilder().createGroovyScriptExpression("check", "!endtask",
                    Boolean.class.getName(), dataExpr);
            final Expression loopMax = new ExpressionBuilder().createConstantIntegerExpression(10);
            final ProcessDefinitionBuilder processToKillDefinitionBuilder = new ProcessDefinitionBuilder()
                    .createNewInstance("ProcessToKill", PROCESS_VERSION);
            processToKillDefinitionBuilder.addActor(ACTOR_NAME).addStartEvent("ToKillStart").addEndEvent("ToKillEnd")
                    .addTerminateEventTrigger();
            processToKillDefinitionBuilder.addData("endtask", Boolean.class.getName(), falseExpr);
            processToKillDefinitionBuilder.addUserTask("Step1", ACTOR_NAME).addLoop(false, condition, loopMax);
            processToKillDefinitionBuilder.addUserTask("Step2", ACTOR_NAME);
            processToKillDefinitionBuilder.addGateway("Gateway", GatewayType.PARALLEL);
            // Catch Message Event
            final CatchMessageEventTriggerDefinitionBuilder catchMessageEventTriggerDefinitionBuilder = processToKillDefinitionBuilder
                    .addIntermediateCatchEvent(CATCH_EVENT_NAME).addMessageEventTrigger("msgKiller");
            final ArrayList<BEntry<Expression, Expression>> correlations = new ArrayList<>(1);
            final Expression correlationKey = new ExpressionBuilder().createConstantStringExpression("key");
            final Expression correlationValue = new ExpressionBuilder().createGroovyScriptExpression("getId",
                    "processInstanceId", Long.class.getName(),
                    new ExpressionBuilder().createEngineConstant(ExpressionConstants.PROCESS_INSTANCE_ID));
            correlations.add(new BEntry<>(correlationKey, correlationValue));
            addCorrelations(catchMessageEventTriggerDefinitionBuilder, correlations);
            // Transitions
            processToKillDefinitionBuilder.addTransition("ToKillStart", "Gateway");
            processToKillDefinitionBuilder.addTransition("Gateway", "Step1");
            processToKillDefinitionBuilder.addTransition("Step1", "Step2");
            processToKillDefinitionBuilder.addTransition("Step2", "ToKillEnd");
            processToKillDefinitionBuilder.addTransition("Gateway", CATCH_EVENT_NAME);
            processToKillDefinitionBuilder.addTransition(CATCH_EVENT_NAME, "ToKillEnd");
            processToKillDefinition = deployAndEnableProcessWithActor(processToKillDefinitionBuilder.done(), ACTOR_NAME,
                    user);

            final StartProcessUntilStep toKillStartProcessAndWaitForTask = startProcessAndWaitForTask(
                    processToKillDefinition.getId(), "Step1");
            final ProcessInstance processToKillInstance = toKillStartProcessAndWaitForTask.getProcessInstance();

            // Killer process
            final ProcessDefinitionBuilder killerProcessDefinitionBuilder = new ProcessDefinitionBuilder()
                    .createNewInstance("KillerProcess", PROCESS_VERSION);
            killerProcessDefinitionBuilder.addActor(ACTOR_NAME).addStartEvent("KillerStart");
            killerProcessDefinitionBuilder.addUserTask("Step3", ACTOR_NAME);
            // Throw Message Event
            final Expression targetProcess = new ExpressionBuilder().createConstantStringExpression("ProcessToKill");
            final Expression targetFlowNode = new ExpressionBuilder().createConstantStringExpression(CATCH_EVENT_NAME);
            final ThrowMessageEventTriggerBuilder throwMessageEventTriggerBuilder = killerProcessDefinitionBuilder
                    .addEndEvent("KillerEnd")
                    .addMessageEventTrigger("msgKiller", targetProcess, targetFlowNode);
            final ArrayList<BEntry<Expression, Expression>> endCorrelations = new ArrayList<>(1);
            final Expression endCorrelationKey = new ExpressionBuilder().createConstantStringExpression("key");
            final Expression endCorrelationValue = new ExpressionBuilder()
                    .createConstantLongExpression(processToKillInstance.getId());
            endCorrelations.add(new BEntry<>(endCorrelationKey, endCorrelationValue));
            addCorrelations(throwMessageEventTriggerBuilder, endCorrelations);
            // Transitions
            killerProcessDefinitionBuilder.addTransition("KillerStart", "Step3");
            killerProcessDefinitionBuilder.addTransition("Step3", "KillerEnd");
            killerProcessDefinition = deployAndEnableProcessWithActor(killerProcessDefinitionBuilder.done(), ACTOR_NAME,
                    user);

            final StartProcessUntilStep killerStartProcessAndWaitForTask = startProcessAndWaitForTask(
                    killerProcessDefinition.getId(), "Step3");
            assignAndExecuteStep(killerStartProcessAndWaitForTask.getActivityInstance(), user);
            waitForProcessToFinish(killerStartProcessAndWaitForTask.getProcessInstance());

            // Check that process to kill is terminated
            waitForProcessToFinish(processToKillInstance);
            final ArchivedActivityInstance step1 = getProcessAPI()
                    .getArchivedActivityInstance(toKillStartProcessAndWaitForTask.getActivityInstance().getId());
            assertEquals(ActivityStates.ABORTED_STATE, step1.getState());
        } finally {
            // Clean up
            disableAndDeleteProcess(processToKillDefinition, killerProcessDefinition);
        }
    }

    @Test
    public void can_use_a_variable_to_define_target_process() throws Exception {
        //given
        ProcessDefinition receiveMsgProcess = deployAndEnableProcessWithStartMessageEvent("receiveMsgProcess", "go");
        ProcessDefinition sendMessageProcess = deployAndEnableProcessSendingMessageUsingVariableAsTarget(
                "receiveMsgProcess", "startEvent", "go");

        //when
        ProcessInstance processInstance = getProcessAPI().startProcess(sendMessageProcess.getId());

        //then
        waitForProcessToFinish(processInstance);
        long taskId = waitForUserTask(START_WITH_MESSAGE_STEP1_NAME);
        HumanTaskInstance taskInstance = getProcessAPI().getHumanTaskInstance(taskId);
        Assertions.assertThat(taskInstance.getProcessDefinitionId()).isEqualTo(receiveMsgProcess.getId());

        //clean up
        disableAndDeleteProcess(receiveMsgProcess, sendMessageProcess);
    }

    @Test
    public void can_delete_message_by_creationDate() throws Exception {
        //clean any existing messages
        getProcessAPI().deleteMessageByCreationDate(System.currentTimeMillis(), null);

        final Expression targetProcessExpression = new ExpressionBuilder().createConstantStringExpression("p1");
        final Expression targetFlowNodeExpression = new ExpressionBuilder().createConstantStringExpression("step1");

        getProcessAPI().sendMessage("go", targetProcessExpression, targetFlowNodeExpression, null, null);
        long untilDate = System.currentTimeMillis();

        TimeUnit.MILLISECONDS.sleep(5);

        getProcessAPI().sendMessage("go2", targetProcessExpression, targetFlowNodeExpression, null, null);

        getProcessAPI().sendMessage("go3", targetProcessExpression, targetFlowNodeExpression, null, null);

        int nbMessageDeleted = getProcessAPI().deleteMessageByCreationDate(untilDate, null);
        Assertions.assertThat(nbMessageDeleted).isEqualTo(1);

        SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 2);
        searchOptionsBuilder.filter("messageName", "go2");

        nbMessageDeleted = getProcessAPI().deleteMessageByCreationDate(System.currentTimeMillis(),
                searchOptionsBuilder.done());
        Assertions.assertThat(nbMessageDeleted).isEqualTo(1);

        //clean up
        nbMessageDeleted = getProcessAPI().deleteMessageByCreationDate(System.currentTimeMillis(), null);
        Assertions.assertThat(nbMessageDeleted).isEqualTo(1);

        nbMessageDeleted = getProcessAPI().deleteMessageByCreationDate(System.currentTimeMillis(), null);
        Assertions.assertThat(nbMessageDeleted).isEqualTo(0);

    }
}
