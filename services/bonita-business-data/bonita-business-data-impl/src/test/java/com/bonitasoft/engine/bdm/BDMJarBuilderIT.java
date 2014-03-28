package com.bonitasoft.engine.bdm;

import static com.bonitasoft.engine.BOMBuilder.aBOM;

import org.junit.Test;

import com.bonitasoft.engine.BOMBuilder;

public class BDMJarBuilderIT {

    /* Just to test we have no errors in full chain. Must be improved */
    @Test
    public void jar_builder_should_goes_well_without_errors() throws Exception {
        final BDMJarBuilder bdmJarBuilder = new BDMJarBuilder(BDMCompiler.create());
        bdmJarBuilder.build(aBOM().buildZip());
    }

    @Test
    public void jar_builder_should_goes_well_without_errors_with_queries() throws Exception {
        final BDMJarBuilder bdmJarBuilder = new BDMJarBuilder(BDMCompiler.create());
        final BOMBuilder builder = new BOMBuilder();
        final BusinessObjectModelConverter converter = new BusinessObjectModelConverter();
        final byte[] zip = converter.zip(builder.buildComplex());
        bdmJarBuilder.build(zip);
    }

}
