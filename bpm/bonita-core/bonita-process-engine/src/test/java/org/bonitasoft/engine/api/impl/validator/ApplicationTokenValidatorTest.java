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
        //when
        final ValidationStatus valid = validator.validate("123/name");

        //then
        assertThat(valid.isValid()).isFalse();
        assertThat(valid.getMessage()).isEqualTo(getMessage("123/name"));
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
        ValidationStatusAssert.assertThat(valid).isNotValid().hasMessage(getMessage(""));

    }

    @Test
    public void value_cannot_contain_only_white_spaces() throws Exception {
        //when
        final ValidationStatus valid = validator.validate("  ");

        //then
        ValidationStatusAssert.assertThat(valid).isNotValid().hasMessage(getMessage("  "));

    }

    @Test
    public void value_cannot_be_null() throws Exception {
        //when
        final ValidationStatus valid = validator.validate(null);

        //then
        ValidationStatusAssert.assertThat(valid).isNotValid().hasMessage(getMessage(null));
    }

    @Test
    public void should_be_invalid_if_token_contains_keyword_content_lower_case() throws Exception {
        //when
        ValidationStatus status = validator.validate("content");

        //then
        ValidationStatusAssert.assertThat(status).isNotValid()
                .hasMessage(getMessage("content"));
    }

    @Test
    public void should_be_invalid_if_token_contains_keyword_content_up_case() throws Exception {
        //when
        String token = "CONTENT";
        ValidationStatus status = validator.validate(token);

        //then
        ValidationStatusAssert.assertThat(status).isNotValid()
                .hasMessage(getMessage("CONTENT"));
    }

    @Test
    public void should_be_invalid_if_token_contains_keyword_api_lower_case() throws Exception {
        //when
        ValidationStatus status = validator.validate("api");

        //then
        ValidationStatusAssert.assertThat(status).isNotValid()
                .hasMessage(getMessage("api"));
    }

    public String getMessage(String token) {
        StringBuilder stb = new StringBuilder("The token '");
        stb.append(token);
        stb.append("' is invalid: the token can not be null or empty and should contain only alpha numeric characters and the following ");
        stb.append("special characters '-', '.', '_' or '~'. In addition, the following words are reserved keywords and cannot be used as token: 'api', 'content', 'theme'.");
        return stb.toString();
    }

    @Test
    public void should_be_invalid_if_token_contains_keyword_api_up_case() throws Exception {
        //when
        ValidationStatus status = validator.validate("API");

        //then
        ValidationStatusAssert.assertThat(status).isNotValid()
                .hasMessage(getMessage("API"));
    }

    @Test
    public void should_be_invalid_if_token_contains_keyword_theme_lower_case() throws Exception {
        //when
        ValidationStatus status = validator.validate("theme");

        //then
        ValidationStatusAssert.assertThat(status).isNotValid()
                .hasMessage(getMessage("theme"));
    }

    @Test
    public void should_be_invalid_if_token_contains_keyword_theme_up_case() throws Exception {
        //when
        ValidationStatus status = validator.validate("THEME");

        //then
        ValidationStatusAssert.assertThat(status).isNotValid()
                .hasMessage(getMessage("THEME"));
    }

}
