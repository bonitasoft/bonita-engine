/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.validator.rule;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Colin PUY
 */
public class ValidationRuleTest {

    @Test
    public void should_apply_to_type_parameter_class() {
        ExceptionRule objectRule = new ExceptionRule();

        boolean apply = objectRule.appliesTo(new Exception());

        assertThat(apply).isTrue();
    }

    @Test
    public void should_apply_to_type_parameter_subclass() {
        ExceptionRule objectRule = new ExceptionRule();

        boolean apply = objectRule.appliesTo(new RuntimeException());

        assertThat(apply).isTrue();
    }

    @Test
    public void should_not_apply_to_other_type() {
        ExceptionRule objectRule = new ExceptionRule();

        boolean apply = objectRule.appliesTo(new Throwable());

        assertThat(apply).isFalse();
    }

    @Test
    public void should_not_apply_to_null() {
        ExceptionRule objectRule = new ExceptionRule();

        boolean apply = objectRule.appliesTo(null);

        assertThat(apply).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_exception_when_trying_to_check_rule_on_object_that_rule_cannot_apply_on() {
        ExceptionRule objectRule = new ExceptionRule();

        objectRule.checkRule(new String());
    }

    @Test
    public void should_validate_object_according_to_the_implemented_validation_strategy() {
        ValidationStatus expectedValidationStatus = new ValidationStatus();
        ExceptionRule objectRule = new ExceptionRule(expectedValidationStatus);

        ValidationStatus status = objectRule.checkRule(new Exception());

        assertThat(status).isEqualTo(expectedValidationStatus);
    }

    /**
     * ValidationRule test implementation - return the given validationStatus
     * 
     * @author Colin PUY
     */
    private class ExceptionRule extends ValidationRule<Exception> {

        private ValidationStatus validationStatus;

        public ExceptionRule() {
            super(Exception.class);
        }

        public ExceptionRule(ValidationStatus validationStatus) {
            this();
            this.validationStatus = validationStatus;
        }

        @Override
        protected ValidationStatus validate(Exception modelElement) {
            return validationStatus;
        }
    }
}
