/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class BonitaClassLoader extends MonoParentJarFileClassLoader {

    private final ClassLoaderIdentifier id;
    protected Map<String, byte[]> nonJarResources = new HashMap<>();
    protected Set<URL> urls = new HashSet<>();
    private final File temporaryDirectory;
    private boolean isActive = true;
    private final long creationTime = System.currentTimeMillis();
    private final String uuid = generateUUID();
    private Set<BonitaClassLoader> children = new HashSet<>();

    BonitaClassLoader(Stream<BonitaResource> resources, ClassLoaderIdentifier id, URI temporaryDirectoryUri,
            ClassLoader parent) throws IOException {
        super(id.getType().name() + "__" + id.getId(), new URL[] {}, parent);
        NullCheckingUtil.checkArgsNotNull(resources, id, temporaryDirectoryUri, parent);
        this.id = id;
        this.temporaryDirectory = createTemporaryDirectory(temporaryDirectoryUri, uuid);
        addResources(resources);
        addURLs(urls.toArray(new URL[0]));
        log.debug("Created {}", this);
    }

    private static File createTemporaryDirectory(URI temporaryDirectoryUri, String uuid) throws IOException {
        Path temporaryDirectory = new File(temporaryDirectoryUri).toPath();
        if (!Files.exists(temporaryDirectory)) {
            Files.createDirectory(temporaryDirectory);
        }
        Path tempDir = temporaryDirectory.resolve(uuid.substring(0, 8));
        Files.createDirectory(tempDir);
        return tempDir.toFile();
    }

    private static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    protected void addResources(final Stream<BonitaResource> resources) {
        if (resources == null) {
            return;
        }
        resources.forEach(resource -> {
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
        InputStream is = getInternalInputStream(name);
        if (is == null && name.length() > 0 && name.charAt(0) == '/') {
            is = getInternalInputStream(name.substring(1));
        }
        return is;
    }

    private InputStream getInternalInputStream(final String name) {
        final byte[] classData = loadProcessResource(name);
        if (classData != null) {
            return new ByteArrayInputStream(classData);
        }
        return super.getResourceAsStream(name);
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
        log.debug("Destroyed {}", this);
    }

    public ClassLoaderIdentifier getId() {
        return id;
    }

    public File getTemporaryFolder() {
        return temporaryDirectory;
    }

    @Override
    public String toString() {
        return "[" + this.getClass().getName() + ":" + " uuid=" + uuid + ", name=" + this.getName() + ", creationTime="
                + creationTime + ", id=" + id
                + ", isActive: " + isActive
                + ", parent= " + getParent();
    }
}
