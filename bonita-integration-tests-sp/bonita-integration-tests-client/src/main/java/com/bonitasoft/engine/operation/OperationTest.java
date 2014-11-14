/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.operation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.xml.StringIndex;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.AutomaticTaskDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionConstants;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.operation.LeftOperand;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.bonitasoft.engine.xml.DocumentManager;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;

/**
 * @author Baptiste Mesta
 */
public class OperationTest extends CommonAPITest {

    private static final String JOHN = "john";

    private User john;

    @After
    public void afterTest() throws BonitaException {
        logoutOnTenant();
        loginOnDefaultTenantWithDefaultTechnicalUser();
        deleteUser(JOHN);
        logoutOnTenant();
    }

    @Before
    public void beforeTest() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        john = createUser(JOHN, "bpm");
        logoutOnTenant();
        loginOnDefaultTenantWith(JOHN, "bpm");
    }

    @Test
    public void executeStringOperationOnData() throws Exception {
        createAndExecuteProcessWithOperations("executeStringOperationOnData", Collections.singletonList("myData1"),
                Collections.singletonList(String.class.getName()),
                Collections.singletonList(new LeftOperandBuilder().createNewInstance().setName("myData1").done()),
                Collections.singletonList(OperatorType.ASSIGNMENT), Collections.singletonList("="),
                Collections.singletonList(new ExpressionBuilder().createConstantStringExpression("val1")),
                Collections.singletonList(new ExpressionBuilder().createConstantStringExpression("val2")), Collections.singletonList((Object) "val1"),
                Collections.singletonList((Object) "val2"));
    }

    @Test
    public void executeBooleanOperationOnData() throws Exception {
        createAndExecuteProcessWithOperations("executeBooleanOperationOnData", Collections.singletonList("myData1"),
                Collections.singletonList(Boolean.class.getName()),
                Collections.singletonList(new LeftOperandBuilder().createNewInstance().setName("myData1").done()),
                Collections.singletonList(OperatorType.ASSIGNMENT), Collections.singletonList("="),
                Collections.singletonList(new ExpressionBuilder().createConstantBooleanExpression(true)),
                Collections.singletonList(new ExpressionBuilder().createConstantBooleanExpression(false)), Collections.singletonList((Object) true),
                Collections.singletonList((Object) false));
    }

    @Cover(classes = StringIndex.class, concept = BPMNConcept.OTHERS, jira = "ENGINE-679", keywords = { "string index", "operation" }, story = "update a a string index using operation")
    @Test
    public void executeStringIndexOperation() throws Exception {
        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance("procWithStringIndexes", "1.0");
        final String actorName = "doctor";
        designProcessDefinition.addActor(actorName).addDescription("The doc'");
        designProcessDefinition.addUserTask("step1", actorName);
        designProcessDefinition.addUserTask("step3", actorName);
        final AutomaticTaskDefinitionBuilder addAutomaticTask = designProcessDefinition.addAutomaticTask("step2");
        addAutomaticTask.addOperation(new OperationBuilder().createSetStringIndexOperation(1,
                new ExpressionBuilder().createConstantStringExpression("newValue1")));
        addAutomaticTask.addOperation(new OperationBuilder().createSetStringIndexOperation(2,
                new ExpressionBuilder().createConstantStringExpression("newValue2")));
        addAutomaticTask.addOperation(new OperationBuilder().createSetStringIndexOperation(3,
                new ExpressionBuilder().createConstantStringExpression("newValue3")));
        addAutomaticTask.addOperation(new OperationBuilder().createSetStringIndexOperation(4,
                new ExpressionBuilder().createConstantStringExpression("newValue4")));
        addAutomaticTask.addOperation(new OperationBuilder().createSetStringIndexOperation(5,
                new ExpressionBuilder().createConstantStringExpression("newValue5")));
        designProcessDefinition.addTransition("step1", "step2");
        designProcessDefinition.addTransition("step2", "step3");
        designProcessDefinition.setStringIndex(1, "label1", new ExpressionBuilder().createConstantStringExpression("value1"));
        designProcessDefinition.setStringIndex(2, "label2", new ExpressionBuilder().createConstantStringExpression("value2"));
        designProcessDefinition.setStringIndex(3, "label3", new ExpressionBuilder().createConstantStringExpression("value3"));
        designProcessDefinition.setStringIndex(4, "label4", new ExpressionBuilder().createConstantStringExpression("value4"));
        designProcessDefinition.setStringIndex(5, "label5", new ExpressionBuilder().createConstantStringExpression("value5"));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), actorName, john);

        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance humanTaskInstance = waitForUserTask("step1", startProcess);
        ProcessInstance processInstance = getProcessAPI().getProcessInstance(startProcess.getId());
        assertEquals("value1", processInstance.getStringIndex1());
        assertEquals("value2", processInstance.getStringIndex2());
        assertEquals("value3", processInstance.getStringIndex3());
        assertEquals("value4", processInstance.getStringIndex4());
        assertEquals("value5", processInstance.getStringIndex5());
        assignAndExecuteStep(humanTaskInstance, john.getId());
        waitForUserTask("step3", startProcess);
        processInstance = getProcessAPI().getProcessInstance(startProcess.getId());
        assertEquals("newValue1", processInstance.getStringIndex1());
        assertEquals("newValue2", processInstance.getStringIndex2());
        assertEquals("newValue3", processInstance.getStringIndex3());
        assertEquals("newValue4", processInstance.getStringIndex4());
        assertEquals("newValue5", processInstance.getStringIndex5());
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = StringIndex.class, concept = BPMNConcept.OTHERS, jira = "ENGINE-679", keywords = { "string index", "operation" }, story = "update a a string index using operation that use data expression")
    @Test
    public void executeStringIndexOperationUsingData() throws Exception {
        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance("procWithStringIndexes", "1.0");
        final String actorName = "doctor";
        designProcessDefinition.addData("baseData", String.class.getName(), new ExpressionBuilder().createConstantStringExpression("baseValue"));
        designProcessDefinition.addActor(actorName).addDescription("The doc'");
        designProcessDefinition.addUserTask("step1", actorName);
        designProcessDefinition.addUserTask("step3", actorName);
        final AutomaticTaskDefinitionBuilder addAutomaticTask = designProcessDefinition.addAutomaticTask("step2");
        addAutomaticTask.addOperation(new LeftOperandBuilder().createDataLeftOperand("baseData"), OperatorType.ASSIGNMENT, null, null,
                new ExpressionBuilder().createConstantStringExpression("changedData"));
        addAutomaticTask.addOperation(new OperationBuilder().createSetStringIndexOperation(1,
                new ExpressionBuilder().createDataExpression("baseData", String.class.getName())));
        designProcessDefinition.addTransition("step1", "step2");
        designProcessDefinition.addTransition("step2", "step3");
        designProcessDefinition.setStringIndex(1, "label1", new ExpressionBuilder().createDataExpression("baseData", String.class.getName()));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), actorName, john);

        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance humanTaskInstance = waitForUserTask("step1", startProcess);
        ProcessInstance processInstance = getProcessAPI().getProcessInstance(startProcess.getId());
        assertEquals("baseValue", processInstance.getStringIndex1());
        assignAndExecuteStep(humanTaskInstance, john.getId());
        waitForUserTask("step3", startProcess);
        processInstance = getProcessAPI().getProcessInstance(startProcess.getId());
        assertEquals("changedData", processInstance.getStringIndex1());
        disableAndDeleteProcess(processDefinition);
    }

    @Test(expected = InvalidProcessDefinitionException.class)
    public void executeOperationWithNoExpression() throws Exception {
        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance("procWithStringIndexes", "1.0");
        final String actorName = "doctor";
        designProcessDefinition.addActor(actorName).addDescription("The doc'");
        designProcessDefinition.addUserTask("step1", actorName);
        designProcessDefinition.addUserTask("step3", actorName);
        final AutomaticTaskDefinitionBuilder addAutomaticTask = designProcessDefinition.addAutomaticTask("step2");
        addAutomaticTask.addOperation(new OperationBuilder().createSetStringIndexOperation(1, null));
        designProcessDefinition.addTransition("step1", "step2");
        designProcessDefinition.addTransition("step2", "step3");
        designProcessDefinition.done();
    }

    @Test
    public void executeIntegerOperationOnData() throws Exception {
        createAndExecuteProcessWithOperations("executeIntegerOperationOnData", Collections.singletonList("myData12"),
                Collections.singletonList(Integer.class.getName()),
                Collections.singletonList(new LeftOperandBuilder().createNewInstance().setName("myData12").done()),
                Collections.singletonList(OperatorType.ASSIGNMENT), Collections.singletonList("="),
                Collections.singletonList(new ExpressionBuilder().createConstantIntegerExpression(1)),
                Collections.singletonList(new ExpressionBuilder().createConstantIntegerExpression(2)), Collections.singletonList((Object) 1),
                Collections.singletonList((Object) 2));
    }

    @Test
    public void executeLongOperationOnData() throws Exception {
        createAndExecuteProcessWithOperations("executeLongOperationOnData", Collections.singletonList("myData12"),
                Collections.singletonList(Long.class.getName()),
                Collections.singletonList(new LeftOperandBuilder().createNewInstance().setName("myData12").done()),
                Collections.singletonList(OperatorType.ASSIGNMENT), Collections.singletonList("="),
                Collections.singletonList(new ExpressionBuilder().createConstantLongExpression(1234567891234567891l)),
                Collections.singletonList(new ExpressionBuilder().createConstantLongExpression(1234567891234567892l)),
                Collections.singletonList((Object) 1234567891234567891l), Collections.singletonList((Object) 1234567891234567892l));
    }

    @Test
    public void executeMultipleOperations() throws Exception {
        createAndExecuteProcessWithOperations("executeMultipleOperations", Arrays.asList("myData1", "myData2", "myData3"), Arrays.asList(
                String.class.getName(), Boolean.class.getName(), Long.class.getName()), Arrays.asList(
                new LeftOperandBuilder().createNewInstance().setName("myData1").done(), new LeftOperandBuilder().createNewInstance().setName("myData2").done(),
                new LeftOperandBuilder().createNewInstance().setName("myData3").done()), Arrays.asList(OperatorType.ASSIGNMENT, OperatorType.ASSIGNMENT,
                OperatorType.ASSIGNMENT), Arrays.asList("=", "=", "="), Arrays.asList(new ExpressionBuilder().createConstantStringExpression("test1"),
                new ExpressionBuilder().createConstantBooleanExpression(false), new ExpressionBuilder().createConstantLongExpression(1234567891234567891l)),
                Arrays.asList(new ExpressionBuilder().createConstantStringExpression("test2"), new ExpressionBuilder().createConstantBooleanExpression(true),
                        new ExpressionBuilder().createConstantLongExpression(1234567891234567892l)), Arrays.asList((Object) "test1", (Object) false,
                        (Object) 1234567891234567891l), Arrays.asList((Object) "test2", (Object) true, (Object) 1234567891234567892l));
    }

    @Test
    public void executeMultipleOperationsWithSameData() throws Exception {
        createAndExecuteProcessWithOperations("executeMultipleOperationsWithSameData", Arrays.asList("myData1", "myData2", "myData3", "myData1"),
                Arrays.asList(String.class.getName(), Boolean.class.getName(), Long.class.getName(), String.class.getName()), Arrays.asList(
                        new LeftOperandBuilder().createNewInstance().setName("myData1").done(), new LeftOperandBuilder().createNewInstance().setName("myData2")
                                .done(), new LeftOperandBuilder().createNewInstance().setName("myData3").done(), new LeftOperandBuilder().createNewInstance()
                                .setName("myData1").done()), Arrays.asList(OperatorType.ASSIGNMENT, OperatorType.ASSIGNMENT, OperatorType.ASSIGNMENT,
                        OperatorType.ASSIGNMENT), Arrays.asList("=", "=", "=", "="), Arrays.asList(
                        new ExpressionBuilder().createConstantStringExpression("test1"), new ExpressionBuilder().createConstantBooleanExpression(false),
                        new ExpressionBuilder().createConstantLongExpression(1234567891234567891l),
                        new ExpressionBuilder().createConstantStringExpression("test1")), Arrays.asList(
                        new ExpressionBuilder().createConstantStringExpression("test2"), new ExpressionBuilder().createConstantBooleanExpression(true),
                        new ExpressionBuilder().createConstantLongExpression(1234567891234567892l),
                        new ExpressionBuilder().createConstantStringExpression("test3")), Arrays.asList((Object) "test1", (Object) false,
                        (Object) 1234567891234567891l, (Object) "test1"), Arrays.asList((Object) "test3", (Object) true, (Object) 1234567891234567892l,
                        (Object) "test3"));
    }

    protected void createAndExecuteProcessWithOperations(final String procName, final List<String> dataName, final List<String> className,
            final List<LeftOperand> leftOperand, final List<OperatorType> assignment, final List<String> operator, final List<Expression> dataDefaultValue,
            final List<Expression> expression, final List<Object> valueBefore, final List<Object> valueAfter) throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(procName, "1.0");
        final HashSet<String> dataSet = new HashSet<String>(dataName.size());
        for (int i = 0; i < dataName.size(); i++) {
            final String name = dataName.get(i);
            if (!dataSet.contains(name)) {
                designProcessDefinition.addData(name, className.get(i), dataDefaultValue.get(i));
                dataSet.add(name);
            }
        }
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addUserTask("step0", delivery);
        final AutomaticTaskDefinitionBuilder addAutomaticTask = designProcessDefinition.addAutomaticTask("step1");
        for (int i = 0; i < leftOperand.size(); i++) {
            // No Java operation, so empty string passed:
            addAutomaticTask.addOperation(leftOperand.get(i), assignment.get(i), operator.get(i), "", expression.get(i));
        }
        designProcessDefinition.addUserTask("step2", delivery);
        designProcessDefinition.addTransition("step0", "step1");
        designProcessDefinition.addTransition("step1", "step2");
        designProcessDefinition.setStringIndex(1, "label1", new ExpressionBuilder().createConstantStringExpression("value1"));
        designProcessDefinition.setStringIndex(2, "label2", new ExpressionBuilder().createConstantStringExpression("value2"));
        designProcessDefinition.setStringIndex(3, "label3", new ExpressionBuilder().createConstantStringExpression("value3"));
        designProcessDefinition.setStringIndex(4, "label4", new ExpressionBuilder().createConstantStringExpression("value4"));
        designProcessDefinition.setStringIndex(5, "label5", new ExpressionBuilder().createConstantStringExpression("value5"));

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), delivery, john);

        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        final HashMap<String, Integer> inverDataOrder = new HashMap<String, Integer>(dataName.size());
        for (int i = dataName.size() - 1; i >= 0; i--) {
            final String name = dataName.get(i);
            if (!inverDataOrder.containsKey(name)) {
                inverDataOrder.put(name, i);
            }
        }
        for (final Entry<String, Integer> dataIndex : inverDataOrder.entrySet()) {
            assertEquals("before execution of operation " + dataIndex.getKey(), valueBefore.get(dataIndex.getValue()),
                    getProcessAPI().getProcessDataInstance(dataIndex.getKey(), startProcess.getId()).getValue());
        }

        final ActivityInstance waitForStep0 = waitForUserTask("step0", startProcess);
        assignAndExecuteStep(waitForStep0, john.getId());

        waitForUserTask("step2", startProcess).getId();

        for (final Entry<String, Integer> dataIndex : inverDataOrder.entrySet()) {
            assertEquals("after execution of operation " + dataIndex.getKey(), valueAfter.get(dataIndex.getValue()),
                    getProcessAPI().getProcessDataInstance(dataIndex.getKey(), startProcess.getId()).getValue());
        }
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void updateAnAttributeOfAnXMLNodeOfADataUsingXPath() throws Exception {
        final String defaultValue = "<root name=\"before\"/>";
        final String updatedValue = "<root name=\"after\"/>";
        final String xPathExpression = "/root/@name";
        final String updatedContent = "after";
        executeProcessAndUpdateData(defaultValue, xPathExpression, updatedContent, updatedValue);
    }

    @Test
    public void setTheContentOfAnXMLNodeOfADataUsingXPath() throws Exception {
        final String defaultValue = "<root />";
        final String updatedValue = "<root>after</root>";
        final String xPathExpression = "/root/text()";
        final String updatedContent = "after";
        executeProcessAndUpdateData(defaultValue, xPathExpression, updatedContent, updatedValue);
    }

    @Test
    public void updateTheContentOfAnXMLNodeOfADataUsingXPath() throws Exception {
        final String defaultValue = "<root>before</root>";
        final String updatedValue = "<root>after</root>";
        final String xPathExpression = "/root/text()";
        final String updatedContent = "after";
        executeProcessAndUpdateData(defaultValue, xPathExpression, updatedContent, updatedValue);
    }

    @Test
    public void updateTheContentOfASubNodeOfAnXMLNodeOfADataUsingXPath() throws Exception {
        final String defaultValue = "<node><subnode>before</subnode></node>";
        final String updatedValue = "<node><subnode>after</subnode></node>";
        final String xPathExpression = "/node/subnode[1]/text()";
        final String updatedContent = "after";
        executeProcessAndUpdateData(defaultValue, xPathExpression, updatedContent, updatedValue);
    }

    @Test
    public void removeAndCreateANewNodeOfAnXMLNodeOfADataUsingXPath() throws Exception {
        final String defaultValue = "<node><before/></node>";
        final String updatedValue = "<node><after/></node>";
        final String xPathExpression = "/node/before";
        final String updatedContent = "after";
        executeProcessAndUpdateDataUsingAGroovyScript("removeAndCreateANewNodeOfAnXMLNodeOfADataUsingXPath", defaultValue, xPathExpression, updatedContent,
                updatedValue);
    }

    private void executeProcessAndUpdateDataUsingAGroovyScript(final String name, final String defaultValue, final String xPathExpression,
            final String updatedContent, final String updatedValue) throws Exception {
        final ExpressionBuilder expressionBuilder = new ExpressionBuilder();
        final Expression rightOperand = expressionBuilder.createGroovyScriptExpression(name, "import javax.xml.parsers.DocumentBuilder;"
                + "import javax.xml.parsers.DocumentBuilderFactory;import org.w3c.dom.Node;"
                + "DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder(); return builder.newDocument().createElement(\""
                + updatedContent + "\");", Node.class.getName());
        executeProcessAndUpdateData(defaultValue, xPathExpression, rightOperand, updatedValue);
    }

    private void executeProcessAndUpdateData(final String defaultValue, final String xPathExpression, final String updatedContent, final String updatedValue)
            throws Exception {
        final ExpressionBuilder expressionBuilder = new ExpressionBuilder();
        final Expression rightOperand = expressionBuilder.createConstantStringExpression(updatedContent);
        executeProcessAndUpdateData(defaultValue, xPathExpression, rightOperand, updatedValue);
    }

    private void executeProcessAndUpdateData(final String defaultValue, final String xPathExpression, final Expression rightOperand, final String updatedValue)
            throws Exception {
        final ExpressionBuilder expressionBuilder = new ExpressionBuilder();
        final Expression defaultExpression = expressionBuilder.createConstantStringExpression(defaultValue);
        final String variableName = "var1";
        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance("XPathEvaluation", "1.0");
        designProcessDefinition.addXMLData(variableName, defaultExpression).setNamespace("http://www.w3c.org");
        designProcessDefinition.addActor("Workers");
        designProcessDefinition.addAutomaticTask("start").addUserTask("step1", "Workers")
                .addOperation(new OperationBuilder().createXPathOperation(variableName, xPathExpression, rightOperand)).addUserTask("step2", "Workers")
                .addTransition("start", "step1").addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), "Workers", john);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance waitForUserTask = waitForUserTask("step1", startProcess);

        assertEquals(defaultValue, getProcessAPI().getProcessDataInstance(variableName, startProcess.getId()).getValue());
        assignAndExecuteStep(waitForUserTask, john.getId());

        waitForUserTask("step2", startProcess);

        final Document expected = DocumentManager.generateDocument(updatedValue);
        final Document actual = DocumentManager
                .generateDocument((String) getProcessAPI().getProcessDataInstance(variableName, startProcess.getId()).getValue());

        XMLUnit.setIgnoreWhitespace(true);
        assertTrue(XMLUnit.compareXML(expected, actual).identical());
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void initializeAVariableUsingAPIAccessor() throws Exception {
        final ExpressionBuilder expressionBuilder = new ExpressionBuilder();
        final Expression apiAccessor = expressionBuilder.createAPIAccessorExpression();
        final List<Expression> dependencies = new ArrayList<Expression>(1);
        dependencies.add(apiAccessor);
        final Expression defaultExpression = expressionBuilder.createGroovyScriptExpression("initializeAVariableUsingAPIAccessor",
                "apiAccessor.getIdentityAPI().getNumberOfUsers()", Long.class.getName(), dependencies);

        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance("APIAccessorInGroovyScript", "1.0");
        designProcessDefinition.addLongData("users", defaultExpression);
        designProcessDefinition.addActor("Workers").addUserTask("step1", "Workers");
        designProcessDefinition.addAutomaticTask("start").addTransition("start", "step1");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), "Workers", john);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask("step1", startProcess);

        final long numberOfUsers = getIdentityAPI().getNumberOfUsers();
        assertEquals(numberOfUsers, getProcessAPI().getProcessDataInstance("users", startProcess.getId()).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void updateAVariableUsingLoggedUser() throws Exception {

        final LeftOperandBuilder leftOperandBuilder = new LeftOperandBuilder();
        final LeftOperand leftOperand = leftOperandBuilder.createDataLeftOperand("userId");

        final ExpressionBuilder expressionBuilder = new ExpressionBuilder();
        final Expression loggedUser = expressionBuilder.createEngineConstant(ExpressionConstants.LOGGED_USER_ID);

        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance("APIAccessorInGroovyScript", "1.0");
        designProcessDefinition.addLongData("userId", new ExpressionBuilder().createConstantLongExpression(123l));
        designProcessDefinition.addActor("Workers").addUserTask("step1", "Workers").addOperation(leftOperand, OperatorType.ASSIGNMENT, "=", null, loggedUser);
        designProcessDefinition.addAutomaticTask("start").addUserTask("step2", "Workers").addTransition("start", "step1").addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), "Workers", john);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt("step1", startProcess, john);
        waitForUserTask("step2", startProcess);
        assertEquals(john.getId(), getProcessAPI().getProcessDataInstance("userId", startProcess.getId()).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void updateAVariableUsingAPIAccessor() throws Exception {
        final LeftOperandBuilder leftOperandBuilder = new LeftOperandBuilder();
        final LeftOperand leftOperand = leftOperandBuilder.createNewInstance("users").done();

        final ExpressionBuilder expressionBuilder = new ExpressionBuilder();
        final Expression apiAccessor = expressionBuilder.createAPIAccessorExpression();
        final List<Expression> dependencies = new ArrayList<Expression>(1);
        dependencies.add(apiAccessor);
        final Expression defaultExpression = expressionBuilder.createGroovyScriptExpression("updateAVariableUsingAPIAccessor",
                "apiAccessor.getIdentityAPI().getNumberOfUsers()", Long.class.getName(), dependencies);

        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance("APIAccessorInGroovyScript", "1.0");
        designProcessDefinition.addLongData("users", null);
        designProcessDefinition.addActor("Workers").addUserTask("step1", "Workers")
                .addOperation(leftOperand, OperatorType.ASSIGNMENT, "=", null, defaultExpression);
        designProcessDefinition.addAutomaticTask("start").addUserTask("step2", "Workers").addTransition("start", "step1").addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), "Workers", john);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt("step1", startProcess, john);
        waitForUserTask("step2", startProcess);

        final long numberOfUsers = getIdentityAPI().getNumberOfUsers();
        assertEquals(numberOfUsers, getProcessAPI().getProcessDataInstance("users", startProcess.getId()).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void takeTransitionUsingAPIAccessor() throws Exception {
        final ExpressionBuilder expressionBuilder = new ExpressionBuilder();
        final Expression apiAccessor = expressionBuilder.createAPIAccessorExpression();
        final List<Expression> dependencies = new ArrayList<Expression>(1);
        dependencies.add(apiAccessor);
        final Expression defaultExpression = expressionBuilder.createGroovyScriptExpression("takeTransitionUsingAPIAccessor",
                "apiAccessor.getIdentityAPI().getNumberOfUsers() < 100", Boolean.class.getName(), dependencies);

        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance("APIAccessorInGroovyScript", "1.0");
        designProcessDefinition.addActor("Workers").addUserTask("step1", "Workers");
        designProcessDefinition.addAutomaticTask("start").addUserTask("step2", "Workers").addTransition("start", "step1", defaultExpression)
                .addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), "Workers", john);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask("step1", startProcess);

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Operation.class, concept = BPMNConcept.CONNECTOR, keywords = { "Operation", "JavaMethodOperationExecutorStrategy", "primitive type" }, story = "execution of an JavaMethod operation with primitive parameters", jira = "ENGINE-1067")
    @Test
    public void javaMethodOperationWithPrimitiveParameters() throws Exception {
        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance(
                "javaMethodOperationWithPrimitiveParameters",
                String.valueOf(System.currentTimeMillis()));
        final AutomaticTaskDefinitionBuilder task1Def = processDefinitionBuilder
                .addActor(ACTOR_NAME)
                .addData(
                        "myDatum",
                        "java.lang.StringBuilder",
                        new ExpressionBuilder().createGroovyScriptExpression("myInitGroovyScript", "new java.lang.StringBuilder(\"_\")",
                                "java.lang.StringBuilder")).addAutomaticTask("step1");
        task1Def.addOperation(new OperationBuilder().createJavaMethodOperation("myDatum", "append", "int",
                new ExpressionBuilder().createConstantIntegerExpression(55)));
        task1Def.addUserTask("step2", ACTOR_NAME).addTransition("step1", "step2");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, john);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance activityInstance = waitForUserTask("step2", processInstance);
        final DataInstance activityDataInstance = getProcessAPI().getActivityDataInstance("myDatum", activityInstance.getId());

        assertEquals(StringBuilder.class, activityDataInstance.getValue().getClass());
        assertEquals("_55", activityDataInstance.getValue().toString());

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { Operation.class }, concept = BPMNConcept.OPERATION, keywords = { "custom type", "java operation", "classloader" }, jira = "ENGINE-1067", story = "update a custom variable using a java operation")
    @Test
    public void executeJavaOperationWithCustomType() throws Exception {
        final BusinessArchiveBuilder builder = new BusinessArchiveBuilder().createNewBusinessArchive();
        builder.addClasspathResource(getResource("/custom-0.1.jar.bak", "custom-0.1.jar"));

        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance("ProcessWithCustomData", "1.0");
        designProcessDefinition.addData("adress", "org.bonitasoft.custom.Address", new ExpressionBuilder().createGroovyScriptExpression("create adress",
                "new org.bonitasoft.custom.Address(\"name1\",\"Rue ampère\",\"38000\",\"Grenoble\",\"France\")", "org.bonitasoft.custom.Address"));
        designProcessDefinition
                .addActor("Workers")
                .addUserTask("step1", "Workers")
                .addOperation(
                        new OperationBuilder().createJavaMethodOperation("adress", "setName", String.class.getName(),
                                new ExpressionBuilder().createConstantStringExpression("myAddress")));
        designProcessDefinition.addAutomaticTask("start").addUserTask("step2", "Workers").addTransition("start", "step1").addTransition("step1", "step2");
        builder.setProcessDefinition(designProcessDefinition.done());
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), "Workers", john);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt("step1", startProcess, john);
        waitForUserTask("step2", startProcess);
        disableAndDeleteProcess(processDefinition);
    }
}
