package org.bonitasoft.engine.process.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.FlowNodeExecutionException;
import org.bonitasoft.engine.bpm.flownode.HumanTaskDefinition;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.UserTaskInstance;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.connectors.VariableStorage;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.filter.UserFilter;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
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
        logoutOnTenant();
    }

    @Before
    public void beforeTest() throws BonitaException {
         loginOnDefaultTenantWithDefaultTechnicalUser();
        john = createUser(JOHN, "bpm");
        jack = createUser(JACK, "bpm");
        loginOnDefaultTenantWith(JOHN, "bpm");
    }

    @Test
    public void getAssignedHumanTasksWithStartedState() throws Exception {
        final String actorName = "Commercial";
        final ProcessDefinition processDefinition = deployAndEnableSimpleProcess("getAssignedHumanTasksWithStartedState", "0.12", actorName, "Trade business");
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        HumanTaskInstance pendingTask = waitForUserTask("step2", startProcess);
        assignAndExecuteStep(pendingTask, john.getId());

        final List<HumanTaskInstance> toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);
        
        // Task is in STARTED state so should not be retrieved:
        assertEquals(0, toDoTasks.size());
        waitForProcessToFinish(startProcess);
        disableAndDeleteProcess(processDefinition);
    }

    @Test(expected = FlowNodeExecutionException.class)
    public void cannotExecuteAnUnassignedTask() throws Exception {
        final String actorName = "Commercial";
        final ProcessDefinition processDefinition = deployAndEnableSimpleProcess("simple process", "0.12", actorName, "Trade business");
        ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        HumanTaskInstance pendingTask = waitForUserTask("step2", startProcess);

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
        ProcessInstance process = getProcessAPI().startProcess(processDefinition.getId());
        HumanTaskInstance pendingTask = waitForUserTask("step2", process);

        getProcessAPI().assignUserTask(pendingTask.getId(), john.getId());
        try {
            // No exception expected
            getProcessAPI().assignUserTask(pendingTask.getId(), john.getId());
        } finally {
            disableAndDeleteProcess(processDefinition);
        }
    }

    @Test
    public void assignUserTaskSeveralTimes() throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinition processDefinition = deployAndEnableSimpleProcess("test release user task", "1.0", delivery, "Delivery all day and night long");

        ProcessInstance process = getProcessAPI().startProcess(processDefinition.getId());
        HumanTaskInstance pendingTask = waitForUserTask("step2", process);
        
        // after assign
        getProcessAPI().assignUserTask(pendingTask.getId(), john.getId());
        List<HumanTaskInstance> toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, toDoTasks.size());
        assertEquals(john.getId(), toDoTasks.get(0).getAssigneeId());
        List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
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

        ProcessInstance process = getProcessAPI().startProcess(processDefinition.getId());
        HumanTaskInstance pendingTask = waitForUserTask("step2", process);
        // after assign
        getProcessAPI().assignUserTask(pendingTask.getId(), john.getId());
        List<HumanTaskInstance> toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, toDoTasks.size());
        assertEquals(john.getId(), toDoTasks.get(0).getAssigneeId());
        List<HumanTaskInstance>  pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
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
        ProcessInstance process = getProcessAPI().startProcess(processDefinition.getId());

        // login as jack
        logoutOnTenant();
        loginOnDefaultTenantWith(JACK, "bpm");

        // before assign
        HumanTaskInstance pendingTask = waitForUserTask("step2", process);
        // assign
        getProcessAPI().assignUserTask(pendingTask.getId(), jack.getId());
        List<HumanTaskInstance> toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(jack.getId(), 0, 10, null);
        assertEquals(1, toDoTasks.size());
        assertEquals(jack.getId(), toDoTasks.get(0).getAssigneeId());
        List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
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

        return deployAndEnableProcessWithActor(designProcessDefinition.done(), actorName, john);
    }

    @Test
    public void assignedDateUpdate() throws Exception {
        final String delivery = "Delivery men";
        // Run a process
        final ProcessDefinition processDefinition = deployAndEnableSimpleProcess("test release user task", "1.0", delivery, "Delivery all day and night long");
        ProcessInstance process = getProcessAPI().startProcess(processDefinition.getId());

        // Wait until the first task appears
        HumanTaskInstance pendingTask = waitForUserTask("step2", process);
        final Long taskId = pendingTask.getId();

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
        ProcessInstance process = getProcessAPI().startProcess(processDefinition.getId());

        HumanTaskInstance task = waitForUserTask("step2", process);
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
        task = waitForUserTask("step2", process);
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

    @Cover(jira = "ENGINE-1819", classes = { User.class, HumanTaskInstance.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "possible users",
            "human task" })
    @Test
    public void getPossibleUsersOfTaskUserActor() throws Exception {
        final Group group = createGroup("group");
        final Role role = createRole("role");
        final User jaakko = createUser("jaakko", "bpm");
        createUserMembership(jaakko.getUserName(), role.getName(), group.getPath());

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("assign", "5.0");
        designProcessDefinition.addActor("acme");
        designProcessDefinition.addUserTask("step1", "acme");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), "acme", jaakko);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance userTask = waitForUserTask("step1", processInstance);

        final List<User> possibleUsers = getProcessAPI().getPossibleUsersOfPendingHumanTask(userTask.getId(), 0, 10);
        assertEquals(1, possibleUsers.size());
        assertEquals(jaakko, possibleUsers.get(0));

        // cleanup:
        deleteGroups(group);
        deleteRoles(role);
        deleteUser(jaakko);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1819", classes = { User.class, HumanTaskInstance.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "possible users",
            "human task" })
    @Test
    public void getPossibleUsersOfTaskUserActorWithoutMembership() throws Exception {
        final Group group = createGroup("group");
        final Role role = createRole("role");
        final User jaakko = createUser("jaakko", "bpm");
        // createUserMembership(jack.getUserName(), role.getName(), group.getName());

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("assign", "5.0");
        designProcessDefinition.addActor("acme");
        designProcessDefinition.addUserTask("step1", "acme");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), "acme", jaakko);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance userTask = waitForUserTask("step1", processInstance);

        final List<User> possibleUsers = getProcessAPI().getPossibleUsersOfPendingHumanTask(userTask.getId(), 0, 10);
        assertEquals(1, possibleUsers.size());
        assertEquals(jaakko, possibleUsers.get(0));

        // cleanup:
        deleteGroups(group);
        deleteRoles(role);
        deleteUser(jaakko);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1819", classes = { User.class, HumanTaskInstance.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "possible users",
            "human task" })
    @Test
    public void getPossibleUsersOfTaskRoleActor() throws Exception {
        final Group group = createGroup("group");
        final Role role = createRole("role");
        final User jaakko = createUser("jaakko", "bpm");
        createUserMembership(jaakko.getUserName(), role.getName(), group.getPath());

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("assign", "5.0");
        designProcessDefinition.addActor("acme");
        designProcessDefinition.addUserTask("step1", "acme");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), "acme", role);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance userTask = waitForUserTask("step1", processInstance);

        final List<User> possibleUsers = getProcessAPI().getPossibleUsersOfPendingHumanTask(userTask.getId(), 0, 10);
        assertEquals(1, possibleUsers.size());
        assertEquals(jaakko, possibleUsers.get(0));

        // cleanup:
        deleteGroups(group);
        deleteRoles(role);
        deleteUser(jaakko);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1819", classes = { User.class, HumanTaskInstance.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "possible users",
            "human task" })
    @Test
    public void getPossibleUsersOfTaskGroupActor() throws Exception {
        final Group group = createGroup("group");
        final Role role = createRole("role");
        final User jaakko = createUser("jaakko", "bpm");
        createUserMembership(jaakko.getUserName(), role.getName(), group.getPath());

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("assign", "5.0");
        designProcessDefinition.addActor("acme");
        designProcessDefinition.addUserTask("step1", "acme");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), "acme", group);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance userTask = waitForUserTask("step1", processInstance);

        final List<User> possibleUsers = getProcessAPI().getPossibleUsersOfPendingHumanTask(userTask.getId(), 0, 10);
        assertEquals(1, possibleUsers.size());
        assertEquals(jaakko, possibleUsers.get(0));

        // cleanup:
        deleteGroups(group);
        deleteRoles(role);
        deleteUser(jaakko);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "BS-6798", classes = { ProcessAPI.class }, concept = BPMNConcept.ACTOR, keywords = { "possible users", "pagination" })
    @Test
    public void getPossibleUsersOfTaskShouldReturnAllUsersInThePaginationRange() throws Exception {
        final Group group = createGroup("group");
        final Role role = createRole("role");
        final int USER_LIST_SIZE = 21;
        final List<User> users = new ArrayList<User>(USER_LIST_SIZE);
        for (int i = 0; i < USER_LIST_SIZE; i++) {
            User newUser = createUser("user_" + i, "pwd");
            users.add(newUser);
            createUserMembership(newUser.getUserName(), role.getName(), group.getName());
        }

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("getPossible_pagination", "1.1");
        String actorName = "major";
        String activityName = "step1";
        designProcessDefinition.addActor(actorName);
        designProcessDefinition.addUserTask(activityName, actorName);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), actorName, users);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance userTask = waitForUserTask(activityName, processInstance);

        final List<User> possibleUsers = getProcessAPI().getPossibleUsersOfPendingHumanTask(userTask.getId(), 0, 30);
        // make sure the list is not limited to 20:
        assertEquals(21, possibleUsers.size());

        // cleanup:
        deleteGroups(group);
        deleteRoles(role);
        deleteUsers(users);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1819", classes = { User.class, HumanTaskInstance.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "possible users",
            "human task" })
    @Test
    public void getPossibleUsersOfTaskSubGroupActor() throws Exception {
        final Group group = createGroup("group");
        final Group group2 = createGroup("gr", group.getPath());
        final Role role = createRole("role");
        final User jaakko = createUser("jaakko", "bpm");
        createUserMembership(jack.getUserName(), role.getName(), group.getPath());
        createUserMembership(jaakko.getUserName(), role.getName(), group2.getPath());

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("assign", "5.0");
        designProcessDefinition.addActor("acme");
        designProcessDefinition.addUserTask("step1", "acme");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), "acme", group);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance userTask = waitForUserTask("step1", processInstance);

        List<User> possibleUsers = getProcessAPI().getPossibleUsersOfPendingHumanTask(userTask.getId(), 0, 10);
        assertEquals(2, possibleUsers.size());
        assertEquals(jaakko, possibleUsers.get(0));
        assertEquals(jack, possibleUsers.get(1));

        possibleUsers = getProcessAPI().getPossibleUsersOfPendingHumanTask(userTask.getId(), 1, 10);
        assertEquals(1, possibleUsers.size());
        assertEquals(jack, possibleUsers.get(0));

        // cleanup:
        deleteGroups(group);
        deleteRoles(role);
        deleteUser(jaakko);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1819", classes = { User.class, HumanTaskInstance.class, UserFilter.class }, concept = BPMNConcept.ACTIVITIES, keywords = {
            "possible users", "human task" })
    @Test
    public void getPossibleUsersOfFilteredTask() throws Exception {
        final Group group = createGroup("group");
        final Group group2 = createGroup("gr", group.getPath());
        final Role role = createRole("role");
        final User jaakko = createUser("jaakko", "bpm");
        createUserMembership(jaakko.getUserName(), role.getName(), group2.getPath());
        createUserMembership(jack.getUserName(), role.getName(), group2.getPath());

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("assign", "5.2");
        designProcessDefinition.addActor("acme");
        final UserTaskDefinitionBuilder taskDefinitionBuilder = designProcessDefinition.addUserTask("step1", "acme");
        taskDefinitionBuilder.addUserFilter("test", "org.bonitasoft.engine.filter.user.testFilter", "1.0").addInput("userId",
                new ExpressionBuilder().createConstantLongExpression(jaakko.getId()));
        final UserTaskDefinitionBuilder definitionBuilder = designProcessDefinition.addUserTask("step2", "acme");
        definitionBuilder.addUserFilter("test", "org.bonitasoft.engine.filter.user.testFilter", "1.0").addInput("userId",
                new ExpressionBuilder().createConstantLongExpression(jack.getId()));
        final ProcessDefinition processDefinition = deployProcessWithTestFilter(designProcessDefinition, "acme", jaakko, "TestFilter");
        getProcessAPI().addUserToActor("acme", processDefinition, jack.getId());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final HumanTaskInstance userTask = waitForUserTask("step1", processInstance);
        waitForUserTask("step2", processInstance);
        final List<User> possibleUsers = getProcessAPI().getPossibleUsersOfPendingHumanTask(userTask.getId(), 0, 2);
        assertEquals(1, possibleUsers.size());
        assertEquals(jaakko, possibleUsers.get(0));

        // cleanup:
        deleteGroups(group);
        deleteRoles(role);
        deleteUser(jaakko);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1819", classes = { User.class, HumanTaskInstance.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "possible users",
            "human task" })
    @Test
    public void getPossibleUsersOfUnknownTask() {
        final List<User> possibleUsers = getProcessAPI().getPossibleUsersOfPendingHumanTask(-156l, 0, 10);
        assertEquals(0, possibleUsers.size());
    }

    @Cover(jira = "ENGINE-1823", classes = { User.class, HumanTaskInstance.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "possible users",
            "human task" })
    @Test
    public void getPossibleUsersOfTaskDefinitionUserActor() throws Exception {
        final Group group = createGroup("group");
        final Role role = createRole("role");
        final User jaakko = createUser("jaakko", "bpm");
        // createUserMembership(jaakko.getUserName(), role.getName(), group.getPath());
        createUserMembership(jack.getUserName(), role.getName(), group.getPath());

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("assign", "5.0");
        designProcessDefinition.addActor("acme");
        designProcessDefinition.addActor("emca");
        designProcessDefinition.addUserTask("step1", "acme");
        designProcessDefinition.addUserTask("step2", "emca");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), Arrays.asList("acme", "emca"),
                Arrays.asList(jaakko, jack));

        final List<User> possibleUsers = getProcessAPI().getPossibleUsersOfHumanTask(processDefinition.getId(), "step1", 0, 10);
        assertEquals(1, possibleUsers.size());
        assertEquals(jaakko, possibleUsers.get(0));

        // cleanup:
        deleteGroups(group);
        deleteRoles(role);
        deleteUser(jaakko);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1823", classes = { User.class, HumanTaskDefinition.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "possible users",
            "human task" })
    @Test
    public void getPossibleUsersOfTaskDefinitionRoleActor() throws Exception {
        final Group group = createGroup("group");
        final Role role = createRole("role");
        final User jaakko = createUser("jaakko", "bpm");
        createUserMembership(jaakko.getUserName(), role.getName(), group.getPath());

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("assign", "5.0");
        designProcessDefinition.addActor("acme");
        designProcessDefinition.addUserTask("step1", "acme");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), "acme", role);

        final List<User> possibleUsers = getProcessAPI().getPossibleUsersOfHumanTask(processDefinition.getId(), "step1", 0, 10);
        assertEquals(1, possibleUsers.size());
        assertEquals(jaakko, possibleUsers.get(0));

        // cleanup:
        deleteGroups(group);
        deleteRoles(role);
        deleteUser(jaakko);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1823, BS-8854", classes = { User.class, HumanTaskDefinition.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "possible users",
            "human task", "users for actor" })
    @Test
    public void getPossibleUsersOfTaskDefinitionGroupActor() throws Exception {
        //given
        final Group group = createGroup("group");
        final Role role = createRole("role");
        final User jaakko = createUser("jaakko", "bpm");
        final User pato = createUser("pato", "bpm");
        getIdentityAPI().addUserMembership(jaakko.getId(), role.getId(), group.getId());
        getIdentityAPI().addUserMembership(pato.getId(), group.getId(), role.getId());

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("assign", "5.0");
        designProcessDefinition.addActor("acme");
        designProcessDefinition.addUserTask("step1", "acme");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), "acme", group);

        //when
        final List<User> possibleUsers = getProcessAPI().getPossibleUsersOfHumanTask(processDefinition.getId(), "step1", 0, 10);
        List<Long> userIdsForActor = getProcessAPI().getUserIdsForActor(processDefinition.getId(), "acme", 0, 10);

        //then
        assertThat(possibleUsers).containsExactly(jaakko, pato);
        assertThat(userIdsForActor).containsExactly(jaakko.getId(), pato.getId());
        
        // cleanup:
        deleteGroups(group);
        deleteRoles(role);
        deleteUsers(jaakko, pato);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1823", classes = { User.class, HumanTaskDefinition.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "possible users",
            "human task" })
    @Test
    public void getPossibleUsersOfTaskDefinitionSubGroupActor() throws Exception {
        final Group group = createGroup("group");
        final Group group2 = createGroup("gr", group.getPath());
        final Role role = createRole("role");
        final User jaakko = createUser("jaakko", "bpm");
        createUserMembership(jack.getUserName(), role.getName(), group.getPath());
        createUserMembership(jaakko.getUserName(), role.getName(), group2.getPath());

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("assign", "5.0");
        designProcessDefinition.addActor("acme");
        designProcessDefinition.addUserTask("step1", "acme");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), "acme", group);

        List<User> possibleUsers = getProcessAPI().getPossibleUsersOfHumanTask(processDefinition.getId(), "step1", 0, 10);
        assertEquals(2, possibleUsers.size());
        assertEquals(jaakko, possibleUsers.get(0));
        assertEquals(jack, possibleUsers.get(1));

        possibleUsers = getProcessAPI().getPossibleUsersOfHumanTask(processDefinition.getId(), "step1", 1, 10);
        assertEquals(1, possibleUsers.size());
        assertEquals(jack, possibleUsers.get(0));

        // cleanup:
        deleteGroups(group);
        deleteRoles(role);
        deleteUser(jaakko);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1823", classes = { User.class, HumanTaskDefinition.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "possible users",
            "human task" })
    @Test
    public void getPossibleUsersOfUnknownProcessDefinition() {
        final List<User> possibleUsers = getProcessAPI().getPossibleUsersOfHumanTask(-156l, "step1", 0, 10);
        assertEquals(0, possibleUsers.size());
    }

    @Cover(jira = "ENGINE-1823", classes = { User.class, HumanTaskDefinition.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "possible users",
            "human task" })
    @Test
    public void getPossibleUsersOfUnknownTaskDefinition() throws Exception {
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("assign", "5.0");
        designProcessDefinition.addActor("acme");
        designProcessDefinition.addUserTask("step1", "acme");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), "acme", jack);

        final List<User> possibleUsers = getProcessAPI().getPossibleUsersOfHumanTask(processDefinition.getId(), "step83", 0, 10);
        assertEquals(0, possibleUsers.size());

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1823", classes = { User.class, HumanTaskDefinition.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "possible users",
            "human task" })
    @Test
    public void getPossibleUsersOfSystemTaskDefinition() throws Exception {
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("assign", "5.0");
        designProcessDefinition.addAutomaticTask("auto");
        final ProcessDefinition processDefinition = deployAndEnableProcess(designProcessDefinition.done());

        final List<User> possibleUsers = getProcessAPI().getPossibleUsersOfHumanTask(processDefinition.getId(), "auto", 0, 10);
        assertEquals(0, possibleUsers.size());

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1823", classes = { User.class, HumanTaskDefinition.class, UserFilter.class }, concept = BPMNConcept.ACTIVITIES, keywords = {
            "possible users", "human task" })
    @Test
    public void getPossibleUsersOfTaskDefinitionWithAFilter() throws Exception {
        final Group group = createGroup("group");
        final Group group2 = createGroup("gr", group.getPath());
        final Role role = createRole("role");
        final User jaakko = createUser("jaakko", "bpm");
        createUserMembership(jaakko.getUserName(), role.getName(), group2.getPath());
        createUserMembership(jack.getUserName(), role.getName(), group2.getPath());

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("assign", "5.2");
        designProcessDefinition.addActor("acme");
        final UserTaskDefinitionBuilder taskDefinitionBuilder = designProcessDefinition.addUserTask("step1", "acme");
        taskDefinitionBuilder.addUserFilter("test", "org.bonitasoft.engine.filter.user.testFilter", "1.0").addInput("userId",
                new ExpressionBuilder().createConstantLongExpression(jaakko.getId()));
        final UserTaskDefinitionBuilder definitionBuilder = designProcessDefinition.addUserTask("step2", "acme");
        definitionBuilder.addUserFilter("test", "org.bonitasoft.engine.filter.user.testFilter", "1.0").addInput("userId",
                new ExpressionBuilder().createConstantLongExpression(jack.getId()));
        final ProcessDefinition processDefinition = deployProcessWithTestFilter(designProcessDefinition, "acme", jaakko, "TestFilter");
        getProcessAPI().addUserToActor("acme", processDefinition, jack.getId());

        List<User> possibleUsers = getProcessAPI().getPossibleUsersOfHumanTask(processDefinition.getId(), "step1", 0, 2);
        assertEquals(2, possibleUsers.size());
        assertEquals(jaakko, possibleUsers.get(0));
        assertEquals(jack, possibleUsers.get(1));

        possibleUsers = getProcessAPI().getPossibleUsersOfHumanTask(processDefinition.getId(), "step1", 2, 4);
        assertEquals(0, possibleUsers.size());

        // cleanup:
        deleteGroups(group);
        deleteRoles(role);
        deleteUser(jaakko);
        disableAndDeleteProcess(processDefinition);
    }
    
}
