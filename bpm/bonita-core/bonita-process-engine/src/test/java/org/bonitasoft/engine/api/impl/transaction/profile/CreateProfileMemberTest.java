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
package org.bonitasoft.engine.api.impl.transaction.profile;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.MemberType;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.builder.SProfileUpdateBuilder;
import org.bonitasoft.engine.profile.model.SProfileMember;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CreateProfileMemberTest {

    private static final long USER_ID = 1L;

    private static final long ROLE_ID = 2L;

    private static final long GROUP_ID = 3L;

    private static final long PROFILE_ID = 1l;

    @Mock
    private ProfileService profileService;

    @Mock
    private IdentityService identityService;

    @Mock
    private SProfileMember addGroupToProfile;

    @Mock
    SProfileUpdateBuilder sProfileUpdateBuilder;

    @Mock
    private SGroup sGroup;

    @Mock
    private SUser sUser;

    @Mock
    private SRole sRole;

    @Before
    public void before() throws Exception {
        doReturn(sGroup).when(identityService).getGroup(anyLong());
        doReturn(sUser).when(identityService).getUser(anyLong());
        doReturn(sRole).when(identityService).getRole(anyLong());

        doReturn(null).when(profileService).addUserToProfile(anyLong(), anyLong(), anyString(), anyString(), anyString());
        doReturn(null).when(profileService).addGroupToProfile(anyLong(), anyLong(), anyString(), anyString());
        doReturn(null).when(profileService).addRoleToProfile(anyLong(), anyLong(), anyString());
        doReturn(null).when(profileService).addRoleAndGroupToProfile(anyLong(), anyLong(), anyLong(), anyString(), anyString(), anyString());

        doReturn("group").when(sGroup).getName();
        doReturn("/parent").when(sGroup).getParentPath();

        doReturn("role").when(sRole).getName();
    }

    @Test
    public void should_updateProfileMetaData_and_addUserToProfile_when_userId_is_not_empty_and_member_is_user() throws Exception {
        // Given
        final CreateProfileMember createProfileMember = spy(new CreateProfileMember(profileService, identityService, PROFILE_ID, USER_ID, null, null,
                MemberType.USER));

        // When
        createProfileMember.execute();

        // Then
        verify(profileService).addUserToProfile(eq(PROFILE_ID), eq(USER_ID), anyString(), anyString(), anyString());
        verify(profileService, times(1)).updateProfileMetaData(anyLong());
    }

    @Test
    public void should_just_updateProfileMetaData_when_no_user_and_member_is_user() throws Exception {
        // Given
        final CreateProfileMember createProfileMember = spy(new CreateProfileMember(profileService, identityService, PROFILE_ID, USER_ID, null, null,
                MemberType.USER));
        doReturn(null).when(identityService).getUser(USER_ID);

        // When
        createProfileMember.execute();

        // Then
        verify(profileService, never()).addUserToProfile(eq(PROFILE_ID), eq(USER_ID), anyString(), anyString(), anyString());
        verify(profileService, times(1)).updateProfileMetaData(anyLong());
    }

    @Test
    public void should_just_updateProfileMetaData_when_userId_is_null_and_member_is_user() throws Exception {
        // Given
        final CreateProfileMember createProfileMember = spy(new CreateProfileMember(profileService, identityService, PROFILE_ID, null, null, null,
                MemberType.USER));

        // When
        createProfileMember.execute();

        // Then
        verify(profileService, never()).addUserToProfile(eq(PROFILE_ID), eq(USER_ID), anyString(), anyString(), anyString());
        verify(profileService, times(1)).updateProfileMetaData(anyLong());
    }

    @Test
    public void should_just_updateProfileMetaData_when_userId_equals_0_and_member_is_user() throws Exception {
        // Given
        final CreateProfileMember createProfileMember = spy(new CreateProfileMember(profileService, identityService, PROFILE_ID, 0L, null, null,
                MemberType.USER));

        // When
        createProfileMember.execute();

        // Then
        verify(profileService, never()).addUserToProfile(eq(PROFILE_ID), eq(USER_ID), anyString(), anyString(), anyString());
        verify(profileService, times(1)).updateProfileMetaData(anyLong());
    }

    @Test
    public void should_addGroupToProfile_when_member_is_group() throws Exception {
        // Given
        final CreateProfileMember createProfileMember = spy(new CreateProfileMember(profileService, identityService, PROFILE_ID, null, GROUP_ID, null,
                MemberType.GROUP));

        // When
        createProfileMember.execute();

        // Then
        verify(profileService, times(1)).addGroupToProfile(eq(PROFILE_ID), eq(GROUP_ID), anyString(), anyString());
        verify(profileService, times(1)).updateProfileMetaData(anyLong());
    }

    @Test
    public void should_just_updateProfileMetaData_when_no_group_and_member_is_group() throws Exception {
        // Given
        final CreateProfileMember createProfileMember = spy(new CreateProfileMember(profileService, identityService, PROFILE_ID, null, GROUP_ID, null,
                MemberType.GROUP));
        doReturn(null).when(identityService).getGroup(GROUP_ID);

        // When
        createProfileMember.execute();

        // Then
        verify(profileService, never()).addGroupToProfile(eq(PROFILE_ID), eq(GROUP_ID), anyString(), anyString());
        verify(profileService, times(1)).updateProfileMetaData(anyLong());
    }

    @Test
    public void should_just_updateProfileMetaData_when_groupId_is_null_and_member_is_group() throws Exception {
        // Given
        final CreateProfileMember createProfileMember = spy(new CreateProfileMember(profileService, identityService, PROFILE_ID, null, null, null,
                MemberType.GROUP));

        // When
        createProfileMember.execute();

        // Then
        verify(profileService, never()).addGroupToProfile(eq(PROFILE_ID), eq(GROUP_ID), anyString(), anyString());
        verify(profileService, times(1)).updateProfileMetaData(anyLong());
    }

    @Test
    public void should_just_updateProfileMetaData_when_groupId_equals_0_and_member_is_group() throws Exception {
        // Given
        final CreateProfileMember createProfileMember = spy(new CreateProfileMember(profileService, identityService, PROFILE_ID, null, 0L, null,
                MemberType.GROUP));

        // When
        createProfileMember.execute();

        // Then
        verify(profileService, never()).addGroupToProfile(eq(PROFILE_ID), eq(GROUP_ID), anyString(), anyString());
        verify(profileService, times(1)).updateProfileMetaData(anyLong());
    }

    @Test
    public void should_addRoleAndGroupToProfile_when_member_is_membership() throws Exception {
        // Given
        final CreateProfileMember createProfileMember = spy(new CreateProfileMember(profileService, identityService, PROFILE_ID, null, GROUP_ID, ROLE_ID,
                MemberType.MEMBERSHIP));

        // When
        createProfileMember.execute();

        // Then
        verify(profileService, times(1)).addRoleAndGroupToProfile(eq(PROFILE_ID), eq(ROLE_ID), eq(GROUP_ID), anyString(), anyString(), anyString());
    }

    @Test
    public void should_just_updateProfileMetaData_when_no_group_and_member_is_membership() throws Exception {
        // Given
        final CreateProfileMember createProfileMember = spy(new CreateProfileMember(profileService, identityService, PROFILE_ID, null, GROUP_ID, ROLE_ID,
                MemberType.MEMBERSHIP));
        doReturn(null).when(identityService).getGroup(GROUP_ID);

        // When
        createProfileMember.execute();

        // Then
        verify(profileService, never()).addRoleAndGroupToProfile(eq(PROFILE_ID), eq(ROLE_ID), eq(GROUP_ID), anyString(), anyString(), anyString());
        verify(profileService, times(1)).updateProfileMetaData(anyLong());
    }

    @Test
    public void should_just_updateProfileMetaData_when_no_role_and_member_is_membership() throws Exception {
        // Given
        final CreateProfileMember createProfileMember = spy(new CreateProfileMember(profileService, identityService, PROFILE_ID, null, GROUP_ID, ROLE_ID,
                MemberType.MEMBERSHIP));
        doReturn(null).when(identityService).getRole(ROLE_ID);

        // When
        createProfileMember.execute();

        // Then
        verify(profileService, never()).addRoleAndGroupToProfile(eq(PROFILE_ID), eq(ROLE_ID), eq(GROUP_ID), anyString(), anyString(), anyString());
        verify(profileService, times(1)).updateProfileMetaData(anyLong());
    }

    @Test
    public void should_just_updateProfileMetaData_when_groupId_is_null_and_member_is_membership() throws Exception {
        // Given
        final CreateProfileMember createProfileMember = spy(new CreateProfileMember(profileService, identityService, PROFILE_ID, null, null, ROLE_ID,
                MemberType.MEMBERSHIP));

        // When
        createProfileMember.execute();

        // Then
        verify(profileService, never()).addRoleAndGroupToProfile(eq(PROFILE_ID), eq(ROLE_ID), eq(GROUP_ID), anyString(), anyString(), anyString());
        verify(profileService, times(1)).updateProfileMetaData(anyLong());
    }

    @Test
    public void should_just_updateProfileMetaData_when_groupId_equals_0_and_member_is_membership() throws Exception {
        // Given
        final CreateProfileMember createProfileMember = spy(new CreateProfileMember(profileService, identityService, PROFILE_ID, null, 0L, ROLE_ID,
                MemberType.MEMBERSHIP));

        // When
        createProfileMember.execute();

        // Then
        verify(profileService, never()).addRoleAndGroupToProfile(eq(PROFILE_ID), eq(ROLE_ID), eq(GROUP_ID), anyString(), anyString(), anyString());
        verify(profileService, times(1)).updateProfileMetaData(anyLong());
    }

    @Test
    public void should_just_updateProfileMetaData_when_roleId_is_null_and_member_is_membership() throws Exception {
        // Given
        final CreateProfileMember createProfileMember = spy(new CreateProfileMember(profileService, identityService, PROFILE_ID, null, GROUP_ID, null,
                MemberType.MEMBERSHIP));

        // When
        createProfileMember.execute();

        // Then
        verify(profileService, never()).addRoleAndGroupToProfile(eq(PROFILE_ID), eq(ROLE_ID), eq(GROUP_ID), anyString(), anyString(), anyString());
        verify(profileService, times(1)).updateProfileMetaData(anyLong());
    }

    @Test
    public void should_just_updateProfileMetaData_when_roleId_equals_0_and_member_is_membership() throws Exception {
        // Given
        final CreateProfileMember createProfileMember = spy(new CreateProfileMember(profileService, identityService, PROFILE_ID, null, GROUP_ID, 0L,
                MemberType.MEMBERSHIP));

        // When
        createProfileMember.execute();

        // Then
        verify(profileService, never()).addRoleAndGroupToProfile(eq(PROFILE_ID), eq(ROLE_ID), eq(GROUP_ID), anyString(), anyString(), anyString());
        verify(profileService, times(1)).updateProfileMetaData(anyLong());
    }

    @Test
    public void should_addRoleToProfile_when_member_is_role() throws Exception {
        // Given
        final CreateProfileMember createProfileMember = spy(new CreateProfileMember(profileService, identityService, PROFILE_ID, null, null, ROLE_ID,
                MemberType.ROLE));

        // When
        createProfileMember.execute();

        // Then
        verify(profileService, times(1)).addRoleToProfile(eq(PROFILE_ID), eq(ROLE_ID), anyString());
    }

    @Test
    public void should_just_updateProfileMetaData_when_no_role_and_member_is_role() throws Exception {
        // Given
        final CreateProfileMember createProfileMember = spy(new CreateProfileMember(profileService, identityService, PROFILE_ID, null, null, ROLE_ID,
                MemberType.ROLE));
        doReturn(null).when(identityService).getRole(ROLE_ID);

        // When
        createProfileMember.execute();

        // Then
        verify(profileService, never()).addRoleToProfile(eq(PROFILE_ID), eq(ROLE_ID), anyString());
        verify(profileService, times(1)).updateProfileMetaData(anyLong());
    }

    @Test
    public void should_just_updateProfileMetaData_when_roleId_is_null_and_member_is_role() throws Exception {
        // Given
        final CreateProfileMember createProfileMember = spy(new CreateProfileMember(profileService, identityService, PROFILE_ID, null, null, null,
                MemberType.ROLE));

        // When
        createProfileMember.execute();

        // Then
        verify(profileService, never()).addRoleToProfile(eq(PROFILE_ID), eq(ROLE_ID), anyString());
        verify(profileService, times(1)).updateProfileMetaData(anyLong());
    }

    @Test
    public void should_just_updateProfileMetaData_when_roleId_equals_0_and_member_is_role() throws Exception {
        // Given
        final CreateProfileMember createProfileMember = spy(new CreateProfileMember(profileService, identityService, PROFILE_ID, null, null, 0L,
                MemberType.ROLE));

        // When
        createProfileMember.execute();

        // Then
        verify(profileService, never()).addRoleToProfile(eq(PROFILE_ID), eq(ROLE_ID), anyString());
        verify(profileService, times(1)).updateProfileMetaData(anyLong());
    }

    @Test
    public void getResult() {
        // Given
        final CreateProfileMember createProfileMember = new CreateProfileMember(profileService, identityService, PROFILE_ID, null, null, null, MemberType.ROLE);

        // When
        final SProfileMember result = createProfileMember.getResult();

        // Then
        assertNull(result);
    }
}
