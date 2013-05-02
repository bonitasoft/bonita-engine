/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.process;

import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.bpm.model.HumanTaskInstance;
import org.bonitasoft.engine.bpm.model.ManualTaskInstance;
import org.bonitasoft.engine.bpm.model.ProcessDefinition;
import org.bonitasoft.engine.bpm.model.ProcessInstance;
import org.bonitasoft.engine.bpm.model.TaskPriority;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.ObjectDeletionException;
import org.bonitasoft.engine.exception.ObjectNotFoundException;
import org.bonitasoft.engine.exception.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.bonitasoft.engine.test.check.CheckNbAssignedTaskOf;
import org.bonitasoft.engine.test.check.CheckNbPendingTaskOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.bpm.model.ProcessDefinitionBuilderExt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TaskOnDemandTest extends CommonAPISPTest {

    // FIXME add test that there is no pending task if the parent task is finished

    private User john;

    private User jack;

    @Before
    public void setUp() throws Exception {
        login();
        john = createUser("john", "secretPassword");
        jack = createUser("jack", "bpm");
    }

    @After
    public void tearDown() throws Exception {
        deleteUser(john.getId());
        deleteUser(jack.getId());
        logout();
    }

    @Test
    public void createManualTaskOnManualTask() throws Exception {
        final String password = "secretPassword";
        final String delivery = "Delivery men";

        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("ProcessWithTaskOnDemand", "1.0");
        processBuilder.addActor(delivery).addDescription("kikoo lol").addAutomaticTask("toHaveIdGreaterThan1").addUserTask("userTask1", delivery);
        final ProcessDefinition processDefinition = deployAndEnableWithActor(processBuilder.done(), delivery, john);

        logout();
        loginWith(john.getUserName(), password);

        getProcessAPI().startProcess(processDefinition.getId());
        final CheckNbPendingTaskOf checkNbPendingTaskOf = new CheckNbPendingTaskOf(getProcessAPI(), 30, 3000, true, 1, john);
        assertTrue("no new activity found", checkNbPendingTaskOf.waitUntil());

        final List<HumanTaskInstance> pendingTasks = checkNbPendingTaskOf.getPendingHumanTaskInstances();
        final HumanTaskInstance parentTask = pendingTasks.get(0);
        getProcessAPI().assignUserTask(parentTask.getId(), john.getId());
        final List<HumanTaskInstance> toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, toDoTasks.size());
        assertEquals("userTask1", toDoTasks.get(0).getName());

        final Date dueDate = new Date(System.currentTimeMillis());
        getProcessAPI().addManualUserTask(parentTask.getId(), "newTask1", "newTask1", jack.getId(), "add new manual user task", dueDate, TaskPriority.NORMAL);
        final ManualTaskInstance m2 = getProcessAPI().addManualUserTask(parentTask.getId(), "newTask2", "newTask2", john.getId(), "add new manual user task",
                dueDate, TaskPriority.NORMAL);
        // can we properly add a sub task of a sub task?:
        final ManualTaskInstance m21 = getProcessAPI().addManualUserTask(m2.getId(), "newTask21", "newTask21", jack.getId(),
                "add new manual user task child of newTask2", dueDate, TaskPriority.NORMAL);
        assertTrue("m11.getParentContainerId() should not be 1", 1 != m21.getParentContainerId());
        assertEquals(m2.getId(), m21.getParentContainerId());

        disableAndDelete(processDefinition);
    }

    @Test
    public void executeManualTaskWithChildManualTask() throws Exception {
        final String password = "secretPassword";
        final String delivery = "Delivery men";

        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("ProcessWithTaskOnDemandAndChildTask", "1.0");
        processBuilder.addActor(delivery).addDescription("kikoo lol").addAutomaticTask("toHaveIdGreaterThan1").addUserTask("userTask1", delivery);
        final ProcessDefinition processDefinition = deployAndEnableWithActor(processBuilder.done(), delivery, john);

        logout();
        loginWith(john.getUserName(), password);

        final ProcessInstance pInstance = getProcessAPI().startProcess(processDefinition.getId());
        final CheckNbPendingTaskOf checkNbPendingTaskOf = new CheckNbPendingTaskOf(getProcessAPI(), 30, 3000, true, 1, john);
        assertTrue("no new activity found", checkNbPendingTaskOf.waitUntil());

        final List<HumanTaskInstance> pendingTasks = checkNbPendingTaskOf.getPendingHumanTaskInstances();
        final HumanTaskInstance parentTask = pendingTasks.get(0);
        getProcessAPI().assignUserTask(parentTask.getId(), john.getId());

        final Date dueDate = new Date(System.currentTimeMillis() + 1200000L);
        getProcessAPI().addManualUserTask(parentTask.getId(), "newTask1", "newTask1", jack.getId(), "add new manual user task", dueDate, TaskPriority.NORMAL);
        final ManualTaskInstance m2 = getProcessAPI().addManualUserTask(parentTask.getId(), "newTask2", "newTask2", john.getId(), "add new manual user task",
                dueDate, TaskPriority.NORMAL);
        // can we properly add a sub task of a sub task?:
        final ManualTaskInstance m21 = getProcessAPI().addManualUserTask(m2.getId(), "newTask21", "newTask21", jack.getId(),
                "add new manual user task child of newTask2", dueDate, TaskPriority.NORMAL);
        assertTrue("m11.getParentContainerId() should not be 1", 1 != m21.getParentContainerId());
        assertEquals(m2.getId(), m21.getParentContainerId());

        assertEquals(2, getProcessAPI().getAssignedHumanTaskInstances(jack.getId(), 0, 10, null).size());
        getProcessAPI().executeActivity(m2.getId());
        waitForFinalArchivedActivity("newTask2", pInstance);

        // m21 should be canceled, so only newTask1 should remain among the tasks assigned to Jack
        assertEquals("newTask21 should be canceled since its parent task has been executed", 1,
                getProcessAPI().getAssignedHumanTaskInstances(jack.getId(), 0, 10, null).size());

        disableAndDelete(processDefinition);
    }

    @Cover(classes = { ManualTaskInstance.class }, concept = BPMNConcept.SUB_TASK, keywords = { "add subtask" })
    @Test
    public void createUserTaskOnParent() throws Exception {
        final String password = "secretPassword";
        final String delivery = "Delivery men";

        final ProcessDefinition processDefinition = deployAndEnableSimpleProcess(delivery);

        logout();
        loginWith(john.getUserName(), password);

        getProcessAPI().startProcess(processDefinition.getId());
        final List<HumanTaskInstance> pendingTasks = waitForPendingTasks(john.getId(), 1);
        final HumanTaskInstance parentTask = pendingTasks.get(0);
        getProcessAPI().assignUserTask(parentTask.getId(), john.getId());
        final List<HumanTaskInstance> toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, toDoTasks.size());
        assertEquals("userTask1", toDoTasks.get(0).getName());

        final Date dueDate = new Date(System.currentTimeMillis());
        ManualTaskInstance m1 = getProcessAPI().addManualUserTask(parentTask.getId(), "newTask1", "New task 1", jack.getId(), "add new manual user task",
                dueDate, TaskPriority.NORMAL);
        final ManualTaskInstance m2 = getProcessAPI().addManualUserTask(parentTask.getId(), "newTask2", "newTask2", john.getId(), "add new manual user task",
                dueDate, TaskPriority.NORMAL);

        assertEquals("New task 1", m1.getDisplayName());
        assertEquals("add new manual user task", m1.getDisplayDescription());
        assertEquals("add new manual user task", m1.getDescription());
        m1 = (ManualTaskInstance) getProcessAPI().getActivityInstance(m1.getId());
        assertEquals("New task 1", m1.getDisplayName());
        assertEquals("add new manual user task", m1.getDisplayDescription());
        assertEquals("add new manual user task", m1.getDescription());
        assertEquals(dueDate, m2.getExpectedEndDate());

        final CheckNbAssignedTaskOf checkNbAssignedTaskOfJohn = new CheckNbAssignedTaskOf(getProcessAPI(), 50, 5000, true, 2, john);
        assertTrue("no new activity found", checkNbAssignedTaskOfJohn.waitUntil());
        final CheckNbAssignedTaskOf checkNbAssignedTaskOfJack = new CheckNbAssignedTaskOf(getProcessAPI(), 50, 5000, true, 1, jack);
        assertTrue("no new activity found", checkNbAssignedTaskOfJack.waitUntil());
        final List<HumanTaskInstance> toDoTasks2 = checkNbAssignedTaskOfJohn.getAssingnedHumanTaskInstances();
        HumanTaskInstance humanTaskInstance;
        if ("newTask2".equals(toDoTasks2.get(0).getName())) {
            humanTaskInstance = toDoTasks2.get(0);
        } else {
            humanTaskInstance = toDoTasks2.get(1);
        }
        assertTrue("newTask2".equals(humanTaskInstance.getName()));
        assertTrue(humanTaskInstance instanceof ManualTaskInstance);
        final HumanTaskInstance userTaskInstance = checkNbAssignedTaskOfJack.getAssingnedHumanTaskInstances().get(0);
        assertEquals("newTask1", userTaskInstance.getName());
        disableAndDelete(processDefinition);
    }

    @Cover(classes = { ManualTaskInstance.class }, concept = BPMNConcept.SUB_TASK, jira = "ENGINE-650", keywords = { "delete subtask" })
    @Test
    public void deleteManualTask() throws Exception {
        final String password = "secretPassword";
        final String delivery = "Delivery men";

        final ProcessDefinition processDefinition = deployAndEnableSimpleProcess(delivery);

        logout();
        loginWith(john.getUserName(), password);

        getProcessAPI().startProcess(processDefinition.getId());
        final List<HumanTaskInstance> pendingTasks = waitForPendingTasks(john.getId(), 1);
        final HumanTaskInstance parentTask = pendingTasks.get(0);
        getProcessAPI().assignUserTask(parentTask.getId(), john.getId());
        final List<HumanTaskInstance> toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, toDoTasks.size());
        assertEquals("userTask1", toDoTasks.get(0).getName());

        final Date dueDate = new Date(System.currentTimeMillis());
        final ManualTaskInstance subtask = getProcessAPI().addManualUserTask(parentTask.getId(), "newTask2", "newTask2", john.getId(),
                "add new manual user task", dueDate, TaskPriority.NORMAL);
        CheckNbAssignedTaskOf checkNbAssignedTaskOfJohn = new CheckNbAssignedTaskOf(getProcessAPI(), 50, 5000, true, 2, john);
        assertTrue("no new activity found", checkNbAssignedTaskOfJohn.waitUntil());
        getProcessAPI().deleteManualUserTask(subtask.getId());
        checkNbAssignedTaskOfJohn = new CheckNbAssignedTaskOf(getProcessAPI(), 50, 5000, true, 1, john);
        assertTrue("no new activity found", checkNbAssignedTaskOfJohn.waitUntil());
        assertEquals("userTask1", checkNbAssignedTaskOfJohn.getAssingnedHumanTaskInstances().get(0).getName());
        disableAndDelete(processDefinition);
    }

    @Cover(classes = { ManualTaskInstance.class }, concept = BPMNConcept.SUB_TASK, jira = "ENGINE-650", keywords = { "delete subtask" })
    @Test(expected = ObjectDeletionException.class)
    public void deleteNotManualTask() throws Exception {
        final String password = "secretPassword";
        final String delivery = "Delivery men";
        final ProcessDefinition processDefinition = deployAndEnableSimpleProcess(delivery);
        logout();
        loginWith(john.getUserName(), password);
        getProcessAPI().startProcess(processDefinition.getId());
        final List<HumanTaskInstance> pendingTasks = waitForPendingTasks(john.getId(), 1);
        final HumanTaskInstance parentTask = pendingTasks.get(0);
        getProcessAPI().assignUserTask(parentTask.getId(), john.getId());
        final List<HumanTaskInstance> toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);
        final HumanTaskInstance humanTaskInstance = toDoTasks.get(0);
        try {
            getProcessAPI().deleteManualUserTask(humanTaskInstance.getId());
        } finally {
            disableAndDelete(processDefinition);
        }
    }

    @Cover(classes = { ManualTaskInstance.class }, concept = BPMNConcept.SUB_TASK, jira = "ENGINE-650", keywords = { "delete subtask" })
    @Test(expected = ObjectNotFoundException.class)
    public void deleteUnexistingManualTask() throws Exception {
        getProcessAPI().deleteManualUserTask(123);
    }

    private ProcessDefinition deployAndEnableSimpleProcess(final String actorName) throws BonitaException, InvalidProcessDefinitionException {
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("ProcessWithTaskOnDemand", "1.0");
        processBuilder.addActor(actorName).addDescription("kikoo lol").addUserTask("userTask1", actorName);
        final ProcessDefinition processDefinition = deployAndEnableWithActor(processBuilder.done(), actorName, john);
        return processDefinition;
    }

    @Test
    public void checkAssignedDateOfASubTask() throws Exception {
        final String password = "secretPassword";
        final String delivery = "Delivery men";

        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("ProcessWithTaskOnDemand", "1.0");
        processBuilder.addActor(delivery).addDescription("kikoo lol").addAutomaticTask("toHaveIdGreaterThan1").addUserTask("userTask1", delivery);
        final ProcessDefinition processDefinition = deployAndEnableWithActor(processBuilder.done(), delivery, john);

        logout();
        loginWith(john.getUserName(), password);

        getProcessAPI().startProcess(processDefinition.getId());
        final CheckNbPendingTaskOf checkNbPendingTaskOf = new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, true, 1, john);
        assertTrue("no new activity found", checkNbPendingTaskOf.waitUntil());

        final List<HumanTaskInstance> pendingTasks = checkNbPendingTaskOf.getPendingHumanTaskInstances();
        final HumanTaskInstance parentTask = pendingTasks.get(0);
        getProcessAPI().assignUserTask(parentTask.getId(), john.getId());
        final List<HumanTaskInstance> toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, toDoTasks.size());
        assertEquals("userTask1", toDoTasks.get(0).getName());

        final Date dueDate = new Date(System.currentTimeMillis());
        final ManualTaskInstance manualUserTask = getProcessAPI().addManualUserTask(parentTask.getId(), "newTask1", "newTask1", john.getId(),
                "add new manual user task", dueDate, TaskPriority.NORMAL);
        final Date claimedDate = manualUserTask.getClaimedDate();
        assertNotNull(claimedDate);

        disableAndDelete(processDefinition);
    }

}
