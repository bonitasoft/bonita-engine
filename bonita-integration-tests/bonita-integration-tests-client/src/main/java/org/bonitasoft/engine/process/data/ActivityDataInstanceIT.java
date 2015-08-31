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

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.data.ArchivedDataInstance;
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
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.bonitasoft.engine.test.EngineInitializerAPIFactoryImpl;
import org.bonitasoft.engine.test.PlatformStarter;
import org.bonitasoft.engine.test.ReachedDataInstance;
import org.bonitasoft.engine.test.StartedProcess;
import org.bonitasoft.engine.test.UserTaskAPI;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ActivityDataInstanceIT /* extends TestWithUser */{

    protected User user;
    protected UserTaskAPI userTaskAPI;
    protected EngineInitializerAPIFactoryImpl engineInitializer = new EngineInitializerAPIFactoryImpl();
    public static final String DEFAULT_TECHNICAL_LOGGER_USERNAME = "install";

    public static final String DEFAULT_TECHNICAL_LOGGER_PASSWORD = "install";

    APITestUtil apiTestUtil = new APITestUtil();
    String ACTOR_NAME = "william.jobs";

    @Before
    public void before() throws Exception {
        PlatformStarter platformStarter = new PlatformStarter();
        platformStarter.startEngine();
        userTaskAPI = engineInitializer.getUserTaskAPI(
                TenantAPIAccessor.getLoginAPI().login(DEFAULT_TECHNICAL_LOGGER_USERNAME, DEFAULT_TECHNICAL_LOGGER_PASSWORD));
        engineInitializer.createProcessAPIandIdentityAPIfromSession(TenantAPIAccessor.getLoginAPI().login(DEFAULT_TECHNICAL_LOGGER_USERNAME,
                DEFAULT_TECHNICAL_LOGGER_PASSWORD));
        user = apiTestUtil.createUser("william.jobs", "bpm");
    }

    @After
    public void after() throws BonitaException, InterruptedException, IllegalAccessException, NoSuchMethodException, InvocationTargetException,
            ClassNotFoundException {
        PlatformStarter platformStarter = new PlatformStarter();
        platformStarter.stopEngine();
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
        final ProcessDefinition processDefinition = apiTestUtil.deployAndEnableProcessWithActor(processDef, actorName, user);
        final ProcessDeploymentInfo processDeploymentInfo = apiTestUtil.getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        // test execution
        StartedProcess process = engineInitializer.startProcess(processDeploymentInfo.getProcessId());
        process.waitForUserTask("step1").hasOnlyActivityDataInstance("var1").hasValue(expectedNumber);

        apiTestUtil.disableAndDeleteProcess(processDeploymentInfo.getProcessId());
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
        final ProcessDefinition processDefinition = apiTestUtil.deployAndEnableProcessWithActor(addUserTask.getProcess(), ACTOR_NAME, user);
        StartedProcess process = engineInitializer.startProcess(processDefinition.getId());
        process.waitForUserTask("step1").hasOnlyActivityDataInstance("var1").hasValue(defaultDataValue);
        process.updateActivityDataInstance("var1", updatedDataValue).hasOnlyActivityDataInstance("var1").hasValue(updatedDataValue);
        apiTestUtil.disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void updateActivityInstanceOperationsWithStringDataNotTransient() throws Exception {
        final DesignProcessDefinition designProcessDefinition = createProcessWithActorAndHumanTaskAndInitStringDataNotTransient();
        final ProcessDefinition processDefinition = apiTestUtil.deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = apiTestUtil.getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());
        StartedProcess process = engineInitializer.startProcess(processDefinition.getId());
        process.waitForUserTask("step1");
        // Update data instance
        final String updatedValue = "afterUpdate";
        final Operation stringOperation = BuildTestUtil.buildStringOperation("dataName", updatedValue, false);
        final List<Operation> operations = new ArrayList<Operation>();
        operations.add(stringOperation);
        process.getProcessAPI().updateActivityInstanceVariables(operations, process.getTemporaryUserTaskId(), null);
        new ReachedDataInstance(process.getProcessAPI().getActivityDataInstances(process.getTemporaryUserTaskId(), 0, 10).get(0)).hasValue(updatedValue);
        // Clean
        apiTestUtil.disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void updateActivityInstanceVariable() throws Exception {
        final DesignProcessDefinition processDef = createProcessWithActorAndHumanTaskAndInitStringDataNotTransient();
        final ProcessDefinition processDefinition = apiTestUtil.deployAndEnableProcessWithActor(processDef, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = apiTestUtil.getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());
        StartedProcess process = engineInitializer.startProcess(processDefinition.getId());
        process.waitForUserTask("step1");
        final String updatedValue = "afterUpdate";
        final Map<String, Serializable> variables = new HashMap<String, Serializable>(2);
        variables.put("dataName", updatedValue);
        process.updateActivityInstanceVariables(variables).hasValue(updatedValue);
        apiTestUtil.disableAndDeleteProcess(processDefinition);
    }

    @Test(expected = UpdateException.class)
    public void cannotUpdateAnActivityInstanceVariable() throws Exception {
        final DesignProcessDefinition processDef = createProcessWithActorAndHumanTaskAndInitStringDataNotTransient();
        final ProcessDefinition processDefinition = apiTestUtil.deployAndEnableProcessWithActor(processDef, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = apiTestUtil.getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());
        final ProcessInstance processInstance = apiTestUtil.getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
        final long activityInstanceId = apiTestUtil.waitForUserTask(processInstance, "step1");
        final Map<String, Serializable> variables = new HashMap<String, Serializable>(2);
        variables.put("dataName1", "afterUpdate");
        try {
            apiTestUtil.getProcessAPI().updateActivityInstanceVariables(activityInstanceId, variables);
        } finally {
            apiTestUtil.disableAndDeleteProcess(processDefinition);
        }
    }

    private DataInstance getActivityDataInstance(final long activityInstanceId) {
        final List<DataInstance> activityDataInstances = apiTestUtil.getProcessAPI().getActivityDataInstances(activityInstanceId, 0, 10);
        assertEquals(1, activityDataInstances.size());
        return activityDataInstances.get(0);
    }

    @Test(expected = RetrieveException.class)
    public void dataNotAvailableAfterArchiveFromActivity() throws Exception {
        final DesignProcessDefinition processDef = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0").addActor(ACTOR_NAME)
                .addDescription("Delivery all day and night long").addUserTask("step1", ACTOR_NAME)
                .addIntegerData("var1", new ExpressionBuilder().createConstantIntegerExpression(1)).getProcess();
        final ProcessDefinition processDefinition = apiTestUtil.deployAndEnableProcessWithActor(processDef, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = apiTestUtil.getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        // test execution
        final ProcessInstance processInstance = apiTestUtil.getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
        StartedProcess process = engineInitializer.startProcess(processDefinition.getId());
        process.waitForUserTask("step1").hasOnlyActivityDataInstance("var1").hasValue(1);

        // verify the retrieved data
        List<DataInstance> processDataInstances = apiTestUtil.getProcessAPI().getActivityDataInstances(process.getTemporaryUserTaskId(), 0, 10);
        // Execute pending task
        final List<ActivityInstance> activities = apiTestUtil.getProcessAPI().getActivities(processInstance.getId(), 0, 200);
        final ActivityInstance activityInstance = activities.iterator().next();
        apiTestUtil.assignAndExecuteStep(activityInstance, user.getId());
        userTaskAPI.waitForProcessToFinish(processInstance.getId(), -1);

        // retrieve data after process has finished
        try {
            processDataInstances = apiTestUtil.getProcessAPI().getActivityDataInstances(processInstance.getId(), 0, 10);
        } finally {
            apiTestUtil.disableAndDeleteProcess(processDefinition);
        }
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.DATA, keywords = { "3 Process instances", "Data", "Boolean", "Transition", "Condition",
            "Default", "Update" }, jira = "ENGINE-1459")
    @Test
    public void executeProcess3TimesWithNotInitializedData() throws Exception {
        final Expression dataExpr = new ExpressionBuilder().createDataExpression("booleanData", Boolean.class.getName());
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(apiTestUtil.PROCESS_NAME, apiTestUtil.PROCESS_VERSION);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME).addBooleanData("booleanData", null);
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME);
        processDefinitionBuilder.addTransition("step1", "step2", dataExpr).addDefaultTransition("step1", "step3");
        final ProcessDefinition processDefinition = apiTestUtil.deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, user);

        // Start first process, and wait the first step
        final ProcessInstance processInstance1 = apiTestUtil.getProcessAPI().startProcess(processDefinition.getId());
        final Long step1_1Id = apiTestUtil.waitForUserTask(processInstance1, "step1");
        // Set data to true, for the first instance
        apiTestUtil.getProcessAPI().updateActivityDataInstance("booleanData", step1_1Id, true);

        // Start second process, and wait the first step
        final ProcessInstance processInstance2 = apiTestUtil.getProcessAPI().startProcess(processDefinition.getId());
        final Long step1_2Id = apiTestUtil.waitForUserTask(processInstance2, "step1");
        // Set data to false, for the second instance
        apiTestUtil.getProcessAPI().updateActivityDataInstance("booleanData", step1_2Id, false);

        // Start third process, and wait the first step
        final ProcessInstance processInstance3 = apiTestUtil.getProcessAPI().startProcess(processDefinition.getId());
        final Long step1_3Id = apiTestUtil.waitForUserTask(processInstance3, "step1");
        // Set data to true, for the third instance
        apiTestUtil.getProcessAPI().updateActivityDataInstance("booleanData", step1_3Id, true);

        // Execute all step1
        apiTestUtil.assignAndExecuteStep(step1_1Id, user);
        apiTestUtil.waitForUserTask(processInstance1, "step2");
        apiTestUtil.assignAndExecuteStep(step1_2Id, user);
        apiTestUtil.waitForUserTask(processInstance2, "step3");
        apiTestUtil.assignAndExecuteStep(step1_3Id, user);
        apiTestUtil.waitForUserTask(processInstance3, "step2");

        // Check that only these 3 steps are pending
        assertEquals(3, apiTestUtil.getProcessAPI().getNumberOfPendingHumanTaskInstances(user.getId()));

        // Clean-up
        apiTestUtil.disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void runProcessWithInvalidInitialValue() throws Exception {
        final ProcessDefinitionBuilder createNewInstance = new ProcessDefinitionBuilder().createNewInstance("processwithIntegerData", "1.0");
        createNewInstance.addAutomaticTask("step1").addIntegerData("intdata",
                new ExpressionBuilder().createExpression("d", "d", Integer.class.getName(), ExpressionType.TYPE_CONSTANT));

        final ProcessDefinition processDefinition = apiTestUtil.deployAndEnableProcess(createNewInstance.done());
        StartedProcess process = engineInitializer.startProcess(processDefinition.getId());
        process.waitForTaskToFail().hasSameNameAs("step1");
        apiTestUtil.disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = DataInstance.class, concept = BPMNConcept.DATA, jira = "ENGINE-679", keywords = { "data", "container" }, story = "process with data having same name in different container")
    @Test
    public void processWithDataHavingSameNameInTwoContainer() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(apiTestUtil.PROCESS_NAME, apiTestUtil.PROCESS_VERSION);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME).addShortTextData("a", new ExpressionBuilder().createConstantStringExpression("step1"));
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME).addShortTextData("a", new ExpressionBuilder().createConstantStringExpression("step2"));
        processDefinitionBuilder.addShortTextData("a", new ExpressionBuilder().createConstantStringExpression("process"));
        final DesignProcessDefinition designProcessDefinition = processDefinitionBuilder.done();
        final ProcessDefinition processDefinition = apiTestUtil.deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = apiTestUtil.getProcessAPI().startProcess(processDefinition.getId());
        StartedProcess process = engineInitializer.startProcess(processDefinition.getId());
        process.waitForUserTask("step1").hasSameNameAs(
                (String) userTaskAPI.getProcessAPI().getActivityDataInstance("a", process.getTemporaryUserTaskId()).getValue());
        process.waitForUserTask("step2").hasSameNameAs(
                (String) userTaskAPI.getProcessAPI().getActivityDataInstance("a", process.getTemporaryUserTaskId()).getValue());
        process.accessDataInTask("a").hasValue("process");
        apiTestUtil.disableAndDeleteProcess(processDefinition);
    }

    private DesignProcessDefinition createProcessWithActorAndHumanTaskAndInitStringDataNotTransient() throws Exception {
        return createProcessWithActorAndHumanTaskAndStringData(new ExpressionBuilder().createConstantStringExpression("beforeUpdate"), false).done();
    }

    private ProcessDefinitionBuilder createProcessWithActorAndHumanTaskAndStringData(final Expression defaultValue, final boolean isTransient) {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(apiTestUtil.PROCESS_NAME, apiTestUtil.PROCESS_VERSION);
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
        final ProcessInstance processInstance = apiTestUtil.getProcessAPI().startProcess(processDefinition.getId());
        final Long userTaskId = apiTestUtil.waitForUserTask(processInstance, userTaskName);

        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<Expression, Map<String, Serializable>>(2);
        final Expression persistedVariableExpression = new ExpressionBuilder().createDataExpression("persistedVariable", String.class.getName());
        final Expression transientVariableExpression = new ExpressionBuilder().createTransientDataExpression("transientVariable", String.class.getName());
        expressions.put(persistedVariableExpression, (Map<String, Serializable>) null);
        expressions.put(transientVariableExpression, (Map<String, Serializable>) null);

        final Map<String, Serializable> expressionResult = apiTestUtil.getProcessAPI().evaluateExpressionsOnActivityInstance(userTaskId, expressions);
        assertEquals("default", expressionResult.get(persistedVariableExpression.getName()));
        assertEquals("default", expressionResult.get(transientVariableExpression.getName()));

        apiTestUtil.disableAndDeleteProcess(processDefinition.getId());
    }

    @Cover(jira = "ENGINE-1821", classes = { ArchivedDataInstance.class, ProcessAPI.class }, concept = BPMNConcept.DATA, keywords = { "last archived data",
            "activity instance" })
    @Test
    public void getArchivedActivityDataInstance() throws Exception {
        final String dataName = "title";
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessToArchive", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("step", ACTOR_NAME).addShortTextData(dataName, new ExpressionBuilder().createConstantStringExpression("1"));

        final ProcessDefinition processDefinition = apiTestUtil.deployAndEnableProcessWithActor(builder.getProcess(), ACTOR_NAME, user);
        StartedProcess process = engineInitializer.startProcess(processDefinition.getId());
        process.waitForUserTask("step");
        process.updateActivityDataInstance(dataName, "2").hasOnlyActivityDataInstance(dataName).hasValue(
                process.getProcessAPI().getArchivedActivityDataInstance(dataName, process.getTemporaryUserTaskId()).getValue());
        apiTestUtil.disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1821", classes = { ArchivedDataInstance.class, ProcessAPI.class }, concept = BPMNConcept.DATA, keywords = { "last archived data",
            "activity instance" })
    @Test
    public void getArchivedActivityDataInstanceFromAnArchivedProcess() throws Exception {
        final String dataName = "title";
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessToArchive", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("step", ACTOR_NAME).addShortTextData(dataName, new ExpressionBuilder().createConstantStringExpression("1"));

        final ProcessDefinition processDefinition = apiTestUtil.deployAndEnableProcessWithActor(builder.getProcess(), ACTOR_NAME, user);
        StartedProcess process = engineInitializer.startProcess(processDefinition.getId());
        process.waitForUserTask("step");
        process.updateActivityDataInstance(dataName, "2").assignAndExecuteTask(user.getId());
        userTaskAPI.waitForProcessToFinish(process.getProcessInstance().getId(), -1);
        process.getArchivedData(dataName).hasValue("2");
        apiTestUtil.disableAndDeleteProcess(processDefinition);
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
        final ProcessDefinition processDefinition = apiTestUtil.deployAndEnableProcessWithActor(builder.getProcess(), ACTOR_NAME, user);
        String[] dataNames = new String[] { "2", "job", "desc" };
        StartedProcess process = engineInitializer.startProcess(processDefinition.getId());
        process.waitForUserTask("step");
        process.updateActivityDataInstance(dataName, "2").assignAndExecuteTask(user.getId());
        userTaskAPI.waitForProcessToFinish(process.getProcessInstance().getId(), -1);
        process.getArchivedDatas(0, 10).hasSize(3).containsValues(dataNames);
        process.getArchivedDatas(0, 1).hasSize(1).containsValue("2");
        process.getArchivedDatas(1, 10).hasSize(2).containsValue("job").containsValue("desc");
        apiTestUtil.disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1821", classes = { ArchivedDataInstance.class, ProcessAPI.class }, concept = BPMNConcept.DATA, keywords = { "last archived data",
            "activity instance" })
    @Test
    public void getUnknownArchivedActivityDataInstance() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessToArchive", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("step", ACTOR_NAME);

        final ProcessDefinition processDefinition = apiTestUtil.deployAndEnableProcessWithActor(builder.getProcess(), ACTOR_NAME, user);
        StartedProcess process = engineInitializer.startProcess(processDefinition.getId());
        process.waitForUserTask("step");
        try {
            process.getArchivedData("o");
            fail("The data named 'o' does not exists");
        } catch (final ArchivedDataNotFoundException dnfe) {
            // Do nothing
        } finally {
            apiTestUtil.disableAndDeleteProcess(processDefinition);
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

        return apiTestUtil.deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.DATA, jira = "BS-1984", keywords = { "update", "activity data", "wrong type" })
    @Test
    public void cantUpdateActivityDataInstanceWithWrongValue() throws Exception {
        final DesignProcessDefinition processDef = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0").addActor(ACTOR_NAME)
                .addDescription("Delivery all day and night long")
                .addUserTask("step1", ACTOR_NAME).addData("data", List.class.getName(), null).getProcess();
        final ProcessDefinition processDefinition = apiTestUtil.deployAndEnableProcessWithActor(processDef, ACTOR_NAME, user);
        StartedProcess process = engineInitializer.startProcess(processDefinition.getId());
        process.waitForUserTask("step1");
        // test execution
        // verify the retrieved data
        try {
            process.updateActivityDataInstance("data", "wrong value");
            fail();
        } catch (final UpdateException e) {
            assertEquals("USERNAME=" + DEFAULT_TECHNICAL_LOGGER_USERNAME + " | DATA_NAME=data | DATA_CLASS_NAME=java.util.List | The type of new value ["
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

        apiTestUtil.disableAndDeleteProcess(processDefinition);
    }

}
