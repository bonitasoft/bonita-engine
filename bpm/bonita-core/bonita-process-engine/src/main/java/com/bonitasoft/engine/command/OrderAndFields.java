/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.command;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.persistence.OrderAndField;
import org.bonitasoft.engine.persistence.OrderByType;

import com.bonitasoft.engine.bpm.breakpoint.BreakpointCriterion;
import com.bonitasoft.engine.core.process.instance.model.builder.SBreakpointBuilderFactory;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class OrderAndFields extends org.bonitasoft.engine.api.impl.OrderAndFields {

    static OrderAndField getOrderAndFieldForBreakpoints(final BreakpointCriterion sort) {
        final SBreakpointBuilderFactory keyProvider = BuilderFactory.get(SBreakpointBuilderFactory.class);
        String field;
        OrderByType type;
        switch (sort) {
            case DEFINITION_ID_ASC:
                field = keyProvider.getDefinitionIdKey();
                type = OrderByType.ASC;
                break;
            case DEFINITION_ID_DESC:
                field = keyProvider.getDefinitionIdKey();
                type = OrderByType.DESC;
                break;
            case ELEMENT_NAME_ASC:
                field = keyProvider.getElementNameKey();
                type = OrderByType.ASC;
                break;
            case ELEMENT_NAME_DESC:
                field = keyProvider.getElementNameKey();
                type = OrderByType.DESC;
                break;
            case INSTANCE_ID_ASC:
                field = keyProvider.getInstanceIdKey();
                type = OrderByType.ASC;
                break;
            case INSTANCE_ID_DESC:
                field = keyProvider.getInstanceIdKey();
                type = OrderByType.DESC;
                break;
            case STATE_ID_ASC:
                field = keyProvider.getStateIdKey();
                type = OrderByType.ASC;
                break;
            case STATE_ID_DESC:
                field = keyProvider.getStateIdKey();
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
