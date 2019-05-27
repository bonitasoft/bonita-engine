package org.bonitasoft.engine.test;

import static org.bonitasoft.engine.DefaultBonitaDatabaseConfigurations.defaultConfiguration;

import org.bonitasoft.engine.BonitaDatabaseConfiguration;
import org.bonitasoft.engine.BonitaEngine;

import lombok.extern.slf4j.Slf4j;

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
        String dbVendor = System.getProperty(BonitaEngine.BONITA_DB_VENDOR, System.getProperty("db.vendor"));
        if (nullOrEmpty(dbVendor)) {
            log.info("System property '{}' or 'db.vendor' is not set, will use h2", BonitaEngine.BONITA_DB_VENDOR);
            dbVendor = "h2";
        }
        String url = System.getProperty("db.url");
        String driverClass = System.getProperty("db.driverClass");
        String user = System.getProperty("db.user");
        String password = System.getProperty("db.password");

        BonitaDatabaseConfiguration.BonitaDatabaseConfigurationBuilder builder = BonitaDatabaseConfiguration.builder();
        builder.dbVendor(dbVendor);
        builder.url(nullOrEmpty(url) ? defaultConfiguration(dbVendor).getUrl() : url);
        if (!nullOrEmpty(driverClass)) {
            builder.driverClassName(driverClass);
        }
        builder.user(nullOrEmpty(user) ? defaultConfiguration(dbVendor).getUser() : user);
        builder.password(password == null ? defaultConfiguration(dbVendor).getPassword() : password);
        return builder.build();
    }

    private static boolean nullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

}
