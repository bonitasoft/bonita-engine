package org.bonitasoft.engine.core.process.instance.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.core.process.instance.model.builder.ActorBuilder.anActor;
import static org.bonitasoft.engine.core.process.instance.model.builder.ActorMemberBuilder.anActorMember;
import static org.bonitasoft.engine.core.process.instance.model.builder.PendingActivityMappingBuilder.aPendingActivityMapping;
import static org.bonitasoft.engine.core.process.instance.model.builder.UserBuilder.aUser;
import static org.bonitasoft.engine.core.process.instance.model.builder.UserMembershipBuilder.aUserMembership;
import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.List;

import org.bonitasoft.engine.actor.mapping.model.impl.SActorImpl;
import org.bonitasoft.engine.actor.mapping.model.impl.SActorMemberImpl;
import org.bonitasoft.engine.core.process.instance.model.builder.ActorBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.ActorMemberBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.PendingActivityMappingBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.UserBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.UserMembershipBuilder;
import org.bonitasoft.engine.core.process.instance.model.impl.SPendingActivityMappingImpl;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.impl.SPersistentObjectImpl;
import org.bonitasoft.engine.identity.model.impl.SUserImpl;
import org.bonitasoft.engine.identity.model.impl.SUserMembershipImpl;
import org.bonitasoft.engine.persistence.PersistentObjectId;
import org.hamcrest.MatcherAssert;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class ProcessInstanceQueriesTest {

    @Autowired
    private SessionFactory sessionFactory;
    
    @Test
    public void getPossibleUserIdsOfPendingTasks_should_return_users_mapped_with_by_user_id_in_penging_activities() throws Exception {
        SUser expectedUser = addToRepository(aUser().withId(1L).build());
        addToRepository(aUser().withId(2L).build()); // not expected user
        SPendingActivityMappingImpl addedPendingMapping = addToRepository(aPendingActivityMapping().withUserId(expectedUser.getId()).build());
        
        List<Long> userIds = getPossibleUserIdsOfPendingTasks(addedPendingMapping.getActivityId());
        
        assertThat(userIds).containsExactly(expectedUser.getId());
    }
    
    @Test
    public void getPossibleUserIdsOfPendingTasks_should_return_users_mapped_by_a_user_based_actormember_to_pending_activity() throws Exception {
        SActorImpl actor = addToRepository(anActor().build());
        SPendingActivityMappingImpl addedPendingMapping = addToRepository(aPendingActivityMapping().withActorId(actor.getId()).build());
        SUser expectedUser = addToRepository(aUser().withId(1L).build());
        addToRepository(anActorMember().forActor(actor).withUserId(expectedUser.getId()).build());
        addToRepository(aUser().withId(2L).build()); // not expected user
        
        List<Long> userIds = getPossibleUserIdsOfPendingTasks(addedPendingMapping.getActivityId());
        
        assertThat(userIds).containsExactly(expectedUser.getId());
    }
    
    @Test
    public void getPossibleUserIdsOfPendingTasks_should_return_users_mapped_by_a_group_based_actormember_to_pending_activity() throws Exception {
        long aGroupId = 999L;
        long anotherGroupId = 77L;
        long aRoleId = 5L;
        SActorImpl actor = addToRepository(anActor().build());
        SPendingActivityMappingImpl addedPendingMapping = addToRepository(aPendingActivityMapping().withActorId(actor.getId()).build());
        addToRepository(anActorMember().forActor(actor).withGroupId(aGroupId).build());
        SUser expectedUser = addToRepository(aUser().withId(1L).build());
        addToRepository(aUserMembership().forUser(expectedUser).memberOf(aGroupId, aRoleId).build());
        SUser notExpectedUser = addToRepository(aUser().withId(2L).build()); 
        addToRepository(aUserMembership().forUser(notExpectedUser).memberOf(anotherGroupId, aRoleId).build());
        
        List<Long> userIds = getPossibleUserIdsOfPendingTasks(addedPendingMapping.getActivityId());
        
        assertThat(userIds).containsExactly(expectedUser.getId());
    }
    
    @Test
    public void getPossibleUserIdsOfPendingTasks_should_return_users_mapped_by_a_role_based_actormember_to_pending_activity() throws Exception {
        SActorImpl actor = addToRepository(anActor().build());
        SPendingActivityMappingImpl addedPendingMapping = addToRepository(aPendingActivityMapping().withActorId(actor.getId()).build());
        long aRoleId = 999L;
        long anotheraRoleId = 4L;
        long aGrouId = 1L;
        addToRepository(anActorMember().forActor(actor).withRoleId(aRoleId).build());
        SUser expectedUser = addToRepository(aUser().withId(1L).build());
        addToRepository(aUserMembership().forUser(expectedUser).memberOf(aGrouId, aRoleId).build());
        SUser notexpectedUser = addToRepository(aUser().withId(2L).build());
        addToRepository(aUserMembership().forUser(notexpectedUser).memberOf(aGrouId, anotheraRoleId).build());
        
        List<Long> userIds = getPossibleUserIdsOfPendingTasks(addedPendingMapping.getActivityId());
        
        assertThat(userIds).containsExactly(expectedUser.getId());
    }
    
    
    @Test
    public void getPossibleUserIdsOfPendingTasks_should_return_users_mapped_by_a_membership_based_actormember_to_pending_activity() throws Exception {
        SUser addedUser = addToRepository(aUser().withId(1L).build());
        SUser user2 = addToRepository(aUser().withId(2L).build());
        SUser user3 = addToRepository(aUser().withId(3L).build());
        SUser user4 = addToRepository(aUser().withId(4L).build());
        SActorImpl actor = addToRepository(anActor().build());
        long aRoleId = 999L;
        long aGroupId = 888L;
        long anotherGroupId = 777L;
        long anotherRoleId = 546L;
        addToRepository(anActorMember().forActor(actor).withRoleId(aRoleId).withGroupId(aGroupId).build());
        addToRepository(aUserMembership().forUser(addedUser).memberOf(aGroupId, aRoleId).build());
        addToRepository(aUserMembership().forUser(user2).memberOf(anotherGroupId, aRoleId).build());
        addToRepository(aUserMembership().forUser(user3).memberOf(aGroupId, anotherRoleId).build());
        addToRepository(aUserMembership().forUser(user4).memberOf(aGroupId, aRoleId).build());
        SPendingActivityMappingImpl addedPendingMapping = addToRepository(aPendingActivityMapping().withActorId(actor.getId()).build());
        
        List<Long> userIds = getPossibleUserIdsOfPendingTasks(addedPendingMapping.getActivityId());
        
        assertThat(userIds).containsExactly(1L, 4L);
    }
    
    @Test
    public void getPossibleUserIdsOfPendingTasks_return_userIds_ordered_by_userName() throws Exception {
        SUser john = addToRepository(aUser().withUserName("john").withId(1L).build());
        SUser paul = addToRepository(aUser().withUserName("paul").withId(2L).build());
        SUser walter = addToRepository(aUser().withUserName("walter").withId(3L).build());
        SUser marie = addToRepository(aUser().withUserName("marie").withId(4L).build());
        
        long aRoleId = 999L;
        long aGroupId = 888L;
        SActorImpl actor = addToRepository(anActor().build());
        addToRepository(anActorMember().forActor(actor).withRoleId(aRoleId).withGroupId(aGroupId).build());
        SPendingActivityMappingImpl addedPendingMapping = addToRepository(aPendingActivityMapping().withActorId(actor.getId()).build());
        
        addToRepository(aUserMembership().forUser(john).memberOf(aGroupId, aRoleId).build());
        addToRepository(aUserMembership().forUser(paul).memberOf(aGroupId, aRoleId).build());
        addToRepository(aUserMembership().forUser(walter).memberOf(aGroupId, aRoleId).build());
        addToRepository(aUserMembership().forUser(marie).memberOf(aGroupId, aRoleId).build());
        
        List<Long> userIds = getPossibleUserIdsOfPendingTasks(addedPendingMapping.getActivityId());
        
        assertThat(userIds).containsExactly(john.getId(), marie.getId(), paul.getId(), walter.getId());
    }
    
    private List<Long> getPossibleUserIdsOfPendingTasks(long activityInstanceId) {
        Query namedQuery = sessionFactory.getCurrentSession().getNamedQuery("getPossibleUserIdsOfPendingTasks");
        namedQuery.setParameter("humanTaskInstanceId", activityInstanceId);
        return namedQuery.list();
    }
    
    private SUser addToRepository(SUserImpl peristentObject) {
        sessionFactory.getCurrentSession().save(peristentObject);
        return (SUser) sessionFactory.getCurrentSession().get(peristentObject.getClass(), new PersistentObjectId(peristentObject.getId(), peristentObject.getTenantId()));
    }
    
    private SPendingActivityMappingImpl addToRepository(SPendingActivityMappingImpl peristentObject) {
        sessionFactory.getCurrentSession().save(peristentObject);
        return (SPendingActivityMappingImpl) sessionFactory.getCurrentSession().get(peristentObject.getClass(), new PersistentObjectId(peristentObject.getId(), peristentObject.getTenantId()));
    }
    
    private SActorMemberImpl addToRepository(SActorMemberImpl peristentObject) {
        sessionFactory.getCurrentSession().save(peristentObject);
        return (SActorMemberImpl) sessionFactory.getCurrentSession().get(peristentObject.getClass(), new PersistentObjectId(peristentObject.getId(), peristentObject.getTenantId()));
    }
    
    private SActorImpl addToRepository(SActorImpl peristentObject) {
        sessionFactory.getCurrentSession().save(peristentObject);
        return (SActorImpl) sessionFactory.getCurrentSession().get(peristentObject.getClass(), new PersistentObjectId(peristentObject.getId(), peristentObject.getTenantId()));
    }
    
    private SUserMembershipImpl addToRepository(SUserMembershipImpl peristentObject) {
        sessionFactory.getCurrentSession().save(peristentObject);
        return (SUserMembershipImpl) sessionFactory.getCurrentSession().get(peristentObject.getClass(), new PersistentObjectId(peristentObject.getId(), peristentObject.getTenantId()));
    }
}
