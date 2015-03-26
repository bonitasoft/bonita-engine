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
package org.bonitasoft.engine.core.process.instance.model;

import static org.assertj.core.api.Assertions.*;
import static org.bonitasoft.engine.test.persistence.builder.ActorBuilder.*;
import static org.bonitasoft.engine.test.persistence.builder.ActorMemberBuilder.*;
import static org.bonitasoft.engine.test.persistence.builder.CallActivityInstanceBuilder.*;
import static org.bonitasoft.engine.test.persistence.builder.GatewayInstanceBuilder.*;
import static org.bonitasoft.engine.test.persistence.builder.PendingActivityMappingBuilder.*;
import static org.bonitasoft.engine.test.persistence.builder.ProcessInstanceBuilder.*;
import static org.bonitasoft.engine.test.persistence.builder.SupervisorBuilder.*;
import static org.bonitasoft.engine.test.persistence.builder.UserBuilder.*;
import static org.bonitasoft.engine.test.persistence.builder.UserMembershipBuilder.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.actor.mapping.model.SActorMember;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.impl.SAProcessInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SCallActivityInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SGatewayInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SProcessInstanceImpl;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.test.persistence.builder.PersistentObjectBuilder;
import org.bonitasoft.engine.test.persistence.repository.ProcessInstanceRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class ProcessInstanceQueriesTest {

    private static final long aGroupId = 654L;

    private static final long anotherGroupId = 9875L;

    private static final long aRoleId = 1235L;

    private static final long anotherRoleId = 956L;

    @Inject
    private ProcessInstanceRepository repository;

    @Test
    public void getPossibleUserIdsOfPendingTasks_should_return_users_mapped_through_user_filters() {
        final SUser expectedUser = repository.add(aUser().withId(1L).build());
        repository.add(aUser().withId(2L).build()); // not expected user
        final SPendingActivityMapping pendingActivity = repository.add(aPendingActivityMapping().withUserId(expectedUser.getId()).build());

        final List<Long> userIds = repository.getPossibleUserIdsOfPendingTasks(pendingActivity.getActivityId());

        assertThat(userIds).containsOnly(expectedUser.getId());
    }

    @Test
    public void isTaskPendingForUser_should_be_true_when_mapped_using_pending_mapping() {
        final SUser expectedUser = repository.add(aUser().withId(1L).build());
        repository.add(aUser().withId(2L).build()); // not expected user
        final SPendingActivityMapping pendingActivity = repository.add(aPendingActivityMapping().withUserId(expectedUser.getId()).build());

        boolean taskPendingForUser = repository.isTaskPendingForUser(pendingActivity.getActivityId(), expectedUser.getId());

        assertThat(taskPendingForUser).isTrue();
    }


    @Test
    public void isTaskPendingForUser_should_be_true_when_mapped_using_actor() {
        final SUser expectedUser = repository.add(aUser().build());
        SActor actor = repository.add(anActor().build());
        SActorMember actorMember = repository.add(anActorMember().withUserId(expectedUser.getId()).forActor(actor).build());
        final SPendingActivityMapping pendingActivity = repository.add(aPendingActivityMapping().withActorId(actor.getId()).build());

        boolean taskPendingForUser = repository.isTaskPendingForUser(pendingActivity.getActivityId(), expectedUser.getId());

        assertThat(taskPendingForUser).isTrue();
    }


    @Test
    public void isTaskPendingForUser_should_be_true_when_mapped_using_actor_having_role() {
        final SUser expectedUser = repository.add(aUser().build());
        repository.add(aUserMembership().forUser(expectedUser).memberOf(aGroupId,aRoleId).build());
        SActor actor = repository.add(anActor().build());
        repository.add(anActorMember().withRoleId(aRoleId).forActor(actor).build());
        final SPendingActivityMapping pendingActivity = repository.add(aPendingActivityMapping().withActorId(actor.getId()).build());

        boolean taskPendingForUser = repository.isTaskPendingForUser(pendingActivity.getActivityId(), expectedUser.getId());

        assertThat(taskPendingForUser).isTrue();
    }
    @Test
    public void isTaskPendingForUser_should_be_true_when_mapped_using_actor_having_group() {
        final SUser expectedUser = repository.add(aUser().build());
        repository.add(aUserMembership().forUser(expectedUser).memberOf(aGroupId,aRoleId).build());
        SActor actor = repository.add(anActor().build());
        repository.add(anActorMember().withGroupId(aGroupId).forActor(actor).build());
        final SPendingActivityMapping pendingActivity = repository.add(aPendingActivityMapping().withActorId(actor.getId()).build());

        boolean taskPendingForUser = repository.isTaskPendingForUser(pendingActivity.getActivityId(), expectedUser.getId());

        assertThat(taskPendingForUser).isTrue();
    }
    @Test
    public void isTaskPendingForUser_should_be_true_when_mapped_using_actor_having_membership() {
        final SUser expectedUser = repository.add(aUser().build());
        repository.add(aUserMembership().forUser(expectedUser).memberOf(anotherGroupId,anotherRoleId).build());
        SActor actor = repository.add(anActor().build());
        repository.add(anActorMember().withGroupId(anotherGroupId).withRoleId(anotherRoleId).forActor(actor).build());
        final SPendingActivityMapping pendingActivity = repository.add(aPendingActivityMapping().withActorId(actor.getId()).build());

        boolean taskPendingForUser = repository.isTaskPendingForUser(pendingActivity.getActivityId(), expectedUser.getId());

        assertThat(taskPendingForUser).isTrue();
    }

    @Test
    public void isTaskPendingForUser_should_be_false_when_not_pending() {
        final SUser expectedUser = repository.add(aUser().withId(1L).build());
        SUser notPendingUser = repository.add(aUser().withId(2L).build());// not expected user
        final SPendingActivityMapping pendingActivity = repository.add(aPendingActivityMapping().withUserId(expectedUser.getId()).build());

        boolean taskPendingForUser = repository.isTaskPendingForUser(pendingActivity.getActivityId(), notPendingUser.getId());

        assertThat(taskPendingForUser).isFalse();
    }

    @Test
    public void isTaskPendingForUser() {
        final SUser expectedUser = repository.add(aUser().withId(1L).build());
        repository.add(aUser().withId(2L).build()); // not expected user
        final SPendingActivityMapping pendingActivity = repository.add(aPendingActivityMapping().withUserId(expectedUser.getId()).build());

        boolean taskPendingForUser = repository.isTaskPendingForUser(pendingActivity.getActivityId(), expectedUser.getId());

        assertThat(taskPendingForUser).isTrue();
    }

    @Test
    public void getPossibleUserIdsOfPendingTasks_should_return_users_mapped_through_his_userid_in_actormember() {
        final SActor actor = repository.add(anActor().build());
        final SPendingActivityMapping addedPendingMapping = repository.add(aPendingActivityMapping().withActorId(actor.getId()).build());
        final SUser expectedUser = repository.add(aUser().withId(1L).build());
        repository.add(anActorMember().forActor(actor).withUserId(expectedUser.getId()).build());
        repository.add(aUser().withId(2L).build()); // not expected user

        final List<Long> userIds = repository.getPossibleUserIdsOfPendingTasks(addedPendingMapping.getActivityId());

        assertThat(userIds).containsOnly(expectedUser.getId());
    }

    @Test
    public void getPossibleUserIdsOfPendingTasks_should_return_users_mapped_through_his_groupid_in_actormember() {
        final SActor actor = repository.add(anActor().build());
        final SPendingActivityMapping addedPendingMapping = repository.add(aPendingActivityMapping().withActorId(actor.getId()).build());
        repository.add(anActorMember().forActor(actor).withGroupId(aGroupId).build());
        final SUser expectedUser = repository.add(aUser().withId(1L).build());
        repository.add(aUserMembership().forUser(expectedUser).memberOf(aGroupId, aRoleId).build());
        final SUser notExpectedUser = repository.add(aUser().withId(2L).build());
        repository.add(aUserMembership().forUser(notExpectedUser).memberOf(anotherGroupId, aRoleId).build());

        final List<Long> userIds = repository.getPossibleUserIdsOfPendingTasks(addedPendingMapping.getActivityId());

        assertThat(userIds).containsOnly(expectedUser.getId());
    }

    @Test
    public void getPossibleUserIdsOfPendingTasks_should_return_users_mapped_through_his_roleid_in_actormember() {
        final SActor actor = repository.add(anActor().build());
        final SPendingActivityMapping addedPendingMapping = repository.add(aPendingActivityMapping().withActorId(actor.getId()).build());
        repository.add(anActorMember().forActor(actor).withRoleId(aRoleId).build());
        final SUser expectedUser = repository.add(aUser().withId(1L).build());
        repository.add(aUserMembership().forUser(expectedUser).memberOf(aGroupId, aRoleId).build());
        final SUser notexpectedUser = repository.add(aUser().withId(2L).build());
        repository.add(aUserMembership().forUser(notexpectedUser).memberOf(aGroupId, anotherRoleId).build());

        final List<Long> userIds = repository.getPossibleUserIdsOfPendingTasks(addedPendingMapping.getActivityId());

        assertThat(userIds).containsOnly(expectedUser.getId());
    }

    @Test
    public void getPossibleUserIdsOfPendingTasks_should_return_users_mapped_through_his_membership_in_actormember() {
        final SUser expectedUser = repository.add(aUser().withId(1L).build());
        final SUser expectedUser2 = repository.add(aUser().withId(4L).build());
        final SUser notExpectedUser = repository.add(aUser().withId(2L).build());
        final SUser notExpectedUser2 = repository.add(aUser().withId(3L).build());
        final SActor actor = repository.add(anActor().build());
        final SPendingActivityMapping addedPendingMapping = repository.add(aPendingActivityMapping().withActorId(actor.getId()).build());
        repository.add(anActorMember().forActor(actor).withGroupId(aGroupId).withRoleId(aRoleId).build());
        repository.add(aUserMembership().forUser(expectedUser).memberOf(aGroupId, aRoleId).build());
        repository.add(aUserMembership().forUser(expectedUser2).memberOf(aGroupId, aRoleId).build());
        repository.add(aUserMembership().forUser(notExpectedUser).memberOf(anotherGroupId, aRoleId).build());
        repository.add(aUserMembership().forUser(notExpectedUser2).memberOf(aGroupId, anotherRoleId).build());

        final List<Long> userIds = repository.getPossibleUserIdsOfPendingTasks(addedPendingMapping.getActivityId());

        assertThat(userIds).containsOnly(expectedUser.getId(), expectedUser2.getId());
    }

    @Test
    public void getPossibleUserIdsOfPendingTasks_return_userIds_ordered_by_userName() {
        final SUser john = repository.add(aUser().withUserName("john").withId(1L).build());
        final SUser paul = repository.add(aUser().withUserName("paul").withId(2L).build());
        final SUser walter = repository.add(aUser().withUserName("walter").withId(3L).build());
        final SUser marie = repository.add(aUser().withUserName("marie").withId(4L).build());
        final SActor actor = repository.add(anActor().build());
        final SPendingActivityMapping addedPendingMapping = repository.add(aPendingActivityMapping().withActorId(actor.getId()).build());
        repository.add(anActorMember().forActor(actor).withGroupId(aGroupId).withRoleId(aRoleId).build());
        repository.add(aUserMembership().forUser(john).memberOf(aGroupId, aRoleId).build());
        repository.add(aUserMembership().forUser(paul).memberOf(aGroupId, aRoleId).build());
        repository.add(aUserMembership().forUser(walter).memberOf(aGroupId, aRoleId).build());
        repository.add(aUserMembership().forUser(marie).memberOf(aGroupId, aRoleId).build());

        final List<Long> userIds = repository.getPossibleUserIdsOfPendingTasks(addedPendingMapping.getActivityId());

        assertThat(userIds).containsExactly(john.getId(), marie.getId(), paul.getId(), walter.getId());
    }

    @Test
    public void getNumberOfSUserWhoCanStartPendingTask_should_return_users_mapped_through_user_filters() {
        final SUser expectedUser = repository.add(aUser().withId(1L).build());
        repository.add(aUser().withId(2L).build()); // not expected user
        final SPendingActivityMapping pendingActivity = repository.add(aPendingActivityMapping().withUserId(expectedUser.getId()).build());

        final long numberOfUsers = repository.getNumberOfSUserWhoCanStartPendingTask(pendingActivity.getActivityId());

        assertThat(numberOfUsers).isEqualTo(1);
    }

    @Test
    public void getNumberOfSUserWhoCanStartPendingTask_should_return_users_mapped_through_his_userid_in_actormember() {
        final SActor actor = repository.add(anActor().build());
        final SPendingActivityMapping addedPendingMapping = repository.add(aPendingActivityMapping().withActorId(actor.getId()).build());
        final SUser expectedUser = repository.add(aUser().withId(1L).build());
        repository.add(anActorMember().forActor(actor).withUserId(expectedUser.getId()).build());
        repository.add(aUser().withId(2L).build()); // not expected user

        final long numberOfUsers = repository.getNumberOfSUserWhoCanStartPendingTask(addedPendingMapping.getActivityId());

        assertThat(numberOfUsers).isEqualTo(1);
    }

    @Test
    public void getNumberOfSUserWhoCanStartPendingTask_should_return_users_mapped_through_his_groupid_in_actormember() {
        final SActor actor = repository.add(anActor().build());
        final SPendingActivityMapping addedPendingMapping = repository.add(aPendingActivityMapping().withActorId(actor.getId()).build());
        repository.add(anActorMember().forActor(actor).withGroupId(aGroupId).build());
        final SUser expectedUser = repository.add(aUser().withId(1L).build());
        repository.add(aUserMembership().forUser(expectedUser).memberOf(aGroupId, aRoleId).build());
        final SUser notExpectedUser = repository.add(aUser().withId(2L).build());
        repository.add(aUserMembership().forUser(notExpectedUser).memberOf(anotherGroupId, aRoleId).build());

        final long numberOfUsers = repository.getNumberOfSUserWhoCanStartPendingTask(addedPendingMapping.getActivityId());

        assertThat(numberOfUsers).isEqualTo(1);
    }

    @Test
    public void getNumberOfSUserWhoCanStartPendingTask_should_return_users_mapped_through_his_roleid_in_actormember() {
        final SActor actor = repository.add(anActor().build());
        final SPendingActivityMapping addedPendingMapping = repository.add(aPendingActivityMapping().withActorId(actor.getId()).build());
        repository.add(anActorMember().forActor(actor).withRoleId(aRoleId).build());
        final SUser expectedUser = repository.add(aUser().withId(1L).build());
        repository.add(aUserMembership().forUser(expectedUser).memberOf(aGroupId, aRoleId).build());
        final SUser notexpectedUser = repository.add(aUser().withId(2L).build());
        repository.add(aUserMembership().forUser(notexpectedUser).memberOf(aGroupId, anotherRoleId).build());

        final long numberOfUsers = repository.getNumberOfSUserWhoCanStartPendingTask(addedPendingMapping.getActivityId());

        assertThat(numberOfUsers).isEqualTo(1);
    }

    @Test
    public void getNumberOfSUserWhoCanStartPendingTask_should_return_users_mapped_through_his_membership_in_actormember() {
        final SUser expectedUser = repository.add(aUser().withId(1L).build());
        final SUser expectedUser2 = repository.add(aUser().withId(4L).build());
        final SUser notExpectedUser = repository.add(aUser().withId(2L).build());
        final SUser notExpectedUser2 = repository.add(aUser().withId(3L).build());
        final SActor actor = repository.add(anActor().build());
        final SPendingActivityMapping addedPendingMapping = repository.add(aPendingActivityMapping().withActorId(actor.getId()).build());
        repository.add(anActorMember().forActor(actor).withGroupId(aGroupId).withRoleId(aRoleId).build());
        repository.add(aUserMembership().forUser(expectedUser).memberOf(aGroupId, aRoleId).build());
        repository.add(aUserMembership().forUser(expectedUser2).memberOf(aGroupId, aRoleId).build());
        repository.add(aUserMembership().forUser(notExpectedUser).memberOf(anotherGroupId, aRoleId).build());
        repository.add(aUserMembership().forUser(notExpectedUser2).memberOf(aGroupId, anotherRoleId).build());

        final long numberOfUsers = repository.getNumberOfSUserWhoCanStartPendingTask(addedPendingMapping.getActivityId());

        assertThat(numberOfUsers).isEqualTo(2);
    }

    @Test
    public void getNumberOfSUserWhoCanStartPendingTask_return_userIds_ordered_by_userName() {
        final SUser john = repository.add(aUser().withUserName("john").withId(1L).build());
        final SUser paul = repository.add(aUser().withUserName("paul").withId(2L).build());
        final SUser walter = repository.add(aUser().withUserName("walter").withId(3L).build());
        final SUser marie = repository.add(aUser().withUserName("marie").withId(4L).build());
        final SActor actor = repository.add(anActor().build());
        final SPendingActivityMapping addedPendingMapping = repository.add(aPendingActivityMapping().withActorId(actor.getId()).build());
        repository.add(anActorMember().forActor(actor).withGroupId(aGroupId).withRoleId(aRoleId).build());
        repository.add(aUserMembership().forUser(john).memberOf(aGroupId, aRoleId).build());
        repository.add(aUserMembership().forUser(paul).memberOf(aGroupId, aRoleId).build());
        repository.add(aUserMembership().forUser(walter).memberOf(aGroupId, aRoleId).build());
        repository.add(aUserMembership().forUser(marie).memberOf(aGroupId, aRoleId).build());

        final long numberOfUsers = repository.getNumberOfSUserWhoCanStartPendingTask(addedPendingMapping.getActivityId());

        assertThat(numberOfUsers).isEqualTo(4);
    }

    @Test
    public void searchSUserWhoCanStartPendingTask_should_return_users_mapped_through_user_filters() {
        final SUser expectedUser = repository.add(aUser().withId(1L).build());
        repository.add(aUser().withId(2L).build()); // not expected user
        final SPendingActivityMapping pendingActivity = repository.add(aPendingActivityMapping().withUserId(expectedUser.getId()).build());

        final List<SUser> userIds = repository.searchPossibleUserIdsOfPendingTasks(pendingActivity.getActivityId());

        assertThat(userIds).containsOnly(expectedUser);
    }

    @Test
    public void searchSUserWhoCanStartPendingTask_should_return_users_mapped_through_his_userid_in_actormember() {
        final SActor actor = repository.add(anActor().build());
        final SPendingActivityMapping addedPendingMapping = repository.add(aPendingActivityMapping().withActorId(actor.getId()).build());
        final SUser expectedUser = repository.add(aUser().withId(1L).build());
        repository.add(anActorMember().forActor(actor).withUserId(expectedUser.getId()).build());
        repository.add(aUser().withId(2L).build()); // not expected user

        final List<SUser> userIds = repository.searchPossibleUserIdsOfPendingTasks(addedPendingMapping.getActivityId());

        assertThat(userIds).containsOnly(expectedUser);
    }

    @Test
    public void searchSUserWhoCanStartPendingTask_should_return_users_mapped_through_his_groupid_in_actormember() {
        final SActor actor = repository.add(anActor().build());
        final SPendingActivityMapping addedPendingMapping = repository.add(aPendingActivityMapping().withActorId(actor.getId()).build());
        repository.add(anActorMember().forActor(actor).withGroupId(aGroupId).build());
        final SUser expectedUser = repository.add(aUser().withId(1L).build());
        repository.add(aUserMembership().forUser(expectedUser).memberOf(aGroupId, aRoleId).build());
        final SUser notExpectedUser = repository.add(aUser().withId(2L).build());
        repository.add(aUserMembership().forUser(notExpectedUser).memberOf(anotherGroupId, aRoleId).build());

        final List<SUser> userIds = repository.searchPossibleUserIdsOfPendingTasks(addedPendingMapping.getActivityId());

        assertThat(userIds).containsOnly(expectedUser);
    }

    @Test
    public void searchSUserWhoCanStartPendingTask_should_return_users_mapped_through_his_roleid_in_actormember() {
        final SActor actor = repository.add(anActor().build());
        final SPendingActivityMapping addedPendingMapping = repository.add(aPendingActivityMapping().withActorId(actor.getId()).build());
        repository.add(anActorMember().forActor(actor).withRoleId(aRoleId).build());
        final SUser expectedUser = repository.add(aUser().withId(1L).build());
        repository.add(aUserMembership().forUser(expectedUser).memberOf(aGroupId, aRoleId).build());
        final SUser notexpectedUser = repository.add(aUser().withId(2L).build());
        repository.add(aUserMembership().forUser(notexpectedUser).memberOf(aGroupId, anotherRoleId).build());

        final List<SUser> userIds = repository.searchPossibleUserIdsOfPendingTasks(addedPendingMapping.getActivityId());

        assertThat(userIds).containsOnly(expectedUser);
    }

    @Test
    public void searchSUserWhoCanStartPendingTask_should_return_users_mapped_through_his_membership_in_actormember() {
        final SUser expectedUser = repository.add(aUser().withId(1L).build());
        final SUser expectedUser2 = repository.add(aUser().withId(4L).build());
        final SUser notExpectedUser = repository.add(aUser().withId(2L).build());
        final SUser notExpectedUser2 = repository.add(aUser().withId(3L).build());
        final SActor actor = repository.add(anActor().build());
        final SPendingActivityMapping addedPendingMapping = repository.add(aPendingActivityMapping().withActorId(actor.getId()).build());
        repository.add(anActorMember().forActor(actor).withGroupId(aGroupId).withRoleId(aRoleId).build());
        repository.add(aUserMembership().forUser(expectedUser).memberOf(aGroupId, aRoleId).build());
        repository.add(aUserMembership().forUser(expectedUser2).memberOf(aGroupId, aRoleId).build());
        repository.add(aUserMembership().forUser(notExpectedUser).memberOf(anotherGroupId, aRoleId).build());
        repository.add(aUserMembership().forUser(notExpectedUser2).memberOf(aGroupId, anotherRoleId).build());

        final List<SUser> userIds = repository.searchPossibleUserIdsOfPendingTasks(addedPendingMapping.getActivityId());

        assertThat(userIds).containsOnly(expectedUser, expectedUser2);
    }

    @Test
    public void searchSUserWhoCanStartPendingTask_return_userIds_ordered_by_userName() {
        final SUser john = repository.add(aUser().withUserName("john").withId(1L).build());
        final SUser paul = repository.add(aUser().withUserName("paul").withId(2L).build());
        final SUser walter = repository.add(aUser().withUserName("walter").withId(3L).build());
        final SUser marie = repository.add(aUser().withUserName("marie").withId(4L).build());
        final SActor actor = repository.add(anActor().build());
        final SPendingActivityMapping addedPendingMapping = repository.add(aPendingActivityMapping().withActorId(actor.getId()).build());
        repository.add(anActorMember().forActor(actor).withGroupId(aGroupId).withRoleId(aRoleId).build());
        repository.add(aUserMembership().forUser(john).memberOf(aGroupId, aRoleId).build());
        repository.add(aUserMembership().forUser(paul).memberOf(aGroupId, aRoleId).build());
        repository.add(aUserMembership().forUser(walter).memberOf(aGroupId, aRoleId).build());
        repository.add(aUserMembership().forUser(marie).memberOf(aGroupId, aRoleId).build());

        final List<SUser> users = repository.searchPossibleUserIdsOfPendingTasks(addedPendingMapping.getActivityId());

        assertThat(users).hasSize(4).contains(john, marie, paul, walter);
    }

    @Test
    public void searchSingleChildrenSProcessInstanceOfProcessInstance_return_processInstancesIds() {
        final SProcessInstance parentPI = repository.add(aProcessInstance().withContainerId(1).withName("test Parent Process Instance")
                .withStateId(ProcessInstanceState.STARTED.getId()).build());
        final SCallActivityInstance callActivity = (SCallActivityInstanceImpl) repository.add(aCallActivityInstanceBuilder()
                .withLogicalGroup4(parentPI.getId())
                .withName("call Activity")
                .build());
        final SProcessInstance childPI = repository.add(aProcessInstance().withContainerId(1).withName("test Child Process Instance")
                .withCallerId(callActivity.getId()).withCallerType(SFlowNodeType.CALL_ACTIVITY)
                .withStateId(ProcessInstanceState.STARTED.getId()).build());

        assertThat(repository.countChildrenInstanceIdsOfProcessInstance(parentPI.getId())).isEqualTo(1);
        final List<Long> piIds = repository.getChildrenInstanceIdsOfProcessInstance(parentPI.getId());
        assertThat(piIds).isNotEmpty().containsExactly(childPI.getId());
    }

    @Test
    public void searchOnlyChildrenSProcessInstanceOfProcessInstance_return_processInstancesIdsFromCallActivity() {
        final SProcessInstance parentPI = repository.add(aProcessInstance().withContainerId(1).withName("test Parent Process Instance")
                .withStateId(ProcessInstanceState.STARTED.getId()).build());
        final SCallActivityInstance callActivity = (SCallActivityInstanceImpl) repository.add(aCallActivityInstanceBuilder()
                .withLogicalGroup4(parentPI.getId())
                .withName("call Activity")
                .build());
        final SGatewayInstance gatewayActivity = (SGatewayInstance) repository.add(aGatewayInstanceBuilder().withLogicalGroup4(parentPI.getId())
                .withName("call Activity")
                .build());
        final SProcessInstance childPI = repository.add(aProcessInstance().withContainerId(1).withName("test Child Process Instance")
                .withCallerId(callActivity.getId()).withCallerType(SFlowNodeType.CALL_ACTIVITY)
                .withStateId(ProcessInstanceState.STARTED.getId()).build());
        repository.add(aProcessInstance().withContainerId(1).withName("test Parent Process Instance")
                .withCallerId(gatewayActivity.getId()).withCallerType(SFlowNodeType.GATEWAY)
                .withStateId(ProcessInstanceState.STARTED.getId()).build());

        assertThat(repository.countChildrenInstanceIdsOfProcessInstance(parentPI.getId())).isEqualTo(1);
        final List<Long> piIds = repository.getChildrenInstanceIdsOfProcessInstance(parentPI.getId());
        assertThat(piIds).isNotEmpty().containsExactly(childPI.getId());
    }

    @Test
    public void searchChildProcessInstanceOfProcessInstance_return_processInstancesIdsNotGrandChildren() {
        final SProcessInstance parentPI = repository.add(aProcessInstance().withContainerId(1).withName("test Parent Process Instance")
                .withStateId(ProcessInstanceState.STARTED.getId()).build());
        final SCallActivityInstance callActivity = (SCallActivityInstanceImpl) repository.add(aCallActivityInstanceBuilder()
                .withLogicalGroup4(parentPI.getId())
                .withName("call Activity")
                .build());
        final SProcessInstance childPI = repository.add(aProcessInstance().withContainerId(1).withName("test Child Process Instance Started")
                .withCallerId(callActivity.getId()).withCallerType(SFlowNodeType.CALL_ACTIVITY)
                .withStateId(ProcessInstanceState.STARTED.getId()).build());
        final SCallActivityInstance callSubActivity = (SCallActivityInstanceImpl) repository.add(aCallActivityInstanceBuilder()
                .withLogicalGroup4(childPI.getId())
                .withName("call Activity")
                .build());
        repository.add(aProcessInstance().withContainerId(1).withName("test Grand Child Process Instance Started")
                .withCallerId(callSubActivity.getId()).withCallerType(SFlowNodeType.CALL_ACTIVITY)
                .withStateId(ProcessInstanceState.CANCELLED.getId()).build());

        assertThat(repository.countChildrenInstanceIdsOfProcessInstance(parentPI.getId())).isEqualTo(1);
        final List<Long> piIds = repository.getChildrenInstanceIdsOfProcessInstance(parentPI.getId());
        assertThat(piIds).isNotEmpty().containsOnly(childPI.getId());
    }

    @Test
    public void searchGrandChildProcessInstanceOfChildProcessInstance_return_processInstancesIdsNotChild() {
        final SProcessInstance parentPI = repository.add(aProcessInstance().withContainerId(1).withName("test Parent Process Instance")
                .withStateId(ProcessInstanceState.STARTED.getId()).build());
        final SCallActivityInstance callActivity = (SCallActivityInstanceImpl) repository.add(aCallActivityInstanceBuilder()
                .withLogicalGroup4(parentPI.getId())
                .withName("call Activity")
                .build());
        final SProcessInstance childPI = repository.add(aProcessInstance().withContainerId(1).withName("test Child Process Instance Started")
                .withCallerId(callActivity.getId()).withCallerType(SFlowNodeType.CALL_ACTIVITY)
                .withStateId(ProcessInstanceState.STARTED.getId()).build());
        final SCallActivityInstance callSubActivity = (SCallActivityInstanceImpl) repository.add(aCallActivityInstanceBuilder()
                .withLogicalGroup4(childPI.getId())
                .withName("call Activity")
                .build());
        final SProcessInstance grandChildPI = repository.add(aProcessInstance().withContainerId(1).withName("test Grand Child Process Instance Started")
                .withCallerId(callSubActivity.getId()).withCallerType(SFlowNodeType.CALL_ACTIVITY)
                .withStateId(ProcessInstanceState.CANCELLED.getId()).build());

        assertThat(repository.countChildrenInstanceIdsOfProcessInstance(childPI.getId())).isEqualTo(1);
        final List<Long> piIds = repository.getChildrenInstanceIdsOfProcessInstance(childPI.getId());
        assertThat(piIds).isNotEmpty().containsOnly(grandChildPI.getId());
    }

    @Test
    public void searchChildrenSProcessInstanceOfProcessInstance_return_processInstancesIds() {
        final SProcessInstance parentPI = repository.add(aProcessInstance().withContainerId(1).withName("test Parent Process Instance")
                .withStateId(ProcessInstanceState.STARTED.getId()).build());
        final SCallActivityInstance callActivity = (SCallActivityInstanceImpl) repository.add(aCallActivityInstanceBuilder()
                .withLogicalGroup4(parentPI.getId())
                .withName("call Activity")
                .build());
        final SProcessInstance childPI1 = repository.add(aProcessInstance().withContainerId(1).withName("test Child Process Instance Started")
                .withCallerId(callActivity.getId()).withCallerType(SFlowNodeType.CALL_ACTIVITY)
                .withStateId(ProcessInstanceState.STARTED.getId()).build());
        final SProcessInstance childPI2 = repository.add(aProcessInstance().withContainerId(1).withName("test Child Process Instance Started")
                .withCallerId(callActivity.getId()).withCallerType(SFlowNodeType.CALL_ACTIVITY)
                .withStateId(ProcessInstanceState.COMPLETED.getId()).build());
        final SProcessInstance childPI3 = repository.add(aProcessInstance().withContainerId(1).withName("test Child Process Instance Started")
                .withCallerId(callActivity.getId()).withCallerType(SFlowNodeType.CALL_ACTIVITY)
                .withStateId(ProcessInstanceState.CANCELLED.getId()).build());

        assertThat(repository.countChildrenInstanceIdsOfProcessInstance(parentPI.getId())).isEqualTo(3);
        final List<Long> piIds = repository.getChildrenInstanceIdsOfProcessInstance(parentPI.getId());
        assertThat(piIds).isNotEmpty().containsOnly(childPI1.getId(), childPI2.getId(), childPI3.getId());
    }

    @Test
    public void searchChildSProcessInstanceOfProcessInstance_return_processInstancesIdsFromParentPIOnly() {
        final SProcessInstance parentPI = repository.add(aProcessInstance().withContainerId(1).withName("test Parent Process Instance")
                .withStateId(ProcessInstanceState.STARTED.getId()).build());
        repository.add(aProcessInstance().withContainerId(1).withName("test Process Instance Independent Started")
                .withStateId(ProcessInstanceState.STARTED.getId()).build());
        repository.add(aProcessInstance().withContainerId(1).withName("test Process Instance Independent Completed")
                .withStateId(ProcessInstanceState.COMPLETED.getId()).build());
        repository.add(aProcessInstance().withContainerId(1).withName("test Process Instance Independent Cancelled")
                .withStateId(ProcessInstanceState.CANCELLED.getId()).build());
        final SCallActivityInstance callActivity = (SCallActivityInstanceImpl) repository.add(aCallActivityInstanceBuilder()
                .withLogicalGroup4(parentPI.getId())
                .withName("call Activity")
                .build());
        final SProcessInstance childPI = repository.add(aProcessInstance().withContainerId(1).withName("test Child Process Instance")
                .withCallerId(callActivity.getId()).withCallerType(SFlowNodeType.CALL_ACTIVITY)
                .withStateId(ProcessInstanceState.STARTED.getId()).build());

        assertThat(repository.countChildrenInstanceIdsOfProcessInstance(parentPI.getId())).isEqualTo(1);
        final List<Long> piIds = repository.getChildrenInstanceIdsOfProcessInstance(parentPI.getId());
        assertThat(piIds).isNotEmpty().containsExactly(childPI.getId());
    }

    @Test
    public void getNumberOfProcessInstances_should_return_the_number_of_running_instances_of_a_process_definition() {
        repository.add(aProcessInstance().withProcessDefinitionId(45l).build());
        repository.add(aProcessInstance().withProcessDefinitionId(45l).build());
        repository.add(aProcessInstance().withProcessDefinitionId(45l).build());
        repository.add(aProcessInstance().withProcessDefinitionId(12l).build());

        assertThat(repository.getNumberOfProcessInstances(45l)).isEqualTo(3);
    }

    @Test
    public void getNumberOfSProcessInstanceFailedShouldAcceptExtraFilters() {
        // Given
        repository.add(buildFailedProcessInstance(1, 777777L));
        repository.add(buildFailedProcessInstance(2, 777777L));
        repository.add(buildFailedProcessInstance(3, 888888L));

        // When
        final long numberOfSProcessInstanceFailed = repository.getNumberOfSProcessInstanceFailedForProcessDefinition(777777L);

        // Then
        assertEquals(2, numberOfSProcessInstanceFailed);
    }

    @Test
    public void searchSProcessInstanceFailedShouldAcceptExtraFilters() {
        // Given
        repository.add(buildFailedProcessInstance(1, 777777L));
        repository.add(buildFailedProcessInstance(2, 777777L));
        repository.add(buildFailedProcessInstance(3, 888888L));

        // When
        final List<SProcessInstance> sProcessInstanceFailed = repository.searchSProcessInstanceFailedForProcessDefinition(777777L);

        // Then
        assertEquals(2, sProcessInstanceFailed.size());
    }

    @Test
    public void getNumberOfSProcessInstanceFailed_should_return_number_of_distinct_process_instances() {
        // Given
        repository.add(buildFailedProcessInstance(1));

        final SProcessInstanceImpl processInstanceWithFailedFlowNode = new SProcessInstanceImpl("process2", 10L);
        processInstanceWithFailedFlowNode.setId(2);
        processInstanceWithFailedFlowNode.setTenantId(PersistentObjectBuilder.DEFAULT_TENANT_ID);
        repository.add(processInstanceWithFailedFlowNode);
        repository.add(buildFailedGateway(852, processInstanceWithFailedFlowNode.getId()));

        final SProcessInstanceImpl failedProcessInstanceWithFailedFlowNode = repository.add(buildFailedProcessInstance(3));
        repository.add(buildFailedGateway(56, failedProcessInstanceWithFailedFlowNode.getId()));

        // When
        final long numberOfSProcessInstanceFailed = repository.getNumberOfSProcessInstanceFailed();

        // Then
        assertEquals(3, numberOfSProcessInstanceFailed);
    }

    @Test
    public void getNumberOfSProcessInstanceFailed_should_return_number_of_failed_process_instances() {
        // Given
        repository.add(buildFailedProcessInstance(1));

        // When
        final long numberOfSProcessInstanceFailed = repository.getNumberOfSProcessInstanceFailed();

        // Then
        assertEquals(1, numberOfSProcessInstanceFailed);
    }

    @Test
    public void getNumberOfSProcessInstanceFailed_should_return_number_of_process_instances_with_failed_flow_nodes() {
        // Given
        final SProcessInstanceImpl processInstanceWithFailedFlowNode = new SProcessInstanceImpl("process2", 10L);
        processInstanceWithFailedFlowNode.setId(2);
        processInstanceWithFailedFlowNode.setTenantId(PersistentObjectBuilder.DEFAULT_TENANT_ID);
        repository.add(processInstanceWithFailedFlowNode);
        repository.add(buildFailedGateway(1, processInstanceWithFailedFlowNode.getId()));

        // When
        final long numberOfSProcessInstanceFailed = repository.getNumberOfSProcessInstanceFailed();

        // Then
        assertEquals(1, numberOfSProcessInstanceFailed);
    }

    @Test
    public void searchSProcessInstanceFailed_return_distinct_process_instances() {
        // Given
        final SProcessInstanceImpl failedProcessInstance = repository.add(buildFailedProcessInstance(1));

        final SProcessInstanceImpl processInstanceWithFailedFlowNode = new SProcessInstanceImpl("process2", 10L);
        processInstanceWithFailedFlowNode.setId(2);
        processInstanceWithFailedFlowNode.setTenantId(PersistentObjectBuilder.DEFAULT_TENANT_ID);
        repository.add(processInstanceWithFailedFlowNode);
        repository.add(buildFailedGateway(1, processInstanceWithFailedFlowNode.getId()));

        final SProcessInstanceImpl failedProcessInstanceWithFailedFlowNode = repository.add(buildFailedProcessInstance(3));
        repository.add(buildFailedGateway(2, failedProcessInstanceWithFailedFlowNode.getId()));

        // When
        final List<SProcessInstance> failedSProcessInstance = repository.searchSProcessInstanceFailed();

        // Then
        assertEquals(3, failedSProcessInstance.size());
        assertEquals(failedProcessInstance, failedSProcessInstance.get(0));
        assertEquals(processInstanceWithFailedFlowNode, failedSProcessInstance.get(1));
        assertEquals(failedProcessInstanceWithFailedFlowNode, failedSProcessInstance.get(2));
    }

    @Test
    public void searchSProcessInstanceFailed_return_failed_process_instances() {
        // Given
        final SProcessInstanceImpl failedProcessInstance = repository.add(buildFailedProcessInstance(1));

        // When
        final List<SProcessInstance> failedSProcessInstance = repository.searchSProcessInstanceFailed();

        // Then
        assertEquals(1, failedSProcessInstance.size());
        assertEquals(failedProcessInstance, failedSProcessInstance.get(0));
    }

    @Test
    public void searchSProcessInstanceFailed_return_process_instances_with_failed_flow_nodes() {
        // Given
        final SProcessInstanceImpl processInstanceWithFailedFlowNode = new SProcessInstanceImpl("process2", 10L);
        processInstanceWithFailedFlowNode.setId(2);
        processInstanceWithFailedFlowNode.setTenantId(PersistentObjectBuilder.DEFAULT_TENANT_ID);
        repository.add(processInstanceWithFailedFlowNode);
        repository.add(buildFailedGateway(1, processInstanceWithFailedFlowNode.getId()));

        // When
        final List<SProcessInstance> failedSProcessInstance = repository.searchSProcessInstanceFailed();

        // Then
        assertEquals(1, failedSProcessInstance.size());
        assertEquals(processInstanceWithFailedFlowNode, failedSProcessInstance.get(0));
    }

    @Test
    public void getNumberOfSProcessInstanceFailedAndSupervisedBy_should_return_number_of_distinct_process_instances() {
        // Given
        final long userId = 2L;
        repository.add(buildFailedProcessInstance(1));
        repository.add(aSupervisor().withProcessDefinitionId(9L).withUserId(userId).build());

        repository.add(aProcessInstance().withProcessDefinitionId(8L).withContainerId(1).withName("test Parent Process Instance")
                .withStateId(ProcessInstanceState.STARTED.getId()).build());
        repository.add(aSupervisor().withProcessDefinitionId(8L).withUserId(userId).build());

        SProcessInstanceImpl processInstanceWithFailedFlowNode = new SProcessInstanceImpl("process2", 10L);
        processInstanceWithFailedFlowNode.setId(2);
        processInstanceWithFailedFlowNode.setTenantId(PersistentObjectBuilder.DEFAULT_TENANT_ID);
        repository.add(processInstanceWithFailedFlowNode);
        repository.add(buildFailedGateway(852, processInstanceWithFailedFlowNode.getId()));
        repository.add(buildFailedProcessInstance(3, 11L));

        processInstanceWithFailedFlowNode = new SProcessInstanceImpl("process2", 15L);
        processInstanceWithFailedFlowNode.setId(4);
        processInstanceWithFailedFlowNode.setTenantId(PersistentObjectBuilder.DEFAULT_TENANT_ID);
        repository.add(processInstanceWithFailedFlowNode);
        repository.add(buildFailedGateway(853, processInstanceWithFailedFlowNode.getId()));
        repository.add(buildStartedProcessInstance(5, 15L));
        repository.add(aSupervisor().withProcessDefinitionId(15L).withUserId(userId).build());

        // When
        final long numberOfSProcessInstanceFailed = repository.getNumberOfFailedSProcessInstanceSupervisedBy(userId);

        // Then
        assertEquals(2, numberOfSProcessInstanceFailed);
    }

    @Test
    public void getSProcessInstanceFailedAndSupervisedBy_should_return_number_of_distinct_process_instances() {
        // Given
        final long userId = 2L;
        repository.add(buildFailedProcessInstance(1));
        repository.add(aSupervisor().withProcessDefinitionId(9L).withUserId(userId).build());

        repository.add(aProcessInstance().withProcessDefinitionId(8L).withContainerId(1).withName("test Parent Process Instance")
                .withStateId(ProcessInstanceState.STARTED.getId()).build());
        repository.add(aSupervisor().withProcessDefinitionId(8L).withUserId(userId).build());

        SProcessInstanceImpl processInstanceWithFailedFlowNode = new SProcessInstanceImpl("process2", 10L);
        processInstanceWithFailedFlowNode.setId(2);
        processInstanceWithFailedFlowNode.setTenantId(PersistentObjectBuilder.DEFAULT_TENANT_ID);
        repository.add(processInstanceWithFailedFlowNode);
        repository.add(buildFailedGateway(852, processInstanceWithFailedFlowNode.getId()));
        repository.add(buildFailedProcessInstance(3, 11L));

        processInstanceWithFailedFlowNode = new SProcessInstanceImpl("process2", 15L);
        processInstanceWithFailedFlowNode.setId(4);
        processInstanceWithFailedFlowNode.setTenantId(PersistentObjectBuilder.DEFAULT_TENANT_ID);
        repository.add(processInstanceWithFailedFlowNode);
        repository.add(buildFailedGateway(853, processInstanceWithFailedFlowNode.getId()));
        repository.add(buildStartedProcessInstance(5, 15L));
        repository.add(aSupervisor().withProcessDefinitionId(15L).withUserId(userId).build());

        // When
        final List<SProcessInstance> sProcessInstanceFailedList = repository.searchFailedSProcessInstanceSupervisedBy(userId);

        // Then
        assertThat(sProcessInstanceFailedList).hasSize(2).extracting("id").contains(1L, 4L);

    }

    private SGatewayInstanceImpl buildFailedGateway(final long gatewayId, final long parentProcessInstanceId) {
        final SGatewayInstanceImpl sGatewayInstanceImpl = new SGatewayInstanceImpl();
        sGatewayInstanceImpl.setId(gatewayId);
        sGatewayInstanceImpl.setStateId(3);
        sGatewayInstanceImpl.setLogicalGroup(3, parentProcessInstanceId);
        sGatewayInstanceImpl.setTenantId(PersistentObjectBuilder.DEFAULT_TENANT_ID);
        return sGatewayInstanceImpl;
    }

    private SProcessInstanceImpl buildFailedProcessInstance(final long processInstanceId) {
        return buildFailedProcessInstance(processInstanceId, 9L);
    }

    private SProcessInstanceImpl buildStartedProcessInstance(final long processInstanceId, final long processDefinitionId) {
        final SProcessInstanceImpl sProcessInstance = new SProcessInstanceImpl("process" + processInstanceId, processDefinitionId);
        sProcessInstance.setId(processInstanceId);
        sProcessInstance.setStateId(ProcessInstanceState.STARTED.getId());
        sProcessInstance.setTenantId(PersistentObjectBuilder.DEFAULT_TENANT_ID);
        return sProcessInstance;
    }

    private SProcessInstanceImpl buildFailedProcessInstance(final long processInstanceId, final long processDefinitionId) {
        final SProcessInstanceImpl sProcessInstance = new SProcessInstanceImpl("process" + processInstanceId, processDefinitionId);
        sProcessInstance.setId(processInstanceId);
        sProcessInstance.setStateId(7);
        sProcessInstance.setTenantId(PersistentObjectBuilder.DEFAULT_TENANT_ID);
        return sProcessInstance;
    }

    @Test
    public void getArchivedProcessInstancesInAllStates_should_return_archived_process_instances_when_exist() {
        // Given
        final SAProcessInstance saProcessInstance1 = repository.add(buildSAProcessInstance(1L));
        final SAProcessInstance saProcessInstance2 = repository.add(buildSAProcessInstance(2L));

        // When
        final List<SAProcessInstance> archivedProcessInstances = repository.getArchivedProcessInstancesInAllStates(Arrays.asList(1L, 2L));

        // Then
        assertFalse("The list of archived process instance must not be empty !!", archivedProcessInstances.isEmpty());
        assertEquals("The first element of the list must to have as id 1", saProcessInstance1, archivedProcessInstances.get(0));
        assertEquals("The second element of the list must to have as id 2", saProcessInstance2, archivedProcessInstances.get(1));
    }

    @Test
    public void getArchivedProcessInstancesInAllStates_should_return_empty_list_when_no_archived_process_instances_with_ids() {
        // Given

        // When
        final List<SAProcessInstance> archivedProcessInstances = repository.getArchivedProcessInstancesInAllStates(Arrays.asList(1L, 2L));

        // Then
        assertTrue("The list of archived process instance must be empty !!", archivedProcessInstances.isEmpty());
    }

    private SAProcessInstanceImpl buildSAProcessInstance(final long id) {
        final SAProcessInstanceImpl saProcessInstanceImpl = new SAProcessInstanceImpl();
        saProcessInstanceImpl.setId(id);
        saProcessInstanceImpl.setSourceObjectId(id);
        saProcessInstanceImpl.setTenantId(PersistentObjectBuilder.DEFAULT_TENANT_ID);
        saProcessInstanceImpl.setName("process" + id);
        return saProcessInstanceImpl;
    }

}
