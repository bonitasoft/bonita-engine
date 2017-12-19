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
package org.bonitasoft.engine.persistence;

/**
 * @author Charles Souillard
 * @author Matthieu Chaffotte
 */
public abstract class AbstractSelectDescriptor<T> {

    private final String queryName;

    private final Class<? extends PersistentObject> entityType;

    private final Class<T> returnType;

    public AbstractSelectDescriptor(final String queryName, final Class<? extends PersistentObject> entityType, final Class<T> returnType) {
        this.entityType = entityType;
        this.queryName = queryName;
        this.returnType = returnType;
    }

    public String getQueryName() {
        return queryName;
    }

    public Class<? extends PersistentObject> getEntityType() {
        return entityType;
    }

    public Class<T> getReturnType() {
        return returnType;
    }

    @Override
    public String toString() {
        return "AbstractSelectDescriptor [entityType=" + entityType + ", queryName=" + queryName + ", returnType=" + returnType + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((entityType == null) ? 0 : entityType.hashCode());
        result = prime * result + ((queryName == null) ? 0 : queryName.hashCode());
        result = prime * result + ((returnType == null) ? 0 : returnType.hashCode());
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
        final AbstractSelectDescriptor<?> other = (AbstractSelectDescriptor<?>) obj;
        if (entityType == null) {
            if (other.entityType != null) {
                return false;
            }
        } else if (!entityType.equals(other.entityType)) {
            return false;
        }
        if (queryName == null) {
            if (other.queryName != null) {
                return false;
            }
        } else if (!queryName.equals(other.queryName)) {
            return false;
        }
        if (returnType == null) {
            if (other.returnType != null) {
                return false;
            }
        } else if (!returnType.equals(other.returnType)) {
            return false;
        }
        return true;
    }

}
