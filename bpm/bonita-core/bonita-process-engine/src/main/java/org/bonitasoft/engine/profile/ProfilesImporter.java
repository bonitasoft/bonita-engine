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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportError.Type;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.api.ImportStatus.Status;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SGroupNotFoundException;
import org.bonitasoft.engine.identity.SRoleNotFoundException;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.profile.builder.SProfileBuilderFactory;
import org.bonitasoft.engine.profile.builder.SProfileEntryBuilderFactory;
import org.bonitasoft.engine.profile.exception.profile.SProfileCreationException;
import org.bonitasoft.engine.profile.exception.profile.SProfileNotFoundException;
import org.bonitasoft.engine.profile.exception.profile.SProfileUpdateException;
import org.bonitasoft.engine.profile.exception.profileentry.SProfileEntryCreationException;
import org.bonitasoft.engine.profile.exception.profileentry.SProfileEntryDeletionException;
import org.bonitasoft.engine.profile.exception.profilemember.SProfileMemberCreationException;
import org.bonitasoft.engine.profile.exception.profilemember.SProfileMemberDeletionException;
import org.bonitasoft.engine.profile.xml.MembershipNode;
import org.bonitasoft.engine.profile.xml.ParentProfileEntryNode;
import org.bonitasoft.engine.profile.xml.ProfileEntryNode;
import org.bonitasoft.engine.profile.xml.ProfileMappingNode;
import org.bonitasoft.engine.profile.xml.ProfileNode;
import org.bonitasoft.engine.profile.xml.ProfilesNode;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.model.SProfileEntry;

/**
 * Import profiles with mapping and entries using Policy
 *
 * @author Baptiste Mesta
 */
public class ProfilesImporter {

    private final ProfileService profileService;
    private final IdentityService identityService;
    private final ProfilesParser profilesParser;

    public ProfilesImporter(final ProfileService profileService, final IdentityService identityService, ProfilesParser profilesParser) {
        this.profileService = profileService;
        this.identityService = identityService;
        this.profilesParser = profilesParser;
    }

    private static ProfileImportStrategy getStrategy(final ProfileService profileService, final ImportPolicy policy) {
        switch (policy) {
            case DELETE_EXISTING:
                return new DeleteExistingImportStrategy(profileService);
            case FAIL_ON_DUPLICATES:
                return new FailOnDuplicateImportStrategy(profileService);
            case IGNORE_DUPLICATES:
                return new IgnoreDuplicateImportStrategy(profileService);
            case REPLACE_DUPLICATES:
                return new ReplaceDuplicateImportStrategy(profileService);
            case UPDATE_DEFAULTS:
                return new UpdateDefaultsImportStrategy(profileService);
            default:
                throw new IllegalStateException("No strategy defined for policy: " + policy);
        }
    }

    public List<ImportStatus> importProfiles(ProfilesNode profiles, ImportPolicy policy, final long importerId) throws ExecutionException {
        ProfileImportStrategy importStrategy = getStrategy(profileService, policy);
        importStrategy.beforeImport();
        try {
            final List<ImportStatus> importStatus = new ArrayList<>(profiles.getProfiles().size());
            for (final ProfileNode profile : profiles.getProfiles()) {
                if (profile.getName() == null || profile.getName().isEmpty()) {
                    continue;
                }
                final ImportStatus currentStatus = new ImportStatus(profile.getName());
                importStatus.add(currentStatus);
                SProfile existingProfile = null;

                try {
                    existingProfile = profileService.getProfileByName(profile.getName());
                    currentStatus.setStatus(Status.REPLACED);
                } catch (final SProfileNotFoundException e1) {
                    // profile does not exists
                }
                final SProfile newProfile = importTheProfile(importerId, profile, existingProfile, importStrategy);
                if (newProfile == null) {
                    // in case of skip
                    currentStatus.setStatus(Status.SKIPPED);
                    continue;
                }

                final long profileId = newProfile.getId();

                /*
                 * Import mapping with pages
                 */
                if (existingProfile == null || importStrategy.shouldUpdateProfileEntries(profile, existingProfile)) {
                    // update entries only if it's a custom profile
                    if (existingProfile != null) {
                        profileService.deleteAllProfileEntriesOfProfile(existingProfile);
                    }
                    currentStatus.getErrors().addAll(importProfileEntries(profileService, profile.getParentProfileEntries(), profileId));
                }

                /*
                 * Import mapping with organization
                 */
                ProfileMappingNode profileMapping = profile.getProfileMapping();
                if (profileMapping != null) {
                    currentStatus.getErrors().addAll(importProfileMapping(profileService, identityService, profileId, profileMapping));
                }
            }
            return importStatus;

        } catch (final SBonitaException e) {
            throw new ExecutionException(e);
        }
    }

    protected List<ImportError> importProfileEntries(final ProfileService profileService, final List<ParentProfileEntryNode> parentProfileEntries,
            final long profileId)
            throws SProfileEntryCreationException {
        final ArrayList<ImportError> errors = new ArrayList<>();
        for (final ParentProfileEntryNode parentProfileEntry : parentProfileEntries) {
            if (parentProfileEntry.hasErrors()) {
                errors.addAll(parentProfileEntry.getErrors());
                continue;
            }
            final SProfileEntry parentEntry = profileService.createProfileEntry(createProfileEntry(parentProfileEntry, profileId, 0));
            final long parentProfileEntryId = parentEntry.getId();
            final List<ProfileEntryNode> childrenProfileEntry = parentProfileEntry.getChildProfileEntries();
            if (childrenProfileEntry != null && childrenProfileEntry.size() > 0) {
                for (final ProfileEntryNode childProfileEntry : childrenProfileEntry) {
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

    List<ImportError> importProfileMapping(final ProfileService profileService, final IdentityService identityService, final long profileId,
            final ProfileMappingNode profileMapping) throws SProfileMemberCreationException {
        final ArrayList<ImportError> errors = new ArrayList<>();

        for (final String userName : profileMapping.getUsers()) {
            SUser user = null;
            try {
                user = identityService.getUserByUserName(userName);
            } catch (final SUserNotFoundException e) {
                errors.add(new ImportError(userName, Type.USER));
                continue;
            }
            profileService.addUserToProfile(profileId, user.getId(), user.getFirstName(), user.getLastName(), user.getUserName());
        }
        for (final String groupPath : profileMapping.getGroups()) {
            SGroup group = null;
            try {
                group = identityService.getGroupByPath(groupPath);
            } catch (final SGroupNotFoundException e) {
                errors.add(new ImportError(groupPath, Type.GROUP));
                continue;
            }
            profileService.addGroupToProfile(profileId, group.getId(), group.getName(), group.getParentPath());
        }
        for (final String roleName : profileMapping.getRoles()) {
            SRole role = null;
            try {
                role = identityService.getRoleByName(roleName);
            } catch (final SRoleNotFoundException e) {
                errors.add(new ImportError(roleName, Type.ROLE));
                continue;
            }
            profileService.addRoleToProfile(profileId, role.getId(), role.getName());
        }

        for (final MembershipNode membership : profileMapping.getMemberships()) {
            SGroup group = null;
            try {
                group = identityService.getGroupByPath(membership.getGroup());
            } catch (final SGroupNotFoundException e) {
                errors.add(new ImportError(membership.getGroup(), Type.GROUP));
            }
            SRole role = null;
            try {
                role = identityService.getRoleByName(membership.getRole());
            } catch (final SRoleNotFoundException e) {
                errors.add(new ImportError(membership.getRole(), Type.ROLE));
            }
            if (group == null || role == null) {
                continue;
            }
            profileService.addRoleAndGroupToProfile(profileId, role.getId(), group.getId(), role.getName(), group.getName(), group.getParentPath());
        }
        return errors;
    }

    protected SProfile importTheProfile(final long importerId, final ProfileNode profile, final SProfile existingProfile, ProfileImportStrategy importStrategy)
            throws ExecutionException, SProfileEntryDeletionException, SProfileMemberDeletionException, SProfileUpdateException, SProfileCreationException {
        final SProfile newProfile;
        if (existingProfile != null) {
            newProfile = importStrategy.whenProfileExists(importerId, profile, existingProfile);
        } else if (importStrategy.canCreateProfileIfNotExists(profile)) {
            newProfile = profileService.createProfile(createSProfile(profile, importerId));
        } else {
            newProfile = null;
        }
        return newProfile;
    }

    SProfile createSProfile(final ProfileNode profileNode, final long importerId) {
        final boolean isDefault = profileNode.isDefault();
        final long creationDate = System.currentTimeMillis();
        return BuilderFactory.get(SProfileBuilderFactory.class).createNewInstance(profileNode.getName(),
                isDefault, creationDate, importerId, creationDate, importerId).setDescription(profileNode.getDescription()).done();
    }

    protected SProfileEntry createProfileEntry(final ParentProfileEntryNode parentEntry, final long profileId, final long parentId) {
        return BuilderFactory.get(SProfileEntryBuilderFactory.class).createNewInstance(parentEntry.getName(), profileId)
                .setDescription(parentEntry.getDescription()).setIndex(parentEntry.getIndex()).setPage(parentEntry.getPage())
                .setParentId(parentId).setType(parentEntry.getType()).setCustom(parentEntry.isCustom()).done();
    }

    protected SProfileEntry createProfileEntry(final ProfileEntryNode childEntry, final long profileId, final long parentId) {
        return BuilderFactory.get(SProfileEntryBuilderFactory.class).createNewInstance(childEntry.getName(), profileId)
                .setDescription(childEntry.getDescription()).setIndex(childEntry.getIndex()).setPage(childEntry.getPage())
                .setParentId(parentId).setType(childEntry.getType()).setCustom(childEntry.isCustom()).done();
    }

    public List<String> toWarnings(final List<ImportStatus> importProfiles) {
        final ArrayList<String> warns = new ArrayList<>();
        for (final ImportStatus importStatus : importProfiles) {
            for (final ImportError error : importStatus.getErrors()) {
                warns.add("Unable to find the " + error.getType().name().toLowerCase() + " " + error.getName() + " on " + importStatus.getName());
            }
        }
        return warns;
    }

    public ProfilesNode convertFromXml(final String xmlContent) throws IOException {
        try {
            return profilesParser.convert(xmlContent);
        } catch (JAXBException e) {
            throw new IOException(e);
        }
    }

    static File getFileContainingMD5(long tenantId) throws BonitaHomeNotSetException, IOException {
        return BonitaHomeServer.getInstance().getTenantStorage().getProfileMD5(tenantId);
    }

}
