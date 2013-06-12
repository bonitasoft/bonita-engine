/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.cache.ehcache;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

import org.bonitasoft.engine.cache.CacheException;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.TenantIdNotSetException;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Hongwen Zang
 */
public class EhCacheCacheService implements CacheService {

    private final CacheManager cacheManager;

    private final TechnicalLoggerService logger;

    private final ReadSessionAccessor sessionAccessor;

    private final Map<String, CacheConfiguration> cacheConfigurations;

    public EhCacheCacheService(final TechnicalLoggerService logger, final ReadSessionAccessor sessionAccessor,
            final Map<String, org.bonitasoft.engine.cache.CacheConfiguration> cacheConfigurations) {
        this.logger = logger;
        this.sessionAccessor = sessionAccessor;
        this.cacheConfigurations = new HashMap<String, CacheConfiguration>(cacheConfigurations.size());
        for (final Entry<String, org.bonitasoft.engine.cache.CacheConfiguration> cacheConfig : cacheConfigurations.entrySet()) {
            this.cacheConfigurations.put(cacheConfig.getKey(), getEhCacheConfiguration(cacheConfig.getValue()));

        }
        cacheManager = CacheManager.create();
    }

    public EhCacheCacheService(final TechnicalLoggerService logger, final ReadSessionAccessor sessionAccessor,
            final Map<String, org.bonitasoft.engine.cache.CacheConfiguration> cacheConfigurations, final URL configFile) {
        this.logger = logger;
        this.sessionAccessor = sessionAccessor;
        this.cacheConfigurations = new HashMap<String, CacheConfiguration>(cacheConfigurations.size());
        for (final Entry<String, org.bonitasoft.engine.cache.CacheConfiguration> cacheConfig : cacheConfigurations.entrySet()) {
            this.cacheConfigurations.put(cacheConfig.getKey(), getEhCacheConfiguration(cacheConfig.getValue()));
        }
        cacheManager = CacheManager.create(configFile);
    }

    private CacheConfiguration getEhCacheConfiguration(final org.bonitasoft.engine.cache.CacheConfiguration cacheConfig) {
        final CacheConfiguration ehCacheConfig = new CacheConfiguration();
        ehCacheConfig.setMaxElementsInMemory(cacheConfig.getMaxElementsInMemory());
        ehCacheConfig.setMaxElementsOnDisk(cacheConfig.getMaxElementsOnDisk());
        ehCacheConfig.setOverflowToDisk(!cacheConfig.isInMemoryOnly());
        ehCacheConfig.setTimeToLiveSeconds(cacheConfig.getTimeToLiveSeconds());
        ehCacheConfig.setEternal(cacheConfig.isEternal());
        return ehCacheConfig;
    }

    protected synchronized Cache createCache(final String cacheName, final String internalCacheName) {
        Cache cache = cacheManager.getCache(internalCacheName);
        if (cache == null) {
            final CacheConfiguration cacheConfiguration = cacheConfigurations.get(cacheName);
            if (cacheConfiguration != null) {
                final CacheConfiguration newCacheConfig = cacheConfiguration.clone();
                newCacheConfig.setName(internalCacheName);
                cache = new Cache(newCacheConfig);
                cacheManager.addCache(cache);
            } else {
                cacheManager.addCache(internalCacheName);
                cache = cacheManager.getCache(internalCacheName);
            }
        }
        return cache;
    }

    @Override
    public void store(final String cacheName, final Serializable key, final Object value) throws CacheException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "store"));
        }
        final String cacheNameKey = getKeyFromCacheName(cacheName);
        try {
            Ehcache cache = cacheManager.getEhcache(cacheNameKey);
            if (cache == null) {
                cache = createCache(cacheName, cacheNameKey);
            }
            if (value instanceof Serializable) {
                cache.put(new Element(key, (Serializable) value));
            } else {
                cache.put(new Element(key, value));
            }
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
        } catch (final ClassCastException e) {
            // obscure exception throw by ehcache
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "store", e));
            }
            throw new CacheException(e);
        } catch (final net.sf.ehcache.CacheException ce) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "store", ce));
            }
            throw new CacheException(ce);
        }
    }

    /**
     * @param cacheName
     * @return
     * @throws CacheException
     */
    private String getKeyFromCacheName(final String cacheName) throws CacheException {
        try {
            return String.valueOf(sessionAccessor.getTenantId()) + '_' + cacheName;
        } catch (final TenantIdNotSetException e) {
            throw new CacheException(e);
        }
    }

    /**
     * @param cacheName
     * @return
     */
    private String getCacheNameFromKey(final String cacheNameKey) {
        return cacheNameKey.substring(cacheNameKey.indexOf('_') + 1);
    }

    @Override
    public Object get(final String cacheName, final Object key) throws CacheException {
        final String cacheNameKey = getKeyFromCacheName(cacheName);
        try {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "get"));
            }
            final Cache cache = cacheManager.getCache(cacheNameKey);
            if (cache == null) {
                // the cache does not exists = the key was not stored
                return null;
            }
            final Element element = cache.get(key);
            if (element != null) {
                if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                    logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "get"));
                }
                return element.getObjectValue();
            } else {
                if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                    logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "get"));
                }
                return null;
            }
        } catch (final IllegalStateException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "get", e));
            }
            final StringBuilder stringBuilder = new StringBuilder("The cache '");
            stringBuilder.append(cacheNameKey).append("' is not alive");
            final String msg = stringBuilder.toString();
            throw new CacheException(msg, e);
        } catch (final Exception e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "get", e));
            }
            final StringBuilder stringBuilder = new StringBuilder("The cache '");
            stringBuilder.append(cacheNameKey).append("' does not exists");
            final String msg = stringBuilder.toString();
            throw new CacheException(msg, e);
        }
    }

    @Override
    public boolean clear(final String cacheName) throws CacheException {
        final String cacheNameKey = getKeyFromCacheName(cacheName);
        try {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "clear"));
            }
            final Cache cache = cacheManager.getCache(cacheNameKey);
            if (cache != null) {
                cache.removeAll();
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "clear"));
            }
            return cache == null;
        } catch (final IllegalStateException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "clear", e));
            }
            final StringBuilder stringBuilder = new StringBuilder("The cache '");
            stringBuilder.append(cacheNameKey).append("' is not alive");
            final String msg = stringBuilder.toString();
            throw new CacheException(msg, e);
        } catch (final Exception e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "clear", e));
            }
            final StringBuilder stringBuilder = new StringBuilder("The cache '");
            stringBuilder.append(cacheNameKey).append("' does not exists");
            final String msg = stringBuilder.toString();
            throw new CacheException(msg, e);
        }
    }

    @Override
    public int getCacheSize(final String cacheName) throws CacheException {
        final Cache cache = cacheManager.getCache(getKeyFromCacheName(cacheName));
        if (cache == null) {
            return 0;
        }
        return cache.getSize();
    }

    @Override
    public List<String> getCachesNames() {
        final String[] cacheNames = cacheManager.getCacheNames();
        final ArrayList<String> cacheNamesList = new ArrayList<String>(cacheNames.length);
        String prefix;
        try {
            prefix = String.valueOf(sessionAccessor.getTenantId()) + '_';
            for (final String cacheName : cacheNames) {
                if (cacheName.startsWith(prefix)) {
                    cacheNamesList.add(getCacheNameFromKey(cacheName));
                }
            }
        } catch (final TenantIdNotSetException e) {
            throw new RuntimeException(e);
        }
        return cacheNamesList;
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
        } catch (final net.sf.ehcache.CacheException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "clearAll", e));
            }
            throw new CacheException(e);
        }
    }

    @Override
    public boolean remove(final String cacheName, final Object key) throws CacheException {
        final String cacheNameKey = getKeyFromCacheName(cacheName);
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "remove"));
        }
        final Cache cache = cacheManager.getCache(cacheNameKey);
        if (cache == null) {
            // key was not removed
            return false;
        }
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "remove"));
        }
        return cache.remove(key);
    }

    @Override
    public List<?> getKeys(final String cacheName) throws CacheException {
        final String cacheNameKey = getKeyFromCacheName(cacheName);
        try {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getKeys"));
            }
            final Cache cache = cacheManager.getCache(cacheNameKey);
            if (cache == null) {
                final StringBuilder message = new StringBuilder("The cache '");
                message.append(cacheNameKey).append("' does not exists");
                throw new CacheException(message.toString());
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getKeys"));
            }
            return cache.getKeys();
        } catch (final IllegalStateException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getKeys", e));
            }
            final StringBuilder stringBuilder = new StringBuilder("The cache '");
            stringBuilder.append(cacheNameKey).append("' is not alive");
            final String msg = stringBuilder.toString();
            throw new CacheException(msg, e);
        } catch (final net.sf.ehcache.CacheException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getKeys", e));
            }
            throw new CacheException(e);
        }
    }

}
