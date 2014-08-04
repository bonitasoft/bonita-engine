package com.bonitasoft.engine.bdm.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.compiler.JDTCompiler;

@RunWith(MockitoJUnitRunner.class)
public class ServerBDMJarBuilderTest {

    @Mock
    private BusinessObjectModel bom;

    private File directory;

    @Before
    public void setUp() throws Exception {
        directory = File.createTempFile(ServerBDMJarBuilderTest.class.getName(), "");
        directory.delete();
        directory.mkdir();
    }

    @After
    public void tearDown() throws Exception {
        directory.delete();
    }

    @Test
    public void should_addPersistenceUnittestGetPersistenceFileContentFor() throws Exception {
        final JDTCompiler compiler = mock(JDTCompiler.class);
        final ServerBDMJarBuilder builder = spy(new ServerBDMJarBuilder(compiler, null));

        builder.addPersistenceFile(directory, bom);

        verify(bom).getBusinessObjects();
        assertThat(directory).isDirectory();
        final File metaInf = new File(directory, "META-INF");
        assertThat(metaInf).exists();
        assertThat(new File(metaInf, "persistence.xml")).exists();
    }

}
