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

import org.junit.Before;
import org.junit.Test;

import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.SimpleField;
import org.bonitasoft.engine.bdm.validator.ValidationStatus;

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
