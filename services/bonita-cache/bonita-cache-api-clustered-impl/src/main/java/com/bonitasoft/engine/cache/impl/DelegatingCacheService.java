/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.cache.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.bonitasoft.engine.cache.CacheException;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * Delegate cache request to local or distributed cache services
 * if the cache name is contains in the list localOnlyCaches the call is delegated to local cache, else it is delegated to the distributed cache
 * 
 * @author Baptiste Mesta
 */
public class DelegatingCacheService implements CacheService {

    private final CacheService distributedCache;

    private final CacheService localCache;

    private final HashSet<String> localOnlyCaches;

    public DelegatingCacheService(final CacheService localCache, final CacheService distributedCache, final Set<String> localOnlyCaches) {
        this.localOnlyCaches = new HashSet<String>(localOnlyCaches);
        this.localCache = localCache;
        this.distributedCache = distributedCache;
    }

    @Override
    public void store(final String cacheName, final Serializable key, final Object value) throws CacheException {
        getCacheServiceFor(cacheName).store(cacheName, key, value);
    }

    private CacheService getCacheServiceFor(final String cacheName) {
        if (localOnlyCaches.contains(cacheName)) {
            return localCache;
        } else {
            return distributedCache;
        }
    }

    @Override
    public boolean remove(final String cacheName, final Object key) throws CacheException {
        return getCacheServiceFor(cacheName).remove(cacheName, key);
    }

    @Override
    public Object get(final String cacheName, final Object key) throws CacheException {
        return getCacheServiceFor(cacheName).get(cacheName, key);
    }

    @Override
    public List<Object> getKeys(final String cacheName) throws CacheException {
        return getCacheServiceFor(cacheName).getKeys(cacheName);
    }

    @Override
    public boolean clear(final String cacheName) throws CacheException {
        return getCacheServiceFor(cacheName).clear(cacheName);
    }

    @Override
    public void clearAll() throws CacheException {
        localCache.clearAll();
        distributedCache.clearAll();
    }

    @Override
    public int getCacheSize(final String cacheName) throws CacheException {
        return getCacheServiceFor(cacheName).getCacheSize(cacheName);
    }

    @Override
    public List<String> getCachesNames() {
        final List<String> localCachesNames = localCache.getCachesNames();
        final List<String> distributedCachesNames = distributedCache.getCachesNames();
        final ArrayList<String> cachesNames = new ArrayList<String>(localCachesNames.size() + distributedCachesNames.size());
        cachesNames.addAll(localCachesNames);
        cachesNames.addAll(distributedCachesNames);
        Collections.sort(cachesNames);
        return cachesNames;
    }

    @Override
    public void start() throws SBonitaException {
        // do nothing it's done on on each instance using the NodeConfiguration that have all ServiceWithLifeCycle autowired using spring
    }

    @Override
    public void stop() throws SBonitaException, TimeoutException {
        // do nothing it's done on on each instance using the NodeConfiguration that have all ServiceWithLifeCycle autowired using spring
    }

    @Override
    public void pause() throws SBonitaException, TimeoutException {
        // nothing to do
    }

    @Override
    public void resume() throws SBonitaException {
        // nothing to do
    }

}
