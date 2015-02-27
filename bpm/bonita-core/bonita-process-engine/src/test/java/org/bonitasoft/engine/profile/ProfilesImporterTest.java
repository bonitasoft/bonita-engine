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
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportError.Type;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.api.ImportStatus.Status;
import org.bonitasoft.engine.commons.Pair;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SGroupNotFoundException;
import org.bonitasoft.engine.identity.SRoleNotFoundException;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.profile.exception.profile.SProfileCreationException;
import org.bonitasoft.engine.profile.exception.profile.SProfileNotFoundException;
import org.bonitasoft.engine.profile.exception.profile.SProfileUpdateException;
import org.bonitasoft.engine.profile.exception.profileentry.SProfileEntryCreationException;
import org.bonitasoft.engine.profile.exception.profileentry.SProfileEntryDeletionException;
import org.bonitasoft.engine.profile.impl.ExportedParentProfileEntry;
import org.bonitasoft.engine.profile.impl.ExportedProfile;
import org.bonitasoft.engine.profile.impl.ExportedProfileEntry;
import org.bonitasoft.engine.profile.impl.ExportedProfileMapping;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.model.SProfileEntry;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProfilesImporterTest {

    @Mock
    private IdentityService identityService;

    @Mock
    private ProfileImportStrategy importStrategy;

    @Mock
    private ProfileService profileService;

    @Mock
    EntityUpdateDescriptor entityUpdateDescriptor;

    private ProfilesImporter profilesImporter;

    private ReplaceDuplicateImportStrategy replaceDuplicateImportStrategy;

    @Test
    public void should_importProfiles_replace_custom_profile() throws Exception {
        // given
        final ExportedProfile exportedProfile = new ExportedProfile("Mine", false);

        addTwoProfileEntries(exportedProfile);
        createReplaceDuplicateStrategy();

        createImporter(replaceDuplicateImportStrategy, exportedProfile);

        doReturn(mock(SProfile.class)).when(profileService).getProfileByName(exportedProfile.getName());

        // when
        final List<ImportStatus> importProfiles = profilesImporter.importProfiles(-1);

        // then: all entries and mappings are replaced
        verify(profileService, times(1)).deleteAllProfileMembersOfProfile(any(SProfile.class));
        verify(profileService, times(1)).deleteAllProfileEntriesOfProfile(any(SProfile.class));
        verify(profilesImporter, times(1)).importProfileEntries(any(ProfileService.class), anyListOf(ExportedParentProfileEntry.class), anyLong());
        assertThat(importProfiles.get(0)).isEqualTo(importStatusWith("Mine", Status.REPLACED));
    }

    @Test
    public void should_importProfiles_replace_default_profile() throws Exception {
        // given
        final ExportedProfile exportedProfile = new ExportedProfile("Mine", true);

        addTwoProfileEntries(exportedProfile);
        createReplaceDuplicateStrategy();
        createImporter(replaceDuplicateImportStrategy, exportedProfile);

        doReturn(mock(SProfile.class)).when(profileService).getProfileByName(exportedProfile.getName());

        // when
        final List<ImportStatus> importProfiles = profilesImporter.importProfiles(-1);

        // then: all entries and mappings are replaced
        verify(profileService, times(1)).deleteAllProfileMembersOfProfile(any(SProfile.class));
        verify(profileService, times(0)).deleteAllProfileEntriesOfProfile(any(SProfile.class));
        verify(profilesImporter, times(0)).importProfileEntries(any(ProfileService.class), anyListOf(ExportedParentProfileEntry.class), anyLong());
        assertThat(importProfiles.get(0)).isEqualTo(importStatusWith("Mine", Status.REPLACED));
    }

    private void createReplaceDuplicateStrategy() {
        replaceDuplicateImportStrategy = spy(new ReplaceDuplicateImportStrategy(profileService));
        doReturn(entityUpdateDescriptor).when(replaceDuplicateImportStrategy).getProfileUpdateDescriptor(any(ExportedProfile.class), anyLong(),
                anyBoolean());
    }

    private void addTwoProfileEntries(final ExportedProfile exportedProfile) {
        exportedProfile.getParentProfileEntries().add(
                createParent("p1", "page1", true));
        exportedProfile.getParentProfileEntries().add(
                createParent("p2",
                        createChild("c1", "pagec1", true),
                        createChild("c2", "pagec2", false)));
    }

    @Test(expected = ExecutionException.class)
    public void should_importProfiles_throw_ExecutionException() throws Exception {
        // given
        final ExportedProfile exportedProfile = new ExportedProfile("Mine", false);
        createImporter(importStrategy, exportedProfile);
        doThrow(new SProfileEntryDeletionException("")).when(profilesImporter).importTheProfile(anyLong(), any(ExportedProfile.class), any(SProfile.class));
        // when
        profilesImporter.importProfiles(-1);

        // then: expected exception
    }

    @Test
    public void should_importProfiles_with_replace_do_not_delete_profile_entries() throws Exception {
        // given
        final ExportedProfile exportedProfile = new ExportedProfile("Mine", true);

        addTwoProfileEntries(exportedProfile);
        createReplaceDuplicateStrategy();
        createImporter(replaceDuplicateImportStrategy, exportedProfile);
        doReturn(mock(SProfile.class)).when(profileService).getProfileByName(exportedProfile.getName());
        // when
        final List<ImportStatus> importProfiles = profilesImporter.importProfiles(-1);

        // then
        verify(profileService, times(1)).deleteAllProfileMembersOfProfile(any(SProfile.class));
        verify(profileService, never()).deleteAllProfileEntriesOfProfile(any(SProfile.class));
        verify(profilesImporter, never()).importProfileEntries(any(ProfileService.class), anyListOf(ExportedParentProfileEntry.class), anyLong());
        assertThat(importProfiles.get(0)).isEqualTo(importStatusWith("Mine", Status.REPLACED));
    }

    @Test
    public void should_importProfiles_on_default_with_replace_do_not_delete_profile_entries() throws Exception {
        // given
        final ExportedProfile exportedProfile = new ExportedProfile("Mine", false);

        addTwoProfileEntries(exportedProfile);
        createReplaceDuplicateStrategy();
        createImporter(replaceDuplicateImportStrategy, exportedProfile);

        final SProfile existingProfile = mock(SProfile.class);

        doReturn(true).when(existingProfile).isDefault();
        doReturn(existingProfile).when(profileService).getProfileByName(exportedProfile.getName());
        // when
        final List<ImportStatus> importProfiles = profilesImporter.importProfiles(-1);

        // then
        verify(profileService, times(1)).deleteAllProfileMembersOfProfile(any(SProfile.class));
        verify(profileService, never()).deleteAllProfileEntriesOfProfile(any(SProfile.class));
        verify(profilesImporter, never()).importProfileEntries(any(ProfileService.class), anyListOf(ExportedParentProfileEntry.class), anyLong());
        assertThat(importProfiles.get(0)).isEqualTo(importStatusWith("Mine", Status.REPLACED));
    }

    @Test
    public void should_importProfiles_whith_ReplaceDuplicate_do_not_insert_default_profile() throws Exception {
        // given
        final ExportedProfile exportedProfile = new ExportedProfile("Mine", true);
        addTwoProfileEntries(exportedProfile);

        final ReplaceDuplicateImportStrategy replaceDuplicateImportStrategy = spy(new ReplaceDuplicateImportStrategy(profileService));
        final SProfile existingProfile = mock(SProfile.class);
        // doReturn(existingProfile).when(replaceDuplicateImportStrategy).createSProfile(any(ExportedProfile.class), anyLong());

        createImporter(replaceDuplicateImportStrategy, exportedProfile);

        doReturn(true).when(existingProfile).isDefault();
        doThrow(new SProfileNotFoundException("")).when(profileService).getProfileByName("Mine");

        // when
        final List<ImportStatus> importProfiles = profilesImporter.importProfiles(-1);

        // then
        assertThat(importProfiles.get(0)).isEqualTo(importStatusWith("Mine", Status.SKIPPED));
        verify(profileService, never()).createProfile(any(SProfile.class));
    }

    @Test
    public void should_importProfiles_add_profile() throws Exception {
        // given
        final ExportedProfile exportedProfile = new ExportedProfile("MineNotDefault", false);
        addTwoProfileEntries(exportedProfile);
        createImporter(new ReplaceDuplicateImportStrategy(profileService), exportedProfile, new ExportedProfile("MineDefault", true));
        // profile do not exists
        doThrow(new SProfileNotFoundException("")).when(profileService).getProfileByName("MineNotDefault");
        doThrow(new SProfileNotFoundException("")).when(profileService).getProfileByName("MineDefault");

        // when
        final List<ImportStatus> importProfiles = profilesImporter.importProfiles(-1);

        assertThat(importProfiles.get(0)).isEqualTo(importStatusWith("MineNotDefault", Status.ADDED));
        assertThat(importProfiles.get(1)).isEqualTo(importStatusWith("MineDefault", Status.SKIPPED));
    }

    @Test
    public void should_importProfiles_skip_profile_when_strategy_tells_it() throws Exception {
        // given
        final ExportedProfile exportedProfile = new ExportedProfile("Mine", false);
        addTwoProfileEntries(exportedProfile);
        createImporter(new IgnoreDuplicateImportStrategy(profileService), exportedProfile);
        // profile exists
        doReturn(mock(SProfile.class)).when(profileService).getProfileByName(exportedProfile.getName());

        // when
        final List<ImportStatus> importProfiles = profilesImporter.importProfiles(-1);

        assertThat(importProfiles.get(0)).isEqualTo(importStatusWith("Mine", Status.SKIPPED));
    }

    @Test
    public void should_importProfiles_import_nothing_when_profile_name_is_empty() throws Exception {
        // given
        createImporter(importStrategy, new ExportedProfile("", false));

        // when
        final List<ImportStatus> importProfiles = profilesImporter.importProfiles(-1);

        // then
        assertThat(importProfiles).isEmpty();
    }

    @Test
    public void should_importProfiles_import_nothing_when_profile_name_is_null() throws Exception {
        // given
        createImporter(importStrategy, new ExportedProfile(null, false));

        // when
        final List<ImportStatus> importProfiles = profilesImporter.importProfiles(-1);

        // then
        assertThat(importProfiles).isEmpty();
    }

    @Test
    public void should_importProfiles_with_skip_strategy_return_skip_status() throws Exception {
        // given
        final ExportedProfile exportedProfile = new ExportedProfile("Mine", true);

        addTwoProfileEntries(exportedProfile);
        createImporter(new IgnoreDuplicateImportStrategy(profileService), exportedProfile);
        doReturn(mock(SProfile.class)).when(profileService).getProfileByName(exportedProfile.getName());
        // when
        final List<ImportStatus> importProfiles = profilesImporter.importProfiles(-1);

        // then
        verify(profileService, never()).deleteAllProfileMembersOfProfile(any(SProfile.class));
        verify(profileService, never()).deleteAllProfileEntriesOfProfile(any(SProfile.class));
        verify(profilesImporter, never()).importProfileEntries(any(ProfileService.class), anyListOf(ExportedParentProfileEntry.class), anyLong());
        assertThat(importProfiles.get(0)).isEqualTo(importStatusWith("Mine", Status.SKIPPED));
    }

    private ImportStatus importStatusWith(final String name, final Status status) {
        final ImportStatus expected = new ImportStatus(name);
        expected.setStatus(status);
        return expected;
    }

    private void createImporter(final ProfileImportStrategy importStrategy, final ExportedProfile... exportedProfile) throws SProfileUpdateException,
            SProfileCreationException, SProfileEntryCreationException {
        profilesImporter = spy(new ProfilesImporter(profileService, identityService, Arrays.asList(exportedProfile), importStrategy));
        doReturn(mock(SProfile.class)).when(profilesImporter).createSProfile(any(ExportedProfile.class), anyLong());
        doReturn(mock(SProfileEntry.class)).when(profilesImporter).createProfileEntry(any(ExportedParentProfileEntry.class), anyLong(), anyLong());
        doReturn(mock(SProfileEntry.class)).when(profilesImporter).createProfileEntry(any(ExportedParentProfileEntry.class), anyLong(), anyLong());
        doReturn(mock(SProfileEntry.class)).when(profilesImporter).createProfileEntry(any(ExportedProfileEntry.class), anyLong(), anyLong());
        doReturn(mock(SProfile.class)).when(profileService).createProfile(any(SProfile.class));
        doReturn(mock(SProfileEntry.class)).when(profileService).createProfileEntry(any(SProfileEntry.class));
        doReturn(mock(SProfile.class)).when(profileService).updateProfile(any(SProfile.class), any(EntityUpdateDescriptor.class));

    }

    private ExportedParentProfileEntry createParent(final String name, final String pageName, final boolean custom) {
        final ExportedParentProfileEntry parentProfileEntry = new ExportedParentProfileEntry(name);
        parentProfileEntry.setPage(pageName);
        parentProfileEntry.setCustom(custom);
        return parentProfileEntry;
    }

    private ExportedParentProfileEntry createParent(final String name, final ExportedProfileEntry... children) {
        final ExportedParentProfileEntry parentProfileEntry = new ExportedParentProfileEntry(name);
        parentProfileEntry.setChildProfileEntries(Arrays.asList(children));
        return parentProfileEntry;
    }

    private ExportedProfileEntry createChild(final String name, final String pageName, final boolean custom) {
        final ExportedProfileEntry parentProfileEntry = new ExportedProfileEntry(name);
        parentProfileEntry.setPage(pageName);
        parentProfileEntry.setCustom(custom);
        return parentProfileEntry;
    }

    @Test
    public void should_importProfileMapping_return_error_if_user_do_not_exists() throws Exception {
        // given
        createImporter(new IgnoreDuplicateImportStrategy(profileService));
        final ExportedProfileMapping exportedProfileMapping = new ExportedProfileMapping();
        exportedProfileMapping.setUsers(Arrays.asList("john"));
        doThrow(new SUserNotFoundException("john")).when(identityService).getUserByUserName("john");
        // when
        final List<ImportError> importProfileMapping = profilesImporter.importProfileMapping(profileService, identityService, 123, exportedProfileMapping);

        // then
        assertThat(importProfileMapping.get(0)).isEqualTo(new ImportError("john", Type.USER));
    }

    @Test
    public void should_importProfileMapping_return_no_error_if_user_exists() throws Exception {
        // given
        createImporter(new IgnoreDuplicateImportStrategy(profileService));
        final ExportedProfileMapping exportedProfileMapping = new ExportedProfileMapping();
        exportedProfileMapping.setUsers(Arrays.asList("john"));
        final SUser user = mock(SUser.class);
        when(user.getId()).thenReturn(456l);
        when(user.getUserName()).thenReturn("john");
        doReturn(user).when(identityService).getUserByUserName("john");

        // when
        final List<ImportError> importProfileMapping = profilesImporter.importProfileMapping(profileService, identityService, 123, exportedProfileMapping);

        // then
        assertThat(importProfileMapping).isEmpty();
        verify(profileService, times(1)).addUserToProfile(123l, 456l, null, null, "john");
    }

    @Test
    public void should_importProfileMapping_return_error_if_role_do_not_exists() throws Exception {
        // given
        createImporter(new IgnoreDuplicateImportStrategy(profileService));
        final ExportedProfileMapping exportedProfileMapping = new ExportedProfileMapping();
        exportedProfileMapping.setRoles(Arrays.asList("role"));
        doThrow(new SRoleNotFoundException("role")).when(identityService).getRoleByName("role");
        // when
        final List<ImportError> importProfileMapping = profilesImporter.importProfileMapping(profileService, identityService, 123, exportedProfileMapping);

        // then
        assertThat(importProfileMapping.get(0)).isEqualTo(new ImportError("role", Type.ROLE));
    }

    @Test
    public void should_importProfileMapping_return_no_error_if_role_exists() throws Exception {
        // given
        createImporter(new IgnoreDuplicateImportStrategy(profileService));
        final ExportedProfileMapping exportedProfileMapping = new ExportedProfileMapping();
        exportedProfileMapping.setRoles(Arrays.asList("role"));
        final SRole role = mock(SRole.class);
        when(role.getId()).thenReturn(456l);
        when(role.getName()).thenReturn("role");
        doReturn(role).when(identityService).getRoleByName("role");

        // when
        final List<ImportError> importProfileMapping = profilesImporter.importProfileMapping(profileService, identityService, 123, exportedProfileMapping);

        // then
        assertThat(importProfileMapping).isEmpty();

        verify(profileService, times(1)).addRoleToProfile(123l, 456l, "role");
    }

    @Test
    public void should_importProfileMapping_return_error_if_group_do_not_exists() throws Exception {
        // given
        createImporter(new IgnoreDuplicateImportStrategy(profileService));
        final ExportedProfileMapping exportedProfileMapping = new ExportedProfileMapping();
        exportedProfileMapping.setGroups(Arrays.asList("group"));
        doThrow(new SGroupNotFoundException("group")).when(identityService).getGroupByPath("group");
        // when
        final List<ImportError> importProfileMapping = profilesImporter.importProfileMapping(profileService, identityService, 123, exportedProfileMapping);

        // then
        assertThat(importProfileMapping.get(0)).isEqualTo(new ImportError("group", Type.GROUP));
    }

    @Test
    public void should_importProfileMapping_return_no_error_if_group_exists() throws Exception {
        // given
        createImporter(new IgnoreDuplicateImportStrategy(profileService));
        final ExportedProfileMapping exportedProfileMapping = new ExportedProfileMapping();
        exportedProfileMapping.setGroups(Arrays.asList("group"));
        final SGroup group = mock(SGroup.class);
        when(group.getId()).thenReturn(456l);
        when(group.getName()).thenReturn("group");
        doReturn(group).when(identityService).getGroupByPath("group");

        // when
        final List<ImportError> importProfileMapping = profilesImporter.importProfileMapping(profileService, identityService, 123, exportedProfileMapping);

        // then
        assertThat(importProfileMapping).isEmpty();

        verify(profileService, times(1)).addGroupToProfile(123l, 456l, "group", null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void should_importProfileMapping_return_error_if_group_membership_do_not_exists() throws Exception {
        // given
        createImporter(new IgnoreDuplicateImportStrategy(profileService));
        final ExportedProfileMapping exportedProfileMapping = new ExportedProfileMapping();
        exportedProfileMapping.setMemberships(Arrays.asList(new Pair<String, String>("group", "role")));
        doThrow(new SGroupNotFoundException("group")).when(identityService).getGroupByPath("group");
        // when
        final List<ImportError> importProfileMapping = profilesImporter.importProfileMapping(profileService, identityService, 123, exportedProfileMapping);

        // then
        assertThat(importProfileMapping.get(0)).isEqualTo(new ImportError("group", Type.GROUP));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void should_importProfileMapping_return_error_if_role_membership_do_not_exists() throws Exception {
        // given
        createImporter(new IgnoreDuplicateImportStrategy(profileService));
        final ExportedProfileMapping exportedProfileMapping = new ExportedProfileMapping();
        exportedProfileMapping.setMemberships(Arrays.asList(new Pair<String, String>("group", "role")));
        doThrow(new SRoleNotFoundException("role")).when(identityService).getRoleByName("role");
        // when
        final List<ImportError> importProfileMapping = profilesImporter.importProfileMapping(profileService, identityService, 123, exportedProfileMapping);

        // then
        assertThat(importProfileMapping.get(0)).isEqualTo(new ImportError("role", Type.ROLE));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void should_importProfileMapping_return_error_if_role_and_group_membership_do_not_exists() throws Exception {
        // given
        createImporter(new IgnoreDuplicateImportStrategy(profileService));
        final ExportedProfileMapping exportedProfileMapping = new ExportedProfileMapping();
        exportedProfileMapping.setMemberships(Arrays.asList(new Pair<String, String>("group", "role")));
        doThrow(new SRoleNotFoundException("role")).when(identityService).getRoleByName("role");
        doThrow(new SGroupNotFoundException("group")).when(identityService).getGroupByPath("group");
        // when
        final List<ImportError> importProfileMapping = profilesImporter.importProfileMapping(profileService, identityService, 123, exportedProfileMapping);

        // then
        assertThat(importProfileMapping.get(1)).isEqualTo(new ImportError("role", Type.ROLE));
        assertThat(importProfileMapping.get(0)).isEqualTo(new ImportError("group", Type.GROUP));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void should_importProfileMapping_return_no_error_if_membershi_exists() throws Exception {
        // given
        createImporter(new IgnoreDuplicateImportStrategy(profileService));
        final ExportedProfileMapping exportedProfileMapping = new ExportedProfileMapping();
        exportedProfileMapping.setMemberships(Arrays.asList(new Pair<String, String>("group", "role")));
        final SRole role = mock(SRole.class);
        when(role.getId()).thenReturn(456l);
        when(role.getName()).thenReturn("role");
        doReturn(role).when(identityService).getRoleByName("role");
        final SGroup group = mock(SGroup.class);
        when(group.getId()).thenReturn(789l);
        when(group.getName()).thenReturn("group");
        doReturn(group).when(identityService).getGroupByPath("group");

        // when
        final List<ImportError> importProfileMapping = profilesImporter.importProfileMapping(profileService, identityService, 123, exportedProfileMapping);

        // then
        assertThat(importProfileMapping).isEmpty();

        verify(profileService, times(1)).addRoleAndGroupToProfile(123l, 456l, 789l, "role", "group", null);
    }
}
