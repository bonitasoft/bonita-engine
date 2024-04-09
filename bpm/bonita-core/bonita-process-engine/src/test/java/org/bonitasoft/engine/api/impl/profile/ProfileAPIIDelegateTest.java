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
package org.bonitasoft.engine.api.impl.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.List;

import org.bonitasoft.engine.api.impl.AvailableWhenTenantIsPaused;
import org.bonitasoft.engine.api.impl.ProfileAPIImpl;
import org.bonitasoft.engine.api.impl.transaction.profile.CreateProfileMember;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.MemberType;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.profile.ProfileCriterion;
import org.bonitasoft.engine.profile.ProfileMember;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.model.SProfileMember;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProfileAPIIDelegateTest {

    private static final long USER_ID = 1L;

    private static final long GROUP_ID = 1L;

    private static final long ROLE_ID = 1L;

    @Mock
    ProfileService profileService;

    @Mock
    private IdentityService identityService;

    @Mock
    SProfileMember sProfileMember;

    @Mock
    private SUser sUser;

    @Mock
    private ProfileMember profileMember;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private SearchEntitiesDescriptor searchEntitiesDescriptor;

    private ProfileAPIDelegate profileApiDelegate;

    @Before
    public void before() throws Exception {
        profileApiDelegate = spy(
                new ProfileAPIDelegate(profileService, identityService, searchEntitiesDescriptor, applicationService));
        doReturn(sUser).when(identityService).getUser(anyLong());
        doReturn(profileMember).when(profileApiDelegate).convertToProfileMember(any(CreateProfileMember.class));
        doReturn(sProfileMember).when(profileService).getProfileMemberWithoutDisplayName(anyLong());
    }

    @Test
    public void getProfilesForUser_should_return_empty_list_for_system_user() throws SBonitaReadException {
        // when:
        final List<Profile> profilesForUser = profileApiDelegate.getProfilesForUser(-1, 0, 10, ProfileCriterion.ID_ASC);

        // then:
        assertThat(profilesForUser).isEmpty();
        verify(profileService, never()).searchProfilesOfUser(anyLong(), anyInt(), anyInt(), anyString(),
                any(OrderByType.class));
    }

    @Test
    public void should_deleteProfileMember_update_profile_metadata() throws Exception {
        // when
        profileApiDelegate.deleteProfileMember(1L);

        // then
        verify(profileService, times(1)).updateProfileMetaData(anyLong());
    }

    @Test
    public void should_updateProfileMetaData_update_profilemetadata() throws Exception {
        final Long profileId = 1L;
        final Long userId = 2L;
        final Long groupId = 3L;
        final Long roleId = 4L;

        doNothing().when(profileApiDelegate).checkIfProfileMemberExists(
                any(Long.class), any(Long.class), any(Long.class), any(Long.class),
                any(MemberType.class));

        // when
        profileApiDelegate.createProfileMember(profileId, userId, groupId, roleId);

        // then
        verify(profileService, times(1)).updateProfileMetaData(anyLong());

    }

    @Test
    public void memberType_User() throws Exception {
        assertThat(profileApiDelegate.getMemberType(USER_ID, null, null)).isEqualTo(MemberType.USER);
        assertThat(profileApiDelegate.getMemberType(USER_ID, -1L, null)).isEqualTo(MemberType.USER);
        assertThat(profileApiDelegate.getMemberType(USER_ID, -1L, -1L)).isEqualTo(MemberType.USER);

    }

    @Test
    public void memberType_Group() throws Exception {
        assertThat(profileApiDelegate.getMemberType(null, GROUP_ID, null)).isEqualTo(MemberType.GROUP);
        assertThat(profileApiDelegate.getMemberType(-1L, GROUP_ID, null)).isEqualTo(MemberType.GROUP);
        assertThat(profileApiDelegate.getMemberType(null, GROUP_ID, -1L)).isEqualTo(MemberType.GROUP);
    }

    @Test
    public void memberType_Role() throws Exception {
        assertThat(profileApiDelegate.getMemberType(null, null, ROLE_ID)).isEqualTo(MemberType.ROLE);
        assertThat(profileApiDelegate.getMemberType(-1L, null, ROLE_ID)).isEqualTo(MemberType.ROLE);
        assertThat(profileApiDelegate.getMemberType(null, -1L, ROLE_ID)).isEqualTo(MemberType.ROLE);

    }

    @Test
    public void memberType_MemberShip() throws Exception {
        assertThat(profileApiDelegate.getMemberType(null, GROUP_ID, ROLE_ID)).isEqualTo(MemberType.MEMBERSHIP);
        assertThat(profileApiDelegate.getMemberType(-1L, GROUP_ID, ROLE_ID)).isEqualTo(MemberType.MEMBERSHIP);

    }

    @Test
    public void should_profile_api_be_available_when_tenant_is_paused() throws Exception {
        assertThat(ProfileAPIImpl.class.isAnnotationPresent(AvailableWhenTenantIsPaused.class))
                .as("should profile api be available when tenant is paused")
                .isTrue();
    }
}
