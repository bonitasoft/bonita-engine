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
public class ValidationStatusTest {

    private ValidationStatus validationStatus;

    @Before
    public void setUp() {
        validationStatus = new ValidationStatus();
    }

    @Test
    public void shouldIsOk_returns_true() {
        assertThat(validationStatus.isOk()).isTrue();
    }

    @Test
    public void shouldIsOk_returns_false_if_contains_erros() {
        validationStatus.addError("an error");
        assertThat(validationStatus.isOk()).isFalse();
    }

    @Test
    public void shouldIsOk_returns_false_if_add_a_status_with_erros() {
        ValidationStatus status = new ValidationStatus();
        status.addError("an error");
        validationStatus.addValidationStatus(status);
        assertThat(validationStatus.isOk()).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldAddError_throw_an_IllegalArgumentException_for_null_input() {
        validationStatus.addError(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldAddError_throw_an_IllegalArgumentException_for_empty_input() {
        validationStatus.addError(null);
    }

}
