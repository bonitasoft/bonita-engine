package com.bonitasoft.engine.bdm.validator;

/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 *
 */
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Romain Bioteau
 *
 */
public class ValidationStatusTest {

	private ValidationStatus validationStatus;

	@Before
	public void setUp() throws Exception {
		validationStatus = new ValidationStatus();
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void shouldIsOk_returns_true() throws Exception {
		assertThat(validationStatus.isOk()).isTrue();
	}
	
	@Test
	public void shouldIsOk_returns_false_if_contains_erros() throws Exception {
		validationStatus.addError("an error");
		assertThat(validationStatus.isOk()).isFalse();
	}
	
	@Test
	public void shouldIsOk_returns_false_if_add_a_status_with_erros() throws Exception {
		ValidationStatus status = new ValidationStatus();
		status.addError("an error");
		validationStatus.addValidationStatus(status);
		assertThat(validationStatus.isOk()).isFalse();
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldAddError_throw_an_IllegalArgumentException_for_null_input() throws Exception {
		validationStatus.addError(null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldAddError_throw_an_IllegalArgumentException_for_empty_input() throws Exception {
		validationStatus.addError(null);
	}

}
