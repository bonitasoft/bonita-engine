/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class URLValidatorTest {

    @Test
    public void isValid_should_return_true_if_value_is_alphanumeric() throws Exception {
        //when
        final boolean valid = URLValidator.isValid("123name");

        //then
        assertTrue(valid);
    }

    @Test
    public void isValid_should_return_false_if_value_contains_special_characters() throws Exception {
        //when
        final boolean valid = URLValidator.isValid("123/name");

        //then
        assertFalse(valid);
    }

    @Test
    public void isValid_should_return_false_if_value_contains_space() throws Exception {
        //when
        final boolean valid = URLValidator.isValid("my name");

        //then
        assertFalse(valid);
    }

    @Test
    public void value_can_contains_hyphen() throws Exception {
        //when
        final boolean valid = URLValidator.isValid("my-name");

        //then
        assertTrue(valid);
    }

    @Test
    public void value_can_contains_dot() throws Exception {
        //when
        final boolean valid = URLValidator.isValid("my.name");

        //then
        assertTrue(valid);
    }

    @Test
    public void value_can_contains_underscore() throws Exception {
        //when
        final boolean valid = URLValidator.isValid("my_name");

        //then
        assertTrue(valid);
    }

    @Test
    public void value_can_contains_tilde() throws Exception {
        //when
        final boolean valid = URLValidator.isValid("my~name");

        //then
        assertTrue(valid);
    }

    @Test
    public void value_can_contains_all_autorized_symbols() throws Exception {
        //when
        final boolean valid = URLValidator.isValid("m-y.n_a~-m.e");

        //then
        assertTrue(valid);
    }

    @Test
    public void value_cannot_be_empty() throws Exception {
        //when
        final boolean valid = URLValidator.isValid("");

        //then
        assertFalse(valid);
    }

    @Test
    public void value_cannot_be_null() throws Exception {
        //when
        final boolean valid = URLValidator.isValid(null);

        //then
        assertFalse(valid);
    }

}
