/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.bpm.process;

import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.Sort;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public enum ProcessDeploymentInfoCriterion {

    /**
     * Process name ascending order
     */
    NAME_ASC(ProcessDeploymentInfoSearchDescriptor.NAME, Order.ASC),

    /**
     * Process label ascending order
     */
    LABEL_ASC(ProcessDeploymentInfoSearchDescriptor.LABEL, Order.ASC),

    /**
     * Process version ascending order
     */
    VERSION_ASC(ProcessDeploymentInfoSearchDescriptor.VERSION, Order.ASC),

    /**
     * Process activation state ascending order
     */
    ACTIVATION_STATE_ASC(ProcessDeploymentInfoSearchDescriptor.ACTIVATION_STATE, Order.ASC),

    /**
     * Process configuration state ascending order
     */
    CONFIGURATION_STATE_ASC(ProcessDeploymentInfoSearchDescriptor.CONFIGURATION_STATE, Order.ASC),

    /**
     * Process name ascending order
     */
    NAME_DESC(ProcessDeploymentInfoSearchDescriptor.NAME, Order.DESC),

    /**
     * Process label ascending order
     */
    LABEL_DESC(ProcessDeploymentInfoSearchDescriptor.LABEL, Order.DESC),

    /**
     * Process version ascending order
     */
    VERSION_DESC(ProcessDeploymentInfoSearchDescriptor.VERSION, Order.DESC),

    /**
     * Process activation state descending order
     */
    ACTIVATION_STATE_DESC(ProcessDeploymentInfoSearchDescriptor.ACTIVATION_STATE, Order.DESC),

    /**
     * Process configuration state descending order
     */
    CONFIGURATION_STATE_DESC(ProcessDeploymentInfoSearchDescriptor.CONFIGURATION_STATE, Order.DESC),

    /**
     * Default criterion
     */
    DEFAULT(ProcessDeploymentInfoSearchDescriptor.NAME, Order.ASC);

    private final String field;

    private final Order order;

    ProcessDeploymentInfoCriterion(final String field, final Order order) {
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
