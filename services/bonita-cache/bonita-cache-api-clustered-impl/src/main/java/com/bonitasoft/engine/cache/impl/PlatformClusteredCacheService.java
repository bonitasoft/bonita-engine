package com.bonitasoft.engine.cache.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bonitasoft.engine.cache.CacheException;
import org.bonitasoft.engine.cache.PlatformCacheService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

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
        final Collection<DistributedObject> distributedObjects = hazelcastInstance.getDistributedObjects();
        final ArrayList<String> cacheNamesList = new ArrayList<String>(distributedObjects.size());

        final String prefix = "P_";
        for (final DistributedObject instance : distributedObjects) {
            if (instance instanceof IMap<?, ?> && instance.getName().startsWith(prefix)) {
                cacheNamesList.add(getCacheNameFromKey(instance.getName()));
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
