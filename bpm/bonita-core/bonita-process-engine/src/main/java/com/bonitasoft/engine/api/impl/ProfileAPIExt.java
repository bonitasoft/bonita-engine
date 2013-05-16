/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
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
import org.bonitasoft.engine.api.impl.transaction.profile.DeleteAllExistingProfiles;
import org.bonitasoft.engine.api.impl.transaction.profile.GetProfileByName;
import org.bonitasoft.engine.api.impl.transaction.profile.GetProfileEntry;
import org.bonitasoft.engine.api.impl.transaction.profile.ImportProfiles;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.profile.ExportedParentProfileEntry;
import org.bonitasoft.engine.profile.ExportedProfile;
import org.bonitasoft.engine.profile.ExportedProfileEntry;
import org.bonitasoft.engine.profile.ExportedProfileMapping;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.builder.SProfileBuilder;
import org.bonitasoft.engine.profile.builder.SProfileBuilderAccessor;
import org.bonitasoft.engine.profile.builder.SProfileEntryBuilder;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.model.SProfileEntry;
import org.bonitasoft.engine.service.ModelConvertor;
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
import com.bonitasoft.engine.api.impl.transaction.profile.UpdateProfileEntryIndexOnDelete;
import com.bonitasoft.engine.api.impl.transaction.profile.UpdateProfileEntryIndexOnInsert;
import com.bonitasoft.engine.bpm.model.ProfileEntryUpdateDescriptor;
import com.bonitasoft.engine.bpm.model.ProfileEntryUpdateDescriptor.ProfileEntryField;
import com.bonitasoft.engine.bpm.model.ProfileUpdateDescriptor;
import com.bonitasoft.engine.exception.profile.ProfileExportException;
import com.bonitasoft.engine.exception.profile.ProfileImportException;
import com.bonitasoft.engine.profile.ImportPolicy;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.LicenseChecker;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import com.bonitasoft.engine.service.impl.TenantServiceSingleton;
import com.bonitasoft.manager.Features;

/**
 * @author Celine Souchet
 */
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
    public Map<String, Serializable> createProfile(final String profileName, final String profileDescription, final String profileIconPath)
            throws CreationException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.CUSTOM_PROFILES);

        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProfileService profileService = tenantAccessor.getProfileService();
        final SProfileBuilder profileBuilder = profileService.getSProfileBuilderAccessor().getSProfileBuilder();

        SProfile profile;

        profileBuilder.createNewInstance(profileName);
        profileBuilder.setDescription(profileDescription);
        profileBuilder.setIconPath(profileIconPath);

        final CreateProfile addProfileTransaction = new CreateProfile(profileService, profileBuilder.done());

        try {
            transactionExecutor.execute(addProfileTransaction);
            profile = addProfileTransaction.getResult();
        } catch (final SBonitaException e) {
            throw new CreationException(e);
        }

        return ProfileUtils.profileToMap(ModelConvertor.toProfile(profile));
    }

    @Override
    public void deleteProfile(final long id) throws DeletionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProfileService profileService = tenantAccessor.getProfileService();

        final DeleteProfile deleteProfileTransaction = new DeleteProfile(profileService, id);
        try {
            transactionExecutor.execute(deleteProfileTransaction);
        } catch (final SBonitaException e) {
            throw new DeletionException(e);
        }
    }

    @Override
    public byte[] exportAllProfiles() throws ProfileExportException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProfileService profileService = tenantAccessor.getProfileService();
        final IdentityService identityservice = tenantAccessor.getIdentityService();
        final XMLWriter writer = tenantAccessor.getXMLWriter();

        final ExportAllProfiles exportProfiles = new ExportAllProfiles(profileService, identityservice, writer);
        try {
            transactionExecutor.execute(exportProfiles);
        } catch (final SBonitaException e) {
            throw new ProfileExportException(e);
        }
        final String profileXml = exportProfiles.getResult();
        return profileXml.getBytes();
    }

    @Override
    public byte[] exportProfilesWithIdsSpecified(final long[] profileIds) throws ProfileExportException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
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
                transactionExecutor.execute(exportProfiles);
            } catch (final SBonitaException e) {
                throw new ProfileExportException(e);
            }
            final String profileXml = exportProfiles.getResult();
            return profileXml.getBytes();
        } else {
            return null;
        }
    }

    @Override
    public List<String> importProfilesUsingSpecifiedPolicy(final byte[] xmlContent, final ImportPolicy policy) throws ProfileImportException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.CUSTOM_PROFILES);

        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProfileService profileService = tenantAccessor.getProfileService();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        List<String> warnings;

        final Parser parser = tenantAccessor.getProfileParser();
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
            final IdentityService identityService, final List<ExportedProfile> profiles) throws ProfileImportException {
        final List<String> warnings = new ArrayList<String>();
        final SProfileBuilderAccessor builders = profileService.getSProfileBuilderAccessor();
        final SProfileBuilder profileBuilder = builders.getSProfileBuilder();
        final SProfileEntryBuilder proEntryBuilder = builders.getSProfileEntryBuilder();
        for (final ExportedProfile profile : profiles) {

            // insert profile
            if (profile.getName() != null && !"".equals(profile.getName())) {
                final GetProfileByName getProfileByName = new GetProfileByName(profileService, profile.getName());
                try {
                    transactionExecutor.execute(getProfileByName);
                } catch (final SBonitaException e) {
                    throw new ProfileImportException(e);
                }
                // delete duplicated ones
                final SProfile existingProfile = getProfileByName.getResult();
                if (existingProfile != null) {
                    final DeleteProfile deleteProfileTransaction = new DeleteProfile(profileService, existingProfile.getId());
                    try {
                        transactionExecutor.execute(deleteProfileTransaction);
                    } catch (final SBonitaException e) {
                        throw new ProfileImportException("Can't remove profile with id " + existingProfile.getId(), e);
                    }
                }
                final SProfile sProfile = profileBuilder.createNewInstance(profile.getName()).setDescription(profile.getDescription())
                        .setIconPath(profile.getIconPath()).done();
                final CreateProfile createProfile = new CreateProfile(profileService, sProfile);
                try {
                    transactionExecutor.execute(createProfile);
                } catch (final SBonitaException e) {
                    throw new ProfileImportException("Error happend when create a new profile with name " + profile.getName(), e);
                }
                final SProfile newProfile = createProfile.getResult();

                // insert profileEntries
                final List<ExportedParentProfileEntry> parentProfileEntries = profile.getParentProfileEntries();
                for (final ExportedParentProfileEntry parentprofileEntry : parentProfileEntries) {
                    final SProfileEntry sproEntry = proEntryBuilder.createNewInstance(parentprofileEntry.getName(), newProfile.getId())
                            .setDescription(parentprofileEntry.getDescription()).setIndex(parentprofileEntry.getIndex()).setPage(parentprofileEntry.getPage())
                            .setParentId(0).setType(parentprofileEntry.getType()).done();
                    final CreateProfileEntry addProfileEntryTransaction = new CreateProfileEntry(profileService, sproEntry);
                    try {
                        transactionExecutor.execute(addProfileEntryTransaction);
                    } catch (final SBonitaException e) {
                        throw new ProfileImportException("Error happend when create a new profileEntry with name " + parentprofileEntry.getName(), e);
                    }
                    final SProfileEntry parentEntry = addProfileEntryTransaction.getResult();

                    final List<ExportedProfileEntry> childrenProEn = parentprofileEntry.getChildProfileEntries();
                    if (childrenProEn != null && childrenProEn.size() > 0) {
                        for (final ExportedProfileEntry childProfileEntry : childrenProEn) {
                            final SProfileEntry sproEntrytp = proEntryBuilder.createNewInstance(childProfileEntry.getName(), newProfile.getId())
                                    .setDescription(childProfileEntry.getDescription()).setIndex(childProfileEntry.getIndex())
                                    .setPage(childProfileEntry.getPage()).setParentId(parentEntry.getId()).setType(childProfileEntry.getType()).done();
                            final CreateProfileEntry addProfileEntryTransactionc = new CreateProfileEntry(profileService, sproEntrytp);
                            try {
                                transactionExecutor.execute(addProfileEntryTransactionc);
                            } catch (final SBonitaException e) {
                                throw new ProfileImportException("Error happend when create a new profileEntry with name " + childProfileEntry.getName(), e);
                            }
                        }
                    }
                }

                // insert profileMapping
                final ExportedProfileMapping profileMapp = profile.getProfileMapping();
                final long profileId = newProfile.getId();
                final ImportProfileMember importpm = new ImportProfileMember(profileService, identityService, profileMapp, profileId);
                try {
                    transactionExecutor.execute(importpm);
                } catch (final SBonitaException e) {
                    throw new ProfileImportException("Error happend when insert a new profile mapping related with profile " + profile.getName(), e);
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
            final IdentityService identityService, final List<ExportedProfile> profiles, final ImportPolicy policy) throws ProfileImportException {
        final ImportAndHandleSameNameProfiles importAndHandler = new ImportAndHandleSameNameProfiles(profileService, identityService, profiles, policy);
        try {
            transactionExecutor.execute(importAndHandler);
        } catch (final SBonitaException e) {
            throw new ProfileImportException(e);
        }
        return importAndHandler.getResult();
    }

    private List<String> importWithDeleteExisting(final TransactionExecutor transactionExecutor, final ProfileService profileService,
            final IdentityService identityService, final List<ExportedProfile> profiles) throws ProfileImportException {
        final DeleteAllExistingProfiles deleteAll = new DeleteAllExistingProfiles(profileService);
        try {
            transactionExecutor.execute(deleteAll);
        } catch (final SBonitaException e) {
            throw new ProfileImportException(e);
        }
        final ImportProfiles importProfiles = new ImportProfiles(profileService, identityService, profiles);
        try {
            transactionExecutor.execute(importProfiles);
        } catch (final SBonitaException e) {
            throw new ProfileImportException(e);
        }
        return importProfiles.getResult();
    }

    private List<ExportedProfile> getProfilesFromXML(final String xmlContent, final Parser parser) throws ProfileImportException {
        StringReader reader = new StringReader(xmlContent);
        try {
            parser.validate(reader);
            reader.close();
            reader = new StringReader(xmlContent);
            return (List<ExportedProfile>) parser.getObjectFromXML(reader);
        } catch (final IOException ioe) {
            throw new ProfileImportException(ioe);
        } catch (final SValidationException e) {
            throw new ProfileImportException(e);
        } catch (final SXMLParseException e) {
            throw new ProfileImportException(e);
        } finally {
            reader.close();
        }
    }

    @Override
    public Map<String, Serializable> updateProfile(final long id, final ProfileUpdateDescriptor updateDescriptor) throws UpdateException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.CUSTOM_PROFILES);

        if (updateDescriptor == null || updateDescriptor.getFields().isEmpty()) {
            throw new UpdateException("The update descriptor does not contain field updates");
        }

        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProfileService profileService = tenantAccessor.getProfileService();

        SProfile profile;
        final UpdateProfile updateProfile = new UpdateProfile(profileService, profileService.getSProfileBuilderAccessor().getSProfileUpdateBuilder(), id,
                updateDescriptor);
        try {
            transactionExecutor.execute(updateProfile);
            profile = updateProfile.getResult();
        } catch (final SBonitaException e) {
            throw new UpdateException(e);
        }

        return ProfileUtils.profileToMap(ModelConvertor.toProfile(profile));
    }

    @Override
    public Map<String, Serializable> createProfileEntry(final String name, final String description, final Long parentId, final long profileId,
            final Long index, final String type, final String page) throws CreationException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.CUSTOM_PROFILES);

        if ("link".equalsIgnoreCase(type) && (page == null || "".equals(page))) {
            throw new CreationException("For a link, the page is mandatory.");
        }

        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProfileService profileService = tenantAccessor.getProfileService();
        final SProfileEntryBuilder profileEntryBuilder = profileService.getSProfileBuilderAccessor().getSProfileEntryBuilder();

        SProfileEntry profileEntry;

        // Set the profileEntry properties
        profileEntryBuilder.createNewInstance(name, profileId);
        profileEntryBuilder.setDescription(description);
        if (parentId != null) {
            profileEntryBuilder.setParentId(parentId);
        }
        if (index != null) {
            profileEntryBuilder.setIndex(index);
        } else {
            // Insert the profile entry at the end of the profile entry list
            profileEntryBuilder.setIndex(Long.MAX_VALUE);
        }
        profileEntryBuilder.setType(type);
        profileEntryBuilder.setPage(page);

        final CreateProfileEntry addProfileEntryTransaction = new CreateProfileEntry(profileService, profileEntryBuilder.done());
        try {
            transactionExecutor.execute(addProfileEntryTransaction);
            profileEntry = addProfileEntryTransaction.getResult();
        } catch (final SBonitaException e) {
            throw new CreationException(e);
        }

        final UpdateProfileEntryIndexOnInsert updateProfileEntryIndexTransaction = new UpdateProfileEntryIndexOnInsert(profileService, profileEntry);
        try {
            transactionExecutor.execute(updateProfileEntryIndexTransaction);
        } catch (final SBonitaException e) {
            throw new CreationException(e);
        }

        return ProfileEntryUtils.profileEntryToMap(profileEntry);
    }

    @Override
    public void deleteProfileEntry(final long profileEntryId) throws DeletionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProfileService profileService = tenantAccessor.getProfileService();

        SProfileEntry profileEntry;

        final GetProfileEntry getProfileEntryTransaction = new GetProfileEntry(profileService, profileEntryId);
        try {
            transactionExecutor.execute(getProfileEntryTransaction);
            profileEntry = getProfileEntryTransaction.getResult();
        } catch (final SBonitaException e) {
            throw new DeletionException(e);
        }

        final DeleteProfileEntry deleteProfileEntryTransaction = new DeleteProfileEntry(profileService, profileEntryId);
        try {
            transactionExecutor.execute(deleteProfileEntryTransaction);
        } catch (final SBonitaException e) {
            throw new DeletionException(e);
        }

        final UpdateProfileEntryIndexOnDelete updateProfileEntryIndexTransaction = new UpdateProfileEntryIndexOnDelete(profileService, profileEntry);
        try {
            transactionExecutor.execute(updateProfileEntryIndexTransaction);
        } catch (final SBonitaException e) {
            throw new DeletionException(e);
        }
    }

    @Override
    public Map<String, Serializable> updateProfileEntry(final long id, final ProfileEntryUpdateDescriptor updateDescriptor) throws UpdateException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.CUSTOM_PROFILES);

        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProfileService profileService = tenantAccessor.getProfileService();

        SProfileEntry profileEntry;

        final UpdateProfileEntry updateProfileEntryTransaction = new UpdateProfileEntry(profileService, profileService.getSProfileBuilderAccessor()
                .getSProfileEntryUpdateBuilder(), id, updateDescriptor);
        try {
            transactionExecutor.execute(updateProfileEntryTransaction);
            profileEntry = updateProfileEntryTransaction.getResult();
        } catch (final SBonitaException e) {
            throw new UpdateException(e);
        }

        final Map<ProfileEntryField, Serializable> fields = updateDescriptor.getFields();
        if (fields.get(ProfileEntryField.INDEX) != null) {
            final UpdateProfileEntryIndexOnInsert updateProfileEntryIndexTransaction = new UpdateProfileEntryIndexOnInsert(profileService, profileEntry);
            try {
                transactionExecutor.execute(updateProfileEntryIndexTransaction);
            } catch (final SBonitaException e) {
                throw new UpdateException(e);
            }
        }

        return ProfileEntryUtils.profileEntryToMap(profileEntry);
    }

}
