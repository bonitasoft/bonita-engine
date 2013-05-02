/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package com.bonitasoft.engine.api.impl.transaction.profile;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.bpm.bar.xml.XMLProcessDefinition.BEntry;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SGroupNotFoundException;
import org.bonitasoft.engine.identity.SRoleNotFoundException;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
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

import com.bonitasoft.engine.profile.ImportPolicy;
import com.bonitasoft.engine.profile.xml.SProfileImportDuplicatedException;

/**
 * @author Zhao Na
 */
public class ImportAndHandleSameNameProfiles implements TransactionContentWithResult<List<String>> {

    private final ProfileService profileService;

    private final IdentityService identityService;

    private final List<ExportedProfile> profiles;

    private final ImportPolicy profilePolicy;

    private final List<String> warnings = new ArrayList<String>();

    public ImportAndHandleSameNameProfiles(final ProfileService profileService, final IdentityService identityService, final List<ExportedProfile> profiles,
            final ImportPolicy profilePolicy) {
        super();
        this.profileService = profileService;
        this.identityService = identityService;
        this.profiles = profiles;
        this.profilePolicy = profilePolicy;
    }

    @Override
    public void execute() throws SBonitaException {
        SProfileBuilderAccessor builders = profileService.getSProfileBuilderAccessor();
        SProfileBuilder profileBuilder = builders.getSProfileBuilder();
        SProfileEntryBuilder proEntryBuilder = builders.getSProfileEntryBuilder();

        for (final ExportedProfile profile : profiles) {
            // insert profile
            if (profile.getName() != null && !"".equals(profile.getName())) {
                SProfile existingProfile = profileService.getProfileByName(profile.getName());
                if (existingProfile != null) {
                    /*
                     * if (ImportPolicy.REPLACE_DUPLICATES.equals(profilePolicy)) {
                     * profileService.deleteProfile(existingProfile);
                     * }
                     */
                    if (ImportPolicy.IGNORE_DUPLICATES.equals(profilePolicy)) {
                        continue;
                    }
                    if (ImportPolicy.FAIL_ON_DUPLICATES.equals(profilePolicy)) {
                        throw new SProfileImportDuplicatedException("There's a same name profile when import a profile named " + profile.getName());
                    }
                }

                SProfile sprofile = profileBuilder.createNewInstance(profile.getName()).setDescription(profile.getDescription())
                        .setIconPath(profile.getIconPath()).done();
                SProfile newProfile = profileService.createProfile(sprofile);

                // insert profileEntries
                final List<ExportedParentProfileEntry> parentProfileEntries = profile.getParentProfileEntries();
                for (final ExportedParentProfileEntry parentprofileEntry : parentProfileEntries) {
                    SProfileEntry sproEntry = proEntryBuilder.createNewInstance(parentprofileEntry.getName(), newProfile.getId())
                            .setDescription(parentprofileEntry.getDescription()).setIndex(parentprofileEntry.getIndex()).setPage(parentprofileEntry.getPage())
                            .setParentId(0).setType(parentprofileEntry.getType()).done();
                    SProfileEntry parentEntry = profileService.createProfileEntry(sproEntry);

                    final List<ExportedProfileEntry> childrenProEn = parentprofileEntry.getChildProfileEntries();
                    if (childrenProEn != null && childrenProEn.size() > 0) {
                        for (final ExportedProfileEntry childProfileEntry : childrenProEn) {
                            SProfileEntry sproEntrytp = proEntryBuilder.createNewInstance(childProfileEntry.getName(), newProfile.getId())
                                    .setDescription(childProfileEntry.getDescription()).setIndex(childProfileEntry.getIndex())
                                    .setPage(childProfileEntry.getPage()).setParentId(parentEntry.getId()).setType(childProfileEntry.getType()).done();
                            profileService.createProfileEntry(sproEntrytp);
                        }
                    }
                }
                // insert profileMapping
                final ExportedProfileMapping profileMapp = profile.getProfileMapping();
                final long profileId = newProfile.getId();
                final List<String> userNames = profileMapp.getUsers();
                for (final String userName : userNames) {
                    SUser user = null;
                    try {
                        user = identityService.getUserByUserName(userName);
                    } catch (SUserNotFoundException e) {
                        warnings.add("User with name " + userName + " not found.");
                        continue;
                    }
                    profileService.addUserToProfile(profileId, user.getId(), user.getFirstName(), user.getLastName(), user.getUserName());
                }
                final List<String> roleNames = profileMapp.getRoles();
                for (final String roleName : roleNames) {
                    SRole role = null;
                    try {
                        role = identityService.getRoleByName(roleName);
                    } catch (SRoleNotFoundException e) {
                        warnings.add("Role with name " + roleName + " not found.");
                        continue;
                    }
                    profileService.addRoleToProfile(profileId, role.getId(), role.getName());
                }
                final List<String> groupPaths = profileMapp.getGroups();
                for (final String groupPath : groupPaths) {
                    SGroup group = null;
                    try {
                        group = identityService.getGroupByPath(groupPath);
                    } catch (SGroupNotFoundException e) {
                        warnings.add("Group with path " + groupPath + " not found.");
                        continue;
                    }
                    profileService.addGroupToProfile(profileId, group.getId(), group.getName(), group.getParentPath());
                }
                final List<BEntry<String, String>> memberships = profileMapp.getMemberships();
                boolean hasGroup = false;
                boolean hasRole = false;
                for (final BEntry<String, String> membership : memberships) {
                    SGroup group = null;
                    try {
                        group = identityService.getGroupByPath(membership.getKey());
                    } catch (SGroupNotFoundException e) {
                        warnings.add("Group with path " + membership.getKey() + " not found in profile memberShip.");
                        hasGroup = true;
                    }
                    SRole role = null;
                    try {
                        role = identityService.getRoleByName(membership.getValue());
                    } catch (SRoleNotFoundException e) {
                        warnings.add("Role with name " + membership.getValue() + " not found in profile memberShip.");
                        hasRole = true;
                    }
                    if (hasGroup || hasRole) {
                        hasGroup = false;
                        hasRole = false;
                        continue;
                    }
                    profileService.addRoleAndGroupToProfile(profileId, role.getId(), group.getId(), role.getName(), group.getName(), group.getParentPath());
                }
            }

        }

    }

    @Override
    public List<String> getResult() {
        return warnings;
    }
}
