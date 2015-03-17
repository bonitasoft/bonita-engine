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
package org.bonitasoft.engine.bdm.client;

import static org.mockito.Mockito.verify;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.BOMBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.bonitasoft.engine.bdm.dao.client.resources.BusinessObjectDeserializer;
import org.bonitasoft.engine.compiler.JDTCompiler;
import org.bonitasoft.engine.io.IOUtils;

@RunWith(MockitoJUnitRunner.class)
public class ClientBDMJarBuilderTest {
    
    @Mock
    private JDTCompiler compiler;

    @Mock
    private ResourcesLoader resourcesLoader;
    
    @InjectMocks
    private ClientBDMJarBuilder clientBDMJarBuilder;

    @Before
    public void setUp() {
        clientBDMJarBuilder = new ClientBDMJarBuilder(compiler, resourcesLoader);
    }
    
    @Test
    public void should_add_client_resources_to_compilation_folder() throws Exception {
        File directory = IOUtils.createTempDirectory("bdmtest");
        
        clientBDMJarBuilder.addSourceFilesToDirectory(BOMBuilder.aBOM().build(), directory);
        
        verify(resourcesLoader).copyJavaFilesToDirectory(BusinessObjectDeserializer.class.getPackage().getName(), directory);
        FileUtils.deleteDirectory(directory);
    }

}
