package org.bonitasoft.engine.home;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;

/**
 * @author Charles Souillard
 * @author Emmanuel Duchastenier
 */
class FolderMgr {

    private static Folder getFolder(final File baseFolder, final String subFolder) throws IOException {
        return new Folder(new Folder(baseFolder), subFolder);
    }

    static Folder getFolder(final Folder baseFolder, final String subFolder) throws IOException {
        return new Folder(baseFolder, subFolder);
    }

    private static Folder getTempFolder() throws IOException {
        final Folder tempFolder = getFolder(new File(System.getProperty("java.io.tmpdir")), "bonita_engine_" + getJvmName());
        if (!tempFolder.exists()) {
            tempFolder.createAsTemporaryFolder();
        }
        return tempFolder;
    }

    static Folder getPlatformTempFolder() throws IOException {
        return getFolder(getTempFolder(), "platform").createIfNotExists();
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
