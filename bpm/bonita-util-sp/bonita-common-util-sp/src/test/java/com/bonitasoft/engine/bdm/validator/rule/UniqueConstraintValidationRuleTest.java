/*******************************************************************************
 * Copyright (C) 2013-2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.validator.rule;

import static com.bonitasoft.engine.bdm.validator.assertion.ValidationStatusAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.model.UniqueConstraint;
import com.bonitasoft.engine.bdm.model.field.SimpleField;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;

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
