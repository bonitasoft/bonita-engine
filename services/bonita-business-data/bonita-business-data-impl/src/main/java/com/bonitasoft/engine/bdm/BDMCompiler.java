package com.bonitasoft.engine.bdm;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;

import com.bonitasoft.engine.compiler.ClassPathResolver;
import com.bonitasoft.engine.compiler.CompilationException;
import com.bonitasoft.engine.compiler.JDTCompiler;

public class BDMCompiler {

    /**
     * Classes listed to resolve dependencies needed by bdm to be compiled
     * - javax.persistence
     * - com.bonitasoft.engine.bdm.Entity
     */
    private static Class<?>[] classes = new Class[] { javax.persistence.Basic.class, com.bonitasoft.engine.bdm.Entity.class };

    private ClassPathResolver classPathResolver;

    private JDTCompiler jdtCompiler;

    public BDMCompiler(JDTCompiler jdtCompiler, ClassPathResolver classPathResolver) {
        this.jdtCompiler = jdtCompiler;
        this.classPathResolver = classPathResolver;
    }

    public void compile(File srcDirectory) throws CompilationException {
        final Collection<File> files = FileUtils.listFiles(srcDirectory, new String[] { "java" }, true);
        Collection<String> jarsPath = classPathResolver.getJarsPath(classes);
        jdtCompiler.compile(files, srcDirectory, jarsPath);
    }

    public static BDMCompiler create() {
        return new BDMCompiler(new JDTCompiler(), new ClassPathResolver());
    }
}
