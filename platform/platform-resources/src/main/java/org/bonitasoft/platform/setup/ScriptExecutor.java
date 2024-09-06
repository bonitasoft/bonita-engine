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
package org.bonitasoft.platform.setup;

import static java.util.Arrays.asList;
import static org.bonitasoft.platform.setup.PlatformSetup.BONITA_SETUP_FOLDER;
import static org.bonitasoft.platform.setup.PlatformSetup.PLATFORM_CONF_FOLDER_NAME;
import static org.bonitasoft.platform.setup.SimpleEncryptor.encrypt;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.platform.exception.PlatformException;
import org.bonitasoft.platform.version.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

/**
 * @author Emmanuel Duchastenier
 */
@Slf4j
@Component
@ConditionalOnSingleCandidate(ScriptExecutor.class)
@PropertySource("classpath:/application.properties")
public class ScriptExecutor {

    private static final boolean CONTINUE_ON_ERROR = true;

    public static final boolean FAIL_ON_ERROR = false;

    private final String sqlFolder;

    private final DataSource datasource;

    private final String dbVendor;

    private final VersionService versionService;

    @Autowired
    public ScriptExecutor(@Value("${db.vendor}") String dbVendor, DataSource datasource,
            VersionService versionService) {
        if (dbVendor == null) {
            throw new IllegalArgumentException("dbVendor is null");
        }
        this.dbVendor = dbVendor;
        this.datasource = datasource;
        log.info("configuration for Database vendor: {}", dbVendor);
        this.sqlFolder = "/sql/" + dbVendor;
        this.versionService = versionService;
    }

    public void createTables() throws PlatformException {
        try {
            executeSQLResources(asList("createTables.sql", "createQuartzTables.sql", "postCreateStructure.sql"),
                    FAIL_ON_ERROR);
        } catch (final IOException e) {
            throw new PlatformException(e);
        }
    }

    /**
     * Creates and initializes the platform if it is not already done. It creates database tables and populates it.
     *
     * @return <code>true</code> if it is a first initialization of the platform.
     * @throws PlatformException if it fails to create or initialize tables.
     */
    public void createAndInitializePlatformIfNecessary() throws PlatformException {
        if (!isPlatformAlreadyCreated()) {
            createTables();
            initializePlatformStructure();
            insertPlatform();
            insertTenant();
        } else {
            log.info("Bonita platform already exists. Nothing to do. Stopping.");
        }
    }

    protected void insertPlatform() {
        String version = versionService.getPlatformSetupVersion();
        String databaseSchemaVersion = versionService.getSupportedDatabaseSchemaVersion();

        final String sql = "INSERT INTO platform (id, version, initial_bonita_version, application_version, " +
                "maintenance_message_active, created, created_by, information) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        new JdbcTemplate(datasource).update(sql, 1L, databaseSchemaVersion, version, "0.0.0", false,
                System.currentTimeMillis(), "platformAdmin", getInformationInitialValue());
    }

    protected String getInformationInitialValue() {
        try {
            return encrypt(new ObjectMapper().writeValueAsBytes(new ArrayList<Long>()));
        } catch (GeneralSecurityException | JsonProcessingException e) {
            log.debug(e.getMessage(), e);
            throw new IllegalStateException("Cannot properly setup Bonita platform");
        }
    }

    private void insertTenant() {
        final String sql = "INSERT INTO tenant (id, created, createdBy, description, defaultTenant, name, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        new JdbcTemplate(datasource).update(sql, 1L, System.currentTimeMillis(),
                "defaultUser", "Default tenant", true, "default", "ACTIVATED");
    }

    public boolean isPlatformAlreadyCreated() {
        try {
            return new JdbcTemplate(datasource).queryForObject("select count(*) from sequence", Integer.class) > 0;
        } catch (DataAccessException e) {
            return false;
        }
    }

    /**
     * @param sqlFiles the sql files to execute
     * @param shouldContinueOnError
     */
    protected void executeSQLResources(final List<String> sqlFiles, boolean shouldContinueOnError) throws IOException {
        for (final String sqlFile : sqlFiles) {
            executeSQLResource(sqlFile, shouldContinueOnError);
        }
    }

    /**
     * @param sqlFolder the folder to look in.
     * @param sqlFile the name of the file to load.
     * @return null if not found, the SQL text content in normal cases.
     */
    private Resource getSQLResource(final String sqlFolder, final String sqlFile) {
        String setupFolderPath = System.getProperty(BONITA_SETUP_FOLDER);
        if (setupFolderPath != null) {
            return getResourceFromFileSystem(setupFolderPath, sqlFile);
        } else {
            return getResourceFromClassPath(sqlFolder, sqlFile);
        }
    }

    private Resource getResourceFromFileSystem(String setupFolderPath, String sqlFile) {
        Path path = Paths.get(setupFolderPath).resolve(PLATFORM_CONF_FOLDER_NAME).resolve("sql").resolve(dbVendor)
                .resolve(sqlFile);
        final File file = path.toFile();
        if (file.exists()) {
            return new FileSystemResource(file);
        } else {
            final String msg = "SQL resource file not found in filesystem: " + file.getAbsolutePath();
            log.error(msg);
            throw new RuntimeException(msg);
        }
    }

    private Resource getResourceFromClassPath(String sqlFolder, String sqlFile) {
        final String resourcePath = sqlFolder + "/" + sqlFile; // Must always be forward slash, even on Windows.
        final URL url = this.getClass().getResource(resourcePath);
        if (url != null) {
            return new UrlResource(url);
        } else {
            final String msg = "SQL resource file not found in classpath: " + resourcePath;
            log.warn(msg);
            throw new RuntimeException(msg);
        }
    }

    /**
     * @param sqlFile the sql file to execute
     * @param shouldContinueOnError
     * @throws IOException
     */
    protected void executeSQLResource(final String sqlFile, boolean shouldContinueOnError) throws IOException {
        final Resource sqlResource = getSQLResource(sqlFolder, sqlFile);
        ResourceDatabasePopulator populate = new ResourceDatabasePopulator();
        populate.setContinueOnError(shouldContinueOnError);
        populate.setIgnoreFailedDrops(true);
        populate.addScript(sqlResource);
        populate.execute(datasource);
        log.info("Executed SQL script " + sqlResource.getURL().getFile());
    }

    public void initializePlatformStructure() throws PlatformException {
        try {
            executeSQLResources(Collections.singletonList("initTables.sql"), FAIL_ON_ERROR);
        } catch (final IOException e) {
            throw new PlatformException(e);
        }
    }

    public void deleteTables() throws PlatformException {
        try {
            executeSQLResources(asList("preDropStructure.sql", "dropQuartzTables.sql", "dropTables.sql"),
                    CONTINUE_ON_ERROR);
        } catch (final IOException e) {
            throw new PlatformException(e);
        }
    }

}
