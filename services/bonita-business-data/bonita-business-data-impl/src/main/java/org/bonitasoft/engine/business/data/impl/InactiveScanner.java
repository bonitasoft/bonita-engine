/**
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
 **/
package org.bonitasoft.engine.business.data.impl;

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
