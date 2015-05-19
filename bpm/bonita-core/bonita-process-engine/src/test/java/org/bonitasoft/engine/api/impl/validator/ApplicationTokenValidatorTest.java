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
package org.bonitasoft.engine.api.impl.validator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ApplicationTokenValidatorTest {

    private ApplicationTokenValidator validator = new ApplicationTokenValidator();

    @Test
    public void isValid_should_return_true_if_value_is_alphanumeric() throws Exception {
        //when
        final ValidationStatus valid = validator.validate("123name");

        //then
        assertThat(valid.isValid()).isTrue();
    }

    @Test
    public void isValid_should_return_false_if_value_contains_special_characters() throws Exception {
        //given
        String token = "123/name";

        //when
        final ValidationStatus valid = validator.validate(token);

        //then
        String message = "The token '"
                + token
                + "' is invalid: the token can not be null or empty and should contain only alpha numeric characters and the following special characters '-', '.', '_' or '~'";
        assertThat(valid.isValid()).isFalse();
        assertThat(valid.getMessage()).isEqualTo(message);
    }

    @Test
    public void isValid_should_return_false_if_value_contains_space() throws Exception {
        //when
        final ValidationStatus valid = validator.validate("my name");

        //then
        assertThat(valid.isValid()).isFalse();
    }

    @Test
    public void value_can_contains_hyphen() throws Exception {
        //when
        final ValidationStatus valid = validator.validate("my-name");

        //then
        assertThat(valid.isValid()).isTrue();
    }

    @Test
    public void value_can_contains_dot() throws Exception {
        //when
        final ValidationStatus valid = validator.validate("my.name");

        //then
        assertThat(valid.isValid()).isTrue();
    }

    @Test
    public void value_can_contains_underscore() throws Exception {
        //when
        final ValidationStatus valid = validator.validate("my_name");

        //then
        assertThat(valid.isValid()).isTrue();
    }

    @Test
    public void value_can_contains_tilde() throws Exception {
        //when
        final ValidationStatus valid = validator.validate("my~name");

        //then
        assertThat(valid.isValid()).isTrue();
    }

    @Test
    public void value_can_contains_all_authorized_symbols() throws Exception {
        //when
        final ValidationStatus valid = validator.validate("m-y.n_a~-m.e");

        //then
        assertThat(valid.isValid()).isTrue();
    }

    @Test
    public void value_cannot_be_empty() throws Exception {
        //when
        final ValidationStatus valid = validator.validate("");

        //then
        assertThat(valid.isValid()).isFalse();
    }

    @Test
    public void value_cannot_be_null() throws Exception {
        //when
        final ValidationStatus valid = validator.validate(null);

        //then
        assertThat(valid.isValid()).isFalse();
    }

}
