/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 *
 */
package com.bonitasoft.engine.bdm.validator.rule;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.model.SimpleField;
import com.bonitasoft.engine.bdm.model.FieldType;
import com.bonitasoft.engine.bdm.model.UniqueConstraint;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Romain Bioteau
 *
 */
public class FieldValidationRuleTest {

	private FieldValidationRule fieldValidationRule;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		fieldValidationRule = new FieldValidationRule();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void shoudAppliesTo_UniqueConstraint() throws Exception {
		assertThat(fieldValidationRule.appliesTo(new BusinessObjectModel())).isFalse();
		assertThat(fieldValidationRule.appliesTo(new BusinessObject())).isFalse();
		assertThat(fieldValidationRule.appliesTo(new SimpleField())).isTrue();
		assertThat(fieldValidationRule.appliesTo(new UniqueConstraint())).isFalse();
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouddCheckRule_throw_IllegalArgumentException() throws Exception {
		fieldValidationRule.checkRule(new BusinessObject());
	}
	
	@Test
	public void shoudCheckRule_returns_valid_status() throws Exception {
		SimpleField field = new SimpleField();
		field.setName("name");
		field.setType(FieldType.STRING);
		ValidationStatus validationStatus = fieldValidationRule.checkRule(field);
		assertThat(validationStatus.isOk()).isTrue();
	}
	
	@Test
	public void shoudCheckRule_returns_error_status() throws Exception {
		SimpleField field = new SimpleField();
		field.setName("name");
		ValidationStatus validationStatus = fieldValidationRule.checkRule(field);
		assertThat(validationStatus.isOk()).isFalse();

		field = new SimpleField();
		field.setName("");
		field.setType(FieldType.STRING);
		validationStatus = fieldValidationRule.checkRule(field);
		assertThat(validationStatus.isOk()).isFalse();
		
		field.setName(null);
		validationStatus = fieldValidationRule.checkRule(field);
		assertThat(validationStatus.isOk()).isFalse();
		
		field.setName("with whitespaces ");
		validationStatus = fieldValidationRule.checkRule(field);
		assertThat(validationStatus.isOk()).isFalse();
		
		field.setName("persisTenceId");
		validationStatus = fieldValidationRule.checkRule(field);
		assertThat(validationStatus.isOk()).isFalse();
		
		field.setName("PersisTenceVersion");
		validationStatus = fieldValidationRule.checkRule(field);
		assertThat(validationStatus.isOk()).isFalse();
	
		field.setName("import");
		validationStatus = fieldValidationRule.checkRule(field);
		assertThat(validationStatus.isOk()).isFalse();
	}

}
