package com.bonitasoft.engine.cache.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.bonitasoft.engine.cache.CacheException;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.TenantIdNotSetException;

import com.bonitasoft.manager.Manager;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class ClusteredCacheService extends CommonClusteredCacheService implements CacheService {

    private final ReadSessionAccessor sessionAccessor;

    public ClusteredCacheService(final Manager manager, final TechnicalLoggerService logger, final ReadSessionAccessor sessionAccessor,
            final HazelcastInstance hazelcastInstance) {
        super(manager, logger, hazelcastInstance);
        this.sessionAccessor = sessionAccessor;
    }

    public ClusteredCacheService(final TechnicalLoggerService logger, final ReadSessionAccessor sessionAccessor, final HazelcastInstance hazelcastInstance) {
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
        final Collection<DistributedObject> distributedObjects = hazelcastInstance.getDistributedObjects();
        final ArrayList<String> cacheNamesList = new ArrayList<String>(distributedObjects.size());

        try {
            final String prefix = String.valueOf(sessionAccessor.getTenantId()) + '_';
            for (final DistributedObject instance : distributedObjects) {
                if (instance instanceof IMap<?, ?> && instance.getName().startsWith(prefix)) {
                    cacheNamesList.add(getCacheNameFromKey(instance.getName()));
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

    @Override
    public void start() throws SBonitaException {
    }

    @Override
    public void stop() throws SBonitaException, TimeoutException {
    }
}
