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
package org.bonitasoft.platform.version.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.platform.exception.PlatformException;
import org.bonitasoft.platform.version.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * @author Laurent Leseigneur
 */
@Service
public class VersionServiceImpl implements VersionService {

    private static final String SQL_PLATFORM_VERSION = "SELECT p.version FROM platform p ORDER BY p.id";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public VersionServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String retrieveDatabaseSchemaVersion() throws PlatformException {
        final List<String> strings;
        try {
            strings = jdbcTemplate.query(SQL_PLATFORM_VERSION, new PlatformRowMapper());
        } catch (DataAccessException e) {
            throw new PlatformException("Platform is not created. Run 'setup init' first.", e);
        }
        if (hasNotSingleResult(strings)) {
            throw new PlatformException("Platform is not created. Run 'setup init' first.");
        }
        return strings.get(0);
    }

    private boolean hasNotSingleResult(List<String> strings) {
        return strings == null || strings.size() != 1;
    }

    @Override
    public String getPlatformSetupVersion() {
        return getVersionProperty("PLATFORM_ENGINE_VERSION");
    }

    @Override
    public String getSupportedDatabaseSchemaVersion() {
        // right now the supported database schema version is equal to the minor version of the product:
        return extractMinorVersion(getPlatformSetupVersion());
    }

    /**
     * This method is duplicate in class CheckPlatformVersion.
     * This is accepted to limit over-engineering just to extract an util method.
     */
    private String extractMinorVersion(String version) {
        String major = version.substring(0, version.indexOf('.'));
        String minor = version.substring(version.indexOf('.') + 1);
        minor = minor.substring(0, minor.indexOf('.'));
        return major + "." + minor;
    }

    private String getVersionProperty(String versionFileName) {
        try {
            return IOUtils.toString(this.getClass().getResource("/" + versionFileName), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(versionFileName + " file in jar resources does not exists");
        }
    }

    @Override
    public boolean isValidPlatformVersion() throws PlatformException {
        return getSupportedDatabaseSchemaVersion().equals(retrieveDatabaseSchemaVersion());
    }

}
