/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.console.common.server.preferences.properties;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.console.common.server.preferences.constants.WebBonitaConstantsUtils;
import org.bonitasoft.console.common.server.utils.PlatformManagementUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Baptiste Mesta, Emmanuel Duchastenier, Anthony Birembaut
 */
public class ConfigurationFilesManager {

    private static final ConfigurationFilesManager INSTANCE = new ConfigurationFilesManager();

    public static ConfigurationFilesManager getInstance() {
        return INSTANCE;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationFilesManager.class.getName());

    private final Map<String, Properties> tenantConfigurations = new HashMap<>();

    private final Map<String, File> tenantConfigurationFiles = new HashMap<>();

    private Map<String, Properties> platformConfigurations = new HashMap<>();

    private final Map<String, File> platformConfigurationFiles = new HashMap<>();

    public Properties getPlatformProperties(String propertiesFile) {
        Properties properties = platformConfigurations.get(propertiesFile);
        if (properties == null) {
            return new Properties();
        }
        return properties;
    }

    Properties getAlsoCustomAndInternalPropertiesFromFilename(String propertiesFileName) {
        Properties properties = new Properties();
        Properties tenantConfiguration = getTenantConfiguration(propertiesFileName);
        if (tenantConfiguration != null) {
            properties.putAll(tenantConfiguration);
            // if -internal properties also exists, merge key/value pairs:
            final String internalPropertyFilename = getSuffixedPropertyFilename(propertiesFileName, "-internal");
            final Properties internalConfiguration = getTenantConfiguration(internalPropertyFilename);
            if (internalConfiguration != null) {
                properties.putAll(internalConfiguration);
            }
            // if -custom properties also exists, merge key/value pairs (and overwrite previous values if same key name):
            final String customPropertyFilename = getSuffixedPropertyFilename(propertiesFileName, "-custom");
            final Properties customConfiguration = getTenantConfiguration(customPropertyFilename);
            if (customConfiguration != null) {
                properties.putAll(customConfiguration);
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("File " + propertiesFileName + " not found. Returning empty properties object.");
            }
        }
        return properties;
    }

    public Properties getTenantProperties(String propertiesFileName) {
        return getAlsoCustomAndInternalPropertiesFromFilename(propertiesFileName);
    }

    /**
     * Parses the content as a Properties object.
     * If content is null, return empty properties.
     */
    public static Properties getProperties(byte[] content) {
        Properties properties = new Properties();
        if (content != null) {
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(content)) {
                properties.load(inputStream);
            } catch (IOException ioe) {
                LOGGER.error("Cannot parse properties file content", ioe);
            }
        }
        return properties;
    }

    public void setPlatformConfigurations(Map<String, byte[]> configurationFiles) throws IOException {
        platformConfigurations = new HashMap<>(configurationFiles.size());
        for (Map.Entry<String, byte[]> entry : configurationFiles.entrySet()) {
            if (entry.getKey().endsWith(".properties")) {
                platformConfigurations.put(entry.getKey(), getProperties(entry.getValue()));
            } else {
                File file = new File(WebBonitaConstantsUtils.getPlatformInstance().getTempFolder(), entry.getKey());
                FileUtils.writeByteArrayToFile(file, entry.getValue());
                platformConfigurationFiles.put(entry.getKey(), file);
            }
        }
    }

    public synchronized void setTenantConfigurationFiles(Map<String, byte[]> configurationFiles) throws IOException {
        for (Map.Entry<String, byte[]> entry : configurationFiles.entrySet()) {
            if (!entry.getKey().endsWith(".properties")) {
                File file = new File(WebBonitaConstantsUtils.getTenantInstance().getTempFolder(), entry.getKey());
                FileUtils.writeByteArrayToFile(file, entry.getValue());
                tenantConfigurationFiles.put(entry.getKey(), file);
            }
            tenantConfigurations.put(entry.getKey(),
                    ConfigurationFilesManager.getProperties(entry.getValue()));
        }
    }

    private String getSuffixedPropertyFilename(String propertiesFilename, String suffix) {
        return propertiesFilename.replaceAll("\\.properties$", suffix + ".properties");
    }

    PlatformManagementUtils getPlatformManagementUtils() {
        return new PlatformManagementUtils();
    }

    public File getTenantConfigurationFile(String fileName) {
        if (tenantConfigurationFiles.isEmpty()) {
            try {
                setTenantConfigurationFiles(getPlatformManagementUtils().readTenantConfigurationsFromEngine());
            } catch (IOException e) {
                LOGGER.error("Cannot retrieve tenant configuration files", e);
                throw new RuntimeException(e);
            }
        }
        return tenantConfigurationFiles.get(fileName);
    }

    Properties getTenantConfiguration(String propertiesFilename) {
        if (tenantConfigurations.isEmpty()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Tenant configuration file {} not yet in cache. Adding it.", propertiesFilename);
            }
            try {
                setTenantConfigurationFiles(getPlatformManagementUtils().readTenantConfigurationsFromEngine());
            } catch (IOException e) {
                LOGGER.error("Cannot retrieve tenant configuration", e);
                throw new RuntimeException(e);
            }
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Retrieving tenant configuration file {} from cache.", propertiesFilename);
        }
        return tenantConfigurations.get(propertiesFilename);
    }

    public File getPlatformConfigurationFile(String fileName) {
        return platformConfigurationFiles.get(fileName);
    }

}
