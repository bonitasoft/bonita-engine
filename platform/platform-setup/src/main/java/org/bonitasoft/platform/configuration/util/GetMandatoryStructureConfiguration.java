/**
 * Copyright (C) 2018 Bonitasoft S.A.
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
package org.bonitasoft.platform.configuration.util;

import static org.bonitasoft.platform.configuration.impl.ConfigurationFields.CONTENT_TYPE;
import static org.bonitasoft.platform.configuration.impl.ConfigurationFields.TENANT_ID;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.bonitasoft.platform.configuration.model.LightBonitaConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

/**
 * @author Emmanuel Duchastenier
 */
public class GetMandatoryStructureConfiguration implements TransactionCallback<List<LightBonitaConfiguration>> {

    private final static Logger LOGGER = LoggerFactory.getLogger(GetMandatoryStructureConfiguration.class);
    private final JdbcTemplate jdbcTemplate;

    public GetMandatoryStructureConfiguration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<LightBonitaConfiguration> doInTransaction(TransactionStatus transactionStatus) {
        final List<LightBonitaConfiguration> lightBonitaConfigurations = jdbcTemplate
                .query(LightBonitaConfigurationRowMapper.SELECT, new LightBonitaConfigurationRowMapper());

        LOGGER.debug("configurations found:" + lightBonitaConfigurations.toString());

        return lightBonitaConfigurations;
    }

    class LightBonitaConfigurationRowMapper implements RowMapper<LightBonitaConfiguration> {

        public static final String SELECT = "SELECT distinct tenant_id, content_type" +
                " FROM configuration" +
                " WHERE content_type <> 'LICENSES'" +
                " ORDER BY tenant_id, content_type";

        @Override
        public LightBonitaConfiguration mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new LightBonitaConfiguration(rs.getLong(TENANT_ID), rs.getString(CONTENT_TYPE));
        }
    }

}
