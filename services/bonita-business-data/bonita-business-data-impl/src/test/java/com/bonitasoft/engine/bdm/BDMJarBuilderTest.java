package com.bonitasoft.engine.bdm;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.junit.Test;

import com.bonitasoft.engine.io.IOUtils;

public class BDMJarBuilderTest {

    @Test
    public void testGetPersistenceFileContentFor() throws Exception {
        // given
        final byte[] bomZip = "bomZip".getBytes();
        final BDMCompiler compiler = mock(BDMCompiler.class);
        final BDMJarBuilder builder = new BDMJarBuilder(compiler);
        final BDMJarBuilder spyBuilder = spy(builder);
        final BusinessObjectModel model = new BusinessObjectModel();
        final File tmpDir = IOUtils.createTempDirectory("bdm");
        final byte[] jar = "jar".getBytes();
        doReturn(model).when(spyBuilder).getBOM(bomZip);
        doReturn(tmpDir).when(spyBuilder).createBDMTmpDir();
        doNothing().when(spyBuilder).generateJavaFiles(model, tmpDir);
        doReturn(jar).when(spyBuilder).generateJar(tmpDir);

        // when
        spyBuilder.build(bomZip);

        // verify
        verify(spyBuilder).getBOM(bomZip);
        verify(spyBuilder).createBDMTmpDir();
        verify(spyBuilder).generateJavaFiles(model, tmpDir);
        verify(compiler).compile(tmpDir);
        verify(spyBuilder).generateJar(tmpDir);
        verify(spyBuilder).addPersistenceFile(tmpDir, model);
    }

}
