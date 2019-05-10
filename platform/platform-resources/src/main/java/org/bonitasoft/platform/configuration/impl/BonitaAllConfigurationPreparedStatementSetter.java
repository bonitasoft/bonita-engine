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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.bonitasoft.platform.configuration.model.FullBonitaConfiguration;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.support.lob.TemporaryLobCreator;

/**
 * @author Laurent Leseigneur
 */
public class BonitaAllConfigurationPreparedStatementSetter implements BatchPreparedStatementSetter, ConfigurationColumns {

    public static final String INSERT_CONFIGURATION = "INSERT into configuration(tenant_id, content_type, resource_name, resource_content) values (?,?,?,?)";
    private final List<FullBonitaConfiguration> bonitaConfigurations;

    String dbVendor;

    public BonitaAllConfigurationPreparedStatementSetter(List<FullBonitaConfiguration> bonitaConfigurations, String dbVendor) {
        this.bonitaConfigurations = bonitaConfigurations;
        this.dbVendor = dbVendor;
        if (this.dbVendor == null) {
            this.dbVendor = System.getProperty("sysprop.bonita.db.vendor");
        }
    }

    @Override
    public void setValues(PreparedStatement ps, int i) throws SQLException {
        final TemporaryLobCreator temporaryLobCreator = new TemporaryLobCreator();

        final FullBonitaConfiguration bonitaConfiguration = bonitaConfigurations.get(i);
        ps.setLong(COLUMN_INDEX_TENANT_ID, bonitaConfiguration.getTenantId());
        ps.setString(COLUMN_INDEX_TYPE, bonitaConfiguration.getConfigurationType());
        ps.setString(COLUMN_INDEX_RESOURCE_NAME, bonitaConfiguration.getResourceName());
        switch (dbVendor) {
            case "h2":
            case "postgres":
                ps.setBytes(COLUMN_INDEX_RESOURCE_CONTENT, bonitaConfiguration.getResourceContent());
                break;
            case "oracle":
            case "mysql":
            case "sqlserver":
                temporaryLobCreator.setBlobAsBytes(ps, COLUMN_INDEX_RESOURCE_CONTENT, bonitaConfiguration.getResourceContent());
                break;
            default:
                throw new IllegalArgumentException(new StringBuilder("unsupported db vendor:").append(dbVendor).toString());

        }

    }

    @Override
    public int getBatchSize() {
        return bonitaConfigurations.size();
    }
}
