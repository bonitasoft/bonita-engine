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
package org.bonitasoft.engine.bdm.validator.rule;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import org.bonitasoft.engine.bdm.validator.ValidationStatus;

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
