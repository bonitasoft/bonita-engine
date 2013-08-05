/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.identity.xml;

import java.util.List;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.SActorMemberDeletionException;
import org.bonitasoft.engine.actor.mapping.SActorMemberNotFoundException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SIdentityException;
import org.bonitasoft.engine.identity.SUserDeletionException;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.SUserMembership;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.profile.ProfileService;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class DeleteOrganization implements TransactionContent {

    private static final int NB_BY_PAGE = 50;

    private final IdentityService identityService;

    private final ProfileService profileService;

    private final ActorMappingService actorMappingService;

    public DeleteOrganization(final IdentityService identityService, final ProfileService profileService, final ActorMappingService actorMappingService) {
        super();
        this.identityService = identityService;
        this.profileService = profileService;
        this.actorMappingService = actorMappingService;
    }

    @Override
    public void execute() throws SBonitaException {
        deleteActorMembers();
        deleteProfileMembers();
        deleteOrganization();
    }

    private void deleteProfileMembers() throws SBonitaException {
        profileService.deleteAllProfileMembers();
    }

    private void deleteActorMembers() throws SActorMemberNotFoundException, SActorMemberDeletionException, SUserNotFoundException, SUserDeletionException,
            SBonitaReadException {
        actorMappingService.deleteAllActorMembers();

    }

    private void deleteOrganization() throws SIdentityException {
        List<SUserMembership> memberships;
        do {
            memberships = identityService.getLightUserMemberships(0, NB_BY_PAGE);
            for (final SUserMembership sMembership : memberships) {
                identityService.deleteUserMembership(sMembership);
            }
        } while (memberships.size() == NB_BY_PAGE);

        List<SRole> roles;
        do {
            roles = identityService.getRoles(0, NB_BY_PAGE);
            for (final SRole sRole : roles) {
                identityService.deleteRole(sRole);
            }
        } while (roles.size() == NB_BY_PAGE);

        List<SUser> users;
        do {
            users = identityService.getUsers(0, NB_BY_PAGE);
            for (final SUser sUser : users) {
                identityService.deleteUser(sUser);
            }
        } while (users.size() == NB_BY_PAGE);

        List<SGroup> groups;
        do {
            groups = identityService.getGroups(0, NB_BY_PAGE);
            for (final SGroup sGroup : groups) {
                identityService.deleteGroup(sGroup);
            }
        } while (groups.size() == NB_BY_PAGE);
    }

}
