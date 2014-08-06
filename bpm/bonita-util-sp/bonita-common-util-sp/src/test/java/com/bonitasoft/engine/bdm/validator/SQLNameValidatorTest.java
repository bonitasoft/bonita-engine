/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.bonitasoft.engine.bdm.validator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Romain Bioteau
 *
 */
public class SQLNameValidatorTest {

	private SQLNameValidator sqlNameValidator;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		sqlNameValidator = new SQLNameValidator();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void shouldIsValid_ReturnsTrue() throws Exception {
		assertThat(sqlNameValidator.isValid("EMPLOYEE")).isTrue();
		assertThat(sqlNameValidator.isValid("employee")).isTrue();
		assertThat(sqlNameValidator.isValid("employee_#")).isTrue();
		assertThat(sqlNameValidator.isValid("name")).isTrue();
	}
	
	@Test
	public void shouldSQLKeyword__Have_Correct_Size() throws Exception {
		assertThat(SQLNameValidator.sqlKeywords.size()).isEqualTo(337);
	}
	
	@Test
	public void shouldIsValid_ReturnsFalse() throws Exception {
		assertThat(sqlNameValidator.isValid("E MPLOYEE")).isFalse();
		assertThat(sqlNameValidator.isValid("@employee")).isFalse();
		assertThat(sqlNameValidator.isValid("5employee")).isFalse();
		assertThat(sqlNameValidator.isValid("émployee")).isFalse();
		assertThat(sqlNameValidator.isValid("employee.name")).isFalse();
		assertThat(sqlNameValidator.isValid("order")).isFalse();
		assertThat(sqlNameValidator.isValid("SCOPE")).isFalse();
	}
	

	@Test
	public void shouldIsValid_ReturnsFalse_if_too_long() throws Exception {
		sqlNameValidator = new SQLNameValidator(30);
		assertThat(sqlNameValidator.isValid("IMTOOLONGANDVALIDATIONSHOULDFAILED")).isFalse();
		assertThat(sqlNameValidator.isValid("IMNOTTOOLONG")).isTrue();
	}

}
