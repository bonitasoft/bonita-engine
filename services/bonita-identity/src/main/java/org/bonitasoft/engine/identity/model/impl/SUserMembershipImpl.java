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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
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

    private String groupParentPath;

    private transient String roleName;

    private transient String groupName;

    private transient String username;

    public SUserMembershipImpl() {
        super();
    }

    public SUserMembershipImpl(final long id, final long userId, final long groupId, final long roleId, final long assignedBy, final long assignedDate,
            final String roleName, final String groupName, final String username, String groupParentPath) {
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
        this.groupParentPath = groupParentPath;
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
    public String getGroupParentPath() {
        return groupParentPath;
    }

    public void setGroupParentPath(String groupParentPath) {
        this.groupParentPath = groupParentPath;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("roleId", roleId)
                .append("groupId", groupId)
                .append("userId", userId)
                .append("assignedBy", assignedBy)
                .append("assignedDate", assignedDate)
                .append("groupParentPath", groupParentPath)
                .append("roleName", roleName)
                .append("groupName", groupName)
                .append("username", username)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof SUserMembershipImpl))
            return false;

        SUserMembershipImpl that = (SUserMembershipImpl) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(getRoleId(), that.getRoleId())
                .append(getGroupId(), that.getGroupId())
                .append(getUserId(), that.getUserId())
                .append(getAssignedBy(), that.getAssignedBy())
                .append(getAssignedDate(), that.getAssignedDate())
                .append(getGroupParentPath(), that.getGroupParentPath())
                .append(getRoleName(), that.getRoleName())
                .append(getGroupName(), that.getGroupName())
                .append(getUsername(), that.getUsername())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(getRoleId())
                .append(getGroupId())
                .append(getUserId())
                .append(getAssignedBy())
                .append(getAssignedDate())
                .append(getGroupParentPath())
                .append(getRoleName())
                .append(getGroupName())
                .append(getUsername())
                .toHashCode();
    }
}
