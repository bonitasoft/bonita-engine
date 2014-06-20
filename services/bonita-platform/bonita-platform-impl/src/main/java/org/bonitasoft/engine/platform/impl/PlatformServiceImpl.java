/**
 * Copyright (C) 2011, 2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.platform.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.cache.PlatformCacheService;
import org.bonitasoft.engine.cache.SCacheException;
import org.bonitasoft.engine.commons.CollectionUtil;
import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.SDeletingActivatedTenantException;
import org.bonitasoft.engine.platform.SPlatformCreationException;
import org.bonitasoft.engine.platform.SPlatformDeletionException;
import org.bonitasoft.engine.platform.SPlatformNotFoundException;
import org.bonitasoft.engine.platform.SPlatformUpdateException;
import org.bonitasoft.engine.platform.STenantActivationException;
import org.bonitasoft.engine.platform.STenantAlreadyExistException;
import org.bonitasoft.engine.platform.STenantCreationException;
import org.bonitasoft.engine.platform.STenantDeactivationException;
import org.bonitasoft.engine.platform.STenantDeletionException;
import org.bonitasoft.engine.platform.STenantException;
import org.bonitasoft.engine.platform.STenantNotFoundException;
import org.bonitasoft.engine.platform.STenantUpdateException;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.SPlatformProperties;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.model.builder.STenantBuilderFactory;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.services.TenantPersistenceService;
import org.bonitasoft.engine.services.UpdateDescriptor;

/**
 * @author Charles Souillard
 * @author Celine Souchet
 */
public class PlatformServiceImpl implements PlatformService {

    private static final String LOG_ACTIVATE_TENANT = "activateTenant";

    private static final String LOG_DELETE_TENANT_OBJECTS = "deleteTenantObjects";

    private static final String LOG_DEACTIVE_TENANT = "deactiveTenant";

    private static final String LOG_UPDATE_TENANT = "updateTenant";

    private static final String LOG_UPDATE_PLATFORM = "updatePlatform";

    private static final String LOG_GET_TENANTS = "getTenants";

    private static final String LOG_GET_PLATFORM = "getPlatform";

    private static final String LOG_DELETE_TENANT_TABLES = "deleteTenantTables";

    private static final String LOG_DELETE_TENANT = "deleteTenant";

    private static final String LOG_DELETE_PLATFORM_TABLES = "deletePlatformTables";

    private static final String LOG_DELETE_PLATFORM = "deletePlatform";

    private static final String LOG_CREATE_TENANT_TABLES = "createTenantTables";

    private static final String LOG_CREATE_TENANT = "createTenant";

    private static final String LOG_CREATE_PLATFORM = "createPlatform";

    private static final String LOG_CREATE_PLATFORM_TABLES = "createPlatformTables";

    private static final String LOG_IS_PLATFORM_CREATED = "isPlatformCreated";

    private static final String QUERY_GET_DEFAULT_TENANT = "getDefaultTenant";

    private static final String QUERY_GET_NUMBER_OF_TENANTS = "getNumberOfTenants";

    private static final String QUERY_GET_TENANTS_BY_IDS = "getTenantsByIds";

    private static final String QUERY_GET_TENANT_BY_NAME = "getTenantByName";

    private static final String QUERY_GET_TENANT_BY_ID = "getTenantById";

    private static final String QUERY_GET_PLATFORM = "getPlatform";

    private static String CACHE_KEY = "PLATFORM";

    private final PersistenceService platformPersistenceService;

    private final List<TenantPersistenceService> tenantPersistenceServices;

    private final TechnicalLoggerService logger;

    private final boolean trace;

    private final PlatformCacheService platformCacheService;

    private final SPlatformProperties sPlatformProperties;

    public PlatformServiceImpl(final PersistenceService platformPersistenceService, final List<TenantPersistenceService> tenantPersistenceServices,
            final TechnicalLoggerService logger, final PlatformCacheService platformCacheService, final SPlatformProperties sPlatformProperties) {
        this.platformPersistenceService = platformPersistenceService;
        this.tenantPersistenceServices = tenantPersistenceServices;
        this.logger = logger;
        this.platformCacheService = platformCacheService;
        this.sPlatformProperties = sPlatformProperties;
        trace = logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE);
    }

    @Override
    public void createPlatformTables() throws SPlatformCreationException {
        // create platform tables
        if (trace) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), LOG_CREATE_PLATFORM_TABLES));
        }
        try {
            platformPersistenceService.createStructure();
            platformPersistenceService.postCreateStructure();
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), LOG_CREATE_PLATFORM_TABLES));
            }
        } catch (final SPersistenceException e) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), LOG_CREATE_PLATFORM_TABLES, e));
            }
            throw new SPlatformCreationException("Unable to create platform tables : " + e.getMessage(), e);
        } catch (final IOException e) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), LOG_CREATE_PLATFORM_TABLES, e));
            }
            throw new SPlatformCreationException("Unable to create platform tables : " + e.getMessage(), e);
        }
    }

    @Override
    public void createPlatform(final SPlatform platform) throws SPlatformCreationException {
        if (trace) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), LOG_CREATE_PLATFORM));
        }
        // if platform doesn't exist, insert it
        try {
            platformPersistenceService.insert(platform);
            cachePlatform(platform);
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), LOG_CREATE_PLATFORM));
            }
        } catch (final SPersistenceException e) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), LOG_CREATE_PLATFORM, e));
            }
            throw new SPlatformCreationException("Unable to insert the platform row : " + e.getMessage(), e);
        }
    }

    @Override
    public long createTenant(final STenant tenant) throws STenantCreationException, STenantAlreadyExistException {
        if (trace) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), LOG_CREATE_TENANT));
        }
        // check if the tenant already exists. If yes, throws
        // TenantAlreadyExistException
        STenant existingTenant = null;
        final String tenantName = tenant.getName();
        try {
            existingTenant = getTenantByName(tenantName);
        } catch (final STenantNotFoundException e) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), LOG_CREATE_TENANT, e));
            }
            // OK
        }
        if (existingTenant != null) {
            throw new STenantAlreadyExistException("Unable to create the tenant " + tenantName + " : it already exists.");
        }

        // create the tenant
        try {
            platformPersistenceService.insert(tenant);
        } catch (final SPersistenceException e) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), LOG_CREATE_TENANT, e));
            }
            throw new STenantCreationException("Unable to insert the tenant row : " + e.getMessage(), e);
        }

        // initialize tenant
        try {
            final Map<String, String> replacements = new HashMap<String, String>();
            replacements.put("tenantid", Long.toString(tenant.getId()));
            for (final PersistenceService tenantPersistenceService : tenantPersistenceServices) {
                tenantPersistenceService.initializeStructure(replacements);
            }
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), LOG_CREATE_TENANT));
            }
            return tenant.getId();
        } catch (final SPersistenceException e) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), LOG_CREATE_TENANT, e));
            }
            throw new STenantCreationException("Unable to create tenant tables : " + e.getMessage(), e);
        } catch (final IOException e) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), LOG_CREATE_TENANT, e));
            }
            throw new STenantCreationException("Unable to create tenant tables : " + e.getMessage(), e);
        }
    }

    @Override
    public void createTenantTables() throws STenantCreationException {
        // this is the first tenant, we should create tenant tables
        try {
            for (final PersistenceService tenantPersistenceService : tenantPersistenceServices) {
                tenantPersistenceService.createStructure();
                tenantPersistenceService.postCreateStructure();
            }
        } catch (final SPersistenceException e) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), LOG_CREATE_TENANT_TABLES, e));
            }
            throw new STenantCreationException("Unable to create tenant tables : " + e.getMessage(), e);
        } catch (final IOException e) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), LOG_CREATE_TENANT_TABLES, e));
            }
            throw new STenantCreationException("Unable to create tenant tables : " + e.getMessage(), e);
        }
    }

    @Override
    public void initializePlatformStructure() throws SPlatformCreationException {
        try {
            platformPersistenceService.initializeStructure();
        } catch (final SPersistenceException e) {
            throw new SPlatformCreationException("Unable to initialize platform structure.", e);
        } catch (final IOException e) {
            throw new SPlatformCreationException("Unable to initialize platform structure.", e);
        }
    }

    @Override
    public void deletePlatform() throws SPlatformDeletionException, SPlatformNotFoundException {
        if (trace) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), LOG_DELETE_PLATFORM));
        }
        final SPlatform platform = readPlatform();
        checkNotExistingTenant();

        try {
            platformPersistenceService.delete(platform);
            platformCacheService.clear(CACHE_KEY);
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), LOG_DELETE_PLATFORM));
            }
        } catch (final SPersistenceException e) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), LOG_DELETE_PLATFORM, e));
            }
            throw new SPlatformDeletionException("Unable to delete the platform row : " + e.getMessage(), e);
        } catch (final SCacheException e) {
            throw new SPlatformDeletionException("Unable to delete the platform from cache : " + e.getMessage(), e);
        }
    }

    private void checkNotExistingTenant() throws SPlatformDeletionException {
        List<STenant> existingTenants;
        try {
            existingTenants = getTenants(new QueryOptions(0, 1, STenant.class, "id", OrderByType.ASC));
        } catch (final STenantException e) {
            throw new SPlatformDeletionException(e);
        }

        if (existingTenants.size() > 0) {
            throw new SPlatformDeletionException("Some tenants still are in the system. Can not delete platform.");
        }
    }

    @Override
    public void deletePlatformTables() throws SPlatformDeletionException {
        if (trace) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), LOG_DELETE_PLATFORM_TABLES));
        }
        // delete platform tables
        try {
            platformPersistenceService.preDropStructure();
            platformPersistenceService.deleteStructure();
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), LOG_DELETE_PLATFORM_TABLES));
            }
        } catch (final SPersistenceException e) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), LOG_DELETE_PLATFORM_TABLES, e));
            }

            throw new SPlatformDeletionException("Unable to delete platform tables : " + e.getMessage(), e);
        } catch (final IOException e) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), LOG_DELETE_PLATFORM_TABLES, e));
            }

            throw new SPlatformDeletionException("Unable to delete platform tables : " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteTenant(final long tenantId) throws STenantDeletionException, STenantNotFoundException, SDeletingActivatedTenantException {
        if (trace) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), LOG_DELETE_TENANT));
        }
        final STenant tenant = getTenant(tenantId);
        if (tenant.getStatus().equals(STenant.ACTIVATED)) {
            throw new SDeletingActivatedTenantException();
        }
        try {
            platformPersistenceService.delete(tenant);
        } catch (final SPersistenceException e) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), LOG_DELETE_TENANT, e));
            }
            throw new STenantDeletionException("Unable to delete the tenant : " + e.getMessage(), e);
        }

    }

    @Override
    public void deleteTenantTables() throws STenantDeletionException {
        try {
            for (final PersistenceService tenantPersistenceService : tenantPersistenceServices) {
                tenantPersistenceService.preDropStructure();
                tenantPersistenceService.deleteStructure();
            }
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), LOG_DELETE_TENANT_TABLES));
            }
        } catch (final SPersistenceException e) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), LOG_DELETE_TENANT_TABLES, e));
            }
            throw new STenantDeletionException("Unable to delete tenant tables: " + e.getMessage(), e);
        } catch (final IOException e) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), LOG_DELETE_TENANT_TABLES, e));
            }
            throw new STenantDeletionException("Unable to delete tenant tables : " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteTenantObjects(final long tenantId) throws STenantDeletionException, STenantNotFoundException, SDeletingActivatedTenantException {
        if (trace) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), LOG_DELETE_TENANT_OBJECTS));
        }
        final STenant tenant = getTenant(tenantId);
        if (tenant.getStatus().equals(STenant.ACTIVATED)) {
            throw new SDeletingActivatedTenantException();
        }
        try {
            for (final TenantPersistenceService tenantPersistenceService : tenantPersistenceServices) {
                tenantPersistenceService.deleteTenant(tenantId);
            }
        } catch (final SPersistenceException e) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), LOG_DELETE_TENANT_OBJECTS, e));
            }
            throw new STenantDeletionException("Unable to delete the tenant object : " + e.getMessage(), e);
        } catch (final IOException e) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), LOG_DELETE_TENANT_OBJECTS, e));
            }
            throw new STenantDeletionException("Unable to delete the tenant object : " + e.getMessage(), e);
        }
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

    /* *************************
     * The Platform is stored 1 time for each cache
     * We do this because the cache service handle
     * multi tenancy
     * *************************
     */
    private void cachePlatform(final SPlatform platform) {
        try {
            platformCacheService.store(CACHE_KEY, CACHE_KEY, platform);
        } catch (final SCacheException e) {
            logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Can't cache the platform, maybe the platform cache service is not started yet");
        }
    }

    private SPlatform readPlatform() throws SPlatformNotFoundException {
        if (trace) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), LOG_GET_PLATFORM));
        }
        SPlatform platform = null;
        try {
            platform = platformPersistenceService.selectOne(new SelectOneDescriptor<SPlatform>(QUERY_GET_PLATFORM, null, SPlatform.class));
            if (platform == null) {
                throw new SPlatformNotFoundException("No platform found");
            }
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), LOG_GET_PLATFORM));
            }
            return platform;
        } catch (final Exception e) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE,
                        LogUtil.getLogOnExceptionMethod(this.getClass(), LOG_GET_PLATFORM, "Unable to check if a platform already exists : " + e.getMessage()));
            }
            throw new SPlatformNotFoundException("Unable to check if a platform already exists : " + e.getMessage(), e);
        }
    }

    @Override
    public STenant getTenant(final long id) throws STenantNotFoundException {
        if (trace) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), QUERY_GET_TENANT_BY_ID));
        }
        STenant tenant;
        try {
            tenant = platformPersistenceService.selectById(new SelectByIdDescriptor<STenant>(QUERY_GET_TENANT_BY_ID, STenant.class, id));
            if (tenant == null) {
                throw new STenantNotFoundException("No tenant found with id: " + id);
            }
        } catch (final Exception e) {
            throw new STenantNotFoundException("Unable to get the tenant : " + e.getMessage(), e);
        }
        if (trace) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), QUERY_GET_TENANT_BY_ID));
        }
        return tenant;
    }

    @Override
    public boolean isPlatformCreated() {
        if (trace) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), LOG_IS_PLATFORM_CREATED));
        }
        boolean flag = false;
        try {
            getPlatform();
            flag = true;
        } catch (final SPlatformNotFoundException e) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), LOG_IS_PLATFORM_CREATED, e));
            }
            return false;
        }
        if (trace) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), LOG_IS_PLATFORM_CREATED));
        }
        return flag;
    }

    @Override
    public STenant getTenantByName(final String name) throws STenantNotFoundException {
        if (trace) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), QUERY_GET_TENANT_BY_NAME));
        }
        STenant tenant;
        try {
            final Map<String, Object> parameters = CollectionUtil.buildSimpleMap(BuilderFactory.get(STenantBuilderFactory.class).getNameKey(), name);
            tenant = platformPersistenceService.selectOne(new SelectOneDescriptor<STenant>(QUERY_GET_TENANT_BY_NAME, parameters, STenant.class));
            if (tenant == null) {
                throw new STenantNotFoundException("No tenant found with name: " + name);
            }
        } catch (final SBonitaReadException e) {
            throw new STenantNotFoundException("Unable to check if a tenant already exists: " + e.getMessage(), e);
        }
        if (trace) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), QUERY_GET_TENANT_BY_NAME));
        }
        return tenant;
    }

    @Override
    public STenant getDefaultTenant() throws STenantNotFoundException {
        if (trace) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), QUERY_GET_DEFAULT_TENANT));
        }
        STenant tenant = null;
        try {
            tenant = platformPersistenceService.selectOne(new SelectOneDescriptor<STenant>(QUERY_GET_DEFAULT_TENANT, null, STenant.class));
            if (tenant == null) {
                throw new STenantNotFoundException("No default tenant found");
            }
        } catch (final SBonitaReadException e) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), QUERY_GET_DEFAULT_TENANT, e));
            }
            throw new STenantNotFoundException("Unable to check if a default tenant already exists: " + e.getMessage(), e);
        }
        if (trace) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), QUERY_GET_DEFAULT_TENANT));
        }
        return tenant;
    }

    @Override
    public List<STenant> getTenants(final Collection<Long> ids, final QueryOptions queryOptions) throws STenantNotFoundException, STenantException {
        if (trace) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), LOG_GET_TENANTS));
        }
        List<STenant> tenants;
        try {
            final Map<String, Object> parameters = CollectionUtil.buildSimpleMap("ids", ids);
            tenants = platformPersistenceService
                    .selectList(new SelectListDescriptor<STenant>(QUERY_GET_TENANTS_BY_IDS, parameters, STenant.class, queryOptions));
            if (tenants.size() != ids.size()) {
                throw new STenantNotFoundException("Unable to retrieve all tenants by ids. Expected: " + ids + ", retrieved: " + tenants);
            }
        } catch (final SBonitaReadException e) {
            throw new STenantException("Problem getting list of tenants: " + e.getMessage(), e);
        }
        if (trace) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), LOG_GET_TENANTS));
        }
        return tenants;
    }

    @Override
    public void updatePlatform(final SPlatform platform, final EntityUpdateDescriptor descriptor) throws SPlatformUpdateException {
        if (trace) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), LOG_UPDATE_PLATFORM));
        }
        final UpdateDescriptor desc = new UpdateDescriptor(platform);
        desc.addFields(descriptor.getFields());
        try {
            platformPersistenceService.update(desc);
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), LOG_UPDATE_PLATFORM));
            }
        } catch (final SPersistenceException e) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), LOG_UPDATE_PLATFORM, e));
            }
            throw new SPlatformUpdateException("Problem while updating platform: " + platform, e);
        }
    }

    @Override
    public void updateTenant(final STenant tenant, final EntityUpdateDescriptor descriptor) throws STenantUpdateException {
        if (trace) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), LOG_UPDATE_TENANT));
        }
        final String tenantName = (String) descriptor.getFields().get(BuilderFactory.get(STenantBuilderFactory.class).getNameKey());
        if (tenantName != null) {
            try {
                if (getTenantByName(tenantName).getId() != tenant.getId()) {
                    throw new STenantUpdateException("Unable to update the tenant with new name " + tenantName + " : it already exists.");
                }
            } catch (final STenantNotFoundException e) {
                // Ok
            }
        }
        final UpdateDescriptor desc = new UpdateDescriptor(tenant);
        desc.addFields(descriptor.getFields());
        try {
            platformPersistenceService.update(desc);
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), LOG_UPDATE_TENANT));
            }
        } catch (final SPersistenceException e) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), LOG_UPDATE_TENANT, e));
            }
            throw new STenantUpdateException("Problem while updating tenant: " + tenant, e);
        }
    }

    @Override
    public boolean activateTenant(final long tenantId) throws STenantNotFoundException, STenantActivationException {
        if (trace) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), LOG_ACTIVATE_TENANT));
        }
        final STenant tenant = getTenant(tenantId);
        if (isTenantActivated(tenant)) {
            return false;
        }

        final UpdateDescriptor desc = new UpdateDescriptor(tenant);
        desc.addField(BuilderFactory.get(STenantBuilderFactory.class).getStatusKey(), STenant.ACTIVATED);
        try {
            platformPersistenceService.update(desc);
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), LOG_ACTIVATE_TENANT));
            }
        } catch (final SPersistenceException e) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), LOG_ACTIVATE_TENANT, e));
            }
            if (logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
                logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, e);
            }
            throw new STenantActivationException("Problem while activating tenant: " + tenant, e);
        }
        return true;
    }

    @Override
    public void deactiveTenant(final long tenantId) throws STenantNotFoundException, STenantDeactivationException {
        if (trace) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), LOG_DEACTIVE_TENANT));
        }
        final STenant tenant = getTenant(tenantId);
        final UpdateDescriptor desc = new UpdateDescriptor(tenant);
        desc.addField(BuilderFactory.get(STenantBuilderFactory.class).getStatusKey(), STenant.DEACTIVATED);
        try {
            platformPersistenceService.update(desc);
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), LOG_DEACTIVE_TENANT));
            }
        } catch (final SPersistenceException e) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), LOG_DEACTIVE_TENANT, e));
            }
            throw new STenantDeactivationException("Problem while deactivating tenant: " + tenant, e);
        }
    }

    @Override
    public List<STenant> getTenants(final QueryOptions queryOptions) throws STenantException {
        if (trace) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), LOG_GET_TENANTS));
        }
        List<STenant> tenants;
        try {
            tenants = platformPersistenceService.selectList(new SelectListDescriptor<STenant>(LOG_GET_TENANTS, null, STenant.class, queryOptions));
        } catch (final SBonitaReadException e) {
            throw new STenantException("Problem getting list of tenants : " + e.getMessage(), e);
        }
        if (trace) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), LOG_GET_TENANTS));
        }
        return tenants;
    }

    @Override
    public int getNumberOfTenants() throws STenantException {
        if (trace) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), QUERY_GET_NUMBER_OF_TENANTS));
        }
        final Map<String, Object> emptyMap = Collections.emptyMap();
        try {
            final Long read = platformPersistenceService.selectOne(new SelectOneDescriptor<Long>(QUERY_GET_NUMBER_OF_TENANTS, emptyMap, STenant.class,
                    Long.class));
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), QUERY_GET_NUMBER_OF_TENANTS));
            }
            return read.intValue();
        } catch (final SBonitaReadException e) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), QUERY_GET_NUMBER_OF_TENANTS, e));
            }
            throw new STenantException(e);
        }
    }

    @Override
    public boolean isTenantActivated(final STenant sTenant) {
        // the tenant is activated as soon as it is not deactivated (but it can be paused)
        return !STenant.DEACTIVATED.equals(sTenant.getStatus());
    }

    @Override
    public List<STenant> searchTenants(final QueryOptions options) throws SBonitaSearchException {
        if (trace) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "searchSTenants"));
        }
        try {
            final List<STenant> listsSTenants = platformPersistenceService.searchEntity(STenant.class, options, null);
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "searchSTenants"));
            }
            return listsSTenants;
        } catch (final SBonitaReadException bre) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "searchSTenants", bre));
            }
            throw new SBonitaSearchException(bre);
        }
    }

    @Override
    public long getNumberOfTenants(final QueryOptions options) throws SBonitaSearchException {
        if (trace) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getNumberOfSTenant"));
        }
        try {
            final long number = platformPersistenceService.getNumberOfEntities(STenant.class, options, null);
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getNumberOfSTenant"));
            }
            return number;
        } catch (final SBonitaReadException bre) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getNumberOfSTenant", bre));
            }
            throw new SBonitaSearchException(bre);
        }
    }

    @Override
    public void cleanTenantTables() throws STenantUpdateException {
        try {
            for (final PersistenceService tenantPersistenceService : tenantPersistenceServices) {
                tenantPersistenceService.cleanStructure();
            }
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "cleanTenantTables"));
            }
        } catch (final Exception e) {
            if (trace) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "cleanTenantTables", e));
            }
            throw new STenantUpdateException("Unable to clean tenant tables : " + e.getMessage(), e);
        }
    }

    @Override
    public SPlatformProperties getSPlatformProperties() {
        return sPlatformProperties;
    }

}
