/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
package org.bonitasoft.engine.profile;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.profile.exception.profile.SProfileUpdateException;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.xml.ProfileNode;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDefaultsAndCreateNewImportStrategyTest {

    @Mock
    private ProfileService profileService;
    @InjectMocks
    private UpdateDefaultsAndCreateNewImportStrategy strategy;

    @Test
    public void should_update_profile_if_new_default_profile() throws SProfileUpdateException {
        //given
        SProfile existingProfile = SProfile.builder().isDefault(false).build();
        ProfileNode newProfile = new ProfileNode("foo", true);

        //when
        strategy.whenProfileExists(-1, newProfile, existingProfile);

        //then
        verify(profileService).updateProfile(eq(existingProfile), any(EntityUpdateDescriptor.class));
    }

    @Test
    public void should_update_profile_if_existing_default_profile() throws SProfileUpdateException {
        //given
        SProfile existingProfile = SProfile.builder().isDefault(true).build();
        ProfileNode newProfile = new ProfileNode("foo", false);

        //when
        strategy.whenProfileExists(-1, newProfile, existingProfile);

        //then
        verify(profileService).updateProfile(eq(existingProfile), any(EntityUpdateDescriptor.class));
    }

    @Test
    public void should_update_profile_if_new_and_existing_default_profiles() throws SProfileUpdateException {
        //given
        SProfile existingProfile = SProfile.builder().isDefault(true).build();
        ProfileNode newProfile = new ProfileNode("foo", true);

        //when
        strategy.whenProfileExists(-1, newProfile, existingProfile);

        //then
        verify(profileService).updateProfile(eq(existingProfile), any(EntityUpdateDescriptor.class));
    }

    @Test
    public void should_raise_exception_if_existing_non_default_profile() {
        //given
        SProfile existingProfile = SProfile.builder().isDefault(false).build();
        ProfileNode newProfile = new ProfileNode("foo", false);

        //then
        assertThatExceptionOfType(SProfileUpdateException.class)
                .isThrownBy(() -> strategy.whenProfileExists(-1, newProfile, existingProfile))
                .withMessage("A profile already exists with name: foo");
    }

}
