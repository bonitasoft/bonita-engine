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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedSendTaskInstance;
import org.bonitasoft.engine.bpm.flownode.EventInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.IntermediateCatchEventInstance;
import org.bonitasoft.engine.bpm.flownode.SendTaskInstance;
import org.bonitasoft.engine.bpm.flownode.StartEventInstance;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.SendTaskDefinitionBuilder;
import org.bonitasoft.engine.event.AbstractEventIT;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.operation.LeftOperand;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class SendTaskIT extends AbstractEventIT {

    public static final String DATA_INPUT_ITEM_REF_NAME = "lastName";

    private ProcessDefinition deployAndEnableProcessWithSendTask(final String processName, final String messageName, final String targetProcess,
            final String targetFlowNode, final Map<String, String> processData,
            final Map<String, String> messageData, final Map<String, String> dataInputMapping) throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder();
        processBuilder.createNewInstance(processName, "1.0");
        addProcessData(processData, processBuilder);
        processBuilder.addStartEvent("startEvent");
        // create expression for target process/flowNode
        final Expression targetProcessExpression = new ExpressionBuilder().createConstantStringExpression(targetProcess);
        final SendTaskDefinitionBuilder sendTaskDefinitionBuilder;
        sendTaskDefinitionBuilder = processBuilder.addSendTask("sendMessage", messageName, targetProcessExpression);
        if (targetFlowNode != null) {
            sendTaskDefinitionBuilder.setTargetFlowNode(new ExpressionBuilder().createConstantStringExpression(targetFlowNode));
        }
        processBuilder.addEndEvent("endEvent");
        addMessageData(messageData, dataInputMapping, sendTaskDefinitionBuilder);
        processBuilder.addTransition("startEvent", "sendMessage");
        processBuilder.addTransition("sendMessage", "endEvent");
        final DesignProcessDefinition designProcessDefinition = processBuilder.done();

        final ProcessDefinition sendMessageProcess = deployAndEnableProcess(designProcessDefinition);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(sendMessageProcess.getId());
        assertThat(processDeploymentInfo.getActivationState()).isEqualTo(ActivationState.ENABLED);

        return sendMessageProcess;
    }

    private void addMessageData(final Map<String, String> messageData, final Map<String, String> dataInputMapping,
            final SendTaskDefinitionBuilder sendTaskDefinitionBuilder) throws InvalidExpressionException {
        if (messageData != null) {
            for (final Entry<String, String> entry : messageData.entrySet()) {
                final Expression displayName = new ExpressionBuilder().createConstantStringExpression(entry.getKey());

                Expression defaultValue = null;
                if (dataInputMapping.containsKey(entry.getKey())) {
                    defaultValue = new ExpressionBuilder().createDataExpression(dataInputMapping.get(entry.getKey()), entry.getValue());
                }
                sendTaskDefinitionBuilder.addMessageContentExpression(displayName, defaultValue);
            }
        }
    }

    private ProcessDefinition deployAndEnableProcessWithMultiInstantiatedSendTask(final String processName, final String messageName,
            final String targetProcess,
            final String targetFlowNode, String inputListScript, final Map<String, String> messageData, final Map<String, String> dataInputMapping)
            throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder();
        processBuilder.createNewInstance(processName, "1.0");
        processBuilder.addStartEvent("startEvent");
        String loopDataInput = "loopDataInput";
        processBuilder
                .addData(
                        loopDataInput,
                        List.class.getName(),
                        new ExpressionBuilder().createGroovyScriptExpression("executeAMultiInstanceWithLoopDataInputAndOutput1", inputListScript,
                                List.class.getName()));
        // create expression for target process/flowNode
        final Expression targetProcessExpression = new ExpressionBuilder().createConstantStringExpression(targetProcess);
        final SendTaskDefinitionBuilder sendTaskDefinitionBuilder;
        sendTaskDefinitionBuilder = processBuilder.addSendTask("sendMessage", messageName, targetProcessExpression);
        if (targetFlowNode != null) {
            sendTaskDefinitionBuilder.setTargetFlowNode(new ExpressionBuilder().createConstantStringExpression(targetFlowNode));
        }
        sendTaskDefinitionBuilder.addShortTextData(DATA_INPUT_ITEM_REF_NAME, null);
        sendTaskDefinitionBuilder.addMultiInstance(false, loopDataInput).addDataInputItemRef(DATA_INPUT_ITEM_REF_NAME);
        processBuilder.addEndEvent("endEvent");
        addMessageData(messageData, dataInputMapping, sendTaskDefinitionBuilder);
        processBuilder.addTransition("startEvent", "sendMessage");
        processBuilder.addTransition("sendMessage", "endEvent");
        final DesignProcessDefinition designProcessDefinition = processBuilder.done();

        final ProcessDefinition sendMessageProcess = deployAndEnableProcess(designProcessDefinition);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(sendMessageProcess.getId());
        assertThat(processDeploymentInfo.getActivationState()).isEqualTo(ActivationState.ENABLED);

        return sendMessageProcess;
    }

    private Operation buildAssignOperation(final String dataInstanceName, final String newConstantValue, final String className,
            final ExpressionType expressionType) throws InvalidExpressionException {
        final LeftOperand leftOperand = new LeftOperandBuilder().createNewInstance().setName(dataInstanceName).done();
        final Expression expression = new ExpressionBuilder().createNewInstance(dataInstanceName).setContent(newConstantValue)
                .setExpressionType(expressionType).setReturnType(className).done();
        final Operation operation;
        operation = new OperationBuilder().createNewInstance().setOperator("=").setLeftOperand(leftOperand).setType(OperatorType.ASSIGNMENT)
                .setRightOperand(expression).done();
        return operation;
    }

    /*
     * 1 receiveProcess, 1 sendProcess, Message contains data goes from SendTask to IntermediateEvent
     * dynamic -> deployAndEnable(sendProcess), deployAndEnable(receiveProcess), startProcess(sendProcess)
     * checks : receiveProcess start and stop on catchEvent, sendProcess is finished, , receiveProcess continues and reaches user task , data is transmitted to
     * the receiveProcess.
     */
    @Cover(classes = { EventInstance.class, IntermediateCatchEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message event",
            "Intermediate catch event", "Send", "Receive" }, story = "Send a message with data from an and event of a process  to an intermediate event of an other process.", jira = "")
    @Test
    public void dataTransferFromSendTaskToMessageIntermediateCatchEventWithTargetFlowNode() throws Exception {
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithSendTask("sendMessageProcess", "m14", "receiveMessageProcess",
                "waitForMessage", Collections.singletonMap("lastName", String.class.getName()),
                Collections.singletonMap("lName", String.class.getName()), Collections.singletonMap("lName", "lastName"));

        final List<Operation> catchMessageOperations = Collections.singletonList(buildAssignOperation("name", "lName", String.class.getName(),
                ExpressionType.TYPE_VARIABLE));
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithIntermediateCatchMessageEvent("receiveMessageProcess", "m14", null,
                Collections.singletonMap("name", String.class.getName()), catchMessageOperations);

        // start a instance of a receive message process
        final ProcessInstance receiveMessageProcessInstance = getProcessAPI().startProcess(receiveMessageProcess.getId());

        // wait the event node instance
        waitForEventInWaitingState(receiveMessageProcessInstance, CATCH_EVENT_NAME);

        DataInstance dataInstance = getProcessAPI().getProcessDataInstance("name", receiveMessageProcessInstance.getId());
        assertThat(dataInstance.getValue()).isNull();

        final ProcessInstance sendMessageProcessInstance = getProcessAPI().startProcess(sendMessageProcess.getId(),
                Arrays.asList(buildAssignOperation("lastName", "Doe", String.class.getName(), ExpressionType.TYPE_CONSTANT)), null);
        waitForProcessToFinish(sendMessageProcessInstance);
        final List<ArchivedActivityInstance> archivedActivityInstances = getProcessAPI().getArchivedActivityInstances(sendMessageProcessInstance.getId(), 0,
                10, ActivityInstanceCriterion.LAST_UPDATE_DESC);
        assertThat(archivedActivityInstances.get(0)).isInstanceOf(ArchivedSendTaskInstance.class);
        forceMatchingOfEvents();
        waitForUserTask(receiveMessageProcessInstance, CATCH_MESSAGE_STEP1_NAME);

        dataInstance = getProcessAPI().getProcessDataInstance("name", receiveMessageProcessInstance.getId());
        assertThat(dataInstance.getValue()).isEqualTo("Doe");

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);
    }

    /*
     * 1 receiveProcess, 1 sendProcess, Message contains data goes from SendTask to IntermediateEvent
     * dynamic -> deployAndEnable(sendProcess), deployAndEnable(receiveProcess), startProcess(sendProcess)
     * checks : receiveProcess start and stop on catchEvent, sendProcess is finished, , receiveProcess continues and reaches user task , data is transmitted to
     * the receiveProcess.
     */
    @Cover(classes = { EventInstance.class, IntermediateCatchEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message event",
            "Intermediate catch event", "Send", "Receive" }, story = "Send a message with data from an and event of a process  to an intermediate event of an other process.", jira = "")
    @Test
    public void dataTransferFromSendTaskToMessageIntermediateCatchEvent() throws Exception {
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithSendTask("sendMessageProcess", "m14", "receiveMessageProcess",
                null, Collections.singletonMap("lastName", String.class.getName()), Collections.singletonMap("lName", String.class.getName()),
                Collections.singletonMap("lName", "lastName"));

        final List<Operation> catchMessageOperations = Collections.singletonList(buildAssignOperation("name", "lName", String.class.getName(),
                ExpressionType.TYPE_VARIABLE));
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithIntermediateCatchMessageEvent("receiveMessageProcess", "m14", null,
                Collections.singletonMap("name", String.class.getName()), catchMessageOperations);

        // start a instance of a receive message process
        final ProcessInstance receiveMessageProcessInstance = getProcessAPI().startProcess(receiveMessageProcess.getId());

        // wait the event node instance
        waitForEventInWaitingState(receiveMessageProcessInstance, CATCH_EVENT_NAME);

        DataInstance dataInstance = getProcessAPI().getProcessDataInstance("name", receiveMessageProcessInstance.getId());
        assertThat(dataInstance.getValue()).isNull();

        final ProcessInstance sendMessageProcessInstance = getProcessAPI().startProcess(sendMessageProcess.getId(),
                Arrays.asList(buildAssignOperation("lastName", "Doe", String.class.getName(), ExpressionType.TYPE_CONSTANT)), null);
        waitForProcessToFinish(sendMessageProcessInstance);
        forceMatchingOfEvents();
        waitForUserTask(receiveMessageProcessInstance, CATCH_MESSAGE_STEP1_NAME);

        dataInstance = getProcessAPI().getProcessDataInstance("name", receiveMessageProcessInstance.getId());
        assertThat(dataInstance.getValue()).isEqualTo("Doe");

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);
    }

    /*
     * 1 receiveProcess, 1 sendProcess, Message contains data goes from multi instantiated SendTask to StartEvent
     * dynamic -> deployAndEnable(sendProcess), deployAndEnable(receiveProcess), startProcess(sendProcess)
     * checks : receiveProcess start and reaches user task , data is transmitted to the receiveProcess.
     */
    @Cover(classes = { EventInstance.class, SendTaskInstance.class, StartEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event",
            "Message event",
            "Start event", "Send", "multi-instance", "Receive" }, story = "Send a message with data from a send of a process  to a start event of an other process.", jira = "BS-12292")
    @Test
    public void can_transfer_taskData_from_multi_instance_sendTask_to_messageStartEvent() throws Exception {
        //given
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithMultiInstantiatedSendTask("sendMessageProcess", "m15", "receiveMessageProcess",
                START_EVENT_NAME, "[\"Doe\", \"Smith\"]", Collections.singletonMap("lName", String.class.getName()),
                Collections.singletonMap("lName", DATA_INPUT_ITEM_REF_NAME));

        final List<Operation> catchMessageOperations = Collections.singletonList(buildAssignOperation("name", "lName", String.class.getName(),
                ExpressionType.TYPE_VARIABLE));
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithStartMessageEvent("receiveMessageProcess", "m15",
                Collections.singletonMap("name", String.class.getName()), catchMessageOperations);

        //when
        final ProcessInstance sendMessageProcessInstance = getProcessAPI().startProcess(sendMessageProcess.getId());

        //then
        waitForProcessToFinish(sendMessageProcessInstance);
        forceMatchingOfEvents();

        //two instances should be created
        //step of first instance
        HumanTaskInstance step1I1 = waitForUserTaskAndGetIt(START_WITH_MESSAGE_STEP1_NAME);
        assertThat(step1I1.getProcessDefinitionId()).isEqualTo(receiveMessageProcess.getId());

        //step of second instance
        HumanTaskInstance step1I2 = waitForUserTaskAndGetIt(START_WITH_MESSAGE_STEP1_NAME);
        assertThat(step1I2.getProcessDefinitionId()).isEqualTo(receiveMessageProcess.getId());

        assertThat(step1I1.getParentProcessInstanceId()).isNotEqualTo(step1I2.getParentProcessInstanceId());

        //data of first instance
        DataInstance dataInstanceI1 = getProcessAPI().getProcessDataInstance("name", step1I1.getParentProcessInstanceId());
        //data of second instance
        DataInstance dataInstanceI2 = getProcessAPI().getProcessDataInstance("name", step1I2.getParentProcessInstanceId());

        assertThat(Arrays.asList(dataInstanceI1.getValue(), dataInstanceI2.getValue())).contains("Doe", "Smith");

        //clean up
        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);
    }

}
