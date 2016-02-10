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
package org.bonitasoft.engine.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

/**
 * @author Julien Mege
 * @author Celine Souchet
 */
public class ProfileIT extends AbstractProfileIT {

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Search" }, story = "Search profile.", jira = "")
    @Test
    public void searchProfile() throws BonitaException {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileSearchDescriptor.NAME, Order.DESC);

        final SearchResult<Profile> searchedProfiles = getProfileAPI().searchProfiles(builder.done());
        assertEquals(4, searchedProfiles.getCount());
        assertEquals("User", searchedProfiles.getResult().get(0).getName());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Search" }, story = "Search profile.", jira = "")
    @Test
    public void searchProfileWithSearchTerm() throws BonitaException {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileSearchDescriptor.NAME, Order.ASC);
        builder.searchTerm("Adm");

        final SearchResult<Profile> searchedProfiles = getProfileAPI().searchProfiles(builder.done());
        assertEquals(1, searchedProfiles.getCount());
        assertEquals("Administrator", searchedProfiles.getResult().get(0).getName());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Search", "Wrong parameter" }, jira = "ENGINE-548")
    @Test(expected = SearchException.class)
    public void searchProfileWithWrongParameter() throws Exception {
        getProfileAPI().searchProfiles(null);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Wrong parameter" }, jira = "ENGINE-548")
    @Test(expected = ProfileNotFoundException.class)
    public void getProfileWithWrongParameter() throws Exception {
        getProfileAPI().getProfile(855);
    }

    @Test
    public void addUser_to_profile_updates_profile_metadata() throws Exception {
        final ProfileMemberCreator creator = new ProfileMemberCreator(getProfileAPI().getProfile(adminProfileId).getId());
        creator.setUserId(user3.getId());

        shouldProfileMemberOperation_update_profile_metadata(creator);
    }

    @Test
    public void addGroup_to_profile_updates_profile_metadata() throws Exception {
        final ProfileMemberCreator creator = new ProfileMemberCreator(getProfileAPI().getProfile(adminProfileId).getId());
        creator.setGroupId(group3.getId());

        shouldProfileMemberOperation_update_profile_metadata(creator);
    }

    @Test
    public void addRole_to_profile_updates_profile_metadata() throws Exception {
        final ProfileMemberCreator creator = new ProfileMemberCreator(getProfileAPI().getProfile(adminProfileId).getId());
        creator.setRoleId(role3.getId());

        shouldProfileMemberOperation_update_profile_metadata(creator);
    }

    @Test
    public void addRoleAndGroup_to_profile_updates_profile_metadata() throws Exception {
        final ProfileMemberCreator creator = new ProfileMemberCreator(getProfileAPI().getProfile(adminProfileId).getId());
        creator.setGroupId(group3.getId());
        creator.setRoleId(role3.getId());

        shouldProfileMemberOperation_update_profile_metadata(creator);
    }

    private void shouldProfileMemberOperation_update_profile_metadata(final ProfileMemberCreator creator) throws ProfileNotFoundException, BonitaException,
            CreationException, AlreadyExistsException,
            DeletionException, Exception {
        // given
        final Profile profileBefore = getProfileAPI().getProfile(adminProfileId);

        // when
        logoutOnTenant();
        loginOnDefaultTenantWith("userName3", "User3Pwd");
        final ProfileMember createProfileMember = getProfileAPI().createProfileMember(creator);

        // then
        final Profile profileAfter = getProfileAPI().getProfile(adminProfileId);
        checkMetaData(profileBefore, profileAfter, user3);

        // when
        logoutOnTenant();
        loginOnDefaultTenantWith("userName1", "User1Pwd");
        getProfileAPI().deleteProfileMember(createProfileMember.getId());

        // then
        final Profile profileAfterDelete = getProfileAPI().getProfile(adminProfileId);
        checkMetaData(profileAfter, profileAfterDelete, user1);
    }

    private void checkMetaData(final Profile profileBefore, final Profile profileAfter, final User user) {
        assertThat(profileAfter.getLastUpdateDate()).as("lastUpdateDate should be modified").isAfter(profileBefore.getLastUpdateDate());
        assertThat(profileAfter.getLastUpdatedBy()).as("lastUpdatedBy should be modified").isNotEqualTo(profileBefore.getLastUpdatedBy());
        assertThat(profileAfter.getLastUpdatedBy()).as("lastUpdatedBy should be modified").isEqualTo(user.getId());

    }

}
