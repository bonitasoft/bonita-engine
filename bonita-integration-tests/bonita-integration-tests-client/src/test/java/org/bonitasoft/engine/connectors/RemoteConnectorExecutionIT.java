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
package org.bonitasoft.engine.connectors;

import static org.assertj.core.api.Assertions.tuple;
import static org.bonitasoft.engine.test.BuildTestUtil.generateConnectorImplementation;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
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
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
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
import org.bonitasoft.engine.test.BuildTestUtil;
import org.bonitasoft.engine.test.TestStates;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@SuppressWarnings("javadoc")
public class RemoteConnectorExecutionIT extends ConnectorExecutionIT {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static final String CONNECTOR_OUTPUT_NAME = "output1";

    private static final String CONNECTOR_INPUT_NAME = "input1";

    private static final String CONNECTOR_WITH_OUTPUT_ID = "org.bonitasoft.connector.testConnectorWithOutput";

    private static final String FLOWNODE = "flowNode";

    private static final String PROCESS = "process";

    public static final String TEST_CONNECTOR_THAT_THROW_EXCEPTION_ID = "testConnectorThatThrowException";

    @Test
    public void executeConnectorWithJNDILookupAndAPICall() throws Exception {
        final Expression localDataExpression = new ExpressionBuilder().createConstantLongExpression(0L);
        final Expression processNameExpression = new ExpressionBuilder().createConstantStringExpression(PROCESS_NAME);
        final Expression processVersionExpression = new ExpressionBuilder().createConstantStringExpression("1.0");
        final Expression outputExpression = new ExpressionBuilder().createInputExpression("processId",
                Long.class.getName());

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder()
                .createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        designProcessDefinition.addLongData("processId", localDataExpression);
        designProcessDefinition.addAutomaticTask("start");
        designProcessDefinition.addAutomaticTask("step1")
                .addConnector("myConnector", "org.bonitasoft.engine.connectors.TestConnectorWithAPICall", "1.0",
                        ConnectorEvent.ON_FINISH)
                .addInput("processName", processNameExpression).addInput("processVersion", processVersionExpression)
                .addOutput(new OperationBuilder().createSetDataOperation("processId", outputExpression));
        designProcessDefinition.addAutomaticTask("end");
        designProcessDefinition.addTransition("start", "step1");
        designProcessDefinition.addTransition("step1", "end");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithTestConnectorWithAPICall(
                designProcessDefinition);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForProcessToBeInState(processInstance, ProcessInstanceState.COMPLETED);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void executeConnectorOnFinishOfAnAutomaticActivityWithDataAsOutput() throws Exception {
        final String valueOfInput1 = "valueOfInput1";
        final String defaultValue = "default";
        final String dataName = "myData1";

        final Expression dataDefaultValue = new ExpressionBuilder().createConstantStringExpression(defaultValue);
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression(valueOfInput1);
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(
                "executeConnectorOnFinishOfAnAutomaticActivityWithDataAsOutput", "1.0");
        processDefinitionBuilder.addShortTextData(dataName, dataDefaultValue);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addUserTask("step0", ACTOR_NAME);
        processDefinitionBuilder
                .addAutomaticTask("step1")
                .addConnector("myConnector", CONNECTOR_WITH_OUTPUT_ID, "1.0", ConnectorEvent.ON_FINISH)
                .addInput(CONNECTOR_INPUT_NAME, input1Expression)
                .addOutput(new LeftOperandBuilder().createNewInstance().setName(dataName).done(),
                        OperatorType.ASSIGNMENT, "=", "",
                        new ExpressionBuilder().createInputExpression(CONNECTOR_OUTPUT_NAME, String.class.getName()));
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addTransition("step0", "step1");
        processDefinitionBuilder.addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployProcessWithActorAndTestConnectorWithOutput(
                processDefinitionBuilder, ACTOR_NAME, user);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        assertEquals(defaultValue, getProcessAPI().getProcessDataInstance(dataName, startProcess.getId()).getValue());
        waitForUserTaskAndExecuteIt(startProcess, "step0", user);

        waitForUserTask(startProcess, "step2");

        assertEquals(valueOfInput1, getProcessAPI().getProcessDataInstance(dataName, startProcess.getId()).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void executeConnectorOnFinishOfMultiInstancedActivity() throws Exception {
        // deploy the process
        final String globalDataName = "globalData";
        final String userTaskName = "step2";
        final String multiTaskName = "multi";
        final ProcessDefinition processDefinition = deployProcessWithConnectorOnMutiInstance(globalDataName,
                userTaskName, multiTaskName, true, 2,
                ConnectorEvent.ON_FINISH);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        // wait for the first Multi-instance and execute it: the connector must be executed
        waitForUserTaskAndExecuteIt(processInstance, multiTaskName, user);

        // wait for the second Multi-instance
        final long multiInstanceId = waitForUserTask(processInstance, multiTaskName);

        // check the data value
        DataInstance globalData = getProcessAPI().getProcessDataInstance(globalDataName, processInstance.getId());
        assertEquals(1L, globalData.getValue());

        // execute the second Multi-instance: the connector must be executed again
        assignAndExecuteStep(multiInstanceId, userId);

        // wait for user task that follows the multi task
        waitForUserTask(processInstance, userTaskName);

        // check the data value
        globalData = getProcessAPI().getProcessDataInstance(globalDataName, processInstance.getId());
        assertEquals(2L, globalData.getValue());

        // delete process
        disableAndDeleteProcess(processDefinition);
    }

    private ProcessDefinition deployProcessWithConnectorOnMutiInstance(final String globalDataName,
            final String userTaskName, final String multiTaskName,
            final boolean isSequential, final int nbOfInstances, final ConnectorEvent activationEvent)
            throws BonitaException, IOException {
        final String localDataName = "localData";

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder()
                .createNewInstance("connectorOnMultiInstance", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addLongData(globalDataName, new ExpressionBuilder().createConstantLongExpression(0L));
        builder.addStartEvent("start");

        final UserTaskDefinitionBuilder taskBuilder = builder.addUserTask(multiTaskName, ACTOR_NAME);
        taskBuilder.addMultiInstance(isSequential,
                new ExpressionBuilder().createConstantIntegerExpression(nbOfInstances));
        taskBuilder.addLongData(localDataName, new ExpressionBuilder().createConstantLongExpression(1L));
        taskBuilder
                .addConnector("connector", "connectorInJar", "1.0.0", activationEvent)
                .addInput(CONNECTOR_INPUT_NAME,
                        new ExpressionBuilder().createDataExpression(localDataName, Long.class.getName()))
                .addOutput(
                        new LeftOperandBuilder().createDataLeftOperand(globalDataName),
                        OperatorType.ASSIGNMENT,
                        "=",
                        "",
                        new ExpressionBuilder().createGroovyScriptExpression("Script", globalDataName + " + 1",
                                Long.class.getName(),
                                new ExpressionBuilder().createDataExpression(globalDataName, Long.class.getName())));

        // final UserTaskDefinitionBuilder taskBuilder = builder.addUserTask(multiTaskName, ACTOR_NAME);
        // taskBuilder.addMultiInstance(isSequential, new ExpressionBuilder().createConstantIntegerExpression(nbOfInstances));
        // taskBuilder.addLongData(localDataName, new ExpressionBuilder().createConstantLongExpression(1L));
        // taskBuilder
        // .addConnector("connector", CONNECTOR_WITH_OUTPUT_ID, "1.0", activationEvent)
        // .addSimpleInput(CONNECTOR_INPUT_NAME, new ExpressionBuilder().createDataExpression(localDataName, Long.class.getName()))
        // .addOutput(
        // new LeftOperandBuilder().createDataLeftOperand(globalDataName),
        // OperatorType.ASSIGNMENT, "=", "",
        // new ExpressionBuilder().createGroovyScriptExpression("Script", globalDataName + " + 1", Long.class.getName(),
        // new ExpressionBuilder().createDataExpression(globalDataName, Long.class.getName())));

        builder.addUserTask(userTaskName, ACTOR_NAME);
        builder.addEndEvent("end");
        builder.addTransition("start", multiTaskName);
        builder.addTransition(multiTaskName, userTaskName);
        builder.addTransition(userTaskName, "end");

        final List<BarResource> resources = new ArrayList<>();
        addResource(resources, "/org/bonitasoft/engine/connectors/TestConnectorInJar.impl", "TestConnectorInJar.impl");
        addResource(resources, "/org/bonitasoft/engine/connectors/connector-in-jar.jar.bak", "connector-in-jar.jar");
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchiveBuilder.addConnectorImplementation(resources.get(0));
        businessArchiveBuilder.addClasspathResource(resources.get(1));
        businessArchiveBuilder.setProcessDefinition(builder.done());

        return deployAndEnableProcessWithActor(businessArchiveBuilder.done(), ACTOR_NAME, user);
    }

    @Test
    public void executeConnectorMultipleConnectorsOnOneActivity() throws Exception {
        final String defaultValue = "a";
        final String dataName = "myData1";
        final Expression dataDefaultValue = new ExpressionBuilder().createConstantStringExpression(defaultValue);
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression("a");
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(
                "executeConnectorOnFinishOfAnAutomaticActivityWithDataAsOutput", "1.0");
        processDefinitionBuilder.addShortTextData(dataName, dataDefaultValue);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        final String inputName = CONNECTOR_INPUT_NAME;
        processDefinitionBuilder.addUserTask("step0", ACTOR_NAME);
        final AutomaticTaskDefinitionBuilder addAutomaticTask = processDefinitionBuilder.addAutomaticTask("step1");
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
                            new ExpressionBuilder().createGroovyScriptExpression("concat", "myData1+output1",
                                    String.class.getName(),
                                    new ExpressionBuilder().createInputExpression(CONNECTOR_OUTPUT_NAME,
                                            String.class.getName()),
                                    new ExpressionBuilder().createDataExpression("myData1", String.class.getName())));
        }
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addTransition("step0", "step1");
        processDefinitionBuilder.addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployProcessWithActorAndTestConnectorWithOutput(
                processDefinitionBuilder, ACTOR_NAME, user);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        assertEquals(defaultValue, getProcessAPI().getProcessDataInstance(dataName, startProcess.getId()).getValue());
        waitForUserTaskAndExecuteIt(startProcess, "step0", user);
        waitForUserTask(startProcess, "step2");

        final String value = (String) getProcessAPI().getProcessDataInstance(dataName, startProcess.getId()).getValue();
        assertEquals(nbOfConnectors + 1, value.length());
        for (int i = 0; i < value.length(); i++) {
            assertEquals('a', value.charAt(i));
        }

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void redeployProcessWithNoConnectorImplem() throws Exception {
        final ProcessDefinitionBuilder processDefBuilder = new ProcessDefinitionBuilder()
                .createNewInstance("executeConnectorOnActivityInstance", "1.0");
        processDefBuilder.addActor(ACTOR_NAME);
        processDefBuilder.addUserTask("step0", ACTOR_NAME);

        final ProcessDefinition processDefinition1 = deployProcessWithExternalTestConnector(processDefBuilder,
                ACTOR_NAME, user);
        List<ConnectorImplementationDescriptor> connectorImplems = getProcessAPI()
                .getConnectorImplementations(processDefinition1.getId(), 0, 100, null);
        assertEquals(1, getProcessAPI().getNumberOfConnectorImplementations(processDefinition1.getId()));
        assertEquals(1, connectorImplems.size());
        disableAndDeleteProcess(processDefinition1);

        // redeploy same process:
        final ProcessDefinition processDefinition2 = deployAndEnableProcessWithActor(processDefBuilder.done(),
                ACTOR_NAME, user);
        connectorImplems = getProcessAPI().getConnectorImplementations(processDefinition2.getId(), 0, 100, null);
        assertEquals(0, connectorImplems.size());

        disableAndDeleteProcess(processDefinition2);
    }

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
        final Expression input1Expression = new ExpressionBuilder().createInputExpression(inputName1,
                String.class.getName());
        final Expression input2Expression = new ExpressionBuilder().createInputExpression(inputName2,
                String.class.getName());

        // Main Expression
        final Expression mainExp = new ExpressionBuilder().createExpression(mainInputName1, mainExpContent,
                ExpressionType.TYPE_READ_ONLY_SCRIPT.toString(),
                String.class.getName(), "GROOVY", Arrays.asList(input1Expression, input2Expression));

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder()
                .createNewInstance("executeConnectorOnActivityInstance", "1.0");
        final String actorName = "Isabelle Adjani";
        designProcessDefinition.addActor(actorName);

        final ProcessDefinition processDefinition = deployProcessWithExternalTestConnector(designProcessDefinition,
                actorName, user);

        final Map<String, Expression> connectorInputParameters = getConnectorInputParameters(mainInputName1, mainExp);
        final Map<String, Map<String, Serializable>> inputValues = getInputValues(mainInputName1,
                Arrays.asList(inputName1, inputName2),
                Arrays.asList(valueOfInput1, valueOfInput2));

        final Map<String, Serializable> res = getProcessAPI().executeConnectorOnProcessDefinition(
                ConnectorExecutionIT.DEFAULT_EXTERNAL_CONNECTOR_ID,
                ConnectorExecutionIT.DEFAULT_EXTERNAL_CONNECTOR_VERSION, connectorInputParameters, inputValues,
                processDefinition.getId());

        assertEquals(resContent, res.get(mainInputName1));
        assertTrue((Boolean) res.get("hasBeenValidated"));

        disableAndDeleteProcess(processDefinition);
    }

    private Map<String, Expression> getConnectorInputParameters(final String mainName, final Expression mainExp) {
        final Map<String, Expression> connectorInputParameters = new HashMap<>();
        connectorInputParameters.put(mainName, mainExp);
        return connectorInputParameters;
    }

    private Map<String, Map<String, Serializable>> getInputValues(final String mainName, final List<String> names,
            final List<String> vars) {
        final Map<String, Map<String, Serializable>> inputValues = new HashMap<>();
        final Map<String, Serializable> values = new HashMap<>();
        if (names != null && !names.isEmpty() && vars != null && !vars.isEmpty() && names.size() == vars.size()) {
            for (int i = 0; i < names.size(); i++) {
                values.put(names.get(i), vars.get(i));
            }
        }
        inputValues.put(mainName, values);
        return inputValues;
    }

    @Test
    public void executeConnectorInJar() throws Exception {
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder()
                .createNewInstance("testConnectorWithExecutionTooLong", "1.0");
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addAutomaticTask("step1").addConnector("myConnector1", "connectorInJar", "1.0.0",
                ConnectorEvent.ON_ENTER);

        final List<BarResource> resources = new ArrayList<>();
        addResource(resources, "/org/bonitasoft/engine/connectors/TestConnectorInJar.impl", "TestConnectorInJar.impl");
        addResource(resources, "/org/bonitasoft/engine/connectors/connector-in-jar.jar.bak", "connector-in-jar.jar");
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchiveBuilder.addConnectorImplementation(resources.get(0));
        businessArchiveBuilder.addClasspathResource(resources.get(1));
        businessArchiveBuilder.setProcessDefinition(designProcessDefinition.done());
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(businessArchiveBuilder.done(),
                ACTOR_NAME, user);
        final ProcessInstance process = getProcessAPI().startProcess(processDefinition.getId());
        waitForProcessToFinish(process);
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getNumberOfConnectorImplementationsWhenProcessDoesNotExists() {
        assertEquals(0, getProcessAPI().getNumberOfConnectorImplementations(123L));
    }

    @Test
    public void getConnectorImplementations() throws Exception {
        // connector information
        final String connectorId = "org.bonitasoft.connector.testConnector";
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression("valueOfInput");
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder()
                .createNewInstance("processWithConnector", "1.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addAutomaticTask("step1")
                .addConnector("myConnector", connectorId, "1.0", ConnectorEvent.ON_ENTER)
                .addInput(CONNECTOR_INPUT_NAME, input1Expression);
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addTransition("step1", "step2");

        final List<BarResource> connectorImplementations = Arrays.asList(
                BuildTestUtil.getContentAndBuildBarResource("TestConnector.impl", TestConnector.class),
                BuildTestUtil.getContentAndBuildBarResource("TestConnectorWithOutput.impl",
                        TestConnectorWithOutput.class),
                BuildTestUtil.getContentAndBuildBarResource("TestConnector3.impl", TestConnector3.class));
        final List<BarResource> generateConnectorDependencies = Arrays.asList(
                BuildTestUtil.generateJarAndBuildBarResource(TestConnector.class, "TestConnector.jar"),
                BuildTestUtil.generateJarAndBuildBarResource(VariableStorage.class, "VariableStorage.jar"));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActorAndConnectorAndParameter(
                processDefinitionBuilder, ACTOR_NAME, user,
                connectorImplementations, generateConnectorDependencies, null);
        final long processDefinitionId = processDefinition.getId();
        assertEquals(3, getProcessAPI().getNumberOfConnectorImplementations(processDefinitionId));

        // test ASC
        List<ConnectorImplementationDescriptor> connectorImplementationDescriptors = getProcessAPI()
                .getConnectorImplementations(processDefinitionId, 0, 3,
                        ConnectorCriterion.DEFINITION_ID_ASC);
        assertNotNull(connectorImplementationDescriptors);
        assertEquals(3, connectorImplementationDescriptors.size());
        assertEquals(connectorId, connectorImplementationDescriptors.get(0).getDefinitionId());
        assertEquals(CONNECTOR_WITH_OUTPUT_ID, connectorImplementationDescriptors.get(2).getDefinitionId());

        connectorImplementationDescriptors = getProcessAPI().getConnectorImplementations(processDefinitionId, 0, 1,
                ConnectorCriterion.DEFINITION_ID_ASC);
        assertEquals(1, connectorImplementationDescriptors.size());
        assertEquals(connectorId, connectorImplementationDescriptors.get(0).getDefinitionId());
        connectorImplementationDescriptors = getProcessAPI().getConnectorImplementations(processDefinitionId, 2, 1,
                ConnectorCriterion.DEFINITION_ID_ASC);
        assertEquals(1, connectorImplementationDescriptors.size());
        assertEquals(CONNECTOR_WITH_OUTPUT_ID, connectorImplementationDescriptors.get(0).getDefinitionId());
        // test DESC
        connectorImplementationDescriptors = getProcessAPI().getConnectorImplementations(processDefinitionId, 2, 1,
                ConnectorCriterion.DEFINITION_ID_DESC);
        assertNotNull(connectorImplementationDescriptors);
        assertEquals(1, connectorImplementationDescriptors.size());
        assertEquals(connectorId, connectorImplementationDescriptors.get(0).getDefinitionId());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void longConnectorOutputStoredInProcessVariableShouldThrowANiceException() throws Exception {
        // connector information
        final Expression outputOfConnectorExpression = new ExpressionBuilder()
                .createConstantStringExpression("outputExpression");
        final Expression groovyExpression = new ExpressionBuilder().createGroovyScriptExpression("generateLongOutput",
                "'a'*1000", String.class.getName());
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder()
                .createNewInstance("processWithConnector", "1.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addShortTextData("outputOfConnector", outputOfConnectorExpression);
        processDefinitionBuilder
                .addAutomaticTask("step1")
                .addConnector("theConnector", CONNECTOR_WITH_OUTPUT_ID, "1.0", ConnectorEvent.ON_ENTER)
                .addInput(CONNECTOR_INPUT_NAME, groovyExpression)
                .addOutput(new LeftOperandBuilder().createNewInstance().setName("outputOfConnector").done(),
                        OperatorType.ASSIGNMENT, "=", "",
                        new ExpressionBuilder().createInputExpression(CONNECTOR_OUTPUT_NAME, String.class.getName()));

        final ProcessDefinition processDefinition = deployProcessWithActorAndTestConnectorWithOutput(
                processDefinitionBuilder, ACTOR_NAME, user);
        final long processDefinitionId = processDefinition.getId();

        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        waitForTaskToFail(startProcess);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void executeConnectorThatThrowExceptionFailUserTask() throws Exception {
        final ProcessDefinition processDefinition = getProcessWithConnectorThatThrowErrorOnUserTaskOnEnter("normal",
                FailAction.FAIL, false);
        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        waitForTaskToFail(startProcess);
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void executeConnectorThatThrowExceptionFailAutomaticTaskOnEnter() throws Exception {
        final ProcessDefinition processDefinition = getProcessWithConnectorThatThrowErrorOnAutomaticTaskOnEnter(
                "normal", FailAction.FAIL, false);
        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        waitForTaskToFail(startProcess);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void executeConnectorThatThrowExceptionFailAutomaticTaskOnFinish() throws Exception {
        final ProcessDefinition processDefinition = getProcessWithConnectorThatThrowErrorOnAutomaticTaskOnFinish(
                "normal", FailAction.FAIL, false);
        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        waitForTaskToFail(startProcess);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void connectorThatThrowExceptionFailPolicyOnTaskInput() throws Exception {
        final Expression outputOfConnectorExpression = new ExpressionBuilder()
                .createConstantStringExpression("outputExpression");
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder()
                .createNewInstance("processWithConnector", "1.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addShortTextData("outputOfConnector", outputOfConnectorExpression);
        final UserTaskDefinitionBuilder userTaskBuilder = processDefinitionBuilder.addUserTask("step1", ACTOR_NAME);
        final ConnectorDefinitionBuilder addConnector = userTaskBuilder.addConnector("testConnectorThatThrowException",
                "testConnectorThatThrowException",
                "1.0", ConnectorEvent.ON_ENTER);
        addConnector.addInput("kind",
                new ExpressionBuilder().createGroovyScriptExpression("fails", "8/0", String.class.getName()));

        final ProcessDefinition processDefinition = deployProcessWithActorAndTestConnectorThatThrowException(
                processDefinitionBuilder, ACTOR_NAME, user);

        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        final ActivityInstance activityInstance = waitForTaskToFail(startProcess);
        final SearchOptions searchOptions = getFirst100ConnectorInstanceSearchOptions(activityInstance.getId(),
                FLOWNODE).done();
        final SearchResult<ConnectorInstance> connectorInstances = getProcessAPI()
                .searchConnectorInstances(searchOptions);
        assertEquals(1, connectorInstances.getCount());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void connectorThatThrowExceptionIgnorePolicyOnTaskInput() throws Exception {
        final Expression outputOfConnectorExpression = new ExpressionBuilder()
                .createConstantStringExpression("outputExpression");
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder()
                .createNewInstance("processWithConnector", "1.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addShortTextData("outputOfConnector", outputOfConnectorExpression);
        final UserTaskDefinitionBuilder userTaskBuilder = processDefinitionBuilder.addUserTask("step1", ACTOR_NAME);
        final ConnectorDefinitionBuilder addConnector = userTaskBuilder.addConnector("testConnectorThatThrowException",
                "testConnectorThatThrowException",
                "1.0", ConnectorEvent.ON_ENTER);
        addConnector.addInput("kind",
                new ExpressionBuilder().createGroovyScriptExpression("fails", "8/0", String.class.getName()));
        addConnector.ignoreError();
        final ProcessDefinition processDefinition = deployProcessWithActorAndTestConnectorThatThrowException(
                processDefinitionBuilder, ACTOR_NAME, user);

        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(startProcess, "step1");
        final SearchOptions searchOptions = getFirst100ConnectorInstanceSearchOptions(step1Id, FLOWNODE).done();
        final SearchResult<ConnectorInstance> connectorInstances = getProcessAPI()
                .searchConnectorInstances(searchOptions);
        assertEquals(1, connectorInstances.getCount());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void connectorThatThrowExceptionFailPolicyOnProcessInput() throws Exception {
        final Expression outputOfConnectorExpression = new ExpressionBuilder()
                .createConstantStringExpression("outputExpression");
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder()
                .createNewInstance("processWithConnector", "1.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addShortTextData("outputOfConnector", outputOfConnectorExpression);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME);
        final ConnectorDefinitionBuilder addConnector = processDefinitionBuilder.addConnector(
                "testConnectorThatThrowException",
                "testConnectorThatThrowException", "1.0",
                ConnectorEvent.ON_ENTER);
        addConnector.addInput("kind",
                new ExpressionBuilder().createGroovyScriptExpression("fails", "8/0", String.class.getName()));
        final ProcessDefinition processDefinition = deployProcessWithActorAndTestConnectorThatThrowException(
                processDefinitionBuilder, ACTOR_NAME, user);

        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        waitForProcessToBeInState(startProcess, ProcessInstanceState.ERROR);
        final SearchOptions searchOptions = getFirst100ConnectorInstanceSearchOptions(startProcess.getId(), PROCESS)
                .done();
        final SearchResult<ConnectorInstance> connectorInstances = getProcessAPI()
                .searchConnectorInstances(searchOptions);
        assertEquals(1, connectorInstances.getCount());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void connectorThatThrowExceptionErrorEventPolicyBoundaryOnTaskInput() throws Exception {
        final Expression outputOfConnectorExpression = new ExpressionBuilder()
                .createConstantStringExpression("outputExpression");
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder()
                .createNewInstance("processWithConnector", "1.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addShortTextData("outputOfConnector", outputOfConnectorExpression);
        final UserTaskDefinitionBuilder userTaskBuilder = processDefinitionBuilder.addUserTask("step1inputfail",
                ACTOR_NAME);
        final ConnectorDefinitionBuilder addConnector = userTaskBuilder.addConnector("testConnectorThatThrowException",
                "testConnectorThatThrowException",
                "1.0", ConnectorEvent.ON_ENTER);
        addConnector.addInput("kind",
                new ExpressionBuilder().createGroovyScriptExpression("fails", "8/0", String.class.getName()));
        addConnector.throwErrorEventWhenFailed("error");
        final BoundaryEventDefinitionBuilder boundaryEvent = userTaskBuilder.addBoundaryEvent("errorBoundary", true);
        boundaryEvent.addErrorEventTrigger("error");
        processDefinitionBuilder.addUserTask("errorTask", ACTOR_NAME);
        processDefinitionBuilder.addTransition("errorBoundary", "errorTask");
        final ProcessDefinition processDefinition = deployProcessWithActorAndTestConnectorThatThrowException(
                processDefinitionBuilder, ACTOR_NAME, user);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        // the connector must trigger this exception step
        waitForUserTaskAndExecuteIt(startProcess, "errorTask", user);
        waitForProcessToFinish(startProcess);
        // clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void connectorThatThrowExceptionErrorEventPolicyBoundaryOnAutomaticTask() throws Exception {
        final Expression outputOfConnectorExpression = new ExpressionBuilder()
                .createConstantStringExpression("outputExpression");
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder()
                .createNewInstance("processWithConnector", "1.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addShortTextData("outputOfConnector", outputOfConnectorExpression);
        final AutomaticTaskDefinitionBuilder taskBuilder = processDefinitionBuilder.addAutomaticTask("step1inputfail");
        final ConnectorDefinitionBuilder addConnector = taskBuilder.addConnector("testConnectorThatThrowException",
                "testConnectorThatThrowException", "1.0",
                ConnectorEvent.ON_ENTER);
        addConnector.addInput("kind",
                new ExpressionBuilder().createGroovyScriptExpression("fails", "8/0", String.class.getName()));
        addConnector.throwErrorEventWhenFailed("error");
        final BoundaryEventDefinitionBuilder boundaryEvent = taskBuilder.addBoundaryEvent("errorBoundary");
        boundaryEvent.addErrorEventTrigger("error");
        processDefinitionBuilder.addUserTask("errorTask", ACTOR_NAME);
        processDefinitionBuilder.addTransition("errorBoundary", "errorTask");
        final ProcessDefinition processDefinition = deployProcessWithActorAndTestConnectorThatThrowException(
                processDefinitionBuilder, ACTOR_NAME, user);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        // the connector must trigger this exception step
        waitForUserTaskAndExecuteIt(startProcess, "errorTask", user);
        waitForProcessToFinish(startProcess);
        // clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void connectorThatThrowExceptionErrorEventPolicyBoundaryOnReceiveTask() throws Exception {
        final Expression outputOfConnectorExpression = new ExpressionBuilder()
                .createConstantStringExpression("outputExpression");
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder()
                .createNewInstance("processWithConnector", "1.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addShortTextData("outputOfConnector", outputOfConnectorExpression);
        final ReceiveTaskDefinitionBuilder taskBuilder = processDefinitionBuilder.addReceiveTask("step1inputfail",
                "m1");
        final ConnectorDefinitionBuilder addConnector = taskBuilder.addConnector("testConnectorThatThrowException",
                "testConnectorThatThrowException", "1.0",
                ConnectorEvent.ON_ENTER);
        addConnector.addInput("kind",
                new ExpressionBuilder().createGroovyScriptExpression("fails", "8/0", String.class.getName()));
        addConnector.throwErrorEventWhenFailed("error");
        final BoundaryEventDefinitionBuilder boundaryEvent = taskBuilder.addBoundaryEvent("errorBoundary");
        boundaryEvent.addErrorEventTrigger("error");
        processDefinitionBuilder.addUserTask("errorTask", ACTOR_NAME);
        processDefinitionBuilder.addTransition("errorBoundary", "errorTask");

        final ProcessDefinition processDefinition = deployProcessWithActorAndTestConnectorThatThrowException(
                processDefinitionBuilder, ACTOR_NAME, user);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        // the connector must trigger this exception step
        waitForUserTaskAndExecuteIt(startProcess, "errorTask", user);
        waitForProcessToFinish(startProcess);
        // clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void connectorThatThrowExceptionErrorEventPolicyBoundaryOnSendTask() throws Exception {
        final Expression outputOfConnectorExpression = new ExpressionBuilder()
                .createConstantStringExpression("outputExpression");
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder()
                .createNewInstance("processWithConnector", "1.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addShortTextData("outputOfConnector", outputOfConnectorExpression);
        final SendTaskDefinitionBuilder taskBuilder = processDefinitionBuilder.addSendTask("step1inputfail", "m1",
                new ExpressionBuilder().createConstantStringExpression("p2"));
        final ConnectorDefinitionBuilder addConnector = taskBuilder.addConnector("testConnectorThatThrowException",
                "testConnectorThatThrowException", "1.0",
                ConnectorEvent.ON_ENTER);
        addConnector.addInput("kind",
                new ExpressionBuilder().createGroovyScriptExpression("fails", "8/0", String.class.getName()));
        addConnector.throwErrorEventWhenFailed("error");
        final BoundaryEventDefinitionBuilder boundaryEvent = taskBuilder.addBoundaryEvent("errorBoundary");
        boundaryEvent.addErrorEventTrigger("error");
        processDefinitionBuilder.addUserTask("errorTask", ACTOR_NAME);
        processDefinitionBuilder.addTransition("errorBoundary", "errorTask");
        final ProcessDefinition processDefinition = deployProcessWithActorAndTestConnectorThatThrowException(
                processDefinitionBuilder, ACTOR_NAME, user);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        // the connector must trigger this exception step
        waitForUserTaskAndExecuteIt(startProcess, "errorTask", user);
        waitForProcessToFinish(startProcess);
        // clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void connectorThatThrowExceptionErrorEventPolicyBoundaryOnTaskOutput() throws Exception {
        final Expression outputOfConnectorExpression = new ExpressionBuilder()
                .createConstantStringExpression("outputExpression");
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder()
                .createNewInstance("processWithConnector", "1.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addShortTextData("outputOfConnector", outputOfConnectorExpression);
        final UserTaskDefinitionBuilder userTaskBuilder = processDefinitionBuilder.addUserTask("step1inputfail",
                ACTOR_NAME);
        final ConnectorDefinitionBuilder addConnector = userTaskBuilder.addConnector("myConnector",
                CONNECTOR_WITH_OUTPUT_ID, "1.0", ConnectorEvent.ON_ENTER);
        addConnector.addInput(CONNECTOR_INPUT_NAME, new ExpressionBuilder().createConstantStringExpression("a"));
        addConnector.addOutput(new LeftOperandBuilder().createNewInstance().setName("outputOfConnector").done(),
                OperatorType.ASSIGNMENT, "=", "",
                new ExpressionBuilder().createGroovyScriptExpression("concat", "8/0", String.class.getName()));
        addConnector.throwErrorEventWhenFailed("error");
        final BoundaryEventDefinitionBuilder boundaryEvent = userTaskBuilder.addBoundaryEvent("errorBoundary", true);
        boundaryEvent.addErrorEventTrigger("error");
        processDefinitionBuilder.addUserTask("errorTask", ACTOR_NAME);
        processDefinitionBuilder.addTransition("errorBoundary", "errorTask");
        final ProcessDefinition processDefinition = deployProcessWithActorAndTestConnectorWithOutput(
                processDefinitionBuilder, ACTOR_NAME, user);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        // the connector must trigger this exception step
        waitForUserTaskAndExecuteIt(startProcess, "errorTask", user);
        waitForProcessToFinish(startProcess);
        // clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void ignoreErrorConnectorOnBoundaryWhenInputFail() throws Exception {
        final Expression outputOfConnectorExpression = new ExpressionBuilder()
                .createConstantStringExpression("outputExpression");
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder()
                .createNewInstance("processWithConnector", "1.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addShortTextData("outputOfConnector", outputOfConnectorExpression);
        final UserTaskDefinitionBuilder userTaskBuilder = processDefinitionBuilder.addUserTask("step1", ACTOR_NAME);
        final ConnectorDefinitionBuilder addConnector = userTaskBuilder.addConnector("testConnectorThatThrowException",
                "testConnectorThatThrowException",
                "1.0", ConnectorEvent.ON_ENTER);
        addConnector.addInput("kind",
                new ExpressionBuilder().createGroovyScriptExpression("fails", "8/0", String.class.getName()));
        addConnector.ignoreError();
        final ProcessDefinition processDefinition = deployProcessWithActorAndTestConnectorThatThrowException(
                processDefinitionBuilder, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        // the connector must trigger this exception step
        waitForUserTaskAndExecuteIt(processInstance, "step1", user);
        waitForProcessToFinish(processInstance);
        // clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void connectorThatThrowExceptionIgnorePolicyOnTaskOutput() throws Exception {
        final Expression outputOfConnectorExpression = new ExpressionBuilder()
                .createConstantStringExpression("outputExpression");
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder()
                .createNewInstance("processWithConnector", "1.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addShortTextData("outputOfConnector", outputOfConnectorExpression);
        final UserTaskDefinitionBuilder userTaskBuilder = processDefinitionBuilder.addUserTask("step1", ACTOR_NAME);
        final ConnectorDefinitionBuilder addConnector = userTaskBuilder.addConnector("myConnector",
                CONNECTOR_WITH_OUTPUT_ID, "1.0", ConnectorEvent.ON_ENTER);
        addConnector.addInput(CONNECTOR_INPUT_NAME, new ExpressionBuilder().createConstantStringExpression("a"));
        addConnector.addOutput(new LeftOperandBuilder().createNewInstance().setName("outputOfConnector").done(),
                OperatorType.ASSIGNMENT, "=", "",
                new ExpressionBuilder().createGroovyScriptExpression("concat", "8/0", String.class.getName()));
        addConnector.ignoreError();
        final ProcessDefinition processDefinition = deployProcessWithActorAndTestConnectorWithOutput(
                processDefinitionBuilder, ACTOR_NAME, user);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        // the connector must trigger this exception step
        waitForUserTaskAndExecuteIt(startProcess, "step1", user);
        waitForProcessToFinish(startProcess);
        // clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void connectorThatThrowExceptionFailPolicyOnTaskOutput() throws Exception {
        final Expression dataDefaultValue = new ExpressionBuilder().createConstantStringExpression("NaN");
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder()
                .createNewInstance("processWithConnector", "1.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addShortTextData("result", dataDefaultValue);
        final UserTaskDefinitionBuilder userTaskBuilder = processDefinitionBuilder.addUserTask("step1", ACTOR_NAME);
        final ConnectorDefinitionBuilder addConnector = userTaskBuilder.addConnector("myConnector",
                CONNECTOR_WITH_OUTPUT_ID, "1.0", ConnectorEvent.ON_ENTER);
        addConnector.addInput(CONNECTOR_INPUT_NAME, new ExpressionBuilder().createConstantStringExpression("a"));
        addConnector.addOutput(new LeftOperandBuilder().createNewInstance().setName("outputOfConnector").done(),
                OperatorType.ASSIGNMENT, "=", "",
                new ExpressionBuilder().createGroovyScriptExpression("concat", "8/0", String.class.getName()));

        final ProcessDefinition processDefinition = deployProcessWithActorAndTestConnectorWithOutput(
                processDefinitionBuilder, ACTOR_NAME, user);
        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        final ActivityInstance activityInstance = waitForTaskToFail(startProcess);
        final SearchOptions searchOptions = getFirst100ConnectorInstanceSearchOptions(activityInstance.getId(),
                FLOWNODE).done();
        final SearchResult<ConnectorInstance> connectorInstances = getProcessAPI()
                .searchConnectorInstances(searchOptions);
        assertEquals(1, connectorInstances.getCount());

        disableAndDeleteProcess(processDefinition);
    }

    private SearchOptionsBuilder getFirst100ConnectorInstanceSearchOptions(final long containerId,
            final String containerType) {
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 100);
        searchOptionsBuilder.filter(ConnectorInstancesSearchDescriptor.CONTAINER_ID, containerId);
        searchOptionsBuilder.filter(ConnectorInstancesSearchDescriptor.CONTAINER_TYPE, containerType);
        searchOptionsBuilder.filter(ConnectorInstancesSearchDescriptor.STATE, ConnectorState.FAILED.name());
        searchOptionsBuilder.sort(ConnectorInstancesSearchDescriptor.NAME, Order.ASC);
        return searchOptionsBuilder;
    }

    @Test
    public void executeConnectorThatThrowRuntimeExceptionFailUserTaskOnEnter() throws Exception {
        final ProcessDefinition processDefinition = getProcessWithConnectorThatThrowErrorOnUserTaskOnEnter("runtime",
                FailAction.FAIL, false);
        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        waitForTaskToFail(startProcess);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void executeConnectorThatThrowRuntimeExceptionInConnectFailUserTaskOnEnter() throws Exception {
        final ProcessDefinition processDefinition = getProcessWithConnectorThatThrowErrorOnUserTaskOnEnter("connect",
                FailAction.FAIL, false);
        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        waitForTaskToFail(startProcess);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void executeConnectorThatThrowRuntimeExceptionInDisconnectFailUserTaskOnEnter() throws Exception {
        final ProcessDefinition processDefinition = getProcessWithConnectorThatThrowErrorOnUserTaskOnEnter("disconnect",
                FailAction.FAIL, false);
        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        waitForTaskToFail(startProcess);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void executeConnectorThatThrowRuntimeExceptionFailAutomaticTaskOnEnter() throws Exception {
        final ProcessDefinition processDefinition = getProcessWithConnectorThatThrowErrorOnAutomaticTaskOnEnter(
                "runtime", FailAction.FAIL, false);
        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        waitForTaskToFail(startProcess);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void connectorThatThrowRuntimeExceptionFailTask() throws Exception {
        final ProcessDefinition processDefinition = getProcessWithConnectorThatThrowErrorOnAutomaticTaskOnFinish(
                "runtime", FailAction.FAIL, false);
        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        waitForTaskToFail(startProcess);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void connectorThatThrowRuntimeExceptionFailProcess() throws Exception {
        final ProcessDefinition processDefinition = getProcessWithConnectorThatThrowErrorOnUserTaskOnEnter("runtime",
                FailAction.FAIL, true);
        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        waitForProcessToBeInState(startProcess, ProcessInstanceState.ERROR);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void connectorThatThrowExceptionFailProcess() throws Exception {
        final ProcessDefinition processDefinition = getProcessWithConnectorThatThrowErrorOnUserTaskOnEnter("normal",
                FailAction.FAIL, true);
        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        waitForProcessToBeInState(startProcess, ProcessInstanceState.ERROR);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void connectorThatThrowExceptionIgnorePolicyOnTask() throws Exception {
        final ProcessDefinition processDefinition = getProcessWithConnectorThatThrowErrorOnUserTaskOnEnter("normal",
                FailAction.IGNORE, false);
        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        waitForUserTask(startProcess, "step1");

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void connectorThatThrowExceptionIgnorePolicyOnProcess() throws Exception {
        final ProcessDefinition processDefinition = getProcessWithConnectorThatThrowErrorOnUserTaskOnEnter("normal",
                FailAction.IGNORE, true);
        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        waitForUserTask(startProcess, "step1");

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void connectorThatThrowExceptionErrorEventPolicyWithEventSubProcessOnTask() throws Exception {
        final ProcessDefinition processDefinition = getProcessWithConnectorThatThrowErrorOnUserTaskOnEnter("normal",
                FailAction.ERROR_EVENT, false);
        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        waitForUserTask(startProcess, "errorTask");
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void connectorThatThrowExceptionErrorEventPolicyWithEventSubProcessOnProcess() throws Exception {
        final ProcessDefinition processDefinition = getProcessWithConnectorThatThrowErrorOnUserTaskOnEnter("normal",
                FailAction.ERROR_EVENT, true);
        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinitionId);
        waitForUserTask(startProcess, "errorTask");
        disableAndDeleteProcess(processDefinition);
    }

    private ProcessDefinition getProcessWithConnectorThatThrowError(final String exceptionType,
            final FailAction failAction, final boolean onProcess,
            final boolean withUserTask, final boolean onEnter) throws BonitaException, IOException {
        final Expression outputOfConnectorExpression = new ExpressionBuilder()
                .createConstantStringExpression("outputExpression");
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder()
                .createNewInstance("processWithConnector", "1.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addShortTextData("outputOfConnector", outputOfConnectorExpression);

        // Add a task
        final ActivityDefinitionBuilder activityDefinitionBuilder;
        if (withUserTask) {
            activityDefinitionBuilder = processDefinitionBuilder.addUserTask("step1", ACTOR_NAME);
        } else {
            activityDefinitionBuilder = processDefinitionBuilder.addAutomaticTask("step1");
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
            addConnector = processDefinitionBuilder.addConnector("testConnectorThatThrowException",
                    "testConnectorThatThrowException", "1.0", connectorEvent);
        } else {
            addConnector = activityDefinitionBuilder.addConnector("testConnectorThatThrowException",
                    "testConnectorThatThrowException", "1.0", connectorEvent);
        }
        addConnector.addInput("kind", new ExpressionBuilder().createConstantStringExpression(exceptionType));
        switch (failAction) {
            case ERROR_EVENT:
                addConnector.throwErrorEventWhenFailed("error");
                final SubProcessDefinitionBuilder subProcessBuilder = processDefinitionBuilder
                        .addSubProcess("errorSub", true).getSubProcessBuilder();
                subProcessBuilder.addStartEvent("errorstart").addErrorEventTrigger("error");
                subProcessBuilder.addUserTask("errorTask", ACTOR_NAME);
                subProcessBuilder.addTransition("errorstart", "errorTask");
                break;
            case IGNORE:
                addConnector.ignoreError();
                break;
            case FAIL:
            default:
                break;
        }

        return deployProcessWithActorAndTestConnectorThatThrowException(processDefinitionBuilder, ACTOR_NAME, user);
    }

    private ProcessDefinition getProcessWithConnectorThatThrowErrorOnUserTaskOnEnter(final String exceptionType,
            final FailAction failAction,
            final boolean onProcess) throws BonitaException, IOException {
        return getProcessWithConnectorThatThrowError(exceptionType, failAction, onProcess, true, true);
    }

    private ProcessDefinition getProcessWithConnectorThatThrowErrorOnAutomaticTaskOnEnter(final String exceptionType,
            final FailAction failAction,
            final boolean onProcess) throws BonitaException, IOException {
        return getProcessWithConnectorThatThrowError(exceptionType, failAction, onProcess, false, true);
    }

    private ProcessDefinition getProcessWithConnectorThatThrowErrorOnAutomaticTaskOnFinish(final String exceptionType,
            final FailAction failAction,
            final boolean onProcess) throws BonitaException, IOException {
        return getProcessWithConnectorThatThrowError(exceptionType, failAction, onProcess, false, false);
    }

    @Test
    public void connectorThatThrowExceptionErrorEventPolicyBoundaryOnTaskShouldAbortCurrentTask() throws Exception {
        final Expression outputOfConnectorExpression = new ExpressionBuilder()
                .createConstantStringExpression("outputExpression");
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder()
                .createNewInstance("processWithConnector", "1.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addShortTextData("outputOfConnector", outputOfConnectorExpression);
        final UserTaskDefinitionBuilder userTaskBuilder = processDefinitionBuilder.addUserTask("step1", ACTOR_NAME);
        final ConnectorDefinitionBuilder addConnector;
        addConnector = userTaskBuilder.addConnector("testConnectorThatThrowException",
                "testConnectorThatThrowException", "1.0", ConnectorEvent.ON_ENTER);
        addConnector.addInput("kind", new ExpressionBuilder().createConstantStringExpression("normal"));
        addConnector.throwErrorEventWhenFailed("error");
        final BoundaryEventDefinitionBuilder boundaryEvent = userTaskBuilder.addBoundaryEvent("errorBoundary", true);
        boundaryEvent.addErrorEventTrigger("error");
        processDefinitionBuilder.addUserTask("errorTask", ACTOR_NAME);
        processDefinitionBuilder.addTransition("errorBoundary", "errorTask");
        final ProcessDefinition processDefinition = deployProcessWithActorAndTestConnectorThatThrowException(
                processDefinitionBuilder, ACTOR_NAME, user);
        try {
            final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
            // the connector must trigger this exception step
            waitForUserTask(startProcess, "errorTask");

            waitForFlowNodeInState(startProcess, "step1", TestStates.ABORTED, false);
        } finally {
            // clean up
            disableAndDeleteProcess(processDefinition);
        }
    }

    @Test
    public void connectorThatThrowExceptionErrorEventPolicyNotCatchOnTask() throws Exception {
        final Expression outputOfConnectorExpression = new ExpressionBuilder()
                .createConstantStringExpression("outputExpression");
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder()
                .createNewInstance("processWithConnector", "1.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addShortTextData("outputOfConnector", outputOfConnectorExpression);
        final UserTaskDefinitionBuilder userTaskBuilder = processDefinitionBuilder.addUserTask("step1", ACTOR_NAME);
        final ConnectorDefinitionBuilder addConnector;
        addConnector = userTaskBuilder.addConnector("testConnectorThatThrowException",
                "testConnectorThatThrowException", "1.0", ConnectorEvent.ON_ENTER);
        addConnector.addInput("kind", new ExpressionBuilder().createConstantStringExpression("normal"));
        addConnector.throwErrorEventWhenFailed("error");
        final ProcessDefinition processDefinition = deployProcessWithActorAndTestConnectorThatThrowException(
                processDefinitionBuilder, ACTOR_NAME, user);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());

        // the connector must trigger this exception step
        waitForTaskToFail(startProcess);

        // clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void connectorThatThrowExceptionErrorEventPolicyCallActivityOnTask() throws Exception {
        // create the process with connector throwing error
        final Expression outputOfConnectorExpression = new ExpressionBuilder()
                .createConstantStringExpression("outputExpression");
        ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder()
                .createNewInstance("processWithConnector", "1.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addShortTextData("outputOfConnector", outputOfConnectorExpression).addUserTask("step1",
                ACTOR_NAME);
        final UserTaskDefinitionBuilder userTaskBuilder = processDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        final ConnectorDefinitionBuilder addConnector = userTaskBuilder.addConnector("testConnectorThatThrowException",
                "testConnectorThatThrowException",
                "1.0", ConnectorEvent.ON_ENTER);
        addConnector.addInput("kind", new ExpressionBuilder().createConstantStringExpression("normal"));
        addConnector.throwErrorEventWhenFailed("error");
        processDefinitionBuilder.addTransition("step1", "step2");
        final ProcessDefinition calledProcess = deployProcessWithActorAndTestConnectorThatThrowException(
                processDefinitionBuilder, ACTOR_NAME, user);

        // create parent process with call activity and boundary
        processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("parentProcess", "1.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        final CallActivityBuilder callActivityBuilder = processDefinitionBuilder.addCallActivity("processWithConnector",
                new ExpressionBuilder().createConstantStringExpression("processWithConnector"),
                new ExpressionBuilder().createConstantStringExpression("1.0"));
        final BoundaryEventDefinitionBuilder boundaryEvent = callActivityBuilder.addBoundaryEvent("errorBoundary",
                true);
        boundaryEvent.addErrorEventTrigger("error");
        processDefinitionBuilder.addUserTask("errorTask", ACTOR_NAME);
        processDefinitionBuilder.addTransition("errorBoundary", "errorTask");
        final ProcessDefinition callingProcess = deployAndEnableProcessWithActor(processDefinitionBuilder.done(),
                ACTOR_NAME, user);
        // start parent
        final ProcessInstance processInstance = getProcessAPI().startProcess(callingProcess.getId());

        final HumanTaskInstance step1 = waitForUserTaskAndExecuteAndGetIt("step1", user);
        // the connector must trigger this exception step of the calling process
        waitForUserTaskAndExecuteIt(processInstance, "errorTask", user);
        waitForProcessToFinish(processInstance);
        waitForProcessToBeInState(step1.getParentProcessInstanceId(), ProcessInstanceState.ABORTED);

        // clean up
        disableAndDeleteProcess(callingProcess, calledProcess);
    }

    @Test
    public void should_order_connectors_by_execution_order_when_no_filter_are_set() throws Exception {
        AutomaticTaskDefinitionBuilder automaticTask = new ProcessDefinitionBuilder()
                .createNewInstance("processWithConnectors", " 1.0")
                .addAutomaticTask("step");
        automaticTask.addConnector("enter1", "connectorDef1", "1.0", ConnectorEvent.ON_ENTER);
        automaticTask.addConnector("enter2", "failingConnector", "1.0", ConnectorEvent.ON_ENTER);
        automaticTask.addConnector("enter3", "connectorDef1", "1.0", ConnectorEvent.ON_ENTER);
        automaticTask.addConnector("finish1", "connectorDef1", "1.0", ConnectorEvent.ON_FINISH);
        automaticTask.addConnector("finish2", "connectorDef1", "1.0", ConnectorEvent.ON_FINISH);
        BusinessArchive bar = new BusinessArchiveBuilder().createNewBusinessArchive()
                .setProcessDefinition(automaticTask
                        .getProcess())
                .addConnectorImplementation(
                        generateConnectorImplementation("connectorDef1", "1.0", DoNothingConnector.class))
                .addConnectorImplementation(
                        generateConnectorImplementation("failingConnector", "1.0", FailingConnector.class))
                .done();
        ProcessDefinition processDefinition = getProcessAPI().deploy(bar);
        getProcessAPI().enableProcess(processDefinition.getId());
        ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        FlowNodeInstance failedTask = waitForFlowNodeInFailedState(processInstance, "step");

        SearchResult<ConnectorInstance> connectorInstances = getProcessAPI().searchConnectorInstances(
                new SearchOptionsBuilder(0, 100)
                        .filter(ConnectorInstancesSearchDescriptor.CONTAINER_ID, failedTask.getId())
                        .done());

        Assertions.assertThat(connectorInstances.getResult()).extracting("name", "state")
                .containsExactly(
                        tuple("enter1", ConnectorState.DONE),
                        tuple("enter2", ConnectorState.FAILED),
                        tuple("enter3", ConnectorState.TO_BE_EXECUTED),
                        tuple("finish1", ConnectorState.TO_BE_EXECUTED),
                        tuple("finish2", ConnectorState.TO_BE_EXECUTED));

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void searchArchivedConnectorInstance() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder()
                .createNewInstance("executeConnectorOnActivityInstance", "1.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder
                .addConnector("myConnectorOnProcess", CONNECTOR_WITH_OUTPUT_ID, "1.0", ConnectorEvent.ON_ENTER)
                .addInput(CONNECTOR_INPUT_NAME,
                        new ExpressionBuilder().createConstantStringExpression("value1"));
        processDefinitionBuilder.addAutomaticTask("step1")
                .addConnector("myConnectorOnStep", CONNECTOR_WITH_OUTPUT_ID, "1.0", ConnectorEvent.ON_ENTER)
                .addInput(CONNECTOR_INPUT_NAME, new ExpressionBuilder().createConstantStringExpression("value1"));
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addTransition("step1", "step2");
        final ProcessDefinition processDefinition = deployProcessWithActorAndTestConnectorWithOutput(
                processDefinitionBuilder, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step2Id = waitForUserTask(processInstance, "step2");
        // search with filter on name
        SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 100);
        searchOptionsBuilder.filter(ArchiveConnectorInstancesSearchDescriptor.NAME, "myConnectorOnStep");
        searchOptionsBuilder.sort(ArchiveConnectorInstancesSearchDescriptor.NAME, Order.ASC);
        SearchOptions searchOptions = searchOptionsBuilder.done();
        SearchResult<ArchivedConnectorInstance> connectorInstances = getProcessAPI()
                .searchArchivedConnectorInstances(searchOptions);
        Assertions.assertThat(connectorInstances.getResult()).extracting("name").containsExactly("myConnectorOnStep");

        // finish process
        assignAndExecuteStep(step2Id, userId);
        waitForProcessToFinish(processInstance);

        // search for archived connector instances
        searchOptionsBuilder = new SearchOptionsBuilder(0, 100);
        searchOptionsBuilder.filter(ArchiveConnectorInstancesSearchDescriptor.CONTAINER_ID, processInstance.getId());
        searchOptionsBuilder.filter(ArchiveConnectorInstancesSearchDescriptor.CONTAINER_TYPE, PROCESS);
        searchOptionsBuilder.sort(ArchiveConnectorInstancesSearchDescriptor.NAME, Order.ASC);
        searchOptions = searchOptionsBuilder.done();
        connectorInstances = getProcessAPI().searchArchivedConnectorInstances(searchOptions);
        Assertions.assertThat(connectorInstances.getResult()).extracting("name")
                .containsExactly("myConnectorOnProcess");

        // search for archived connector instances using SOURCE_OBJECT_ID
        searchOptionsBuilder = new SearchOptionsBuilder(0, 100);
        searchOptionsBuilder.filter(ArchiveConnectorInstancesSearchDescriptor.SOURCE_OBJECT_ID,
                connectorInstances.getResult().get(0).getSourceObjectId());
        searchOptions = searchOptionsBuilder.done();
        Assertions.assertThat(getProcessAPI().searchArchivedConnectorInstances(searchOptions).getResult())
                .extracting("name")
                .containsOnly("myConnectorOnProcess");

        // now also connector of process is archived
        searchOptionsBuilder = new SearchOptionsBuilder(0, 100);
        searchOptionsBuilder.sort(ArchiveConnectorInstancesSearchDescriptor.NAME, Order.ASC);
        searchOptions = searchOptionsBuilder.done();
        connectorInstances = getProcessAPI().searchArchivedConnectorInstances(searchOptions);
        Assertions.assertThat(connectorInstances.getResult()).extracting("name").containsExactly("myConnectorOnProcess",
                "myConnectorOnStep");

        disableAndDeleteProcess(processDefinition);

        // check all archived connectors are deleted
        searchOptionsBuilder = new SearchOptionsBuilder(0, 100);
        searchOptions = searchOptionsBuilder.done();
        connectorInstances = getProcessAPI().searchArchivedConnectorInstances(searchOptions);
        assertTrue("there should be no archived connector anymore", connectorInstances.getResult().isEmpty());
    }

    @Test
    public void connectorWithExternalLibraryInInputAndOutput() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder();
        final ProcessDefinitionBuilder pBuilder = processDefinitionBuilder.createNewInstance("emptyProcess",
                String.valueOf(System.currentTimeMillis()));
        final UserTaskDefinitionBuilder addUserTask = pBuilder.addActor(ACTOR_NAME).addUserTask("step1", ACTOR_NAME);
        addUserTask.addData("dataActivity", java.lang.Object.class.getName(),
                new ExpressionBuilder().createGroovyScriptExpression("myScript",
                        "new org.bonitasoft.dfgdfg.Restaurant()", java.lang.Object.class.getName()));
        addUserTask
                .addConnector("myConnector1", "connectorInJar", "1.0.0", ConnectorEvent.ON_ENTER)
                .addInput("input",
                        new ExpressionBuilder().createGroovyScriptExpression("myScript",
                                "new org.bonitasoft.dfgdfg.Restaurant()", Object.class.getName()))
                .addOutput(
                        new OperationBuilder().createSetDataOperation("dataActivity",
                                new ExpressionBuilder().createGroovyScriptExpression("myScript",
                                        "new org.bonitasoft.dfgdfg.Restaurant()", "org.bonitasoft.dfgdfg.Restaurant")));
        final DesignProcessDefinition done = pBuilder.done();
        final BusinessArchiveBuilder builder = new BusinessArchiveBuilder().createNewBusinessArchive()
                .setProcessDefinition(done);
        builder.addConnectorImplementation(
                getResource("/org/bonitasoft/engine/connectors/TestConnectorInJar.impl", "TestConnectorInJar.impl"));
        builder.addClasspathResource(
                getResource("/org/bonitasoft/engine/connectors/connector-in-jar.jar.bak", "connector-in-jar.jar"));
        builder.addClasspathResource(getResource("/org.bonitasoft.dfgdfg.bak", "org.bonitasoft.dfgdfg.jar"));
        final BusinessArchive businessArchive = builder.done();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(businessArchive, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance, "step1");
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void executeConnectorOnProcessDefinitionWithOperations() throws Exception {
        final Expression input1Expression = new ExpressionBuilder().createInputExpression("valueOfInput1",
                String.class.getName());
        final Expression input2Expression = new ExpressionBuilder().createInputExpression("valueOfInput2",
                String.class.getName());
        final Expression mainExp = new ExpressionBuilder().createExpression("param1",
                "'welcome '+valueOfInput1+' and '+valueOfInput2",
                ExpressionType.TYPE_READ_ONLY_SCRIPT.toString(), String.class.getName(), "GROOVY",
                Arrays.asList(input1Expression, input2Expression));

        // process with data "Mett"
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder()
                .createNewInstance("executeConnectorOnActivityInstance", "1.0");
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addUserTask("step1", ACTOR_NAME);
        final ProcessDefinition processDefinition = deployProcessWithExternalTestConnector(designProcessDefinition,
                ACTOR_NAME, user);

        // execute connector with operations:
        // connector return param1="welcome Lily and Lucy and Mett"
        // operations: put "Jack" in data valueOfInput3, param1 in "externalData" and "John" in "externalDataConst"
        // Create Operation map:
        final List<Operation> operations = new ArrayList<>(2);
        // set valueOfInput3
        final Map<String, Serializable> contexts = new HashMap<>();
        operations.add(new OperationBuilder().createNewInstance().setLeftOperand("externalData", true)
                .setRightOperand(new ExpressionBuilder().createInputExpression("param1", String.class.getName()))
                .setType(OperatorType.ASSIGNMENT).done());
        operations.add(new OperationBuilder().createNewInstance().setLeftOperand("externalDataConst", true)
                .setRightOperand(new ExpressionBuilder().createConstantStringExpression("John"))
                .setType(OperatorType.ASSIGNMENT).done());
        final Map<String, Expression> connectorInputParameters = getConnectorInputParameters("param1", mainExp);
        final Map<String, Map<String, Serializable>> inputValues = getInputValues("param1",
                Arrays.asList("valueOfInput1", "valueOfInput2"),
                Arrays.asList("Lily", "Lucy"));

        final Map<String, Serializable> res = getProcessAPI().executeConnectorOnProcessDefinition(
                ConnectorExecutionIT.DEFAULT_EXTERNAL_CONNECTOR_ID,
                ConnectorExecutionIT.DEFAULT_EXTERNAL_CONNECTOR_VERSION, connectorInputParameters, inputValues,
                operations, contexts,
                processDefinition.getId());
        assertEquals("welcome Lily and Lucy", res.get("externalData"));
        assertEquals("John", res.get("externalDataConst"));

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    // The connector output is list to which an element is added when the method connect or disconnect is called.
    // when the output operations are executed only the method is connect is supposed to be called, that is, the list must contain only one element.
    public void executeConnectorWithConnectedResouce() throws Exception {
        final String dataName = "isConnected";
        final String userTaskName = "step1";
        final ProcessDefinition processDefinition = deployProcessWithConnectorWithConnectedResources(dataName,
                userTaskName);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance, userTaskName);

        final DataInstance dataInstance = getProcessAPI().getProcessDataInstance(dataName, processInstance.getId());
        assertEquals(1, dataInstance.getValue());

        disableAndDeleteProcess(processDefinition);
    }

    private ProcessDefinition deployProcessWithConnectorWithConnectedResources(final String dataName,
            final String userTaskName)
            throws InvalidExpressionException, BonitaException, IOException {
        // expression to get the connector output
        final Expression connectorOutPutExpr = new ExpressionBuilder().createInputExpression("output",
                List.class.getName());
        // expression to get the list size
        final Expression rightSideOperation = new ExpressionBuilder().createJavaMethodCallExpression("getOutPut",
                "size", Integer.class.getName(),
                connectorOutPutExpr);
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder()
                .createNewInstance("proc", "1.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addIntegerData(dataName, null);// data to store the list size
        processDefinitionBuilder.addUserTask(userTaskName, ACTOR_NAME)
                .addConnector("myConnector", "org.bonitasoft.connector.testConnectorWithConnectedResource", "1.0",
                        ConnectorEvent.ON_ENTER)
                .addOutput(new OperationBuilder().createSetDataOperation(dataName, rightSideOperation));

        return deployProcessWithActorAndTestConnectorWithConnectedResource(processDefinitionBuilder, ACTOR_NAME, user);
    }

    @Test
    public void executeConnectorOnEnterAfterUserFilter() throws Exception {
        final String dataName = "taskAssignee";

        final User jack = createUser("jack", "bpm");

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder()
                .createNewInstance("processWithConnector", "1.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addLongData(dataName, null);

        // expression to get the connector output
        final Expression connectorOutPutExpr = new ExpressionBuilder().createInputExpression(
                ExpressionConstants.TASK_ASSIGNEE_ID.getEngineConstantName(),
                Long.class.getName());

        final UserTaskDefinitionBuilder addUserTask = processDefinitionBuilder.addUserTask("step1", ACTOR_NAME);
        addUserTask.addConnector("myConnector", "org.bonitasoft.connector.testConnectorEngineExecutionContext", "1.0",
                ConnectorEvent.ON_ENTER).addOutput(
                        new OperationBuilder().createSetDataOperation(dataName, connectorOutPutExpr));
        addUserTask.addUserFilter("test", "org.bonitasoft.engine.filter.user.testFilterWithAutoAssign", "1.0").addInput(
                "userId",
                new ExpressionBuilder().createConstantLongExpression(userId));

        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addTransition("step1", "step2");

        final List<User> userIds = new ArrayList<>();
        userIds.add(user);
        userIds.add(jack);
        final ProcessDefinition processDefinition = deployProcessWithActorAndTestConnectorEngineExecutionContextAndFilterWithAutoAssign(
                processDefinitionBuilder, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(processInstance, "step1", jack);
        waitForUserTask(processInstance, "step2");

        final DataInstance processDataInstance = getProcessAPI().getProcessDataInstance(dataName,
                processInstance.getId());
        assertEquals(Long.valueOf(userId), processDataInstance.getValue());

        disableAndDeleteProcess(processDefinition);
        deleteUser(jack);
    }

    @Test
    public void getEngineExecutionContext() throws Exception {
        final String dataName = "taskAssignee";
        final String step1 = "step1";
        final String step2 = "step2";
        final ProcessDefinition processDefinition = deployProcWithConnectorEngineExecContext(dataName, step1, step2);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(processInstance, step1, user);
        waitForUserTask(processInstance, step2);

        final DataInstance dataInstance = getProcessAPI().getProcessDataInstance(dataName, processInstance.getId());
        assertEquals(userId, dataInstance.getValue());

        disableAndDeleteProcess(processDefinition);
    }

    private ProcessDefinition deployProcWithConnectorEngineExecContext(final String dataName, final String step1,
            final String step2)
            throws InvalidExpressionException, BonitaException, IOException {
        // expression to get the connector output
        final Expression connectorOutPutExpr = new ExpressionBuilder().createInputExpression(
                ExpressionConstants.TASK_ASSIGNEE_ID.getEngineConstantName(),
                Long.class.getName());

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder()
                .createNewInstance("proc", "1.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addLongData(dataName, null);// data to store the connector output
        processDefinitionBuilder.addUserTask(step1, ACTOR_NAME)
                .addConnector("myConnector", "org.bonitasoft.connector.testConnectorEngineExecutionContext", "1.0",
                        ConnectorEvent.ON_FINISH)
                .addOutput(new OperationBuilder().createSetDataOperation(dataName, connectorOutPutExpr));
        processDefinitionBuilder.addUserTask(step2, ACTOR_NAME);
        processDefinitionBuilder.addTransition(step1, step2);

        return deployProcessWithActorAndTestConnectorEngineExecutionContext(processDefinitionBuilder, ACTOR_NAME, user);
    }

    @Test
    public void executeConnectorWithInputExpressionUsingAPI() throws Exception {
        final String definitionId = "connectorThatUseAPI";
        final String definitionVersion = "1.0";
        final byte[] connectorImplementationFile = BuildTestUtil.buildConnectorImplementationFile(definitionId,
                definitionVersion, "impl1", "1.0",
                TestConnectorWithOutput.class.getName());

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder()
                .createNewInstance("ProcessWithConnector", "1.12");
        builder.addActor(ACTOR_NAME);
        builder.addLongData("numberOfUser", new ExpressionBuilder().createConstantLongExpression(-1));
        final ConnectorDefinitionBuilder connector = builder.addAutomaticTask("step1").addConnector("theConnector",
                definitionId, definitionVersion,
                ConnectorEvent.ON_ENTER);

        String script = "org.bonitasoft.engine.identity.User createUser = apiAccessor.getIdentityAPI().createUser(new org.bonitasoft.engine.identity.UserCreator(\"myUser\", \"password\"));\n";
        script += "apiAccessor.getProcessAPI().attachDocument(processInstanceId, \"a\", \"a\", \"application/pdf\",\"test\".getBytes());";
        script += "return apiAccessor.getIdentityAPI().getNumberOfUsers();";

        connector.addInput(
                CONNECTOR_INPUT_NAME,
                new ExpressionBuilder().createGroovyScriptExpression("script", script, Long.class.getName(),
                        new ExpressionBuilder().createEngineConstant(ExpressionConstants.API_ACCESSOR),
                        new ExpressionBuilder().createEngineConstant(ExpressionConstants.PROCESS_INSTANCE_ID)));
        connector.addOutput(new OperationBuilder().createSetDataOperation("numberOfUser",
                new ExpressionBuilder().createInputExpression(CONNECTOR_OUTPUT_NAME, Long.class.getName())));
        builder.addUserTask("step2", ACTOR_NAME);
        builder.addTransition("step1", "step2");

        final BusinessArchiveBuilder barBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        barBuilder.addConnectorImplementation(new BarResource("connector.impl", connectorImplementationFile));
        barBuilder.addClasspathResource(
                new BarResource("connector.jar", IOUtil.generateJar(TestConnectorWithOutput.class)));
        barBuilder.setProcessDefinition(builder.done());
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(barBuilder.done(), ACTOR_NAME,
                user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance, "step2");
        final DataInstance numberOfUser = getProcessAPI().getProcessDataInstance("numberOfUser",
                processInstance.getId());
        assertEquals(2L, numberOfUser.getValue());
        final SearchResult<Document> documents = getProcessAPI().searchDocuments(
                new SearchOptionsBuilder(0, 10)
                        .filter(DocumentsSearchDescriptor.PROCESSINSTANCE_ID, processInstance.getId()).done());
        assertEquals(1, documents.getCount());
        disableAndDeleteProcess(processDefinition);
        getIdentityAPI().deleteUser("myUser");

    }

    @Test
    public void getConnectorWithFailureInformationOnConnectorExecution() throws Exception {
        //given
        final ProcessDefinition processDefinition = deployAndEnableProcessWithFailingConnector();
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance failedTask = waitForTaskToFail(processInstance);
        final SearchResult<ConnectorInstance> searchResult = getProcessAPI().searchConnectorInstances(
                getFirst100ConnectorInstanceSearchOptions(failedTask.getId(), FLOWNODE).done());
        Assertions.assertThat(searchResult.getCount()).isEqualTo(1);
        Assertions.assertThat(searchResult.getResult().get(0).getState()).isEqualTo(ConnectorState.FAILED);

        //when
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ArchivedFlowNodeInstanceSearchDescriptor.ORIGINAL_FLOW_NODE_ID, Order.ASC);
        builder.filter(ArchivedFlowNodeInstanceSearchDescriptor.ORIGINAL_FLOW_NODE_ID, failedTask.getId());
        builder.filter(ArchivedFlowNodeInstanceSearchDescriptor.STATE_NAME, "executing");
        final SearchResult<ArchivedFlowNodeInstance> archivedFlowNodeInstanceSearchResult = getProcessAPI()
                .searchArchivedFlowNodeInstances(builder.done());

        //then
        Assertions.assertThat(archivedFlowNodeInstanceSearchResult.getCount()).isEqualTo(1);

        disableAndDeleteProcess(processDefinition);
    }

    private ProcessDefinition deployAndEnableProcessWithFailingConnector() throws BonitaException, IOException {
        final Expression normal = new ExpressionBuilder()
                .createConstantStringExpression(TestConnectorThatThrowException.NORMAL);
        final Expression defaultValueExpression = new ExpressionBuilder().createConstantStringExpression("initial");
        final String dataName = "myVar";

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder()
                .createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addShortTextData(dataName, defaultValueExpression);
        designProcessDefinition
                .addAutomaticTask("step1")
                .addConnector("myConnector", TEST_CONNECTOR_THAT_THROW_EXCEPTION_ID, "1.0", ConnectorEvent.ON_ENTER)
                .addInput("kind", normal)
                .addOutput(new LeftOperandBuilder().createNewInstance().setName(dataName).done(),
                        OperatorType.ASSIGNMENT, "=", "",
                        new ExpressionBuilder().createInputExpression("output1", String.class.getName()));

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActorAndTestConnectorThatThrowException(
                designProcessDefinition, ACTOR_NAME,
                user);
        return processDefinition;
    }

    public ProcessDefinition deployAndEnableProcessWithActorAndTestConnectorThatThrowException(
            final ProcessDefinitionBuilder processDefinitionBuilder,
            final String actor, final User user) throws BonitaException, IOException {
        return deployAndEnableProcessWithActorAndConnectorAndParameter(processDefinitionBuilder, actor, user, null,
                "TestConnectorThatThrowException.impl", TestConnectorThatThrowException.class,
                "TestConnectorThatThrowException.jar");
    }

    @Test
    public void should_be_able_to_execute_process_connector_without_flownode() throws Exception {

        ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder()
                .createNewInstance("ProcessWithoutFlownodes", "1.0");
        processBuilder.addActor(ACTOR_NAME);
        processBuilder
                .addConnector("myConnector", "org.bonitasoft.connector.testConnector", "1.0", ConnectorEvent.ON_ENTER)
                .addInput(TestConnector.INPUT1,
                        new ExpressionBuilder().createConstantStringExpression("valueOfInput1"));

        ProcessDefinition processDefinition = deployProcessWithActorAndTestConnector(processBuilder, ACTOR_NAME, user);

        ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        waitForProcessToFinish(processInstance);
    }

}
