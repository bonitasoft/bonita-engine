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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.FileHandler;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
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

    private static final String SERVER_API_IMPLEMENTATION = "serverApi";

    private Properties platformProperties = null;

    private String version;

    public static final BonitaHomeServer INSTANCE = new BonitaHomeServer();

    private BonitaHomeServer() {
        platformProperties = null;
    }

    public static BonitaHomeServer getInstance() {
        return INSTANCE;
    }

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

    private String[] getResourcesFromFiles(final List<File> files) {
        final List<String> resources = new ArrayList<String>();
        if (files != null) {
            for (File file : files) {
                resources.add(file.getAbsolutePath());
            }
        }
        //System.err.println(this.getClass().getName() + "-" + "getResourcesFromFiles" + ":" + "resources=" + resources);
        final String[] strings = resources.toArray(new String[resources.size()]);
        //System.err.println(this.getClass().getName() + "-" + "getResourcesFromFiles" + ":" + "strings=" + strings);
        return strings;
    }

    public FileHandler getIncidentFileHandler(long tenantId) throws BonitaHomeNotSetException, IOException {
        final File incidentFile = FolderMgr.getTenantWorkFolder(getBonitaHomeFolder(), tenantId).getFile("incidents.log");
        return new FileHandler(incidentFile.getAbsolutePath());

    }


    private File getBDMFile(final long tenantId) throws BonitaHomeNotSetException, IOException {
        final Folder bdmFolder = FolderMgr.getTenantWorkBDMFolder(getBonitaHomeFolder(), tenantId);
        return bdmFolder.getFile("client-bdm.zip");
    }

    public byte[] getClientBDMZip(final long tenantId) throws BonitaHomeNotSetException, IOException {
        final File bdmFile = getBDMFile(tenantId);
        return IOUtil.getAllContentFrom(bdmFile);
    }

    public void writeClientBDMZip(final long tenantId, byte[] clientBdmJar) throws BonitaHomeNotSetException, IOException {
        final File bdmFile = getBDMFile(tenantId);
        if (bdmFile.exists()) {
            bdmFile.delete();
        }
        IOUtil.write(bdmFile, clientBdmJar);
    }

    public void removeBDMZip(final long tenantId) throws BonitaHomeNotSetException, IOException {
        final File bdmFile = getBDMFile(tenantId);
        if (bdmFile.exists()) {
            bdmFile.delete();
        }
    }

    public File getPlatformTempFile(final String fileName) throws BonitaHomeNotSetException, IOException {
        final Folder tempFolder = FolderMgr.getPlatformTempFolder(getBonitaHomeFolder());
        final File file = tempFolder.getFile(fileName);
        file.delete();
        file.createNewFile();
        return file;
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

    private static List<File> getXmlResourcesOfFolder(final Folder folder, final FileFilter filter) throws IOException {
        //sort this to have always the same order
        File[] listFiles = folder.listFiles(filter);
        List<File> listFilesCollection = Arrays.asList(listFiles);
        Collections.sort(listFilesCollection);
        return listFilesCollection;
    }

    private String[] getConfigurationFiles(final Folder... folders) throws BonitaHomeNotSetException, IOException {
        final Properties platformProperties = getPlatformProperties();
        final List<File> files = new ArrayList<File>();
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

    public void createTenant(final long tenantId) throws BonitaHomeNotSetException, IOException {
        FolderMgr.createTenant(getBonitaHomeFolder(), tenantId);

        //put the right id in tenant properties file
        final File file = FolderMgr.getTenantWorkFolder(getBonitaHomeFolder(), tenantId).getFile("bonita-tenant-id.properties");
        //maybe a replace is better?
        final Properties tenantProperties = new Properties();
        tenantProperties.put("tenantId", String.valueOf(tenantId));
        PropertiesManager.saveProperties(tenantProperties, file);
    }

    public void deleteTenant(final long tenantId) throws BonitaHomeNotSetException, IOException {
        FolderMgr.deleteTenant(getBonitaHomeFolder(), tenantId);
    }

    @Override
    protected void refresh() {
        //System.err.println("----- REFRESH Thread: " + Thread.currentThread().getId() + "-----");
        platformProperties = null;
        //System.err.println("----- END REFRESH Thread: " + Thread.currentThread().getId() + "-----");
    }

    private Properties mergeProperties(final Folder folder, Properties mergeInto) throws IOException {
        final FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String filename) { return filename.endsWith(".properties"); }
        };

        final List<File> files = folder.listFiles(filter);
        for (File file : files) {
            //System.out.println("load properties " + file);
            Properties properties = getProperties(file);
            for (Map.Entry<Object, Object> property : properties.entrySet()) {
                Object put = mergeInto.put(property.getKey(), property.getValue());
                if (put != null) {
                    //System.out.println("Overriding " + property.getKey() + " with " + property.getValue());
                }
            }
            properties.putAll(properties);
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
            if (version == null) {
                throw new IllegalStateException("Invalid version file: " + versionFile);
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


    private Folder getProcessFolder(long tenantId, long processId) throws BonitaHomeNotSetException, IOException {
        return FolderMgr.getTenantWorkProcessFolder(getBonitaHomeFolder(), tenantId, processId);
    }

    public File getProcessDefinitionFile(long tenantId, long processId, String fileName) throws BonitaHomeNotSetException, IOException {
        return getProcessFolder(tenantId, processId).getFile(fileName);
    }

    public FileOutputStream getProcessDefinitionFileOutputstream(long tenantId, long processId, String fileName) throws BonitaHomeNotSetException, IOException {
        final Folder processFolder = getProcessFolder(tenantId, processId);
        final File file = processFolder.getFile(fileName);
        return new FileOutputStream(file);
    }

    private Folder getProcessClasspathFolder(final long tenantId, final long processId) throws BonitaHomeNotSetException, IOException {
        return FolderMgr.getTenantWorkProcessClasspathFolder(getBonitaHomeFolder(), tenantId, processId);
    }
    public Map<String, byte[]> getProcessClasspath(final long tenantId, final long processId) throws BonitaHomeNotSetException, IOException {
        final Map<String, byte[]> resources = new HashMap<String, byte[]>();
        final Folder processClasspathFolder = getProcessClasspathFolder(tenantId, processId);
        final File[] listFiles = processClasspathFolder.listFiles();
        for (final File jarFile : listFiles) {
            final String name = jarFile.getName();
            final byte[] jarContent = FileUtils.readFileToByteArray(jarFile);
            resources.put(name, jarContent);
        }
        return resources;
    }

    public void deleteProcess(long tenantId, long processId) throws BonitaHomeNotSetException, IOException {
        FolderMgr.deleteTenantWorkProcessFolder(getBonitaHomeFolder(), tenantId, processId);
        FolderMgr.deleteTenantTempProcessFolder(getBonitaHomeFolder(), tenantId, processId);
    }

    public Map<String, byte[]> getProcessResources(long tenantId, long processId, String filenamesPattern) throws BonitaHomeNotSetException, IOException {
        final Folder processFolder = getProcessFolder(tenantId, processId);
        return processFolder.getResources(filenamesPattern);
    }

    public byte[] getProcessDocument(long tenantId, long processId, String documentName) throws BonitaHomeNotSetException, IOException {
        final Folder documentsFolder = FolderMgr.getTenantWorkProcessDocumentFolder(getBonitaHomeFolder(), tenantId, processId);
        return FileUtils.readFileToByteArray(documentsFolder.getFile(documentName));
    }

    public File getTenantWorkFile(long tenantId, String fileName) throws BonitaHomeNotSetException, IOException {
        return FolderMgr.getTenantWorkFolder(getBonitaHomeFolder(), tenantId).getFile(fileName);
    }

    public void writeBusinessArchive(long tenantId, long processIdd, BusinessArchive businessArchive) throws BonitaHomeNotSetException, IOException {
        final Folder processFolder = getProcessFolder(tenantId, processIdd);
        processFolder.writeBusinessArchive(businessArchive);
    }

    public Map<String, byte[]> getConnectorFiles(long tenantId, long processId) throws BonitaHomeNotSetException, IOException {
        return getConnectorsFolder(tenantId, processId).listFilesAsResources();
    }

    private Folder getConnectorsFolder(long tenantId, long processId) throws BonitaHomeNotSetException, IOException {
        return FolderMgr.getTenantWorkProcessConnectorsFolder(getBonitaHomeFolder(), tenantId, processId);
    }

    public void deleteConnectorFile(long tenantId, long processId, String fileName) throws BonitaHomeNotSetException, IOException {
        final Folder connectorsFolder = getConnectorsFolder(tenantId, processId);
        connectorsFolder.deleteFile(fileName);
    }

    public void deleteClasspathFiles(long tenantId, long processId, String... resourceNames) throws BonitaHomeNotSetException, IOException {
        if (resourceNames != null) {
            final Folder classpathFolder = getProcessClasspathFolder(tenantId, processId);
            for (String resourceName : resourceNames) {
                classpathFolder.deleteFile(resourceName);
            }
        }
    }

    public void storeClasspathFile(long tenantId, final long processId, String resourceName, byte[] resourceContent) throws BonitaHomeNotSetException, IOException {
        final File newFile = getProcessClasspathFolder(tenantId, processId).getFile(resourceName);
        IOUtil.write(newFile, resourceContent);
    }

    public void storeConnectorFile(long tenantId, final long processId, String resourceName, byte[] resourceContent) throws BonitaHomeNotSetException, IOException {
        final File newFile = getConnectorsFolder(tenantId, processId).getFile(resourceName);
        IOUtil.write(newFile, resourceContent);
    }

    public void storeSecurityScript(long tenantId, String scriptFileContent, String fileName, String[] folders) throws IOException, BonitaHomeNotSetException {
        final Folder secuFolder = FolderMgr.getTenantWorkSecurityFolder(getBonitaHomeFolder(), tenantId);
        Folder current = secuFolder;
        for (String folder : folders) {
            current = new Folder(current, folder);
            current.create();
        }
        final File fileToWrite = current.newFile(fileName);
        org.bonitasoft.engine.commons.io.IOUtil.writeFile(fileToWrite, scriptFileContent);
    }

    public void createProcess(long tenantId, long processId) throws BonitaHomeNotSetException, IOException {
        FolderMgr.createTenantWorkProcessFolder(getBonitaHomeFolder(), tenantId, processId);
        FolderMgr.createTenantTempProcessFolder(getBonitaHomeFolder(), tenantId, processId);
    }

    private File getParameterFile(long tenantId, long processId) throws BonitaHomeNotSetException, IOException {
        return getProcessFolder(tenantId, processId).getFile("parameters.properties");
    }
    public Properties getParameters(long tenantId, long processId) throws BonitaHomeNotSetException, IOException {
        return PropertiesManager.getProperties(getParameterFile(tenantId, processId));
    }

    public void storeParameters(long tenantId, long processId, Properties properties) throws BonitaHomeNotSetException, IOException {
        PropertiesManager.saveProperties(properties, getParameterFile(tenantId, processId));
    }

    public boolean hasParameters(long tenantId, long processId) throws IOException, BonitaHomeNotSetException {
        final File file = getParameterFile(tenantId, processId);
        return file.exists();
    }

    public boolean deleteParameters(long tenantId, long processId) throws IOException, BonitaHomeNotSetException {
        final File file = getParameterFile(tenantId, processId);
        return file.delete();
    }

    public File[] getUserFiltersFiles(long tenantId, long processId) throws BonitaHomeNotSetException, IOException {
        return getUserFiltersFolder(tenantId, processId).listFiles();
    }

    private Folder getUserFiltersFolder(long tenantId, long processId) throws BonitaHomeNotSetException, IOException {
        return FolderMgr.getTenantWorkProcessUserFiltersFolder(getBonitaHomeFolder(), tenantId, processId);
    }

    public File getSecurityScriptsFolder(long tenantId) throws BonitaHomeNotSetException, IOException {
        return FolderMgr.getTenantWorkSecurityFolder(getBonitaHomeFolder(), tenantId).getFile();
    }

    public void modifyTechnicalUser(long tenantId, String userName, String password) throws IOException, BonitaHomeNotSetException {
        final Folder workFolder = FolderMgr.getTenantWorkFolder(getBonitaHomeFolder(), tenantId);
        final File propertiesFile = workFolder.getFile("bonita-tenant-community.properties");
        final Map<String, String> pairs = new HashMap<>();
        if (userName != null) {
            pairs.put("userName", userName);
        }
        if (password != null) {
            pairs.put("userPassword", password);
        }
        org.bonitasoft.engine.commons.io.IOUtil.updatePropertyValue(propertiesFile, pairs);
    }



    public byte[] exportBarProcessContentUnderHome(long tenantId, long processId, final String actorMappingContent) throws IOException, BonitaHomeNotSetException {
        final FileOutputStream actorMappingOS = getProcessDefinitionFileOutputstream(tenantId, processId, "actorMapping.xml");
        IOUtil.writeContentToFileOutputStream(actorMappingContent, actorMappingOS);
        //final FileOutputStream parametersOS = getProcessDefinitionFileOutputstream(tenantId, processId, "current-parameters.properties");
        //IOUtil.writeContentToFileOutputStream(parametersContent, parametersOS);
        final Folder processFolder = getProcessFolder(tenantId, processId);
        return processFolder.zip(processFolder);
    }

    public URI getGlobalTemporaryFolder() throws BonitaHomeNotSetException, IOException {
        return FolderMgr.getPlatformGobalClassLoaderFolder(getBonitaHomeFolder()).toURI();
    }

    public URI getLocalTemporaryFolder(final String artifactType, final long artifactId) throws BonitaHomeNotSetException, IOException {
        return FolderMgr.getPlatformLocalClassLoaderFolder(getBonitaHomeFolder(), artifactType, artifactId).toURI();
    }


}
