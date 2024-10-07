/**
 * Copyright (C) 2017 Bonitasoft S.A.
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

import org.bonitasoft.platform.configuration.model.BonitaConfiguration;
import org.bonitasoft.platform.configuration.type.ConfigurationType;
import org.bonitasoft.platform.database.DatabaseVendor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.support.lob.TemporaryLobCreator;

/**
 * @author Emmanuel Duchastenier
 */
public class BonitaConfigurationTenantUpdater implements BatchPreparedStatementSetter {

    public static final String UPDATE_ALL_TENANTS_CONFIGURATION = "UPDATE configuration SET resource_content=? WHERE content_type=? AND resource_name=?";

    private final List<BonitaConfiguration> bonitaConfigurations;
    private final String dbVendor;
    private final ConfigurationType type;

    public BonitaConfigurationTenantUpdater(List<BonitaConfiguration> bonitaConfigurations, String dbVendor,
            ConfigurationType type) {
        this.bonitaConfigurations = bonitaConfigurations;
        this.dbVendor = dbVendor;
        this.type = type;
    }

    @Override
    public void setValues(PreparedStatement ps, int i) throws SQLException {
        final BonitaConfiguration bonitaConfiguration = bonitaConfigurations.get(i);
        ps.setString(2, type.toString());
        ps.setString(3, bonitaConfiguration.getResourceName());
        switch (DatabaseVendor.parseValue(dbVendor)) {
            case H2, POSTGRES:
                ps.setBytes(1, bonitaConfiguration.getResourceContent());
                break;
            case ORACLE, MYSQL, SQLSERVER:
                new TemporaryLobCreator().setBlobAsBytes(ps, 1, bonitaConfiguration.getResourceContent());
                break;
            default:
                throw new IllegalArgumentException("unsupported db vendor:" + dbVendor);
        }
    }

    @Override
    public int getBatchSize() {
        return bonitaConfigurations.size();
    }
}
