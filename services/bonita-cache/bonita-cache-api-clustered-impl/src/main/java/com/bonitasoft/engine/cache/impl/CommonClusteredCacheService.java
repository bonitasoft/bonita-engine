/*******************************************************************************
 * Copyright (C) 2013, 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.cache.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.cache.CommonCacheService;
import org.bonitasoft.engine.cache.SCacheException;
import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;
import com.hazelcast.core.HazelcastInstance;

/**
 * Cache service that uses hazelcast
 *
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public abstract class CommonClusteredCacheService implements CommonCacheService {

    private final TechnicalLoggerService logger;

    protected final HazelcastInstance hazelcastInstance;

    public CommonClusteredCacheService(final TechnicalLoggerService logger, final HazelcastInstance hazelcastInstance) {
        this(Manager.getInstance(), logger, hazelcastInstance);
    }

    public CommonClusteredCacheService(final Manager manager, final TechnicalLoggerService logger, final HazelcastInstance hazelcastInstance) {
        if (!manager.isFeatureActive(Features.ENGINE_CLUSTERING)) {
            throw new IllegalStateException("The clustering is not an active feature.");
        }
        this.logger = logger;
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public void store(final String cacheName, final Serializable key, final Object value) throws SCacheException {
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
            throw new SCacheException(msg, e);
        }
    }

    @Override
    public Object get(final String cacheName, final Object key) throws SCacheException {
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
            throw new SCacheException(msg, e);
        }
    }

    @Override
    public List<Object> getKeys(final String cacheName) throws SCacheException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getKeys"));
        }

        final String cacheNameKey = getKeyFromCacheName(cacheName);
        try {
            final Map<Serializable, Object> cache = hazelcastInstance.getMap(cacheNameKey);
            final List<Object> keys = new ArrayList<Object>(cache.keySet());

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
            throw new SCacheException(msg, e);
        }
    }

    @Override
    public boolean remove(final String cacheName, final Object key) throws SCacheException {
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
            throw new SCacheException(msg, e);
        }
    }

    @Override
    public boolean clear(final String cacheName) throws SCacheException {
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
            throw new SCacheException(msg, e);
        }
    }

    @Override
    public void clearAll() throws SCacheException {
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
            throw new SCacheException(e);
        }

    }

    @Override
    public int getCacheSize(final String cacheName) throws SCacheException {
        final Map<Serializable, Object> cache = hazelcastInstance.getMap(getKeyFromCacheName(cacheName));
        return cache.size();
    }

    protected abstract String getKeyFromCacheName(final String cacheName) throws SCacheException;

}
