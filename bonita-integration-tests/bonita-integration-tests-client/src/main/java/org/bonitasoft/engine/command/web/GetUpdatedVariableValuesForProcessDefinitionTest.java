/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.command.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.command.CommandExecutionException;
import org.bonitasoft.engine.command.CommandParameterizationException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.operation.LeftOperand;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.bonitasoft.engine.test.wait.WaitForStep;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Yanyan Liu
 * @author Emmanuel Duchastenier
 */
public class GetUpdatedVariableValuesForProcessDefinitionTest extends CommonAPITest {

    @Before
    public void before() throws Exception {
         loginOnDefaultTenantWithDefaultTechnicalUser();
    }

    @After
    public void after() throws BonitaException, BonitaHomeNotSetException {
        logoutOnTenant();
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.PROCESS, keywords = { "Command", "Updated variable value", "Process definition" }, story = "Get updated variable values for process definition.", jira = "")
    @Test(expected = CommandExecutionException.class)
    public void testGetUpdatedVariableValuesForProcessDefinition() throws Exception {
        final User user = createUser("toto", "titi");
        // create process definition:
        final String dataName1 = "data1";
        final String dataName2 = "data2";
        final int dataValue = 1;
        final String actorName = "actor";
        // Process Def level default data values are: data1=1, data2=1
        final UserTaskDefinitionBuilder taskDefBuilder = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0").addActor(actorName)
                .addIntegerData(dataName1, new ExpressionBuilder().createConstantIntegerExpression(dataValue))
                .addIntegerData(dataName2, new ExpressionBuilder().createConstantIntegerExpression(dataValue)).addUserTask("step1", actorName);
        // a data in step1 with same name 'data1' is also defined with value 11:
        taskDefBuilder.addIntegerData(dataName1, new ExpressionBuilder().createConstantIntegerExpression(11));
        final DesignProcessDefinition processDef = taskDefBuilder.addUserTask("step2", actorName).addTransition("step1", "step2").getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDef, actorName, user);
        try {
            final long processDefinitionId = processDefinition.getId();
            final ProcessInstance pi = getProcessAPI().startProcess(processDefinitionId);
            final WaitForStep waitForStep = waitForStep("step1", pi);
            final long activityInstanceId = waitForStep.getStepId();

            // Let's update the value of data1 to 22. It should not be taken into account at process def level:
            getProcessAPI().updateActivityDataInstance(dataName1, activityInstanceId, 22);

            // create current variable Name&Value map: var1 = 12, var2 = ArrayList{"a", "b"}
            final String varName1 = "var1";
            final String varName2 = "var2";
            final ArrayList<String> var2Value = new ArrayList<String>();
            var2Value.add("a");
            var2Value.add("b");
            final Map<String, Serializable> currentVariables = new HashMap<String, Serializable>();
            currentVariables.put(varName1, 12);
            currentVariables.put(varName2, var2Value);

            // Create Operation keyed map:
            final List<Operation> operations = new ArrayList<Operation>();
            // First one is 'var1 = data1 + 33'
            final Expression dependencyData1 = new ExpressionBuilder().createDataExpression(dataName1, Integer.class.getName());
            final Expression expression1 = new ExpressionBuilder().createNewInstance("data1 + 33").setContent("data1 + 33")
                    .setDependencies(Arrays.asList(dependencyData1)).setExpressionType(ExpressionType.TYPE_READ_ONLY_SCRIPT.name()).setInterpreter("GROOVY")
                    .setReturnType(Integer.class.getName()).done();
            final Operation integerOperation1 = new OperationBuilder().createNewInstance()
                    .setLeftOperand(new LeftOperandBuilder().createNewInstance().setName(varName1).setExternal(true).done()).setType(OperatorType.ASSIGNMENT)
                    .setOperator("=").setRightOperand(expression1).done();
            final Map<String, Serializable> contexts = new HashMap<String, Serializable>();
            // contexts.put("processDefinitionId", processDefinition.getId()); // FIXME
            // contexts.put("containerType", ); // FIXME
            operations.add(integerOperation1);

            // Second one is 'var2.add("toto" + data1)'
            final Expression dependencyToto = new ExpressionBuilder().createConstantStringExpression("\"toto\"");
            final Expression expression2 = new ExpressionBuilder().createNewInstance("concat 'toto' to data1 value").setContent("\"toto\" + data1")
                    .setDependencies(Arrays.asList(dependencyToto, dependencyData1)).setExpressionType(ExpressionType.TYPE_READ_ONLY_SCRIPT.name())
                    .setInterpreter("GROOVY").setReturnType(String.class.getName()).done();
            final LeftOperand leftOperand = new LeftOperandBuilder().createNewInstance().setName(varName2).setExternal(true).done();
            final Operation integerOperation2 = new OperationBuilder().createNewInstance().setOperator("add").setOperatorInputType(Object.class.getName())
                    .setLeftOperand(leftOperand).setType(OperatorType.JAVA_METHOD).setRightOperand(expression2).done();
            operations.add(integerOperation2);

            // execute command:
            final String commandName = "getUpdatedVariableValuesForProcessDefinition";
            final HashMap<String, Serializable> commandParameters = new HashMap<String, Serializable>();
            commandParameters.put("OPERATIONS_LIST_KEY", (Serializable) operations);
            commandParameters.put("OPERATIONS_INPUT_KEY", (Serializable) contexts);
            commandParameters.put("CURRENT_VARIABLE_VALUES_MAP_KEY", (Serializable) currentVariables);
            commandParameters.put("PROCESS_DEFINITION_ID_KEY", processDefinitionId);
            @SuppressWarnings("unchecked")
            final Map<String, Serializable> updatedVariable = (Map<String, Serializable>) getCommandAPI().execute(commandName, commandParameters);

            // FIXME: change the test so that there is no evaluation of variables at definition level:
            // check and do assert:
            assertTrue(updatedVariable.size() == 2);
            assertEquals(34, updatedVariable.get(varName1));
            assertEquals(3, ((ArrayList<String>) updatedVariable.get(varName2)).size());
            assertEquals("toto1", ((ArrayList<String>) updatedVariable.get(varName2)).get(2));
        } finally {
            disableAndDeleteProcess(processDefinition);
            deleteUser(user);
        }
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.PROCESS, keywords = { "Command", "Updated variable value", "Process definition", "Wrong parameter" }, story = "Execute get updated variable values for process definition command with wrong parameter", jira = "ENGINE-586")
    @Test(expected = CommandParameterizationException.class)
    public void testGetUpdatedVariableValuesForProcessDefinitionCommandWithWrongParameter() throws Exception {

        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("BAD_KEY", "bad_value");

        getCommandAPI().execute("getUpdatedVariableValuesForProcessDefinition", parameters);
    }
}
