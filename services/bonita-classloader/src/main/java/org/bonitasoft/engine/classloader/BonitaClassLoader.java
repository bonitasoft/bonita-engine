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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.home.BonitaResource;

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

    private File temporaryDirectory;

    private boolean isActive = true;

    private final long creationTime;

    private String uuid;

    BonitaClassLoader(final Stream<BonitaResource> resources, final String type, final long id, final URI temporaryDirectoryUri, final ClassLoader parent) {
        super(type + "__" + id, new URL[]{}, parent);
        this.creationTime = System.currentTimeMillis();
        NullCheckingUtil.checkArgsNotNull(resources, type, id, temporaryDirectoryUri, parent);
        this.type = type;
        this.id = id;
        this.uuid = generateUUID();

        nonJarResources = new HashMap<>();
        urls = new HashSet<>();
        this.temporaryDirectory = createTemporaryDirectory(temporaryDirectoryUri);
        addResources(resources);
        addURLs(urls.toArray(new URL[urls.size()]));
    }

    private File createTemporaryDirectory(URI temporaryDirectoryUri) {
        File temporaryDirectory = new File(temporaryDirectoryUri);
        if (!temporaryDirectory.exists()) {
            temporaryDirectory.mkdirs();
        }
        temporaryDirectory = createFolderFromUUID(temporaryDirectory, uuid);
        if (temporaryDirectory.exists()) {
            uuid = generateUUID();
            //retry
            return createTemporaryDirectory(temporaryDirectoryUri);
        }
        temporaryDirectory.mkdir();
        return temporaryDirectory;
    }

    private File createFolderFromUUID(File temporaryDirectory, String uuid) {
        return new File(temporaryDirectory, uuid.substring(0, 5));
    }

    String generateUUID() {
        return UUID.randomUUID().toString();
    }

    protected void addResources(final Stream<BonitaResource> resources) {
        if (resources == null) {
            return;
        }
        resources.forEach((resource)->{
            if (resource.getName().matches(".*\\.jar")) {
                try {
                    final File file = writeResource(resource);
                    final String path = file.getAbsolutePath();
                    final URL url = new File(path).toURI().toURL();
                    urls.add(url);
                } catch (final IOException e) {
                    throw new BonitaRuntimeException(e);
                }
            } else {
                nonJarResources.put(resource.getName(), resource.getContent());
            }
        });
    }

    File writeResource(BonitaResource resource) throws IOException {
        final File file = File.createTempFile(resource.getName(), ".jar", temporaryDirectory);
        IOUtil.write(file, resource.getContent());
        return file;
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
        return super.toString() + ", uuid=" + uuid + ", creationTime=" + creationTime + ", type=" + type + ", id=" + id + ", isActive: " + isActive
                + ", parent= " + getParent();
    }
}
