package org.bonitasoft.engine.home;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;

import org.bonitasoft.engine.commons.io.IOUtil;

/**
 * @author Charles Souillard
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

    private static Folder getTempFolder(final File bonitaHomeFolder) throws IOException {
        final Folder tempFolder = getFolder(getServerFolder(bonitaHomeFolder), "temp");
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

    public static Folder getPlatformTempFolder(final File bonitaHomeFolder) throws IOException {
        return getFolder(getWorkFolder(bonitaHomeFolder), "platform");
    }

    private static Folder getTenantsWorkFolder(final File bonitaHomeFolder) throws IOException {
        return getFolder(getWorkFolder(bonitaHomeFolder), "tenants");
    }

    private static Folder getTenantsConfFolder(final File bonitaHomeFolder) throws IOException {
        return getFolder(getConfFolder(bonitaHomeFolder), "tenants");
    }

    private static Folder getTenantsTempFolder(final File bonitaHomeFolder) throws IOException {
        Folder tenants = getFolder(getTempFolder(bonitaHomeFolder), "tenants");
        tenants.createIfNotExists();
        return tenants;
    }

    public static Folder getTenantWorkFolder(final File bonitaHomeFolder, long tenantId) throws IOException {
        return getFolder(getTenantsWorkFolder(bonitaHomeFolder), Long.toString(tenantId));
    }

    public static Folder getTenantConfFolder(final File bonitaHomeFolder, long tenantId) throws IOException {
        return getFolder(getTenantsConfFolder(bonitaHomeFolder), Long.toString(tenantId));
    }
    public static Folder getTenantTempFolder(File bonitaHomeFolder, long tenantId) throws IOException {
        Folder tenantsTempFolder = getTenantsTempFolder(bonitaHomeFolder);
        tenantsTempFolder.createIfNotExists();
        return getFolder(tenantsTempFolder, Long.toString(tenantId));
    }

    public static Folder getTenantTemplateWorkFolder(File bonitaHomeFolder) throws IOException {
        return getFolder(getTenantsWorkFolder(bonitaHomeFolder), "template");
    }

    public static Folder getTenantTemplateConfFolder(File bonitaHomeFolder) throws IOException {
        return getFolder(getTenantsConfFolder(bonitaHomeFolder), "template");
    }

    public static Folder getTenantTemplateTempFolder(File bonitaHomeFolder) throws IOException {
        return getFolder(getTenantsTempFolder(bonitaHomeFolder), "template");
    }

    public static Folder getTenantWorkProcessesFolder(File bonitaHomeFolder, long tenantId) throws IOException {
        return getFolder(getTenantWorkFolder(bonitaHomeFolder, tenantId), "processes");
    }

    public static Folder getTenantTempProcessesFolder(File bonitaHomeFolder, long tenantId) throws IOException {
        Folder folder = getTenantTempFolder(bonitaHomeFolder, tenantId);
        folder.createIfNotExists();
        return getFolder(folder, "processes");
    }

    public static Folder getTenantWorkProcessFolder(File bonitaHomeFolder, long tenantId, long processId) throws IOException {
        return getFolder(getTenantWorkProcessesFolder(bonitaHomeFolder, tenantId), Long.toString(processId));
    }

    public static Folder getTenantTempProcessFolder(File bonitaHomeFolder, long tenantId, long processId) throws IOException {
        Folder tenantTempProcessesFolder = getTenantTempProcessesFolder(bonitaHomeFolder, tenantId);
        tenantTempProcessesFolder.createIfNotExists();
        Folder folder = getFolder(tenantTempProcessesFolder, Long.toString(processId));
        folder.createIfNotExists();
        return folder;
    }

    public static void deleteTenant(File bonitaHomeFolder, long tenantId) throws IOException {
        getTenantWorkFolder(bonitaHomeFolder, tenantId).delete();
        getTenantConfFolder(bonitaHomeFolder, tenantId).delete();
        getTenantTempFolder(bonitaHomeFolder, tenantId).delete();
    }


    public static void createTenant(File bonitaHomeFolder, long tenantId) throws IOException {
        final Folder tenantWorkFolder = getTenantWorkFolder(bonitaHomeFolder, tenantId);
        final Folder tenantConfFolder = getTenantConfFolder(bonitaHomeFolder, tenantId);
        final Folder tenantTempFolder = getTenantTempFolder(bonitaHomeFolder, tenantId);
        final Folder templateWorkFolder = FolderMgr.getTenantTemplateWorkFolder(bonitaHomeFolder);
        final Folder templateConfFolder = FolderMgr.getTenantTemplateConfFolder(bonitaHomeFolder);
        final Folder templateTempFolder = FolderMgr.getTenantTemplateTempFolder(bonitaHomeFolder);

        // copy configuration file
        try {
            templateWorkFolder.copyTo(tenantWorkFolder);
            templateConfFolder.copyTo(tenantConfFolder);
            templateTempFolder.copyTo(tenantTempFolder);
            getTenantWorkProcessesFolder(bonitaHomeFolder, tenantId).create();
            getTenantTempProcessesFolder(bonitaHomeFolder, tenantId).create();
            getTenantWorkSecurityFolder(bonitaHomeFolder, tenantId).create();
        } catch (final IOException e) {
            deleteTenant(bonitaHomeFolder, tenantId);
            throw e;
        }
    }

    public static void createTenantWorkProcessFolder(File bonitaHomeFolder, long tenantId, long processId) throws IOException {
        getTenantWorkProcessFolder(bonitaHomeFolder, tenantId, processId).create();
        getTenantWorkProcessClasspathFolder(bonitaHomeFolder, tenantId, processId).create();
        getTenantWorkProcessConnectorsFolder(bonitaHomeFolder, tenantId, processId).create();
        getTenantWorkProcessDocumentFolder(bonitaHomeFolder, tenantId, processId).create();
        getTenantWorkProcessUserFiltersFolder(bonitaHomeFolder, tenantId, processId).create();
    }

    public static void createTenantTempProcessFolder(File bonitaHomeFolder, long tenantId, long processId) throws IOException {
        getTenantTempProcessFolder(bonitaHomeFolder, tenantId, processId).create();
        //getTenantTempProcessClasspathFolder(bonitaHomeFolder, tenantId, processId).create();
        //getTenantTempProcessConnectorsFolder(bonitaHomeFolder, tenantId, processId).create();
        //getTenantTempProcessDocumentFolder(bonitaHomeFolder, tenantId, processId).create();
        //getTenantTempProcessUserFiltersFolder(bonitaHomeFolder, tenantId, processId).create();
    }

    public static Folder getTenantWorkProcessClasspathFolder(File bonitaHomeFolder, long tenantId, long processId) throws IOException {
        return getFolder(getTenantWorkProcessFolder(bonitaHomeFolder, tenantId, processId), "classpath");
    }

    public static void deleteTenantWorkProcessFolder(File bonitaHomeFolder, long tenantId, long processId) throws IOException {
        final Folder processFolder = getTenantWorkProcessFolder(bonitaHomeFolder, tenantId, processId);
        processFolder.delete();
    }

    public static void deleteTenantTempProcessFolder(File bonitaHomeFolder, long tenantId, long processId) throws IOException {
        final Folder processFolder = getTenantTempProcessFolder(bonitaHomeFolder, tenantId, processId);
        processFolder.delete();
    }

    public static Folder getTenantWorkProcessDocumentFolder(File bonitaHomeFolder, long tenantId, long processId) throws IOException {
        return getFolder(getTenantWorkProcessFolder(bonitaHomeFolder, tenantId, processId), "documents");
    }

    public static Folder getTenantWorkProcessConnectorsFolder(File bonitaHomeFolder, long tenantId, long processId) throws IOException {
        return getFolder(getTenantWorkProcessFolder(bonitaHomeFolder, tenantId, processId), "connector");
    }

    public static Folder getTenantWorkSecurityFolder(File bonitaHomeFolder, long tenantId) throws IOException {
        return getFolder(getTenantWorkFolder(bonitaHomeFolder, tenantId), "security-scripts");
    }

    public static Folder getTenantWorkProcessUserFiltersFolder(File bonitaHomeFolder, long tenantId, long processId) throws IOException {
        return getFolder(getTenantWorkProcessFolder(bonitaHomeFolder, tenantId, processId), "userFilters");
    }

    public static Folder getPlatformClassLoaderFolder(File bonitaHomeFolder) throws IOException {
        final Folder classloadersFolder = getFolder(getPlatformTempFolder(bonitaHomeFolder), "classloaders");
        classloadersFolder.createIfNotExists();
        final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        final String subFolderName = "bonita_engine_" + jvmName;
        final Folder jvmFolder = getFolder(getPlatformTempFolder(bonitaHomeFolder), subFolderName);
        jvmFolder.createIfNotExists();
        return jvmFolder;
    }

    public static Folder getPlatformGobalClassLoaderFolder(File bonitaHomeFolder) throws IOException {
        final Folder globalFolder = getFolder(getPlatformClassLoaderFolder(bonitaHomeFolder), "global");
        globalFolder.createIfNotExists();
        return globalFolder;
    }

    private static Folder getPlatformLocalClassLoaderFolder(File bonitaHomeFolder) throws IOException {
        final Folder localFolder = getFolder(getPlatformClassLoaderFolder(bonitaHomeFolder), "local");
        localFolder.createIfNotExists();
        return localFolder;
    }

    public static Folder getPlatformLocalClassLoaderFolder(File bonitaHomeFolder, String artifactType, long artifactId) throws IOException {
        final Folder localFolder = getPlatformLocalClassLoaderFolder(bonitaHomeFolder);
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
