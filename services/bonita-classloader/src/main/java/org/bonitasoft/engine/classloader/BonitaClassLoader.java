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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.commons.ExceptionUtils;
import org.bonitasoft.engine.data.instance.model.impl.XStreamFactory;

/**
 * @author Elias Ricken de Medeiros
 * @author Charles Souillard
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
@Slf4j
public class BonitaClassLoader extends URLClassLoader {

    private final ClassLoaderIdentifier id;
    protected Map<String, File> nonJarResources;
    // that directory contains the jars and resources given in the constructor
    private final File temporaryDirectory;
    private boolean isActive = true;
    private final Instant creationTime = Instant.now();
    private final String uuid = generateUUID();
    private final Set<BonitaClassLoader> children = new HashSet<>();

    BonitaClassLoader(ClassLoaderIdentifier id, ClassLoader parent, Set<File> jars, Map<String, File> nonJarResources,
            File temporaryDirectory) {
        super(id.getType().name() + "__" + id.getId(), jars.stream().map(BonitaClassLoader::toURL).toArray(URL[]::new),
                parent);
        this.id = id;
        //TODO: These non-jar resources might be added along with jars without having to do special handling
        this.nonJarResources = new HashMap<>(nonJarResources);
        this.temporaryDirectory = temporaryDirectory;
        if (parent instanceof BonitaClassLoader) {
            //The parent is not a BonitaClassloader when we are on the Global classloader
            ((BonitaClassLoader) parent).children.add(this);
            log.debug("Classloader {} added as a child of {}", this, parent);
        }
        if (log.isDebugEnabled()) {
            log.debug("Classloader {} created with \n jars: {}, \n nonJarResources: {}", this,
                    jars.stream().map(File::getPath).collect(Collectors.joining(", ")),
                    nonJarResources.values().stream().map(File::getPath).collect(Collectors.joining(", ")));
        }
    }

    private static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    private static URL toURL(File f) {
        try {
            return f.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
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
        File classData = nonJarResources.get(name);
        if (classData != null) {
            try {
                return new FileInputStream(classData);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return super.getResourceAsStream(name);
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

    public void destroy() {
        XStreamFactory.remove(this);
        ClassLoader parent = getParent();
        if (parent instanceof BonitaClassLoader) {
            //The parent is not a BonitaClassloader when we are on the Global classloader
            ((BonitaClassLoader) parent).children.remove(this);
        }
        try {
            super.close();
        } catch (IOException e) {
            log.warn("Unable to close the classloader {}. Some file might still be present in {}. Cause {}", id,
                    temporaryDirectory.getAbsolutePath(),
                    ExceptionUtils.printLightWeightStacktrace(e));
            log.debug("Full cause:", e);
        }
        FileUtils.deleteQuietly(temporaryDirectory);
        isActive = false;
        log.debug("Destroyed {}", this);
    }

    public boolean isDestroyed() {
        return !isActive;
    }

    public ClassLoaderIdentifier getIdentifier() {
        return id;
    }

    public File getTemporaryFolder() {
        return temporaryDirectory;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public Set<BonitaClassLoader> getChildren() {
        return Collections.unmodifiableSet(new HashSet<>(children));
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BonitaClassLoader.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("isActive=" + isActive)
                .add("creationTime=" + creationTime)
                .add("uuid='" + uuid + "'")
                .add("children=" + children.stream().map(BonitaClassLoader::getIdentifier)
                        .map(ClassLoaderIdentifier::toString).collect(Collectors.joining(", ", "[", "]")))
                .toString();
    }
}
