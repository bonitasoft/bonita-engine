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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.commons.io.IOUtil;

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

    private final File temporaryDirectory;

    private boolean isActive = true;

    private final long creationTime;

    private final String uuid;

    /**
     * Logger
     */
    // TODO logger

    BonitaClassLoader(final Map<String, byte[]> resources, final String type, final long id, final URI temporaryDirectoryUri, final ClassLoader parent) {
        super(type + "__" + id, new URL[] {}, parent);
        this.creationTime = System.currentTimeMillis();
        NullCheckingUtil.checkArgsNotNull(resources, type, id, temporaryDirectoryUri, parent);
        this.type = type;
        this.id = id;
        this.uuid = UUID.randomUUID().toString();

        nonJarResources = new HashMap<String, byte[]>();
        urls = new HashSet<URL>();
        temporaryDirectory = new File(temporaryDirectoryUri);
        if (!temporaryDirectory.exists()) {
            temporaryDirectory.mkdirs();
        }
        addResources(resources);
        addURLs(urls.toArray(new URL[urls.size()]));
    }

    protected void addResources(final Map<String, byte[]> resources) {
        if (resources != null) {
            for (final Map.Entry<String, byte[]> resource : resources.entrySet()) {
                if (resource.getKey().matches(".*\\.jar")) {
                    final byte[] data = resource.getValue();
                    try {
                        final File file = IOUtil.createTempFile(resource.getKey(), null, temporaryDirectory);
                        IOUtil.write(file, data);
                        final String path = file.getAbsolutePath();
                        final URL url = new File(path).toURI().toURL();
                        urls.add(url);
                    } catch (final MalformedURLException e) {
                        e.printStackTrace();
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    nonJarResources.put(resource.getKey(), resource.getValue());
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
        if (nonJarResources == null) {
            return new byte[0];
        }
        return nonJarResources.get(resourceName);
    }

    @Override
    protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        Class<?> c = null;
        c = findLoadedClass(name);
        if (c == null) {
            try {
                c = findClass(name);
            } catch (final ClassNotFoundException e) {
                // ignore
            } catch (final LinkageError le) {
                // might be because of a duplicate loading (concurrency loading), retry to find it one time See BS-2483
                c = findLoadedClass(name);
                if (c == null) {
                    // was not because of duplicate loading: throw the exception
                    throw le;
                }
            }
        }

        if (c == null) {
            c = getParent().loadClass(name);
        }

        if (resolve) {
            resolveClass(c);
        }
        return c;
    }

    @Override
    public void destroy() {
        super.destroy();
        FileUtils.deleteQuietly(temporaryDirectory);
        isActive = false;
    }

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public File getTemporaryFolder() {
        return temporaryDirectory;
    }

    @Override
    public String toString() {
        return super.toString() + ", uuid=" + uuid + ", creationTime=" + creationTime + ", type=" + type + ", id=" + id + ", isActive: " + isActive + ", parent= " + getParent();
    }
}
