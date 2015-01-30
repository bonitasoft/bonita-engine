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
package org.bonitasoft.engine.builder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class BuilderFactory {

    private static final String BUILDER_FACTORIES_DEFAULT_FILE = "builder-factories.properties";

    private static final String BUILDER_FACTORIES_EXTENSION_FILE = "builder-factories-ext.properties";

    private final Map<String, Object> factoryCache;

    private final Properties properties;

    private static final Object MUTEX = new BuilderFactoryMutex();

    private static BuilderFactory INSTANCE = null;

    private BuilderFactory(final Properties properties) {
        this.properties = properties;
        factoryCache = new HashMap<String, Object>();
    }

    private static final class BuilderFactoryMutex {

    }

    public static BuilderFactory getInstance() {
        if (INSTANCE == null) {
            synchronized (MUTEX) {
                // ensure we do not create many instances of this class
                if (INSTANCE == null) {
                    URL defaultFileURL = null;
                    try {
                        defaultFileURL = BuilderFactory.class.getResource(BUILDER_FACTORIES_DEFAULT_FILE);
                        final Properties defaultProperties = getProperties(defaultFileURL);
                        final Properties allProperties = new Properties(defaultProperties);

                        final URL extensionFileURL = BuilderFactory.class.getResource(BUILDER_FACTORIES_EXTENSION_FILE);
                        if (extensionFileURL != null) {
                            final Properties extensionProperties = getProperties(extensionFileURL);
                            allProperties.putAll(extensionProperties);
                        }

                        INSTANCE = new BuilderFactory(allProperties);

                    } catch (final Exception e) {
                        throw new RuntimeException("Unable to load builder factories from : fileURL=" + defaultFileURL);
                    }
                }
            }
        }
        return INSTANCE;
    }

    private synchronized void cacheFactory(final String interfaceName, final String className) {
        try {
            if (className == null || "null".equals(className)) {
                throw new Exception("Factory implementation of " + interfaceName + " is required.");
            }
            final Class<?> clazz = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
            final Object factory = clazz.newInstance();
            factoryCache.put(interfaceName, factory);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends Object> T get(final Class<T> clazz) {
        final T factoryImplementation = getInstance().getInternalBuilderFactory(clazz);
        if (factoryImplementation == null) {
            throw new RuntimeException("No factory found for interface: " + clazz);
        }
        return factoryImplementation;
    }

    @SuppressWarnings("unchecked")
    private <T extends Object> T getInternalBuilderFactory(final Class<T> clazz) {
        if (!factoryCache.containsKey(clazz.getName())) {
            cacheFactory(clazz.getName(), properties.getProperty(clazz.getName()));
        }
        return (T) factoryCache.get(clazz.getName());
    }

    public static Properties getProperties(final URL url) throws IOException {
        final InputStreamReader reader = new InputStreamReader(url.openStream());
        return getProperties(reader);
    }

    private static Properties getProperties(final Reader reader) throws IOException {
        final Properties properties = new Properties();
        try {
            properties.load(reader);
            return properties;
        } finally {
            reader.close();
        }
    }

}
