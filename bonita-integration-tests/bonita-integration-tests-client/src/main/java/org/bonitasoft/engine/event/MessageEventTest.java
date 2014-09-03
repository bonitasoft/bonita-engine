package org.bonitasoft.engine.event;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.ProcessRuntimeAPI;
import org.bonitasoft.engine.bpm.actor.ActorCriterion;
import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.xml.XMLProcessDefinition.BEntry;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.ActivityStates;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.EventInstance;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.IntermediateCatchEventInstance;
import org.bonitasoft.engine.bpm.flownode.IntermediateThrowEventInstance;
import org.bonitasoft.engine.bpm.flownode.SendEventException;
import org.bonitasoft.engine.bpm.flownode.ThrowEventInstance;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.CatchMessageEventTriggerDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.IntermediateThrowEventDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ThrowMessageEventTriggerBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionConstants;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.operation.LeftOperand;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.test.StartProcessUntilStep;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.bonitasoft.engine.test.check.CheckNbPendingTaskOf;
import org.bonitasoft.engine.test.wait.WaitForEvent;
import org.bonitasoft.engine.test.wait.WaitForStep;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class MessageEventTest extends CommonAPITest {

    private static final String MESSAGE = "message";

    private static final String START_WITH_MESSAGE_STEP1_NAME = "userStart1";

    private static final String START_WITH_MESSAGE_PROCESS_NAME = "Start from message";

    private static final String CATCH_EVENT_NAME = "waitForMessage";

    private static final String CATCH_MESSAGE_PROCESS_NAME = "Catch a message";

    private static final String CATCH_MESSAGE_STEP1_NAME = "step1";

    private static final String SEND_MESSAGE_PROCESS_NAME = "Send a message";

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

    private ProcessDefinition deployAndEnableProcessWithEndMessageEvent(final String targetProcess, final String targetFlowNode) throws BonitaException {
        return deployAndEnableProcessWithEndMessageEvent(targetProcess, targetFlowNode, null, null, null, null);
    }

    private ProcessDefinition deployAndEnableProcessWithEndMessageEventWithCorrelations(final String targetProcess, final String targetFlowNode,
            final List<BEntry<Expression, Expression>> correlations) throws BonitaException {
        return deployAndEnableProcessWithEndMessageEvent(targetProcess, targetFlowNode, correlations, null, null, null);
    }

    private ProcessDefinition deployAndEnableProcessWithEndMessageEvent(final String targetProcess, final String targetFlowNode,
            final List<BEntry<Expression, Expression>> correlations, final Map<String, String> processData, final Map<String, String> messageData,
            final Map<String, String> dataInputMapping) throws BonitaException {
        return deployAndEnableProcessWithEndMessageEvent("Send message in the end", MESSAGE, targetProcess, targetFlowNode, correlations, processData,
                messageData, dataInputMapping);
    }

    private ProcessDefinition deployAndEnableProcessWithEndMessageEvent(final String processName, final String messageName, final String targetProcess,
            final String targetFlowNode, final List<BEntry<Expression, Expression>> correlations, final Map<String, String> processData,
            final Map<String, String> messageData, final Map<String, String> dataInputMapping) throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder();
        processBuilder.createNewInstance(processName, PROCESS_VERSION);
        addProcessData(processData, processBuilder);
        processBuilder.addStartEvent("startEvent");
        processBuilder.addAutomaticTask("auto1");
        // create expression for target process/flowNode
        final Expression targetProcessExpression = new ExpressionBuilder().createConstantStringExpression(targetProcess);
        final ThrowMessageEventTriggerBuilder throwMessageEventTriggerBuilder;
        if (targetFlowNode != null) {
            final Expression targetFlowNodeExpression = new ExpressionBuilder().createConstantStringExpression(targetFlowNode);
            throwMessageEventTriggerBuilder = processBuilder.addEndEvent("endEvent").addMessageEventTrigger(messageName, targetProcessExpression,
                    targetFlowNodeExpression);
        } else {
            throwMessageEventTriggerBuilder = processBuilder.addEndEvent("endEvent").addMessageEventTrigger(messageName, targetProcessExpression);
        }

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

    private ProcessDefinition deployAndEnableProcessWithIntermediateThrowMessageEvent(final String targetProcess, final String targetFlowNode)
            throws BonitaException {
        return deployAndEnableProcessWithIntermediateThrowMessageEvent(Collections.singletonList(MESSAGE), Collections.singletonList(targetProcess),
                Collections.singletonList(targetFlowNode));
    }

    private ProcessDefinition deployAndEnableProcessWithIntermediateThrowMessageEvent(final List<String> messageNames, final List<String> targetProcesses,
            final List<String> targetFlowNodes) throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder();
        processBuilder.createNewInstance(SEND_MESSAGE_PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addStartEvent("startEvent");
        processBuilder.addAutomaticTask("auto1");

        final IntermediateThrowEventDefinitionBuilder intermediateThrowEvent = processBuilder.addIntermediateThrowEvent("sendMessage");
        for (final String targetProcess : targetProcesses) {
            // create expression for target process/flowNode
            final Expression targetProcessExpression = new ExpressionBuilder().createConstantStringExpression(targetProcess);
            final int indexOfTargetProcess = targetProcesses.indexOf(targetProcess);
            final Expression targetFlowNodeExpression = new ExpressionBuilder().createConstantStringExpression(targetFlowNodes.get(indexOfTargetProcess));
            intermediateThrowEvent.addMessageEventTrigger(messageNames.get(indexOfTargetProcess), targetProcessExpression, targetFlowNodeExpression);
        }
        processBuilder.addEndEvent("endEvent");
        processBuilder.addTransition("startEvent", "auto1");
        processBuilder.addTransition("auto1", "sendMessage");
        processBuilder.addTransition("sendMessage", "endEvent");
        final DesignProcessDefinition designProcessDefinition = processBuilder.done();

        final ProcessDefinition sendMessageProcess = deployAndEnableProcess(designProcessDefinition);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(sendMessageProcess.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        return sendMessageProcess;
    }

    protected ProcessDefinition deployAndEnableProcessWithStartMessageEvent(final Map<String, String> data, final List<Operation> catchMessageOperations)
            throws BonitaException {
        return deployAndEnableProcessWithStartMessageEvent(START_WITH_MESSAGE_PROCESS_NAME, MESSAGE, data, catchMessageOperations);
    }

    private ProcessDefinition deployAndEnableProcessWithStartMessageEvent(final String processName, final String messageName, final Map<String, String> data,
            final List<Operation> catchMessageOperations) throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder();
        processBuilder.createNewInstance(processName, PROCESS_VERSION);
        addProcessData(data, processBuilder);
        final CatchMessageEventTriggerDefinitionBuilder messageEventTrigger = processBuilder.addStartEvent("startEvent").addMessageEventTrigger(messageName);
        addCatchMessageOperations(catchMessageOperations, messageEventTrigger);
        processBuilder.addActor(ACTOR_NAME);
        processBuilder.addUserTask(START_WITH_MESSAGE_STEP1_NAME, ACTOR_NAME);
        processBuilder.addEndEvent("endEvent");
        processBuilder.addTransition("startEvent", START_WITH_MESSAGE_STEP1_NAME);
        processBuilder.addTransition(START_WITH_MESSAGE_STEP1_NAME, "endEvent");
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

    private ProcessDefinition deployAndEnableProcessWithMessageIntermediateCatchEvent(final List<BEntry<Expression, Expression>> correlations,
            final Map<String, String> processData, final List<Operation> operations) throws BonitaException {
        return deployAndEnableProcessWithMessageIntermediateCatchEvent(CATCH_MESSAGE_PROCESS_NAME, MESSAGE, correlations, processData, operations);
    }

    private ProcessDefinition deployAndEnableProcessWithMessageIntermediateCatchEvent(final String processName, final String messageName,
            final List<BEntry<Expression, Expression>> correlations, final Map<String, String> processData, final List<Operation> operations)
                    throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder();
        processBuilder.createNewInstance(processName, PROCESS_VERSION);
        addProcessData(processData, processBuilder);
        processBuilder.addStartEvent("startEvent");
        processBuilder.addAutomaticTask("auto1");
        final CatchMessageEventTriggerDefinitionBuilder catchMessageEventTriggerDefinitionBuilder = processBuilder.addIntermediateCatchEvent(CATCH_EVENT_NAME)
                .addMessageEventTrigger(messageName);
        addCorrelations(correlations, catchMessageEventTriggerDefinitionBuilder);
        addCatchMessageOperations(operations, catchMessageEventTriggerDefinitionBuilder);
        processBuilder.addActor(ACTOR_NAME);
        processBuilder.addUserTask(CATCH_MESSAGE_STEP1_NAME, ACTOR_NAME);
        processBuilder.addEndEvent("endEvent");
        processBuilder.addTransition("startEvent", "auto1");
        processBuilder.addTransition("auto1", CATCH_EVENT_NAME);
        processBuilder.addTransition(CATCH_EVENT_NAME, CATCH_MESSAGE_STEP1_NAME);
        processBuilder.addTransition(CATCH_MESSAGE_STEP1_NAME, "endEvent");
        final DesignProcessDefinition designProcessDefinition = processBuilder.done();

        final BusinessArchiveBuilder archiveBuilder = new BusinessArchiveBuilder();
        archiveBuilder.createNewBusinessArchive().setProcessDefinition(designProcessDefinition);
        final BusinessArchive receiveMessaceArchive = archiveBuilder.done();

        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithActor(receiveMessaceArchive, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(receiveMessageProcess.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        return receiveMessageProcess;
    }

    private void addCorrelations(final List<BEntry<Expression, Expression>> correlations,
            final CatchMessageEventTriggerDefinitionBuilder catchMessageEventTriggerDefinitionBuilder) {
        if (correlations != null) {
            for (final Entry<Expression, Expression> entry : correlations) {
                catchMessageEventTriggerDefinitionBuilder.addCorrelation(entry.getKey(), entry.getValue());
            }
        }
    }

    /*
     * 1 receiveProcess, 1 sendProcess, Message goes from EndEvent to StartEvent
     * dynamic -> deployAndEnable(sendProcess), deployAndEnable(receiveProcess), startProcess(sendProcess)
     * check receiveProcess has started and halt on the user task.
     */
    @Cover(classes = EventInstance.class, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message event", "Start event", "End event", "Send", "Receive" }, story = "Send a message from an end event of a process and receive it in a start event of an other process. (Step : deploy send process -> deploy receive process -> start send process )", jira = "")
    @Test
    public void messageStartEventMessageSentAfterEnable() throws Exception {
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithEndMessageEvent(START_WITH_MESSAGE_PROCESS_NAME, "startEvent");
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithStartMessageEvent(null, null);

        final ProcessInstance sendMessageProcessInstance = getProcessAPI().startProcess(sendMessageProcess.getId());
        assertTrue(waitForProcessToFinishAndBeArchived(sendMessageProcessInstance));
        forceMatchingOfEvents();
        waitForUserTask(START_WITH_MESSAGE_STEP1_NAME);

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);
    }

    /*
     * 1 receiveProcess, 1 sendProcess, Message goes from EndEvent to StartEvent
     * dynamic -> deployAndEnable(sendProcess), deployAndEnable(receiveProcess), startProcess(sendProcess)
     * check receiveProcess has started and halt on the user task.
     */
    @Cover(classes = EventInstance.class, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message event", "Start event", "End event", "Send", "Receive" }, story = "Send a message from an end event of a process and receive it in a start event of an other process. (Step : deploy send process -> deploy receive process -> start send process )", jira = "")
    @Test
    public void messageStartEventMessageSentAfterEnableWithNoTargetFlowNode() throws Exception {
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithEndMessageEvent(START_WITH_MESSAGE_PROCESS_NAME, null);
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithStartMessageEvent(null, null);

        final ProcessInstance sendMessageProcessInstance = getProcessAPI().startProcess(sendMessageProcess.getId());
        assertTrue(waitForProcessToFinishAndBeArchived(sendMessageProcessInstance));
        forceMatchingOfEvents();
        waitForUserTask(START_WITH_MESSAGE_STEP1_NAME);

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);
    }

    /*
     * Differs only by the dynamic with messageStartEventMessageSentAfterEnable (Before instead of After)
     * dynamic -> deployAndEnable(sendProcess), startProcess(sendProcess), deployAndEnable(receiveProcess)
     */
    @Cover(classes = EventInstance.class, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message event", "Start event", "End event", "Send", "Receive" }, story = "Send a message from an end event of a process and receive it in a start event of an other process. (Step : deploy send process -> start send process -> deploy receive process)", jira = "")
    @Test
    public void messageStartEventMessageSentBeforeEnable() throws Exception {
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithEndMessageEvent(START_WITH_MESSAGE_PROCESS_NAME, "startEvent");
        // the message will be send before the target process is deployed
        final ProcessInstance sendMessageProcessInstance = getProcessAPI().startProcess(sendMessageProcess.getId());
        assertTrue(waitForProcessToFinishAndBeArchived(sendMessageProcessInstance));

        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithStartMessageEvent(null, null);
        forceMatchingOfEvents();
        waitForUserTask(START_WITH_MESSAGE_STEP1_NAME);

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);
    }

    /*
     * 1 receiveProcess, 1 sendProcess, Message goes from EndEvent to IntermediateEvent
     * dynamic -> deployAndEnable(sendProcess), deployAndEnable(receiveProcess), startProcess(receiveProcess), startProcess(sendProcess)
     * checks : receiveProcess stop on catchEvent, sendProcess is finished, receiveProcess continue and reaches user task.
     */
    @Cover(classes = { EventInstance.class, IntermediateCatchEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message event",
            "Intermediate catch event", "End event", "Send", "Receive" }, story = "Send a message from an end event of a process, and receive it in an intermediate event of an other process. Start Sender after Receiver", jira = "ENGINE-1652")
    @Test
    public void messageIntermediateCatchEventMessageSentAfterCatch() throws Exception {
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithEndMessageEvent(CATCH_MESSAGE_PROCESS_NAME, CATCH_EVENT_NAME);
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithMessageIntermediateCatchEvent(null, null, null);

        final ProcessInstance receiveMessageProcessInstance = getProcessAPI().startProcess(receiveMessageProcess.getId());
        waitForEventInWaitingState(receiveMessageProcessInstance, CATCH_EVENT_NAME);

        final ProcessInstance sendMessageProcessInstance = getProcessAPI().startProcess(sendMessageProcess.getId());
        assertTrue(waitForProcessToFinishAndBeArchived(sendMessageProcessInstance));
        forceMatchingOfEvents();
        waitForUserTask(CATCH_MESSAGE_STEP1_NAME);

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);
    }

    @Cover(classes = { EventInstance.class, IntermediateCatchEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message event",
            "Intermediate catch event", "End event", "Send", "Receive" }, story = "Send a message from an end event of a process, and receive it in an intermediate event of an other process. Start Sender before Receiver", jira = "ENGINE-1652")
    @Test
    public void messageIntermediateCatchEventMessageSentBeforeCatch() throws Exception {
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithEndMessageEvent(CATCH_MESSAGE_PROCESS_NAME, CATCH_EVENT_NAME);
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithMessageIntermediateCatchEvent(null, null, null);
        final ProcessInstance sendMessageProcessInstance = getProcessAPI().startProcess(sendMessageProcess.getId());
        assertTrue(waitForProcessToFinishAndBeArchived(sendMessageProcessInstance));

        final ProcessInstance receiveMessageProcessInstance = getProcessAPI().startProcess(receiveMessageProcess.getId());
        Thread.sleep(100);// small sleep but don't wait for the event to be waiting, it might happen that that event is already matched at this point
        forceMatchingOfEvents();
        waitForUserTask(CATCH_MESSAGE_STEP1_NAME, receiveMessageProcessInstance);

        disableAndDeleteProcess(receiveMessageProcess);
        disableAndDeleteProcess(sendMessageProcess);
    }

    /*
     * Verify that correlation to determine the targeted process work well (Both receiveProcess are instance of the same processDefinition)
     * 2 receiveProcess, 2 sendProcess, sendProcess1[1, Doe] -> receiveProcess1[1], sendProcess2[2,Doe Doe] -> receiveProcess2[2]
     * checks : receiveProcesses stop on catchEvent, sendProcess1 is finished.
     */
    @Cover(classes = { EventInstance.class, IntermediateThrowEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message event",
            "Intermediate catch event", "Send", "Receive", "Several messages", "Correlation" }, story = "Check correlation, to determine the target process when sending a message, works well.", jira = "")
    @Test
    public void messageIntermediateCatchEventWithCorrelations() throws Exception {
        final Map<String, String> data = new HashMap<String, String>();
        data.put("docNumber", Integer.class.getName());
        data.put("lastName", String.class.getName());

        final ArrayList<BEntry<Expression, Expression>> correlations = new ArrayList<BEntry<Expression, Expression>>(1);
        final Expression docCorrelationKey = new ExpressionBuilder().createConstantStringExpression("docKey");
        final Expression docCorrelationValue = new ExpressionBuilder().createDataExpression("docNumber", Integer.class.getName());
        correlations.add(new BEntry<Expression, Expression>(docCorrelationKey, docCorrelationValue));

        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithEndMessageEvent(CATCH_MESSAGE_PROCESS_NAME, CATCH_EVENT_NAME, correlations,
                data, null, null);
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithMessageInterCatchEventAnd1Correlation();

        // start two instances of a receive message process
        final ProcessInstance receiveMessageProcessInstance1 = getProcessAPI().startProcess(receiveMessageProcess.getId(),
                Arrays.asList(buildAssignOperation("docRef", "1", Integer.class.getName(), ExpressionType.TYPE_CONSTANT)), null);
        final ProcessInstance receiveMessageProcessInstance2 = getProcessAPI().startProcess(receiveMessageProcess.getId(),
                Arrays.asList(buildAssignOperation("docRef", "2", Integer.class.getName(), ExpressionType.TYPE_CONSTANT)), null);

        // wait the event node instance
        waitForEvent(receiveMessageProcessInstance1, CATCH_EVENT_NAME, TestStates.getWaitingState());
        waitForEvent(receiveMessageProcessInstance2, CATCH_EVENT_NAME, TestStates.getWaitingState());

        // instantiate a process containing correlations matching with receiveMessageProcessInstance1
        final ProcessInstance sendMessageProcessInstance1 = getProcessAPI().startProcess(
                sendMessageProcess.getId(),
                Arrays.asList(buildAssignOperation("docNumber", "1", Integer.class.getName(), ExpressionType.TYPE_CONSTANT),
                        buildAssignOperation("lastName", "Doe", String.class.getName(), ExpressionType.TYPE_CONSTANT)), null);
        assertTrue(waitForProcessToFinishAndBeArchived(sendMessageProcessInstance1));
        forceMatchingOfEvents();
        assertNotNull(waitForUserTask(CATCH_MESSAGE_STEP1_NAME, receiveMessageProcessInstance1.getId()));
        waitForEventInWaitingState(receiveMessageProcessInstance2, CATCH_EVENT_NAME);

        // instantiate a process containing correlations matching with receiveMessageProcessInstance2
        final ProcessInstance sendMessageProcessInstance2 = getProcessAPI().startProcess(
                sendMessageProcess.getId(),
                Arrays.asList(buildAssignOperation("docNumber", "2", Integer.class.getName(), ExpressionType.TYPE_CONSTANT),
                        buildAssignOperation("lastName", "Doe Doe", String.class.getName(), ExpressionType.TYPE_CONSTANT)), null);
        assertTrue(waitForProcessToFinishAndBeArchived(sendMessageProcessInstance2));

        assertNotNull(waitForUserTask(CATCH_MESSAGE_STEP1_NAME, receiveMessageProcessInstance2.getId()));

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);

    }

    /*
     * Verify that if a sendProcess has for targets 2 instances of the same ProcessDefinition and no correlation key is defined
     * (equivalent to matching keys), exactly one of the receiveProcess catches the message.
     * 2 receiveProcess, 1 sendProcess, sendProcess1 -> receiveProcess1, sendProcess2 -> receiveProcess2[2]
     * checks : receiveProcesses stop on catchEvent, sendProcess1 is finished.
     */
    @Cover(classes = { EventInstance.class, IntermediateCatchEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message event",
            "Intermediate catch event", "Send", "Receive", "Correlation" }, story = "Verify that if a send process has for targets two instances of the same ProcessDefinition and no correlation key is defined (equivalent to matching keys), exactly one of the receive process catches the message.", jira = "")
    @Test
    public void messageIntermediateCatchEventWithoutCorrelations() throws Exception {
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithEndMessageEvent(CATCH_MESSAGE_PROCESS_NAME, CATCH_EVENT_NAME);
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithMessageIntermediateCatchEvent(null, null, null);

        // start two instances of a receive message process
        final ProcessInstance receiveMessageProcessInstance1 = getProcessAPI().startProcess(receiveMessageProcess.getId());
        final ProcessInstance receiveMessageProcessInstance2 = getProcessAPI().startProcess(receiveMessageProcess.getId());

        // wait the event node instance
        waitForEvent(receiveMessageProcessInstance1, CATCH_EVENT_NAME, TestStates.getWaitingState());
        waitForEvent(receiveMessageProcessInstance2, CATCH_EVENT_NAME, TestStates.getWaitingState());

        // instantiate a process containing whom the targetProcess is of the ProcessDefinition receiveMessageProcess
        final ProcessInstance sendMessageProcessInstance1 = getProcessAPI().startProcess(sendMessageProcess.getId());
        assertTrue(waitForProcessToFinishAndBeArchived(sendMessageProcessInstance1));
        forceMatchingOfEvents();

        final HumanTaskInstance waitForUserTask = waitForUserTask(CATCH_MESSAGE_STEP1_NAME);

        final long processInstance = waitForUserTask.getRootContainerId();
        assertTrue(processInstance == receiveMessageProcessInstance1.getId() || processInstance == receiveMessageProcessInstance2.getId());
        assertEquals(1, getProcessAPI().getPendingHumanTaskInstances(user.getId(), 0, 10, ActivityInstanceCriterion.DEFAULT).size());

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);
    }

    /*
     * Verify that even if matching correlations keys are not in the same order in the receiveProcess and sendProcess, the message is transmitted.
     * 1 receiveProcess [value1,value2], 1 sendProcesss[value1,value2], message goes from endEvent to intermediateEvent
     * checks : receiveProcess stop on catchEvent , sendProcess is finished, receiveProcess continues and reaches user task.
     */
    @Cover(classes = { EventInstance.class, IntermediateCatchEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message event",
            "Send", "Receive", "Correlation" }, story = "Verify that even if matching correlations keys are not in the same order in the receiveProcess and sendProcess, the message is transmitted.", jira = "")
    @Test
    public void correlationKeyInWrongOrderShouldWork() throws Exception {
        final ArrayList<BEntry<Expression, Expression>> correlations = new ArrayList<BEntry<Expression, Expression>>(2);
        correlations.add(new BEntry<Expression, Expression>(new ExpressionBuilder().createConstantStringExpression("aKey"), new ExpressionBuilder()
        .createConstantStringExpression("value1")));
        correlations.add(new BEntry<Expression, Expression>(new ExpressionBuilder().createConstantStringExpression("bKey"), new ExpressionBuilder()
        .createConstantStringExpression("value2")));

        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithEndMessageEventWithCorrelations(CATCH_MESSAGE_PROCESS_NAME, CATCH_EVENT_NAME,
                correlations);

        final ArrayList<BEntry<Expression, Expression>> correlationsReceive = new ArrayList<BEntry<Expression, Expression>>(2);
        correlationsReceive.add(new BEntry<Expression, Expression>(new ExpressionBuilder().createConstantStringExpression("bKey"), new ExpressionBuilder()
        .createConstantStringExpression("value2")));
        correlationsReceive.add(new BEntry<Expression, Expression>(new ExpressionBuilder().createConstantStringExpression("aKey"), new ExpressionBuilder()
        .createConstantStringExpression("value1")));

        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithMessageIntermediateCatchEvent(correlationsReceive, null, null);

        // start two instances of a receive message process
        final ProcessInstance receiveMessageProcessInstance1 = getProcessAPI().startProcess(receiveMessageProcess.getId());

        // wait the event node instance
        waitForEvent(receiveMessageProcessInstance1, CATCH_EVENT_NAME, TestStates.getWaitingState());

        // instantiate a process containing correlations matching with receiveMessageProcessInstance1
        final ProcessInstance sendMessageProcessInstance1 = getProcessAPI().startProcess(sendMessageProcess.getId());
        assertTrue(waitForProcessToFinishAndBeArchived(sendMessageProcessInstance1));
        forceMatchingOfEvents();

        waitForUserTask(CATCH_MESSAGE_STEP1_NAME, receiveMessageProcessInstance1);
        // waitForStep(100, 5000, "userTask1", receiveMessageProcessInstance1);

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);
    }

    /*
     * Verify that a sendProcess must have at least all correlation keys of the receiveProcess for the message to be transmitted.
     * 1 receiveProcess, 2 sendProcess, receiveProcess1, sendProcess1, sendProcess2
     * dynamic -> deployAndEnable(sendProcess), deployAndEnable(receiveProcess), startProcess(sendProcess)
     * checks : sendProcess is finished, , receiveProcess start and stop on user task, data is transmitted to the receiveProcess
     */
    @Cover(classes = { EventInstance.class, IntermediateCatchEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message event",
            "Intermediate catch event", "Send", "Receive", "Correlation" }, story = "Verify that a send process must have at least all correlation keys of the receive process for the message to be transmitted.", jira = "")
    @Test
    public void multipleCorrelationsKeys() throws Exception {
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithMessageEndEventAndCorrelation();
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithMessageInterCatchEventAnd2Correlations();

        // start a instance of a receive message process
        final ProcessInstance receiveMessageProcessInstance1 = getProcessAPI().startProcess(
                receiveMessageProcess.getId(),
                Arrays.asList(buildAssignOperation("docRef", "1", Integer.class.getName(), ExpressionType.TYPE_CONSTANT),
                        buildAssignOperation("name", "Doe Doe", String.class.getName(), ExpressionType.TYPE_CONSTANT)), null);

        // wait the event node instance
        waitForEvent(receiveMessageProcessInstance1, CATCH_EVENT_NAME, TestStates.getWaitingState());

        checkUserHasNoPendingTasks();

        // instantiate a process having one one correlation key matching, the process must not go further
        final ProcessInstance sendMessageProcessInstance1 = getProcessAPI().startProcess(
                sendMessageProcess.getId(),
                Arrays.asList(buildAssignOperation("docNumber", "1", Integer.class.getName(), ExpressionType.TYPE_CONSTANT),
                        buildAssignOperation("lastName", "Doe 2", String.class.getName(), ExpressionType.TYPE_CONSTANT)), null);
        assertTrue(waitForProcessToFinishAndBeArchived(sendMessageProcessInstance1));
        forceMatchingOfEvents();
        // 1 sec because it's an assert false and we forced matching of event
        assertFalse(new WaitForStep(DEFAULT_REPEAT_EACH, 500, CATCH_MESSAGE_STEP1_NAME, receiveMessageProcessInstance1.getId(), getProcessAPI()).waitUntil());

        // instantiate a process having both two correlation keys matching, the process must go further
        final ProcessInstance sendMessageProcessInstance2 = getProcessAPI().startProcess(
                sendMessageProcess.getId(),
                Arrays.asList(buildAssignOperation("docNumber", "1", Integer.class.getName(), ExpressionType.TYPE_CONSTANT),
                        buildAssignOperation("lastName", "Doe Doe", String.class.getName(), ExpressionType.TYPE_CONSTANT)), null);
        assertTrue(waitForProcessToFinishAndBeArchived(sendMessageProcessInstance2));
        forceMatchingOfEvents();
        waitForStep(CATCH_MESSAGE_STEP1_NAME, receiveMessageProcessInstance1);

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);
    }

    private ProcessDefinition deployAndEnableProcessWithMessageInterCatchEventAnd1Correlation() throws BonitaException {
        final Map<String, String> data = new HashMap<String, String>();
        data.put("docRef", Integer.class.getName());
        final ArrayList<BEntry<Expression, Expression>> correlations = new ArrayList<BEntry<Expression, Expression>>(1);
        final Expression docCorrelationKey = new ExpressionBuilder().createConstantStringExpression("docKey");
        final Expression docCorrelationValue = new ExpressionBuilder().createDataExpression("docRef", Integer.class.getName());
        correlations.add(new BEntry<Expression, Expression>(docCorrelationKey, docCorrelationValue));
        return deployAndEnableProcessWithMessageIntermediateCatchEvent(correlations, data, null);
    }

    private ProcessDefinition deployAndEnableProcessWithMessageInterCatchEventAnd2Correlations() throws BonitaException {
        final Map<String, String> data = new HashMap<String, String>();
        data.put("docRef", Integer.class.getName());
        data.put("name", String.class.getName());
        final Expression docCorrelationKey = new ExpressionBuilder().createConstantStringExpression("docKey");
        final Expression docCorrelationValue = new ExpressionBuilder().createDataExpression("docRef", Integer.class.getName());
        final Expression nameCorrelationKey = new ExpressionBuilder().createConstantStringExpression("nameKey");
        final Expression nameCorrelationValue = new ExpressionBuilder().createDataExpression("name", String.class.getName());
        final ArrayList<BEntry<Expression, Expression>> correlations = new ArrayList<BEntry<Expression, Expression>>(2);
        correlations.add(new BEntry<Expression, Expression>(docCorrelationKey, docCorrelationValue));
        correlations.add(new BEntry<Expression, Expression>(nameCorrelationKey, nameCorrelationValue));
        return deployAndEnableProcessWithMessageIntermediateCatchEvent(correlations, data, null);
    }

    private ProcessDefinition deployAndEnableProcessWithMessageEndEventAndCorrelation() throws BonitaException {
        final Map<String, String> data = new HashMap<String, String>();
        data.put("docNumber", Integer.class.getName());
        data.put("lastName", String.class.getName());

        final ArrayList<BEntry<Expression, Expression>> correlations = new ArrayList<BEntry<Expression, Expression>>(2);
        final Expression docCorrelationKey = new ExpressionBuilder().createConstantStringExpression("docKey");
        final Expression docCorrelationValue = new ExpressionBuilder().createDataExpression("docNumber", Integer.class.getName());
        final Expression nameCorrelationKey = new ExpressionBuilder().createConstantStringExpression("nameKey");
        final Expression nameCorrelationValue = new ExpressionBuilder().createDataExpression("lastName", String.class.getName());
        correlations.add(new BEntry<Expression, Expression>(docCorrelationKey, docCorrelationValue));
        correlations.add(new BEntry<Expression, Expression>(nameCorrelationKey, nameCorrelationValue));

        return deployAndEnableProcessWithEndMessageEvent(CATCH_MESSAGE_PROCESS_NAME, CATCH_EVENT_NAME, correlations, data, null, null);
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
     * 1 receiveProcess, 1 sendProcess, Message goes from IntermediateEvent to StartEvent
     * dynamic -> deployAndEnable(sendProcess), deployAndEnable(receiveProcess), startProcess(sendProcess)
     * checks : sendProcess is finished, receiveProcess start and stop on user task.
     */
    @Cover(classes = { EventInstance.class, ThrowEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message event",
            "Intermediate throw event", "Start event", "Send", "Receive" }, story = "Send a message from an intermediate throw event of a process and receive it in a start event of an other process.", jira = "")
    @Test
    public void messageIntermediateThrowEventMessageSentAfterEnable() throws Exception {
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithIntermediateThrowMessageEvent(START_WITH_MESSAGE_PROCESS_NAME, "startEvent");
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithStartMessageEvent(null, null);

        final ProcessInstance sendMessageProcessInstance = getProcessAPI().startProcess(sendMessageProcess.getId());
        assertTrue(waitForProcessToFinishAndBeArchived(sendMessageProcessInstance));
        forceMatchingOfEvents();
        waitForUserTask(START_WITH_MESSAGE_STEP1_NAME);

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);
    }

    @Cover(classes = { EventInstance.class, ThrowEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message event",
            "Intermediate throw event", "Start event", "Send", "Receive" }, story = "Send a message from an intermediate throw event of a process and receive it in a start event of an other process.", jira = "")
    @Test
    public void messageIntermediateThrow2EventMessages() throws Exception {
        final List<String> messages = new ArrayList<String>();
        messages.add("catchMessage");
        messages.add("startMessage");
        final List<String> targetProcesses = new ArrayList<String>();
        targetProcesses.add(CATCH_MESSAGE_PROCESS_NAME);
        targetProcesses.add(START_WITH_MESSAGE_PROCESS_NAME);
        final List<String> targetFlowNodes = new ArrayList<String>();
        targetFlowNodes.add(CATCH_EVENT_NAME);
        targetFlowNodes.add("startEvent");
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithIntermediateThrowMessageEvent(messages, targetProcesses, targetFlowNodes);

        final ProcessDefinition startWithMessageProcess = deployAndEnableProcessWithStartMessageEvent(START_WITH_MESSAGE_PROCESS_NAME, "startMessage", null,
                null);
        final ProcessDefinition catchMessageProcess = deployAndEnableProcessWithMessageIntermediateCatchEvent(CATCH_MESSAGE_PROCESS_NAME, "catchMessage", null,
                null, null);

        final ProcessInstance catchMessageProcessInstance = getProcessAPI().startProcess(catchMessageProcess.getId());
        waitForEventInWaitingState(catchMessageProcessInstance, CATCH_EVENT_NAME);

        final ProcessInstance sendMessageProcessInstance = getProcessAPI().startProcess(sendMessageProcess.getId());
        assertTrue(waitForProcessToFinishAndBeArchived(sendMessageProcessInstance));
        forceMatchingOfEvents();
        checkNbPendingTaskOf(2, user);

        final List<HumanTaskInstance> taskInstances = getProcessAPI().getPendingHumanTaskInstances(user.getId(), 0, 10, ActivityInstanceCriterion.NAME_ASC);
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
     * checks : sendProcess is finished, , receiveProcess start and stop on user task, data is transmitted to the receiveProcess
     */
    @Cover(classes = EventInstance.class, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message event", "Start event", "End event", " Message data",
        "Send", "Receive" }, story = "Send a message with data from an end event of a process to a start event of an other process.", jira = "")
    @Test
    public void dataTransferFromMessageEndEventToStartMessageEvent() throws Exception {
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithEndMessageEvent(START_WITH_MESSAGE_PROCESS_NAME, "startEvent", null,
                Collections.singletonMap("lastName", String.class.getName()), Collections.singletonMap("lName", String.class.getName()),
                Collections.singletonMap("lName", "lastName"));
        final List<Operation> catchMessageOperations = Collections.singletonList(buildAssignOperation("name", "lName", String.class.getName(),
                ExpressionType.TYPE_VARIABLE));
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithStartMessageEvent(Collections.singletonMap("name", String.class.getName()),
                catchMessageOperations);

        final ProcessInstance sendMessageProcessInstance = getProcessAPI().startProcess(sendMessageProcess.getId(),
                Arrays.asList(buildAssignOperation("lastName", "Doe", String.class.getName(), ExpressionType.TYPE_CONSTANT)), null);
        assertTrue(waitForProcessToFinishAndBeArchived(sendMessageProcessInstance));
        forceMatchingOfEvents();

        // at the first test some time the cron job time some time before executing
        final HumanTaskInstance userTask = waitForUserTask(START_WITH_MESSAGE_STEP1_NAME);

        final DataInstance dataInstance = getProcessAPI().getProcessDataInstance("name", userTask.getRootContainerId());
        assertEquals("Doe", dataInstance.getValue());

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);
    }

    /*
     * 1 receiveProcess, 1 sendProcess, Message contains datas goes from EndEvent to IntermediateEvent
     * dynamic -> deployAndEnable(sendProcess), deployAndEnable(receiveProcess), startProcess(sendProcess)
     * checks : receiveProcess start and stop on catchEvent, sendProcess is finished, , receiveProcess continues and reaches user task , data is transmitted to
     * the receiveProcess.
     */
    @Cover(classes = { EventInstance.class, IntermediateCatchEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message event",
            "Intermediate catch event", "Send", "Receive" }, story = "Send a message with data from an and event of a process  to an intermediate event of an other process.", jira = "")
    @Test
    public void dataTransferFromMessageEndEventToMessageIntermediateCatchEvent() throws Exception {
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithEndMessageEvent(CATCH_MESSAGE_PROCESS_NAME, CATCH_EVENT_NAME, null,
                Collections.singletonMap("lastName", String.class.getName()), Collections.singletonMap("lName", String.class.getName()),
                Collections.singletonMap("lName", "lastName"));

        final List<Operation> catchMessageOperations = Collections.singletonList(buildAssignOperation("name", "lName", String.class.getName(),
                ExpressionType.TYPE_VARIABLE));
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithMessageIntermediateCatchEvent(null,
                Collections.singletonMap("name", String.class.getName()), catchMessageOperations);

        // start a instance of a receive message process
        final ProcessInstance receiveMessageProcessInstance = getProcessAPI().startProcess(receiveMessageProcess.getId());

        // wait the event node instance
        waitForEventInWaitingState(receiveMessageProcessInstance, CATCH_EVENT_NAME);

        DataInstance dataInstance = getProcessAPI().getProcessDataInstance("name", receiveMessageProcessInstance.getId());
        assertNull("Data is not null", dataInstance.getValue());

        final ProcessInstance sendMessageProcessInstance = getProcessAPI().startProcess(sendMessageProcess.getId(),
                Arrays.asList(buildAssignOperation("lastName", "Doe", String.class.getName(), ExpressionType.TYPE_CONSTANT)), null);
        assertTrue(waitForProcessToFinishAndBeArchived(sendMessageProcessInstance));
        forceMatchingOfEvents();
        waitForUserTask(CATCH_MESSAGE_STEP1_NAME, receiveMessageProcessInstance);

        dataInstance = getProcessAPI().getProcessDataInstance("name", receiveMessageProcessInstance.getId());
        assertEquals("Doe", dataInstance.getValue());

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);
    }

    /*
     * Verify receiveProcess receive message targeting it, even if sent before its existence.
     * 1 receiveProcess, 1 sendProcess, Message goes from IntermediateEvent to IntermediateEvent
     * dynamic -> deployAndEnable(sendProcess), startProcess(sendProcess), deployAndEnable(receiveProcess), startProcess(receiveProcess)
     * checks : sendProcess is finished, receiveProcess reaches catchEvent and continue (found message sent by sendProcess) and reaches user task.
     */
    @Cover(classes = { EventInstance.class, IntermediateThrowEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message event",
            "Intermediate throw event", "Send", "Receive" }, story = "Verify receive process receive message targeting it, even if the message is sent before its existence.", jira = "")
    @Test
    public void messageSentProcessFinishBeforeReceiveProcessIsEnabled() throws Exception {
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithIntermediateThrowMessageEvent(CATCH_MESSAGE_PROCESS_NAME, CATCH_EVENT_NAME);
        final ProcessInstance sendMessageProcessInstance = getProcessAPI().startProcess(sendMessageProcess.getId());
        assertTrue(waitForProcessToFinishAndBeArchived(sendMessageProcessInstance));

        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithMessageIntermediateCatchEvent(null, null, null);

        final ProcessInstance receiveMessageProcessInstance = getProcessAPI().startProcess(receiveMessageProcess.getId());
        Thread.sleep(100);// small sleep but don't wait for the event to be waiting, it might happen that that event is already matched at this point
        forceMatchingOfEvents();
        waitForStep(CATCH_MESSAGE_STEP1_NAME, receiveMessageProcessInstance);

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);
    }

    private ProcessDefinition deployAndEnableProcessWithIntraMessageEvent(final String targetProcess, final String targetFlowNode) throws BonitaException {
        return deployAndEnableProcessWithIntraMessageEvent("sendAndReceiveMessageProcess", MESSAGE, targetProcess, targetFlowNode);
    }

    private ProcessDefinition deployAndEnableProcessWithIntraMessageEvent(final String processName, final String messageName, final String targetProcess,
            final String targetFlowNode) throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder();
        // create expression for target process/flowNode
        final Expression targetProcessExpression = new ExpressionBuilder().createConstantStringExpression(targetProcess);
        final Expression targetFlowNodeExpression = new ExpressionBuilder().createConstantStringExpression(targetFlowNode);
        processBuilder.createNewInstance(processName, PROCESS_VERSION);
        processBuilder.addStartEvent("startEvent");
        processBuilder.addAutomaticTask("auto1");
        processBuilder.addGateway("gateway1", GatewayType.PARALLEL);
        processBuilder.addIntermediateThrowEvent("sendMessage").addMessageEventTrigger(messageName, targetProcessExpression, targetFlowNodeExpression);
        processBuilder.addIntermediateCatchEvent(targetFlowNode).addMessageEventTrigger(messageName);
        processBuilder.addGateway("gateway2", GatewayType.PARALLEL);
        processBuilder.addActor(ACTOR_NAME);
        processBuilder.addUserTask("userTask1", ACTOR_NAME).addUserTask("userTask2", ACTOR_NAME).addUserTask("userTask3", ACTOR_NAME);
        processBuilder.addEndEvent("endEvent");
        processBuilder.addTransition("startEvent", "auto1");
        processBuilder.addTransition("auto1", "gateway1");
        processBuilder.addTransition("gateway1", "userTask2");
        processBuilder.addTransition("userTask2", "sendMessage");
        processBuilder.addTransition("sendMessage", "gateway2");
        processBuilder.addTransition("gateway1", "userTask1");
        processBuilder.addTransition("userTask1", "gateway2");
        processBuilder.addTransition("gateway2", "endEvent");
        processBuilder.addTransition("auto1", targetFlowNode);
        processBuilder.addTransition(targetFlowNode, "userTask3");
        processBuilder.addTransition("userTask3", "endEvent");
        final DesignProcessDefinition designProcessDefinition = processBuilder.done();

        final BusinessArchiveBuilder archiveBuilder = new BusinessArchiveBuilder();
        archiveBuilder.createNewBusinessArchive().setProcessDefinition(designProcessDefinition);
        final BusinessArchive sendAndReceiveMessaceArchive = archiveBuilder.done();
        final ProcessDefinition receiveMessageProcess = getProcessAPI().deploy(sendAndReceiveMessaceArchive);

        final List<ActorInstance> actors = getProcessAPI().getActors(receiveMessageProcess.getId(), 0, 1, ActorCriterion.NAME_ASC);
        getProcessAPI().addUserToActor(actors.get(0).getId(), user.getId());

        getProcessAPI().enableProcess(receiveMessageProcess.getId());

        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(receiveMessageProcess.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        return receiveMessageProcess;
    }

    /*
     * Test where the message goes from a throwEvent to a catchEvent belonging to the same process hence the same pool (forbidden by BPMN 2.0)
     * But the studio forbid this case, so this should never happen in the Engine.
     */
    @Cover(classes = { EventInstance.class, IntermediateCatchEventInstance.class, IntermediateThrowEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = {
            "Event", "Message event", "Throw event", "Catch event", "Send", "Receive" }, story = "Message goes from a throw event to a catch event belonging to the same process hence the same pool (forbidden by BPMN 2.0).", jira = "")
    @Test
    public void messageEventIntraProcess() throws Exception {
        final ProcessDefinition sendAndReceiveMessageProcess = deployAndEnableProcessWithIntraMessageEvent("sendAndReceiveMessageProcess", CATCH_EVENT_NAME);
        final ProcessInstance sendAndReceiveMessageProcessInstance = getProcessAPI().startProcess(sendAndReceiveMessageProcess.getId());

        final ActivityInstance step2 = waitForUserTask("userTask2", sendAndReceiveMessageProcessInstance);
        waitForEventInWaitingState(sendAndReceiveMessageProcessInstance, CATCH_EVENT_NAME);
        assignAndExecuteStep(step2.getId(), user.getId());
        forceMatchingOfEvents();
        waitForUserTask("userTask3", sendAndReceiveMessageProcessInstance);

        disableAndDeleteProcess(sendAndReceiveMessageProcess);
    }

    /*
     * 1 receiveProcess, 2 sendProcess, 2 Messages go from EndEvent to IntermediateEvent
     * dynamic -> deployAndEnable(sendProcess), deployAndEnable(receiveProcess), startProcess(sendProcess), startProcess(receiveProcess)
     * checks : sendProcess is finished, receiveProcess reaches catchEvent and continue (found message sent by sendProcess) and reaches user task.
     */
    @Cover(classes = { EventInstance.class, IntermediateCatchEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message event",
            "Intermediate catch event", "Send", "Receive" }, story = "Send a message from an end event to an intermediate event. Check send process is finished, receive process reaches catch event and continue.", jira = "")
    @Test
    public void messageIntermediateCatchEventMessageMultiSend() throws Exception {
        final ProcessDefinition sendMessageProcess1 = deployAndEnableProcessWithEndMessageEvent("sendMessageProcess1", MESSAGE, CATCH_MESSAGE_PROCESS_NAME,
                CATCH_EVENT_NAME, null, null, null, null);
        final ProcessDefinition sendMessageProcess2 = deployAndEnableProcessWithEndMessageEvent("sendMessageProcess2", MESSAGE, CATCH_MESSAGE_PROCESS_NAME,
                CATCH_EVENT_NAME, null, null, null, null);
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithMessageIntermediateCatchEvent(null, null, null);

        final ProcessInstance receiveMessageProcessInstance = getProcessAPI().startProcess(receiveMessageProcess.getId());

        waitForEventInWaitingState(receiveMessageProcessInstance, CATCH_EVENT_NAME);

        final ProcessInstance sendMessageProcessInstance1 = getProcessAPI().startProcess(sendMessageProcess1.getId());
        assertTrue(waitForProcessToFinishAndBeArchived(sendMessageProcessInstance1));
        forceMatchingOfEvents();
        waitForUserTask(CATCH_MESSAGE_STEP1_NAME);

        disableAndDeleteProcess(sendMessageProcess1);
        disableAndDeleteProcess(sendMessageProcess2);
        disableAndDeleteProcess(receiveMessageProcess);
    }

    @Cover(classes = { EventInstance.class, IntermediateCatchEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Message event",
            "Intermediate catch event", "Delete" }, story = "Check that delete process instance should delete waiting events.", jira = "")
    @Test
    public void deleteProcessInstanceShouldDeleteWaitingEvents() throws Exception {
        final ProcessDefinition processDefinition = deployAndEnableProcessWithMessageIntermediateCatchEvent(null, null, null);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        assumeNotNull(processInstance);

        waitForEventInWaitingState(processInstance, CATCH_EVENT_NAME);

        final long processInstanceId = processInstance.getId();
        getProcessAPI().deleteProcessInstance(processInstanceId);
        forceMatchingOfEvents();
        assertThat(new WaitForEvent(50, 1000, CATCH_EVENT_NAME, processInstanceId, getProcessAPI()).waitUntil(), is(false));

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { ProcessRuntimeAPI.class }, concept = BPMNConcept.EVENTS, keywords = { "message", "throw event", "send message", "start event" }, jira = "ENGINE-447")
    @Test
    public void sendMessageViaAPIToStartMessageEvent() throws Exception {
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithStartMessageEvent(null, null);

        // send message
        sendMessage(MESSAGE, START_WITH_MESSAGE_PROCESS_NAME, "startEvent", null);
        forceMatchingOfEvents();
        waitForUserTask(START_WITH_MESSAGE_STEP1_NAME);

        disableAndDeleteProcess(receiveMessageProcess);
    }

    protected void sendMessage(final String messageName, final String targetProcessName, final String targetFlowNodeName,
            final Map<Expression, Expression> messageContent) throws BonitaException {
        final Expression targetProcessExpression = new ExpressionBuilder().createConstantStringExpression(targetProcessName);
        final Expression targetFlowNodeExpression = new ExpressionBuilder().createConstantStringExpression(targetFlowNodeName);
        getProcessAPI().sendMessage(messageName, targetProcessExpression, targetFlowNodeExpression, messageContent);
    }

    private void sendMessage(final String messageName, final String targetProcessName, final String targetFlowNodeName,
            final Map<Expression, Expression> messageContent, final Map<Expression, Expression> correlations) throws BonitaException {
        final Expression targetProcessExpression = new ExpressionBuilder().createConstantStringExpression(targetProcessName);
        final Expression targetFlowNodeExpression = new ExpressionBuilder().createConstantStringExpression(targetFlowNodeName);
        getProcessAPI().sendMessage(messageName, targetProcessExpression, targetFlowNodeExpression, messageContent, correlations);
    }

    @Cover(classes = { ProcessRuntimeAPI.class }, concept = BPMNConcept.EVENTS, keywords = { "message", "throw event", "send message",
    "intermediate catch event" }, jira = "ENGINE-447")
    @Test
    public void sendMessageViaAPIToIntermediateMessageEvent() throws Exception {
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithMessageIntermediateCatchEvent(null, null, null);

        final ProcessInstance receiveMessageProcessInstance = getProcessAPI().startProcess(receiveMessageProcess.getId());
        waitForEventInWaitingState(receiveMessageProcessInstance, CATCH_EVENT_NAME);

        checkUserHasNoPendingTasks();

        // send message
        sendMessage(MESSAGE, CATCH_MESSAGE_PROCESS_NAME, CATCH_EVENT_NAME, null);
        forceMatchingOfEvents();
        waitForUserTask(CATCH_MESSAGE_STEP1_NAME);

        disableAndDeleteProcess(receiveMessageProcess);
    }

    @Cover(classes = { ProcessRuntimeAPI.class }, concept = BPMNConcept.EVENTS, keywords = { "message", "message content", "throw event", "send message",
    "start event" }, jira = "ENGINE-447")
    @Test
    public void sendMessageWithDataViaAPIToStartMessageEvent() throws Exception {
        final Expression lastNameDisplay = new ExpressionBuilder().createConstantStringExpression("lName");
        final Expression lastNameValue = new ExpressionBuilder().createConstantStringExpression("Doe");

        final List<Operation> catchMessageOperations = Collections.singletonList(buildAssignOperation("name", "lName", String.class.getName(),
                ExpressionType.TYPE_VARIABLE));
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithStartMessageEvent(Collections.singletonMap("name", String.class.getName()),
                catchMessageOperations);

        checkUserHasNoPendingTasks();

        sendMessage(MESSAGE, START_WITH_MESSAGE_PROCESS_NAME, "startEvent", Collections.singletonMap(lastNameDisplay, lastNameValue));
        forceMatchingOfEvents();
        // at the first test some time the cron job time some time before executing
        final CheckNbPendingTaskOf checkNbPendingTaskOf = new CheckNbPendingTaskOf(getProcessAPI(), 100, 11 * 1000, true, 1, user);
        assertTrue("there is no pending task", checkNbPendingTaskOf.waitUntil());

        final List<HumanTaskInstance> taskInstances = getProcessAPI().getPendingHumanTaskInstances(user.getId(), 0, 10, ActivityInstanceCriterion.NAME_ASC);
        assertEquals(1, taskInstances.size());
        final HumanTaskInstance taskInstance = taskInstances.get(0);
        assertEquals(START_WITH_MESSAGE_STEP1_NAME, taskInstance.getName());

        final DataInstance dataInstance = getProcessAPI().getProcessDataInstance("name", taskInstance.getRootContainerId());
        assertEquals("Doe", dataInstance.getValue());

        // Clean up
        disableAndDeleteProcess(receiveMessageProcess);
    }

    @Test
    public void sendMessageTwiceTriggersTwoStartMessageEvents() throws Exception {
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithStartMessageEvent(null, null);
        sendMessage(MESSAGE, START_WITH_MESSAGE_PROCESS_NAME, "startEvent", Collections.<Expression, Expression> emptyMap());
        forceMatchingOfEvents();
        ActivityInstance taskFirstProcInst = waitForUserTask(START_WITH_MESSAGE_STEP1_NAME);

        sendMessage(MESSAGE, START_WITH_MESSAGE_PROCESS_NAME, "startEvent", Collections.<Expression, Expression> emptyMap());
        forceMatchingOfEvents();
        ActivityInstance taskSecondProcInst = waitForUserTask(START_WITH_MESSAGE_STEP1_NAME);
        assertNotEquals(taskFirstProcInst.getId(), taskSecondProcInst.getId());
        assertNotEquals(taskFirstProcInst.getParentProcessInstanceId(), taskSecondProcInst.getParentProcessInstanceId());

        disableAndDeleteProcess(receiveMessageProcess);
    }

    private void checkUserHasNoPendingTasks() {
        final List<HumanTaskInstance> taskInstances = getProcessAPI().getPendingHumanTaskInstances(user.getId(), 0, 10, ActivityInstanceCriterion.NAME_ASC);
        assertEquals(0, taskInstances.size());
    }

    @Cover(classes = { ProcessRuntimeAPI.class }, concept = BPMNConcept.EVENTS, keywords = { "message", "correlation", "throw event", "send message",
    "intermediate catch event" }, jira = "ENGINE-447")
    @Test
    public void sendMessageWithCorrelationViaAPIToIntermediateMessageEvent() throws Exception {
        final Expression docCorrelationKey = new ExpressionBuilder().createConstantStringExpression("docKey");
        final Expression docCorrelationValue = new ExpressionBuilder().createConstantIntegerExpression(1);
        final Expression nameCorrelationKey = new ExpressionBuilder().createConstantStringExpression("nameKey");
        final Expression nameCorrelationValue1 = new ExpressionBuilder().createConstantStringExpression("Doe 2");
        final Expression nameCorrelationValue2 = new ExpressionBuilder().createConstantStringExpression("Doe Doe");

        final Map<Expression, Expression> correlations1 = new HashMap<Expression, Expression>(2);
        correlations1.put(docCorrelationKey, docCorrelationValue);
        correlations1.put(nameCorrelationKey, nameCorrelationValue1); // don't match

        final Map<Expression, Expression> correlations2 = new HashMap<Expression, Expression>(2);
        correlations2.put(docCorrelationKey, docCorrelationValue);
        correlations2.put(nameCorrelationKey, nameCorrelationValue2);

        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithMessageInterCatchEventAnd2Correlations();

        // start a instance of a receive message process
        final ProcessInstance receiveMessageProcessInstance1 = getProcessAPI().startProcess(
                receiveMessageProcess.getId(),
                Arrays.asList(buildAssignOperation("docRef", "1", Integer.class.getName(), ExpressionType.TYPE_CONSTANT),
                        buildAssignOperation("name", "Doe Doe", String.class.getName(), ExpressionType.TYPE_CONSTANT)), null);

        // wait the event node instance
        waitForEvent(receiveMessageProcessInstance1, CATCH_EVENT_NAME, TestStates.getWaitingState());

        // send a message having only one correlation key matching, the process must not go further
        sendMessage(MESSAGE, CATCH_MESSAGE_PROCESS_NAME, CATCH_EVENT_NAME, Collections.<Expression, Expression> emptyMap(), correlations1);
        forceMatchingOfEvents();
        assertFalse(new WaitForStep(50, 1000, "userTask1", receiveMessageProcessInstance1.getId(), getProcessAPI()).waitUntil());

        // send a message having both two correlations keys matching, the process must go further
        sendMessage(MESSAGE, CATCH_MESSAGE_PROCESS_NAME, CATCH_EVENT_NAME, Collections.<Expression, Expression> emptyMap(), correlations2);
        forceMatchingOfEvents();
        waitForStep(CATCH_MESSAGE_STEP1_NAME, receiveMessageProcessInstance1);

        disableAndDeleteProcess(receiveMessageProcess);
    }

    @Cover(classes = { ProcessRuntimeAPI.class }, concept = BPMNConcept.EVENTS, keywords = { "message", "correlation", "throw event", "send message",
    "intermediate catch event" }, jira = "ENGINE-447")
    @Test
    public void sendMessageWithDataViaAPIToIntermediateCatchMessageEvent() throws Exception {
        final List<Operation> catchMessageOperations = Collections.singletonList(buildAssignOperation("name", "lName", String.class.getName(),
                ExpressionType.TYPE_VARIABLE));
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithMessageIntermediateCatchEvent(null,
                Collections.singletonMap("name", String.class.getName()), catchMessageOperations);

        // start a instance of a receive message process
        final ProcessInstance receiveMessageProcessInstance = getProcessAPI().startProcess(receiveMessageProcess.getId());

        // wait the event node instance
        waitForEventInWaitingState(receiveMessageProcessInstance, CATCH_EVENT_NAME);

        DataInstance dataInstance = getProcessAPI().getProcessDataInstance("name", receiveMessageProcessInstance.getId());
        assertNull("Data is not null", dataInstance.getValue());

        final Expression lastNameDisplay = new ExpressionBuilder().createConstantStringExpression("lName");
        final Expression lastNameValue = new ExpressionBuilder().createConstantStringExpression("Doe");
        sendMessage(MESSAGE, CATCH_MESSAGE_PROCESS_NAME, CATCH_EVENT_NAME, Collections.singletonMap(lastNameDisplay, lastNameValue));
        forceMatchingOfEvents();
        waitForStep(CATCH_MESSAGE_STEP1_NAME, receiveMessageProcessInstance);

        dataInstance = getProcessAPI().getProcessDataInstance("name", receiveMessageProcessInstance.getId());
        assertEquals("Doe", dataInstance.getValue());

        disableAndDeleteProcess(receiveMessageProcess);
    }

    @Cover(classes = { ProcessRuntimeAPI.class }, concept = BPMNConcept.EVENTS, keywords = { "message", "too many correlation" }, jira = "ENGINE-447")
    @Test(expected = SendEventException.class)
    public void sendMessageWithTooManyCorrelations() throws Exception {
        final Map<Expression, Expression> correlations = new HashMap<Expression, Expression>(6);
        correlations.put(new ExpressionBuilder().createConstantStringExpression("key1"), new ExpressionBuilder().createConstantIntegerExpression(1));
        correlations.put(new ExpressionBuilder().createConstantStringExpression("key2"), new ExpressionBuilder().createConstantStringExpression("label"));
        correlations.put(new ExpressionBuilder().createConstantStringExpression("key3"), new ExpressionBuilder().createConstantStringExpression("2"));
        correlations.put(new ExpressionBuilder().createConstantStringExpression("key4"), new ExpressionBuilder().createConstantStringExpression("Doe 2"));
        correlations.put(new ExpressionBuilder().createConstantStringExpression("key5"), new ExpressionBuilder().createConstantStringExpression("Doe 2"));
        correlations.put(new ExpressionBuilder().createConstantStringExpression("key6"), new ExpressionBuilder().createConstantStringExpression("Doe 2"));

        final Expression targetProcessExpression = new ExpressionBuilder().createConstantStringExpression("p1");
        final Expression targetFlowNodeExpression = new ExpressionBuilder().createConstantStringExpression("step1");

        getProcessAPI().sendMessage(MESSAGE, targetProcessExpression, targetFlowNodeExpression, null, correlations);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.EVENTS, keywords = { "Message", "Loop", "Terminate", "Process" }, jira = "ENGINE-1723")
    @Test
    public void sendMessageToTerminateProcessWithLoop() throws Exception {
        ProcessDefinition processToKillDefinition = null;
        ProcessDefinition killerProcessDefinition = null;
        try {
            // Process to kill
            final Expression falseExpr = new ExpressionBuilder().createConstantBooleanExpression(false);
            final Expression dataExpr = new ExpressionBuilder().createDataExpression("endtask", Boolean.class.getName());
            final Expression condition = new ExpressionBuilder().createGroovyScriptExpression("check", "!endtask", Boolean.class.getName(), dataExpr);
            final Expression loopMax = new ExpressionBuilder().createConstantIntegerExpression(10);
            final ProcessDefinitionBuilder processToKillDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("ProcessToKill", PROCESS_VERSION);
            processToKillDefinitionBuilder.addActor(ACTOR_NAME).addStartEvent("ToKillStart").addEndEvent("ToKillEnd").addTerminateEventTrigger();
            processToKillDefinitionBuilder.addData("endtask", Boolean.class.getName(), falseExpr);
            processToKillDefinitionBuilder.addUserTask("Step1", ACTOR_NAME).addLoop(false, condition, loopMax);
            processToKillDefinitionBuilder.addUserTask("Step2", ACTOR_NAME);
            processToKillDefinitionBuilder.addGateway("Gateway", GatewayType.PARALLEL);
            // Catch Message Event
            final CatchMessageEventTriggerDefinitionBuilder catchMessageEventTriggerDefinitionBuilder = processToKillDefinitionBuilder
                    .addIntermediateCatchEvent(CATCH_EVENT_NAME).addMessageEventTrigger("msgKiller");
            final ArrayList<BEntry<Expression, Expression>> correlations = new ArrayList<BEntry<Expression, Expression>>(1);
            final Expression correlationKey = new ExpressionBuilder().createConstantStringExpression("key");
            final Expression correlationValue = new ExpressionBuilder().createGroovyScriptExpression("getId", "processInstanceId", Long.class.getName(),
                    new ExpressionBuilder().createEngineConstant(ExpressionConstants.PROCESS_INSTANCE_ID));
            correlations.add(new BEntry<Expression, Expression>(correlationKey, correlationValue));
            addCorrelations(correlations, catchMessageEventTriggerDefinitionBuilder);
            // Transitions
            processToKillDefinitionBuilder.addTransition("ToKillStart", "Gateway");
            processToKillDefinitionBuilder.addTransition("Gateway", "Step1");
            processToKillDefinitionBuilder.addTransition("Step1", "Step2");
            processToKillDefinitionBuilder.addTransition("Step2", "ToKillEnd");
            processToKillDefinitionBuilder.addTransition("Gateway", CATCH_EVENT_NAME);
            processToKillDefinitionBuilder.addTransition(CATCH_EVENT_NAME, "ToKillEnd");
            processToKillDefinition = deployAndEnableProcessWithActor(processToKillDefinitionBuilder.done(), ACTOR_NAME, user);

            final StartProcessUntilStep toKillStartProcessAndWaitForTask = startProcessAndWaitForTask(processToKillDefinition.getId(), "Step1");
            final ProcessInstance processToKillInstance = toKillStartProcessAndWaitForTask.getProcessInstance();

            // Killer process
            final ProcessDefinitionBuilder killerProcessDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("KillerProcess", PROCESS_VERSION);
            killerProcessDefinitionBuilder.addActor(ACTOR_NAME).addStartEvent("KillerStart");
            killerProcessDefinitionBuilder.addUserTask("Step3", ACTOR_NAME);
            // Throw Message Event
            final Expression targetProcess = new ExpressionBuilder().createConstantStringExpression("ProcessToKill");
            final Expression targetFlowNode = new ExpressionBuilder().createConstantStringExpression(CATCH_EVENT_NAME);
            final ThrowMessageEventTriggerBuilder throwMessageEventTriggerBuilder = killerProcessDefinitionBuilder.addEndEvent("KillerEnd")
                    .addMessageEventTrigger("msgKiller", targetProcess, targetFlowNode);
            final ArrayList<BEntry<Expression, Expression>> endCorrelations = new ArrayList<BEntry<Expression, Expression>>(1);
            final Expression endCorrelationKey = new ExpressionBuilder().createConstantStringExpression("key");
            final Expression endCorrelationValue = new ExpressionBuilder().createConstantLongExpression(processToKillInstance.getId());
            endCorrelations.add(new BEntry<Expression, Expression>(endCorrelationKey, endCorrelationValue));
            addCorrelations(endCorrelations, throwMessageEventTriggerBuilder);
            // Transitions
            killerProcessDefinitionBuilder.addTransition("KillerStart", "Step3");
            killerProcessDefinitionBuilder.addTransition("Step3", "KillerEnd");
            killerProcessDefinition = deployAndEnableProcessWithActor(killerProcessDefinitionBuilder.done(), ACTOR_NAME, user);

            final StartProcessUntilStep killerStartProcessAndWaitForTask = startProcessAndWaitForTask(killerProcessDefinition.getId(), "Step3");
            assignAndExecuteStep(killerStartProcessAndWaitForTask.getActivityInstance(), user.getId());
            waitForProcessToFinish(killerStartProcessAndWaitForTask.getProcessInstance());

            // Check that process to kill is terminated
            waitForProcessToFinish(processToKillInstance);
            final ArchivedActivityInstance step1 = getProcessAPI().getArchivedActivityInstance(toKillStartProcessAndWaitForTask.getActivityInstance().getId());
            assertEquals(ActivityStates.ABORTED_STATE, step1.getState());
        } finally {
            // Clean up
            disableAndDeleteProcess(processToKillDefinition, killerProcessDefinition);
        }
    }
}
