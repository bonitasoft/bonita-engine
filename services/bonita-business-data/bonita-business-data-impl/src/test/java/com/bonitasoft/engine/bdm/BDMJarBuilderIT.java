package com.bonitasoft.engine.bdm;

import static com.bonitasoft.engine.BOMBuilder.aBOM;

import org.junit.Test;

public class BDMJarBuilderIT {

    /* Just to test we have no errors in full chain. Must be improved */
    @Test
    public void jar_builder_should_goes_well_without_errors() throws Exception {
        BDMJarBuilder bdmJarBuilder = new BDMJarBuilder(BDMCompiler.create());
        bdmJarBuilder.build(aBOM().buildZip());
    }
}
