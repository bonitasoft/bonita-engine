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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.springframework.stereotype.Component;

/**
 * @author Baptiste Mesta
 * @author Emmanuel Duchastenier
 * @author Anthony Birembaut
 */
@Slf4j
@Component
public class ConfigurationFilesManager {

    protected Properties getAlsoCustomAndInternalPropertiesFromFilename(long tenantId, String propertiesFileName,
            boolean setKeysToLowerCase) {
        Properties properties = new Properties();
        try {
            final Map<String, Properties> propertiesByFilename = getTenantConfigurations(tenantId);
            if (propertiesByFilename.containsKey(propertiesFileName)) {
                properties.putAll(propertiesKeysToLowerCaseIfNeeded(propertiesByFilename.get(propertiesFileName),
                        setKeysToLowerCase));
                // if -internal properties also exists, merge key/value pairs:
                final String internalSuffixedVersion = getInternalPropertiesFilename(propertiesFileName);
                if (propertiesByFilename.containsKey(internalSuffixedVersion)) {
                    properties.putAll(propertiesKeysToLowerCaseIfNeeded(
                            propertiesByFilename.get(internalSuffixedVersion), setKeysToLowerCase));
                }
                // if -custom properties also exists, merge key/value pairs (and overwrite previous values if same key name):
                final String customSuffixedVersion = getCustomPropertiesFilename(propertiesFileName);
                if (propertiesByFilename.containsKey(customSuffixedVersion)) {
                    properties.putAll(propertiesKeysToLowerCaseIfNeeded(propertiesByFilename.get(customSuffixedVersion),
                            setKeysToLowerCase));
                }
            } else {
                if (log.isTraceEnabled()) {
                    log.trace("File " + propertiesFileName + " not found. Returning empty properties object.");
                }
            }
        } catch (IOException e) {
            log.error("Cannot retrieve tenant configurations", e);
        }
        return properties;
    }

    protected Properties propertiesKeysToLowerCaseIfNeeded(Properties properties, boolean setKeysToLowerCase) {
        if (setKeysToLowerCase) {
            Properties reworkedProperties = new Properties();
            properties.forEach((k, v) -> {
                reworkedProperties.put(k.toString().toLowerCase(), v);
            });
            return reworkedProperties;
        } else {
            return properties;
        }
    }

    public Properties getTenantProperties(String propertiesFileName, long tenantId) {
        return getTenantProperties(propertiesFileName, tenantId, false);
    }

    public Properties getTenantProperties(String propertiesFileName, long tenantId, boolean setKeysToLowerCase) {
        return getAlsoCustomAndInternalPropertiesFromFilename(tenantId, propertiesFileName, setKeysToLowerCase);
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
                log.error("Cannot parse properties file content", ioe);
            }
        }
        return properties;
    }

    public void removeProperty(String propertiesFilename, long tenantId, String propertyName) throws IOException {
        Map<String, Properties> resources = getTenantConfigurations(tenantId);
        Properties properties = resources.get(propertiesFilename);
        if (properties != null) {
            properties.remove(propertyName);
            update(tenantId, propertiesFilename, properties);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("File " + propertiesFilename + " not found. Cannot remove property '" + propertyName + "'.");
            }
        }
    }

    public static String getInternalPropertiesFilename(String propertiesFilename) {
        // Internal behavior stores and removes from -internal file (for automatic updates when deploying/updating a page/API extension)
        return propertiesFilename.replaceAll("\\.properties$", "-internal" + ".properties");
    }

    public static String getCustomPropertiesFilename(String propertiesFilename) {
        // Custom behavior stores and removes from -custom files (for manual updates):
        return propertiesFilename.replaceAll("\\.properties$", "-custom" + ".properties");
    }

    protected void update(long tenantId, String propertiesFilename, Properties properties) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            properties.store(byteArrayOutputStream, "");
            getConfigurationFilesUtils().updateTenantPortalConfigurationFile(tenantId, propertiesFilename,
                    byteArrayOutputStream.toByteArray());
        } catch (UpdateException e) {
            throw new IOException(e);
        }
    }

    protected BonitaHomeServer getConfigurationFilesUtils() {
        return BonitaHomeServer.getInstance();
    }

    protected Properties getTenantPortalConfiguration(long tenantId, String propertiesFilename) {
        return ConfigurationFilesManager
                .getProperties(getConfigurationFilesUtils().getTenantPortalConfiguration(tenantId, propertiesFilename));
    }

    protected Map<String, Properties> getTenantConfigurations(long tenantId)
            throws IOException {
        Map<String, byte[]> clientTenantConfigurations = getConfigurationFilesUtils()
                .getTenantPortalConfigurations(tenantId);
        return clientTenantConfigurations.entrySet().stream().collect(Collectors.toMap(
                Entry::getKey, v -> ConfigurationFilesManager.getProperties(v.getValue())));
    }

    public void setProperty(String propertiesFilename, long tenantId, String propertyName, String propertyValue)
            throws IOException {
        Properties properties = getTenantPortalConfiguration(tenantId, propertiesFilename);
        if (properties != null) {
            properties.setProperty(propertyName, propertyValue);
            update(tenantId, propertiesFilename, properties); // store them back in database
        } else {
            if (log.isDebugEnabled()) {
                log.debug("File " + propertiesFilename + " not found. Cannot set property '" + propertyName + "'.");
            }
        }
    }

}
