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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportError.Type;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.profile.ImportPolicy;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.ProfilesImporter;
import org.bonitasoft.engine.profile.exception.profile.SProfileCreationException;
import org.bonitasoft.engine.profile.exception.profile.SProfileUpdateException;
import org.bonitasoft.engine.profile.exception.profileentry.SProfileEntryCreationException;
import org.bonitasoft.engine.profile.exception.profileentry.SProfileEntryDeletionException;
import org.bonitasoft.engine.profile.exception.profilemember.SProfileMemberDeletionException;
import org.bonitasoft.engine.profile.impl.ExportedParentProfileEntry;
import org.bonitasoft.engine.profile.impl.ExportedProfile;
import org.bonitasoft.engine.profile.impl.ExportedProfileEntry;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.model.SProfileEntry;

import com.bonitasoft.engine.page.PageService;
import com.bonitasoft.engine.service.impl.LicenseChecker;
import com.bonitasoft.manager.Features;

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

    @Override
    protected List<ImportError> importProfileEntries(final ProfileService profileService, final List<ExportedParentProfileEntry> parentProfileEntries,
            final long profileId)
            throws SProfileEntryCreationException {
        final ArrayList<ImportError> errors = new ArrayList<ImportError>();
        for (final ExportedParentProfileEntry parentProfileEntry : parentProfileEntries) {
            if (parentProfileEntry.hasErrors()) {
                errors.addAll(parentProfileEntry.getErrors());
                continue;
            }
            final List<ImportError> checkParentProfileEntryForError = checkParentProfileEntryForCustomPageErrors(parentProfileEntry);
            if (checkParentProfileEntryForError != null)
            {
                errors.addAll(checkParentProfileEntryForError);
                continue;
            }

            final SProfileEntry parentEntry = profileService.createProfileEntry(createProfileEntry(parentProfileEntry, profileId, 0));
            final long parentProfileEntryId = parentEntry.getId();
            final List<ExportedProfileEntry> childrenProfileEntry = parentProfileEntry.getChildProfileEntries();
            if (childrenProfileEntry != null && childrenProfileEntry.size() > 0) {
                for (final ExportedProfileEntry childProfileEntry : childrenProfileEntry) {
                    if (childProfileEntry.hasError()) {
                        errors.add(childProfileEntry.getError());
                        continue;
                    }
                    profileService.createProfileEntry(createProfileEntry(childProfileEntry, profileId, parentProfileEntryId));
                }
            }
        }
        return errors;
    }

    protected ImportError checkProfileEntryForCustomPageError(final ExportedProfileEntry profileEntry) {
        if (profileEntry.hasError()) {
            return profileEntry.getError();
        }
        final String page = profileEntry.getPage();
        if (page != null && !page.isEmpty()) { // there is a page
            if (profileEntry.isCustom()) { // it's a custom page
                try {
                    if (pageService.getPageByName(profileEntry.getPage()) == null) {
                        return new ImportError(profileEntry.getPage(), Type.PAGE);
                    }
                } catch (final SBonitaReadException e) {
                    return new ImportError(profileEntry.getPage(), Type.PAGE);
                }
            }
        }
        return null;
    }

    protected List<ImportError> checkParentProfileEntryForCustomPageErrors(final ExportedParentProfileEntry parentProfileEntry) {
        if (parentProfileEntry.hasErrors()) {
            return parentProfileEntry.getErrors();
        }
        final List<ExportedProfileEntry> childProfileEntries = parentProfileEntry.getChildProfileEntries();
        if (childProfileEntries == null || childProfileEntries.isEmpty()) {// no children
            final ImportError error = checkProfileEntryForCustomPageError(parentProfileEntry);
            if (error != null) {
                return Arrays.asList(error);
            }
        } else {
            final ArrayList<ImportError> errors = new ArrayList<ImportError>();
            for (final ExportedProfileEntry childProfileEntry : childProfileEntries) {
                final ImportError customPageEntryError = checkProfileEntryForCustomPageError(childProfileEntry);
                if (customPageEntryError != null) {
                    errors.add(customPageEntryError);
                }
            }
            if (!errors.isEmpty()) {
                // one or more custom page is missing on children
                return errors;
            }
        }
        return null;
    }

    @Override
    protected SProfile importTheProfile(final long importerId, final ExportedProfile exportedProfile, final SProfile existingProfile)
            throws ExecutionException,
            SProfileEntryDeletionException, SProfileMemberDeletionException, SProfileUpdateException, SProfileCreationException {
        if (checkProfileForRequiredFeatures(exportedProfile))
        {
            return super.importTheProfile(importerId, exportedProfile, existingProfile);
        }
        // some feature are missing
        // skip status
        return null;
    }

    /**
     * 
     * @param exportedProfile
     * @return
     *         true when needed features are present
     *         false otherwise
     */
    private boolean checkProfileForRequiredFeatures(final ExportedProfile exportedProfile) {
        final Set<String> featuresToCheck = new HashSet<String>();
        if (!exportedProfile.isDefault()) {
            featuresToCheck.add(Features.CUSTOM_PROFILES);
        }
        if (exportedProfile.hasCustomPages()) {
            featuresToCheck.add(Features.CUSTOM_PAGE);
        }
        for (final String feature : featuresToCheck) {
            try {
                LicenseChecker.getInstance().checkLicenceAndFeature(feature);
            } catch (final IllegalStateException e) {
                return false;
            }
        }
        return true;
    }

}
