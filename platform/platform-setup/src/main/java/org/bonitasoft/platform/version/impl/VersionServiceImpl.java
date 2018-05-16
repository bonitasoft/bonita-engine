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
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.platform.exception.PlatformException;
import org.bonitasoft.platform.version.VersionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final static Logger LOGGER = LoggerFactory.getLogger(VersionServiceImpl.class);

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public VersionServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String getPlatformVersion() throws PlatformException {
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
        String version = null;
        try {
            version = IOUtils.toString(this.getClass().getResource("/PLATFORM_ENGINE_VERSION"),
                    Charset.forName("UTF-8"));
        } catch (IOException e) {
            LOGGER.error("unable to read version.");
        }
        return version;
    }

    @Override
    public boolean isValidPlatformVersion() throws PlatformException {
        return getPlatformVersion().equals(getPlatformSetupVersion());
    }

}
