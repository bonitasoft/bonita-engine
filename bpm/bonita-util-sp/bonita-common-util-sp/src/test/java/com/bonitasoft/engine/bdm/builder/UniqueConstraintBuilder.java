/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.builder;

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

    public UniqueConstraint build() {
        return uniqueConstraint;
    }
}
