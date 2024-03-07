/**
 * Copyright (C) 2022 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.web.rest.server.datastore.utils;

import org.bonitasoft.engine.search.Order;
import org.bonitasoft.web.rest.server.datastore.converter.AttributeConverter;
import org.bonitasoft.web.rest.server.datastore.converter.EmptyAttributeConverter;

/**
 * Convenient object to deal with credentials sort options
 * Default sort order is ASCENDING
 *
 * @author Colin PUY
 */
public class Sort {

    public static final Order DEFAULT_ORDER = Order.ASC;

    private static final String SEPARATOR = " ";

    private final String field;

    private final Order order;

    private final AttributeConverter converter;

    public Sort(String sortValue, AttributeConverter converter) {
        this.converter = converter;
        field = getSortedFieldValue(sortValue);
        order = getOrder(sortValue);
    }

    public Sort(String sortValue) {
        this(sortValue, new EmptyAttributeConverter());
    }

    private Order getOrder(String sortValue) {
        String[] split = sortValue.split(SEPARATOR);
        if (split.length > 1) {
            return Order.valueOf(split[1].toUpperCase());
        }
        return DEFAULT_ORDER;
    }

    private String getSortedFieldValue(String sortValue) {
        String fieldValue = sortValue.split(SEPARATOR)[0];
        String convertedFieldValue = converter.convert(fieldValue);
        return convertedFieldValue != null ? convertedFieldValue : fieldValue;
    }

    public String getField() {
        return field;
    }

    public Order getOrder() {
        return order;
    }

    @Override
    public String toString() {
        return getField() +
                SEPARATOR +
                getOrder();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Sort other = (Sort) obj;
        if (field == null) {
            if (other.field != null) {
                return false;
            }
        } else if (!field.equals(other.field)) {
            return false;
        }
        return order == other.order;
    }

}
