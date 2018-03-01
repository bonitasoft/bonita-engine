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
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportError.Type;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.api.ImportStatus.Status;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SGroupNotFoundException;
import org.bonitasoft.engine.identity.SRoleNotFoundException;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.profile.exception.profile.SProfileNotFoundException;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.model.SProfileEntry;
import org.bonitasoft.engine.profile.xml.MembershipNode;
import org.bonitasoft.engine.profile.xml.ParentProfileEntryNode;
import org.bonitasoft.engine.profile.xml.ProfileEntryNode;
import org.bonitasoft.engine.profile.xml.ProfileMappingNode;
import org.bonitasoft.engine.profile.xml.ProfileNode;
import org.bonitasoft.engine.profile.xml.ProfilesNode;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.session.SessionService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @InjectMocks
    private ProfilesImporter profilesImporter;

    @Before
    public void before() throws Exception {
        doReturn(mock(SProfile.class)).when(profileService).createProfile(any(SProfile.class));
        doReturn(mock(SProfileEntry.class)).when(profileService).createProfileEntry(any(SProfileEntry.class));
        doReturn(mock(SProfile.class)).when(profileService).updateProfile(any(SProfile.class), any(EntityUpdateDescriptor.class));
    }

    @Test
    public void should_importProfiles_replace_custom_profile() throws Exception {
        // given
        final ProfileNode exportedProfile = new ProfileNode("Mine", false);

        addTwoProfileEntries(exportedProfile);

        doReturn(mock(SProfile.class)).when(profileService).getProfileByName(exportedProfile.getName());

        // when
        final List<ImportStatus> importProfiles = profilesImporter.importProfiles(exportedProfiles(exportedProfile),
                ImportPolicy.REPLACE_DUPLICATES, -1);

        // then: all entries and mappings are replaced
        verify(profileService, times(1)).deleteAllProfileMembersOfProfile(any(SProfile.class));
        verify(profileService, times(1)).deleteAllProfileEntriesOfProfile(any(SProfile.class));
        assertThat(importProfiles.get(0)).isEqualTo(importStatusWith("Mine", Status.REPLACED));
    }

    @Test
    public void should_importProfiles_replace_default_profile() throws Exception {
        // given
        final ProfileNode exportedProfile = new ProfileNode("Mine", true);

        addTwoProfileEntries(exportedProfile);

        doReturn(mock(SProfile.class)).when(profileService).getProfileByName(exportedProfile.getName());

        // when
        final List<ImportStatus> importProfiles = profilesImporter.importProfiles(exportedProfiles(exportedProfile), ImportPolicy.REPLACE_DUPLICATES, -1);

        // then: all entries and mappings are replaced
        verify(profileService, times(1)).deleteAllProfileMembersOfProfile(any(SProfile.class));
        verify(profileService, times(0)).deleteAllProfileEntriesOfProfile(any(SProfile.class));
        assertThat(importProfiles.get(0)).isEqualTo(importStatusWith("Mine", Status.REPLACED));
    }

    private ProfilesNode exportedProfiles(ProfileNode... exportedProfile) {
        return new ProfilesNode(Arrays.asList(exportedProfile));
    }

    private void addTwoProfileEntries(final ProfileNode exportedProfile) {
        exportedProfile.getParentProfileEntries().add(
                createParent("p1", "page1", true));
        exportedProfile.getParentProfileEntries().add(
                createParent("p2",
                        createChild("c1", "pagec1", true),
                        createChild("c2", "pagec2", false)));
    }

    @Test
    public void should_importProfiles_with_replace_do_not_delete_profile_entries() throws Exception {
        // given
        final ProfileNode exportedProfile = new ProfileNode("Mine", true);

        addTwoProfileEntries(exportedProfile);
        doReturn(mock(SProfile.class)).when(profileService).getProfileByName(exportedProfile.getName());
        // when
        final List<ImportStatus> importProfiles = profilesImporter.importProfiles(exportedProfiles(exportedProfile), ImportPolicy.REPLACE_DUPLICATES, -1);

        // then
        verify(profileService, times(1)).deleteAllProfileMembersOfProfile(any(SProfile.class));
        verify(profileService, never()).deleteAllProfileEntriesOfProfile(any(SProfile.class));
        assertThat(importProfiles.get(0)).isEqualTo(importStatusWith("Mine", Status.REPLACED));
    }

    @Test
    public void should_importProfiles_on_default_with_replace_do_not_delete_profile_entries() throws Exception {
        // given
        final ProfileNode exportedProfile = new ProfileNode("Mine", false);

        addTwoProfileEntries(exportedProfile);

        final SProfile existingProfile = mock(SProfile.class);

        doReturn(true).when(existingProfile).isDefault();
        doReturn(existingProfile).when(profileService).getProfileByName(exportedProfile.getName());
        // when
        final List<ImportStatus> importProfiles = profilesImporter.importProfiles(exportedProfiles(exportedProfile), ImportPolicy.REPLACE_DUPLICATES, -1);

        // then
        verify(profileService, times(1)).deleteAllProfileMembersOfProfile(any(SProfile.class));
        verify(profileService, never()).deleteAllProfileEntriesOfProfile(any(SProfile.class));
        assertThat(importProfiles.get(0)).isEqualTo(importStatusWith("Mine", Status.REPLACED));
    }

    @Test
    public void should_importProfiles_with_ReplaceDuplicate_do_not_insert_default_profile() throws Exception {
        // given
        final ProfileNode exportedProfile = new ProfileNode("Mine", true);
        addTwoProfileEntries(exportedProfile);

        final SProfile existingProfile = mock(SProfile.class);

        doThrow(new SProfileNotFoundException("")).when(profileService).getProfileByName("Mine");

        // when
        final List<ImportStatus> importProfiles = profilesImporter.importProfiles(exportedProfiles(exportedProfile), ImportPolicy.REPLACE_DUPLICATES,
                SessionService.SYSTEM_ID);

        // then
        assertThat(importProfiles.get(0)).isEqualTo(importStatusWith("Mine", Status.SKIPPED));
        verify(profileService, never()).createProfile(any(SProfile.class));
    }

    @Test
    public void should_importProfiles_add_profile() throws Exception {
        // given
        final ProfileNode exportedProfile = new ProfileNode("MineNotDefault", false);
        addTwoProfileEntries(exportedProfile);
        // profile do not exists
        doThrow(new SProfileNotFoundException("")).when(profileService).getProfileByName("MineNotDefault");
        doThrow(new SProfileNotFoundException("")).when(profileService).getProfileByName("MineDefault");

        // when
        final List<ImportStatus> importProfiles = profilesImporter.importProfiles(exportedProfiles(exportedProfile, new ProfileNode("MineDefault", true)),
                ImportPolicy.REPLACE_DUPLICATES, -1);

        assertThat(importProfiles.get(0)).isEqualTo(importStatusWith("MineNotDefault", Status.ADDED));
        assertThat(importProfiles.get(1)).isEqualTo(importStatusWith("MineDefault", Status.SKIPPED));
    }

    @Test
    public void should_importProfiles_skip_profile_when_strategy_tells_it() throws Exception {
        // given
        final ProfileNode exportedProfile = new ProfileNode("Mine", false);
        addTwoProfileEntries(exportedProfile);
        // profile exists
        doReturn(mock(SProfile.class)).when(profileService).getProfileByName(exportedProfile.getName());

        // when
        final List<ImportStatus> importProfiles = profilesImporter.importProfiles(exportedProfiles(exportedProfile), ImportPolicy.IGNORE_DUPLICATES, -1);

        assertThat(importProfiles.get(0)).isEqualTo(importStatusWith("Mine", Status.SKIPPED));
    }

    @Test
    public void should_importProfiles_import_nothing_when_profile_name_is_empty() throws Exception {
        // when
        final List<ImportStatus> importProfiles = profilesImporter.importProfiles(exportedProfiles(new ProfileNode("", false)),
                ImportPolicy.UPDATE_DEFAULTS, -1);

        // then
        assertThat(importProfiles).isEmpty();
    }

    @Test
    public void should_importProfiles_import_nothing_when_profile_name_is_null() throws Exception {
        // when
        final List<ImportStatus> importProfiles = profilesImporter.importProfiles(exportedProfiles(new ProfileNode(null, false)),
                ImportPolicy.UPDATE_DEFAULTS, -1);

        // then
        assertThat(importProfiles).isEmpty();
    }

    @Test
    public void should_importProfiles_with_skip_strategy_return_skip_status() throws Exception {
        // given
        final ProfileNode exportedProfile = new ProfileNode("Mine", true);

        addTwoProfileEntries(exportedProfile);
        doReturn(mock(SProfile.class)).when(profileService).getProfileByName(exportedProfile.getName());
        // when
        final List<ImportStatus> importProfiles = profilesImporter.importProfiles(exportedProfiles(exportedProfile), ImportPolicy.IGNORE_DUPLICATES, -1);

        // then
        verify(profileService, never()).deleteAllProfileMembersOfProfile(any(SProfile.class));
        verify(profileService, never()).deleteAllProfileEntriesOfProfile(any(SProfile.class));
        assertThat(importProfiles.get(0)).isEqualTo(importStatusWith("Mine", Status.SKIPPED));
    }

    private ImportStatus importStatusWith(final String name, final Status status) {
        final ImportStatus expected = new ImportStatus(name);
        expected.setStatus(status);
        return expected;
    }

    private ParentProfileEntryNode createParent(final String name, final String pageName, final boolean custom) {
        final ParentProfileEntryNode parentProfileEntry = new ParentProfileEntryNode(name);
        parentProfileEntry.setPage(pageName);
        parentProfileEntry.setCustom(custom);
        return parentProfileEntry;
    }

    private ParentProfileEntryNode createParent(final String name, final ProfileEntryNode... children) {
        final ParentProfileEntryNode parentProfileEntry = new ParentProfileEntryNode(name);
        parentProfileEntry.setChildProfileEntries(Arrays.asList(children));
        return parentProfileEntry;
    }

    private ProfileEntryNode createChild(final String name, final String pageName, final boolean custom) {
        final ProfileEntryNode parentProfileEntry = new ProfileEntryNode(name);
        parentProfileEntry.setPage(pageName);
        parentProfileEntry.setCustom(custom);
        return parentProfileEntry;
    }

    @Test
    public void should_importProfileMapping_return_error_if_user_do_not_exists() throws Exception {
        // given
        final ProfileMappingNode exportedProfileMapping = new ProfileMappingNode();
        exportedProfileMapping.setUsers(Collections.singletonList("john"));
        doThrow(new SUserNotFoundException("john")).when(identityService).getUserByUserName("john");
        // when
        final List<ImportError> importProfileMapping = profilesImporter.importProfileMapping(profileService, identityService, 123, exportedProfileMapping);

        // then
        assertThat(importProfileMapping.get(0)).isEqualTo(new ImportError("john", Type.USER));
    }

    @Test
    public void should_importProfileMapping_return_no_error_if_user_exists() throws Exception {
        // given
        final ProfileMappingNode exportedProfileMapping = new ProfileMappingNode();
        exportedProfileMapping.setUsers(Collections.singletonList("john"));
        final SUser user = mock(SUser.class);
        when(user.getId()).thenReturn(456L);
        when(user.getUserName()).thenReturn("john");
        doReturn(user).when(identityService).getUserByUserName("john");

        // when
        final List<ImportError> importProfileMapping = profilesImporter.importProfileMapping(profileService, identityService, 123, exportedProfileMapping);

        // then
        assertThat(importProfileMapping).isEmpty();
        verify(profileService, times(1)).addUserToProfile(123L, 456L, null, null, "john");
    }

    @Test
    public void should_importProfileMapping_return_error_if_role_do_not_exists() throws Exception {
        // given
        final ProfileMappingNode exportedProfileMapping = new ProfileMappingNode();
        exportedProfileMapping.setRoles(Collections.singletonList("role"));
        doThrow(new SRoleNotFoundException("role")).when(identityService).getRoleByName("role");
        // when
        final List<ImportError> importProfileMapping = profilesImporter.importProfileMapping(profileService, identityService, 123, exportedProfileMapping);

        // then
        assertThat(importProfileMapping.get(0)).isEqualTo(new ImportError("role", Type.ROLE));
    }

    @Test
    public void should_importProfileMapping_return_no_error_if_role_exists() throws Exception {
        // given
        final ProfileMappingNode exportedProfileMapping = new ProfileMappingNode();
        exportedProfileMapping.setRoles(Collections.singletonList("role"));
        final SRole role = mock(SRole.class);
        when(role.getId()).thenReturn(456L);
        when(role.getName()).thenReturn("role");
        doReturn(role).when(identityService).getRoleByName("role");

        // when
        final List<ImportError> importProfileMapping = profilesImporter.importProfileMapping(profileService, identityService, 123, exportedProfileMapping);

        // then
        assertThat(importProfileMapping).isEmpty();

        verify(profileService, times(1)).addRoleToProfile(123L, 456L, "role");
    }

    @Test
    public void should_importProfileMapping_return_error_if_group_do_not_exists() throws Exception {
        // given
        final ProfileMappingNode exportedProfileMapping = new ProfileMappingNode();
        exportedProfileMapping.setGroups(Collections.singletonList("group"));
        doThrow(new SGroupNotFoundException("group")).when(identityService).getGroupByPath("group");
        // when
        final List<ImportError> importProfileMapping = profilesImporter.importProfileMapping(profileService, identityService, 123, exportedProfileMapping);

        // then
        assertThat(importProfileMapping.get(0)).isEqualTo(new ImportError("group", Type.GROUP));
    }

    @Test
    public void should_importProfileMapping_return_no_error_if_group_exists() throws Exception {
        // given
        final ProfileMappingNode exportedProfileMapping = new ProfileMappingNode();
        exportedProfileMapping.setGroups(Collections.singletonList("group"));
        final SGroup group = mock(SGroup.class);
        when(group.getId()).thenReturn(456L);
        when(group.getName()).thenReturn("group");
        doReturn(group).when(identityService).getGroupByPath("group");

        // when
        final List<ImportError> importProfileMapping = profilesImporter.importProfileMapping(profileService, identityService, 123, exportedProfileMapping);

        // then
        assertThat(importProfileMapping).isEmpty();

        verify(profileService, times(1)).addGroupToProfile(123L, 456L, "group", null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void should_importProfileMapping_return_error_if_group_membership_do_not_exists() throws Exception {
        // given
        final ProfileMappingNode exportedProfileMapping = new ProfileMappingNode();
        exportedProfileMapping.setMemberships(Collections.singletonList(new MembershipNode("group", "role")));
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
        final ProfileMappingNode exportedProfileMapping = new ProfileMappingNode();
        exportedProfileMapping.setMemberships(Collections.singletonList(new MembershipNode("group", "role")));
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
        final ProfileMappingNode exportedProfileMapping = new ProfileMappingNode();
        exportedProfileMapping.setMemberships(Collections.singletonList(new MembershipNode("group", "role")));
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
        final ProfileMappingNode exportedProfileMapping = new ProfileMappingNode();
        exportedProfileMapping.setMemberships(Collections.singletonList(new MembershipNode("group", "role")));
        final SRole role = mock(SRole.class);
        when(role.getId()).thenReturn(456L);
        when(role.getName()).thenReturn("role");
        doReturn(role).when(identityService).getRoleByName("role");
        final SGroup group = mock(SGroup.class);
        when(group.getId()).thenReturn(789L);
        when(group.getName()).thenReturn("group");
        doReturn(group).when(identityService).getGroupByPath("group");

        // when
        final List<ImportError> importProfileMapping = profilesImporter.importProfileMapping(profileService, identityService, 123, exportedProfileMapping);

        // then
        assertThat(importProfileMapping).isEmpty();

        verify(profileService, times(1)).addRoleAndGroupToProfile(123L, 456L, 789L, "role", "group", null);
    }
}
