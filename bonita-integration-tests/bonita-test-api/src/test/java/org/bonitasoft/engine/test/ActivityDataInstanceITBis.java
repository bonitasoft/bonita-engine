/*
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
 */

package org.bonitasoft.engine.test;

import org.bonitasoft.engine.bpm.data.ArchivedDataNotFoundException;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.DataDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.ExceptionContext;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.operation.LeftOperand;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author mazourd
 */
@RunWith(Engine.class)
public class ActivityDataInstanceITBis {

    @EngineAnnotationInterface(user = "william.jobs", password = "bpm")
    private EngineInitializer engineInitializer;

    String ACTOR_NAME = "william.jobs";

    @Test
    public void getDataFromActivity() throws Exception {
        final DesignProcessDefinition processDef = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0").addActor(ACTOR_NAME)
                .addDescription("Delivery all day and night long").addUserTask("step1", ACTOR_NAME)
                .addIntegerData("var1", new ExpressionBuilder().createConstantIntegerExpression(1)).getProcess();

        assertDataOnActivityIntances(ACTOR_NAME, processDef, 1);
    }

    @Test
    public void getDataFromActivityThatIsAlsoInParent() throws Exception {
        final DesignProcessDefinition processDef = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.1").addActor(ACTOR_NAME)
                .addDescription("Delivery all day and night long").addIntegerData("var1", new ExpressionBuilder().createConstantIntegerExpression(1))
                .addUserTask("step1", ACTOR_NAME).addIntegerData("var1", new ExpressionBuilder().createConstantIntegerExpression(2)).getProcess();

        assertDataOnActivityIntances(ACTOR_NAME, processDef, 2);
    }

    @Test
    public void getDataFromActivityThatIsOnlyInParent() throws Exception {
        final DesignProcessDefinition processDef = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.1").addActor(ACTOR_NAME)
                .addDescription("Delivery all day and night long").addIntegerData("var1", new ExpressionBuilder().createConstantIntegerExpression(3))
                .addUserTask("step1", ACTOR_NAME).getProcess();

        assertDataOnActivityIntances(ACTOR_NAME, processDef, 3);
    }

    private void assertDataOnActivityIntances(final String actorName, final DesignProcessDefinition processDef, final int expectedNumber) throws Exception {
        final ProcessDefinition processDefinition = engineInitializer.getProcessDeployer().deployAndEnableProcessWithActor(processDef, actorName, engineInitializer.getUser());
        final ProcessDeploymentInfo processDeploymentInfo = engineInitializer.getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        // test execution
        StartedProcess process = engineInitializer.getAPITestProcessAnalyser().startProcess(processDeploymentInfo.getProcessId());
        process.waitForUserTask("step1").hasOnlyActivityDataInstance("var1").hasValue(expectedNumber);

        engineInitializer.getProcessDeployer().disableAndDeleteProcess(processDeploymentInfo.getProcessId());
    }

    @Test
    public void updateIntegerActivityDataInstance() throws Exception {
        updateActivityDataInstance(1, 2);
    }

    @Test
    public void updateBooleanActivityDataInstance() throws Exception {
        updateActivityDataInstance(false, true);
    }

    private void updateActivityDataInstance(final Serializable defaultDataValue, final Serializable updatedDataValue) throws Exception {
        final UserTaskDefinitionBuilder addUserTask = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0").addActor(ACTOR_NAME)
                .addDescription("Delivery all day and night long").addUserTask("step1", ACTOR_NAME);
        if (defaultDataValue instanceof Boolean) {
            addUserTask.addBooleanData("var1", new ExpressionBuilder().createConstantBooleanExpression((Boolean) defaultDataValue));
        } else if (defaultDataValue instanceof Integer) {
            addUserTask.addIntegerData("var1", new ExpressionBuilder().createConstantIntegerExpression((Integer) defaultDataValue));
        } else {
            throw new Exception("This test does not support data type different from (boolean, integer)");
        }
        final ProcessDefinition processDefinition = engineInitializer.getProcessDeployer().deployAndEnableProcessWithActor(addUserTask.getProcess(), ACTOR_NAME, engineInitializer.getUser());
        StartedProcess process = engineInitializer.getAPITestProcessAnalyser().startProcess(processDefinition.getId());
        process.waitForUserTask("step1").hasOnlyActivityDataInstance("var1").hasValue(defaultDataValue);
        process.updateActivityDataInstance("var1", updatedDataValue).hasOnlyActivityDataInstance("var1").hasValue(updatedDataValue);
        engineInitializer.getProcessDeployer().disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void updateActivityInstanceOperationsWithStringDataNotTransient() throws Exception {
        final DesignProcessDefinition designProcessDefinition = createProcessWithActorAndHumanTaskAndInitStringDataNotTransient();
        final ProcessDefinition processDefinition = engineInitializer.getProcessDeployer().deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, engineInitializer.getUser());
        final ProcessDeploymentInfo processDeploymentInfo = engineInitializer.getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());
        StartedProcess process = engineInitializer.getAPITestProcessAnalyser().startProcess(processDefinition.getId());
        process.waitForUserTask("step1");
        // Update data instance
        final String updatedValue = "afterUpdate";
        final Operation stringOperation = buildStringOperation("dataName", updatedValue, false);
        final List<Operation> operations = new ArrayList<Operation>();
        operations.add(stringOperation);
        process.getProcessAPI().updateActivityInstanceVariables(operations, process.getTemporaryUserTaskId(), null);
        new ReachedDataInstance(process.getProcessAPI().getActivityDataInstances(process.getTemporaryUserTaskId(), 0, 10).get(0)).hasValue(updatedValue);
        // Clean
        engineInitializer.getProcessDeployer().disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void updateActivityInstanceVariable() throws Exception {
        final DesignProcessDefinition processDef = createProcessWithActorAndHumanTaskAndInitStringDataNotTransient();
        final ProcessDefinition processDefinition = engineInitializer.getProcessDeployer().deployAndEnableProcessWithActor(processDef, ACTOR_NAME, engineInitializer.getUser());
        final ProcessDeploymentInfo processDeploymentInfo = engineInitializer.getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());
        StartedProcess process = engineInitializer.getAPITestProcessAnalyser().startProcess(processDefinition.getId());
        process.waitForUserTask("step1");
        final String updatedValue = "afterUpdate";
        final Map<String, Serializable> variables = new HashMap<String, Serializable>(2);
        variables.put("dataName", updatedValue);
        process.updateActivityInstanceVariables(variables).hasValue(updatedValue);
        engineInitializer.getProcessDeployer().disableAndDeleteProcess(processDefinition);
    }

    @Test(expected = UpdateException.class)
    public void cannotUpdateAnActivityInstanceVariable() throws Exception {
        final DesignProcessDefinition processDef = createProcessWithActorAndHumanTaskAndInitStringDataNotTransient();
        final ProcessDefinition processDefinition = engineInitializer.getProcessDeployer().deployAndEnableProcessWithActor(processDef, ACTOR_NAME, engineInitializer.getUser());
        final ProcessDeploymentInfo processDeploymentInfo = engineInitializer.getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());
        final ProcessInstance processInstance = engineInitializer.getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
        final long activityInstanceId = engineInitializer.getUserTaskAPI().waitForUserTask(processInstance.getId(), "step1", -1);
        final Map<String, Serializable> variables = new HashMap<String, Serializable>(2);
        variables.put("dataName1", "afterUpdate");
        try {
            engineInitializer.getProcessAPI().updateActivityInstanceVariables(activityInstanceId, variables);
        } finally {
            engineInitializer.getProcessDeployer().disableAndDeleteProcess(processDefinition);
        }
    }

    private DataInstance getActivityDataInstance(final long activityInstanceId) {
        final List<DataInstance> activityDataInstances = engineInitializer.getProcessAPI().getActivityDataInstances(activityInstanceId, 0, 10);
        assertEquals(1, activityDataInstances.size());
        return activityDataInstances.get(0);
    }

    @Test(expected = RetrieveException.class)
    public void dataNotAvailableAfterArchiveFromActivity() throws Exception {
        final DesignProcessDefinition processDef = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0").addActor(ACTOR_NAME)
                .addDescription("Delivery all day and night long").addUserTask("step1", ACTOR_NAME)
                .addIntegerData("var1", new ExpressionBuilder().createConstantIntegerExpression(1)).getProcess();
        final ProcessDefinition processDefinition = engineInitializer.getProcessDeployer().deployAndEnableProcessWithActor(processDef, ACTOR_NAME, engineInitializer.getUser());
        final ProcessDeploymentInfo processDeploymentInfo = engineInitializer.getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        // test execution
        final ProcessInstance processInstance = engineInitializer.getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
        StartedProcess process = engineInitializer.getAPITestProcessAnalyser().startProcess(processDefinition.getId());
        process.waitForUserTask("step1").hasOnlyActivityDataInstance("var1").hasValue(1);

        // verify the retrieved data
        List<DataInstance> processDataInstances = engineInitializer.getProcessAPI().getActivityDataInstances(process.getTemporaryUserTaskId(), 0, 10);
        // Execute pending task
        final List<ActivityInstance> activities = engineInitializer.getProcessAPI().getActivities(processInstance.getId(), 0, 200);
        final ActivityInstance activityInstance = activities.iterator().next();
        assignAndExecuteStep(activityInstance.getId(), engineInitializer.getUser().getId());
        engineInitializer.getUserTaskAPI().waitForProcessToFinish(processInstance.getId(), -1);

        // retrieve data after process has finished
        try {
            processDataInstances = engineInitializer.getProcessAPI().getActivityDataInstances(processInstance.getId(), 0, 10);
        } finally {
            engineInitializer.getProcessDeployer().disableAndDeleteProcess(processDefinition);
        }
    }

    @Test
    public void executeProcess3TimesWithNotInitializedData() throws Exception {
        final Expression dataExpr = new ExpressionBuilder().createDataExpression("booleanData", Boolean.class.getName());
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("ProcessName", "1.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME).addBooleanData("booleanData", null);
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME);
        processDefinitionBuilder.addTransition("step1", "step2", dataExpr).addDefaultTransition("step1", "step3");
        final ProcessDefinition processDefinition = engineInitializer.getProcessDeployer().deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, engineInitializer.getUser());

        // Start first process, and wait the first step
        final ProcessInstance processInstance1 = engineInitializer.getProcessAPI().startProcess(processDefinition.getId());
        final Long step1_1Id = engineInitializer.getUserTaskAPI().waitForUserTask(processInstance1.getId(), "step1", -1);
        // Set data to true, for the first instance
        engineInitializer.getProcessAPI().updateActivityDataInstance("booleanData", step1_1Id, true);

        // Start second process, and wait the first step
        final ProcessInstance processInstance2 = engineInitializer.getProcessAPI().startProcess(processDefinition.getId());
        final Long step1_2Id = engineInitializer.getUserTaskAPI().waitForUserTask(processInstance2.getId(), "step1", -1);
        // Set data to false, for the second instance
        engineInitializer.getProcessAPI().updateActivityDataInstance("booleanData", step1_2Id, false);

        // Start third process, and wait the first step
        final ProcessInstance processInstance3 = engineInitializer.getProcessAPI().startProcess(processDefinition.getId());
        final Long step1_3Id = engineInitializer.getUserTaskAPI().waitForUserTask(processInstance3.getId(), "step1", -1);
        // Set data to true, for the third instance
        engineInitializer.getProcessAPI().updateActivityDataInstance("booleanData", step1_3Id, true);

        // Execute all step1
        assignAndExecuteStep(step1_1Id, engineInitializer.getUser().getId());
        engineInitializer.getUserTaskAPI().waitForUserTask(processInstance1.getId(), "step2", -1);
        assignAndExecuteStep(step1_2Id, engineInitializer.getUser().getId());
        engineInitializer.getUserTaskAPI().waitForUserTask(processInstance2.getId(), "step3", -1);
        assignAndExecuteStep(step1_3Id, engineInitializer.getUser().getId());
        engineInitializer.getUserTaskAPI().waitForUserTask(processInstance3.getId(), "step2", -1);

        // Check that only these 3 steps are pending
        assertEquals(3, engineInitializer.getProcessAPI().getNumberOfPendingHumanTaskInstances(engineInitializer.getUser().getId()));

        // Clean-up
        engineInitializer.getProcessDeployer().disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void runProcessWithInvalidInitialValue() throws Exception {
        final ProcessDefinitionBuilder createNewInstance = new ProcessDefinitionBuilder().createNewInstance("processwithIntegerData", "1.0");
        createNewInstance.addAutomaticTask("step1").addIntegerData("intdata",
                new ExpressionBuilder().createExpression("d", "d", Integer.class.getName(), ExpressionType.TYPE_CONSTANT));

        final ProcessDefinition processDefinition = engineInitializer.getProcessDeployer().deployAndEnableProcess(createNewInstance.done());
        StartedProcess process = engineInitializer.getAPITestProcessAnalyser().startProcess(processDefinition.getId());
        process.waitForTaskToFail().hasSameNameAs("step1");
        engineInitializer.getProcessDeployer().disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void processWithDataHavingSameNameInTwoContainer() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("ProcessName", "1.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME).addShortTextData("a", new ExpressionBuilder().createConstantStringExpression("step1"));
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME).addShortTextData("a", new ExpressionBuilder().createConstantStringExpression("step2"));
        processDefinitionBuilder.addShortTextData("a", new ExpressionBuilder().createConstantStringExpression("process"));
        final DesignProcessDefinition designProcessDefinition = processDefinitionBuilder.done();
        final ProcessDefinition processDefinition = engineInitializer.getProcessDeployer().deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, engineInitializer.getUser());
        final ProcessInstance processInstance = engineInitializer.getProcessAPI().startProcess(processDefinition.getId());
        StartedProcess process = engineInitializer.getAPITestProcessAnalyser().startProcess(processDefinition.getId());
        process.waitForUserTask("step1").hasSameNameAs(
                (String) engineInitializer.getUserTaskAPI().getProcessAPI().getActivityDataInstance("a", process.getTemporaryUserTaskId()).getValue());
        process.waitForUserTask("step2").hasSameNameAs(
                (String) engineInitializer.getUserTaskAPI().getProcessAPI().getActivityDataInstance("a", process.getTemporaryUserTaskId()).getValue());
        process.accessDataInTask("a").hasValue("process");
        engineInitializer.getProcessDeployer().disableAndDeleteProcess(processDefinition);
    }

    private DesignProcessDefinition createProcessWithActorAndHumanTaskAndInitStringDataNotTransient() throws Exception {
        return createProcessWithActorAndHumanTaskAndStringData(new ExpressionBuilder().createConstantStringExpression("beforeUpdate"), false).done();
    }

    private ProcessDefinitionBuilder createProcessWithActorAndHumanTaskAndStringData(final Expression defaultValue, final boolean isTransient) {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("processName", "1.0");
        processDefinitionBuilder.addActor(ACTOR_NAME).addDescription("Delivery all day and night long");
        final UserTaskDefinitionBuilder userTaskBuilder = processDefinitionBuilder.addUserTask("step1", ACTOR_NAME);
        final DataDefinitionBuilder shortTextData = userTaskBuilder.addShortTextData("dataName", defaultValue);
        if (isTransient) {
            shortTextData.isTransient();
        }
        return processDefinitionBuilder;
    }

    @Test
    public void canGetDataInstanceWhenThereAreTransientData() throws Exception {
        final String userTaskName = "task1";
        final ProcessDefinition processDefinition = deployAndEnableProcWithPersistedAndTransientVariable(userTaskName);
        final ProcessInstance processInstance = engineInitializer.getProcessAPI().startProcess(processDefinition.getId());
        final Long userTaskId = engineInitializer.getUserTaskAPI().waitForUserTask(processInstance.getId(), userTaskName, -1);

        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<Expression, Map<String, Serializable>>(2);
        final Expression persistedVariableExpression = new ExpressionBuilder().createDataExpression("persistedVariable", String.class.getName());
        final Expression transientVariableExpression = new ExpressionBuilder().createTransientDataExpression("transientVariable", String.class.getName());
        expressions.put(persistedVariableExpression, (Map<String, Serializable>) null);
        expressions.put(transientVariableExpression, (Map<String, Serializable>) null);

        final Map<String, Serializable> expressionResult = engineInitializer.getProcessAPI().evaluateExpressionsOnActivityInstance(userTaskId, expressions);
        assertEquals("default", expressionResult.get(persistedVariableExpression.getName()));
        assertEquals("default", expressionResult.get(transientVariableExpression.getName()));

        engineInitializer.getProcessDeployer().disableAndDeleteProcess(processDefinition.getId());
    }

    @Test
    public void getArchivedActivityDataInstance() throws Exception {
        final String dataName = "title";
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessToArchive", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("step", ACTOR_NAME).addShortTextData(dataName, new ExpressionBuilder().createConstantStringExpression("1"));

        final ProcessDefinition processDefinition = engineInitializer.getProcessDeployer().deployAndEnableProcessWithActor(builder.getProcess(), ACTOR_NAME, engineInitializer.getUser());
        StartedProcess process = engineInitializer.getAPITestProcessAnalyser().startProcess(processDefinition.getId());
        process.waitForUserTask("step");
        process.updateActivityDataInstance(dataName, "2").hasOnlyActivityDataInstance(dataName).hasValue(
                process.getProcessAPI().getArchivedActivityDataInstance(dataName, process.getTemporaryUserTaskId()).getValue());
        engineInitializer.getProcessDeployer().disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getArchivedActivityDataInstanceFromAnArchivedProcess() throws Exception {
        final String dataName = "title";
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessToArchive", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("step", ACTOR_NAME).addShortTextData(dataName, new ExpressionBuilder().createConstantStringExpression("1"));

        final ProcessDefinition processDefinition = engineInitializer.getProcessDeployer().deployAndEnableProcessWithActor(builder.getProcess(), ACTOR_NAME, engineInitializer.getUser());
        StartedProcess process = engineInitializer.getAPITestProcessAnalyser().startProcess(processDefinition.getId());
        process.waitForUserTask("step");
        process.updateActivityDataInstance(dataName, "2").assignAndExecuteTask(engineInitializer.getUser().getId());
        engineInitializer.getUserTaskAPI().waitForProcessToFinish(process.getProcessInstance().getId(), -1);
        process.getArchivedData(dataName).hasValue("2");
        engineInitializer.getProcessDeployer().disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getArchivedActivityDataInstances() throws Exception {
        final String dataName = "title";
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessToArchive", "1.0");
        builder.addShortTextData(dataName, new ExpressionBuilder().createConstantStringExpression("0"));
        builder.addActor(ACTOR_NAME);
        final UserTaskDefinitionBuilder taskDefinitionBuilder = builder.addUserTask("step", ACTOR_NAME);
        taskDefinitionBuilder.addShortTextData(dataName, new ExpressionBuilder().createConstantStringExpression("1"));
        taskDefinitionBuilder.addShortTextData("job", new ExpressionBuilder().createConstantStringExpression("job"));
        taskDefinitionBuilder.addShortTextData("desc", new ExpressionBuilder().createConstantStringExpression("desc"));
        final ProcessDefinition processDefinition = engineInitializer.getProcessDeployer().deployAndEnableProcessWithActor(builder.getProcess(), ACTOR_NAME, engineInitializer.getUser());
        String[] dataNames = new String[] { "2", "job", "desc" };
        StartedProcess process = engineInitializer.getAPITestProcessAnalyser().startProcess(processDefinition.getId());
        process.waitForUserTask("step");
        process.updateActivityDataInstance(dataName, "2").assignAndExecuteTask(engineInitializer.getUser().getId());
        engineInitializer.getUserTaskAPI().waitForProcessToFinish(process.getProcessInstance().getId(), -1);
        process.getArchivedDatas(0, 10).hasSize(3).containsValues(dataNames);
        process.getArchivedDatas(0, 1).hasSize(1).containsValue("2");
        process.getArchivedDatas(1, 10).hasSize(2).containsValue("job").containsValue("desc");
        engineInitializer.getProcessDeployer().disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getUnknownArchivedActivityDataInstance() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessToArchive", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("step", ACTOR_NAME);

        final ProcessDefinition processDefinition = engineInitializer.getProcessDeployer().deployAndEnableProcessWithActor(builder.getProcess(), ACTOR_NAME, engineInitializer.getUser());
        StartedProcess process = engineInitializer.getAPITestProcessAnalyser().startProcess(processDefinition.getId());
        process.waitForUserTask("step");
        try {
            process.getArchivedData("o");
            fail("The data named 'o' does not exists");
        } catch (final ArchivedDataNotFoundException dnfe) {
            // Do nothing
        } finally {
            engineInitializer.getProcessDeployer().disableAndDeleteProcess(processDefinition);
        }
    }

    private ProcessDefinition deployAndEnableProcWithPersistedAndTransientVariable(final String userTaskName) throws InvalidExpressionException,
            BonitaException, InvalidProcessDefinitionException {
        final String startName = "start";
        final String endName = "end";
        final Expression defaultValue = new ExpressionBuilder().createConstantStringExpression("default");
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("proc", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addStartEvent(startName);
        final UserTaskDefinitionBuilder taskBuilder = builder.addUserTask(userTaskName, ACTOR_NAME);
        taskBuilder.addShortTextData("persistedVariable", defaultValue);
        taskBuilder.addShortTextData("transientVariable", defaultValue).isTransient();
        builder.addEndEvent(endName);
        builder.addTransition(startName, userTaskName);
        builder.addTransition(userTaskName, endName);

        return engineInitializer.getProcessDeployer().deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, engineInitializer.getUser());
    }

    @Test
    public void cantUpdateActivityDataInstanceWithWrongValue() throws Exception {
        final DesignProcessDefinition processDef = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0").addActor(ACTOR_NAME)
                .addDescription("Delivery all day and night long")
                .addUserTask("step1", ACTOR_NAME).addData("data", List.class.getName(), null).getProcess();
        final ProcessDefinition processDefinition = engineInitializer.getProcessDeployer().deployAndEnableProcessWithActor(processDef, ACTOR_NAME, engineInitializer.getUser());
        StartedProcess process = engineInitializer.getAPITestProcessAnalyser().startProcess(processDefinition.getId());
        process.waitForUserTask("step1");
        // test execution
        // verify the retrieved data
        try {
            process.updateActivityDataInstance("data", "wrong value");
            fail();
        } catch (final UpdateException e) {
            assertEquals("USERNAME=" + "install" + " | DATA_NAME=data | DATA_CLASS_NAME=java.util.List | The type of new value ["
                    + String.class.getName()
                    + "] is not compatible with the type of the data.", e.getMessage());
            final Map<ExceptionContext, Serializable> exceptionContext = e.getContext();
            assertEquals(List.class.getName(), exceptionContext.get(ExceptionContext.DATA_CLASS_NAME));
            assertEquals("data", exceptionContext.get(ExceptionContext.DATA_NAME));
        }

        process.getArchivedDatas(0, 10).hasSize(1).containsValue(null);
        final List<Expression> dependencies = Collections.singletonList(new ExpressionBuilder().createDataExpression("data", List.class.getName()));
        final Expression longExpression = new ExpressionBuilder().createGroovyScriptExpression("Script",
                "data = new ArrayList<String>(); data.add(\"plop\"); return data;", List.class.getName(), dependencies);
        final Map<Expression, Map<String, Serializable>> expressions = Collections.singletonMap(longExpression, Collections.<String, Serializable> emptyMap());
        process.getProcessAPI().evaluateExpressionsOnActivityInstance(process.getTemporaryUserTaskId(), expressions);

        engineInitializer.getProcessDeployer().disableAndDeleteProcess(processDefinition);
    }

    private static Operation buildStringOperation(final String dataInstanceName, final String newConstantValue, final boolean isTransient)
            throws InvalidExpressionException {
        final LeftOperand leftOperand = new LeftOperandBuilder().createNewInstance().setName(dataInstanceName)
                .setType(isTransient ? LeftOperand.TYPE_TRANSIENT_DATA : LeftOperand.TYPE_DATA).done();
        final Expression expression = new ExpressionBuilder().createConstantStringExpression(newConstantValue);
        final Operation operation;
        operation = new OperationBuilder().createNewInstance().setOperator("=").setLeftOperand(leftOperand).setType(OperatorType.ASSIGNMENT)
                .setRightOperand(expression).done();
        return operation;
    }

    private void assignAndExecuteStep(final long activityInstanceId, final long userId) throws BonitaException {
        engineInitializer.getProcessAPI().assignUserTask(activityInstanceId, userId);
        engineInitializer.getProcessAPI().executeFlowNode(activityInstanceId);
    }
}
