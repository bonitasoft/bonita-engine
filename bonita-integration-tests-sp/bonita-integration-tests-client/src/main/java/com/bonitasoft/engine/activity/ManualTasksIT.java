/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.activity;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ManualTaskInstance;
import org.bonitasoft.engine.bpm.flownode.TaskPriority;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.connectors.VariableStorage;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.bonitasoft.engine.test.TestStates;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPIT;
import com.bonitasoft.engine.bpm.flownode.ManualTaskCreator;
import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;

public class ManualTasksIT extends CommonAPISPIT {

    private User user;

    @Before
    public void beforeTest() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        user = createUser(USERNAME, "bpm");
        logoutOnTenant();
        loginOnDefaultTenantWith(USERNAME, "bpm");
    }

    @After
    public void afterTest() throws BonitaException {
        deleteUser(user);
        VariableStorage.clearAll();
        logoutOnTenant();
    }

    private ProcessDefinition deployProcessWithUserTask(final User user1) throws Exception {
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("firstProcess", "1.0");
        processBuilder.addActor("myActor");
        processBuilder.addUserTask("Request", "myActor");
        return deployAndEnableProcessWithActor(processBuilder.done(), "myActor", user1);
    }

    @Test(expected = UpdateException.class)
    public void unableToReleaseManualTask() throws Exception {
        final User user = createUser("login1", "password");
        final ProcessDefinition processDefinition = deployProcessWithUserTask(user);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        final long taskId = waitForUserTask(startProcess, "Request");
        loginOnDefaultTenantWithDefaultTechnicalUser();
        loginOnDefaultTenantWith("login1", "password");
        getProcessAPI().assignUserTask(taskId, user.getId());

        final ManualTaskCreator taskCreator = buildManualTaskCreator(taskId, "subtask", user.getId(), "desk", new Date(), TaskPriority.NORMAL);
        final ManualTaskInstance manualUserTask = getProcessAPI().addManualUserTask(taskCreator);
        try {
            getProcessAPI().releaseUserTask(manualUserTask.getId());
        } finally {
            disableAndDeleteProcess(processDefinition);
            deleteUser(user);
        }
    }

    @Test
    public void executeTaskShouldAbortSubtasks() throws Exception {
        final String userTaskName = "userTask";
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList(userTaskName, "step2"),
                Arrays.asList(true, true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(user.getId(), processDefinition.getId());
        final ActivityInstance parentTask = waitForUserTaskAndAssigneIt(processInstance, userTaskName, user);

        // add sub task
        final ManualTaskCreator taskCreator = buildManualTaskCreator(parentTask.getId(), "newManualTask1", user.getId(), "add new manual user task 1",
                new Date(System.currentTimeMillis()), TaskPriority.ABOVE_NORMAL);
        getProcessAPI().addManualUserTask(taskCreator);
        waitForFlowNodeInReadyState(processInstance, "newManualTask1", true);

        assignAndExecuteStep(parentTask, user.getId());

        waitForFlowNodeInState(processInstance, parentTask.getName(), TestStates.NORMAL_FINAL, true);
        waitForFlowNodeInState(processInstance, "newManualTask1", TestStates.ABORTED, true);
        waitForUserTask(processInstance, "step2");

        // Verify if the user task is archived
        final SearchOptionsBuilder searchOptionsBuilder2 = new SearchOptionsBuilder(0, 20);
        searchOptionsBuilder2.sort(ArchivedActivityInstanceSearchDescriptor.NAME, Order.ASC);
        searchOptionsBuilder2.filter(ArchivedActivityInstanceSearchDescriptor.NAME, userTaskName);
        searchOptionsBuilder2.filter(ArchivedActivityInstanceSearchDescriptor.STATE_NAME, TestStates.NORMAL_FINAL.getStateName());
        final List<ArchivedActivityInstance> result2 = getProcessAPI().searchArchivedActivities(searchOptionsBuilder2.done()).getResult();
        assertEquals(1, result2.size());

        // Verify if the manual task is archived
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 20);
        searchOptionsBuilder.sort(ArchivedActivityInstanceSearchDescriptor.NAME, Order.ASC);
        searchOptionsBuilder.filter(ArchivedActivityInstanceSearchDescriptor.NAME, "newManualTask1");
        searchOptionsBuilder.filter(ArchivedActivityInstanceSearchDescriptor.STATE_NAME, TestStates.ABORTED.getStateName());
        final List<ArchivedActivityInstance> result = getProcessAPI().searchArchivedActivities(searchOptionsBuilder.done()).getResult();
        assertEquals(1, result.size());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    @Ignore("BS-9946")
    public void skipTaskShouldAbortSubtasks() throws Exception {
        final String userTaskName = "userTask";
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList(userTaskName, "step2"), Arrays.asList(true, true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(user.getId(), processDefinition.getId());
        final ActivityInstance parentTask = waitForUserTaskAndAssigneIt(processInstance, userTaskName, user);

        // add sub task
        final ManualTaskCreator taskCreator = buildManualTaskCreator(parentTask.getId(), "newManualTask1", user.getId(), "add new manual user task 1",
                new Date(System.currentTimeMillis()), TaskPriority.ABOVE_NORMAL);
        getProcessAPI().addManualUserTask(taskCreator);
        waitForFlowNodeInReadyState(processInstance, "newManualTask1", true);

        skipTask(parentTask.getId());
        waitForFlowNodeInState(processInstance, parentTask.getName(), TestStates.SKIPPED, true);
        waitForFlowNodeInState(processInstance, "newManualTask1", TestStates.ABORTED, true);
        waitForUserTaskAndExecuteIt(processInstance, "step2", user);
        waitForProcessToFinish(processInstance);

        // Verify if the user task is archived
        final SearchOptionsBuilder searchOptionsBuilder2 = new SearchOptionsBuilder(0, 20);
        searchOptionsBuilder2.sort(ArchivedActivityInstanceSearchDescriptor.NAME, Order.ASC);
        searchOptionsBuilder2.filter(ArchivedActivityInstanceSearchDescriptor.NAME, userTaskName);
        searchOptionsBuilder2.filter(ArchivedActivityInstanceSearchDescriptor.STATE_NAME, TestStates.SKIPPED.getStateName());
        final List<ArchivedActivityInstance> result2 = getProcessAPI().searchArchivedActivities(searchOptionsBuilder2.done()).getResult();
        assertEquals(1, result2.size());

        // Verify if the manual task is archived
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 20);
        searchOptionsBuilder.sort(ArchivedActivityInstanceSearchDescriptor.NAME, Order.ASC);
        searchOptionsBuilder.filter(ArchivedActivityInstanceSearchDescriptor.NAME, "newManualTask1");
        searchOptionsBuilder.filter(ArchivedActivityInstanceSearchDescriptor.STATE_NAME, TestStates.ABORTED.getStateName());
        final List<ArchivedActivityInstance> result = getProcessAPI().searchArchivedActivities(searchOptionsBuilder.done()).getResult();
        assertEquals(1, result.size());

        disableAndDeleteProcess(processDefinition);
    }

}
