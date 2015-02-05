/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
import java.util.Collections;
import java.util.List;

import net.sf.ehcache.CacheManager;

import org.bonitasoft.engine.cache.CacheConfigurations;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.cache.SCacheException;
import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Hongwen Zang
 */
public class EhCacheCacheService extends CommonEhCacheCacheService implements CacheService {

    private final URL configFile;

    public EhCacheCacheService(final TechnicalLoggerService logger, final ReadSessionAccessor sessionAccessor, final CacheConfigurations cacheConfigurations,
            final URL configFile) {
        super(logger, sessionAccessor, cacheConfigurations);
        this.configFile = configFile;
    }

    public EhCacheCacheService(final TechnicalLoggerService logger, final ReadSessionAccessor sessionAccessor, final CacheConfigurations cacheConfigurations) {
        this(logger, sessionAccessor, cacheConfigurations, null);
    }

    @Override
    protected String getKeyFromCacheName(final String cacheName) throws SCacheException {
        try {
            return String.valueOf(sessionAccessor.getTenantId()) + '_' + cacheName;
        } catch (final STenantIdNotSetException e) {
            throw new SCacheException(e);
        }
    }

    @Override
    public List<String> getCachesNames() {
        if (cacheManager == null) {
            return Collections.emptyList();
        }
        final String[] cacheNames = cacheManager.getCacheNames();
        final List<String> cacheNamesList = new ArrayList<String>(cacheNames.length);
        String prefix;
        try {
            prefix = String.valueOf(sessionAccessor.getTenantId()) + '_';
            for (final String cacheName : cacheNames) {
                if (cacheName.startsWith(prefix)) {
                    cacheNamesList.add(getCacheNameFromKey(cacheName));
                }
            }
        } catch (final STenantIdNotSetException e) {
            throw new RuntimeException(e);
        }
        return cacheNamesList;
    }

    private String getCacheNameFromKey(final String cacheNameKey) {
        return cacheNameKey.substring(cacheNameKey.indexOf('_') + 1);
    }

    @Override
    public synchronized void start() {
        cacheManager = configFile != null ? new CacheManager(configFile) : new CacheManager();
    }

    @Override
    public synchronized void stop() {
        if (cacheManager != null) {
            cacheManager.shutdown();
            cacheManager = null;
        }
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
    public void resume() {
        if (cacheManager == null) {
            start();
        }
    }

}
