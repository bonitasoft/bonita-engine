/**
 * Copyright (C) 2013 BonitaSoft S.A.
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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.cache.CacheConfiguration;
import org.bonitasoft.engine.cache.CacheException;
import org.bonitasoft.engine.cache.PlatformCacheService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;

/**
 * Platform version of the EhCacheCacheService
 * 
 * @author Baptiste Mesta
 */
public class PlatformEhCacheCacheService extends
        CommonEhCacheCacheService implements PlatformCacheService {

    public PlatformEhCacheCacheService(TechnicalLoggerService logger, ReadSessionAccessor sessionAccessor, Map<String, CacheConfiguration> cacheConfigurations,
            URL configFile) {
        super(logger, sessionAccessor, cacheConfigurations, configFile);
    }

    public PlatformEhCacheCacheService(TechnicalLoggerService logger, ReadSessionAccessor sessionAccessor, Map<String, CacheConfiguration> cacheConfigurations) {
        super(logger, sessionAccessor, cacheConfigurations);
    }

    @Override
    protected String getKeyFromCacheName(final String cacheName) throws CacheException {
        return "P_" + cacheName;
    }

    @Override
    public List<String> getCachesNames() {
        final String[] cacheNames = cacheManager.getCacheNames();
        final ArrayList<String> cacheNamesList = new ArrayList<String>(cacheNames.length);
        String prefix;
        prefix = "P_";
        for (final String cacheName : cacheNames) {
            if (cacheName.startsWith(prefix)) {
                cacheNamesList.add(getCacheNameFromKey(cacheName));
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
