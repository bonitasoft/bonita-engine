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

import org.bonitasoft.platform.configuration.impl.FullBonitaConfigurationRowMapper;
import org.bonitasoft.platform.configuration.model.FullBonitaConfiguration;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

/**
 * @author Laurent Leseigneur
 */
public class GetAllConfigurationInTransaction implements TransactionCallback<List<FullBonitaConfiguration>> {

    private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GetAllConfigurationInTransaction.class);
    private final JdbcTemplate jdbcTemplate;

    public GetAllConfigurationInTransaction(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<FullBonitaConfiguration> doInTransaction(TransactionStatus transactionStatus) {
        LOGGER.debug("get all configurations");

        final List<FullBonitaConfiguration> fullBonitaConfigurations = jdbcTemplate.query(
                FullBonitaConfigurationRowMapper.SELECT_CONFIGURATION,
                new FullBonitaConfigurationRowMapper());

        LOGGER.debug("configurations found:" + fullBonitaConfigurations.toString());

        return fullBonitaConfigurations;
    }

}
