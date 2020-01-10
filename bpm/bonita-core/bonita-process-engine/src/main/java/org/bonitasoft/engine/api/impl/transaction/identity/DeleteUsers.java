/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.profile.ProfileService;

/**
 * @author Lu Kai
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @author Emmanuel Duchastenier
 */
public class DeleteUsers extends DeleteWithActorMembers implements TransactionContent {

    private final List<Long> userIds;

    public DeleteUsers(final IdentityService identityService, final ActorMappingService actorMappingService,
            final ProfileService profileService,
            final List<Long> userIds) {
        super(actorMappingService, profileService, identityService);
        this.userIds = userIds;
    }

    @Override
    public void execute() throws SBonitaException {
        for (final Long userId : userIds) {
            deleteUserMembershipsByUser(userId);
            deleteActorMembersOfUser(userId);
            deleteProfileMembersOfUser(userId);
            getIdentityService().deleteUser(userId);
        }
    }

}
