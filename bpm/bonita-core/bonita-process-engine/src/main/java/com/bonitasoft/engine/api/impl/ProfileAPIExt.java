/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.api.impl.ProfileAPIImpl;
import org.bonitasoft.engine.api.impl.SessionInfos;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.profile.ProfileEntry;
import org.bonitasoft.engine.profile.ProfileEntryNotFoundException;
import org.bonitasoft.engine.profile.ProfileSearchDescriptor;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.ProfilesImporter;
import org.bonitasoft.engine.profile.exception.profile.SProfileCreationException;
import org.bonitasoft.engine.profile.exception.profile.SProfileNotFoundException;
import org.bonitasoft.engine.profile.impl.ExportedProfile;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.model.SProfileEntry;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.xml.Parser;
import org.bonitasoft.engine.xml.XMLWriter;

import com.bonitasoft.engine.api.ProfileAPI;
import com.bonitasoft.engine.api.impl.transaction.profile.DeleteProfileEntry;
import com.bonitasoft.engine.api.impl.transaction.profile.ExportAllProfiles;
import com.bonitasoft.engine.api.impl.transaction.profile.ExportProfilesSpecified;
import com.bonitasoft.engine.api.impl.transaction.profile.UpdateProfile;
import com.bonitasoft.engine.api.impl.transaction.profile.UpdateProfileEntry;
import com.bonitasoft.engine.api.impl.transaction.profile.UpdateProfileEntryIndexOnInsert;
import com.bonitasoft.engine.profile.ImportPolicy;
import com.bonitasoft.engine.profile.ProfileCreator;
import com.bonitasoft.engine.profile.ProfileCreator.ProfileField;
import com.bonitasoft.engine.profile.ProfileEntryCreator;
import com.bonitasoft.engine.profile.ProfileEntryCreator.ProfileEntryField;
import com.bonitasoft.engine.profile.ProfileEntryUpdater;
import com.bonitasoft.engine.profile.ProfileEntryUpdater.ProfileEntryUpdateField;
import com.bonitasoft.engine.profile.ProfileUpdater;
import com.bonitasoft.engine.service.SPModelConvertor;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.LicenseChecker;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import com.bonitasoft.engine.service.impl.TenantServiceSingleton;
import com.bonitasoft.manager.Features;

/**
 * @author Celine Souchet
 */
@AvailableWhenTenantIsPaused
public class ProfileAPIExt extends ProfileAPIImpl implements ProfileAPI {

    @Override
    public TenantServiceAccessor getTenantAccessor() {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    @Override
    public Profile createProfile(final ProfileCreator creator) throws CreationException, AlreadyExistsException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.CUSTOM_PROFILES);
        final Map<ProfileField, Serializable> fields = creator.getFields();
        final String name = (String) fields.get(ProfileField.NAME);
        if (name == null || name.isEmpty()) {
            throw new CreationException("Name is mandatory.");
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProfileService profileService = tenantAccessor.getProfileService();
        try {
            profileService.getProfileByName(name);
            throw new AlreadyExistsException("A profile with name \"" + name + "\" already exists");
        } catch (final SProfileNotFoundException sProfileNotFoundException) {
            try {
                final SProfile profile = profileService.createProfile(SPModelConvertor.constructSProfile(creator, false, SessionInfos.getUserIdFromSession()));
                return SPModelConvertor.toProfile(profile);
            } catch (final SProfileCreationException e) {
                throw new CreationException(e);
            }
        }
    }

    @Override
    public Profile createProfile(final String name, final String description, final String iconPath) throws CreationException {
        final ProfileCreator creator = new ProfileCreator(name);
        creator.setDescription(description);
        creator.setIconPath(iconPath);
        return createProfile(creator);
    }

    @Override
    public void deleteProfile(final long id) throws DeletionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProfileService profileService = tenantAccessor.getProfileService();

        try {
            final SProfile sProfile = profileService.getProfile(id);
            if (!sProfile.isDefault()) {
                profileService.deleteProfile(sProfile);
            } else {
                throw new DeletionException("Can't delete a default profile. Profile id = <" + id + ">, name = <" + sProfile.getName() + ">");
            }
        } catch (final SBonitaException e) {
            throw new DeletionException(e);
        }
    }

    @Override
    public byte[] exportAllProfiles() throws ExecutionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProfileService profileService = tenantAccessor.getProfileService();
        final IdentityService identityservice = tenantAccessor.getIdentityService();
        final XMLWriter writer = tenantAccessor.getXMLWriter();

        final ExportAllProfiles exportProfiles = new ExportAllProfiles(profileService, identityservice, writer);
        try {
            exportProfiles.execute();
        } catch (final SBonitaException e) {
            throw new ExecutionException(e);
        }
        final String profileXml = exportProfiles.getResult();
        return profileXml.getBytes();
    }

    @Override
    public byte[] exportProfilesWithIdsSpecified(final long[] profileIds) throws ExecutionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProfileService profileService = tenantAccessor.getProfileService();
        final IdentityService identityservice = tenantAccessor.getIdentityService();
        if (profileIds.length > 0) {
            final List<Long> profileIDs = new ArrayList<Long>(profileIds.length);
            for (int i = 0; i < profileIds.length; i++) {
                profileIDs.add(profileIds[i]);
            }

            final XMLWriter writer = tenantAccessor.getXMLWriter();
            final ExportProfilesSpecified exportProfiles = new ExportProfilesSpecified(profileService, identityservice, writer, profileIDs);
            try {
                exportProfiles.execute();
            } catch (final SBonitaException e) {
                throw new ExecutionException(e);
            }
            final String profileXml = exportProfiles.getResult();
            return profileXml.getBytes();
        }
        return new byte[] {};
    }

    @Override
    public List<ImportStatus> importProfiles(final byte[] xmlContent, final ImportPolicy policy) throws ExecutionException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.CUSTOM_PROFILES);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProfileService profileService = tenantAccessor.getProfileService();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final Parser parser = tenantAccessor.getProfileParser();
        final List<ExportedProfile> profiles = ProfilesImporter.getProfilesFromXML(new String(xmlContent), parser);
        return new ProfilesImporter(profileService, identityService, profiles, org.bonitasoft.engine.profile.ImportPolicy.valueOf(policy.name()))
                .importProfiles(SessionInfos.getUserIdFromSession());

    }

    @Override
    @Deprecated
    public List<String> importProfilesUsingSpecifiedPolicy(final byte[] xmlContent, final ImportPolicy policy) throws ExecutionException {
        return ProfilesImporter.toWarnings(importProfiles(xmlContent, policy));
    }

    @Override
    public Profile updateProfile(final long id, final ProfileUpdater updateDescriptor) throws UpdateException, AlreadyExistsException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.CUSTOM_PROFILES);

        if (updateDescriptor == null || updateDescriptor.getFields().isEmpty()) {
            throw new UpdateException("The update descriptor does not contain field updates");
        }
        final Serializable updatedName = updateDescriptor.getFields().get(ProfileUpdater.ProfileField.NAME);
        if (updatedName != null) {
            SearchResult<Profile> searchProfiles;
            try {
                searchProfiles = searchProfiles(new SearchOptionsBuilder(0, 1).differentFrom(ProfileSearchDescriptor.ID, id)
                        .filter(ProfileSearchDescriptor.NAME, updatedName).done());
                if (searchProfiles.getCount() > 0) {
                    throw new AlreadyExistsException("A profile with the name '" + updatedName + "' already exists");
                }
            } catch (final SearchException e) {
                throw new UpdateException("Cannot check if a profile with the name '" + updatedName + "' already exists", e);
            }
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProfileService profileService = tenantAccessor.getProfileService();

        final UpdateProfile updateProfile = new UpdateProfile(profileService, id,
                updateDescriptor, SessionInfos.getUserIdFromSession());
        try {
            updateProfile.execute();
            return SPModelConvertor.toProfile(updateProfile.getResult());
        } catch (final SBonitaException e) {
            throw new UpdateException(e);
        }
    }

    @Override
    public ProfileEntry createProfileEntry(final ProfileEntryCreator creator) throws CreationException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.CUSTOM_PROFILES);

        final Map<ProfileEntryField, Serializable> fields = creator.getFields();
        final String type = (String) fields.get(ProfileEntryField.TYPE);
        final String page = (String) fields.get(ProfileEntryField.PAGE);
        if ("link".equalsIgnoreCase(type) && (page == null || "".equals(page))) {
            throw new CreationException("For a link, the page is mandatory.");
        }

        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProfileService profileService = tenantAccessor.getProfileService();

        SProfileEntry sProfileEntry;
        try {
            sProfileEntry = profileService.createProfileEntry(SPModelConvertor.constructSProfileEntry(creator));
        } catch (final SBonitaException e) {
            throw new CreationException(e);
        }

        final UpdateProfileEntryIndexOnInsert updateProfileEntryIndexTransaction = new UpdateProfileEntryIndexOnInsert(profileService, sProfileEntry);
        try {
            updateProfileEntryIndexTransaction.execute();
        } catch (final SBonitaException e) {
            throw new CreationException(e);
        }

        return SPModelConvertor.toProfileEntry(sProfileEntry);
    }

    @Override
    public ProfileEntry createProfileEntry(final String name, final String description, final long profileId, final String type) throws CreationException {
        final ProfileEntryCreator creator = new ProfileEntryCreator(name, profileId).setName(name).setDescription(description).setType(type);
        return createProfileEntry(creator);
    }

    @Override
    public ProfileEntry createProfileEntry(final String name, final String description, final long profileId, final String type, final String page)
            throws CreationException {
        final ProfileEntryCreator creator = new ProfileEntryCreator(name, profileId).setDescription(description).setType(type).setPage(page);
        return createProfileEntry(creator);
    }

    @Override
    public void deleteProfileEntry(final long id) throws DeletionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProfileService profileService = tenantAccessor.getProfileService();
        try {
            new DeleteProfileEntry(profileService, id).execute();
        } catch (final SBonitaException e) {
            throw new DeletionException(e);
        }
    }

    @Override
    public ProfileEntry updateProfileEntry(final long id, final ProfileEntryUpdater updateDescriptor) throws UpdateException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.CUSTOM_PROFILES);

        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProfileService profileService = tenantAccessor.getProfileService();

        SProfileEntry sProfileEntry;

        final UpdateProfileEntry updateProfileEntry = new UpdateProfileEntry(profileService, id, updateDescriptor);
        try {
            updateProfileEntry.execute();
            sProfileEntry = updateProfileEntry.getResult();
        } catch (final SBonitaException e) {
            throw new UpdateException(e);
        }

        final Map<ProfileEntryUpdateField, Serializable> fields = updateDescriptor.getFields();
        if (fields.get(ProfileEntryUpdateField.INDEX) != null) {
            final UpdateProfileEntryIndexOnInsert updateProfileEntryIndexTransaction = new UpdateProfileEntryIndexOnInsert(profileService, sProfileEntry);
            try {
                updateProfileEntryIndexTransaction.execute();
            } catch (final SBonitaException e) {
                throw new UpdateException(e);
            }
        }

        ProfileEntry updatedSProfileEntry = null;
        try {
            updatedSProfileEntry = getProfileEntry(id);
        } catch (final ProfileEntryNotFoundException e) {
            throw new UpdateException(e.getCause());
        }
        return updatedSProfileEntry;

    }

}
