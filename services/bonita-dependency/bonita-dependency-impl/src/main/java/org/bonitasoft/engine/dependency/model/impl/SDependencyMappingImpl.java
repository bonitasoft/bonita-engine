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
package org.bonitasoft.engine.dependency.model.impl;

import org.bonitasoft.engine.dependency.model.SDependencyMapping;
import org.bonitasoft.engine.dependency.model.ScopeType;

public class SDependencyMappingImpl implements SDependencyMapping {

    private static final long serialVersionUID = 3669487911530579373L;

    private long id;

    private long artifactId;

    private ScopeType artifactType;

    private long dependencyId;

    private long tenantId;

    public SDependencyMappingImpl() {
        super();
    }

    public SDependencyMappingImpl(final long artifactId, final ScopeType artifactType, final long dependencyId) {
        super();
        this.artifactId = artifactId;
        this.artifactType = artifactType;
        this.dependencyId = dependencyId;
    }

    @Override
    public void setId(final long id) {
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }

    public long getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(final long tenantId) {
        this.tenantId = tenantId;
    }

    public void setArtifactId(final long artifactId) {
        this.artifactId = artifactId;
    }

    public void setArtifactType(final ScopeType artifactType) {
        this.artifactType = artifactType;
    }

    public void setDependencyId(final long dependencyId) {
        this.dependencyId = dependencyId;
    }

    @Override
    public long getArtifactId() {
        return artifactId;
    }

    @Override
    public ScopeType getArtifactType() {
        return artifactType;
    }

    @Override
    public long getDependencyId() {
        return dependencyId;
    }

    @Override
    public String getDiscriminator() {
        return SDependencyMappingImpl.class.getName();
    }

    @Override
    public String toString() {
        return "SDependencyMappingImpl [id=" + id + ", artifactId=" + artifactId + ", artifactType=" + artifactType + ", dependencyId=" + dependencyId
                + ", tenantId=" + tenantId + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (artifactId ^ artifactId >>> 32);
        result = prime * result + (artifactType == null ? 0 : artifactType.hashCode());
        result = prime * result + (int) (dependencyId ^ dependencyId >>> 32);
        result = prime * result + (int) (id ^ id >>> 32);
        result = prime * result + (int) (tenantId ^ tenantId >>> 32);
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
        final SDependencyMappingImpl other = (SDependencyMappingImpl) obj;
        if (artifactId != other.artifactId) {
            return false;
        }
        if (artifactType == null) {
            if (other.artifactType != null) {
                return false;
            }
        } else if (!artifactType.equals(other.artifactType)) {
            return false;
        }
        if (dependencyId != other.dependencyId) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (tenantId != other.tenantId) {
            return false;
        }
        return true;
    }

}
