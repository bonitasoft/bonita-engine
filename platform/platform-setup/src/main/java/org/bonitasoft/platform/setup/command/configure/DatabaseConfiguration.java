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
package org.bonitasoft.platform.setup.command.configure;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.bonitasoft.platform.exception.PlatformException;

/**
 * @author Emmanuel Duchastenier
 */
public class DatabaseConfiguration {

    public static final String H2_DB_VENDOR = "h2";
    private static final String H2_DATABASE_DIR = "${h2.database.dir}";

    private String dbVendor;
    private String nonXaDriverClassName;
    private String xaDriverClassName;
    private String xaDataSourceFactory;
    private String databaseUser;
    private String databasePassword;
    private String databaseName;
    private String serverName;
    private String serverPort;
    private String url;
    private String testQuery;
    private PropertyReader propertyReader;

    public DatabaseConfiguration(String prefix, Properties properties, Path rootPath) throws PlatformException {
        propertyReader = new PropertyReader(properties);
        dbVendor = getMandatoryProperty(prefix + "db.vendor");

        nonXaDriverClassName = getMandatoryProperty(dbVendor + ".nonXaDriver");
        xaDriverClassName = getMandatoryProperty(dbVendor + ".xaDriver");
        xaDataSourceFactory = getMandatoryProperty(dbVendor + ".xaDSFactory");
        databaseName = getMandatoryProperty(prefix + "db.database.name");
        url = getMandatoryProperty(dbVendor + "." + prefix + "url");
        // Configuration for H2 is a little different from other DB vendors:
        if (H2_DB_VENDOR.equals(dbVendor)) {
            String h2DatabaseDir = getMandatoryProperty("h2.database.dir");
            Path h2DatabasePath = Paths.get(h2DatabaseDir);
            if (h2DatabasePath.isAbsolute() || h2DatabaseDir.startsWith("${")) {
                url = url.replace(H2_DATABASE_DIR, h2DatabasePath.normalize().toString());
            } else {
                // generate absolute path
                url = url.replace(H2_DATABASE_DIR,
                        rootPath.resolve("setup").resolve(h2DatabaseDir).toAbsolutePath().normalize().toString());
            }
        } else {
            serverName = getMandatoryProperty(prefix + "db.server.name");
            url = url.replace("${" + prefix + "db.server.name}", serverName);
            serverPort = getMandatoryProperty(prefix + "db.server.port");
            url = url.replace("${" + prefix + "db.server.port}", serverPort);
        }
        url = url.replace("${" + prefix + "db.database.name}", databaseName);
        databaseUser = getMandatoryProperty(prefix + "db.user");
        databasePassword = getMandatoryProperty(prefix + "db.password");
        testQuery = getMandatoryProperty(dbVendor + "." + prefix + "testQuery");
    }

    public String getDbVendor() {
        return dbVendor;
    }

    public String getNonXaDriverClassName() {
        return nonXaDriverClassName;
    }

    String getXaDriverClassName() {
        return xaDriverClassName;
    }

    public String getXaDataSourceFactory() {
        return xaDataSourceFactory;
    }

    String getDatabaseUser() {
        return databaseUser;
    }

    String getDatabasePassword() {
        return databasePassword;
    }

    String getDatabaseName() {
        return databaseName;
    }

    String getServerName() {
        return serverName != null ? serverName : "";
    }

    String getServerPort() {
        return serverPort != null ? serverPort : "";
    }

    String getUrl() {
        return url;
    }

    String getTestQuery() {
        return testQuery;
    }

    private String getMandatoryProperty(String s) throws PlatformException {
        return propertyReader.getPropertyAndFailIfNull(s);
    }

}
