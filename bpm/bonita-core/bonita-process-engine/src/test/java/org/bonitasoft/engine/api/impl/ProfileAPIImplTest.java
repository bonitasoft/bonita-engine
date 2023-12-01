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
package org.bonitasoft.engine.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.bonitasoft.engine.api.impl.profile.ProfileAPIDelegate;
import org.bonitasoft.engine.profile.ProfileCriterion;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProfileAPIImplTest {

    private static final long USER_ID = 1L;

    @Mock
    ProfileAPIDelegate profileApiDelegate;

    @Spy
    private ProfileAPIImpl profileAPIImpl;

    @Before
    public void before() throws Exception {
        doReturn(profileApiDelegate).when(profileAPIImpl).getProfileAPIDelegate();
    }

    @Test
    public void should_invoke_getProfilesForUser_from_delegate() {
        // when:
        profileAPIImpl.getProfilesForUser(-1, 0, 10, ProfileCriterion.ID_ASC);

        // then:
        verify(profileApiDelegate, times(1)).getProfilesForUser(-1, 0, 10, ProfileCriterion.ID_ASC);
    }

    @Test
    public void should_invoke_deleteProfileMember_from_delegate() throws Exception {
        // when
        profileAPIImpl.deleteProfileMember(1L);

        // then
        verify(profileApiDelegate, times(1)).deleteProfileMember(1L);
    }

    @Test
    public void should_invoke_createProfileMember_from_delegate() throws Exception {
        final Long profileId = 1L;
        final Long userId = 2L;
        final Long groupId = 3L;
        final Long roleId = 4L;

        // when
        profileAPIImpl.createProfileMember(profileId, userId, groupId, roleId);

        // then
        verify(profileApiDelegate, times(1)).createProfileMember(profileId, userId, groupId, roleId);

    }

    @Test
    public void should_invoke_getMemberType_from_delegate() throws Exception {
        profileAPIImpl.getMemberType(USER_ID, null, null);
        verify(profileApiDelegate, times(1)).getMemberType(USER_ID, null, null);

    }

    @Test
    public void should_profile_api_be_available_when_tenant_is_paused() throws Exception {
        assertThat(ProfileAPIImpl.class.isAnnotationPresent(AvailableInMaintenanceMode.class))
                .as("should profile api be available when tenant is paused")
                .isTrue();
    }
}
