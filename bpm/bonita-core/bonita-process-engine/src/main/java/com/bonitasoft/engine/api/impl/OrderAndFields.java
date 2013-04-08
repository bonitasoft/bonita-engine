/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package com.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.persistence.OrderAndField;
import org.bonitasoft.engine.persistence.OrderByType;

import com.bonitasoft.engine.bpm.model.breakpoint.BreakpointCriterion;
import com.bonitasoft.engine.core.process.instance.model.builder.SBreakpointBuilder;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class OrderAndFields extends org.bonitasoft.engine.api.impl.OrderAndFields {

    static OrderAndField getOrderAndFieldForBreakpoints(final BreakpointCriterion sort, final SBreakpointBuilder builder) {
        String field;
        OrderByType type;
        switch (sort) {
            case DEFINITION_ID_ASC:
                field = builder.getDefinitionIdKey();
                type = OrderByType.ASC;
                break;
            case DEFINITION_ID_DESC:
                field = builder.getDefinitionIdKey();
                type = OrderByType.DESC;
                break;
            case ELEMENT_NAME_ASC:
                field = builder.getElementNameKey();
                type = OrderByType.ASC;
                break;
            case ELEMENT_NAME_DESC:
                field = builder.getElementNameKey();
                type = OrderByType.DESC;
                break;
            case INSTANCE_ID_ASC:
                field = builder.getInstanceIdKey();
                type = OrderByType.ASC;
                break;
            case INSTANCE_ID_DESC:
                field = builder.getInstanceIdKey();
                type = OrderByType.DESC;
                break;
            case STATE_ID_ASC:
                field = builder.getStateIdKey();
                type = OrderByType.ASC;
                break;
            case STATE_ID_DESC:
                field = builder.getStateIdKey();
                type = OrderByType.DESC;
                break;
            default:
                field = null;
                type = null;
                break;
        }
        return new OrderAndField(type, field);
    }
}
