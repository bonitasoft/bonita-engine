package com.bonitasoft.engine.bdm;

import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.junit.Test;

import com.bonitasoft.engine.compiler.JDTCompiler;
import com.bonitasoft.engine.io.IOUtils;

public class BDMJarBuilderTest {

    @Test
    public void testGetPersistenceFileContentFor() throws Exception {
        // given
        final byte[] bomZip = "bomZip".getBytes();
        final JDTCompiler compiler = mock(JDTCompiler.class);
        final BDMJarBuilder builder = new BDMJarBuilder(new JDTCompiler());
        final BDMJarBuilder spyBuilder = spy(builder);
        final BusinessObjectModel model = new BusinessObjectModel();
        final File tmpDir = IOUtils.createTempDirectory("bdm");
        final byte[] jar = "jar".getBytes();
        doReturn(model).when(spyBuilder).getBOM(bomZip);
        doReturn(tmpDir).when(spyBuilder).createBDMTmpDir();
        doAnswer(new VoidAnswer()).when(spyBuilder).generateJavaFiles(model, tmpDir);
        doAnswer(new VoidAnswer()).when(compiler).compile(anyCollectionOf(File.class), eq(tmpDir));
        doReturn(jar).when(spyBuilder).generateJar(tmpDir);
        doReturn(jar).when(spyBuilder).addPersistenceFile(jar, model);

        // when
        spyBuilder.build(bomZip);

        // verify
        verify(spyBuilder).getBOM(bomZip);
        verify(spyBuilder).createBDMTmpDir();
        verify(spyBuilder).generateJavaFiles(model, tmpDir);
        verify(spyBuilder).compileJavaClasses(tmpDir);
        verify(spyBuilder).generateJar(tmpDir);
        verify(spyBuilder).addPersistenceFile(jar, model);
    }

}
