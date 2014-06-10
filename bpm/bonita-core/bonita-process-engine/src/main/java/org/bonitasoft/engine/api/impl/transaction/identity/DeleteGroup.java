/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
import org.bonitasoft.engine.identity.SIdentityException;
import org.bonitasoft.engine.identity.model.SGroup;
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
 * @author Emmanuel Duchastenier
 */
public class DeleteGroup extends DeleteWithActorMembers implements TransactionContent {

    private static final int BATCH_SIZE = 100;

    private final IdentityService identityService;

    private final ActorMappingService actorMappingService;

    private final ProfileService profileService;

    private final long groupId;

    public DeleteGroup(final IdentityService identityService, final ActorMappingService actorMappingService, final ProfileService profileService,
            final long groupId) {
        super();
        this.identityService = identityService;
        this.actorMappingService = actorMappingService;
        this.groupId = groupId;
        this.profileService = profileService;
    }

    @Override
    public void execute() throws SBonitaException {
        deleteMembershipsByGroup(groupId);
        deleActorMembers(groupId);
        deleteProfileMembers(groupId);
        identityService.deleteChildrenGroup(groupId);
        identityService.deleteGroup(groupId);
    }

    private void deleActorMembers(final long groupId) throws SActorMemberNotFoundException, SActorMemberDeletionException, SBonitaReadException,
            SIdentityException {
        final List<SActorMember> actorMembers = actorMappingService.getActorMembersOfGroup(groupId);
        for (final SActorMember sActorMember : actorMembers) {
            setActorIdsOfRemovedElements(actorMappingService.removeActorMember(sActorMember.getId()));
        }
        int i = 0;
        List<SGroup> childrenGroup;
        do {
            childrenGroup = identityService.getGroupChildren(groupId, i, BATCH_SIZE);
            i += BATCH_SIZE;
            for (final SGroup sGroup : childrenGroup) {
                deleActorMembers(sGroup.getId());
            }
        } while (childrenGroup.size() == BATCH_SIZE);
    }

    private void deleteProfileMembers(final long groupId) throws SBonitaException {
        final String field = BuilderFactory.get(SProfileMemberBuilderFactory.class).getIdKey();
        List<SProfileMember> profileMembers;
        do {
            profileMembers = profileService.getProfileMembersOfGroup(groupId, 0, BATCH_SIZE, field, OrderByType.ASC);
            for (final SProfileMember sProfileMember : profileMembers) {
                profileService.deleteProfileMember(sProfileMember);
            }
        } while (profileMembers.size() == BATCH_SIZE);
        int i = 0;
        List<SGroup> childrenGroup;
        do {
            childrenGroup = identityService.getGroupChildren(groupId, i, BATCH_SIZE);
            i += BATCH_SIZE;
            for (final SGroup sGroup : childrenGroup) {
                deleteProfileMembers(sGroup.getId());
            }
        } while (childrenGroup.size() == BATCH_SIZE);

    }

    private void deleteMembershipsByGroup(final long groupId) throws SBonitaException {
        int i = 0;
        List<SUserMembership> memberships;
        do {
            memberships = identityService.getUserMembershipsOfGroup(groupId, i, i + BATCH_SIZE);
            i += BATCH_SIZE;
            for (final SUserMembership sUserMembership : memberships) {
                identityService.deleteUserMembership(sUserMembership.getId());
            }
        } while (memberships.size() == BATCH_SIZE);
    }

}
