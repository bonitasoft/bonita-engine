package com.bonitasoft.engine.bdm;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        final List<String> optionList = new ArrayList<String>();
      
        String javaxPersistencefilePath = findJarPath(javax.persistence.Basic.class);
        String bdmEntityfilePath = findJarPath(Entity.class);
        optionList.addAll(Arrays.asList("-classpath", javaxPersistencefilePath+":"+bdmEntityfilePath));
        final Boolean compiled = compiler.getTask(null, fileManager, null, optionList, null, compUnits).call();
        assertThat(compiled).isTrue();
    }

	private String findJarPath(Class<?> clazzToFind) {
		URL jarURL = clazzToFind.getResource(clazzToFind.getSimpleName()+".class");
        String jarPath = jarURL.getFile();
        if(jarPath.indexOf("!") != -1){
        	jarPath = jarPath.split("!")[0];
        }
		return jarPath;
	}

}
