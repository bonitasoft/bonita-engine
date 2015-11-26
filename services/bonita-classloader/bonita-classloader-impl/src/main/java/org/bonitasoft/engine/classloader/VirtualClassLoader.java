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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.data.instance.model.impl.XStreamFactory;

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

    private VirtualClassLoader virtualParent;

    private List<ClassLoaderListener> listeners;

    private Set<VirtualClassLoader> children = new HashSet<>();
    private ClassLoaderIdentifier identifier;

    VirtualClassLoader(final String artifactType, final long artifactId, final ClassLoader parent) {
        super(parent);
        identifier = new ClassLoaderIdentifier(artifactType, artifactId);
        listeners = new ArrayList<>();
    }

    VirtualClassLoader(final String artifactType, final long artifactId, final VirtualClassLoader parent) {
        this(artifactType, artifactId, (ClassLoader) parent);
        virtualParent = parent;
        virtualParent.addChild(this);
    }

    void replaceClassLoader(final BonitaClassLoader classloader) {
        BonitaClassLoader oldClassLoader = this.classloader;
        this.classloader = classloader;
        notifyUpdate();
        if (oldClassLoader != null) {
            destroy(oldClassLoader);
        }
    }

    private void notifyUpdate() {
        for (ClassLoaderListener listener : getListeners()) {
            listener.onUpdate(this);
        }
        for (VirtualClassLoader child : children) {
            child.notifyUpdate();
        }
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
        final BonitaClassLoader classloader = this.classloader;
        destroy(classloader);
        notifyDestroy();
        if(virtualParent != null){
            virtualParent.removeChild(this);
        }
    }

    private void notifyDestroy() {
        for (ClassLoaderListener listener : getListeners()) {
            listener.onDestroy(this);
        }
        //do not notify children, it should not happen
    }

    private synchronized List<ClassLoaderListener> getListeners() {
        return new ArrayList<>(listeners);
    }

    private void destroy(BonitaClassLoader classloader) {
        XStreamFactory.remove(this);
        if (classloader != null) {
            classloader.destroy();
        }
    }

    @Override
    public String toString() {
        return super.toString() + ", type=" + identifier.getType() + ", id=" + identifier.getId() + " delegate: " + classloader;
    }

    public synchronized boolean addListener(ClassLoaderListener listener) {
        return !listeners.contains(listener) && listeners.add(listener);
    }

    public synchronized boolean removeListener(ClassLoaderListener classLoaderListener) {
        return listeners.remove(classLoaderListener);
    }

    public void addChild(VirtualClassLoader virtualClassLoader) {
        children.add(virtualClassLoader);
    }

    private void removeChild(VirtualClassLoader child) {
        children.remove(child);

    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public Set<VirtualClassLoader> getChildren() {
        return children;
    }

    public ClassLoaderIdentifier getIdentifier() {
        return identifier;
    }
}
