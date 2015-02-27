/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.dao.client.resources.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class CapitalizerTest {

    @Test
    public void should_capitalize_a_string() {

        String capitalized = Capitalizer.capitalize("uncapitalized");

        assertThat(capitalized).isEqualTo("Uncapitalized");
    }

    @Test
    public void should_do_nothing_for_a_null_string() {
        String capitalized = Capitalizer.capitalize(null);

        assertThat(capitalized).isNull();
    }

    @Test
    public void should_do_nothing_for_an_empty_string() {
        String capitalized = Capitalizer.capitalize("");

        assertThat(capitalized).isEmpty();
    }
}
