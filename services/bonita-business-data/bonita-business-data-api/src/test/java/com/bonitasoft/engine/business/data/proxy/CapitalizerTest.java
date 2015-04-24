/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.business.data.proxy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class CapitalizerTest {

    @Test
    public void testCapitalizer() throws Exception {
        assertThat(Capitalizer.capitalize("name")).isEqualTo("Name");
    }

    @Test
    public void capitalizer_null_value() throws Exception {
        assertThat(Capitalizer.capitalize(null)).isEqualTo(null);
    }

    @Test
    public void capitalizer_empty_value() throws Exception {
        assertThat(Capitalizer.capitalize("")).isEqualTo("");
    }
}
