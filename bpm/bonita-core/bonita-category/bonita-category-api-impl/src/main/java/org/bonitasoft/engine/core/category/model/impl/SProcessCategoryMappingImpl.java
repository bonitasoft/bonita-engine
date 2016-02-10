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
package org.bonitasoft.engine.core.category.model.impl;

import org.bonitasoft.engine.core.category.model.SProcessCategoryMapping;

/**
 * @author Matthieu Chaffotte
 */
public class SProcessCategoryMappingImpl implements SProcessCategoryMapping {

    private static final long serialVersionUID = 475782425337396808L;

    private long tenantId;

    private long id;

    private long categoryId;

    private long processId;

    public SProcessCategoryMappingImpl() {
        super();
    }

    public SProcessCategoryMappingImpl(final long categoryId, final long processId) {
        this.categoryId = categoryId;
        this.processId = processId;
    }

    public long getTenantId() {
        return tenantId;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getDiscriminator() {
        return SProcessCategoryMappingImpl.class.getName();
    }

    @Override
    public long getCategoryId() {
        return categoryId;
    }

    @Override
    public long getProcessId() {
        return processId;
    }

    public void setTenantId(final long tenantId) {
        this.tenantId = tenantId;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setCategoryId(final long categoryId) {
        this.categoryId = categoryId;
    }

    public void setProcessId(final long processId) {
        this.processId = processId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (categoryId ^ (categoryId >>> 32));
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + (int) (processId ^ (processId >>> 32));
        result = prime * result + (int) (tenantId ^ (tenantId >>> 32));
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
        final SProcessCategoryMappingImpl other = (SProcessCategoryMappingImpl) obj;
        if (categoryId != other.categoryId) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (processId != other.processId) {
            return false;
        }
        if (tenantId != other.tenantId) {
            return false;
        }
        return true;
    }

}
