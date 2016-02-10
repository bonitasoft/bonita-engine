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
package org.bonitasoft.engine.supervisor.mapping.model.impl;

import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisor;

/**
 * @author Yanyan Liu
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SProcessSupervisorImpl implements SProcessSupervisor {

    private static final long serialVersionUID = 5585086715395975025L;

    private long id;

    private long tenantId;

    private long processDefId;

    private long userId = -1;

    private long groupId = -1;

    private long roleId = -1;

    public SProcessSupervisorImpl() {
        super();
    }

    public SProcessSupervisorImpl(final long processDefId) {
        this.processDefId = processDefId;
    }

    public SProcessSupervisorImpl(final long id, final long tenantId, final long processDefId, final long userId, final long groupId, final long roleId) {
        this.id = id;
        this.tenantId = tenantId;
        this.processDefId = processDefId;
        this.userId = userId;
        this.groupId = groupId;
        this.roleId = roleId;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getDiscriminator() {
        return SProcessSupervisorImpl.class.getName();
    }

    @Override
    public long getProcessDefId() {
        return processDefId;
    }

    @Override
    public long getUserId() {
        return userId;
    }

    @Override
    public void setId(final long id) {
        this.id = id;
    }

    public long getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(final long tenantId) {
        this.tenantId = tenantId;
    }

    public void setProcessDefId(final long processDefId) {
        this.processDefId = processDefId;
    }

    public void setUserId(final long userId) {
        this.userId = userId;
    }

    @Override
    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(final long groupId) {
        this.groupId = groupId;
    }

    @Override
    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(final long roleId) {
        this.roleId = roleId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (groupId ^ groupId >>> 32);
        result = prime * result + (int) (id ^ id >>> 32);
        result = prime * result + (int) (processDefId ^ processDefId >>> 32);
        result = prime * result + (int) (roleId ^ roleId >>> 32);
        result = prime * result + (int) (tenantId ^ tenantId >>> 32);
        result = prime * result + (int) (userId ^ userId >>> 32);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SProcessSupervisorImpl other = (SProcessSupervisorImpl) obj;
        if (groupId != other.groupId) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (processDefId != other.processDefId) {
            return false;
        }
        if (roleId != other.roleId) {
            return false;
        }
        if (tenantId != other.tenantId) {
            return false;
        }
        if (userId != other.userId) {
            return false;
        }
        return true;
    }

}
