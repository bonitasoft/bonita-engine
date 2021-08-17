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
package org.bonitasoft.engine.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
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
public class UpdateDefaultsImportStrategyTest {

    @Mock
    public ProfileService profileService;
    @InjectMocks
    public UpdateDefaultsImportStrategy updateDefaultsImportStrategy;

    @Test
    public void should_whenProfileExists_update_if_default()
            throws SProfileUpdateException {
        //when
        SProfile sProfile = SProfile.builder().build();
        sProfile.setDefault(false);
        updateDefaultsImportStrategy.whenProfileExists(-1, new ProfileNode("plop", true), sProfile);

        verify(profileService).updateProfile(eq(sProfile), any(EntityUpdateDescriptor.class));
    }

    @Test
    public void should_whenProfileExists_update_if_default2()
            throws SProfileUpdateException {
        //when
        SProfile sProfile = SProfile.builder().build();
        sProfile.setDefault(true);
        updateDefaultsImportStrategy.whenProfileExists(-1, new ProfileNode("plop", false), sProfile);

        verify(profileService).updateProfile(eq(sProfile), any(EntityUpdateDescriptor.class));
    }

    @Test
    public void should_whenProfileExists_not_update_if_custom()
            throws SProfileUpdateException {
        //when
        SProfile sProfile = SProfile.builder().build();
        sProfile.setDefault(false);
        updateDefaultsImportStrategy.whenProfileExists(-1, new ProfileNode("plop", false), sProfile);

        verify(profileService, times(0)).updateProfile(any(SProfile.class), any(EntityUpdateDescriptor.class));
    }

    @Test
    public void should_canCreateProfileIfNotExists_return_true_if_default() {
        boolean canCreateProfileIfNotExists = updateDefaultsImportStrategy
                .canCreateProfileIfNotExists(new ProfileNode("plop", true));

        assertThat(canCreateProfileIfNotExists).isTrue();
    }

    @Test
    public void should_canCreateProfileIfNotExists_return_false_if_custom() {
        boolean canCreateProfileIfNotExists = updateDefaultsImportStrategy
                .canCreateProfileIfNotExists(new ProfileNode("plop", false));

        assertThat(canCreateProfileIfNotExists).isFalse();
    }
}
