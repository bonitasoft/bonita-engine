/**
 * Copyright (C) 2023 Bonitasoft S.A.
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

import java.util.List;

import org.bonitasoft.platform.exception.PlatformException;
import org.bonitasoft.platform.version.ApplicationVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ApplicationVersionServiceImpl implements ApplicationVersionService {

    protected static final String SQL_PLATFORM_APPLICATION_VERSION = "SELECT p.application_version FROM platform p ORDER BY p.id";
    protected static final String SQL_PLATFORM_APPLICATION_VERSION_UPDATE = "UPDATE platform SET application_version = ?";

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ApplicationVersionServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String retrieveApplicationVersion() throws PlatformException {
        final List<String> strings;
        try {
            strings = jdbcTemplate.queryForList(SQL_PLATFORM_APPLICATION_VERSION, String.class);
        } catch (DataAccessException e) {
            throw new PlatformException("Platform is not created. Run 'setup init' first.", e);
        }
        if (strings.size() != 1) {
            throw new PlatformException("Platform is not created. Run 'setup init' first.");
        }
        return strings.get(0);
    }

    @Override
    public void updateApplicationVersion(String version) throws PlatformException {
        try {
            jdbcTemplate.update(SQL_PLATFORM_APPLICATION_VERSION_UPDATE, version);
        } catch (DataAccessException e) {
            throw new PlatformException("Error when updating Platform application version", e);
        }
    }
}
