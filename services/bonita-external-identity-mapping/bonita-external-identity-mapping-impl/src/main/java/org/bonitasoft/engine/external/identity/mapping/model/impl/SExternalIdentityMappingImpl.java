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
package org.bonitasoft.engine.external.identity.mapping.model.impl;

import org.bonitasoft.engine.external.identity.mapping.model.SExternalIdentityMapping;

/**
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public class SExternalIdentityMappingImpl implements SExternalIdentityMapping {

    private static final long serialVersionUID = 5365215010000132154L;

    private long id;

    private long tenantId;

    private String kind;

    private String externalId;

    private long userId = -1;

    private long groupId = -1;

    private long roleId = -1;

    private transient String displayNamePart1;

    private transient String displayNamePart2;

    private transient String displayNamePart3;

    public SExternalIdentityMappingImpl() {
        super();
    }

    public SExternalIdentityMappingImpl(final String externalId) {
        this.externalId = externalId;
    }

    public SExternalIdentityMappingImpl(final long id, final long tenantId, final String externalId, final long userId, final long groupId, final long roleId,
            final String displayNamePart1, final String displayNamePart2, final String displayNamePart3) {
        this.id = id;
        this.tenantId = tenantId;
        this.externalId = externalId;
        this.userId = userId;
        this.groupId = groupId;
        this.roleId = roleId;
        this.displayNamePart1 = displayNamePart1;
        this.displayNamePart2 = displayNamePart2;
        this.displayNamePart3 = displayNamePart3;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getDiscriminator() {
        return SExternalIdentityMappingImpl.class.getName();
    }

    @Override
    public String getExternalId() {
        return externalId;
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

    public void setExternalId(final String externalId) {
        this.externalId = externalId;
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
    public String getKind() {
        return kind;
    }

    public void setKind(final String kind) {
        this.kind = kind;
    }

    @Override
    public String getDisplayNamePart1() {
        return displayNamePart1;
    }

    public void setDisplayNamePart1(final String namePart1) {
        displayNamePart1 = namePart1;
    }

    @Override
    public String getDisplayNamePart2() {
        return displayNamePart2;
    }

    public void setDisplayNamePart2(final String namePart2) {
        displayNamePart2 = namePart2;
    }

    @Override
    public String getDisplayNamePart3() {
        return displayNamePart3;
    }

    public void setDisplayNamePart3(final String namePart3) {
        displayNamePart3 = namePart3;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (externalId == null ? 0 : externalId.hashCode());
        result = prime * result + (int) (groupId ^ groupId >>> 32);
        result = prime * result + (int) (id ^ id >>> 32);
        result = prime * result + (kind == null ? 0 : kind.hashCode());
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
        final SExternalIdentityMappingImpl other = (SExternalIdentityMappingImpl) obj;
        if (externalId == null) {
            if (other.externalId != null) {
                return false;
            }
        } else if (!externalId.equals(other.externalId)) {
            return false;
        }
        if (groupId != other.groupId) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (kind == null) {
            if (other.kind != null) {
                return false;
            }
        } else if (!kind.equals(other.kind)) {
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
