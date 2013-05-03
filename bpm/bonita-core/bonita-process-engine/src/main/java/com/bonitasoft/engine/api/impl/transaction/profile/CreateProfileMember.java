/*******************************************************************************
 * Copyright (C) 2011, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction.profile;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.bpm.model.MemberType;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.model.SProfileMember;

import com.bonitasoft.engine.api.impl.ProfileMemberUtils;

/**
 * @author Julien Mege
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class CreateProfileMember implements TransactionContentWithResult<Map<String, Serializable>> {

    private final ProfileService profileService;

    private final IdentityService identityService;

    private final long profileId;

    private final Long userId;

    private final Long groupId;

    private final Long roleId;

    private final MemberType memberType;

    private Map<String, Serializable> userProfile;

    public CreateProfileMember(final ProfileService profileService, final IdentityService identityService, final long profileId, final Long userId,
            final Long groupId, final Long roleId, final MemberType memberType) {
        super();
        this.profileService = profileService;
        this.identityService = identityService;
        this.profileId = profileId;
        this.userId = userId;
        this.groupId = groupId;
        this.roleId = roleId;
        this.memberType = memberType;
    }

    @Override
    public void execute() throws SBonitaException {
        SProfileMember profileMember;
        SUser user = null;
        SGroup group = null;
        SRole role = null;
        if (userId != null && userId > 0) {
            user = identityService.getUser(userId);
        }
        if (groupId != null && groupId > 0) {
            group = identityService.getGroup(groupId);

        }
        if (roleId != null && roleId > 0) {
            role = identityService.getRole(roleId);
        }
        switch (memberType) {
            case USER:
                profileMember = profileService.addUserToProfile(profileId, userId, user.getUserName(), user.getLastName(), user.getUserName());
                break;

            case GROUP:
                profileMember = profileService.addGroupToProfile(profileId, groupId, group.getName(), group.getParentPath());
                break;

            case ROLE:
                profileMember = profileService.addRoleToProfile(profileId, roleId, role.getName());
                break;

            default:
                profileMember = profileService.addRoleAndGroupToProfile(profileId, roleId, groupId, role.getName(), group.getName(), group.getParentPath());
                break;
        }
        userProfile = ProfileMemberUtils.memberAsProfileMembersMap(profileMember);
    }

    @Override
    public Map<String, Serializable> getResult() {
        return userProfile;
    }

}
