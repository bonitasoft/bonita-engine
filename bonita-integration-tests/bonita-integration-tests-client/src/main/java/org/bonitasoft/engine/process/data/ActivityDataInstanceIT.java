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
package org.bonitasoft.engine.process.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.data.ArchivedDataInstance;
import org.bonitasoft.engine.bpm.data.ArchivedDataNotFoundException;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
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
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

public class ActivityDataInstanceIT extends TestWithUser {

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
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDef, actorName, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        // test execution
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
        final Long step1Id = waitForUserTask(processInstance, "step1");

        // verify there are data
        final List<DataInstance> processDataInstances = getProcessAPI().getActivityDataInstances(step1Id, 0, 10);
        assertTrue(!processDataInstances.isEmpty());
        assertEquals(1, processDataInstances.size());
        assertEquals("var1", processDataInstances.get(0).getName());
        assertEquals(expectedNumber, processDataInstances.get(0).getValue());

        disableAndDeleteProcess(processDefinition);
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
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(addUserTask.getProcess(), ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long activityInstanceId = waitForUserTask(processInstance, "step1");

        final DataInstance dataInstance = getActivityDataInstance(activityInstanceId);
        assertEquals("var1", dataInstance.getName());
        assertEquals(defaultDataValue, dataInstance.getValue());

        getProcessAPI().updateActivityDataInstance("var1", activityInstanceId, updatedDataValue);
        final DataInstance updatedData = getActivityDataInstance(activityInstanceId);
        assertEquals(updatedDataValue, updatedData.getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void updateActivityInstanceOperationsWithStringDataNotTransient() throws Exception {
        final DesignProcessDefinition designProcessDefinition = createProcessWithActorAndHumanTaskAndInitStringDataNotTransient();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
        final long activityInstanceId = waitForUserTask(processInstance, "step1");

        // Update data instance
        final String updatedValue = "afterUpdate";
        updateActivityInstanceVariablesWithOperations(updatedValue, activityInstanceId, "dataName", false);
        assertEquals(updatedValue, getProcessAPI().getActivityDataInstance("dataName", activityInstanceId).getValue());

        // Clean
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void updateActivityInstanceVariable() throws Exception {
        final DesignProcessDefinition processDef = createProcessWithActorAndHumanTaskAndInitStringDataNotTransient();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDef, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
        final long activityInstanceId = waitForUserTask(processInstance, "step1");

        final String updatedValue = "afterUpdate";
        final Map<String, Serializable> variables = new HashMap<String, Serializable>(2);
        variables.put("dataName", updatedValue);
        getProcessAPI().updateActivityInstanceVariables(activityInstanceId, variables);

        final DataInstance dataInstance = getProcessAPI().getActivityDataInstance("dataName", activityInstanceId);
        assertEquals(updatedValue, dataInstance.getValue());
        disableAndDeleteProcess(processDefinition);
    }

    @Test(expected = UpdateException.class)
    public void cannotUpdateAnActivityInstanceVariable() throws Exception {
        final DesignProcessDefinition processDef = createProcessWithActorAndHumanTaskAndInitStringDataNotTransient();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDef, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
        final long activityInstanceId = waitForUserTask(processInstance, "step1");

        final Map<String, Serializable> variables = new HashMap<String, Serializable>(2);
        variables.put("dataName1", "afterUpdate");
        try {
            getProcessAPI().updateActivityInstanceVariables(activityInstanceId, variables);
        } finally {
            disableAndDeleteProcess(processDefinition);
        }
    }

    private DataInstance getActivityDataInstance(final long activityInstanceId) {
        final List<DataInstance> activityDataInstances = getProcessAPI().getActivityDataInstances(activityInstanceId, 0, 10);
        assertEquals(1, activityDataInstances.size());
        return activityDataInstances.get(0);
    }

    @Test(expected = RetrieveException.class)
    public void dataNotAvailableAfterArchiveFromActivity() throws Exception {
        final DesignProcessDefinition processDef = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0").addActor(ACTOR_NAME)
                .addDescription("Delivery all day and night long").addUserTask("step1", ACTOR_NAME)
                .addIntegerData("var1", new ExpressionBuilder().createConstantIntegerExpression(1)).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDef, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        // test execution
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
        final Long step1Id = waitForUserTask(processInstance, "step1");

        // verify the retrieved data
        List<DataInstance> processDataInstances = getProcessAPI().getActivityDataInstances(step1Id, 0, 10);
        assertTrue(!processDataInstances.isEmpty());
        assertEquals(1, processDataInstances.size());
        assertEquals("var1", processDataInstances.get(0).getName());
        assertEquals(1, processDataInstances.get(0).getValue());

        // Execute pending task
        final List<ActivityInstance> activities = getProcessAPI().getActivities(processInstance.getId(), 0, 200);
        final ActivityInstance activityInstance = activities.iterator().next();
        assignAndExecuteStep(activityInstance, user.getId());
        waitForProcessToFinish(processInstance);

        // retrieve data after process has finished
        try {
            processDataInstances = getProcessAPI().getActivityDataInstances(processInstance.getId(), 0, 10);
        } finally {
            disableAndDeleteProcess(processDefinition);
        }
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.DATA, keywords = { "3 Process instances", "Data", "Boolean", "Transition", "Condition",
            "Default", "Update" }, jira = "ENGINE-1459")
    @Test
    public void executeProcess3TimesWithNotInitializedData() throws Exception {
        final Expression dataExpr = new ExpressionBuilder().createDataExpression("booleanData", Boolean.class.getName());
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME).addBooleanData("booleanData", null);
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME);
        processDefinitionBuilder.addTransition("step1", "step2", dataExpr).addDefaultTransition("step1", "step3");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, user);

        // Start first process, and wait the first step
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(processDefinition.getId());
        final Long step1_1Id = waitForUserTask(processInstance1, "step1");
        // Set data to true, for the first instance
        getProcessAPI().updateActivityDataInstance("booleanData", step1_1Id, true);

        // Start second process, and wait the first step
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(processDefinition.getId());
        final Long step1_2Id = waitForUserTask(processInstance2, "step1");
        // Set data to false, for the second instance
        getProcessAPI().updateActivityDataInstance("booleanData", step1_2Id, false);

        // Start third process, and wait the first step
        final ProcessInstance processInstance3 = getProcessAPI().startProcess(processDefinition.getId());
        final Long step1_3Id = waitForUserTask(processInstance3, "step1");
        // Set data to true, for the third instance
        getProcessAPI().updateActivityDataInstance("booleanData", step1_3Id, true);

        // Execute all step1
        assignAndExecuteStep(step1_1Id, user);
        waitForUserTask(processInstance1, "step2");
        assignAndExecuteStep(step1_2Id, user);
        waitForUserTask(processInstance2, "step3");
        assignAndExecuteStep(step1_3Id, user);
        waitForUserTask(processInstance3, "step2");

        // Check that only these 3 steps are pending
        assertEquals(3, getProcessAPI().getNumberOfPendingHumanTaskInstances(user.getId()));

        // Clean-up
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void runProcessWithInvalidInitialValue() throws Exception {
        final ProcessDefinitionBuilder createNewInstance = new ProcessDefinitionBuilder().createNewInstance("processwithIntegerData", "1.0");
        createNewInstance.addAutomaticTask("step1").addIntegerData("intdata",
                new ExpressionBuilder().createExpression("d", "d", Integer.class.getName(), ExpressionType.TYPE_CONSTANT));
        final ProcessDefinition processDefinition = deployAndEnableProcess(createNewInstance.done());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final ActivityInstance failedTask = waitForTaskToFail(processInstance);
        assertEquals("step1", failedTask.getName());

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = DataInstance.class, concept = BPMNConcept.DATA, jira = "ENGINE-679", keywords = { "data", "container" }, story = "process with data having same name in different container")
    @Test
    public void processWithDataHavingSameNameInTwoContainer() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME).addShortTextData("a", new ExpressionBuilder().createConstantStringExpression("step1"));
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME).addShortTextData("a", new ExpressionBuilder().createConstantStringExpression("step2"));
        processDefinitionBuilder.addShortTextData("a", new ExpressionBuilder().createConstantStringExpression("process"));
        final DesignProcessDefinition designProcessDefinition = processDefinitionBuilder.done();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final HumanTaskInstance step1 = waitForUserTaskAndGetIt(processInstance, "step1");
        assertEquals(step1.getName(), getProcessAPI().getActivityDataInstance("a", step1.getId()).getValue());

        final HumanTaskInstance step2 = waitForUserTaskAndGetIt(processInstance, "step2");
        assertEquals(step2.getName(), getProcessAPI().getActivityDataInstance("a", step2.getId()).getValue());

        assertEquals("process", getProcessAPI().getProcessDataInstance("a", processInstance.getId()).getValue());
        disableAndDeleteProcess(processDefinition);
    }

    private DesignProcessDefinition createProcessWithActorAndHumanTaskAndInitStringDataNotTransient() throws Exception {
        return createProcessWithActorAndHumanTaskAndStringData(new ExpressionBuilder().createConstantStringExpression("beforeUpdate"), false).done();
    }

    private ProcessDefinitionBuilder createProcessWithActorAndHumanTaskAndStringData(final Expression defaultValue, final boolean isTransient) {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilder.addActor(ACTOR_NAME).addDescription("Delivery all day and night long");
        final UserTaskDefinitionBuilder userTaskBuilder = processDefinitionBuilder.addUserTask("step1", ACTOR_NAME);
        final DataDefinitionBuilder shortTextData = userTaskBuilder.addShortTextData("dataName", defaultValue);
        if (isTransient) {
            shortTextData.isTransient();
        }
        return processDefinitionBuilder;
    }

    @Cover(classes = { DataInstance.class }, concept = BPMNConcept.DATA, keywords = { "data instance", "transient data", "persisted data" }, jira = "ENGINE-1447", story = "It's possible to evaluate a data expression in a task containing transient data and persisted data")
    @Test
    public void canGetDataInstanceWhenThereAreTransientData() throws Exception {
        final String userTaskName = "task1";
        final ProcessDefinition processDefinition = deployAndEnableProcWithPersistedAndTransientVariable(userTaskName);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final Long userTaskId = waitForUserTask(processInstance, userTaskName);

        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<Expression, Map<String, Serializable>>(2);
        final Expression persistedVariableExpression = new ExpressionBuilder().createDataExpression("persistedVariable", String.class.getName());
        final Expression transientVariableExpression = new ExpressionBuilder().createTransientDataExpression("transientVariable", String.class.getName());
        expressions.put(persistedVariableExpression, (Map<String, Serializable>) null);
        expressions.put(transientVariableExpression, (Map<String, Serializable>) null);

        final Map<String, Serializable> expressionResult = getProcessAPI().evaluateExpressionsOnActivityInstance(userTaskId, expressions);
        assertEquals("default", expressionResult.get(persistedVariableExpression.getName()));
        assertEquals("default", expressionResult.get(transientVariableExpression.getName()));

        disableAndDeleteProcess(processDefinition.getId());
    }

    @Cover(jira = "ENGINE-1821", classes = { ArchivedDataInstance.class, ProcessAPI.class }, concept = BPMNConcept.DATA, keywords = { "last archived data",
            "activity instance" })
    @Test
    public void getArchivedActivityDataInstance() throws Exception {
        final String dataName = "title";
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessToArchive", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("step", ACTOR_NAME).addShortTextData(dataName, new ExpressionBuilder().createConstantStringExpression("1"));

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.getProcess(), ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final Long userTaskId = waitForUserTask(processInstance, "step");
        getProcessAPI().updateActivityDataInstance(dataName, userTaskId, "2");

        final ArchivedDataInstance archivedData = getProcessAPI().getArchivedActivityDataInstance(dataName, userTaskId);
        assertEquals("2", archivedData.getValue());
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1821", classes = { ArchivedDataInstance.class, ProcessAPI.class }, concept = BPMNConcept.DATA, keywords = { "last archived data",
            "activity instance" })
    @Test
    public void getArchivedActivityDataInstanceFromAnArchivedProcess() throws Exception {
        final String dataName = "title";
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessToArchive", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("step", ACTOR_NAME).addShortTextData(dataName, new ExpressionBuilder().createConstantStringExpression("1"));

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.getProcess(), ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final Long userTaskId = waitForUserTask(processInstance, "step");
        getProcessAPI().updateActivityDataInstance(dataName, userTaskId, "2");
        assignAndExecuteStep(userTaskId, user.getId());
        waitForProcessToFinish(processInstance.getId());

        final ArchivedDataInstance archivedData = getProcessAPI().getArchivedActivityDataInstance(dataName, userTaskId);
        assertEquals("2", archivedData.getValue());
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1822", classes = { ArchivedDataInstance.class, ProcessAPI.class }, concept = BPMNConcept.DATA, keywords = { "last archived data",
            "activity instance" })
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

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.getProcess(), ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final Long userTaskId = waitForUserTask(processInstance, "step");
        getProcessAPI().updateActivityDataInstance(dataName, userTaskId, "2");
        assignAndExecuteStep(userTaskId, user);
        waitForProcessToFinish(processInstance.getId());

        List<ArchivedDataInstance> archivedDataInstances = getProcessAPI().getArchivedActivityDataInstances(userTaskId, 0, 10);
        assertEquals(3, archivedDataInstances.size());
        ArchivedDataInstance archivedDataInstance = getArchivedDataInstance(archivedDataInstances, dataName);
        assertEquals("2", archivedDataInstance.getValue());
        archivedDataInstance = getArchivedDataInstance(archivedDataInstances, "job");
        assertEquals("job", archivedDataInstance.getValue());
        archivedDataInstance = getArchivedDataInstance(archivedDataInstances, "desc");
        assertEquals("desc", archivedDataInstance.getValue());

        archivedDataInstances = getProcessAPI().getArchivedActivityDataInstances(userTaskId, 0, 1);
        assertEquals(1, archivedDataInstances.size());
        archivedDataInstance = getArchivedDataInstance(archivedDataInstances, dataName);
        assertEquals("2", archivedDataInstance.getValue());

        archivedDataInstances = getProcessAPI().getArchivedActivityDataInstances(userTaskId, 1, 10);
        assertEquals(2, archivedDataInstances.size());
        archivedDataInstance = getArchivedDataInstance(archivedDataInstances, "job");
        assertEquals("job", archivedDataInstance.getValue());
        archivedDataInstance = getArchivedDataInstance(archivedDataInstances, "desc");
        assertEquals("desc", archivedDataInstance.getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1821", classes = { ArchivedDataInstance.class, ProcessAPI.class }, concept = BPMNConcept.DATA, keywords = { "last archived data",
            "activity instance" })
    @Test
    public void getUnknownArchivedActivityDataInstance() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessToArchive", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("step", ACTOR_NAME);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.getProcess(), ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final Long userTaskId = waitForUserTask(processInstance, "step");

        try {
            getProcessAPI().getArchivedProcessDataInstance("o", userTaskId);
            fail("The data named 'o' does not exists");
        } catch (final ArchivedDataNotFoundException dnfe) {
            // Do nothing
        } finally {
            disableAndDeleteProcess(processDefinition);
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

        return deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.DATA, jira = "BS-1984", keywords = { "update", "activity data", "wrong type" })
    @Test
    public void cantUpdateActivityDataInstanceWithWrongValue() throws Exception {
        final DesignProcessDefinition processDef = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0").addActor(ACTOR_NAME)
                .addDescription("Delivery all day and night long")
                .addUserTask("step1", ACTOR_NAME).addData("data", List.class.getName(), null).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDef, ACTOR_NAME, user);

        // test execution
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(processInstance, "step1");

        // verify the retrieved data
        try {
            getProcessAPI().updateActivityDataInstance("data", step1Id, "wrong value");
            fail();
        } catch (final UpdateException e) {
            assertEquals("USERNAME=" + USERNAME + " | DATA_NAME=data | DATA_CLASS_NAME=java.util.List | The type of new value [" + String.class.getName()
                    + "] is not compatible with the type of the data.", e.getMessage());
            final Map<ExceptionContext, Serializable> exceptionContext = e.getContext();
            assertEquals(List.class.getName(), exceptionContext.get(ExceptionContext.DATA_CLASS_NAME));
            assertEquals("data", exceptionContext.get(ExceptionContext.DATA_NAME));
        }

        // retrieve data after the update
        final List<DataInstance> activityDataInstances = getProcessAPI().getActivityDataInstances(step1Id, 0, 10);
        assertEquals(1, activityDataInstances.size());
        assertEquals(null, activityDataInstances.get(0).getValue());

        // Evaluate the data
        final List<Expression> dependencies = Collections.singletonList(new ExpressionBuilder().createDataExpression("data", List.class.getName()));
        final Expression longExpression = new ExpressionBuilder().createGroovyScriptExpression("Script",
                "data = new ArrayList<String>(); data.add(\"plop\"); return data;", List.class.getName(), dependencies);
        final Map<Expression, Map<String, Serializable>> expressions = Collections.singletonMap(longExpression, Collections.<String, Serializable> emptyMap());
        getProcessAPI().evaluateExpressionsOnActivityInstance(step1Id, expressions);

        disableAndDeleteProcess(processDefinition);
    }

}
