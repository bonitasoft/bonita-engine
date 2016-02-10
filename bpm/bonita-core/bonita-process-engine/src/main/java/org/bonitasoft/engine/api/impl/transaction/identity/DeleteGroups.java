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

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.profile.ProfileService;

/**
 * @author Lu Kai
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class DeleteGroups extends DeleteWithActorMembers implements TransactionContent {

    private final List<Long> groupIds;

    public DeleteGroups(final IdentityService identityService, final ActorMappingService actorMappingService, final ProfileService profileService,
            final List<Long> groupIds) {
        super(actorMappingService, profileService, identityService);
        this.groupIds = groupIds;
    }

    @Override
    public void execute() throws SBonitaException {
        final ArrayList<Long> list = new ArrayList<Long>(groupIds);
        while (!list.isEmpty()) {
            final Long groupId = list.get(0);
            deleteMembershipsByGroup(groupId);
            deleteActorMembersOfGroup(groupId);
            deleteProfileMembersOfGroup(groupId);
            final List<Long> deleteChildrenGroup = getIdentityService().deleteChildrenGroup(groupId);
            list.removeAll(deleteChildrenGroup);
            getIdentityService().deleteGroup(groupId);
            list.remove(0);
        }
    }

}
