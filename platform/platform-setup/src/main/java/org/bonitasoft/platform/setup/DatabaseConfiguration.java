/*
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
 */
package org.bonitasoft.platform.setup;

import java.util.Properties;

import org.bonitasoft.platform.exception.PlatformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Emmanuel Duchastenier
 */
public class DatabaseConfiguration {

    // Use BundleConfigurator logger for easier configuration (no need for a specific logger here):
    private final static Logger LOGGER = LoggerFactory.getLogger(BundleConfigurator.class);

    static final String H2_DB_VENDOR = "h2";

    private String dbVendor;
    private String nonXaDriverClassName;
    private String xaDriverClassName;
    private String databaseUser;
    private String databasePassword;
    private String databaseName;
    private String serverName;
    private String serverPort;
    private String url;
    private String testQuery;

    DatabaseConfiguration(String prefix, Properties properties) throws PlatformException {
        dbVendor = getPropertyAndFailIfNull(properties, prefix + "db.vendor");
        // No need to configure anything for H2:
        if (H2_DB_VENDOR.equals(dbVendor)) {
            return;
        }
        nonXaDriverClassName = getPropertyAndFailIfNull(properties, dbVendor + ".nonXaDriver");
        xaDriverClassName = getPropertyAndFailIfNull(properties, dbVendor + ".xaDriver");
        serverName = getPropertyAndFailIfNull(properties, prefix + "db.server.name");
        serverPort = getPropertyAndFailIfNull(properties, prefix + "db.server.port");
        databaseName = getPropertyAndFailIfNull(properties, prefix + "db.database.name");
        url = getPropertyAndFailIfNull(properties, dbVendor + "." + prefix + "url");
        url = url.replace("${" + prefix + "db.server.name}", serverName);
        url = url.replace("${" + prefix + "db.server.port}", serverPort);
        url = url.replace("${" + prefix + "db.database.name}", databaseName);
        databaseUser = getPropertyAndFailIfNull(properties, prefix + "db.user");
        databasePassword = getPropertyAndFailIfNull(properties, prefix + "db.password");
        testQuery = getPropertyAndFailIfNull(properties, dbVendor + "." + prefix + "testQuery");
    }

    public String getDbVendor() {
        return dbVendor;
    }

    public void setDbVendor(String dbVendor) {
        this.dbVendor = dbVendor;
    }

    public String getNonXaDriverClassName() {
        return nonXaDriverClassName;
    }

    public String getXaDriverClassName() {
        return xaDriverClassName;
    }

    public String getDatabaseUser() {
        return databaseUser;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getServerName() {
        return serverName;
    }

    public String getServerPort() {
        return serverPort;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTestQuery() {
        return testQuery;
    }

    private String getPropertyAndFailIfNull(Properties properties, String propertyName) throws PlatformException {
        // Any property value can be overridden by system property with the same name:
        final String sysPropValue = System.getProperty(propertyName);
        if (sysPropValue != null) {
            LOGGER.info("System property '" + propertyName + "' set to '" + sysPropValue + "', overriding value from file database.properties.");
            return sysPropValue;
        }

        final String property = properties.getProperty(propertyName);
        if (property == null) {
            throw new PlatformException(
                    "Mandatory property '" + propertyName + "' is missing. Ensure you did not remove lines from file 'database.properties'");
        }
        return property;
    }

}
