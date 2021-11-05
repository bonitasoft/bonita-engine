/**
 * Copyright (C) 2021 Bonitasoft S.A.
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
package org.bonitasoft.engine.authorization.properties;

import static org.bonitasoft.engine.authorization.properties.PropertiesWithSet.stringToSet;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

/**
 * @author Anthony Birembaut
 */
public class ConfigurationFile {

    private final String propertiesFilename;
    private final long tenantId;

    public ConfigurationFile(String propertiesFilename, long tenantId) {
        this.propertiesFilename = propertiesFilename;
        this.tenantId = tenantId;
    }

    public String getProperty(final String propertyName) {
        final String propertyValue = getTenantProperties().getProperty(propertyName);
        return propertyValue != null ? propertyValue.trim() : null;
    }

    public Properties getTenantProperties() {
        return ConfigurationFilesManager.getInstance().getTenantProperties(propertiesFilename, tenantId);
    }

    public void removeProperty(final String propertyName) {
        try {
            ConfigurationFilesManager.getInstance().removeProperty(propertiesFilename, tenantId, propertyName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setProperty(final String propertyName, final String propertyValue) {
        try {
            ConfigurationFilesManager.getInstance().setProperty(propertiesFilename, tenantId, propertyName,
                    propertyValue);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<String> getPropertyAsSet(final String propertyName) {
        final String propertyAsString = getProperty(propertyName);
        return stringToSet(propertyAsString);
    }

    public void setPropertyAsSet(final String property, final Set<String> permissions) {
        setProperty(property, permissions.toString());
    }
}
