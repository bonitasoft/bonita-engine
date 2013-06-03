package org.bonitasoft.engine.test;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.ProcessRuntimeAPI;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.ComparisonOperator;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.process.Employee;
import org.bonitasoft.engine.process.Secretary;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class ProcessWithExpressionTestLocal extends CommonAPITest {

    private static final String JOHN = "john";

    @After
    public void afterTest() throws BonitaException {
        deleteUser(JOHN);
        logout();
    }

    @Before
    public void beforeTest() throws BonitaException {
        login();
        createUser(JOHN, "bpm");
        logout();
        loginWith(JOHN, "bpm");
    }

    private ProcessDefinition deployEmptyProcess() throws BonitaException {
        final DesignProcessDefinition done = new ProcessDefinitionBuilder().createNewInstance("emptyProcess", String.valueOf(System.currentTimeMillis()))
                .done();
        return deployAndEnableProcess(done);
    }

    @Cover(classes = { ExpressionBuilder.class, ProcessRuntimeAPI.class }, concept = BPMNConcept.EXPRESSIONS, keywords = { "Expression",
            "Condition expression", "Comparison expression" }, story = "Compare two objects with operator GREATER_THAN.", jira = "ENGINE-562")
    @Test
    public void evaluateObjectComparisonWithGreaterThan() throws Exception {
        final Employee emp1 = new Employee("Doe", "John", 4);
        final Employee emp2 = new Employee("Doe", "Jane", 1);
        final Expression exprOperand1 = new ExpressionBuilder().createDataExpression("emp1", Employee.class.getName());
        final Expression exprOperand2 = new ExpressionBuilder().createDataExpression("emp2", Employee.class.getName());

        final Map<String, Serializable> inputValues = new HashMap<String, Serializable>(2);
        inputValues.put("emp1", emp1);
        inputValues.put("emp2", emp2);

        final ProcessDefinition processDefinition = deployEmptyProcess();
        final Expression expression1 = new ExpressionBuilder().createComparisonExpression("GreaterThan", exprOperand1, ComparisonOperator.GREATER_THAN,
                exprOperand2);
        final Expression expression2 = new ExpressionBuilder().createComparisonExpression("GreaterThan", exprOperand2, ComparisonOperator.GREATER_THAN,
                exprOperand1);

        assertEquals(true, getProcessAPI().evaluateExpressionOnProcessDefinition(expression1, inputValues, processDefinition.getId()));
        assertEquals(false, getProcessAPI().evaluateExpressionOnProcessDefinition(expression2, inputValues, processDefinition.getId()));
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { ExpressionBuilder.class, ProcessRuntimeAPI.class }, concept = BPMNConcept.EXPRESSIONS, keywords = { "Expression",
            "Condition expression", "Comparison expression" }, story = "Compare two objects with operator EQUALS.", jira = "ENGINE-562")
    @Test
    public void evaluateObjectComparisonWithEquals() throws Exception {
        final Employee emp1 = new Employee("Doe", "John", 3);
        final Employee emp2 = new Employee("Doe", "John", 3);
        final Employee emp3 = new Employee("Doe", "John", 4);
        final Expression exprOperand1 = new ExpressionBuilder().createDataExpression("emp1", Employee.class.getName());
        final Expression exprOperand2 = new ExpressionBuilder().createDataExpression("emp2", Employee.class.getName());
        final Expression exprOperand3 = new ExpressionBuilder().createDataExpression("emp3", Employee.class.getName());

        final Map<String, Serializable> inputValues = new HashMap<String, Serializable>(3);
        inputValues.put("emp1", emp1);
        inputValues.put("emp2", emp2);
        inputValues.put("emp3", emp3);

        final ProcessDefinition processDefinition = deployEmptyProcess();
        final Expression expression1 = new ExpressionBuilder().createComparisonExpression("Equals", exprOperand1, ComparisonOperator.EQUALS, exprOperand2);
        final Expression expression2 = new ExpressionBuilder().createComparisonExpression("Equals2", exprOperand1, ComparisonOperator.EQUALS, exprOperand3);

        assertEquals(true, getProcessAPI().evaluateExpressionOnProcessDefinition(expression1, inputValues, processDefinition.getId()));
        assertEquals(false, getProcessAPI().evaluateExpressionOnProcessDefinition(expression2, inputValues, processDefinition.getId()));
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { ExpressionBuilder.class, ProcessRuntimeAPI.class }, concept = BPMNConcept.EXPRESSIONS, keywords = { "Expression",
            "Condition expression", "Comparison expression" }, story = "Compare object and parent object with operator EQUALS.", jira = "ENGINE-562")
    @Test
    public void evaluateComparisonExpressionWithObjectAndParentObject() throws Exception {
        final Employee employee = new Employee("Smith", "Ashley", 2);
        final Secretary secretary = new Secretary("Smith", "Ashley", 2);
        final Expression exprOperand1 = new ExpressionBuilder().createDataExpression("emp1", Employee.class.getName());
        final Expression exprOperand2 = new ExpressionBuilder().createDataExpression("emp2", Secretary.class.getName());

        final Map<String, Serializable> inputValues = new HashMap<String, Serializable>(2);
        inputValues.put("emp1", employee);
        inputValues.put("emp2", secretary);

        final ProcessDefinition processDefinition = deployEmptyProcess();
        final Expression expression = new ExpressionBuilder().createComparisonExpression("Equals", exprOperand1, ComparisonOperator.EQUALS, exprOperand2);

        assertEquals(true, getProcessAPI().evaluateExpressionOnProcessDefinition(expression, inputValues, processDefinition.getId()));
        disableAndDeleteProcess(processDefinition);
    }

}
