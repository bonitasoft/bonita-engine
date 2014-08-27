package org.bonitasoft.engine.process.data;

import static org.bonitasoft.engine.matchers.NameMatcher.nameIs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.AutomaticTaskDefinitionBuilder;
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
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.bonitasoft.engine.test.check.CheckNbOfActivities;
import org.bonitasoft.engine.test.wait.WaitForStep;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DataInstanceIntegrationTest extends CommonAPITest {

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
    public void getIntegerDataInstanceFromProcess() throws Exception {
        final String className = Integer.class.getName();
        final ProcessDefinition processDefinition = operateProcess(user, "var1", new ExpressionBuilder().createConstantIntegerExpression(1), className);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());

        final List<DataInstance> processDataInstances = getProcessAPI().getProcessDataInstances(processInstance.getId(), 0, 10);
        assertFalse(processDataInstances.isEmpty());
        assertEquals(1, processDataInstances.size());
        assertEquals("var1", processDataInstances.get(0).getName());
        assertEquals(1, processDataInstances.get(0).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getShortTextDataInstanceFromProcess() throws Exception {
        final String className = String.class.getName();
        final ProcessDefinition processDefinition = operateProcess(user, "var1", new ExpressionBuilder().createConstantStringExpression("aaa"), className);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());

        final List<DataInstance> processDataInstances = getProcessAPI().getProcessDataInstances(processInstance.getId(), 0, 10);
        assertFalse(processDataInstances.isEmpty());
        assertEquals(1, processDataInstances.size());
        assertEquals("var1", processDataInstances.get(0).getName());
        assertEquals("aaa", processDataInstances.get(0).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void processWithShortAndLongTextData() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("processWithlongAndshortText", "1.0");
        builder.addUserTask("step1", "actor");
        builder.addActor("actor");
        final String shortTextValue = "shortTextValue";
        builder.addShortTextData("shortTextData", new ExpressionBuilder().createConstantStringExpression(shortTextValue));
        final String longTextValue = "longTextValue";
        final StringBuilder longBuilder = new StringBuilder(longTextValue);
        for (int i = 0; i < 10; i++) {
            longBuilder.append(longTextValue);
        }
        builder.addLongTextData("longTextData", new ExpressionBuilder().createConstantStringExpression(longBuilder.toString()));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), "actor", user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        assertEquals(shortTextValue, getProcessAPI().getProcessDataInstance("shortTextData", processInstance.getId()).getValue());
        assertEquals(longBuilder.toString(), getProcessAPI().getProcessDataInstance("longTextData", processInstance.getId()).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getDoubleDataInstanceFromProcess() throws Exception {
        final String className = Double.class.getName();
        final ProcessDefinition processDefinition = operateProcess(user, "var1", new ExpressionBuilder().createConstantDoubleExpression(1.5), className);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());

        final List<DataInstance> processDataInstances = getProcessAPI().getProcessDataInstances(processInstance.getId(), 0, 10);
        assertFalse(processDataInstances.isEmpty());
        assertEquals(1, processDataInstances.size());
        assertEquals("var1", processDataInstances.get(0).getName());
        assertEquals(1.5, processDataInstances.get(0).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { ProcessAPI.class, DataInstance.class }, concept = BPMNConcept.PROCESS, keywords = { "Float", "DataInstance" }, jira = "ENGINE-563")
    @Test
    public void getFloatDataInstanceFromProcess() throws Exception {
        final String className = Float.class.getName();
        final ProcessDefinition processDefinition = operateProcess(user, "var1", new ExpressionBuilder().createConstantFloatExpression(1.5f), className);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());

        final List<DataInstance> processDataInstances = getProcessAPI().getProcessDataInstances(processInstance.getId(), 0, 10);
        assertFalse(processDataInstances.isEmpty());
        assertEquals(1, processDataInstances.size());
        assertEquals("var1", processDataInstances.get(0).getName());
        assertEquals(1.5f, processDataInstances.get(0).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getBooleanDataInstanceFromProcess() throws Exception {
        final String className = Boolean.class.getName();
        final ProcessDefinition processDefinition = operateProcess(user, "var1", new ExpressionBuilder().createConstantBooleanExpression(true), className);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());

        final List<DataInstance> processDataInstances = getProcessAPI().getProcessDataInstances(processInstance.getId(), 0, 10);
        assertFalse(processDataInstances.isEmpty());
        assertEquals(1, processDataInstances.size());
        assertEquals("var1", processDataInstances.get(0).getName());
        assertEquals(true, processDataInstances.get(0).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { Date.class, DataInstance.class, ProcessAPI.class }, concept = BPMNConcept.EXPRESSIONS, keywords = { "Date", "Data", "Expression" }, jira = "ENGINE-1559, ENGINE-1099")
    @Test
    public void getDateDataInstanceFromProcess() throws Exception {
        final ProcessDefinition processDefinition = operateProcess(user, "var1",
                new ExpressionBuilder().createConstantDateExpression("2013-07-18T14:49:26.86+02:00"), Date.class.getName());
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());

        final List<DataInstance> processDataInstances = getProcessAPI().getProcessDataInstances(processInstance.getId(), 0, 10);
        assertFalse(processDataInstances.isEmpty());
        assertEquals(1, processDataInstances.size());
        assertEquals("var1", processDataInstances.get(0).getName());
        assertNotNull(processDataInstances.get(0).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getLongDataInstanceFromProcess() throws Exception {
        final String className = Long.class.getName();
        final ProcessDefinition processDefinition = operateProcess(user, "var1", new ExpressionBuilder().createConstantLongExpression(1), className);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());

        final List<DataInstance> processDataInstances = getProcessAPI().getProcessDataInstances(processInstance.getId(), 0, 10);
        assertFalse(processDataInstances.isEmpty());
        assertEquals(1, processDataInstances.size());
        assertEquals("var1", processDataInstances.get(0).getName());
        assertEquals(1L, processDataInstances.get(0).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    private ProcessDefinition operateProcess(final User user, final String dataName, final Expression expression, final String className) throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0");
        if (className.equals(Integer.class.getName())) {
            processDefinitionBuilder.addIntegerData(dataName, expression);
        } else if (className.equals(Long.class.getName())) {
            processDefinitionBuilder.addLongData(dataName, expression);
        } else if (className.equals(Double.class.getName())) {
            processDefinitionBuilder.addDoubleData(dataName, expression);
        } else if (className.equals(Float.class.getName())) {
            processDefinitionBuilder.addFloatData(dataName, expression);
        } else if (className.equals(Boolean.class.getName())) {
            processDefinitionBuilder.addBooleanData(dataName, expression);
        } else if (className.equals(Date.class.getName())) {
            processDefinitionBuilder.addDateData(dataName, expression);
        } else if (className.equals(String.class.getName())) {
            processDefinitionBuilder.addShortTextData(dataName, expression);
        }
        final DesignProcessDefinition processDef = processDefinitionBuilder.addActor(delivery).addDescription("Delivery all day and night long")
                .addUserTask("step1", delivery).getProcess();

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDef, delivery, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        return processDefinition;
    }

    @Test
    public void updateProcessDataInstance() throws Exception {
        final String delivery = "Delivery men";
        final DesignProcessDefinition processDef = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0").addActor(delivery)
                .addDescription("Delivery all day and night long").addIntegerData("var1", new ExpressionBuilder().createConstantIntegerExpression(1))
                .addUserTask("step1", delivery).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDef, delivery, user);

        // test execution
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        // verify the retrieved data
        List<DataInstance> processDataInstances = getProcessAPI().getProcessDataInstances(processInstance.getId(), 0, 10);
        assertTrue(!processDataInstances.isEmpty());
        assertEquals(1, processDataInstances.size());
        assertEquals("var1", processDataInstances.get(0).getName());
        assertEquals(1, processDataInstances.get(0).getValue());

        getProcessAPI().updateProcessDataInstance("var1", processInstance.getId(), 2);

        // retrieve data after the update
        processDataInstances = getProcessAPI().getProcessDataInstances(processInstance.getId(), 0, 10);
        assertTrue(!processDataInstances.isEmpty());
        assertEquals(1, processDataInstances.size());
        assertEquals(2, processDataInstances.get(0).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void updateProcessDataInstanceTwice() throws Exception {
        final String delivery = "Delivery men";
        final DesignProcessDefinition processDef = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0").addActor(delivery)
                .addDescription("Delivery all day and night long").addIntegerData("var1", new ExpressionBuilder().createConstantIntegerExpression(1))
                .addUserTask("step1", delivery).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDef, delivery, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        // test execution
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());

        // verify the retrieved data
        List<DataInstance> processDataInstances = getProcessAPI().getProcessDataInstances(processInstance.getId(), 0, 10);
        assertTrue(!processDataInstances.isEmpty());
        assertEquals(1, processDataInstances.size());
        assertEquals("var1", processDataInstances.get(0).getName());
        assertEquals(1, processDataInstances.get(0).getValue());

        getProcessAPI().updateProcessDataInstance("var1", processInstance.getId(), 2);

        // retrieve data after the update
        processDataInstances = getProcessAPI().getProcessDataInstances(processInstance.getId(), 0, 10);
        assertTrue(!processDataInstances.isEmpty());
        assertEquals(1, processDataInstances.size());
        assertEquals(2, processDataInstances.get(0).getValue());

        getProcessAPI().updateProcessDataInstance("var1", processInstance.getId(), 3);

        // retrieve data after the update
        processDataInstances = getProcessAPI().getProcessDataInstances(processInstance.getId(), 0, 10);
        assertTrue(!processDataInstances.isEmpty());
        assertEquals(1, processDataInstances.size());
        assertEquals(3, processDataInstances.get(0).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Test(expected = RetrieveException.class)
    public void dataNotAvailableAfterArchiveFromProcess() throws Exception {
        final String delivery = "Delivery men";
        final DesignProcessDefinition processDef = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0").addActor(delivery)
                .addDescription("Delivery all day and night long").addIntegerData("var1", new ExpressionBuilder().createConstantIntegerExpression(1))
                .addUserTask("step1", delivery).getProcess();

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDef, delivery, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        // test execution
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());

        // verify the retrieved data
        List<DataInstance> processDataInstances = getProcessAPI().getProcessDataInstances(processInstance.getId(), 0, 10);
        assertTrue(!processDataInstances.isEmpty());
        assertEquals(1, processDataInstances.size());
        assertEquals("var1", processDataInstances.get(0).getName());
        assertEquals(1, processDataInstances.get(0).getValue());

        // Execute pending task
        assertTrue("expected an activity", new CheckNbOfActivities(getProcessAPI(), 20, 1000, true, processInstance, 1, TestStates.getReadyState()).waitUntil());
        final List<ActivityInstance> activities = getProcessAPI().getActivities(processInstance.getId(), 0, 200);
        final ActivityInstance activityInstance = activities.get(0);
        assignAndExecuteStep(activityInstance, user.getId());
        assertTrue("process was not completed", waitForProcessToFinishAndBeArchived(processInstance));

        // retrieve data after process has finished
        try {
            processDataInstances = getProcessAPI().getProcessDataInstances(processInstance.getId(), 0, 10);
        } finally {
            disableAndDeleteProcess(processDefinition);
        }
    }

    @Test
    public void getDataFromActivity() throws Exception {
        final String delivery = "Delivery men";
        final DesignProcessDefinition processDef = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0").addActor(delivery)
                .addDescription("Delivery all day and night long").addUserTask("step1", delivery)
                .addIntegerData("var1", new ExpressionBuilder().createConstantIntegerExpression(1)).getProcess();

        assertDataOnActivityIs(delivery, processDef, 1);
    }

    @Test
    public void getDataFromActivityThatIsAlsoInParent() throws Exception {
        final String delivery = "Delivery men";
        final DesignProcessDefinition processDef = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.1").addActor(delivery)
                .addDescription("Delivery all day and night long").addIntegerData("var1", new ExpressionBuilder().createConstantIntegerExpression(1))
                .addUserTask("step1", delivery).addIntegerData("var1", new ExpressionBuilder().createConstantIntegerExpression(2)).getProcess();

        assertDataOnActivityIs(delivery, processDef, 2);
    }

    @Test
    public void getDataFromActivityThatIsOnlyInParent() throws Exception {
        final String delivery = "Delivery men";
        final DesignProcessDefinition processDef = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.1").addActor(delivery)
                .addDescription("Delivery all day and night long").addIntegerData("var1", new ExpressionBuilder().createConstantIntegerExpression(3))
                .addUserTask("step1", delivery).getProcess();

        assertDataOnActivityIs(delivery, processDef, 3);
    }

    private void assertDataOnActivityIs(final String actorName, final DesignProcessDefinition processDef, final int expectedNumber) throws Exception {
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDef, actorName, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        // test execution
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
        final WaitForStep waitForStep = waitForStep("step1", processInstance);

        // verify there are data
        final List<DataInstance> processDataInstances = getProcessAPI().getActivityDataInstances(waitForStep.getStepId(), 0, 10);
        assertTrue(!processDataInstances.isEmpty());
        assertEquals(1, processDataInstances.size());
        assertEquals("var1", processDataInstances.get(0).getName());
        assertEquals(expectedNumber, processDataInstances.get(0).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void updateIntegerActivityDataInstance() throws Exception {
        final int INT_CONSTANT = 1;
        final int UPDATED_INT_VALUE = 2;
        updateActivityDataInstance(INT_CONSTANT, UPDATED_INT_VALUE);
    }

    @Test
    public void updateBooleanActivityDataInstance() throws Exception {
        updateActivityDataInstance(false, true);
    }

    private void updateActivityDataInstance(final Serializable defaultDataValue, final Serializable updatedDataValue) throws Exception {
        final String delivery = "Delivery men";
        final UserTaskDefinitionBuilder addUserTask = createBaseProcess(delivery);
        if (defaultDataValue instanceof Boolean) {
            addUserTask.addBooleanData("var1", new ExpressionBuilder().createConstantBooleanExpression((Boolean) defaultDataValue));
        } else if (defaultDataValue instanceof Integer) {
            addUserTask.addIntegerData("var1", new ExpressionBuilder().createConstantIntegerExpression((Integer) defaultDataValue));
        } else {
            throw new Exception("This test does not support data type different from (boolean, integer)");
        }
        final DesignProcessDefinition processDef = addUserTask.getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDef, delivery, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final WaitForStep waitForStep = waitForStep("step1", processInstance);
        final long activityInstanceId = waitForStep.getStepId();
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
        final String updatedValue = "afterUpdate";

        final DesignProcessDefinition designProcessDefinition = createProcessWithActorAndHumanTaskAndInitStringDataNotTransient();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
        final long activityInstanceId = waitForUserTask("step1", processInstance).getId();

        // Update data instance
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
        final WaitForStep waitForStep = waitForStep("step1", processInstance);
        final long activityInstanceId = waitForStep.getStepId();

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
        waitForStep("step1", processInstance);

        final List<ActivityInstance> activityInstances = getProcessAPI().getActivities(processInstance.getId(), 0, 10);
        final ActivityInstance activityInstance = activityInstances.get(0);
        final long activityInstanceId = activityInstance.getId();
        final String updatedValue = "afterUpdate";

        final Map<String, Serializable> variables = new HashMap<String, Serializable>(2);
        variables.put("dataName1", updatedValue);
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

    private UserTaskDefinitionBuilder createBaseProcess(final String delivery) {
        final UserTaskDefinitionBuilder addUserTask = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0").addActor(delivery)
                .addDescription("Delivery all day and night long").addUserTask("step1", delivery);
        return addUserTask;
    }

    @Test(expected = RetrieveException.class)
    public void dataNotAvailableAfterArchiveFromActivity() throws Exception {
        final String delivery = "Delivery men";
        final DesignProcessDefinition processDef = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0").addActor(delivery)
                .addDescription("Delivery all day and night long").addUserTask("step1", delivery)
                .addIntegerData("var1", new ExpressionBuilder().createConstantIntegerExpression(1)).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDef, delivery, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        // test execution
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
        final WaitForStep waitForStep = waitForStep("step1", processInstance);

        // verify the retrieved data
        List<DataInstance> processDataInstances = getProcessAPI().getActivityDataInstances(waitForStep.getStepId(), 0, 10);
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

    @Test
    public void getProcessDataDefinitions() throws Exception {
        final String PROCESS_NAME = "myProcessName";

        final String intDataName = "luckyNum";
        final Expression intDefaultExp = new ExpressionBuilder().createConstantIntegerExpression(10);
        final String strDataName = "luckyColor";
        final Expression strDefaultExp = new ExpressionBuilder().createConstantStringExpression("blue");

        final String actorName = "Actor1";

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilder.addActor(actorName);
        processDefinitionBuilder.addIntegerData(intDataName, intDefaultExp);
        processDefinitionBuilder.addShortTextData(strDataName, strDefaultExp);
        final DesignProcessDefinition designProcessDefinition = processDefinitionBuilder.done();

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, actorName, user);
        final List<DataDefinition> dataDefList = getProcessAPI().getProcessDataDefinitions(processDefinition.getId(), 0, 5);
        assertEquals(2, dataDefList.size());

        final DataDefinition dataDef1 = dataDefList.get(0);
        assertEquals(intDataName, dataDef1.getName());
        assertEquals(Integer.class.getName(), dataDef1.getClassName());
        // assertEquals(intDefaultExp,dataDef1.getDefaultValueExpression());

        final DataDefinition dataDef2 = dataDefList.get(1);
        assertEquals(strDataName, dataDef2.getName());
        assertEquals(String.class.getName(), dataDef2.getClassName());
        // assertEquals(strDefaultExp,dataDef2.getDefaultValueExpression());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getProcessDataDefinitionsPaginated() throws Exception {
        final String PROCESS_NAME = "myProcessName";

        final String intDataName = "luckyNum";
        final Expression intDefaultExp = new ExpressionBuilder().createConstantIntegerExpression(10);
        final String strDataName = "color";
        final Expression strDefaultExp = new ExpressionBuilder().createConstantStringExpression("blue");
        final String actorName = "Actor1";
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilder.addActor(actorName);
        processDefinitionBuilder.addIntegerData(intDataName, intDefaultExp);
        for (int i = 1; i <= 10; i++) {
            processDefinitionBuilder.addShortTextData(strDataName + i, strDefaultExp);
        }
        final DesignProcessDefinition designProcessDefinition = processDefinitionBuilder.done();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, actorName, user);
        List<DataDefinition> processDataDefinitions = getProcessAPI().getProcessDataDefinitions(processDefinition.getId(), 0, 5);
        assertEquals(5, processDataDefinitions.size());
        assertThat(processDataDefinitions.get(0), nameIs("luckyNum"));
        assertThat(processDataDefinitions.get(1), nameIs("color1"));
        assertThat(processDataDefinitions.get(2), nameIs("color2"));
        assertThat(processDataDefinitions.get(3), nameIs("color3"));
        assertThat(processDataDefinitions.get(4), nameIs("color4"));
        processDataDefinitions = getProcessAPI().getProcessDataDefinitions(processDefinition.getId(), 5, 5);
        assertEquals(5, processDataDefinitions.size());
        assertThat(processDataDefinitions.get(0), nameIs("color5"));
        assertThat(processDataDefinitions.get(1), nameIs("color6"));
        assertThat(processDataDefinitions.get(2), nameIs("color7"));
        assertThat(processDataDefinitions.get(3), nameIs("color8"));
        assertThat(processDataDefinitions.get(4), nameIs("color9"));
        processDataDefinitions = getProcessAPI().getProcessDataDefinitions(processDefinition.getId(), 10, 5);
        assertEquals(1, processDataDefinitions.size());
        assertThat(processDataDefinitions.get(0), nameIs("color10"));
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getDataDefinitionsHavingComplexeInitialValue() throws Exception {
        final String PROCESS_NAME = "myProcessName";

        final Expression scriptExpression = new ExpressionBuilder().createGroovyScriptExpression("a+b", "a+b", String.class.getName(),
                new ExpressionBuilder().createDataExpression("a", String.class.getName()),
                new ExpressionBuilder().createDataExpression("b", String.class.getName()));

        final String actorName = "Actor1";

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilder.addActor(actorName);
        processDefinitionBuilder.addShortTextData("a", new ExpressionBuilder().createConstantStringExpression("avalue"));
        processDefinitionBuilder.addShortTextData("b", new ExpressionBuilder().createConstantStringExpression("bvalue"));
        processDefinitionBuilder.addUserTask("step1", actorName).addData("myData", String.class.getName(), scriptExpression);
        final DesignProcessDefinition designProcessDefinition = processDefinitionBuilder.done();

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, actorName, user);
        final List<DataDefinition> dataDefList = getProcessAPI().getActivityDataDefinitions(processDefinition.getId(), "step1", 0, 10);
        assertEquals(1, dataDefList.size());
        assertEquals(2, dataDefList.get(0).getDefaultValueExpression().getDependencies().size());
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getNumberOfProcessDataDefinitions() throws Exception {
        final String PROCESS_NAME = "myProcessName";

        final String intDataName = "luckyNum";
        final Expression intDefaultExp = new ExpressionBuilder().createConstantIntegerExpression(10);
        final String strDataName = "luckyColor";
        final Expression strDefaultExp = new ExpressionBuilder().createConstantStringExpression("blue");

        final String actorName = "Actor1";

        final DesignProcessDefinition designProcessDefinition;
        // process level
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilder.addActor(actorName);
        processDefinitionBuilder.addIntegerData(intDataName, intDefaultExp);
        processDefinitionBuilder.addShortTextData(strDataName, strDefaultExp);

        designProcessDefinition = processDefinitionBuilder.done();

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, actorName, user);
        final int i = getProcessAPI().getNumberOfProcessDataDefinitions(processDefinition.getId());
        assertEquals(2, i);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getNumberOfActivityDataDefinitions() throws Exception {
        final String PROCESS_NAME = "myProcessName";

        final String intDataName = "luckyNum";
        final Expression intDefaultExp = new ExpressionBuilder().createConstantIntegerExpression(10);
        final String strDataName = "luckyColor";
        final Expression strDefaultExp = new ExpressionBuilder().createConstantStringExpression("blue");

        final String actorName = "Actor1";
        final String taskName = "autoTask1";

        final DesignProcessDefinition designProcessDefinition;
        // process level
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilder.addActor(actorName);
        // activity level
        final AutomaticTaskDefinitionBuilder activityDefinitionBuilder = processDefinitionBuilder.addAutomaticTask(taskName);
        activityDefinitionBuilder.addIntegerData(intDataName, intDefaultExp);
        activityDefinitionBuilder.addShortTextData(strDataName, strDefaultExp);

        designProcessDefinition = processDefinitionBuilder.done();

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, actorName, user);

        final int number = getProcessAPI().getNumberOfActivityDataDefinitions(processDefinition.getId(), taskName);
        assertEquals(2, number);

        disableAndDeleteProcess(processDefinition);
    }

    @Test(expected = ProcessDefinitionNotFoundException.class)
    public void getProcessDataDefinitionsWithException() throws Exception {
        final String PROCESS_NAME = "myProcessName";

        final String intDataName = "luckyNum";
        final Expression intDefaultExp = new ExpressionBuilder().createConstantIntegerExpression(10);
        final String strDataName = "luckyColor";
        final Expression strDefaultExp = new ExpressionBuilder().createConstantStringExpression("blue");

        final String actorName = "Actor1";

        final DesignProcessDefinition designProcessDefinition;
        // process level
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilder.addActor(actorName);
        processDefinitionBuilder.addIntegerData(intDataName, intDefaultExp);
        processDefinitionBuilder.addShortTextData(strDataName, strDefaultExp);

        designProcessDefinition = processDefinitionBuilder.done();

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, actorName, user);
        try {
            getProcessAPI().getProcessDataDefinitions(processDefinition.getId() + 1, 0, 5);
        } finally {
            disableAndDeleteProcess(processDefinition);
        }
    }

    @Test(expected = ActivityDefinitionNotFoundException.class)
    public void getActivityDataDefinitionsWithException() throws Exception {
        final String PROCESS_NAME = "myProcessName";

        final String intDataName = "luckyNum";
        final Expression intDefaultExp = new ExpressionBuilder().createConstantIntegerExpression(10);
        final String strDataName = "luckyColor";
        final Expression strDefaultExp = new ExpressionBuilder().createConstantStringExpression("blue");

        final String actorName = "Actor1";
        final String taskName = "autoTask1";

        final DesignProcessDefinition designProcessDefinition;
        // process level
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilder.addActor(actorName);
        // activity level
        final AutomaticTaskDefinitionBuilder activityDefinitionBuilder = processDefinitionBuilder.addAutomaticTask(taskName);
        activityDefinitionBuilder.addIntegerData(intDataName, intDefaultExp);
        activityDefinitionBuilder.addShortTextData(strDataName, strDefaultExp);

        designProcessDefinition = processDefinitionBuilder.done();

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, actorName, user);
        try {
            getProcessAPI().getActivityDataDefinitions(processDefinition.getId(), taskName + "qwer", 0, 5);
        } finally {
            disableAndDeleteProcess(processDefinition);
        }
    }

    @Test
    public void getActivityDataDefinitions() throws Exception {
        final String PROCESS_NAME = "myProcessName";

        final String intDataName = "luckyNum";
        final Expression intDefaultExp = new ExpressionBuilder().createConstantIntegerExpression(10);
        final String strDataName = "luckyColor";
        final Expression strDefaultExp = new ExpressionBuilder().createConstantStringExpression("blue");

        final String actorName = "Actor1";
        final String taskName = "autoTask1";

        final DesignProcessDefinition designProcessDefinition;
        // process level
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilder.addActor(actorName);
        // activity level
        final AutomaticTaskDefinitionBuilder activityDefinitionBuilder = processDefinitionBuilder.addAutomaticTask(taskName);
        activityDefinitionBuilder.addIntegerData(intDataName, intDefaultExp);
        activityDefinitionBuilder.addShortTextData(strDataName, strDefaultExp);

        designProcessDefinition = processDefinitionBuilder.done();

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, actorName, user);

        final List<DataDefinition> dataDefList = getProcessAPI().getActivityDataDefinitions(processDefinition.getId(), taskName, 0, 5);
        assertEquals(2, dataDefList.size());

        final DataDefinition dataDef1 = dataDefList.get(0);
        assertEquals(intDataName, dataDef1.getName());
        assertEquals(Integer.class.getName(), dataDef1.getClassName());
        // assertEquals(intDefaultExp,dataDef1.getDefaultValueExpression());

        final DataDefinition dataDef2 = dataDefList.get(1);
        assertEquals(strDataName, dataDef2.getName());
        assertEquals(String.class.getName(), dataDef2.getClassName());
        // assertEquals(strDefaultExp,dataDef2.getDefaultValueExpression());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getActivityDataDefinitionsPaginated() throws Exception {
        final String PROCESS_NAME = "myProcessName";

        final String intDataName = "luckyNum";
        final Expression intDefaultExp = new ExpressionBuilder().createConstantIntegerExpression(10);
        final String strDataName = "color";
        final Expression strDefaultExp = new ExpressionBuilder().createConstantStringExpression("blue");

        final String actorName = "Actor1";
        final String taskName = "autoTask1";

        final DesignProcessDefinition designProcessDefinition;
        // process level
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilder.addActor(actorName);
        // activity level
        final AutomaticTaskDefinitionBuilder activityDefinitionBuilder = processDefinitionBuilder.addAutomaticTask(taskName);
        activityDefinitionBuilder.addIntegerData(intDataName, intDefaultExp);
        for (int i = 1; i <= 10; i++) {
            activityDefinitionBuilder.addShortTextData(strDataName + i, strDefaultExp);
        }
        designProcessDefinition = processDefinitionBuilder.done();

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, actorName, user);
        List<DataDefinition> processDataDefinitions = getProcessAPI().getActivityDataDefinitions(processDefinition.getId(), taskName, 0, 5);
        assertEquals(5, processDataDefinitions.size());
        assertThat(processDataDefinitions.get(0), nameIs("luckyNum"));
        assertThat(processDataDefinitions.get(1), nameIs("color1"));
        assertThat(processDataDefinitions.get(2), nameIs("color2"));
        assertThat(processDataDefinitions.get(3), nameIs("color3"));
        assertThat(processDataDefinitions.get(4), nameIs("color4"));
        processDataDefinitions = getProcessAPI().getActivityDataDefinitions(processDefinition.getId(), taskName, 5, 5);
        assertEquals(5, processDataDefinitions.size());
        assertThat(processDataDefinitions.get(0), nameIs("color5"));
        assertThat(processDataDefinitions.get(1), nameIs("color6"));
        assertThat(processDataDefinitions.get(2), nameIs("color7"));
        assertThat(processDataDefinitions.get(3), nameIs("color8"));
        assertThat(processDataDefinitions.get(4), nameIs("color9"));
        processDataDefinitions = getProcessAPI().getActivityDataDefinitions(processDefinition.getId(), taskName, 10, 5);
        assertEquals(1, processDataDefinitions.size());
        assertThat(processDataDefinitions.get(0), nameIs("color10"));

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void executeProcessWithNotInitializedData() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilder.addAutomaticTask("step1");
        processDefinitionBuilder.addIntegerData("intdata", null);
        processDefinitionBuilder.addShortTextData("stringData", null);
        processDefinitionBuilder.addDateData("dateData", null);
        processDefinitionBuilder.addBlobData("blobData", null);
        processDefinitionBuilder.addBooleanData("booleanData", null);
        processDefinitionBuilder.addLongData("longData", null);
        processDefinitionBuilder.addDoubleData("doubleData", null);
        processDefinitionBuilder.addFloatData("floatData", null);
        processDefinitionBuilder.addXMLData("xmlData", null);
        processDefinitionBuilder.addData("javaData", "java.util.List", null);

        final DesignProcessDefinition designProcessDefinition = processDefinitionBuilder.done();

        final ProcessDefinition processDefinition = deployAndEnableProcess(designProcessDefinition);
        final List<DataDefinition> dataDefList = getProcessAPI().getProcessDataDefinitions(processDefinition.getId(), 0, 15);
        assertEquals(10, dataDefList.size());

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForProcessToFinish(processInstance);

        disableAndDeleteProcess(processDefinition);
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
        processDefinitionBuilder.addActor("actor");
        processDefinitionBuilder.addUserTask("step1", "actor").addShortTextData("a", new ExpressionBuilder().createConstantStringExpression("step1"));
        processDefinitionBuilder.addUserTask("step2", "actor").addShortTextData("a", new ExpressionBuilder().createConstantStringExpression("step2"));
        processDefinitionBuilder.addShortTextData("a", new ExpressionBuilder().createConstantStringExpression("process"));
        final DesignProcessDefinition designProcessDefinition = processDefinitionBuilder.done();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, "actor", user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final List<HumanTaskInstance> waitForPendingTasks = waitForPendingTasks(user.getId(), 2);
        for (final HumanTaskInstance humanTaskInstance : waitForPendingTasks) {
            assertEquals(humanTaskInstance.getName(), getProcessAPI().getActivityDataInstance("a", humanTaskInstance.getId()).getValue());
        }
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
    public void canGetDataInstanceWhenThereAreTranseintData() throws Exception {
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
