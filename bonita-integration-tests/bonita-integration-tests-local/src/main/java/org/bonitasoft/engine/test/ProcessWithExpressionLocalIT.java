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
package org.bonitasoft.engine.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.LocalServerTestsInitializer;
import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.api.ProcessRuntimeAPI;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.expression.ComparisonOperator;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionEvaluationException;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.operation.LeftOperand;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.process.Employee;
import org.bonitasoft.engine.process.Secretary;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner;
import org.bonitasoft.engine.test.runner.BonitaTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Baptiste Mesta
 */
@RunWith(BonitaTestRunner.class)
@BonitaSuiteRunner.Initializer(LocalServerTestsInitializer.class)
public class ProcessWithExpressionLocalIT extends TestWithUser {

    private ProcessDefinition processDefinition;

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

    @Cover(classes = { DataInstance.class }, concept = BPMNConcept.OPERATION, keywords = { "Expression", "Transient data" }, story = "Compare two objects with operator GREATER_THAN.", jira = "BS-1379")
    @Test
    public void should_operation_with_transient_data_reevaluate_the_definition_if_lost() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("processWithTransientData",
                String.valueOf(System.currentTimeMillis()));
        builder.addActor("actor");
        builder.addUserTask("step1", "actor")
                .addData("tData", String.class.getName(), new ExpressionBuilder().createConstantStringExpression("The default value")).isTransient();
        processDefinition = deployAndEnableProcessWithActor(builder.done(), "actor", user);
        getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance step1 = waitForUserTaskAndGetIt("step1");

        // evaluate the expression of the transient data, it should return the default value
        assertThat(evaluateTransientDataWithExpression(step1).get("tData")).isEqualTo("The default value");
        // update it using operation
        updateDataWithOperation(step1, "The updated value", "tData");
        // evaluate it: it should return the updated value
        assertThat(evaluateTransientDataWithExpression(step1).get("tData")).isEqualTo("The updated value");
        // clear the cache
        getTenantAccessor().getCacheService().clear("transient_data");
        // evaluate it: it should return the default value
        assertThat(evaluateTransientDataWithExpression(step1).get("tData")).isEqualTo("The default value");

        disableAndDeleteProcess(processDefinition);
    }

    protected TenantServiceAccessor getTenantAccessor() {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    private void updateDataWithOperation(final HumanTaskInstance step1, final String value, final String name) throws InvalidExpressionException,
            UpdateException {
        final Operation operation = new OperationBuilder().createNewInstance().setLeftOperand(name, LeftOperand.TYPE_TRANSIENT_DATA)
                .setRightOperand(new ExpressionBuilder().createConstantStringExpression(value)).setType(OperatorType.ASSIGNMENT).done();
        getProcessAPI().updateActivityInstanceVariables(Arrays.asList(operation), step1.getId(), null);
    }

    private Map<String, Serializable> evaluateTransientDataWithExpression(final HumanTaskInstance step1) throws ExpressionEvaluationException,
            InvalidExpressionException {
        final Map<Expression, Map<String, Serializable>> expressionMap = new HashMap<Expression, Map<String, Serializable>>();
        expressionMap
                .put(new ExpressionBuilder().createTransientDataExpression("tData", String.class.getName()), Collections.<String, Serializable> emptyMap());
        return getProcessAPI().evaluateExpressionsOnActivityInstance(step1.getId(), expressionMap);
    }

}
