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
package org.bonitasoft.engine.business.data.generator.compiler;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;

import org.bonitasoft.engine.commons.io.IOUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Colin PUY
 * @author Celine Souchet
 */
public class JDTCompilerTest {

    private JDTCompiler jdtCompiler;

    private File outputDirectory;
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void instanciateCompiler() throws IOException {
        jdtCompiler = new JDTCompiler();
        outputDirectory = temporaryFolder.newFolder();
    }

    @After
    public void after() throws IOException {
        IOUtil.deleteDir(outputDirectory);
    }

    private File getTestResourceAsFile(final String fileName) throws URISyntaxException {
        final URL resource = JDTCompilerTest.class.getResource(fileName);
        if (resource == null) {
            throw new RuntimeException("Test resource " + fileName + " not found");
        }
        return new File(resource.toURI());
    }

    @Test
    public void should_compile_files_in_output_directory() throws Exception {
        final File compilableOne = getTestResourceAsFile("CompilableOne.java");
        final File compilableTwo = getTestResourceAsFile("CompilableTwo.java");

        jdtCompiler.compile(asList(compilableOne, compilableTwo), outputDirectory, Thread.currentThread().getContextClassLoader());

        assertThat(new File(outputDirectory, "org/bonitasoft/CompilableOne.class")).exists();
        assertThat(new File(outputDirectory, "org/bonitasoft/CompilableTwo.class")).exists();

        try (URLClassLoader urlClassLoader = new URLClassLoader(new URL[] { outputDirectory.toURI().toURL() },
                Thread.currentThread().getContextClassLoader())) {
            final Class<?> compilableOneClass = urlClassLoader.loadClass("org.bonitasoft.CompilableOne");
            final Method method = compilableOneClass.getMethod("setaClassVariable", int.class);
            assertThat(method.getParameters()[0].getName()).isEqualTo("aClassVariable");
        }
    }

    @Test(expected = CompilationException.class)
    public void should_throw_exception_if_compilation_errors_occurs() throws Exception {
        final File uncompilable = getTestResourceAsFile("CannotBeResolvedToATypeError.java");

        jdtCompiler.compile(asList(uncompilable), outputDirectory, Thread.currentThread().getContextClassLoader());
    }

    @Test
    public void should_show_compilation_errors_in_exception_message() throws Exception {
        final File uncompilable = getTestResourceAsFile("CannotBeResolvedToATypeError.java");

        try {
            jdtCompiler.compile(asList(uncompilable), outputDirectory, Thread.currentThread().getContextClassLoader());
        } catch (final CompilationException e) {
            assertThat(e.getMessage()).contains("cannot be resolved to a type");
        }
    }

    @Test
    public void should_compile_class_with_external_dependencies() throws Exception {
        final File compilableWithDependency = getTestResourceAsFile("DependenciesNeeded.java");
        final File externalLib = getTestResourceAsFile("external-lib.jar");
        final URLClassLoader urlClassLoader = new URLClassLoader(new URL[] { externalLib.toURI().toURL() }, Thread.currentThread().getContextClassLoader());

        jdtCompiler.compile(asList(compilableWithDependency), outputDirectory, urlClassLoader);
    }

}
