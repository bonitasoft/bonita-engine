/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.validator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Romain Bioteau
 */
public class ValidationStatusTest {

    private ValidationStatus validationStatus;

    @Before
    public void setUp() {
        validationStatus = new ValidationStatus();
    }

    @Test
    public void shouldIsOk_returns_true() {
        assertThat(validationStatus.isOk()).isTrue();
    }

    @Test
    public void shouldIsOk_returns_false_if_contains_erros() {
        validationStatus.addError("an error");
        assertThat(validationStatus.isOk()).isFalse();
    }

    @Test
    public void shouldIsOk_returns_false_if_add_a_status_with_erros() {
        ValidationStatus status = new ValidationStatus();
        status.addError("an error");
        validationStatus.addValidationStatus(status);
        assertThat(validationStatus.isOk()).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldAddError_throw_an_IllegalArgumentException_for_null_input() {
        validationStatus.addError(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldAddError_throw_an_IllegalArgumentException_for_empty_input() {
        validationStatus.addError(null);
    }

}
