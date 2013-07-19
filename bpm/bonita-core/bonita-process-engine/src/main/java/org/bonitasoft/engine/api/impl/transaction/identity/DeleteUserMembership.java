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

import java.util.concurrent.Callable;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.identity.IdentityService;

/**
 * @author Bole Zhang
 */
public class DeleteUserMembership implements Callable<Void> {

    private final IdentityService identityService;

    private final long userMembershipId;

    private final long userId;

    private final long groupId;

    private final long roleId;

    public DeleteUserMembership(final long userMembershipId, final IdentityService identityService) {
        this.identityService = identityService;
        this.userMembershipId = userMembershipId;
        userId = -1;
        groupId = -1;
        roleId = -1;
    }

    public DeleteUserMembership(final long userId, final long groupId, final long roleId, final IdentityService identityService) {
        this.userId = userId;
        this.groupId = groupId;
        this.roleId = roleId;
        this.identityService = identityService;
        userMembershipId = -1;
    }

    @Override
    public Void call() throws SBonitaException {
        if (userMembershipId == -1) {
            identityService.deleteUserMembership(identityService.getLightUserMembership(userId, groupId, roleId));
        } else {
            identityService.deleteUserMembership(userMembershipId);
        }
        return null;
    }

}
