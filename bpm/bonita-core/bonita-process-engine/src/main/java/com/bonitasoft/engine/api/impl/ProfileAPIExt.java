/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.impl.ProfileAPIImpl;
import org.bonitasoft.engine.api.impl.SessionInfos;
import org.bonitasoft.engine.api.impl.transaction.CustomTransactions;
import org.bonitasoft.engine.api.impl.transaction.profile.DeleteAllExistingProfiles;
import org.bonitasoft.engine.api.impl.transaction.profile.GetProfile;
import org.bonitasoft.engine.api.impl.transaction.profile.GetProfileByName;
import org.bonitasoft.engine.api.impl.transaction.profile.ImportProfiles;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
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
import org.bonitasoft.engine.profile.exception.profile.SProfileCreationException;
import org.bonitasoft.engine.profile.exception.profile.SProfileNotFoundException;
import org.bonitasoft.engine.profile.impl.ExportedParentProfileEntry;
import org.bonitasoft.engine.profile.impl.ExportedProfile;
import org.bonitasoft.engine.profile.impl.ExportedProfileEntry;
import org.bonitasoft.engine.profile.impl.ExportedProfileMapping;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.model.SProfileEntry;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.xml.Parser;
import org.bonitasoft.engine.xml.SValidationException;
import org.bonitasoft.engine.xml.SXMLParseException;
import org.bonitasoft.engine.xml.XMLWriter;

import com.bonitasoft.engine.api.ProfileAPI;
import com.bonitasoft.engine.api.impl.transaction.profile.CreateProfile;
import com.bonitasoft.engine.api.impl.transaction.profile.CreateProfileEntry;
import com.bonitasoft.engine.api.impl.transaction.profile.DeleteProfile;
import com.bonitasoft.engine.api.impl.transaction.profile.DeleteProfileEntry;
import com.bonitasoft.engine.api.impl.transaction.profile.ExportAllProfiles;
import com.bonitasoft.engine.api.impl.transaction.profile.ExportProfilesSpecified;
import com.bonitasoft.engine.api.impl.transaction.profile.ImportAndHandleSameNameProfiles;
import com.bonitasoft.engine.api.impl.transaction.profile.ImportProfileMember;
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

        final GetProfile getProfile = new GetProfile(profileService, id);
        try {
            getProfile.execute();
            final SProfile sProfile = getProfile.getResult();
            if (!sProfile.isDefault()) {
                final DeleteProfile deleteProfileTransaction = new DeleteProfile(profileService, id);
                deleteProfileTransaction.execute();
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
    @CustomTransactions
    public List<String> importProfilesUsingSpecifiedPolicy(final byte[] xmlContent, final ImportPolicy policy) throws ExecutionException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.CUSTOM_PROFILES);

        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProfileService profileService = tenantAccessor.getProfileService();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        List<String> warnings;

        final Parser parser = tenantAccessor.getProfileParser();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final List<ExportedProfile> profiles = getProfilesFromXML(new String(xmlContent), parser);

        switch (policy) {
            case DELETE_EXISTING:
                warnings = importWithDeleteExisting(transactionExecutor, profileService, identityService, profiles);
                break;
            case REPLACE_DUPLICATES:
                warnings = importInCaseReplaceOnDuplicate(transactionExecutor, profileService, identityService, profiles);
                break;
            default:
                warnings = importInCaseDuplicate(transactionExecutor, profileService, identityService, profiles, policy);
                break;
        }
        return warnings;
    }

    private List<String> importInCaseReplaceOnDuplicate(final TransactionExecutor transactionExecutor, final ProfileService profileService,
            final IdentityService identityService, final List<ExportedProfile> exportedProfiles) throws ExecutionException {
        final List<String> warnings = new ArrayList<String>();
        final long importerId = SessionInfos.getUserIdFromSession();
        for (final ExportedProfile exportedProfile : exportedProfiles) {
            if (exportedProfile.getName() != null && !"".equals(exportedProfile.getName())) {
                final GetProfileByName getProfileByName = new GetProfileByName(profileService, exportedProfile.getName());
                try {
                    transactionExecutor.execute(getProfileByName);
                } catch (final SBonitaException e) {
                    throw new ExecutionException(e);
                }
                // delete duplicated ones
                final SProfile existingProfile = getProfileByName.getResult();
                if (existingProfile != null) {
                    final DeleteProfile deleteProfileTransaction = new DeleteProfile(profileService, existingProfile.getId());
                    try {
                        transactionExecutor.execute(deleteProfileTransaction);
                    } catch (final SBonitaException e) {
                        throw new ExecutionException("Can't remove profile with id " + existingProfile.getId(), e);
                    }
                }
                // insert profile
                final CreateProfile createProfile = new CreateProfile(profileService, getProfileCreator(exportedProfile),
                        exportedProfile.isDefault(), importerId);
                try {
                    transactionExecutor.execute(createProfile);
                } catch (final SBonitaException e) {
                    throw new ExecutionException("Error happend when create a new profile with name " + exportedProfile.getName(), e);
                }
                final SProfile newProfile = createProfile.getResult();

                // insert profileEntries
                final List<ExportedParentProfileEntry> parentProfileEntries = exportedProfile.getParentProfileEntries();
                final long profileId = newProfile.getId();
                for (final ExportedParentProfileEntry parentProfileEntry : parentProfileEntries) {
                    final ProfileEntryCreator parentProfileEntryCreator = getProfileEntryCreator(parentProfileEntry, profileId, 0);
                    final CreateProfileEntry createProfileEntry = new CreateProfileEntry(profileService, parentProfileEntryCreator);
                    try {
                        transactionExecutor.execute(createProfileEntry);
                    } catch (final SBonitaException e) {
                        throw new ExecutionException("Error happend when create a new profileEntry with name " + parentProfileEntry.getName(), e);
                    }

                    final SProfileEntry parentEntry = createProfileEntry.getResult();
                    final long parentProfileEntryId = parentEntry.getId();
                    final List<ExportedProfileEntry> childrenProEn = parentProfileEntry.getChildProfileEntries();
                    if (childrenProEn != null && childrenProEn.size() > 0) {
                        for (final ExportedProfileEntry childProfileEntry : childrenProEn) {
                            final ProfileEntryCreator childProfileEntryCreator = getProfileEntryCreator(childProfileEntry, profileId, parentProfileEntryId);
                            final CreateProfileEntry addProfileEntryTransactionc = new CreateProfileEntry(profileService,
                                    childProfileEntryCreator);
                            try {
                                transactionExecutor.execute(addProfileEntryTransactionc);
                            } catch (final SBonitaException e) {
                                throw new ExecutionException("Error happend when create a new profileEntry with name " + childProfileEntry.getName(), e);
                            }
                        }
                    }
                }

                // insert profileMapping
                final ExportedProfileMapping profileMapp = exportedProfile.getProfileMapping();
                final ImportProfileMember importpm = new ImportProfileMember(profileService, identityService, profileMapp, profileId);
                try {
                    transactionExecutor.execute(importpm);
                } catch (final SBonitaException e) {
                    throw new ExecutionException("Error happend when insert a new profile mapping related with profile " + exportedProfile.getName(), e);
                }
                final List<String> warns = importpm.getResult();
                if (warns != null && !warns.isEmpty()) {
                    warnings.addAll(warns);
                }
            }
        }
        return warnings;
    }

    private List<String> importInCaseDuplicate(final TransactionExecutor transactionExecutor, final ProfileService profileService,
            final IdentityService identityService, final List<ExportedProfile> profiles, final ImportPolicy policy) throws ExecutionException {
        final ImportAndHandleSameNameProfiles importAndHandler = new ImportAndHandleSameNameProfiles(profileService, identityService, profiles, policy,
                SessionInfos.getUserIdFromSession());
        try {
            transactionExecutor.execute(importAndHandler);
        } catch (final SBonitaException e) {
            throw new ExecutionException(e);
        }
        return importAndHandler.getResult();
    }

    private List<String> importWithDeleteExisting(final TransactionExecutor transactionExecutor, final ProfileService profileService,
            final IdentityService identityService, final List<ExportedProfile> profiles) throws ExecutionException {
        final DeleteAllExistingProfiles deleteAll = new DeleteAllExistingProfiles(profileService);
        try {
            transactionExecutor.execute(deleteAll);
        } catch (final SBonitaException e) {
            throw new ExecutionException(e);
        }
        final ImportProfiles importProfiles = new ImportProfiles(profileService, identityService, profiles, SessionInfos.getUserIdFromSession());
        try {
            transactionExecutor.execute(importProfiles);
        } catch (final SBonitaException e) {
            throw new ExecutionException(e);
        }
        return importProfiles.getResult();
    }

    @SuppressWarnings("unchecked")
    private List<ExportedProfile> getProfilesFromXML(final String xmlContent, final Parser parser) throws ExecutionException {
        StringReader reader = new StringReader(xmlContent);
        try {
            parser.validate(reader);
            reader.close();
            reader = new StringReader(xmlContent);
            return (List<ExportedProfile>) parser.getObjectFromXML(reader);
        } catch (final IOException ioe) {
            throw new ExecutionException(ioe);
        } catch (final SValidationException e) {
            throw new ExecutionException(e);
        } catch (final SXMLParseException e) {
            throw new ExecutionException(e);
        } finally {
            reader.close();
        }
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

        final DeleteProfileEntry deleteProfileEntryTransaction = new DeleteProfileEntry(profileService, id);
        try {
            deleteProfileEntryTransaction.execute();
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

    private ProfileCreator getProfileCreator(final ExportedProfile exportedProfile) {
        final ProfileCreator creator = new ProfileCreator(exportedProfile.getName());
        creator.setDescription(exportedProfile.getDescription());
        return creator;
    }

    private ProfileEntryCreator getProfileEntryCreator(final ExportedParentProfileEntry exportedProfileEntry, final long profileId, final long parentId) {
        final ProfileEntryCreator creator = new ProfileEntryCreator(exportedProfileEntry.getName(), profileId);
        creator.setParentId(parentId);
        creator.setDescription(exportedProfileEntry.getDescription());
        creator.setIndex(exportedProfileEntry.getIndex());
        creator.setPage(exportedProfileEntry.getPage());
        creator.setType(exportedProfileEntry.getType());
        return creator;
    }

    private ProfileEntryCreator getProfileEntryCreator(final ExportedProfileEntry exportedProfileEntry, final long profileId, final long parentId) {
        final ProfileEntryCreator creator = new ProfileEntryCreator(exportedProfileEntry.getName(), profileId);
        creator.setParentId(parentId);
        creator.setDescription(exportedProfileEntry.getDescription());
        creator.setIndex(exportedProfileEntry.getIndex());
        creator.setPage(exportedProfileEntry.getPage());
        creator.setType(exportedProfileEntry.getType());
        return creator;
    }

}
