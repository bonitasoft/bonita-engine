/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine;

import static org.bonitasoft.engine.xa.XADataSourceIsSameRMOverride.overrideSameRM;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.managed.BasicManagedDataSource;

class BonitaDataSourceInitializer {

    private static final String H2 = "h2";
    private static final String MYSQL = "mysql";
    private static final String ORACLE = "oracle";
    private static final String POSTGRES = "postgres";
    private static final String SQLSERVER = "sqlserver";

    private static final Map<String, String> defaultDriver;
    private static final Map<String, String> defaultXADataSourceNames;
    private static final Map<String, String> defaultXADataSourceFactories;
    private static final Map<String, String> defaultTestQueries;

    static {
        HashMap<String, String> drivers = new HashMap<>();
        drivers.put(H2, "org.h2.Driver");
        drivers.put(MYSQL, "com.mysql.cj.jdbc.Driver");
        drivers.put(ORACLE, "oracle.jdbc.OracleDriver");
        drivers.put(POSTGRES, "org.postgresql.Driver");
        drivers.put(SQLSERVER, "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        defaultDriver = Collections.unmodifiableMap(drivers);
        HashMap<String, String> xaDataSourceNames = new HashMap<>();
        xaDataSourceNames.put(H2, "org.h2.jdbcx.JdbcDataSource");
        xaDataSourceNames.put(MYSQL, "com.mysql.cj.jdbc.MysqlXADataSource");
        xaDataSourceNames.put(ORACLE, "oracle.jdbc.xa.client.OracleXADataSource");
        xaDataSourceNames.put(POSTGRES, "org.postgresql.xa.PGXADataSource");
        xaDataSourceNames.put(SQLSERVER, "com.microsoft.sqlserver.jdbc.SQLServerXADataSource");
        defaultXADataSourceNames = Collections.unmodifiableMap(xaDataSourceNames);
        HashMap<String, String> xaDataSourceFactories = new HashMap<>();
        xaDataSourceFactories.put(H2, "org.h2.jdbcx.JdbcDataSourceFactory");
        xaDataSourceFactories.put(MYSQL, "com.mysql.cj.jdbc.MysqlDataSourceFactory");
        xaDataSourceFactories.put(ORACLE, "oracle.jdbc.pool.OracleDataSourceFactory");
        xaDataSourceFactories.put(POSTGRES, "org.postgresql.xa.PGXADataSourceFactory");
        xaDataSourceFactories.put(SQLSERVER, "com.microsoft.sqlserver.jdbc.SQLServerDataSourceObjectFactory");
        defaultXADataSourceFactories = Collections.unmodifiableMap(xaDataSourceFactories);
        HashMap<String, String> testQueries = new HashMap<>();
        testQueries.put(H2, "SELECT 1");
        testQueries.put(MYSQL, "SELECT 1");
        testQueries.put(ORACLE, "SELECT 1 FROM DUAL");
        testQueries.put(POSTGRES, "SELECT 1");
        testQueries.put(SQLSERVER, "SELECT 1");
        defaultTestQueries = Collections.unmodifiableMap(testQueries);
    }

    BasicManagedDataSource createManagedDataSource(BonitaDatabaseConfiguration configuration,
            TransactionManager transactionManager) throws Exception {
        validate(configuration);
        String dbVendor = configuration.getDbVendor();
        String xaDatasourceClass = defaultXADataSourceNames.get(dbVendor);
        String xaDatasourceFactoryClass = defaultXADataSourceFactories.get(dbVendor);
        //create a 'native' xa datasources

        ObjectFactory datasourceFactory = (ObjectFactory) Class.forName(xaDatasourceFactoryClass).getConstructor()
                .newInstance();
        Reference reference = new Reference(xaDatasourceClass);
        String description = "RawDataSource of " + dbVendor;
        reference.add(new StringRefAddr("description", description));
        reference.add(new StringRefAddr("closeMethod", "close"));
        reference.add(new StringRefAddr("loginTimeout", "0"));
        if (dbVendor.equals(POSTGRES)) {
            DatabaseUrlParser.DatabaseMetadata metadata = DatabaseUrlParser.parsePostgresUrl(configuration.getUrl());
            reference.add(new StringRefAddr("serverName", metadata.getServerName()));
            reference.add(new StringRefAddr("portNumber", metadata.getPort()));
            reference.add(new StringRefAddr("databaseName", metadata.getDatabaseName()));
        } else if (dbVendor.equals(SQLSERVER)) {
            reference.add(new StringRefAddr("dataSourceURL", configuration.getUrl()));
            reference.add(new StringRefAddr("dataSourceDescription", description));
            reference.add(new StringRefAddr("class", xaDatasourceClass));
        } else {
            reference.add(new StringRefAddr("explicitUrl", "true"));
            reference.add(new StringRefAddr("url", configuration.getUrl()));
        }
        reference.add(new StringRefAddr("user", configuration.getUser()));
        reference.add(new StringRefAddr("password", configuration.getPassword()));

        XADataSource xaDataSource = (XADataSource) datasourceFactory.getObjectInstance(reference, null, null, null);

        BasicManagedDataSource bonitaDataSource = new BasicManagedDataSource();
        bonitaDataSource.setDefaultAutoCommit(false);
        bonitaDataSource.setRemoveAbandonedOnBorrow(false);
        bonitaDataSource.setRemoveAbandonedOnMaintenance(false);
        bonitaDataSource.setLogAbandoned(false);
        bonitaDataSource.setTestOnBorrow(true);
        bonitaDataSource.setValidationQuery(defaultTestQueries.get(dbVendor));
        bonitaDataSource.setTransactionManager(transactionManager);
        bonitaDataSource.setInitialSize(1);
        bonitaDataSource.setTestWhileIdle(false);
        bonitaDataSource.setTimeBetweenEvictionRunsMillis(60000);
        bonitaDataSource.setMinEvictableIdleTimeMillis(600000);
        if (dbVendor.equals(ORACLE)) {
            bonitaDataSource.setXaDataSourceInstance(overrideSameRM(xaDataSource));
        } else {
            bonitaDataSource.setXaDataSourceInstance(xaDataSource);

        }
        configureDatasource(configuration.getXaDatasource(), bonitaDataSource);
        return bonitaDataSource;
    }

    private String getDriverClassName(BonitaDatabaseConfiguration configuration, String driver) {
        String driverClassName;
        if (driver == null || driver.isEmpty()) {
            driverClassName = defaultDriver.get(configuration.getDbVendor());
        } else {
            driverClassName = driver;
        }
        return driverClassName;
    }

    private void validate(BonitaDatabaseConfiguration configuration) {
        checkNullOrEmpty(configuration.getDbVendor(), "dbVendor");
        if (!defaultDriver.containsKey(configuration.getDbVendor())) {
            throw new IllegalArgumentException(String.format("Database db vendor %s is invalid ( should be one of %s )",
                    configuration.getDbVendor(), defaultDriver.keySet()));
        }
        checkNullOrEmpty(configuration.getUrl(), "url");
        checkNullOrEmpty(configuration.getUser(), "user");
    }

    private static void checkNullOrEmpty(String field, String fieldName) {
        if (field == null || field.isEmpty()) {
            throw new IllegalArgumentException("Database " + fieldName + " not set");
        }
    }

    BasicDataSource createDataSource(BonitaDatabaseConfiguration configuration) {
        validate(configuration);
        BasicDataSource dataSource = new BasicDataSource();
        configureDatasource(configuration.getDatasource(), dataSource);
        dataSource.setInitialSize(1);
        dataSource.setDriverClassName(getDriverClassName(configuration, configuration.getDriverClassName()));
        dataSource.setUrl(configuration.getUrl());
        dataSource.setUsername(configuration.getUser());
        dataSource.setPassword(configuration.getPassword());
        return dataSource;
    }

    private void configureDatasource(DatasourceConfiguration configuration, BasicDataSource dataSource) {
        if (configuration != null && configuration.getMaxPoolSize() > 0) {
            dataSource.setMaxTotal(configuration.getMaxPoolSize());
        } else {
            dataSource.setMaxTotal(7);
        }
    }
}
