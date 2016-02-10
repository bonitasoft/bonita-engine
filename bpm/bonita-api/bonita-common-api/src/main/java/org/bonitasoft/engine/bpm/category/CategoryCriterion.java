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
package org.bonitasoft.engine.bpm.category;

import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.Sort;

/**
 * Criterion to sort categories
 * 
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public enum CategoryCriterion {

    /**
     * By ascending name
     */
    NAME_ASC("name", Order.ASC),

    /**
     * By descending name
     */
    NAME_DESC("name", Order.DESC);

    private final String field;

    private final Order order;

    CategoryCriterion(final String field, final Order order) {
        this.field = field;
        this.order = order;
    }

    public Order getOrder() {
        return order;
    }

    public String getField() {
        return field;
    }

    public Sort getSort() {
        return new Sort(order, field);
    }

}
