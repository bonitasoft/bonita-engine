/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.compiler;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.join;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.compiler.batch.BatchCompiler;

/**
 * Compiler based on JDTCompiler
 * 
 * @author Colin PUY
 */
public class JDTCompiler {

    private static final String COMPILER_COMPLIANCE_LEVEL = "-1.6";

    /**
     * Compile files in output directory using provided classpath
     * Put null for classpath argument to take current classpath
     * 
     * @throws CompilationException if compilation errors occurs
     */
    public void compile(final Collection<File> filesToBeCompiled, final File outputdirectory, Collection<String> classpathEntries) throws CompilationException {
        final String[] commandLine = buildCommandLineArguments(filesToBeCompiled, outputdirectory, classpathEntries);
        launchCompiler(commandLine);
    }

    private String[] buildCommandLineArguments(final Collection<File> files, final File outputdirectory, Collection<String> classpathEntries) {
        final List<String> arguments = new ArrayList<String>();
        arguments.add(COMPILER_COMPLIANCE_LEVEL);
        arguments.addAll(outputDirectoryArguments(outputdirectory));
        arguments.addAll(filesToBeCompiledArguments(files));
        arguments.addAll(classpathArguments(classpathEntries));
        return arguments.toArray(new String[arguments.size()]);
    }

    private List<String> classpathArguments(Collection<String> classpathEntries) {
        if (classpathEntries == null || classpathEntries.isEmpty()) {
            return emptyList();
        }
        return asList("-cp", join(classpathEntries, ":"));
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
            return emptyList();
        }
        return asList("-d", outputdirectory.getAbsolutePath());
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
