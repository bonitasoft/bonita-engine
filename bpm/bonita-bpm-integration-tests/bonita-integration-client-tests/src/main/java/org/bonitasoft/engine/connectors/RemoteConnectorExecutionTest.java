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
package org.bonitasoft.engine.connectors;

import static org.bonitasoft.engine.matchers.ListElementMatcher.nameAre;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.connector.ArchiveConnectorInstancesSearchDescriptor;
import org.bonitasoft.engine.bpm.connector.ArchivedConnectorInstance;
import org.bonitasoft.engine.bpm.connector.ConnectorCriterion;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.connector.ConnectorImplementationDescriptor;
import org.bonitasoft.engine.bpm.connector.ConnectorInstance;
import org.bonitasoft.engine.bpm.connector.ConnectorInstancesSearchDescriptor;
import org.bonitasoft.engine.bpm.connector.ConnectorState;
import org.bonitasoft.engine.bpm.connector.FailAction;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentsSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityStates;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.BoundaryEventDefinition;
import org.bonitasoft.engine.bpm.flownode.ErrorEventTriggerDefinition;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.bpm.process.impl.ActivityDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.AutomaticTaskDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.BoundaryEventDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.CallActivityBuilder;
import org.bonitasoft.engine.bpm.process.impl.ConnectorDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ReceiveTaskDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.SendTaskDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.SubProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.connector.Connector;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionConstants;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.bonitasoft.engine.test.wait.WaitForVariableValue;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class RemoteConnectorExecutionTest extends ConnectorExecutionTest {

    private static final String CONNECTOR_OUTPUT_NAME = "output1";

    private static final String CONNECTOR_INPUT_NAME = "input1";

    private static final String CONNECTOR_WITH_OUTPUT_ID = "org.bonitasoft.connector.testConnectorWithOutput";

    @Test
    public void executeConnectorWithJNDILookupAndAPICall() throws Exception {
        final Expression localDataExpression = new ExpressionBuilder().createConstantLongExpression(0L);
        final Expression processNameExpression = new ExpressionBuilder().createConstantStringExpression("process");
        final Expression processVersionExpression = new ExpressionBuilder().createConstantStringExpression("1.0");
        final Expression outputExpression = new ExpressionBuilder().createInputExpression("processId", Long.class.getName());

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("process", "1.0");
        designProcessDefinition.addLongData("processId", localDataExpression);
        designProcessDefinition.addAutomaticTask("start");
        designProcessDefinition.addAutomaticTask("step1")
                .addConnector("myConnector", "org.bonitasoft.engine.connectors.TestConnectorWithAPICall", "1.0", ConnectorEvent.ON_FINISH)
                .addInput("processName", processNameExpression).addInput("processVersion", processVersionExpression)
                .addOutput(new OperationBuilder().createSetDataOperation("processId", outputExpression));
        designProcessDefinition.addAutomaticTask("end");
        designProcessDefinition.addTransition("start", "step1");
        designProcessDefinition.addTransition("step1", "end");

        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchiveBuilder.setProcessDefinition(designProcessDefinition.done());
        addConnectorToBusinessArchive(businessArchiveBuilder, TestConnectorWithAPICall.class);
        final BusinessArchive businessArchive = businessArchiveBuilder.done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);
        getProcessAPI().enableProcess(processDefinition.getId());

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForProcessToBeInState(processInstance, ProcessInstanceState.COMPLETED);

        // quelle api pour trouver une data dans une instance archiv�e?
        // Assert.assertEquals(processDefinition.getId(), getProcessAPI().getProcessDataInstance("processId", processInstance.getId()).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    private void addConnectorToBusinessArchive(final BusinessArchiveBuilder businessArchiveBuilder, final Class<? extends Connector> clazz) throws IOException {
        final String fileBaseName = clazz.getSimpleName();
        final String connectorImplResource = "/" + clazz.getName().replaceAll("\\.", "/") + ".impl";
        final byte[] descByteArray = IOUtil.getAllContentFrom(clazz.getResourceAsStream(connectorImplResource));
        businessArchiveBuilder.addConnectorImplementation(new BarResource(fileBaseName + ".impl", descByteArray));
        businessArchiveBuilder.addClasspathResource(new BarResource(fileBaseName + ".jar", IOUtil.generateJar(clazz)));
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "On finish", "Automatic activity", "Data output" }, story = "Test connector on finish of an automatic activity with data output.", jira = "")
    @Test
    public void executeConnectorOnFinishOfAnAutomaticActivityWithDataAsOutput() throws Exception {
        final String delivery = "Delivery men";
        final String valueOfInput1 = "valueOfInput1";
        final String defaultValue = "default";
        final Expression dataDefaultValue = new ExpressionBuilder().createConstantStringExpression(defaultValue);
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression(valueOfInput1);
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(
                "executeConnectorOnFinishOfAnAutomaticActivityWithDataAsOutput", "1.0");
        final String dataName = "myData1";
        designProcessDefinition.addShortTextData(dataName, dataDefaultValue);
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        final String inputName = CONNECTOR_INPUT_NAME;
        designProcessDefinition.addUserTask("step0", delivery);
        designProcessDefinition
                .addAutomaticTask("step1")
                .addConnector("myConnector", CONNECTOR_WITH_OUTPUT_ID, "1.0", ConnectorEvent.ON_FINISH)
                .addInput(inputName, input1Expression)
                .addOutput(new LeftOperandBuilder().createNewInstance().setName(dataName).done(), OperatorType.ASSIGNMENT, "=", "",
                        new ExpressionBuilder().createInputExpression(CONNECTOR_OUTPUT_NAME, String.class.getName()));
        designProcessDefinition.addUserTask("step2", delivery);
        designProcessDefinition.addTransition("step0", "step1");
        designProcessDefinition.addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployProcessWithDefaultTestConnector(delivery, johnUserId, designProcessDefinition, false);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        assertEquals(defaultValue, getProcessAPI().getProcessDataInstance(dataName, startProcess.getId()).getValue());
        waitForUserTaskAndExecuteIt("step0", startProcess, johnUserId);

        waitForUserTask("step2", startProcess);

        assertEquals(valueOfInput1, getProcessAPI().getProcessDataInstance(dataName, startProcess.getId()).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "On finish", "multi-instance", "activitity data" }, story = "Test connector on finish of an multi-instance activity with local data expression.", jira = "")
    @Test
    public void executeConnectorOnFinishOfMultiInstancedActivity() throws Exception {
        // deploy the process
        final String globalDataName = "globalData";
        final String localDataValue = "entry";
        final String userTaskName = "step2";
        final String multiTaskName = "multi";
        final String outputValue = "result";
        final ProcessDefinition processDefinition = deployProcessWithConnectorOnMutiInstance("localData", globalDataName, localDataValue, userTaskName,
                multiTaskName, outputValue, 2);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        // wait for the first multi-instance and execute it: the connector must be executed
        waitForUserTaskAndExecuteIt(multiTaskName, processInstance, johnUserId);

        // wait for the second multi-instance
        final ActivityInstance multiInstance = waitForUserTask(multiTaskName, processInstance);

        // check the data value
        DataInstance globalData = getProcessAPI().getProcessDataInstance(globalDataName, processInstance.getId());
        assertEquals(outputValue, globalData.getValue());

        // change the data value to be sure that the connector will be executed in the second instance
        getProcessAPI().updateProcessDataInstance(globalDataName, processInstance.getId(), "updated");

        // execute the second multi-instance: the connector must be executed again
        assignAndExecuteStep(multiInstance, johnUserId);

        // wait for user task that follows the multi task
        waitForUserTask(userTaskName, processInstance);

        // check the data value
        globalData = getProcessAPI().getProcessDataInstance(globalDataName, processInstance.getId());
        assertEquals(outputValue, globalData.getValue());

        // delete process
        disableAndDeleteProcess(processDefinition);
    }

    private ProcessDefinition deployProcessWithConnectorOnMutiInstance(final String localDataName, final String globalDataName, final String localDataValue,
            final String userTaskName, final String multiTaskName, final String outputValue, final int nbOfInstances) throws BonitaException, IOException {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("connectorOnMultiInstance", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addShortTextData(globalDataName, new ExpressionBuilder().createConstantStringExpression("initial"));
        builder.addStartEvent("start");
        final UserTaskDefinitionBuilder taskBuilder = builder.addUserTask(multiTaskName, ACTOR_NAME);
        taskBuilder.addMultiInstance(true, new ExpressionBuilder().createConstantIntegerExpression(nbOfInstances));
        taskBuilder.addShortTextData(localDataName, new ExpressionBuilder().createConstantStringExpression(localDataValue));
        taskBuilder
                .addConnector("connectorOnFinish", CONNECTOR_WITH_OUTPUT_ID, "1.0", ConnectorEvent.ON_FINISH)
                .addInput(CONNECTOR_INPUT_NAME, new ExpressionBuilder().createDataExpression(localDataName, String.class.getName()))
                .addOutput(new LeftOperandBuilder().createDataLeftOperand(globalDataName), OperatorType.ASSIGNMENT, "=", "",
                        new ExpressionBuilder().createConstantStringExpression(outputValue));
        builder.addUserTask(userTaskName, ACTOR_NAME);
        builder.addEndEvent("end");
        builder.addTransition("start", multiTaskName);
        builder.addTransition(multiTaskName, userTaskName);
        builder.addTransition(userTaskName, "end");

        return deployProcessWithDefaultTestConnector(ACTOR_NAME, johnUserId, builder, false);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Multiple", "On activity" }, story = "Test multiple connectors on one activity.", jira = "")
    @Test
    public void executeConnectorMultipleConnectorsOnOneActivity() throws Exception {
        final String delivery = "Delivery men";
        final String defaultValue = "a";
        final String dataName = "myData1";
        final Expression dataDefaultValue = new ExpressionBuilder().createConstantStringExpression(defaultValue);
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression("a");
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(
                "executeConnectorOnFinishOfAnAutomaticActivityWithDataAsOutput", "1.0");
        designProcessDefinition.addShortTextData(dataName, dataDefaultValue);
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        final String inputName = CONNECTOR_INPUT_NAME;
        designProcessDefinition.addUserTask("step0", delivery);
        final AutomaticTaskDefinitionBuilder addAutomaticTask = designProcessDefinition.addAutomaticTask("step1");
        final int nbOfConnectors = 25;
        for (int i = 0; i < nbOfConnectors; i++) {
            addAutomaticTask
                    .addConnector("myConnector" + i, CONNECTOR_WITH_OUTPUT_ID, "1.0", ConnectorEvent.ON_FINISH)
                    .addInput(inputName, input1Expression)
                    .addOutput(
                            new LeftOperandBuilder().createNewInstance().setName(dataName).done(),
                            OperatorType.ASSIGNMENT,
                            "=",
                            "",
                            new ExpressionBuilder().createGroovyScriptExpression("concat", "myData1+output1", String.class.getName(),
                                    new ExpressionBuilder().createInputExpression(CONNECTOR_OUTPUT_NAME, String.class.getName()),
                                    new ExpressionBuilder().createDataExpression("myData1", String.class.getName())));
        }
        designProcessDefinition.addUserTask("step2", delivery);
        designProcessDefinition.addTransition("step0", "step1");
        designProcessDefinition.addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployProcessWithDefaultTestConnector(delivery, johnUserId, designProcessDefinition, false);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        assertEquals(defaultValue, getProcessAPI().getProcessDataInstance(dataName, startProcess.getId()).getValue());
        waitForUserTaskAndExecuteIt("step0", startProcess, johnUserId);
        waitForUserTask("step2", startProcess);

        final String value = (String) getProcessAPI().getProcessDataInstance(dataName, startProcess.getId()).getValue();
        assertEquals(nbOfConnectors + 1, value.length());
        for (int i = 0; i < value.length(); i++) {
            assertEquals('a', value.charAt(i));
        }

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "No connector implementation", "Redeploy process" }, story = "Test process redeployment with no connector implementation.", jira = "")
    @Test
    public void redeployProcessWithNoConnectorImplem() throws Exception {
        final String delivery = "Delivery men";

        final ProcessDefinitionBuilder processDefBuilder = new ProcessDefinitionBuilder().createNewInstance("executeConnectorOnActivityInstance", "1.0");
        processDefBuilder.addActor(delivery).addDescription("Delivery all day and night long");
        processDefBuilder.addUserTask("step0", delivery);

        final ProcessDefinition processDefinition1 = deployProcessWithExternalTestConnector(processDefBuilder, delivery, johnUserId);
        List<ConnectorImplementationDescriptor> connectorImplems = getProcessAPI().getConnectorImplementations(processDefinition1.getId(), 0, 100, null);
        assertEquals(1, getProcessAPI().getNumberOfConnectorImplementations(processDefinition1.getId()));
        assertEquals(1, connectorImplems.size());
        disableAndDeleteProcess(processDefinition1);

        // redeploy same process:
        final ProcessDefinition processDefinition2 = deployAndEnableWithActor(processDefBuilder.done(), delivery, johnUser);
        connectorImplems = getProcessAPI().getConnectorImplementations(processDefinition2.getId(), 0, 100, null);
        assertEquals(0, connectorImplems.size());

        disableAndDeleteProcess(processDefinition2);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Process definition" }, story = "Execute connector on process definition.", jira = "")
    @Test
    public void executeConnectorOnProcessDefinition() throws Exception {
        final String valueOfInput1 = "Lily";
        final String valueOfInput2 = "Lucy";
        final String mainExpContent = "'welcome '+valueOfInput1+' and '+valueOfInput2";
        final String inputName1 = "valueOfInput1";
        final String inputName2 = "valueOfInput2";
        final String mainInputName1 = "param1";
        final String resContent = "welcome Lily and Lucy";

        // Input expression
        final Expression input1Expression = new ExpressionBuilder().createInputExpression(inputName1, String.class.getName());
        final Expression input2Expression = new ExpressionBuilder().createInputExpression(inputName2, String.class.getName());

        // Main Expression
        final Expression mainExp = new ExpressionBuilder().createExpression(mainInputName1, mainExpContent, ExpressionType.TYPE_READ_ONLY_SCRIPT.toString(),
                String.class.getName(), "GROOVY", Arrays.asList(input1Expression, input2Expression));

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("executeConnectorOnActivityInstance", "1.0");
        final String actorName = "Isabelle Adjani";
        designProcessDefinition.addActor(actorName);

        final ProcessDefinition processDefinition = deployProcessWithExternalTestConnector(designProcessDefinition, actorName, johnUserId);

        final Map<String, Expression> connectorInputParameters = getConnectorInputParameters(mainInputName1, mainExp);
        final Map<String, Map<String, Serializable>> inputValues = getInputValues(mainInputName1, Arrays.asList(inputName1, inputName2),
                Arrays.asList(valueOfInput1, valueOfInput2));

        final Map<String, Serializable> res = getProcessAPI().executeConnectorOnProcessDefinition(ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_ID,
                ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_VERSION, connectorInputParameters, inputValues, processDefinition.getId());

        assertEquals(resContent, res.get(mainInputName1));
        assertTrue((Boolean) res.get("hasBeenValidated"));

        disableAndDeleteProcess(processDefinition);
    }

    private Map<String, Expression> getConnectorInputParameters(final String mainName, final Expression mainExp) {
        final Map<String, Expression> connectorInputParameters = new HashMap<String, Expression>();
        connectorInputParameters.put(mainName, mainExp);
        return connectorInputParameters;
    }

    private Map<String, Map<String, Serializable>> getInputValues(final String mainName, final List<String> names, final List<String> vars) {
        final Map<String, Map<String, Serializable>> inputValues = new HashMap<String, Map<String, Serializable>>();
        final Map<String, Serializable> values = new HashMap<String, Serializable>();
        if (names != null && !names.isEmpty() && vars != null && !vars.isEmpty() && names.size() == vars.size()) {
            for (int i = 0; i < names.size(); i++) {
                values.put(names.get(i), vars.get(i));
            }
        }
        inputValues.put(mainName, values);
        return inputValues;
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.OTHERS, keywords = { "Connector", "Classpath" }, jira = "")
    @Test
    public void executeConnectorInJar() throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("testConnectorWithExecutionTooLong", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addAutomaticTask("step1").addConnector("myConnector1", "connectorInJar", "1.0.0", ConnectorEvent.ON_ENTER);

        final List<BarResource> resources = new ArrayList<BarResource>();
        addResource(resources, "/org/bonitasoft/engine/connectors/TestConnectorInJar.impl", "TestConnectorInJar.impl");
        addResource(resources, "/org/bonitasoft/engine/connectors/connector-in-jar.jar.bak", "connector-in-jar.jar");
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchiveBuilder.addConnectorImplementation(resources.get(0));
        businessArchiveBuilder.addClasspathResource(resources.get(1));
        businessArchiveBuilder.setProcessDefinition(designProcessDefinition.done());
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchiveBuilder.done());
        addMappingOfActorsForUser(delivery, johnUserId, processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        final ProcessInstance process = getProcessAPI().startProcess(processDefinition.getId());
        waitForProcessToFinish(process);
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getNumberofConnectorImplementationsWhenProcessDoesNotExists() {
        assertEquals(0, getProcessAPI().getNumberOfConnectorImplementations(123l));
    }

    @Test
    public void getConnectorImplementations() throws Exception {
        final String delivery = "Delivery men";
        // connector information
        final String connectorId = "org.bonitasoft.connector.testConnector";
        final String connectorId2 = CONNECTOR_WITH_OUTPUT_ID;
        final String connectorVersion = "1.0";
        final String inputName = CONNECTOR_INPUT_NAME;
        final String valueOfInput = "valueOfInput";
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression(valueOfInput);
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("processWithConnector", "1.0");

        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addAutomaticTask("step1").addConnector("myConnector", connectorId, connectorVersion, ConnectorEvent.ON_ENTER)
                .addInput(inputName, input1Expression);
        designProcessDefinition.addUserTask("step2", delivery);
        designProcessDefinition.addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithTestConnector(delivery, johnUserId, designProcessDefinition);
        final long processDefinitionId = processDefinition.getId();
        assertEquals(3, getProcessAPI().getNumberOfConnectorImplementations(processDefinitionId));
        // test ASC
        List<ConnectorImplementationDescriptor> connectorImplementations = getProcessAPI().getConnectorImplementations(processDefinitionId, 0, 2,
                ConnectorCriterion.DEFINITION_ID_ASC);
        assertNotNull(connectorImplementations);
        assertEquals(2, connectorImplementations.size());
        assertEquals(connectorId, connectorImplementations.get(0).getDefinitionId());
        assertEquals(connectorId2, connectorImplementations.get(1).getDefinitionId());

        connectorImplementations = getProcessAPI().getConnectorImplementations(processDefinitionId, 0, 1, ConnectorCriterion.DEFINITION_ID_ASC);
        assertEquals(1, connectorImplementations.size());
        assertEquals(connectorId, connectorImplementations.get(0).getDefinitionId());
        connectorImplementations = getProcessAPI().getConnectorImplementations(processDefinitionId, 1, 1, ConnectorCriterion.DEFINITION_ID_ASC);
        assertEquals(1, connectorImplementations.size());
        assertEquals(connectorId2, connectorImplementations.get(0).getDefinitionId());
        // test DESC
        connectorImplementations = getProcessAPI().getConnectorImplementations(processDefinitionId, 2, 1, ConnectorCriterion.DEFINITION_ID_DESC);
        assertNotNull(connectorImplementations);
        assertEquals(1, connectorImplementations.size());
        assertEquals(connectorId, connectorImplementations.get(0).getDefinitionId());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void longConnectorOutputStoredInProcessVariableShouldThrowANiceException() throws Exception {
        final String delivery = "Delivery men";
        // connector information
        final String connectorId2 = CONNECTOR_WITH_OUTPUT_ID;
        final String connectorVersion = "1.0";
        final String inputName = CONNECTOR_INPUT_NAME;
        final Expression outputOfConnectorExpression = new ExpressionBuilder().createConstantStringExpression("outputExpression");
        final Expression groovyExpression = new ExpressionBuilder().createGroovyScriptExpression("generateLongOutput", "'a'*1000", String.class.getName());
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("processWithConnector", "1.0");

        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addShortTextData("outputOfConnector", outputOfConnectorExpression);

        designProcessDefinition
                .addAutomaticTask("step1")
                .addConnector("theConnector", connectorId2, connectorVersion, ConnectorEvent.ON_ENTER)
                .addInput(inputName, groovyExpression)
                .addOutput(new LeftOperandBuilder().createNewInstance().setName("outputOfConnector").done(), OperatorType.ASSIGNMENT, "=", "",
                        new ExpressionBuilder().createInputExpression(CONNECTOR_OUTPUT_NAME, String.class.getName()));

        final ProcessDefinition processDefinition = deployAndEnableProcessWithTestConnector(delivery, johnUserId, designProcessDefinition);
        final long processDefinitionId = processDefinition.getId();

        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        waitForTaskToFail(startProcess);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void executeConnectorThatThrowExceptionFailUserTask() throws Exception {
        final ProcessDefinition processDefinition = getProcessWithConnectorThatThrowErrorOnUserTaskOnEnter("normal", FailAction.FAIL, false);
        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        waitForTaskToFail(startProcess);

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Throw Exception", "On enter", "Failed", "Automatic task" }, jira = "ENGINE-936")
    @Test
    public void executeConnectorThatThrowExceptionFailAutomaticTaskOnEnter() throws Exception {
        final ProcessDefinition processDefinition = getProcessWithConnectorThatThrowErrorOnAutomaticTaskOnEnter("normal", FailAction.FAIL, false);
        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        waitForTaskToFail(startProcess);

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Throw Exception", "On finish", "Failed", "Automatic task" }, jira = "ENGINE-936")
    @Test
    public void executeConnectorThatThrowExceptionFailAutomaticTaskOnFinish() throws Exception {
        final ProcessDefinition processDefinition = getProcessWithConnectorThatThrowErrorOnAutomaticTaskOnFinish("normal", FailAction.FAIL, false);
        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        waitForTaskToFail(startProcess);

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Failed state", "Connector", "Activity" }, story = "When a connector fails during inputs' setting, the activity fails as well.", jira = "ENGINE-885")
    @Test
    public void connectorThatThrowExceptionFailPolicyOnTaskInput() throws Exception {
        final String delivery = "Delivery men";
        final Expression outputOfConnectorExpression = new ExpressionBuilder().createConstantStringExpression("outputExpression");
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("processWithConnector", "1.0");
        builder.addActor(delivery).addDescription("Delivery all day and night long");
        builder.addShortTextData("outputOfConnector", outputOfConnectorExpression);
        final UserTaskDefinitionBuilder userTaskBuilder = builder.addUserTask("step1", delivery);
        final ConnectorDefinitionBuilder addConnector = userTaskBuilder.addConnector("testConnectorThatThrowException", "testConnectorThatThrowException",
                "1.0", ConnectorEvent.ON_ENTER);
        addConnector.addInput("kind", new ExpressionBuilder().createGroovyScriptExpression("fails", "8/0", String.class.getName()));

        final ProcessDefinition processDefinition = deployAndEnableProcessWithTestConnector(delivery, johnUserId, builder);

        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        final ActivityInstance activityInstance = waitForTaskToFail(startProcess);
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 100);
        searchOptionsBuilder.filter(ConnectorInstancesSearchDescriptor.CONTAINER_ID, activityInstance.getId());
        searchOptionsBuilder.sort(ConnectorInstancesSearchDescriptor.NAME, Order.ASC);
        final SearchOptions searchOptions = searchOptionsBuilder.done();
        final SearchResult<ConnectorInstance> connectorInstances = getProcessAPI().searchConnectorInstances(searchOptions);
        assertEquals(1, connectorInstances.getCount());
        final ConnectorInstance instance = connectorInstances.getResult().get(0);
        assertEquals(ConnectorState.FAILED, instance.getState());

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Failed state", "Connector", "Activity" }, story = "When a connector fails during inputs' setting, ignore the connector and continue.", jira = "ENGINE-987")
    @Test
    public void connectorThatThrowExceptionIgnorePolicyOnTaskInput() throws Exception {
        final String delivery = "Delivery men";
        final Expression outputOfConnectorExpression = new ExpressionBuilder().createConstantStringExpression("outputExpression");
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("processWithConnector", "1.0");
        builder.addActor(delivery).addDescription("Delivery all day and night long");
        builder.addShortTextData("outputOfConnector", outputOfConnectorExpression);
        final UserTaskDefinitionBuilder userTaskBuilder = builder.addUserTask("step1", delivery);
        final ConnectorDefinitionBuilder addConnector = userTaskBuilder.addConnector("testConnectorThatThrowException", "testConnectorThatThrowException",
                "1.0", ConnectorEvent.ON_ENTER);
        addConnector.addInput("kind", new ExpressionBuilder().createGroovyScriptExpression("fails", "8/0", String.class.getName()));
        addConnector.ignoreError();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithTestConnector(delivery, johnUserId, builder);

        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        final ActivityInstance activityInstance = waitForUserTask("step1", startProcess);
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 100);
        searchOptionsBuilder.filter(ConnectorInstancesSearchDescriptor.CONTAINER_ID, activityInstance.getId());
        searchOptionsBuilder.sort(ConnectorInstancesSearchDescriptor.NAME, Order.ASC);
        final SearchOptions searchOptions = searchOptionsBuilder.done();
        final SearchResult<ConnectorInstance> connectorInstances = getProcessAPI().searchConnectorInstances(searchOptions);
        assertEquals(1, connectorInstances.getCount());
        final ConnectorInstance instance = connectorInstances.getResult().get(0);
        assertEquals(ConnectorState.FAILED, instance.getState());

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Failed state", "Connector", "Activity" }, story = "When a connector fails during inputs' setting, the activity fails as well.", jira = "ENGINE-885")
    @Test
    public void connectorThatThrowExceptionFailPolicyOnProcessInput() throws Exception {
        final String delivery = "Delivery men";
        final Expression outputOfConnectorExpression = new ExpressionBuilder().createConstantStringExpression("outputExpression");
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("processWithConnector", "1.0");
        builder.addActor(delivery).addDescription("Delivery all day and night long");
        builder.addShortTextData("outputOfConnector", outputOfConnectorExpression);
        builder.addUserTask("step1", delivery);
        final ConnectorDefinitionBuilder addConnector = builder.addConnector("testConnectorThatThrowException", "testConnectorThatThrowException", "1.0",
                ConnectorEvent.ON_ENTER);
        addConnector.addInput("kind", new ExpressionBuilder().createGroovyScriptExpression("fails", "8/0", String.class.getName()));

        final ProcessDefinition processDefinition = deployAndEnableProcessWithTestConnector(delivery, johnUserId, builder);

        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        waitForProcessToBeInState(startProcess, ProcessInstanceState.ERROR);
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 100);
        searchOptionsBuilder.filter(ConnectorInstancesSearchDescriptor.CONTAINER_ID, startProcess.getId());
        searchOptionsBuilder.sort(ConnectorInstancesSearchDescriptor.NAME, Order.ASC);
        final SearchOptions searchOptions = searchOptionsBuilder.done();
        final SearchResult<ConnectorInstance> connectorInstances = getProcessAPI().searchConnectorInstances(searchOptions);
        assertEquals(1, connectorInstances.getCount());
        final ConnectorInstance instance = connectorInstances.getResult().get(0);
        assertEquals(ConnectorState.FAILED, instance.getState());

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Failed state", "Connector", "Activity" }, story = "When a connector fails during inputs' setting, the activity throw error event.", jira = "ENGINE-987")
    @Test
    public void connectorThatThrowExceptionErrorEventPolicyBoundaryOnTaskInput() throws Exception {
        final String delivery = "Delivery men";
        final Expression outputOfConnectorExpression = new ExpressionBuilder().createConstantStringExpression("outputExpression");
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("processWithConnector", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addShortTextData("outputOfConnector", outputOfConnectorExpression);
        final UserTaskDefinitionBuilder userTaskBuilder = designProcessDefinition.addUserTask("step1inputfail", delivery);
        final ConnectorDefinitionBuilder addConnector = userTaskBuilder.addConnector("testConnectorThatThrowException", "testConnectorThatThrowException",
                "1.0", ConnectorEvent.ON_ENTER);
        addConnector.addInput("kind", new ExpressionBuilder().createGroovyScriptExpression("fails", "8/0", String.class.getName()));
        addConnector.throwErrorEventWhenFailed("error");
        final BoundaryEventDefinitionBuilder boundaryEvent = userTaskBuilder.addBoundaryEvent("errorBoundary", true);
        boundaryEvent.addErrorEventTrigger("error");
        designProcessDefinition.addUserTask("errorTask", delivery);
        designProcessDefinition.addTransition("errorBoundary", "errorTask");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithTestConnector(delivery, johnUserId, designProcessDefinition);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        // the connector must trigger this exception step
        final ActivityInstance errorTask = waitForUserTask("errorTask", startProcess);
        assignAndExecuteStep(errorTask, johnUserId);
        waitForProcessToFinish(startProcess);
        // clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Failed state", "Connector", "Automatic task" }, story = "When a connector fails during inputs' setting, the automatic activity throw error event.", jira = "ENGINE-1304")
    @Test
    public void connectorThatThrowExceptionErrorEventPolicyBoundaryOnAutomaticTask() throws Exception {
        final String delivery = "Delivery men";
        final Expression outputOfConnectorExpression = new ExpressionBuilder().createConstantStringExpression("outputExpression");
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("processWithConnector", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addShortTextData("outputOfConnector", outputOfConnectorExpression);
        final AutomaticTaskDefinitionBuilder taskBuilder = designProcessDefinition.addAutomaticTask("step1inputfail");
        final ConnectorDefinitionBuilder addConnector = taskBuilder.addConnector("testConnectorThatThrowException", "testConnectorThatThrowException", "1.0",
                ConnectorEvent.ON_ENTER);
        addConnector.addInput("kind", new ExpressionBuilder().createGroovyScriptExpression("fails", "8/0", String.class.getName()));
        addConnector.throwErrorEventWhenFailed("error");
        final BoundaryEventDefinitionBuilder boundaryEvent = taskBuilder.addBoundaryEvent("errorBoundary");
        boundaryEvent.addErrorEventTrigger("error");
        designProcessDefinition.addUserTask("errorTask", delivery);
        designProcessDefinition.addTransition("errorBoundary", "errorTask");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithTestConnector(delivery, johnUserId, designProcessDefinition);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        // the connector must trigger this exception step
        final ActivityInstance errorTask = waitForUserTask("errorTask", startProcess);
        assignAndExecuteStep(errorTask, johnUserId);
        waitForProcessToFinish(startProcess);
        // clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Failed state", "Connector", "Receive task" }, story = "When a connector fails during inputs' setting, the receive task throw error event.", jira = "ENGINE-1304")
    @Test
    public void connectorThatThrowExceptionErrorEventPolicyBoundaryOnReceiveTask() throws Exception {
        final String delivery = "Delivery men";
        final Expression outputOfConnectorExpression = new ExpressionBuilder().createConstantStringExpression("outputExpression");
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("processWithConnector", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addShortTextData("outputOfConnector", outputOfConnectorExpression);
        final ReceiveTaskDefinitionBuilder taskBuilder = designProcessDefinition.addReceiveTask("step1inputfail", "m1");
        final ConnectorDefinitionBuilder addConnector = taskBuilder.addConnector("testConnectorThatThrowException", "testConnectorThatThrowException", "1.0",
                ConnectorEvent.ON_ENTER);
        addConnector.addInput("kind", new ExpressionBuilder().createGroovyScriptExpression("fails", "8/0", String.class.getName()));
        addConnector.throwErrorEventWhenFailed("error");
        final BoundaryEventDefinitionBuilder boundaryEvent = taskBuilder.addBoundaryEvent("errorBoundary");
        boundaryEvent.addErrorEventTrigger("error");
        designProcessDefinition.addUserTask("errorTask", delivery);
        designProcessDefinition.addTransition("errorBoundary", "errorTask");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithTestConnector(delivery, johnUserId, designProcessDefinition);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        // the connector must trigger this exception step
        final ActivityInstance errorTask = waitForUserTask("errorTask", startProcess);
        assignAndExecuteStep(errorTask, johnUserId);
        waitForProcessToFinish(startProcess);
        // clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Failed state", "Connector", "Send task" }, story = "When a connector fails during inputs' setting, the send task throw error event.", jira = "ENGINE-1304")
    @Test
    public void connectorThatThrowExceptionErrorEventPolicyBoundaryOnSendTask() throws Exception {
        final String delivery = "Delivery men";
        final Expression outputOfConnectorExpression = new ExpressionBuilder().createConstantStringExpression("outputExpression");
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("processWithConnector", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addShortTextData("outputOfConnector", outputOfConnectorExpression);
        final SendTaskDefinitionBuilder taskBuilder = designProcessDefinition.addSendTask("step1inputfail", "m1",
                new ExpressionBuilder().createConstantStringExpression("p2"));
        final ConnectorDefinitionBuilder addConnector = taskBuilder.addConnector("testConnectorThatThrowException", "testConnectorThatThrowException", "1.0",
                ConnectorEvent.ON_ENTER);
        addConnector.addInput("kind", new ExpressionBuilder().createGroovyScriptExpression("fails", "8/0", String.class.getName()));
        addConnector.throwErrorEventWhenFailed("error");
        final BoundaryEventDefinitionBuilder boundaryEvent = taskBuilder.addBoundaryEvent("errorBoundary");
        boundaryEvent.addErrorEventTrigger("error");
        designProcessDefinition.addUserTask("errorTask", delivery);
        designProcessDefinition.addTransition("errorBoundary", "errorTask");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithTestConnector(delivery, johnUserId, designProcessDefinition);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        // the connector must trigger this exception step
        final ActivityInstance errorTask = waitForUserTask("errorTask", startProcess);
        assignAndExecuteStep(errorTask, johnUserId);
        waitForProcessToFinish(startProcess);
        // clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Failed state", "Connector", "Activity" }, story = "When a connector fails during outputs' setting, the activity throw error event.", jira = "ENGINE-987")
    @Test
    public void connectorThatThrowExceptionErrorEventPolicyBoundaryOnTaskOutput() throws Exception {
        final String delivery = "Delivery men";
        final Expression outputOfConnectorExpression = new ExpressionBuilder().createConstantStringExpression("outputExpression");
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("processWithConnector", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addShortTextData("outputOfConnector", outputOfConnectorExpression);
        final UserTaskDefinitionBuilder userTaskBuilder = designProcessDefinition.addUserTask("step1inputfail", delivery);
        final ConnectorDefinitionBuilder addConnector = userTaskBuilder.addConnector("myConnector", CONNECTOR_WITH_OUTPUT_ID, "1.0", ConnectorEvent.ON_ENTER);
        addConnector.addInput(CONNECTOR_INPUT_NAME, new ExpressionBuilder().createConstantStringExpression("a"));
        addConnector.addOutput(new LeftOperandBuilder().createNewInstance().setName("outputOfConnector").done(), OperatorType.ASSIGNMENT, "=", "",
                new ExpressionBuilder().createGroovyScriptExpression("concat", "8/0", String.class.getName()));
        addConnector.throwErrorEventWhenFailed("error");
        final BoundaryEventDefinitionBuilder boundaryEvent = userTaskBuilder.addBoundaryEvent("errorBoundary", true);
        boundaryEvent.addErrorEventTrigger("error");
        designProcessDefinition.addUserTask("errorTask", delivery);
        designProcessDefinition.addTransition("errorBoundary", "errorTask");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithTestConnector(delivery, johnUserId, designProcessDefinition);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        // the connector must trigger this exception step
        final ActivityInstance errorTask = waitForUserTask("errorTask", startProcess);
        assignAndExecuteStep(errorTask, johnUserId);
        waitForProcessToFinish(startProcess);
        // clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Failed state", "Connector", "Activity" }, story = "When a connector fails during inputs' setting, the error is ignored.", jira = "ENGINE-987")
    @Test
    public void ignoreErrorConnectorOnBoundaryWhenInputFail() throws Exception {
        final String delivery = "Delivery men";
        final Expression outputOfConnectorExpression = new ExpressionBuilder().createConstantStringExpression("outputExpression");
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("processWithConnector", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addShortTextData("outputOfConnector", outputOfConnectorExpression);
        final UserTaskDefinitionBuilder userTaskBuilder = designProcessDefinition.addUserTask("step1", delivery);
        final ConnectorDefinitionBuilder addConnector = userTaskBuilder.addConnector("testConnectorThatThrowException", "testConnectorThatThrowException",
                "1.0", ConnectorEvent.ON_ENTER);
        addConnector.addInput("kind", new ExpressionBuilder().createGroovyScriptExpression("fails", "8/0", String.class.getName()));
        addConnector.ignoreError();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithTestConnector(delivery, johnUserId, designProcessDefinition);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        // the connector must trigger this exception step
        final ActivityInstance errorTask = waitForUserTask("step1", startProcess);
        assignAndExecuteStep(errorTask, johnUserId);
        waitForProcessToFinish(startProcess);
        // clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Failed state", "Connector", "Activity" }, story = "When a connector fails during outputs' setting, the activity throw error event.", jira = "ENGINE-987")
    @Test
    public void connectorThatThrowExceptionIgnorePolicyOnTaskOutput() throws Exception {
        final String delivery = "Delivery men";
        final Expression outputOfConnectorExpression = new ExpressionBuilder().createConstantStringExpression("outputExpression");
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("processWithConnector", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addShortTextData("outputOfConnector", outputOfConnectorExpression);
        final UserTaskDefinitionBuilder userTaskBuilder = designProcessDefinition.addUserTask("step1", delivery);
        final ConnectorDefinitionBuilder addConnector = userTaskBuilder.addConnector("myConnector", CONNECTOR_WITH_OUTPUT_ID, "1.0", ConnectorEvent.ON_ENTER);
        addConnector.addInput(CONNECTOR_INPUT_NAME, new ExpressionBuilder().createConstantStringExpression("a"));
        addConnector.addOutput(new LeftOperandBuilder().createNewInstance().setName("outputOfConnector").done(), OperatorType.ASSIGNMENT, "=", "",
                new ExpressionBuilder().createGroovyScriptExpression("concat", "8/0", String.class.getName()));
        addConnector.ignoreError();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithTestConnector(delivery, johnUserId, designProcessDefinition);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        // the connector must trigger this exception step
        final ActivityInstance errorTask = waitForUserTask("step1", startProcess);
        assignAndExecuteStep(errorTask, johnUserId);
        waitForProcessToFinish(startProcess);
        // clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Failed state", "Connector", "Activity" }, story = "When a connector fails during outputs' setting, the activity fails as well.", jira = "ENGINE-885")
    @Test
    public void connectorThatThrowExceptionFailPolicyOnTaskOutput() throws Exception {
        final String delivery = "Delivery men";
        final String dataName = "result";
        final String defaultValue = "NaN";
        final Expression dataDefaultValue = new ExpressionBuilder().createConstantStringExpression(defaultValue);
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("processWithConnector", "1.0");
        builder.addActor(delivery).addDescription("Delivery all day and night long");
        builder.addShortTextData(dataName, dataDefaultValue);
        final UserTaskDefinitionBuilder userTaskBuilder = builder.addUserTask("step1", delivery);
        final ConnectorDefinitionBuilder addConnector = userTaskBuilder.addConnector("myConnector", CONNECTOR_WITH_OUTPUT_ID, "1.0", ConnectorEvent.ON_ENTER);
        addConnector.addInput(CONNECTOR_INPUT_NAME, new ExpressionBuilder().createConstantStringExpression("a"));
        addConnector.addOutput(new LeftOperandBuilder().createNewInstance().setName("outputOfConnector").done(), OperatorType.ASSIGNMENT, "=", "",
                new ExpressionBuilder().createGroovyScriptExpression("concat", "8/0", String.class.getName()));

        final ProcessDefinition processDefinition = deployAndEnableProcessWithTestConnector(delivery, johnUserId, builder);
        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        final ActivityInstance activityInstance = waitForTaskToFail(startProcess);
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 100);
        searchOptionsBuilder.filter(ConnectorInstancesSearchDescriptor.CONTAINER_ID, activityInstance.getId());
        searchOptionsBuilder.sort(ConnectorInstancesSearchDescriptor.NAME, Order.ASC);
        final SearchOptions searchOptions = searchOptionsBuilder.done();
        final SearchResult<ConnectorInstance> connectorInstances = getProcessAPI().searchConnectorInstances(searchOptions);
        assertEquals(1, connectorInstances.getCount());
        final ConnectorInstance instance = connectorInstances.getResult().get(0);
        assertEquals(ConnectorState.FAILED, instance.getState());

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Throw Exception", "On enter", "Failed", "User task" }, jira = "")
    @Test
    public void executeConnectorThatThrowRuntimeExceptionFailUserTaskOnEnter() throws Exception {
        final ProcessDefinition processDefinition = getProcessWithConnectorThatThrowErrorOnUserTaskOnEnter("runtime", FailAction.FAIL, false);
        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        waitForTaskToFail(startProcess);

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Throw Exception", "On enter", "Failed", "User task" }, jira = "")
    @Test
    public void executeConnectorThatThrowRuntimeExceptionInConnectFailUserTaskOnEnter() throws Exception {
        final ProcessDefinition processDefinition = getProcessWithConnectorThatThrowErrorOnUserTaskOnEnter("connect", FailAction.FAIL, false);
        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        waitForTaskToFail(startProcess);

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Throw Exception", "On enter", "Failed", "User task" }, jira = "")
    @Test
    public void executeConnectorThatThrowRuntimeExceptionInDisconnectFailUserTaskOnEnter() throws Exception {
        final ProcessDefinition processDefinition = getProcessWithConnectorThatThrowErrorOnUserTaskOnEnter("disconnect", FailAction.FAIL, false);
        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        waitForTaskToFail(startProcess);

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Throw Exception", "On enter", "Failed", "Automatic task" }, jira = "ENGINE-936")
    @Test
    public void executeConnectorThatThrowRuntimeExceptionFailAutomaticTaskOnEnter() throws Exception {
        final ProcessDefinition processDefinition = getProcessWithConnectorThatThrowErrorOnAutomaticTaskOnEnter("runtime", FailAction.FAIL, false);
        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        waitForTaskToFail(startProcess);

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Throw Exception", "On finish", "Failed", "Automatic task" }, jira = "ENGINE-936")
    @Test
    public void connectorThatThrowRuntimeExceptionFailTask() throws Exception {
        final ProcessDefinition processDefinition = getProcessWithConnectorThatThrowErrorOnAutomaticTaskOnFinish("runtime", FailAction.FAIL, false);
        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        waitForTaskToFail(startProcess);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void connectorThatThrowRuntimeExceptionFailProcess() throws Exception {
        final ProcessDefinition processDefinition = getProcessWithConnectorThatThrowErrorOnUserTaskOnEnter("runtime", FailAction.FAIL, true);
        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        waitForProcessToBeInState(startProcess, ProcessInstanceState.ERROR);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void connectorThatThrowExceptionFailProcess() throws Exception {
        final ProcessDefinition processDefinition = getProcessWithConnectorThatThrowErrorOnUserTaskOnEnter("normal", FailAction.FAIL, true);
        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        waitForProcessToBeInState(startProcess, ProcessInstanceState.ERROR);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void connectorThatThrowExceptionIgnorePolicyOnTask() throws Exception {
        final ProcessDefinition processDefinition = getProcessWithConnectorThatThrowErrorOnUserTaskOnEnter("normal", FailAction.IGNORE, false);
        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        waitForUserTask("step1", startProcess);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void connectorThatThrowExceptionIgnorePolicyOnProcess() throws Exception {
        final ProcessDefinition processDefinition = getProcessWithConnectorThatThrowErrorOnUserTaskOnEnter("normal", FailAction.IGNORE, true);
        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        waitForUserTask("step1", startProcess);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void connectorThatThrowExceptionErrorEventPolicyWithEventSubProcessOnTask() throws Exception {
        final ProcessDefinition processDefinition = getProcessWithConnectorThatThrowErrorOnUserTaskOnEnter("normal", FailAction.ERROR_EVENT, false);
        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        waitForUserTask("errorTask", startProcess);
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void connectorThatThrowExceptionErrorEventPolicyWithEventSubProcessOnProcess() throws Exception {
        final ProcessDefinition processDefinition = getProcessWithConnectorThatThrowErrorOnUserTaskOnEnter("normal", FailAction.ERROR_EVENT, true);
        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        waitForUserTask("errorTask", startProcess);
        System.out.println("before delete");
        disableAndDeleteProcess(processDefinition);
    }

    private ProcessDefinition getProcessWithConnectorThatThrowError(final String exceptionType, final FailAction failAction, final boolean onProcess,
            final boolean withUserTask, final boolean onEnter) throws BonitaException, IOException {
        final String delivery = "Delivery men";
        final Expression outputOfConnectorExpression = new ExpressionBuilder().createConstantStringExpression("outputExpression");
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("processWithConnector", "1.0");
        builder.addActor(delivery).addDescription("Delivery all day and night long");
        builder.addShortTextData("outputOfConnector", outputOfConnectorExpression);

        // Add a task
        final ActivityDefinitionBuilder activityDefinitionBuilder;
        if (withUserTask) {
            activityDefinitionBuilder = builder.addUserTask("step1", delivery);
        } else {
            activityDefinitionBuilder = builder.addAutomaticTask("step1");
        }

        // Connector to add on enter or on finish
        final ConnectorEvent connectorEvent;
        if (onEnter) {
            connectorEvent = ConnectorEvent.ON_ENTER;
        } else {
            connectorEvent = ConnectorEvent.ON_FINISH;
        }

        // Add connector on process or task
        final ConnectorDefinitionBuilder addConnector;
        if (onProcess) {
            addConnector = builder.addConnector("testConnectorThatThrowException", "testConnectorThatThrowException", "1.0", connectorEvent);
        } else {
            addConnector = activityDefinitionBuilder.addConnector("testConnectorThatThrowException", "testConnectorThatThrowException", "1.0", connectorEvent);
        }
        addConnector.addInput("kind", new ExpressionBuilder().createConstantStringExpression(exceptionType));
        switch (failAction) {
            case ERROR_EVENT:
                addConnector.throwErrorEventWhenFailed("error");
                final SubProcessDefinitionBuilder subProcessBuilder = builder.addSubProcess("errorSub", true).getSubProcessBuilder();
                subProcessBuilder.addStartEvent("errorstart").addErrorEventTrigger("error");
                subProcessBuilder.addUserTask("errorTask", delivery);
                subProcessBuilder.addTransition("errorstart", "errorTask");
                break;
            case IGNORE:
                addConnector.ignoreError();
                break;
            case FAIL:
            default:
                break;
        }

        return deployAndEnableProcessWithTestConnector(delivery, johnUserId, builder);
    }

    private ProcessDefinition getProcessWithConnectorThatThrowErrorOnUserTaskOnEnter(final String exceptionType, final FailAction failAction,
            final boolean onProcess) throws BonitaException, IOException {
        return getProcessWithConnectorThatThrowError(exceptionType, failAction, onProcess, true, true);
    }

    private ProcessDefinition getProcessWithConnectorThatThrowErrorOnAutomaticTaskOnEnter(final String exceptionType, final FailAction failAction,
            final boolean onProcess) throws BonitaException, IOException {
        return getProcessWithConnectorThatThrowError(exceptionType, failAction, onProcess, false, true);
    }

    private ProcessDefinition getProcessWithConnectorThatThrowErrorOnAutomaticTaskOnFinish(final String exceptionType, final FailAction failAction,
            final boolean onProcess) throws BonitaException, IOException {
        return getProcessWithConnectorThatThrowError(exceptionType, failAction, onProcess, false, false);
    }

    @Test
    @Cover(classes = { ErrorEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "error", "boundary",
            "throw", "event", "human task", "connector", "aborted" }, jira = "ENGINE-767, ENGINE-1226", story = "throw error event connector should be catch by boundary event")
    public void connectorThatThrowExceptionErrorEventPolicyBoundaryOnTask() throws Exception {
        final String delivery = "Delivery men";
        final Expression outputOfConnectorExpression = new ExpressionBuilder().createConstantStringExpression("outputExpression");
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("processWithConnector", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addShortTextData("outputOfConnector", outputOfConnectorExpression);
        final UserTaskDefinitionBuilder userTaskBuilder = designProcessDefinition.addUserTask("step1", delivery);
        final ConnectorDefinitionBuilder addConnector;
        addConnector = userTaskBuilder.addConnector("testConnectorThatThrowException", "testConnectorThatThrowException", "1.0", ConnectorEvent.ON_ENTER);
        addConnector.addInput("kind", new ExpressionBuilder().createConstantStringExpression("normal"));
        addConnector.throwErrorEventWhenFailed("error");
        final BoundaryEventDefinitionBuilder boundaryEvent = userTaskBuilder.addBoundaryEvent("errorBoundary", true);
        boundaryEvent.addErrorEventTrigger("error");
        designProcessDefinition.addUserTask("errorTask", delivery);
        designProcessDefinition.addTransition("errorBoundary", "errorTask");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithTestConnector(delivery, johnUserId, designProcessDefinition);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        // the connector must trigger this exception step
        try {
            waitForUserTask("errorTask", startProcess);

            Thread.sleep(500);

            // Ensure that "step1" is aborted
            final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
            searchOptionsBuilder.filter(ArchivedHumanTaskInstanceSearchDescriptor.NAME, "step1");
            searchOptionsBuilder.sort(ArchivedHumanTaskInstanceSearchDescriptor.REACHED_STATE_DATE, Order.DESC);
            final List<ArchivedActivityInstance> activities = getProcessAPI().searchArchivedActivities(searchOptionsBuilder.done()).getResult();
            assertEquals(ActivityStates.ABORTED_STATE, activities.get(0).getState());
        } finally {
            // clean up
            disableAndDeleteProcess(processDefinition);
        }
    }

    @Test
    @Cover(classes = { ErrorEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "error", "boundary",
            "event", "call activity", "connector" }, jira = "ENGINE-767", story = "throw error event connector is not catch, activity should fail")
    public void connectorThatThrowExceptionErrorEventPolicyNotCatchOnTask() throws Exception {
        final String delivery = "Delivery men";
        final Expression outputOfConnectorExpression = new ExpressionBuilder().createConstantStringExpression("outputExpression");
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("processWithConnector", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addShortTextData("outputOfConnector", outputOfConnectorExpression);
        final UserTaskDefinitionBuilder userTaskBuilder = designProcessDefinition.addUserTask("step1", delivery);
        final ConnectorDefinitionBuilder addConnector;
        addConnector = userTaskBuilder.addConnector("testConnectorThatThrowException", "testConnectorThatThrowException", "1.0", ConnectorEvent.ON_ENTER);
        addConnector.addInput("kind", new ExpressionBuilder().createConstantStringExpression("normal"));
        addConnector.throwErrorEventWhenFailed("error");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithTestConnector(delivery, johnUserId, designProcessDefinition);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());

        // the connector must trigger this exception step
        waitForTaskToFail(startProcess);

        // clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    @Cover(classes = { ErrorEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "error", "boundary",
            "event", "call activity", "connector" }, jira = "ENGINE-767", story = "throw error event connector should be catch by boundary event")
    public void connectorThatThrowExceptionErrorEventPolicyCallActivityOnTask() throws Exception {
        final String delivery = "Delivery men";
        // create the process with connector throwing error
        final Expression outputOfConnectorExpression = new ExpressionBuilder().createConstantStringExpression("outputExpression");
        ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("processWithConnector", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addShortTextData("outputOfConnector", outputOfConnectorExpression);
        final UserTaskDefinitionBuilder userTaskBuilder = designProcessDefinition.addUserTask("step1", delivery);
        final ConnectorDefinitionBuilder addConnector;
        addConnector = userTaskBuilder.addConnector("testConnectorThatThrowException", "testConnectorThatThrowException", "1.0", ConnectorEvent.ON_ENTER);
        addConnector.addInput("kind", new ExpressionBuilder().createConstantStringExpression("normal"));
        addConnector.throwErrorEventWhenFailed("error");
        final ProcessDefinition calledProcess = deployAndEnableProcessWithTestConnector(delivery, johnUserId, designProcessDefinition);
        // create parent process with call activity and boundary
        designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("parentProcess", "1.0");
        designProcessDefinition.addActor(delivery);
        final CallActivityBuilder callActivityBuilder = designProcessDefinition.addCallActivity("processWithConnector",
                new ExpressionBuilder().createConstantStringExpression("processWithConnector"), new ExpressionBuilder().createConstantStringExpression("1.0"));
        final BoundaryEventDefinitionBuilder boundaryEvent = callActivityBuilder.addBoundaryEvent("errorBoundary", true);
        boundaryEvent.addErrorEventTrigger("error");
        designProcessDefinition.addUserTask("errorTask", delivery);
        designProcessDefinition.addTransition("errorBoundary", "errorTask");
        final ProcessDefinition callingProcess = deployAndEnableWithActor(designProcessDefinition.done(), delivery, johnUser);
        // start parent
        final ProcessInstance startProcess = getProcessAPI().startProcess(callingProcess.getId());

        // the connector must trigger this exception step of the calling process
        waitForUserTask("errorTask", startProcess);

        // clean up
        disableAndDeleteProcess(callingProcess);
        disableAndDeleteProcess(calledProcess);
    }

    @Test
    @Cover(classes = {}, concept = BPMNConcept.CONNECTOR, jira = "ENGINE-469", keywords = { "node", "restart", "transition", "flownode", "connector" }, story = "elements must be restarted when connectors were not completed when the node was shut down")
    public void restartElementsWithConnector() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithTransition", "1.0");
        builder.addActor("actor");
        builder.addShortTextData("data", new ExpressionBuilder().createConstantStringExpression("default"));
        final UserTaskDefinitionBuilder taskDefinitionBuilder = builder.addUserTask("step1", "actor");
        taskDefinitionBuilder
                .addConnector("myConnector1", CONNECTOR_WITH_OUTPUT_ID, "1.0", ConnectorEvent.ON_FINISH)
                .addInput(CONNECTOR_INPUT_NAME, new ExpressionBuilder().createConstantStringExpression("value1"))
                .addOutput(new LeftOperandBuilder().createNewInstance("data").done(), OperatorType.ASSIGNMENT, "=", null,
                        new ExpressionBuilder().createInputExpression(CONNECTOR_OUTPUT_NAME, String.class.getName()));
        taskDefinitionBuilder.addConnector("wait1", "testConnectorLongToExecute", "1.0.0", ConnectorEvent.ON_FINISH).addInput("timeout",
                new ExpressionBuilder().createConstantLongExpression(700));
        taskDefinitionBuilder.addConnector("wait2", "testConnectorLongToExecute", "1.0.0", ConnectorEvent.ON_FINISH).addInput("timeout",
                new ExpressionBuilder().createConstantLongExpression(500));
        taskDefinitionBuilder
                .addConnector("myConnector2", CONNECTOR_WITH_OUTPUT_ID, "1.0", ConnectorEvent.ON_FINISH)
                .addInput(CONNECTOR_INPUT_NAME, new ExpressionBuilder().createConstantStringExpression("value2"))
                .addOutput(new LeftOperandBuilder().createNewInstance("data").done(), OperatorType.ASSIGNMENT, "=", null,
                        new ExpressionBuilder().createInputExpression(CONNECTOR_OUTPUT_NAME, String.class.getName()));
        builder.addUserTask("step2", "actor");
        builder.addTransition("step1", "step2");
        // start check value1,stop, check still value1, start, check value 2, check step2 is active
        final ProcessDefinition processDefinition = deployProcessWithDefaultTestConnector("actor", johnUserId, builder, false);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt("step1", processInstance, johnUser);
        final WaitForVariableValue waitForConnector = new WaitForVariableValue(getProcessAPI(), processInstance.getId(), "data", "value1");
        assertTrue(waitForConnector.waitUntil());
        logout();
        final PlatformSession loginPlatform = APITestUtil.loginPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(loginPlatform);
        platformAPI.stopNode();
        platformAPI.startNode();
        APITestUtil.logoutPlatform(loginPlatform);
        login();
        assertEquals("value1", getProcessAPI().getProcessDataInstance("data", processInstance.getId()).getValue());
        waitForUserTask("step2", processInstance.getId());
        // connector restarted
        assertEquals("value2", getProcessAPI().getProcessDataInstance("data", processInstance.getId()).getValue());
        disableAndDeleteProcess(processDefinition.getId());
    }

    @Test
    @Cover(classes = {}, concept = BPMNConcept.CONNECTOR, jira = "ENGINE-469", keywords = { "node", "restart", "transition", "flownode", "connector" }, story = "elements must be restarted when connectors were not completed when the node was shut down")
    public void restartProcessWithConnector() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithTransition", "1.0");
        builder.addActor("actor");
        builder.addShortTextData("data", new ExpressionBuilder().createConstantStringExpression("default"));
        builder.addConnector("myConnector1", CONNECTOR_WITH_OUTPUT_ID, "1.0", ConnectorEvent.ON_ENTER)
                .addInput(CONNECTOR_INPUT_NAME, new ExpressionBuilder().createConstantStringExpression("value1"))
                .addOutput(new LeftOperandBuilder().createNewInstance("data").done(), OperatorType.ASSIGNMENT, "=", null,
                        new ExpressionBuilder().createInputExpression(CONNECTOR_OUTPUT_NAME, String.class.getName()));
        builder.addConnector("wait1", "testConnectorLongToExecute", "1.0.0", ConnectorEvent.ON_ENTER).addInput("timeout",
                new ExpressionBuilder().createConstantLongExpression(1000));
        builder.addConnector("wait2", "testConnectorLongToExecute", "1.0.0", ConnectorEvent.ON_ENTER).addInput("timeout",
                new ExpressionBuilder().createConstantLongExpression(500));
        builder.addConnector("myConnector2", CONNECTOR_WITH_OUTPUT_ID, "1.0", ConnectorEvent.ON_ENTER)
                .addInput(CONNECTOR_INPUT_NAME, new ExpressionBuilder().createConstantStringExpression("value2"))
                .addOutput(new LeftOperandBuilder().createNewInstance("data").done(), OperatorType.ASSIGNMENT, "=", null,
                        new ExpressionBuilder().createInputExpression(CONNECTOR_OUTPUT_NAME, String.class.getName()));
        builder.addUserTask("step1", "actor");
        // start check value1,stop, check still value1, start, check value 2, check step2 is active
        final ProcessDefinition processDefinition = deployProcessWithDefaultTestConnector("actor", johnUserId, builder, false);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final WaitForVariableValue waitForConnector = new WaitForVariableValue(getProcessAPI(), processInstance.getId(), "data", "value1");
        assertTrue(waitForConnector.waitUntil());
        logout();
        final PlatformSession loginPlatform = APITestUtil.loginPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(loginPlatform);
        System.out.println("stopping node");
        platformAPI.stopNode();
        System.out.println("node stopped");
        Thread.sleep(300);
        System.out.println("starting node");
        platformAPI.startNode();
        System.out.println("node started");
        APITestUtil.logoutPlatform(loginPlatform);
        login();
        assertEquals("value1", getProcessAPI().getProcessDataInstance("data", processInstance.getId()).getValue());
        final ActivityInstance step1 = waitForUserTask("step1", processInstance.getId());
        // connector restarted
        assertEquals("value2", getProcessAPI().getProcessDataInstance("data", processInstance.getId()).getValue());
        assignAndExecuteStep(step1, johnUserId);
        waitForProcessToFinish(processInstance);
        disableAndDeleteProcess(processDefinition.getId());
    }

    @Cover(classes = ConnectorInstance.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Search" }, story = "Search connector instances", jira = "")
    @Test
    public void searchConnectorInstances() throws Exception {
        final String delivery = "Delivery men";

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("searchConnector", "1.0");
        designProcessDefinition.addActor(delivery).addUserTask("step0", delivery);
        designProcessDefinition.addConnector("onEnterConnector", CONNECTOR_WITH_OUTPUT_ID, "1.0", ConnectorEvent.ON_ENTER).addInput(CONNECTOR_INPUT_NAME,
                new ExpressionBuilder().createConstantStringExpression("test"));;
        designProcessDefinition.addConnector("onFinishConnector", CONNECTOR_WITH_OUTPUT_ID, "1.0", ConnectorEvent.ON_FINISH).addInput(CONNECTOR_INPUT_NAME,
                new ExpressionBuilder().createConstantStringExpression("test"));

        final ProcessDefinition processDefinition = deployProcessWithDefaultTestConnector(delivery, johnUserId, designProcessDefinition, false);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask("step0", processInstance.getId());
        SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 100);
        searchOptionsBuilder.filter(ConnectorInstancesSearchDescriptor.CONTAINER_ID, processInstance.getId());
        searchOptionsBuilder.filter(ConnectorInstancesSearchDescriptor.CONTAINER_TYPE, "process");
        searchOptionsBuilder.sort(ConnectorInstancesSearchDescriptor.NAME, Order.ASC);
        SearchOptions searchOptions = searchOptionsBuilder.done();
        SearchResult<ConnectorInstance> connectorInstances = getProcessAPI().searchConnectorInstances(searchOptions);
        assertEquals(2, connectorInstances.getCount());
        assertThat(connectorInstances.getResult(), nameAre("onEnterConnector", "onFinishConnector"));

        searchOptionsBuilder = new SearchOptionsBuilder(0, 100);
        searchOptionsBuilder.filter(ConnectorInstancesSearchDescriptor.CONTAINER_ID, processInstance.getId());
        searchOptionsBuilder.filter(ConnectorInstancesSearchDescriptor.CONTAINER_TYPE, "process");
        searchOptionsBuilder.sort(ConnectorInstancesSearchDescriptor.NAME, Order.ASC);
        searchOptionsBuilder.searchTerm("onEnter");
        searchOptions = searchOptionsBuilder.done();
        connectorInstances = getProcessAPI().searchConnectorInstances(searchOptions);
        assertEquals(1, connectorInstances.getCount());
        assertThat(connectorInstances.getResult(), nameAre("onEnterConnector"));

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Activity instance" }, story = "search for archived connector instances and check they are deleted at the deletion of the definition", jira = "ENGINE-651")
    @Test
    public void searchArchivedConnectorInstance() throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("executeConnectorOnActivityInstance", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addConnector("myConnectorOnProcess", CONNECTOR_WITH_OUTPUT_ID, "1.0", ConnectorEvent.ON_ENTER).addInput(CONNECTOR_INPUT_NAME,
                new ExpressionBuilder().createConstantStringExpression("value1"));
        designProcessDefinition.addAutomaticTask("step1").addConnector("myConnectorOnStep", CONNECTOR_WITH_OUTPUT_ID, "1.0", ConnectorEvent.ON_ENTER)
                .addInput(CONNECTOR_INPUT_NAME, new ExpressionBuilder().createConstantStringExpression("value1"));
        designProcessDefinition.addUserTask("step2", delivery);
        designProcessDefinition.addTransition("step1", "step2");
        final ProcessDefinition processDefinition = deployProcessWithDefaultTestConnector(delivery, johnUserId, designProcessDefinition, false);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance step2 = waitForUserTask("step2", processInstance.getId());
        // search with filter on name
        SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 100);
        searchOptionsBuilder.filter(ArchiveConnectorInstancesSearchDescriptor.NAME, "myConnectorOnStep");
        searchOptionsBuilder.sort(ArchiveConnectorInstancesSearchDescriptor.NAME, Order.ASC);
        SearchOptions searchOptions = searchOptionsBuilder.done();
        SearchResult<ArchivedConnectorInstance> connectorInstances = getProcessAPI().searchArchivedConnectorInstances(searchOptions);
        assertThat(connectorInstances.getResult(), nameAre("myConnectorOnStep"));

        // finish process
        assignAndExecuteStep(step2, johnUserId);
        waitForProcessToFinish(processInstance);

        // search for archived connector instances
        searchOptionsBuilder = new SearchOptionsBuilder(0, 100);
        searchOptionsBuilder.filter(ArchiveConnectorInstancesSearchDescriptor.CONTAINER_ID, processInstance.getId());
        searchOptionsBuilder.filter(ArchiveConnectorInstancesSearchDescriptor.CONTAINER_TYPE, "process");
        searchOptionsBuilder.sort(ArchiveConnectorInstancesSearchDescriptor.NAME, Order.ASC);
        searchOptions = searchOptionsBuilder.done();
        connectorInstances = getProcessAPI().searchArchivedConnectorInstances(searchOptions);
        assertThat(connectorInstances.getResult(), nameAre("myConnectorOnProcess"));

        // now also connector of process is archived
        searchOptionsBuilder = new SearchOptionsBuilder(0, 100);
        searchOptionsBuilder.sort(ArchiveConnectorInstancesSearchDescriptor.NAME, Order.ASC);
        searchOptions = searchOptionsBuilder.done();
        connectorInstances = getProcessAPI().searchArchivedConnectorInstances(searchOptions);
        assertThat(connectorInstances.getResult(), nameAre("myConnectorOnProcess", "myConnectorOnStep"));

        disableAndDeleteProcess(processDefinition);

        // check all archived connectors are deleted
        searchOptionsBuilder = new SearchOptionsBuilder(0, 100);
        searchOptions = searchOptionsBuilder.done();
        connectorInstances = getProcessAPI().searchArchivedConnectorInstances(searchOptions);
        assertTrue("there should be no archived connector anymore", connectorInstances.getResult().isEmpty());
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Operation", "Classpath" }, story = "execution of input expression and output operations of connector must be done in the process classpath", jira = "ENGINE-1022")
    @Test
    public void connectorWithExternalLibraryInInputAndOutput() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder();
        final ProcessDefinitionBuilder pBuilder = processDefinitionBuilder.createNewInstance("emptyProcess", String.valueOf(System.currentTimeMillis()));
        final UserTaskDefinitionBuilder addUserTask = pBuilder.addActor("actor")
                // .addData(
                // "data",
                // "org.bonitasoft.dfgdfg.Restaurant",
                // new ExpressionBuilder().createGroovyScriptExpression("myScript", "new org..Restaurant()",
                // "org.bonitasoft.dfgdfg.Restaurant"))
                .addUserTask("step1", "actor");
        addUserTask.addData("dataActivity", java.lang.Object.class.getName(),
                new ExpressionBuilder().createGroovyScriptExpression("myScript", "new org.bonitasoft.dfgdfg.Restaurant()", java.lang.Object.class.getName()));
        addUserTask
                .addConnector("myConnector1", "connectorInJar", "1.0.0", ConnectorEvent.ON_ENTER)
                .addInput("input",
                        new ExpressionBuilder().createGroovyScriptExpression("myScript", "new org.bonitasoft.dfgdfg.Restaurant()", Object.class.getName()))
                .addOutput(
                        new OperationBuilder().createSetDataOperation("dataActivity", new ExpressionBuilder().createGroovyScriptExpression("myScript",
                                "new org.bonitasoft.dfgdfg.Restaurant()", "org.bonitasoft.dfgdfg.Restaurant")));
        final DesignProcessDefinition done = pBuilder.done();
        final BusinessArchiveBuilder builder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(done);
        builder.addConnectorImplementation(getResource("/org/bonitasoft/engine/connectors/TestConnectorInJar.impl", "TestConnectorInJar.impl"));
        builder.addClasspathResource(getResource("/org/bonitasoft/engine/connectors/connector-in-jar.jar.bak", "connector-in-jar.jar"));
        builder.addClasspathResource(getResource("/org.bonitasoft.dfgdfg.bak", "org.bonitasoft.dfgdfg.jar"));
        final BusinessArchive businessArchive = builder.done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);
        addMappingOfActorsForUser("actor", johnUserId, processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask("step1", processInstance);
        // work only in local
        // assertEquals("org.bonitasoft.dfgdfg.Restaurant", getProcessAPI().getActivityDataInstance("dataActivity",
        // task.getId()).getValue().getClass().getName());
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Operation" }, story = "execute connector on activity instance and execute operations", jira = "ENGINE-1037")
    @Test
    public void executeConnectorOnProcessDefinitionWithOperations() throws Exception {
        final String delivery = "Delivery men";
        final Expression input1Expression = new ExpressionBuilder().createInputExpression("valueOfInput1", String.class.getName());
        final Expression input2Expression = new ExpressionBuilder().createInputExpression("valueOfInput2", String.class.getName());
        final Expression mainExp = new ExpressionBuilder().createExpression("param1", "'welcome '+valueOfInput1+' and '+valueOfInput2",
                ExpressionType.TYPE_READ_ONLY_SCRIPT.toString(), String.class.getName(), "GROOVY", Arrays.asList(input1Expression, input2Expression));

        // process with data "Mett"
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("executeConnectorOnActivityInstance", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addUserTask("step1", delivery);
        final ProcessDefinition processDefinition = deployProcessWithExternalTestConnector(designProcessDefinition, delivery, johnUserId);

        // execute connector with operations:
        // connector return param1="welcome Lily and Lucy and Mett"
        // operations: put "Jack" in data valueOfInput3, param1 in "externalData" and "John" in "externalDataConst"
        // Create Operation map:
        final List<Operation> operations = new ArrayList<Operation>(2);
        // set valueOfInput3
        final Map<String, Serializable> contexts = new HashMap<String, Serializable>();
        operations.add(new OperationBuilder().createNewInstance().setLeftOperand("externalData", true)
                .setRightOperand(new ExpressionBuilder().createInputExpression("param1", String.class.getName())).setType(OperatorType.ASSIGNMENT).done());
        operations.add(new OperationBuilder().createNewInstance().setLeftOperand("externalDataConst", true)
                .setRightOperand(new ExpressionBuilder().createConstantStringExpression("John")).setType(OperatorType.ASSIGNMENT).done());
        final Map<String, Expression> connectorInputParameters = getConnectorInputParameters("param1", mainExp);
        final Map<String, Map<String, Serializable>> inputValues = getInputValues("param1", Arrays.asList("valueOfInput1", "valueOfInput2"),
                Arrays.asList("Lily", "Lucy"));

        final Map<String, Serializable> res = getProcessAPI().executeConnectorOnProcessDefinition(ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_ID,
                ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_VERSION, connectorInputParameters, inputValues, operations, contexts,
                processDefinition.getId());
        assertEquals("welcome Lily and Lucy", res.get("externalData"));
        assertEquals("John", res.get("externalDataConst"));

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "connected resources" }, story = "Test connector with connected resources.", jira = "")
    @Test
    // The connector output is list to which an element is added when the method connect or disconnect is called.
    // when the output operations are executed only the method is connect is supposed to be called, that is, the list must contain only one element.
    public void executeConnectorWithConnectedResouce() throws Exception {
        final String dataName = "isConnected";
        final String userTaskName = "step1";
        final ProcessDefinition processDefinition = deployProcessWithConnectorWithConnectedResources(dataName, userTaskName);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(userTaskName, processInstance);

        final DataInstance dataInstance = getProcessAPI().getProcessDataInstance(dataName, processInstance.getId());
        assertEquals(1, dataInstance.getValue());

        disableAndDeleteProcess(processDefinition);
    }

    private ProcessDefinition deployProcessWithConnectorWithConnectedResources(final String dataName, final String userTaskName)
            throws InvalidExpressionException, BonitaException, IOException {
        // expression to get the connector output
        final Expression connectorOutPutExpr = new ExpressionBuilder().createInputExpression("output", List.class.getName());
        // expression to get the list size
        final Expression rightSideOperation = new ExpressionBuilder().createJavaMethodCallExpression("getOutPut", "size", Integer.class.getName(),
                connectorOutPutExpr);
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("proc", "1.0");
        builder.addActor(delivery).addDescription("Delivery all day and night long");
        builder.addIntegerData(dataName, null);// data to store the list size
        builder.addUserTask(userTaskName, delivery)
                .addConnector("myConnector", "org.bonitasoft.connector.testConnectorWithConnectedResource", "1.0", ConnectorEvent.ON_ENTER)
                .addOutput(new OperationBuilder().createSetDataOperation(dataName, rightSideOperation));

        return deployProcessWithDefaultTestConnector(delivery, johnUserId, builder, false);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "User filter", "Connector", "On enter" }, jira = "ENGINE-1305")
    @Test
    public void executeConnectorOnEnterAfterUserFilter() throws Exception {
        final String delivery = "Delivery men";
        final String dataName = "taskAssignee";

        final User jack = createUser("jack", "bpm");

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("processWithConnector", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addLongData(dataName, null);

        // expression to get the connector output
        final Expression connectorOutPutExpr = new ExpressionBuilder().createInputExpression(ExpressionConstants.TASK_ASSIGNEE_ID.getEngineConstantName(),
                Long.class.getName());

        final UserTaskDefinitionBuilder addUserTask = designProcessDefinition.addUserTask("step1", delivery);
        addUserTask.addConnector("myConnector", "org.bonitasoft.connector.testConnectorEngineExecutionContext", "1.0", ConnectorEvent.ON_ENTER).addOutput(
                new OperationBuilder().createSetDataOperation(dataName, connectorOutPutExpr));
        addUserTask.addUserFilter("test", "org.bonitasoft.engine.filter.user.testFilterWithAutoAssign", "1.0").addInput("userId",
                new ExpressionBuilder().createConstantLongExpression(johnUserId));

        designProcessDefinition.addUserTask("step2", delivery);
        designProcessDefinition.addTransition("step1", "step2");

        final ArrayList<Long> userIds = new ArrayList<Long>();
        userIds.add(johnUserId);
        userIds.add(jack.getId());
        final ProcessDefinition processDefinition = deployProcessWithDefaultTestConnector(delivery, userIds, designProcessDefinition, true);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt("step1", processInstance, jack);
        waitForUserTask("step2", processInstance);

        final DataInstance processDataInstance = getProcessAPI().getProcessDataInstance(dataName, processInstance.getId());
        assertEquals(Long.valueOf(johnUserId), processDataInstance.getValue());

        disableAndDeleteProcess(processDefinition);
        deleteUser(jack.getId());
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Engine Execution Context", }, story = "Access the connector execution context from a connector.", jira = "")
    @Test
    public void getEngineExecutionContext() throws Exception {
        final String dataName = "taskAssignee";
        final String step1 = "step1";
        final String step2 = "step2";
        final ProcessDefinition processDefinition = deployProcWithConnectorEngineExecContext(dataName, step1, step2);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(step1, processInstance, johnUserId);
        waitForUserTask(step2, processInstance);

        final DataInstance dataInstance = getProcessAPI().getProcessDataInstance(dataName, processInstance.getId());
        assertEquals(johnUserId, dataInstance.getValue());

        disableAndDeleteProcess(processDefinition);
    }

    private ProcessDefinition deployProcWithConnectorEngineExecContext(final String dataName, final String step1, final String step2)
            throws InvalidExpressionException, BonitaException, IOException {
        // expression to get the connector output
        final Expression connectorOutPutExpr = new ExpressionBuilder().createInputExpression(ExpressionConstants.TASK_ASSIGNEE_ID.getEngineConstantName(),
                Long.class.getName());

        final String delivery = "Delivery men";
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("proc", "1.0");
        builder.addActor(delivery).addDescription("Delivery all day and night long");
        builder.addLongData(dataName, null);// data to store the connector output
        builder.addUserTask(step1, delivery)
                .addConnector("myConnector", "org.bonitasoft.connector.testConnectorEngineExecutionContext", "1.0", ConnectorEvent.ON_FINISH)
                .addOutput(new OperationBuilder().createSetDataOperation(dataName, connectorOutPutExpr));
        builder.addUserTask(step2, delivery);
        builder.addTransition(step1, step2);

        return deployProcessWithDefaultTestConnector(delivery, johnUserId, builder, false);
    }

    @Test
    public void executeConnectorWithInputExpressionUsingAPI() throws Exception {
        String definitionId = "connectorThatUseAPI";
        String definitionVersion = "1.0";
        byte[] connectorImplementationFile = getConnectorImplementationFile(definitionId, definitionVersion, "impl1", "1.0",
                TestConnectorWithOutput.class.getName());

        ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithConnector", "1.12");
        builder.addActor("actor");
        builder.addLongData("numberOfUser", new ExpressionBuilder().createConstantLongExpression(-1));
        ConnectorDefinitionBuilder connector = builder.addAutomaticTask("step1").addConnector("theConnector", definitionId, definitionVersion,
                ConnectorEvent.ON_ENTER);

        String script = "org.bonitasoft.engine.identity.User createUser = apiAccessor.getIdentityAPI().createUser(new org.bonitasoft.engine.identity.UserCreator(\"myUser\", \"password\"));\n";
        script += "apiAccessor.getProcessAPI().attachDocument(processInstanceId, \"a\", \"a\", \"application/pdf\",\"test\".getBytes());";
        script += "return apiAccessor.getIdentityAPI().getNumberOfUsers();";

        connector.addInput(
                "input1",
                new ExpressionBuilder().createGroovyScriptExpression("script", script,
                        Long.class.getName(), new ExpressionBuilder().createEngineConstant(ExpressionConstants.API_ACCESSOR),
                        new ExpressionBuilder().createEngineConstant(ExpressionConstants.PROCESS_INSTANCE_ID)));
        connector.addOutput(new OperationBuilder().createSetDataOperation("numberOfUser",
                new ExpressionBuilder().createInputExpression("output1", Long.class.getName())));
        builder.addUserTask("step2", "actor");
        builder.addTransition("step1", "step2");

        BusinessArchiveBuilder barBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        barBuilder.addConnectorImplementation(new BarResource("connector.impl", connectorImplementationFile));
        barBuilder.addClasspathResource(new BarResource("connector.jar", IOUtil.generateJar(TestConnectorWithOutput.class)));
        barBuilder.setProcessDefinition(builder.done());
        ProcessDefinition processDefinition = deployAndEnableWithActor(barBuilder.done(), "actor", johnUser);
        ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask("step2", processInstance);
        DataInstance numberOfUser = getProcessAPI().getProcessDataInstance("numberOfUser", processInstance.getId());
        assertEquals(2l, numberOfUser.getValue());
        SearchResult<Document> documents = getProcessAPI().searchDocuments(
                new SearchOptionsBuilder(0, 10).filter(DocumentsSearchDescriptor.PROCESSINSTANCE_ID, processInstance.getId()).done());
        assertEquals(1, documents.getCount());
        disableAndDeleteProcess(processDefinition);
        getIdentityAPI().deleteUser("myUser");

    }
}
