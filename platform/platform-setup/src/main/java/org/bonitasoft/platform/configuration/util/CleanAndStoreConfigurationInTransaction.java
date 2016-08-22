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

import org.bonitasoft.platform.configuration.impl.BonitaConfigurationContentTypeCleaner;
import org.bonitasoft.platform.configuration.impl.BonitaConfigurationPreparedStatementSetter;
import org.bonitasoft.platform.configuration.impl.ConfigurationServiceImpl;
import org.bonitasoft.platform.configuration.model.BonitaConfiguration;
import org.bonitasoft.platform.configuration.type.ConfigurationType;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

/**
 * @author Laurent Leseigneur
 */
public class CleanAndStoreConfigurationInTransaction extends TransactionCallbackWithoutResult {

    private final JdbcTemplate jdbcTemplate;
    private final List<BonitaConfiguration> bonitaConfigurations;
    private final ConfigurationType type;
    private final long tenantId;
    private final String dbVendor;

    private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ConfigurationServiceImpl.class);

    public CleanAndStoreConfigurationInTransaction(JdbcTemplate jdbcTemplate, String dbVendor, List<BonitaConfiguration> bonitaConfigurations, ConfigurationType type,
                                                   long tenantId) {

        this.jdbcTemplate = jdbcTemplate;
        this.dbVendor = dbVendor;
        this.bonitaConfigurations = bonitaConfigurations;
        this.type = type;
        this.tenantId = tenantId;
    }

    @Override
    protected void doInTransactionWithoutResult(TransactionStatus status) {
        LOGGER.debug(
                "delete existing configurations for type:" + type.name() + " and tenant id:" + tenantId );

        jdbcTemplate.batchUpdate(BonitaConfigurationContentTypeCleaner.DELETE_CONFIGURATION,
                new BonitaConfigurationContentTypeCleaner(type, tenantId));

        jdbcTemplate.batchUpdate(BonitaConfigurationPreparedStatementSetter.INSERT_CONFIGURATION,
                new BonitaConfigurationPreparedStatementSetter(bonitaConfigurations, dbVendor, type, tenantId));

    }

}
