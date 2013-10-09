/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package com.bonitasoft.engine.service.impl;

import java.util.List;
import java.util.concurrent.TimeoutException;

import org.bonitasoft.engine.cache.CacheConfiguration;
import org.bonitasoft.engine.cache.CacheConfigurations;
import org.bonitasoft.engine.commons.ServiceWithLifecycle;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapConfig.EvictionPolicy;
import com.hazelcast.config.MapConfig.InMemoryFormat;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.config.MaxSizeConfig.MaxSizePolicy;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.LifecycleService;

/**
 * @author Baptiste Mesta
 */
public class BonitaHazelcastInstanceFactory implements ServiceWithLifecycle {

    private HazelcastInstance hazelcastInstance;

    public BonitaHazelcastInstanceFactory() {
    }

    public synchronized HazelcastInstance newHazelcastInstance(final Config config, final CacheConfigurations cacheConfigurations) {
        if (hazelcastInstance == null) {
            hazelcastInstance = createNewInstance(config, cacheConfigurations);
        }
        return hazelcastInstance;
    }

    private HazelcastInstance createNewInstance(final Config config, final CacheConfigurations cacheConfigurations) {
        initializeCacheConfigurations(config, cacheConfigurations);
        // set classloader to null in order to use the context classloader instead
        config.setClassLoader(null);
        HazelcastInstance hazelcastInstance2 = Hazelcast.newHazelcastInstance(config);
        return hazelcastInstance2;
    }

    /**
     * @param config
     * @param cacheConfigurations
     */
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
                NearCacheConfig nearCacheConfig = new NearCacheConfig();
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
    public void start() throws SBonitaException {
        // // Do nothing, spring start hazelcast...
    }

    @Override
    public void stop() throws SBonitaException, TimeoutException {
    }

    public void destroy() {
        LifecycleService lifecycleService = hazelcastInstance.getLifecycleService();
        lifecycleService.shutdown();
    }
}
