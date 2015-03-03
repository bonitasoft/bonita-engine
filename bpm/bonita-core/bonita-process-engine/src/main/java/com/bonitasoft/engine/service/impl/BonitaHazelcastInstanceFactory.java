/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.service.impl;

import java.io.IOException;
import java.util.List;

import org.bonitasoft.engine.cache.CacheConfiguration;
import org.bonitasoft.engine.cache.CacheConfigurations;
import org.bonitasoft.engine.commons.PlatformLifecycleService;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapConfig.EvictionPolicy;
import com.hazelcast.config.MapConfig.InMemoryFormat;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.config.MaxSizeConfig.MaxSizePolicy;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class BonitaHazelcastInstanceFactory implements PlatformLifecycleService {

    private HazelcastInstance hazelcastInstance;

    public BonitaHazelcastInstanceFactory() {
    }

    public synchronized HazelcastInstance newHazelcastInstance(final Config config, final CacheConfigurations cacheConfigurations, final boolean enableStats,
            final String statsPath, final long statsPrintInterval) throws IOException {
        if (hazelcastInstance == null) {
            hazelcastInstance = createNewInstance(config, cacheConfigurations);
            if (enableStats) {
                initThreadThatDumpStats(hazelcastInstance, statsPath, statsPrintInterval);
            }
        }
        return hazelcastInstance;
    }

    private void initThreadThatDumpStats(final HazelcastInstance hazelcastInstance, final String statsPath, final long statsPrintInterval) throws IOException {
        final Thread thread = new Thread(new HazelcastStatExtractor(hazelcastInstance, statsPath, statsPrintInterval));
        thread.start();
    }

    private HazelcastInstance createNewInstance(final Config config, final CacheConfigurations cacheConfigurations) {
        initializeCacheConfigurations(config, cacheConfigurations);
        // set classloader to null in order to use the context classloader instead
        config.setClassLoader(null);
        final HazelcastInstance hazelcastInstance2 = Hazelcast.newHazelcastInstance(config);

        return hazelcastInstance2;
    }

    private void initializeCacheConfigurations(final Config config, final CacheConfigurations cacheConfigurations) {
        final List<CacheConfiguration> configurations = cacheConfigurations.getConfigurations();
        for (final CacheConfiguration cacheConfiguration : configurations) {
            // use wildcard because name of caches are: "<tenant id>_<cache name>"
            final MapConfig mapConfig = new MapConfig("*" + cacheConfiguration.getName());

            mapConfig.setTimeToLiveSeconds(cacheConfiguration.isEternal() ? 0 : new Long(cacheConfiguration.getTimeToLiveSeconds()).intValue());

            final MaxSizeConfig maxSizeConfig = new MaxSizeConfig();
            maxSizeConfig.setMaxSizePolicy(MaxSizePolicy.PER_PARTITION);
            maxSizeConfig.setSize(cacheConfiguration.getMaxElementsInMemory());
            mapConfig.setMaxSizeConfig(maxSizeConfig);

            if (cacheConfiguration.isReadIntensive()) {
                final NearCacheConfig nearCacheConfig = new NearCacheConfig();
                nearCacheConfig.setInMemoryFormat(InMemoryFormat.OBJECT);
                nearCacheConfig.setName("nearCacheConfig-" + cacheConfiguration.getName());
                mapConfig.setNearCacheConfig(nearCacheConfig);
            }

            // can be: NONE (no eviction), LRU (Least Recently Used), LFU (Least Frequently Used). NONE is the default
            mapConfig.setEvictionPolicy(EvictionPolicy.valueOf(cacheConfiguration.getEvictionPolicy()));

            // What about the isMemoryOnly ?

            config.addMapConfig(mapConfig);
        }
    }

    public synchronized HazelcastInstance getInstance() {
        if (hazelcastInstance != null) {
            return hazelcastInstance;
        }
        throw new IllegalStateException("The hazelcast instance has not been created. You may not be executed in clustered environment.");
    }

    @Override
    public void start() {
        // Do nothing, spring start hazelcast...
    }

    @Override
    public void stop() {
    }

    public void destroy() {
        try {

            final com.hazelcast.core.LifecycleService lifecycleService = hazelcastInstance.getLifecycleService();
            lifecycleService.shutdown();
        } catch (HazelcastInstanceNotActiveException e) {
            // ignore it, it's already shutdown
        }
    }

    @Override
    public void pause() {
        // nothing to do
    }

    @Override
    public void resume() {
        // nothing to do
    }
}
