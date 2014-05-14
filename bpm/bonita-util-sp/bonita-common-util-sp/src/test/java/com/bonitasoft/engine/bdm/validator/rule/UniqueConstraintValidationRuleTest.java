/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 *
 */
package com.bonitasoft.engine.bdm.validator.rule;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.model.UniqueConstraint;
import com.bonitasoft.engine.bdm.model.field.SimpleField;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * 
 * @author Romain Bioteau
 *
 */
public class UniqueConstraintValidationRuleTest {

	private UniqueConstraintValidationRule uniqueConstraintValidationRule;

	@Before
	public void setUp() throws Exception {
		uniqueConstraintValidationRule = new UniqueConstraintValidationRule();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void shoudAppliesTo_UniqueConstraint() throws Exception {
		assertThat(uniqueConstraintValidationRule.appliesTo(new BusinessObjectModel())).isFalse();
		assertThat(uniqueConstraintValidationRule.appliesTo(new BusinessObject())).isFalse();
		assertThat(uniqueConstraintValidationRule.appliesTo(new SimpleField())).isFalse();
		assertThat(uniqueConstraintValidationRule.appliesTo(new UniqueConstraint())).isTrue();
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouddCheckRule_throw_IllegalArgumentException() throws Exception {
		uniqueConstraintValidationRule.checkRule(new BusinessObject());
	}

	@Test
	public void shoudCheckRule_returns_valid_status() throws Exception {
		UniqueConstraint uc = new UniqueConstraint();
		uc.setName("MY_CONSTRAINT_");
		uc.setFieldNames(Arrays.asList("f1"));
		ValidationStatus validationStatus = uniqueConstraintValidationRule.validate(uc);
		assertThat(validationStatus.isOk()).isTrue();
	}

	@Test
	public void shoudCheckRule_returns_error_status() throws Exception {
		UniqueConstraint uc = new UniqueConstraint();
		uc.setName("MY_CONSTRAINT_");
		uc.setFieldNames(Collections.<String>emptyList());
		ValidationStatus validationStatus = uniqueConstraintValidationRule.validate(uc);
		assertThat(validationStatus.isOk()).isFalse();

		uc = new UniqueConstraint();
		uc.setName("");
		uc.setFieldNames(Arrays.asList("f1"));
		validationStatus = uniqueConstraintValidationRule.validate(uc);
		assertThat(validationStatus.isOk()).isFalse();
		
		uc.setName(null);
		validationStatus = uniqueConstraintValidationRule.validate(uc);
		assertThat(validationStatus.isOk()).isFalse();
		
		uc.setName("with whitespaces ");
		validationStatus = uniqueConstraintValidationRule.validate(uc);
		assertThat(validationStatus.isOk()).isFalse();
	}



}
