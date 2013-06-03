/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.data.instance.model.archive.impl;

import org.bonitasoft.engine.data.instance.model.SDataInstanceVisibilityMapping;
import org.bonitasoft.engine.data.instance.model.archive.SADataInstanceVisibilityMapping;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class SADataInstanceVisibilityMappingImpl implements SADataInstanceVisibilityMapping {

    private static final long serialVersionUID = 3307709384664507703L;

    private long id;

    private long tenantId;

    private long containerId;

    private String containerType;

    private String dataName;

    private long dataInstanceId;

    private long archiveDate;

    private long sourceObjectId;

    public SADataInstanceVisibilityMappingImpl() {
        super();
    }

    public SADataInstanceVisibilityMappingImpl(final long containerId, final String containerType, final String dataName, final long dataInstanceId,
            final long sourceObjectId) {
        this.containerId = containerId;
        this.containerType = containerType;
        this.dataName = dataName;
        this.dataInstanceId = dataInstanceId;
        this.sourceObjectId = sourceObjectId;
    }

    @Override
    public long getId() {
        return id;
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

    @Override
    public long getContainerId() {
        return containerId;
    }

    public void setContainerId(final long containerId) {
        this.containerId = containerId;
    }

    @Override
    public String getContainerType() {
        return containerType;
    }

    public void setContainerType(final String containerType) {
        this.containerType = containerType;
    }

    @Override
    public String getDataName() {
        return dataName;
    }

    public void setDataName(final String dataName) {
        this.dataName = dataName;
    }

    @Override
    public long getDataInstanceId() {
        return dataInstanceId;
    }

    public void setDataInstanceId(final long dataInstanceId) {
        this.dataInstanceId = dataInstanceId;
    }

    @Override
    public long getArchiveDate() {
        return archiveDate;
    }

    public void setArchiveDate(final long archivedDate) {
        archiveDate = archivedDate;
    }

    @Override
    public long getSourceObjectId() {
        return sourceObjectId;
    }

    public void setSourceObjectId(final long sourceObjectId) {
        this.sourceObjectId = sourceObjectId;
    }

    @Override
    public String getDiscriminator() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (archiveDate ^ archiveDate >>> 32);
        result = prime * result + (int) (containerId ^ containerId >>> 32);
        result = prime * result + (containerType == null ? 0 : containerType.hashCode());
        result = prime * result + (int) (dataInstanceId ^ dataInstanceId >>> 32);
        result = prime * result + (dataName == null ? 0 : dataName.hashCode());
        result = prime * result + (int) (id ^ id >>> 32);
        result = prime * result + (int) (sourceObjectId ^ sourceObjectId >>> 32);
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
        final SADataInstanceVisibilityMappingImpl other = (SADataInstanceVisibilityMappingImpl) obj;
        if (archiveDate != other.archiveDate) {
            return false;
        }
        if (containerId != other.containerId) {
            return false;
        }
        if (containerType == null) {
            if (other.containerType != null) {
                return false;
            }
        } else if (!containerType.equals(other.containerType)) {
            return false;
        }
        if (dataInstanceId != other.dataInstanceId) {
            return false;
        }
        if (dataName == null) {
            if (other.dataName != null) {
                return false;
            }
        } else if (!dataName.equals(other.dataName)) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (sourceObjectId != other.sourceObjectId) {
            return false;
        }
        if (tenantId != other.tenantId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SADataInstanceVisibilityMappingImpl [id=" + id + ", tenantId=" + tenantId + ", containerId=" + containerId + ", containerType=" + containerType
                + ", dataName=" + dataName + ", dataInstanceId=" + dataInstanceId + ", archiveDate=" + archiveDate + ", sourceObjectId=" + sourceObjectId + "]";
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return SDataInstanceVisibilityMapping.class;
    }

}
