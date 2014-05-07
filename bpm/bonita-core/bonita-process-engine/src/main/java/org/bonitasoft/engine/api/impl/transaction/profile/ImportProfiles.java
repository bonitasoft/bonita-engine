/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl.transaction.profile;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.bpm.bar.xml.XMLProcessDefinition.BEntry;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SGroupNotFoundException;
import org.bonitasoft.engine.identity.SRoleNotFoundException;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.builder.SProfileBuilderFactory;
import org.bonitasoft.engine.profile.builder.SProfileEntryBuilderFactory;
import org.bonitasoft.engine.profile.impl.ExportedParentProfileEntry;
import org.bonitasoft.engine.profile.impl.ExportedProfile;
import org.bonitasoft.engine.profile.impl.ExportedProfileEntry;
import org.bonitasoft.engine.profile.impl.ExportedProfileMapping;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.model.SProfileEntry;

/**
 * @author Zhao Na
 * @author Celine Souchet
 */
public class ImportProfiles implements TransactionContentWithResult<List<String>> {

    private final ProfileService profileService;

    private final IdentityService identityService;

    private final List<ExportedProfile> exportedProfiles;

    private final long importer;

    private final List<String> warnings = new ArrayList<String>();

    public ImportProfiles(final ProfileService profileService, final IdentityService identityService, final List<ExportedProfile> exportedProfiles,
            final long importer) {
        super();
        this.profileService = profileService;
        this.identityService = identityService;
        this.exportedProfiles = exportedProfiles;
        this.importer = importer;
    }

    @Override
    public void execute() throws SBonitaException {

        for (final ExportedProfile exportedProfile : exportedProfiles) {
            // insert profile
            if (exportedProfile.getName() != null && !"".equals(exportedProfile.getName())) {
                final long creationDate = System.currentTimeMillis();
                final SProfile sprofile = BuilderFactory.get(SProfileBuilderFactory.class)
                        .createNewInstance(exportedProfile.getName(), exportedProfile.isDefault(), creationDate, importer, creationDate, importer)
                        .setDescription(exportedProfile.getDescription())
                        .setIconPath(null).done();
                final SProfile newProfile = profileService.createProfile(sprofile);
                // insert profileEntries
                final List<ExportedParentProfileEntry> parentProfileEntries = exportedProfile.getParentProfileEntries();
                for (final ExportedParentProfileEntry parentprofileEntry : parentProfileEntries) {
                    final SProfileEntry sproEntry = BuilderFactory.get(SProfileEntryBuilderFactory.class)
                            .createNewInstance(parentprofileEntry.getName(), newProfile.getId())
                            .setDescription(parentprofileEntry.getDescription()).setIndex(parentprofileEntry.getIndex()).setPage(parentprofileEntry.getPage())
                            .setParentId(0).setType(parentprofileEntry.getType()).done();
                    final SProfileEntry parentEntry = profileService.createProfileEntry(sproEntry);

                    final List<ExportedProfileEntry> childrenProEn = parentprofileEntry.getChildProfileEntries();
                    if (childrenProEn != null && childrenProEn.size() > 0) {
                        for (final ExportedProfileEntry childProfileEntry : childrenProEn) {
                            final SProfileEntry sproEntrytp = BuilderFactory.get(SProfileEntryBuilderFactory.class)
                                    .createNewInstance(childProfileEntry.getName(), newProfile.getId())
                                    .setDescription(childProfileEntry.getDescription()).setIndex(childProfileEntry.getIndex())
                                    .setPage(childProfileEntry.getPage()).setParentId(parentEntry.getId()).setType(childProfileEntry.getType()).done();
                            profileService.createProfileEntry(sproEntrytp);
                        }
                    }
                }
                // insert profileMapping
                final ExportedProfileMapping profileMapp = exportedProfile.getProfileMapping();
                final long profileId = newProfile.getId();
                final List<String> userNames = profileMapp.getUsers();
                for (final String userName : userNames) {
                    SUser user = null;
                    try {
                        user = identityService.getUserByUserName(userName);
                    } catch (final SUserNotFoundException e) {
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
                    } catch (final SRoleNotFoundException e) {
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
                    } catch (final SGroupNotFoundException e) {
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
                    } catch (final SGroupNotFoundException e) {
                        warnings.add("Group with path " + membership.getKey() + " not found in profile memberShip.");
                        hasGroup = true;
                    }
                    SRole role = null;
                    try {
                        role = identityService.getRoleByName(membership.getValue());
                    } catch (final SRoleNotFoundException e) {
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
