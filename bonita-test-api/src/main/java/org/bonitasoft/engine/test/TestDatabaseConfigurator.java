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
package org.bonitasoft.engine.test;

import static org.bonitasoft.engine.BonitaEngine.BONITA_BDM_DB_VENDOR;
import static org.bonitasoft.engine.BonitaEngine.BONITA_DB_VENDOR;
import static org.bonitasoft.engine.DefaultBonitaDatabaseConfigurations.defaultConfiguration;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.BonitaDatabaseConfiguration;

@Slf4j
class TestDatabaseConfigurator {

    /**
     * construct a database configuration bases on system properties
     * sysprop.bonita.db.vendor
     * db.vendor
     * db.url
     * db.driverClass
     * db.user
     * db.password
     * If some properties are not set use the default one from the db vendor
     */
    BonitaDatabaseConfiguration getDatabaseConfiguration() {
        String dbVendor = System.getProperty(BONITA_DB_VENDOR, System.getProperty("db.vendor"));
        if (nullOrEmpty(dbVendor)) {
            log.info("System property '{}' or 'db.vendor' is not set, will use h2", BONITA_DB_VENDOR);
            dbVendor = "h2";
        }
        String url = System.getProperty("db.url");
        String driverClass = System.getProperty("db.driverClass");
        String user = System.getProperty("db.user");
        String password = System.getProperty("db.password");

        BonitaDatabaseConfiguration.BonitaDatabaseConfigurationBuilder builder = BonitaDatabaseConfiguration.builder();
        builder.dbVendor(dbVendor);
        BonitaDatabaseConfiguration defaultConfig = defaultConfiguration(dbVendor, "bonita");
        builder.url(nullOrEmpty(url) ? defaultConfig.getUrl() : url);
        if (!nullOrEmpty(driverClass)) {
            builder.driverClassName(driverClass);
        }
        builder.user(nullOrEmpty(user) ? defaultConfig.getUser() : user);
        builder.password(password == null ? defaultConfig.getPassword() : password);
        return builder.build();
    }

    BonitaDatabaseConfiguration getBDMDatabaseConfiguration() {
        String dbVendor = System.getProperty(BONITA_BDM_DB_VENDOR, System.getProperty("bdm.db.vendor",
                System.getProperty(BONITA_DB_VENDOR, System.getProperty("db.vendor"))));
        if (nullOrEmpty(dbVendor)) {
            log.info("System property '{}' or 'db.vendor' is not set, will use h2", BONITA_DB_VENDOR);
            dbVendor = "h2";
        }
        String url = System.getProperty("bdm.db.url", System.getProperty("db.url"));
        String driverClass = System.getProperty("bdm.db.driverClass", System.getProperty("db.driverClass"));
        String user = System.getProperty("bdm.db.user", System.getProperty("db.user"));
        String password = System.getProperty("bdm.db.password", System.getProperty("db.password"));

        BonitaDatabaseConfiguration.BonitaDatabaseConfigurationBuilder builder = BonitaDatabaseConfiguration.builder();
        builder.dbVendor(dbVendor);
        BonitaDatabaseConfiguration defaultConfig = defaultConfiguration(dbVendor, "business_data");
        builder.url(nullOrEmpty(url) ? defaultConfig.getUrl() : url);
        if (!nullOrEmpty(driverClass)) {
            builder.driverClassName(driverClass);
        }
        builder.user(nullOrEmpty(user) ? defaultConfig.getUser() : user);
        builder.password(password == null ? defaultConfig.getPassword() : password);
        return builder.build();
    }

    private static boolean nullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

}
