/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * 
 */
package com.bonitasoft.engine.bdm.validator.rule;

import static com.bonitasoft.engine.bdm.validator.assertion.ValidationStatusAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.model.UniqueConstraint;
import com.bonitasoft.engine.bdm.model.field.Field;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Romain Bioteau
 * 
 */
public class FieldValidationRuleTest {

    private FieldValidationRule fieldValidationRule;

    @Before
    public void setUp() throws Exception {
        fieldValidationRule = new FieldValidationRule();
    }

    private Field aFieldWithName(String name) {
        FakeField field = new FakeField();
        field.setName(name);
        return field;
    }

    @Test
    public void should_apply_to_fields() throws Exception {
        assertThat(fieldValidationRule.appliesTo(new BusinessObjectModel())).isFalse();
        assertThat(fieldValidationRule.appliesTo(new BusinessObject())).isFalse();
        assertThat(fieldValidationRule.appliesTo(new UniqueConstraint())).isFalse();

        assertThat(fieldValidationRule.appliesTo(aFieldWithName("aName"))).isTrue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldCheckRule_throw_IllegalArgumentException() throws Exception {
        fieldValidationRule.checkRule(new BusinessObject());
    }

    @Test
    public void should_return_a_valid_status_when_name_is_not_a_forbidden_one() throws Exception {
        Field field = aFieldWithName("aGoodName");

        ValidationStatus validationStatus = fieldValidationRule.validate(field);

        assertThat(validationStatus).isOk();
    }

    @Test
    public void should_validate_that_name_is_not_empty() throws Exception {
        Field field = aFieldWithName("");

        ValidationStatus validationStatus = fieldValidationRule.validate(field);

        assertThat(validationStatus).isNotOk();
    }

    @Test
    public void should_validate_that_name_is_not_null() throws Exception {
        Field field = aFieldWithName(null);

        ValidationStatus validationStatus = fieldValidationRule.validate(field);

        assertThat(validationStatus).isNotOk();
    }

    @Test
    public void should_validate_that_name_has_no_whitespace() throws Exception {
        Field field = aFieldWithName("with whitespaces ");

        ValidationStatus validationStatus = fieldValidationRule.validate(field);

        assertThat(validationStatus).isNotOk();
    }

    @Test
    public void should_validate_that_name_is_not_a_java_keyword() throws Exception {
        Field field = aFieldWithName("import");

        ValidationStatus validationStatus = fieldValidationRule.validate(field);

        assertThat(validationStatus).isNotOk();
    }

    @Test
    public void should_validate_that_name_is_not_persistenceId_wich_is_a_business_data_model_keyword() throws Exception {
        Field field = aFieldWithName(Field.PERSISTENCE_ID.toUpperCase());

        ValidationStatus validationStatus = fieldValidationRule.validate(field);

        assertThat(validationStatus).isNotOk();
    }

    @Test
    public void should_validate_that_name_is_not_persistenceVersion_wich_is_a_business_data_model_keyword() throws Exception {
        Field field = aFieldWithName(Field.PERSISTENCE_VERSION.toLowerCase());

        ValidationStatus validationStatus = fieldValidationRule.validate(field);

        assertThat(validationStatus).isNotOk();
    }

    /**
     * Fake field used for tests. Extending abstract class Field with no extras attributes
     * 
     * @author Colin PUY
     */
    private class FakeField extends Field {
    }

}
