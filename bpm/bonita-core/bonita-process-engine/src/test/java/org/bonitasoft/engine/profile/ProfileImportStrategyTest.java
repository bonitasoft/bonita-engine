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
