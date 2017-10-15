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
package org.bonitasoft.engine.data.instance.model.impl;

import java.io.Serializable;

import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.exceptions.SDataInstanceNotWellFormedException;

/**
 * @author Zhao Na
 * @author Frederic Bouquet
 * @author Matthieu Chaffotte
 */
public abstract class SDataInstanceImpl implements SDataInstance {

    private static final long serialVersionUID = -3752347909196691889L;

    private long tenantId;

    private long id;

    private String name;

    private String description;

    private boolean transientData;

    private String className;

    private long containerId;

    private String containerType;

    public SDataInstanceImpl() {
        super();
    }

    public SDataInstanceImpl(final SDataDefinition dataDefinition) {
        name = dataDefinition.getName();
        description = dataDefinition.getDescription();
        transientData = dataDefinition.isTransientData();
        className = dataDefinition.getClassName();
    }

    @Override
    public void setTenantId(final long tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public void setId(final long id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setTransientData(final boolean transientData) {
        this.transientData = transientData;
    }

    public void setClassName(final String className) {
        this.className = className;
    }

    public void setContainerId(final long containerId) {
        this.containerId = containerId;
    }

    public void setContainerType(final String containerType) {
        this.containerType = containerType;
    }

    public void setDataTypeClassName(final String className) {
        this.className = className;
    }

    public long getTenantId() {
        return tenantId;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public abstract void setValue(Serializable value);

    @Override
    public Boolean isTransientData() {
        return transientData;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public long getContainerId() {
        return containerId;
    }

    @Override
    public String getContainerType() {
        return containerType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (id ^ id >>> 32);
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (int) (containerId ^ containerId >>> 32);
        result = prime * result + (description == null ? 0 : description.hashCode());
        result = prime * result + (className == null ? 0 : className.hashCode());
        result = prime * result + (containerType == null ? 0 : containerType.hashCode());
        result = prime * result + (transientData ? 1231 : 1237);
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
        final SDataInstanceImpl other = (SDataInstanceImpl) obj;
        if (id != other.id) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (containerId != other.containerId) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (className == null) {
            if (other.className != null) {
                return false;
            }
        } else if (!className.equals(other.className)) {
            return false;
        }
        if (containerType == null) {
            if (other.containerType != null) {
                return false;
            }
        } else if (!containerType.equals(other.containerType)) {
            return false;
        }
        if (transientData != other.transientData) {
            return false;
        }
        return true;
    }

    /**
     * Check if the data is well formed
     *
     * @throws org.bonitasoft.engine.data.instance.model.exceptions.SDataInstanceNotWellFormedException
     *         thrown if the data is not well formed
     */
    public void validate() throws SDataInstanceNotWellFormedException {
    }

}
