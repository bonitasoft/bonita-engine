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
package org.bonitasoft.engine.profile.model.impl;

import org.bonitasoft.engine.profile.model.SProfileMember;

/**
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public class SProfileMemberImpl implements SProfileMember {

    private static final long serialVersionUID = -6991070623635291374L;

    private long id;

    private long tenantId;

    private long profileId;

    private long userId = -1;

    private long groupId = -1;

    private long roleId = -1;

    private transient String displayNamePart1;

    private transient String displayNamePart2;

    private transient String displayNamePart3;

    public SProfileMemberImpl() {
        super();
    }

    public SProfileMemberImpl(final long profileId) {
        super();
        this.profileId = profileId;
    }

    // used by Hibernate
    public SProfileMemberImpl(final long id, final long tenantId, final long profileId, final long userId, final long groupId, final long roleId,
            final String displayNamePart1, final String displayNamePart2, final String displayNamePart3) {
        super();
        this.id = id;
        this.tenantId = tenantId;
        this.profileId = profileId;
        this.userId = userId;
        this.groupId = groupId;
        this.roleId = roleId;
        this.displayNamePart1 = displayNamePart1;
        this.displayNamePart2 = displayNamePart2;
        this.displayNamePart3 = displayNamePart3;
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
        return SProfileMember.class.getName();
    }

    @Override
    public long getProfileId() {
        return profileId;
    }

    public void setProfileId(final long profileId) {
        this.profileId = profileId;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (groupId ^ groupId >>> 32);
        result = prime * result + (int) (id ^ id >>> 32);
        result = prime * result + (int) (profileId ^ profileId >>> 32);
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
        final SProfileMemberImpl other = (SProfileMemberImpl) obj;
        if (groupId != other.groupId) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (profileId != other.profileId) {
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
