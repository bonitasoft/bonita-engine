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
package org.bonitasoft.engine.cache.ehcache;

import static org.ehcache.config.builders.ExpiryPolicyBuilder.timeToLiveExpiration;

import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.cache.CacheConfiguration;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.cache.SCacheException;
import org.bonitasoft.engine.commons.PlatformLifecycleService;
import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.core.internal.statistics.DefaultStatisticsService;
import org.ehcache.core.spi.service.StatisticsService;
import org.ehcache.core.statistics.CacheStatistics;
import org.ehcache.core.statistics.TierStatistics;
import org.ehcache.impl.config.serializer.DefaultSerializerConfiguration;
import org.ehcache.impl.config.serializer.DefaultSerializerConfiguration.Type;
import org.ehcache.impl.serialization.PlainJavaSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Matthieu Chaffotte
 */
// Must be started before the work service so it has a "higher" priority
@Order(2)
@Component
@ConditionalOnSingleCandidate(CacheService.class)
@Slf4j
public class EhCacheCacheService implements CacheService, PlatformLifecycleService {

    protected CacheManager cacheManager;

    protected StatisticsService statisticsService;

    protected final Map<String, org.ehcache.config.CacheConfiguration<Object, Object>> cacheConfigurations;

    private final org.ehcache.config.CacheConfiguration<Object, Object> defaultCacheConfiguration;

    public EhCacheCacheService(List<CacheConfiguration> cacheConfigurations,
            @Qualifier("defaultCacheConfiguration") CacheConfiguration defaultCacheConfiguration) {
        this.defaultCacheConfiguration = getEhCacheConfiguration(defaultCacheConfiguration);
        if (cacheConfigurations != null && !cacheConfigurations.isEmpty()) {
            this.cacheConfigurations = new HashMap<>(cacheConfigurations.size());
            for (final CacheConfiguration cacheConfig : cacheConfigurations) {
                this.cacheConfigurations.put(cacheConfig.getName(), getEhCacheConfiguration(cacheConfig));
            }
        } else {
            this.cacheConfigurations = Collections.emptyMap();
        }
    }

    // VisibleForTesting
    protected Set<String> getCacheConfigurationNames() {
        return cacheConfigurations.keySet();
    }

    protected org.ehcache.config.CacheConfiguration<Object, Object> getEhCacheConfiguration(
            final CacheConfiguration cacheConfig) {
        // Build resource pools based on configuration
        // Ehcache 3 disk storage requires explicit serialization configuration which is complex
        // For simplicity, use heap-only caching or heap + offheap (which doesn't require complex serialization)
        // If more capacity is needed beyond heap, offheap provides better performance than disk anyway
        ResourcePoolsBuilder poolsBuilder = ResourcePoolsBuilder.newResourcePoolsBuilder()
                .heap(cacheConfig.getMaxElementsInMemory(), EntryUnit.ENTRIES);

        // Add off-heap tier if configured (provides overflow capacity without disk I/O)
        // Off-heap memory is outside JVM heap, so it doesn't participate in GC
        // This reduces GC pressure while maintaining good performance (all in RAM)
        if (cacheConfig.getOffHeapSizeMB() > 0) {
            poolsBuilder = poolsBuilder.offheap(cacheConfig.getOffHeapSizeMB(), MemoryUnit.MB);
            log.debug("Configuring cache '{}' with off-heap storage: {}MB",
                    cacheConfig.getName(), cacheConfig.getOffHeapSizeMB());
        }

        org.ehcache.config.ResourcePools resourcePools = poolsBuilder.build();

        // Note: Disk storage removed to avoid serialization complexity
        // Ehcache 2 disk overflow is replaced by heap + optional off-heap tiers in Ehcache 3

        // Build cache configuration
        CacheConfigurationBuilder<Object, Object> builder = CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Object.class, Object.class, resourcePools);

        // Configure serialization for off-heap storage
        // Off-heap requires serialization to convert objects to byte arrays for native memory storage
        if (cacheConfig.getOffHeapSizeMB() > 0) {
            builder = builder
                    .withService(new DefaultSerializerConfiguration(PlainJavaSerializer.class, Type.KEY))
                    .withService(new DefaultSerializerConfiguration(PlainJavaSerializer.class, Type.VALUE));
        }

        // Set expiry policy
        if (!cacheConfig.isEternal()) {
            final long timeToLiveSeconds = cacheConfig.getTimeToLiveSeconds();
            builder = builder.withExpiry(timeToLiveExpiration(Duration.ofSeconds(timeToLiveSeconds)));
        }

        return builder.build();
    }

    @Override
    public List<String> getCachesNames() {
        if (cacheManager == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(cacheManager.getRuntimeConfiguration().getCacheConfigurations().keySet());
    }

    @Override
    public synchronized void stop() {
        shutdownCacheManager();
    }

    @Override
    public void pause() {
        try {
            clearAll();
        } catch (final SCacheException sce) {
            throw new SBonitaRuntimeException(sce);
        }
    }

    @Override
    public void resume() throws SCacheException {
        if (cacheManager == null) {
            start();
        }
    }

    protected synchronized Cache<Object, Object> createCache(final String cacheName)
            throws SCacheException {
        if (cacheManager == null) {
            throw new SCacheException("The cache is not started, call start() on the cache service");
        }
        Cache<Object, Object> cache = cacheManager.getCache(cacheName, Object.class, Object.class);
        if (cache == null) {
            final org.ehcache.config.CacheConfiguration<Object, Object> cacheConfiguration = cacheConfigurations
                    .get(cacheName);
            final org.ehcache.config.CacheConfiguration<Object, Object> configToUse;
            if (cacheConfiguration != null) {
                configToUse = cacheConfiguration;
            } else {
                log.warn("No specific cache configuration found for cache '{}'. Using default configuration",
                        cacheName);
                configToUse = defaultCacheConfiguration;
            }
            // In Ehcache 3, we need to create the cache through the CacheManager
            // This requires rebuilding the CacheManager with the new cache, or using a mutable manager
            // For now, we'll create it dynamically - this may require CacheManagerBuilder.build() with persistence
            cache = cacheManager.createCache(cacheName, configToUse);
        }
        return cache;
    }

    @Override
    public void store(final String cacheName, final Serializable key, final Object value) throws SCacheException {
        if (cacheManager == null) {
            throw new SCacheException("The cache is not started, call start() on the cache service");
        }
        try {
            Cache<Object, Object> cache = cacheManager.getCache(cacheName, Object.class, Object.class);
            if (cache == null) {
                cache = createCache(cacheName);
            }
            // In Ehcache 3, we directly put key-value without Element wrapper
            cache.put(key, value);
        } catch (final IllegalStateException e) {
            throw new SCacheException("The cache '" + cacheName + "' is not alive", e);
        } catch (final RuntimeException e) {
            throw new SCacheException("Error storing value in cache '" + cacheName + "'", e);
        }
    }

    @Override
    public Object get(final String cacheName, final Object key) throws SCacheException {
        if (cacheManager == null) {
            return null;
        }
        try {
            final Cache<Object, Object> cache = cacheManager.getCache(cacheName, Object.class, Object.class);
            if (cache == null) {
                // the cache does not exist = the key was not stored
                return null;
            }
            // In Ehcache 3, get() returns the value directly (no Element wrapper)
            return cache.get(key);
        } catch (final IllegalStateException e) {
            throw new SCacheException("The cache '" + cacheName + "' is not alive", e);
        } catch (final Exception e) {
            throw new SCacheException("Error getting value from cache '" + cacheName + "'", e);
        }
    }

    @Override
    public boolean clear(final String cacheName) throws SCacheException {
        if (cacheManager == null) {
            return true;
        }
        try {
            final Cache<Object, Object> cache = cacheManager.getCache(cacheName, Object.class, Object.class);
            if (cache != null) {
                // In Ehcache 3, clear() is used instead of removeAll()
                cache.clear();
            }
            return cache == null;
        } catch (final IllegalStateException e) {
            throw new SCacheException("The cache '" + cacheName + "' is not alive", e);
        } catch (final Exception e) {
            throw new SCacheException("Error clearing cache '" + cacheName + "'", e);
        }
    }

    /**
     * <pre>
     * getCacheSize(cacheName)
     *           ↓
     * [Check cache exists]
     *           ↓
     * [Try Statistics API] ← O(1) - FAST
     *     ├─ Success → Return size from tier statistics
     *     └─ Failure → Log debug + Fall through
     *           ↓
     * [Iterate cache entries] ← O(n) - Fallback
     *           ↓
     * Return count
     * </pre>
     *
     * @param cacheName The name of cache
     */
    @Override
    public int getCacheSize(final String cacheName) throws SCacheException {
        if (cacheManager == null) {
            return 0;
        }
        try {
            final Cache<Object, Object> cache = cacheManager.getCache(cacheName, Object.class, Object.class);
            if (cache == null) {
                return 0;
            }
            // Try to use statistics API for efficient size retrieval (O(1) complexity)
            if (statisticsService != null) {
                try {
                    CacheStatistics cacheStatistics = statisticsService.getCacheStatistics(cacheName);
                    if (cacheStatistics != null) {
                        Map<String, TierStatistics> tierStatistics = cacheStatistics.getTierStatistics();
                        if (tierStatistics != null) {
                            // Sum up entries across all tiers (OnHeap, OffHeap, Disk)
                            long totalSize = tierStatistics.values().stream()
                                    .mapToLong(TierStatistics::getMappings)
                                    .sum();
                            return (int) totalSize;
                        }
                    }
                } catch (Exception e) {
                    log.debug("Failed to get cache size from statistics for cache '{}', falling back to iteration: {}",
                            cacheName, e.getMessage());
                    // Fall through to iteration method
                }
            }
            // Fallback: iterate through cache entries (O(n) complexity)
            // This can be expensive for large caches
            return countEntriesByIteration(cache);
        } catch (final IllegalStateException e) {
            throw new SCacheException("The cache '" + cacheName + "' is not alive", e);
        } catch (final RuntimeException e) {
            throw new SCacheException("Error getting size of cache '" + cacheName + "'", e);
        }
    }

    private int countEntriesByIteration(Cache<Object, Object> cache) {
        int count = 0;
        for (Cache.Entry<Object, Object> ignored : cache) {
            count++;
        }
        return count;
    }

    @Override
    public void clearAll() throws SCacheException {
        if (cacheManager == null) {
            return;
        }
        try {
            final List<String> cacheNames = getCachesNames();
            for (final String cacheName : cacheNames) {
                clear(cacheName);
            }
        } catch (final RuntimeException e) {
            throw new SCacheException("Error clearing all caches", e);
        }
    }

    @Override
    public boolean remove(final String cacheName, final Object key) throws SCacheException {
        if (cacheManager == null) {
            return false;
        }
        final Cache<Object, Object> cache = cacheManager.getCache(cacheName, Object.class, Object.class);
        if (cache == null) {
            return false;
        }
        // In Ehcache 3, remove() returns void, so we need to check if key existed first
        boolean existed = cache.containsKey(key);
        cache.remove(key);
        return existed;
    }

    @Override
    public List<Object> getKeys(final String cacheName) throws SCacheException {
        if (cacheManager == null) {
            return Collections.emptyList();
        }
        try {
            final Cache<Object, Object> cache = cacheManager.getCache(cacheName, Object.class, Object.class);
            if (cache == null) {
                return Collections.emptyList();
            }
            // In Ehcache 3, we need to iterate over entries to get keys
            List<Object> keys = new ArrayList<>();
            for (Cache.Entry<Object, Object> entry : cache) {
                keys.add(entry.getKey());
            }
            return keys;
        } catch (final IllegalStateException e) {
            throw new SCacheException("The cache '" + cacheName + "' is not alive", e);
        } catch (final RuntimeException e) {
            throw new SCacheException("Error getting keys from cache '" + cacheName + "'", e);
        }
    }

    protected void shutdownCacheManager() {
        if (cacheManager != null) {
            // In Ehcache 3, close() is used instead of shutdown()
            cacheManager.close();
            cacheManager = null;
        }
    }

    @Override
    public boolean isStopped() {
        return cacheManager == null;
    }

    protected String getCacheManagerName() {
        return "BONITA";
    }

    @Override
    public synchronized void start() throws SCacheException {
        if (cacheManager == null) {
            // Initialize statistics service for efficient cache size retrieval
            statisticsService = new DefaultStatisticsService();
            // In Ehcache 3, create a simple heap-only CacheManager with statistics enabled
            // Disk persistence removed for simplicity (requires complex serialization configuration)
            cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                    .using(statisticsService)
                    .build(true); // true = initialize immediately
        }
    }
}
