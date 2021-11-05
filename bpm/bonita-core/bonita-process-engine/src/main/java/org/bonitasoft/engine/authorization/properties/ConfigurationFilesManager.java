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

/**
 * @author Baptiste Mesta
 * @author Emmanuel Duchastenier
 * @author Anthony Birembaut
 */
@Slf4j
public class ConfigurationFilesManager {

    private static final ConfigurationFilesManager INSTANCE = new ConfigurationFilesManager();

    public static ConfigurationFilesManager getInstance() {
        return INSTANCE;
    }

    protected Properties getAlsoCustomAndInternalPropertiesFromFilename(long tenantId, String propertiesFileName) {
        Properties properties = new Properties();
        try {
            final Map<String, Properties> propertiesByFilename = getTenantConfigurations(tenantId);
            if (propertiesByFilename.containsKey(propertiesFileName)) {
                properties.putAll(propertiesByFilename.get(propertiesFileName));
                // if -internal properties also exists, merge key/value pairs:
                final String internalSuffixedVersion = getSuffixedPropertyFilename(propertiesFileName, "-internal");
                if (propertiesByFilename.containsKey(internalSuffixedVersion)) {
                    properties.putAll(propertiesByFilename.get(internalSuffixedVersion));
                }
                // if -custom properties also exists, merge key/value pairs (and overwrite previous values if same key name):
                final String customSuffixedVersion = getSuffixedPropertyFilename(propertiesFileName, "-custom");
                if (propertiesByFilename.containsKey(customSuffixedVersion)) {
                    properties.putAll(propertiesByFilename.get(customSuffixedVersion));
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

    public Properties getTenantProperties(String propertiesFileName, long tenantId) {
        return getAlsoCustomAndInternalPropertiesFromFilename(tenantId, propertiesFileName);
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
        // Now internal behavior stores and removes from -internal file:
        final String internalFilename = getSuffixedPropertyFilename(propertiesFilename, "-internal");
        Map<String, Properties> resources = getTenantConfigurations(tenantId);
        Properties properties = resources.get(internalFilename);
        if (properties != null) {
            properties.remove(propertyName);
            update(tenantId, internalFilename, properties);
            updateAggregatedProperties(propertiesFilename, tenantId, propertyName, null, resources);
        } else {
            if (log.isTraceEnabled()) {
                log.trace("File " + internalFilename + " not found. Cannot remove property '" + propertyName + "'.");
            }
        }
    }

    protected String getSuffixedPropertyFilename(String propertiesFilename, String suffix) {
        return propertiesFilename.replaceAll("\\.properties$", suffix + ".properties");
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

    protected Map<String, Properties> getTenantConfigurations(long tenantId) throws IOException {
        Map<String, byte[]> clientTenantConfigurations = getConfigurationFilesUtils()
                .getClientTenantConfigurations(tenantId);
        return clientTenantConfigurations.entrySet().stream().collect(Collectors.toMap(
                Entry::getKey,
                v -> ConfigurationFilesManager.getProperties(v.getValue())));
    }

    public void setProperty(String propertiesFilename, long tenantId, String propertyName, String propertyValue)
            throws IOException {
        Map<String, Properties> resources = getTenantConfigurations(tenantId);
        // Now internal behavior stores and removes from -internal file:
        final String internalFilename = getSuffixedPropertyFilename(propertiesFilename, "-internal");
        Properties properties = resources.get(internalFilename);
        if (properties != null) {
            properties.setProperty(propertyName, propertyValue);
            update(tenantId, internalFilename, properties);
            updateAggregatedProperties(propertiesFilename, tenantId, propertyName, propertyValue, resources);
        } else {
            if (log.isTraceEnabled()) {
                log.trace("File " + internalFilename + " not found. Cannot set property '" + propertyName + "'.");
            }
        }
    }

    public void updateAggregatedProperties(String propertiesFilename, long tenantId, String propertyName,
            String propertyValue,
            Map<String, Properties> resources) throws IOException {
        Map<String, Properties> aggregatedTenantConfigurations = getTenantConfigurations(tenantId);
        if (aggregatedTenantConfigurations == null) {
            return;
        }
        final String customFilename = getSuffixedPropertyFilename(propertiesFilename, "-custom");
        Properties customResources = resources.get(customFilename);
        if (customResources == null || !customResources.containsKey(propertyName)) {
            //only update the aggregated properties if there is not a custom property overriding the internal one
            Properties aggregatedTenantConfiguration = aggregatedTenantConfigurations.get(propertiesFilename);
            if (aggregatedTenantConfiguration != null) {
                if (propertyValue != null) {
                    aggregatedTenantConfiguration.put(propertyName, propertyValue);
                } else {
                    aggregatedTenantConfiguration.remove(propertyName);
                }
            }
        }
    }
}
