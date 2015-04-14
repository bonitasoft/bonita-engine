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
package org.bonitasoft.engine.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.TestWithTechnicalUser;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.actor.ActorCriterion;
import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.TaskPriority;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
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
import org.bonitasoft.engine.identity.UserMembership;
import org.bonitasoft.engine.identity.UserNotFoundException;
import org.bonitasoft.engine.identity.UserSearchDescriptor;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.CollectionUtils;

public class PendingTasksIT extends TestWithTechnicalUser {

    private static final String JACK = "jack";

    private User john;

    private User jack;

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        john = createUser(USERNAME, PASSWORD);
        jack = createUser(JACK, PASSWORD);
    }

    @Override
    @After
    public void after() throws Exception {
        deleteUser(USERNAME);
        deleteUser(JACK);
        VariableStorage.clearAll();
        super.after();
    }

    @Test
    public void searchMyAvailableHumanTasks() throws Exception {
        final User user = createUser("Barnabooth", "Strongwood");
        final User user2 = createUser("Unknown", "Stranger");
        final Group group = createGroup("un_used");

        // Process def with 2 instances:
        final String otherActor = "NotForMe";
        final ProcessDefinitionBuilder processBuilder1 = new ProcessDefinitionBuilder().createNewInstance("My_Process_with_branches", "1.0");
        processBuilder1.addActor(ACTOR_NAME).addActor(otherActor);
        processBuilder1.addUserTask("step1", ACTOR_NAME).addUserTask("step2", ACTOR_NAME).addUserTask("step3", otherActor).addUserTask("step4", otherActor);
        final ProcessDefinition processDefinition = deployProcess(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processBuilder1.done()).done());
        final List<ActorInstance> actors = getProcessAPI().getActors(processDefinition.getId(), 0, 2, ActorCriterion.NAME_ASC);
        getProcessAPI().addUserToActor(actors.get(0).getId(), user.getId());
        getProcessAPI().addGroupToActor(actors.get(1).getId(), group.getId());
        getProcessAPI().enableProcess(processDefinition.getId());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        // Wait for tasks in READY state:
        waitForUserTask(processInstance, "step1");
        final long step2Id = waitForUserTask(processInstance, "step2");
        final long step3Id = waitForUserTask(processInstance, "step3");
        waitForUserTask(processInstance, "step4");

        // 2 tasks should already be pending for me:
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        SearchResult<HumanTaskInstance> humanTasksSearch = getProcessAPI().searchMyAvailableHumanTasks(user.getId(), searchOptionsBuilder.done());
        assertEquals(2, humanTasksSearch.getCount());

        // Force assigning 'task3' (DESC name sort) to me (event though I am not an actor for it):
        getProcessAPI().assignUserTask(step3Id, user.getId());

        // 3 tasks should now be available for me:
        humanTasksSearch = getProcessAPI().searchMyAvailableHumanTasks(user.getId(), searchOptionsBuilder.done());
        assertEquals(3, humanTasksSearch.getCount());

        // Force assigning 'task2' (DESC name sort) to someone else than me (event though he is not an actor for it):
        getProcessAPI().assignUserTask(step2Id, user2.getId());

        // 2 tasks should now be available for me:
        humanTasksSearch = getProcessAPI().searchMyAvailableHumanTasks(user.getId(), searchOptionsBuilder.done());
        assertEquals(2, humanTasksSearch.getCount());

        disableAndDeleteProcess(processDefinition);
        deleteUsers(user, user2);
        deleteGroups(group);
    }

    @Test
    public void getPendingHumanTaskInstancesInTwoProcesses() throws Exception {
        // 1. create a user 'test'
        User test;
        try {
            test = getIdentityAPI().getUserByUserName(JACK);
        } catch (final UserNotFoundException e) {
            test = getIdentityAPI().createUser(JACK, PASSWORD);
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

        final ProcessDefinition processDefinition1 = deployProcess(businessArchive);
        final ProcessDefinition processDefinition2 = deployProcess(businessArchive2);
        // 3. assign user 'test' to actor of both processes
        ActorInstance processActor = getProcessAPI().getActors(processDefinition1.getId(), 0, 1, ActorCriterion.NAME_ASC).get(0);
        getProcessAPI().addUserToActor(processActor.getId(), test.getId());
        processActor = getProcessAPI().getActors(processDefinition2.getId(), 0, 1, ActorCriterion.NAME_ASC).get(0);
        getProcessAPI().addUserToActor(processActor.getId(), test.getId());
        // 4. enable both processes
        getProcessAPI().enableProcess(processDefinition1.getId());
        getProcessAPI().enableProcess(processDefinition2.getId());
        // 5. start both processes
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(processDefinition1.getId());
        waitForUserTask(processInstance1, "Request");
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(processDefinition2.getId());
        waitForUserTask(processInstance2, "Request");
        // 6.check Pending tasks list. The below exception appears.
        final List<HumanTaskInstance> userTaskInstances = getProcessAPI().getPendingHumanTaskInstances(userId, 0, 10, ActivityInstanceCriterion.NAME_ASC);
        assertNotNull(userTaskInstances);
        assertEquals(2, userTaskInstances.size());

        // Clean up
        deleteUser(test);
        disableAndDeleteProcess(processDefinition1, processDefinition2);
    }

    @Test
    public void getPendingHumanTaskInstancePriorityAndExpectedEndDate() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        final TaskPriority priority = TaskPriority.HIGHEST;
        final int oneDay = 24 * 60 * 60 * 1000;
        processBuilder.addActor(ACTOR_NAME).addDescription("Delivery all day and night long").addUserTask("deliver", ACTOR_NAME).addPriority(priority.name())
                .addExpectedDuration(oneDay);
        final DesignProcessDefinition processDesignDefinition = processBuilder.done();

        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDesignDefinition).done();

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(businessArchive, ACTOR_NAME, john);
        final Date before = new Date();
        Thread.sleep(20);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance, "deliver");
        final List<HumanTaskInstance> activityInstances = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, ActivityInstanceCriterion.DEFAULT);
        Thread.sleep(20);
        final Date after = new Date();
        assertEquals(1, activityInstances.size());
        final HumanTaskInstance humanTaskInstance = activityInstances.get(0);
        assertEquals(priority, humanTaskInstance.getPriority());
        final long time = humanTaskInstance.getExpectedEndDate().getTime();
        assertTrue(before.getTime() + oneDay < time && time < after.getTime() + oneDay);
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void taskAlreadyClaimedByMe() throws Exception {
        final User user = createUser("login", "password");
        final ProcessDefinition processDefinition = deployProcessWithUserTask(user);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        final long taskId = waitForUserTask(startProcess, "Request");
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
        final long taskId = waitForUserTask(startProcess, "Request");
        getProcessAPI().assignUserTask(taskId, user1.getId());
        getProcessAPI().assignUserTask(taskId, user2.getId());
        logoutOnTenant();
        loginOnDefaultTenantWith("login1", "password");
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
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processBuilder.done(), "myActor", user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance, taskName);

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
        return deployAndEnableProcessWithActor(processBuilder.done(), "myActor", user1);
    }

    @Test
    public void taskAlreadyReleased() throws Exception {
        final User user = createUser("login1", "password");
        final ProcessDefinition processDefinition = deployProcessWithUserTask(user);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        final long taskId = waitForUserTask(startProcess, "Request");
        getProcessAPI().assignUserTask(taskId, user.getId());
        getProcessAPI().releaseUserTask(taskId);
        getProcessAPI().releaseUserTask(taskId);
        deleteUser(user);
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void actorMappedToGroup() throws Exception {
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
    }

    @Test
    public void actorMappedToDifferrentGroup() throws Exception {
        final Group mainGroup = createGroup("main");
        final Group secondGroup = createGroup("second");
        final Role member = createRole("member");
        final UserMembership m1 = getIdentityAPI().addUserMembership(john.getId(), mainGroup.getId(), member.getId());
        final UserMembership m2 = getIdentityAPI().addUserMembership(jack.getId(), secondGroup.getId(), member.getId());

        final ProcessDefinition processDefinition = deployProcessMappedToGroup(mainGroup);
        getProcessAPI().startProcess(processDefinition.getId());
        waitForPendingTasks(john.getId(), 1);
        final List<HumanTaskInstance> pendingHumanTaskInstances = getProcessAPI().getPendingHumanTaskInstances(jack.getId(), 0, 10, null);
        assertTrue(pendingHumanTaskInstances.isEmpty());

        disableAndDeleteProcess(processDefinition);
        deleteUserMembership(m1.getId());
        deleteUserMembership(m2.getId());
        deleteGroups(secondGroup, mainGroup);
        deleteRoles(member);
    }

    @Test
    public void actorMappedToGrandChildGroup() throws Exception {
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
        }
    }

    @Test
    public void actorMappedToChildGroup() throws Exception {
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
        }
    }

    private ProcessDefinition deployProcessMappedToGroup(final Group mainGroup) throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addActor("myActor");
        processBuilder.addUserTask("Request", "myActor");
        final DesignProcessDefinition designProcessDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        final BusinessArchive businessArchive = businessArchiveBuilder.setProcessDefinition(designProcessDefinition).done();

        final ProcessDefinition processDefinition = deployProcess(businessArchive);
        // map actor to group
        final ActorInstance processActor = getProcessAPI().getActors(processDefinition.getId(), 0, 1, ActorCriterion.NAME_ASC).get(0);
        getProcessAPI().addGroupToActor(processActor.getId(), mainGroup.getId());
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    @Cover(jira = "BS-8392", classes = { User.class, HumanTaskInstance.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "possible users",
            "human task" })
    @Test
    public void searchPossibleUsersOfTaskUserActor() throws Exception {
        loginOnDefaultTenantWith(USERNAME, PASSWORD);
        final Group group = createGroup("group");
        final Role role = createRole("role");
        final User jaakko = createUser("jaakko", PASSWORD);
        createUserMembership(jaakko.getUserName(), role.getName(), group.getPath());

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("assign", "5.0");
        designProcessDefinition.addActor("acme");
        designProcessDefinition.addUserTask("step1", "acme");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), "acme", jaakko);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(processInstance, "step1");

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(UserSearchDescriptor.LAST_NAME, Order.DESC);

        final List<User> possibleUsers = getProcessAPI().searchUsersWhoCanExecutePendingHumanTask(step1Id, builder.done()).getResult();
        assertEquals(1, possibleUsers.size());
        assertEquals(jaakko, possibleUsers.get(0));

        // cleanup:
        deleteGroups(group);
        deleteRoles(role);
        deleteUser(jaakko);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "BS-8392", classes = { User.class, HumanTaskInstance.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "possible users",
            "human task" })
    @Test
    public void searchPossibleUsersOfTaskUserActorWithoutMembership() throws Exception {
        loginOnDefaultTenantWith(USERNAME, PASSWORD);
        final Group group = createGroup("group");
        final Role role = createRole("role");
        final User jaakko = createUser("jaakko", PASSWORD);
        // createUserMembership(jack.getUserName(), role.getName(), group.getName());

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("assign", "5.0");
        designProcessDefinition.addActor("acme");
        designProcessDefinition.addUserTask("step1", "acme");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), "acme", jaakko);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(processInstance, "step1");

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(UserSearchDescriptor.LAST_NAME, Order.DESC);

        final SearchResult<User> searchResult = getProcessAPI().searchUsersWhoCanExecutePendingHumanTask(step1Id, builder.done());
        assertEquals(1, searchResult.getCount());
        final List<User> possibleUsers = searchResult.getResult();
        assertEquals(1, possibleUsers.size());
        assertEquals(jaakko, possibleUsers.get(0));

        // cleanup:
        deleteGroups(group);
        deleteRoles(role);
        deleteUser(jaakko);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "BS-8392", classes = { User.class, HumanTaskInstance.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "possible users",
            "human task" })
    @Test
    public void searchPossibleUsersOfTaskRoleActor() throws Exception {
        loginOnDefaultTenantWith(USERNAME, PASSWORD);
        final Group group = createGroup("group");
        final Role role = createRole("role");
        final User jaakko = createUser("jaakko", PASSWORD);
        createUserMembership(jaakko.getUserName(), role.getName(), group.getPath());

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("assign", "5.0");
        designProcessDefinition.addActor("acme");
        designProcessDefinition.addUserTask("step1", "acme");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), "acme", role);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(processInstance, "step1");

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(UserSearchDescriptor.LAST_NAME, Order.DESC);

        final SearchResult<User> searchResult = getProcessAPI().searchUsersWhoCanExecutePendingHumanTask(step1Id, builder.done());
        assertEquals(1, searchResult.getCount());
        final List<User> possibleUsers = searchResult.getResult();
        assertEquals(1, possibleUsers.size());
        assertEquals(jaakko, possibleUsers.get(0));

        // cleanup:
        deleteGroups(group);
        deleteRoles(role);
        deleteUser(jaakko);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "BS-8392", classes = { User.class, HumanTaskInstance.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "possible users",
            "human task" })
    @Test
    public void searchPossibleUsersOfTaskGroupActor() throws Exception {
        loginOnDefaultTenantWith(USERNAME, PASSWORD);
        final Group group = createGroup("group");
        final Role role = createRole("role");
        final User jaakko = createUser("jaakko", PASSWORD);
        createUserMembership(jaakko.getUserName(), role.getName(), group.getPath());

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("assign", "5.0");
        designProcessDefinition.addActor("acme");
        designProcessDefinition.addUserTask("step1", "acme");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), "acme", group);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(processInstance, "step1");

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(UserSearchDescriptor.LAST_NAME, Order.DESC);

        final SearchResult<User> searchResult = getProcessAPI().searchUsersWhoCanExecutePendingHumanTask(step1Id, builder.done());
        assertEquals(1, searchResult.getCount());
        final List<User> possibleUsers = searchResult.getResult();
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
    public void searchPossibleUsersOfTaskShouldReturnAllUsersInThePaginationRange() throws Exception {
        loginOnDefaultTenantWith(USERNAME, PASSWORD);
        final Group group = createGroup("group");
        final Role role = createRole("role");
        final int USER_LIST_SIZE = 21;
        final List<User> users = new ArrayList<User>(USER_LIST_SIZE);
        for (int i = 0; i < USER_LIST_SIZE; i++) {
            final User newUser = createUser("user_" + i, "pwd");
            users.add(newUser);
            createUserMembership(newUser.getUserName(), role.getName(), group.getName());
        }

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("getPossible_pagination", "1.1");
        final String activityName = "step1";
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addUserTask(activityName, ACTOR_NAME);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), ACTOR_NAME, users);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(processInstance, activityName);
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 30);
        builder.sort(UserSearchDescriptor.LAST_NAME, Order.DESC);

        final SearchResult<User> searchResult = getProcessAPI().searchUsersWhoCanExecutePendingHumanTask(step1Id, builder.done());
        assertEquals(21, searchResult.getCount());
        final List<User> possibleUsers = searchResult.getResult();
        // make sure the list is not limited to 20:
        assertEquals(21, possibleUsers.size());

        // cleanup:
        deleteGroups(group);
        deleteRoles(role);
        deleteUsers(users);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "BS-8392", classes = { User.class, HumanTaskInstance.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "possible users",
            "human task" })
    @Test
    public void searchPossibleUsersOfTaskSubGroupActor() throws Exception {
        loginOnDefaultTenantWith(USERNAME, PASSWORD);
        final Group group = createGroup("group");
        final Group group2 = createGroup("gr", group.getPath());
        final Role role = createRole("role");
        final User jaakko = createUser("jaakko", PASSWORD);
        createUserMembership(jack.getUserName(), role.getName(), group.getPath());
        createUserMembership(jaakko.getUserName(), role.getName(), group2.getPath());

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("assign", "5.0");
        designProcessDefinition.addActor("acme");
        designProcessDefinition.addUserTask("step1", "acme");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), "acme", group);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(processInstance, "step1");

        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(UserSearchDescriptor.USER_NAME, Order.ASC);

        SearchResult<User> searchResult = getProcessAPI().searchUsersWhoCanExecutePendingHumanTask(step1Id, builder.done());
        assertEquals(2, searchResult.getCount());
        List<User> possibleUsers = searchResult.getResult();

        assertEquals(2, possibleUsers.size());
        assertEquals(jaakko, possibleUsers.get(0));
        assertEquals(jack, possibleUsers.get(1));

        builder = new SearchOptionsBuilder(1, 10);
        builder.sort(UserSearchDescriptor.USER_NAME, Order.ASC);

        searchResult = getProcessAPI().searchUsersWhoCanExecutePendingHumanTask(step1Id, builder.done());
        assertEquals(2, searchResult.getCount());
        possibleUsers = searchResult.getResult();
        assertEquals(1, possibleUsers.size());
        assertEquals(jack, possibleUsers.get(0));

        // cleanup:
        deleteGroups(group);
        deleteRoles(role);
        deleteUser(jaakko);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "BS-8392", classes = { User.class, HumanTaskInstance.class, UserFilter.class }, concept = BPMNConcept.ACTIVITIES, keywords = {
            "possible users", "human task" })
    @Test
    public void searchPossibleUsersOfFilteredTask() throws Exception {
        loginOnDefaultTenantWith(USERNAME, PASSWORD);
        final Group group = createGroup("group");
        final Group group2 = createGroup("gr", group.getPath());
        final Role role = createRole("role");
        final User jaakko = createUser("jaakko", PASSWORD);
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

        final long step1Id = waitForUserTask(processInstance, "step1");
        waitForUserTask(processInstance, "step2");
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 2);
        builder.sort(UserSearchDescriptor.LAST_NAME, Order.DESC);

        final SearchResult<User> searchResult = getProcessAPI().searchUsersWhoCanExecutePendingHumanTask(step1Id, builder.done());
        assertEquals(1, searchResult.getCount());
        final List<User> possibleUsers = searchResult.getResult();

        assertEquals(1, possibleUsers.size());
        assertEquals(jaakko, possibleUsers.get(0));

        // cleanup:
        deleteGroups(group);
        deleteRoles(role);
        deleteUser(jaakko);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "BS-8392", classes = { User.class, HumanTaskInstance.class, UserFilter.class }, concept = BPMNConcept.ACTIVITIES, keywords = {
            "possible users", "human task" })
    @Test
    public void searchPossibleUsersShouldReturnThoseStartingWithSearchedNamed() throws Exception {
        loginOnDefaultTenantWith(USERNAME, PASSWORD);
        final Group group = createGroup("group");
        final Group group2 = createGroup("gr", group.getPath());
        final Role role = createRole("role");
        final User jaakko = createUser("jaakko", PASSWORD);
        createUserMembership(jack.getUserName(), role.getName(), group.getPath());
        createUserMembership(jaakko.getUserName(), role.getName(), group2.getPath());

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("assign", "5.0");
        designProcessDefinition.addActor("acme");
        designProcessDefinition.addUserTask("step1", "acme");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), "acme", group);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(processInstance, "step1");

        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(UserSearchDescriptor.USER_NAME, Order.ASC);
        builder.searchTerm("jac");

        SearchResult<User> searchResult = getProcessAPI().searchUsersWhoCanExecutePendingHumanTask(step1Id, builder.done());
        assertEquals(1, searchResult.getCount());
        List<User> possibleUsers = searchResult.getResult();

        assertEquals(1, possibleUsers.size());
        assertEquals(jack, possibleUsers.get(0));

        builder = new SearchOptionsBuilder(0, 1);
        builder.sort(UserSearchDescriptor.USER_NAME, Order.ASC);
        builder.searchTerm("jaa");

        searchResult = getProcessAPI().searchUsersWhoCanExecutePendingHumanTask(step1Id, builder.done());
        assertEquals(1, searchResult.getCount());
        possibleUsers = searchResult.getResult();
        assertEquals(1, possibleUsers.size());
        assertEquals(jaakko, possibleUsers.get(0));

        // cleanup:
        deleteGroups(group);
        deleteRoles(role);
        deleteUser(jaakko);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "BS-8392", classes = { User.class, HumanTaskInstance.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "possible users",
            "human task" })
    @Test
    public void getPossibleUsersOfUnknownTask() throws Exception {
        loginOnDefaultTenantWith(USERNAME, PASSWORD);
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(UserSearchDescriptor.LAST_NAME, Order.DESC);

        final SearchResult<User> searchResult = getProcessAPI().searchUsersWhoCanExecutePendingHumanTask(-156L, builder.done());
        assertEquals(0, searchResult.getCount());
        final List<User> possibleUsers = searchResult.getResult();
        assertTrue(CollectionUtils.isEmpty(possibleUsers));
    }

    @Test
    public void test() throws Exception {
        final Group mainGroup = createGroup("main");
        final Role member = createRole("member");
        final UserMembership m1 = getIdentityAPI().addUserMembership(john.getId(), mainGroup.getId(), member.getId());

        final ProcessDefinition processDefinition = deployProcessMappedToGroup(mainGroup);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance.getId(), "Request");

        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(startProcess, "Request", john);

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("secondProcess", "1.0");
        processBuilder.addActor("myActor").addActor("myActor2");
        processBuilder.addUserTask("Request1", "myActor2");
        processBuilder.addUserTask("Request2", "myActor");
        processBuilder.addTransition("Request2", "Request1");

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processBuilder.done(), Arrays.asList("myActor", "myActor2"),
                Arrays.asList(john, jack));
        final ProcessInstance instance = getProcessAPI().startProcess(definition.getId());
        waitForUserTaskAndAssigneIt(instance, "Request2", john);

        List<HumanTaskInstance> pendingHumanTaskInstances = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertThat(pendingHumanTaskInstances).hasSize(1);

        getProcessAPI().disableProcess(processDefinition.getId());
        pendingHumanTaskInstances = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertThat(pendingHumanTaskInstances).hasSize(1);

        disableAndDeleteProcess(definition);
        deleteProcess(processDefinition);
        deleteUserMembership(m1.getId());
        deleteGroups(mainGroup);
        deleteRoles(member);
    }

}
