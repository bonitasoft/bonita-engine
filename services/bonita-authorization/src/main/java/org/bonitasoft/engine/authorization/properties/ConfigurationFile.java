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

import static java.lang.String.format;
import static org.bonitasoft.engine.authorization.properties.ConfigurationFilesManager.getCustomPropertiesFilename;
import static org.bonitasoft.engine.authorization.properties.ConfigurationFilesManager.getInternalPropertiesFilename;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

/**
 * @author Anthony Birembaut
 */
public abstract class ConfigurationFile {

    private String propertiesFilename;

    private final long tenantId;

    public ConfigurationFile(String propertiesFilename, long tenantId) {
        this.propertiesFilename = propertiesFilename;
        this.tenantId = tenantId;
    }

    abstract protected boolean hasCustomVersion();

    abstract protected boolean hasInternalVersion();

    public Properties getTenantProperties() {
        return ConfigurationFilesManager.getInstance().getTenantProperties(propertiesFilename, tenantId);
    }

    protected void setPropertiesFilename(String propertiesFilename) {
        this.propertiesFilename = propertiesFilename;
    }

    public String getProperty(final String propertyName) {
        final String propertyValue = getTenantProperties().getProperty(propertyName);
        return propertyValue != null ? propertyValue.trim() : null;
    }

    public Set<String> getPropertyAsSet(final String propertyName) {
        return PropertiesWithSet.stringToSet(getProperty(propertyName));
    }

    public void removeProperty(final String propertyName) {
        try {
            if (hasCustomVersion() || hasInternalVersion()) {
                throw new IllegalArgumentException(
                        format("File %s cannot be modified directly, as a writable version exists", propertyName));
            }
            ConfigurationFilesManager.getInstance().removeProperty(propertiesFilename, tenantId, propertyName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeCustomProperty(final String propertyName) {
        try {
            if (!hasCustomVersion()) {
                throw new IllegalArgumentException(format("File %s does not have a -custom version", propertyName));
            }
            ConfigurationFilesManager.getInstance().removeProperty(getCustomPropertiesFilename(propertiesFilename),
                    tenantId, propertyName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeInternalProperty(final String propertyName) {
        try {
            if (!hasInternalVersion()) {
                throw new IllegalArgumentException(format("File %s does not have a -internal version", propertyName));
            }
            ConfigurationFilesManager.getInstance().removeProperty(getInternalPropertiesFilename(propertiesFilename),
                    tenantId, propertyName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setProperty(final String propertyName, final String propertyValue) {
        try {
            if (hasCustomVersion() || hasInternalVersion()) {
                throw new IllegalArgumentException(
                        format("File %s cannot be modified directly, as a writable version exists", propertyName));
            }
            ConfigurationFilesManager.getInstance().setProperty(propertiesFilename, tenantId, propertyName,
                    propertyValue);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setCustomProperty(final String propertyName, final String propertyValue) {
        try {
            if (!hasCustomVersion()) {
                throw new IllegalArgumentException(format("File %s does not have a -custom version", propertyName));
            }
            ConfigurationFilesManager.getInstance().setProperty(getCustomPropertiesFilename(propertiesFilename),
                    tenantId, propertyName, propertyValue);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setInternalProperty(final String propertyName, final String propertyValue) {
        try {
            if (!hasInternalVersion()) {
                throw new IllegalArgumentException(format("File %s does not have a -internal version", propertyName));
            }
            ConfigurationFilesManager.getInstance().setProperty(getInternalPropertiesFilename(propertiesFilename),
                    tenantId, propertyName, propertyValue);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPropertyAsSet(final String property, final Set<String> permissions) {
        setProperty(property, permissions.toString());
    }

    public void setCustomPropertyAsSet(final String property, final Set<String> permissions) {
        setCustomProperty(property, permissions.toString());
    }

    public void setInternalPropertyAsSet(final String property, final Set<String> permissions) {
        setInternalProperty(property, permissions.toString());
    }
}
