package org.bonitasoft.engine.commons;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void uniformizePathPatternShouldChangeAllSlashesToSystemDependentSeparator() {
        final String uniformized = StringUtils.uniformizePathPattern("C:\\toto\\my path\\my\\/file.bak.txt");
        assertThat(uniformized)
                .isEqualTo("C:/toto/my path/my/file.bak.txt");
    }

    @Test
    public void uniformizePathPatternShouldLeaveNoDoubleSeparator() {
        final String uniformized = StringUtils.uniformizePathPattern("C:///toto//my path/////full_slashes/my file.bak.txt");
        assertThat(uniformized).isEqualTo(
                "C:/toto/my path/full_slashes/my file.bak.txt");
    }

}
