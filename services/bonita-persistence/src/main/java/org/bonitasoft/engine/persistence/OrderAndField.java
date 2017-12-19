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
 * @author Baptiste Mesta
 */
public class OrderAndField {

    private OrderByType order;

    private String field;

    public OrderAndField(final OrderByType order, final String field) {
        super();
        this.order = order;
        this.field = field;
    }

    public OrderByType getOrder() {
        return order;
    }

    public void setOrder(final OrderByType order) {
        this.order = order;
    }

    public String getField() {
        return field;
    }

    public void setField(final String field) {
        this.field = field;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (field == null ? 0 : field.hashCode());
        result = prime * result + (order == null ? 0 : order.hashCode());
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
        final OrderAndField other = (OrderAndField) obj;
        if (field == null) {
            if (other.field != null) {
                return false;
            }
        } else if (!field.equals(other.field)) {
            return false;
        }
        if (order != other.order) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "OrderAndField [order=" + order + ", field=" + field + "]";
    }

}
