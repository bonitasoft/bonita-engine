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
package org.bonitasoft.engine.bpm.data.impl;

import java.io.Serializable;

import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.data.DataInstance;

/**
 * @author Feng Hui
 * @author Matthieu Chaffotte
 */
public abstract class DataInstanceImpl implements DataInstance {

    private static final long serialVersionUID = -3752347909196691889L;

    private long tenantId;

    private long id;

    private String name;

    private String description;

    private boolean transientData;

    private String className;

    private long containerId;

    private String containerType;

    public DataInstanceImpl() {
        super();
    }

    public DataInstanceImpl(final DataDefinition dataDefinition) {
        name = dataDefinition.getName();
        description = dataDefinition.getDescription();
        transientData = dataDefinition.isTransientData();
        className = dataDefinition.getClassName();
    }

    @Deprecated
    public void setTenantId(final long tenantId) {
        this.tenantId = tenantId;
    }

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

    @Deprecated
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

    @Override
    public abstract Serializable getValue();

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

}
