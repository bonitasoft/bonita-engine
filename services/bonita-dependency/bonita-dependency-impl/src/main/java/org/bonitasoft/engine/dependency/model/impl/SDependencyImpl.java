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

import java.util.Arrays;

import org.bonitasoft.engine.dependency.model.SDependency;

/**
 * @author Charles Souillard
 * @author Matthieu Chaffotte
 */
public class SDependencyImpl implements SDependency {

    private static final long serialVersionUID = 6330280805579223985L;

    private long tenantId;

    private long id;

    private String name;

    private String fileName;

    private String description;

    private byte[] value_;

    public SDependencyImpl() {
        super();
    }

    public SDependencyImpl(final String name, final String fileName, final byte[] value) {
        super();
        this.name = name;
        this.fileName = fileName;
        this.value_ = value;
    }

    public long getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(final long tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public void setId(final long id) {
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }

    public byte[] getValue_() {
        return value_;
    }

    public void setValue_(final byte[] value_) {
        this.value_ = value_;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public byte[] getValue() {
        return value_;
    }

    @Override
    public String getDiscriminator() {
        return SDependencyImpl.class.getName();
    }

    @Override
    public String toString() {
        return "SDependencyImpl [tenantId=" + tenantId + ", id=" + id + ", name=" + name + ", fileName=" + fileName + ", description=" + description + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (description == null ? 0 : description.hashCode());
        result = prime * result + (fileName == null ? 0 : fileName.hashCode());
        result = prime * result + (int) (id ^ id >>> 32);
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (int) (tenantId ^ tenantId >>> 32);
        result = prime * result + Arrays.hashCode(value_);
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
        final SDependencyImpl other = (SDependencyImpl) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (fileName == null) {
            if (other.fileName != null) {
                return false;
            }
        } else if (!fileName.equals(other.fileName)) {
            return false;
        }
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
        if (tenantId != other.tenantId) {
            return false;
        }
        if (!Arrays.equals(value_, other.value_)) {
            return false;
        }
        return true;
    }

}
