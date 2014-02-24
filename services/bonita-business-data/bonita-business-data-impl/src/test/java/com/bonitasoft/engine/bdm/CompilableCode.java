package com.bonitasoft.engine.bdm;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * @author Romain Bioteau
 */
public abstract class CompilableCode {

    protected void assertCompilationSuccessful(final File sourceFileToCompile) {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        final Iterable<? extends JavaFileObject> compUnits = fileManager.getJavaFileObjects(sourceFileToCompile);
        final Boolean compiled = compiler.getTask(null, fileManager, null, null, null, compUnits).call();
        assertThat(compiled).isTrue();
    }

}
