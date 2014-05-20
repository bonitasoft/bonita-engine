/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.profile;

import static org.junit.Assert.assertEquals;

import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.profile.ProfileMember;
import org.bonitasoft.engine.profile.ProfileMemberSearchDescriptor;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.PlatformSession;
import org.junit.Ignore;
import org.junit.Test;

import com.bonitasoft.engine.api.PlatformAPI;
import com.bonitasoft.engine.api.PlatformAPIAccessor;
import com.bonitasoft.engine.platform.TenantCreator;

public class ProfileMemberTest extends AbstractProfileTest {

    @Ignore("Problem with assumption that default values pre-exist")
    @Test
    public void multitenancyOnSearchUserProfileMembers() throws BonitaException {
        logout();

        final PlatformLoginAPI platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
        PlatformSession platformSession = platformLoginAPI.login("platformAdmin", "platform");
        PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(platformSession);
        final Long tenant2Id = platformAPI.createTenant(new TenantCreator("tenant2", "", "IconName2", "IconPath2", "default_tenant2", "default_password2"));
        platformAPI.activateTenant(tenant2Id);

        platformLoginAPI.logout(platformSession);

        loginOnTenantWith("default_tenant2", "default_password2", tenant2Id);

        final User userTenant2 = createUser("userName_tenant2", "UserPwd_tenant2", "UserFirstName_tenant2", "UserLastName_tenant2");
        getProfileAPI().createProfileMember(Long.valueOf(1), userTenant2.getId(), null, null);
        logout();
        login();

        // Create UserProfile1
        final ProfileMember addProfileMemberResult = getProfileAPI().createProfileMember(Long.valueOf(1), user1.getId(), null, null);

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(ProfileMemberSearchDescriptor.PROFILE_ID, Long.valueOf(1));
        // builder.sort(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART1, Order.ASC);
        SearchResult<ProfileMember> searchedProfileMember = getProfileAPI().searchProfileMembers("user", builder.done());
        assertEquals(1, searchedProfileMember.getResult().size());
        assertEquals("User1FirstName", searchedProfileMember.getResult().get(0).getDisplayNamePart1());
        assertEquals("User1LastName", searchedProfileMember.getResult().get(0).getDisplayNamePart2());
        assertEquals("userName1", searchedProfileMember.getResult().get(0).getDisplayNamePart3());

        // delete UserProfile1
        getProfileAPI().deleteProfileMember(addProfileMemberResult.getId());

        searchedProfileMember = getProfileAPI().searchProfileMembers("user", builder.done());
        assertEquals(0, searchedProfileMember.getResult().size());
        getIdentityAPI().deleteUser(user1.getId());

        logout();
        loginOnTenantWith("default_tenant2", "default_password2", tenant2Id);
        getIdentityAPI().deleteUser(userTenant2.getId());

        platformSession = platformLoginAPI.login("platformAdmin", "platform");
        platformAPI = PlatformAPIAccessor.getPlatformAPI(platformSession);
        platformAPI.deactiveTenant(tenant2Id);
        platformAPI.deleteTenant(tenant2Id);

        login();
    }

}
