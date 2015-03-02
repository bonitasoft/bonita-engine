/*******************************************************************************
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
 ******************************************************************************/
package org.bonitasoft.engine.compiler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.compiler.batch.BatchCompiler;

/**
 * Compiler based on JDTCompiler
 * 
 * @author Colin PUY
 * @author Matthieu Chaffotte
 */
public class JDTCompiler {

    private static final String COMPILER_COMPLIANCE_LEVEL = "-1.6";

    /**
     * Compile files in output directory using provided classpath
     * Put null for classpath argument to take current classpath
     * 
     * @throws CompilationException
     *             if compilation errors occurs
     */
    public void compile(final Collection<File> filesToBeCompiled, final File outputdirectory, final Collection<File> classpathEntries)
            throws CompilationException {
        final String[] commandLine = buildCommandLineArguments(filesToBeCompiled, outputdirectory, classpathEntries);
        launchCompiler(commandLine);
    }

    public void compile(final File srcDirectory, final File compilationPath) throws CompilationException {
        final Collection<File> files = FileUtils.listFiles(srcDirectory, new String[] { "java" }, true);
        Collection<File> classpathEntries;
        if (compilationPath.exists()) {
            classpathEntries = FileUtils.listFiles(compilationPath, new String[] { "jar" }, false);
        } else {
            classpathEntries = Collections.emptyList();
        }
        compile(files, srcDirectory, classpathEntries);
    }

    private String[] buildCommandLineArguments(final Collection<File> files, final File outputdirectory, final Collection<File> classpathEntries) {
        final List<String> arguments = new ArrayList<String>();
        arguments.add(COMPILER_COMPLIANCE_LEVEL);
        arguments.addAll(outputDirectoryArguments(outputdirectory));
        arguments.addAll(filesToBeCompiledArguments(files));
        arguments.addAll(classpathArguments(classpathEntries));
        return arguments.toArray(new String[arguments.size()]);
    }

    private List<String> classpathArguments(final Collection<File> classpathEntries) {
        if (classpathEntries == null || classpathEntries.isEmpty()) {
            return Collections.emptyList();
        }

        final StringBuilder builder = new StringBuilder();
        for (final File file : classpathEntries) {
            builder.append(file.getAbsolutePath()).append(File.pathSeparator);
        }
        return Arrays.asList("-cp", builder.toString());
    }

    private List<String> filesToBeCompiledArguments(final Collection<File> files) {
        final List<String> arguments = new ArrayList<String>(files.size());
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

    private void launchCompiler(final String[] commandLine) throws CompilationException {
        final PrintWriter outWriter = new PrintWriter(new ByteArrayOutputStream());
        // closing outwriter since we don't want to see compilation out stream
        outWriter.close();

        final ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        final PrintWriter errorWriter = new PrintWriter(errorStream);
        try {
            doCompilation(commandLine, outWriter, errorStream, errorWriter);
        } finally {
            // no need to close OutputStream, printWriter is doing it for us
            errorWriter.close();
        }
    }

    private void doCompilation(final String[] commandLine, final PrintWriter outWriter, final ByteArrayOutputStream errorStream, final PrintWriter errorWriter)
            throws CompilationException {
        final boolean succeeded = BatchCompiler.compile(commandLine, outWriter, errorWriter, new DummyCompilationProgress());
        if (!succeeded) {
            throw new CompilationException(new String(errorStream.toByteArray()));
        }
    }

}
