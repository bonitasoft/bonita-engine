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

import java.nio.file.Paths;
import java.util.Properties;

import org.bonitasoft.platform.exception.PlatformException;
import org.bonitasoft.platform.setup.command.configure.DatabaseConfiguration;

/**
 * Preliminary checks done by platform setup tool before it tries to go further:
 * <ul>
 * <li>checks that all mandatory properties are set into database.properties</li>
 * <li>checks that the driver class if found and can be loaded</li>
 * </ul>
 *
 * @author Emmanuel Duchastenier
 */
class ConfigurationChecker {

    private Properties datasourceProperties;

    private String driverClassName;
    private DatabaseConfiguration dbConfiguration;

    ConfigurationChecker(Properties datasourceProperties) {
        this.datasourceProperties = datasourceProperties;
    }

    void loadProperties() throws PlatformException {
        dbConfiguration = new DatabaseConfiguration("", datasourceProperties, Paths.get("."));
        driverClassName = dbConfiguration.getNonXaDriverClassName();
    }

    public void validate() throws PlatformException {
        loadProperties();
        tryToLoadDriverClass();
    }

    void tryToLoadDriverClass() throws PlatformException {
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            throw new PlatformException("The driver class named '" + driverClassName
                    + "' specified in 'internal.properties' configuration file, to connect to your '" + dbConfiguration.getDbVendor()
                    + "' database, cannot be found." +
                    " Either there is an error in the name of the class or the class is not available in the classpath." +
                    " Make sure the driver class name is correct and that the suitable driver is available in the lib/ folder and then try again.",
                    e);
        }
    }

}
