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

import lombok.Getter;
import org.bonitasoft.platform.exception.PlatformException;

/**
 * @author Emmanuel Duchastenier
 */
public class DatabaseConfiguration {

    public static final String H2_DB_VENDOR = "h2";
    private static final String H2_DATABASE_DIR = "${h2.database.dir}";

    @Getter
    private String dbVendor;
    @Getter
    private String nonXaDriverClassName;
    @Getter
    private String xaDriverClassName;
    @Getter
    private String xaDataSourceFactory;
    @Getter
    private String databaseUser;
    @Getter
    private String databasePassword;
    @Getter
    private String databaseName;
    @Getter
    private String serverName = "";
    @Getter
    private String serverPort = "";
    @Getter
    private String url;
    @Getter
    private String testQuery;
    @Getter
    private Integer connectionPoolInitialSize;
    @Getter
    private Integer connectionPoolMaxTotal;
    @Getter
    private Integer connectionPoolMaxIdle;
    @Getter
    private Integer connectionPoolMinIdle;
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
            // h2 path on windows must have forward slashes (H2 convention):
            url = url.replace("\\", "/");
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
        connectionPoolInitialSize = getMandatoryIntegerProperty(prefix + "connection-pool.initialSize");
        connectionPoolMaxTotal = getMandatoryIntegerProperty(prefix + "connection-pool.maxTotal");
        connectionPoolMaxIdle = getMandatoryIntegerProperty(prefix + "connection-pool.maxIdle");
        connectionPoolMinIdle = getMandatoryIntegerProperty(prefix + "connection-pool.minIdle");
    }

    private String getMandatoryProperty(String s) throws PlatformException {
        return propertyReader.getPropertyAndFailIfNull(s);
    }

    private Integer getMandatoryIntegerProperty(String s) throws PlatformException {
        var value = propertyReader.getPropertyAndFailIfNull(s);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new PlatformException(String.format("Invalid integer value '%s' for property '%s'", value, s));
        }
    }

}
