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
package org.bonitasoft.platform.configuration.util;

import java.util.List;

import org.bonitasoft.platform.configuration.impl.BonitaConfigurationRowMapper;
import org.bonitasoft.platform.configuration.impl.ConfigurationServiceImpl;
import org.bonitasoft.platform.configuration.model.BonitaConfiguration;
import org.bonitasoft.platform.configuration.type.ConfigurationType;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

/**
 * @author Laurent Leseigneur
 */
public class GetConfigurationInTransaction implements TransactionCallback<BonitaConfiguration> {

    private final JdbcTemplate jdbcTemplate;
    private final long tenantId;
    private final ConfigurationType type;
    private final String resourceName;

    private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ConfigurationServiceImpl.class);

    public GetConfigurationInTransaction(JdbcTemplate jdbcTemplate, long tenantId, ConfigurationType type, String resourceName) {
        this.jdbcTemplate = jdbcTemplate;
        this.tenantId = tenantId;
        this.type = type;
        this.resourceName = resourceName;
    }

    @Override
    public BonitaConfiguration doInTransaction(TransactionStatus status) {
        LOGGER.debug("get configurations for type:" + type.name() + " resource:" + resourceName + " and tenant id:" + tenantId);

        final List<BonitaConfiguration> bonitaConfigurations = jdbcTemplate.query(
                BonitaConfigurationRowMapper.SELECT_CONFIGURATION,
                new Object[] { tenantId, type.name(), resourceName },
                new BonitaConfigurationRowMapper());

        LOGGER.debug("configurations found:" + bonitaConfigurations.toString());

        if (bonitaConfigurations.size() == 1) {
            return bonitaConfigurations.get(0);
        }
        return null;
    }
}
