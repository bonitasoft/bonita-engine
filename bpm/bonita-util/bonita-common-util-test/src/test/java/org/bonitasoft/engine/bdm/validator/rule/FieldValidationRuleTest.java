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

import static org.bonitasoft.engine.bdm.validator.assertion.ValidationStatusAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.UniqueConstraint;
import org.bonitasoft.engine.bdm.model.field.Field;
import org.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Romain Bioteau
 */
public class FieldValidationRuleTest {

    private FieldValidationRule fieldValidationRule;

    @Before
    public void setUp() {
        fieldValidationRule = new FieldValidationRule();
    }

    private Field aFieldWithName(String name) {
        FakeField field = new FakeField();
        field.setName(name);
        return field;
    }

    @Test
    public void should_apply_to_fields() {
        assertThat(fieldValidationRule.appliesTo(new BusinessObjectModel())).isFalse();
        assertThat(fieldValidationRule.appliesTo(new BusinessObject())).isFalse();
        assertThat(fieldValidationRule.appliesTo(new UniqueConstraint())).isFalse();

        assertThat(fieldValidationRule.appliesTo(aFieldWithName("aName"))).isTrue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldCheckRule_throw_IllegalArgumentException() {
        fieldValidationRule.checkRule(new BusinessObject());
    }

    @Test
    public void should_return_a_valid_status_when_name_is_not_a_forbidden_one() {
        Field field = aFieldWithName("aGoodName");

        ValidationStatus validationStatus = fieldValidationRule.validate(field);

        assertThat(validationStatus).isOk();
    }

    @Test
    public void should_validate_that_name_is_not_empty() {
        Field field = aFieldWithName("");

        ValidationStatus validationStatus = fieldValidationRule.validate(field);

        assertThat(validationStatus).isNotOk();
    }

    @Test
    public void should_validate_that_name_is_not_null() {
        Field field = aFieldWithName(null);

        ValidationStatus validationStatus = fieldValidationRule.validate(field);

        assertThat(validationStatus).isNotOk();
    }

    @Test
    public void should_validate_that_name_has_no_whitespace() {
        Field field = aFieldWithName("with whitespaces ");

        ValidationStatus validationStatus = fieldValidationRule.validate(field);

        assertThat(validationStatus).isNotOk();
    }

    @Test
    public void should_validate_that_name_is_not_a_java_keyword() {
        Field field = aFieldWithName("import");

        ValidationStatus validationStatus = fieldValidationRule.validate(field);

        assertThat(validationStatus).isNotOk();
    }

    @Test
    public void should_validate_that_name_is_not_persistenceId_wich_is_a_business_data_model_keyword() {
        Field field = aFieldWithName(Field.PERSISTENCE_ID.toUpperCase());

        ValidationStatus validationStatus = fieldValidationRule.validate(field);

        assertThat(validationStatus).isNotOk();
    }

    @Test
    public void should_validate_that_name_is_not_persistenceVersion_wich_is_a_business_data_model_keyword() {
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
