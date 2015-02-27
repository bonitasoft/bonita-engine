/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.validator.rule;

import static com.bonitasoft.engine.bdm.validator.assertion.ValidationStatusAssert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.bdm.model.field.FieldType;
import com.bonitasoft.engine.bdm.model.field.SimpleField;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Colin PUY
 */
public class SimpleFieldValidationRuleTest {

    private SimpleFieldValidationRule simpleFieldValidationRule;

    @Before
    public void setUp() {
        simpleFieldValidationRule = new SimpleFieldValidationRule();
    }

    @Test
    public void should_validate_that_type_is_not_empty() {
        SimpleField simpleField = new SimpleField();

        ValidationStatus validationStatus = simpleFieldValidationRule.validate(simpleField);

        assertThat(validationStatus).isNotOk();
    }

    @Test
    public void should_return_a_valid_status_when_type_is_filled() {
        SimpleField simpleField = new SimpleField();
        simpleField.setType(FieldType.BOOLEAN);

        ValidationStatus validationStatus = simpleFieldValidationRule.validate(simpleField);

        assertThat(validationStatus).isOk();
    }
}
