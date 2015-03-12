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

import java.io.Serializable;

/**
 * @author Charles Souillard
 */
public class OrderByOption implements Serializable {

    private static final long serialVersionUID = -3903433577282357734L;

    private final Class<? extends PersistentObject> clazz;

    private final String fieldName;

    private final OrderByType orderByType;

    public OrderByOption(final Class<? extends PersistentObject> clazz, final String fieldName, final OrderByType orderByType) {
        super();
        this.clazz = clazz;
        this.fieldName = fieldName;
        this.orderByType = orderByType;
    }

    public Class<? extends PersistentObject> getClazz() {
        return clazz;
    }

    public String getFieldName() {
        return fieldName;
    }

    public OrderByType getOrderByType() {
        return orderByType;
    }

    @Override
    public String toString() {
        return "OrderByOption [clazz=" + clazz + ", fieldName=" + fieldName + ", orderByType=" + orderByType + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderByOption)) return false;

        OrderByOption that = (OrderByOption) o;

        if (clazz != null ? !clazz.equals(that.clazz) : that.clazz != null) return false;
        if (fieldName != null ? !fieldName.equals(that.fieldName) : that.fieldName != null) return false;
        if (orderByType != that.orderByType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = clazz != null ? clazz.hashCode() : 0;
        result = 31 * result + (fieldName != null ? fieldName.hashCode() : 0);
        result = 31 * result + (orderByType != null ? orderByType.hashCode() : 0);
        return result;
    }

}
