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

import org.bonitasoft.engine.cache.CacheConfiguration;
import org.bonitasoft.engine.cache.CacheConfigurations;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapConfig.EvictionPolicy;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.config.MaxSizeConfig.MaxSizePolicy;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

/**
 * @author Baptiste Mesta
 */
public class BonitaHazelcastInstanceFactory {

    private static HazelcastInstance hazelCastInstance;

    public static synchronized HazelcastInstance newHazelcastInstance(final Config config, final CacheConfigurations cacheConfigurations) {
        if (hazelCastInstance == null) {
            final List<CacheConfiguration> configurations = cacheConfigurations.getConfigurations();
            for (final CacheConfiguration cache : configurations) {
                final CacheConfiguration cacheConfiguration = cache;
                // use wildcard because name of caches are: "<tenant id>_<cache name>"
                final MapConfig mapConfig = new MapConfig("*" + cache.getName());
                mapConfig.setTimeToLiveSeconds(cacheConfiguration.isEternal() ? 0 : new Long(cacheConfiguration.getTimeToLiveSeconds()).intValue());
                final MaxSizeConfig maxSizeConfig = new MaxSizeConfig();
                maxSizeConfig.setMaxSizePolicy(MaxSizePolicy.PER_PARTITION);
                maxSizeConfig.setSize(cacheConfiguration.getMaxElementsInMemory());
                mapConfig.setMaxSizeConfig(maxSizeConfig);
                // can be: NONE (no eviction), LRU (Least Recently Used), LFU (Least Frequently Used). NONE is the default
                mapConfig.setEvictionPolicy(EvictionPolicy.valueOf(cacheConfiguration.getEvictionPolicy()));
                config.addMapConfig(mapConfig);
            }
            // set classloader to null in order to use the context classloader instead
            config.setClassLoader(null);
            hazelCastInstance = Hazelcast.newHazelcastInstance(config);
        }
        return hazelCastInstance;
    }

    public static synchronized HazelcastInstance getInstance() {
        if (hazelCastInstance != null) {
            return hazelCastInstance;
        }
        throw new IllegalStateException("The hazelcast instance has not been created. You may not be executed in clustered environment.");
    }
}
