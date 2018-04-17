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
package org.bonitasoft.engine.identity.impl;

import java.util.Date;

import org.bonitasoft.engine.identity.UserMembership;

/**
 * @author Matthieu Chaffotte
 * @author Bole Zhang
 */
public class UserMembershipImpl implements UserMembership {

    private static final long serialVersionUID = 4218013347165825662L;

    private long id;

    private long roleId;

    private String roleName;

    private long groupId;

    private String groupParentPath;

    private String groupName;

    private long userId;

    private String username;

    private long assignedBy;

    private Date assignedDate;

    private String assignedByName;

    public UserMembershipImpl() {
        super();
    }

    public UserMembershipImpl(final long id, final long userId, final long groupId, final long roleId) {
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
    public long getAssignedBy() {
        return assignedBy;
    }

    @Override
    public Date getAssignedDate() {
        return assignedDate;
    }

    @Override
    public long getRoleId() {
        return roleId;
    }

    @Override
    public long getGroupId() {
        return groupId;
    }

    @Override
    public long getUserId() {
        return userId;
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

    @Override
    public String getAssignedByName() {
        return assignedByName;
    }

    @Override
    public String getGroupParentPath() {
        return groupParentPath;
    }

    public void setAssignedByName(final String assignedByName) {
        this.assignedByName = assignedByName;
    }

    public void setGroupParentPath(final String groupParentPath) {
        this.groupParentPath = groupParentPath;
    }

    public void setAssignedDate(final Date assignedDate) {
        this.assignedDate = assignedDate;
    }

    public void setAssignedBy(final long assignedBy) {
        this.assignedBy = assignedBy;
    }

    public void setGroupName(final String groupName) {
        this.groupName = groupName;
    }

    public void setRoleName(final String roleName) {
        this.roleName = roleName;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

}
