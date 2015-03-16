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

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.UniqueConstraint;
import org.bonitasoft.engine.bdm.model.field.SimpleField;
import org.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Romain Bioteau
 */
public class UniqueConstraintValidationRuleTest {

    private UniqueConstraintValidationRule uniqueConstraintValidationRule;

    @Before
    public void setUp() {
        uniqueConstraintValidationRule = new UniqueConstraintValidationRule();
    }

    @Test
    public void shoudAppliesTo_UniqueConstraint() {
        assertThat(uniqueConstraintValidationRule.appliesTo(new BusinessObjectModel())).isFalse();
        assertThat(uniqueConstraintValidationRule.appliesTo(new BusinessObject())).isFalse();
        assertThat(uniqueConstraintValidationRule.appliesTo(new SimpleField())).isFalse();
        assertThat(uniqueConstraintValidationRule.appliesTo(new UniqueConstraint())).isTrue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouddCheckRule_throw_IllegalArgumentException() {
        uniqueConstraintValidationRule.checkRule(new BusinessObject());
    }

    @Test
    public void shoudCheckRule_returns_valid_status() {
        UniqueConstraint uc = new UniqueConstraint();
        uc.setName("MY_CONSTRAINT_");
        uc.setFieldNames(Arrays.asList("f1"));
        ValidationStatus validationStatus = uniqueConstraintValidationRule.validate(uc);
        assertThat(validationStatus).isOk();
    }

    @Test
    public void shoudCheckRule_returns_error_status() {
        UniqueConstraint uc = new UniqueConstraint();
        uc.setName("MY_CONSTRAINT_");
        uc.setFieldNames(Collections.<String> emptyList());
        ValidationStatus validationStatus = uniqueConstraintValidationRule.validate(uc);
        assertThat(validationStatus).isNotOk();

        uc = new UniqueConstraint();
        uc.setName("");
        uc.setFieldNames(Arrays.asList("f1"));
        validationStatus = uniqueConstraintValidationRule.validate(uc);
        assertThat(validationStatus).isNotOk();

        uc.setName(null);
        validationStatus = uniqueConstraintValidationRule.validate(uc);
        assertThat(validationStatus).isNotOk();

        uc.setName("with whitespaces ");
        validationStatus = uniqueConstraintValidationRule.validate(uc);
        assertThat(validationStatus).isNotOk();
    }

}
