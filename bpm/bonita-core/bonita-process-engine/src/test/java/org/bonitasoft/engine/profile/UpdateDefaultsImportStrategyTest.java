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
package org.bonitasoft.engine.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.profile.exception.profile.SProfileUpdateException;
import org.bonitasoft.engine.profile.exception.profileentry.SProfileEntryDeletionException;
import org.bonitasoft.engine.profile.exception.profilemember.SProfileMemberDeletionException;
import org.bonitasoft.engine.profile.impl.ExportedProfile;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.model.impl.SProfileImpl;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDefaultsImportStrategyTest {

    @Mock
    public ProfileService profileService;
    @InjectMocks
    public UpdateDefaultsImportStrategy updateDefaultsImportStrategy;

    @Test
    public void should_whenProfileExists_update_if_default() throws SProfileUpdateException, SProfileEntryDeletionException, SProfileMemberDeletionException {
        //when
        SProfileImpl sProfile = new SProfileImpl();
        sProfile.setDefault(false);
        updateDefaultsImportStrategy.whenProfileExists(-1,new ExportedProfile("plop",true), sProfile);

        verify(profileService).updateProfile(eq(sProfile), any(EntityUpdateDescriptor.class));
    }
    @Test
    public void should_whenProfileExists_update_if_default2() throws SProfileUpdateException, SProfileEntryDeletionException, SProfileMemberDeletionException {
        //when
        SProfileImpl sProfile = new SProfileImpl();
        sProfile.setDefault(true);
        updateDefaultsImportStrategy.whenProfileExists(-1,new ExportedProfile("plop",false), sProfile);

        verify(profileService).updateProfile(eq(sProfile), any(EntityUpdateDescriptor.class));
    }
    @Test
    public void should_whenProfileExists_not_update_if_custom() throws SProfileUpdateException, SProfileEntryDeletionException, SProfileMemberDeletionException {
        //when
        SProfileImpl sProfile = new SProfileImpl();
        sProfile.setDefault(false);
        updateDefaultsImportStrategy.whenProfileExists(-1,new ExportedProfile("plop",false), sProfile);

        verify(profileService,times(0)).updateProfile(any(SProfile.class), any(EntityUpdateDescriptor.class));
    }
    @Test
    public void should_shouldUpdateProfileEntries_return_true() throws SProfileUpdateException, SProfileEntryDeletionException, SProfileMemberDeletionException {
        //when
        boolean shouldUpdateProfileEntries = updateDefaultsImportStrategy.shouldUpdateProfileEntries(null, null);

        assertThat(shouldUpdateProfileEntries).isTrue();
    }

    @Test
    public void  should_canCreateProfileIfNotExists_return_true_if_default(){
        boolean canCreateProfileIfNotExists = updateDefaultsImportStrategy.canCreateProfileIfNotExists(new ExportedProfile("plop", true));

        assertThat(canCreateProfileIfNotExists).isTrue();
    }
    @Test
    public void  should_canCreateProfileIfNotExists_return_false_if_custom(){
        boolean canCreateProfileIfNotExists = updateDefaultsImportStrategy.canCreateProfileIfNotExists(new ExportedProfile("plop", false));

        assertThat(canCreateProfileIfNotExists).isFalse();
    }
}