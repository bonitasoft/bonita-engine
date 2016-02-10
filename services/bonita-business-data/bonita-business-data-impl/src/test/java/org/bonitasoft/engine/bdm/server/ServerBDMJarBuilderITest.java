/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.bdm.server;

import org.apache.commons.io.filefilter.TrueFileFilter;
import org.bonitasoft.engine.BOMBuilder;
import org.junit.Test;

import org.bonitasoft.engine.bdm.AbstractBDMJarBuilder;
import org.bonitasoft.engine.compiler.JDTCompiler;

public class ServerBDMJarBuilderITest {

    /* Just to test we have no errors in full chain. Must be improved */
    @Test
    public void jar_builder_should_go_well_without_errors() throws Exception {
        final JDTCompiler compiler = new JDTCompiler();
        final AbstractBDMJarBuilder bdmJarBuilder = new ServerBDMJarBuilder(compiler);
        bdmJarBuilder.build(BOMBuilder.aBOM().build(), TrueFileFilter.TRUE);
    }

    @Test
    public void jar_builder_should_go_well_without_errors_with_queries() throws Exception {
        final JDTCompiler compiler = new JDTCompiler();
        final AbstractBDMJarBuilder bdmJarBuilder = new ServerBDMJarBuilder(compiler);
        final BOMBuilder builder = new BOMBuilder();
        bdmJarBuilder.build(builder.buildComplex(), TrueFileFilter.TRUE);
    }

    @Test
    public void jar_builder_should_go_well_without_errors_with_queries2() throws Exception {
        final JDTCompiler compiler = new JDTCompiler();
        final AbstractBDMJarBuilder bdmJarBuilder = new ServerBDMJarBuilder(compiler);
        bdmJarBuilder.build(BOMBuilder.aBOM().buildPerson(), TrueFileFilter.TRUE);
    }

    @Test
    public void jar_builder_should_go_well_with_multipleBoolean() throws Exception {
        final JDTCompiler compiler = new JDTCompiler();
        final AbstractBDMJarBuilder bdmJarBuilder = new ServerBDMJarBuilder(compiler);
        final BOMBuilder builder = new BOMBuilder();
        bdmJarBuilder.build(builder.buildModelWithMultipleBoolean(), TrueFileFilter.TRUE);
    }

}
