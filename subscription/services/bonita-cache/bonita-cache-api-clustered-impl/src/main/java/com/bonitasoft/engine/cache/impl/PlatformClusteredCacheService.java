/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.cache.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bonitasoft.engine.cache.PlatformCacheService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * @author Matthieu Chaffotte
 */
public class PlatformClusteredCacheService extends CommonClusteredCacheService implements PlatformCacheService {

    public PlatformClusteredCacheService(final TechnicalLoggerService logger, final HazelcastInstance hazelcastInstance) {
        super(logger, hazelcastInstance);
    }

    @Override
    protected String getKeyFromCacheName(final String cacheName) {
        return "P_" + cacheName;
    }

    @Override
    public List<String> getCachesNames() {
        final Collection<DistributedObject> distributedObjects = hazelcastInstance.getDistributedObjects();
        final List<String> cacheNamesList = new ArrayList<String>(distributedObjects.size());

        final String prefix = "P_";
        for (final DistributedObject instance : distributedObjects) {
            if (instance instanceof IMap<?, ?> && instance.getName().startsWith(prefix)) {
                cacheNamesList.add(getCacheNameFromKey(instance.getName()));
            }
        }

        return cacheNamesList;
    }

    private String getCacheNameFromKey(final String cacheNameKey) {
        return cacheNameKey.substring(cacheNameKey.indexOf('_') + 1);
    }

    @Override
    public void start() {
        // do nothing, we start/stop hazelcast, not this service
    }

    @Override
    public void stop() {
        // do nothing, we start/stop hazelcast, not this service
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
