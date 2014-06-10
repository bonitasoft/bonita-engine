package com.bonitasoft.engine.bdm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class AbstractBDMCodeGeneratorTest {

    @Test
    public void should_be_a_able_to_suffix_a_package_of_a_qualified_name() throws Exception {
        String suffixedPackage = AbstractBDMCodeGenerator.suffixPackage("a.qualified.name.Object", "suffixed");

        assertThat(suffixedPackage).isEqualTo("a.qualified.name.suffixed.Object");
    }

}
