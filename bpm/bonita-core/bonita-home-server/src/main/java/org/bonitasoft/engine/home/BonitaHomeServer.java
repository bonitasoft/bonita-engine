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

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.io.PropertiesManager;

/**
 * Utility class that handles the path to the server part of the bonita home
 * <p>
 * The server part of the bonita home contains configuration files and working directories
 * </p>
 * 
 * @author Baptiste Mesta
 * @author Frederic Bouquet
 * @author Matthieu Chaffotte
 * @since 6.0.0
 */
public class BonitaHomeServer extends BonitaHome {

    private static final String VERSION_FILE_NAME = "VERSION";

    private static final String TENANT_CONFIGURATION_FILE = "bonita-server.properties";

    private static final String CONF = "conf";

    private static final String REPORTS = "reports";

    private static final String BONITA_HOME_SERVER = "server";

    private static final String BONITA_HOME_PLATFORM = "platform";

    private static final String BONITA_HOME_TENANTS = "tenants";

    private static final String BONITA_HOME_PROCESSES = "processes";

    private static final String BONITA_HOME_WORK = "work";

    private static final String BONITA_HOME_TENANT_TEMPLATE = "tenant-template";

    private static final String SERVER_API_IMPLEMENTATION = "serverApi";

    private String tenantsPath;

    private String platformPath;

    private String serverPath;

    private Properties platformProperties;

    private String version;

    public static final BonitaHomeServer INSTANCE = new BonitaHomeServer();

    private BonitaHomeServer() {
        platformProperties = null;
    }

    public static BonitaHomeServer getInstance() {
        return INSTANCE;
    }

    /**
     * get configuration properties of the bonita instance
     * 
     * @param propertyName
     *        the name of the property
     * @return the value of the property or null if it does not exists
     * @throws IllegalStateException
     *         if the property cannot be retrieved
     */
    public String getBonitaHomeProperty(final String propertyName) throws IllegalStateException {
        try {
            return getPlatformProperties().getProperty(propertyName);
        } catch (BonitaHomeNotSetException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * get the name of the implementation of {@link org.bonitasoft.engine.api.internal.ServerAPI} based on the current configuration of
     * <code>bonita-platform.properties</code>
     *
     * @return the name of the class implementing {@link org.bonitasoft.engine.api.internal.ServerAPI}
     * @throws IllegalStateException
     *         if the name of the implementation cannot be retrieved
     */
    public String getServerAPIImplementation() throws IllegalStateException {
        return getBonitaHomeProperty(SERVER_API_IMPLEMENTATION);
    }

    /**
     * @return the path to the server part of the bonita home
     * @throws BonitaHomeNotSetException
     *         when bonita.home system property is not set
     * @since 6.0.0
     */
    public String getBonitaHomeServerFolder() throws BonitaHomeNotSetException {
        if (serverPath == null) {
            final StringBuilder path = new StringBuilder(getBonitaHomeFolder());
            path.append(File.separatorChar);
            path.append(BONITA_HOME_SERVER);
            serverPath = path.toString();
        }
        return serverPath;
    }

    /**
     * get the part of the bonita home containing configuration files and working directories of the platform
     * 
     * @return the path to the platform part of the bonita home
     * @throws BonitaHomeNotSetException
     *         when bonita.home system property is not set
     * @since 6.0.0
     */
    public String getPlatformFolder() throws BonitaHomeNotSetException {
        if (platformPath == null) {
            final StringBuilder path = new StringBuilder(getBonitaHomeServerFolder());
            path.append(File.separatorChar);
            path.append(BONITA_HOME_PLATFORM);
            platformPath = path.toString();
        }
        return platformPath;
    }

    /**
     * get the absolute path to the folder of the bonita home containing configuration files of the platform
     *
     * @return the path to the platform configuration directory
     * @throws BonitaHomeNotSetException
     *         when bonita.home system property is not set
     * @since 6.0.0
     */
    public String getPlatformConfFolder() throws BonitaHomeNotSetException {
        final StringBuilder path = new StringBuilder(getPlatformFolder());
        path.append(File.separatorChar);
        path.append(CONF);
        return path.toString();
    }

    /**
     * get the file containing the configuration of the platform
     *
     * @return the file containing the configuration of the platform
     * @throws BonitaHomeNotSetException
     *         when bonita.home system property is not set
     * @since 6.0.0
     */
    public File getPlatformFile() throws BonitaHomeNotSetException {
        final String platformFolder = getPlatformConfFolder();
        return new File(platformFolder + File.separatorChar + "bonita-platform.properties");
    }

    /**
     * get the absolute path to the folder of the bonita home containing configuration files and working directories of the tenants
     *
     * @return the path to the tenants part of the bonita home
     * @throws BonitaHomeNotSetException
     *         when bonita.home system property is not set
     * @since 6.0.0
     */
    public String getTenantsFolder() throws BonitaHomeNotSetException {
        if (tenantsPath == null) {
            tenantsPath = getBonitaHomeServerFolder() + File.separatorChar + BONITA_HOME_TENANTS;
        }
        return tenantsPath;
    }

    /**
     * get the absolute path to the folder containing definitions and files related to processes
     *
     * @param tenantId
     *        the id of the tenant
     * @return the path to the folder containing definitions and files related to processes
     * @throws BonitaHomeNotSetException
     *         when bonita.home system property is not set
     * @since 6.0.0
     */
    public String getProcessesFolder(final long tenantId) throws BonitaHomeNotSetException {
        return getTenantFolder(tenantId) + File.separatorChar + BONITA_HOME_WORK + File.separatorChar + BONITA_HOME_PROCESSES;
    }

    /**
     * get the absolute path to the folder containing definition and files related to a processes
     *
     * @param tenantId
     *        the id of the tenant
     * @param processDefinitionId
     *        the id of the process definition
     * @return the path to the folder containing definitions and files related to processes
     * @throws BonitaHomeNotSetException
     *         when bonita.home system property is not set
     * @since 6.0.0
     */
    public String getProcessFolder(final long tenantId, final long processDefinitionId) throws BonitaHomeNotSetException {
        return getTenantFolder(tenantId) + File.separatorChar + BONITA_HOME_WORK + File.separatorChar + BONITA_HOME_PROCESSES + File.separatorChar
                + processDefinitionId;
    }

    /**
     * get the absolute path to the folder containing configuration files and working directories of a tenant
     * 
     * @param tenantId
     *        the id of the tenant
     * @return
     *         the path to this folder
     * @throws BonitaHomeNotSetException
     *         when bonita.home system property is not set
     * @since 6.0.0
     */
    public String getTenantFolder(final long tenantId) throws BonitaHomeNotSetException {
        return getTenantsFolder() + File.separatorChar + tenantId;
    }

    /**
     * @param tenantId
     * @return the path of the work dir for a tenant
     * @throws BonitaHomeNotSetException
     * @since 6.5
     */
    public String getTenantWorkFolder(final long tenantId) throws BonitaHomeNotSetException {
        return getTenantFolder(tenantId) + File.separatorChar + BONITA_HOME_WORK;
    }

    /**
     * get the absolute path to the folder containing configuration files of a tenant
     *
     * @param tenantId
     *        the id of the tenant
     * @return
     *         the path to this folder
     * @throws BonitaHomeNotSetException
     *         when bonita.home system property is not set
     * @since 6.0.0
     */
    public String getTenantConfFolder(final long tenantId) throws BonitaHomeNotSetException {
        return getTenantsFolder() + File.separatorChar + tenantId + File.separatorChar + CONF;
    }

    /**
     * get the absolute path to the reports folder inside a specific tenant folder.
     * 
     * @param tenantId
     *        the id of the tenant
     * @return the absolute path to the reports folder for the tenant.
     * @throws BonitaHomeNotSetException
     *         when bonita.home system property is not set
     * @since 6.0.0
     */
    public String getTenantReportFolder(final long tenantId) throws BonitaHomeNotSetException {
        return getTenantsFolder() + File.separatorChar + tenantId + File.separatorChar + REPORTS;
    }

    /**
     * get the absolute path to the folder containing the template to create a new tenant
     *
     * @return the absolute path to the folder
     * @throws BonitaHomeNotSetException
     *         when bonita.home system property is not set
     * @since 6.0.0
     */
    public String getTenantTemplateFolder() throws BonitaHomeNotSetException {
        return getPlatformFolder() + File.separatorChar + BONITA_HOME_TENANT_TEMPLATE;
    }

    @Override
    protected void refresh() {
        platformPath = null;
        serverPath = null;
        tenantsPath = null;
        platformProperties = null;
    }

    /**
     * get the configuration of the platform
     * 
     * @return the configuration of the platform as <code>Properties</code>
     * @throws BonitaHomeNotSetException
     *         when bonita.home system property is not set
     * @throws IOException
     *         if the properties cannot be read from the file
     * @since 6.0.0
     */
    public Properties getPlatformProperties() throws BonitaHomeNotSetException, IOException {
        if (platformProperties == null) {
            platformProperties = PropertiesManager.getProperties(getPlatformFile());
        }
        return platformProperties;
    }

    /**
     * get the version of the bonita home
     * 
     * @return the version of the bonita home
     */
    public String getVersion() {
        if (version == null) {
            String platformConfFolder = null;
            try {
                platformConfFolder = getPlatformConfFolder();
                File file = new File(new File(platformConfFolder), VERSION_FILE_NAME);
                version = IOUtil.read(file);
            } catch (Exception e) {
                buildUnreadableVersionException(platformConfFolder);
            }
            if (version == null) {
                buildUnreadableVersionException(platformConfFolder);
            }
        }
        return version;
    }

    private void buildUnreadableVersionException(final String platformConfFolder) {
        throw new IllegalStateException(VERSION_FILE_NAME + " file not present or invalid in folder: " + platformConfFolder);
    }

    /**
     * get the configuration of a tenant
     * 
     * @param tenantId
     *        the id of the tenant
     * @return the configuration of the tenant as <code>Properties</code>
     * @throws BonitaHomeNotSetException
     *         when bonita.home system property is not set
     * @throws IOException
     *         if the properties cannot be read from the file
     * @since 6.0.0
     */
    public Properties getTenantProperties(final long tenantId) throws BonitaHomeNotSetException, IOException {
        return PropertiesManager.getProperties(new File(getTenantConfFolder(tenantId) + File.separatorChar + TENANT_CONFIGURATION_FILE));
    }

}
