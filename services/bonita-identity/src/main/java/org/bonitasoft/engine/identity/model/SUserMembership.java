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
package org.bonitasoft.engine.identity.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;

/**
 * @author Anthony Birembaut
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_membership")
@IdClass(PersistentObjectId.class)
public class SUserMembership implements PersistentObject {

    public static final String ID = "id";
    public static final String USER_ID = "userId";
    public static final String ROLE_ID = "roleId";
    public static final String GROUP_ID = "groupId";
    public static final String ASSIGNED_BY = "assignedBy";
    public static final String ASSIGNED_DATE = "assignedDate";
    @Id
    private long id;
    @Id
    private long tenantId;
    @Column
    private long roleId;
    @Column
    private long groupId;
    @Column
    private long userId;
    @Column
    private long assignedBy;
    @Column
    private long assignedDate;
    private transient String groupParentPath;
    private transient String roleName;
    private transient String groupName;
    private transient String username;

    public SUserMembership(final long id, final long userId, final long groupId, final long roleId,
            final long assignedBy, final long assignedDate,
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

    public SUserMembership(final long userId, final long groupId, final long roleId) {
        this.userId = userId;
        this.groupId = groupId;
        this.roleId = roleId;
    }

}
