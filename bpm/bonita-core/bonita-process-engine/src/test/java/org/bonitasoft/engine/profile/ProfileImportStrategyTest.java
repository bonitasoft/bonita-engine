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
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.profile.impl.ExportedProfile;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProfileImportStrategyTest {

    @Mock
    private ProfileService profileService;

    @Mock
    private ExportedProfile defaultExportedProfile;

    @Mock
    private ExportedProfile customExportedProfile;

    @Before
    public void before() {
        when(defaultExportedProfile.isDefault()).thenReturn(true);
        when(customExportedProfile.isDefault()).thenReturn(false);
    }

    @Test
    public void FailOnDuplicate_stategy_allows_to_create_defaults_profile() {
        // given
        final FailOnDuplicateImportStrategy strategy = new FailOnDuplicateImportStrategy(profileService);

        // when then
        assertThat(strategy.canCreateProfileIfNotExists(defaultExportedProfile)).isTrue();
    }

    @Test
    public void FailOnDuplicate_stategy_allows_to_create_custom_profile() {
        // given
        final FailOnDuplicateImportStrategy strategy = new FailOnDuplicateImportStrategy(profileService);

        // when then
        assertThat(strategy.canCreateProfileIfNotExists(customExportedProfile)).isTrue();
    }

    @Test
    public void IgnoreDuplicate_stategy_allows_to_create_defaults_profile() {
        // given
        final IgnoreDuplicateImportStrategy strategy = new IgnoreDuplicateImportStrategy(profileService);

        // when then
        assertThat(strategy.canCreateProfileIfNotExists(defaultExportedProfile)).isTrue();
    }

    @Test
    public void IgnoreDuplicate_stategy_allows_to_create_custom_profile() {
        // given
        final IgnoreDuplicateImportStrategy strategy = new IgnoreDuplicateImportStrategy(profileService);

        // when then
        assertThat(strategy.canCreateProfileIfNotExists(customExportedProfile)).isTrue();
    }

    @Test
    public void DeleteExisting_stategy_allows_to_create_defaults_profile() {
        // given
        final DeleteExistingImportStrategy strategy = new DeleteExistingImportStrategy(profileService);

        // when then
        assertThat(strategy.canCreateProfileIfNotExists(defaultExportedProfile)).isTrue();
    }

    @Test
    public void DeleteExisting_stategy_allows_to_create_custom_profile() {
        // given
        final DeleteExistingImportStrategy strategy = new DeleteExistingImportStrategy(profileService);

        // when then
        assertThat(strategy.canCreateProfileIfNotExists(customExportedProfile)).isTrue();
    }

    @Test
    public void ReplaceDuplicate_stategy_refuse_to_create_defaults_profile() {
        // given
        final ReplaceDuplicateImportStrategy strategy = new ReplaceDuplicateImportStrategy(profileService);

        // when then
        assertThat(strategy.canCreateProfileIfNotExists(defaultExportedProfile)).isFalse();
    }

    @Test
    public void ReplaceDuplicate_stategy_allows_to_create_custom_profile() {
        // given
        final ReplaceDuplicateImportStrategy strategy = new ReplaceDuplicateImportStrategy(profileService);

        // when then
        assertThat(strategy.canCreateProfileIfNotExists(customExportedProfile)).isTrue();
    }
}
