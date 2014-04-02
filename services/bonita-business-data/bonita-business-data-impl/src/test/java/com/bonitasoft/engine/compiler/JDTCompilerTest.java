package com.bonitasoft.engine.compiler;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Colin PUY
 */
public class JDTCompilerTest {

    private static final List<String> EMPTY_CLASSPATH = null;

    private JDTCompiler jdtCompiler;

    private File outputdirectory;

    private File getTestResourceAsFile(String fileName) throws URISyntaxException {
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

    @Before
    public void instanciateCompiler() throws IOException {
        jdtCompiler = new JDTCompiler();
        outputdirectory = createTempDirectory();
    }

    @Test
    public void should_compile_files_in_output_directory() throws Exception {
        File compilableOne = getTestResourceAsFile("CompilableOne.java");
        File compilableTwo = getTestResourceAsFile("CompilableTwo.java");

        jdtCompiler.compile(asList(compilableOne, compilableTwo), outputdirectory, EMPTY_CLASSPATH);

        assertThat(new File(outputdirectory, "com/bonitasoft/CompilableOne.class")).exists();
        assertThat(new File(outputdirectory, "com/bonitasoft/CompilableTwo.class")).exists();
    }

    @Test(expected = CompilationException.class)
    public void should_throw_exception_if_compilation_errors_occurs() throws Exception {
        File uncompilable = getTestResourceAsFile("CannotBeResolvedToATypeError.java");

        jdtCompiler.compile(asList(uncompilable), outputdirectory, EMPTY_CLASSPATH);
    }

    @Test
    public void should_show_compilation_errors_in_exception_message() throws Exception {
        File uncompilable = getTestResourceAsFile("CannotBeResolvedToATypeError.java");

        try {
            jdtCompiler.compile(asList(uncompilable), outputdirectory, EMPTY_CLASSPATH);
        } catch (CompilationException e) {
            assertThat(e.getMessage()).contains("cannot be resolved to a type");
        }
    }

    @Test
    public void should_compile_class_with_external_dependencies() throws Exception {
        File compilableWithDependency = getTestResourceAsFile("DependenciesNeeded.java");
        File externalLib = getTestResourceAsFile("external-lib.jar");
        List<String> classPathEntries = asList(System.getProperty("java.class.path"), externalLib.getAbsolutePath());

        jdtCompiler.compile(asList(compilableWithDependency), outputdirectory, classPathEntries);
    }
}
