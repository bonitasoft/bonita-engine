package com.bonitasoft.engine.cache.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bonitasoft.engine.cache.CacheException;
import org.bonitasoft.engine.cache.PlatformCacheService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Instance;
import com.hazelcast.core.Prefix;

public class PlatformClusteredCacheService extends CommonClusteredCacheService implements PlatformCacheService {

    public PlatformClusteredCacheService(TechnicalLoggerService logger, HazelcastInstance hazelcastInstance) {
        super(logger, hazelcastInstance);
    }

    @Override
    protected String getKeyFromCacheName(final String cacheName) throws CacheException {
        return "P_" + cacheName;
    }

    @Override
    public List<String> getCachesNames() {
        final Collection<Instance> instances = hazelcastInstance.getInstances();
        final ArrayList<String> cacheNamesList = new ArrayList<String>(instances.size());

        // Hazelcast Maps are internally named with a prefix
        final String prefix = Prefix.MAP + "P_";
        for (final Instance instance : instances) {
            if (instance.getInstanceType().isMap() && ((String) instance.getId()).startsWith(prefix)) {
                cacheNamesList.add(getCacheNameFromKey(((String) instance.getId()).substring(Prefix.MAP.length())));
            }
        }

        return cacheNamesList;
    }

    /**
     * @param cacheName
     * @return
     */
    private String getCacheNameFromKey(final String cacheNameKey) {
        return cacheNameKey.substring(cacheNameKey.indexOf('_') + 1);
    }

}
