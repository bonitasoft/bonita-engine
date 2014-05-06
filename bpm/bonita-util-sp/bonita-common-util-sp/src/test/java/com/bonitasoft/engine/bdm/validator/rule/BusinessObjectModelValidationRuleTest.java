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
import com.bonitasoft.engine.bdm.model.Field;
import com.bonitasoft.engine.bdm.model.UniqueConstraint;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Romain Bioteau
 *
 */
public class BusinessObjectModelValidationRuleTest {

	private BusinessObjectModelValidationRule businessObjectModelValidationRule;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		businessObjectModelValidationRule = new BusinessObjectModelValidationRule();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void shoudAppliesTo_UniqueConstraint() throws Exception {
		assertThat(businessObjectModelValidationRule.appliesTo(new BusinessObjectModel())).isTrue();
		assertThat(businessObjectModelValidationRule.appliesTo(new BusinessObject())).isFalse();
		assertThat(businessObjectModelValidationRule.appliesTo(new Field())).isFalse();
		assertThat(businessObjectModelValidationRule.appliesTo(new UniqueConstraint())).isFalse();
	}


	@Test(expected=IllegalArgumentException.class)
	public void shouddCheckRule_throw_IllegalArgumentException() throws Exception {
		businessObjectModelValidationRule.checkRule(new Field());
	}
	
	@Test
	public void shoudCheckRule_returns_valid_status() throws Exception {
		BusinessObjectModel bom = new BusinessObjectModel();
		BusinessObject bo = new BusinessObject();
		bom.addBusinessObject(bo);
		ValidationStatus validationStatus = businessObjectModelValidationRule.checkRule(bom);
		assertThat(validationStatus.isOk()).isTrue();
	}
	
	@Test
	public void shoudCheckRule_returns_error_status() throws Exception {
		BusinessObjectModel bom = new BusinessObjectModel();
		ValidationStatus validationStatus = businessObjectModelValidationRule.checkRule(bom);
		assertThat(validationStatus.isOk()).isFalse();
	}
}
