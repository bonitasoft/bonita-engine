package org.bonitasoft.engine.process.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.FlowNodeExecutionException;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.UserTaskInstance;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.connectors.VariableStorage;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.check.CheckNbAssignedTaskOf;
import org.bonitasoft.engine.test.check.CheckNbPendingTaskOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class UserTaskAssignationTest extends CommonAPITest {

    private static final String JOHN = "john";

    private static final String JACK = "jack";

    private User john;

    private User jack;

    @After
    public void afterTest() throws BonitaException {
        deleteUser(JOHN);
        deleteUser(JACK);
        VariableStorage.clearAll();
        logout();
    }

    @Before
    public void beforeTest() throws BonitaException {
        login();
        john = createUser(JOHN, "bpm");
        jack = createUser(JACK, "bpm");
        loginWith(JOHN, "bpm");
    }

    @Test
    public void getAssignedHumanTasksWithStartedState() throws Exception {
        final String actorName = "Commercial";
        final ProcessDefinition processDefinition = deployAndEnableSimpleProcess("getAssignedHumanTasksWithStartedState", "0.12", actorName, "Trade business");

        getProcessAPI().startProcess(processDefinition.getId());

        assertTrue(new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, false, 1, john).waitUntil());
        final List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        final UserTaskInstance pendingTask = (UserTaskInstance) pendingTasks.get(0);

        assignAndExecuteStep(pendingTask, john.getId());
        final List<HumanTaskInstance> toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);
        // Task is in STARTED state so should not be retrieved:
        assertEquals(0, toDoTasks.size());

        disableAndDeleteProcess(processDefinition);
    }

    @Test(expected = FlowNodeExecutionException.class)
    public void cannotExecuteUnassignedTask() throws Exception {
        final String actorName = "Commercial";
        final ProcessDefinition processDefinition = deployAndEnableSimpleProcess("simple process", "0.12", actorName, "Trade business");

        getProcessAPI().startProcess(processDefinition.getId());

        assertTrue(new CheckNbPendingTaskOf(getProcessAPI(), 50, 2000, false, 1, john).waitUntil());
        final List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, ActivityInstanceCriterion.NAME_ASC);
        final UserTaskInstance pendingTask = (UserTaskInstance) pendingTasks.get(0);

        try {
            // execute activity without assign it before, an exception is expected
            getProcessAPI().executeFlowNode(pendingTask.getId());
        } finally {
            disableAndDeleteProcess(processDefinition);
        }
    }

    @Test
    public void assignUserTask() throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinition processDefinition = deployAndEnableSimpleProcess("executeConnectorOnStartOfAnAutomaticActivity", "1.0", delivery,
                "Delivery all day and night long");

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        waitForUserTask("step2", processInstance);
        final List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        final UserTaskInstance pendingTask = (UserTaskInstance) pendingTasks.get(0);

        getProcessAPI().assignUserTask(pendingTask.getId(), john.getId());
        final List<HumanTaskInstance> toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, toDoTasks.size());
        assertEquals(john.getId(), toDoTasks.get(0).getAssigneeId());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void canAssignTask2Times() throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinition processDefinition = deployAndEnableSimpleProcess("executeConnectorOnStartOfAnAutomaticActivity", "1.0", delivery,
                "Delivery all day and night long");

        getProcessAPI().startProcess(processDefinition.getId());

        assertTrue(new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, false, 1, john).waitUntil());
        final List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        final UserTaskInstance pendingTask = (UserTaskInstance) pendingTasks.get(0);

        getProcessAPI().assignUserTask(pendingTask.getId(), john.getId());
        try {

            getProcessAPI().assignUserTask(pendingTask.getId(), john.getId());
        } finally {
            disableAndDeleteProcess(processDefinition);
        }

    }

    @Test
    public void assignUserTaskSeveralTimes() throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinition processDefinition = deployAndEnableSimpleProcess("test release user task", "1.0", delivery, "Delivery all day and night long");

        getProcessAPI().startProcess(processDefinition.getId());
        // before assign
        assertTrue(new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, false, 1, john).waitUntil());
        List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, pendingTasks.size());
        final HumanTaskInstance pendingTask = pendingTasks.get(0);
        // after assign
        getProcessAPI().assignUserTask(pendingTask.getId(), john.getId());
        List<HumanTaskInstance> toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, toDoTasks.size());
        assertEquals(john.getId(), toDoTasks.get(0).getAssigneeId());
        pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(0, pendingTasks.size());
        // after release
        getProcessAPI().releaseUserTask(toDoTasks.get(0).getId());
        toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(0, toDoTasks.size());
        pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, pendingTasks.size());
        assertEquals(0, pendingTasks.get(0).getAssigneeId());
        // re assign
        getProcessAPI().assignUserTask(pendingTask.getId(), jack.getId());
        toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(jack.getId(), 0, 10, null);
        assertEquals(1, toDoTasks.size());
        assertEquals(jack.getId(), toDoTasks.get(0).getAssigneeId());
        pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(0, pendingTasks.size());

        getProcessAPI().releaseUserTask(toDoTasks.get(0).getId());
        pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, pendingTasks.size());
        assertEquals(0, pendingTasks.get(0).getAssigneeId());
        // re assign
        getProcessAPI().assignUserTask(pendingTask.getId(), john.getId());
        toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, toDoTasks.size());
        assertEquals(john.getId(), toDoTasks.get(0).getAssigneeId());
        pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(0, pendingTasks.size());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void releaseUserTask() throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinition processDefinition = deployAndEnableSimpleProcess("test release user task", "1.0", delivery, "Delivery all day and night long");

        getProcessAPI().startProcess(processDefinition.getId());
        // before assign
        assertTrue(new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, false, 1, john).waitUntil());
        List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, pendingTasks.size());
        final HumanTaskInstance pendingTask = pendingTasks.get(0);
        // after assign
        getProcessAPI().assignUserTask(pendingTask.getId(), john.getId());
        List<HumanTaskInstance> toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, toDoTasks.size());
        assertEquals(john.getId(), toDoTasks.get(0).getAssigneeId());
        pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(0, pendingTasks.size());
        // after release
        getProcessAPI().releaseUserTask(toDoTasks.get(0).getId());
        toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(0, toDoTasks.size());
        pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, pendingTasks.size());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void assignUserTaskSeveralTimesByChangingLogin() throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinition processDefinition = deployAndEnableSimpleProcess("test release user task", "1.0", delivery, "Delivery all day and night long");
        // process started by john
        getProcessAPI().startProcess(processDefinition.getId());

        // login as jack
        logout();
        loginWith(JACK, "bpm");

        // before assign
        assertTrue(new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, false, 1, john).waitUntil());
        List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, pendingTasks.size());
        HumanTaskInstance pendingTask = pendingTasks.get(0);
        // assign
        getProcessAPI().assignUserTask(pendingTask.getId(), jack.getId());
        List<HumanTaskInstance> toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(jack.getId(), 0, 10, null);
        assertEquals(1, toDoTasks.size());
        assertEquals(jack.getId(), toDoTasks.get(0).getAssigneeId());
        pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(0, pendingTasks.size());
        // release
        getProcessAPI().releaseUserTask(toDoTasks.get(0).getId());
        toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(jack.getId(), 0, 10, null);
        assertEquals(0, toDoTasks.size());
        pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, pendingTasks.size());
        // re assign
        pendingTask = pendingTasks.get(0);
        getProcessAPI().assignUserTask(pendingTask.getId(), jack.getId());
        toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(jack.getId(), 0, 10, null);
        assertEquals(1, toDoTasks.size());
        assertEquals(jack.getId(), toDoTasks.get(0).getAssigneeId());
        pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(0, pendingTasks.size());

        disableAndDeleteProcess(processDefinition);
    }

    private ProcessDefinition deployAndEnableSimpleProcess(final String processName, final String version, final String actorName, final String actorDescription)
            throws BonitaException, InvalidProcessDefinitionException {
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(processName, version);
        designProcessDefinition.addActor(actorName).addDescription(actorDescription);
        designProcessDefinition.addAutomaticTask("step1");
        designProcessDefinition.addUserTask("step2", actorName);
        designProcessDefinition.addTransition("step1", "step2");

        return deployAndEnableWithActor(designProcessDefinition.done(), actorName, john);
    }

    @Test
    public void assignedDateUpdate() throws Exception {
        final String delivery = "Delivery men";
        // Run a process
        final ProcessDefinition processDefinition = deployAndEnableSimpleProcess("test release user task", "1.0", delivery, "Delivery all day and night long");
        getProcessAPI().startProcess(processDefinition.getId());

        // Wait until the first task appears
        assertTrue("Fail to start process", new CheckNbPendingTaskOf(getProcessAPI(), 30, 2000, false, 1, john).waitUntil());
        final Long taskId = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null).get(0).getId();

        // First assign
        getProcessAPI().assignUserTask(taskId, john.getId());
        assertTrue("Fail to claim task", new CheckNbAssignedTaskOf(getProcessAPI(), 30, 2000, false, 1, john).waitUntil());
        final Date firstClaimedDate = getProcessAPI().getHumanTaskInstance(taskId).getClaimedDate();
        assertNotNull("Claimed date not set during first assignment", firstClaimedDate);

        // Release
        getProcessAPI().releaseUserTask(taskId);
        assertTrue("Fail to release task", new CheckNbPendingTaskOf(getProcessAPI(), 30, 2000, false, 1, john).waitUntil());
        assertNull("Claimed date not unset during release", getProcessAPI().getHumanTaskInstance(taskId).getClaimedDate());

        // Second assign
        getProcessAPI().assignUserTask(taskId, john.getId());
        assertTrue("Fail to claim task for the second time", new CheckNbAssignedTaskOf(getProcessAPI(), 30, 2000, false, 1, john).waitUntil());
        final HumanTaskInstance task = getProcessAPI().getHumanTaskInstance(taskId);
        assertNotNull("Claimed date not set during first assignment", task.getClaimedDate());
        assertFalse("Claimed date not updated", firstClaimedDate.equals(task.getClaimedDate()));

        // cleanup:
        disableAndDeleteProcess(processDefinition);

    }

    @Test
    @Ignore("lastUpdateDate should be removed (not used)")
    public void lastUpdateDateUpdate() throws Exception {
        final String delivery = "Delivery men";

        // Run a process
        final ProcessDefinition processDefinition = deployAndEnableSimpleProcess("test release user task", "1.0", delivery, "Delivery all day and night long");
        getProcessAPI().startProcess(processDefinition.getId());

        // Wait until the first task appears
        if (!new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, false, 1, john).waitUntil()) {
            fail("Fail to start process");
        }
        HumanTaskInstance task = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null).get(0);
        final Long taskId = task.getId();
        Date previousUpdateDate = task.getLastUpdateDate();

        // First assign
        getProcessAPI().assignUserTask(taskId, john.getId());
        if (!new CheckNbAssignedTaskOf(getProcessAPI(), 50, 5000, false, 1, john).waitUntil()) {
            fail("Fail to claim task");
        }
        task = getProcessAPI().getHumanTaskInstance(taskId);
        assertNotNull("Last update date not set during first assignment", task.getLastUpdateDate());
        assertFalse("Last update date not updated during first assignment", task.getLastUpdateDate().equals(previousUpdateDate));
        previousUpdateDate = task.getLastUpdateDate();

        // Release
        getProcessAPI().releaseUserTask(taskId);
        if (!new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, false, 1, john).waitUntil()) {
            fail("Fail to release task");
        }
        task = getProcessAPI().getHumanTaskInstance(taskId);
        assertFalse("Last update date not updated during release", previousUpdateDate.equals(task.getLastUpdateDate()));
        previousUpdateDate = task.getLastUpdateDate();

        // Second assign
        getProcessAPI().assignUserTask(taskId, john.getId());
        if (!new CheckNbAssignedTaskOf(getProcessAPI(), 50, 5000, false, 1, john).waitUntil()) {
            fail("Fail to claim task for the second time");
        }
        task = getProcessAPI().getHumanTaskInstance(taskId);
        assertFalse("Last update date not updated during second assignment", previousUpdateDate.equals(task.getLastUpdateDate()));

        disableAndDeleteProcess(processDefinition);
    }

}
