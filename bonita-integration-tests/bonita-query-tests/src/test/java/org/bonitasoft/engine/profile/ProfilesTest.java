/**
 * Copyright (C) 2017 Bonitasoft S.A.
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
package org.bonitasoft.engine.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.bonitasoft.engine.test.persistence.builder.GroupBuilder.aGroup;
import static org.bonitasoft.engine.test.persistence.builder.ProfileBuilder.aProfile;
import static org.bonitasoft.engine.test.persistence.builder.ProfileEntryBuilder.aProfileEntry;
import static org.bonitasoft.engine.test.persistence.builder.ProfileMemberBuilder.aProfileMember;
import static org.bonitasoft.engine.test.persistence.builder.RoleBuilder.aRole;
import static org.bonitasoft.engine.test.persistence.builder.UserBuilder.aUser;
import static org.bonitasoft.engine.test.persistence.builder.UserMembershipBuilder.aUserMembership;

import java.util.List;

import javax.inject.Inject;

import org.bonitasoft.engine.identity.model.impl.SGroupImpl;
import org.bonitasoft.engine.identity.model.impl.SRoleImpl;
import org.bonitasoft.engine.identity.model.impl.SUserImpl;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.model.impl.SProfileImpl;
import org.bonitasoft.engine.test.persistence.repository.ProfileRepository;
import org.bonitasoft.engine.test.persistence.repository.UserMembershipRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Emmanuel Duchastenier
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class ProfilesTest {

    @SuppressWarnings("unused")
    @Inject
    private ProfileRepository repository;

    @SuppressWarnings("unused")
    @Inject
    private UserMembershipRepository userMembershipRepository;

    @Test
    public void profile_with_user_mapped_and_profile_entry_should_be_retrieved() throws Exception {
        // given:
        final SUserImpl user = aUser().build();
        repository.add(user);
        final String profileName = "retrieved";
        final SProfileImpl profile = aProfile().withName(profileName).build();
        repository.add(profile);
        repository.add(aProfileMember().withProfileId(profile.getId()).withUserId(user.getId()).build());
        repository.add(aProfileEntry().withProfileId(profile.getId()).build());

        // when:
        final List<SProfile> profiles = repository.getProfilesWithNavigationOfUser(user.getId());

        // then:
        assertThat(profiles).hasSize(1).extracting("id", "name").containsExactly(tuple(profile.getId(), profileName));
    }

    @Test
    public void profile_with_role_mapped_and_profile_entry_should_be_retrieved() throws Exception {
        // given:
        final SRoleImpl role = aRole().build();
        final SUserImpl user = aUser().build();
        userMembershipRepository.add(aUserMembership().forUser(user).memberOf(-1, role.getId()).build());
        repository.add(role);
        repository.add(user);
        final String profileName = "retrieved";
        final SProfileImpl profile = aProfile().withName(profileName).build();
        repository.add(profile);
        repository.add(aProfileMember().withProfileId(profile.getId()).withRoleId(role.getId()).build());
        repository.add(aProfileEntry().withProfileId(profile.getId()).build());

        // when:
        final List<SProfile> profiles = repository.getProfilesWithNavigationOfUser(user.getId());

        // then:
        assertThat(profiles).hasSize(1).extracting("id", "name").containsExactly(tuple(profile.getId(), profileName));
    }

    @Test
    public void profile_with_group_mapped_and_profile_entry_should_be_retrieved() throws Exception {
        // given:
        final SGroupImpl group = aGroup().build();
        final SUserImpl user = aUser().build();
        userMembershipRepository.add(aUserMembership().forUser(user).memberOf(group.getId(), -1).build());
        repository.add(group);
        repository.add(user);
        final String profileName = "retrieved";
        final SProfileImpl profile = aProfile().withName(profileName).build();
        repository.add(profile);
        repository.add(aProfileMember().withProfileId(profile.getId()).withGroupId(group.getId()).build());
        repository.add(aProfileEntry().withProfileId(profile.getId()).build());

        // when:
        final List<SProfile> profiles = repository.getProfilesWithNavigationOfUser(user.getId());

        // then:
        assertThat(profiles).hasSize(1).extracting("id", "name").containsExactly(tuple(profile.getId(), profileName));
    }

    @Test
    public void profile_with_membership_mapped_and_profile_entry_should_be_retrieved() throws Exception {
        // given:
        final SGroupImpl group = aGroup().build();
        final SRoleImpl role = aRole().build();
        final SUserImpl user = aUser().build();
        userMembershipRepository.add(aUserMembership().forUser(user).memberOf(group.getId(), role.getId()).build());
        repository.add(group);
        repository.add(role);
        repository.add(user);
        final String profileName = "retrieved";
        final SProfileImpl profile = aProfile().withName(profileName).build();
        repository.add(profile);
        repository.add(aProfileMember().withProfileId(profile.getId()).withGroupId(group.getId())
                .withRoleId(role.getId()).build());
        repository.add(aProfileEntry().withProfileId(profile.getId()).build());

        // when:
        final List<SProfile> profiles = repository.getProfilesWithNavigationOfUser(user.getId());

        // then:
        assertThat(profiles).hasSize(1).extracting("id", "name").containsExactly(tuple(profile.getId(), profileName));
    }

    @Test
    public void profile_without_user_mapped_should_not_be_retrieved() throws Exception {
        // given:
        final SUserImpl user = aUser().build();
        repository.add(user);
        final String profileName = "should not be retrieved";
        final SProfileImpl profile = aProfile().withName(profileName).build();
        repository.add(profile);
        repository.add(aProfileEntry().withProfileId(profile.getId()).build());

        // when:
        final List<SProfile> profiles = repository.getProfilesWithNavigationOfUser(user.getId());

        // then:
        assertThat(profiles).hasSize(0);
    }

    @Test
    public void profile_with_user_mapped_but_without_navigation_should_not_be_retrieved() throws Exception {
        // given:
        final SUserImpl user = aUser().build();
        repository.add(user);
        final String profileName = "should not be retrieved";
        final SProfileImpl profile = aProfile().withName(profileName).build();
        repository.add(profile);
        repository.add(aProfileMember().withProfileId(profile.getId()).withUserId(user.getId()).build());

        // when:
        final List<SProfile> profiles = repository.getProfilesWithNavigationOfUser(user.getId());

        // then:
        assertThat(profiles).hasSize(0);
    }

    @Test
    public void profile_without_given_user_mapped_should_not_be_retrieved() throws Exception {
        // given:
        final SUserImpl userMapped = aUser().build();
        final SUserImpl userAsked = aUser().build();
        repository.add(userMapped);
        final String profileName = "should not be retrieved";
        final SProfileImpl profile = aProfile().withName(profileName).build();
        repository.add(profile);
        repository.add(aProfileMember().withProfileId(profile.getId()).withUserId(userMapped.getId()).build());
        repository.add(aProfileEntry().withProfileId(profile.getId()).build());

        // when:
        final List<SProfile> profiles = repository.getProfilesWithNavigationOfUser(userAsked.getId());

        // then:
        assertThat(profiles).hasSize(0);
    }

}
