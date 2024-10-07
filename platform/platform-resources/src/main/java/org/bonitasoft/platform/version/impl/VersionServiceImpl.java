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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final String SQL_PLATFORM_VERSION = "SELECT p.version FROM platform p";
    private static final String SQL_PLATFORM_INFORMATION = "SELECT p.information FROM platform p";
    private static final String SQL_CLEAR_PLATFORM_INFORMATION = "UPDATE platform SET information = NULL";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public VersionServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String retrieveDatabaseSchemaVersion() throws PlatformException {
        final List<String> strings;
        try {
            strings = jdbcTemplate.queryForList(SQL_PLATFORM_VERSION, String.class);
        } catch (DataAccessException e) {
            throw new PlatformException("Platform is not created. Run 'setup init' first.", e);
        }
        if (hasNotSingleResult(strings)) {
            throw new PlatformException("Platform is not created. Run 'setup init' first.");
        }
        return strings.get(0);
    }

    @Override
    public String retrievePlatformInformation() throws PlatformException {
        final List<String> strings;
        try {
            strings = jdbcTemplate.queryForList(SQL_PLATFORM_INFORMATION, String.class);
        } catch (DataAccessException e) {
            throw new PlatformException("Platform is not created. Run 'setup init' first.", e);
        }
        if (hasNotSingleResult(strings)) {
            throw new PlatformException("Platform is not created. Run 'setup init' first.");
        }
        return strings.get(0);
    }

    @Override
    public void clearPlatformInformation() throws PlatformException {
        try {
            jdbcTemplate.execute(SQL_CLEAR_PLATFORM_INFORMATION);
        } catch (DataAccessException e) {
            throw new PlatformException("Unable to clear platform information", e);
        }
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
        final Matcher matcher = Pattern.compile("(\\d+\\.\\d+).*").matcher(version);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            throw new IllegalArgumentException(version + " does not respect Semantic Versioning");
        }
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
