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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.cache.CacheConfiguration;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.cache.SCacheException;
import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

// Must be started before the work service so it has a "higher" priority
@Order(10)
@Component
@ConditionalOnSingleCandidate(CacheService.class)
public class EhCacheCacheService extends CommonEhCacheCacheService implements CacheService {

    private final long tenantId;

    public EhCacheCacheService(@Qualifier("tenantTechnicalLoggerService") TechnicalLoggerService logger,
            List<CacheConfiguration> cacheConfigurations,
            @Qualifier("defaultTenantCacheConfiguration") CacheConfiguration defaultCacheConfiguration,
            @Value("java.io.tmpdir/tenant.${tenantId}.cache") String diskStorePath,
            @Value("${tenantId}") long tenantId) {
        super(logger, cacheConfigurations, defaultCacheConfiguration, diskStorePath);
        this.tenantId = tenantId;
    }

    @Override
    protected String getKeyFromCacheName(final String cacheName) throws SCacheException {
        return String.valueOf(tenantId) + '_' + cacheName;
    }

    @Override
    protected String getCacheManagerName() {
        return "BONITA_TENANT_" + tenantId;
    }

    @Override
    public List<String> getCachesNames() {
        if (cacheManager == null) {
            return Collections.emptyList();
        }
        final String[] cacheNames = cacheManager.getCacheNames();
        final List<String> cacheNamesList = new ArrayList<>(cacheNames.length);
        String prefix = String.valueOf(tenantId) + '_';
        for (final String cacheName : cacheNames) {
            if (cacheName.startsWith(prefix)) {
                cacheNamesList.add(getCacheNameFromKey(cacheName));
            }
        }
        return cacheNamesList;
    }

    private String getCacheNameFromKey(final String cacheNameKey) {
        return cacheNameKey.substring(cacheNameKey.indexOf('_') + 1);
    }

    @Override
    public synchronized void start() throws SCacheException {
        buildCacheManagerWithDefaultConfiguration();
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

    @Override
    protected String getCacheManagerIdentifier() {
        return "tenant " + tenantId;
    }
}
