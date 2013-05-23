/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.command.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.BPMRemoteTests;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinitionWithInputValues;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.connector.impl.ConnectorDefinitionImpl;
import org.bonitasoft.engine.bpm.connector.impl.ConnectorDefinitionWithInputValuesImpl;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connectors.TestExternalConnector;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.operation.LeftOperand;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.test.WaitUntil;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.api.ProcessAPI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Ruiheng Fan
 * @author Celine Souchet
 * @author Elias Ricken de Medeiros
 */
public class ActivityCommandExtTest extends CommonAPISPTest {

    /*
     * List<Operation>
     */
    protected static final String OPERATIONS_LIST_KEY = "OPERATIONS_LIST_KEY";

    /*
     * Map<String, Object>
     */
    protected static final String OPERATIONS_INPUT_KEY = "OPERATIONS_INPUT_KEY";

    /*
     * List<ConnectorDefinitionWithInputValues>
     */
    protected static final String CONNECTORS_LIST_KEY = "CONNECTORS_LIST_KEY";

    private static final String ACTIVITY_INSTANCE_ID_KEY = "ACTIVITY_INSTANCE_ID_KEY";

    private final String COMMAND_EXECUTE_OPERATIONS_AND_TERMINATE_EXT = "executeActionsAndTerminateExt";

    private final String COMMAND_EXECUTE_ACTIONS_AND_START_INSTANCE_EXT = "executeActionsAndStartInstanceExt";

    protected static final String USERNAME = "dwight";

    protected static final String PASSWORD = "Schrute";

    protected ProcessDefinition processDefinition;

    protected User businessUser;

    private final String dataName = "var1";

    private final String dataName2 = "var2";

    private final String dataName3 = "var3";

    private final String intDataName = "intVar";

    private final String delivery = "Delivery men";

    @Before
    public void before() throws Exception {
        login();
        businessUser = createUser(USERNAME, PASSWORD);
        logout();
        loginWith(USERNAME, PASSWORD);
    }

    @After
    public void after() throws BonitaException {
        disableAndDeleteProcess(processDefinition);
        deleteUser(businessUser.getId());
        logout();
    }

    private void createAndDeployProcess() throws Exception {
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("executeConnectorOnActivityInstance", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addUserTask("step1", delivery);
        designProcessDefinition.addUserTask("step2", delivery);
        designProcessDefinition.addTransition("step1", "step2");
        designProcessDefinition.addShortTextData("text", new ExpressionBuilder().createConstantStringExpression("default"));
        processDefinition = deployProcessWithExternalTestConnector(designProcessDefinition, delivery, businessUser.getId());
    }

    private void createAndDeployProcess2() throws Exception {
        // Data expression
        final Expression defaultExpression = new ExpressionBuilder().createConstantStringExpression("defaultString");

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("executeConnectorOnActivityInstance", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addShortTextData(dataName, defaultExpression);
        designProcessDefinition.addBooleanData(dataName2, new ExpressionBuilder().createConstantBooleanExpression(false));
        designProcessDefinition.addShortTextData(dataName3, defaultExpression);
        designProcessDefinition.addIntegerData(intDataName, new ExpressionBuilder().createConstantIntegerExpression(0));
        designProcessDefinition.addUserTask("step1", delivery);
        designProcessDefinition.addUserTask("step2", delivery);
        designProcessDefinition.addTransition("step1", "step2");
        processDefinition = deployProcessWithExternalTestConnector(designProcessDefinition, delivery, businessUser.getId());
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "ExecuteActionsAndStartInstanceExt" }, jira = "ENGINE-732, ENGINE-726")
    @Test
    public void testInstantiateProcessWithDataConversionOperation() throws Exception {
        final String myDdataName = "mon_entier_1";
        final String actorName = "Employee";
        final String userTaskName = "Étape1";
        final ProcessDefinitionBuilder builder = deployProcessWithIntegerData(myDdataName, actorName, userTaskName);

        processDefinition = deployAndEnableWithActor(builder.done(), actorName, businessUser);

        final HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
        final String fieldName = "field_entier";
        final LeftOperand leftOperand = new LeftOperandBuilder().createNewInstance().setName(myDdataName).done();
        final Expression expression = new ExpressionBuilder().createGroovyScriptExpression("int_conversion_script", "Integer.parseInt(" + fieldName + ")",
                Integer.class.getName(), new ExpressionBuilder().createInputExpression(fieldName, String.class.getName()));
        final Operation operation = new OperationBuilder().createNewInstance().setOperator("=").setLeftOperand(leftOperand).setType(OperatorType.ASSIGNMENT)
                .setRightOperand(expression).done();
        final String enteredValue = "17";
        final ArrayList<Operation> operations = new ArrayList<Operation>(1);
        final HashMap<String, Serializable> contexts = new HashMap<String, Serializable>();
        contexts.put(fieldName, enteredValue);
        operations.add(operation);
        parameters.put(CONNECTORS_LIST_KEY, new ArrayList<ConnectorDefinitionWithInputValues>(0));
        parameters.put("PROCESS_DEFINITION_ID_KEY", processDefinition.getId());
        parameters.put(OPERATIONS_LIST_KEY, operations);
        parameters.put(OPERATIONS_INPUT_KEY, contexts);
        parameters.put("USER_ID_KEY", businessUser.getId());
        final long processInstanceId = (Long) getCommandAPI().execute(COMMAND_EXECUTE_ACTIONS_AND_START_INSTANCE_EXT, parameters);
        assertTrue("processInstanceId should be > 0", processInstanceId > 0);
        final DataInstance dataInstance = getProcessAPI().getProcessDataInstance(myDdataName, processInstanceId);
        assertEquals(Integer.parseInt(enteredValue), ((Integer) dataInstance.getValue()).intValue());
        waitForUserTask(userTaskName, getProcessAPI().getProcessInstance(processInstanceId));
    }

    private ProcessDefinitionBuilder deployProcessWithIntegerData(final String myDdataName, final String actorName, final String userTaskName) {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("Pool2", "1.0");
        builder.addIntegerData(myDdataName, null);
        builder.addActor(actorName, true);
        builder.addStartEvent("Début1");
        builder.addUserTask(userTaskName, actorName);
        builder.addEndEvent("Fin1");
        builder.addTransition("Début1", userTaskName);
        return builder;
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "ExecuteActionsAndStartInstanceExt" }, jira = "ENGINE-732, ENGINE-726")
    @Test
    public void testExecuteActionsAndStartInstanceExt() throws Exception {
        final String userName = "first";
        final String password = "user";
        final User firstUser = createUser(userName, password);

        createAndDeployProcess();
        final String valueOfInput1 = "Lily";
        final String valueOfInput2 = "Lucy";
        final String valueOfInput3 = "Mett";
        final String mainExpContent = "'welcome '+valueOfInput1+' and '+valueOfInput2+' and '+valueOfInput3";
        final String inputName1 = "valueOfInput1";
        final String inputName2 = "valueOfInput2";
        final String inputName3 = "valueOfInput3";
        final String mainInputName1 = "param1";
        final String resContent = "welcome Lily and Lucy and Mett";

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

        final HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
        final ConnectorDefinitionImpl connectDefinition = new ConnectorDefinitionImpl("myConnector", "org.bonitasoft.connector.testExternalConnector", "1.0",
                ConnectorEvent.ON_ENTER);
        connectDefinition.addInput(mainInputName1, mainExp);
        // set the data with the output of the connector
        connectDefinition.addOutput(new OperationBuilder().createSetDataOperation("text",
                new ExpressionBuilder().createInputExpression(mainInputName1, String.class.getName())));
        final ArrayList<ConnectorDefinitionWithInputValues> connectors = new ArrayList<ConnectorDefinitionWithInputValues>();
        connectors.add(new ConnectorDefinitionWithInputValuesImpl(connectDefinition, inputValues));

        final ArrayList<Operation> operations = new ArrayList<Operation>();
        final HashMap<String, Serializable> contexts = new HashMap<String, Serializable>();
        contexts.put("page", "1");
        final Operation integerOperation = buildIntegerOperation(dataName, 2);
        operations.add(integerOperation);
        parameters.put(CONNECTORS_LIST_KEY, connectors);
        parameters.put("PROCESS_DEFINITION_ID_KEY", processDefinition.getId());
        parameters.put(OPERATIONS_LIST_KEY, operations);
        parameters.put(OPERATIONS_INPUT_KEY, contexts);
        parameters.put("USER_ID_KEY", firstUser.getId());
        final long processInstanceId = (Long) getCommandAPI().execute(COMMAND_EXECUTE_ACTIONS_AND_START_INSTANCE_EXT, parameters);

        assertEquals(resContent, getProcessAPI().getProcessDataInstance("text", processInstanceId).getValue());

        assertNotNull("User task step1 don't exist.", waitForUserTask("step1", processInstanceId, 12000));
        HumanTaskInstance userTaskInstance = getProcessAPI().getPendingHumanTaskInstances(getSession().getUserId(), 0, 1, ActivityInstanceCriterion.NAME_ASC)
                .get(0);
        assignAndExecuteStep(userTaskInstance, getSession().getUserId());

        assertNotNull("User task step2 don't exist.", waitForUserTask("step2", processInstanceId));
        userTaskInstance = getProcessAPI().getPendingHumanTaskInstances(getSession().getUserId(), 0, 1, ActivityInstanceCriterion.NAME_ASC).get(0);
        final String activityName = userTaskInstance.getName();
        assertNotNull(activityName);
        assertEquals("step2", activityName);

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

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long processInstanceId = processInstance.getId();
        waitForStep("step1", processInstance);

        final HashMap<String, Serializable> fieldValues = new HashMap<String, Serializable>();
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
        final ArrayList<Operation> operations = new ArrayList<Operation>(1);
        operations.add(operation);
        final HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
        final ConnectorDefinitionImpl connectDefinition = new ConnectorDefinitionImpl("myConnector", "org.bonitasoft.connector.testExternalConnector", "1.0",
                ConnectorEvent.ON_ENTER);
        connectDefinition.addInput(mainInputName1, mainExp);
        connectDefinition.addOutput(operation2);
        connectDefinition.addOutput(operation3);
        final ArrayList<ConnectorDefinitionWithInputValuesImpl> connectors = new ArrayList<ConnectorDefinitionWithInputValuesImpl>();
        connectors.add(new ConnectorDefinitionWithInputValuesImpl(connectDefinition, inputValues));
        parameters.put(CONNECTORS_LIST_KEY, connectors);
        parameters.put(ACTIVITY_INSTANCE_ID_KEY, taskId);
        parameters.put(OPERATIONS_LIST_KEY, operations);
        parameters.put(OPERATIONS_INPUT_KEY, fieldValues);
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

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "ExecuteActionsAndTerminateTaskExt" }, jira = "ENGINE-1053")
    @Test
    public void testExecuteActionsAndTerminateDataExpression() throws Exception {
        // deploy and start a process containing a integer data
        createAndDeployProcess2();
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        // wait for the user task and assign it
        final ActivityInstance userTask = waitForUserTask("step1", processInstance.getId());
        getProcessAPI().assignUserTask(userTask.getId(), getSession().getUserId());

        // create operation to increment the variable
        final Expression dataExpression = new ExpressionBuilder().createDataExpression(intDataName, Integer.class.getName());
        final Expression rightOperand = new ExpressionBuilder().createGroovyScriptExpression("increment", intDataName + " + 1;", Integer.class.getName(),
                Collections.singletonList(dataExpression));

        final Operation operation = createOperation(intDataName, OperatorType.ASSIGNMENT, "=", rightOperand);
        final ArrayList<Operation> operations = new ArrayList<Operation>(1);
        operations.add(operation);
        // execute the command
        final HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(ACTIVITY_INSTANCE_ID_KEY, userTask.getId());
        parameters.put(OPERATIONS_LIST_KEY, operations);
        parameters.put(OPERATIONS_INPUT_KEY, new HashMap<String, Serializable>(0));

        getCommandAPI().execute(COMMAND_EXECUTE_OPERATIONS_AND_TERMINATE_EXT, parameters);

        // check that the variable was incremented
        final DataInstance dataInstance = getProcessAPI().getProcessDataInstance(intDataName, processInstance.getId());
        assertEquals(1, dataInstance.getValue());

    }

    // Connector in Form: for Input parameter, try type_input for ExpressionType.TYPE_INPUT
    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "ExecuteActionsAndTerminateTaskExt" }, jira = "ENGINE-732, ENGINE-726")
    @Test
    public void testExecuteActionsAndTerminate2() throws Exception {
        createAndDeployProcess2();
        final String mainInputName1 = "param1";
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long processInstanceId = processInstance.getId();
        waitForStep("step1", processInstance);

        // Main Expression
        final HashMap<String, Serializable> fieldValues = new HashMap<String, Serializable>();
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
        final ArrayList<Operation> operations = new ArrayList<Operation>(1);
        operations.add(operation);
        final HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
        final ConnectorDefinitionImpl connectDefinition = new ConnectorDefinitionImpl("myConnector", "org.bonitasoft.connector.testExternalConnector", "1.0",
                ConnectorEvent.ON_ENTER);
        connectDefinition.addInput(mainInputName1, mainExp);
        connectDefinition.addOutput(operation2);
        connectDefinition.addOutput(operation3);
        final ArrayList<ConnectorDefinitionWithInputValues> connectors = new ArrayList<ConnectorDefinitionWithInputValues>();
        connectors.add(new ConnectorDefinitionWithInputValuesImpl(connectDefinition, inputValues));
        parameters.put(CONNECTORS_LIST_KEY, connectors);
        parameters.put(ACTIVITY_INSTANCE_ID_KEY, taskId);
        parameters.put(OPERATIONS_LIST_KEY, operations);
        parameters.put(OPERATIONS_INPUT_KEY, fieldValues);
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

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.ACTIVITIES, keywords = { "Command", "Activity", "Action" }, story = "Execute actions and terminate with custom jar.", jira = "ENGINE-928")
    @Test
    public void testExecuteActionsAndTerminateWithCustomJarInOperation() throws Exception {
        // process is deployed here with a custom jar
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        final ExpressionBuilder expressionBuilder = new ExpressionBuilder();
        final String content = "Word";
        processBuilder.addData("Application", String.class.getName(), expressionBuilder.createConstantStringExpression(content));
        processBuilder.addActor("myActor");
        processBuilder.addUserTask("Request", "myActor");
        processBuilder.addUserTask("Approval", "myActor");
        processBuilder.addTransition("Request", "Approval");
        final DesignProcessDefinition designProcessDefinition = processBuilder.done();
        final BusinessArchiveBuilder builder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition);
        final InputStream stream = BPMRemoteTests.class.getResourceAsStream("/mylibrary-jar.bak");
        assertNotNull(stream);
        final byte[] byteArray = IOUtils.toByteArray(stream);
        builder.addClasspathResource(new BarResource("mylibrary.jar", byteArray));
        final BusinessArchive businessArchive = builder.done();
        processDefinition = getProcessAPI().deploy(businessArchive);
        addMappingOfActorsForUser("myActor", businessUser.getId(), processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        final long processInstanceID = getProcessAPI().startProcess(processDefinition.getId()).getId();
        // wait for first task and assign it
        final ActivityInstance userTaskInstance = waitForUserTask("Request", processInstanceID);
        final long taskId = userTaskInstance.getId();
        getProcessAPI().assignUserTask(taskId, getSession().getUserId());

        // execute it with operation using the command
        final HashMap<String, Serializable> fieldValues = new HashMap<String, Serializable>();
        // the operation execute a groovy script that depend in a class in the jar
        final Expression rightOperand = new ExpressionBuilder().createGroovyScriptExpression("myScript",
                "new org.bonitasoft.engine.test.TheClassOfMyLibrary().aPublicMethod()", String.class.getName());
        final Operation operation = createOperation("Application", OperatorType.ASSIGNMENT, "=", rightOperand);
        final ArrayList<Operation> operations = new ArrayList<Operation>(1);
        operations.add(operation);
        final HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(ACTIVITY_INSTANCE_ID_KEY, taskId);
        parameters.put(OPERATIONS_LIST_KEY, operations);
        parameters.put(OPERATIONS_INPUT_KEY, fieldValues);

        // just check the operation is executed normally
        getCommandAPI().execute(COMMAND_EXECUTE_OPERATIONS_AND_TERMINATE_EXT, parameters);
    }
}
