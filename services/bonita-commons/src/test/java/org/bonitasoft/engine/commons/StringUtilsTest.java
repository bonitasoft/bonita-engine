package org.bonitasoft.engine.commons;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void uniformizePathPatternShouldChangeBackslashesToForwardSlashes() throws Exception {
        final String uniformized = StringUtils.uniformizePathPattern("C:\\toto\\my path\\my file.bak.txt");
        assertThat(uniformized).isEqualTo("C:/toto/my path/my file.bak.txt");
    }

    @Test
    public void uniformizePathPatternShouldLeaveNoDoubleSlashes() throws Exception {
        final String uniformized = StringUtils.uniformizePathPattern("C:///toto//my path/////full_slashes/my file.bak.txt");
        assertThat(uniformized).isEqualTo("C:/toto/my path/full_slashes/my file.bak.txt");
    }

}
