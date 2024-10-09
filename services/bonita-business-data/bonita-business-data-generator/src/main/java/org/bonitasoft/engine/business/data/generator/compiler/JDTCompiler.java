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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.internal.compiler.batch.Main;

/**
 * Compiler based on JDTCompiler
 *
 * @author Colin PUY
 * @author Matthieu Chaffotte
 */
public class JDTCompiler {

    private static final String COMPILER_COMPLIANCE_LEVEL = "-17";
    //Used to keep parameters name in bytecode to allow reflection in DAO
    private static final String PARAMETERS_NAME_ARG = "-parameters";

    public static File lookupJarContaining(Class<?> clazz) {
        try {
            File jarFile = Path.of(clazz.getProtectionDomain().getCodeSource().getLocation().toURI()).toFile();
            if (!jarFile.exists()) {
                throw new IllegalArgumentException("Cannot find jar file for class " + clazz.getName()
                        + ". Resolved jar file: " + jarFile.getAbsolutePath() + " does not exist.");
            }
            return jarFile;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Cannot find jar file for class " + clazz.getName(), e);
        }

    }

    public static File lookupJarContaining(String className) throws ClassNotFoundException {
        return lookupJarContaining(Class.forName(className));
    }

    /**
     * Compile files in output directory using provided classpath
     * Put null for classpath argument to take current classpath
     *
     * @throws CompilationException
     *         if compilation errors occurs
     */
    @Deprecated
    public void compile(final Collection<File> filesToBeCompiled, final File outputDirectory,
            File... additionalClasspath)
            throws CompilationException {
        compile(filesToBeCompiled, outputDirectory, additionalClasspath);
    }

    public void compile(final File srcDirectory, File outputDirectory, File... additionalClasspath)
            throws CompilationException {
        Map<String, File> sourceFiles = listJavaFilesAndClasses(srcDirectory);
        launchCompiler(buildCommandLineArguments(sourceFiles.values(), outputDirectory, additionalClasspath));
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

    private String[] buildCommandLineArguments(final Collection<File> files, final File outputDirectory,
            File... additionalClasspath) {
        final List<String> arguments = new ArrayList<>();
        if (additionalClasspath != null) {
            arguments.add("-classpath");
            arguments.add(
                    Arrays.stream(additionalClasspath)
                            .map(File::getAbsolutePath)
                            .collect(Collectors.joining(File.pathSeparator)));
        }
        arguments.add(COMPILER_COMPLIANCE_LEVEL);
        arguments.add(PARAMETERS_NAME_ARG);
        arguments.addAll(outputDirectoryArguments(outputDirectory));
        arguments.addAll(filesToBeCompiledArguments(files));
        return arguments.toArray(new String[0]);
    }

    private List<String> filesToBeCompiledArguments(final Collection<File> files) {
        final List<String> arguments = new ArrayList<>(files.size());
        for (final File file : files) {
            arguments.add(file.getAbsolutePath());
        }
        return arguments;
    }

    private List<String> outputDirectoryArguments(final File outputDirectory) {
        if (outputDirectory == null) {
            return Collections.emptyList();
        }
        return Arrays.asList("-d", outputDirectory.getAbsolutePath());
    }

    private void launchCompiler(final String[] commandLine)
            throws CompilationException {
        final PrintWriter outWriter = new PrintWriter(new ByteArrayOutputStream());
        // closing outwriter since we don't want to see compilation out stream
        outWriter.close();

        final ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        try (var errorWriter = new PrintWriter(errorStream)) {
            doCompilation(commandLine, outWriter, errorStream, errorWriter);
        }
    }

    private void doCompilation(final String[] commandLine, final PrintWriter outWriter,
            final ByteArrayOutputStream errorStream, final PrintWriter errorWriter)
            throws CompilationException {
        final Main mainCompiler = new Main(outWriter, errorWriter, false /* systemExit */, null /* options */,
                new DummyCompilationProgress());
        final boolean succeeded = mainCompiler.compile(commandLine);
        if (!succeeded) {
            throw new CompilationException(errorStream.toString());
        }
    }

}
