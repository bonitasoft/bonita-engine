/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.bonitasoft.engine.bpm.process.ProcessInstanceState.*;
import static org.bonitasoft.engine.commons.Pair.mapOf;
import static org.bonitasoft.engine.commons.Pair.pair;
import static org.bonitasoft.engine.core.process.definition.model.SFlowNodeType.RECEIVE_TASK;
import static org.bonitasoft.engine.test.persistence.builder.ActorBuilder.anActor;
import static org.bonitasoft.engine.test.persistence.builder.ActorMemberBuilder.anActorMember;
import static org.bonitasoft.engine.test.persistence.builder.CallActivityInstanceBuilder.aCallActivityInstanceBuilder;
import static org.bonitasoft.engine.test.persistence.builder.GatewayInstanceBuilder.aGatewayInstanceBuilder;
import static org.bonitasoft.engine.test.persistence.builder.PendingActivityMappingBuilder.aPendingActivityMapping;
import static org.bonitasoft.engine.test.persistence.builder.PersistentObjectBuilder.DEFAULT_TENANT_ID;
import static org.bonitasoft.engine.test.persistence.builder.ProcessInstanceBuilder.aProcessInstance;
import static org.bonitasoft.engine.test.persistence.builder.SupervisorBuilder.aSupervisor;
import static org.bonitasoft.engine.test.persistence.builder.UserBuilder.aUser;
import static org.bonitasoft.engine.test.persistence.builder.UserMembershipBuilder.aUserMembership;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.actor.mapping.model.SActorMember;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.model.business.data.SFlowNodeSimpleRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SProcessMultiRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SProcessSimpleRefBusinessDataInstance;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.test.persistence.repository.ProcessInstanceRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class ProcessInstanceQueriesTest {

    private static final long aGroupId = 654L;
    private static final long anotherGroupId = 9875L;
    private static final long aRoleId = 1235L;
    private static final long anotherRoleId = 956L;
    private static final long PROCESS_INSTANCE_ID = 43578923425L;
    private static final long FLOW_NODE_INSTANCE_ID = 342678L;

    @Inject
    private ProcessInstanceRepository repository;

    @Inject
    private JdbcTemplate jdbcTemplate;

    @Test
    public void getPossibleUserIdsOfPendingTasks_should_return_users_mapped_through_user_filters() {
        final SUser expectedUser = repository.add(aUser().withId(1L).build());
        repository.add(aUser().withId(2L).build()); // not expected user
        final SPendingActivityMapping pendingActivity = repository
                .add(aPendingActivityMapping().withUserId(expectedUser.getId()).build());

        final List<Long> userIds = repository.getPossibleUserIdsOfPendingTasks(pendingActivity.getActivityId());

        assertThat(userIds).containsOnly(expectedUser.getId());
    }

    @Test
    public void isTaskPendingForUser_should_be_true_when_mapped_using_pending_mapping() {
        final SUser expectedUser = repository.add(aUser().withId(1L).build());
        repository.add(aUser().withId(2L).build()); // not expected user
        final SPendingActivityMapping pendingActivity = repository
                .add(aPendingActivityMapping().withUserId(expectedUser.getId()).build());

        boolean taskPendingForUser = repository.isTaskPendingForUser(pendingActivity.getActivityId(),
                expectedUser.getId());

        assertThat(taskPendingForUser).isTrue();
    }

    @Test
    public void isTaskPendingForUser_should_be_true_when_mapped_using_actor() {
        final SUser expectedUser = repository.add(aUser().build());
        SActor actor = repository.add(anActor().build());
        SActorMember actorMember = repository
                .add(anActorMember().withUserId(expectedUser.getId()).forActor(actor).build());
        final SPendingActivityMapping pendingActivity = repository
                .add(aPendingActivityMapping().withActorId(actor.getId()).build());

        boolean taskPendingForUser = repository.isTaskPendingForUser(pendingActivity.getActivityId(),
                expectedUser.getId());

        assertThat(taskPendingForUser).isTrue();
    }

    @Test
    public void isTaskPendingForUser_should_be_true_when_mapped_using_actor_having_role() {
        final SUser expectedUser = repository.add(aUser().build());
        repository.add(aUserMembership().forUser(expectedUser).memberOf(aGroupId, aRoleId).build());
        SActor actor = repository.add(anActor().build());
        repository.add(anActorMember().withRoleId(aRoleId).forActor(actor).build());
        final SPendingActivityMapping pendingActivity = repository
                .add(aPendingActivityMapping().withActorId(actor.getId()).build());

        boolean taskPendingForUser = repository.isTaskPendingForUser(pendingActivity.getActivityId(),
                expectedUser.getId());

        assertThat(taskPendingForUser).isTrue();
    }

    @Test
    public void isTaskPendingForUser_should_be_true_when_mapped_using_actor_having_group() {
        final SUser expectedUser = repository.add(aUser().build());
        repository.add(aUserMembership().forUser(expectedUser).memberOf(aGroupId, aRoleId).build());
        SActor actor = repository.add(anActor().build());
        repository.add(anActorMember().withGroupId(aGroupId).forActor(actor).build());
        final SPendingActivityMapping pendingActivity = repository
                .add(aPendingActivityMapping().withActorId(actor.getId()).build());

        boolean taskPendingForUser = repository.isTaskPendingForUser(pendingActivity.getActivityId(),
                expectedUser.getId());

        assertThat(taskPendingForUser).isTrue();
    }

    @Test
    public void isTaskPendingForUser_should_be_true_when_mapped_using_actor_having_membership() {
        final SUser expectedUser = repository.add(aUser().build());
        repository.add(aUserMembership().forUser(expectedUser).memberOf(anotherGroupId, anotherRoleId).build());
        SActor actor = repository.add(anActor().build());
        repository.add(anActorMember().withGroupId(anotherGroupId).withRoleId(anotherRoleId).forActor(actor).build());
        final SPendingActivityMapping pendingActivity = repository
                .add(aPendingActivityMapping().withActorId(actor.getId()).build());

        boolean taskPendingForUser = repository.isTaskPendingForUser(pendingActivity.getActivityId(),
                expectedUser.getId());

        assertThat(taskPendingForUser).isTrue();
    }

    @Test
    public void isTaskPendingForUser_should_be_false_when_not_pending() {
        final SUser expectedUser = repository.add(aUser().withId(1L).build());
        SUser notPendingUser = repository.add(aUser().withId(2L).build());// not expected user
        final SPendingActivityMapping pendingActivity = repository
                .add(aPendingActivityMapping().withUserId(expectedUser.getId()).build());

        boolean taskPendingForUser = repository.isTaskPendingForUser(pendingActivity.getActivityId(),
                notPendingUser.getId());

        assertThat(taskPendingForUser).isFalse();
    }

    @Test
    public void isTaskPendingForUser() {
        final SUser expectedUser = repository.add(aUser().withId(1L).build());
        repository.add(aUser().withId(2L).build()); // not expected user
        final SPendingActivityMapping pendingActivity = repository
                .add(aPendingActivityMapping().withUserId(expectedUser.getId()).build());

        boolean taskPendingForUser = repository.isTaskPendingForUser(pendingActivity.getActivityId(),
                expectedUser.getId());

        assertThat(taskPendingForUser).isTrue();
    }

    @Test
    public void getPossibleUserIdsOfPendingTasks_should_return_users_mapped_through_his_userid_in_actormember() {
        final SActor actor = repository.add(anActor().build());
        final SPendingActivityMapping addedPendingMapping = repository
                .add(aPendingActivityMapping().withActorId(actor.getId()).build());
        final SUser expectedUser = repository.add(aUser().withId(1L).build());
        repository.add(anActorMember().forActor(actor).withUserId(expectedUser.getId()).build());
        repository.add(aUser().withId(2L).build()); // not expected user

        final List<Long> userIds = repository.getPossibleUserIdsOfPendingTasks(addedPendingMapping.getActivityId());

        assertThat(userIds).containsOnly(expectedUser.getId());
    }

    @Test
    public void getPossibleUserIdsOfPendingTasks_should_return_users_mapped_through_his_groupid_in_actormember() {
        final SActor actor = repository.add(anActor().build());
        final SPendingActivityMapping addedPendingMapping = repository
                .add(aPendingActivityMapping().withActorId(actor.getId()).build());
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
        final SPendingActivityMapping addedPendingMapping = repository
                .add(aPendingActivityMapping().withActorId(actor.getId()).build());
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
        final SPendingActivityMapping addedPendingMapping = repository
                .add(aPendingActivityMapping().withActorId(actor.getId()).build());
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
        final SPendingActivityMapping addedPendingMapping = repository
                .add(aPendingActivityMapping().withActorId(actor.getId()).build());
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
        final SPendingActivityMapping pendingActivity = repository
                .add(aPendingActivityMapping().withUserId(expectedUser.getId()).build());

        final long numberOfUsers = repository.getNumberOfSUserWhoCanStartPendingTask(pendingActivity.getActivityId());

        assertThat(numberOfUsers).isEqualTo(1);
    }

    @Test
    public void getNumberOfSUserWhoCanStartPendingTask_should_return_users_mapped_through_his_userid_in_actormember() {
        final SActor actor = repository.add(anActor().build());
        final SPendingActivityMapping addedPendingMapping = repository
                .add(aPendingActivityMapping().withActorId(actor.getId()).build());
        final SUser expectedUser = repository.add(aUser().withId(1L).build());
        repository.add(anActorMember().forActor(actor).withUserId(expectedUser.getId()).build());
        repository.add(aUser().withId(2L).build()); // not expected user

        final long numberOfUsers = repository
                .getNumberOfSUserWhoCanStartPendingTask(addedPendingMapping.getActivityId());

        assertThat(numberOfUsers).isEqualTo(1);
    }

    @Test
    public void getNumberOfSUserWhoCanStartPendingTask_should_return_users_mapped_through_his_groupid_in_actormember() {
        final SActor actor = repository.add(anActor().build());
        final SPendingActivityMapping addedPendingMapping = repository
                .add(aPendingActivityMapping().withActorId(actor.getId()).build());
        repository.add(anActorMember().forActor(actor).withGroupId(aGroupId).build());
        final SUser expectedUser = repository.add(aUser().withId(1L).build());
        repository.add(aUserMembership().forUser(expectedUser).memberOf(aGroupId, aRoleId).build());
        final SUser notExpectedUser = repository.add(aUser().withId(2L).build());
        repository.add(aUserMembership().forUser(notExpectedUser).memberOf(anotherGroupId, aRoleId).build());

        final long numberOfUsers = repository
                .getNumberOfSUserWhoCanStartPendingTask(addedPendingMapping.getActivityId());

        assertThat(numberOfUsers).isEqualTo(1);
    }

    @Test
    public void getNumberOfSUserWhoCanStartPendingTask_should_return_users_mapped_through_his_roleid_in_actormember() {
        final SActor actor = repository.add(anActor().build());
        final SPendingActivityMapping addedPendingMapping = repository
                .add(aPendingActivityMapping().withActorId(actor.getId()).build());
        repository.add(anActorMember().forActor(actor).withRoleId(aRoleId).build());
        final SUser expectedUser = repository.add(aUser().withId(1L).build());
        repository.add(aUserMembership().forUser(expectedUser).memberOf(aGroupId, aRoleId).build());
        final SUser notexpectedUser = repository.add(aUser().withId(2L).build());
        repository.add(aUserMembership().forUser(notexpectedUser).memberOf(aGroupId, anotherRoleId).build());

        final long numberOfUsers = repository
                .getNumberOfSUserWhoCanStartPendingTask(addedPendingMapping.getActivityId());

        assertThat(numberOfUsers).isEqualTo(1);
    }

    @Test
    public void getNumberOfSUserWhoCanStartPendingTask_should_return_users_mapped_through_his_membership_in_actormember() {
        final SUser expectedUser = repository.add(aUser().withId(1L).build());
        final SUser expectedUser2 = repository.add(aUser().withId(4L).build());
        final SUser notExpectedUser = repository.add(aUser().withId(2L).build());
        final SUser notExpectedUser2 = repository.add(aUser().withId(3L).build());
        final SActor actor = repository.add(anActor().build());
        final SPendingActivityMapping addedPendingMapping = repository
                .add(aPendingActivityMapping().withActorId(actor.getId()).build());
        repository.add(anActorMember().forActor(actor).withGroupId(aGroupId).withRoleId(aRoleId).build());
        repository.add(aUserMembership().forUser(expectedUser).memberOf(aGroupId, aRoleId).build());
        repository.add(aUserMembership().forUser(expectedUser2).memberOf(aGroupId, aRoleId).build());
        repository.add(aUserMembership().forUser(notExpectedUser).memberOf(anotherGroupId, aRoleId).build());
        repository.add(aUserMembership().forUser(notExpectedUser2).memberOf(aGroupId, anotherRoleId).build());

        final long numberOfUsers = repository
                .getNumberOfSUserWhoCanStartPendingTask(addedPendingMapping.getActivityId());

        assertThat(numberOfUsers).isEqualTo(2);
    }

    @Test
    public void getNumberOfSUserWhoCanStartPendingTask_return_userIds_ordered_by_userName() {
        final SUser john = repository.add(aUser().withUserName("john").withId(1L).build());
        final SUser paul = repository.add(aUser().withUserName("paul").withId(2L).build());
        final SUser walter = repository.add(aUser().withUserName("walter").withId(3L).build());
        final SUser marie = repository.add(aUser().withUserName("marie").withId(4L).build());
        final SActor actor = repository.add(anActor().build());
        final SPendingActivityMapping addedPendingMapping = repository
                .add(aPendingActivityMapping().withActorId(actor.getId()).build());
        repository.add(anActorMember().forActor(actor).withGroupId(aGroupId).withRoleId(aRoleId).build());
        repository.add(aUserMembership().forUser(john).memberOf(aGroupId, aRoleId).build());
        repository.add(aUserMembership().forUser(paul).memberOf(aGroupId, aRoleId).build());
        repository.add(aUserMembership().forUser(walter).memberOf(aGroupId, aRoleId).build());
        repository.add(aUserMembership().forUser(marie).memberOf(aGroupId, aRoleId).build());

        final long numberOfUsers = repository
                .getNumberOfSUserWhoCanStartPendingTask(addedPendingMapping.getActivityId());

        assertThat(numberOfUsers).isEqualTo(4);
    }

    @Test
    public void searchSUserWhoCanStartPendingTask_should_return_users_mapped_through_user_filters() {
        final SUser expectedUser = repository.add(aUser().withId(1L).build());
        repository.add(aUser().withId(2L).build()); // not expected user
        final SPendingActivityMapping pendingActivity = repository
                .add(aPendingActivityMapping().withUserId(expectedUser.getId()).build());

        final List<SUser> userIds = repository.searchPossibleUserIdsOfPendingTasks(pendingActivity.getActivityId());

        assertThat(userIds).containsOnly(expectedUser);
    }

    @Test
    public void searchSUserWhoCanStartPendingTask_should_return_users_mapped_through_his_userid_in_actormember() {
        final SActor actor = repository.add(anActor().build());
        final SPendingActivityMapping addedPendingMapping = repository
                .add(aPendingActivityMapping().withActorId(actor.getId()).build());
        final SUser expectedUser = repository.add(aUser().withId(1L).build());
        repository.add(anActorMember().forActor(actor).withUserId(expectedUser.getId()).build());
        repository.add(aUser().withId(2L).build()); // not expected user

        final List<SUser> userIds = repository.searchPossibleUserIdsOfPendingTasks(addedPendingMapping.getActivityId());

        assertThat(userIds).containsOnly(expectedUser);
    }

    @Test
    public void searchSUserWhoCanStartPendingTask_should_return_users_mapped_through_his_groupid_in_actormember() {
        final SActor actor = repository.add(anActor().build());
        final SPendingActivityMapping addedPendingMapping = repository
                .add(aPendingActivityMapping().withActorId(actor.getId()).build());
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
        final SPendingActivityMapping addedPendingMapping = repository
                .add(aPendingActivityMapping().withActorId(actor.getId()).build());
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
        final SPendingActivityMapping addedPendingMapping = repository
                .add(aPendingActivityMapping().withActorId(actor.getId()).build());
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
        final SPendingActivityMapping addedPendingMapping = repository
                .add(aPendingActivityMapping().withActorId(actor.getId()).build());
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
        final SProcessInstance parentPI = repository
                .add(aProcessInstance().withContainerId(1).withName("test Parent Process Instance")
                        .withStateId(STARTED.getId()).build());
        final SCallActivityInstance callActivity = (SCallActivityInstance) repository.add(aCallActivityInstanceBuilder()
                .withLogicalGroup4(parentPI.getId())
                .withName("call Activity")
                .build());
        final SProcessInstance childPI = repository
                .add(aProcessInstance().withContainerId(1).withName("test Child Process Instance")
                        .withCallerId(callActivity.getId()).withCallerType(SFlowNodeType.CALL_ACTIVITY)
                        .withStateId(STARTED.getId()).build());

        assertThat(repository.countChildrenInstanceIdsOfProcessInstance(parentPI.getId())).isEqualTo(1);
        final List<Long> piIds = repository.getChildrenInstanceIdsOfProcessInstance(parentPI.getId());
        assertThat(piIds).isNotEmpty().containsExactly(childPI.getId());
    }

    @Test
    public void searchOnlyChildrenSProcessInstanceOfProcessInstance_return_processInstancesIdsFromCallActivity() {
        final SProcessInstance parentPI = repository
                .add(aProcessInstance().withContainerId(1).withName("test Parent Process Instance")
                        .withStateId(STARTED.getId()).build());
        final SCallActivityInstance callActivity = (SCallActivityInstance) repository.add(aCallActivityInstanceBuilder()
                .withLogicalGroup4(parentPI.getId())
                .withName("call Activity")
                .build());
        final SGatewayInstance gatewayActivity = (SGatewayInstance) repository
                .add(aGatewayInstanceBuilder().withLogicalGroup4(parentPI.getId())
                        .withName("call Activity")
                        .build());
        final SProcessInstance childPI = repository
                .add(aProcessInstance().withContainerId(1).withName("test Child Process Instance")
                        .withCallerId(callActivity.getId()).withCallerType(SFlowNodeType.CALL_ACTIVITY)
                        .withStateId(STARTED.getId()).build());
        repository.add(aProcessInstance().withContainerId(1).withName("test Parent Process Instance")
                .withCallerId(gatewayActivity.getId()).withCallerType(SFlowNodeType.GATEWAY)
                .withStateId(STARTED.getId()).build());

        assertThat(repository.countChildrenInstanceIdsOfProcessInstance(parentPI.getId())).isEqualTo(1);
        final List<Long> piIds = repository.getChildrenInstanceIdsOfProcessInstance(parentPI.getId());
        assertThat(piIds).isNotEmpty().containsExactly(childPI.getId());
    }

    @Test
    public void searchChildProcessInstanceOfProcessInstance_return_processInstancesIdsNotGrandChildren() {
        final SProcessInstance parentPI = repository
                .add(aProcessInstance().withContainerId(1).withName("test Parent Process Instance")
                        .withStateId(STARTED.getId()).build());
        final SCallActivityInstance callActivity = (SCallActivityInstance) repository.add(aCallActivityInstanceBuilder()
                .withLogicalGroup4(parentPI.getId())
                .withName("call Activity")
                .build());
        final SProcessInstance childPI = repository
                .add(aProcessInstance().withContainerId(1).withName("test Child Process Instance Started")
                        .withCallerId(callActivity.getId()).withCallerType(SFlowNodeType.CALL_ACTIVITY)
                        .withStateId(STARTED.getId()).build());
        final SCallActivityInstance callSubActivity = (SCallActivityInstance) repository
                .add(aCallActivityInstanceBuilder()
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
        final SProcessInstance parentPI = repository
                .add(aProcessInstance().withContainerId(1).withName("test Parent Process Instance")
                        .withStateId(STARTED.getId()).build());
        final SCallActivityInstance callActivity = (SCallActivityInstance) repository.add(aCallActivityInstanceBuilder()
                .withLogicalGroup4(parentPI.getId())
                .withName("call Activity")
                .build());
        final SProcessInstance childPI = repository
                .add(aProcessInstance().withContainerId(1).withName("test Child Process Instance Started")
                        .withCallerId(callActivity.getId()).withCallerType(SFlowNodeType.CALL_ACTIVITY)
                        .withStateId(STARTED.getId()).build());
        final SCallActivityInstance callSubActivity = (SCallActivityInstance) repository
                .add(aCallActivityInstanceBuilder()
                        .withLogicalGroup4(childPI.getId())
                        .withName("call Activity")
                        .build());
        final SProcessInstance grandChildPI = repository
                .add(aProcessInstance().withContainerId(1).withName("test Grand Child Process Instance Started")
                        .withCallerId(callSubActivity.getId()).withCallerType(SFlowNodeType.CALL_ACTIVITY)
                        .withStateId(ProcessInstanceState.CANCELLED.getId()).build());

        assertThat(repository.countChildrenInstanceIdsOfProcessInstance(childPI.getId())).isEqualTo(1);
        final List<Long> piIds = repository.getChildrenInstanceIdsOfProcessInstance(childPI.getId());
        assertThat(piIds).isNotEmpty().containsOnly(grandChildPI.getId());
    }

    @Test
    public void searchChildrenSProcessInstanceOfProcessInstance_return_processInstancesIds() {
        final SProcessInstance parentPI = repository
                .add(aProcessInstance().withContainerId(1).withName("test Parent Process Instance")
                        .withStateId(STARTED.getId()).build());
        final SCallActivityInstance callActivity = (SCallActivityInstance) repository.add(aCallActivityInstanceBuilder()
                .withLogicalGroup4(parentPI.getId())
                .withName("call Activity")
                .build());
        final SProcessInstance childPI1 = repository
                .add(aProcessInstance().withContainerId(1).withName("test Child Process Instance Started")
                        .withCallerId(callActivity.getId()).withCallerType(SFlowNodeType.CALL_ACTIVITY)
                        .withStateId(STARTED.getId()).build());
        final SProcessInstance childPI2 = repository
                .add(aProcessInstance().withContainerId(1).withName("test Child Process Instance Started")
                        .withCallerId(callActivity.getId()).withCallerType(SFlowNodeType.CALL_ACTIVITY)
                        .withStateId(ProcessInstanceState.COMPLETED.getId()).build());
        final SProcessInstance childPI3 = repository
                .add(aProcessInstance().withContainerId(1).withName("test Child Process Instance Started")
                        .withCallerId(callActivity.getId()).withCallerType(SFlowNodeType.CALL_ACTIVITY)
                        .withStateId(ProcessInstanceState.CANCELLED.getId()).build());

        assertThat(repository.countChildrenInstanceIdsOfProcessInstance(parentPI.getId())).isEqualTo(3);
        final List<Long> piIds = repository.getChildrenInstanceIdsOfProcessInstance(parentPI.getId());
        assertThat(piIds).isNotEmpty().containsOnly(childPI1.getId(), childPI2.getId(), childPI3.getId());
    }

    @Test
    public void searchChildSProcessInstanceOfProcessInstance_return_processInstancesIdsFromParentPIOnly() {
        final SProcessInstance parentPI = repository
                .add(aProcessInstance().withContainerId(1).withName("test Parent Process Instance")
                        .withStateId(STARTED.getId()).build());
        repository.add(aProcessInstance().withContainerId(1).withName("test Process Instance Independent Started")
                .withStateId(STARTED.getId()).build());
        repository.add(aProcessInstance().withContainerId(1).withName("test Process Instance Independent Completed")
                .withStateId(ProcessInstanceState.COMPLETED.getId()).build());
        repository.add(aProcessInstance().withContainerId(1).withName("test Process Instance Independent Cancelled")
                .withStateId(ProcessInstanceState.CANCELLED.getId()).build());
        final SCallActivityInstance callActivity = (SCallActivityInstance) repository.add(aCallActivityInstanceBuilder()
                .withLogicalGroup4(parentPI.getId())
                .withName("call Activity")
                .build());
        final SProcessInstance childPI = repository
                .add(aProcessInstance().withContainerId(1).withName("test Child Process Instance")
                        .withCallerId(callActivity.getId()).withCallerType(SFlowNodeType.CALL_ACTIVITY)
                        .withStateId(STARTED.getId()).build());

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
        final long numberOfSProcessInstanceFailed = repository
                .getNumberOfSProcessInstanceFailedForProcessDefinition(777777L);

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
        final List<SProcessInstance> sProcessInstanceFailed = repository
                .searchSProcessInstanceFailedForProcessDefinition(777777L);

        // Then
        assertEquals(2, sProcessInstanceFailed.size());
    }

    @Test
    public void getNumberOfSProcessInstanceFailed_should_return_number_of_distinct_process_instances() {
        // Given
        repository.add(buildFailedProcessInstance(1));

        final SProcessInstance processInstanceWithFailedFlowNode = new SProcessInstance("process2", 10L);
        processInstanceWithFailedFlowNode.setId(2);
        processInstanceWithFailedFlowNode.setTenantId(DEFAULT_TENANT_ID);
        repository.add(processInstanceWithFailedFlowNode);
        repository.add(buildFailedGateway(852, processInstanceWithFailedFlowNode.getId()));

        final SProcessInstance failedProcessInstanceWithFailedFlowNode = repository.add(buildFailedProcessInstance(3));
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
        final SProcessInstance processInstanceWithFailedFlowNode = new SProcessInstance("process2", 10L);
        processInstanceWithFailedFlowNode.setId(2);
        processInstanceWithFailedFlowNode.setTenantId(DEFAULT_TENANT_ID);
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
        final SProcessInstance failedProcessInstance = repository.add(buildFailedProcessInstance(1));

        final SProcessInstance processInstanceWithFailedFlowNode = SProcessInstance.builder().name("process2")
                .processDefinitionId(10L)
                .id(2)
                .tenantId(DEFAULT_TENANT_ID)
                .build();
        repository.add(processInstanceWithFailedFlowNode);
        repository.add(buildFailedGateway(1, processInstanceWithFailedFlowNode.getId()));

        final SProcessInstance failedProcessInstanceWithFailedFlowNode = repository.add(buildFailedProcessInstance(3));
        repository.add(buildFailedGateway(2, failedProcessInstanceWithFailedFlowNode.getId()));

        // When
        final List<SProcessInstance> failedSProcessInstance = repository.searchSProcessInstanceFailed();

        // Then
        assertThat(failedSProcessInstance).containsOnly(failedProcessInstance, failedProcessInstanceWithFailedFlowNode,
                processInstanceWithFailedFlowNode);
    }

    @Test
    public void searchSProcessInstanceFailed_return_failed_process_instances() {
        // Given
        final SProcessInstance failedProcessInstance = repository.add(buildFailedProcessInstance(1));

        // When
        final List<SProcessInstance> failedSProcessInstance = repository.searchSProcessInstanceFailed();

        // Then
        assertEquals(1, failedSProcessInstance.size());
        assertEquals(failedProcessInstance, failedSProcessInstance.get(0));
    }

    @Test
    public void searchSProcessInstanceFailed_return_process_instances_with_failed_flow_nodes() {
        // Given
        final SProcessInstance processInstanceWithFailedFlowNode = new SProcessInstance("process2", 10L);
        processInstanceWithFailedFlowNode.setId(2);
        processInstanceWithFailedFlowNode.setTenantId(DEFAULT_TENANT_ID);
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

        repository.add(aProcessInstance().withProcessDefinitionId(8L).withContainerId(1)
                .withName("test Parent Process Instance")
                .withStateId(STARTED.getId()).build());
        repository.add(aSupervisor().withProcessDefinitionId(8L).withUserId(userId).build());

        SProcessInstance processInstanceWithFailedFlowNode = new SProcessInstance("process2", 10L);
        processInstanceWithFailedFlowNode.setId(2);
        processInstanceWithFailedFlowNode.setTenantId(DEFAULT_TENANT_ID);
        repository.add(processInstanceWithFailedFlowNode);
        repository.add(buildFailedGateway(852, processInstanceWithFailedFlowNode.getId()));
        repository.add(buildFailedProcessInstance(3, 11L));

        processInstanceWithFailedFlowNode = new SProcessInstance("process2", 15L);
        processInstanceWithFailedFlowNode.setId(4);
        processInstanceWithFailedFlowNode.setTenantId(DEFAULT_TENANT_ID);
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

        repository.add(aProcessInstance().withProcessDefinitionId(8L).withContainerId(1)
                .withName("test Parent Process Instance")
                .withStateId(STARTED.getId()).build());
        repository.add(aSupervisor().withProcessDefinitionId(8L).withUserId(userId).build());

        SProcessInstance processInstanceWithFailedFlowNode = new SProcessInstance("process2", 10L);
        processInstanceWithFailedFlowNode.setId(2);
        processInstanceWithFailedFlowNode.setTenantId(DEFAULT_TENANT_ID);
        repository.add(processInstanceWithFailedFlowNode);
        repository.add(buildFailedGateway(852, processInstanceWithFailedFlowNode.getId()));
        repository.add(buildFailedProcessInstance(3, 11L));

        processInstanceWithFailedFlowNode = new SProcessInstance("process2", 15L);
        processInstanceWithFailedFlowNode.setId(4);
        processInstanceWithFailedFlowNode.setTenantId(DEFAULT_TENANT_ID);
        repository.add(processInstanceWithFailedFlowNode);
        repository.add(buildFailedGateway(853, processInstanceWithFailedFlowNode.getId()));
        repository.add(buildStartedProcessInstance(5, 15L));
        repository.add(aSupervisor().withProcessDefinitionId(15L).withUserId(userId).build());

        // When
        final List<SProcessInstance> sProcessInstanceFailedList = repository
                .searchFailedSProcessInstanceSupervisedBy(userId);

        // Then
        assertThat(sProcessInstanceFailedList).hasSize(2).extracting("id").contains(1L, 4L);

    }

    private SGatewayInstance buildFailedGateway(final long gatewayId, final long parentProcessInstanceId) {
        final SGatewayInstance sGatewayInstance = new SGatewayInstance();
        sGatewayInstance.setId(gatewayId);
        sGatewayInstance.setStateId(3);
        sGatewayInstance.setLogicalGroup(3, parentProcessInstanceId);
        sGatewayInstance.setTenantId(DEFAULT_TENANT_ID);
        return sGatewayInstance;
    }

    private SProcessInstance buildFailedProcessInstance(final long processInstanceId) {
        return buildFailedProcessInstance(processInstanceId, 9L);
    }

    private SProcessInstance buildStartedProcessInstance(final long processInstanceId, final long processDefinitionId) {
        return SProcessInstance.builder().name("process" + processInstanceId).processDefinitionId(processDefinitionId)
                .id(processInstanceId)
                .stateId(STARTED.getId())
                .tenantId(DEFAULT_TENANT_ID).build();
    }

    private SProcessInstance buildFailedProcessInstance(final long processInstanceId, final long processDefinitionId) {
        return SProcessInstance.builder().name("process" + processInstanceId).processDefinitionId(processDefinitionId)
                .id(processInstanceId)
                .stateId(7)
                .tenantId(DEFAULT_TENANT_ID).build();
    }

    @Test
    public void should_get_processInstances_by_callertype_and_stateCategory() {
        SProcessInstance sProcessInstance = buildStartedProcessInstance(12L, 102L);
        sProcessInstance.setCallerType(RECEIVE_TASK);
        repository.add(sProcessInstance);
        repository.flush();
        final String stateCategory = jdbcTemplate.queryForObject("select stateCategory from process_instance",
                String.class);
        final String callerType = jdbcTemplate.queryForObject("select callerType from process_instance", String.class);
        assertThat(stateCategory).isEqualTo("NORMAL");
        assertThat(callerType).isEqualTo("RECEIVE_TASK");
    }

    @Test
    public void should_save_and_get_multi_business_data_reference_for_process() {
        SProcessMultiRefBusinessDataInstance multiRefBusinessDataInstance = new SProcessMultiRefBusinessDataInstance();
        multiRefBusinessDataInstance.setDataIds(Arrays.asList(23L, 25L, 27L));
        multiRefBusinessDataInstance.setProcessInstanceId(PROCESS_INSTANCE_ID);
        multiRefBusinessDataInstance.setName("myMultiProcData");
        multiRefBusinessDataInstance.setDataClassName("someDataClassName");
        multiRefBusinessDataInstance = repository.add(multiRefBusinessDataInstance);
        repository.flush();

        PersistentObject multiRefBusinessData = repository.selectOne("getSRefBusinessDataInstance",
                pair("processInstanceId", PROCESS_INSTANCE_ID), pair("name", "myMultiProcData"));
        Map<String, Object> multiRefBusinessDataAsMap = jdbcTemplate
                .queryForMap("SELECT * FROM ref_biz_data_inst WHERE proc_inst_id=" + PROCESS_INSTANCE_ID
                        + " AND name='myMultiProcData'");
        List<Map<String, Object>> dataIds = jdbcTemplate.queryForList(
                "SELECT ID, IDX, DATA_ID FROM multi_biz_data WHERE id=" + multiRefBusinessDataInstance.getId());

        assertThat(((SProcessMultiRefBusinessDataInstance) multiRefBusinessData).getDataIds())
                .isEqualTo(Arrays.asList(23L, 25L, 27L));
        assertThat(multiRefBusinessData).isEqualTo(multiRefBusinessDataInstance);
        assertThat(multiRefBusinessDataAsMap).containsOnly(
                entry("TENANTID", 0L), // remove when tenant notion disappears completely
                entry("ID", multiRefBusinessDataInstance.getId()),
                entry("KIND", "proc_multi_ref"),
                entry("NAME", "myMultiProcData"),
                entry("DATA_CLASSNAME", "someDataClassName"),
                entry("DATA_ID", null),
                entry("PROC_INST_ID", PROCESS_INSTANCE_ID),
                entry("FN_INST_ID", null));
        assertThat(dataIds).containsExactly(
                mapOf(pair("ID", multiRefBusinessDataInstance.getId()), pair("IDX", 0), pair("DATA_ID", 23L)),
                mapOf(pair("ID", multiRefBusinessDataInstance.getId()), pair("IDX", 1), pair("DATA_ID", 25L)),
                mapOf(pair("ID", multiRefBusinessDataInstance.getId()), pair("IDX", 2), pair("DATA_ID", 27L)));
    }

    @Test
    public void should_save_and_get_single_business_data_reference_for_process() {
        SProcessSimpleRefBusinessDataInstance singleRef = new SProcessSimpleRefBusinessDataInstance();
        singleRef.setDataId(43L);
        singleRef.setProcessInstanceId(PROCESS_INSTANCE_ID);
        singleRef.setName("mySingleData");
        singleRef.setDataClassName("someDataClassName");
        singleRef = repository.add(singleRef);
        repository.flush();

        PersistentObject singleRefFromQuery = repository.selectOne("getSRefBusinessDataInstance",
                pair("processInstanceId", PROCESS_INSTANCE_ID), pair("name", "mySingleData"));
        Map<String, Object> multiRefBusinessDataAsMap = jdbcTemplate
                .queryForMap(
                        "SELECT ID, KIND, NAME, DATA_CLASSNAME, DATA_ID, PROC_INST_ID, FN_INST_ID FROM ref_biz_data_inst WHERE proc_inst_id="
                                + PROCESS_INSTANCE_ID + " AND name='mySingleData'");
        assertThat(singleRefFromQuery).isEqualTo(singleRef);
        assertThat(multiRefBusinessDataAsMap).containsOnly(
                entry("ID", singleRef.getId()),
                entry("KIND", "proc_simple_ref"),
                entry("NAME", "mySingleData"),
                entry("DATA_CLASSNAME", "someDataClassName"),
                entry("DATA_ID", 43L),
                entry("PROC_INST_ID", PROCESS_INSTANCE_ID),
                entry("FN_INST_ID", null));
    }

    @Test
    public void should_save_and_get_single_business_data_reference_for_flow_node() {
        SFlowNodeSimpleRefBusinessDataInstance singleRef = new SFlowNodeSimpleRefBusinessDataInstance();
        singleRef.setDataId(43L);
        singleRef.setFlowNodeInstanceId(FLOW_NODE_INSTANCE_ID);
        singleRef.setName("mySingleData");
        singleRef.setDataClassName("someDataClassName");
        singleRef = repository.add(singleRef);
        repository
                .flush();

        PersistentObject singleRefFromQuery = repository.selectOne("getSFlowNodeRefBusinessDataInstance",
                pair("flowNodeInstanceId", FLOW_NODE_INSTANCE_ID), pair("name", "mySingleData"));
        Map<String, Object> multiRefBusinessDataAsMap = jdbcTemplate
                .queryForMap(
                        "SELECT ID, KIND, NAME, DATA_CLASSNAME, DATA_ID, PROC_INST_ID, FN_INST_ID FROM ref_biz_data_inst WHERE fn_inst_id="
                                + FLOW_NODE_INSTANCE_ID
                                + " AND name='mySingleData'");
        assertThat(singleRefFromQuery).isEqualTo(singleRef);
        assertThat(multiRefBusinessDataAsMap).containsOnly(
                entry("ID", singleRef.getId()),
                entry("KIND", "fn_simple_ref"),
                entry("NAME", "mySingleData"),
                entry("DATA_CLASSNAME", "someDataClassName"),
                entry("DATA_ID", 43L),
                entry("PROC_INST_ID", null),
                entry("FN_INST_ID", FLOW_NODE_INSTANCE_ID));
    }

    @Test
    public void should_return_process_instance_ids_to_restart_that_older_than_max_last_update_date() {
        long now = System.currentTimeMillis();

        SProcessInstance oldProcess1 = SProcessInstance.builder().name("oldProcess1").stateId(INITIALIZING.getId())
                .lastUpdate(now().minusSeconds(60).toEpochMilli()).build();
        SProcessInstance oldProcess2 = SProcessInstance.builder().name("oldProcess2").stateId(INITIALIZING.getId())
                .lastUpdate(now().minusSeconds(70).toEpochMilli()).build();
        SProcessInstance recentProcess1 = SProcessInstance.builder().name("recentProcess1")
                .stateId(INITIALIZING.getId()).lastUpdate(now().minusSeconds(10).toEpochMilli()).build();
        SProcessInstance recentProcess2 = SProcessInstance.builder().name("recentProcess2")
                .stateId(INITIALIZING.getId()).lastUpdate(now).build();

        repository.add(oldProcess1, oldProcess2, recentProcess1, recentProcess2);
        List<Long> processInstanceIdsToRestart = repository
                .getProcessInstanceIdsToRecover(now().minusSeconds(30).toEpochMilli());

        assertThat(processInstanceIdsToRestart).containsOnly(oldProcess1.getId(), oldProcess2.getId());
    }

    @Test
    public void should_return_process_instance_ids_to_restart_are_in_the_expected_states() {

        SProcessInstance process1 = SProcessInstance.builder().id(1).name("process1").stateId(INITIALIZING.getId())
                .tenantId(DEFAULT_TENANT_ID).build();
        SProcessInstance process2 = SProcessInstance.builder().id(2).name("process2").stateId(COMPLETING.getId())
                .tenantId(DEFAULT_TENANT_ID).build();
        SProcessInstance process3 = SProcessInstance.builder().id(3).name("process3").stateId(COMPLETED.getId())
                .tenantId(DEFAULT_TENANT_ID).build();
        SProcessInstance process4 = SProcessInstance.builder().id(4).name("process4").stateId(CANCELLED.getId())
                .tenantId(DEFAULT_TENANT_ID).build();
        SProcessInstance process5 = SProcessInstance.builder().id(5).name("process5").stateId(ABORTED.getId())
                .tenantId(DEFAULT_TENANT_ID).build();
        SProcessInstance process6 = SProcessInstance.builder().id(6).name("process6").stateId(STARTED.getId())
                .tenantId(DEFAULT_TENANT_ID).build();
        SProcessInstance process7 = SProcessInstance.builder().id(7).name("process7").stateId(ERROR.getId())
                .tenantId(DEFAULT_TENANT_ID).build();
        SProcessInstance process8 = SProcessInstance.builder().id(8).name("process8").stateId(ABORTING.getId())
                .tenantId(DEFAULT_TENANT_ID).build();

        repository.add(process1, process2, process3, process4, process5, process6, process7, process8);

        List<Long> processInstanceIdsToRestart = repository.getProcessInstanceIdsToRecover(System.currentTimeMillis());

        assertThat(processInstanceIdsToRestart).containsOnly(1L, 2L, 3L, 4L, 5L);
    }
}
