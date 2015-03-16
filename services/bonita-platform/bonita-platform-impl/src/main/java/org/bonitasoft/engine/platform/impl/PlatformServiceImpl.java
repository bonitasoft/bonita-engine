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
package org.bonitasoft.engine.platform.impl;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.cache.PlatformCacheService;
import org.bonitasoft.engine.cache.SCacheException;
import org.bonitasoft.engine.commons.CollectionUtil;
import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.exception.SDeletingActivatedTenantException;
import org.bonitasoft.engine.platform.exception.SPlatformCreationException;
import org.bonitasoft.engine.platform.exception.SPlatformDeletionException;
import org.bonitasoft.engine.platform.exception.SPlatformNotFoundException;
import org.bonitasoft.engine.platform.exception.SPlatformUpdateException;
import org.bonitasoft.engine.platform.exception.STenantActivationException;
import org.bonitasoft.engine.platform.exception.STenantAlreadyExistException;
import org.bonitasoft.engine.platform.exception.STenantCreationException;
import org.bonitasoft.engine.platform.exception.STenantDeactivationException;
import org.bonitasoft.engine.platform.exception.STenantDeletionException;
import org.bonitasoft.engine.platform.exception.STenantException;
import org.bonitasoft.engine.platform.exception.STenantNotFoundException;
import org.bonitasoft.engine.platform.exception.STenantUpdateException;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.SPlatformProperties;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.model.builder.STenantBuilderFactory;
import org.bonitasoft.engine.platform.model.builder.STenantUpdateBuilderFactory;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.services.TenantPersistenceService;
import org.bonitasoft.engine.services.UpdateDescriptor;

/**
 * @author Charles Souillard
 * @author Celine Souchet
 */
public class PlatformServiceImpl implements PlatformService {

    private static final String TENANT = "TENANT";

    private static final String QUERY_GET_TENANT_BY_NAME = "getTenantByName";

    private static final String QUERY_GET_TENANT_BY_ID = "getTenantById";

    private static final String LOG_UPDATE_TENANT = "updateTenant";

    private static final String LOG_ACTIVATE_TENANT = "activateTenant";

    private static final String LOG_DELETE_TENANT_OBJECTS = "deleteTenantObjects";

    private static final String LOG_DEACTIVE_TENANT = "deactiveTenant";

    private static final String LOG_UPDATE_PLATFORM = "updatePlatform";

    private static final String LOG_GET_TENANTS = "getTenants";

    private static final String LOG_GET_PLATFORM = "getPlatform";

    private static final String LOG_DELETE_TENANT = "deleteTenant";

    private static final String LOG_DELETE_PLATFORM = "deletePlatform";

    private static final String LOG_CREATE_TENANT = "createTenant";

    private static final String LOG_CREATE_PLATFORM = "createPlatform";

    private static final String LOG_IS_PLATFORM_CREATED = "isPlatformCreated";

    private static final String QUERY_GET_DEFAULT_TENANT = "getDefaultTenant";

    private static final String QUERY_GET_NUMBER_OF_TENANTS = "getNumberOfTenants";

    private static final String QUERY_GET_TENANTS_BY_IDS = "getTenantsByIds";

    private static final String QUERY_GET_PLATFORM = "getPlatform";

    private static String CACHE_KEY = "PLATFORM";

    private final PersistenceService platformPersistenceService;

    private final List<TenantPersistenceService> tenantPersistenceServices;

    private final TechnicalLoggerService logger;

    private final boolean isTraced;

    private final PlatformCacheService platformCacheService;

    private final SPlatformProperties sPlatformProperties;

    private final Recorder recorder;

    private final DataSource datasource;

    private final String statementDelimiter;

    private final List<String> sqlFolders;

    public PlatformServiceImpl(final PersistenceService platformPersistenceService, final Recorder recorder,
            final List<TenantPersistenceService> tenantPersistenceServices, final TechnicalLoggerService logger,
            final PlatformCacheService platformCacheService, final SPlatformProperties sPlatformProperties, final DataSource datasource,
            final String sqlFolder,
            final String statementDelimiter) {
        this(platformPersistenceService, recorder, tenantPersistenceServices, logger, platformCacheService, sPlatformProperties, datasource, asList(sqlFolder),
                statementDelimiter);
    }

    public PlatformServiceImpl(final PersistenceService platformPersistenceService, final Recorder recorder,
            final List<TenantPersistenceService> tenantPersistenceServices, final TechnicalLoggerService logger,
            final PlatformCacheService platformCacheService, final SPlatformProperties sPlatformProperties, final DataSource datasource,
            final List<String> sqlFolders, final String statementDelimiter) {
        this.platformPersistenceService = platformPersistenceService;
        this.tenantPersistenceServices = tenantPersistenceServices;
        this.logger = logger;
        this.platformCacheService = platformCacheService;
        this.sPlatformProperties = sPlatformProperties;
        this.recorder = recorder;
        this.datasource = datasource;
        this.sqlFolders = sqlFolders;
        this.statementDelimiter = statementDelimiter;

        isTraced = logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE);
    }

    // Copied from PlatformAPIImpl
    @Override
    public void createTables() throws SPlatformCreationException {
        try {
            executeSQLResources(asList("createTables.sql", "createQuartzTables.sql", "postCreateStructure.sql"));
        } catch (final IOException e) {
            throw new SPlatformCreationException(e);
        } catch (final SQLException e) {
            throw new SPlatformCreationException(e);
        }
    }

    /**
     * @param sqlFiles
     * @throws SQLException
     * @throws SPlatformCreationException
     */
    protected void executeSQLResources(final List<String> sqlFiles) throws IOException, SQLException {
        executeSQLResources(sqlFiles, Collections.<String, String> emptyMap());
    }

    /**
     * @param sqlFiles
     * @throws SQLException
     * @throws SPlatformCreationException
     */
    protected void executeSQLResources(final List<String> sqlFiles, final Map<String, String> replacements) throws IOException, SQLException {
        for (final String sqlFile : sqlFiles) {
            executeSQLResource(sqlFile, replacements);
        }
    }

    /**
     * @param sqlFolder the folder to look in.
     * @param sqlFile the name of the file to load.
     * @return null if not found, the SQL text content in normal cases.
     */
    private String getSQLFileContent(final String sqlFolder, final String sqlFile) {
        final String resourcePath = sqlFolder + "/" + sqlFile; // Must always be forward slash, even on Windows.
        try {
            final URL url = this.getClass().getResource(resourcePath);
            if (url != null) {
                final byte[] content = IOUtil.getAllContentFrom(url);
                if (content != null) {
                    return new String(content);
                }
            }
            else {
                // try to read from File():
                final File sqlResource = new File(sqlFolder, sqlFile);
                if (sqlResource.exists()) {
                    return new String(IOUtil.getAllContentFrom(sqlResource));
                }
            }
        } catch (final IOException e) {
            // ignore, will return null
        }
        return null;
    }

    /**
     * @param sqlFile
     * @throws IOException
     * @throws SPersistenceException
     */
    protected void executeSQLResource(final String sqlFile, final Map<String, String> replacements) throws IOException, SQLException {
        for (final String sqlFolder : sqlFolders) {
            final String fileContent = getSQLFileContent(sqlFolder, sqlFile);

            final String path = sqlFolder + File.separator + sqlFile;
            if (fileContent != null) {
                if (logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
                    logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Processing SQL resource : " + path);
                }
                final String regex = statementDelimiter.concat("\r?\n");
                final List<String> commands = new ArrayList<String>(asList(fileContent.split(regex)));
                final int lastIndex = commands.size() - 1;

                // TODO : Review the algo and see if we can avoid the array.
                String lastCommand = commands.get(lastIndex);
                final int index = lastCommand.lastIndexOf(statementDelimiter);
                if (index > 0) {
                    lastCommand = lastCommand.substring(0, index);
                    commands.remove(lastIndex);
                    commands.add(lastCommand);
                }

                doExecuteSQLThroughJDBC(commands, replacements);
            }
            else {
                logger.log(getClass(), TechnicalLogSeverity.WARNING, "SQL resource file not found: " + path);
            }
        }
    }

    private void doExecuteSQLThroughJDBC(final List<String> commands, final Map<String, String> replacements) throws SQLException {
        final Connection connection = getConnection();
        connection.setAutoCommit(false);
        try {
            for (final String command : commands) {
                if (command.trim().length() > 0) {
                    final Statement stmt = connection.createStatement();
                    String filledCommand = null;
                    try {
                        if (logger.isLoggable(getClass(), TechnicalLogSeverity.TRACE)) {
                            logger.log(getClass(), TechnicalLogSeverity.TRACE, command);
                        }
                        filledCommand = fillTemplate(command, replacements);
                        if (logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
                            logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Executing the following command : " + filledCommand);
                        }

                        stmt.execute(filledCommand);
                    } catch (final SQLException e) {
                        // Just log the Failing command in case of ERROR:
                        logger.log(getClass(), TechnicalLogSeverity.ERROR, "Following SQL command failed: " + filledCommand);
                        throw e;
                    } finally {
                        stmt.close();
                    }
                }
            }
            connection.commit();
        } catch (final SQLException sqe) {
            connection.rollback();
            throw sqe;
        } finally {
            connection.close();
        }
    }

    private String fillTemplate(final String command, final Map<String, String> replacements) {
        String trimmedCommand = command.trim();
        if (trimmedCommand.isEmpty() || replacements == null) {
            return trimmedCommand;
        }

        for (final Map.Entry<String, String> tableMapping : replacements.entrySet()) {
            final String stringToReplace = tableMapping.getKey();
            final String value = tableMapping.getValue();
            trimmedCommand = trimmedCommand.replaceAll(stringToReplace, value);
        }
        return trimmedCommand;
    }

    private Connection getConnection() throws SQLException {
        return datasource.getConnection();
    }

    @Override
    public void createPlatform(final SPlatform platform) throws SPlatformCreationException {
        if (isTraced) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), LOG_CREATE_PLATFORM));
        }
        // if platform doesn't exist, insert it
        try {
            platformPersistenceService.insert(platform);
            cachePlatform(platform);
            if (isTraced) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), LOG_CREATE_PLATFORM));
            }
        } catch (final SPersistenceException e) {
            if (isTraced) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), LOG_CREATE_PLATFORM, e));
            }
            throw new SPlatformCreationException("Unable to insert the platform row : " + e.getMessage(), e);
        }
    }

    @Override
    public long createTenant(final STenant tenant) throws STenantCreationException, STenantAlreadyExistException {
        if (isTraced) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), LOG_CREATE_TENANT));
        }
        // check if the tenant already exists. If yes, throws TenantAlreadyExistException
        checkIfTenantAlreadyExists(tenant);

        // create the tenant
        final InsertRecord insertRecord = new InsertRecord(tenant);
        final SInsertEvent insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(TENANT)
                .setObject(tenant).done();
        try {
            recorder.recordInsert(insertRecord, insertEvent);
        } catch (final SRecorderException e) {
            if (isTraced) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), LOG_CREATE_TENANT, e));
            }
            throw new STenantCreationException("Unable to insert the tenant row : " + e.getMessage(), e);
        }

        initializeTenant(tenant);
        return tenant.getId();
    }

    private void checkIfTenantAlreadyExists(final STenant tenant) throws STenantAlreadyExistException {
        try {
            final String tenantName = tenant.getName();
            final STenant existingTenant = getTenantByName(tenantName);
            if (existingTenant != null) {
                throw new STenantAlreadyExistException("Unable to create the tenant " + tenantName + " : it already exists.");
            }
        } catch (final STenantNotFoundException e) {
            if (isTraced) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), LOG_CREATE_TENANT, e));
            }
            // OK
        }
    }

    private void initializeTenant(final STenant tenant) throws STenantCreationException {
        try {
            executeSQLResources(asList("initTenantTables.sql"), buildReplacements(tenant));
        } catch (final IOException e) {
            throw new STenantCreationException(e);
        } catch (final SQLException e) {
            throw new STenantCreationException(e);
        }

        if (isTraced) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), LOG_CREATE_TENANT));
        }
    }

    /**
     * @param tenant
     * @return
     */
    private Map<String, String> buildReplacements(final STenant tenant) {
        return singletonMap("\\$\\{tenantid\\}", Long.toString(tenant.getId()));
    }

    @Override
    public void initializePlatformStructure() throws SPlatformCreationException {
        // Read the files initTables.sql from ${bonita.home}/server/sql/${db.vendor}
        try {
            executeSQLResources(asList("initTables.sql"));
        } catch (final IOException e) {
            throw new SPlatformCreationException(e);
        } catch (final SQLException e) {
            throw new SPlatformCreationException(e);
        }
    }

    @Override
    public void deletePlatform() throws SPlatformDeletionException, SPlatformNotFoundException {
        if (isTraced) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), LOG_DELETE_PLATFORM));
        }
        final SPlatform platform = readPlatform();
        checkNotExistingTenant();

        try {
            platformPersistenceService.delete(platform);
            platformCacheService.clear(CACHE_KEY);
            if (isTraced) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), LOG_DELETE_PLATFORM));
            }
        } catch (final SPersistenceException e) {
            if (isTraced) {
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
    public void deleteTables() throws SPlatformDeletionException {
        try {
            executeSQLResources(asList("preDropStructure.sql", "dropQuartzTables.sql", "dropTables.sql"));
        } catch (final IOException e) {
            throw new SPlatformDeletionException(e);
        } catch (final SQLException e) {
            throw new SPlatformDeletionException(e);
        }
    }

    @Override
    public void deleteTenant(final long tenantId) throws STenantDeletionException, STenantNotFoundException, SDeletingActivatedTenantException {
        if (isTraced) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), LOG_DELETE_TENANT));
        }
        final STenant tenant = getTenant(tenantId);
        if (tenant.getStatus().equals(STenant.ACTIVATED)) {
            throw new SDeletingActivatedTenantException();
        }
        try {
            platformPersistenceService.delete(tenant);
        } catch (final SPersistenceException e) {
            if (isTraced) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), LOG_DELETE_TENANT, e));
            }
            throw new STenantDeletionException("Unable to delete the tenant : " + e.getMessage(), e);
        }

    }

    @Override
    public void deleteTenantObjects(final long tenantId) throws STenantDeletionException, STenantNotFoundException, SDeletingActivatedTenantException {
        if (isTraced) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), LOG_DELETE_TENANT_OBJECTS));
        }
        final STenant tenant = getTenant(tenantId);
        if (tenant.getStatus().equals(STenant.ACTIVATED)) {
            throw new SDeletingActivatedTenantException();
        }

        final Map<String, String> replacements = buildReplacements(tenant);

        try {
            executeSQLResources(asList("deleteTenantObjects.sql"), replacements);
        } catch (final IOException e) {
            throw new STenantDeletionException(e);
        } catch (final SQLException e) {
            throw new STenantDeletionException(e);
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
        if (isTraced) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), LOG_GET_PLATFORM));
        }
        SPlatform platform = null;
        try {
            platform = platformPersistenceService.selectOne(new SelectOneDescriptor<SPlatform>(QUERY_GET_PLATFORM, null, SPlatform.class));
            if (platform == null) {
                throw new SPlatformNotFoundException("No platform found");
            }
            if (isTraced) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), LOG_GET_PLATFORM));
            }
            return platform;
        } catch (final Exception e) {
            if (isTraced) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE,
                        LogUtil.getLogOnExceptionMethod(this.getClass(), LOG_GET_PLATFORM, "Unable to check if a platform already exists : " + e.getMessage()));
            }
            throw new SPlatformNotFoundException("Unable to check if a platform already exists : " + e.getMessage(), e);
        }
    }

    @Override
    public STenant getTenant(final long id) throws STenantNotFoundException {
        if (isTraced) {
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
        if (isTraced) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), QUERY_GET_TENANT_BY_ID));
        }
        return tenant;
    }

    @Override
    public boolean isPlatformCreated() {
        if (isTraced) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), LOG_IS_PLATFORM_CREATED));
        }
        boolean flag = false;
        try {
            getPlatform();
            flag = true;
        } catch (final SPlatformNotFoundException e) {
            if (isTraced) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), LOG_IS_PLATFORM_CREATED, e));
            }
            return false;
        }
        if (isTraced) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), LOG_IS_PLATFORM_CREATED));
        }
        return flag;
    }

    @Override
    public STenant getTenantByName(final String name) throws STenantNotFoundException {
        if (isTraced) {
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
        if (isTraced) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), QUERY_GET_TENANT_BY_NAME));
        }
        return tenant;
    }

    @Override
    public STenant getDefaultTenant() throws STenantNotFoundException {
        if (isTraced) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), QUERY_GET_DEFAULT_TENANT));
        }
        STenant tenant = null;
        try {
            tenant = platformPersistenceService.selectOne(new SelectOneDescriptor<STenant>(QUERY_GET_DEFAULT_TENANT, null, STenant.class));
            if (tenant == null) {
                throw new STenantNotFoundException("No default tenant found");
            }
        } catch (final SBonitaReadException e) {
            if (isTraced) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), QUERY_GET_DEFAULT_TENANT, e));
            }
            throw new STenantNotFoundException("Unable to check if a default tenant already exists: " + e.getMessage(), e);
        }
        if (isTraced) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), QUERY_GET_DEFAULT_TENANT));
        }
        return tenant;
    }

    @Override
    public List<STenant> getTenants(final Collection<Long> ids, final QueryOptions queryOptions) throws STenantNotFoundException, STenantException {
        if (isTraced) {
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
        if (isTraced) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), LOG_GET_TENANTS));
        }
        return tenants;
    }

    @Override
    public void updatePlatform(final SPlatform platform, final EntityUpdateDescriptor descriptor) throws SPlatformUpdateException {
        if (isTraced) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), LOG_UPDATE_PLATFORM));
        }
        final UpdateDescriptor desc = new UpdateDescriptor(platform);
        desc.addFields(descriptor.getFields());
        try {
            platformPersistenceService.update(desc);
            if (isTraced) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), LOG_UPDATE_PLATFORM));
            }
        } catch (final SPersistenceException e) {
            if (isTraced) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), LOG_UPDATE_PLATFORM, e));
            }
            throw new SPlatformUpdateException("Problem while updating platform: " + platform, e);
        }
    }

    @Override
    public void updateTenant(final STenant tenant, final EntityUpdateDescriptor descriptor) throws STenantUpdateException {
        if (isTraced) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), LOG_UPDATE_TENANT));
        }
        final String tenantName = (String) descriptor.getFields().get(STenantUpdateBuilderFactory.NAME);
        if (tenantName != null) {
            try {
                if (getTenantByName(tenantName).getId() != tenant.getId()) {
                    throw new STenantUpdateException("Unable to update the tenant with new name " + tenantName + " : it already exists.");
                }
            } catch (final STenantNotFoundException e) {
                // Ok
            }
        }

        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(tenant, descriptor);
        final SUpdateEvent updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(TENANT).setObject(tenant).done();
        try {
            recorder.recordUpdate(updateRecord, updateEvent);
            if (isTraced) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), LOG_UPDATE_TENANT));
            }
        } catch (final SRecorderException e) {
            if (isTraced) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), LOG_UPDATE_TENANT, e));
            }
            throw new STenantUpdateException("Problem while updating tenant: " + tenant, e);
        }
    }

    @Override
    public boolean activateTenant(final long tenantId) throws STenantNotFoundException, STenantActivationException {
        if (isTraced) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), LOG_ACTIVATE_TENANT));
        }
        final STenant tenant = getTenant(tenantId);
        if (tenant.isActivated()) {
            return false;
        }

        final UpdateDescriptor desc = new UpdateDescriptor(tenant);
        desc.addField(BuilderFactory.get(STenantBuilderFactory.class).getStatusKey(), STenant.ACTIVATED);
        try {
            platformPersistenceService.update(desc);
            if (isTraced) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), LOG_ACTIVATE_TENANT));
            }
        } catch (final SPersistenceException e) {
            if (isTraced) {
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
        if (isTraced) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), LOG_DEACTIVE_TENANT));
        }
        final STenant tenant = getTenant(tenantId);
        final UpdateDescriptor desc = new UpdateDescriptor(tenant);
        desc.addField(BuilderFactory.get(STenantBuilderFactory.class).getStatusKey(), STenant.DEACTIVATED);
        try {
            platformPersistenceService.update(desc);
            if (isTraced) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), LOG_DEACTIVE_TENANT));
            }
        } catch (final SPersistenceException e) {
            if (isTraced) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), LOG_DEACTIVE_TENANT, e));
            }
            throw new STenantDeactivationException("Problem while deactivating tenant: " + tenant, e);
        }
    }

    @Override
    public List<STenant> getTenants(final QueryOptions queryOptions) throws STenantException {
        if (isTraced) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), LOG_GET_TENANTS));
        }
        List<STenant> tenants;
        try {
            tenants = platformPersistenceService.selectList(new SelectListDescriptor<STenant>(LOG_GET_TENANTS, null, STenant.class, queryOptions));
        } catch (final SBonitaReadException e) {
            throw new STenantException("Problem getting list of tenants : " + e.getMessage(), e);
        }
        if (isTraced) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), LOG_GET_TENANTS));
        }
        return tenants;
    }

    @Override
    public int getNumberOfTenants() throws STenantException {
        if (isTraced) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), QUERY_GET_NUMBER_OF_TENANTS));
        }
        final Map<String, Object> emptyMap = Collections.emptyMap();
        try {
            final Long read = platformPersistenceService.selectOne(new SelectOneDescriptor<Long>(QUERY_GET_NUMBER_OF_TENANTS, emptyMap, STenant.class,
                    Long.class));
            if (isTraced) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), QUERY_GET_NUMBER_OF_TENANTS));
            }
            return read.intValue();
        } catch (final SBonitaReadException e) {
            if (isTraced) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), QUERY_GET_NUMBER_OF_TENANTS, e));
            }
            throw new STenantException(e);
        }
    }

    @Override
    public List<STenant> searchTenants(final QueryOptions options) throws SBonitaReadException {
        if (isTraced) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "searchSTenants"));
        }
        final List<STenant> listsSTenants = platformPersistenceService.searchEntity(STenant.class, options, null);
        if (isTraced) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "searchSTenants"));
        }
        return listsSTenants;
    }

    @Override
    public long getNumberOfTenants(final QueryOptions options) throws SBonitaReadException {
        if (isTraced) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getNumberOfSTenant"));
        }
        try {
            final long number = platformPersistenceService.getNumberOfEntities(STenant.class, options, null);
            if (isTraced) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getNumberOfSTenant"));
            }
            return number;
        } catch (final SBonitaReadException bre) {
            if (isTraced) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getNumberOfSTenant", bre));
            }
            throw new SBonitaReadException(bre);
        }
    }

    @Override
    public void cleanTenantTables() throws STenantUpdateException {
        try {
            // TODO Rename the file to cleanTenantTables
            executeSQLResources(asList("cleanTables.sql"));
        } catch (final IOException e) {
            throw new STenantUpdateException(e);
        } catch (final SQLException e) {
            throw new STenantUpdateException(e);
        }

    }

    @Override
    public SPlatformProperties getSPlatformProperties() {
        return sPlatformProperties;
    }

}
