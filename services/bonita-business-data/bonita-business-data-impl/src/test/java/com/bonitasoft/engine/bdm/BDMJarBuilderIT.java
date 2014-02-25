package com.bonitasoft.engine.bdm;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class BDMJarBuilderIT {

    /* Just to test we have no errors in full chain. Must be improved */
    @Test
    public void jar_builder_should_goes_well_without_errors() throws Exception {
        BDMJarBuilder bdmJarBuilder = new BDMJarBuilder(BDMCompiler.create());
        InputStream bdm = BDMJarBuilderIT.class.getResourceAsStream("BDM.zip");
        bdmJarBuilder.build(IOUtils.toByteArray(bdm));
    }
}
