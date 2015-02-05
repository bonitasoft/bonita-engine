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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.TestWithTechnicalUser;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.HumanTaskDefinition;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.connectors.VariableStorage;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.filter.UserFilter;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GetPossibleUsersOfPendingHumanTaskIT extends TestWithTechnicalUser {

    private static final String JOHN = "john";

    private static final String JACK = "jack";

    private User john;

    private User jack;

    private Group group;

    private Role role;

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        john = createUser(JOHN, "bpm");
        jack = createUser(JACK, "bpm");
        group = createGroup("group");
        role = createRole("role");
        loginOnDefaultTenantWith(JOHN, "bpm");
    }

    @Override
    @After
    public void after() throws Exception {
        deleteUser(JOHN);
        deleteUser(JACK);
        deleteGroups(group);
        deleteRoles(role);
        VariableStorage.clearAll();
        super.after();
    }

    @Cover(jira = "ENGINE-1819", classes = { User.class, HumanTaskInstance.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "possible users",
            "human task" })
    @Test
    public void getPossibleUsersOfTaskUserActor() throws Exception {
        final long userMembershipId = getIdentityAPI().addUserMembership(jack.getId(), group.getId(), role.getId()).getId();

        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("step1"),
                Arrays.asList(true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, jack);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(processInstance, "step1");

        final List<User> possibleUsers = getProcessAPI().getPossibleUsersOfPendingHumanTask(step1Id, 0, 10);
        assertEquals(1, possibleUsers.size());
        assertEquals(jack, possibleUsers.get(0));

        // cleanup:
        getIdentityAPI().deleteUserMembership(userMembershipId);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1819", classes = { User.class, HumanTaskInstance.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "possible users",
            "human task" })
    @Test
    public void getPossibleUsersOfTaskUserActorWithoutMembership() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("step1"),
                Arrays.asList(true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, jack);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(processInstance, "step1");

        final List<User> possibleUsers = getProcessAPI().getPossibleUsersOfPendingHumanTask(step1Id, 0, 10);
        assertEquals(1, possibleUsers.size());
        assertEquals(jack, possibleUsers.get(0));

        // cleanup:
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1819", classes = { User.class, HumanTaskInstance.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "possible users",
            "human task" })
    @Test
    public void getPossibleUsersOfTaskRoleActor() throws Exception {
        final long userMembershipId = getIdentityAPI().addUserMembership(jack.getId(), group.getId(), role.getId()).getId();

        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("step1"),
                Arrays.asList(true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, role);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(processInstance, "step1");

        final List<User> possibleUsers = getProcessAPI().getPossibleUsersOfPendingHumanTask(step1Id, 0, 10);
        assertEquals(1, possibleUsers.size());
        assertEquals(jack, possibleUsers.get(0));

        // cleanup:
        getIdentityAPI().deleteUserMembership(userMembershipId);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1819", classes = { User.class, HumanTaskInstance.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "possible users",
            "human task" })
    @Test
    public void getPossibleUsersOfTaskGroupActor() throws Exception {
        final long userMembershipId = getIdentityAPI().addUserMembership(jack.getId(), group.getId(), role.getId()).getId();

        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("step1"),
                Arrays.asList(true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, group);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(processInstance, "step1");

        final List<User> possibleUsers = getProcessAPI().getPossibleUsersOfPendingHumanTask(step1Id, 0, 10);
        assertEquals(1, possibleUsers.size());
        assertEquals(jack, possibleUsers.get(0));

        // cleanup:
        getIdentityAPI().deleteUserMembership(userMembershipId);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "BS-6798", classes = { ProcessAPI.class }, concept = BPMNConcept.ACTOR, keywords = { "possible users", "pagination" })
    @Test
    public void getPossibleUsersOfTaskShouldReturnAllUsersInThePaginationRange() throws Exception {
        final int nbUsers = 21;
        final List<User> users = new ArrayList<User>(nbUsers);
        final List<Long> userMembershipIds = new ArrayList<Long>(nbUsers);
        for (int i = 0; i < nbUsers; i++) {
            final User newUser = createUser("user_" + i, "pwd");
            users.add(newUser);
            userMembershipIds.add(createUserMembership(newUser.getUserName(), role.getName(), group.getName()).getId());
        }

        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("step1"),
                Arrays.asList(true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, users);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(processInstance, "step1");

        final List<User> possibleUsers = getProcessAPI().getPossibleUsersOfPendingHumanTask(step1Id, 0, 30);
        // make sure the list is not limited to 20:
        assertEquals(21, possibleUsers.size());

        // cleanup:
        disableAndDeleteProcess(processDefinition);
        for (final Long userMembershipId : userMembershipIds) {
            getIdentityAPI().deleteUserMembership(userMembershipId);
        }
        deleteUsers(users);
    }

    @Cover(jira = "ENGINE-1819", classes = { User.class, HumanTaskInstance.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "possible users",
            "human task" })
    @Test
    public void getPossibleUsersOfTaskSubGroupActor() throws Exception {
        final Group group2 = createGroup("gr", group.getPath());
        final long userMembershipId1 = getIdentityAPI().addUserMembership(jack.getId(), group.getId(), role.getId()).getId();
        final long userMembershipId2 = getIdentityAPI().addUserMembership(john.getId(), group2.getId(), role.getId()).getId();

        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("step1"),
                Arrays.asList(true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, group);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(processInstance, "step1");

        List<User> possibleUsers = getProcessAPI().getPossibleUsersOfPendingHumanTask(step1Id, 0, 10);
        assertEquals(2, possibleUsers.size());
        assertEquals(jack, possibleUsers.get(0));
        assertEquals(john, possibleUsers.get(1));

        possibleUsers = getProcessAPI().getPossibleUsersOfPendingHumanTask(step1Id, 1, 10);
        assertEquals(1, possibleUsers.size());
        assertEquals(john, possibleUsers.get(0));

        // cleanup:
        getIdentityAPI().deleteUserMembership(userMembershipId1);
        getIdentityAPI().deleteUserMembership(userMembershipId2);
        deleteGroups(group2);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1819", classes = { User.class, HumanTaskInstance.class, UserFilter.class }, concept = BPMNConcept.ACTIVITIES, keywords = {
            "possible users", "human task" })
    @Test
    public void getPossibleUsersOfFilteredTask() throws Exception {
        final Group group2 = createGroup("gr", group.getPath());
        final long userMembershipId1 = getIdentityAPI().addUserMembership(jack.getId(), group2.getId(), role.getId()).getId();
        final long userMembershipId2 = getIdentityAPI().addUserMembership(john.getId(), group2.getId(), role.getId()).getId();

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("assign", "5.2");
        designProcessDefinition.addActor(ACTOR_NAME);
        final UserTaskDefinitionBuilder taskDefinitionBuilder = designProcessDefinition.addUserTask("step1", ACTOR_NAME);
        taskDefinitionBuilder.addUserFilter("test", "org.bonitasoft.engine.filter.user.testFilter", "1.0").addInput("userId",
                new ExpressionBuilder().createConstantLongExpression(john.getId()));
        final UserTaskDefinitionBuilder definitionBuilder = designProcessDefinition.addUserTask("step2", ACTOR_NAME);
        definitionBuilder.addUserFilter("test", "org.bonitasoft.engine.filter.user.testFilter", "1.0").addInput("userId",
                new ExpressionBuilder().createConstantLongExpression(jack.getId()));
        final ProcessDefinition processDefinition = deployProcessWithTestFilter(designProcessDefinition, ACTOR_NAME, john, "TestFilter");
        getProcessAPI().addUserToActor(ACTOR_NAME, processDefinition, jack.getId());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final long step1Id = waitForUserTask(processInstance, "step1");
        waitForUserTask(processInstance, "step2");
        final List<User> possibleUsers = getProcessAPI().getPossibleUsersOfPendingHumanTask(step1Id, 0, 2);
        assertEquals(1, possibleUsers.size());
        assertEquals(john, possibleUsers.get(0));

        // cleanup:
        getIdentityAPI().deleteUserMembership(userMembershipId1);
        getIdentityAPI().deleteUserMembership(userMembershipId2);
        deleteGroups(group2);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1819", classes = { User.class, HumanTaskInstance.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "possible users",
            "human task" })
    @Test
    public void getPossibleUsersOfUnknownTask() {
        final List<User> possibleUsers = getProcessAPI().getPossibleUsersOfPendingHumanTask(-156L, 0, 10);
        assertEquals(0, possibleUsers.size());
    }

    @Cover(jira = "ENGINE-1823", classes = { User.class, HumanTaskInstance.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "possible users",
            "human task" })
    @Test
    public void getPossibleUsersOfTaskDefinitionUserActor() throws Exception {
        final long userMembershipId = getIdentityAPI().addUserMembership(jack.getId(), group.getId(), role.getId()).getId();

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("assign", "5.0");
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addActor("emca");
        designProcessDefinition.addUserTask("step1", ACTOR_NAME);
        designProcessDefinition.addUserTask("step2", "emca");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), Arrays.asList(ACTOR_NAME, "emca"),
                Arrays.asList(john, jack));

        final List<User> possibleUsers = getProcessAPI().getPossibleUsersOfHumanTask(processDefinition.getId(), "step1", 0, 10);
        assertEquals(1, possibleUsers.size());
        assertEquals(john, possibleUsers.get(0));

        // cleanup:
        getIdentityAPI().deleteUserMembership(userMembershipId);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1823", classes = { User.class, HumanTaskDefinition.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "possible users",
            "human task" })
    @Test
    public void getPossibleUsersOfTaskDefinitionRoleActor() throws Exception {
        final long userMembershipId = getIdentityAPI().addUserMembership(jack.getId(), group.getId(), role.getId()).getId();

        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("step1"),
                Arrays.asList(true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, role);

        final List<User> possibleUsers = getProcessAPI().getPossibleUsersOfHumanTask(processDefinition.getId(), "step1", 0, 10);
        assertEquals(1, possibleUsers.size());
        assertEquals(jack, possibleUsers.get(0));

        // cleanup:
        getIdentityAPI().deleteUserMembership(userMembershipId);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1823, BS-8854", classes = { User.class, HumanTaskDefinition.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "possible users",
            "human task", "users for actor" })
    @Test
    public void getPossibleUsersOfTaskDefinitionGroupActor() throws Exception {
        //given
        final long userMembershipId1 = getIdentityAPI().addUserMembership(jack.getId(), group.getId(), role.getId()).getId();
        final long userMembershipId2 = getIdentityAPI().addUserMembership(john.getId(), group.getId(), role.getId()).getId();

        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("step1"),
                Arrays.asList(true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, group);

        //when
        final List<User> possibleUsers = getProcessAPI().getPossibleUsersOfHumanTask(processDefinition.getId(), "step1", 0, 10);
        final List<Long> userIdsForActor = getProcessAPI().getUserIdsForActor(processDefinition.getId(), ACTOR_NAME, 0, 10);

        //then
        assertThat(possibleUsers).containsOnly(jack, john);
        assertThat(userIdsForActor).containsOnly(jack.getId(), john.getId());

        // cleanup:
        getIdentityAPI().deleteUserMembership(userMembershipId1);
        getIdentityAPI().deleteUserMembership(userMembershipId2);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1823", classes = { User.class, HumanTaskDefinition.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "possible users",
            "human task" })
    @Test
    public void getPossibleUsersOfTaskDefinitionSubGroupActor() throws Exception {
        final Group group2 = createGroup("gr", group.getPath());
        final long userMembershipId1 = getIdentityAPI().addUserMembership(jack.getId(), group.getId(), role.getId()).getId();
        final long userMembershipId2 = getIdentityAPI().addUserMembership(john.getId(), group2.getId(), role.getId()).getId();

        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("step1"),
                Arrays.asList(true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, group);

        List<User> possibleUsers = getProcessAPI().getPossibleUsersOfHumanTask(processDefinition.getId(), "step1", 0, 10);
        assertEquals(2, possibleUsers.size());
        assertEquals(jack, possibleUsers.get(0));
        assertEquals(john, possibleUsers.get(1));

        possibleUsers = getProcessAPI().getPossibleUsersOfHumanTask(processDefinition.getId(), "step1", 1, 10);
        assertEquals(1, possibleUsers.size());
        assertEquals(john, possibleUsers.get(0));

        // cleanup:
        getIdentityAPI().deleteUserMembership(userMembershipId1);
        getIdentityAPI().deleteUserMembership(userMembershipId2);
        deleteGroups(group2);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1823", classes = { User.class, HumanTaskDefinition.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "possible users",
            "human task" })
    @Test
    public void getPossibleUsersOfUnknownProcessDefinition() {
        final List<User> possibleUsers = getProcessAPI().getPossibleUsersOfHumanTask(-156L, "step1", 0, 10);
        assertEquals(0, possibleUsers.size());
    }

    @Cover(jira = "ENGINE-1823", classes = { User.class, HumanTaskDefinition.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "possible users",
            "human task" })
    @Test
    public void getPossibleUsersOfUnknownTaskDefinition() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("step1"),
                Arrays.asList(true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, jack);

        final List<User> possibleUsers = getProcessAPI().getPossibleUsersOfHumanTask(processDefinition.getId(), "step83", 0, 10);
        assertEquals(0, possibleUsers.size());

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1823", classes = { User.class, HumanTaskDefinition.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "possible users",
            "human task" })
    @Test
    public void getPossibleUsersOfSystemTaskDefinition() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("auto"),
                Arrays.asList(false));
        final ProcessDefinition processDefinition = deployAndEnableProcess(designProcessDefinition);

        final List<User> possibleUsers = getProcessAPI().getPossibleUsersOfHumanTask(processDefinition.getId(), "auto", 0, 10);
        assertEquals(0, possibleUsers.size());

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1823", classes = { User.class, HumanTaskDefinition.class, UserFilter.class }, concept = BPMNConcept.ACTIVITIES, keywords = {
            "possible users", "human task" })
    @Test
    public void getPossibleUsersOfTaskDefinitionWithAFilter() throws Exception {
        final Group group2 = createGroup("gr", group.getPath());
        final long userMembershipId1 = getIdentityAPI().addUserMembership(john.getId(), group2.getId(), role.getId()).getId();
        final long userMembershipId2 = getIdentityAPI().addUserMembership(jack.getId(), group2.getId(), role.getId()).getId();

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("assign", "5.2");
        designProcessDefinition.addActor(ACTOR_NAME);
        final UserTaskDefinitionBuilder taskDefinitionBuilder = designProcessDefinition.addUserTask("step1", ACTOR_NAME);
        taskDefinitionBuilder.addUserFilter("test", "org.bonitasoft.engine.filter.user.testFilter", "1.0").addInput("userId",
                new ExpressionBuilder().createConstantLongExpression(john.getId()));
        final UserTaskDefinitionBuilder definitionBuilder = designProcessDefinition.addUserTask("step2", ACTOR_NAME);
        definitionBuilder.addUserFilter("test", "org.bonitasoft.engine.filter.user.testFilter", "1.0").addInput("userId",
                new ExpressionBuilder().createConstantLongExpression(jack.getId()));
        final ProcessDefinition processDefinition = deployProcessWithTestFilter(designProcessDefinition, ACTOR_NAME, john, "TestFilter");
        getProcessAPI().addUserToActor(ACTOR_NAME, processDefinition, jack.getId());

        List<User> possibleUsers = getProcessAPI().getPossibleUsersOfHumanTask(processDefinition.getId(), "step1", 0, 2);
        assertEquals(2, possibleUsers.size());
        assertEquals(jack, possibleUsers.get(0));
        assertEquals(john, possibleUsers.get(1));

        possibleUsers = getProcessAPI().getPossibleUsersOfHumanTask(processDefinition.getId(), "step1", 2, 4);
        assertEquals(0, possibleUsers.size());

        // cleanup:
        getIdentityAPI().deleteUserMembership(userMembershipId1);
        getIdentityAPI().deleteUserMembership(userMembershipId2);
        deleteGroups(group2);
        disableAndDeleteProcess(processDefinition);
    }

}
