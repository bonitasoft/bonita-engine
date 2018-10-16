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

import java.util.List;

import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.platform.LoginException;
import org.bonitasoft.engine.platform.LogoutException;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.junit.Test;

/**
 * @author Julien Mege
 * @author Celine Souchet
 */
public class ProfileIT extends AbstractProfileIT {

    @Test
    public void should_have_profiles_in_session() throws Exception {
        getApiClient().logout();
        List<String> profilesOfUser1 = getProfilesFromSession(this.user1, "User1Pwd");
        List<String> profilesOfUser2 = getProfilesFromSession(this.user2, "User2Pwd");
        List<String> profilesOfUser3 = getProfilesFromSession(this.user3, "User3Pwd");
        List<String> profilesOfUser4 = getProfilesFromSession(this.user4, "User4Pwd");
        List<String> profilesOfUser5 = getProfilesFromSession(this.user5, "User5Pwd");
        loginOnDefaultTenantWithDefaultTechnicalUser();

        //Theses profiles are the one mapped in the AllProfiles.xml test file
        assertThat(profilesOfUser1).containsExactlyInAnyOrder("Administrator", "User");
        assertThat(profilesOfUser2).isEmpty();
        assertThat(profilesOfUser3).isEmpty();
        assertThat(profilesOfUser4).containsExactlyInAnyOrder("Team manager");
        assertThat(profilesOfUser5).isEmpty();
    }

    private List<String> getProfilesFromSession(User user, String password) throws LoginException, LogoutException {
        getApiClient().login(user.getUserName(), password);
        List<String> profiles = getApiClient().getSession().getProfiles();
        getApiClient().logout();
        return profiles;
    }

    @Test
    public void searchProfile() throws BonitaException {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileSearchDescriptor.NAME, Order.DESC);

        final SearchResult<Profile> searchedProfiles = getProfileAPI().searchProfiles(builder.done());
        assertEquals(4, searchedProfiles.getCount());
        assertEquals("User", searchedProfiles.getResult().get(0).getName());
    }

    @Test
    public void searchProfileWithSearchTerm() throws BonitaException {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileSearchDescriptor.NAME, Order.ASC);
        builder.searchTerm("Adm");

        final SearchResult<Profile> searchedProfiles = getProfileAPI().searchProfiles(builder.done());
        assertEquals(1, searchedProfiles.getCount());
        assertEquals("Administrator", searchedProfiles.getResult().get(0).getName());
    }

    @Test(expected = SearchException.class)
    public void searchProfileWithWrongParameter() throws Exception {
        getProfileAPI().searchProfiles(null);
    }

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

    private void shouldProfileMemberOperation_update_profile_metadata(final ProfileMemberCreator creator) throws Exception {
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
