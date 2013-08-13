package org.bonitasoft.engine.process.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.bpm.actor.ActorCriterion;
import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.TaskPriority;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserMembership;
import org.bonitasoft.engine.identity.UserNotFoundException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.WaitUntil;
import org.bonitasoft.engine.test.check.CheckNbOfHumanTasks;
import org.bonitasoft.engine.test.wait.WaitForPendingTasks;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PendingTasksTest extends CommonAPITest {

    @Before
    public void beforeTest() throws BonitaException {
        login();
    }

    @After
    public void afterTest() throws BonitaException {
        logout();
    }

    @Test
    public void searchMyAvailableHumanTasks() throws Exception {
        final User user = createUser("Barnabooth", "Strongwood");
        final User user2 = createUser("Unknown", "Stranger");
        final Group group = createGroup("un_used");
        // Process def with 2 instances:
        final ProcessDefinitionBuilder processBuilder1 = new ProcessDefinitionBuilder().createNewInstance("My_Process_with_branches", "1.0");
        final String actorName = "1_Furniture assembly";
        final String otherActor = "2_NotForMe";
        processBuilder1.addActor(actorName).addActor(otherActor);
        final DesignProcessDefinition designProcessDefinition = processBuilder1.addUserTask("step1", actorName).addUserTask("step2", actorName)
                .addUserTask("step3", otherActor).addUserTask("step4", otherActor).getProcess();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        final List<ActorInstance> actors = getProcessAPI().getActors(processDefinition.getId(), 0, 2, ActorCriterion.NAME_ASC);
        getProcessAPI().addUserToActor(actors.get(0).getId(), user.getId());
        getProcessAPI().addGroupToActor(actors.get(1).getId(), group.getId());
        // addUserToFirstActorOfProcess(user.getId(), processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        getProcessAPI().startProcess(processDefinition.getId());

        // Wait for tasks in READY state:
        final CheckNbOfHumanTasks checkNbOfHumanTasks = checkNbOfHumanTasks(4);
        final SearchResult<HumanTaskInstance> tasks = checkNbOfHumanTasks.getHumanTaskInstances();

        // 2 tasks should already be pending for me:
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        final SearchOptions searchOptions = searchOptionsBuilder.done();
        SearchResult<HumanTaskInstance> humanTasksSearch = getProcessAPI().searchMyAvailableHumanTasks(user.getId(), searchOptions);
        assertEquals(2, humanTasksSearch.getCount());

        // Force assigning 'task3' (DESC name sort) to me (event though I am not an actor for it):
        getProcessAPI().assignUserTask(tasks.getResult().get(1).getId(), user.getId());

        // 3 tasks should now be available for me:
        humanTasksSearch = getProcessAPI().searchMyAvailableHumanTasks(user.getId(), searchOptions);
        assertEquals(3, humanTasksSearch.getCount());

        // Force assigning 'task2' (DESC name sort) to someone else than me (event though he is not an actor for it):
        getProcessAPI().assignUserTask(tasks.getResult().get(2).getId(), user2.getId());

        // 2 tasks should now be available for me:
        humanTasksSearch = getProcessAPI().searchMyAvailableHumanTasks(user.getId(), searchOptions);
        assertEquals(2, humanTasksSearch.getCount());

        deleteUsers(user, user2);
        deleteGroups(group);
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getPendingHumanTaskInstancesInTwoProcesses() throws Exception {
        // 1. create a user 'test'
        User test;
        final String USERNAME = "jack";
        final String PASSWORD = "bpm";
        try {
            test = getIdentityAPI().getUserByUserName(USERNAME);
        } catch (final UserNotFoundException e) {
            test = getIdentityAPI().createUser(USERNAME, PASSWORD);
        }
        final long userId = test.getId();
        // 2. install two processes
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addActor("myActor");
        processBuilder.addUserTask("Request", "myActor");
        processBuilder.addUserTask("Approval", "myActor");
        processBuilder.addTransition("Request", "Approval");
        final DesignProcessDefinition designProcessDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        final BusinessArchive businessArchive = businessArchiveBuilder.setProcessDefinition(designProcessDefinition).done();

        final ProcessDefinitionBuilder processBuilder2 = new ProcessDefinitionBuilder().createNewInstance("SecontProcess", "1.0");
        processBuilder2.addActor("myActor").addUserTask("Request", "myActor").addUserTask("Approval", "myActor").addTransition("Request", "Approval");
        final DesignProcessDefinition designProcessDefinition2 = processBuilder2.done();
        final BusinessArchive businessArchive2 = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition2).done();

        final ProcessDefinition processDefinition1 = getProcessAPI().deploy(businessArchive);
        final ProcessDefinition processDefinition2 = getProcessAPI().deploy(businessArchive2);
        // 3. assign user 'test' to actor of both processes
        ActorInstance processActor = getProcessAPI().getActors(processDefinition1.getId(), 0, 1, ActorCriterion.NAME_ASC).get(0);
        getProcessAPI().addUserToActor(processActor.getId(), test.getId());
        processActor = getProcessAPI().getActors(processDefinition2.getId(), 0, 1, ActorCriterion.NAME_ASC).get(0);
        getProcessAPI().addUserToActor(processActor.getId(), test.getId());
        // 4. enable both processes
        getProcessAPI().enableProcess(processDefinition1.getId());
        getProcessAPI().enableProcess(processDefinition2.getId());
        // 5. start both processes
        getProcessAPI().startProcess(processDefinition1.getId());
        getProcessAPI().startProcess(processDefinition2.getId());
        // 6.check Pending tasks list. The below exception appears.
        assertTrue("no pending user task instances are found", new WaitUntil(150, 1000) {

            @Override
            protected boolean check() {
                return getProcessAPI().getPendingHumanTaskInstances(userId, 0, 10, null).size() == 2;
            }
        }.waitUntil());
        final List<HumanTaskInstance> userTaskInstances = getProcessAPI().getPendingHumanTaskInstances(userId, 0, 10, ActivityInstanceCriterion.NAME_ASC);
        assertNotNull(userTaskInstances);
        assertEquals(2, userTaskInstances.size());

        // Clean up
        deleteUser(test);
        disableAndDeleteProcess(processDefinition1, processDefinition2);
    }

    @Test
    public void getPendingHumanTaskInstancePriorityAndExpectedEndDate() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);
        final long userId = user.getId();

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        final TaskPriority priority = TaskPriority.HIGHEST;
        final int oneDay = 24 * 60 * 60 * 1000;
        processBuilder.addActor(ACTOR_NAME).addDescription("Delivery all day and night long").addUserTask("deliver", ACTOR_NAME).addPriority(priority.name())
                .addExpectedDuration(oneDay);
        final DesignProcessDefinition processDesignDefinition = processBuilder.done();

        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDesignDefinition).done();

        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);
        addMappingOfActorsForUser(ACTOR_NAME, userId, processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        final Date before = new Date();
        Thread.sleep(100);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        waitForStep(50, 1000, "deliver", startProcess);
        final List<HumanTaskInstance> activityInstances = getProcessAPI().getPendingHumanTaskInstances(userId, 0, 10, ActivityInstanceCriterion.DEFAULT);
        Thread.sleep(100);
        final Date after = new Date();
        assertEquals(1, activityInstances.size());
        final HumanTaskInstance humanTaskInstance = activityInstances.get(0);
        assertEquals(priority, humanTaskInstance.getPriority());
        final long time = humanTaskInstance.getExpectedEndDate().getTime();
        assertTrue(before.getTime() + oneDay < time && time < after.getTime() + oneDay);
        disableAndDeleteProcess(processDefinition);
        deleteUser(userId);
    }

    @Test
    public void taskAlreadyClaimedByMe() throws Exception {
        final User user = createUser("login", "password");
        final ProcessDefinition processDefinition = deployProcessWithUserTask(user);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance task = waitForUserTask("Request", startProcess);
        final long taskId = task.getId();
        getProcessAPI().assignUserTask(taskId, user.getId());
        getProcessAPI().assignUserTask(taskId, user.getId());
        assertEquals(1, getProcessAPI().getAssignedHumanTaskInstances(user.getId(), 0, 10, ActivityInstanceCriterion.DEFAULT).size());
        deleteUser(user);
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void taskAlreadyClaimedByOther() throws Exception {
        final User user1 = createUser("login1", "password");
        final User user2 = createUser("login2", "password");
        final ProcessDefinition processDefinition = deployProcessWithUserTask(user1);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance task = waitForUserTask("Request", startProcess);
        final long taskId = task.getId();
        getProcessAPI().assignUserTask(taskId, user1.getId());
        getProcessAPI().assignUserTask(taskId, user2.getId());
        logout();
        loginWith("login1", "password");
        assertEquals(1, getProcessAPI().getAssignedHumanTaskInstances(user2.getId(), 0, 10, ActivityInstanceCriterion.DEFAULT).size());
        assertEquals(0, getProcessAPI().getAssignedHumanTaskInstances(user1.getId(), 0, 10, ActivityInstanceCriterion.DEFAULT).size());
        deleteUser(user1);
        deleteUser(user2);
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void searchTasksWithSpecialCharsInFreeText() throws Exception {
        final User user = createUser("login", "password");
        final String taskName = "étape";
        final String processName = "никола";
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(processName, "1");
        processBuilder.addActor("myActor");
        processBuilder.addUserTask(taskName, "myActor");
        final ProcessDefinition processDefinition = deployAndEnableWithActor(processBuilder.done(), "myActor", user);
        getProcessAPI().startProcess(processDefinition.getId());
        assertTrue("étape1 should be pending", new WaitUntil(30, 2000) {

            @Override
            protected boolean check() {
                return getProcessAPI().getPendingHumanTaskInstances(user.getId(), 0, 10, null).size() == 1;
            }
        }.waitUntil());

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.searchTerm(taskName);
        final SearchResult<HumanTaskInstance> taskRes = getProcessAPI().searchHumanTaskInstances(builder.done());
        assertEquals(1, taskRes.getCount());
        assertEquals(taskName, taskRes.getResult().get(0).getName());

        builder.searchTerm(processName);
        final SearchResult<ProcessDeploymentInfo> procDefRes = getProcessAPI().searchProcessDeploymentInfos(builder.done());
        assertEquals(1, procDefRes.getCount());
        assertEquals(processName, procDefRes.getResult().get(0).getName());

        deleteUser(user);
        disableAndDeleteProcess(processDefinition);
    }

    private ProcessDefinition deployProcessWithUserTask(final User user1) throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addActor("myActor");
        processBuilder.addUserTask("Request", "myActor");
        return deployAndEnableWithActor(processBuilder.done(), "myActor", user1);
    }

    @Test
    public void taskAlreadyReleased() throws Exception {
        final User user = createUser("login1", "password");
        final ProcessDefinition processDefinition = deployProcessWithUserTask(user);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance task = waitForUserTask("Request", startProcess);
        final long taskId = task.getId();
        getProcessAPI().assignUserTask(taskId, user.getId());
        getProcessAPI().releaseUserTask(taskId);
        getProcessAPI().releaseUserTask(taskId);
        deleteUser(user);
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void actorMappedToGroup() throws Exception {
        final User john = createUser("john", "bpm");
        final User jack = createUser("jack", "bpm");
        final Group mainGroup = createGroup("main");
        final Role member = createRole("member");
        final UserMembership m1 = getIdentityAPI().addUserMembership(john.getId(), mainGroup.getId(), member.getId());
        final UserMembership m2 = getIdentityAPI().addUserMembership(jack.getId(), mainGroup.getId(), member.getId());

        final ProcessDefinition processDefinition = deployProcessMappedToGroup(mainGroup);
        getProcessAPI().startProcess(processDefinition.getId());
        waitForPendingTasks(john.getId(), 1);
        waitForPendingTasks(jack.getId(), 1);

        disableAndDeleteProcess(processDefinition);
        deleteUserMembership(m1.getId());
        deleteUserMembership(m2.getId());
        deleteGroups(mainGroup);
        deleteRoles(member);
        deleteUser(jack);
        deleteUser(john);
    }

    @Test
    public void actorMappedToDifferrentGroup() throws Exception {
        final User john = createUser("john", "bpm");
        final User jack = createUser("jack", "bpm");
        final Group mainGroup = createGroup("main");
        final Group secondGroup = createGroup("second");
        final Role member = createRole("member");
        final UserMembership m1 = getIdentityAPI().addUserMembership(john.getId(), mainGroup.getId(), member.getId());
        final UserMembership m2 = getIdentityAPI().addUserMembership(jack.getId(), secondGroup.getId(), member.getId());

        final ProcessDefinition processDefinition = deployProcessMappedToGroup(mainGroup);
        getProcessAPI().startProcess(processDefinition.getId());
        waitForPendingTasks(john.getId(), 1);
        final WaitForPendingTasks waitForPendingTasks = new WaitForPendingTasks(DEFAULT_REPEAT, 250, 1, jack.getId(), getProcessAPI());
        assertFalse(waitForPendingTasks.waitUntil());

        disableAndDeleteProcess(processDefinition);
        deleteUserMembership(m1.getId());
        deleteUserMembership(m2.getId());
        deleteGroups(secondGroup, mainGroup);
        deleteRoles(member);
        deleteUser(jack);
        deleteUser(john);
    }

    @Test
    public void actorMappedToGrandChildGroup() throws Exception {
        final User john = createUser("john", "bpm");
        final User jack = createUser("jack", "bpm");
        final Group mainGroup = createGroup("main");
        final Group childGroup = createGroup("child", "/main");
        final Group grandChildGroup = createGroup("gChild", "/main/child");
        assertEquals(childGroup.getPath(), grandChildGroup.getParentPath());
        assertEquals(mainGroup.getPath(), childGroup.getParentPath());
        final Role member = createRole("member");
        final UserMembership m1 = getIdentityAPI().addUserMembership(john.getId(), mainGroup.getId(), member.getId());
        final UserMembership m2 = getIdentityAPI().addUserMembership(jack.getId(), grandChildGroup.getId(), member.getId());

        final ProcessDefinition processDefinition = deployProcessMappedToGroup(mainGroup);
        try {
            getProcessAPI().startProcess(processDefinition.getId());
            waitForPendingTasks(john.getId(), 1);
            waitForPendingTasks(jack.getId(), 1);
        } finally {
            disableAndDeleteProcess(processDefinition);
            deleteUserMembership(m1.getId());
            deleteUserMembership(m2.getId());
            deleteGroups(grandChildGroup, childGroup, mainGroup);
            deleteRoles(member);
            deleteUser(jack);
            deleteUser(john);
        }
    }

    @Test
    public void actorMappedToChildGroup() throws Exception {
        final User john = createUser("john", "bpm");
        final User jack = createUser("jack", "bpm");
        final Group mainGroup = createGroup("main");
        final Group secondGroup = createGroup("second", "/main");
        final Role member = createRole("member");
        final UserMembership m1 = getIdentityAPI().addUserMembership(john.getId(), mainGroup.getId(), member.getId());
        final UserMembership m2 = getIdentityAPI().addUserMembership(jack.getId(), secondGroup.getId(), member.getId());

        final ProcessDefinition processDefinition = deployProcessMappedToGroup(mainGroup);
        try {
            getProcessAPI().startProcess(processDefinition.getId());
            waitForPendingTasks(john.getId(), 1);
            waitForPendingTasks(jack.getId(), 1);
        } finally {
            disableAndDeleteProcess(processDefinition);
            deleteUserMembership(m1.getId());
            deleteUserMembership(m2.getId());
            deleteGroups(secondGroup, mainGroup);
            deleteRoles(member);
            deleteUser(jack);
            deleteUser(john);
        }
    }

    private ProcessDefinition deployProcessMappedToGroup(final Group mainGroup) throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addActor("myActor");
        processBuilder.addUserTask("Request", "myActor");
        final DesignProcessDefinition designProcessDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        final BusinessArchive businessArchive = businessArchiveBuilder.setProcessDefinition(designProcessDefinition).done();

        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);
        // map actor to group
        final ActorInstance processActor = getProcessAPI().getActors(processDefinition.getId(), 0, 1, ActorCriterion.NAME_ASC).get(0);
        getProcessAPI().addGroupToActor(processActor.getId(), mainGroup.getId());
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

}
