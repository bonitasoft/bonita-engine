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
package org.bonitasoft.engine.classloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * @author Elias Ricken de Medeiros
 * @author Charles Souillard
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class VirtualClassLoader extends ClassLoader {

    /**
     * WARNING!!!!!!
     * The bigger weakness of this class is that it does not override ALL public, package and protected methods of the java.lang.ClassLoader class
     * The risk (already experimented with Groovy integration for example) is that this VirtualClassLoader will be the parent of any other kind of classloader
     * (GroovyClassLoader for example)
     * and thus, all protected/package methods can be called with this "child" classloader. If VirtualClassLoader does not override the given method to delegate
     * this to the BonitaClassLoader instance
     * then the delegation model does not work anymore and some classes/resources can't be found. A good implementation should override all methods...
     */
    private BonitaClassLoader classloader;

    protected final String artifactType;

    protected final long artifactId;

    protected VirtualClassLoader(final String artifactType, final long artifactId, final ClassLoader parent) {
        super(parent);
        this.artifactType = artifactType;
        this.artifactId = artifactId;
    }

    void setClassLoader(final BonitaClassLoader classloader) {
        this.classloader = classloader;
    }

    @Override
    public Class<?> loadClass(final String name) throws ClassNotFoundException {
        if (classloader != null) {
            return classloader.loadClass(name, false);
        }
        return getParent().loadClass(name);
    }

    @Override
    protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        if (classloader != null) {
            return classloader.loadClass(name, resolve);
        }
        return getParent().loadClass(name);
    }

    @Override
    public InputStream getResourceAsStream(final String name) {
        if (classloader != null) {
            return classloader.getResourceAsStream(name);
        }
        return getParent().getResourceAsStream(name);
    }

    public BonitaClassLoader getClassLoader() {
        return classloader;
    }

    @Override
    public URL getResource(final String name) {
        if (classloader != null) {
            return classloader.getResource(name);
        }
        return getParent().getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(final String name) throws IOException {
        if (classloader != null) {
            return classloader.getResources(name);
        }
        return getParent().getResources(name);
    }

    public void destroy() {
        if (classloader != null) {
            classloader.destroy();
        }
    }

    @Override
    public String toString() {
        return super.toString() + ", type=" + artifactType + ", id=" + artifactId + " delegate: " + classloader;
    }

}
