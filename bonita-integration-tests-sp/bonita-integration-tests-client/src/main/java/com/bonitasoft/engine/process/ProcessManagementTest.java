/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ArchivedManualTaskInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ManualTaskInstance;
import org.bonitasoft.engine.bpm.flownode.TaskPriority;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceCriterion;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.bonitasoft.engine.BuildTestUtilSP;
import com.bonitasoft.engine.CommonAPISPIT;
import com.bonitasoft.engine.api.ProcessAPI;
import com.bonitasoft.engine.bpm.flownode.ManualTaskCreator;
import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;

public class ProcessManagementTest extends CommonAPISPIT {

    private User john;

    @After
    public void afterTest() throws BonitaException {
        deleteUser(john);
        logoutOnTenant();
    }

    @Before
    public void beforeTest() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        john = createUser(USERNAME, PASSWORD);
        logoutOnTenant();
        loginOnDefaultTenantWith(USERNAME, PASSWORD);
    }

    @Test
    public void searchArchivedSubTasks() throws Exception {
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        final String userTaskName = "userTask";
        processBuilder.addActor(ACTOR_NAME).addDescription("test process with archived sub tasks").addUserTask(userTaskName, ACTOR_NAME);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(john.getId(), processDefinition.getId());

        // make userTask1 as parentTask
        final ActivityInstance parentTask = waitForUserTaskAndAssigneIt(processInstance, userTaskName, john);
        // add sub task
        final Date dueDate = new Date(System.currentTimeMillis());
        final TaskPriority newPriority = TaskPriority.ABOVE_NORMAL;
        ManualTaskCreator taskCreator = buildManualTaskCreator(parentTask.getId(), "newManualTask1", john.getId(), "add new manual user task 1", dueDate,
                newPriority);
        final ManualTaskInstance manualUserTask1 = getProcessAPI().addManualUserTask(taskCreator);

        taskCreator = buildManualTaskCreator(parentTask.getId(), "newManualTask2", john.getId(), "add new manual user task 2", dueDate, newPriority);
        final ManualTaskInstance manualUserTask2 = getProcessAPI().addManualUserTask(taskCreator);
        assertEquals("add new manual user task 1", manualUserTask1.getDisplayDescription());
        assertEquals(dueDate, manualUserTask2.getExpectedEndDate());
        assertEquals(newPriority, manualUserTask2.getPriority());

        final FlowNodeInstance newManualTask1 = waitForFlowNodeInReadyState(processInstance, "newManualTask1", true);
        final FlowNodeInstance newManualTask2 = waitForFlowNodeInReadyState(processInstance, "newManualTask2", true);

        // let's archive children tasks:
        skipTask(newManualTask1.getId());
        waitForFlowNodeInState(processInstance, "newManualTask1", TestStates.SKIPPED, true);
        skipTask(newManualTask2.getId());
        waitForFlowNodeInState(processInstance, "newManualTask2", TestStates.SKIPPED, true);

        getProcessAPI().executeFlowNode(parentTask.getId());
        waitForProcessToFinish(processInstance);

        // filter the sub tasks
        final SearchOptionsBuilder searchhBuilder = new SearchOptionsBuilder(0, 10);
        searchhBuilder.filter(ArchivedHumanTaskInstanceSearchDescriptor.PARENT_ACTIVITY_INSTANCE_ID, parentTask.getId());
        searchhBuilder.sort(ArchivedHumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        final SearchResult<ArchivedHumanTaskInstance> searchResult = getProcessAPI().searchArchivedHumanTasks(searchhBuilder.done());
        assertEquals(2, searchResult.getCount());
        final List<ArchivedHumanTaskInstance> subTasks = searchResult.getResult();

        final ArchivedHumanTaskInstance aTask1 = subTasks.get(0);
        assertEquals("newManualTask1", aTask1.getName());
        assertTrue(aTask1 instanceof ArchivedManualTaskInstance);

        final ArchivedHumanTaskInstance aTask2 = subTasks.get(1);
        assertEquals("newManualTask2", aTask2.getName());
        assertTrue(aTask2 instanceof ArchivedManualTaskInstance);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void searchSubTasks() throws Exception {
        // create user and process
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME).addDescription("test process with sub task").addUserTask("userTask0", ACTOR_NAME)
                .addUserTask("userTask1", ACTOR_NAME);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, john);

        final User jack = createUser("jack", "bpm");

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance, "userTask0");
        final HumanTaskInstance userTask1 = waitForUserTaskAndAssigneIt(processInstance, "userTask1", john);

        // add sub task
        final Date dueDate = new Date(System.currentTimeMillis());
        ManualTaskCreator taskCreator = buildManualTaskCreator(userTask1.getId(), "newTask'1", jack.getId(), "add new manual user task", dueDate,
                TaskPriority.HIGHEST);
        getProcessAPI().addManualUserTask(taskCreator);

        taskCreator = buildManualTaskCreator(userTask1.getId(), "newTask'2", john.getId(), "add new manual user task", dueDate, TaskPriority.LOWEST);
        getProcessAPI().addManualUserTask(taskCreator);

        waitForUserTask(processInstance, "newTask'1");
        waitForUserTask(processInstance, "newTask'2");

        assertEquals(2, getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null).size());
        assertEquals(1, getProcessAPI().getAssignedHumanTaskInstances(jack.getId(), 0, 10, null).size());

        // filter the sub tasks
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(HumanTaskInstanceSearchDescriptor.PARENT_ACTIVITY_INSTANCE_ID, userTask1.getId());
        builder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        final SearchResult<HumanTaskInstance> searchResult = getProcessAPI().searchHumanTaskInstances(builder.done());
        assertNotNull(searchResult);
        assertEquals(2, searchResult.getCount());
        final List<HumanTaskInstance> subTasks = searchResult.getResult();
        final HumanTaskInstance subTask1 = subTasks.get(0);
        assertEquals("newTask'1", subTask1.getName());
        assertEquals(TaskPriority.HIGHEST, subTask1.getPriority());
        final HumanTaskInstance subTask2 = subTasks.get(1);
        assertEquals("newTask'2", subTask2.getName());
        assertEquals(TaskPriority.LOWEST, subTask2.getPriority());
        assertTrue(subTask1 instanceof ManualTaskInstance);

        disableAndDeleteProcess(processDefinition);
        deleteUser(jack.getUserName());
    }

    @Test
    public void executingSubTasks() throws Exception {
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME).addDescription("test process with archived sub tasks").addUserTask("userTask", ACTOR_NAME);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, john);

        final ProcessInstance processInstance = getProcessAPI().startProcess(john.getId(), processDefinition.getId());
        final HumanTaskInstance parentTask = waitForUserTaskAndAssigneIt(processInstance, "userTask", john);

        // add sub task
        final Date dueDate = new Date(System.currentTimeMillis());
        ManualTaskCreator taskCreator = buildManualTaskCreator(parentTask.getId(), "newManualTask1", john.getId(), "add new manual user task", dueDate,
                TaskPriority.NORMAL);
        final ManualTaskInstance manualUserTask1 = getProcessAPI().addManualUserTask(taskCreator);

        taskCreator = buildManualTaskCreator(parentTask.getId(), "newManualTask2", john.getId(), "add new manual user task", dueDate,
                TaskPriority.ABOVE_NORMAL);
        final ManualTaskInstance manualUserTask2 = getProcessAPI().addManualUserTask(taskCreator);

        waitForUserTask(processInstance, "newManualTask1");
        waitForUserTask(processInstance, "newManualTask2");

        // archive sub task:
        // archive first children tasks:
        getProcessAPI().executeFlowNode(manualUserTask1.getId());
        waitForFlowNodeInCompletedState(processInstance, "newManualTask1", true);

        getProcessAPI().executeFlowNode(manualUserTask2.getId());
        waitForFlowNodeInCompletedState(processInstance, "newManualTask2", true);

        getProcessAPI().executeFlowNode(parentTask.getId());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void createHumanTaskAndExecutingItWithOtherUser() throws Exception {
        final User userWhoCreateTheManualTask = createUser("userWhoCreateTheManualTask", PASSWORD);

        logoutOnTenant();
        loginOnDefaultTenantWith("userWhoCreateTheManualTask", PASSWORD);

        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        final String userTaskName = "userTask";
        processBuilder.addActor(ACTOR_NAME).addDescription("test process with archived sub tasks").addUserTask(userTaskName, ACTOR_NAME);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, userWhoCreateTheManualTask);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance parentTask = waitForUserTaskAndAssigneIt(processInstance, userTaskName, userWhoCreateTheManualTask);

        // Add sub task
        final ManualTaskCreator taskCreator = buildManualTaskCreator(parentTask.getId(), "newManualTask1", john.getId(), "add new manual user task",
                new Date(System.currentTimeMillis()), TaskPriority.NORMAL);
        final ManualTaskInstance manualUserTask = getProcessAPI().addManualUserTask(taskCreator);

        logoutOnTenant();
        loginOnDefaultTenantWith(USERNAME, PASSWORD);

        // archive sub task:
        // archive first children tasks:
        getProcessAPI().executeFlowNode(manualUserTask.getId());

        getProcessAPI().executeFlowNode(parentTask.getId());
        waitForProcessToFinish(processInstance);

        final ArchivedActivityInstance archivedActivityInstance = getProcessAPI().getArchivedActivityInstance(manualUserTask.getId());
        assertEquals(john.getId(), archivedActivityInstance.getExecutedBy());
        assertEquals(john.getId(), archivedActivityInstance.getExecutedBySubstitute());

        // Clean up
        disableAndDeleteProcess(processDefinition);
        deleteUser(userWhoCreateTheManualTask);
    }

    @Test
    public void getProcessInstancesWithLabelOnStringIndex() throws Exception {
        ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("1" + PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME);
        processBuilder.addUserTask("step1", ACTOR_NAME);
        processBuilder.setStringIndex(1, "Label1", null);
        processBuilder.setStringIndex(2, "Label2", null);
        processBuilder.setStringIndex(3, "Label3", null);
        processBuilder.setStringIndex(4, "Label4", null);
        processBuilder.setStringIndex(5, "Label5", null);
        final ProcessDefinition process1 = deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, john);
        processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("2" + PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME);
        processBuilder.addUserTask("step1", ACTOR_NAME);
        processBuilder.setStringIndex(1, "LabelBis1", null);
        processBuilder.setStringIndex(2, "LabelBis2", null);
        processBuilder.setStringIndex(3, "LabelBis3", null);
        processBuilder.setStringIndex(4, "LabelBis4", null);
        processBuilder.setStringIndex(5, "LabelBis5", null);
        final ProcessDefinition process2 = deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, john);
        startProcessAndWaitForTask(process1.getId(), "step1");
        startProcessAndWaitForTask(process2.getId(), "step1");
        final List<ProcessInstance> processInstances = getProcessAPI().getProcessInstances(0, 10, ProcessInstanceCriterion.NAME_ASC);
        assertEquals(2, processInstances.size());
        ProcessInstance processInstance = processInstances.get(0);
        assertEquals("1" + PROCESS_NAME, processInstance.getName());
        assertEquals("Label1", processInstance.getStringIndexLabel(1));
        assertEquals("Label2", processInstance.getStringIndexLabel(2));
        assertEquals("Label3", processInstance.getStringIndexLabel(3));
        assertEquals("Label4", processInstance.getStringIndexLabel(4));
        assertEquals("Label5", processInstance.getStringIndexLabel(5));
        processInstance = processInstances.get(1);
        assertEquals("2" + PROCESS_NAME, processInstance.getName());
        assertEquals("LabelBis1", processInstance.getStringIndexLabel(1));
        assertEquals("LabelBis2", processInstance.getStringIndexLabel(2));
        assertEquals("LabelBis3", processInstance.getStringIndexLabel(3));
        assertEquals("LabelBis4", processInstance.getStringIndexLabel(4));
        assertEquals("LabelBis5", processInstance.getStringIndexLabel(5));
        disableAndDeleteProcess(process1);
        disableAndDeleteProcess(process2);
    }

    @Test
    public void getProcessInstancesWithStringIndex() throws Exception {
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("1" + PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME);
        processBuilder.addUserTask("step1", ACTOR_NAME);
        final ExpressionBuilder expressionBuilder = new ExpressionBuilder();
        processBuilder.setStringIndex(1, "Label1", expressionBuilder.createConstantStringExpression("Value1"));
        processBuilder.setStringIndex(2, "Label2", expressionBuilder.createGroovyScriptExpression("script", "return \"a\"+\"b\";", String.class.getName()));
        processBuilder.setStringIndex(3, "Label3", null);
        final ProcessDefinition process1 = deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, john);
        final ProcessInstance processInstance = startProcessAndWaitForTask(process1.getId(), "step1").getProcessInstance();
        assertEquals("Label1", processInstance.getStringIndexLabel(1));
        assertEquals("Label2", processInstance.getStringIndexLabel(2));
        assertEquals("Label3", processInstance.getStringIndexLabel(3));
        assertEquals(null, processInstance.getStringIndexLabel(4));
        assertEquals(null, processInstance.getStringIndexLabel(5));
        assertEquals("Value1", processInstance.getStringIndex1());
        assertEquals("ab", processInstance.getStringIndex2());
        assertEquals(null, processInstance.getStringIndex3());
        assertEquals(null, processInstance.getStringIndex4());
        assertEquals(null, processInstance.getStringIndex5());

        disableAndDeleteProcess(process1);
    }

    @Ignore("wait for fix BS-9946")
    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.SUB_TASK, jira = "BS-2735", keywords = { "Sub-task", "Human task", "Skipped" })
    @Test
    public void skipHumanTaskShouldAbortSubtasks() throws Exception {
        final ProcessDefinitionBuilderExt processBuilder = BuildTestUtilSP
                .buildProcessDefinitionWithFailedConnectorOnUserTask("testArchiveTaskShouldArchiveSubtasks");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActorAndTestConnectorThatThrowException(processBuilder, ACTOR_NAME, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(john.getId(), processDefinition.getId());
        final ActivityInstance parentTask = waitForUserTaskAndAssigneIt(processInstance, "StepWithFailedConnector", john);

        // add sub task
        final ManualTaskCreator taskCreator = new ManualTaskCreator(parentTask.getId(), "newManualTask1").setAssignTo(john.getId());
        final ManualTaskInstance manualUserTask = getProcessAPI().addManualUserTask(taskCreator);
        waitForFlowNodeInReadyState(processInstance, manualUserTask.getName(), true);

        // Execute parent task to failed it
        assignAndExecuteStep(parentTask, john.getId());
        waitForFlowNodeInFailedState(processInstance);
        skipTask(parentTask.getId());
        waitForUserTask("Step2");

        // One tasks
        checkNbOfOpenActivities(processInstance, 1);

        waitForFlowNodeInState(processInstance, parentTask.getName(), TestStates.SKIPPED, true);
        waitForFlowNodeInState(processInstance, manualUserTask.getName(), TestStates.ABORTED, true);

        disableAndDeleteProcess(processDefinition);
    }

}
