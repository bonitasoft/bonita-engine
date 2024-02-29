/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.console.common.server.utils;

import java.io.File;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import net.sf.ehcache.config.DiskStoreConfiguration;
import org.bonitasoft.console.common.server.preferences.properties.ConfigurationFilesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheUtil {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheUtil.class.getName());

    protected static CacheManager CACHE_MANAGER = null;

    protected static synchronized CacheManager getCacheManager(final String diskStorePath) {
        if (CACHE_MANAGER == null) {
            File cacheConfigFile = ConfigurationFilesManager.getInstance()
                    .getPlatformConfigurationFile("cache-config.xml");
            if (cacheConfigFile != null && cacheConfigFile.exists()) {
                final Configuration configuration = ConfigurationFactory.parseConfiguration(cacheConfigFile);
                final DiskStoreConfiguration diskStoreConfiguration = new DiskStoreConfiguration();
                diskStoreConfiguration.setPath(diskStorePath);
                configuration.addDiskStore(diskStoreConfiguration);
                CACHE_MANAGER = CacheManager.create(configuration);
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn(
                            "Unable to retrieve the cache configuration file. Creating a cache manager with the default configuration");
                }
                CACHE_MANAGER = CacheManager.create();
            }
        }
        return CACHE_MANAGER;
    }

    protected static synchronized Cache createCache(final CacheManager cacheManager, final String cacheName) {
        // Double-check
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            cacheManager.addCache(cacheName);
            cache = cacheManager.getCache(cacheName);
        }
        return cache;
    }

    public static void store(final String diskStorePath, final String cacheName, final Object key, final Object value) {
        final CacheManager cacheManager = getCacheManager(diskStorePath);
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            cache = createCache(cacheManager, cacheName);
        }
        final Element element = new Element(key, value);
        cache.put(element);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("####Element " + key + " created in cache with name " + cacheName);
        }
    }

    public static Object get(final String diskStorePath, final String cacheName, final Object key) {
        Object value = null;
        final CacheManager cacheManager = getCacheManager(diskStorePath);
        final Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            final Element element = cache.get(key);
            if (element != null) {
                value = element.getValue();
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("####Element " + key + " found in cache with name " + cacheName);
                }
            } else {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("####Element " + key + " not found in cache with name " + cacheName);
                }
            }
        } else {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("####Cache with name " + cacheName + " doesn't exists or wasn't created yet.");
            }
        }
        return value;
    }

    public static void clear(final String diskStorePath, final String cacheName) {
        final CacheManager cacheManager = getCacheManager(diskStorePath);
        final Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.removeAll();
        }
    }
}
