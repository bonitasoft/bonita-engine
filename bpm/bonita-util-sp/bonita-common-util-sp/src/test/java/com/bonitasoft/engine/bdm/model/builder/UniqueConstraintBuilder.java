/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.model.builder;

import static java.util.Arrays.asList;

import com.bonitasoft.engine.bdm.model.UniqueConstraint;

/**
 * @author Colin PUY
 */
public class UniqueConstraintBuilder {

    private UniqueConstraint uniqueConstraint = new UniqueConstraint();

    public static UniqueConstraintBuilder aUniqueConstraint() {
        return new UniqueConstraintBuilder();
    }

    public UniqueConstraintBuilder withName(String name) {
        uniqueConstraint.setName(name);
        return this;
    }

    public UniqueConstraintBuilder withFieldNames(String... fieldNames) {
        uniqueConstraint.setFieldNames(asList(fieldNames));
        return this;
    }

    public UniqueConstraint build() {
        return uniqueConstraint;
    }
}
