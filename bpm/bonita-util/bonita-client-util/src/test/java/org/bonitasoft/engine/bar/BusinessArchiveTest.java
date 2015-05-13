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
package org.bonitasoft.engine.bar;

import static org.assertj.core.api.Assertions.assertThat;
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
import java.util.Iterator;
import java.util.List;

import org.bonitasoft.engine.bpm.actor.ActorDefinition;
import org.bonitasoft.engine.bpm.bar.ActorMappingContribution;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveFactory;
import org.bonitasoft.engine.bpm.bar.InvalidBusinessArchiveFormatException;
import org.bonitasoft.engine.bpm.bar.ProcessDefinitionBARContribution;
import org.bonitasoft.engine.bpm.bar.form.model.FormMappingDefinition;
import org.bonitasoft.engine.bpm.bar.form.model.FormMappingModel;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinition;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.contract.InputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.contract.impl.InputDefinitionImpl;
import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.data.TextDataDefinition;
import org.bonitasoft.engine.bpm.document.DocumentDefinition;
import org.bonitasoft.engine.bpm.flownode.ActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.AutomaticTaskDefinition;
import org.bonitasoft.engine.bpm.flownode.BoundaryEventDefinition;
import org.bonitasoft.engine.bpm.flownode.CatchMessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.EndEventDefinition;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.flownode.SendTaskDefinition;
import org.bonitasoft.engine.bpm.flownode.ThrowMessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.flownode.TransitionDefinition;
import org.bonitasoft.engine.bpm.flownode.UserTaskDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.internal.MultiInstanceLoopCharacteristics;
import org.bonitasoft.engine.bpm.flownode.impl.internal.StandardLoopCharacteristics;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.AutomaticTaskDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.CallActivityBuilder;
import org.bonitasoft.engine.bpm.process.impl.CatchMessageEventTriggerDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ContractDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.MultiInstanceLoopCharacteristicsBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.SendTaskDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ThrowMessageEventTriggerBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.form.FormMappingTarget;
import org.bonitasoft.engine.form.FormMappingType;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.operation.LeftOperand;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @author Emmanuel Duchastenier
 */
public class BusinessArchiveTest {

    private static final String ASSIGN_OPERATOR = "=";

    private File tempFolder;

    private File barFile;

    @Before
    public void before() throws IOException {
        final String barFolderName = "tmpBar";
        tempFolder = IOUtil.createTempDirectoryInDefaultTempDirectory(barFolderName);
        deleteDirOnExit(tempFolder);
        IOUtil.deleteDir(tempFolder);
        barFile = IOUtil.createTempFileInDefaultTempDirectory(barFolderName, ".bar");
        IOUtil.deleteFile(barFile, 2, 3);
    }

    @After
    public void after() throws IOException {
        IOUtil.deleteFile(barFile, 1, 0);
        IOUtil.deleteDir(tempFolder);
    }

    @Test(expected = InvalidBusinessArchiveFormatException.class)
    public void invalidBOSHashIsRejected() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyBOSProcess", "1.0");
        final DesignProcessDefinition process = processDefinitionBuilder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process).done();
        BusinessArchiveFactory.writeBusinessArchiveToFolder(businessArchive, tempFolder);
        final File infoFile = getFile(ProcessDefinitionBARContribution.PROCESS_INFOS_FILE);
        IOUtil.writeContentToFile("bad process infos", infoFile);
        BusinessArchiveFactory.readBusinessArchive(tempFolder);
    }

    private File getFile(final String fileName) {
        final File infoFile = new File(tempFolder, fileName);
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                IOUtil.deleteFile(infoFile, 1, 0);
            }
        });
        return infoFile;
    }

    @Test(expected = InvalidBusinessArchiveFormatException.class)
    public void barWithNoHashIsRejected() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyBOSProcess", "1.0");
        final DesignProcessDefinition process = processDefinitionBuilder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process).done();
        BusinessArchiveFactory.writeBusinessArchiveToFolder(businessArchive, tempFolder);
        final File infoFile = getFile(ProcessDefinitionBARContribution.PROCESS_INFOS_FILE);
        infoFile.delete();
        BusinessArchiveFactory.readBusinessArchive(tempFolder);
    }

    @Test
    public void createBusinessArchiveFolder() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        final DesignProcessDefinition process = processDefinitionBuilder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process).done();
        BusinessArchiveFactory.writeBusinessArchiveToFolder(businessArchive, tempFolder);
        assertTrue(tempFolder.exists());
        assertTrue(tempFolder.isDirectory());
        final File file = getFile(ProcessDefinitionBARContribution.PROCESS_DEFINITION_XML);
        assertTrue(file.exists());
        assertFalse(file.isDirectory());
    }

    @Test
    public void createBusinessArchiveFileFromFolder() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        final DesignProcessDefinition process = processDefinitionBuilder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process).done();
        BusinessArchiveFactory.writeBusinessArchiveToFolder(businessArchive, tempFolder);

        BusinessArchiveFactory.businessArchiveFolderToFile(barFile, tempFolder.getAbsolutePath());
        assertTrue(barFile.exists());

        final InputStream inputStream = new FileInputStream(barFile);
        final BusinessArchive businessArchive2;
        try {
            businessArchive2 = BusinessArchiveFactory.readBusinessArchive(inputStream);
        } finally {
            inputStream.close();
        }
        final ProcessDefinition result = businessArchive2.getProcessDefinition();

        assertEquals(process, result);
    }

    @Test
    public void createBusinessArchiveFromFile() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        final DesignProcessDefinition process = processDefinitionBuilder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process).done();
        BusinessArchiveFactory.writeBusinessArchiveToFolder(businessArchive, tempFolder);

        BusinessArchiveFactory.businessArchiveFolderToFile(barFile, tempFolder.getAbsolutePath());
        assertTrue(barFile.exists());
        final BusinessArchive businessArchive2 = BusinessArchiveFactory.readBusinessArchive(barFile);
        final ProcessDefinition result = businessArchive2.getProcessDefinition();

        assertEquals(process, result);
    }

    @Test(expected = InvalidBusinessArchiveFormatException.class)
    public void readInvalidBusinessArchive() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        final DesignProcessDefinition process = processDefinitionBuilder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process).done();
        BusinessArchiveFactory.writeBusinessArchiveToFolder(businessArchive, tempFolder);
        final File file = getFile(ProcessDefinitionBARContribution.PROCESS_DEFINITION_XML);
        file.delete();
        createNewFile(file);
        final FileWriter fileWriter = new FileWriter(file);
        fileWriter.write("test");
        fileWriter.flush();
        fileWriter.close();

        BusinessArchiveFactory.readBusinessArchive(tempFolder);
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

    @Test(expected = InvalidBusinessArchiveFormatException.class)
    public void createEmptyBusinessArchive() throws Exception {
        new BusinessArchiveBuilder().createNewBusinessArchive().done();
    }

    @Test
    public void exportBusinessArchiveAsFile() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDefinitionBuilder.done())
                .done();
        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, barFile);
        assertTrue(barFile.exists());
        assertFalse(barFile.isDirectory());
    }

    @Test(expected = IOException.class)
    public void exportBusinessArchiveAsFileOnExistingFile() throws Exception {
        barFile = IOUtil.createTempFileInDefaultTempDirectory("exportBusinessArchiveAsFileOnExistingFile-businessArchive", ".bar");
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        final DesignProcessDefinition process = processDefinitionBuilder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process).done();
        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, barFile);
    }

    @Test
    public void readBusinessArchive() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        final DesignProcessDefinition process = processDefinitionBuilder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process).done();
        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, barFile);

        final InputStream inputStream = new FileInputStream(barFile);
        final BusinessArchive businessArchive2;
        try {
            businessArchive2 = BusinessArchiveFactory.readBusinessArchive(inputStream);
        } finally {
            inputStream.close();
        }

        final ProcessDefinition result = businessArchive2.getProcessDefinition();
        assertEquals(process, result);
    }

    @Test(expected = InvalidBusinessArchiveFormatException.class)
    public void importOldBusinessArchiveFail() throws Exception {
        final InputStream resourceAsStream = this.getClass().getResourceAsStream("MyProcess--1.0.bar");
        try {
            BusinessArchiveFactory.readBusinessArchive(resourceAsStream);
        } finally {
            resourceAsStream.close();
        }
    }

    @Test(expected = InvalidBusinessArchiveFormatException.class)
    public void importOldBusinessArchiveFileFail() throws Exception {
        final InputStream inputStream = this.getClass().getResourceAsStream("MyProcess--1.0.bar");
        final OutputStream out = new FileOutputStream(barFile);
        try {
            final byte buf[] = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            out.close();
            inputStream.close();
        }

        BusinessArchiveFactory.readBusinessArchive(barFile);
    }

    @Test
    public void manageBusinessArchiveResources() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDefinitionBuilder.done())
                .addExternalResource(new BarResource("dummy.txt", new byte[] { 'a', 'b', 'c', 'd' })).done();

        // Add a resource to the biz archive:
        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, barFile);

        // read from the file
        final BusinessArchive readBusinessArchive = BusinessArchiveFactory.readBusinessArchive(barFile);
        assertTrue("Added resource not found in BusinessArchive", readBusinessArchive.getResources().containsKey("resources/dummy.txt"));
    }

    @Test
    public void formMappingInBarShouldBeWrittenAndReadProperly() throws Exception {
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("MethCookingPlanning", "Season 5").done();

        final FormMappingModel formMappingModel = new FormMappingModel();
        formMappingModel.addFormMapping(new FormMappingDefinition("/?myPageTokenID", FormMappingType.PROCESS_START, FormMappingTarget.INTERNAL));
        formMappingModel.addFormMapping(new FormMappingDefinition("someExternalPage", FormMappingType.TASK, FormMappingTarget.URL, "requestTask"));

        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition)
                .setFormMappings(formMappingModel).done();

        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, barFile);

        final BusinessArchive readBusinessArchive = BusinessArchiveFactory.readBusinessArchive(barFile);
        assertThat(readBusinessArchive.getFormMappingModel().getFormMappings()).as("Form Mapping should be found in BusinessArchive").hasSize(2);
    }

    @Test
    public void putActorMappingInBar() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("ProductionPlanning", "3.1");
        final DesignProcessDefinition designProcessDefinition = processDefinitionBuilder.done();
        // Add a resource to the biz archive:
        final byte[] xmlBytes = "<toto>text</toto>".getBytes();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition)
                .setActorMapping(xmlBytes).done();

        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, barFile);

        // read from the file
        final BusinessArchive readBusinessArchive = BusinessArchiveFactory.readBusinessArchive(barFile);
        // final ProcessDefinition processDefinition = processAPI.deploy(readBusinessArchive);
        assertTrue("Actor Mapping not found in BusinessArchive",
                Arrays.equals(xmlBytes, readBusinessArchive.getResource(ActorMappingContribution.ACTOR_MAPPING_FILE)));
    }

    @Test
    public void readProcessFromBusinessArchive() throws Exception {
        final Expression trueExpression = new ExpressionBuilder().createConstantBooleanExpression(true);
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        processDefinitionBuilder.addDocumentDefinition("testDoc").addContentFileName("testFile.txt").addFile("testFile.txt").addDescription("desc")
                .addMimeType("text/plain");
        processDefinitionBuilder.addDocumentDefinition("testDocUrl").addContentFileName("testFile.txt").addUrl("http://test.com/testFile.txt")
                .addDescription("desc");
        processDefinitionBuilder.addDescription("a very good description");
        processDefinitionBuilder.addDisplayDescription("A very good and clean description that will be displayed in user xp");
        processDefinitionBuilder.addDisplayName("Truck Handling Process");
        processDefinitionBuilder.addActor("Truck Driver").addDescription("A man that is driving bigs trucks");
        processDefinitionBuilder.addStartEvent("start1").addTimerEventTriggerDefinition(TimerType.CYCLE,
                new ExpressionBuilder().createConstantStringExpression("*/3 * * * * ?"));
        // No Java operation, so empty string passed:
        processDefinitionBuilder
                .addAutomaticTask("auto1")
                .addOperation(new LeftOperandBuilder().createNewInstance().setName("testData").done(), OperatorType.ASSIGNMENT, ASSIGN_OPERATOR, null,
                        trueExpression).addConnector("conn1", "connId1", "1.0.0", ConnectorEvent.ON_FINISH).ignoreError();
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
        final String ERROR_CODE = "errorToBeCaught";
        addUserTask.addConnector("conn2", "connId2", "1.0.0", ConnectorEvent.ON_ENTER).throwErrorEventWhenFailed(ERROR_CODE);
        addUserTask.addUserFilter("myUserFilter", "org.bonitasoft.test.user.filter", "1.0.0");
        addUserTask.addData("testData", String.class.getName(), null);
        addUserTask.addShortTextData("shortText", new ExpressionBuilder().createConstantStringExpression("shortText"));
        addUserTask.addLongTextData("longText", new ExpressionBuilder().createConstantStringExpression("longText"));
        processDefinitionBuilder.addGateway("gate1", GatewayType.INCLUSIVE).addDefaultTransition("user1");
        processDefinitionBuilder.addEndEvent("end1");
        processDefinitionBuilder.addTransition("start1", "auto1", trueExpression);
        processDefinitionBuilder.addTransition("auto1", "intermediateTimerEvent");
        processDefinitionBuilder.addTransition("intermediateTimerEvent", "user1");
        processDefinitionBuilder.addTransition("user1", "gate1");
        processDefinitionBuilder.addTransition("user1", "end1");
        processDefinitionBuilder
                .addConnector("conn3", "connId3", "1.0.0", ConnectorEvent.ON_FINISH)
                .ignoreError()
                .addInput("input1", trueExpression)
                .addOutput(new LeftOperandBuilder().createNewInstance().setName("testData").done(), OperatorType.ASSIGNMENT, ASSIGN_OPERATOR, null,
                        trueExpression);
        processDefinitionBuilder.addData("myData", "java.lang.Boolean", trueExpression).addDescription("My boolean data");
        final DesignProcessDefinition process = processDefinitionBuilder.done();

        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process)
                .addDocumentResource(new BarResource("testFile.txt", new byte[] { 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 })).done();
        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, barFile);

        final InputStream inputStream = new FileInputStream(barFile);
        final BusinessArchive businessArchive2;
        try {
            businessArchive2 = BusinessArchiveFactory.readBusinessArchive(inputStream);
        } finally {
            inputStream.close();
        }

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
        assertEquals(process.getActorsList().size(), result.getActorsList().size());
        assertEquals(process.getActorsList().iterator().next(), result.getActorsList().iterator().next());
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
        assertEquals(3, user1.getDataDefinitions().size());
        final DataDefinition dataDefinition1 = user1.getDataDefinitions().get(0);
        final DataDefinition dataDefinition2 = user1.getDataDefinitions().get(1);
        final DataDefinition dataDefinition3 = user1.getDataDefinitions().get(2);
        assertEquals("testData", dataDefinition1.getName());
        assertEquals("shortText", dataDefinition2.getName());
        assertEquals("longText", dataDefinition3.getName());
        assertTrue(dataDefinition2 instanceof TextDataDefinition);
        assertTrue(dataDefinition3 instanceof TextDataDefinition);
        assertFalse(((TextDataDefinition) dataDefinition2).isLongText());
        assertTrue(((TextDataDefinition) dataDefinition3).isLongText());
        assertEquals(1, auto1.getOperations().size());
        assertEquals(auto1.getOperations().get(0), auto1.getOperations().get(0));
        assertTrue(procAct2.equals(resAct1) || procAct2.equals(resAct2) || procAct2.equals(resAct3));
        assertEquals(process.getProcessContainer().getGatewaysList().size(), result.getProcessContainer().getGatewaysList().size());
        assertEquals(process.getProcessContainer().getGatewaysList().iterator().next(), result.getProcessContainer().getGatewaysList().iterator().next());
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
        final Iterator<ConnectorDefinition> resultConnectors = result.getProcessContainer().getConnectors().iterator();
        for (final ConnectorDefinition connectorDef : process.getProcessContainer().getConnectors()) {
            final ConnectorDefinition resultConn = resultConnectors.next();
            assertEquals(connectorDef, resultConn);
        }
        assertEquals(process.getProcessContainer().getDataDefinitions().size(), result.getProcessContainer().getDataDefinitions().size());
        assertEquals(process.getProcessContainer().getDataDefinitions().iterator().next(), result.getProcessContainer().getDataDefinitions().iterator().next());
        final ActivityDefinition sourceActivity = process.getProcessContainer().getActivity("user1");
        final ActivityDefinition resultActivity = result.getProcessContainer().getActivity("user1");
        assertTrue(resultActivity instanceof UserTaskDefinition);
        assertEquals(((UserTaskDefinition) sourceActivity).getUserFilter(), ((UserTaskDefinition) resultActivity).getUserFilter());

        final List<ActivityDefinition> processActivities = process.getProcessContainer().getActivities();
        final Iterator<ActivityDefinition> itResultActs = result.getProcessContainer().getActivities().iterator();
        for (final ActivityDefinition processActivity : processActivities) {
            final ActivityDefinition resultAct = itResultActs.next();
            final List<ConnectorDefinition> processActivityConnectors = processActivity.getConnectors();
            final Iterator<ConnectorDefinition> itResultCon = resultAct.getConnectors().iterator();
            for (final ConnectorDefinition connectorDefinition : processActivityConnectors) {
                final ConnectorDefinition nextResultConnector = itResultCon.next();
                assertEquals(connectorDefinition.getFailAction(), nextResultConnector.getFailAction());
            }
        }
    }

    @Test
    public void readProcessWithContract() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("contract", "1.0");
        builder.addActor("myActor");
        final ContractDefinitionBuilder contractDefinitionBuilder = builder.addUserTask("step1", "myActor").addContract();
        createContract(contractDefinitionBuilder);
        createContract(builder.addContract());
        final DesignProcessDefinition process = builder.getProcess();
        final DesignProcessDefinition result = getDesignProcessDefinition(builder);

        assertThat(process).isEqualTo(result);
    }

    void createContract(final ContractDefinitionBuilder contractDefinitionBuilder) {
        contractDefinitionBuilder.addInput("numberOfDays", Type.INTEGER, null).addConstraint("Mystical constraint", "true", null, "numberOfDays");
        final InputDefinition childText = new InputDefinitionImpl("childText", Type.TEXT, "a text simple input");
        final InputDefinition childDecimal = new InputDefinitionImpl("childDecimal", Type.DECIMAL, "a decimal simple input");
        contractDefinitionBuilder.addInput("complex", "a complex input", Arrays.asList(childText, childDecimal));
    }

    @Test
    public void readProcessWithMessageEventsFromBusinessArchive() throws Exception {
        final Expression conditionKey = new ExpressionBuilder().createConstantStringExpression("coditionKey");
        final Expression trueExpression = new ExpressionBuilder().createConstantBooleanExpression(true);
        final Expression displayNameExpression = new ExpressionBuilder().createConstantStringExpression("dataToSend");
        final LeftOperandBuilder leftOperandBuilder = new LeftOperandBuilder();
        leftOperandBuilder.createNewInstance().setName("var1");
        final OperationBuilder opb = new OperationBuilder();
        opb.createNewInstance().setOperator(ASSIGN_OPERATOR).setRightOperand(trueExpression).setType(OperatorType.ASSIGNMENT)
                .setLeftOperand(leftOperandBuilder.done());

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        builder.addBooleanData("var1", null);
        builder.addStartEvent("start1").addMessageEventTrigger("m1").addOperation(opb.done());
        builder.addAutomaticTask("auto1");
        final CatchMessageEventTriggerDefinitionBuilder catchMessageEventTriggerDefinitionBuilder = builder.addIntermediateCatchEvent("waitForMessage")
                .addMessageEventTrigger("m2");
        catchMessageEventTriggerDefinitionBuilder.addCorrelation(conditionKey, trueExpression);
        catchMessageEventTriggerDefinitionBuilder.addOperation(opb.done());
        // create expression for target process/flowNode
        Expression targetProcess = new ExpressionBuilder().createDataExpression("p3", String.class.getName());
        final Expression receiveMessage = new ExpressionBuilder().createDataExpression("receiveMessage", String.class.getName());
        builder.addIntermediateThrowEvent("sendMessage").addMessageEventTrigger("m4", targetProcess, receiveMessage);
        targetProcess = new ExpressionBuilder().createConstantStringExpression("p2");
        final Expression waitMessage = new ExpressionBuilder().createConstantStringExpression("waitMessage");
        final ThrowMessageEventTriggerBuilder throwMessageEventTriggerBuilder = builder.addEndEvent("end1").addMessageEventTrigger("m2", targetProcess,
                waitMessage);
        throwMessageEventTriggerBuilder.addCorrelation(conditionKey, trueExpression);
        throwMessageEventTriggerBuilder.addMessageContentExpression(displayNameExpression, trueExpression);
        builder.addTransition("start1", "auto1", trueExpression);
        builder.addTransition("auto1", "waitForMessage");
        builder.addTransition("waitForMessage", "sendMessage");
        builder.addTransition("sendMessage", "end1");

        final DesignProcessDefinition process = builder.getProcess();
        final DesignProcessDefinition result = getDesignProcessDefinition(builder);

        checkProcessForMessagesEvents(process, result);
    }

    @Test(expected = InvalidProcessDefinitionException.class)
    public void tooMuchCorrelationOnCatchMessage() throws Exception {
        final Expression conditionKey = new ExpressionBuilder().createConstantStringExpression("coditionKey");
        final Expression trueExpression = new ExpressionBuilder().createConstantBooleanExpression(true);
        final Expression displayNameExpression = new ExpressionBuilder().createConstantStringExpression("dataToSend");

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        processDefinitionBuilder.addStartEvent("start1").addMessageEventTrigger("m1");
        processDefinitionBuilder.addAutomaticTask("auto1");
        final CatchMessageEventTriggerDefinitionBuilder catchMessageEventTriggerDefinitionBuilder = processDefinitionBuilder.addIntermediateCatchEvent(
                "waitForMessage").addMessageEventTrigger("m2");
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
        final CatchMessageEventTriggerDefinitionBuilder catchMessageEventTriggerDefinitionBuilder = processDefinitionBuilder.addIntermediateCatchEvent(
                "waitForMessage").addMessageEventTrigger("m2");
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
        final Expression fromCallerData = new ExpressionBuilder().createDataExpression("var1", Boolean.class.getName());
        final LeftOperand dataInputLeftOp = new LeftOperandBuilder().createNewInstance("data1").done();
        final Operation dataInputOperation = getOperation(fromCallerData, dataInputLeftOp);

        final Expression fromCallableElementData = new ExpressionBuilder().createDataExpression("data2", Integer.class.getName());
        final LeftOperand dataOutputOp = new LeftOperandBuilder().createNewInstance("var2").done();
        final Operation dataOutputOperation = getOperation(fromCallableElementData, dataOutputOp);

        final Expression targetProcess = new ExpressionBuilder().createConstantStringExpression("MyProcess2");
        final Expression processVersion = new ExpressionBuilder().createConstantStringExpression("1.0");

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        builder.addBooleanData("var1", null);
        builder.addIntegerData("var2", null);
        builder.addStartEvent("start1");
        final CallActivityBuilder callActivityBuilder = builder.addCallActivity("callActivity", targetProcess, processVersion);
        callActivityBuilder.addDataInputOperation(dataInputOperation);
        callActivityBuilder.addDataOutputOperation(dataOutputOperation);
        builder.addEndEvent("end1");
        builder.addTransition("start1", "callActivity");
        builder.addTransition("callActivity", "end1");

        final DesignProcessDefinition process = builder.getProcess();
        final DesignProcessDefinition result = getDesignProcessDefinition(builder);

        checkProcessForCallActivity(process, result);
    }

    @Test
    public void readProcessWithMultiInstanceFromBusinessArchive() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithMultiInstances", "1.0");
        builder.addData("inputList", List.class.getName(), null).addData("outputList", List.class.getName(), null);
        final AutomaticTaskDefinitionBuilder automaticTaskBuilder = builder.addAutomaticTask("auto1");
        automaticTaskBuilder.addShortTextData("input", null).addShortTextData("output", null);
        final MultiInstanceLoopCharacteristicsBuilder multiInstance1 = automaticTaskBuilder.addMultiInstance(false, "inputList");
        multiInstance1.addDataInputItemRef("input");
        multiInstance1.addDataOutputItemRef("output");
        multiInstance1.addLoopDataOutputRef("outputList");
        final MultiInstanceLoopCharacteristicsBuilder multiInstance2 = builder.addAutomaticTask("auto2").addMultiInstance(true,
                new ExpressionBuilder().createConstantIntegerExpression(5));
        multiInstance2.addCompletionCondition(new ExpressionBuilder().createConstantBooleanExpression(false));
        builder.addAutomaticTask("auto3").addLoop(true, new ExpressionBuilder().createConstantBooleanExpression(true),
                new ExpressionBuilder().createConstantIntegerExpression(5));
        final DesignProcessDefinition result = getDesignProcessDefinition(builder);

        final AutomaticTaskDefinition auto1 = (AutomaticTaskDefinition) result.getProcessContainer().getFlowNode("auto1");
        final MultiInstanceLoopCharacteristics multi1 = (MultiInstanceLoopCharacteristics) auto1.getLoopCharacteristics();
        assertEquals(false, multi1.isSequential());
        assertEquals("inputList", multi1.getLoopDataInputRef());
        assertEquals("outputList", multi1.getLoopDataOutputRef());
        assertEquals("input", multi1.getDataInputItemRef());
        assertEquals("output", multi1.getDataOutputItemRef());

        final AutomaticTaskDefinition auto2 = (AutomaticTaskDefinition) result.getProcessContainer().getFlowNode("auto2");
        final MultiInstanceLoopCharacteristics multi2 = (MultiInstanceLoopCharacteristics) auto2.getLoopCharacteristics();
        assertEquals(true, multi2.isSequential());
        assertEquals("5", multi2.getLoopCardinality().getContent());
        assertEquals("false", multi2.getCompletionCondition().getContent());

        final AutomaticTaskDefinition auto3 = (AutomaticTaskDefinition) result.getProcessContainer().getFlowNode("auto3");
        final StandardLoopCharacteristics loop2 = (StandardLoopCharacteristics) auto3.getLoopCharacteristics();
        assertEquals(true, loop2.isTestBefore());
        assertEquals("true", loop2.getLoopCondition().getContent());
        assertEquals("5", loop2.getLoopMax().getContent());
    }

    @Test
    public void readProcessWithBoundaryEventsFromBusinessArchive() throws Exception {
        final Expression timerExpression = new ExpressionBuilder().createConstantLongExpression(1000);
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        builder.addStartEvent("start1");
        final UserTaskDefinitionBuilder userTaskDefinitionBuilder = builder.addUserTask("userTask", "delivery");
        userTaskDefinitionBuilder.addBoundaryEvent("b1", true).addTimerEventTriggerDefinition(TimerType.DURATION, timerExpression);
        userTaskDefinitionBuilder.addBoundaryEvent("b2", true).addMessageEventTrigger("m1");
        userTaskDefinitionBuilder.addBoundaryEvent("b3", true).addErrorEventTrigger("e1");
        builder.addAutomaticTask("exceptionFlowB1");
        builder.addAutomaticTask("exceptionFlowB2");
        builder.addAutomaticTask("exceptionFlowB3");
        builder.addEndEvent("end1");
        builder.addTransition("start1", "userTask");
        builder.addTransition("userTask", "end1");
        builder.addTransition("b1", "exceptionFlowB1");
        builder.addTransition("b2", "exceptionFlowB2");
        builder.addTransition("b3", "exceptionFlowB3");

        final DesignProcessDefinition process = builder.getProcess();
        final DesignProcessDefinition result = getDesignProcessDefinition(builder);

        checkProcessForBoundaryEvents(process, result, true);
    }

    @Test
    public void readProcessWithNonInterruptingBoundaryEventsFromBusinessArchive() throws Exception {
        final Expression timerExpression = new ExpressionBuilder().createConstantLongExpression(1000);
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        builder.addStartEvent("start1");
        final UserTaskDefinitionBuilder userTaskDefinitionBuilder = builder.addUserTask("userTask", "delivery");
        userTaskDefinitionBuilder.addBoundaryEvent("b1", false).addTimerEventTriggerDefinition(TimerType.DURATION, timerExpression);
        builder.addAutomaticTask("exceptionFlowB1");
        builder.addEndEvent("end1");
        builder.addTransition("start1", "userTask");
        builder.addTransition("userTask", "end1");
        builder.addTransition("b1", "exceptionFlowB1");

        final DesignProcessDefinition process = builder.getProcess();
        final DesignProcessDefinition result = getDesignProcessDefinition(builder);

        checkProcessForBoundaryEvents(process, result, false);
    }

    @Test
    public void readProcessWithSendTaskFromBusinessArchive() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        final SendTaskDefinitionBuilder sendTaskDefinitionBuilder = builder.addSendTask("sendTask", "messageName",
                new ExpressionBuilder().createConstantStringExpression("processName"));
        sendTaskDefinitionBuilder.setTargetFlowNode(new ExpressionBuilder().createConstantStringExpression("flowNodeName"));
        sendTaskDefinitionBuilder.addCorrelation(new ExpressionBuilder().createConstantStringExpression("un"),
                new ExpressionBuilder().createConstantStringExpression("value"));
        sendTaskDefinitionBuilder.addMessageContentExpression(new ExpressionBuilder().createConstantStringExpression("myData"),
                new ExpressionBuilder().createConstantStringExpression("dataValue"));
        final DesignProcessDefinition result = getDesignProcessDefinition(builder);

        final SendTaskDefinition flowNode = (SendTaskDefinition) result.getProcessContainer().getFlowNode("sendTask");
        assertEquals("sendTask", flowNode.getName());
        final ThrowMessageEventTriggerDefinition messageTrigger = flowNode.getMessageTrigger();
        assertEquals("processName", messageTrigger.getTargetProcess().getContent());
        assertEquals("messageName", messageTrigger.getMessageName());
        assertEquals("flowNodeName", messageTrigger.getTargetFlowNode().getContent());
        assertEquals("myData", messageTrigger.getDataDefinitions().get(0).getName());
        assertEquals("java.lang.String", messageTrigger.getDataDefinitions().get(0).getClassName());
        assertEquals("dataValue", messageTrigger.getDataDefinitions().get(0).getDefaultValueExpression().getContent());
        assertEquals("un", messageTrigger.getCorrelations().get(0).getKey().getContent());
        assertEquals("value", messageTrigger.getCorrelations().get(0).getValue().getContent());
    }

    @Test(expected = InvalidProcessDefinitionException.class)
    public void unableToExportSendTaskWithNoMessageName() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        final SendTaskDefinitionBuilder sendTaskDefinitionBuilder = builder.addSendTask("sendTask", null,
                new ExpressionBuilder().createConstantStringExpression("processName"));
        sendTaskDefinitionBuilder.setTargetFlowNode(new ExpressionBuilder().createConstantStringExpression("flowNodeName"));
        sendTaskDefinitionBuilder.addCorrelation(new ExpressionBuilder().createConstantStringExpression("un"),
                new ExpressionBuilder().createConstantStringExpression("value"));
        sendTaskDefinitionBuilder.addMessageContentExpression(new ExpressionBuilder().createConstantStringExpression("myData"),
                new ExpressionBuilder().createConstantStringExpression("dataValue"));
        builder.done();
    }

    @Test
    public void readProcessWithThowErrorEventFromBusinessArchive() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("MyProcess", "1.0");
        builder.addStartEvent("start1");
        builder.addAutomaticTask("a1");
        builder.addEndEvent("end1").addErrorEventTrigger("e1");
        builder.addTransition("start1", "a1");
        builder.addTransition("a1", "end1");
        final DesignProcessDefinition process = builder.getProcess();
        final DesignProcessDefinition result = getDesignProcessDefinition(builder);

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
    }

    private Operation getOperation(final Expression rightOperand, final LeftOperand leftOperand) {
        final OperationBuilder opb = new OperationBuilder().createNewInstance();
        opb.setLeftOperand(leftOperand);
        opb.setOperator(ASSIGN_OPERATOR);
        opb.setRightOperand(rightOperand);
        opb.setType(OperatorType.ASSIGNMENT);
        return opb.done();
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

    private void checkProcessForBoundaryEvents(final DesignProcessDefinition process, final DesignProcessDefinition result, final boolean isInterrupting) {
        assertEquals(process.getName(), result.getName());
        assertEquals(process.getVersion(), result.getVersion());

        assertEquals(process.getProcessContainer().getStartEvents(), result.getProcessContainer().getStartEvents());
        assertEquals(1, result.getProcessContainer().getStartEvents().size());

        assertEquals(process.getProcessContainer().getActivities().size(), result.getProcessContainer().getActivities().size());
        final ActivityDefinition resultActivity = result.getProcessContainer().getActivity("userTask");
        final ActivityDefinition origActivity = process.getProcessContainer().getActivity("userTask");
        assertEquals(origActivity, resultActivity);
        assertEquals(origActivity.getBoundaryEventDefinitions().size(), resultActivity.getBoundaryEventDefinitions().size());
        for (final BoundaryEventDefinition boundary : resultActivity.getBoundaryEventDefinitions()) {
            assertEquals(1, boundary.getEventTriggers().size());
            assertEquals(isInterrupting, boundary.isInterrupting());
        }

        assertEquals(process.getProcessContainer().getEndEvents(), result.getProcessContainer().getEndEvents());
        assertEquals(1, result.getProcessContainer().getEndEvents().size());
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

        assertEquals(process.getActorsList().size(), result.getActorsList().size());
        assertEquals(process.getProcessContainer().getActivities().size(), result.getProcessContainer().getActivities().size());
        assertEquals(process.getProcessContainer().getGatewaysList().size(), result.getProcessContainer().getGatewaysList().size());
        assertEquals(process.getProcessContainer().getTransitions().size(), result.getProcessContainer().getTransitions().size());
        assertEquals(process.getProcessContainer().getDataDefinitions().size(), result.getProcessContainer().getDataDefinitions().size());
    }

    @Test
    public void checkErrorMessageOnInvalidTransition() {
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
    public void readProcessWithAnActorWithADescription() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("MyProcessTT", "1.0");
        builder.addActor("Truck Driver").addDescription("desc");
        builder.addUserTask("step", "Truck Driver");

        final DesignProcessDefinition result = getDesignProcessDefinition(builder);
        final List<ActorDefinition> actors = result.getActorsList();
        assertEquals(1, actors.size());
        final ActorDefinition actor = actors.iterator().next();
        assertEquals("Truck Driver", actor.getName());
        assertEquals("desc", actor.getDescription());
    }

    private DesignProcessDefinition getDesignProcessDefinition(final ProcessDefinitionBuilder builder) throws Exception {
        final DesignProcessDefinition process = builder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process).done();
        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, barFile);

        final InputStream inputStream = new FileInputStream(barFile);
        try {
            final BusinessArchive businessArchive2 = BusinessArchiveFactory.readBusinessArchive(inputStream);
            return businessArchive2.getProcessDefinition();
        } finally {
            inputStream.close();
        }
    }

    @Test
    public void readProcessWithActorWithoutDescription() throws Exception {
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

        final DesignProcessDefinition process = processDefinitionBuilder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process).done();
        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, barFile);

        final InputStream inputStream = new FileInputStream(barFile);
        final BusinessArchive businessArchive2;
        try {
            businessArchive2 = BusinessArchiveFactory.readBusinessArchive(inputStream);
        } finally {
            inputStream.close();
        }

        final DesignProcessDefinition result = businessArchive2.getProcessDefinition();
        assertEquals(process.getName(), result.getName());
        assertEquals(process.getVersion(), result.getVersion());
        assertEquals(process.getProcessContainer().getStartEvents().size(), result.getProcessContainer().getStartEvents().size());
        assertEquals(process.getProcessContainer().getStartEvents().get(0), result.getProcessContainer().getStartEvents().get(0));
        assertEquals(process.getProcessContainer().getEndEvents().size(), result.getProcessContainer().getEndEvents().size());
        assertEquals(process.getProcessContainer().getEndEvents().get(0), result.getProcessContainer().getEndEvents().get(0));
        assertEquals(process.getActorsList().size(), result.getActorsList().size());
        assertEquals(process.getActorsList().iterator().next(), result.getActorsList().iterator().next());
        assertEquals(process.getProcessContainer().getActivities().size(), result.getProcessContainer().getActivities().size());
        final Iterator<ActivityDefinition> iterator = result.getProcessContainer().getActivities().iterator();
        final Iterator<ActivityDefinition> iterator2 = process.getProcessContainer().getActivities().iterator();
        final ActivityDefinition procAct1 = iterator2.next();
        final ActivityDefinition procAct2 = iterator2.next();
        final ActivityDefinition resAct1 = iterator.next();
        final ActivityDefinition resAct2 = iterator.next();
        assertTrue(procAct1.equals(resAct1) || procAct1.equals(resAct2));
        assertTrue(procAct2.equals(resAct1) || procAct2.equals(resAct2));
        assertEquals(process.getProcessContainer().getGatewaysList().size(), result.getProcessContainer().getGatewaysList().size());
        assertEquals(process.getProcessContainer().getGatewaysList().iterator().next(), result.getProcessContainer().getGatewaysList().iterator().next());
        assertEquals(process.getProcessContainer().getTransitions().size(), result.getProcessContainer().getTransitions().size());
        assertEquals(process.getProcessContainer().getConnectors().size(), result.getProcessContainer().getConnectors().size());
        assertEquals(process.getProcessContainer().getConnectors().iterator().next(), result.getProcessContainer().getConnectors().iterator().next());
    }

    @Test(expected = InvalidBusinessArchiveFormatException.class)
    public void readInvalidProcessFromBusinessArchive() throws Exception {
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

        final DesignProcessDefinition process = processDefinitionBuilder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process).done();
        BusinessArchiveFactory.writeBusinessArchiveToFolder(businessArchive, tempFolder);

        final File file = getFile(ProcessDefinitionBARContribution.PROCESS_DEFINITION_XML);
        String fileContent = IOUtil.read(file);
        fileContent = fileContent.replace("<processDefinition", "<porcessDefinition");
        fileContent = fileContent.replace("</processDefinition", "</porcessDefinition");
        file.delete();
        createNewFile(file);
        IOUtil.writeContentToFile(fileContent, file);
        BusinessArchiveFactory.readBusinessArchive(tempFolder);
    }

    @Test(expected = InvalidBusinessArchiveFormatException.class)
    public void readInvalidXMLProcessFromBusinessArchive() throws Exception {
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
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDefinitionBuilder.done())
                .done();
        BusinessArchiveFactory.writeBusinessArchiveToFolder(businessArchive, tempFolder);

        final File file = getFile(ProcessDefinitionBARContribution.PROCESS_DEFINITION_XML);
        String fileContent = IOUtil.read(file);
        fileContent = fileContent.replace("<processDefinition", "<porcessDefinition");
        file.delete();
        createNewFile(file);
        IOUtil.writeContentToFile(fileContent, file);
        BusinessArchiveFactory.readBusinessArchive(tempFolder);
    }

    private void createNewFile(final File file) throws IOException {
        file.createNewFile();
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                IOUtil.deleteFile(file, 1, 0);
            }
        });
    }

    private void deleteDirOnExit(final File directory) {
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                if (directory != null) {
                    try {
                        IOUtil.deleteDir(directory);
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Test(expected = InvalidProcessDefinitionException.class)
    public void createProcessWithADocumentHavingBothUrlAndFile() throws InvalidProcessDefinitionException {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("MyProcessWithExternalDocuments", "1.0");
        builder.addDocumentDefinition("myDoc").addContentFileName("testFile.txt").addDescription("a cool pdf document").addMimeType("application/pdf")
                .addFile("myPdf.pdf").addUrl("http://plop");
        builder.done();
    }

    @Test(expected = InvalidBusinessArchiveFormatException.class)
    public void createProcessWithAInBarDocumentMissingFile() throws InvalidProcessDefinitionException, InvalidBusinessArchiveFormatException {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("MyProcessWithExternalDocuments", "1.0");
        builder.addDocumentDefinition("myDoc").addContentFileName("testFile.txt").addDescription("a cool pdf document").addMimeType("application/pdf")
                .addFile("myPdf.pdf");
        final DesignProcessDefinition processDefinition = builder.done();
        new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDefinition)
                .addDocumentResource(new BarResource("testFile.txt", new byte[] { 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 })).done();
    }

    @Test(expected = InvalidProcessDefinitionException.class)
    public void createProcessWithConnectorHavingNullInput() throws InvalidProcessDefinitionException {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("processWithConnectorNullInput", "1.0");
        builder.addAutomaticTask("step1").addConnector("eee", "zzz", "eee", ConnectorEvent.ON_ENTER).addInput("name", null);
        builder.addConnector("eee", "zzz", "eee", ConnectorEvent.ON_ENTER).addInput("name", null);
        builder.done();
    }

    @Test
    public void subProcess() throws Exception {
        final Expression createdExpression = new ExpressionBuilder().createConstantBooleanExpression(false);

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder();
        builder.createNewInstance("process", "10.2").addSubProcess("subProcessActivity", true).getSubProcessBuilder().addStartEvent("start")
                .addTimerEventTriggerDefinition(TimerType.DURATION, new ExpressionBuilder().createConstantLongExpression(5000))
                .addBooleanData("created", createdExpression).addAutomaticTask("auto");

        final DesignProcessDefinition process = builder.getProcess();
        assertEquals(1, process.getProcessContainer().getActivities().size());
        final DesignProcessDefinition result = getDesignProcessDefinition(builder);
        assertEquals(1, result.getProcessContainer().getActivities().size());
    }

    @Test
    public void generatingOutgoingDefaultTransitionShouldBeConformToProcessDefinitionXsd() throws Exception {
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

        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDefinitionBuilder.done())
                .done();
        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, barFile);

        BusinessArchiveFactory.readBusinessArchive(barFile);
    }

    @Test(expected = InvalidBusinessArchiveFormatException.class)
    public void readBarWithConnectorFailActionsFails() throws Exception {
        final InputStream resourceAsStream = BusinessArchiveTest.class.getResourceAsStream("testBuy_a_mini_extended--6.1.bar");
        try {
            BusinessArchiveFactory.readBusinessArchive(resourceAsStream);
        } finally {
            resourceAsStream.close();
        }
    }

}
