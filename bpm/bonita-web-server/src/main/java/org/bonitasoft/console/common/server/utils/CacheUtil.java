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

import static org.ehcache.config.builders.CacheConfigurationBuilder.newCacheConfigurationBuilder;
import static org.ehcache.config.builders.CacheManagerBuilder.newCacheManagerBuilder;
import static org.ehcache.config.builders.ResourcePoolsBuilder.newResourcePoolsBuilder;

import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.units.EntryUnit;

@Slf4j
public class CacheUtil {

    private static final int DEFAULT_ON_HEAP_MAX_ENTRIES = 10_000;

    protected static CacheManager CACHE_MANAGER = null;

    protected static synchronized CacheManager getCacheManager() {
        if (CACHE_MANAGER == null) {
            if (log.isInfoEnabled()) {
                log.info(
                        "Initializing Ehcache 3 CacheManager with programmatic configuration (heap-only, LRU eviction)");
            }
            // Create CacheManager with programmatic configuration (no XML needed)
            // Default cache template: heap-only with 10,000 entry capacity and LRU eviction
            // Note: Ehcache 3 uses LRU (Least Recently Used) eviction by default when heap capacity is exceeded
            CACHE_MANAGER = newCacheManagerBuilder()
                    .withCache("default", newCacheConfigurationBuilder(Object.class, Object.class,
                            newResourcePoolsBuilder().heap(DEFAULT_ON_HEAP_MAX_ENTRIES, EntryUnit.ENTRIES).build())
                            .build())
                    .build(true);
        }
        return CACHE_MANAGER;
    }

    protected static synchronized Cache<Object, Object> createCache(final CacheManager cacheManager,
            final String cacheName) {
        // Double-check
        Cache<Object, Object> cache = cacheManager.getCache(cacheName, Object.class, Object.class);
        if (cache == null) {
            // In Ehcache 3, we need to provide a configuration when creating a cache
            // Using a simple heap-based cache with default settings (10,000 entries)
            // Eviction policy: LRU (Least Recently Used) when heap capacity is exceeded
            cache = cacheManager.createCache(cacheName,
                    newCacheConfigurationBuilder(Object.class, Object.class,
                            newResourcePoolsBuilder().heap(DEFAULT_ON_HEAP_MAX_ENTRIES, EntryUnit.ENTRIES).build())
                            .build());
        }
        return cache;
    }

    public static void store(final String cacheName, final Object key, final Object value) {
        final CacheManager cacheManager = getCacheManager();
        Cache<Object, Object> cache = cacheManager.getCache(cacheName, Object.class, Object.class);
        if (cache == null) {
            cache = createCache(cacheManager, cacheName);
        }
        // In Ehcache 3, we directly put key-value without Element wrapper
        cache.put(key, value);

        if (log.isTraceEnabled()) {
            log.trace("####Element {} created in cache with name {}", key, cacheName);
        }
    }

    public static Object get(final String cacheName, final Object key) {
        Object value = null;
        final CacheManager cacheManager = getCacheManager();
        final Cache<Object, Object> cache = cacheManager.getCache(cacheName, Object.class, Object.class);
        if (cache != null) {
            // In Ehcache 3, get() returns the value directly (no Element wrapper)
            value = cache.get(key);
            if (value != null) {
                if (log.isTraceEnabled()) {
                    log.trace("####Element {} found in cache with name {}", key, cacheName);
                }
            } else {
                if (log.isTraceEnabled()) {
                    log.trace("####Element {} not found in cache with name {}", key, cacheName);
                }
            }
        } else {
            if (log.isTraceEnabled()) {
                log.trace("####Cache with name {} doesn't exists or wasn't created yet.", cacheName);
            }
        }
        return value;
    }

    public static void clear(final String cacheName) {
        final Cache<Object, Object> cache = getCacheManager().getCache(cacheName, Object.class, Object.class);
        if (cache != null) {
            // In Ehcache 3, clear() is used instead of removeAll()
            cache.clear();
        }
    }
}
