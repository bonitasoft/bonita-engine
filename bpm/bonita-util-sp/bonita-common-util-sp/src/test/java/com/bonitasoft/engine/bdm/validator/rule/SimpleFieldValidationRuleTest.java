/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
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
    public void should_validate_that_type_is_not_empty() throws Exception {
        SimpleField simpleField = new SimpleField();

        ValidationStatus validationStatus = simpleFieldValidationRule.validate(simpleField);

        assertThat(validationStatus).isNotOk();
    }

    @Test
    public void should_return_a_valid_status_when_type_is_filled() throws Exception {
        SimpleField simpleField = new SimpleField();
        simpleField.setType(FieldType.BOOLEAN);

        ValidationStatus validationStatus = simpleFieldValidationRule.validate(simpleField);

        assertThat(validationStatus).isOk();
    }
}
