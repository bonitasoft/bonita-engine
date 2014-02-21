package com.bonitasoft.engine.bdm.compiler;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class JDTCompilerTest {

    @Test
    public void should_compile_files_in_output_directory() throws Exception {
        File compilableOne = getResourceAsFile("CompilableOne.java");
        File compilableTwo = getResourceAsFile("CompilableTwo.java");
        File outputdirectory = createTempDirectory();
        
        JDTCompiler jdtCompiler = new JDTCompiler();
        jdtCompiler.compile(asList(compilableOne, compilableTwo), outputdirectory);
        
        assertThat(new File(outputdirectory, "com/bonitasoft/engine/bdm/compiler/CompilableOne.class")).exists();
        assertThat(new File(outputdirectory, "com/bonitasoft/engine/bdm/compiler/CompilableTwo.class")).exists();
    }
    

    private File getResourceAsFile(String fileName) throws URISyntaxException {
        return new File(JDTCompilerTest.class.getResource(fileName).toURI());
    }

    private File createTempDirectory() throws IOException {
        File outputdirectory = File.createTempFile("testFolder", "");
        // in order to create a directory, we have to delete it first ... !!
        outputdirectory.delete();
        outputdirectory.mkdir();
        outputdirectory.deleteOnExit();
        return outputdirectory;
    }
}
