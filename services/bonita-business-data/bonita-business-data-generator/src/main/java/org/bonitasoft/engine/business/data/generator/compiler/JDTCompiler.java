/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import static java.lang.String.join;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.batch.Main;

/**
 * Compiler based on JDTCompiler
 *
 * @author Colin PUY
 * @author Matthieu Chaffotte
 */
public class JDTCompiler {

    private static final String COMPILER_COMPLIANCE_LEVEL = "-1.8";
    //Used to keep parameters name in bytecode to allow reflection in DAO
    private static final String PARAMETERS_NAME_ARG = "-parameters";
    private ClassLoader classLoader;

    /**
     * Compile files in output directory using provided classpath
     * Put null for classpath argument to take current classpath
     *
     * @throws CompilationException
     *         if compilation errors occurs
     */
    @Deprecated
    public void compile(final Collection<File> filesToBeCompiled, final File outputDirectory, ClassLoader classLoader)
            throws CompilationException {
        compile(filesToBeCompiled, outputDirectory, classLoader, emptySet());
    }

    public void compile(final File srcDirectory, File outputDirectory, ClassLoader classLoader)
            throws CompilationException {
        Map<String, File> sourceFiles = listJavaFilesAndClasses(srcDirectory);
        compile(sourceFiles.values(), outputDirectory, classLoader, sourceFiles.keySet());
    }

    private Map<String, File> listJavaFilesAndClasses(File srcFile) {
        Map<String, File> result = new HashMap<>();
        File[] files = srcFile.listFiles();
        if (files == null) {
            return result;
        }
        for (File file : files) {
            doListJavaFilesAndClasses(file, result, emptyList());
        }
        return result;
    }

    private void doListJavaFilesAndClasses(File srcFile, Map<String, File> files, List<String> parentPath) {
        if (!srcFile.exists()) {
            return;
        }
        if (srcFile.isFile() && srcFile.getName().endsWith(".java")) {
            files.put(join(".", getPath(parentPath, srcFile)), srcFile);
        } else {
            List<String> path = getPath(parentPath, srcFile);
            File[] children = srcFile.listFiles();
            if (children == null) {
                return;
            }
            for (File file : children) {
                doListJavaFilesAndClasses(file, files, path);
            }
        }
    }

    private List<String> getPath(List<String> parentPath, File srcFile) {
        List<String> path = new ArrayList<>(parentPath);
        path.add(srcFile.getName().replace(".java", ""));
        return path;
    }

    private void compile(Collection<File> filesToBeCompiled, File outputDirectory,
            ClassLoader classLoader, Set<String> classesToBeCompiled) throws CompilationException {
        this.classLoader = classLoader;
        final String[] commandLine = buildCommandLineArguments(filesToBeCompiled, outputDirectory);
        launchCompiler(commandLine, classesToBeCompiled);
    }

    private String[] buildCommandLineArguments(final Collection<File> files, final File outputdirectory) {
        final List<String> arguments = new ArrayList<>();
        arguments.add(COMPILER_COMPLIANCE_LEVEL);
        arguments.add(PARAMETERS_NAME_ARG);
        arguments.addAll(outputDirectoryArguments(outputdirectory));
        arguments.addAll(filesToBeCompiledArguments(files));
        return arguments.toArray(new String[arguments.size()]);
    }

    private List<String> filesToBeCompiledArguments(final Collection<File> files) {
        final List<String> arguments = new ArrayList<>(files.size());
        for (final File file : files) {
            arguments.add(file.getAbsolutePath());
        }
        return arguments;
    }

    private List<String> outputDirectoryArguments(final File outputdirectory) {
        if (outputdirectory == null) {
            return Collections.emptyList();
        }
        return Arrays.asList("-d", outputdirectory.getAbsolutePath());
    }

    private void launchCompiler(final String[] commandLine, Set<String> classesToBeCompiled)
            throws CompilationException {
        final PrintWriter outWriter = new PrintWriter(new ByteArrayOutputStream());
        // closing outwriter since we don't want to see compilation out stream
        outWriter.close();

        final ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        final PrintWriter errorWriter = new PrintWriter(errorStream);
        try {
            doCompilation(commandLine, outWriter, errorStream, errorWriter, classesToBeCompiled);
        } finally {
            // no need to close OutputStream, printWriter is doing it for us
            errorWriter.close();
        }
    }

    private void doCompilation(final String[] commandLine, final PrintWriter outWriter,
            final ByteArrayOutputStream errorStream, final PrintWriter errorWriter, Set<String> classesToBeCompiled)
            throws CompilationException {
        final Main mainCompiler = new Main(outWriter, errorWriter, false /* systemExit */, null /* options */,
                new DummyCompilationProgress()) {

            @Override
            public FileSystem getLibraryAccess() {
                final ClassLoader contextClassLoader = classLoader;
                return new ClassLoaderEnvironment(contextClassLoader, classesToBeCompiled);
            }
        };
        final boolean succeeded = mainCompiler.compile(commandLine);
        if (!succeeded) {
            throw new CompilationException(new String(errorStream.toByteArray()));
        }
    }

}
