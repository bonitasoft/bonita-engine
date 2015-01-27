/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/
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
