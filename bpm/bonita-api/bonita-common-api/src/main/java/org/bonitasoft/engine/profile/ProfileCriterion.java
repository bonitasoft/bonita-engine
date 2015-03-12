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
package org.bonitasoft.engine.profile;

import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.Sort;

/**
 * @author Celine Souchet
 */
public enum ProfileCriterion {

    /**
     * Profile identifier ascending order
     */
    ID_ASC(ProfileSearchDescriptor.ID, Order.ASC),

    /**
     * Profile identifier descending order
     */
    ID_DESC(ProfileSearchDescriptor.ID, Order.DESC),

    /**
     * Profile is default ascending order
     */
    IS_DEFAULT_ASC(ProfileSearchDescriptor.IS_DEFAULT, Order.ASC),

    /**
     * Profile is default descending order
     */
    IS_DEFAULT_DESC(ProfileSearchDescriptor.IS_DEFAULT, Order.DESC),

    /**
     * Profile name ascending order
     */
    NAME_ASC(ProfileSearchDescriptor.NAME, Order.ASC),

    /**
     * Profile name descending order
     */
    NAME_DESC(ProfileSearchDescriptor.NAME, Order.DESC);

    private final String field;

    private final Order order;

    ProfileCriterion(final String field, final Order order) {
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
