/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
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

    public Collection<String> getJarsPath(final Class<?>... classes) {
        final Set<String> paths = new LinkedHashSet<String>();
        for (final Class<?> clazz : classes) {
            paths.add(findJarPath(clazz));
        }
        return paths;
    }

    private String findJarPath(final Class<?> clazzToFind) {
        try {
            final CodeSource codeSource = clazzToFind.getProtectionDomain().getCodeSource();
            return new File(codeSource.getLocation().getPath()).getAbsolutePath();
        } catch (final NullPointerException e) {
            throw new RuntimeException("Unable to find jar for class " + clazzToFind + " source code not in classpath or not in protection domain");
        }
    }
}
