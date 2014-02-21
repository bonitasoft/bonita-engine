package com.bonitasoft.engine.bdm.compiler;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.compiler.batch.BatchCompiler;

public class JDTCompiler {

    public void compile(Collection<File> filesToBeCompiled, File outputdirectory) {
        String[] commandLine = buildCommandLineArguments(filesToBeCompiled, outputdirectory);
        BatchCompiler.compile(commandLine, new PrintWriter(System.out), new PrintWriter(System.err), new DummyCompilationProgress());
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
}
