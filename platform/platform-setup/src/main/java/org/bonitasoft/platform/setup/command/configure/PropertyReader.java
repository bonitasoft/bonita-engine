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

import java.util.Properties;

import org.bonitasoft.platform.exception.PlatformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Emmanuel Duchastenier
 */
public class PropertyReader {

    // Use BundleConfigurator logger for easier configuration (no need for a specific logger here):
    private final static Logger LOGGER = LoggerFactory.getLogger(BundleConfigurator.class);

    private final Properties properties;

    public PropertyReader(Properties properties) {
        this.properties = properties;
    }

    public String getPropertyAndFailIfNull(String propertyName) throws PlatformException {
        // Any property value can be overridden by system property with the same name:
        final String sysPropValue = System.getProperty(propertyName);
        if (sysPropValue != null) {
            LOGGER.info("System property '" + propertyName + "' set to '" + sysPropValue + "', overriding value from file database.properties.");
            return sysPropValue;
        }

        final String property = properties.getProperty(propertyName);
        if (property == null) {
            throw new PlatformException(
                    "Mandatory property '" + propertyName + "' is missing." +
                            " Ensure you did not remove lines from file 'database.properties' (neither from file 'internal.properties')"
                            + " and that the line is NOT commented out with a '#' character at start of line.");
        }
        return property;
    }

}
