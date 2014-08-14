/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.InvalidBusinessArchiveFormatException;
import org.bonitasoft.engine.bpm.bar.ProcessDefinitionBARContribution;
import org.bonitasoft.engine.bpm.businessdata.BusinessDataDefinition;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinition;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.document.DocumentDefinition;
import org.bonitasoft.engine.bpm.flownode.ActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.CatchMessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.flownode.ThrowMessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.flownode.TransitionDefinition;
import org.bonitasoft.engine.bpm.flownode.UserTaskDefinition;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.CatchMessageEventTriggerDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ThrowMessageEventTriggerBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.junit.Test;

import com.bonitasoft.engine.bpm.bar.BusinessArchiveFactory;
import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;

/**
 * @author Baptiste Mesta
 */
public class BusinessArchiveTests {

    private static final String ASSIGN_OPERATOR = "=";

    private static boolean deleteDir(final File dir) {
        if (dir.isDirectory()) {
            final String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                final boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    @Test(expected = InvalidBusinessArchiveFormatException.class)
    public void invalidSPHashIsRejected() throws Exception {
        final File tempFolder = createTempFile();

        try {
            final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MySPProcess", "1.0");
            processDefinitionBuilder.addActor("BradPitt").addActor("Angelina").addAutomaticTask("a").addAutomaticTask("b").addTransition("a", "b")
                    .addGateway("MyGate", GatewayType.PARALLEL);
            final DesignProcessDefinition process = processDefinitionBuilder.done();
            final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process).done();
            com.bonitasoft.engine.bpm.bar.BusinessArchiveFactory.writeBusinessArchiveToFolder(businessArchive, tempFolder);
            org.bonitasoft.engine.bpm.bar.BusinessArchiveFactory.readBusinessArchive(tempFolder);
        } finally {
            deleteDir(tempFolder);
        }
    }

    private File createTempFile() throws IOException {
        final File tempFolder = File.createTempFile("businessArchive", "folder");
        tempFolder.delete();

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                if (tempFolder != null) {
                    deleteDir(tempFolder);
                }
            }
        });
        return tempFolder;
    }

    @Test
    public void validSPHashIsAccepted() throws Exception {
        final File tempFolder = createTempFile();

        try {
            final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("MySPProcess", "1.1");
            processDefinitionBuilder.addActor("BradPitt").addActor("Angelina").addAutomaticTask("a").addAutomaticTask("b").addTransition("a", "b")
                    .addGateway("MyGate", GatewayType.PARALLEL);
            processDefinitionBuilder.addParameter("Metre", String.class.getName());
            final DesignProcessDefinition process = processDefinitionBuilder.done();
            final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process).done();
            com.bonitasoft.engine.bpm.bar.BusinessArchiveFactory.writeBusinessArchiveToFolder(businessArchive, tempFolder);
            com.bonitasoft.engine.bpm.bar.BusinessArchiveFactory.readBusinessArchive(tempFolder);
        } finally {
            deleteDir(tempFolder);
        }
    }

    @Test
    public void validBOSHashIsAccepted() throws Exception {
        final File tempFolder = createTempFile();

        try {
            final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyBOSProcess", "1.0");
            processDefinitionBuilder.addActor("BradPitt").addActor("Angelina").addAutomaticTask("a").addAutomaticTask("b").addTransition("a", "b")
                    .addGateway("MyGate", GatewayType.PARALLEL);
            final DesignProcessDefinition process = processDefinitionBuilder.done();
            final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process).done();
            org.bonitasoft.engine.bpm.bar.BusinessArchiveFactory.writeBusinessArchiveToFolder(businessArchive, tempFolder);
            com.bonitasoft.engine.bpm.bar.BusinessArchiveFactory.readBusinessArchive(tempFolder);
        } finally {
            deleteDir(tempFolder);
        }
    }

    @Test
    public void readProcessFromBusinessArchive() throws Exception {
        final File barFile = File.createTempFile("businessArchive", ".bar");
        barFile.delete();

        final Expression trueExpression = new ExpressionBuilder().createConstantBooleanExpression(true);
        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("MyProcess", "1.0");
        processDefinitionBuilder.addDocumentDefinition("testDoc").addContentFileName("testFile.txt").addFile("testFile.txt").addDescription("desc")
                .addMimeType("text/plain");
        processDefinitionBuilder.addDocumentDefinition("testDocUrl").addContentFileName("testFile.txt").addUrl("http://test.com/testFile.txt")
                .addDescription("desc");
        processDefinitionBuilder.addDescription("a very good description");
        processDefinitionBuilder.addDisplayDescription("A very good and clean description that will be displayed in user xp");
        processDefinitionBuilder.addDisplayName("Truck Handling Process");
        processDefinitionBuilder.addBusinessData(
                "myLeaveRequest",
                "org.bonitasoft.hr.LeaveRequest",
                new ExpressionBuilder().createGroovyScriptExpression("leaveRequestGroovy", "return new org.bonitasoft.hr.LeaveRequest(\"toto\", 17L);",
                        "org.bonitasoft.hr.LeaveRequest")).addDescription("This is the leave request I will handle in the current process");
        processDefinitionBuilder.addActor("Truck Driver").addDescription("A man that is driving bigs trucks");
        processDefinitionBuilder.addStartEvent("start1").addTimerEventTriggerDefinition(TimerType.CYCLE,
                new ExpressionBuilder().createConstantStringExpression("*/3 * * * * ?"));
        // No Java operation, so empty string passed:
        processDefinitionBuilder
                .addAutomaticTask("auto1")
                .addOperation(new LeftOperandBuilder().createNewInstance().setName("testData").done(), OperatorType.ASSIGNMENT, ASSIGN_OPERATOR, null,
                        trueExpression).addConnector("conn1", "connId1", "1.0.0", ConnectorEvent.ON_FINISH);
        processDefinitionBuilder
                .addManualTask("manual1", "Truck Driver")
                .addPriority("urgent")
                .addExpectedDuration(5000000)
                .addDescription("description of manual task1")
                .addDisplayDescription(
                        new ExpressionBuilder().createConstantStringExpression("this is an urgent task that will take more than one hour to be done"))
                .addDisplayName(new ExpressionBuilder().createConstantStringExpression("Urgent task"))
                .addDisplayDescriptionAfterCompletion(new ExpressionBuilder().createConstantStringExpression("this is a done task that was urgent"));
        processDefinitionBuilder.addIntermediateCatchEvent("intermediateTimerEvent").addTimerEventTriggerDefinition(TimerType.DURATION,
                new ExpressionBuilder().createConstantLongExpression(1000));
        final UserTaskDefinitionBuilder addUserTask = processDefinitionBuilder.addUserTask("user1", "Truck Driver");
        addUserTask.addConnector("conn2", "connId2", "1.0.0", ConnectorEvent.ON_ENTER);
        addUserTask.addUserFilter("myUserFilter", "org.bonitasoft.test.user.filter", "1.0.0");
        addUserTask.addData("testData", String.class.getName(), null);
        processDefinitionBuilder.addGateway("gate1", GatewayType.INCLUSIVE).addDefaultTransition("user1");
        processDefinitionBuilder.addEndEvent("end1");
        processDefinitionBuilder.addTransition("start1", "auto1", trueExpression);
        processDefinitionBuilder.addTransition("auto1", "intermediateTimerEvent");
        processDefinitionBuilder.addTransition("intermediateTimerEvent", "user1");
        processDefinitionBuilder.addTransition("user1", "gate1");
        processDefinitionBuilder.addTransition("user1", "end1");
        processDefinitionBuilder
                .addConnector("conn3", "connId3", "1.0.0", ConnectorEvent.ON_FINISH)
                .addInput("input1", trueExpression)
                .addOutput(new LeftOperandBuilder().createNewInstance().setName("testData").done(), OperatorType.ASSIGNMENT, ASSIGN_OPERATOR, null,
                        trueExpression);
        processDefinitionBuilder.addParameter("myParam", String.class.getName()).addDescription("an important parameter");
        processDefinitionBuilder.addData("myData", "java.lang.Boolean", trueExpression).addDescription("My boolean data");
        final DesignProcessDefinition process = processDefinitionBuilder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process)
                .addDocumentResource(new BarResource("testFile.txt", new byte[] { 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 })).done();
        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, barFile);

        final InputStream inputStream = new FileInputStream(barFile);
        final BusinessArchive businessArchive2 = BusinessArchiveFactory.readBusinessArchive(inputStream);
        inputStream.close();
        final DesignProcessDefinition result = businessArchive2.getProcessDefinition();

        final List<DocumentDefinition> documentDefinitions = result.getProcessContainer().getDocumentDefinitions();
        final DocumentDefinition testDoc1 = documentDefinitions.get(0);
        assertEquals("testDoc", testDoc1.getName());
        assertEquals("desc", testDoc1.getDescription());
        assertEquals("testFile.txt", testDoc1.getFile());
        assertEquals("text/plain", testDoc1.getContentMimeType());
        final DocumentDefinition testDoc2 = documentDefinitions.get(1);
        assertEquals("testDocUrl", testDoc2.getName());
        assertEquals("desc", testDoc2.getDescription());
        assertEquals("http://test.com/testFile.txt", testDoc2.getUrl());
        assertEquals("application/octet-stream", testDoc2.getContentMimeType());
        assertEquals(process.getName(), result.getName());
        assertEquals(process.getVersion(), result.getVersion());
        assertEquals(process.getDescription(), result.getDescription());
        assertEquals(process.getDisplayName(), result.getDisplayName());
        assertEquals(process.getDisplayDescription(), result.getDisplayDescription());
        assertEquals(process.getProcessContainer().getStartEvents().size(), result.getProcessContainer().getStartEvents().size());
        assertEquals(process.getProcessContainer().getStartEvents().get(0), result.getProcessContainer().getStartEvents().get(0));
        assertEquals(process.getProcessContainer().getIntermediateCatchEvents().size(), result.getProcessContainer().getIntermediateCatchEvents().size());
        assertEquals(process.getProcessContainer().getIntermediateCatchEvents().get(0), result.getProcessContainer().getIntermediateCatchEvents().get(0));
        assertEquals(process.getProcessContainer().getEndEvents().size(), result.getProcessContainer().getEndEvents().size());
        assertEquals(process.getProcessContainer().getEndEvents().get(0), result.getProcessContainer().getEndEvents().get(0));
        assertEquals(process.getActors().size(), result.getActors().size());
        assertEquals(process.getActors().iterator().next(), result.getActors().iterator().next());
        assertEquals(process.getProcessContainer().getActivities().size(), result.getProcessContainer().getActivities().size());
        final Iterator<ActivityDefinition> iterator = result.getProcessContainer().getActivities().iterator();
        final Iterator<ActivityDefinition> iterator2 = process.getProcessContainer().getActivities().iterator();
        final ActivityDefinition procAct1 = iterator2.next();
        final ActivityDefinition procAct2 = iterator2.next();
        final ActivityDefinition procAct3 = iterator2.next();
        ActivityDefinition orgAuto1 = null;
        ActivityDefinition orgUser1 = null;
        ActivityDefinition orgManual1 = null;
        List<ActivityDefinition> asList = Arrays.asList(procAct1, procAct2, procAct3);
        for (final ActivityDefinition activityDefinition : asList) {
            if (activityDefinition.getName().equals("auto1")) {
                orgAuto1 = activityDefinition;
            } else if (activityDefinition.getName().equals("user1")) {
                orgUser1 = activityDefinition;
            } else if (activityDefinition.getName().equals("manual1")) {
                orgManual1 = activityDefinition;
            }
        }
        final ActivityDefinition resAct1 = iterator.next();
        final ActivityDefinition resAct2 = iterator.next();
        final ActivityDefinition resAct3 = iterator.next();
        ActivityDefinition auto1 = null;
        ActivityDefinition user1 = null;
        ActivityDefinition manual1 = null;
        asList = Arrays.asList(resAct1, resAct2, resAct3);
        for (final ActivityDefinition activityDefinition : asList) {
            if (activityDefinition.getName().equals("auto1")) {
                auto1 = activityDefinition;
            } else if (activityDefinition.getName().equals("user1")) {
                user1 = activityDefinition;
            } else if (activityDefinition.getName().equals("manual1")) {
                manual1 = activityDefinition;
            }
        }
        assertNotNull("user task not found", user1);
        assertNotNull("auto task not found", auto1);
        assertNotNull("manual task not found", manual1);
        assertEquals("user task not same", orgUser1, user1);
        assertEquals("auto task not same", orgAuto1, auto1);
        assertEquals("manual task not same", orgManual1, manual1);
        assertEquals(1, user1.getDataDefinitions().size());
        assertEquals(1, auto1.getOperations().size());
        assertEquals(auto1.getOperations().get(0), auto1.getOperations().get(0));
        assertTrue(procAct2.equals(resAct1) || procAct2.equals(resAct2) || procAct2.equals(resAct3));
        assertEquals(process.getProcessContainer().getGateways().size(), result.getProcessContainer().getGateways().size());
        assertEquals(process.getProcessContainer().getGateways().iterator().next(), result.getProcessContainer().getGateways().iterator().next());
        assertEquals(process.getProcessContainer().getTransitions().size(), result.getProcessContainer().getTransitions().size());
        boolean trWithConditionOk = false;
        final long startId = result.getProcessContainer().getFlowNode("start1").getId();
        for (final TransitionDefinition transition : result.getProcessContainer().getTransitions()) {
            if (startId == transition.getSource() && trueExpression.equals(transition.getCondition())) {
                trWithConditionOk = true;
                break;
            }
        }
        assertTrue("the condition on the transition was not kept", trWithConditionOk);

        // Business Data:
        List<BusinessDataDefinition> designedBusinessData = process.getProcessContainer().getBusinessDataDefinitions();
        List<BusinessDataDefinition> retrievedbusinessData = result.getProcessContainer().getBusinessDataDefinitions();
        assertEquals(designedBusinessData.size(), retrievedbusinessData.size());
        for (int i = 0; i < designedBusinessData.size(); i++) {
            assertEquals(designedBusinessData.get(i), retrievedbusinessData.get(i));
        }

        assertEquals(process.getProcessContainer().getConnectors().size(), result.getProcessContainer().getConnectors().size());
        boolean connectorWithInputOutputOk = false;
        for (final ConnectorDefinition connector : result.getProcessContainer().getConnectors()) {
            final Operation operation = connector.getOutputs().get(0);
            if ("conn3".equals(connector.getName()) && trueExpression.equals(connector.getInputs().get("input1"))
                    && "testData".equals(operation.getLeftOperand().getName()) && OperatorType.ASSIGNMENT.equals(operation.getType())
                    && ASSIGN_OPERATOR.equals(operation.getOperator()) && trueExpression.equals(operation.getRightOperand())) {
                connectorWithInputOutputOk = true;
                break;
            }
        }
        assertTrue("the input/output on the connector was not kept", connectorWithInputOutputOk);
        assertEquals(process.getProcessContainer().getConnectors().iterator().next(), result.getProcessContainer().getConnectors().iterator().next());
        assertEquals(process.getParameters().size(), result.getParameters().size());
        assertEquals(process.getParameters().iterator().next(), result.getParameters().iterator().next());
        assertEquals(process.getProcessContainer().getDataDefinitions().size(), result.getProcessContainer().getDataDefinitions().size());
        assertEquals(process.getProcessContainer().getDataDefinitions().iterator().next(), result.getProcessContainer().getDataDefinitions().iterator().next());
        final ActivityDefinition sourceActivity = process.getProcessContainer().getActivity("user1");
        final ActivityDefinition resultActivity = result.getProcessContainer().getActivity("user1");
        assertTrue(resultActivity instanceof UserTaskDefinition);
        assertEquals(((UserTaskDefinition) sourceActivity).getUserFilter(), ((UserTaskDefinition) resultActivity).getUserFilter());
        barFile.delete();
    }

    @Test
    public void readParametersFromBusinessArchive() throws Exception {
        final File barFile = File.createTempFile("businessArchive", ".bar");
        barFile.delete();

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("MyProcess", "1.0");
        final DesignProcessDefinition process = processDefinitionBuilder.done();

        final HashMap<String, String> initialParameters = new HashMap<String, String>();
        initialParameters.put("myKey1", "myValue1");
        initialParameters.put("myKey2", "myValue2");
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process)
                .setParameters(initialParameters).setActorMapping(null).done();
        com.bonitasoft.engine.bpm.bar.BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, barFile);

        final InputStream inputStream = new FileInputStream(barFile);
        final BusinessArchive businessArchive2 = com.bonitasoft.engine.bpm.bar.BusinessArchiveFactory.readBusinessArchive(inputStream);
        inputStream.close();
        final Map<String, String> result = businessArchive2.getParameters();

        assertEquals(initialParameters, result);
        barFile.delete();
    }

    @Test
    public void readProcessWithMessageEventsFromBusinessArchive() throws Exception {
        final File barFile = File.createTempFile("businessArchive", ".bar");
        barFile.delete();

        final Expression conditionKey = new ExpressionBuilder().createConstantStringExpression("coditionKey");

        final Expression trueExpression = new ExpressionBuilder().createConstantBooleanExpression(true);

        final Expression displayNameExpression = new ExpressionBuilder().createConstantStringExpression("dataToSend");

        final LeftOperandBuilder leftOperandBuilder = new LeftOperandBuilder();
        leftOperandBuilder.createNewInstance().setName("var1");
        final OperationBuilder opb = new OperationBuilder();
        opb.createNewInstance().setOperator(ASSIGN_OPERATOR).setRightOperand(trueExpression).setType(OperatorType.ASSIGNMENT)
                .setLeftOperand(leftOperandBuilder.done());

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("MyProcess", "1.0");
        processDefinitionBuilder.addBooleanData("var1", null);
        processDefinitionBuilder.addStartEvent("start1").addMessageEventTrigger("m1").addOperation(opb.done());
        processDefinitionBuilder.addAutomaticTask("auto1");
        final CatchMessageEventTriggerDefinitionBuilder catchMessageEventTriggerDefinitionBuilder = processDefinitionBuilder.addIntermediateCatchEvent(
                "waitForMessage").addMessageEventTrigger("m2");
        catchMessageEventTriggerDefinitionBuilder.addCorrelation(conditionKey, trueExpression);
        catchMessageEventTriggerDefinitionBuilder.addOperation(opb.done());
        // create expression for target process/flowNode
        Expression targetProcess = new ExpressionBuilder().createDataExpression("p3", String.class.getName());
        final Expression receiveMessage = new ExpressionBuilder().createDataExpression("receiveMessage", String.class.getName());
        processDefinitionBuilder.addIntermediateThrowEvent("sendMessage").addMessageEventTrigger("m4", targetProcess, receiveMessage);
        targetProcess = new ExpressionBuilder().createConstantStringExpression("p2");
        final Expression waitMessage = new ExpressionBuilder().createConstantStringExpression("waitMessage");
        final ThrowMessageEventTriggerBuilder throwMessageEventTriggerBuilder = processDefinitionBuilder.addEndEvent("end1").addMessageEventTrigger("m2",
                targetProcess, waitMessage);
        throwMessageEventTriggerBuilder.addCorrelation(conditionKey, trueExpression);
        throwMessageEventTriggerBuilder.addMessageContentExpression(displayNameExpression, trueExpression);
        processDefinitionBuilder.addTransition("start1", "auto1", trueExpression);
        processDefinitionBuilder.addTransition("auto1", "waitForMessage");
        processDefinitionBuilder.addTransition("waitForMessage", "sendMessage");
        processDefinitionBuilder.addTransition("sendMessage", "end1");

        final DesignProcessDefinition process = processDefinitionBuilder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process).done();
        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, barFile);

        final InputStream inputStream = new FileInputStream(barFile);
        final BusinessArchive businessArchive2 = BusinessArchiveFactory.readBusinessArchive(inputStream);
        inputStream.close();
        final DesignProcessDefinition result = businessArchive2.getProcessDefinition();

        checkProcessForMessagesEvents(process, result);

        barFile.delete();
    }

    private void checkProcessForMessagesEvents(final DesignProcessDefinition process, final DesignProcessDefinition result) {
        assertEquals(process.getName(), result.getName());
        assertEquals(process.getVersion(), result.getVersion());

        assertEquals(process.getProcessContainer().getStartEvents().size(), result.getProcessContainer().getStartEvents().size());
        assertEquals(1, result.getProcessContainer().getStartEvents().size());
        assertEquals(process.getProcessContainer().getStartEvents().get(0), result.getProcessContainer().getStartEvents().get(0));
        assertEquals(1, result.getProcessContainer().getStartEvents().get(0).getMessageEventTriggerDefinitions().size());
        assertEquals(process.getProcessContainer().getStartEvents().get(0).getMessageEventTriggerDefinitions().get(0).getOperations(), result
                .getProcessContainer().getStartEvents().get(0).getMessageEventTriggerDefinitions().get(0).getOperations());
        assertEquals(1, result.getProcessContainer().getStartEvents().get(0).getMessageEventTriggerDefinitions().get(0).getOperations().size());

        assertEquals(process.getProcessContainer().getIntermediateCatchEvents().size(), result.getProcessContainer().getIntermediateCatchEvents().size());
        assertEquals(1, result.getProcessContainer().getIntermediateCatchEvents().size());
        assertEquals(process.getProcessContainer().getIntermediateCatchEvents().get(0), result.getProcessContainer().getIntermediateCatchEvents().get(0));
        assertEquals(1, result.getProcessContainer().getIntermediateCatchEvents().get(0).getMessageEventTriggerDefinitions().size());

        final CatchMessageEventTriggerDefinition expectedCatchMessageEventTrigger = process.getProcessContainer().getIntermediateCatchEvents().get(0)
                .getMessageEventTriggerDefinitions().get(0);
        final CatchMessageEventTriggerDefinition actualCatchMessageEventTrigger = result.getProcessContainer().getIntermediateCatchEvents().get(0)
                .getMessageEventTriggerDefinitions().get(0);
        assertEquals(expectedCatchMessageEventTrigger.getCorrelations(), actualCatchMessageEventTrigger.getCorrelations());
        assertEquals(1, actualCatchMessageEventTrigger.getCorrelations().size());
        assertEquals(expectedCatchMessageEventTrigger.getOperations(), actualCatchMessageEventTrigger.getOperations());
        assertEquals(1, actualCatchMessageEventTrigger.getOperations().size());

        assertEquals(process.getProcessContainer().getIntermediateThrowEvents().size(), result.getProcessContainer().getIntermediateThrowEvents().size());
        assertEquals(1, result.getProcessContainer().getIntermediateThrowEvents().size());
        assertEquals(process.getProcessContainer().getIntermediateThrowEvents().get(0), result.getProcessContainer().getIntermediateThrowEvents().get(0));
        assertEquals(1, result.getProcessContainer().getIntermediateThrowEvents().get(0).getMessageEventTriggerDefinitions().size());

        assertEquals(process.getProcessContainer().getEndEvents().size(), result.getProcessContainer().getEndEvents().size());
        assertEquals(1, result.getProcessContainer().getEndEvents().size());
        assertEquals(process.getProcessContainer().getEndEvents().get(0), result.getProcessContainer().getEndEvents().get(0));
        assertEquals(1, result.getProcessContainer().getEndEvents().get(0).getMessageEventTriggerDefinitions().size());

        final ThrowMessageEventTriggerDefinition actualThrowMessage = result.getProcessContainer().getEndEvents().get(0).getMessageEventTriggerDefinitions()
                .get(0);
        final ThrowMessageEventTriggerDefinition expectedThrowMessage = process.getProcessContainer().getEndEvents().get(0).getMessageEventTriggerDefinitions()
                .get(0);
        assertEquals(expectedThrowMessage.getCorrelations(), actualThrowMessage.getCorrelations());
        assertEquals(1, actualThrowMessage.getCorrelations().size());
        assertEquals(expectedThrowMessage.getDataDefinitions(), actualThrowMessage.getDataDefinitions());
        assertEquals(1, actualThrowMessage.getDataDefinitions().size());

        assertEquals(process.getActors().size(), result.getActors().size());
        assertEquals(process.getProcessContainer().getActivities().size(), result.getProcessContainer().getActivities().size());
        assertEquals(process.getProcessContainer().getGateways().size(), result.getProcessContainer().getGateways().size());
        assertEquals(process.getProcessContainer().getTransitions().size(), result.getProcessContainer().getTransitions().size());
        assertEquals(process.getParameters().size(), result.getParameters().size());
        assertEquals(process.getProcessContainer().getDataDefinitions().size(), result.getProcessContainer().getDataDefinitions().size());
    }

    @Test
    public void readProcessWithActorWithoutDescription() throws Exception {
        final File barFile = File.createTempFile("businessArchive", ".bar");
        barFile.delete();

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("MyProcessTT", "1.0");
        processDefinitionBuilder.addActor("Truck Driver");
        processDefinitionBuilder.addStartEvent("start1");
        processDefinitionBuilder.addAutomaticTask("auto1").addConnector("conn1", "connId1", "1.0.0", ConnectorEvent.ON_FINISH);
        processDefinitionBuilder.addUserTask("user1", "Truck Driver").addConnector("conn2", "1.0.0", "connId2", ConnectorEvent.ON_ENTER);
        processDefinitionBuilder.addGateway("gate1", GatewayType.INCLUSIVE).addDefaultTransition("user1");
        processDefinitionBuilder.addEndEvent("end1");
        processDefinitionBuilder.addTransition("start1", "auto1");
        processDefinitionBuilder.addTransition("auto1", "user1");
        processDefinitionBuilder.addTransition("user1", "gate1");
        processDefinitionBuilder.addTransition("user1", "end1");
        processDefinitionBuilder.addConnector("conn3", "connId3", "1.0.0", ConnectorEvent.ON_FINISH);
        processDefinitionBuilder.addParameter("myParam", String.class.getName()).addDescription("an important parameter");

        final DesignProcessDefinition process = processDefinitionBuilder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process).done();
        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, barFile);

        final InputStream inputStream = new FileInputStream(barFile);
        final BusinessArchive businessArchive2 = BusinessArchiveFactory.readBusinessArchive(inputStream);
        inputStream.close();
        final DesignProcessDefinition result = businessArchive2.getProcessDefinition();

        assertEquals(process.getName(), result.getName());
        assertEquals(process.getVersion(), result.getVersion());
        assertEquals(process.getProcessContainer().getStartEvents().size(), result.getProcessContainer().getStartEvents().size());
        assertEquals(process.getProcessContainer().getStartEvents().get(0), result.getProcessContainer().getStartEvents().get(0));
        assertEquals(process.getProcessContainer().getEndEvents().size(), result.getProcessContainer().getEndEvents().size());
        assertEquals(process.getProcessContainer().getEndEvents().get(0), result.getProcessContainer().getEndEvents().get(0));
        assertEquals(process.getActors().size(), result.getActors().size());
        assertEquals(process.getActors().iterator().next(), result.getActors().iterator().next());
        assertEquals(process.getProcessContainer().getActivities().size(), result.getProcessContainer().getActivities().size());
        final Iterator<ActivityDefinition> iterator = result.getProcessContainer().getActivities().iterator();
        final Iterator<ActivityDefinition> iterator2 = process.getProcessContainer().getActivities().iterator();
        final ActivityDefinition procAct1 = iterator2.next();
        final ActivityDefinition procAct2 = iterator2.next();
        final ActivityDefinition resAct1 = iterator.next();
        final ActivityDefinition resAct2 = iterator.next();
        assertTrue(procAct1.equals(resAct1) || procAct1.equals(resAct2));
        assertTrue(procAct2.equals(resAct1) || procAct2.equals(resAct2));
        assertEquals(process.getProcessContainer().getGateways().size(), result.getProcessContainer().getGateways().size());
        assertEquals(process.getProcessContainer().getGateways().iterator().next(), result.getProcessContainer().getGateways().iterator().next());
        assertEquals(process.getProcessContainer().getTransitions().size(), result.getProcessContainer().getTransitions().size());
        assertEquals(process.getProcessContainer().getConnectors().size(), result.getProcessContainer().getConnectors().size());
        assertEquals(process.getProcessContainer().getConnectors().iterator().next(), result.getProcessContainer().getConnectors().iterator().next());
        assertEquals(process.getParameters().size(), result.getParameters().size());
        assertEquals(process.getParameters().iterator().next(), result.getParameters().iterator().next());

        barFile.delete();
    }

    @Test(expected = InvalidBusinessArchiveFormatException.class)
    public void readInvalidProcessFromBusinessArchive() throws Exception {
        final File tempFile = createTempFile();
        final File barFile = File.createTempFile("businessArchive", ".bar");
        barFile.delete();

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("MyProcess", "1.0");
        processDefinitionBuilder.addActor("Truck Driver").addDescription("A man that is driving bigs trucks");
        processDefinitionBuilder.addStartEvent("start1");
        processDefinitionBuilder.addAutomaticTask("auto1").addConnector("conn1", "connId1", "1.0.0", ConnectorEvent.ON_FINISH);
        processDefinitionBuilder.addUserTask("user1", "Truck Driver").addConnector("conn2", "connId2", "1.0.0", ConnectorEvent.ON_ENTER);
        processDefinitionBuilder.addGateway("gate1", GatewayType.INCLUSIVE).addDefaultTransition("user1");
        processDefinitionBuilder.addEndEvent("end1");
        processDefinitionBuilder.addTransition("start1", "auto1");
        processDefinitionBuilder.addTransition("auto1", "user1");
        processDefinitionBuilder.addTransition("user1", "gate1");
        processDefinitionBuilder.addTransition("user1", "end1");
        processDefinitionBuilder.addConnector("conn3", "connId3", "1.0.0", ConnectorEvent.ON_FINISH);
        processDefinitionBuilder.addParameter("myParam", String.class.getName()).addDescription("an important parameter");

        final DesignProcessDefinition process = processDefinitionBuilder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process).done();
        BusinessArchiveFactory.writeBusinessArchiveToFolder(businessArchive, tempFile);

        final File file = new File(tempFile, ProcessDefinitionBARContribution.PROCESS_DEFINITION_XML);
        String fileContent = IOUtil.read(file);
        fileContent = fileContent.replace("<processDefinition", "<porcessDefinition");
        fileContent = fileContent.replace("</processDefinition", "</porcessDefinition");
        file.delete();
        file.createNewFile();
        IOUtil.writeContentToFile(fileContent, file);
        try {
            BusinessArchiveFactory.readBusinessArchive(tempFile);
        } finally {
            barFile.delete();
            deleteDir(tempFile);

        }
        barFile.delete();
    }

    @Test(expected = InvalidBusinessArchiveFormatException.class)
    public void readInvalidXMLProcessFromBusinessArchive() throws Exception {
        final File tempFile = createTempFile();
        final File barFile = File.createTempFile("businessArchive", ".bar");
        barFile.delete();

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("MyProcess", "1.0");
        processDefinitionBuilder.addActor("Truck Driver").addDescription("A man that is driving bigs trucks");
        processDefinitionBuilder.addStartEvent("start1");
        processDefinitionBuilder.addAutomaticTask("auto1").addConnector("conn1", "connId1", "1.0.0", ConnectorEvent.ON_FINISH);
        processDefinitionBuilder.addUserTask("user1", "Truck Driver").addConnector("conn2", "connId2", "1.0.0", ConnectorEvent.ON_ENTER);
        processDefinitionBuilder.addGateway("gate1", GatewayType.INCLUSIVE).addDefaultTransition("user1");
        processDefinitionBuilder.addEndEvent("end1");
        processDefinitionBuilder.addTransition("start1", "auto1");
        processDefinitionBuilder.addTransition("auto1", "user1");
        processDefinitionBuilder.addTransition("user1", "gate1");
        processDefinitionBuilder.addTransition("user1", "end1");
        processDefinitionBuilder.addConnector("conn3", "connId3", "1.0.0", ConnectorEvent.ON_FINISH);
        processDefinitionBuilder.addParameter("myParam", String.class.getName()).addDescription("an important parameter");

        final DesignProcessDefinition process = processDefinitionBuilder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process).done();
        BusinessArchiveFactory.writeBusinessArchiveToFolder(businessArchive, tempFile);

        final File file = new File(tempFile, ProcessDefinitionBARContribution.PROCESS_DEFINITION_XML);
        String fileContent = IOUtil.read(file);
        fileContent = fileContent.replace("<processDefinition", "<porcessDefinition");
        file.delete();
        file.createNewFile();
        IOUtil.writeContentToFile(fileContent, file);
        try {
            BusinessArchiveFactory.readBusinessArchive(tempFile);
        } finally {
            barFile.delete();
            deleteDir(tempFile);

        }
        barFile.delete();
    }

    @Test
    public void testParameters() {
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("firstProcess", "1.0");
        processBuilder.addParameter("key1", String.class.getCanonicalName()).addParameter("key.2", String.class.getCanonicalName())
                .addUserTask("userTask1", null);

    }

}
