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
package org.bonitasoft.engine.entitymember.impl;

import org.bonitasoft.engine.entitymember.EntityMember;

/**
 * @author Emmanuel Duchastenier
 */
public class EntityMemberImpl implements EntityMember {

    private static final long serialVersionUID = 8729465559012579249L;

    long entityMemberId;

    String externalId;

    long userId;

    long groupId;

    long roleId;

    private String displayNamePart1;

    private String displayNamePart2;

    private String displayNamePart3;

    public EntityMemberImpl(final long entityMemberId, final String externalId, final long userId, final long groupId, final long roleId,
            final String displayNamePart1, final String displayNamePart2, final String displayNamePart3) {
        super();
        this.entityMemberId = entityMemberId;
        this.externalId = externalId;
        this.userId = userId;
        this.groupId = groupId;
        this.roleId = roleId;
        this.displayNamePart1 = displayNamePart1;
        this.displayNamePart2 = displayNamePart2;
        this.displayNamePart3 = displayNamePart3;
    }

    @Override
    public long getEntityMemberId() {
        return entityMemberId;
    }

    public void setEntityMemberId(final long entityMemberId) {
        this.entityMemberId = entityMemberId;
    }

    @Override
    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(final String externalId) {
        this.externalId = externalId;
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
    public String getDisplayNamePart1() {
        return displayNamePart1;
    }

    public void setDisplayNamePart1(final String displayNamePart1) {
        this.displayNamePart1 = displayNamePart1;
    }

    @Override
    public String getDisplayNamePart2() {
        return displayNamePart2;
    }

    public void setDisplayNamePart2(final String displayNamePart2) {
        this.displayNamePart2 = displayNamePart2;
    }

    @Override
    public String getDisplayNamePart3() {
        return displayNamePart3;
    }

    public void setDisplayNamePart3(final String displayNamePart3) {
        this.displayNamePart3 = displayNamePart3;
    }
}
