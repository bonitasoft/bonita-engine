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
import org.bonitasoft.engine.identity.SUserNotFoundException;
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
public class DeleteUser extends DeleteWithActorMembers implements TransactionContent {

    private static final int BATCH_SIZE = 100;

    private final IdentityService identityService;

    private final ActorMappingService actorMappingService;

    private final ProfileService profileService;

    private final long userId;

    private final String userName;

    public DeleteUser(final IdentityService identityService, final ActorMappingService actorMappingService, final ProfileService profileService,
            final long userId) {
        super();
        this.identityService = identityService;
        this.actorMappingService = actorMappingService;
        this.profileService = profileService;
        this.userId = userId;
        userName = null;
    }

    public DeleteUser(final IdentityService identityService, final ActorMappingService actorMappingService, final ProfileService profileService,
            final String userName) {
        super();
        this.identityService = identityService;
        this.actorMappingService = actorMappingService;
        this.profileService = profileService;
        userId = -1;
        this.userName = userName;
    }

    @Override
    public void execute() throws SBonitaException {
        try {
            long id = userId;
            if (id == -1) {
                id = identityService.getUserByUserName(userName).getId();
            }
            deleteUserMembershipsByUser(id);
            deleteActorMembers(id);
            deleteProfileMembers(id);
            identityService.deleteUser(id);
        } catch (SUserNotFoundException notFound) {
            // not found, don't do anything specific
        }
    }

    private void deleteProfileMembers(final long id) throws SBonitaException {
        final String field = BuilderFactory.get(SProfileMemberBuilderFactory.class).getIdKey();
        List<SProfileMember> profileMembersOfUser;
        do {
            profileMembersOfUser = profileService.getProfileMembersOfUser(id, 0, BATCH_SIZE, field, OrderByType.ASC);
            for (final SProfileMember sProfileMember : profileMembersOfUser) {
                profileService.deleteProfileMember(sProfileMember);
            }
        } while (profileMembersOfUser.size() == BATCH_SIZE);
    }

    private void deleteActorMembers(final long id) throws SActorMemberNotFoundException, SActorMemberDeletionException, SBonitaReadException {
        final List<SActorMember> actorMembers = actorMappingService.getActorMembersOfUser(id);
        for (final SActorMember sActorMember : actorMembers) {
            setActorIdsOfRemovedElements(actorMappingService.removeActorMember(sActorMember.getId()));
        }
    }

    private void deleteUserMembershipsByUser(final long id) throws SIdentityException {
        final List<SUserMembership> sUserMemberships = identityService.getUserMembershipsOfUser(id);
        for (final SUserMembership sUserMembership : sUserMemberships) {
            identityService.deleteUserMembership(sUserMembership.getId());
        }
    }

}
