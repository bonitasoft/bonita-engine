/*
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
 */
package org.bonitasoft.engine.core.process.instance.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.test.persistence.builder.ActorBuilder.anActor;
import static org.bonitasoft.engine.test.persistence.builder.ActorMemberBuilder.anActorMember;
import static org.bonitasoft.engine.test.persistence.builder.UserBuilder.aUser;
import static org.bonitasoft.engine.test.persistence.builder.UserMembershipBuilder.aUserMembership;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.actor.mapping.model.SActorMember;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.test.persistence.repository.UserMembershipRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * author Emmanuel Duchastenier
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class ActorMappingTest {

    @Inject
    private UserMembershipRepository repository;

    @Test
    public void userWithUserMappingShouldBeReturned() {
        final SActor actor = repository.add(anActor().build());
        long unusedRoleId = 111L;
        long unusedGroupId = 222L;
        final SUser user = repository.add(aUser().withId(1L).build());
        SActorMember actorMember = repository.add(anActorMember().forActor(actor).withUserId(user.getId()).build());
        repository.add(aUserMembership().forUser(user).memberOf(unusedGroupId, unusedRoleId).build());

        List<Long> actorMemberIds = new ArrayList<Long>(1);
        actorMemberIds.add(actorMember.getId());
        Long members = repository.getNumberOfUserMembersForUserOrManagerForActorMembers(user.getId(), actorMemberIds);
        assertThat(members).isEqualTo(1L);
    }

    @Test
    public void userWithRoleMappingShouldBeReturned() {
        final SActor actor = repository.add(anActor().build());
        long aRoleId = 111L;
        long unusedGroupId = 222L;
        SActorMember actorMember = repository.add(anActorMember().forActor(actor).withRoleId(aRoleId).build());
        final SUser user = repository.add(aUser().withId(22L).build());
        repository.add(aUserMembership().forUser(user).memberOf(unusedGroupId, aRoleId).build());

        List<Long> actorMemberIds = new ArrayList<Long>(1);
        actorMemberIds.add(actorMember.getId());
        Long members = repository.getNumberOfUserMembersForUserOrManagerForActorMembers(user.getId(), actorMemberIds);
        assertThat(members).isEqualTo(1L);
    }

    @Test
    public void userWithGroupMappingShouldBeReturned() {
        final SActor actor = repository.add(anActor().build());
        long unusedRoleId = 111L;
        long aGroupId = 222L;
        SActorMember actorMember = repository.add(anActorMember().forActor(actor).withGroupId(aGroupId).build());
        final SUser user = repository.add(aUser().withId(333L).build());
        repository.add(aUserMembership().forUser(user).memberOf(aGroupId, unusedRoleId).build());

        List<Long> actorMemberIds = new ArrayList<Long>(1);
        actorMemberIds.add(actorMember.getId());
        Long members = repository.getNumberOfUserMembersForUserOrManagerForActorMembers(user.getId(), actorMemberIds);
        assertThat(members).isEqualTo(1L);
    }

    @Test
    public void userWithGroupAndRoleMappingShouldBeReturned() {
        final SActor actor = repository.add(anActor().build());
        long aRoleId = 111L;
        long aGroupId = 222L;
        SActorMember actorMember = repository.add(anActorMember().forActor(actor).withRoleId(aRoleId).withGroupId(aGroupId).build());
        final SUser user = repository.add(aUser().withId(4444L).build());
        repository.add(aUserMembership().forUser(user).memberOf(aGroupId, aRoleId).build());

        Long members = repository.getNumberOfUserMembersForUserOrManagerForActorMembers(user.getId(), Arrays.asList(actorMember.getId()));
        assertThat(members).isEqualTo(1L);
    }

    @Test
    public void managerOfUserWithUserMappingShouldBeReturned() {
        final SActor actor = repository.add(anActor().build());
        long unusedRoleId = 111L;
        long unusedGroupId = 222L;
        final SUser manager = repository.add(aUser().withId(2L).build());
        final SUser user = repository.add(aUser().withId(5555L).withManager(manager.getId()).build());
        SActorMember actorMember = repository.add(anActorMember().forActor(actor).withUserId(user.getId()).build());
        repository.add(aUserMembership().forUser(user).memberOf(unusedGroupId, unusedRoleId).build());

        Long members = repository.getNumberOfUserMembersForUserOrManagerForActorMembers(manager.getId(), Arrays.asList(actorMember.getId()));
        assertThat(members).isEqualTo(1L);
    }

    @Test
    public void managerOfUserWithRoleMappingShouldBeReturned() {
        final SActor actor = repository.add(anActor().build());
        long aRoleId = 111L;
        long unusedGroupId = 222L;
        SActorMember actorMember = repository.add(anActorMember().forActor(actor).withRoleId(aRoleId).build());
        final SUser manager = repository.add(aUser().withId(2L).build());
        final SUser user = repository.add(aUser().withId(66666L).withManager(manager.getId()).build());
        repository.add(aUserMembership().forUser(user).memberOf(unusedGroupId, aRoleId).build());

        Long members = repository.getNumberOfUserMembersForUserOrManagerForActorMembers(manager.getId(), Arrays.asList(actorMember.getId()));
        assertThat(members).isEqualTo(1L);
    }

    @Test
    public void managerOfUserWithGroupMappingShouldBeReturned() {
        final SActor actor = repository.add(anActor().build());
        long unusedRoleId = 111L;
        long aGroupId = 222L;
        SActorMember actorMember = repository.add(anActorMember().forActor(actor).withGroupId(aGroupId).build());
        final SUser manager = repository.add(aUser().withId(2L).build());
        final SUser user = repository.add(aUser().withId(77777L).withManager(manager.getId()).build());
        repository.add(aUserMembership().forUser(user).memberOf(aGroupId, unusedRoleId).build());

        Long members = repository.getNumberOfUserMembersForUserOrManagerForActorMembers(manager.getId(), Arrays.asList(actorMember.getId()));
        assertThat(members).isEqualTo(1L);
    }

    @Test
    public void managerOfUserWithGroupAndRoleMappingShouldBeReturned() {
        final SActor actor = repository.add(anActor().build());
        long aRoleId = 111L;
        long aGroupId = 222L;
        SActorMember actorMember = repository.add(anActorMember().forActor(actor).withGroupId(aGroupId).withRoleId(aRoleId).build());
        final SUser manager = repository.add(aUser().withId(2L).build());
        final SUser user = repository.add(aUser().withId(888888L).withManager(manager.getId()).build());
        repository.add(aUserMembership().forUser(user).memberOf(aGroupId, aRoleId).build());

        Long members = repository.getNumberOfUserMembersForUserOrManagerForActorMembers(manager.getId(), Arrays.asList(actorMember.getId()));
        assertThat(members).isEqualTo(1L);
    }

    @Test
    public void userWithNoMatchingGroupAndRoleShouldNotBeReturned() {
        final SActor actor = repository.add(anActor().build());
        long aRoleId = 111L;
        long aGroupId = 222L;
        SActorMember actorMember = repository.add(anActorMember().forActor(actor).withGroupId(aGroupId).withRoleId(aRoleId).build());
        final SUser user = repository.add(aUser().withId(99999999L).build());
        repository.add(aUserMembership().forUser(user).memberOf(aGroupId, 16545L).build());

        Long members = repository.getNumberOfUserMembersForUserOrManagerForActorMembers(user.getId(), Arrays.asList(actorMember.getId()));
        assertThat(members).isEqualTo(0L);
    }

    @Test
    public void userWithNoMembershipButWithDirectActorMappingShouldBeReturned() {
        final SActor actor = repository.add(anActor().build());
        long aRoleId = 111L;
        long aGroupId = 222L;
        final SUser user = repository.add(aUser().withId(132457L).build());
        SActorMember actorMember = repository.add(anActorMember().forActor(actor).withUserId(user.getId()).build());

        Long members = repository.getNumberOfUserMembersForUserOrManagerForActorMembers(user.getId(), Arrays.asList(actorMember.getId()));
        assertThat(members).isEqualTo(1L);
    }

}
