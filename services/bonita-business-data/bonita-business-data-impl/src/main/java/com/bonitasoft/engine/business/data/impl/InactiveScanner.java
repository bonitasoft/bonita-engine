/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.data.impl;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Collections;
import java.util.Set;

import org.hibernate.ejb.packaging.NamedInputStream;
import org.hibernate.ejb.packaging.Scanner;

/**
 * @author Matthieu Chaffotte
 */
public class InactiveScanner implements Scanner {

    @Override
    public Set<Package> getPackagesInJar(final URL jartoScan, final Set<Class<? extends Annotation>> annotationsToLookFor) {
        return Collections.emptySet();
    }

    @Override
    public Set<Class<?>> getClassesInJar(final URL jartoScan, final Set<Class<? extends Annotation>> annotationsToLookFor) {
        return Collections.emptySet();
    }

    @Override
    public Set<NamedInputStream> getFilesInJar(final URL jartoScan, final Set<String> filePatterns) {
        return Collections.emptySet();
    }

    @Override
    public Set<NamedInputStream> getFilesInClasspath(final Set<String> filePatterns) {
        return Collections.emptySet();
    }

    @Override
    public String getUnqualifiedJarName(final URL jarUrl) {
        return "";
    }

}
