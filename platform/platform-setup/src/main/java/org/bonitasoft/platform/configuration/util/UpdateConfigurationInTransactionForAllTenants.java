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
package org.bonitasoft.platform.configuration.util;

import java.util.List;

import org.bonitasoft.platform.configuration.impl.BonitaConfigurationTenantUpdater;
import org.bonitasoft.platform.configuration.model.BonitaConfiguration;
import org.bonitasoft.platform.configuration.type.ConfigurationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

/**
 * @author Emmanuel Duchastenier
 */
public class UpdateConfigurationInTransactionForAllTenants extends TransactionCallbackWithoutResult {

    private final JdbcTemplate jdbcTemplate;
    private final List<BonitaConfiguration> bonitaConfigurations;
    private final ConfigurationType type;
    private final String dbVendor;

    private final static Logger LOGGER = LoggerFactory.getLogger(UpdateConfigurationInTransactionForAllTenants.class);

    public UpdateConfigurationInTransactionForAllTenants(JdbcTemplate jdbcTemplate, String dbVendor, List<BonitaConfiguration> bonitaConfigurations,
            ConfigurationType type) {

        this.jdbcTemplate = jdbcTemplate;
        this.dbVendor = dbVendor;
        this.bonitaConfigurations = bonitaConfigurations;
        this.type = type;
    }

    @Override
    protected void doInTransactionWithoutResult(TransactionStatus status) {
        LOGGER.debug(
                "Updating configuration files " + bonitaConfigurations.toString() + " of type:" + type.name() + " for all tenants");

        jdbcTemplate.batchUpdate(BonitaConfigurationTenantUpdater.UPDATE_ALL_TENANTS_CONFIGURATION,
                new BonitaConfigurationTenantUpdater(bonitaConfigurations, dbVendor, type));

    }

}
