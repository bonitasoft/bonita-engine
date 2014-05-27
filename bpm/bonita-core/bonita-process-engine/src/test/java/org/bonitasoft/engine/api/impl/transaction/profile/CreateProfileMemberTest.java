package org.bonitasoft.engine.api.impl.transaction.profile;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.MemberType;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.builder.SProfileUpdateBuilder;
import org.bonitasoft.engine.profile.exception.profile.SProfileUpdateException;
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

    private static final long UPDATED_BY_ID = 4l;

    private static final long PROFILE_ID = 1l;

    @Mock
    private ProfileService profileService;

    @Mock
    private IdentityService identityService;

    @Mock
    private CreateProfileMember createProfileMember;

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
    public void should_create_profile_member_user_update_profile_metadata() throws Exception {
        checkProfileMetaDataUpdate(PROFILE_ID, USER_ID, null, null, MemberType.USER, UPDATED_BY_ID);
    }

    @Test
    public void should_create_profile_member_group_update_profile_metadata() throws Exception {
        checkProfileMetaDataUpdate(PROFILE_ID, null, GROUP_ID, null, MemberType.GROUP, UPDATED_BY_ID);
    }

    @Test
    public void should_create_profile_member_membership_update_profile_metadata() throws Exception {
        checkProfileMetaDataUpdate(PROFILE_ID, null, GROUP_ID, ROLE_ID, MemberType.MEMBERSHIP, UPDATED_BY_ID);
    }

    @Test
    public void should_create_profile_member_role_update_profile_metadata() throws Exception {
        checkProfileMetaDataUpdate(PROFILE_ID, null, null, ROLE_ID, MemberType.ROLE, UPDATED_BY_ID);
    }

    private void checkProfileMetaDataUpdate(final long profileId, final Long userId, final Long groupId, final Long roleId, final MemberType memberType,
            final long updatedById) throws SBonitaException, SProfileUpdateException {

        // given
        createProfileMember = spy(new CreateProfileMember(profileService, identityService, profileId, userId, groupId, roleId,
                memberType, updatedById));

        // when
        createProfileMember.execute();

        // then
        verify(profileService, times(1)).updateProfileMetaData(anyLong(), anyLong());
    }
}
