/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
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

import java.util.ArrayList;
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
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class DeleteGroups extends DeleteWithActorMembers implements TransactionContent {

    private static final int BATCH_SIZE = 100;

    private final IdentityService identityService;

    private final ActorMappingService actorMappingService;

    private final ProfileService profileService;

    private final List<Long> groupIds;

    public DeleteGroups(final IdentityService identityService, final ActorMappingService actorMappingService, final ProfileService profileService,
            final List<Long> groupIds) {
        super();
        this.identityService = identityService;
        this.actorMappingService = actorMappingService;
        this.groupIds = groupIds;
        this.profileService = profileService;
    }

    @Override
    public void execute() throws SBonitaException {
        final ArrayList<Long> list = new ArrayList<Long>(groupIds);
        while (!list.isEmpty()) {
            final Long groupId = list.get(0);
            deleteMembershipsByGroup(groupId);
            deleteActorMembers(groupId);
            deleteProfileMembers(groupId);
            final List<Long> deleteChildrenGroup = identityService.deleteChildrenGroup(groupId);
            list.removeAll(deleteChildrenGroup);
            identityService.deleteGroup(groupId);
            list.remove(0);
        }
    }

    private void deleteActorMembers(final Long groupId) throws SActorMemberNotFoundException, SActorMemberDeletionException, SBonitaReadException {
        final List<SActorMember> actorMembers = actorMappingService.getActorMembersOfGroup(groupId);
        for (final SActorMember sActorMember : actorMembers) {
            setActorIdsOfRemovedElements(actorMappingService.removeActorMember(sActorMember.getId()));
        }
    }

    private void deleteProfileMembers(final Long groupId) throws SBonitaException {
        final int numberOfElements = 1000;
        final String field = BuilderFactory.get(SProfileMemberBuilderFactory.class).getIdKey();
        List<SProfileMember> profileMembers = profileService.getProfileMembersOfGroup(groupId, 0, numberOfElements, field, OrderByType.ASC);
        while (profileMembers != null && !profileMembers.isEmpty()) {
            for (final SProfileMember sProfileMember : profileMembers) {
                profileService.deleteProfileMember(sProfileMember);
            }
            profileMembers = profileService.getProfileMembersOfGroup(groupId, 0, numberOfElements, field, OrderByType.ASC);
        }

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
