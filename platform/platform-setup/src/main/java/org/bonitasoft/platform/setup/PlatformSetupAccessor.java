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

package org.bonitasoft.platform.setup;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.bonitasoft.platform.configuration.ConfigurationService;
import org.bonitasoft.platform.configuration.impl.ConfigurationServiceImpl;
import org.bonitasoft.platform.version.VersionService;
import org.bonitasoft.platform.version.impl.VersionServiceImpl;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * access platform setup outside of a spring context ( to be replace by a spring context...)
 *
 * @author Baptiste Mesta
 */
public class PlatformSetupAccessor {

    public static PlatformSetup getPlatformSetup() throws NamingException {
        final DataSource dataSource = lookupDataSource();
        String dbVendor = System.getProperty("sysprop.bonita.db.vendor");
        return getPlatformSetup(dataSource, dbVendor);
    }

    public static PlatformSetup getPlatformSetup(DataSource dataSource, String dbVendor) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        final DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager(dataSource);
        TransactionTemplate transactionTemplate = new TransactionTemplate(dataSourceTransactionManager);
        VersionService versionService = new VersionServiceImpl(jdbcTemplate);
        return new PlatformSetup(new ScriptExecutor(dbVendor, dataSource, versionService),
                new ConfigurationServiceImpl(jdbcTemplate, transactionTemplate, dbVendor), versionService, dataSource);
    }

    private static DataSource lookupDataSource() throws NamingException {
        Context ctx = new InitialContext();
        return (DataSource) ctx.lookup(
                System.getProperty("sysprop.bonita.database.sequence.manager.datasource.name", "java:comp/env/bonitaSequenceManagerDS"));
    }

    public static ConfigurationService getConfigurationService() throws NamingException {
        final DataSource dataSource = lookupDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        final DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager(dataSource);
        TransactionTemplate transactionTemplate = new TransactionTemplate(dataSourceTransactionManager);
        return new ConfigurationServiceImpl(jdbcTemplate, transactionTemplate, System.getProperty("sysprop.bonita.db.vendor"));
    }
}
