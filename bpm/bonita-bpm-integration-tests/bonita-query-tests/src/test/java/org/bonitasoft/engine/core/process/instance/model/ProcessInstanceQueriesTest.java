package org.bonitasoft.engine.core.process.instance.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.test.persistence.builder.ActorBuilder.anActor;
import static org.bonitasoft.engine.test.persistence.builder.ActorMemberBuilder.anActorMember;
import static org.bonitasoft.engine.test.persistence.builder.PendingActivityMappingBuilder.aPendingActivityMapping;
import static org.bonitasoft.engine.test.persistence.builder.UserBuilder.aUser;
import static org.bonitasoft.engine.test.persistence.builder.UserMembershipBuilder.aUserMembership;

import java.util.List;

import javax.inject.Inject;

import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.identity.model.SUser;
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
        SUser expectedUser = repository.add(aUser().withId(1L).build());
        repository.add(aUser().withId(2L).build()); // not expected user
        SPendingActivityMapping pendingActivity = repository.add(aPendingActivityMapping().withUserId(expectedUser.getId()).build());

        List<Long> userIds = repository.getPossibleUserIdsOfPendingTasks(pendingActivity.getActivityId());

        assertThat(userIds).containsOnly(expectedUser.getId());
    }

    @Test
    public void getPossibleUserIdsOfPendingTasks_should_return_users_mapped_through_his_userid_in_actormember() {
        SActor actor = repository.add(anActor().build());
        SPendingActivityMapping addedPendingMapping = repository.add(aPendingActivityMapping().withActorId(actor.getId()).build());
        SUser expectedUser = repository.add(aUser().withId(1L).build());
        repository.add(anActorMember().forActor(actor).withUserId(expectedUser.getId()).build());
        repository.add(aUser().withId(2L).build()); // not expected user

        List<Long> userIds = repository.getPossibleUserIdsOfPendingTasks(addedPendingMapping.getActivityId());

        assertThat(userIds).containsOnly(expectedUser.getId());
    }

    @Test
    public void getPossibleUserIdsOfPendingTasks_should_return_users_mapped_through_his_groupid_in_actormember() {
        SActor actor = repository.add(anActor().build());
        SPendingActivityMapping addedPendingMapping = repository.add(aPendingActivityMapping().withActorId(actor.getId()).build());
        repository.add(anActorMember().forActor(actor).withGroupId(aGroupId).build());
        SUser expectedUser = repository.add(aUser().withId(1L).build());
        repository.add(aUserMembership().forUser(expectedUser).memberOf(aGroupId, aRoleId).build());
        SUser notExpectedUser = repository.add(aUser().withId(2L).build());
        repository.add(aUserMembership().forUser(notExpectedUser).memberOf(anotherGroupId, aRoleId).build());

        List<Long> userIds = repository.getPossibleUserIdsOfPendingTasks(addedPendingMapping.getActivityId());

        assertThat(userIds).containsOnly(expectedUser.getId());
    }

    @Test
    public void getPossibleUserIdsOfPendingTasks_should_return_users_mapped_through_his_roleid_in_actormember() {
        SActor actor = repository.add(anActor().build());
        SPendingActivityMapping addedPendingMapping = repository.add(aPendingActivityMapping().withActorId(actor.getId()).build());
        repository.add(anActorMember().forActor(actor).withRoleId(aRoleId).build());
        SUser expectedUser = repository.add(aUser().withId(1L).build());
        repository.add(aUserMembership().forUser(expectedUser).memberOf(aGroupId, aRoleId).build());
        SUser notexpectedUser = repository.add(aUser().withId(2L).build());
        repository.add(aUserMembership().forUser(notexpectedUser).memberOf(aGroupId, anotherRoleId).build());

        List<Long> userIds = repository.getPossibleUserIdsOfPendingTasks(addedPendingMapping.getActivityId());

        assertThat(userIds).containsOnly(expectedUser.getId());
    }

    @Test
    public void getPossibleUserIdsOfPendingTasks_should_return_users_mapped_through_his_membership_in_actormember() {
        SUser expectedUser = repository.add(aUser().withId(1L).build());
        SUser expectedUser2 = repository.add(aUser().withId(4L).build());
        SUser notExpectedUser = repository.add(aUser().withId(2L).build());
        SUser notExpectedUser2 = repository.add(aUser().withId(3L).build());
        SActor actor = repository.add(anActor().build());
        SPendingActivityMapping addedPendingMapping = repository.add(aPendingActivityMapping().withActorId(actor.getId()).build());
        repository.add(anActorMember().forActor(actor).withGroupId(aGroupId).withRoleId(aRoleId).build());
        repository.add(aUserMembership().forUser(expectedUser).memberOf(aGroupId, aRoleId).build());
        repository.add(aUserMembership().forUser(expectedUser2).memberOf(aGroupId, aRoleId).build());
        repository.add(aUserMembership().forUser(notExpectedUser).memberOf(anotherGroupId, aRoleId).build());
        repository.add(aUserMembership().forUser(notExpectedUser2).memberOf(aGroupId, anotherRoleId).build());

        List<Long> userIds = repository.getPossibleUserIdsOfPendingTasks(addedPendingMapping.getActivityId());

        assertThat(userIds).containsOnly(expectedUser.getId(), expectedUser2.getId());
    }

    @Test
    public void getPossibleUserIdsOfPendingTasks_return_userIds_ordered_by_userName() {
        SUser john = repository.add(aUser().withUserName("john").withId(1L).build());
        SUser paul = repository.add(aUser().withUserName("paul").withId(2L).build());
        SUser walter = repository.add(aUser().withUserName("walter").withId(3L).build());
        SUser marie = repository.add(aUser().withUserName("marie").withId(4L).build());
        SActor actor = repository.add(anActor().build());
        SPendingActivityMapping addedPendingMapping = repository.add(aPendingActivityMapping().withActorId(actor.getId()).build());
        repository.add(anActorMember().forActor(actor).withGroupId(aGroupId).withRoleId(aRoleId).build());
        repository.add(aUserMembership().forUser(john).memberOf(aGroupId, aRoleId).build());
        repository.add(aUserMembership().forUser(paul).memberOf(aGroupId, aRoleId).build());
        repository.add(aUserMembership().forUser(walter).memberOf(aGroupId, aRoleId).build());
        repository.add(aUserMembership().forUser(marie).memberOf(aGroupId, aRoleId).build());

        List<Long> userIds = repository.getPossibleUserIdsOfPendingTasks(addedPendingMapping.getActivityId());

        assertThat(userIds).containsExactly(john.getId(), marie.getId(), paul.getId(), walter.getId());
    }

}
