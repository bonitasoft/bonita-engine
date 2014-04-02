package com.bonitasoft.engine.bdm.server;

import static com.bonitasoft.engine.BOMBuilder.aBOM;

import org.junit.Test;

import com.bonitasoft.engine.BOMBuilder;
import com.bonitasoft.engine.bdm.AbstractBDMJarBuilder;
import com.bonitasoft.engine.bdm.BDMCompiler;
import com.bonitasoft.engine.bdm.server.ServerBDMJarBuilder;

public class ServerBDMJarBuilderIT {

    /* Just to test we have no errors in full chain. Must be improved */
    @Test
    public void jar_builder_should_goes_well_without_errors() throws Exception {
        final AbstractBDMJarBuilder bdmJarBuilder = new ServerBDMJarBuilder(BDMCompiler.create());
        bdmJarBuilder.build(aBOM().build());
    }

    @Test
    public void jar_builder_should_goes_well_without_errors_with_queries() throws Exception {
        final AbstractBDMJarBuilder bdmJarBuilder = new ServerBDMJarBuilder(BDMCompiler.create());
        final BOMBuilder builder = new BOMBuilder();
        bdmJarBuilder.build(builder.buildComplex());
    }

}
