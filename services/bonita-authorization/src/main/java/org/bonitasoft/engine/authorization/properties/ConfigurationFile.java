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

    private final String propertiesFilename;

    protected final String cacheKey;

    protected boolean setKeysToLowerCase;

    CacheService cacheService;

    ConfigurationFilesManager configurationFilesManager;

    public ConfigurationFile(CacheService cacheService, ConfigurationFilesManager configurationFilesManager) {
        this(cacheService, configurationFilesManager, false);
    }

    public ConfigurationFile(CacheService cacheService,
            ConfigurationFilesManager configurationFilesManager, boolean setKeysToLowerCase) {
        this.propertiesFilename = getPropertiesFileName();
        this.cacheKey = propertiesFilename;
        this.cacheService = cacheService;
        this.configurationFilesManager = configurationFilesManager;
        this.setKeysToLowerCase = setKeysToLowerCase;
    }

    protected Properties readPropertiesAndStoreThemInCache() {
        return readPropertiesFromDatabaseAndStoreThemInCache();
    }

    protected Properties readPropertiesFromDatabaseAndStoreThemInCache() {
        Properties properties = configurationFilesManager.getTenantProperties(propertiesFilename,
                setKeysToLowerCase);
        storePropertiesInCache(properties);
        return properties;
    }

    protected Properties readPropertiesFromClasspathAndStoreThemInCache() {
        //Read properties from classpath
        Properties classpathProperties = new Properties();
        try {
            classpathProperties.load(ConfigurationFile.class.getResourceAsStream(getPropertiesFileName()));
            storePropertiesInCache(classpathProperties);
        } catch (IOException e) {
            log.error("Cannot retrieve dynamic authorizations", e);
        }
        return classpathProperties;
    }

    protected abstract String getPropertiesFileName();

    abstract protected boolean hasCustomVersion();

    abstract protected boolean hasInternalVersion();

    void storePropertiesInCache(Properties properties) {
        try {
            cacheService.store(CONFIGURATION_FILES_CACHE, cacheKey, properties);
            log.debug("Successfully stored configuration file {} in dedicated cache", propertiesFilename);
        } catch (SCacheException e) {
            log.warn("Problem storing configuration file {} in dedicated cache ({})", propertiesFilename,
                    e.getMessage());
        }
    }

    Properties getProperties() {
        Properties properties;
        try {
            properties = (Properties) cacheService.get(CONFIGURATION_FILES_CACHE, cacheKey);
        } catch (SCacheException e) {
            log.warn("Problem retrieving configuration file {} from dedicated cache", propertiesFilename);
            return new Properties(); // Should we return null?
        }
        if (properties == null) {
            properties = readPropertiesAndStoreThemInCache();
        }
        return properties;
    }

    public String getProperty(final String propertyName) {
        final String propertyValue = getProperties().getProperty(propertyName);
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
            final Properties properties = getProperties();
            if (properties.remove(propertyName) != null) { // if the property was present
                storePropertiesInCache(properties);
                configurationFilesManager.removeProperty(propertiesFilename, propertyName);
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
            final Properties properties = getProperties();
            // FIXME: is there a risk to remove a property that is not custom, here?
            if (properties.remove(propertyName) != null) { // if the property was present
                storePropertiesInCache(properties);
                configurationFilesManager.removeProperty(getCustomPropertiesFilename(propertiesFilename),
                        propertyName);
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
            final Properties properties = getProperties();
            // FIXME: is there a risk to remove a property that is not internal, here?
            if (properties.remove(propertyName) != null) { // if the property was present
                storePropertiesInCache(properties);
                configurationFilesManager.removeProperty(getInternalPropertiesFilename(propertiesFilename),
                        propertyName);
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
            final Properties properties = getProperties();
            properties.setProperty(propertyName, propertyValue);
            storePropertiesInCache(properties);
            configurationFilesManager.setProperty(propertiesFilename, propertyName,
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
            final Properties properties = getProperties();
            properties.setProperty(propertyName, propertyValue);
            storePropertiesInCache(properties);
            configurationFilesManager.setProperty(getCustomPropertiesFilename(propertiesFilename),
                    propertyName, propertyValue);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setInternalProperty(final String propertyName, final String propertyValue) {
        try {
            if (!hasInternalVersion()) {
                throw new IllegalArgumentException(format("File %s does not have a -internal version", propertyName));
            }
            final Properties properties = getProperties();
            properties.setProperty(propertyName, propertyValue);
            storePropertiesInCache(properties);
            configurationFilesManager.setProperty(getInternalPropertiesFilename(propertiesFilename),
                    propertyName, propertyValue);
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
