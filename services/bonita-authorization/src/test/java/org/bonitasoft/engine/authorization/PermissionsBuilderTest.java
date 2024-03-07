/**
 * Copyright (C) 2021 Bonitasoft S.A.
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
package org.bonitasoft.engine.authorization;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.authorization.properties.CompoundPermissionsMapping;
import org.bonitasoft.engine.authorization.properties.CustomPermissionsMapping;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.session.model.SSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PermissionsBuilderTest {

    @Mock
    private CustomPermissionsMapping customPermissionsMapping;
    @Mock
    private CompoundPermissionsMapping compoundPermissionsMapping;
    @Mock
    private ApplicationService applicationService;

    private PermissionsBuilder permissionsBuilder;

    @Before
    public void setUp() {
        init();
    }

    private void init() {
        permissionsBuilder = spy(new PermissionsBuilder(applicationService, customPermissionsMapping,
                compoundPermissionsMapping));
    }

    @Test
    public void should_getPermissions_work_without_profile_nor_custom_user_permissions() throws Exception {
        doReturn(emptySet()).when(permissionsBuilder).getProfilesPermissions(anyList());
        doReturn(emptySet()).when(permissionsBuilder).getCustomUserPermissions(any());
        final SSession session = mock(SSession.class);
        doReturn("Jean-Claude").when(session).getUserName();

        final Set<String> permissions = permissionsBuilder.getPermissions(session.isTechnicalUser(),
                session.getProfiles(), session.getUserName());

        assertThat(permissions).as("No permissions other than the user should have been returned")
                .containsOnly("user|Jean-Claude");
    }

    @Test
    public void should_getPermissions_add_profile_and_custom_permissions() throws Exception {
        doReturn(emptySet()).when(permissionsBuilder).getProfilesPermissions(anyList());
        doReturn(emptySet()).when(permissionsBuilder).getCustomUserPermissions(anyString());
        final SSession session = mock(SSession.class);
        doReturn("Loïc").when(session).getUserName();

        permissionsBuilder.getPermissions(session.isTechnicalUser(), session.getProfiles(), session.getUserName());

        verify(permissionsBuilder).getProfilesPermissions(anyList());
        verify(permissionsBuilder).getCustomUserPermissions(anyString());
    }

    @Test
    public void should_getProfilesPermissions_return_all_types_of_permissions() throws Exception {
        // given
        doReturn(List.of("custompage_page1", "custompage_page2")).when(applicationService)
                .getAllPagesForProfile("profile1");
        doReturn(List.of("custompage_page1", "custompage_page3")).when(applicationService)
                .getAllPagesForProfile("profile2");
        doReturn(aSet("Perm1", "Perm2")).when(compoundPermissionsMapping).getPropertyAsSet(eq("custompage_page1"));
        doReturn(aSet("Perm1", "Perm21")).when(compoundPermissionsMapping).getPropertyAsSet(eq("custompage_page2"));
        doReturn(aSet("Perm31", "Perm32")).when(compoundPermissionsMapping).getPropertyAsSet(eq("custompage_page3"));
        doReturn(Set.of("customprofile_permission11")).when(customPermissionsMapping)
                .getPropertyAsSet("profile|profile1");
        doReturn(Set.of("customprofile_permission31")).when(customPermissionsMapping)
                .getPropertyAsSet("user|Jean-Claude");
        doReturn(Set.of("customprofile_permission21", "customprofile_permission22")).when(customPermissionsMapping)
                .getPropertyAsSet("profile|profile2");
        doReturn(aSet("Perm41", "Perm42")).when(compoundPermissionsMapping)
                .getPropertyAsSet(eq("customprofile_permission22"));

        final SSession session = mock(SSession.class);
        final List<String> myProfiles = List.of("profile1", "profile2");
        doReturn(myProfiles).when(session).getProfiles();
        doReturn("Jean-Claude").when(session).getUserName();

        // when
        final Set<String> permissions = permissionsBuilder.getPermissions(session.isTechnicalUser(),
                session.getProfiles(), session.getUserName());

        // then
        assertThat(permissions).containsExactlyInAnyOrder("Perm1", "Perm2", "Perm21", "Perm31", "Perm32",
                "customprofile_permission11",
                "customprofile_permission21", "Perm41", "Perm42", "profile|profile1", "profile|profile2",
                "customprofile_permission31",
                "user|Jean-Claude");
    }

    @Test
    public void should_getCustomPermissions_work_with_compound_permissions() {
        doReturn(aSet("Perm1", "Perm2", "taskListing")).when(customPermissionsMapping).getPropertyAsSet("user|myUser");
        doReturn(aSet("Perm3", "Perm4")).when(compoundPermissionsMapping).getPropertyAsSet("taskListing");

        final Set<String> permissions = permissionsBuilder.getCustomPermissions("user", "myUser");

        assertThat(permissions).containsOnly("Perm1", "Perm2", "Perm3", "Perm4");
    }

    @Test
    public void getPermissions_should_return_empty_list_for_technical_user() throws Exception {
        // given:
        final SSession session = mock(SSession.class);
        doReturn(true).when(session).isTechnicalUser();

        // when:
        final Set<String> permissions = permissionsBuilder.getPermissions(session.isTechnicalUser(),
                session.getProfiles(), session.getUserName());

        // then:
        assertThat(permissions).isEmpty();
    }

    private Set<String> aSet(String... elements) {
        return new HashSet<>(asList(elements));
    }
}
