/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.business.data.generator.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.apache.commons.io.filefilter.TrueFileFilter;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.business.data.generator.BOMBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServerBDMJarBuilderTest {

    @Mock
    private BusinessObjectModel bom;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File directory;
    private ServerBDMJarBuilder bdmJarBuilder;

    @Before
    public void setUp() throws Exception {
        directory = temporaryFolder.newFolder();
        bdmJarBuilder = new ServerBDMJarBuilder(new ServerBDMCodeGenerator());
    }

    @Test
    public void should_addPersistenceUnittestGetPersistenceFileContentFor() throws Exception {
        bdmJarBuilder.addPersistenceFile(directory, bom);

        verify(bom).getBusinessObjects();
        assertThat(directory).isDirectory();
        final File metaInf = new File(directory, "META-INF");
        assertThat(metaInf).exists();
        assertThat(new File(metaInf, "persistence.xml")).exists();
    }

    /* Just to test we have no errors in full chain. Must be improved */
    @Test
    public void jar_builder_should_go_well_without_errors() throws Exception {
        bdmJarBuilder.build(BOMBuilder.aBOM().build(), TrueFileFilter.TRUE);
    }

    @Test
    public void jar_builder_should_go_well_without_errors_with_queries() throws Exception {
        final BOMBuilder builder = new BOMBuilder();
        bdmJarBuilder.build(builder.buildComplex(), TrueFileFilter.TRUE);
    }

    @Test
    public void jar_builder_should_go_well_without_errors_with_queries2() throws Exception {
        bdmJarBuilder.build(BOMBuilder.aBOM().buildPerson(), TrueFileFilter.TRUE);
    }

    @Test
    public void jar_builder_should_go_well_with_multipleBoolean() throws Exception {
        final BOMBuilder builder = new BOMBuilder();
        bdmJarBuilder.build(builder.buildModelWithMultipleBoolean(), TrueFileFilter.TRUE);
    }

}
