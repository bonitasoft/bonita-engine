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
package org.bonitasoft.engine.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.ProcessRuntimeAPI;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityStates;
import org.bonitasoft.engine.bpm.flownode.AutomaticTaskInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.ComparisonOperator;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionConstants;
import org.bonitasoft.engine.expression.ExpressionEvaluationException;
import org.bonitasoft.engine.expression.XPathReturnType;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class ProcessWithExpressionIT extends TestWithUser {

    @Test
    public void executeProcessWithListExpression() throws Exception {
        final Serializable result = executeProcessAndGetResultOfExpression(new ExpressionBuilder().createListExpression("list1",
                Collections.singletonList(new ExpressionBuilder().createConstantStringExpression("test"))), List.class.getName());
        assertTrue(result instanceof List);
        assertEquals(1, ((List<?>) result).size());
        assertEquals("test", ((List<?>) result).get(0));
    }

    @Test
    public void executeProcessWithListOfListOfExpression() throws Exception {
        final Serializable result = executeProcessAndGetResultOfExpression(
                new ExpressionBuilder().createListOfListExpression("myMap",
                        Collections.singletonList(Collections.singletonList(new ExpressionBuilder().createConstantStringExpression("test")))), "java.util.List");
        assertTrue(result instanceof List);
        assertEquals(1, ((List<?>) result).size());
        assertEquals("test", ((List<?>) ((List<?>) result).get(0)).get(0));
    }

    @Cover(classes = { AutomaticTaskInstance.class, ProcessRuntimeAPI.class }, concept = BPMNConcept.EXPRESSIONS, keywords = { "Expression", "Groovy", "API" }, story = "Evaluate a script that use the API", jira = "ENGINE-903")
    @Test
    public void executeProcessWithScriptUsingApi() throws Exception {
        final Serializable result = executeProcessAndGetResultOfExpression(new ExpressionBuilder().createGroovyScriptExpression("scriptAPI",
                "apiAccessor.getProcessAPI().getProcessDefinition(processDefinitionId) != null ?Boolean.TRUE:Boolean.false", Boolean.class.getName(),
                new ExpressionBuilder().createEngineConstant(ExpressionConstants.API_ACCESSOR),
                new ExpressionBuilder().createEngineConstant(ExpressionConstants.PROCESS_DEFINITION_ID)), Boolean.class.getName());

        assertEquals(Boolean.TRUE, result);
    }

    private Serializable executeProcessAndGetResultOfExpression(final Expression expression, final String dataType) throws Exception {
        return executeProcessAndGetResultOfExpression(expression, dataType, null, null, null);
    }

    private Serializable executeProcessAndGetResultOfExpression(final Expression expression, final String dataType, final String extraDataName,
            final String extraDataType, final Expression extraDataValue) throws Exception {
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("processWithExpression", "1.0");
        designProcessDefinition.addActor(ACTOR_NAME).addDescription("Delivery all day and night long");
        designProcessDefinition.addData("data1", dataType, null);
        if (extraDataName != null) {
            designProcessDefinition.addData(extraDataName, extraDataType, extraDataValue);
        }
        designProcessDefinition.addAutomaticTask("step1").addOperation(new LeftOperandBuilder().createNewInstance("data1").done(), OperatorType.ASSIGNMENT,
                "=", null, expression);
        designProcessDefinition.addUserTask("step2", ACTOR_NAME);
        designProcessDefinition.addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), ACTOR_NAME, user);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance, "step2");

        final DataInstance processDataInstance = getProcessAPI().getProcessDataInstance("data1", processInstance.getId());
        final Serializable value = processDataInstance.getValue();
        disableAndDeleteProcess(processDefinition);
        return value;
    }

    @Test
    public void evaluateGroovyScriptWithConnectorHavingDependencies() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder();
        final ProcessDefinitionBuilder pBuilder = processDefinitionBuilder.createNewInstance("emptyProcess", String.valueOf(System.currentTimeMillis()));
        pBuilder.addActor(ACTOR_NAME)
                .addUserTask("step1", ACTOR_NAME)
                .addDisplayName(
                        new ExpressionBuilder().createGroovyScriptExpression("myScript",
                                "new org.bonitasoft.engine.test.TheClassOfMyLibrary().aPublicMethod()", String.class.getName()));
        final DesignProcessDefinition done = pBuilder.done();
        final BusinessArchiveBuilder builder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(done);
        final InputStream stream = CommonAPIIT.class.getResourceAsStream("/mylibrary-jar.bak");
        assertNotNull(stream);
        final byte[] byteArray = IOUtils.toByteArray(stream);
        stream.close();
        builder.addClasspathResource(new BarResource("mylibrary.jar", byteArray));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);

        try {
            final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
            final ActivityInstance task = waitForUserTaskAndGetIt(processInstance, "step1");
            assertEquals("stringFromPublicMethod", task.getDisplayName());
        } finally {
            disableAndDeleteProcess(processDefinition);
        }
    }

    @Test
    public void evaluateGroovyScriptWithDifferentClassloader() throws Exception {
        final BarResource myLibrary1 = getMyLibrary();
        final BarResource myLibrary2 = getMyLibrary2();
        final String scriptLib1 = "new org.bonitasoft.engine.test.TheClassOfMyLibrary().aPublicMethod()";
        final String scriptLib2 = "new org.bonitasoft.engine.test2.TheClassOfMyLibrary2().aPublicMethod2()";
        final ProcessDefinition p11 = deployProcessWithScriptAndLibrary(myLibrary1, scriptLib1);
        final ProcessDefinition p22 = deployProcessWithScriptAndLibrary(myLibrary2, scriptLib2);
        final ProcessDefinition p12 = deployProcessWithScriptAndLibrary(myLibrary1, scriptLib2);
        try {
            final ProcessInstance pi11 = getProcessAPI().startProcess(p11.getId());
            final ProcessInstance pi22 = getProcessAPI().startProcess(p22.getId());
            final ProcessInstance pi12 = getProcessAPI().startProcess(p12.getId());
            final ActivityInstance task1 = waitForUserTaskAndGetIt(pi11, "step1");
            assertEquals("stringFromPublicMethod", task1.getDisplayName());
            final ActivityInstance task2 = waitForUserTaskAndGetIt(pi22, "step1");
            assertEquals("stringFromPublicMethod2", task2.getDisplayName());
            final ActivityInstance task3 = waitForTaskToFail(pi12);
            assertEquals("step1", task3.getName());
            final ProcessInstance pi11bis = getProcessAPI().startProcess(p11.getId());
            final ActivityInstance task1bis = waitForUserTaskAndGetIt(pi11bis, "step1");
            assertEquals("stringFromPublicMethod", task1bis.getDisplayName());
        } finally {
            disableAndDeleteProcess(p11);
            disableAndDeleteProcess(p22);
            disableAndDeleteProcess(p12);
        }
    }

    private ProcessDefinition deployProcessWithScriptAndLibrary(final BarResource myLibrary, final String script) throws BonitaException {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder();
        final ProcessDefinitionBuilder pBuilder = processDefinitionBuilder.createNewInstance("emptyProcess", String.valueOf(System.currentTimeMillis()));
        pBuilder.addActor(ACTOR_NAME).addUserTask("step1", ACTOR_NAME)
                .addDisplayName(new ExpressionBuilder().createGroovyScriptExpression("myScript", script, String.class.getName()));
        final DesignProcessDefinition done = pBuilder.done();
        final BusinessArchiveBuilder builder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(done);
        builder.addClasspathResource(myLibrary);
        return deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);
    }

    private BarResource getMyLibrary() throws IOException {
        final InputStream stream = CommonAPIIT.class.getResourceAsStream("/mylibrary-jar.bak");
        final byte[] byteArray = IOUtils.toByteArray(stream);
        stream.close();
        return new BarResource("mylibrary.jar", byteArray);
    }

    private BarResource getMyLibrary2() throws IOException {
        final InputStream stream = CommonAPIIT.class.getResourceAsStream("/mylibrary2-jar.bak");
        final byte[] byteArray = IOUtils.toByteArray(stream);
        stream.close();
        return new BarResource("mylibrary.jar", byteArray);
    }

    @Test
    public void evaluateConstantExpressionFromApi() throws Exception {
        final ProcessDefinition processDefinition = deployEmptyProcess();
        Expression expression = new ExpressionBuilder().createConstantBooleanExpression(true);
        final Map<String, Serializable> inputValues = new HashMap<String, Serializable>(0);
        assertEquals(true, getProcessAPI().evaluateExpressionOnProcessDefinition(expression, inputValues, processDefinition.getId()));

        expression = new ExpressionBuilder().createConstantStringExpression("test");
        assertEquals("test", getProcessAPI().evaluateExpressionOnProcessDefinition(expression, inputValues, processDefinition.getId()));

        expression = new ExpressionBuilder().createConstantLongExpression(123456L);
        assertEquals(123456L, getProcessAPI().evaluateExpressionOnProcessDefinition(expression, inputValues, processDefinition.getId()));
        disableAndDeleteProcess(processDefinition);
    }

    private ProcessDefinition deployEmptyProcess() throws BonitaException {
        final DesignProcessDefinition done = new ProcessDefinitionBuilder().createNewInstance("emptyProcess", String.valueOf(System.currentTimeMillis()))
                .done();
        return deployAndEnableProcess(done);
    }

    @Test
    public void evaluateInputExpressionFromApi() throws Exception {
        final ProcessDefinition processDefinition = deployEmptyProcess();
        final Expression expression = new ExpressionBuilder().createInputExpression("test", Boolean.class.getName());
        final Map<String, Serializable> inputValues = new HashMap<String, Serializable>();
        inputValues.put("test", true);
        assertEquals(true, getProcessAPI().evaluateExpressionOnProcessDefinition(expression, inputValues, processDefinition.getId()));
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void evaluateGroovyExpressionFromApi() throws Exception {
        final ProcessDefinition processDefinition = deployEmptyProcess();
        final Expression expression = new ExpressionBuilder().createGroovyScriptExpression("evaluateGroovyExpressionFromApi", "input1 + 12",
                Integer.class.getName(), new ExpressionBuilder().createInputExpression("input1", Integer.class.getName()));
        final Map<String, Serializable> inputValues = new HashMap<String, Serializable>(1);
        inputValues.put("input1", 8);
        assertEquals(20, getProcessAPI().evaluateExpressionOnProcessDefinition(expression, inputValues, processDefinition.getId()));
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void evaluateGroovyWithDataProvidedExpressionFromApi() throws Exception {
        final ProcessDefinition processDefinition = deployEmptyProcess();
        final Expression expression = new ExpressionBuilder().createGroovyScriptExpression("evaluateGroovyWithDataProvidedExpressionFromApi",
                "input1 + data1 + 12", Integer.class.getName(), new ExpressionBuilder().createInputExpression("input1", Integer.class.getName()),
                new ExpressionBuilder().createDataExpression("data1", Integer.class.getName()));
        final Map<String, Serializable> inputValues = new HashMap<String, Serializable>(2);
        inputValues.put("input1", 6);
        inputValues.put("data1", 2);
        assertEquals(20, getProcessAPI().evaluateExpressionOnProcessDefinition(expression, inputValues, processDefinition.getId()));
        disableAndDeleteProcess(processDefinition);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void evaluateEmbeddedListExpressions() throws Exception {
        final ProcessDefinition processDefinition = deployEmptyProcess();
        final Expression constantExpr = new ExpressionBuilder().createConstantStringExpression("DUMMY");
        final String data1Content = "data1_name";
        final Expression data1Expr = new ExpressionBuilder().createDataExpression(data1Content, String.class.getName());
        final Expression listExpression1 = new ExpressionBuilder().createListExpression("manuList1", Arrays.asList(constantExpr, data1Expr));
        final Expression felixConstExp = new ExpressionBuilder().createConstantStringExpression("FELIX");
        final Expression listExpression2 = new ExpressionBuilder().createListExpression("manuList2", Arrays.asList(listExpression1, felixConstExp));

        final Map<String, Serializable> inputValues = new HashMap<String, Serializable>(1);
        inputValues.put(data1Content, "dataValue");
        final List<Serializable> result = (List<Serializable>) getProcessAPI().evaluateExpressionOnProcessDefinition(listExpression2, inputValues,
                processDefinition.getId());
        assertEquals(2, result.size());
        assertEquals(2, ((List<Serializable>) result.get(0)).size());
        assertEquals("DUMMY", ((List<Serializable>) result.get(0)).get(0));
        assertEquals("dataValue", ((List<Serializable>) result.get(0)).get(1));
        assertEquals("FELIX", result.get(1));
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void evaluateListExpression() throws Exception {
        final ProcessDefinition processDefinition = deployEmptyProcess();
        final Expression groovyExpr = new ExpressionBuilder()
                .createGroovyScriptExpression("evaluateListExpression", "1+'_'+data1_name", String.class.getName());
        final Expression constantExpr = new ExpressionBuilder().createConstantStringExpression("Newbee");
        final String data1Content = "data1_name";
        final Expression data1Expr = new ExpressionBuilder().createDataExpression(data1Content, String.class.getName());

        final Expression listExpression = new ExpressionBuilder().createListExpression("ManuList1", Arrays.asList(groovyExpr, constantExpr, data1Expr));

        final Map<String, Serializable> inputValues = new HashMap<String, Serializable>(1);
        inputValues.put(data1Content, "EXPRESSION");
        @SuppressWarnings("unchecked")
        final List<Serializable> result = (List<Serializable>) getProcessAPI().evaluateExpressionOnProcessDefinition(listExpression, inputValues,
                processDefinition.getId());
        assertEquals(3, result.size());
        assertEquals("1_EXPRESSION", result.get(0));
        assertEquals("Newbee", result.get(1));
        assertEquals("EXPRESSION", result.get(2));
        disableAndDeleteProcess(processDefinition);
    }

    @Test(expected = ExpressionEvaluationException.class)
    public void evaluateDataExpressionFromApiOnUnknownData() throws Exception {
        final ProcessDefinition processDefinition = deployEmptyProcess();
        final Expression expression = new ExpressionBuilder().createDataExpression("data", Boolean.class.getName());
        final Map<String, Serializable> inputValues = new HashMap<String, Serializable>(0);
        try {
            getProcessAPI().evaluateExpressionOnProcessDefinition(expression, inputValues, processDefinition.getId());
        } finally {
            disableAndDeleteProcess(processDefinition);
        }
    }

    @Cover(classes = { ExpressionBuilder.class, ProcessRuntimeAPI.class }, concept = BPMNConcept.OTHERS, keywords = { "Expression", "Condition Expression",
            "Compararison expression" }, jira = "ENGINE-380")
    @Test
    public void evaluateGreaterThanComparationExpression() throws Exception {
        final Expression dependExpr1 = new ExpressionBuilder().createConstantDoubleExpression(5.1);
        final Expression dependExpr2 = new ExpressionBuilder().createConstantIntegerExpression(6);
        final Expression expression1 = new ExpressionBuilder().createComparisonExpression("comp1", dependExpr1, ComparisonOperator.GREATER_THAN, dependExpr2);
        Serializable result = executeProcessAndGetResultOfExpression(expression1, Boolean.class.getName());
        assertTrue(result instanceof Boolean);
        assertFalse((Boolean) result);

        final Expression expression2 = new ExpressionBuilder().createComparisonExpression("comp1", dependExpr2, ComparisonOperator.GREATER_THAN, dependExpr1);
        result = executeProcessAndGetResultOfExpression(expression2, Boolean.class.getName());
        assertTrue(result instanceof Boolean);
        assertTrue((Boolean) result);

        final Expression expression3 = new ExpressionBuilder().createComparisonExpression("comp2", dependExpr2, ComparisonOperator.GREATER_THAN, dependExpr2);
        result = executeProcessAndGetResultOfExpression(expression3, Boolean.class.getName());
        assertTrue(result instanceof Boolean);
        assertFalse((Boolean) result);
    }

    @Cover(classes = { ExpressionBuilder.class, ProcessRuntimeAPI.class }, concept = BPMNConcept.OTHERS, keywords = { "Expression", "Condition Expression",
            "Logical complement expression" }, jira = "ENGINE-380")
    @Test
    public void evaluateLogicalComplementExpression() throws Exception {
        final Expression dependExpr1 = new ExpressionBuilder().createConstantBooleanExpression(true);
        final Expression dependExpr2 = new ExpressionBuilder().createGroovyScriptExpression("toReturnFalse", "4 == 5", Boolean.class.getName());

        final Expression expression1 = new ExpressionBuilder().createLogicalComplementExpression("complement1", dependExpr1);
        Serializable result = executeProcessAndGetResultOfExpression(expression1, Boolean.class.getName());
        assertTrue(result instanceof Boolean);
        assertFalse((Boolean) result);

        final Expression expression2 = new ExpressionBuilder().createLogicalComplementExpression("complement1", dependExpr2);
        result = executeProcessAndGetResultOfExpression(expression2, Boolean.class.getName());
        assertTrue(result instanceof Boolean);
        assertTrue((Boolean) result);
    }

    @Cover(classes = { ExpressionBuilder.class, ProcessRuntimeAPI.class }, concept = BPMNConcept.EXPRESSIONS, keywords = { "Expression",
            "Condition expression", "Comparison expression" }, story = "Test operator GREATER_THAN_OR_EQUALS", jira = "ENGINE-562")
    @Test
    public void evaluateGreaterThanOrEqualsComparisonExpression() throws Exception {
        final Expression exprOperand1 = new ExpressionBuilder().createConstantIntegerExpression(7);
        final Expression exprOperand2 = new ExpressionBuilder().createConstantIntegerExpression(9);
        final Expression exprOperand3 = new ExpressionBuilder().createConstantIntegerExpression(7);
        final Expression exprOperand4 = new ExpressionBuilder().createConstantIntegerExpression(-8);

        final Expression exprResult1 = new ExpressionBuilder().createComparisonExpression("GreaterThanOrEquals1", exprOperand1,
                ComparisonOperator.GREATER_THAN_OR_EQUALS, exprOperand2);
        Serializable result = executeProcessAndGetResultOfExpression(exprResult1, Boolean.class.getName());
        assertTrue(result instanceof Boolean);
        assertFalse((Boolean) result);

        final Expression exprResult2 = new ExpressionBuilder().createComparisonExpression("GreaterThanOrEquals2", exprOperand1,
                ComparisonOperator.GREATER_THAN_OR_EQUALS, exprOperand3);
        result = executeProcessAndGetResultOfExpression(exprResult2, Boolean.class.getName());
        assertTrue(result instanceof Boolean);
        assertTrue((Boolean) result);

        final Expression exprResult3 = new ExpressionBuilder().createComparisonExpression("GreaterThanOrEquals3", exprOperand3,
                ComparisonOperator.GREATER_THAN_OR_EQUALS, exprOperand4);
        result = executeProcessAndGetResultOfExpression(exprResult3, Boolean.class.getName());
        assertTrue(result instanceof Boolean);
        assertTrue((Boolean) result);
    }

    @Cover(classes = { ExpressionBuilder.class, ProcessRuntimeAPI.class }, concept = BPMNConcept.EXPRESSIONS, keywords = { "Expression",
            "Condition expression", "Comparison expression" }, story = "Test operator LESS_THAN", jira = "ENGINE-562")
    @Test
    public void evaluateLessThanComparisonExpression() throws Exception {
        final Expression exprOperand1 = new ExpressionBuilder().createConstantIntegerExpression(7);
        final Expression exprOperand2 = new ExpressionBuilder().createConstantIntegerExpression(9);
        final Expression exprOperand3 = new ExpressionBuilder().createConstantIntegerExpression(7);
        final Expression exprOperand4 = new ExpressionBuilder().createConstantIntegerExpression(-8);

        final Expression exprResult1 = new ExpressionBuilder()
                .createComparisonExpression("LessThan1", exprOperand1, ComparisonOperator.LESS_THAN, exprOperand2);
        Serializable result = executeProcessAndGetResultOfExpression(exprResult1, Boolean.class.getName());
        assertTrue(result instanceof Boolean);
        assertTrue((Boolean) result);

        final Expression exprResult2 = new ExpressionBuilder()
                .createComparisonExpression("LessThan2", exprOperand1, ComparisonOperator.LESS_THAN, exprOperand3);
        result = executeProcessAndGetResultOfExpression(exprResult2, Boolean.class.getName());
        assertTrue(result instanceof Boolean);
        assertFalse((Boolean) result);

        final Expression exprResult3 = new ExpressionBuilder()
                .createComparisonExpression("LessThan3", exprOperand3, ComparisonOperator.LESS_THAN, exprOperand4);
        result = executeProcessAndGetResultOfExpression(exprResult3, Boolean.class.getName());
        assertTrue(result instanceof Boolean);
        assertFalse((Boolean) result);
    }

    @Cover(classes = { ExpressionBuilder.class, ProcessRuntimeAPI.class }, concept = BPMNConcept.EXPRESSIONS, keywords = { "Expression",
            "Condition expression", "Comparison expression" }, story = "Test operator LESS_THAN_OR_EQUALS", jira = "ENGINE-562")
    @Test
    public void evaluateLessThanOrEqualsComparisonExpression() throws Exception {
        final Expression exprOperand1 = new ExpressionBuilder().createConstantIntegerExpression(7);
        final Expression exprOperand2 = new ExpressionBuilder().createConstantIntegerExpression(9);
        final Expression exprOperand3 = new ExpressionBuilder().createConstantIntegerExpression(7);
        final Expression exprOperand4 = new ExpressionBuilder().createConstantIntegerExpression(-8);

        final Expression exprResult1 = new ExpressionBuilder().createComparisonExpression("LessThanOrEquals1", exprOperand1,
                ComparisonOperator.LESS_THAN_OR_EQUALS, exprOperand2);
        Serializable result = executeProcessAndGetResultOfExpression(exprResult1, Boolean.class.getName());
        assertTrue(result instanceof Boolean);
        assertTrue((Boolean) result);

        final Expression exprResult2 = new ExpressionBuilder().createComparisonExpression("LessThanOrEquals2", exprOperand1,
                ComparisonOperator.LESS_THAN_OR_EQUALS, exprOperand3);
        result = executeProcessAndGetResultOfExpression(exprResult2, Boolean.class.getName());
        assertTrue(result instanceof Boolean);
        assertTrue((Boolean) result);

        final Expression exprResult3 = new ExpressionBuilder().createComparisonExpression("LessThanOrEquals3", exprOperand3,
                ComparisonOperator.LESS_THAN_OR_EQUALS, exprOperand4);
        result = executeProcessAndGetResultOfExpression(exprResult3, Boolean.class.getName());
        assertTrue(result instanceof Boolean);
        assertFalse((Boolean) result);
    }

    @Cover(classes = { ExpressionBuilder.class, ProcessRuntimeAPI.class }, concept = BPMNConcept.EXPRESSIONS, keywords = { "Expression",
            "Condition expression", "Comparison expression" }, story = "Execute comparison with boolean values.", jira = "ENGINE-562")
    @Test
    public void evaluateComparisonExpressionWithBoolean() throws Exception {
        final Expression exprOperand1 = new ExpressionBuilder().createConstantBooleanExpression(false);
        final Expression exprOperand2 = new ExpressionBuilder().createConstantBooleanExpression(true);

        final Expression exprResult1 = new ExpressionBuilder().createComparisonExpression("Equals", exprOperand1, ComparisonOperator.EQUALS, exprOperand2);
        Serializable result = executeProcessAndGetResultOfExpression(exprResult1, Boolean.class.getName());
        assertTrue(result instanceof Boolean);
        assertFalse((Boolean) result);

        final Expression exprResult2 = new ExpressionBuilder().createComparisonExpression("GreaterThan", exprOperand2, ComparisonOperator.GREATER_THAN,
                exprOperand1);
        result = executeProcessAndGetResultOfExpression(exprResult2, Boolean.class.getName());
        assertTrue(result instanceof Boolean);
        assertTrue((Boolean) result);
    }

    @Cover(classes = { ExpressionBuilder.class, ProcessRuntimeAPI.class }, concept = BPMNConcept.EXPRESSIONS, keywords = { "Expression",
            "Condition expression", "Comparison expression" }, story = "Execute comparison with double values.", jira = "ENGINE-562")
    @Test
    public void evaluateComparisonExpressionWithDouble() throws Exception {
        final Expression exprOperand1 = new ExpressionBuilder().createConstantDoubleExpression(1.0);
        final Expression exprOperand2 = new ExpressionBuilder().createConstantDoubleExpression(2.0);
        final Expression exprOperand3 = new ExpressionBuilder().createConstantDoubleExpression(1.0);

        final Expression exprResult1 = new ExpressionBuilder().createComparisonExpression("Equals", exprOperand1, ComparisonOperator.EQUALS, exprOperand3);
        Serializable result = executeProcessAndGetResultOfExpression(exprResult1, Boolean.class.getName());
        assertTrue(result instanceof Boolean);
        assertTrue((Boolean) result);

        final Expression exprResult2 = new ExpressionBuilder().createComparisonExpression("GreaterThan", exprOperand1, ComparisonOperator.GREATER_THAN,
                exprOperand2);
        result = executeProcessAndGetResultOfExpression(exprResult2, Boolean.class.getName());
        assertTrue(result instanceof Boolean);
        assertFalse((Boolean) result);
    }

    @Cover(classes = { ExpressionBuilder.class, ProcessRuntimeAPI.class }, concept = BPMNConcept.EXPRESSIONS, keywords = { "Expression",
            "Condition expression", "Comparison expression" }, story = "Execute comparison with long values.", jira = "ENGINE-562")
    @Test
    public void evaluateComparisonExpressionWithLong() throws Exception {
        final Expression exprOperand1 = new ExpressionBuilder().createConstantLongExpression(145);
        final Expression exprOperand2 = new ExpressionBuilder().createConstantLongExpression(353);
        final Expression exprOperand3 = new ExpressionBuilder().createConstantLongExpression(145);

        final Expression exprResult1 = new ExpressionBuilder().createComparisonExpression("Equals", exprOperand1, ComparisonOperator.EQUALS, exprOperand3);
        Serializable result = executeProcessAndGetResultOfExpression(exprResult1, Boolean.class.getName());
        assertTrue(result instanceof Boolean);
        assertTrue((Boolean) result);

        final Expression exprResult2 = new ExpressionBuilder().createComparisonExpression("GreaterThan", exprOperand1, ComparisonOperator.GREATER_THAN,
                exprOperand2);
        result = executeProcessAndGetResultOfExpression(exprResult2, Boolean.class.getName());
        assertTrue(result instanceof Boolean);
        assertFalse((Boolean) result);
    }

    @Cover(classes = { ExpressionBuilder.class, ProcessRuntimeAPI.class }, concept = BPMNConcept.EXPRESSIONS, keywords = { "Expression",
            "Condition expression", "Comparison expression" }, story = "Execute comparison with lowercase string values.", jira = "ENGINE-562")
    @Test
    public void evaluateComparisonExpressionWithLowerCaseString() throws Exception {
        final Expression exprOperand1 = new ExpressionBuilder().createConstantStringExpression("john");
        final Expression exprOperand2 = new ExpressionBuilder().createConstantStringExpression("ashley");
        final Expression exprOperand3 = new ExpressionBuilder().createConstantStringExpression("john");

        final Expression exprResult1 = new ExpressionBuilder().createComparisonExpression("Equals", exprOperand1, ComparisonOperator.EQUALS, exprOperand3);
        Serializable result = executeProcessAndGetResultOfExpression(exprResult1, Boolean.class.getName());
        assertTrue(result instanceof Boolean);
        assertTrue((Boolean) result);

        final Expression exprResult2 = new ExpressionBuilder().createComparisonExpression("GreaterThan", exprOperand2, ComparisonOperator.GREATER_THAN,
                exprOperand1);
        result = executeProcessAndGetResultOfExpression(exprResult2, Boolean.class.getName());
        assertTrue(result instanceof Boolean);
        assertFalse((Boolean) result);
    }

    @Cover(classes = { ExpressionBuilder.class, ProcessRuntimeAPI.class }, concept = BPMNConcept.EXPRESSIONS, keywords = { "Expression",
            "Condition expression", "Comparison expression" }, story = "Execute comparison with lowercase and uppercase string values.", jira = "ENGINE-562")
    @Test
    public void evaluateComparisonExpressionWithLowerCaseAndUpperCaseString() throws Exception {
        final Expression exprOperand1 = new ExpressionBuilder().createConstantStringExpression("john");
        final Expression exprOperand2 = new ExpressionBuilder().createConstantStringExpression("ashley");
        final Expression exprOperand3 = new ExpressionBuilder().createConstantStringExpression("JOHN");
        final Expression exprOperand4 = new ExpressionBuilder().createConstantStringExpression("ASHLEY");

        final Expression exprResult1 = new ExpressionBuilder().createComparisonExpression("Equals", exprOperand1, ComparisonOperator.EQUALS, exprOperand3);
        Serializable result = executeProcessAndGetResultOfExpression(exprResult1, Boolean.class.getName());
        assertTrue(result instanceof Boolean);
        assertFalse((Boolean) result);

        final Expression exprResult2 = new ExpressionBuilder().createComparisonExpression("GreaterThan", exprOperand2, ComparisonOperator.GREATER_THAN,
                exprOperand4);
        result = executeProcessAndGetResultOfExpression(exprResult2, Boolean.class.getName());
        assertTrue(result instanceof Boolean);
        assertTrue((Boolean) result);

        final Expression exprResult3 = new ExpressionBuilder().createComparisonExpression("Equals", exprOperand3, ComparisonOperator.EQUALS, exprOperand1);
        result = executeProcessAndGetResultOfExpression(exprResult3, Boolean.class.getName());
        assertTrue(result instanceof Boolean);
        assertFalse((Boolean) result);

        final Expression exprResult4 = new ExpressionBuilder().createComparisonExpression("GreaterThan", exprOperand4, ComparisonOperator.GREATER_THAN,
                exprOperand1);
        result = executeProcessAndGetResultOfExpression(exprResult4, Boolean.class.getName());
        assertTrue(result instanceof Boolean);
        assertFalse((Boolean) result);
    }

    @Cover(classes = { AutomaticTaskInstance.class, ProcessRuntimeAPI.class }, concept = BPMNConcept.EXPRESSIONS, keywords = { "Expression", "Groovy", "Fail" }, story = "Eecute task that fail because of the groovy script.", jira = "ENGINE-672")
    @Test
    public void runProcessWithScriptThatThrowExceptionFailTheTask() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("processWithGroovy", "1.0");
        processBuilder.addAutomaticTask("activityThatFail").addData("data1", String.class.getName(),
                new ExpressionBuilder().createGroovyScriptExpression("script", "throw new Exception()", String.class.getName()));
        processBuilder.addUserTask("aTask", ACTOR_NAME);
        processBuilder.addActor(ACTOR_NAME);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance, "aTask");
        waitForTaskToFail(processInstance);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { ProcessRuntimeAPI.class }, concept = BPMNConcept.EXPRESSIONS, keywords = { "Expression", "XPathReadExpressionExecutorStrategy" }, jira = "ENGINE-1044")
    @Test
    public void processWithXPathExpression() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("XPathExpression", "1.0");
        processBuilder.addData("data", String.class.getName(), new ExpressionBuilder().createXPathExpression("xpath", "/root/element/@name",
                XPathReturnType.STRING, "<root><element name='Alexander Corvinus' /></root>"));
        processBuilder.addUserTask("aDummyTask", ACTOR_NAME);
        processBuilder.addActor(ACTOR_NAME);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance, "aDummyTask");

        assertEquals("Alexander Corvinus", getProcessAPI().getProcessDataInstance("data", processInstance.getId()).getValue());
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { ProcessAPI.class, AutomaticTaskInstance.class, ActivityStates.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "UserTask",
            "Transition", "State", "Failed" }, jira = "ENGINE-796")
    @Test
    public void executeProcessWithAutomaticTasksAndTransitionFailed() throws Exception {
        // Build condition
        final Expression condition = new ExpressionBuilder().createGroovyScriptExpression("evaluateGroovyExpressionFromApi", "throw new Exception()",
                Boolean.class.getName());

        // Build process
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(
                "executeConnectorOnStartOfAnAutomaticActivity", "1.0");
        designProcessDefinition.addActor(ACTOR_NAME).addDescription("Delivery all day and night long");
        designProcessDefinition.addAutomaticTask("step1");
        designProcessDefinition.addAutomaticTask("step2");
        designProcessDefinition.addAutomaticTask("default");
        designProcessDefinition.addTransition("step1", "step2", condition);
        designProcessDefinition.addDefaultTransition("step1", "default");

        // Start process
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), ACTOR_NAME, user);
        getProcessAPI().startProcess(processDefinition.getId());

        // Test if step1 state is FAILED
        waitForFlowNodeInFailedState("step1");

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { ExpressionBuilder.class, ProcessRuntimeAPI.class }, concept = BPMNConcept.OTHERS, keywords = { "Expression", "Java method call", }, jira = "ENGINE-1128")
    @Test
    public void evaluateJavaMethodCallExpression() throws Exception {
        final String extraDataName = "myValues";
        final String extraDataType = List.class.getName();
        final Expression extraDataValue = new ExpressionBuilder().createGroovyScriptExpression("defaultList", "return Arrays.asList(1, 2 , 3);", extraDataType);
        final Expression dep = new ExpressionBuilder().createDataExpression(extraDataName, extraDataType);

        final Expression expression = new ExpressionBuilder().createJavaMethodCallExpression("getter", "toString", String.class.getName(), dep);
        final Serializable result = executeProcessAndGetResultOfExpression(expression, String.class.getName(), extraDataName, extraDataType, extraDataValue);
        assertEquals("[1, 2, 3]", result);

    }

    @Cover(classes = { Expression.class }, concept = BPMNConcept.EXPRESSIONS, keywords = { "custom type", "java expression", "classloader" }, jira = "ENGINE-1249", story = "can't call 2 times a same java operation after a redeploy")
    @Test
    public void evaluateJavaMethodCallExpressionTwice() throws Exception {
        // the cache on ClassReflector made impossible to call the same expression on the same class if it was loaded by 2 different process
        final BusinessArchiveBuilder builder = new BusinessArchiveBuilder().createNewBusinessArchive();
        builder.addClasspathResource(getResource("/custom-0.1.jar.bak", "custom-0.1.jar"));
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("ProcessWithCustomData", "1.0");
        designProcessDefinition.addData("adress", "org.bonitasoft.custom.Address", new ExpressionBuilder().createGroovyScriptExpression("create adress",
                "new org.bonitasoft.custom.Address(\"name1\",\"Rue ampère\",\"38000\",\"Grenoble\",\"France\")", "org.bonitasoft.custom.Address"));
        designProcessDefinition
                .addActor(ACTOR_NAME)
                .addUserTask("step1", ACTOR_NAME)
                .addDisplayName(
                        new ExpressionBuilder().createJavaMethodCallExpression("getNameOfAdress", "getName", String.class.getName(),
                                new ExpressionBuilder().createDataExpression("adress", "org.bonitasoft.custom.Address")));
        designProcessDefinition.addAutomaticTask("start").addTransition("start", "step1");
        builder.setProcessDefinition(designProcessDefinition.done());
        final ProcessDefinition processDefinition1 = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(processDefinition1.getId());
        final ActivityInstance userTask = waitForUserTaskAndGetIt(processInstance1, "step1");
        assertEquals("name1", userTask.getDisplayName());

        // do the same thing a second time
        final BusinessArchiveBuilder builder2 = new BusinessArchiveBuilder().createNewBusinessArchive();
        builder2.addClasspathResource(getResource("/custom-0.1.jar.bak", "custom-0.1.jar"));
        final ProcessDefinitionBuilder designProcessDefinition2 = new ProcessDefinitionBuilder().createNewInstance("ProcessWithCustomData", "1.1");
        designProcessDefinition2.addData("adress", "org.bonitasoft.custom.Address", new ExpressionBuilder().createGroovyScriptExpression("create adress",
                "new org.bonitasoft.custom.Address(\"name1\",\"Rue ampère\",\"38000\",\"Grenoble\",\"France\")", "org.bonitasoft.custom.Address"));
        designProcessDefinition2
                .addActor(ACTOR_NAME)
                .addUserTask("step1", ACTOR_NAME)
                .addDisplayName(
                        new ExpressionBuilder().createJavaMethodCallExpression("getNameOfAdress", "getName", String.class.getName(),
                                new ExpressionBuilder().createDataExpression("adress", "org.bonitasoft.custom.Address")));
        designProcessDefinition2.addAutomaticTask("start").addTransition("start", "step1");
        builder2.setProcessDefinition(designProcessDefinition2.done());
        final ProcessDefinition processDefinition2 = deployAndEnableProcessWithActor(builder2.done(), ACTOR_NAME, user);
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(processDefinition2.getId());
        final ActivityInstance userTask2 = waitForUserTaskAndGetIt(processInstance2, "step1");
        assertEquals("name1", userTask2.getDisplayName());

        final ProcessInstance processInstance3 = getProcessAPI().startProcess(processDefinition1.getId());
        final ActivityInstance userTask3 = waitForUserTaskAndGetIt(processInstance3, "step1");
        assertEquals("name1", userTask3.getDisplayName());

        disableAndDeleteProcess(processDefinition1);
        disableAndDeleteProcess(processDefinition2);
    }

}
