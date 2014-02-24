package com.bonitasoft.engine.bdm.compiler;

import static java.util.Arrays.asList;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.compiler.batch.BatchCompiler;

public class JDTCompiler {

    public void compile(Collection<File> filesToBeCompiled, File outputdirectory) throws CompilationException {
        String[] commandLine = buildCommandLineArguments(filesToBeCompiled, outputdirectory);
        launchCompiler(commandLine);
    }

    private String[] buildCommandLineArguments(Collection<File> files, File outputdirectory) {
        List<String> arguments = new ArrayList<String>();
        arguments.addAll(outputDirectoryArguments(outputdirectory));
        for (File file : files) {
            arguments.add(file.getAbsolutePath());
        }
        return arguments.toArray(new String[arguments.size()]);
    }

    private List<String> outputDirectoryArguments(File outputdirectory) {
        return asList("-d", outputdirectory.getAbsolutePath());
    }

    private void launchCompiler(String[] commandLine) throws CompilationException {
        PrintWriter outWriter = new PrintWriter(new ByteArrayOutputStream());
        // closing outwriter since we don't want to see compilation out stream
        outWriter.close();
        
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        PrintWriter errorWriter = new PrintWriter(errorStream);
        try {
            compile(commandLine, outWriter, errorStream, errorWriter);
        } finally {
            // no need to close OutputStream, printWriter is doing it for us
            if (errorWriter != null) errorWriter.close(); 
        }
    }

    private void compile(String[] commandLine, PrintWriter outWriter, ByteArrayOutputStream errorStream, PrintWriter errorWriter) throws CompilationException {
        boolean succeeded = BatchCompiler.compile(commandLine, outWriter, errorWriter, new DummyCompilationProgress());
        if (!succeeded) {
            throw new CompilationException(new String(errorStream.toByteArray()));
        }
    }
}
