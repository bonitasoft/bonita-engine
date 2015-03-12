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

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SUserMembership;

/**
 * @author Bole Zhang
 */
public class GetUserMembership implements TransactionContentWithResult<SUserMembership> {

    private final IdentityService identityService;

    private final long userId;

    private SUserMembership sUserMembership;

    private final long groupId;

    private final long roleId;

    private final long userMembershipId;

    public GetUserMembership(final long userId, final long groupId, final long roleId, final IdentityService identityService) {
        this.userId = userId;
        this.groupId = groupId;
        this.roleId = roleId;
        this.identityService = identityService;
        userMembershipId = -1;
    }

    public GetUserMembership(final long userMembershipId, final IdentityService identityService) {
        this.userMembershipId = userMembershipId;
        this.identityService = identityService;
        userId = -1;
        groupId = -1;
        roleId = -1;
    }

    @Override
    public void execute() throws SBonitaException {
        if (userMembershipId == -1) {
            sUserMembership = identityService.getUserMembership(userId, groupId, roleId);
        } else {
            sUserMembership = identityService.getUserMembership(userMembershipId);
        }
    }

    @Override
    public SUserMembership getResult() {
        return sUserMembership;
    }

}
