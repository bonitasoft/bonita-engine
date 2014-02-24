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
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class JDTCompilerTest {

    private JDTCompiler jdtCompiler;

    @Before
    public void instanciateCompiler() {
        jdtCompiler = new JDTCompiler();
    }
    
    @Test
    public void should_compile_files_in_output_directory() throws Exception {
        File compilableOne = getResourceAsFile("CompilableOne.java");
        File compilableTwo = getResourceAsFile("CompilableTwo.java");
        File outputdirectory = createTempDirectory();
        
        jdtCompiler.compile(asList(compilableOne, compilableTwo), outputdirectory);
        
        assertThat(new File(outputdirectory, "com/bonitasoft/engine/bdm/compiler/CompilableOne.class")).exists();
        assertThat(new File(outputdirectory, "com/bonitasoft/engine/bdm/compiler/CompilableTwo.class")).exists();
    }
    
    @Test(expected = CompilationException.class)
    public void should_throw_exception_if_compilation_errors_occurs() throws Exception {
        File uncompilable = getResourceAsFile("UnCompilable.java");
        File outputdirectory = createTempDirectory();
        
        jdtCompiler.compile(asList(uncompilable), outputdirectory);
    }
    
    @Test
    public void if_compilation_exception_occurs_exception_message_must_list_compilation_errors() throws Exception {
        File uncompilable = getResourceAsFile("UnCompilable.java");
        File outputdirectory = createTempDirectory();
        
        try {
            jdtCompiler.compile(asList(uncompilable), outputdirectory);
        } catch (CompilationException e) {
            assertThat(e.getMessage()).contains("cannot be resolved to a type");
        }
        
    }

    private File getResourceAsFile(String fileName) throws URISyntaxException {
        URL resource = JDTCompilerTest.class.getResource(fileName);
        if (resource == null) {
            throw new RuntimeException("Test resource " + fileName + " not found");
        }
        return new File(resource.toURI());
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
