package org.bonitasoft.engine.external;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.CommonAPISPTest;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.command.CommandExecutionException;
import org.bonitasoft.engine.command.CommandNotFoundException;
import org.bonitasoft.engine.command.CommandParameterizationException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.InvalidSessionException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserBuilder;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.PlatformSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.api.PlatformAPI;
import com.bonitasoft.engine.api.PlatformAPIAccessor;

public class SPProfileMemberCommandTest extends CommonAPISPTest {

    private static final String SEARCH_PROFILE_MEMBERS_FOR_PROFILE = "searchProfileMembersForProfile";

    private static final String USER_ID = "userId";

    private static final String PROFILE_ID = "profileId";

    private static final String ADD_PROFILE_MEMEBER = "addProfileMember";

    private static final String PROFILE_MEMBER_ID = "profileMemberId";

    private static final String DELETE_PROFILE_MEMBER = "deleteProfileMember";

    @Before
    public void before() throws Exception {
        login();
    }

    @After
    public void after() throws BonitaException, BonitaHomeNotSetException {
        logout();
    }

    @Test
    public void testMultitenancyOnSearchUserProfileMembers() throws BonitaException, IOException {
        logout();

        final PlatformLoginAPI platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
        PlatformSession platformSession = platformLoginAPI.login("platformAdmin", "platform");
        PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(platformSession);
        final Long tenant2Id = platformAPI.createTenant("tenant2", "", "testIconName2", "testIconPath2", "default_tenant2", "default_password2");
        platformAPI.activateTenant(tenant2Id);

        platformLoginAPI.logout(platformSession);

        loginWith("default_tenant2", "default_password2", tenant2Id);

        final User user_tenant2 = createUserByUsernameAndPassword("userName_tenant2", "UserFirstName_tenant2", "UserLastName_tenant2", "UserPwd_tenant2");
        final Map<String, Serializable> addParameters = new HashMap<String, Serializable>();
        addParameters.put(PROFILE_ID, Long.valueOf(1));
        addParameters.put(USER_ID, user_tenant2.getId());
        final Map<String, Serializable> addProfileMemberResult = (Map<String, Serializable>) getCommandAPI().execute(ADD_PROFILE_MEMEBER, addParameters);

        logout();
        login();

        // create User1
        final User user1 = createUserByUsernameAndPassword("userName1", "User1FirstName", "User1LastName", "User1Pwd");

        // Add UserProfile1
        final Map<String, Serializable> addParameters1 = new HashMap<String, Serializable>();
        addParameters1.put(PROFILE_ID, Long.valueOf(1));
        addParameters1.put(USER_ID, user1.getId());
        final Map<String, Serializable> addProfileMemberResult1 = (Map<String, Serializable>) getCommandAPI().execute(ADD_PROFILE_MEMEBER, addParameters1);

        final Map<String, Serializable> searchParameters = new HashMap<String, Serializable>();
        searchParameters.put("profileId", Long.valueOf(1));
        searchParameters.put("memberType", "user");

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        // builder.sort(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART1, Order.ASC);
        searchParameters.put("searchOptions", builder.done());

        SearchResult<HashMap<String, Serializable>> searchedProfileMember = (SearchResult<HashMap<String, Serializable>>) getCommandAPI().execute(
                SEARCH_PROFILE_MEMBERS_FOR_PROFILE, searchParameters);

        assertEquals(1, searchedProfileMember.getResult().size());
        assertEquals("User1FirstName", searchedProfileMember.getResult().get(0).get("displayNamePart1"));
        assertEquals("User1LastName", searchedProfileMember.getResult().get(0).get("displayNamePart2"));
        assertEquals("userName1", searchedProfileMember.getResult().get(0).get("displayNamePart3"));

        // delete UserProfile1
        deleteProfileMember(addProfileMemberResult1);

        searchedProfileMember = (SearchResult<HashMap<String, Serializable>>) getCommandAPI().execute(SEARCH_PROFILE_MEMBERS_FOR_PROFILE, searchParameters);

        assertEquals(0, searchedProfileMember.getResult().size());
        getIdentityAPI().deleteUser(user1.getId());

        logout();
        loginWith("default_tenant2", "default_password2", tenant2Id);
        getIdentityAPI().deleteUser(user_tenant2.getId());

        platformSession = platformLoginAPI.login("platformAdmin", "platform");
        platformAPI = PlatformAPIAccessor.getPlatformAPI(platformSession);
        platformAPI.deactiveTenant(tenant2Id);
        platformAPI.deleteTenant(tenant2Id);

        login();
    }

    private User createUserByUsernameAndPassword(final String userName, final String firstName, final String lastName, final String password)
            throws BonitaException, BonitaHomeNotSetException {
        final UserBuilder userBuilder = new UserBuilder().createNewInstance(userName, password);
        userBuilder.setFirstName(firstName).setLastName(lastName);
        return getIdentityAPI().createUser(userBuilder.done(), null, null);
    }

    private void deleteProfileMember(final Map<String, Serializable> addProfileMemberResult) throws InvalidSessionException, CommandNotFoundException,
            CommandParameterizationException, CommandExecutionException {
        final Map<String, Serializable> deleteParameters = new HashMap<String, Serializable>(1);
        deleteParameters.put(PROFILE_MEMBER_ID, addProfileMemberResult.get(PROFILE_MEMBER_ID));
        getCommandAPI().execute(DELETE_PROFILE_MEMBER, deleteParameters);
    }

}
