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
package org.bonitasoft.engine.actor.mapping.model.impl;

import org.bonitasoft.engine.actor.mapping.model.SActorMember;

/**
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public class SActorMemberImpl implements SActorMember {

    private static final long serialVersionUID = -7382301171956752480L;

    private long tenantId;

    private long id;

    private long actorId;

    private long userId = -1;

    private long groupId = -1;

    private long roleId = -1;

    public SActorMemberImpl() {
        super();
    }

    public long getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(final long tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(final long id) {
        this.id = id;
    }

    @Override
    public String getDiscriminator() {
        return SActorMemberImpl.class.getName();
    }

    @Override
    public long getActorId() {
        return actorId;
    }

    public void setActorId(final long actorId) {
        this.actorId = actorId;
    }

    @Override
    public long getUserId() {
        return userId;
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
        result = prime * result + (int) (actorId ^ actorId >>> 32);
        result = prime * result + (int) (groupId ^ groupId >>> 32);
        result = prime * result + (int) (id ^ id >>> 32);
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
        final SActorMemberImpl other = (SActorMemberImpl) obj;
        if (actorId != other.actorId) {
            return false;
        }
        if (groupId != other.groupId) {
            return false;
        }
        if (id != other.id) {
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
