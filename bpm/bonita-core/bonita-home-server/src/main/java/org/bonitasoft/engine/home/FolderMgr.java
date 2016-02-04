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

    private static Folder getFolder(final Folder baseFolder, final String subFolder) throws IOException {
        return new Folder(baseFolder, subFolder);
    }

    private static Folder getServerFolder(final File bonitaHomeFolder) throws IOException {
        return getFolder(bonitaHomeFolder, "engine-server");
    }

    private static Folder getWorkFolder(final File bonitaHomeFolder) throws IOException {
        return getFolder(getServerFolder(bonitaHomeFolder), "work");
    }

    private static Folder getConfFolder(final File bonitaHomeFolder) throws IOException {
        return getFolder(getServerFolder(bonitaHomeFolder), "conf");
    }

    private static Folder getTempFolder() throws IOException {
        final Folder tempFolder = getFolder(new File(System.getProperty("java.io.tmpdir")), "bonita_tmp_" + getJvmName());
        if (!tempFolder.exists()) {
            tempFolder.createAsTemporaryFolder();
        }
        return tempFolder;
    }

    public static Folder getPlatformInitWorkFolder(final File bonitaHomeFolder) throws IOException {
        return getFolder(getWorkFolder(bonitaHomeFolder), "platform-init");
    }

    public static Folder getPlatformInitConfFolder(File bonitaHomeFolder) throws IOException {
        return getFolder(getConfFolder(bonitaHomeFolder), "platform-init");
    }

    public static Folder getPlatformWorkFolder(final File bonitaHomeFolder) throws IOException {
        return getFolder(getWorkFolder(bonitaHomeFolder), "platform");
    }

    public static Folder getPlatformConfFolder(final File bonitaHomeFolder) throws IOException {
        return getFolder(getConfFolder(bonitaHomeFolder), "platform");
    }

    public static Folder getPlatformTempFolder() throws IOException {
        return getFolder(getTempFolder(), "platform").createIfNotExists();
    }

    private static Folder getTenantsWorkFolder(final File bonitaHomeFolder) throws IOException {
        return getFolder(getWorkFolder(bonitaHomeFolder), "tenants");
    }

    private static Folder getTenantsConfFolder(final File bonitaHomeFolder) throws IOException {
        return getFolder(getConfFolder(bonitaHomeFolder), "tenants");
    }

    private static Folder getTenantsTempFolder() throws IOException {
        return getFolder(getTempFolder(), "tenants").createIfNotExists();
    }

    public static Folder getTenantWorkFolder(final File bonitaHomeFolder, long tenantId) throws IOException {
        return getFolder(getTenantsWorkFolder(bonitaHomeFolder), Long.toString(tenantId));
    }

    public static Folder getTenantConfFolder(final File bonitaHomeFolder, long tenantId) throws IOException {
        return getFolder(getTenantsConfFolder(bonitaHomeFolder), Long.toString(tenantId));
    }

    public static Folder getTenantTempFolder(long tenantId) throws IOException {
        Folder tenantsTempFolder = getTenantsTempFolder();
        return getFolder(tenantsTempFolder, Long.toString(tenantId)).createIfNotExists();
    }

    public static Folder getTenantTemplateWorkFolder(File bonitaHomeFolder) throws IOException {
        return getFolder(getTenantsWorkFolder(bonitaHomeFolder), "template");
    }

    public static Folder getTenantTemplateConfFolder(File bonitaHomeFolder) throws IOException {
        return getFolder(getTenantsConfFolder(bonitaHomeFolder), "template");
    }

    public static void deleteTenant(File bonitaHomeFolder, long tenantId) throws IOException {
        getTenantWorkFolder(bonitaHomeFolder, tenantId).delete();
        getTenantConfFolder(bonitaHomeFolder, tenantId).delete();
        getTenantTempFolder(tenantId).delete();
    }

    public static void createTenant(File bonitaHomeFolder, long tenantId) throws IOException {
        final Folder tenantWorkFolder = getTenantWorkFolder(bonitaHomeFolder, tenantId);
        final Folder tenantConfFolder = getTenantConfFolder(bonitaHomeFolder, tenantId);
        final Folder templateWorkFolder = getTenantTemplateWorkFolder(bonitaHomeFolder);
        final Folder templateConfFolder = getTenantTemplateConfFolder(bonitaHomeFolder);

        // copy configuration file
        try {
            templateWorkFolder.copyTo(tenantWorkFolder);
            templateConfFolder.copyTo(tenantConfFolder);
            getTenantWorkSecurityFolder(bonitaHomeFolder, tenantId).create();
        } catch (final IOException e) {
            deleteTenant(bonitaHomeFolder, tenantId);
            throw e;
        }
    }

    public static Folder getTenantWorkSecurityFolder(File bonitaHomeFolder, long tenantId) throws IOException {
        return getFolder(getTenantWorkFolder(bonitaHomeFolder, tenantId), "security-scripts");
    }

    public static Folder getPlatformClassLoaderFolder() throws IOException {
        return getFolder(getPlatformTempFolder(), "classloaders").createIfNotExists();
    }

    private static String getJvmName() {
        return ManagementFactory.getRuntimeMXBean().getName();
    }

    public static Folder getPlatformGlobalClassLoaderFolder() throws IOException {
        final Folder globalFolder = getFolder(getPlatformClassLoaderFolder(), "global");
        globalFolder.createIfNotExists();
        return globalFolder;
    }

    private static Folder getPlatformLocalClassLoaderFolder() throws IOException {
        final Folder localFolder = getFolder(getPlatformClassLoaderFolder(), "local");
        localFolder.createIfNotExists();
        return localFolder;
    }

    public static Folder getPlatformLocalClassLoaderFolder(String artifactType, long artifactId) throws IOException {
        final Folder localFolder = getPlatformLocalClassLoaderFolder();
        final Folder artifactTypeFolder = getFolder(localFolder, artifactType);
        artifactTypeFolder.createIfNotExists();
        final Folder artifactIdFolder = getFolder(artifactTypeFolder, Long.toString(artifactId));
        artifactIdFolder.createIfNotExists();
        return artifactIdFolder;
    }

    public static Folder getTenantWorkBDMFolder(File bonitaHomeFolder, final long tenantId) throws IOException {
        final Folder folder = getFolder(getTenantWorkFolder(bonitaHomeFolder, tenantId), "data-management-client");
        folder.createIfNotExists();
        return folder;
    }
}
