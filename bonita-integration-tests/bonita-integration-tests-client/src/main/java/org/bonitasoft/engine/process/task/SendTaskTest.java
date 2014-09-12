/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.process.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.bpm.actor.ActorCriterion;
import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.xml.XMLProcessDefinition.BEntry;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedSendTaskInstance;
import org.bonitasoft.engine.bpm.flownode.EventInstance;
import org.bonitasoft.engine.bpm.flownode.IntermediateCatchEventInstance;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.CatchMessageEventTriggerDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.SendTaskDefinitionBuilder;
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
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class SendTaskTest extends CommonAPITest {

    private User user = null;

    @Before
    public void setUp() throws Exception {
         loginOnDefaultTenantWithDefaultTechnicalUser();
        user = getIdentityAPI().createUser("john", "bpm");
    }

    @After
    public void tearDown() throws Exception {
        if (user != null) {
            getIdentityAPI().deleteUser(user.getId());
        }
        logoutOnTenant();
    }

    private ProcessDefinition deployAndEnableProcessWithEndMessageEvent(final String processName, final String messageName, final String targetProcess,
            final String targetFlowNode, final List<BEntry<Expression, Expression>> correlations, final Map<String, String> processData,
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
        if (correlations != null) {
            for (final Entry<Expression, Expression> entry1 : correlations) {
                sendTaskDefinitionBuilder.addCorrelation(entry1.getKey(), entry1.getValue());
            }
        }
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
        processBuilder.addTransition("startEvent", "sendMessage");
        processBuilder.addTransition("sendMessage", "endEvent");
        final DesignProcessDefinition designProcessDefinition = processBuilder.done();

        final ProcessDefinition sendMessageProcess = deployAndEnableProcess(designProcessDefinition);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(sendMessageProcess.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        return sendMessageProcess;
    }

    private void addProcessData(final Map<String, String> data, final ProcessDefinitionBuilder processBuilder) {
        if (data != null) {
            for (final Entry<String, String> entry : data.entrySet()) {
                processBuilder.addData(entry.getKey(), entry.getValue(), null);
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

    /*
     * 1 receiveProcess, 1 sendProcess, Message contains datas goes from SendTask to IntermediateEvent
     * dynamic -> deployAndEnable(sendProcess), deployAndEnable(receiveProcess), startProcess(sendProcess)
     * checks : receiveProcess start and stop on catchEvent, sendProcess is finished, , receiveProcess continues and reaches user task , data is transmitted to
     * the receiveProcess.
     */
    @Cover(classes = { EventInstance.class, IntermediateCatchEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message event",
            "Intermediate catch event", "Send", "Receive" }, story = "Send a message with data from an and event of a process  to an intermediate event of an other process.", jira = "")
    @Test
    public void dataTransferFromSendTaskToMessageIntermediateCatchEventWithTargetFlowNode() throws Exception {
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithEndMessageEvent("sendMessageProcess", "m14", "receiveMessageProcess",
                "waitForMessage", null, Collections.singletonMap("lastName", String.class.getName()),
                Collections.singletonMap("lName", String.class.getName()), Collections.singletonMap("lName", "lastName"));

        final List<Operation> catchMessageOperations = Collections.singletonList(buildAssignOperation("name", "lName", String.class.getName(),
                ExpressionType.TYPE_VARIABLE));
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithMessageIntermediateCatchEvent("receiveMessageProcess", "waitForMessage",
                "step1", "delivery", user, "m14", null, Collections.singletonMap("name", String.class.getName()), catchMessageOperations);

        // start a instance of a receive message process
        final ProcessInstance receiveMessageProcessInstance = getProcessAPI().startProcess(receiveMessageProcess.getId());

        // wait the event node instance
        waitForEventInWaitingState(receiveMessageProcessInstance, "waitForMessage");

        DataInstance dataInstance = getProcessAPI().getProcessDataInstance("name", receiveMessageProcessInstance.getId());
        assertNull("Data is not null", dataInstance.getValue());

        final ProcessInstance sendMessageProcessInstance = getProcessAPI().startProcess(sendMessageProcess.getId(),
                Arrays.asList(buildAssignOperation("lastName", "Doe", String.class.getName(), ExpressionType.TYPE_CONSTANT)), null);
        assertTrue(waitForProcessToFinishAndBeArchived(sendMessageProcessInstance));
        final List<ArchivedActivityInstance> archivedActivityInstances = getProcessAPI().getArchivedActivityInstances(sendMessageProcessInstance.getId(), 0,
                10, ActivityInstanceCriterion.LAST_UPDATE_DESC);
        assertTrue(archivedActivityInstances.get(0) instanceof ArchivedSendTaskInstance);
        forceMatchingOfEvents();
        waitForStep("step1", receiveMessageProcessInstance);

        dataInstance = getProcessAPI().getProcessDataInstance("name", receiveMessageProcessInstance.getId());
        assertEquals("Doe", dataInstance.getValue());

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);
    }

    /*
     * 1 receiveProcess, 1 sendProcess, Message contains datas goes from SendTask to IntermediateEvent
     * dynamic -> deployAndEnable(sendProcess), deployAndEnable(receiveProcess), startProcess(sendProcess)
     * checks : receiveProcess start and stop on catchEvent, sendProcess is finished, , receiveProcess continues and reaches user task , data is transmitted to
     * the receiveProcess.
     */
    @Cover(classes = { EventInstance.class, IntermediateCatchEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message event",
            "Intermediate catch event", "Send", "Receive" }, story = "Send a message with data from an and event of a process  to an intermediate event of an other process.", jira = "")
    @Test
    public void dataTransferFromSendTaskToMessageIntermediateCatchEvent() throws Exception {
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithEndMessageEvent("sendMessageProcess", "m14", "receiveMessageProcess", null,
                null, Collections.singletonMap("lastName", String.class.getName()), Collections.singletonMap("lName", String.class.getName()),
                Collections.singletonMap("lName", "lastName"));

        final List<Operation> catchMessageOperations = Collections.singletonList(buildAssignOperation("name", "lName", String.class.getName(),
                ExpressionType.TYPE_VARIABLE));
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithMessageIntermediateCatchEvent("receiveMessageProcess", "waitForMessage",
                "step1", "delivery", user, "m14", null, Collections.singletonMap("name", String.class.getName()), catchMessageOperations);

        // start a instance of a receive message process
        final ProcessInstance receiveMessageProcessInstance = getProcessAPI().startProcess(receiveMessageProcess.getId());

        // wait the event node instance
        waitForEventInWaitingState(receiveMessageProcessInstance, "waitForMessage");

        DataInstance dataInstance = getProcessAPI().getProcessDataInstance("name", receiveMessageProcessInstance.getId());
        assertNull("Data is not null", dataInstance.getValue());

        final ProcessInstance sendMessageProcessInstance = getProcessAPI().startProcess(sendMessageProcess.getId(),
                Arrays.asList(buildAssignOperation("lastName", "Doe", String.class.getName(), ExpressionType.TYPE_CONSTANT)), null);
        assertTrue(waitForProcessToFinishAndBeArchived(sendMessageProcessInstance));
        forceMatchingOfEvents();

        waitForStep("step1", receiveMessageProcessInstance);

        dataInstance = getProcessAPI().getProcessDataInstance("name", receiveMessageProcessInstance.getId());
        assertEquals("Doe", dataInstance.getValue());

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);
    }

    private ProcessDefinition deployAndEnableProcessWithMessageIntermediateCatchEvent(final String processName, final String intermediateCatchEventName,
            final String userTaskName, final String actorName, final User user, final String messageName,
            final List<BEntry<Expression, Expression>> correlations, final Map<String, String> processData, final List<Operation> operations)
            throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder();
        processBuilder.createNewInstance(processName, "1.0");
        addProcessData(processData, processBuilder);
        processBuilder.addStartEvent("startEvent");
        processBuilder.addAutomaticTask("auto1");
        final CatchMessageEventTriggerDefinitionBuilder catchMessageEventTriggerDefinitionBuilder = processBuilder.addIntermediateCatchEvent(
                intermediateCatchEventName).addMessageEventTrigger(messageName);
        addCorrelations(correlations, catchMessageEventTriggerDefinitionBuilder);
        addCatchMessageOperations(operations, catchMessageEventTriggerDefinitionBuilder);
        processBuilder.addActor(actorName);
        processBuilder.addUserTask(userTaskName, actorName);
        processBuilder.addEndEvent("endEvent");
        processBuilder.addTransition("startEvent", "auto1");
        processBuilder.addTransition("auto1", intermediateCatchEventName);
        processBuilder.addTransition(intermediateCatchEventName, userTaskName);
        processBuilder.addTransition(userTaskName, "endEvent");
        final DesignProcessDefinition designProcessDefinition = processBuilder.done();

        final BusinessArchiveBuilder archiveBuilder = new BusinessArchiveBuilder();
        archiveBuilder.createNewBusinessArchive().setProcessDefinition(designProcessDefinition);
        final BusinessArchive receiveMessaceArchive = archiveBuilder.done();
        final ProcessDefinition receiveMessageProcess = getProcessAPI().deploy(receiveMessaceArchive);

        final List<ActorInstance> actors = getProcessAPI().getActors(receiveMessageProcess.getId(), 0, 1, ActorCriterion.NAME_ASC);
        getProcessAPI().addUserToActor(actors.get(0).getId(), user.getId());

        getProcessAPI().enableProcess(receiveMessageProcess.getId());

        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(receiveMessageProcess.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        return receiveMessageProcess;
    }

    private void addCatchMessageOperations(final List<Operation> catchMessageOperations, final CatchMessageEventTriggerDefinitionBuilder messageEventTrigger) {
        if (catchMessageOperations != null) {
            for (final Operation operation : catchMessageOperations) {
                messageEventTrigger.addOperation(operation);
            }
        }
    }

    private void addCorrelations(final List<BEntry<Expression, Expression>> correlations,
            final CatchMessageEventTriggerDefinitionBuilder catchMessageEventTriggerDefinitionBuilder) {
        if (correlations != null) {
            for (final Entry<Expression, Expression> entry : correlations) {
                catchMessageEventTriggerDefinitionBuilder.addCorrelation(entry.getKey(), entry.getValue());
            }
        }
    }
}
