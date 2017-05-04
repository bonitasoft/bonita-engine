/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.home;

import static org.bonitasoft.engine.home.FolderMgr.getFolder;
import static org.bonitasoft.engine.home.FolderMgr.getPlatformTempFolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.naming.NamingException;

import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.platform.configuration.ConfigurationService;
import org.bonitasoft.platform.configuration.model.BonitaConfiguration;
import org.bonitasoft.platform.setup.PlatformSetupAccessor;
import org.springframework.core.io.ClassPathResource;

/**
 * Utility class that handles the path to the server part of the bonita home
 * <p>
 * The server part of the bonita home contains configuration files and working directories
 * </p>
 *
 * @author Baptiste Mesta
 * @author Frederic Bouquet
 * @author Matthieu Chaffotte
 * @author Charles Souillard
 * @since 6.0.0
 */
public class BonitaHomeServer {

    public static final BonitaHomeServer INSTANCE = new BonitaHomeServer();
    private static final String SERVER_API_IMPLEMENTATION = "serverApi";
    private final TenantStorage tenantStorage;
    private ConfigurationService configurationService;

    private BonitaHomeServer() {
        tenantStorage = new TenantStorage();
    }

    public static BonitaHomeServer getInstance() {
        return INSTANCE;
    }

    private ConfigurationService getConfigurationService() {
        if (configurationService == null) {//should be given by spring
            try {
                configurationService = PlatformSetupAccessor.getConfigurationService();
            } catch (NamingException e) {
                throw new IllegalStateException(e);
            }
        }
        return configurationService;
    }

    public Properties getPlatformProperties() throws IOException {
        return mergeProperties(getPropertiesFromClassPath("bonita-platform-community.properties",
                "bonita-platform-private-community.properties",
                "bonita-platform-private-sp.properties",
                "bonita-platform-sp.properties",
                "bonita-platform-sp-cluster.properties"), getConfigurationService().getPlatformEngineConf());
    }

    public Properties getTenantProperties(long tenantId) throws IOException {
        Properties allProperties = mergeProperties(getPropertiesFromClassPath("bonita-tenant-community.properties",
                "bonita-tenant-private-community.properties",
                "bonita-tenant-sp.properties",
                "bonita-tenant-sp-cluster.properties"), getConfigurationService().getTenantEngineConf(tenantId));
        allProperties.setProperty("tenantId", String.valueOf(tenantId));
        return allProperties;
    }

    public Properties getPropertiesFromClassPath(String... files) throws IOException {
        Properties properties = new Properties();
        for (String file : files) {
            Properties fileProperties = new Properties();
            ClassPathResource classPathResource = new ClassPathResource(file);
            if (!classPathResource.exists()) {
                continue;
            }
            fileProperties.load(classPathResource.getInputStream());
            for (String property : fileProperties.stringPropertyNames()) {
                properties.put(property, fileProperties.getProperty(property));
            }
        }
        return properties;
    }

    public List<BonitaConfiguration> getPlatformInitConfiguration() throws IOException {
        return getAllXmlConfiguration(getConfigurationService().getPlatformInitEngineConf());
    }

    public List<BonitaConfiguration> getPlatformConfiguration() throws IOException {
        return getAllXmlConfiguration(getConfigurationService().getPlatformEngineConf());
    }

    public List<BonitaConfiguration> getTenantConfiguration(long tenantId) throws IOException {
        return getAllXmlConfiguration(getConfigurationService().getTenantEngineConf(tenantId));
    }

    private Properties mergeProperties(Properties mergeInto, List<BonitaConfiguration> configurationFiles) throws IOException {
        for (BonitaConfiguration bonitaConfiguration : configurationFiles) {
            if (bonitaConfiguration.getResourceName().endsWith(".properties")) {
                Properties properties = new Properties();
                properties.load(new ByteArrayInputStream(bonitaConfiguration.getResourceContent()));
                mergeInto.putAll(properties);
            }
        }
        return mergeInto;
    }

    private List<BonitaConfiguration> getAllXmlConfiguration(List<BonitaConfiguration> configurationFiles) throws IOException {
        List<BonitaConfiguration> configurations = new ArrayList<>();
        for (BonitaConfiguration bonitaConfiguration : configurationFiles) {
            if (bonitaConfiguration.getResourceName().endsWith(".xml")) {
                configurations.add(bonitaConfiguration);
            }
        }
        return configurations;
    }

    /*
     * =================================================
     * process/tenant management
     * =================================================
     */

    public TenantStorage getTenantStorage() {
        return tenantStorage;
    }

    /**
     * get the name of the implementation of {@link org.bonitasoft.engine.api.internal.ServerAPI} based on the current configuration of
     * <code>bonita-platform.properties</code>
     *
     * @return the name of the class implementing {@link org.bonitasoft.engine.api.internal.ServerAPI}
     * @throws IllegalStateException if the name of the implementation cannot be retrieved
     */
    public String getServerAPIImplementation() throws IllegalStateException {
        try {
            return getPlatformProperties().getProperty(SERVER_API_IMPLEMENTATION);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /*
     * =================================================
     * temporary files
     * =================================================
     */

    public File getPlatformTempFile(final String fileName) throws BonitaHomeNotSetException, IOException {
        final Folder tempFolder = getPlatformTempFolder();
        final File file = tempFolder.getFile(fileName);
        file.delete();
        file.createNewFile();
        return file;
    }

    public File getLicensesFolder() throws IOException {
        return FolderMgr.getLicensesFolder().getFile();
    }

    public URI getGlobalTemporaryFolder() throws BonitaHomeNotSetException, IOException {
        return FolderMgr.getPlatformGlobalClassLoaderFolder().toURI();
    }

    public URI getLocalTemporaryFolder(final String artifactType, final long artifactId) throws BonitaHomeNotSetException, IOException {
        return FolderMgr.getPlatformLocalClassLoaderFolder(artifactType, artifactId).toURI();
    }

    public void createTenant(final long tenantId) {
        getConfigurationService().storeTenantEngineConf(getConfigurationService().getTenantTemplateEngineConf(), tenantId);
        getConfigurationService().storeTenantSecurityScripts(getConfigurationService().getTenantTemplateSecurityScripts(), tenantId);
        getConfigurationService().storeTenantPortalConf(getConfigurationService().getTenantTemplatePortalConf(), tenantId);

    }

    public void deleteTenant(final long tenantId) throws BonitaHomeNotSetException, IOException {
        getConfigurationService().deleteTenantConfiguration(tenantId);
    }

    public void modifyTechnicalUser(long tenantId, String userName, String password) throws IOException, BonitaHomeNotSetException {
        List<BonitaConfiguration> tenantEngineConf = getConfigurationService().getTenantEngineConf(tenantId);
        for (BonitaConfiguration bonitaConfiguration : tenantEngineConf) {
            if (bonitaConfiguration.getResourceName().equals("bonita-tenant-community-custom.properties")) {
                Properties properties = new Properties();
                properties.load(new ByteArrayInputStream(bonitaConfiguration.getResourceContent()));
                if (userName != null) {
                    properties.setProperty("userName", userName);
                }
                if (password != null) {
                    properties.setProperty("userPassword", password);
                }
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                properties.store(out, "");
                bonitaConfiguration.setResourceContent(out.toByteArray());
                break;
            }
        }
        getConfigurationService().storeTenantEngineConf(tenantEngineConf, tenantId);
    }

    public File getSecurityScriptsFolder(long tenantId) throws BonitaHomeNotSetException, IOException {
        final Folder localFolder = getFolder(getPlatformTempFolder(), "security-scripts").createIfNotExists();
        final Folder tenantSecurityScriptsFolder = getFolder(localFolder, String.valueOf(tenantId)).createIfNotExists();
        List<BonitaConfiguration> tenantSecurityScripts = getConfigurationService().getTenantSecurityScripts(tenantId);
        writeBonitaConfiguration(tenantSecurityScriptsFolder.getFile(), tenantSecurityScripts);
        return tenantSecurityScriptsFolder.getFile();
    }

    private void writeBonitaConfiguration(File folder, List<BonitaConfiguration> bonitaConfigurations) throws IOException {
        for (BonitaConfiguration bonitaConfiguration : bonitaConfigurations) {
            String[] pathArray = bonitaConfiguration.getResourceName().split("/");
            Path path = folder.toPath();
            for (String pathChunk : pathArray) {
                path = path.resolve(pathChunk);
            }
            path.toFile().getParentFile().mkdirs();
            IOUtil.write(path.toFile(), bonitaConfiguration.getResourceContent());
        }
    }

    public Map<String, byte[]> getClientPlatformConfigurations() {
        List<BonitaConfiguration> platformPortalConf = getConfigurationService().getPlatformPortalConf();
        HashMap<String, byte[]> map = new HashMap<>();
        for (BonitaConfiguration bonitaConfiguration : platformPortalConf) {
            map.put(bonitaConfiguration.getResourceName(), bonitaConfiguration.getResourceContent());
        }
        return map;
    }

    public Map<String, byte[]> getClientTenantConfigurations(long tenantId) {
        List<BonitaConfiguration> platformPortalConf = getConfigurationService().getTenantPortalConf(tenantId);
        HashMap<String, byte[]> map = new HashMap<>();
        for (BonitaConfiguration bonitaConfiguration : platformPortalConf) {
            map.put(bonitaConfiguration.getResourceName(), bonitaConfiguration.getResourceContent());
        }
        return map;
    }

    public byte[] getTenantPortalConfiguration(long tenantId, String file) {
        return getConfigurationService().getTenantPortalConfiguration(tenantId, file).getResourceContent();
    }

    public void updateTenantPortalConfigurationFile(long tenantId, String file, byte[] content) throws UpdateException {
        List<BonitaConfiguration> tenantPortalConf = getConfigurationService().getTenantPortalConf(tenantId);
        for (BonitaConfiguration bonitaConfiguration : tenantPortalConf) {
            if (bonitaConfiguration.getResourceName().equals(file)) {
                bonitaConfiguration.setResourceContent(content);
                getConfigurationService().storeTenantPortalConf(Collections.singletonList(bonitaConfiguration), tenantId);
                return;
            }
        }
        throw new UpdateException("unable to update the configuration file " + file + " because it does not exists");
    }

}
