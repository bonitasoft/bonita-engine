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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

public class ProfileMemberIT extends AbstractProfileIT {

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "Create", "Delete", "Group" }, story = "Create and delete group to profile.", jira = "")
    @Test
    public void createAndDeleteGroupToProfile() throws BonitaException {
        checkCreateAndDeleProfileMember("group", null, group1.getId(), null);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "Create", "Delete", "Role", "Group" }, story = "Create and delete role and group to profile.", jira = "")
    @Test
    public void createAndDeleteRoleAndGroupToProfile() throws BonitaException {
        getIdentityAPI().addUserMembership(user1.getId(), group1.getId(), role1.getId());
        getIdentityAPI().addUserMembership(user2.getId(), group1.getId(), role2.getId());
        getIdentityAPI().addUserMembership(user3.getId(), group2.getId(), role1.getId());

        // Create group Profile1
        checkCreateAndDeleProfileMember("roleAndGroup", null, group1.getId(), role1.getId());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "Create", "Delete", "Role" }, story = "Create and delete role to profile.", jira = "")
    @Test
    public void createAndDeleteRoleToProfile() throws BonitaException {
        checkCreateAndDeleProfileMember("role", null, null, role3.getId());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "Create", "Delete" }, story = "Create and delete user profile.", jira = "")
    @Test
    public void createandDeleteUserProfile() throws BonitaException {
        checkCreateAndDeleProfileMember("user", user2.getId(), null, null);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "Wrong parameter" }, story = "Execute profile member command with wrong parameter", jira = "ENGINE-586")
    @Test(expected = CreationException.class)
    public void createProfileMemberWithWrongParameter() throws Exception {
        getProfileAPI().createProfileMember(856L, null, null, null);
    }

    @Cover(classes = { ProfileAPI.class, ProfileMember.class, Group.class, Role.class, User.class }, concept = BPMNConcept.ORGANIZATION, jira = "ENGINE-808", keywords = { "delete organization profile  mapping" })
    @Test
    public void deleteOrganizationRemoveProfileMember() throws BonitaException {
        // create user and add profile
        final User user = getIdentityAPI().createUser("mixmaster.spike", "123456789");
        getProfileAPI().createProfileMember(adminProfileId, user.getId(), null, null);

        // delete user
        getIdentityAPI().deleteUser(user.getId());

        // check there is no user mixmaster.spike anymore in this profile
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART1, Order.ASC);
        builder.filter(ProfileMemberSearchDescriptor.PROFILE_ID, adminProfileId);
        final SearchResult<ProfileMember> searchedProfileMember = getProfileAPI().searchProfileMembers("user", builder.done());
        assertEquals(1, searchedProfileMember.getResult().size());
    }

    private void checkCreateAndDeleProfileMember(final String memberType, final Long userId, final Long groupId, final Long roleId) throws BonitaException {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART1, Order.ASC);
        builder.filter(ProfileMemberSearchDescriptor.PROFILE_ID, adminProfileId);
        SearchResult<ProfileMember> searchedProfileMember = getProfileAPI().searchProfileMembers(memberType, builder.done());
        final long numberOfProfileMembersBeforeCreation = searchedProfileMember.getCount();

        final ProfileMember addProfileMemberResult = getProfileAPI().createProfileMember(adminProfileId, userId, groupId, roleId);

        searchedProfileMember = getProfileAPI().searchProfileMembers(memberType, builder.done());
        assertEquals(numberOfProfileMembersBeforeCreation + 1, searchedProfileMember.getCount());

        // delete UserProfile1
        getProfileAPI().deleteProfileMember(addProfileMemberResult.getId());
        searchedProfileMember = getProfileAPI().searchProfileMembers(memberType, builder.done());
        assertEquals(numberOfProfileMembersBeforeCreation, searchedProfileMember.getCount());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "Wrong parameter" }, story = "Execute profile member command with wrong parameter", jira = "ENGINE-586")
    @Test(expected = DeletionException.class)
    public void deleteProfileMemberWithWrongParameter() throws Exception {
        getProfileAPI().deleteProfileMember(856L);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "User", "Twice", "Same" }, jira = "ENGINE-919")
    @Test(expected = CreationException.class)
    public void createTwiceSameProfileMemberWithUser() throws BonitaException {
        getProfileAPI().createProfileMember(adminProfileId, user1.getId(), null, null);
        getProfileAPI().createProfileMember(adminProfileId, user1.getId(), null, null);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "Role", "Twice", "Same" }, jira = "ENGINE-919")
    @Test(expected = CreationException.class)
    public void createTwiceSameProfileMemberWithRole() throws BonitaException {
        getProfileAPI().createProfileMember(adminProfileId, null, null, role1.getId());
        getProfileAPI().createProfileMember(adminProfileId, null, null, role1.getId());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "Group", "Twice", "Same" }, jira = "ENGINE-919")
    @Test(expected = CreationException.class)
    public void createTwiceSameProfileMemberWithGroup() throws BonitaException {
        getProfileAPI().createProfileMember(adminProfileId, null, group1.getId(), null);
        getProfileAPI().createProfileMember(adminProfileId, null, group1.getId(), null);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "UserMembership", "Twice", "Same" }, jira = "ENGINE-919")
    @Test(expected = CreationException.class)
    public void createTwiceSameProfileMemberWithMembership() throws BonitaException {
        getIdentityAPI().addUserMembership(user1.getId(), group1.getId(), role1.getId());

        // Create UserProfile
        getProfileAPI().createProfileMember(adminProfileId, null, group1.getId(), role1.getId());
        getProfileAPI().createProfileMember(adminProfileId, null, group1.getId(), role1.getId());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "User" }, story = "Get profile for user.", jira = "")
    @Test
    public void getProfileForUser() throws BonitaException {
        // Get Profile For User
        final List<Profile> profiles = getProfileAPI().getProfilesForUser(user1.getId());
        assertEquals(1, profiles.size());
        assertEquals("Administrator", profiles.get(0).getName());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "User", "Group" }, story = "Get profile for user must return same profil only once", jira = "ENGINE-979")
    @Test
    public void getProfileForUserReturnDisctinctProfiles() throws BonitaException {
        // user 1 is mapped to profile "Administrator" through direct "userName1" mapping + through "role1" mapping:
        getIdentityAPI().addUserMembership(user1.getId(), group2.getId(), role1.getId());

        // Get Profile For User
        final List<Profile> getUserProfiles = getProfileAPI().getProfilesForUser(user1.getId());

        assertEquals(1, getUserProfiles.size());
        assertEquals("Administrator", getUserProfiles.get(0).getName());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "User", "Group" }, story = "Get profile for user from group.", jira = "")
    @Test
    public void getProfileForUserFromGroup() throws BonitaException {
        getIdentityAPI().addUserMembership(user1.getId(), group1.getId(), role1.getId());

        // Get Profile For User
        final List<Profile> getUserProfiles = getProfileAPI().getProfilesForUser(user1.getId());
        assertEquals(2, getUserProfiles.size());
        assertEquals("Administrator", getUserProfiles.get(0).getName());
        assertEquals("User", getUserProfiles.get(1).getName());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "User", "Role" }, story = "Get profile for user from role.", jira = "")
    @Test
    public void getProfileForUserFromRole() throws BonitaException {
        getIdentityAPI().addUserMembership(user1.getId(), group1.getId(), role1.getId());

        // Get Profile For User
        final List<Profile> getUserProfiles = getProfileAPI().getProfilesForUser(user1.getId());
        assertEquals(2, getUserProfiles.size());
        assertEquals("Administrator", getUserProfiles.get(0).getName());
        assertEquals("User", getUserProfiles.get(1).getName());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "User", "Group", "Role" }, story = "Get profile for user from role and group.", jira = "")
    @Test
    public void getProfileForUserFromRoleAndGroup() throws BonitaException {
        getIdentityAPI().addUserMembership(user5.getId(), group3.getId(), role3.getId());
        getIdentityAPI().addUserMembership(user2.getId(), group2.getId(), role3.getId());
        getIdentityAPI().addUserMembership(user3.getId(), group3.getId(), role2.getId());

        // Get Profile For User
        List<Profile> userProfiles = getProfileAPI().getProfilesForUser(user5.getId());
        assertEquals(1, userProfiles.size());
        assertEquals("Process owner", userProfiles.get(0).getName());

        // For profile "Process owner", good group, but bad role:
        userProfiles = getProfileAPI().getProfilesForUser(user2.getId());
        assertEquals(0, userProfiles.size());

        // For profile "Process owner", good role, but bad group:
        userProfiles = getProfileAPI().getProfilesForUser(user3.getId());
        assertEquals(1, userProfiles.size());
        assertEquals("Administrator", userProfiles.get(0).getName());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "Wrong parameter" }, story = "Execute profile member command with wrong parameter", jira = "ENGINE-586")
    @Test
    public void getProfilesForUserWithWrongParameter() throws Exception {
        final List<Profile> profilesForUser = getProfileAPI().getProfilesForUser(564162L);
        assertEquals(0, profilesForUser.size());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "User", "Search" }, story = "Search user profile members for proile.", jira = "")
    @Test
    public void searchUserProfileMembersForProfile() throws BonitaException {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART1, Order.ASC);
        builder.filter(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART1, "User1FirstName");
        builder.filter(ProfileMemberSearchDescriptor.PROFILE_ID, adminProfileId);
        builder.searchTerm("userName1");
        final SearchResult<ProfileMember> searchedProfileMember = getProfileAPI().searchProfileMembers("user", builder.done());
        assertEquals(1, searchedProfileMember.getResult().size());
        assertEquals("User1FirstName", searchedProfileMember.getResult().get(0).getDisplayNamePart1());
        assertEquals("User1LastName", searchedProfileMember.getResult().get(0).getDisplayNamePart2());
        assertEquals("userName1", searchedProfileMember.getResult().get(0).getDisplayNamePart3());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "Group", "Search" }, story = "Search group profile members for profile.", jira = "")
    @Test
    public void searchGroupProfileMembersForProfile() throws BonitaException {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART1, Order.ASC);
        builder.filter(ProfileMemberSearchDescriptor.PROFILE_ID, userProfileId);
        final SearchResult<ProfileMember> searchedProfileMember = getProfileAPI().searchProfileMembers("group", builder.done());

        assertEquals(1, searchedProfileMember.getResult().size());
        final ProfileMember profileMember = searchedProfileMember.getResult().get(0);
        assertEquals("group1", profileMember.getDisplayNamePart1());
        final String displayNamePart3 = profileMember.getDisplayNamePart3();
        // Can be null with Oracle
        assertTrue(displayNamePart3 == null || displayNamePart3.isEmpty());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "Role", "Search" }, story = "Search role profile members for profile.", jira = "")
    @Test
    public void searchRoleProfileMembersForProfile() throws BonitaException {
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART1, Order.ASC);
        builder.filter(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART1, "role2");
        builder.filter(ProfileMemberSearchDescriptor.PROFILE_ID, adminProfileId);
        builder.searchTerm("role2");
        SearchResult<ProfileMember> searchedProfileMember = getProfileAPI().searchProfileMembers("role", builder.done());
        assertEquals(1, searchedProfileMember.getCount());
        ProfileMember profileMember = searchedProfileMember.getResult().get(0);
        assertEquals("role2", profileMember.getDisplayNamePart1());

        builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART1, Order.ASC);
        builder.filter(ProfileMemberSearchDescriptor.PROFILE_ID, adminProfileId);
        searchedProfileMember = getProfileAPI().searchProfileMembers("role", builder.done());
        profileMember = searchedProfileMember.getResult().get(0);
        assertEquals(2, searchedProfileMember.getCount());
        assertEquals("role1", profileMember.getDisplayNamePart1());
        final String displayNamePart2 = profileMember.getDisplayNamePart2();
        // Can be null with Oracle
        assertTrue(displayNamePart2 == null || displayNamePart2.isEmpty());
        final String displayNamePart3 = profileMember.getDisplayNamePart3();
        assertTrue(displayNamePart3 == null || displayNamePart3.isEmpty());
        assertEquals("role2", searchedProfileMember.getResult().get(1).getDisplayNamePart1());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "Role", "Group", "Search" }, story = "Search role and group of profile members for profile.", jira = "")
    @Test
    public void searchRoleAndGroupProfileMembersForProfile() throws BonitaException {
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART1, Order.ASC);
        builder.sort(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART2, Order.ASC);
        builder.filter(ProfileMemberSearchDescriptor.PROFILE_ID, adminProfileId);
        SearchResult<ProfileMember> searchedProfileMember = getProfileAPI().searchProfileMembers("roleAndGroup", builder.done());
        assertEquals(2, searchedProfileMember.getCount());
        assertEquals("role2", searchedProfileMember.getResult().get(0).getDisplayNamePart1());
        assertEquals("group1", searchedProfileMember.getResult().get(0).getDisplayNamePart2());
        assertEquals("role2", searchedProfileMember.getResult().get(1).getDisplayNamePart1());
        assertEquals("group2", searchedProfileMember.getResult().get(1).getDisplayNamePart2());

        builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART1, Order.ASC);
        builder.filter(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART2, "group2");
        builder.filter(ProfileMemberSearchDescriptor.PROFILE_ID, adminProfileId);
        searchedProfileMember = getProfileAPI().searchProfileMembers("roleAndGroup", builder.done());
        assertEquals("role2", searchedProfileMember.getResult().get(0).getDisplayNamePart1());
        assertEquals("group2", searchedProfileMember.getResult().get(0).getDisplayNamePart2());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member" }, story = "Get number of profile members.", jira = "")
    @Test
    public void getNumberOfProfileMembers() {
        final List<Long> profileIds = new ArrayList<Long>();
        profileIds.add(adminProfileId);
        profileIds.add(userProfileId);
        final Map<Long, Long> numberOfProfileMembers = getProfileAPI().getNumberOfProfileMembers(profileIds);
        assertNotNull(numberOfProfileMembers);
        assertEquals(2, numberOfProfileMembers.size());
        assertEquals(Long.valueOf(5L), numberOfProfileMembers.get(adminProfileId));
        assertEquals(Long.valueOf(1L), numberOfProfileMembers.get(userProfileId));
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "Wrong parameter" }, story = "Execute profile member command with wrong parameter", jira = "ENGINE-586")
    @Test(expected = SearchException.class)
    public void searchProfileMembersForProfileWithWrongParameter() throws Exception {
        getProfileAPI().searchProfileMembers("plop", null);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "User", "Search" }, story = "Search user profile members for profile.", jira = "ENGINE-1989")
    @Test
    public void searchUserProfileMembersOfUser() throws BonitaException {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(ProfileMemberSearchDescriptor.USER_ID, user1.getId());
        final SearchResult<ProfileMember> searchedProfileMember = getProfileAPI().searchProfileMembers("user", builder.done());
        assertEquals(1, searchedProfileMember.getResult().size());
        assertEquals("User1FirstName", searchedProfileMember.getResult().get(0).getDisplayNamePart1());
        assertEquals("User1LastName", searchedProfileMember.getResult().get(0).getDisplayNamePart2());
        assertEquals("userName1", searchedProfileMember.getResult().get(0).getDisplayNamePart3());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "Group", "Search" }, story = "Search user profile members for profile.", jira = "ENGINE-2023")
    @Test
    public void searchUserProfileMembersOfGroup() throws BonitaException {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(ProfileMemberSearchDescriptor.GROUP_ID, group1.getId());
        final SearchResult<ProfileMember> searchedProfileMember = getProfileAPI().searchProfileMembers("group", builder.done());
        assertEquals(1, searchedProfileMember.getResult().size());
        assertEquals("group1", searchedProfileMember.getResult().get(0).getDisplayNamePart1());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "Role", "Search" }, story = "Search user profile members for profile.", jira = "ENGINE-2023")
    @Test
    public void searchUserProfileMembersOfRole() throws BonitaException {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(ProfileMemberSearchDescriptor.ROLE_ID, role1.getId());
        final SearchResult<ProfileMember> searchedProfileMember = getProfileAPI().searchProfileMembers("role", builder.done());
        assertEquals(1, searchedProfileMember.getResult().size());
        assertEquals("role1", searchedProfileMember.getResult().get(0).getDisplayNamePart1());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "Role", "Group", "Search" }, story = "Search user profile members for profile.", jira = "ENGINE-2023")
    @Test
    public void searchUserProfileMembersOfRoleAndGroup() throws BonitaException {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(ProfileMemberSearchDescriptor.ROLE_ID, role3.getId());
        builder.filter(ProfileMemberSearchDescriptor.GROUP_ID, group3.getId());
        final SearchResult<ProfileMember> searchedProfileMember = getProfileAPI().searchProfileMembers("roleAndGroup", builder.done());
        assertEquals(1, searchedProfileMember.getResult().size());
        assertEquals("role3", searchedProfileMember.getResult().get(0).getDisplayNamePart1());
        assertEquals("group3", searchedProfileMember.getResult().get(0).getDisplayNamePart2());
    }

}
