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
package org.bonitasoft.engine.identity.model.builder.impl;

import org.bonitasoft.engine.identity.model.SUserMembership;
import org.bonitasoft.engine.identity.model.builder.SUserMembershipUpdateBuilder;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class SUserMembershipUpdateBuilderImpl implements SUserMembershipUpdateBuilder {

    private final EntityUpdateDescriptor descriptor;

    public SUserMembershipUpdateBuilderImpl(final EntityUpdateDescriptor descriptor) {
        super();
        this.descriptor = descriptor;
    }

    @Override
    public EntityUpdateDescriptor done() {
        return descriptor;
    }

    @Override
    public SUserMembershipUpdateBuilder updateUserId(final long userId) {
        descriptor.addField(SUserMembership.USER_ID, userId);
        return this;
    }

    @Override
    public SUserMembershipUpdateBuilder updateGroupId(final long groupId) {
        descriptor.addField(SUserMembership.GROUP_ID, groupId);
        return this;
    }

    @Override
    public SUserMembershipUpdateBuilder updateRoleId(final long roleId) {
        descriptor.addField(SUserMembership.ROLE_ID, roleId);
        return this;
    }
}
