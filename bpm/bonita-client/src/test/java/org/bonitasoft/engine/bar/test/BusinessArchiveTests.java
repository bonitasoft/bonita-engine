package org.bonitasoft.engine.bar.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.bar.ActorMappingContribution;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveFactory;
import org.bonitasoft.engine.bpm.bar.ProcessDefinitionBARContribution;
import org.bonitasoft.engine.bpm.model.ActivityDefinition;
import org.bonitasoft.engine.bpm.model.CallActivityBuilder;
import org.bonitasoft.engine.bpm.model.ConnectorDefinition;
import org.bonitasoft.engine.bpm.model.ConnectorEvent;
import org.bonitasoft.engine.bpm.model.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.model.DocumentDefinition;
import org.bonitasoft.engine.bpm.model.GatewayType;
import org.bonitasoft.engine.bpm.model.IntermediateCatchMessageEventTriggerDefinitionBuilder;
import org.bonitasoft.engine.bpm.model.ProcessDefinition;
import org.bonitasoft.engine.bpm.model.ThrowMessageEventTriggerBuilder;
import org.bonitasoft.engine.bpm.model.TransitionDefinition;
import org.bonitasoft.engine.bpm.model.UserTaskDefinition;
import org.bonitasoft.engine.bpm.model.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.bpm.model.event.EndEventDefinition;
import org.bonitasoft.engine.bpm.model.event.trigger.CatchMessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.model.event.trigger.ThrowMessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.model.event.trigger.TimerType;
import org.bonitasoft.engine.core.operation.LeftOperand;
import org.bonitasoft.engine.core.operation.LeftOperandBuilder;
import org.bonitasoft.engine.core.operation.Operation;
import org.bonitasoft.engine.core.operation.OperationBuilder;
import org.bonitasoft.engine.core.operation.OperatorType;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.InvalidBusinessArchiveFormat;
import org.bonitasoft.engine.exception.InvalidProcessDefinitionException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.util.FileUtil;
import org.junit.Test;

import com.bonitasoft.engine.bpm.model.ProcessDefinitionBuilder;

/**
 * @author Baptiste Mesta
 */
public class BusinessArchiveTests {

    private static final String ASSIGN_OPERATOR = "=";

    @Test
    public void createBusinessArchiveFolder() throws Exception {
        final File tempFile = File.createTempFile("businessArchive", "folder");
        tempFile.delete();

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        final DesignProcessDefinition process = processDefinitionBuilder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process).done();
        BusinessArchiveFactory.writeBusinessArchiveToFolder(businessArchive, tempFile);
        assertTrue(tempFile.exists());
        assertTrue(tempFile.isDirectory());
        final File file = new File(tempFile, ProcessDefinitionBARContribution.PROCESS_DEFINITION_XML);
        assertTrue(file.exists());
        assertFalse(file.isDirectory());
        deleteDir(tempFile);
    }

    @Test
    public void createBusinessArchiveFileFromFolder() throws Exception {
        final File tempFile = File.createTempFile("businessArchive", "folder");
        tempFile.delete();
        final File barFile = File.createTempFile("businessArchive", ".bar");
        barFile.delete();

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        final DesignProcessDefinition process = processDefinitionBuilder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process).done();
        BusinessArchiveFactory.writeBusinessArchiveToFolder(businessArchive, tempFile);

        BusinessArchiveFactory.businessArchiveFolderToFile(barFile, tempFile.getAbsolutePath());
        assertTrue(barFile.exists());

        final InputStream inputStream = new FileInputStream(barFile);
        final BusinessArchive businessArchive2 = BusinessArchiveFactory.readBusinessArchive(inputStream);
        inputStream.close();
        final ProcessDefinition result = businessArchive2.getProcessDefinition();

        assertEquals(process, result);
        barFile.delete();
        deleteDir(tempFile);
    }

    @Test
    public void createBusinessArchiveFromFile() throws Exception {
        final File tempFile = File.createTempFile("businessArchive", "folder");
        tempFile.delete();
        final File barFile = File.createTempFile("businessArchive", ".bar");
        barFile.delete();

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        final DesignProcessDefinition process = processDefinitionBuilder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process).done();
        BusinessArchiveFactory.writeBusinessArchiveToFolder(businessArchive, tempFile);

        BusinessArchiveFactory.businessArchiveFolderToFile(barFile, tempFile.getAbsolutePath());
        assertTrue(barFile.exists());
        final BusinessArchive businessArchive2 = BusinessArchiveFactory.readBusinessArchive(barFile);
        final ProcessDefinition result = businessArchive2.getProcessDefinition();

        assertEquals(process, result);
        barFile.delete();
        deleteDir(tempFile);
    }

    @Test(expected = InvalidBusinessArchiveFormat.class)
    public void readInvalidBusinessArchive() throws Exception {
        final File tempFile = File.createTempFile("businessArchive", "folder");
        tempFile.delete();
        final File barFile = File.createTempFile("businessArchive", ".bar");
        barFile.delete();

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        final DesignProcessDefinition process = processDefinitionBuilder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process).done();
        BusinessArchiveFactory.writeBusinessArchiveToFolder(businessArchive, tempFile);
        final File file = new File(tempFile, ProcessDefinitionBARContribution.PROCESS_DEFINITION_XML);
        file.delete();
        file.createNewFile();
        final FileWriter fileWriter = new FileWriter(file);
        fileWriter.write("test");
        fileWriter.flush();
        fileWriter.close();
        try {
            BusinessArchiveFactory.readBusinessArchive(tempFile);
        } finally {
            barFile.delete();
            deleteDir(tempFile);

        }
    }

    @Test(expected = IOException.class)
    public void createBusinessArchiveFolderWithInvalidPath() throws Exception {
        BusinessArchiveFactory.readBusinessArchive(new File("$$$an invalidPath@//\\ùù%%%"));
    }

    @Test
    public void createBusinessArchiveWithProcessDefinition() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        final DesignProcessDefinition process = processDefinitionBuilder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process).done();
        final ProcessDefinition result = businessArchive.getProcessDefinition();

        assertEquals(process, result);
    }

    @Test(expected = InvalidBusinessArchiveFormat.class)
    public void createEmptyBusinessArchive() throws Exception {
        new BusinessArchiveBuilder().createNewBusinessArchive().done();
    }

    @Test
    public void exportBusinessArchiveAsFile() throws Exception {
        final File barFile = File.createTempFile("businessArchive", ".bar");
        barFile.delete();

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        final DesignProcessDefinition process = processDefinitionBuilder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process).done();
        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, barFile);
        assertTrue(barFile.exists());
        assertFalse(barFile.isDirectory());
        barFile.delete();
    }

    @Test(expected = IOException.class)
    public void exportBusinessArchiveAsFileOnExistingFile() throws Exception {
        final File barFile = File.createTempFile("businessArchive", ".bar");

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        final DesignProcessDefinition process = processDefinitionBuilder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process).done();
        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, barFile);
    }

    @Test
    public void readBusinessArchive() throws Exception {
        final File barFile = File.createTempFile("businessArchive", ".bar");
        barFile.delete();

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        final DesignProcessDefinition process = processDefinitionBuilder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process).done();
        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, barFile);

        final InputStream inputStream = new FileInputStream(barFile);
        final BusinessArchive businessArchive2 = BusinessArchiveFactory.readBusinessArchive(inputStream);
        inputStream.close();
        final ProcessDefinition result = businessArchive2.getProcessDefinition();

        assertEquals(process, result);
        barFile.delete();
    }

    @Test(expected = InvalidBusinessArchiveFormat.class)
    public void importOldBusinessArchiveFail() throws Exception {
        final InputStream resourceAsStream = this.getClass().getResourceAsStream("MyProcess--1.0.bar");
        BusinessArchiveFactory.readBusinessArchive(resourceAsStream);
    }

    @Test(expected = InvalidBusinessArchiveFormat.class)
    public void importOldBusinessArchiveFileFail() throws Exception {
        final File barFile = File.createTempFile("businessArchive", ".bar");
        barFile.delete();
        final InputStream inputStream = this.getClass().getResourceAsStream("MyProcess--1.0.bar");
        final OutputStream out = new FileOutputStream(barFile);
        final byte buf[] = new byte[1024];
        int len;
        while ((len = inputStream.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
        inputStream.close();
        try {
            BusinessArchiveFactory.readBusinessArchive(barFile);
        } finally {
            barFile.delete();
        }
    }

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

    @Test
    public void manageBusinessArchiveResources() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        final DesignProcessDefinition designProcessDefinition = processDefinitionBuilder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition)
                .addExternalResource(new BarResource("dummy.txt", new byte[] { 'a', 'b', 'c', 'd' })).done();

        // Add a resource to the biz archive:
        final File tempFile = File.createTempFile("testbar", ".bar");
        tempFile.delete();
        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, tempFile);

        // read from the file
        final BusinessArchive readBusinessArchive = BusinessArchiveFactory.readBusinessArchive(tempFile);
        // final ProcessDefinition processDefinition = processAPI.deploy(readBusinessArchive);
        assertTrue("Added resource not found in BusinessArchive", readBusinessArchive.getResources().containsKey("resources/dummy.txt"));
        tempFile.delete();
    }

    @Test
    public void putActorMappingInBar() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("ProductionPlanning", "3.1");
        final DesignProcessDefinition designProcessDefinition = processDefinitionBuilder.done();
        // Add a resource to the biz archive:
        final byte[] xmlBytes = "<toto>text</toto>".getBytes();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition)
                .setActorMapping(xmlBytes).done();

        final File tempFile = File.createTempFile("testbar", ".bar");
        tempFile.delete();
        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, tempFile);

        // read from the file
        final BusinessArchive readBusinessArchive = BusinessArchiveFactory.readBusinessArchive(tempFile);
        // final ProcessDefinition processDefinition = processAPI.deploy(readBusinessArchive);
        assertTrue("Actor Mapping not found in BusinessArchive",
                Arrays.equals(xmlBytes, readBusinessArchive.getResource(ActorMappingContribution.ACTOR_MAPPING_FILE)));
        tempFile.delete();
    }

    @Test
    public void readProcessFromBusinessArchive() throws Exception {
        final File barFile = File.createTempFile("businessArchive", ".bar");
        barFile.delete();

        final Expression trueExpression = new ExpressionBuilder().createConstantBooleanExpression(true);
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        processDefinitionBuilder.addDocumentDefinition("testDoc", "testFile.txt").addFile("testFile.txt").addDescription("desc").addMimeType("text/plain");
        processDefinitionBuilder.addDocumentDefinition("testDocUrl", "testFile.txt").addUrl("http://test.com/testFile.txt").addDescription("desc");
        processDefinitionBuilder.addDescription("a very good description");
        processDefinitionBuilder.addDisplayDescription("A very good and clean description that will be displayed in user xp");
        processDefinitionBuilder.addDisplayName("Truck Handling Process");
        processDefinitionBuilder.addActor("Truck Driver").addDescription("A man that is driving bigs trucks");
        processDefinitionBuilder.addStartEvent("start1").addTimerEventTriggerDefinition(TimerType.CYCLE,
                new ExpressionBuilder().createConstantStringExpression("*/3 * * * * ?"));
        // No Java operation, so empty string passed:
        processDefinitionBuilder
                .addAutomaticTask("auto1")
                .addOperation(new LeftOperandBuilder().createNewInstance().setDataName("testData").done(), OperatorType.ASSIGNMENT, ASSIGN_OPERATOR, null,
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
                .addOutput(new LeftOperandBuilder().createNewInstance().setDataName("testData").done(), OperatorType.ASSIGNMENT, ASSIGN_OPERATOR, null,
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
        for (final TransitionDefinition transition : result.getProcessContainer().getTransitions()) {
            if ("start1".equals(transition.getSource()) && trueExpression.equals(transition.getCondition())) {
                trWithConditionOk = true;
                break;
            }
        }
        assertTrue("the condition on the transition was not kept", trWithConditionOk);
        assertEquals(process.getProcessContainer().getConnectors().size(), result.getProcessContainer().getConnectors().size());
        boolean connectorWithInputOutputOk = false;
        for (final ConnectorDefinition connector : result.getProcessContainer().getConnectors()) {
            final Operation operation = connector.getOutputs().get(0);
            if ("conn3".equals(connector.getName()) && trueExpression.equals(connector.getInputs().get("input1"))
                    && "testData".equals(operation.getVariableToSet().getDataName()) && OperatorType.ASSIGNMENT.equals(operation.getType())
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

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        final DesignProcessDefinition process = processDefinitionBuilder.done();

        final HashMap<String, String> initialParameters = new HashMap<String, String>();
        initialParameters.put("myKey1", "myValue1");
        initialParameters.put("myKey2", "myValue2");
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process)
                .setParameters(initialParameters).setActorMapping(null).done();
        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, barFile);

        final InputStream inputStream = new FileInputStream(barFile);
        final BusinessArchive businessArchive2 = BusinessArchiveFactory.readBusinessArchive(inputStream);
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
        leftOperandBuilder.createNewInstance().setDataName("var1");
        final OperationBuilder opb = new OperationBuilder();
        opb.createNewInstance().setOperator(ASSIGN_OPERATOR).setRightOperand(trueExpression).setType(OperatorType.ASSIGNMENT)
                .setVariableToSet(leftOperandBuilder.done());

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        processDefinitionBuilder.addBooleanData("var1", null);
        processDefinitionBuilder.addStartEvent("start1").addMessageEventTrigger("m1").addOperation(opb.done());
        processDefinitionBuilder.addAutomaticTask("auto1");
        final IntermediateCatchMessageEventTriggerDefinitionBuilder catchMessageEventTriggerDefinitionBuilder = processDefinitionBuilder
                .addIntermediateCatchEvent("waitForMessage").addMessageEventTrigger("m2");
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

    @Test(expected = InvalidProcessDefinitionException.class)
    public void tooMuchCorrelationOnCatchMessage() throws Exception {
        final Expression conditionKey = new ExpressionBuilder().createConstantStringExpression("coditionKey");
        final Expression trueExpression = new ExpressionBuilder().createConstantBooleanExpression(true);
        final Expression displayNameExpression = new ExpressionBuilder().createConstantStringExpression("dataToSend");

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        processDefinitionBuilder.addStartEvent("start1").addMessageEventTrigger("m1");
        processDefinitionBuilder.addAutomaticTask("auto1");
        final IntermediateCatchMessageEventTriggerDefinitionBuilder catchMessageEventTriggerDefinitionBuilder = processDefinitionBuilder
                .addIntermediateCatchEvent("waitForMessage").addMessageEventTrigger("m2");
        catchMessageEventTriggerDefinitionBuilder.addCorrelation(conditionKey, trueExpression);
        catchMessageEventTriggerDefinitionBuilder.addCorrelation(conditionKey, trueExpression);
        catchMessageEventTriggerDefinitionBuilder.addCorrelation(conditionKey, trueExpression);
        catchMessageEventTriggerDefinitionBuilder.addCorrelation(conditionKey, trueExpression);
        catchMessageEventTriggerDefinitionBuilder.addCorrelation(conditionKey, trueExpression);
        catchMessageEventTriggerDefinitionBuilder.addCorrelation(conditionKey, trueExpression);
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
        processDefinitionBuilder.done();
    }

    @Test(expected = InvalidProcessDefinitionException.class)
    public void tooMuchCorrelationOnThrowMessage() throws Exception {
        final Expression conditionKey = new ExpressionBuilder().createConstantStringExpression("coditionKey");
        final Expression trueExpression = new ExpressionBuilder().createConstantBooleanExpression(true);
        final Expression displayNameExpression = new ExpressionBuilder().createConstantStringExpression("dataToSend");

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        processDefinitionBuilder.addStartEvent("start1").addMessageEventTrigger("m1");
        processDefinitionBuilder.addAutomaticTask("auto1");
        final IntermediateCatchMessageEventTriggerDefinitionBuilder catchMessageEventTriggerDefinitionBuilder = processDefinitionBuilder
                .addIntermediateCatchEvent("waitForMessage").addMessageEventTrigger("m2");
        catchMessageEventTriggerDefinitionBuilder.addCorrelation(conditionKey, trueExpression);
        // create expression for target process/flowNode
        Expression targetProcess = new ExpressionBuilder().createDataExpression("p3", String.class.getName());
        final Expression receiveMessage = new ExpressionBuilder().createDataExpression("receiveMessage", String.class.getName());
        processDefinitionBuilder.addIntermediateThrowEvent("sendMessage").addMessageEventTrigger("m4", targetProcess, receiveMessage);
        targetProcess = new ExpressionBuilder().createConstantStringExpression("p2");
        final Expression waitMessage = new ExpressionBuilder().createConstantStringExpression("waitMessage");
        final ThrowMessageEventTriggerBuilder throwMessageEventTriggerBuilder = processDefinitionBuilder.addEndEvent("end1").addMessageEventTrigger("m2",
                targetProcess, waitMessage);
        throwMessageEventTriggerBuilder.addCorrelation(conditionKey, trueExpression);
        throwMessageEventTriggerBuilder.addCorrelation(conditionKey, trueExpression);
        throwMessageEventTriggerBuilder.addCorrelation(conditionKey, trueExpression);
        throwMessageEventTriggerBuilder.addCorrelation(conditionKey, trueExpression);
        throwMessageEventTriggerBuilder.addCorrelation(conditionKey, trueExpression);
        throwMessageEventTriggerBuilder.addCorrelation(conditionKey, trueExpression);
        throwMessageEventTriggerBuilder.addMessageContentExpression(displayNameExpression, trueExpression);
        processDefinitionBuilder.addTransition("start1", "auto1", trueExpression);
        processDefinitionBuilder.addTransition("auto1", "waitForMessage");
        processDefinitionBuilder.addTransition("waitForMessage", "sendMessage");
        processDefinitionBuilder.addTransition("sendMessage", "end1");
        processDefinitionBuilder.done();
    }

    @Test
    public void readProcessWithCallActivityFromBusinessArchive() throws Exception {
        final File barFile = File.createTempFile("businessArchive", ".bar");
        barFile.delete();

        final Expression fromCallerData = new ExpressionBuilder().createDataExpression("var1", Boolean.class.getName());
        final LeftOperand dataInputLeftOp = new LeftOperandBuilder().createNewInstance("data1").done();
        final Operation dataInputOperation = getOperation(fromCallerData, dataInputLeftOp);

        final Expression fromCallableElementData = new ExpressionBuilder().createDataExpression("data2", Integer.class.getName());
        final LeftOperand dataOutputOp = new LeftOperandBuilder().createNewInstance("var2").done();
        final Operation dataOutputOperation = getOperation(fromCallableElementData, dataOutputOp);

        final Expression targetProcess = new ExpressionBuilder().createConstantStringExpression("MyProcess2");
        final Expression processVersion = new ExpressionBuilder().createConstantStringExpression("1.0");

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        processDefinitionBuilder.addBooleanData("var1", null);
        processDefinitionBuilder.addIntegerData("var2", null);
        processDefinitionBuilder.addStartEvent("start1");
        final CallActivityBuilder callActivityBuilder = processDefinitionBuilder.addCallActivity("callActivity", targetProcess, processVersion);
        callActivityBuilder.addDataInputOperation(dataInputOperation);
        callActivityBuilder.addDataOutputOperation(dataOutputOperation);
        processDefinitionBuilder.addEndEvent("end1");
        processDefinitionBuilder.addTransition("start1", "callActivity");
        processDefinitionBuilder.addTransition("callActivity", "end1");

        final DesignProcessDefinition process = processDefinitionBuilder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process).done();
        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, barFile);

        final InputStream inputStream = new FileInputStream(barFile);
        final BusinessArchive businessArchive2 = BusinessArchiveFactory.readBusinessArchive(inputStream);
        inputStream.close();
        final DesignProcessDefinition result = businessArchive2.getProcessDefinition();

        checkProcessForCallActivity(process, result);

        barFile.delete();
    }

    @Test
    public void readProcessWithBoundaryEventsFromBusinessArchive() throws Exception {
        final File barFile = File.createTempFile("businessArchive", ".bar");
        barFile.delete();

        final Expression timerExpression = new ExpressionBuilder().createConstantLongExpression(1000);

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        processDefinitionBuilder.addStartEvent("start1");
        final UserTaskDefinitionBuilder userTaskDefinitionBuilder = processDefinitionBuilder.addUserTask("userTask", "delivery");
        userTaskDefinitionBuilder.addBoundaryEvent("b1").addTimerEventTriggerDefinition(TimerType.DURATION, timerExpression);
        userTaskDefinitionBuilder.addBoundaryEvent("b2").addMessageEventTrigger("m1");
        userTaskDefinitionBuilder.addBoundaryEvent("b3").addErrorEventTrigger("e1");
        processDefinitionBuilder.addAutomaticTask("exceptionFlowB1");
        processDefinitionBuilder.addAutomaticTask("exceptionFlowB2");
        processDefinitionBuilder.addAutomaticTask("exceptionFlowB3");
        processDefinitionBuilder.addEndEvent("end1");
        processDefinitionBuilder.addTransition("start1", "userTask");
        processDefinitionBuilder.addTransition("userTask", "end1");
        processDefinitionBuilder.addTransition("b1", "exceptionFlowB1");
        processDefinitionBuilder.addTransition("b2", "exceptionFlowB2");
        processDefinitionBuilder.addTransition("b3", "exceptionFlowB3");

        final DesignProcessDefinition process = processDefinitionBuilder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process).done();
        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, barFile);

        final InputStream inputStream = new FileInputStream(barFile);
        final BusinessArchive businessArchive2 = BusinessArchiveFactory.readBusinessArchive(inputStream);
        inputStream.close();
        final DesignProcessDefinition result = businessArchive2.getProcessDefinition();

        checkProcessForBoundaryEvents(process, result);

        barFile.delete();
    }

    @Test
    public void readProcessWithThowErrorEventFromBusinessArchive() throws Exception {
        final File barFile = File.createTempFile("businessArchive", ".bar");
        barFile.delete();

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        processDefinitionBuilder.addStartEvent("start1");
        processDefinitionBuilder.addAutomaticTask("a1");
        processDefinitionBuilder.addEndEvent("end1").addErrorEventTrigger("e1");
        processDefinitionBuilder.addTransition("start1", "a1");
        processDefinitionBuilder.addTransition("a1", "end1");

        final DesignProcessDefinition process = processDefinitionBuilder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process).done();
        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, barFile);

        final InputStream inputStream = new FileInputStream(barFile);
        final BusinessArchive businessArchive2 = BusinessArchiveFactory.readBusinessArchive(inputStream);
        inputStream.close();
        final DesignProcessDefinition result = businessArchive2.getProcessDefinition();

        assertEquals(process.getName(), result.getName());
        assertEquals(process.getVersion(), result.getVersion());

        assertEquals(process.getProcessContainer().getStartEvents(), result.getProcessContainer().getStartEvents());
        assertEquals(1, result.getProcessContainer().getStartEvents().size());

        assertEquals(1, process.getProcessContainer().getActivities().size());
        assertEquals(1, result.getProcessContainer().getActivities().size());

        final List<EndEventDefinition> resultEndEvents = result.getProcessContainer().getEndEvents();
        assertEquals(process.getProcessContainer().getEndEvents(), resultEndEvents);
        assertEquals(1, result.getProcessContainer().getEndEvents().size());
        final EndEventDefinition endEventDefinition = resultEndEvents.get(0);
        assertEquals(1, endEventDefinition.getEventTriggers().size());
        assertEquals(1, endEventDefinition.getErrorEventTriggerDefinitions().size());

        barFile.delete();
    }

    private Operation getOperation(final Expression rightOperand, final LeftOperand leftOperand) {
        final OperationBuilder opb = new OperationBuilder().createNewInstance();
        opb.setVariableToSet(leftOperand);
        opb.setOperator(ASSIGN_OPERATOR);
        opb.setRightOperand(rightOperand);
        opb.setType(OperatorType.ASSIGNMENT);
        final Operation op = opb.done();
        return op;
    }

    private void checkProcessForCallActivity(final DesignProcessDefinition process, final DesignProcessDefinition result) {
        assertEquals(process.getName(), result.getName());
        assertEquals(process.getVersion(), result.getVersion());

        assertEquals(process.getProcessContainer().getStartEvents(), result.getProcessContainer().getStartEvents());
        assertEquals(1, result.getProcessContainer().getStartEvents().size());

        assertEquals(1, result.getProcessContainer().getActivities().size());
        assertEquals(1, process.getProcessContainer().getActivities().size());
        assertEquals(process.getProcessContainer().getActivities().iterator().next(), result.getProcessContainer().getActivities().iterator().next());

        assertEquals(process.getProcessContainer().getEndEvents(), result.getProcessContainer().getEndEvents());
        assertEquals(1, result.getProcessContainer().getEndEvents().size());
    }

    private void checkProcessForBoundaryEvents(final DesignProcessDefinition process, final DesignProcessDefinition result) {
        assertEquals(process.getName(), result.getName());
        assertEquals(process.getVersion(), result.getVersion());

        assertEquals(process.getProcessContainer().getStartEvents(), result.getProcessContainer().getStartEvents());
        assertEquals(1, result.getProcessContainer().getStartEvents().size());

        assertEquals(4, process.getProcessContainer().getActivities().size());
        assertEquals(4, result.getProcessContainer().getActivities().size());
        final ActivityDefinition resultActivity = result.getProcessContainer().getActivity("userTask");
        assertEquals(process.getProcessContainer().getActivity("userTask"), resultActivity);
        assertEquals(3, resultActivity.getBoundaryEventDefinitions().size());

        assertEquals(process.getProcessContainer().getEndEvents(), result.getProcessContainer().getEndEvents());
        assertEquals(1, result.getProcessContainer().getEndEvents().size());
    }

    private void checkProcessForMessagesEvents(final DesignProcessDefinition process, final DesignProcessDefinition result) {
        assertEquals(process.getName(), result.getName());
        assertEquals(process.getVersion(), result.getVersion());

        assertEquals(process.getProcessContainer().getStartEvents().size(), result.getProcessContainer().getStartEvents().size());
        assertEquals(1, result.getProcessContainer().getStartEvents().size());
        assertEquals(process.getProcessContainer().getStartEvents().get(0), result.getProcessContainer().getStartEvents().get(0));
        assertEquals(1, result.getProcessContainer().getStartEvents().get(0).getMessageEventTriggerDefinitons().size());
        assertEquals(process.getProcessContainer().getStartEvents().get(0).getMessageEventTriggerDefinitons().get(0).getOperations(), result
                .getProcessContainer().getStartEvents().get(0).getMessageEventTriggerDefinitons().get(0).getOperations());
        assertEquals(1, result.getProcessContainer().getStartEvents().get(0).getMessageEventTriggerDefinitons().get(0).getOperations().size());

        assertEquals(process.getProcessContainer().getIntermediateCatchEvents().size(), result.getProcessContainer().getIntermediateCatchEvents().size());
        assertEquals(1, result.getProcessContainer().getIntermediateCatchEvents().size());
        assertEquals(process.getProcessContainer().getIntermediateCatchEvents().get(0), result.getProcessContainer().getIntermediateCatchEvents().get(0));
        assertEquals(1, result.getProcessContainer().getIntermediateCatchEvents().get(0).getMessageEventTriggerDefinitons().size());

        final CatchMessageEventTriggerDefinition expectedCatchMessageEventTrigger = process.getProcessContainer().getIntermediateCatchEvents().get(0)
                .getMessageEventTriggerDefinitons().get(0);
        final CatchMessageEventTriggerDefinition actualCatchMessageEventTrigger = result.getProcessContainer().getIntermediateCatchEvents().get(0)
                .getMessageEventTriggerDefinitons().get(0);
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
    public void checkErrorMessageOnInvalidTransition() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        processDefinitionBuilder.addTransition("unknown1", "unknown2");
        try {
            processDefinitionBuilder.done();
            fail("should have thrown an " + InvalidProcessDefinitionException.class.getSimpleName());
        } catch (final InvalidProcessDefinitionException e) {
            assertTrue(e.getMessage().contains("unknown1"));
            assertTrue(e.getMessage().contains("unknown2"));
        }
    }

    @Test
    public void readProcessWithActorWithoutDescription() throws Exception {
        final File barFile = File.createTempFile("businessArchive", ".bar");
        barFile.delete();

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcessTT", "1.0");
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

    @Test(expected = InvalidBusinessArchiveFormat.class)
    public void readInvalidProcessFromBusinessArchive() throws Exception {
        final File tempFile = File.createTempFile("businessArchive", "folder");
        tempFile.delete();
        final File barFile = File.createTempFile("businessArchive", ".bar");
        barFile.delete();

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
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
        String fileContent = FileUtil.read(file);
        fileContent = fileContent.replace("<processDefinition", "<porcessDefinition");
        fileContent = fileContent.replace("</processDefinition", "</porcessDefinition");
        file.delete();
        file.createNewFile();
        FileUtil.write(file, fileContent);
        try {
            BusinessArchiveFactory.readBusinessArchive(tempFile);
        } finally {
            barFile.delete();
            deleteDir(tempFile);

        }
        barFile.delete();
    }

    @Test(expected = InvalidBusinessArchiveFormat.class)
    public void readInvalidXMLProcessFromBusinessArchive() throws Exception {
        final File tempFile = File.createTempFile("businessArchive", "folder");
        tempFile.delete();
        final File barFile = File.createTempFile("businessArchive", ".bar");
        barFile.delete();

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
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
        String fileContent = FileUtil.read(file);
        fileContent = fileContent.replace("<processDefinition", "<porcessDefinition");
        file.delete();
        file.createNewFile();
        FileUtil.write(file, fileContent);
        try {
            BusinessArchiveFactory.readBusinessArchive(tempFile);
        } finally {
            barFile.delete();
            deleteDir(tempFile);

        }
        barFile.delete();
    }

    @Test(expected = InvalidProcessDefinitionException.class)
    public void createProcessWithADocumentHavingBothUrlAndFile() throws InvalidProcessDefinitionException {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("MyProcessWithExternalDocuments", "1.0");
        builder.addDocumentDefinition("myDoc", "testFile.txt").addDescription("a cool pdf document").addMimeType("application/pdf").addFile("myPdf.pdf")
                .addUrl("http://plop");
        builder.done();
    }

    @Test(expected = InvalidBusinessArchiveFormat.class)
    public void createProcessWithAInBarDocumentMissingFile() throws InvalidProcessDefinitionException, InvalidBusinessArchiveFormat {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("MyProcessWithExternalDocuments", "1.0");
        builder.addDocumentDefinition("myDoc", "testFile.txt").addDescription("a cool pdf document").addMimeType("application/pdf").addFile("myPdf.pdf");
        final DesignProcessDefinition processDefinition = builder.done();
        new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDefinition)
                .addDocumentResource(new BarResource("testFile.txt", new byte[] { 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 })).done();
    }

    @Test
    public void testSubProcess() throws BonitaException, IOException {
        final Expression createdExpression = new ExpressionBuilder().createConstantBooleanExpression(false);

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder();
        builder.createNewInstance("process", "10.2").addSubProcess("subProcessActivity", true).setSubProcess().addStartEvent("start")
                .addTimerEventTriggerDefinition(TimerType.DURATION, new ExpressionBuilder().createConstantLongExpression(5000))
                .addBooleanData("created", createdExpression).addAutomaticTask("auto");

        final DesignProcessDefinition process = builder.getProcess();
        assertEquals(1, process.getProcessContainer().getActivities().size());

        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process).done();
        final File barFile = File.createTempFile("businessArchive", ".bar");
        barFile.delete();
        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, barFile);

        final InputStream inputStream = new FileInputStream(barFile);
        final BusinessArchive businessArchive2 = BusinessArchiveFactory.readBusinessArchive(inputStream);
        inputStream.close();
        final DesignProcessDefinition result = businessArchive2.getProcessDefinition();
        assertEquals(1, result.getProcessContainer().getActivities().size());
        barFile.delete();
    }

    @Test
    public void testParameters() {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addParameter("key1", String.class.getCanonicalName()).addParameter("key.2", String.class.getCanonicalName())
                .addUserTask("userTask1", null);

    }
    
    @Test
    public void testGeneratingOutgoingDefaultTransitionShouldBeConformToProcessDefinitionXsd() throws Exception {
        final File barFile = File.createTempFile("businessArchive", ".bar");
        barFile.delete();

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        processDefinitionBuilder.addActor("Truck Driver").addDescription("A man that is driving bigs trucks");
        processDefinitionBuilder.addStartEvent("start1");
        processDefinitionBuilder.addGateway("Gateway1", GatewayType.EXCLUSIVE);
        processDefinitionBuilder.addEndEvent("end1");
        processDefinitionBuilder.addEndEvent("end2");
        processDefinitionBuilder.addEndEvent("end3");
        processDefinitionBuilder.addTransition("start1", "Gateway1");
        processDefinitionBuilder.addTransition("Gateway1", "end1");
        processDefinitionBuilder.addTransition("Gateway1", "end2");
        processDefinitionBuilder.addDefaultTransition("Gateway1", "end3");

        final DesignProcessDefinition process = processDefinitionBuilder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process).done();
        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, barFile);

        BusinessArchiveFactory.readBusinessArchive(barFile);
    }
}
