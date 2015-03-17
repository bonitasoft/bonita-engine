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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.bonitasoft.engine.commons.io.IOUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.compiler.JDTCompiler;

@RunWith(MockitoJUnitRunner.class)
public class ServerBDMJarBuilderTest {

    @Mock
    private BusinessObjectModel bom;

    private File directory;

    @Before
    public void setUp() throws Exception {
        directory = IOUtil.createTempDirectoryInDefaultTempDirectory(ServerBDMJarBuilderTest.class.getName());
    }

    @After
    public void tearDown() {
        directory.delete();
    }

    @Test
    public void should_addPersistenceUnittestGetPersistenceFileContentFor() throws Exception {
        final JDTCompiler compiler = mock(JDTCompiler.class);
        final ServerBDMJarBuilder builder = spy(new ServerBDMJarBuilder(compiler));

        builder.addPersistenceFile(directory, bom);

        verify(bom).getBusinessObjects();
        assertThat(directory).isDirectory();
        final File metaInf = new File(directory, "META-INF");
        assertThat(metaInf).exists();
        assertThat(new File(metaInf, "persistence.xml")).exists();
    }

}
