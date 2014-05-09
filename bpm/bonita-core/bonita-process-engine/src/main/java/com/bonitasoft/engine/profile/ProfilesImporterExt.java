/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportError.Type;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.profile.ImportPolicy;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.ProfilesImporter;
import org.bonitasoft.engine.profile.impl.ExportedParentProfileEntry;
import org.bonitasoft.engine.profile.impl.ExportedProfile;
import org.bonitasoft.engine.profile.impl.ExportedProfileEntry;

import com.bonitasoft.engine.page.PageService;

/**
 * @author Baptiste Mesta
 * 
 */
public class ProfilesImporterExt extends ProfilesImporter {

    private final PageService pageService;

    /**
     * @param profileService
     * @param identityService
     * @param exportedProfiles
     * @param policy
     */
    public ProfilesImporterExt(final ProfileService profileService, final IdentityService identityService, final PageService pageService,
            final List<ExportedProfile> exportedProfiles, final ImportPolicy policy) {
        super(profileService, identityService, exportedProfiles, policy);
        this.pageService = pageService;
    }

    protected ImportError checkChildProfileEntryForError(final ExportedProfileEntry childProfileEntry) {
        final String page = childProfileEntry.getPage();
        if (page != null && !page.isEmpty()) { // there is a page
            if (childProfileEntry.isCustom()) { // it's a custom page
                try {
                    if (pageService.getPageByName(childProfileEntry.getPage()) == null) {
                        return new ImportError(childProfileEntry.getPage(), Type.PAGE);
                    }
                } catch (final SBonitaReadException e) {
                    return new ImportError(childProfileEntry.getPage(), Type.PAGE);
                }
            }
        }
        return null;
    }

    protected List<ImportError> checkParentProfileEntryForError(final ExportedParentProfileEntry parentProfileEntry) {
        final List<ExportedProfileEntry> childProfileEntries = parentProfileEntry.getChildProfileEntries();
        if (childProfileEntries == null || childProfileEntries.isEmpty()) {// no children
            final ImportError error = checkChildProfileEntryForError(parentProfileEntry);
            if (error != null) {
                return Arrays.asList(error);
            }
        } else {
            final ArrayList<ImportError> errors = new ArrayList<ImportError>();
            for (final ExportedProfileEntry childProfileEntry : childProfileEntries) {
                final ImportError checkChildProfileEntryForError = checkChildProfileEntryForError(childProfileEntry);
                if (checkChildProfileEntryForError != null) {
                    errors.add(checkChildProfileEntryForError);
                }
            }
            // we do not import the parent only if no child have an existing page
            if (errors.size() == childProfileEntries.size()) {
                return errors;
            }
        }
        return null;
    }
}
