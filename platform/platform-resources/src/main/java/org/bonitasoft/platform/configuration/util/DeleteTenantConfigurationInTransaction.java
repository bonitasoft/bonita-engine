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

import org.bonitasoft.platform.configuration.impl.BonitaConfigurationTenantCleaner;
import org.bonitasoft.platform.configuration.impl.ConfigurationServiceImpl;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

/**
 * @author Laurent Leseigneur
 */
public class DeleteTenantConfigurationInTransaction extends TransactionCallbackWithoutResult {


    private final JdbcTemplate jdbcTemplate;
    private final String dbVendor;
    private final long tenantId;

    private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ConfigurationServiceImpl.class);

    public DeleteTenantConfigurationInTransaction(JdbcTemplate jdbcTemplate, String dbVendor, long tenantId) {
        this.jdbcTemplate = jdbcTemplate;
        this.dbVendor = dbVendor;
        this.tenantId = tenantId;
    }

    @Override
    protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
        LOGGER.info(
                "delete existing configurations for tenant id:" + tenantId );

        jdbcTemplate.batchUpdate(BonitaConfigurationTenantCleaner.DELETE_TENANT_CONFIGURATION,
                new BonitaConfigurationTenantCleaner(tenantId));

    }
}
