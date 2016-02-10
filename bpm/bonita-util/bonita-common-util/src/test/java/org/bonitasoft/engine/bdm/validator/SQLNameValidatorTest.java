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
package org.bonitasoft.engine.bdm.validator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Romain Bioteau
 */
public class SQLNameValidatorTest {

    private SQLNameValidator sqlNameValidator;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() {
        sqlNameValidator = new SQLNameValidator();
    }

    @Test
    public void shouldIsValid_ReturnsTrue() {
        assertThat(sqlNameValidator.isValid("EMPLOYEE")).isTrue();
        assertThat(sqlNameValidator.isValid("employee")).isTrue();
        assertThat(sqlNameValidator.isValid("employee_#")).isTrue();
        assertThat(sqlNameValidator.isValid("name")).isTrue();
    }

    @Test
    public void shouldSQLKeyword__Have_Correct_Size() {
        assertThat(SQLNameValidator.sqlKeywords.size()).isEqualTo(337);
    }

    @Test
    public void shouldIsValid_ReturnsFalse() {
        assertThat(sqlNameValidator.isValid("E MPLOYEE")).isFalse();
        assertThat(sqlNameValidator.isValid("@employee")).isFalse();
        assertThat(sqlNameValidator.isValid("5employee")).isFalse();
        assertThat(sqlNameValidator.isValid("émployee")).isFalse();
        assertThat(sqlNameValidator.isValid("employee.name")).isFalse();
        assertThat(sqlNameValidator.isValid("order")).isFalse();
        assertThat(sqlNameValidator.isValid("SCOPE")).isFalse();
    }

    @Test
    public void shouldIsValid_ReturnsFalse_if_too_long() {
        sqlNameValidator = new SQLNameValidator(30);
        assertThat(sqlNameValidator.isValid("IMTOOLONGANDVALIDATIONSHOULDFAILED")).isFalse();
        assertThat(sqlNameValidator.isValid("IMNOTTOOLONG")).isTrue();
    }

}
