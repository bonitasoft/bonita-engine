package com.bonitasoft.engine.profile;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.bpm.model.ActorMember;
import org.bonitasoft.engine.command.GroupProfileMemberAlreadyExistsException;
import org.bonitasoft.engine.command.RoleProfileMemberAlreadyExistsException;
import org.bonitasoft.engine.command.UserMembershipProfileMemberAlreadyExistsException;
import org.bonitasoft.engine.command.UserProfileMemberAlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.identity.UserAlreadyExistException;
import org.bonitasoft.engine.exception.identity.UserCreationException;
import org.bonitasoft.engine.exception.platform.InvalidSessionException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserBuilder;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.descriptor.ProfileMemberSearchDescriptor;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Ignore;
import org.junit.Test;

import com.bonitasoft.engine.api.PlatformAPI;
import com.bonitasoft.engine.api.PlatformAPIAccessor;
import com.bonitasoft.engine.api.ProfileAPI;
import com.bonitasoft.engine.exception.profile.ProfileMemberCreationException;
import com.bonitasoft.engine.exception.profile.ProfileMemberDeletionException;

import static org.junit.Assert.assertEquals;

public class ProfileMemberTest extends AbstractProfileTest {

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "Create", "Delete", "Group" }, story = "Create and delete group to profile.")
    @Test
    public void createAndDeleteGroupToProfile() throws BonitaException, IOException {
        // create User1
        final User user1 = createUserByUsernameAndPassword("userName1", "User1FirstName", "User1LastName", "User1Pwd");

        final Group group = createGroup("group1");
        final Role role = createRole("role1");

        getIdentityAPI().addUserMembership(user1.getId(), group.getId(), role.getId());

        // Create group Profile1
        checkCreateAndDeleProfileMember("group", null, group.getId(), null);

        getIdentityAPI().deleteUser(user1.getId());
        getIdentityAPI().deleteGroup(group.getId());
        getIdentityAPI().deleteRole(role.getId());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "Create", "Delete", "Role", "Group" }, story = "Create and delete role and group to profile.")
    @Test
    public void createAndDeleteRoleAndGroupToProfile() throws BonitaException, IOException {
        // create User1
        final User user1 = createUserByUsernameAndPassword("userName1", "User1FirstName", "User1LastName", "User1Pwd");
        final User user2 = createUserByUsernameAndPassword("userName2", "User2FirstName", "User2LastName", "User1Pwd");
        final User user3 = createUserByUsernameAndPassword("userName3", "User3FirstName", "User3LastName", "User1Pwd");

        final Group group1 = createGroup("group1");
        final Group group2 = createGroup("group2");
        final Role role1 = createRole("role1");
        final Role role2 = createRole("role2");

        getIdentityAPI().addUserMembership(user1.getId(), group1.getId(), role1.getId());
        getIdentityAPI().addUserMembership(user2.getId(), group1.getId(), role2.getId());
        getIdentityAPI().addUserMembership(user3.getId(), group2.getId(), role1.getId());

        // Create group Profile1
        checkCreateAndDeleProfileMember("roleAndGroup", null, group1.getId(), role1.getId());

        getIdentityAPI().deleteUser(user1.getId());
        getIdentityAPI().deleteUser(user2.getId());
        getIdentityAPI().deleteUser(user3.getId());
        getIdentityAPI().deleteGroup(group1.getId());
        getIdentityAPI().deleteGroup(group2.getId());
        getIdentityAPI().deleteRole(role1.getId());
        getIdentityAPI().deleteRole(role2.getId());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "Create", "Delete", "Role" }, story = "Create and delete role to profile.")
    @Test
    public void createAndDeleteRoleToProfile() throws BonitaException, IOException {
        // create User1
        final User user1 = createUserByUsernameAndPassword("userName1", "User1FirstName", "User1LastName", "User1Pwd");

        final Group group = createGroup("group1");
        final Role role = createRole("role1");

        getIdentityAPI().addUserMembership(user1.getId(), group.getId(), role.getId());

        // Create group Profile1
        checkCreateAndDeleProfileMember("role", null, null, role.getId());

        getIdentityAPI().deleteUser(user1.getId());
        getIdentityAPI().deleteGroup(group.getId());
        getIdentityAPI().deleteRole(role.getId());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "Create", "Delete" }, story = "Create and delete user profile.")
    @Test
    public void createandDeleteUserProfile() throws BonitaException, IOException {
        // create User1
        final User user1 = createUserByUsernameAndPassword("userName1", "User1FirstName", "User1LastName", "User1Pwd");

        checkCreateAndDeleProfileMember("user", user1.getId(), null, null);

        getIdentityAPI().deleteUser(user1.getId());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "Wrong parameter" }, story = "Execute profile member command with wrong parameter", jira = "ENGINE-586")
    @Test(expected = ProfileMemberCreationException.class)
    public void createProfileMemberWithWrongParameter() throws Exception {
        getProfileAPI().createProfileMember(856L, null, null, null);
    }

    @Cover(classes = { ProfileAPI.class, ActorMember.class, Group.class, Role.class, User.class }, concept = BPMNConcept.ORGANIZATION, jira = "ENGINE-808", keywords = { "delete organization actor mapping" })
    @Test
    public void deleteOrganizationRemoveActorProfileMember() throws BonitaException {
        // create user and add profile
        final User user = getIdentityAPI().createUser("mixmaster.spike", "123456789");
        getProfileAPI().createProfileMember(adminProfileId, user.getId(), null, null);

        // delete organization
        getIdentityAPI().deleteOrganization();

        // check there is no user anymore in this profile
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART1, Order.ASC);
        final SearchResult<HashMap<String, Serializable>> searchedProfileMember = getProfileAPI().searchProfileMembersForProfile(adminProfileId, "user",
                builder.done());
        assertEquals(0, searchedProfileMember.getResult().size());
    }

    private void checkCreateAndDeleProfileMember(final String memberType, final Long userId, final Long groupId, Long roleId)
            throws BonitaException {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART1, Order.ASC);
        SearchResult<HashMap<String, Serializable>> searchedProfileMember = getProfileAPI().searchProfileMembersForProfile(adminProfileId, memberType,
                builder.done());
        assertEquals(0, searchedProfileMember.getResult().size());

        final Map<String, Serializable> addProfileMemberResult = getProfileAPI().createProfileMember(adminProfileId, userId, groupId, roleId);

        searchedProfileMember = getProfileAPI().searchProfileMembersForProfile(adminProfileId, memberType, builder.done());
        assertEquals(1, searchedProfileMember.getResult().size());

        // delete UserProfile1
        deleteProfileMember(addProfileMemberResult);
        searchedProfileMember = getProfileAPI().searchProfileMembersForProfile(adminProfileId, memberType, builder.done());
        assertEquals(0, searchedProfileMember.getResult().size());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "Wrong parameter" }, story = "Execute profile member command with wrong parameter", jira = "ENGINE-586")
    @Test(expected = ProfileMemberDeletionException.class)
    public void deleteProfileMemberWithWrongParameter() throws Exception {
        getProfileAPI().deleteProfileMember(856L);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "User", "Twice", "Same" }, jira = "ENGINE-919")
    @Test(expected = UserProfileMemberAlreadyExistsException.class)
    public void createTwiceSameProfileMemberWithUser() throws BonitaException, IOException {
        // Create User
        final User user = createUserByUsernameAndPassword("userName1", "User1FirstName", "User1LastName", "User1Pwd");

        // Create UserProfile
        final Map<String, Serializable> addProfileMemberResult = getProfileAPI().createProfileMember(adminProfileId,
                user.getId(), null, null);
        try {
            getProfileAPI().createProfileMember(adminProfileId, user.getId(), null, null);
        } finally {
            // delete UserProfile1
            deleteProfileMember(addProfileMemberResult);
            getIdentityAPI().deleteUser(user.getId());
        }
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "Role", "Twice", "Same" }, jira = "ENGINE-919")
    @Test(expected = RoleProfileMemberAlreadyExistsException.class)
    public void createTwiceSameProfileMemberWithRole() throws BonitaException, IOException {
        // Create role
        final Role role = createRole("role1");

        // Create UserProfile
        final Map<String, Serializable> addProfileMemberResult = getProfileAPI().createProfileMember(adminProfileId,
                null, null, role.getId());
        try {
            getProfileAPI().createProfileMember(adminProfileId, null, null, role.getId());
        } finally {
            // delete UserProfile1
            deleteProfileMember(addProfileMemberResult);
            getIdentityAPI().deleteRole(role.getId());
        }
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "Group", "Twice", "Same" }, jira = "ENGINE-919")
    @Test(expected = GroupProfileMemberAlreadyExistsException.class)
    public void createTwiceSameProfileMemberWithGroup() throws BonitaException, IOException {
        // Create group
        final Group group = createGroup("group1");

        // Create UserProfile
        final Map<String, Serializable> addProfileMemberResult = getProfileAPI().createProfileMember(adminProfileId, null, group.getId(), null);
        try {
            getProfileAPI().createProfileMember(adminProfileId, null, group.getId(), null);
        } finally {
            // delete UserProfile1
            deleteProfileMember(addProfileMemberResult);
            getIdentityAPI().deleteGroup(group.getId());
        }
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile member", "UserMembership", "Twice", "Same" }, jira = "ENGINE-919")
    @Test(expected = UserMembershipProfileMemberAlreadyExistsException.class)
    public void createTwiceSameProfileMemberWithMembership() throws BonitaException, IOException {
        // Create User, group, role
        final User user = createUserByUsernameAndPassword("userName1", "User1FirstName", "User1LastName", "User1Pwd");
        final Group group = createGroup("group1");
        final Role role = createRole("role1");

        getIdentityAPI().addUserMembership(user.getId(), group.getId(), role.getId());

        // Create UserProfile
        final Map<String, Serializable> addProfileMemberResult = getProfileAPI().createProfileMember(adminProfileId,
                null, group.getId(), role.getId());
        try {
            getProfileAPI().createProfileMember(adminProfileId, null, group.getId(), role.getId());
        } finally {
            // delete UserProfile1
            deleteProfileMember(addProfileMemberResult);
            getIdentityAPI().deleteUser(user.getId());
            getIdentityAPI().deleteGroup(group.getId());
            getIdentityAPI().deleteRole(role.getId());
        }
    }

    @Ignore("Problem with assumption that default values pre-exist")
    @Test
    public void multitenancyOnSearchUserProfileMembers() throws BonitaException, IOException {
        logout();

        final PlatformLoginAPI platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
        PlatformSession platformSession = platformLoginAPI.login("platformAdmin", "platform");
        PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(platformSession);
        final Long tenant2Id = platformAPI.createTenant("tenant2", "", "IconName2", "IconPath2", "default_tenant2", "default_password2");
        platformAPI.activateTenant(tenant2Id);

        platformLoginAPI.logout(platformSession);

        loginWith("default_tenant2", "default_password2", tenant2Id);

        final User userTenant2 = createUserByUsernameAndPassword("userName_tenant2", "UserFirstName_tenant2", "UserLastName_tenant2", "UserPwd_tenant2");
        getProfileAPI().createProfileMember(Long.valueOf(1), userTenant2.getId(), null, null);
        logout();
        login();

        // create User1
        final User user1 = createUserByUsernameAndPassword("userName1", "User1FirstName", "User1LastName", "User1Pwd");

        // Create UserProfile1
        final Map<String, Serializable> addProfileMemberResult1 = getProfileAPI().createProfileMember(Long.valueOf(1), user1.getId(), null, null);

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        // builder.sort(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART1, Order.ASC);
        SearchResult<HashMap<String, Serializable>> searchedProfileMember = getProfileAPI().searchProfileMembersForProfile(Long.valueOf(1), "user",
                builder.done());
        assertEquals(1, searchedProfileMember.getResult().size());
        assertEquals("User1FirstName", searchedProfileMember.getResult().get(0).get("displayNamePart1"));
        assertEquals("User1LastName", searchedProfileMember.getResult().get(0).get("displayNamePart2"));
        assertEquals("userName1", searchedProfileMember.getResult().get(0).get("displayNamePart3"));

        // delete UserProfile1
        deleteProfileMember(addProfileMemberResult1);

        searchedProfileMember = getProfileAPI().searchProfileMembersForProfile(Long.valueOf(1), "user", builder.done());
        assertEquals(0, searchedProfileMember.getResult().size());
        getIdentityAPI().deleteUser(user1.getId());

        logout();
        loginWith("default_tenant2", "default_password2", tenant2Id);
        getIdentityAPI().deleteUser(userTenant2.getId());

        platformSession = platformLoginAPI.login("platformAdmin", "platform");
        platformAPI = PlatformAPIAccessor.getPlatformAPI(platformSession);
        platformAPI.deactiveTenant(tenant2Id);
        platformAPI.deleteTenant(tenant2Id);

        login();
    }

    private User createUserByUsernameAndPassword(final String userName, final String firstName, final String lastName, final String password)
            throws UserAlreadyExistException, UserCreationException, InvalidSessionException {
        final UserBuilder userBuilder = new UserBuilder().createNewInstance(userName, password);
        userBuilder.setFirstName(firstName).setLastName(lastName);
        return getIdentityAPI().createUser(userBuilder.done(), null, null);
    }

    private void deleteProfileMember(final Map<String, Serializable> addProfileMemberResult) throws InvalidSessionException, ProfileMemberDeletionException {
        getProfileAPI().deleteProfileMember((Long) addProfileMemberResult.get(PROFILE_MEMBER_ID));
    }
}
