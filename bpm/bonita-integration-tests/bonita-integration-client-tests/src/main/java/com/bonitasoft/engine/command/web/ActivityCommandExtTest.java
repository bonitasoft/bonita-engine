package com.bonitasoft.engine.command.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.BPMRemoteTests;
import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveFactory;
import org.bonitasoft.engine.bpm.model.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.model.ConnectorDefinition;
import org.bonitasoft.engine.bpm.model.ConnectorEvent;
import org.bonitasoft.engine.bpm.model.HumanTaskInstance;
import org.bonitasoft.engine.bpm.model.ProcessDefinition;
import org.bonitasoft.engine.bpm.model.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.model.ProcessInstance;
import org.bonitasoft.engine.bpm.model.data.DataInstance;
import org.bonitasoft.engine.bpm.model.impl.ConnectorDefinitionImpl;
import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connectors.TestConnector;
import org.bonitasoft.engine.connectors.TestConnectorWithOutput;
import org.bonitasoft.engine.connectors.TestExternalConnector;
import org.bonitasoft.engine.connectors.VariableStorage;
import org.bonitasoft.engine.core.operation.LeftOperand;
import org.bonitasoft.engine.core.operation.LeftOperandBuilder;
import org.bonitasoft.engine.core.operation.Operation;
import org.bonitasoft.engine.core.operation.OperationBuilder;
import org.bonitasoft.engine.core.operation.OperatorType;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.InvalidExpressionException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.WaitUntil;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.bonitasoft.engine.util.IOUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.api.ProcessAPI;

/**
 * @author Ruiheng Fan
 * @author Celine Souchet
 */
public class ActivityCommandExtTest extends CommonAPITest {

    private final String COMMAND_EXECUTE_OPERATIONS_AND_TERMINATE_EXT = "executeActionsAndTerminateExt";

    private final String COMMAND_EXECUTE_ACTIONS_AND_START_INSTANCE_EXT = "executeActionsAndStartInstanceExt";

    protected static final String USERNAME = "dwight";

    protected static final String PASSWORD = "Schrute";

    protected ProcessDefinition processDefinition;

    protected User businessUser;

    final String dataName = "var1";

    final String dataName2 = "var2";

    final String dataName3 = "var3";

    final String inputName = "input1";

    final String delivery = "Delivery men";

    @Before
    public void before() throws Exception {
        login();
        businessUser = createUser(USERNAME, PASSWORD);
        logout();
        loginWith(USERNAME, PASSWORD);
    }

    @After
    public void after() throws BonitaException {
        disableAndDelete(processDefinition);
        deleteUser(businessUser.getId());
        logout();
    }

    private void createAndDeployProcess() throws Exception {
        final String valueOfInput1 = "valueOfInput1";
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression(valueOfInput1);
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0");
        processBuilder.addActor(delivery).addIntegerData(dataName, new ExpressionBuilder().createConstantIntegerExpression(1));
        processBuilder.addConnector("myConnector", "org.bonitasoft.connector.testConnector", "1.0", ConnectorEvent.ON_ENTER).addInput(inputName,
                input1Expression);
        processBuilder.addUserTask("step1", delivery).addUserTask("step2", delivery).addTransition("step1", "step2");
        processDefinition = deployProcessWithDefaultTestConnector(delivery, businessUser.getId(), processBuilder);
    }

    private void createAndDeployProcess2() throws Exception {
        // Data expression
        final Expression defaultExpression = new ExpressionBuilder().createConstantStringExpression("defaultString");

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("executeConnectorOnActivityInstance", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addStringData(dataName, defaultExpression);
        designProcessDefinition.addBooleanData(dataName2, new ExpressionBuilder().createConstantBooleanExpression(false));
        designProcessDefinition.addStringData(dataName3, defaultExpression);
        designProcessDefinition.addUserTask("step1", delivery);
        designProcessDefinition.addUserTask("step2", delivery);
        designProcessDefinition.addTransition("step1", "step2");
        processDefinition = deployProcessWithExternalTestConnector(designProcessDefinition, delivery, businessUser.getId());
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "ExecuteActionsAndStartInstanceExt" }, jira = "ENGINE-732, ENGINE-726")
    @Test
    public void testInstantiateProcessWithDataConversionOperation() throws Exception {
        final BusinessArchive businessArchive = BusinessArchiveFactory.readBusinessArchive(this.getClass().getResourceAsStream(
                "Pool2--1.0.bar"));
        processDefinition = getProcessAPI().deploy(businessArchive);
        getProcessAPI().enableProcess(processDefinition.getId());

        final HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
        // final Map<String, Map<String, Serializable>> inputValues = new HashMap<String, Map<String, Serializable>>();
        final HashMap<Operation, Map<String, Serializable>> operations = new HashMap<Operation, Map<String, Serializable>>();
        final Map<String, Serializable> contexts = new HashMap<String, Serializable>();
        final String myDdataName = "mon_entier_1";
        final String fieldName = "field_entier";
        final LeftOperand leftOperand = new LeftOperandBuilder().createNewInstance().setName(myDdataName).done();
        final Expression expression = new ExpressionBuilder().createGroovyScriptExpression("int_conversion_script", "Integer.parseInt(" + fieldName + ")",
                Integer.class.getName(), new ExpressionBuilder().createInputExpression(fieldName, String.class.getName()));
        final Operation operation = new OperationBuilder().createNewInstance().setOperator("=").setLeftOperand(leftOperand).setType(OperatorType.ASSIGNMENT)
                .setRightOperand(expression).done();
        final String enteredValue = "17";
        contexts.put(fieldName, enteredValue);

        operations.put(operation, contexts);
        parameters.put("CONNECTORS_MAP_KEY", new HashMap<ConnectorDefinition, Map<String, Map<String, Serializable>>>(0));
        parameters.put("PROCESS_DEFINITION_ID_KEY", processDefinition.getId());
        parameters.put("OPERATIONS_MAP_KEY", operations);
        parameters.put("USER_ID_KEY", businessUser.getId());
        final long processInstanceId = (Long) getCommandAPI().execute(COMMAND_EXECUTE_ACTIONS_AND_START_INSTANCE_EXT, parameters);
        assertTrue("processInstanceId should be > 0", processInstanceId > 0);
        final DataInstance dataInstance = getProcessAPI().getProcessDataInstance(myDdataName, processInstanceId);
        assertEquals(Integer.parseInt(enteredValue), ((Integer) dataInstance.getValue()).intValue());
        waitForUserTask("Étape1", getProcessAPI().getProcessInstance(processInstanceId));
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "ExecuteActionsAndStartInstanceExt" }, jira = "ENGINE-732, ENGINE-726")
    @Test
    public void testExecuteActionsAndStartInstanceExt() throws Exception {
        final String valueOfInput1 = "valueOfInput1";
        createAndDeployProcess();

        final String userName = "first";
        final String password = "user";
        final User firstUser = createUser(userName, password);

        final HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
        final ConnectorDefinition connectDefinition = new ConnectorDefinitionImpl("myConnector", "org.bonitasoft.connector.testConnector", "1.0",
                ConnectorEvent.ON_ENTER);
        final Map<String, Map<String, Serializable>> inputValues = new HashMap<String, Map<String, Serializable>>();
        final HashMap<ConnectorDefinition, Map<String, Map<String, Serializable>>> connectors = new HashMap<ConnectorDefinition, Map<String, Map<String, Serializable>>>();
        final HashMap<Operation, Map<String, Serializable>> operations = new HashMap<Operation, Map<String, Serializable>>();
        final Map<String, Serializable> contexts = new HashMap<String, Serializable>();
        contexts.put("page", "1");
        final Operation integerOperation = buildIntegerOperation(dataName, 2);
        operations.put(integerOperation, contexts);
        connectors.put(connectDefinition, inputValues);
        parameters.put("CONNECTORS_MAP_KEY", connectors);
        parameters.put("PROCESS_DEFINITION_ID_KEY", processDefinition.getId());
        parameters.put("OPERATIONS_MAP_KEY", operations);
        parameters.put("USER_ID_KEY", firstUser.getId());
        final long processInstanceId = (Long) getCommandAPI().execute(COMMAND_EXECUTE_ACTIONS_AND_START_INSTANCE_EXT, parameters);
        assertTrue("no pending user task instances are found", new WaitUntil(50, 1000) {

            @Override
            protected boolean check() throws Exception {
                return getProcessAPI().getPendingHumanTaskInstances(getSession().getUserId(), 0, 10, null).size() >= 1;
            }
        }.waitUntil());
        final WaitUntil waitUntil = new WaitUntil(50, 2000, false) {

            @Override
            protected boolean check() throws Exception {
                return VariableStorage.getInstance().getVariableValue(inputName).equals(valueOfInput1);
            }
        };
        assertTrue(waitUntil.waitUntil());
        final HumanTaskInstance userTaskInstance = getProcessAPI().getPendingHumanTaskInstances(getSession().getUserId(), 0, 1,
                ActivityInstanceCriterion.NAME_ASC).get(0);
        final String activityName = userTaskInstance.getName();
        assertNotNull(activityName);
        assertEquals("step1", activityName);

        final ProcessInstance processInstance = getProcessAPI().getProcessInstance(processInstanceId);
        assertEquals(firstUser.getId(), processInstance.getStartedBy());
        deleteUser(firstUser);
    }

    // Connector in Form: for Input parameter, try type_input for ExpressionType.TYPE_READ_ONLY_SCRIPT
    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "ExecuteActionsAndTerminateTaskExt" }, jira = "ENGINE-732, ENGINE-726")
    @Test
    public void testExecuteActionsAndTerminate() throws Exception {
        createAndDeployProcess2();
        final String valueOfInput1 = "Lily";
        final String valueOfInput2 = "Lucy";
        final String valueOfInput3 = "Mett";
        final String mainExpContent = "'welcome '+valueOfInput1+' and '+valueOfInput2+' and '+valueOfInput3";
        final String inputName1 = "valueOfInput1";
        final String inputName2 = "valueOfInput2";
        final String inputName3 = "valueOfInput3";
        final String mainInputName1 = "param1";
        final String resContent = "welcome Lily and Lucy and Mett";

        final long processInstanceId = getProcessAPI().startProcess(processDefinition.getId()).getId();
        assertTrue(new WaitForStep(50, 2000, "step1", processInstanceId).waitUntil());

        final Map<String, Serializable> fieldValues = new HashMap<String, Serializable>();
        fieldValues.put("field_fieldId1", "Ryan");
        fieldValues.put(inputName1, valueOfInput1);
        fieldValues.put(inputName2, valueOfInput2);
        fieldValues.put(inputName3, valueOfInput3);

        // Input expression
        final Expression input1Expression = new ExpressionBuilder().createInputExpression(inputName1, String.class.getName());
        final Expression input2Expression = new ExpressionBuilder().createInputExpression(inputName2, String.class.getName());

        final Expression input3Expression = new ExpressionBuilder().createDataExpression(inputName3, String.class.getName());

        // Main Expression
        final Expression mainExp = new ExpressionBuilder().createExpression(mainInputName1, mainExpContent, ExpressionType.TYPE_READ_ONLY_SCRIPT.toString(),
                String.class.getName(), "GROOVY", Arrays.asList(input1Expression, input2Expression, input3Expression));

        final Map<String, Map<String, Serializable>> inputValues = getInputValues(mainInputName1, getInputValueNames(fieldValues),
                getInputValueValues(fieldValues));

        HumanTaskInstance userTaskInstance = getProcessAPI().getPendingHumanTaskInstances(getSession().getUserId(), 0, 1, ActivityInstanceCriterion.NAME_ASC)
                .get(0);
        final long taskId = userTaskInstance.getId();
        getProcessAPI().assignUserTask(taskId, getSession().getUserId());

        final Expression rightOperand = new ExpressionBuilder().createInputExpression("field_fieldId1", String.class.getName());
        final Expression rightOperand2 = new ExpressionBuilder().createInputExpression("hasBeenValidated", Boolean.class.getName());
        final Expression rightOperand3 = new ExpressionBuilder().createInputExpression(mainInputName1, String.class.getName());
        final Operation operation = createOperation(dataName, OperatorType.ASSIGNMENT, "=", rightOperand);
        final Operation operation2 = createOperation(dataName2, OperatorType.ASSIGNMENT, "=", rightOperand2);
        final Operation operation3 = createOperation(dataName3, OperatorType.ASSIGNMENT, "=", rightOperand3);
        final Map<Operation, Map<String, Serializable>> operationsMap = new HashMap<Operation, Map<String, Serializable>>();
        operationsMap.put(operation, fieldValues);
        final HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
        final ConnectorDefinitionImpl connectDefinition = new ConnectorDefinitionImpl("myConnector", "org.bonitasoft.connector.testExternalConnector", "1.0",
                ConnectorEvent.ON_ENTER);
        connectDefinition.addInput(mainInputName1, mainExp);
        connectDefinition.addOutput(operation2);
        connectDefinition.addOutput(operation3);
        final HashMap<ConnectorDefinition, Map<String, Map<String, Serializable>>> connectors = new HashMap<ConnectorDefinition, Map<String, Map<String, Serializable>>>();
        connectors.put(connectDefinition, inputValues);
        parameters.put("CONNECTORS_MAP_KEY", connectors);
        parameters.put("ACTIVITY_INSTANCE_ID_KEY", taskId);
        parameters.put("OPERATIONS_MAP_KEY", (Serializable) operationsMap);
        getCommandAPI().execute(COMMAND_EXECUTE_OPERATIONS_AND_TERMINATE_EXT, parameters);

        assertTrue("no pending user task instances are found", new WaitUntil(50, 1000) {

            @Override
            protected boolean check() throws Exception {
                return getProcessAPI().getPendingHumanTaskInstances(getSession().getUserId(), 0, 10, null).size() >= 1;
            }
        }.waitUntil());
        userTaskInstance = getProcessAPI().getPendingHumanTaskInstances(getSession().getUserId(), 0, 1, ActivityInstanceCriterion.NAME_ASC).get(0);
        final String activityName = userTaskInstance.getName();
        assertNotNull(activityName);
        assertEquals("step2", activityName);
        final DataInstance dataInstance = getProcessAPI().getProcessDataInstance(dataName, processInstanceId);
        assertEquals("Ryan", dataInstance.getValue().toString());
        final DataInstance dataInstance2 = getProcessAPI().getProcessDataInstance(dataName2, processInstanceId);
        assertTrue(Boolean.valueOf(dataInstance2.getValue().toString()));
        final DataInstance dataInstance3 = getProcessAPI().getProcessDataInstance(dataName3, processInstanceId);
        assertEquals(resContent, dataInstance3.getValue().toString());
    }

    // Connector in Form: for Input parameter, try type_input for ExpressionType.TYPE_INPUT
    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "ExecuteActionsAndTerminateTaskExt" }, jira = "ENGINE-732, ENGINE-726")
    @Test
    public void testExecuteActionsAndTerminate2() throws Exception {
        createAndDeployProcess2();
        final String mainInputName1 = "param1";
        final long processInstanceId = getProcessAPI().startProcess(processDefinition.getId()).getId();
        assertTrue(new WaitForStep(50, 2000, "step1", processInstanceId).waitUntil());

        // Main Expression
        final Map<String, Serializable> fieldValues = new HashMap<String, Serializable>();
        fieldValues.put("field_fieldId1", "field_fieldId1_Ryan");
        final List<String> names = getInputValueNames(fieldValues);
        final List<String> values = getInputValueValues(fieldValues);
        final Expression mainExp = new ExpressionBuilder().createExpression(mainInputName1, "field_fieldId1", ExpressionType.TYPE_INPUT.toString(),
                String.class.getName(), null, null);

        final Map<String, Map<String, Serializable>> inputValues = getInputValues(mainInputName1, names, values);

        HumanTaskInstance userTaskInstance = getProcessAPI().getPendingHumanTaskInstances(getSession().getUserId(), 0, 1, ActivityInstanceCriterion.NAME_ASC)
                .get(0);
        final long taskId = userTaskInstance.getId();
        getProcessAPI().assignUserTask(taskId, getSession().getUserId());

        final Expression rightOperand = new ExpressionBuilder().createInputExpression("field_fieldId1", String.class.getName());
        final Expression rightOperand2 = new ExpressionBuilder().createInputExpression("hasBeenValidated", Boolean.class.getName());
        final Expression rightOperand3 = new ExpressionBuilder().createInputExpression(mainInputName1, String.class.getName());
        final Operation operation = createOperation(dataName, OperatorType.ASSIGNMENT, "=", rightOperand);
        final Operation operation2 = createOperation(dataName2, OperatorType.ASSIGNMENT, "=", rightOperand2);
        final Operation operation3 = createOperation(dataName3, OperatorType.ASSIGNMENT, "=", rightOperand3);
        final Map<Operation, Map<String, Serializable>> operationsMap = new HashMap<Operation, Map<String, Serializable>>();
        operationsMap.put(operation, fieldValues);
        final HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
        final ConnectorDefinitionImpl connectDefinition = new ConnectorDefinitionImpl("myConnector", "org.bonitasoft.connector.testExternalConnector", "1.0",
                ConnectorEvent.ON_ENTER);
        connectDefinition.addInput(mainInputName1, mainExp);
        connectDefinition.addOutput(operation2);
        connectDefinition.addOutput(operation3);
        final HashMap<ConnectorDefinition, Map<String, Map<String, Serializable>>> connectors = new HashMap<ConnectorDefinition, Map<String, Map<String, Serializable>>>();
        connectors.put(connectDefinition, inputValues);
        parameters.put("CONNECTORS_MAP_KEY", connectors);
        parameters.put("ACTIVITY_INSTANCE_ID_KEY", taskId);
        parameters.put("OPERATIONS_MAP_KEY", (Serializable) operationsMap);
        getCommandAPI().execute(COMMAND_EXECUTE_OPERATIONS_AND_TERMINATE_EXT, parameters);

        assertTrue("no pending user task instances are found", new WaitUntil(50, 1000) {

            @Override
            protected boolean check() throws Exception {
                return getProcessAPI().getPendingHumanTaskInstances(getSession().getUserId(), 0, 10, null).size() >= 1;
            }
        }.waitUntil());
        userTaskInstance = getProcessAPI().getPendingHumanTaskInstances(getSession().getUserId(), 0, 1, ActivityInstanceCriterion.NAME_ASC).get(0);
        final String activityName = userTaskInstance.getName();
        assertNotNull(activityName);
        assertEquals("step2", activityName);
        final DataInstance dataInstance = getProcessAPI().getProcessDataInstance(dataName, processInstanceId);
        assertEquals("field_fieldId1_Ryan", dataInstance.getValue().toString());
        final DataInstance dataInstance2 = getProcessAPI().getProcessDataInstance(dataName2, processInstanceId);
        assertTrue(Boolean.valueOf(dataInstance2.getValue().toString()));
        final DataInstance dataInstance3 = getProcessAPI().getProcessDataInstance(dataName3, processInstanceId);
        assertEquals("field_fieldId1_Ryan", dataInstance3.getValue().toString());
    }

    /**
     * @param fieldValues
     * @return
     */
    private List<String> getInputValueValues(final Map<String, Serializable> fieldValues) {
        final List<String> keyList = getInputValueNames(fieldValues);
        final List<String> list = new ArrayList<String>();
        for (final String key : keyList) {
            list.add(fieldValues.get(key).toString());
        }
        return list;
    }

    /**
     * @param fieldValues
     * @return
     */
    private List<String> getInputValueNames(final Map<String, Serializable> fieldValues) {
        final Iterator<String> keyIt = fieldValues.keySet().iterator();
        final List<String> list = new ArrayList<String>();
        while (keyIt.hasNext()) {
            list.add(keyIt.next());
        }
        return list;
    }

    private Operation buildIntegerOperation(final String dataInstanceName, final int newConstantValue) throws InvalidExpressionException {
        final LeftOperand leftOperand = new LeftOperandBuilder().createNewInstance().setName(dataInstanceName).done();
        final Expression expression = new ExpressionBuilder().createConstantIntegerExpression(newConstantValue);
        final Operation operation;
        operation = new OperationBuilder().createNewInstance().setOperator("=").setLeftOperand(leftOperand).setType(OperatorType.ASSIGNMENT)
                .setRightOperand(expression).done();
        return operation;
    }

    private ProcessDefinition deployProcessWithDefaultTestConnector(final String delivery, final long userId,
            final ProcessDefinitionBuilder designProcessDefinition) throws BonitaException, IOException {
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                designProcessDefinition.done());
        final ArrayList<BarResource> connectorImplementations = generateDefaultConnectorImplementations();
        for (final BarResource barResource : connectorImplementations) {
            businessArchiveBuilder.addConnectorImplementation(barResource);
        }

        final ArrayList<BarResource> generateConnectorDependencies = generateDefaultConnectorDependencies();
        for (final BarResource barResource : generateConnectorDependencies) {
            businessArchiveBuilder.addClasspathResource(barResource);
        }

        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchiveBuilder.done());
        addMappingOfActorsForUser(delivery, userId, processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    private ArrayList<BarResource> generateDefaultConnectorImplementations() throws IOException {
        final ArrayList<BarResource> resources = new ArrayList<BarResource>(2);
        addResource(resources, "/org/bonitasoft/engine/connectors/TestConnector.impl", "TestConnector.impl");
        addResource(resources, "/org/bonitasoft/engine/connectors/TestConnectorWithOutput.impl", "TestConnectorWithOutput.impl");
        return resources;
    }

    private ArrayList<BarResource> generateDefaultConnectorDependencies() throws IOException {
        final ArrayList<BarResource> resources = new ArrayList<BarResource>(2);
        addResource(resources, TestConnector.class, "TestConnector.jar");
        addResource(resources, TestConnectorWithOutput.class, "TestConnectorWithOutput.jar");
        return resources;
    }

    private void addResource(final List<BarResource> resources, final String path, final String name) throws IOException {
        final InputStream stream = BPMRemoteTests.class.getResourceAsStream(path);
        assertNotNull(stream);
        final byte[] byteArray = IOUtils.toByteArray(stream);
        stream.close();
        resources.add(new BarResource(name, byteArray));
    }

    private void addResource(final List<BarResource> resources, final Class<?> clazz, final String name) throws IOException {
        final byte[] data = IOUtil.generateJar(clazz);
        resources.add(new BarResource(name, data));
    }

    private Operation createOperation(final String dataName, final OperatorType operatorType, final String operator, final Expression rightOperand) {
        final OperationBuilder operationBuilder = new OperationBuilder().createNewInstance();
        operationBuilder.setOperator(operator);
        operationBuilder.setRightOperand(rightOperand);
        operationBuilder.setType(operatorType);
        operationBuilder.setLeftOperand(new LeftOperandBuilder().createNewInstance().setName(dataName).done());
        return operationBuilder.done();
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

    private ProcessDefinition deployProcessWithExternalTestConnector(final ProcessDefinitionBuilder designProcessDefinition, final String delivery,
            final long userId) throws BonitaException, IOException {
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                designProcessDefinition.done());

        addConnectorImplemWithDependency(businessArchiveBuilder, "/org/bonitasoft/engine/connectors/TestExternalConnector.impl", "TestExternalConnector.impl",
                TestExternalConnector.class, "TestExternalConnector.jar");

        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchiveBuilder.done());
        addMappingOfActorsForUser(delivery, userId, processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    private void addConnectorImplemWithDependency(final BusinessArchiveBuilder bizArchive, final String implemPath, final String implemName,
            final Class<? extends AbstractConnector> dependencyClassName, final String dependencyJarName) throws IOException {
        bizArchive.addConnectorImplementation(new BarResource(implemName, IOUtils.toByteArray(BPMRemoteTests.class.getResourceAsStream(implemPath))));
        bizArchive.addClasspathResource(new BarResource(dependencyJarName, IOUtil.generateJar(dependencyClassName)));
    }
}
