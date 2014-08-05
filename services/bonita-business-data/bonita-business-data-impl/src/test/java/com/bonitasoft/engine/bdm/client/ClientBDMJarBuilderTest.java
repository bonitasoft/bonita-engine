package com.bonitasoft.engine.bdm.client;

import static com.bonitasoft.engine.BOMBuilder.aBOM;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.bdm.dao.client.resources.BusinessObjectDeserializer;
import com.bonitasoft.engine.compiler.JDTCompiler;
import com.bonitasoft.engine.io.IOUtils;

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
        clientBDMJarBuilder = new ClientBDMJarBuilder(compiler, resourcesLoader, "");
    }
    
    @Test
    public void should_add_client_resources_to_compilation_folder() throws Exception {
        File directory = IOUtils.createTempDirectory("bdmtest");
        
        clientBDMJarBuilder.addSourceFilesToDirectory(aBOM().build(), directory);
        
        verify(resourcesLoader).copyJavaFilesToDirectory(BusinessObjectDeserializer.class.getPackage().getName(), directory);
        FileUtils.deleteDirectory(directory);
    }

}
