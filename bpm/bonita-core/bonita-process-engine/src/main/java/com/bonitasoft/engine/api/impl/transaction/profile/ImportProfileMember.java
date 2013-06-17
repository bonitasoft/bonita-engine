/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
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
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.impl.ExportedProfileMapping;

/**
 * @author Zhao Na
 * @author Celine Souchet
 */
public class ImportProfileMember implements TransactionContentWithResult<List<String>> {

    private final ProfileService profileService;

    private final IdentityService identityService;

    private final long profileId;

    private final ExportedProfileMapping exportedProfileMapping;

    private final List<String> warnings = new ArrayList<String>();

    public ImportProfileMember(final ProfileService profileService, final IdentityService identityService, final ExportedProfileMapping exportedProfileMapping,
            final long profileId) {
        super();
        this.profileService = profileService;
        this.identityService = identityService;
        this.exportedProfileMapping = exportedProfileMapping;
        this.profileId = profileId;
    }

    @Override
    public void execute() throws SBonitaException {
        for (final String userName : exportedProfileMapping.getUsers()) {
            SUser user = null;
            try {
                user = identityService.getUserByUserName(userName);
            } catch (final SUserNotFoundException e) {
                warnings.add("User with name " + userName + " not found.");
                continue;
            }
            profileService.addUserToProfile(profileId, user.getId(), user.getFirstName(), user.getLastName(), user.getUserName());
        }
        for (final String roleName : exportedProfileMapping.getRoles()) {
            SRole role = null;
            try {
                role = identityService.getRoleByName(roleName);
            } catch (final SRoleNotFoundException e) {
                warnings.add("Role with name " + roleName + " not found.");
                continue;
            }
            profileService.addRoleToProfile(profileId, role.getId(), role.getName());
        }
        for (final String groupPath : exportedProfileMapping.getGroups()) {
            SGroup group = null;
            try {
                group = identityService.getGroupByPath(groupPath);
            } catch (final SGroupNotFoundException e) {
                warnings.add("Group with path " + groupPath + " not found.");
                continue;
            }
            profileService.addGroupToProfile(profileId, group.getId(), group.getName(), group.getParentPath());
        }

        boolean hasGroup = false;
        boolean hasRole = false;
        for (final BEntry<String, String> membership : exportedProfileMapping.getMemberships()) {
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

    @Override
    public List<String> getResult() {
        return warnings;
    }

}
