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

import java.io.IOException;
import java.util.Properties;

import org.bonitasoft.platform.exception.PlatformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Preliminary checks done by platform setup tool before it tries to go further:
 * <ul>
 * <li>checks that the driver class if found and can be loaded</li>
 * </ul>
 *
 * @author Emmanuel Duchastenier
 */
public class ConfigurationChecker {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConfigurationChecker.class);

    private String dbVendor;

    private String driverClassName;

    private void loadProperties() throws PlatformException {
        final Properties properties = new Properties();
        try {
            properties.load(this.getClass().getResourceAsStream("/database.properties"));
        } catch (IOException e) {
            throw new PlatformException("Error reading configuration file database.properties." +
                    " Please make sure the file is present at the root of the Platform Setup Tool folder, and that is has not been moved of deleted", e);
        }
        dbVendor = properties.getProperty("db.vendor");
        final String sysPropDbVendor = System.getProperty("sysprop.bonita.db.vendor");
        if (sysPropDbVendor != null) {
            LOGGER.debug("'sysprop.bonita.db.vendor' set to '" + sysPropDbVendor + "', overriding value from file database.properties.");
            dbVendor = sysPropDbVendor;
        }
        driverClassName = properties.getProperty(dbVendor + ".driverClassName");
        if (driverClassName == null) {
            throw new PlatformException("Driver class name not set for database " + dbVendor
                    + " in file database.properties. In most cases, you should not edit the default driver class name value." +
                    " Please ensure ");
        }
    }

    public void validate() throws PlatformException {
        loadProperties();
        validateDriverClass();
    }

    void validateDriverClass() throws PlatformException {
        tryToLoadDriverClass(driverClassName);
    }

    void tryToLoadDriverClass(String driverClass) throws PlatformException {
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            throw new PlatformException("The driver class named '" + driverClass
                    + "' specified in 'database.properties' configuration file, to connect to your '" + dbVendor + "' database, cannot be found." +
                    " Either there is an error in the name of the class or the class is not available in the classpath." +
                    " Make sure the driver class name is correct and that the suitable driver is available in the lib/ folder and then try again.",
                    e);
        }
    }

}
