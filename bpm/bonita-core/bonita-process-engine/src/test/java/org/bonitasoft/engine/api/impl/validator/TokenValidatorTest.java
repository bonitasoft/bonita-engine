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

public class TokenValidatorTest {

    @Test
    public void isValid_should_return_true_if_value_is_alphanumeric() throws Exception {
        //given
        TokenValidator validator = new TokenValidator("123name");

        //when
        final boolean valid = validator.validate();

        //then
        assertThat(valid).isTrue();
    }

    @Test
    public void isValid_should_return_false_if_value_contains_special_characters() throws Exception {
        //given
        TokenValidator validator = new TokenValidator("123/name");

        //when
        final boolean valid = validator.validate();

        //then
        assertThat(valid).isFalse();
    }

    @Test
    public void isValid_should_return_false_if_value_contains_space() throws Exception {
        //given
        TokenValidator validator = new TokenValidator("my name");

        //when
        final boolean valid = validator.validate();

        //then
        assertThat(valid).isFalse();
    }

    @Test
    public void value_can_contains_hyphen() throws Exception {
        //given
        TokenValidator validator = new TokenValidator("my-name");

        //when
        final boolean valid = validator.validate();

        //then
        assertThat(valid).isTrue();
    }

    @Test
    public void value_can_contains_dot() throws Exception {
        //given
        TokenValidator validator = new TokenValidator("my.name");

        //when
        final boolean valid = validator.validate();

        //then
        assertThat(valid).isTrue();
    }

    @Test
    public void value_can_contains_underscore() throws Exception {
        //given
        TokenValidator validator = new TokenValidator("my_name");

        //when
        final boolean valid = validator.validate();

        //then
        assertThat(valid).isTrue();
    }

    @Test
    public void value_can_contains_tilde() throws Exception {
        //given
        TokenValidator validator = new TokenValidator("my~name");

        //when
        final boolean valid = validator.validate();

        //then
        assertThat(valid).isTrue();
    }

    @Test
    public void value_can_contains_all_authorized_symbols() throws Exception {
        //given
        TokenValidator validator = new TokenValidator("m-y.n_a~-m.e");

        //when
        final boolean valid = validator.validate();

        //then
        assertThat(valid).isTrue();
    }

    @Test
    public void value_cannot_be_empty() throws Exception {
        //given
        TokenValidator validator = new TokenValidator("");

        //when
        final boolean valid = validator.validate();

        //then
        assertThat(valid).isFalse();
    }

    @Test
    public void value_cannot_be_null() throws Exception {
        //given
        TokenValidator validator = new TokenValidator(null);

        //when
        final boolean valid = validator.validate();

        //then
        assertThat(valid).isFalse();
    }

    @Test
    public void getError_should_return_null_when_is_valid() throws Exception {
        //given
        TokenValidator validator = new TokenValidator("123name");

        //when
        validator.validate();
        String error = validator.getError();

        //then
        assertThat(error).isNull();
    }

    @Test
    public void getError_should_return_message_when_is_invalid() throws Exception {
        //given
        String token = "token with spaces";
        TokenValidator validator = new TokenValidator(token);

        //when
        validator.validate();
        String error = validator.getError();

        //then
        String message = "The token '"
                + token
                + "' is invalid: the token can not be null or empty and should contain only alpha numeric characters and the following special characters '-', '.', '_' or '~'";
        assertThat(error).isEqualTo(message);
    }

}
