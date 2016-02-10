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
public class SQLNameValidatorTest {

    private SQLNameValidator sqlNameValidator;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() {
        sqlNameValidator = new SQLNameValidator();
    }

    @Test
    public void shouldIsValid_ReturnsTrue() {
        assertThat(sqlNameValidator.isValid("EMPLOYEE")).isTrue();
        assertThat(sqlNameValidator.isValid("employee")).isTrue();
        assertThat(sqlNameValidator.isValid("employee_#")).isTrue();
        assertThat(sqlNameValidator.isValid("name")).isTrue();
    }

    @Test
    public void shouldSQLKeyword__Have_Correct_Size() {
        assertThat(SQLNameValidator.sqlKeywords.size()).isEqualTo(337);
    }

    @Test
    public void shouldIsValid_ReturnsFalse() {
        assertThat(sqlNameValidator.isValid("E MPLOYEE")).isFalse();
        assertThat(sqlNameValidator.isValid("@employee")).isFalse();
        assertThat(sqlNameValidator.isValid("5employee")).isFalse();
        assertThat(sqlNameValidator.isValid("émployee")).isFalse();
        assertThat(sqlNameValidator.isValid("employee.name")).isFalse();
        assertThat(sqlNameValidator.isValid("order")).isFalse();
        assertThat(sqlNameValidator.isValid("SCOPE")).isFalse();
    }

    @Test
    public void shouldIsValid_ReturnsFalse_if_too_long() {
        sqlNameValidator = new SQLNameValidator(30);
        assertThat(sqlNameValidator.isValid("IMTOOLONGANDVALIDATIONSHOULDFAILED")).isFalse();
        assertThat(sqlNameValidator.isValid("IMNOTTOOLONG")).isTrue();
    }

}
