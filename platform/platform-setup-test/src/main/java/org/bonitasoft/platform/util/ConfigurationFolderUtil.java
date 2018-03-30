/**
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
 **/
package org.bonitasoft.platform.util;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Laurent Leseigneur
 */
public class ConfigurationFolderUtil {

    public static final String PLATFORM_CONF_FOLDER_NAME = "platform_conf";
    public static final String[] ALL_SQL_FILES = new String[] { "cleanTables.sql",
            "createQuartzTables.sql",
            "createTables.sql",
            "deleteTenantObjects.sql",
            "dropQuartzTables.sql",
            "dropTables.sql",
            "initTables.sql",
            "initTenantTables.sql",
            "postCreateStructure.sql",
            "preDropStructure.sql" };

    public Path buildPlatformConfFolder(Path rootFolder) throws IOException {
        Path initialFolder = rootFolder.resolve(PLATFORM_CONF_FOLDER_NAME);
        Files.createDirectories(initialFolder);
        return initialFolder;
    }

    public void buildInitialFolder(Path rootFolder) throws IOException {
        Path platform_init_engine = rootFolder.resolve(PLATFORM_CONF_FOLDER_NAME).resolve("initial").resolve("platform_init_engine");
        Files.createDirectories(platform_init_engine);
        Files.write(platform_init_engine.resolve("initialConfig.properties"), "key=value".getBytes());
    }

    public Path buildCurrentFolder(Path rootFolder) throws IOException {
        Path platform_init_engine = rootFolder.resolve(PLATFORM_CONF_FOLDER_NAME).resolve("current").resolve("platform_init_engine");
        Files.createDirectories(platform_init_engine);
        Files.write(platform_init_engine.resolve("currentConfig.properties"), "key=value".getBytes());
        return platform_init_engine;
    }

    public void buildSqlFolder(Path rootFolder, String dbVendor) throws IOException {
        Path sqlPath = rootFolder.resolve(PLATFORM_CONF_FOLDER_NAME).resolve("sql").resolve(dbVendor);
        Files.createDirectories(sqlPath);
        for (String sqlFile : asList(ALL_SQL_FILES)) {
            Files.copy(ConfigurationFolderUtil.class.getResourceAsStream("/sql/" + dbVendor + "/" + sqlFile), sqlPath.resolve(sqlFile));
        }
    }

}
