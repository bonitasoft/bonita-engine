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
package org.bonitasoft.engine.test.persistence.builder;

import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.impl.SUserMembershipImpl;

public class UserMembershipBuilder extends PersistentObjectBuilder<SUserMembershipImpl, UserMembershipBuilder> {

    private long groupId;

    private long userId;

    private long roleId;

    public static UserMembershipBuilder aUserMembership() {
        return new UserMembershipBuilder();
    }

    @Override
    UserMembershipBuilder getThisBuilder() {
        return this;
    }

    @Override
    SUserMembershipImpl _build() {
        SUserMembershipImpl membership = new SUserMembershipImpl();
        membership.setGroupId(groupId);
        membership.setUserId(userId);
        membership.setRoleId(roleId);
        return membership;
    }

    public UserMembershipBuilder forUser(final SUser user) {
        this.userId = user.getId();
        return this;
    }

    public UserMembershipBuilder forUser(final long userId) {
        this.userId = userId;
        return this;
    }

    public UserMembershipBuilder memberOf(final long groupId, final long roleId) {
        this.groupId = groupId;
        this.roleId = roleId;
        return this;
    }
}
