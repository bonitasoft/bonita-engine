/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.DiskStoreConfiguration;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.cache.SCacheException;
import org.bonitasoft.engine.commons.PlatformLifecycleService;
import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Matthieu Chaffotte
 */
// Must be started before the work service so it has a "higher" priority
@Order(2)
@Component
@ConditionalOnSingleCandidate(CacheService.class)
@Slf4j
public class EhCacheCacheService implements CacheService, PlatformLifecycleService {

    protected CacheManager cacheManager;

    protected final Map<String, CacheConfiguration> cacheConfigurations;

    private final CacheConfiguration defaultCacheConfiguration;

    private final String diskStorePath;

    public EhCacheCacheService(List<org.bonitasoft.engine.cache.CacheConfiguration> cacheConfigurations,
            @Qualifier("defaultCacheConfiguration") org.bonitasoft.engine.cache.CacheConfiguration defaultCacheConfiguration,
            @Value("java.io.tmpdir/platform.cache") String diskStorePath) {
        this.diskStorePath = diskStorePath;
        this.defaultCacheConfiguration = getEhCacheConfiguration(defaultCacheConfiguration);
        if (cacheConfigurations != null && cacheConfigurations.size() > 0) {
            this.cacheConfigurations = new HashMap<>(cacheConfigurations.size());
            for (final org.bonitasoft.engine.cache.CacheConfiguration cacheConfig : cacheConfigurations) {
                this.cacheConfigurations.put(cacheConfig.getName(), getEhCacheConfiguration(cacheConfig));
            }
        } else {
            this.cacheConfigurations = Collections.emptyMap();
        }
    }

    // VisibleForTesting
    protected Set<String> getCacheConfigurationNames() {
        return cacheConfigurations.keySet();
    }

    protected CacheConfiguration getEhCacheConfiguration(
            final org.bonitasoft.engine.cache.CacheConfiguration cacheConfig) {
        final CacheConfiguration ehCacheConfig = new CacheConfiguration();
        ehCacheConfig.setMaxElementsInMemory(cacheConfig.getMaxElementsInMemory());
        ehCacheConfig.setMaxElementsOnDisk(cacheConfig.getMaxElementsOnDisk());
        ehCacheConfig.setOverflowToDisk(!cacheConfig.isInMemoryOnly());
        ehCacheConfig.setEternal(cacheConfig.isEternal());
        ehCacheConfig.setCopyOnRead(cacheConfig.isCopyOnRead());
        ehCacheConfig.setCopyOnWrite(cacheConfig.isCopyOnWrite());
        if (!cacheConfig.isEternal()) {
            ehCacheConfig.setTimeToLiveSeconds(cacheConfig.getTimeToLiveSeconds());
        }
        return ehCacheConfig;
    }

    @Override
    public List<String> getCachesNames() {
        return List.of(cacheManager.getCacheNames());
    }

    @Override
    public synchronized void stop() {
        shutdownCacheManager();
    }

    @Override
    public void pause() {
        try {
            clearAll();
        } catch (final SCacheException sce) {
            throw new SBonitaRuntimeException(sce);
        }
    }

    @Override
    public void resume() throws SCacheException {
        if (cacheManager == null) {
            start();
        }
    }

    protected synchronized Cache createCache(final String cacheName)
            throws SCacheException {
        if (cacheManager == null) {
            throw new SCacheException("The cache is not started, call start() on the cache service");
        }
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            final CacheConfiguration cacheConfiguration = cacheConfigurations.get(cacheName);
            final CacheConfiguration newCacheConfig;
            if (cacheConfiguration != null) {
                newCacheConfig = cacheConfiguration.clone();
            } else {
                log.warn("No specific cache configuration found for cache '{}'. Using default configuration",
                        cacheName);
                newCacheConfig = defaultCacheConfiguration.clone();
            }
            newCacheConfig.setName(cacheName);
            cache = new Cache(newCacheConfig);
            cacheManager.addCache(cache);
        }
        return cache;
    }

    @Override
    public void store(final String cacheName, final Serializable key, final Object value) throws SCacheException {
        if (cacheManager == null) {
            throw new SCacheException("The cache is not started, call start() on the cache service");
        }
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                cache = createCache(cacheName);
            }
            if (value instanceof Serializable) {
                cache.put(new Element(key, (Serializable) value));
            } else {
                cache.put(new Element(key, value));
            }
        } catch (final IllegalStateException e) {
            throw new SCacheException("The cache '" + cacheName + "' is not alive", e);
        } catch (final net.sf.ehcache.CacheException ce) {
            throw new SCacheException(ce);
        }
    }

    @Override
    public Object get(final String cacheName, final Object key) throws SCacheException {
        if (cacheManager == null) {
            return null;
        }
        try {
            final Cache cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                // the cache does not exist = the key was not stored
                return null;
            }
            final Element element = cache.get(key);
            if (element != null) {
                return element.getObjectValue();
            }
            return null;
        } catch (final IllegalStateException e) {
            throw new SCacheException("The cache '" + cacheName + "' is not alive", e);
        } catch (final Exception e) {
            throw new SCacheException("The cache '" + cacheName + "' does not exist", e);
        }
    }

    @Override
    public boolean clear(final String cacheName) throws SCacheException {
        if (cacheManager == null) {
            return true;
        }
        try {
            final Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.removeAll();
            }
            return cache == null;
        } catch (final IllegalStateException e) {
            throw new SCacheException("The cache '" + cacheName + "' is not alive", e);
        } catch (final Exception e) {
            throw new SCacheException("The cache '" + cacheName + "' does not exist", e);
        }
    }

    @Override
    public int getCacheSize(final String cacheName) throws SCacheException {
        if (cacheManager == null) {
            return 0;
        }
        final Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return 0;
        }
        return cache.getSize();
    }

    @Override
    public void clearAll() throws SCacheException {
        if (cacheManager == null) {
            return;
        }
        try {
            final List<String> cacheNames = getCachesNames();
            for (final String cacheName : cacheNames) {
                clear(cacheName);
            }
        } catch (final net.sf.ehcache.CacheException e) {
            throw new SCacheException(e);
        }
    }

    @Override
    public boolean remove(final String cacheName, final Object key) throws SCacheException {
        if (cacheManager == null) {
            return false;
        }
        final Cache cache = cacheManager.getCache(cacheName);
        // key was not removed
        return cache != null && cache.remove(key);
    }

    @Override
    public List<Object> getKeys(final String cacheName) throws SCacheException {
        if (cacheManager == null) {
            return Collections.emptyList();
        }
        try {
            final Cache cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                return Collections.emptyList();
            }
            return cache.getKeys();
        } catch (final IllegalStateException e) {
            throw new SCacheException("The cache '" + cacheName + "' is not alive", e);
        } catch (final net.sf.ehcache.CacheException e) {
            throw new SCacheException(e);
        }
    }

    protected void shutdownCacheManager() {
        if (cacheManager != null) {
            cacheManager.shutdown();
            cacheManager = null;
        }
    }

    @Override
    public boolean isStopped() {
        return cacheManager == null;
    }

    protected String getCacheManagerName() {
        return "BONITA";
    }

    @Override
    public synchronized void start() throws SCacheException {
        if (cacheManager == null) {
            final Configuration configuration = new Configuration();
            configuration.setName(getCacheManagerName());
            configuration.setDefaultCacheConfiguration(defaultCacheConfiguration);
            configuration.diskStore(new DiskStoreConfiguration().path(diskStorePath));
            cacheManager = new CacheManager(configuration);
        }
    }
}
