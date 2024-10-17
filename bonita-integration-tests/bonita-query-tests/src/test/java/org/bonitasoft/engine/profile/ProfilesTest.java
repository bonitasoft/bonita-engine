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
import static org.bonitasoft.engine.test.persistence.builder.ProfileMemberBuilder.aProfileMember;
import static org.bonitasoft.engine.test.persistence.builder.RoleBuilder.aRole;
import static org.bonitasoft.engine.test.persistence.builder.UserBuilder.aUser;
import static org.bonitasoft.engine.test.persistence.builder.UserMembershipBuilder.aUserMembership;

import java.util.List;

import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.test.persistence.repository.ProfileRepository;
import org.bonitasoft.engine.test.persistence.repository.UserMembershipRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private ProfileRepository repository;

    @SuppressWarnings("unused")
    @Autowired
    private UserMembershipRepository userMembershipRepository;

    @Test
    public void profile_with_user_mapped_should_be_retrieved() {
        // given:
        final SUser user = aUser().build();
        repository.add(user);
        final String profileName = "retrieved";
        final SProfile profile = aProfile().withName(profileName).build();
        repository.add(profile);
        repository.add(aProfileMember().withProfileId(profile.getId()).withUserId(user.getId()).build());

        // when:
        final List<SProfile> profiles = repository.getProfilesOfUser(user.getId());

        // then:
        assertThat(profiles).hasSize(1).extracting("id", "name").containsExactly(tuple(profile.getId(), profileName));
    }

    @Test
    public void profile_with_role_mapped_should_be_retrieved() {
        // given:
        final SRole role = aRole().build();
        final SUser user = aUser().build();
        repository.add(role);
        repository.add(user);
        userMembershipRepository.add(aUserMembership().forUser(user).memberOf(-1, role.getId()).build());
        final String profileName = "retrieved";
        final SProfile profile = aProfile().withName(profileName).build();
        repository.add(profile);
        repository.add(aProfileMember().withProfileId(profile.getId()).withRoleId(role.getId()).build());

        // when:
        final List<SProfile> profiles = repository.getProfilesOfUser(user.getId());

        // then:
        assertThat(profiles).hasSize(1).extracting("id", "name").containsExactly(tuple(profile.getId(), profileName));
    }

    @Test
    public void no_profileWithNavigation_should_be_retrieved_for_system_user() {
        // given:
        final SRole role = aRole().build();
        repository.add(role);
        final SProfile profile = aProfile().withName("not retrieved").build();
        repository.add(profile);
        repository.add(aProfileMember().withProfileId(profile.getId()).withRoleId(role.getId()).build());

        // when:
        final List<SProfile> profiles = repository.getProfilesOfUser(-1);

        // then:
        assertThat(profiles).hasSize(0);
    }

    @Test
    public void profile_with_group_mapped_should_be_retrieved() {
        // given:
        final SGroup group = aGroup().build();
        final SUser user = aUser().build();
        repository.add(group);
        repository.add(user);
        userMembershipRepository.add(aUserMembership().forUser(user).memberOf(group.getId(), -1).build());
        final String profileName = "retrieved";
        final SProfile profile = aProfile().withName(profileName).build();
        repository.add(profile);
        repository.add(aProfileMember().withProfileId(profile.getId()).withGroupId(group.getId()).build());

        // when:
        final List<SProfile> profiles = repository.getProfilesOfUser(user.getId());

        // then:
        assertThat(profiles).hasSize(1).extracting("id", "name").containsExactly(tuple(profile.getId(), profileName));
    }

    @Test
    public void profile_with_membership_mapped_should_be_retrieved() {
        // given:
        final SGroup group = aGroup().build();
        final SRole role = aRole().build();
        final SUser user = aUser().build();
        repository.add(group);
        repository.add(role);
        repository.add(user);
        userMembershipRepository.add(aUserMembership().forUser(user).memberOf(group.getId(), role.getId()).build());
        final String profileName = "retrieved";
        final SProfile profile = aProfile().withName(profileName).build();
        repository.add(profile);
        repository.add(aProfileMember().withProfileId(profile.getId()).withGroupId(group.getId())
                .withRoleId(role.getId()).build());

        // when:
        final List<SProfile> profiles = repository.getProfilesOfUser(user.getId());

        // then:
        assertThat(profiles).hasSize(1).extracting("id", "name").containsExactly(tuple(profile.getId(), profileName));
    }

    @Test
    public void profile_without_user_mapped_should_not_be_retrieved() {
        // given:
        final SUser user = aUser().build();
        repository.add(user);
        final String profileName = "should not be retrieved";
        final SProfile profile = aProfile().withName(profileName).build();
        repository.add(profile);

        // when:
        final List<SProfile> profiles = repository.getProfilesOfUser(user.getId());

        // then:
        assertThat(profiles).hasSize(0);
    }

    @Test
    public void profile_without_given_user_mapped_should_not_be_retrieved() {
        // given:
        final SUser userMapped = aUser().build();
        final SUser userAsked = aUser().build();
        repository.add(userMapped);
        final String profileName = "should not be retrieved";
        final SProfile profile = aProfile().withName(profileName).build();
        repository.add(profile);
        repository.add(aProfileMember().withProfileId(profile.getId()).withUserId(userMapped.getId()).build());

        // when:
        final List<SProfile> profiles = repository.getProfilesOfUser(userAsked.getId());

        // then:
        assertThat(profiles).hasSize(0);
    }

    @Test
    public void no_profile_should_be_retrieved_for_system_user() {
        // given:
        final SRole role = aRole().build();
        repository.add(role);
        final SProfile profile = aProfile().withName("not retrieved").build();
        repository.add(profile);
        repository.add(aProfileMember().withProfileId(profile.getId()).withRoleId(role.getId()).build());

        // when:
        final List<SProfile> profiles = repository.getProfilesOfUser(-1);

        // then:
        assertThat(profiles).hasSize(0);
    }

}
