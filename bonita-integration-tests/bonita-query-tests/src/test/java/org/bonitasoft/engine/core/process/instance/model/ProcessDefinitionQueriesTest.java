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

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.test.persistence.builder.ActorBuilder.anActor;
import static org.bonitasoft.engine.test.persistence.builder.ActorMemberBuilder.anActorMember;
import static org.bonitasoft.engine.test.persistence.builder.UserBuilder.aUser;
import static org.bonitasoft.engine.test.persistence.builder.UserMembershipBuilder.aUserMembership;

import java.util.List;

import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.test.persistence.repository.ProcessInstanceRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class ProcessDefinitionQueriesTest {

    private static final long PROCESS_ID = 45354312L;
    private static final long ROLE_ID = 222222L;
    @Autowired
    private ProcessInstanceRepository repository;
    /*
     * Tests for queries:
     * getNumberOfSUserWhoCanStartProcess
     * searchSUserWhoCanStartProcess
     */

    @Test
    public void searchSUserWhoCanStartProcess_should_return_users_having_the_right_user_membership() {
        long G1 = 333331L;
        long G2 = 333332L;
        long G3 = 333333L;
        long G4 = 333334L;

        long R2 = 222225L;
        long R3 = 222226L;
        long R4 = 222227L;

        final SUser john = repository.add(aUser().withUserName("john").withId(1L).build());
        final SUser paul = repository.add(aUser().withUserName("paul").withId(2L).build());
        final SUser walter = repository.add(aUser().withUserName("walter").withId(3L).build());
        final SUser marie = repository.add(aUser().withUserName("marie").withId(4L).build());
        final SUser helen = repository.add(aUser().withUserName("helen").withId(5L).build());
        final SUser jobs = repository.add(aUser().withUserName("jobs").withId(6L).build());

        final SActor actor = repository.add(anActor().withScopeId(PROCESS_ID).whoIsInitiator().build());

        repository.add(anActorMember().forActor(actor).withUserId(helen.getId()).build());
        repository.add(anActorMember().forActor(actor).withGroupId(G1).withRoleId(ROLE_ID).build());
        repository.add(anActorMember().forActor(actor).withRoleId(R3).build());
        repository.add(anActorMember().forActor(actor).withGroupId(G3).build());

        repository.add(aUserMembership().forUser(john).memberOf(G1, ROLE_ID).build());
        repository.add(aUserMembership().forUser(paul).memberOf(G1, ROLE_ID).build());
        repository.add(aUserMembership().forUser(walter).memberOf(G2, R3).build());
        repository.add(aUserMembership().forUser(marie).memberOf(G3, R2).build());
        repository.add(aUserMembership().forUser(jobs).memberOf(G4, R4).build());

        // John is mapped through membership, but also through direct mapping.
        // He should only be returned once:
        repository.add(anActorMember().forActor(actor).withUserId(john.getId()).build());

        final List<SUser> users = repository.searchSUserWhoCanStartProcess(PROCESS_ID);

        assertThat(users).hasSize(5).containsOnly(john, paul, walter, marie, helen);

        long numberOfSUserWhoCanStartProcess = repository.getNumberOfSUserWhoCanStartProcess(PROCESS_ID);

        assertThat(numberOfSUserWhoCanStartProcess).isEqualTo(5);
    }

    @Test
    public void searchSUserWhoCanStartProcess_should_return_users_mapped_on_initiator_actor_of_the_process() {
        SUser john = repository.add(aUser().withUserName("john").withId(1L).build());
        repository.add(aUser().withUserName("paul").withId(2L).build());
        SActor actor = repository.add(anActor().withScopeId(PROCESS_ID).whoIsInitiator().build());
        repository.add(anActorMember().forActor(actor).withUserId(john.getId()).build());

        List<SUser> users = repository.searchSUserWhoCanStartProcess(PROCESS_ID);

        assertThat(users).hasSize(1).contains(john);

        long numberOfSUserWhoCanStartProcess = repository.getNumberOfSUserWhoCanStartProcess(PROCESS_ID);

        assertThat(numberOfSUserWhoCanStartProcess).isEqualTo(1);
    }
}
