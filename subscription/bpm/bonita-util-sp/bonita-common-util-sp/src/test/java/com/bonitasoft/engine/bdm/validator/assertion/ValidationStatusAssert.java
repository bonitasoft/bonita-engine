/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.validator.assertion;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import com.bonitasoft.engine.bdm.validator.ValidationStatus;

public class ValidationStatusAssert extends AbstractAssert<ValidationStatusAssert, ValidationStatus> {

    protected ValidationStatusAssert(ValidationStatus actual) {
        super(actual, ValidationStatusAssert.class);
    }

    public static ValidationStatusAssert assertThat(ValidationStatus actual) {
        return new ValidationStatusAssert(actual);
    }

    public ValidationStatusAssert isOk() {
        Assertions.assertThat(actual.isOk()).isTrue();
        return this;
    }

    public ValidationStatusAssert isNotOk() {
        Assertions.assertThat(actual.isOk()).isFalse();
        return this;
    }
}
