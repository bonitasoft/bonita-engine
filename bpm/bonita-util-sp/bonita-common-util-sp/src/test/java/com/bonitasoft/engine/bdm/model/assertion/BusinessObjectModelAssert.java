/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.model.assertion;

import org.assertj.core.api.AbstractAssert;

import com.bonitasoft.engine.bdm.model.BusinessObjectModel;

/**
 * @author Colin PUY
 */
public class BusinessObjectModelAssert extends AbstractAssert<BusinessObjectModelAssert, BusinessObjectModel> {

    protected BusinessObjectModelAssert(final BusinessObjectModel actual) {
        super(actual, BusinessObjectModelAssert.class);
    }

    public static BusinessObjectModelAssert assertThat(final BusinessObjectModel actual) {
        return new BusinessObjectModelAssert(actual);
    }

    public BusinessObjectModelAssert canBeMarshalled() {
        try {
            BusinessObjectModel bom = Marshaller.marshallUnmarshall(actual);
            isEqualTo(bom);
        } catch (Exception e) {
            failWithMessage("Expected <%s> to be marshallizable : <%s>", actual, e.getCause());
        }
        return this;
    }

    public BusinessObjectModelAssert cannotBeMarshalled() {
        try {
            Marshaller.marshallUnmarshall(actual);
            failWithMessage("Expected <%s> not to be marshallizable", actual);
        } catch (Exception e) {
            // OK
        }
        return this;
    }
}
