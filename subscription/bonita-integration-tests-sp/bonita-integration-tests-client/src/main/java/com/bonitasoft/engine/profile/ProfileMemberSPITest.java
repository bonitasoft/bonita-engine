/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.profile;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.profile.Profile;
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

public class ProfileMemberSPITest extends AbstractProfileSPTest {

    @Ignore("Problem with assumption that default values pre-exist")
    @Test
    public void multitenancyOnSearchUserProfileMembers() throws BonitaException {
        logoutOnTenant();

        final PlatformLoginAPI platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
        PlatformSession platformSession = platformLoginAPI.login("platformAdmin", "platform");
        PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(platformSession);
        final Long tenant2Id = platformAPI.createTenant(new TenantCreator("tenant2", "", "IconName2", "IconPath2", "default_tenant2", "default_password2"));
        platformAPI.activateTenant(tenant2Id);

        platformLoginAPI.logout(platformSession);

        loginOnTenantWith("default_tenant2", "default_password2", tenant2Id);

        final User userTenant2 = createUser("userName_tenant2", "UserPwd_tenant2", "UserFirstName_tenant2", "UserLastName_tenant2");
        getProfileAPI().createProfileMember(Long.valueOf(1), userTenant2.getId(), null, null);
        logoutOnTenant();
        loginOnDefaultTenantWithDefaultTechnicalUser();

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

        logoutOnTenant();
        loginOnTenantWith("default_tenant2", "default_password2", tenant2Id);
        getIdentityAPI().deleteUser(userTenant2.getId());

        platformSession = platformLoginAPI.login("platformAdmin", "platform");
        platformAPI = PlatformAPIAccessor.getPlatformAPI(platformSession);
        platformAPI.deactiveTenant(tenant2Id);
        platformAPI.deleteTenant(tenant2Id);

        loginOnDefaultTenantWithDefaultTechnicalUser();
    }

    @Test
    public void createProfileMembersMultiThreaded() throws Throwable {

        //given
        final Profile profile = getProfileAPI().createProfile("Mine", "My custom profile");
        final User john = getIdentityAPI().createUser("john", "bpm");
        final User jack = getIdentityAPI().createUser("jack", "bpm");
        final User james = getIdentityAPI().createUser("james", "bpm");
        final Role member = getIdentityAPI().createRole("member");

        final List<Group> groups = new ArrayList<Group>();
        for (int i = 0; i < 20; i++) {
            groups.add(getIdentityAPI().createGroup("myGroup" + i, null));
        }

        final List<FailableThread> threads = new ArrayList<FailableThread>();

        threads.add(createThreadForProfileMember("t1", profile.getId(), john.getId(), null, null));
        threads.add(createThreadForProfileMember("t2", profile.getId(), jack.getId(), null, null));
        threads.add(createThreadForProfileMember("t3", profile.getId(), james.getId(), null, null));
        threads.add(createThreadForProfileMember("t4", profile.getId(), null, null, member.getId()));
        threads.add(createThreadForProfileMember("t5", profile.getId(), null, groups.get(0).getId(), member.getId()));
        for (final Group group : groups) {
            threads.add(createThreadForProfileMember(group.getName(), profile.getId(), null, group.getId(), null));
        }

        //when
        for (final Thread thread : threads) {
            thread.start();
        }

        //then: threads are ok
        for (final Thread thread : threads) {
            thread.join(1000);
        }
        for (final Thread thread : threads) {
            thread.join(1000);
        }

        for (final FailableThread thread : threads) {
            if (thread.getException() != null) {
                throw thread.getException();
            }
        }

        deleteGroups(groups);
        deleteRoles(member);
        deleteUsers(john, jack, james);
    }

    private FailableThread createThreadForProfileMember(final String threadName, final Long profileId, final Long userId, final Long grouId, final Long roleId) {
        final FailableThread t1 = new FailableThread(threadName, new FailableRunnable() {
            @Override
            public void run() {
                try {
                    getProfileAPI().createProfileMember(profileId, userId, grouId, roleId);
                } catch (final Throwable e) {
                    setException(e);
                }
            }
        });
        return t1;
    }

}
