/*
 * Copyright (C) 2016 Bonitasoft S.A.
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
 */
package org.bonitasoft.platform.setup;

import static java.util.Arrays.asList;
import static org.bonitasoft.platform.setup.PlatformSetup.BONITA_SETUP_FOLDER;
import static org.bonitasoft.platform.setup.PlatformSetup.PLATFORM_CONF_FOLDER_NAME;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.bonitasoft.platform.exception.PlatformException;
import org.bonitasoft.platform.version.VersionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
@Component
@PropertySource("classpath:/application.properties")
public class ScriptExecutor {

    public static final boolean CONTINUE_ON_ERROR = true;

    public static final boolean FAIL_ON_ERROR = false;

    private final Logger logger = LoggerFactory.getLogger(ScriptExecutor.class);

    private final String sqlFolder;

    private final DataSource datasource;

    private final String dbVendor;

    private VersionService versionService;

    @Autowired
    public ScriptExecutor(@Value("${db.vendor}") String dbVendor, DataSource datasource, VersionService versionService) {
        if (dbVendor == null) {
            throw new IllegalArgumentException("dbVendor is null");
        }
        this.dbVendor = dbVendor;
        this.datasource = datasource;
        logger.info("configuration for Database vendor: " + dbVendor);
        this.sqlFolder = "/sql/" + dbVendor;
        this.versionService = versionService;
    }

    public void createTables() throws PlatformException {
        try {
            executeSQLResources(asList("createTables.sql", "createQuartzTables.sql", "postCreateStructure.sql"),
                    FAIL_ON_ERROR);
        } catch (final IOException | SQLException e) {
            throw new PlatformException(e);
        }
    }

    public void createAndInitializePlatformIfNecessary() throws PlatformException {
        if (!isPlatformAlreadyCreated()) {
            createTables();
            initializePlatformStructure();
            insertPlatform();
        } else {
            logger.info("Bonita platform already exists. Nothing to do. Stopping.");
        }
    }

    protected void insertPlatform() throws PlatformException {
        String version = versionService.getPlatformSetupVersion();
        final String sql = "INSERT INTO platform (id, version, previousversion, initialversion, created, createdby) VALUES (1, '" + version + "', '', '"
                + version + "', " + System.currentTimeMillis() + ", 'platformAdmin')";
        new JdbcTemplate(datasource).update(sql);
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
     * @throws SQLException
     */
    protected void executeSQLResources(final List<String> sqlFiles, boolean shouldContinueOnError) throws IOException, SQLException {
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
        Path path = Paths.get(setupFolderPath).resolve(PLATFORM_CONF_FOLDER_NAME).resolve("sql").resolve(dbVendor).resolve(sqlFile);
        final File file = path.toFile();
        if (file.exists()) {
            return new FileSystemResource(file);
        } else {
            final String msg = "SQL resource file not found in filesystem: " + file.getAbsolutePath();
            logger.error(msg);
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
            logger.warn(msg);
            throw new RuntimeException(msg);
        }
    }

    /**
     * @param sqlFile the sql file to execute
     * @param shouldContinueOnError
     * @throws IOException
     */
    protected void executeSQLResource(final String sqlFile, boolean shouldContinueOnError) throws IOException, SQLException {
        final Resource sqlResource = getSQLResource(sqlFolder, sqlFile);
        ResourceDatabasePopulator populate = new ResourceDatabasePopulator();
        populate.setContinueOnError(shouldContinueOnError);
        populate.setIgnoreFailedDrops(true);
        populate.addScript(sqlResource);
        populate.setSeparator(getSeparator());
        populate.execute(datasource);
        logger.info("Executed SQL script " + sqlResource.getURL().getFile());
    }

    private String getSeparator() {
        if ("sqlserver".equals(dbVendor)) {
            return "GO";
        } else {
            return ";";
        }
    }

    public void initializePlatformStructure() throws PlatformException {
        try {
            executeSQLResources(Collections.singletonList("initTables.sql"), FAIL_ON_ERROR);
        } catch (final IOException | SQLException e) {
            throw new PlatformException(e);
        }
    }

    public void deleteTables() throws PlatformException {
        try {
            executeSQLResources(asList("preDropStructure.sql", "dropQuartzTables.sql", "dropTables.sql"), CONTINUE_ON_ERROR);
        } catch (final IOException | SQLException e) {
            throw new PlatformException(e);
        }
    }

}
