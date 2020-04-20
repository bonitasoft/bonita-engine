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
package org.bonitasoft.engine.profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SUserCreationException;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.profile.exception.profile.SProfileNotFoundException;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.model.SProfileMember;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public class ProfileServiceIT extends CommonBPMServicesTest {

    private static ProfileService profileService;

    private static IdentityService identityService;

    @Before
    public void setup() {
        profileService = getTenantAccessor().getProfileService();
        identityService = getTenantAccessor().getIdentityService();
    }

    @Test(expected = SProfileNotFoundException.class)
    public void cannotGetAnUnknownProfile() throws SBonitaException {
        try {
            getTransactionService().begin();
            profileService.getProfile(10);
            Assert.fail();
        } finally {
            getTransactionService().complete();
        }
    }

    @Test
    public void getProfile() throws SBonitaException {
        getTransactionService().begin();
        final SProfile profile = SProfile.builder()
                .name("profile1")
                .isDefault(true).build();
        final SProfile createdProfile = profileService.createProfile(profile);
        final SProfile gotProfile = profileService.getProfile(createdProfile.getId());
        Assert.assertEquals(createdProfile, gotProfile);
        profileService.deleteProfile(gotProfile);
        try {
            profileService.getProfile(createdProfile.getId());
            Assert.fail();
        } catch (final SProfileNotFoundException spnfe) {
            getTransactionService().complete();
        }
    }

    @Test
    public void getUserProfile() throws SBonitaException {
        getTransactionService().begin();
        final SProfile profile = profileService.createProfile(SProfile.builder()
                .name("profile1")
                .isDefault(true)
                .build());

        final List<OrderByOption> orderByOptions = getOrderByOptions();
        final QueryOptions queryOptions = new QueryOptions(0, 10, orderByOptions,
                Collections.singletonList(new FilterOption(SProfileMember.class, "profileId",
                        profile.getId())),
                null);

        List<SProfileMember> profileMembers = profileService.searchProfileMembers("ForUser", queryOptions);
        Assert.assertEquals(0, profileMembers.size());

        SUser.SUserBuilder userBuilder = SUser.builder().userName("john").password("bpm")
                .firstName("John").lastName("Doe");
        final SUser john = identityService.createUser(userBuilder.build());

        userBuilder = SUser.builder().userName("jane").password("bpm").firstName("Jane")
                .lastName("Doe");
        final SUser jane = identityService.createUser(userBuilder.build());

        final SProfileMember johnProfileMember = profileService.addUserToProfile(profile.getId(), john.getId(), "John",
                "Doe", "john");
        final SProfileMember janeProfileMember = profileService.addUserToProfile(profile.getId(), jane.getId(), "Jane",
                "Doe", "jane");
        profileMembers = profileService.searchProfileMembers("ForUser", queryOptions);
        Assert.assertEquals(2, profileMembers.size());

        profileService.deleteProfileMember(johnProfileMember);
        profileService.deleteProfileMember(janeProfileMember);
        profileMembers = profileService.searchProfileMembers("ForUser", queryOptions);
        Assert.assertEquals(0, profileMembers.size());

        identityService.deleteUser(john);
        identityService.deleteUser(jane);
        profileService.deleteProfile(profile);
        getTransactionService().complete();
    }

    private List<OrderByOption> getOrderByOptions() {
        final List<OrderByOption> orderByOptions = new ArrayList<OrderByOption>(1);
        orderByOptions.add(new OrderByOption(SUser.class, SUser.FIRST_NAME, OrderByType.ASC));
        return orderByOptions;
    }

    private SUser createUser(final String username, final String password) throws SUserCreationException {
        final SUser.SUserBuilder userBuilder = SUser.builder().userName(username).password(password);
        return identityService.createUser(userBuilder.build());
    }

    @Test
    public void getProfileOfUserFrom() throws SBonitaException {
        getTransactionService().begin();
        final SProfile profile = profileService.createProfile(SProfile.builder()
                .name("profile1")
                .build());

        final SUser john = createUser("john", "bpm");
        final SUser jane = createUser("jane", "bpm");

        List<SProfile> profilesOfUser = profileService.searchProfilesOfUser(john.getId(), 0, 10, "name",
                OrderByType.ASC);
        Assert.assertEquals(0, profilesOfUser.size());

        final SProfileMember johnProfileMember = profileService.addUserToProfile(profile.getId(), john.getId(), "John",
                "Doe", "john");
        final SProfileMember janeProfileMember = profileService.addUserToProfile(profile.getId(), jane.getId(), "Jane",
                "Doe", "jane");

        profilesOfUser = profileService.searchProfilesOfUser(john.getId(), 0, 10, "name", OrderByType.ASC);
        Assert.assertEquals(1, profilesOfUser.size());

        final QueryOptions countOptions = new QueryOptions(0, 10, null,
                Collections.singletonList(new FilterOption(SProfileMember.class, "profileId", profile
                        .getId())),
                null);

        Assert.assertEquals(2, profileService.getNumberOfProfileMembers("ForUser", countOptions));

        profileService.deleteProfileMember(johnProfileMember);
        profileService.deleteProfileMember(janeProfileMember);

        profilesOfUser = profileService.searchProfilesOfUser(john.getId(), 0, 10, "name", OrderByType.ASC);
        Assert.assertEquals(0, profilesOfUser.size());

        identityService.deleteUser(john);
        identityService.deleteUser(jane);
        profileService.deleteProfile(profile);
        getTransactionService().complete();
    }

}
