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
import java.util.List;
import java.util.Properties;

import javax.naming.NamingException;

import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.platform.configuration.ConfigurationService;
import org.bonitasoft.platform.configuration.impl.ConfigurationServiceImpl;
import org.bonitasoft.platform.configuration.model.BonitaConfiguration;

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
public class BonitaHomeServer extends BonitaHome {

    public static final BonitaHomeServer INSTANCE = new BonitaHomeServer();
    private static final String SERVER_API_IMPLEMENTATION = "serverApi";
    private final TenantStorage tenantStorage;
    private ConfigurationService configurationService;
    private String version;

    private BonitaHomeServer() {
        tenantStorage = new TenantStorage(this);
    }

    BonitaHomeServer(ConfigurationService configurationService) {
        this();
        this.configurationService = configurationService;
    }

    public static BonitaHomeServer getInstance() {
        return INSTANCE;
    }

    public Properties getPlatformInitProperties() throws IOException {
        return getAllProperties(getConfigurationService().getPlatformInitEngineConf());
    }

    private ConfigurationService getConfigurationService() {
        if (configurationService == null) {
            try {
                configurationService = new ConfigurationServiceImpl();
            } catch (NamingException e) {
                throw new IllegalStateException(e);
            }
        }
        return configurationService;
    }

    public Properties getPlatformProperties() throws IOException {
        return getAllProperties(getConfigurationService().getPlatformEngineConf());
    }

    public Properties getTenantProperties(long tenantId) throws IOException {
        Properties allProperties = getAllProperties(getConfigurationService().getTenantEngineConf(tenantId));
        allProperties.setProperty("tenantId", String.valueOf(tenantId));
        return allProperties;
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

    private Properties getAllProperties(List<BonitaConfiguration> configurationFiles) throws IOException {
        Properties allProperties = new Properties();
        for (BonitaConfiguration bonitaConfiguration : configurationFiles) {
            if (bonitaConfiguration.getResourceName().endsWith(".properties")) {
                Properties properties = new Properties();
                properties.load(new ByteArrayInputStream(bonitaConfiguration.getResourceContent()));
                allProperties.putAll(properties);
            }
        }
        return allProperties;
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

    /**
     * get the version of the bonita home
     *
     * @return the version of the bonita home
     */
    public String getVersion() {
        if (version == null) {
            File versionFile = null;
            try {
                versionFile = FolderMgr.getPlatformWorkFolder(getBonitaHomeFolder()).getFile("VERSION");
                version = IOUtil.read(versionFile);
            } catch (Exception e) {
                throw new IllegalStateException("Error while reading file" + versionFile, e);
            }
        }
        return version;
    }

    @Override
    protected void refresh() {
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

    public URI getGlobalTemporaryFolder() throws BonitaHomeNotSetException, IOException {
        return FolderMgr.getPlatformGlobalClassLoaderFolder().toURI();
    }

    public URI getLocalTemporaryFolder(final String artifactType, final long artifactId) throws BonitaHomeNotSetException, IOException {
        return FolderMgr.getPlatformLocalClassLoaderFolder(artifactType, artifactId).toURI();
    }

    public void createTenant(final long tenantId) {
        getConfigurationService().storeTenantEngineConf(getConfigurationService().getTenantTemplateEngineConf(), tenantId);
        getConfigurationService().storeTenantSecurityScripts(getConfigurationService().getTenantTemplateSecurityScripts(), tenantId);

    }

    public void deleteTenant(final long tenantId) throws BonitaHomeNotSetException, IOException {
        //TODO ?????
    }

    public void modifyTechnicalUser(long tenantId, String userName, String password) throws IOException, BonitaHomeNotSetException {
        List<BonitaConfiguration> tenantEngineConf = getConfigurationService().getTenantEngineConf(tenantId);
        for (BonitaConfiguration bonitaConfiguration : tenantEngineConf) {
            if (bonitaConfiguration.getResourceName().equals("bonita-tenant-community.properties")) {
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

}
