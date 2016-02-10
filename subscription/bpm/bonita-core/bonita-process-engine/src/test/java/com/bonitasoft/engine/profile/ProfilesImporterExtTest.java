/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportError.Type;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.profile.ImportPolicy;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.impl.ExportedParentProfileEntry;
import org.bonitasoft.engine.profile.impl.ExportedProfile;
import org.bonitasoft.engine.profile.impl.ExportedProfileEntry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProfilesImporterExtTest {

    private final List<ExportedProfile> exportedProfiles = null;

    @Mock
    private IdentityService identityService;

    @Mock
    private PageService pageService;

    private final ImportPolicy policy = ImportPolicy.REPLACE_DUPLICATES;

    @Mock
    private ProfileService profileService;

    private ProfilesImporterExt profilesImporterExt;

    @Before
    public void before() {
        profilesImporterExt = new ProfilesImporterExt(profileService, identityService, pageService, exportedProfiles, policy);
    }

    @Test
    public void should_checkChildProfileEntryForError_return_null_if_custom_page_exists() throws Exception {
        final ExportedProfileEntry childProfileEntry = createChild("defaultPage", false);
        when(pageService.getPageByName("customPage")).thenReturn(mock(SPage.class));

        final ImportError error = profilesImporterExt.checkProfileEntryForCustomPageError(childProfileEntry);

        assertThat(error).isNull();
    }

    @Test
    public void should_checkChildProfileEntryForError_return_null_if_default_page() {
        final ExportedProfileEntry childProfileEntry = createChild("defaultPage", false);

        final ImportError error = profilesImporterExt.checkProfileEntryForCustomPageError(childProfileEntry);

        assertThat(error).isNull();
    }

    @Test
    public void should_checkChildProfileEntryForError_return_error_if_custom_page_do_not_exists() {
        final ExportedProfileEntry childProfileEntry = createChild("customPage", true);

        final ImportError error = profilesImporterExt.checkProfileEntryForCustomPageError(childProfileEntry);

        assertThat(error).isEqualTo(new ImportError("customPage", Type.PAGE));
    }

    @Test
    public void should_checkParentProfileEntryForError_return_null_if_custom_page_exists() throws Exception {
        final ExportedParentProfileEntry parentProfileEntry = createParent("customPage", true);
        when(pageService.getPageByName("customPage")).thenReturn(mock(SPage.class));

        final List<ImportError> error = profilesImporterExt.checkParentProfileEntryForCustomPageErrors(parentProfileEntry);

        assertThat(error).isNull();
    }

    @Test
    public void should_checkParentProfileEntryForError_return_erro_if_custom_page_do_not_exists() {
        final ExportedParentProfileEntry parentProfileEntry = createParent("customPage", true);

        final List<ImportError> error = profilesImporterExt.checkParentProfileEntryForCustomPageErrors(parentProfileEntry);

        assertThat(error.get(0)).isEqualTo(new ImportError("customPage", Type.PAGE));
    }

    private ExportedParentProfileEntry createParent(final String pageName, final boolean custom) {
        final ExportedParentProfileEntry parentProfileEntry = new ExportedParentProfileEntry("Mine");
        parentProfileEntry.setPage(pageName);
        parentProfileEntry.setCustom(custom);
        return parentProfileEntry;
    }

    private ExportedProfileEntry createChild(final String pageName, final boolean custom) {
        final ExportedProfileEntry parentProfileEntry = new ExportedProfileEntry("Mine");
        parentProfileEntry.setPage(pageName);
        parentProfileEntry.setCustom(custom);
        return parentProfileEntry;
    }

    @Test
    public void should_checkParentProfileEntryForError_return_null_if_children_are_ok() throws Exception {
        final ExportedParentProfileEntry parentProfileEntry = new ExportedParentProfileEntry("Mine");
        parentProfileEntry.setChildProfileEntries(Arrays.asList(createChild("p1", true), createChild("p2", false)));
        when(pageService.getPageByName("p1")).thenReturn(mock(SPage.class));
        when(pageService.getPageByName("p2")).thenReturn(mock(SPage.class));

        final List<ImportError> error = profilesImporterExt.checkParentProfileEntryForCustomPageErrors(parentProfileEntry);

        assertThat(error).isNull();
    }

    @Test
    public void should_checkParentProfileEntryForError_return_null_if_children_one_is_ok() throws Exception {
        final ExportedParentProfileEntry parentProfileEntry = new ExportedParentProfileEntry("Mine");
        parentProfileEntry.setChildProfileEntries(Arrays.asList(createChild("p1", true), createChild("p2", true)));
        when(pageService.getPageByName("p1")).thenReturn(mock(SPage.class));
        when(pageService.getPageByName("p2")).thenReturn(mock(SPage.class));

        final List<ImportError> error = profilesImporterExt.checkParentProfileEntryForCustomPageErrors(parentProfileEntry);

        assertThat(error).isNull();
    }

    @Test
    public void should_checkParentProfileEntryForError_return_errors_if_children_all_are_error() {
        final ExportedParentProfileEntry parentProfileEntry = new ExportedParentProfileEntry("Mine");
        parentProfileEntry.setChildProfileEntries(Arrays.asList(createChild("p1", true), createChild("p2", true)));

        final List<ImportError> error = profilesImporterExt.checkParentProfileEntryForCustomPageErrors(parentProfileEntry);

        assertThat(error.size()).isEqualTo(2);
    }

}
