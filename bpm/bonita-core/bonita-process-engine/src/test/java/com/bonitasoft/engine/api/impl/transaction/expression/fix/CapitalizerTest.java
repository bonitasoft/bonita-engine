package com.bonitasoft.engine.api.impl.transaction.expression.fix;

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
