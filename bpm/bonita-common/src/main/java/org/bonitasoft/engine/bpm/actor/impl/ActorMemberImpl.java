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
package org.bonitasoft.engine.bpm.actor.impl;

import org.bonitasoft.engine.bpm.actor.ActorMember;

/**
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public class ActorMemberImpl implements ActorMember {

    private static final long serialVersionUID = 284002232284297152L;

    private final long id;

    private final long userId;

    private final long groupId;

    private final long roleId;

    public ActorMemberImpl(final long id, final long userId, final long groupId, final long roleId) {
        super();
        this.id = id;
        this.userId = userId;
        this.groupId = groupId;
        this.roleId = roleId;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getUserId() {
        return userId;
    }

    @Override
    public long getGroupId() {
        return groupId;
    }

    @Override
    public long getRoleId() {
        return roleId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (groupId ^ groupId >>> 32);
        result = prime * result + (int) (id ^ id >>> 32);
        result = prime * result + (int) (roleId ^ roleId >>> 32);
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
        final ActorMemberImpl other = (ActorMemberImpl) obj;
        if (groupId != other.groupId) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (roleId != other.roleId) {
            return false;
        }
        if (userId != other.userId) {
            return false;
        }
        return true;
    }

}
