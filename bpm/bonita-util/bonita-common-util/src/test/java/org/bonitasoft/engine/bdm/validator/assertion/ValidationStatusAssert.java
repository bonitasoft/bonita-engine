package org.bonitasoft.engine.bdm.validator.assertion;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import org.bonitasoft.engine.bdm.validator.ValidationStatus;

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
