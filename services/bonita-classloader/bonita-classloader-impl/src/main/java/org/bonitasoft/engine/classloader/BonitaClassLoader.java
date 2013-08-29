/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.commons.IOUtil;
import org.bonitasoft.engine.commons.NullCheckingUtil;

/**
 * @author Elias Ricken de Medeiros
 * @author Charles Souillard
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class BonitaClassLoader extends MonoParentJarFileClassLoader {

    private final String type;

    private final long id;

    protected Map<String, byte[]> nonJarResources;

    protected Set<URL> urls;

    private final File temporaryFolder;

    /**
     * Logger
     */
    // TODO logger

    BonitaClassLoader(final Map<String, byte[]> resources, final String type, final long id, final String temporaryFolder, final ClassLoader parent) {
        super(type + "__" + id, new URL[] {}, parent);
        NullCheckingUtil.checkArgsNotNull(resources, type, id, temporaryFolder, parent);
        this.type = type;
        this.id = id;

        this.nonJarResources = new HashMap<String, byte[]>();
        this.urls = new HashSet<URL>();
        this.temporaryFolder = new File(temporaryFolder);
        if (!this.temporaryFolder.exists()) {
            this.temporaryFolder.mkdirs();
        }
        addResources(resources);
        addURLs(this.urls.toArray(new URL[this.urls.size()]));
    }

    protected void addResources(final Map<String, byte[]> resources) {
        if (resources != null) {
            for (final Map.Entry<String, byte[]> resource : resources.entrySet()) {
                if (resource.getKey().matches(".*\\.jar")) {
                    final byte[] data = resource.getValue();
                    try {
                        final File file = IOUtil.createTempFile(resource.getKey(), null, this.temporaryFolder);
                        IOUtil.write(file, data);
                        final String path = file.getAbsolutePath();
                        final URL url = new File(path).toURI().toURL();
                        this.urls.add(url);
                        file.deleteOnExit();
                    } catch (final MalformedURLException e) {
                        e.printStackTrace();
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    this.nonJarResources.put(resource.getKey(), resource.getValue());
                }
            }
        }
    }

    @Override
    public InputStream getResourceAsStream(final String name) {
        InputStream is = getInternalInputstream(name);
        if (is == null && name.length() > 0 && name.charAt(0) == '/') {
            is = getInternalInputstream(name.substring(1));
        }
        return is;
    }

    private InputStream getInternalInputstream(final String name) {
        final byte[] classData = loadProcessResource(name);
        if (classData != null) {
            return new ByteArrayInputStream(classData);
        }
        final InputStream is = super.getResourceAsStream(name);
        if (is != null) {
            return is;
        }
        return null;
    }

    private byte[] loadProcessResource(final String resourceName) {
        if (this.nonJarResources == null) {
            return null;
        }
        return this.nonJarResources.get(resourceName);
    }

    @Override
    protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        Class<?> c = null;
        //me
        if (c == null) {
            c = findLoadedClass(name);
        }
        if (c == null) {
            try {
                c = findClass(name);
            } catch (ClassNotFoundException e) {
                //ignore
            }
        }    

        if (c == null) {
            c = getParent().loadClass(name);
        }

        if (resolve) {
            resolveClass(c);
        }
        // TODO FIXME logger LOG.fine("loadClass: " + name + ", result: " + c);
        return c;
    }

    public void release() {
        if (this.temporaryFolder.exists()) {
            this.temporaryFolder.delete();
        }
    }

    public long getId() {
        return this.id;
    }

    public String getType() {
        return this.type;
    }

    public File getTemporaryFolder() {
        return this.temporaryFolder;
    }

    @Override
    public String toString() {
        return super.toString() + ", type=" + this.type + ", id=" + this.id;
    }
}
