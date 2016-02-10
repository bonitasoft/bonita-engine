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
package org.bonitasoft.engine.api.impl.transaction.identity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.SActorMemberDeletionException;
import org.bonitasoft.engine.actor.mapping.SActorMemberNotFoundException;
import org.bonitasoft.engine.actor.mapping.model.SActorMember;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
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
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public class DeleteWithActorMembers {

    protected static final int BATCH_SIZE = 100;

    private final ActorMappingService actorMappingService;

    private final ProfileService profileService;

    private final IdentityService identityService;

    private final Set<Long> removedActorIds = new HashSet<Long>();

    public DeleteWithActorMembers(final ActorMappingService actorMappingService, final ProfileService profileService, final IdentityService identityService) {
        super();
        this.actorMappingService = actorMappingService;
        this.profileService = profileService;
        this.identityService = identityService;
    }

    public Set<Long> getRemovedActorIds() {
        return removedActorIds;
    }

    protected void setActorIdsOfRemovedElements(final SActorMember removedActorMember) {
        removedActorIds.add(removedActorMember.getActorId());
    }

    protected void deleteActorMembersOfUser(final long userId) throws SActorMemberNotFoundException, SActorMemberDeletionException, SBonitaReadException {
        List<SActorMember> actorMembers = actorMappingService.getActorMembersOfUser(userId, 0, BATCH_SIZE);
        while (!actorMembers.isEmpty()) {
            for (final SActorMember sActorMember : actorMembers) {
                setActorIdsOfRemovedElements(actorMappingService.deleteActorMember(sActorMember.getId()));
            }
            actorMembers = actorMappingService.getActorMembersOfUser(userId, 0, BATCH_SIZE);
        }
    }

    protected void deleteProfileMembersOfUser(final long id) throws SBonitaException {
        final String field = BuilderFactory.get(SProfileMemberBuilderFactory.class).getIdKey();
        List<SProfileMember> profileMembersOfUser;
        do {
            profileMembersOfUser = profileService.getProfileMembersOfUser(id, 0, BATCH_SIZE, field, OrderByType.ASC);
            for (final SProfileMember sProfileMember : profileMembersOfUser) {
                profileService.deleteProfileMember(sProfileMember);
            }
        } while (profileMembersOfUser.size() == BATCH_SIZE);
    }

    protected void deleteUserMembershipsByUser(final long id) throws SIdentityException {
        List<SUserMembership> sUserMemberships = identityService.getUserMembershipsOfUser(id, 0, BATCH_SIZE);
        while (!sUserMemberships.isEmpty()) {
            for (final SUserMembership sUserMembership : sUserMemberships) {
                identityService.deleteUserMembership(sUserMembership.getId());
            }
            sUserMemberships = identityService.getUserMembershipsOfUser(id, 0, BATCH_SIZE);
        }
    }

    protected void deleteActorMembersOfGroup(final long groupId) throws SActorMemberNotFoundException, SActorMemberDeletionException, SBonitaReadException,
            SIdentityException {
        List<SActorMember> actorMembers;
        do {
            actorMembers = actorMappingService.getActorMembersOfGroup(groupId, 0, BATCH_SIZE);
            for (final SActorMember sActorMember : actorMembers) {
                setActorIdsOfRemovedElements(actorMappingService.deleteActorMember(sActorMember.getId()));
            }
        } while (actorMembers.size() == BATCH_SIZE);

        deleteActorMembersOfGroupChildren(groupId);
    }

    private void deleteActorMembersOfGroupChildren(final long groupId) throws SIdentityException, SActorMemberNotFoundException, SActorMemberDeletionException,
            SBonitaReadException {
        int i = 0;
        List<SGroup> childrenGroup;
        do {
            childrenGroup = identityService.getGroupChildren(groupId, i, BATCH_SIZE);
            i += BATCH_SIZE;
            for (final SGroup sGroup : childrenGroup) {
                deleteActorMembersOfGroup(sGroup.getId());
            }
        } while (childrenGroup.size() == BATCH_SIZE);
    }

    protected void deleteProfileMembersOfGroup(final long groupId) throws SBonitaException {
        final String field = BuilderFactory.get(SProfileMemberBuilderFactory.class).getIdKey();
        List<SProfileMember> profileMembers;
        do {
            profileMembers = profileService.getProfileMembersOfGroup(groupId, 0, BATCH_SIZE, field, OrderByType.ASC);
            for (final SProfileMember sProfileMember : profileMembers) {
                profileService.deleteProfileMember(sProfileMember);
            }
        } while (profileMembers.size() == BATCH_SIZE);

        deleteProfileMembersOfGroupChildren(groupId);
    }

    private void deleteProfileMembersOfGroupChildren(final long groupId) throws SIdentityException, SBonitaException {
        int i = 0;
        List<SGroup> childrenGroup;
        do {
            childrenGroup = identityService.getGroupChildren(groupId, i, BATCH_SIZE);
            i += BATCH_SIZE;
            for (final SGroup sGroup : childrenGroup) {
                deleteProfileMembersOfGroup(sGroup.getId());
            }
        } while (childrenGroup.size() == BATCH_SIZE);
    }

    protected void deleteMembershipsByGroup(final long groupId) throws SBonitaException {
        List<SUserMembership> memberships;
        do {
            memberships = identityService.getUserMembershipsOfGroup(groupId, 0, BATCH_SIZE);
            for (final SUserMembership sUserMembership : memberships) {
                identityService.deleteUserMembership(sUserMembership.getId());
            }
        } while (memberships.size() == BATCH_SIZE);
    }

    protected void deleteActorMembersOfRole(final long roleId) throws SActorMemberNotFoundException, SActorMemberDeletionException, SBonitaReadException {
        List<SActorMember> actorMembers;
        do {
            actorMembers = actorMappingService.getActorMembersOfRole(roleId, 0, BATCH_SIZE);
            for (final SActorMember sActorMember : actorMembers) {
                setActorIdsOfRemovedElements(actorMappingService.deleteActorMember(sActorMember.getId()));
            }
        } while (actorMembers.size() == BATCH_SIZE);
    }

    protected void deleteProfileMembersOfRole(final long roleId) throws SBonitaException {
        final String field = BuilderFactory.get(SProfileMemberBuilderFactory.class).getIdKey();
        List<SProfileMember> profileMembers;
        do {
            profileMembers = profileService.getProfileMembersOfRole(roleId, 0, BATCH_SIZE, field, OrderByType.ASC);
            for (final SProfileMember sProfileMember : profileMembers) {
                profileService.deleteProfileMember(sProfileMember);
            }
        } while (profileMembers.size() == BATCH_SIZE);
    }

    protected void deleteMembershipsByRole(final long roleId) throws SBonitaException {
        List<SUserMembership> memberships;
        do {
            memberships = identityService.getUserMembershipsOfRole(roleId, 0, BATCH_SIZE);
            for (final SUserMembership sUserMembership : memberships) {
                identityService.deleteUserMembership(sUserMembership.getId());
            }
        } while (memberships.size() == BATCH_SIZE);
    }

    public ActorMappingService getActorMappingService() {
        return actorMappingService;
    }

    public ProfileService getProfileService() {
        return profileService;
    }

    public IdentityService getIdentityService() {
        return identityService;
    }
}
