package com.bonitasoft.engine.cache.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bonitasoft.engine.cache.CacheException;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.TenantIdNotSetException;

import com.bonitasoft.manager.Manager;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Instance;
import com.hazelcast.core.Prefix;

public class ClusteredCacheService extends CommonClusteredCacheService implements CacheService {

    private final ReadSessionAccessor sessionAccessor;

    public ClusteredCacheService(Manager manager, TechnicalLoggerService logger, ReadSessionAccessor sessionAccessor, HazelcastInstance hazelcastInstance) {
        super(manager, logger, hazelcastInstance);
        this.sessionAccessor = sessionAccessor;
    }

    public ClusteredCacheService(TechnicalLoggerService logger, ReadSessionAccessor sessionAccessor, HazelcastInstance hazelcastInstance) {
        super(logger, hazelcastInstance);
        this.sessionAccessor = sessionAccessor;
    }

    /**
     * @param cacheName
     * @return
     * @throws CacheException
     */
    @Override
    protected String getKeyFromCacheName(final String cacheName) throws CacheException {
        try {
            return String.valueOf(sessionAccessor.getTenantId()) + '_' + cacheName;
        } catch (final TenantIdNotSetException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public List<String> getCachesNames() {
        final Collection<Instance> instances = hazelcastInstance.getInstances();
        final ArrayList<String> cacheNamesList = new ArrayList<String>(instances.size());

        try {
            // Hazelcast Maps are internally named with a prefix
            final String prefix = Prefix.MAP + String.valueOf(sessionAccessor.getTenantId()) + '_';
            for (final Instance instance : instances) {
                if (instance.getInstanceType().isMap() && ((String) instance.getId()).startsWith(prefix)) {
                    cacheNamesList.add(getCacheNameFromKey(((String) instance.getId()).substring(Prefix.MAP.length())));
                }
            }

        } catch (final TenantIdNotSetException e) {
            throw new RuntimeException(e);
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
