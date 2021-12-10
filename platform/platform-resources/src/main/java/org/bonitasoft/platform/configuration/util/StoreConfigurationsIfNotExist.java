/**
 * Copyright (C) 2021 Bonitasoft S.A.
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

import static java.util.Collections.singletonList;
import static org.bonitasoft.platform.configuration.impl.ConfigurationServiceImpl.LOGGER;

import java.util.List;

import org.bonitasoft.platform.configuration.impl.BonitaAllConfigurationPreparedStatementSetter;
import org.bonitasoft.platform.configuration.model.FullBonitaConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

/**
 * @author Emmanuel Duchastenier
 */
public class StoreConfigurationsIfNotExist extends TransactionCallbackWithoutResult {

    public static final String SELECT_CONFIGURATION_EXISTS = "SELECT count(1) FROM configuration WHERE tenant_id = ? AND content_type = ? AND resource_name = ?";

    private final JdbcTemplate jdbcTemplate;
    private final String dbVendor;
    private final List<FullBonitaConfiguration> configurations;

    public StoreConfigurationsIfNotExist(JdbcTemplate jdbcTemplate, String dbVendor,
            List<FullBonitaConfiguration> configurations) {
        this.jdbcTemplate = jdbcTemplate;
        this.dbVendor = dbVendor;
        this.configurations = configurations;
    }

    @Override
    public void doInTransactionWithoutResult(TransactionStatus status) {
        for (FullBonitaConfiguration configuration : configurations) {
            final Integer nbRows = jdbcTemplate.queryForObject(SELECT_CONFIGURATION_EXISTS, Integer.class,
                    configuration.getTenantId(), configuration.getConfigurationType(), configuration.getResourceName());

            if (nbRows == 0) {
                // only keep elements that do not already exist in database...
                LOGGER.info("New configuration file detected '{}'. Storing it to database.",
                        configuration.getResourceName());
                jdbcTemplate.batchUpdate(BonitaAllConfigurationPreparedStatementSetter.INSERT_CONFIGURATION,
                        new BonitaAllConfigurationPreparedStatementSetter(singletonList(configuration), dbVendor));
            } else {
                LOGGER.debug("Configuration already exists for type: {}, resource: {} and tenant id: {}. Ignoring it.",
                        configuration.getConfigurationType(),
                        configuration.getResourceName(),
                        configuration.getTenantId());
            }
        }
    }
}
