package org.bonitasoft.engine.commons;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class StringUtilsTest {

    @Before
    public void init() {
        System.setProperty("file.separator", "\\");
    }

    @Test
    public void uniformizePathPatternShouldChangeAllSlashesToSystemDependentSeparator() throws Exception {
        final String uniformized = StringUtils.uniformizePathPattern("C:\\toto\\my path\\my\\/file.bak.txt");
        assertThat(uniformized)
                .isEqualTo("C:" + File.separator + "toto" + File.separator + "my path" + File.separator + "my" + File.separator + "file.bak.txt");
    }

    @Test
    public void uniformizePathPatternShouldLeaveNoDoubleSeparator() throws Exception {
        final String uniformized = StringUtils.uniformizePathPattern("C:///toto//my path/////full_slashes/my file.bak.txt");
        assertThat(uniformized).isEqualTo(
                "C:" + File.separator + "toto" + File.separator + "my path" + File.separator + "full_slashes" + File.separator + "my file.bak.txt");
    }

}
