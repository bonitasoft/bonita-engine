/**
 * Copyright (C) 2011, 2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl.transaction.identity;

import java.util.List;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.SActorMemberDeletionException;
import org.bonitasoft.engine.actor.mapping.SActorMemberNotFoundException;
import org.bonitasoft.engine.actor.mapping.model.SActorMember;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SUserMembership;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.builder.SProfileMemberBuilderFactory;
import org.bonitasoft.engine.profile.model.SProfileMember;

/**
 * @author Lu Kai
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class DeleteRoles extends DeleteWithActorMembers implements TransactionContent {

    private static final int BATCH_SIZE = 100;

    private final IdentityService identityService;

    private final ActorMappingService actorMappingService;

    private final ProfileService profileService;

    private final List<Long> roleIds;

    public DeleteRoles(final IdentityService identityService, final ActorMappingService actorMappingService, final ProfileService profileService,
            final List<Long> roleIds) {
        super();
        this.identityService = identityService;
        this.actorMappingService = actorMappingService;
        this.roleIds = roleIds;
        this.profileService = profileService;
    }

    @Override
    public void execute() throws SBonitaException {
        for (final Long roleId : roleIds) {
            // FIXME: refactor to use the same code as DeleteRole, same thing for DeleteGroups and DeleteUsers
            deleteMembershipsByRole(roleId);
            deleteActorMembers(roleId);
            deleteProfileMembers(roleId);
            identityService.deleteRole(roleId);
        }
    }

    private void deleteActorMembers(final Long roleId) throws SActorMemberNotFoundException, SActorMemberDeletionException, SBonitaReadException {
        final List<SActorMember> actorMembers = actorMappingService.getActorMembersOfRole(roleId);
        for (final SActorMember sActorMember : actorMembers) {
            setActorIdsOfRemovedElements(actorMappingService.removeActorMember(sActorMember.getId()));
        }
    }

    private void deleteProfileMembers(final Long roleId) throws SBonitaException {
        final int numberOfElements = 1000;
        final String field = BuilderFactory.get(SProfileMemberBuilderFactory.class).getIdKey();
        List<SProfileMember> profileMembers = profileService.getProfileMembersOfRole(roleId, 0, numberOfElements, field, OrderByType.ASC);
        while (profileMembers != null && !profileMembers.isEmpty()) {
            for (final SProfileMember sProfileMember : profileMembers) {
                profileService.deleteProfileMember(sProfileMember);
            }
            profileMembers = profileService.getProfileMembersOfRole(roleId, 0, numberOfElements, field, OrderByType.ASC);
        }

    }

    private void deleteMembershipsByRole(final long roleId) throws SBonitaException {
        int i = 0;
        List<SUserMembership> memberships;
        do {
            memberships = identityService.getUserMembershipsOfRole(roleId, i, i + BATCH_SIZE);
            i += BATCH_SIZE;
            for (final SUserMembership sUserMembership : memberships) {
                identityService.deleteUserMembership(sUserMembership.getId());
            }
        } while (memberships.size() == BATCH_SIZE);
    }
}
