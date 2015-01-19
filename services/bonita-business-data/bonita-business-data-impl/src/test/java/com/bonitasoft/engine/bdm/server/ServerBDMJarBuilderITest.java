package com.bonitasoft.engine.bdm.server;

import static com.bonitasoft.engine.BOMBuilder.aBOM;

import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.Test;

import com.bonitasoft.engine.BOMBuilder;
import com.bonitasoft.engine.bdm.AbstractBDMJarBuilder;
import com.bonitasoft.engine.compiler.JDTCompiler;

public class ServerBDMJarBuilderITest {

    /* Just to test we have no errors in full chain. Must be improved */
    @Test
    public void jar_builder_should_go_well_without_errors() throws Exception {
        final JDTCompiler compiler = new JDTCompiler();
        final AbstractBDMJarBuilder bdmJarBuilder = new ServerBDMJarBuilder(compiler, null);
        bdmJarBuilder.build(aBOM().build(), TrueFileFilter.TRUE);
    }

    @Test
    public void jar_builder_should_go_well_without_errors_with_queries() throws Exception {
        final JDTCompiler compiler = new JDTCompiler();
        final AbstractBDMJarBuilder bdmJarBuilder = new ServerBDMJarBuilder(compiler, null);
        final BOMBuilder builder = new BOMBuilder();
        bdmJarBuilder.build(builder.buildComplex(), TrueFileFilter.TRUE);
    }

    @Test
    public void jar_builder_should_go_well_without_errors_with_queries2() throws Exception {
        final JDTCompiler compiler = new JDTCompiler();
        final AbstractBDMJarBuilder bdmJarBuilder = new ServerBDMJarBuilder(compiler, null);
        bdmJarBuilder.build(aBOM().buildPerson(), TrueFileFilter.TRUE);
    }

    @Test
    public void jar_builder_should_go_well_with_multipleBoolean() throws Exception {
        final JDTCompiler compiler = new JDTCompiler();
        final AbstractBDMJarBuilder bdmJarBuilder = new ServerBDMJarBuilder(compiler, null);
        final BOMBuilder builder = new BOMBuilder();
        bdmJarBuilder.build(builder.buildModelWithMultipleBoolean(), TrueFileFilter.TRUE);
    }

}
