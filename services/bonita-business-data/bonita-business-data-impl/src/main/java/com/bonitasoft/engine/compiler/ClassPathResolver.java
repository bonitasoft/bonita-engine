package com.bonitasoft.engine.compiler;

import java.io.File;
import java.security.CodeSource;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Find jar in resources given class
 * 
 * @author Colin PUY
 */
public class ClassPathResolver {

    public Collection<String> getJarsPath(Class<?>... classes) {
        Set<String> paths = new LinkedHashSet<String>();
        for (Class<?> clazz : classes) {
            paths.add(findJarPath(clazz));
        }
        return paths;
    }

    private String findJarPath(Class<?> clazzToFind) {
        try {
            CodeSource codeSource = clazzToFind.getProtectionDomain().getCodeSource();
            return new File(codeSource.getLocation().getPath()).getAbsolutePath();
        } catch (NullPointerException e) {
            throw new RuntimeException("Unable to find jar for class " + clazzToFind + " source code not in classpath or not in protection domain");
        }
    }
}
