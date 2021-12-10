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

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.cache.SCacheException;

/**
 * @author Anthony Birembaut
 */
@Slf4j
public abstract class ConfigurationFile {

    protected static final String CONFIGURATION_FILES_CACHE = "CONFIGURATION_FILES_CACHE";

    private final long tenantId;

    private final String propertiesFilename;

    private final String cacheKey;

    CacheService cacheService;

    ConfigurationFilesManager configurationFilesManager;

    public ConfigurationFile(long tenantId, CacheService cacheService,
            ConfigurationFilesManager configurationFilesManager) {
        this.tenantId = tenantId;
        this.propertiesFilename = getPropertiesFileName();
        this.cacheKey = tenantId + "_" + propertiesFilename;
        this.cacheService = cacheService;
        this.configurationFilesManager = configurationFilesManager;
        readPropertiesFromDatabaseAndStoreThemInCache();
    }

    private Properties readPropertiesFromDatabaseAndStoreThemInCache() {
        Properties tenantProperties = configurationFilesManager.getTenantProperties(propertiesFilename, tenantId);
        storePropertiesInCache(tenantProperties);
        return tenantProperties;
    }

    protected abstract String getPropertiesFileName();

    abstract protected boolean hasCustomVersion();

    abstract protected boolean hasInternalVersion();

    void storePropertiesInCache(Properties tenantProperties) {
        try {
            cacheService.store(CONFIGURATION_FILES_CACHE, cacheKey, tenantProperties);
        } catch (SCacheException e) {
            log.warn(format("Problem storing configuration file %s (tenant %s) in dedicated cache",
                    propertiesFilename, tenantId));
        }
    }

    Properties getTenantProperties() {
        Properties properties;
        try {
            properties = (Properties) cacheService.get(CONFIGURATION_FILES_CACHE, cacheKey);
        } catch (SCacheException e) {
            log.warn(format("Problem retrieving configuration file %s (tenant %s) from dedicated cache",
                    propertiesFilename, tenantId));
            return new Properties(); // Should we return null?
        }
        if (properties == null) {
            properties = readPropertiesFromDatabaseAndStoreThemInCache();
        }
        return properties;
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
            final Properties tenantProperties = getTenantProperties();
            if (tenantProperties.remove(propertyName) != null) { // if the property was present
                storePropertiesInCache(tenantProperties);
                configurationFilesManager.removeProperty(propertiesFilename, tenantId, propertyName);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeCustomProperty(final String propertyName) {
        try {
            if (!hasCustomVersion()) {
                throw new IllegalArgumentException(format("File %s does not have a -custom version", propertyName));
            }
            final Properties tenantProperties = getTenantProperties();
            // FIXME: is there a risk to remove a property that is not custom, here?
            if (tenantProperties.remove(propertyName) != null) { // if the property was present
                storePropertiesInCache(tenantProperties);
                configurationFilesManager.removeProperty(getCustomPropertiesFilename(propertiesFilename),
                        tenantId, propertyName);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeInternalProperty(final String propertyName) {
        try {
            if (!hasInternalVersion()) {
                throw new IllegalArgumentException(format("File %s does not have a -internal version", propertyName));
            }
            final Properties tenantProperties = getTenantProperties();
            // FIXME: is there a risk to remove a property that is not custom, here?
            if (tenantProperties.remove(propertyName) != null) { // if the property was present
                storePropertiesInCache(tenantProperties);
                configurationFilesManager.removeProperty(getInternalPropertiesFilename(propertiesFilename),
                        tenantId, propertyName);
            }
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
            final Properties tenantProperties = getTenantProperties();
            tenantProperties.setProperty(propertyName, propertyValue);
            storePropertiesInCache(tenantProperties);
            configurationFilesManager.setProperty(propertiesFilename, tenantId, propertyName,
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
            final Properties tenantProperties = getTenantProperties();
            tenantProperties.setProperty(propertyName, propertyValue);
            storePropertiesInCache(tenantProperties);
            configurationFilesManager.setProperty(getCustomPropertiesFilename(propertiesFilename),
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
            final Properties tenantProperties = getTenantProperties();
            tenantProperties.setProperty(propertyName, propertyValue);
            storePropertiesInCache(tenantProperties);
            configurationFilesManager.setProperty(getInternalPropertiesFilename(propertiesFilename),
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
