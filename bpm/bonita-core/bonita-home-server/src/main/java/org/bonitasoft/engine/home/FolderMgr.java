package org.bonitasoft.engine.home;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Charles Souillard
 * @author Emmanuel Duchastenier
 */
class FolderMgr {

    public static final Logger LOGGER = LoggerFactory.getLogger(FolderMgr.class);
    public static final String TEMP_FOLDER_NAME_PREFIX = "bonita_engine_";

    private static Folder getFolder(final File baseFolder, final String subFolder) throws IOException {
        return new Folder(new Folder(baseFolder), subFolder);
    }

    static Folder getFolder(final Folder baseFolder, final String subFolder) throws IOException {
        return new Folder(baseFolder, subFolder);
    }

    static Folder getTempFolder() throws IOException {
        File systemTempFolder = new File(System.getProperty("java.io.tmpdir"));
        final Folder tempFolder = getFolder(systemTempFolder, TEMP_FOLDER_NAME_PREFIX + getJvmName());
        if (!tempFolder.exists()) {
            warnIfSomeTempFolderAlreadyExists(systemTempFolder);
            tempFolder.createAsTemporaryFolder();
        }
        return tempFolder;
    }

    private static void warnIfSomeTempFolderAlreadyExists(File systemTempFolder) {
        File[] files = systemTempFolder.listFiles((dir, name) -> dir.isDirectory() && name.startsWith(TEMP_FOLDER_NAME_PREFIX));
        List<File> temporaryFolders = Arrays.asList(files != null ? files : new File[0]);
        if (!temporaryFolders.isEmpty()) {
            LOGGER.warn("The following temporary folders were not deleted on the previous shutdown. " +
                    "This can happen when your JVM crashed or if you did not properly stop the platform.");
            LOGGER.warn("Delete these folders to free up space:");
            for (File temporaryFolder : temporaryFolders) {
                LOGGER.warn(temporaryFolder.getAbsolutePath());
            }
        }

    }

    static Folder getPlatformTempFolder() throws IOException {
        return getFolder(getTempFolder(), "platform").createIfNotExists();
    }

    static Folder getLicensesFolder() throws IOException {
        return getFolder(getTempFolder(), "licenses");
    }

    static Folder getTenantsWorkFolder(final File parentFolder) throws IOException {
        return getFolder(parentFolder, "tenants");
    }

    static Folder getTenantTempFolder(long tenantId) throws IOException {
        return getFolder(getTenantsWorkFolder(getTempFolder().getFile()), Long.toString(tenantId));
    }

    private static Folder getPlatformClassLoaderFolder() throws IOException {
        return getFolder(getPlatformTempFolder(), "classloaders").createIfNotExists();
    }

    private static String getJvmName() {
        return ManagementFactory.getRuntimeMXBean().getName();
    }

    static Folder getPlatformGlobalClassLoaderFolder() throws IOException {
        final Folder globalFolder = getFolder(getPlatformClassLoaderFolder(), "global");
        globalFolder.createIfNotExists();
        return globalFolder;
    }

    private static Folder getPlatformLocalClassLoaderFolder() throws IOException {
        final Folder localFolder = getFolder(getPlatformClassLoaderFolder(), "local");
        localFolder.createIfNotExists();
        return localFolder;
    }

    static Folder getPlatformLocalClassLoaderFolder(String artifactType, long artifactId) throws IOException {
        final Folder localFolder = getPlatformLocalClassLoaderFolder();
        final Folder artifactTypeFolder = getFolder(localFolder, artifactType);
        artifactTypeFolder.createIfNotExists();
        final Folder artifactIdFolder = getFolder(artifactTypeFolder, Long.toString(artifactId));
        artifactIdFolder.createIfNotExists();
        return artifactIdFolder;
    }

}
