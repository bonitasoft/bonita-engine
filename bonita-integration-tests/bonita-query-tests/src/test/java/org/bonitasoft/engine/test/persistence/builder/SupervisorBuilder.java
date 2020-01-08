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
package org.bonitasoft.engine.test.persistence.builder;

import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisor;

public class SupervisorBuilder extends PersistentObjectBuilder<SProcessSupervisor, SupervisorBuilder> {

    private long processDefId;

    private long groupId = 0;

    private long roleId = 0;

    private long userId = 0;

    public static SupervisorBuilder aSupervisor() {
        return new SupervisorBuilder();
    }

    @Override
    SupervisorBuilder getThisBuilder() {
        return this;
    }

    @Override
    public SProcessSupervisor _build() {
        final SProcessSupervisor user = new SProcessSupervisor();
        user.setId(id);
        user.setGroupId(groupId);
        user.setRoleId(roleId);
        user.setUserId(userId);
        user.setProcessDefId(processDefId);
        return user;
    }

    public SupervisorBuilder withProcessDefinitionId(final long processDefId) {
        this.processDefId = processDefId;
        return this;
    }

    public SupervisorBuilder withId(final long id) {
        this.id = id;
        return this;
    }

    public SupervisorBuilder withGroupId(final long groupId) {
        this.groupId = groupId;
        return this;
    }

    public SupervisorBuilder withUserId(final long userId) {
        this.userId = userId;
        return this;
    }

    public SupervisorBuilder withroleId(final long roleId) {
        this.roleId = roleId;
        return this;
    }
}
