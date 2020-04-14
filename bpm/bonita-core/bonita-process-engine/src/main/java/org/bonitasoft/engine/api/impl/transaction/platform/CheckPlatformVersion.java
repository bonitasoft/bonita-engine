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
package org.bonitasoft.engine.api.impl.transaction.platform;

import java.text.MessageFormat;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.EngineInitializer;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.platform.PlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 */
public class CheckPlatformVersion implements Callable<Boolean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EngineInitializer.class);

    private final PlatformService platformService;

    public CheckPlatformVersion(final PlatformService platformService) {
        this.platformService = platformService;
    }

    private String errorMessage;

    @Override
    public Boolean call() throws SBonitaException {
        // the database  schema version
        String databaseSchemaVersion = platformService.getPlatform().getDbSchemaVersion();
        // the version in jars
        final String platformVersionFromBinaries = platformService.getSPlatformProperties().getPlatformVersion();
        String supportedDatabaseSchemaVersion = extractMinorVersion(platformVersionFromBinaries);
        LOGGER.info("Bonita platform version (binaries): {}", platformVersionFromBinaries);
        LOGGER.info("Bonita database schema version: {}", databaseSchemaVersion);

        boolean isDatabaseSchemaSupported = databaseSchemaVersion.equals(supportedDatabaseSchemaVersion);
        if (!isDatabaseSchemaSupported) {
            errorMessage = MessageFormat.format("The version of the platform in database is not the same as expected:" +
                    " Supported database schema version is <{0}> and current database schema version is <{1}>",
                    supportedDatabaseSchemaVersion, databaseSchemaVersion);
        }
        return isDatabaseSchemaSupported;
    }

    /**
     * This method is duplicate in class VersionServiceImpl.
     * This is accepted to limit over-engineering just to extract an util method.
     */
    private String extractMinorVersion(String version) {
        String major = version.substring(0, version.indexOf('.'));
        String minor = version.substring(version.indexOf('.') + 1);
        minor = minor.substring(0, minor.indexOf('.'));
        return major + "." + minor;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
