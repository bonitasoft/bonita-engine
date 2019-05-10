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
package org.bonitasoft.platform.configuration.impl;

import static org.bonitasoft.platform.configuration.impl.ConfigurationFields.*;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bonitasoft.platform.configuration.model.FullBonitaConfiguration;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author Laurent Leseigneur
 */
public class FullBonitaConfigurationRowMapper implements RowMapper<FullBonitaConfiguration> {

    public static final String SELECT_CONFIGURATION = "SELECT tenant_id, content_type, resource_name, resource_content FROM configuration ORDER BY tenant_id, content_type, resource_name";

    @Override
    public FullBonitaConfiguration mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new FullBonitaConfiguration(rs.getString(RESOURCE_NAME), rs.getBytes(RESOURCE_CONTENT), rs.getString(CONTENT_TYPE), rs.getLong(TENANT_ID));
    }
}
