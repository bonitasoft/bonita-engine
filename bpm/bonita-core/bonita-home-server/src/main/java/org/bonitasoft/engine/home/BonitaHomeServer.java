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
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
 * @author Charles Souillard
 * @since 6.0.0
 */
public class BonitaHomeServer extends BonitaHome {

    public static final BonitaHomeServer INSTANCE = new BonitaHomeServer();
    private static final String SERVER_API_IMPLEMENTATION = "serverApi";
    private final ProcessManager processManager;
    private final TenantManager tenantManager;
    private final TenantStorage tenantStorage;
    private Properties platformProperties = null;
    private String version;

    private BonitaHomeServer() {
        platformProperties = null;
        processManager = new ProcessManager(this);
        tenantManager = new TenantManager(this);
        tenantStorage = new TenantStorage(this);
    }

    public static BonitaHomeServer getInstance() {
        return INSTANCE;
    }

    /*
     * =================================================
     * process/tenant management
     * =================================================
     */

    public ProcessManager getProcessManager() {
        return processManager;
    }

    public TenantManager getTenantManager() {
        return tenantManager;
    }

    public TenantStorage getTenantStorage() {
        return tenantStorage;
    }

    /*
     * =================================================
     * Bootstrap the engine
     * =================================================
     */
    public String[] getPrePlatformInitConfigurationFiles() throws BonitaHomeNotSetException, IOException {
        final Folder f1 = FolderMgr.getPlatformInitWorkFolder(getBonitaHomeFolder());
        final Folder f2 = FolderMgr.getPlatformInitConfFolder(getBonitaHomeFolder());
        return getConfigurationFiles(f1, f2);
    }

    public String[] getPlatformConfigurationFiles() throws BonitaHomeNotSetException, IOException {
        final Folder f1 = FolderMgr.getPlatformWorkFolder(getBonitaHomeFolder());
        final Folder f2 = FolderMgr.getPlatformConfFolder(getBonitaHomeFolder());
        return getConfigurationFiles(f1, f2);
    }

    public String[] getTenantConfigurationFiles(final long tenantId) throws BonitaHomeNotSetException, IOException {
        final Folder f1 = FolderMgr.getTenantWorkFolder(getBonitaHomeFolder(), tenantId);
        final Folder f2 = FolderMgr.getTenantConfFolder(getBonitaHomeFolder(), tenantId);
        return getConfigurationFiles(f1, f2);
    }

    private String[] getConfigurationFiles(final Folder... folders) throws BonitaHomeNotSetException, IOException {
        final Properties platformProperties = getPlatformProperties();
        final List<File> files = new ArrayList<>();
        for (Folder folder : folders) {
            files.addAll(getXmlResourcesOfFolder(folder, new NonClusterXmlFilesFilter()));
        }
        //if cluster is activated, add cluster files at the end. We have to ensure cluster files are loaded "last"
        final boolean cluster = Boolean.valueOf(platformProperties.getProperty("bonita.cluster", "false"));
        if (cluster) {
            for (Folder folder : folders) {
                files.addAll(getXmlResourcesOfFolder(folder, new ClusterXmlFilesFilter()));
            }
        }

        return getResourcesFromFiles(files);
    }

    private static List<File> getXmlResourcesOfFolder(final Folder folder, final FileFilter filter) throws IOException {
        //sort this to have always the same order
        File[] listFiles = folder.listFiles(filter);
        List<File> listFilesCollection = Arrays.asList(listFiles);
        Collections.sort(listFilesCollection);
        return listFilesCollection;
    }

    private String[] getResourcesFromFiles(final List<File> files) {
        final List<String> resources = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                resources.add(file.getAbsolutePath());
            }
        }
        return resources.toArray(new String[resources.size()]);
    }

    /*
     * =================================================
     * Configuration
     * =================================================
     */
    private String getBonitaHomeProperty(final String propertyName) throws IllegalStateException {
        try {
            return getPlatformProperties().getProperty(propertyName);
        } catch (BonitaHomeNotSetException | IOException e) {
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

    private Properties mergeProperties(final Folder folder, Properties mergeInto) throws IOException {
        final FilenameFilter filter = new FilenameFilter() {

            public boolean accept(File dir, String filename) {
                return filename.endsWith(".properties");
            }
        };
        final List<File> files = folder.listFiles(filter);
        for (File file : files) {
            Properties properties = getProperties(file);
            for (Map.Entry<Object, Object> property : properties.entrySet()) {
                mergeInto.put(property.getKey(), property.getValue());
            }
        }
        return mergeInto;
    }

    private Properties getProperties(final File propertiesFile) throws IOException {
        return PropertiesManager.getProperties(propertiesFile);
    }

    public Properties getPlatformProperties() throws BonitaHomeNotSetException, IOException {
        if (platformProperties == null) {
            platformProperties = new Properties();
            mergeProperties(FolderMgr.getPlatformWorkFolder(getBonitaHomeFolder()), platformProperties);
            mergeProperties(FolderMgr.getPlatformConfFolder(getBonitaHomeFolder()), platformProperties);
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

    public Properties getTenantProperties(final long tenantId) throws BonitaHomeNotSetException, IOException {
        Properties tenantProperties = new Properties();
        Folder tenantWorkFolder = FolderMgr.getTenantWorkFolder(getBonitaHomeFolder(), tenantId);
        mergeProperties(tenantWorkFolder, tenantProperties);
        mergeProperties(FolderMgr.getTenantConfFolder(getBonitaHomeFolder(), tenantId), tenantProperties);
        return tenantProperties;
    }

    public Properties getPrePlatformInitProperties() throws BonitaHomeNotSetException, IOException {
        Properties preInitProperties = new Properties();
        mergeProperties(FolderMgr.getPlatformInitWorkFolder(getBonitaHomeFolder()), preInitProperties);
        mergeProperties(FolderMgr.getPlatformInitConfFolder(getBonitaHomeFolder()), preInitProperties);
        return preInitProperties;
    }

    @Override
    protected void refresh() {
        platformProperties = null;
    }

    /*
     * =================================================
     * temporary files
     * =================================================
     */

    public File getPlatformTempFile(final String fileName) throws BonitaHomeNotSetException, IOException {
        final Folder tempFolder = FolderMgr.getPlatformTempFolder(getBonitaHomeFolder());
        final File file = tempFolder.getFile(fileName);
        file.delete();
        file.createNewFile();
        return file;
    }

    public URI getGlobalTemporaryFolder() throws BonitaHomeNotSetException, IOException {
        return FolderMgr.getPlatformGobalClassLoaderFolder(getBonitaHomeFolder()).toURI();
    }

    public URI getLocalTemporaryFolder(final String artifactType, final long artifactId) throws BonitaHomeNotSetException, IOException {
        return FolderMgr.getPlatformLocalClassLoaderFolder(getBonitaHomeFolder(), artifactType, artifactId).toURI();
    }

    private static class XmlFilesFilter implements FileFilter {

        @Override
        public boolean accept(final File pathname) {
            return pathname.isFile() && pathname.getName().endsWith(".xml") && !pathname.getName().endsWith("-cache.xml");
        }
    }

    private static class NonClusterXmlFilesFilter extends XmlFilesFilter {

        @Override
        public boolean accept(final File pathname) {
            return super.accept(pathname) && !pathname.getName().contains("cluster");
        }
    }

    private static class ClusterXmlFilesFilter extends XmlFilesFilter {

        @Override
        public boolean accept(final File pathname) {
            return super.accept(pathname) && pathname.getName().contains("cluster");
        }
    }

}
