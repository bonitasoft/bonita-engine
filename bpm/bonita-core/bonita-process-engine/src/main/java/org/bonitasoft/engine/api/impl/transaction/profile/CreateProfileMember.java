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
package org.bonitasoft.engine.api.impl.transaction.profile;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.MemberType;
import org.bonitasoft.engine.identity.SGroupNotFoundException;
import org.bonitasoft.engine.identity.SRoleNotFoundException;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.exception.profilemember.SProfileMemberCreationException;
import org.bonitasoft.engine.profile.model.SProfileMember;

/**
 * @author Julien Mege
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class CreateProfileMember implements TransactionContentWithResult<SProfileMember> {

    private final ProfileService profileService;

    private final IdentityService identityService;

    private final long profileId;

    private final Long userId;

    private final Long groupId;

    private final Long roleId;

    private final MemberType memberType;

    private SProfileMember sProfileMember;

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
        profileService.updateProfileMetaData(profileId);

        switch (memberType) {
            case USER:
                if (isNotNullOrEmpty(userId)) {
                    addUserToProfile();
                }
                break;
            case GROUP:
                if (isNotNullOrEmpty(groupId)) {
                    addGroupToProfile();
                }
                break;
            case ROLE:
                if (isNotNullOrEmpty(roleId)) {
                    addRoleToProfile();
                }
                break;
            default:
                if (isNotNullOrEmpty(groupId) && isNotNullOrEmpty(roleId)) {
                    addRoleAndGroupToProfile();
                }
                break;
        }
    }

    private void addRoleAndGroupToProfile() throws SGroupNotFoundException, SRoleNotFoundException, SProfileMemberCreationException {
        final SGroup group = identityService.getGroup(groupId);
        final SRole role = identityService.getRole(roleId);
        if (group != null && role != null) {
            sProfileMember = profileService
                    .addRoleAndGroupToProfile(profileId, roleId, groupId, role.getName(), group.getName(), group.getParentPath());
        }
    }

    private void addRoleToProfile() throws SRoleNotFoundException, SProfileMemberCreationException {
        final SRole role = identityService.getRole(roleId);

        if (role != null) {
            sProfileMember = profileService.addRoleToProfile(profileId, roleId, role.getName());
        }
    }

    private void addGroupToProfile() throws SGroupNotFoundException, SProfileMemberCreationException {
        final SGroup group = identityService.getGroup(groupId);

        if (group != null) {
            sProfileMember = profileService.addGroupToProfile(profileId, groupId, group.getName(), group.getParentPath());
        }
    }

    private void addUserToProfile() throws SUserNotFoundException, SProfileMemberCreationException {
        final SUser user = identityService.getUser(userId);

        if (user != null) {
            sProfileMember = profileService.addUserToProfile(profileId, userId, user.getUserName(), user.getLastName(), user.getUserName());
        }
    }

    private boolean isNotNullOrEmpty(final Long id) {
        return id != null && id > 0;
    }

    @Override
    public SProfileMember getResult() {
        return sProfileMember;
    }

}
