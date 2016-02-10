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
package org.bonitasoft.engine.identity.model.impl;

import org.bonitasoft.engine.identity.model.SUserMembership;

/**
 * @author Charles Souillard
 * @author Matthieu Chaffotte
 */
public class SUserMembershipImpl extends SPersistentObjectImpl implements SUserMembership {

    private static final long serialVersionUID = -4556925769413381295L;

    private long roleId;

    private long groupId;

    private long userId;

    private long assignedBy;

    private long assignedDate;

    private transient String roleName;

    private transient String groupName;

    private transient String username;

    public SUserMembershipImpl() {
        super();
    }

    public SUserMembershipImpl(final long id, final long userId, final long groupId, final long roleId, final long assignedBy, final long assignedDate,
            final String roleName, final String groupName, final String username) {
        super();
        setId(id);
        this.userId = userId;
        this.groupId = groupId;
        this.roleId = roleId;
        this.assignedBy = assignedBy;
        this.assignedDate = assignedDate;
        this.roleName = roleName;
        this.groupName = groupName;
        this.username = username;
    }

    public SUserMembershipImpl(final long userId, final long groupId, final long roleId) {
        this.userId = userId;
        this.groupId = groupId;
        this.roleId = roleId;
    }

    @Override
    public String getDiscriminator() {
        return SUserMembership.class.getName();
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
    public long getUserId() {
        return userId;
    }

    public void setRoleId(final long roleId) {
        this.roleId = roleId;
    }

    public void setGroupId(final long groupId) {
        this.groupId = groupId;
    }

    public void setUserId(final long userId) {
        this.userId = userId;
    }

    @Override
    public long getAssignedBy() {
        return assignedBy;
    }

    @Override
    public long getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedBy(final long assignedBy) {
        this.assignedBy = assignedBy;
    }

    public void setAssignedDate(final long assignedDate) {
        this.assignedDate = assignedDate;
    }

    @Override
    public String getGroupName() {
        return groupName;
    }

    @Override
    public String getRoleName() {
        return roleName;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setRoleName(final String roleName) {
        this.roleName = roleName;
    }

    public void setGroupName(final String groupName) {
        this.groupName = groupName;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (assignedBy ^ assignedBy >>> 32);
        result = prime * result + (int) (assignedDate ^ assignedDate >>> 32);
        result = prime * result + (int) (groupId ^ groupId >>> 32);
        result = prime * result + (int) (roleId ^ roleId >>> 32);
        result = prime * result + (int) (userId ^ userId >>> 32);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SUserMembershipImpl other = (SUserMembershipImpl) obj;
        if (assignedBy != other.assignedBy) {
            return false;
        }
        if (assignedDate != other.assignedDate) {
            return false;
        }
        if (groupId != other.groupId) {
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

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("SUserMembershipImpl (");
        builder.append(getId());
        builder.append(" [roleId=");
        builder.append(roleId);
        builder.append(", groupId=");
        builder.append(groupId);
        builder.append(", userId=");
        builder.append(userId);
        builder.append(", assignedBy=");
        builder.append(assignedBy);
        builder.append(", assignedDate=");
        builder.append(assignedDate);
        builder.append(", roleName=");
        builder.append(roleName);
        builder.append(", groupName=");
        builder.append(groupName);
        builder.append(", username=");
        builder.append(username);
        builder.append("]");
        return builder.toString();
    }

}
