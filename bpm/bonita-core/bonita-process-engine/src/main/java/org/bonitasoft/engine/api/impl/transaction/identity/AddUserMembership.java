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

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.SUserMembership;
import org.bonitasoft.engine.identity.model.builder.SUserMembershipBuilderFactory;

/**
 * @author Bole Zhang
 * @author Matthieu Chaffotte
 */
public class AddUserMembership implements TransactionContentWithResult<SUserMembership> {

    private final IdentityService identityService;

    private final long userId;

    private final long groupId;

    private final long roleId;

    private final long assignedBy;

    private SUserMembership userMembership;

    public AddUserMembership(final long userId, final long groupId, final long roleId, final long assignedBy, final IdentityService identityService) {
        this.assignedBy = assignedBy;
        this.identityService = identityService;
        this.userId = userId;
        this.groupId = groupId;
        this.roleId = roleId;
    }

    @Override
    public void execute() throws SBonitaException {
        // FIXME: if RDBMS has foreign keys, getUser, getRole, getGroup can be ommitted:
        final SUser user = identityService.getUser(userId);
        final SRole role = identityService.getRole(roleId);
        final SGroup group = identityService.getGroup(groupId);
        userMembership = BuilderFactory.get(SUserMembershipBuilderFactory.class).createNewInstance(user.getId(), group.getId(), role.getId()).setAssignedBy(assignedBy)
                .setAssignedDate(System.currentTimeMillis()).done();
        identityService.createUserMembership(userMembership);
    }

    @Override
    public SUserMembership getResult() {
        return userMembership;
    }

}
