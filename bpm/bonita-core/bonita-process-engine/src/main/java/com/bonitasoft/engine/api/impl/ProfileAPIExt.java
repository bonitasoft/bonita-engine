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
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.profile.ProfileCreator;
import org.bonitasoft.engine.profile.ProfileEntry;
import org.bonitasoft.engine.profile.ProfileEntryCreator;
import org.bonitasoft.engine.profile.ProfileEntryCreator.ProfileEntryField;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.builder.SProfileBuilder;
import org.bonitasoft.engine.profile.builder.SProfileBuilderAccessor;
import org.bonitasoft.engine.profile.builder.SProfileEntryBuilder;
import org.bonitasoft.engine.profile.impl.ExportedParentProfileEntry;
import org.bonitasoft.engine.profile.impl.ExportedProfile;
import org.bonitasoft.engine.profile.impl.ExportedProfileEntry;
import org.bonitasoft.engine.profile.impl.ExportedProfileMapping;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.model.SProfileEntry;
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
import com.bonitasoft.engine.profile.ImportPolicy;
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
    public Profile createProfile(final ProfileCreator creator) throws CreationException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.CUSTOM_PROFILES);

        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProfileService profileService = tenantAccessor.getProfileService();

        final CreateProfile createProfile = new CreateProfile(profileService, profileService.getSProfileBuilderAccessor().getSProfileBuilder(), creator);
        try {
            createProfile.execute();
            return SPModelConvertor.toProfile(createProfile.getResult());
        } catch (final SBonitaException e) {
            throw new CreationException(e);
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

        final DeleteProfile deleteProfileTransaction = new DeleteProfile(profileService, id);
        try {
            deleteProfileTransaction.execute();
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
        } else {
            return null;
        }
    }

    @Override
    public List<String> importProfilesUsingSpecifiedPolicy(final byte[] xmlContent, final ImportPolicy policy) throws ExecutionException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.CUSTOM_PROFILES);

        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProfileService profileService = tenantAccessor.getProfileService();
        final IdentityService identityService = tenantAccessor.getIdentityService();
        List<String> warnings;

        final Parser parser = tenantAccessor.getProfileParser();
        final List<ExportedProfile> profiles = getProfilesFromXML(new String(xmlContent), parser);

        switch (policy) {
            case DELETE_EXISTING:
                warnings = importWithDeleteExisting(profileService, identityService, profiles);
                break;
            case REPLACE_DUPLICATES:
                warnings = importInCaseReplaceOnDuplicate(profileService, identityService, profiles);
                break;
            default:
                warnings = importInCaseDuplicate(profileService, identityService, profiles, policy);
                break;
        }
        return warnings;
    }

    private List<String> importInCaseReplaceOnDuplicate(final ProfileService profileService, final IdentityService identityService,
            final List<ExportedProfile> profiles) throws ExecutionException {
        final List<String> warnings = new ArrayList<String>();
        final SProfileBuilderAccessor builders = profileService.getSProfileBuilderAccessor();
        final SProfileBuilder profileBuilder = builders.getSProfileBuilder();
        final SProfileEntryBuilder sProfileEntryBuilder = builders.getSProfileEntryBuilder();
        for (final ExportedProfile profile : profiles) {

            // insert profile
            if (profile.getName() != null && !"".equals(profile.getName())) {
                final GetProfileByName getProfileByName = new GetProfileByName(profileService, profile.getName());
                try {
                    getProfileByName.execute();
                } catch (final SBonitaException e) {
                    throw new ExecutionException(e);
                }
                // delete duplicated ones
                final SProfile existingProfile = getProfileByName.getResult();
                if (existingProfile != null) {
                    final DeleteProfile deleteProfileTransaction = new DeleteProfile(profileService, existingProfile.getId());
                    try {
                        deleteProfileTransaction.execute();
                    } catch (final SBonitaException e) {
                        throw new ExecutionException("Can't remove profile with id " + existingProfile.getId(), e);
                    }
                }
                final CreateProfile createProfile = new CreateProfile(profileService, profileBuilder, getProfileCreator(profile));
                try {
                    createProfile.execute();
                } catch (final SBonitaException e) {
                    throw new ExecutionException("Error happend when create a new profile with name " + profile.getName(), e);
                }
                final SProfile newProfile = createProfile.getResult();

                // insert profileEntries
                final List<ExportedParentProfileEntry> parentProfileEntries = profile.getParentProfileEntries();
                final long profileId = newProfile.getId();
                for (final ExportedParentProfileEntry parentProfileEntry : parentProfileEntries) {
                    final ProfileEntryCreator parentProfileEntryCreator = getProfileEntryCreator(parentProfileEntry, profileId, 0);
                    final CreateProfileEntry createProfileEntry = new CreateProfileEntry(profileService, sProfileEntryBuilder, parentProfileEntryCreator);
                    try {
                        createProfileEntry.execute();
                    } catch (final SBonitaException e) {
                        throw new ExecutionException("Error happend when create a new profileEntry with name " + parentProfileEntry.getName(), e);
                    }

                    final SProfileEntry parentEntry = createProfileEntry.getResult();
                    final long parentProfileEntryId = parentEntry.getId();
                    final List<ExportedProfileEntry> childrenProEn = parentProfileEntry.getChildProfileEntries();
                    if (childrenProEn != null && childrenProEn.size() > 0) {
                        for (final ExportedProfileEntry childProfileEntry : childrenProEn) {
                            final ProfileEntryCreator childProfileEntryCreator = getProfileEntryCreator(childProfileEntry, profileId, parentProfileEntryId);
                            final CreateProfileEntry addProfileEntryTransactionc = new CreateProfileEntry(profileService, sProfileEntryBuilder,
                                    childProfileEntryCreator);
                            try {
                                addProfileEntryTransactionc.execute();
                            } catch (final SBonitaException e) {
                                throw new ExecutionException("Error happend when create a new profileEntry with name " + childProfileEntry.getName(), e);
                            }
                        }
                    }
                }

                // insert profileMapping
                final ExportedProfileMapping profileMapp = profile.getProfileMapping();
                final ImportProfileMember importpm = new ImportProfileMember(profileService, identityService, profileMapp, profileId);
                try {
                    importpm.execute();
                } catch (final SBonitaException e) {
                    throw new ExecutionException("Error happend when insert a new profile mapping related with profile " + profile.getName(), e);
                }
                final List<String> warns = importpm.getResult();
                if (warns != null && !warns.isEmpty()) {
                    warnings.addAll(warns);
                }
            }
        }
        return warnings;
    }

    private List<String> importInCaseDuplicate(final ProfileService profileService, final IdentityService identityService,
            final List<ExportedProfile> profiles, final ImportPolicy policy) throws ExecutionException {
        final ImportAndHandleSameNameProfiles importAndHandler = new ImportAndHandleSameNameProfiles(profileService, identityService, profiles, policy);
        try {
            importAndHandler.execute();
        } catch (final SBonitaException e) {
            throw new ExecutionException(e);
        }
        return importAndHandler.getResult();
    }

    private List<String> importWithDeleteExisting(final ProfileService profileService, final IdentityService identityService,
            final List<ExportedProfile> profiles) throws ExecutionException {
        final DeleteAllExistingProfiles deleteAll = new DeleteAllExistingProfiles(profileService);
        try {
            deleteAll.execute();
        } catch (final SBonitaException e) {
            throw new ExecutionException(e);
        }
        final ImportProfiles importProfiles = new ImportProfiles(profileService, identityService, profiles);
        try {
            importProfiles.execute();
        } catch (final SBonitaException e) {
            throw new ExecutionException(e);
        }
        return importProfiles.getResult();
    }

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
    public Profile updateProfile(final long id, final ProfileUpdater updateDescriptor) throws UpdateException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.CUSTOM_PROFILES);

        if (updateDescriptor == null || updateDescriptor.getFields().isEmpty()) {
            throw new UpdateException("The update descriptor does not contain field updates");
        }

        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProfileService profileService = tenantAccessor.getProfileService();

        final UpdateProfile updateProfile = new UpdateProfile(profileService, profileService.getSProfileBuilderAccessor().getSProfileUpdateBuilder(), id,
                updateDescriptor);
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
        final SProfileEntryBuilder sProfileEntryBuilder = profileService.getSProfileBuilderAccessor().getSProfileEntryBuilder();

        SProfileEntry sProfileEntry;
        final CreateProfileEntry createProfileEntry = new CreateProfileEntry(profileService, sProfileEntryBuilder, creator);
        try {
            createProfileEntry.execute();
            sProfileEntry = createProfileEntry.getResult();
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
        final ProfileEntryCreator creator = new ProfileEntryCreator(name, profileId).setDescription(description).setType(type);
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

        SProfileEntry sProfileEntry;

        final GetProfileEntry getProfileEntryTransaction = new GetProfileEntry(profileService, id);
        try {
            getProfileEntryTransaction.execute();
            sProfileEntry = getProfileEntryTransaction.getResult();
        } catch (final SBonitaException e) {
            throw new DeletionException(e);
        }

        final DeleteProfileEntry deleteProfileEntryTransaction = new DeleteProfileEntry(profileService, id);
        try {
            deleteProfileEntryTransaction.execute();
        } catch (final SBonitaException e) {
            throw new DeletionException(e);
        }

        final UpdateProfileEntryIndexOnDelete updateProfileEntryIndexTransaction = new UpdateProfileEntryIndexOnDelete(profileService, sProfileEntry);
        try {
            updateProfileEntryIndexTransaction.execute();
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

        final UpdateProfileEntry updateProfileEntry = new UpdateProfileEntry(profileService, profileService.getSProfileBuilderAccessor()
                .getSProfileEntryUpdateBuilder(), id, updateDescriptor);
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

        return SPModelConvertor.toProfileEntry(sProfileEntry);
    }

    private ProfileCreator getProfileCreator(final ExportedProfile exportedProfile) {
        final ProfileCreator creator = new ProfileCreator(exportedProfile.getName());
        creator.setDescription(exportedProfile.getDescription());
        creator.setIconPath(exportedProfile.getIconPath());
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
