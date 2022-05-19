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
package org.bonitasoft.engine.platform.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.cache.SCacheException;
import org.bonitasoft.engine.commons.CollectionUtil;
import org.bonitasoft.engine.log.technical.TechnicalLogger;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.platform.PlatformRetriever;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.cache.PlatformCacheService;
import org.bonitasoft.engine.platform.exception.SPlatformNotFoundException;
import org.bonitasoft.engine.platform.exception.STenantActivationException;
import org.bonitasoft.engine.platform.exception.STenantDeactivationException;
import org.bonitasoft.engine.platform.exception.STenantException;
import org.bonitasoft.engine.platform.exception.STenantNotFoundException;
import org.bonitasoft.engine.platform.exception.STenantUpdateException;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.SPlatformProperties;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.model.builder.STenantUpdateBuilderFactory;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.services.UpdateDescriptor;

/**
 * @author Charles Souillard
 * @author Celine Souchet
 */
public class PlatformServiceImpl implements PlatformService {

    private static final String TENANT = "TENANT";
    private static final String QUERY_GET_TENANT_BY_NAME = "getTenantByName";
    private static final String LOG_GET_TENANTS = "getTenants";
    private static final String QUERY_GET_DEFAULT_TENANT = "getDefaultTenant";
    private static final String QUERY_GET_NUMBER_OF_TENANTS = "getNumberOfTenants";
    private static final String QUERY_GET_TENANTS_BY_IDS = "getTenantsByIds";
    private static final String CACHE_KEY = "PLATFORM";

    private final PersistenceService platformPersistenceService;
    private final TechnicalLogger logger;
    private final PlatformCacheService platformCacheService;
    private final SPlatformProperties sPlatformProperties;
    private final Recorder recorder;
    private final PlatformRetriever platformRetriever;

    public PlatformServiceImpl(final PersistenceService platformPersistenceService, PlatformRetriever platformRetriever,
            final Recorder recorder,
            final TechnicalLoggerService logger, PlatformCacheService platformCacheService,
            final SPlatformProperties sPlatformProperties) {
        this.platformPersistenceService = platformPersistenceService;
        this.logger = logger.asLogger(PlatformServiceImpl.class);
        this.platformCacheService = platformCacheService;
        this.sPlatformProperties = sPlatformProperties;
        this.recorder = recorder;
        this.platformRetriever = platformRetriever;

    }

    @Override
    public SPlatform getPlatform() throws SPlatformNotFoundException {
        try {
            SPlatform sPlatform = (SPlatform) platformCacheService.get(CACHE_KEY, CACHE_KEY);
            if (sPlatform == null) {
                // try to read it from database
                sPlatform = readPlatform();
                cachePlatform(sPlatform);
            }
            return sPlatform;
        } catch (final SCacheException e) {
            throw new SPlatformNotFoundException("Platform not present in cache.", e);
        }
    }

    /*
     * *************************
     * The Platform is stored 1 time for each cache
     * We do this because the cache service handle
     * multi tenancy
     * *************************
     */
    private void cachePlatform(final SPlatform platform) {
        try {
            platformCacheService.store(CACHE_KEY, CACHE_KEY, platform);
        } catch (final SCacheException e) {
            logger.warn("Can't cache the platform, maybe the platform cache service is not started yet: {}",
                    e.getMessage());
        }
    }

    private SPlatform readPlatform() throws SPlatformNotFoundException {
        try {
            return platformRetriever.getPlatform();
        } catch (SPlatformNotFoundException e) {
            throw e;
        } catch (final Exception e) {
            throw new SPlatformNotFoundException("Unable to check if a platform already exists : " + e.getMessage(), e);
        }
    }

    @Override
    public STenant getTenant(final long id) throws STenantNotFoundException {
        STenant tenant;
        try {
            tenant = platformPersistenceService.selectById(new SelectByIdDescriptor<>(STenant.class, id));
            if (tenant == null) {
                throw new STenantNotFoundException("No tenant found with id: " + id);
            }
        } catch (final Exception e) {
            throw new STenantNotFoundException("Unable to get the tenant : " + e.getMessage(), e);
        }
        return tenant;
    }

    @Override
    public boolean isPlatformCreated() {
        try {
            getPlatform();
            return true;
        } catch (final SPlatformNotFoundException e) {
            return false;
        }
    }

    @Override
    public STenant getTenantByName(final String name) throws STenantNotFoundException {
        STenant tenant;
        try {
            final Map<String, Object> parameters = CollectionUtil.buildSimpleMap(STenant.NAME, name);
            tenant = platformPersistenceService
                    .selectOne(new SelectOneDescriptor<>(QUERY_GET_TENANT_BY_NAME, parameters, STenant.class));
            if (tenant == null) {
                throw new STenantNotFoundException("No tenant found with name: " + name);
            }
        } catch (final SBonitaReadException e) {
            throw new STenantNotFoundException("Unable to check if a tenant already exists: " + e.getMessage(), e);
        }
        return tenant;
    }

    @Override
    public STenant getDefaultTenant() throws STenantNotFoundException {
        try {
            STenant tenant = platformPersistenceService
                    .selectOne(new SelectOneDescriptor<>(QUERY_GET_DEFAULT_TENANT, null, STenant.class));
            if (tenant == null) {
                throw new STenantNotFoundException("No default tenant found");
            }
            return tenant;
        } catch (final SBonitaReadException e) {
            throw new STenantNotFoundException("Unable to check if a default tenant already exists: " + e.getMessage(),
                    e);
        }
    }

    @Override
    public boolean isDefaultTenantCreated() throws SBonitaReadException {
        return platformPersistenceService
                .selectOne(new SelectOneDescriptor<STenant>(QUERY_GET_DEFAULT_TENANT, null, STenant.class)) != null;
    }

    @Override
    public List<STenant> getTenants(final Collection<Long> ids, final QueryOptions queryOptions)
            throws STenantNotFoundException, STenantException {
        List<STenant> tenants;
        try {
            final Map<String, Object> parameters = CollectionUtil.buildSimpleMap("ids", ids);
            tenants = platformPersistenceService
                    .selectList(new SelectListDescriptor<>(QUERY_GET_TENANTS_BY_IDS, parameters, STenant.class,
                            queryOptions));
            if (tenants.size() != ids.size()) {
                throw new STenantNotFoundException(
                        "Unable to retrieve all tenants by ids. Expected: " + ids + ", retrieved: " + tenants);
            }
        } catch (final SBonitaReadException e) {
            throw new STenantException("Problem getting list of tenants: " + e.getMessage(), e);
        }
        return tenants;
    }

    @Override
    public void updateTenant(final STenant tenant, final EntityUpdateDescriptor descriptor)
            throws STenantUpdateException {
        final String tenantName = (String) descriptor.getFields().get(STenantUpdateBuilderFactory.NAME);
        if (tenantName != null) {
            try {
                if (getTenantByName(tenantName).getId() != tenant.getId()) {
                    throw new STenantUpdateException(
                            "Unable to update the tenant with new name " + tenantName + " : it already exists.");
                }
            } catch (final STenantNotFoundException e) {
                // Ok
            }
        }

        try {
            recorder.recordUpdate(UpdateRecord.buildSetFields(tenant, descriptor), TENANT);
        } catch (final SRecorderException e) {
            throw new STenantUpdateException("Problem while updating tenant: " + tenant, e);
        }
    }

    @Override
    public void activateTenant(final long tenantId) throws STenantNotFoundException, STenantActivationException {
        final STenant tenant = getTenant(tenantId);
        final UpdateDescriptor desc = new UpdateDescriptor(tenant);
        desc.addField(STenant.STATUS, STenant.ACTIVATED);
        try {
            platformPersistenceService.update(desc);
        } catch (final SPersistenceException e) {
            throw new STenantActivationException("Problem while activating tenant: " + tenant, e);
        }
    }

    @Override
    public void deactivateTenant(final long tenantId) throws STenantNotFoundException, STenantDeactivationException {
        final STenant tenant = getTenant(tenantId);
        final UpdateDescriptor desc = new UpdateDescriptor(tenant);
        desc.addField(STenant.STATUS, STenant.DEACTIVATED);
        try {
            platformPersistenceService.update(desc);
        } catch (final SPersistenceException e) {
            throw new STenantDeactivationException("Problem while deactivating tenant: " + tenant, e);
        }
    }

    @Override
    public void pauseTenant(long tenantId) throws STenantUpdateException, STenantNotFoundException {
        final STenant tenant = getTenant(tenantId);
        final UpdateDescriptor desc = new UpdateDescriptor(tenant);
        desc.addField(STenant.STATUS, STenant.PAUSED);
        try {
            platformPersistenceService.update(desc);
        } catch (final SPersistenceException e) {
            throw new STenantUpdateException("Unable to update tenant status in database.", e);
        }
    }

    @Override
    public List<STenant> getTenants(final QueryOptions queryOptions) throws STenantException {
        List<STenant> tenants;
        try {
            tenants = platformPersistenceService
                    .selectList(new SelectListDescriptor<>(LOG_GET_TENANTS, null, STenant.class, queryOptions));
        } catch (final SBonitaReadException e) {
            throw new STenantException("Problem getting list of tenants : " + e.getMessage(), e);
        }
        return tenants;
    }

    @Override
    public int getNumberOfTenants() throws STenantException {
        final Map<String, Object> emptyMap = Collections.emptyMap();
        try {
            final Long read = platformPersistenceService
                    .selectOne(new SelectOneDescriptor<>(QUERY_GET_NUMBER_OF_TENANTS, emptyMap, STenant.class,
                            Long.class));
            return read.intValue();
        } catch (final SBonitaReadException e) {
            throw new STenantException(e);
        }
    }

    @Override
    public List<STenant> searchTenants(final QueryOptions options) throws SBonitaReadException {
        return platformPersistenceService.searchEntity(STenant.class, options, null);
    }

    @Override
    public long getNumberOfTenants(final QueryOptions options) throws SBonitaReadException {
        return platformPersistenceService.getNumberOfEntities(STenant.class, options, null);
    }

    @Override
    public SPlatformProperties getSPlatformProperties() {
        return sPlatformProperties;
    }

}
