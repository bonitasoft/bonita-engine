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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.cache.CacheException;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.TenantIdNotSetException;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Instance;
import com.hazelcast.core.Prefix;

/**
 * Cache service that uses hazelcast
 * 
 * @author Baptiste Mesta
 */
public class ClusteredCacheService implements CacheService {

    private final TechnicalLoggerService logger;

    private final ReadSessionAccessor sessionAccessor;

    private final HazelcastInstance hazelcastInstance;

    public ClusteredCacheService(final TechnicalLoggerService logger, final ReadSessionAccessor sessionAccessor, final HazelcastInstance hazelcastInstance) {
        if (!Manager.getInstance().isFeatureActive(Features.ENGINE_CLUSTERING)) {
            throw new IllegalStateException("The clustering is not an active feature.");
        }
        this.logger = logger;
        this.sessionAccessor = sessionAccessor;
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public void store(final String cacheName, final Serializable key, final Object value) throws CacheException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "store"));
        }

        final String cacheNameKey = getKeyFromCacheName(cacheName);
        try {
            final Map<Serializable, Object> cache = hazelcastInstance.getMap(cacheNameKey);
            cache.put(key, value);

            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "store"));
            }
        } catch (final IllegalStateException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "store", e));
            }

            final StringBuilder stringBuilder = new StringBuilder("The cache '");
            stringBuilder.append(cacheNameKey).append("' is not alive");
            final String msg = stringBuilder.toString();
            throw new CacheException(msg, e);
        }
    }

    @Override
    public Object get(final String cacheName, final Object key) throws CacheException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "get"));
        }

        final String cacheNameKey = getKeyFromCacheName(cacheName);
        try {
            final Map<Serializable, Object> cache = hazelcastInstance.getMap(cacheNameKey);
            final Object value = cache.get(key);

            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "get"));
            }

            return value;
        } catch (final IllegalStateException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "get", e));
            }

            final StringBuilder stringBuilder = new StringBuilder("The cache '");
            stringBuilder.append(cacheNameKey).append("' is not alive");
            final String msg = stringBuilder.toString();
            throw new CacheException(msg, e);
        }
    }

    @Override
    public List<?> getKeys(final String cacheName) throws CacheException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getKeys"));
        }

        final String cacheNameKey = getKeyFromCacheName(cacheName);
        try {
            final Map<Serializable, Object> cache = hazelcastInstance.getMap(cacheNameKey);
            final List<Serializable> keys = new ArrayList<Serializable>(cache.keySet());

            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getKeys"));
            }

            return keys;
        } catch (final IllegalStateException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getKeys", e));
            }

            final StringBuilder stringBuilder = new StringBuilder("The cache '");
            stringBuilder.append(cacheNameKey).append("' is not alive");
            final String msg = stringBuilder.toString();
            throw new CacheException(msg, e);
        }
    }

    @Override
    public boolean remove(final String cacheName, final Object key) throws CacheException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "remove"));
        }

        final String cacheNameKey = getKeyFromCacheName(cacheName);
        try {
            final Map<Serializable, Object> cache = hazelcastInstance.getMap(cacheNameKey);
            final Object valueRemoved = cache.remove(key);

            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "remove"));
            }

            return valueRemoved != null;
        } catch (final IllegalStateException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "remove", e));
            }
            final StringBuilder stringBuilder = new StringBuilder("The cache '");
            stringBuilder.append(cacheNameKey).append("' is not alive");
            final String msg = stringBuilder.toString();
            throw new CacheException(msg, e);
        }
    }

    @Override
    public boolean clear(final String cacheName) throws CacheException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "clear"));
        }

        final String cacheNameKey = getKeyFromCacheName(cacheName);
        try {
            final Map<Serializable, Object> cache = hazelcastInstance.getMap(cacheNameKey);
            cache.clear();

            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "clear"));
            }

            return cache.isEmpty();
        } catch (final IllegalStateException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "clear", e));
            }
            final StringBuilder stringBuilder = new StringBuilder("The cache '");
            stringBuilder.append(cacheNameKey).append("' is not alive");
            final String msg = stringBuilder.toString();
            throw new CacheException(msg, e);
        }
    }

    @Override
    public void clearAll() throws CacheException {
        try {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "clearAll"));
            }
            final List<String> cacheNames = getCachesNames();
            for (final String cacheName : cacheNames) {
                clear(cacheName);
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "clearAll"));
            }
        } catch (final Exception e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "clearAll", e));
            }
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

    @Override
    public int getCacheSize(final String cacheName) throws CacheException {
        final Map<Serializable, Object> cache = hazelcastInstance.getMap(getKeyFromCacheName(cacheName));
        return cache.size();
    }

    private String getKeyFromCacheName(final String cacheName) throws CacheException {
        try {
            return String.valueOf(sessionAccessor.getTenantId()) + '_' + cacheName;
        } catch (final TenantIdNotSetException e) {
            throw new CacheException(e);
        }
    }

    private String getCacheNameFromKey(final String cacheNameKey) {
        return cacheNameKey.substring(cacheNameKey.indexOf('_') + 1);
    }

}
