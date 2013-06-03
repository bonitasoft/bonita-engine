/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.data.model.impl;

import org.bonitasoft.engine.data.model.SDataSource;
import org.bonitasoft.engine.data.model.SDataSourceState;

/**
 * @author Matthieu Chaffotte
 */
public class SDataSourceImpl implements SDataSource {

    private static final long serialVersionUID = 1L;

    private long tenantId;

    private long id;

    private String name;

    private String version;

    private String implementationClassName;

    private SDataSourceState state;

    public SDataSourceImpl() {
        super();
    }

    public SDataSourceImpl(final String name, final String version, final SDataSourceState state, final String implementationClassName) {
        this.name = name;
        this.version = version;
        this.state = state;
        this.implementationClassName = implementationClassName;
    }

    public void setState(final SDataSourceState state) {
        this.state = state;
    }

    public long getTenantId() {
        return tenantId;
    }

    public void setTenantId(final long tenantId) {
        this.tenantId = tenantId;
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
    public String getVersion() {
        return version;
    }

    @Override
    public String getImplementationClassName() {
        return implementationClassName;
    }

    @Override
    public SDataSourceState getState() {
        return this.state;
    }

    @Override
    public String getDiscriminator() {
        return SDataSource.class.getName();
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setImplementationClassName(final String implementationClassName) {
        this.implementationClassName = implementationClassName;
    }

    @Override
    public String toString() {
        return "SDataSourceImpl [id=" + id + ", implementationClassName=" + implementationClassName + ", name=" + name + ", state=" + state + ", tenantId="
                + tenantId + ", version=" + version + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((implementationClassName == null) ? 0 : implementationClassName.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((state == null) ? 0 : state.hashCode());
        result = prime * result + (int) (tenantId ^ (tenantId >>> 32));
        result = prime * result + ((version == null) ? 0 : version.hashCode());
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
        final SDataSourceImpl other = (SDataSourceImpl) obj;
        if (id != other.id) {
            return false;
        }
        if (implementationClassName == null) {
            if (other.implementationClassName != null) {
                return false;
            }
        } else if (!implementationClassName.equals(other.implementationClassName)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (state != other.state) {
            return false;
        }
        if (tenantId != other.tenantId) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

}
