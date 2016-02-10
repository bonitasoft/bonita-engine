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

import java.util.List;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SUserMembership;
import org.bonitasoft.engine.identity.model.builder.SUserMembershipBuilderFactory;

/**
 * @author Lu Kai
 * @author Matthieu Chaffotte
 */
public class AddUserMemberships implements TransactionContent {

    private final List<Long> userIds;

    private final IdentityService identityService;

    private final long groupId;

    private final long roleId;

    private final long currentUserId;

    public AddUserMemberships(final long groupId, final long roleId, final List<Long> userIds,
            final IdentityService identityService, final long currentUserId) {
        this.groupId = groupId;
        this.roleId = roleId;
        this.userIds = userIds;
        this.identityService = identityService;
        this.currentUserId = currentUserId;
    }

    @Override
    public void execute() throws SBonitaException {

        for (final long userId : userIds) {
            final SUserMembership userMembership = BuilderFactory.get(SUserMembershipBuilderFactory.class).createNewInstance(userId, groupId, roleId)
                    .setAssignedBy(currentUserId).done();
            identityService.createUserMembership(userMembership);
        }
    }

}
