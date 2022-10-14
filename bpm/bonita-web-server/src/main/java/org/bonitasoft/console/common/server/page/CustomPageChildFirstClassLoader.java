/**
 * Copyright (C) 2011-2015 Bonitasoft S.A.
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
package org.bonitasoft.console.common.server.page;

import static org.apache.commons.io.FileUtils.deleteQuietly;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Elias Ricken de Medeiros
 * @author Charles Souillard
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Romain Bioteau
 *         A Classloader adding given custom page resources and bdm resources in its classpath.
 *         This classloader is versioned with a runtime bdm version and should be discard when bdm is updated
 */
public class CustomPageChildFirstClassLoader extends MonoParentJarFileClassLoader implements VersionedClassloader {

    protected final Map<String, byte[]> nonJarResources = new HashMap<>();

    private boolean isActive = true;

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomPageChildFirstClassLoader.class.getName());

    private final CustomPageDependenciesResolver customPageDependenciesResolver;

    private final BDMClientDependenciesResolver bdmDependenciesResolver;

    private final String version;

    CustomPageChildFirstClassLoader(String pageName,
            CustomPageDependenciesResolver customPageDependenciesResolver,
            BDMClientDependenciesResolver bdmDependenciesResolver,
            ClassLoader parent) {
        super(pageName, new URL[] {}, parent);
        this.customPageDependenciesResolver = customPageDependenciesResolver;
        this.bdmDependenciesResolver = bdmDependenciesResolver;
        this.version = bdmDependenciesResolver.getBusinessDataModelVersion();
    }

    public void addCustomPageResources() throws IOException {
        addBDMDependencies();
        addOtherDependencies();
    }

    private void addBDMDependencies() {
        try {
            addURLs(bdmDependenciesResolver.getBDMDependencies());
        } catch (final IOException e) {
            LOGGER.error("Failed to add BDM dependencies in ClassLoader", e);
        }
    }

    private void addOtherDependencies() throws IOException {
        final Map<String, byte[]> customPageDependencies = customPageDependenciesResolver
                .resolveCustomPageDependencies();
        for (final Map.Entry<String, byte[]> resource : customPageDependencies.entrySet()) {
            if (resource.getKey().matches(".*\\.jar") && !bdmDependenciesResolver.isABDMDependency(resource.getKey())) {
                final byte[] data = resource.getValue();
                final File file = File.createTempFile(resource.getKey(), null,
                        customPageDependenciesResolver.getTempFolder());
                file.deleteOnExit();
                FileUtils.writeByteArrayToFile(file, data);
                addURL(new File(file.getAbsolutePath()).toURI().toURL());
            } else {
                nonJarResources.put(resource.getKey(), resource.getValue());
            }
        }
    }

    @Override
    public InputStream getResourceAsStream(final String name) {
        /*
         * if (!isActive) {
         * throw new RuntimeException(this.toString() + " is not active anymore. Don't use it.");
         * }
         */
        InputStream is = getResourceAsStreamInternal(name);
        if (is == null && name.length() > 0 && name.charAt(0) == '/') {
            is = getResourceAsStreamInternal(name.substring(1));
        }
        return is;
    }

    protected InputStream getResourceAsStreamInternal(final String name) {
        final byte[] classData = loadProcessResource(name);
        if (classData != null) {
            return new ByteArrayInputStream(classData);
        }
        return getResourceAsStreamRegular(name);
    }

    protected InputStream getResourceAsStreamRegular(final String name) {
        return super.getResourceAsStream(name);
    }

    private byte[] loadProcessResource(final String resourceName) {
        return nonJarResources.containsKey(resourceName) ? nonJarResources.get(resourceName) : null;
    }

    @Override
    protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        /*
         * if (!isActive) {
         * throw new RuntimeException(this.toString() + " is not active anymore. Don't use it.");
         * }
         */
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

    // never called. Is it normal?
    public void release() {
        deleteQuietly(customPageDependenciesResolver.getTempFolder());
        isActive = false;
    }

    @Override
    public String toString() {
        return super.toString() + ", name=" + getName() + ", isActive: " + isActive + ", parent= " + getParent();
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public boolean hasVersion(String version) {
        return Objects.equals(getVersion(), version);
    }
}
