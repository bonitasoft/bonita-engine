package org.bonitasoft.engine.process.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.CommonAPITest;
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
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ActivityDataInstanceIT extends CommonAPITest {

    protected User user;

    @Before
    public void before() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        user = createUser("pedro", "secreto");
    }

    @After
    public void after() throws Exception {
        deleteUser(user);
        logoutOnTenant();
    }

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
        final HumanTaskInstance step1 = waitForUserTask("step1", processInstance);

        // verify there are data
        final List<DataInstance> processDataInstances = getProcessAPI().getActivityDataInstances(step1.getId(), 0, 10);
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
        final long activityInstanceId = waitForUserTask("step1", processInstance).getId();

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
        final long activityInstanceId = waitForUserTask("step1", processInstance).getId();

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
        final long activityInstanceId = waitForUserTask("step1", processInstance).getId();

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
        final long activityInstanceId = waitForUserTask("step1", processInstance).getId();

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
        final HumanTaskInstance step1 = waitForUserTask("step1", processInstance);

        // verify the retrieved data
        List<DataInstance> processDataInstances = getProcessAPI().getActivityDataInstances(step1.getId(), 0, 10);
        assertTrue(!processDataInstances.isEmpty());
        assertEquals(1, processDataInstances.size());
        assertEquals("var1", processDataInstances.get(0).getName());
        assertEquals(1, processDataInstances.get(0).getValue());

        // Execute pending task
        final List<ActivityInstance> activities = getProcessAPI().getActivities(processInstance.getId(), 0, 200);
        final ActivityInstance activityInstance = activities.iterator().next();
        assignAndExecuteStep(activityInstance, user.getId());
        assertTrue("process was not completed", waitForProcessToFinishAndBeArchived(processInstance));

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
        final ActivityInstance step1_1 = waitForUserTask("step1", processInstance1);
        // Set data to true, for the first instance
        getProcessAPI().updateActivityDataInstance("booleanData", step1_1.getId(), true);

        // Start second process, and wait the first step
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance step1_2 = waitForUserTask("step1", processInstance2);
        // Set data to false, for the second instance
        getProcessAPI().updateActivityDataInstance("booleanData", step1_2.getId(), false);

        // Start third process, and wait the first step
        final ProcessInstance processInstance3 = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance step1_3 = waitForUserTask("step1", processInstance3);
        // Set data to true, for the third instance
        getProcessAPI().updateActivityDataInstance("booleanData", step1_3.getId(), true);

        // Execute all step1
        assignAndExecuteStep(step1_1, user.getId());
        waitForUserTask("step2", processInstance1);
        assignAndExecuteStep(step1_2, user.getId());
        waitForUserTask("step3", processInstance2);
        assignAndExecuteStep(step1_3, user.getId());
        waitForUserTask("step2", processInstance3);

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

        final HumanTaskInstance step1 = waitForUserTask("step1", processInstance);
        assertEquals(step1.getName(), getProcessAPI().getActivityDataInstance("a", step1.getId()).getValue());

        final HumanTaskInstance step2 = waitForUserTask("step2", processInstance);
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
        final ActivityInstance userTask = waitForUserTask(userTaskName, processInstance);

        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<Expression, Map<String, Serializable>>(2);
        final Expression persistedVariableExpression = new ExpressionBuilder().createDataExpression("persistedVariable", String.class.getName());
        final Expression transientVariableExpression = new ExpressionBuilder().createTransientDataExpression("transientVariable", String.class.getName());
        expressions.put(persistedVariableExpression, (Map<String, Serializable>) null);
        expressions.put(transientVariableExpression, (Map<String, Serializable>) null);

        final Map<String, Serializable> expressionResult = getProcessAPI().evaluateExpressionsOnActivityInstance(userTask.getId(), expressions);
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
        final HumanTaskInstance userTask = waitForUserTask("step", processInstance);
        getProcessAPI().updateActivityDataInstance(dataName, userTask.getId(), "2");

        final ArchivedDataInstance archivedData = getProcessAPI().getArchivedActivityDataInstance(dataName, userTask.getId());
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
        final HumanTaskInstance userTask = waitForUserTask("step", processInstance);
        getProcessAPI().updateActivityDataInstance(dataName, userTask.getId(), "2");
        assignAndExecuteStep(userTask, user.getId());
        waitForProcessToFinish(processInstance.getId());

        final ArchivedDataInstance archivedData = getProcessAPI().getArchivedActivityDataInstance(dataName, userTask.getId());
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
        final HumanTaskInstance userTask = waitForUserTask("step", processInstance);
        getProcessAPI().updateActivityDataInstance(dataName, userTask.getId(), "2");
        assignAndExecuteStep(userTask, user.getId());
        waitForProcessToFinish(processInstance.getId());

        List<ArchivedDataInstance> archivedDataInstances = getProcessAPI().getArchivedActivityDataInstances(userTask.getId(), 0, 10);
        assertEquals(3, archivedDataInstances.size());
        ArchivedDataInstance archivedDataInstance = getArchivedDataInstance(archivedDataInstances, dataName);
        assertEquals("2", archivedDataInstance.getValue());
        archivedDataInstance = getArchivedDataInstance(archivedDataInstances, "job");
        assertEquals("job", archivedDataInstance.getValue());
        archivedDataInstance = getArchivedDataInstance(archivedDataInstances, "desc");
        assertEquals("desc", archivedDataInstance.getValue());

        archivedDataInstances = getProcessAPI().getArchivedActivityDataInstances(userTask.getId(), 0, 1);
        assertEquals(1, archivedDataInstances.size());
        archivedDataInstance = getArchivedDataInstance(archivedDataInstances, dataName);
        assertEquals("2", archivedDataInstance.getValue());

        archivedDataInstances = getProcessAPI().getArchivedActivityDataInstances(userTask.getId(), 1, 10);
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
        final HumanTaskInstance userTask = waitForUserTask("step", processInstance);

        try {
            getProcessAPI().getArchivedProcessDataInstance("o", userTask.getId());
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

}
