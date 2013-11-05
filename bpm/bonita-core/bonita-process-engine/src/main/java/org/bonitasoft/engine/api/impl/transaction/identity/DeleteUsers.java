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
import org.bonitasoft.engine.identity.model.SUserMembership;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.builder.SProfileMemberBuilderFactory;
import org.bonitasoft.engine.profile.model.SProfileMember;

/**
 * @author Lu Kai
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @author Emmanuel Duchastenier
 */
public class DeleteUsers extends DeleteWithActorMembers implements TransactionContent {

    private final IdentityService identityService;

    private final ActorMappingService actorMappingService;

    private final ProfileService profileService;

    private final List<Long> userIds;

    public DeleteUsers(final IdentityService identityService, final ActorMappingService actorMappingService, final ProfileService profileService,
            final List<Long> userIds) {
        super();
        this.identityService = identityService;
        this.actorMappingService = actorMappingService;
        this.userIds = userIds;
        this.profileService = profileService;
    }

    @Override
    public void execute() throws SBonitaException {
        for (final Long userId : userIds) {
            deleteUserMembershipsByUser(userId);
            deleteActorMembers(userId);
            deleteProfileMembers(userId);
            identityService.deleteUser(userId);
        }
    }

    private void deleteActorMembers(final Long userId) throws SActorMemberNotFoundException, SActorMemberDeletionException, SBonitaReadException {
        final List<SActorMember> actorMembers = actorMappingService.getActorMembersOfUser(userId);
        for (final SActorMember sActorMember : actorMembers) {
            setActorIdsOfRemovedElements(actorMappingService.removeActorMember(sActorMember.getId()));
        }
    }

    private void deleteProfileMembers(final Long userId) throws SBonitaException {
        final String field = BuilderFactory.get(SProfileMemberBuilderFactory.class).getIdKey();
        final int numberOfElements = 1000;
        List<SProfileMember> profileMembers;
        do {
            profileMembers = profileService.getProfileMembersOfUser(userId, 0, numberOfElements, field, OrderByType.ASC);
            while (profileMembers != null && !profileMembers.isEmpty()) {
                for (final SProfileMember sProfileMember : profileMembers) {
                    profileService.deleteProfileMember(sProfileMember);
                }
            }
        } while (profileMembers != null && profileMembers.size() > 0);

    }

    private void deleteUserMembershipsByUser(final long userId) throws SIdentityException {
        final List<SUserMembership> sUserMemberships = identityService.getUserMembershipsOfUser(userId);
        for (final SUserMembership sUserMembership : sUserMemberships) {
            identityService.deleteUserMembership(sUserMembership.getId());
        }
    }
}
