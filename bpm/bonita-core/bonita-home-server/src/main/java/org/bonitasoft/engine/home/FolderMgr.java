package org.bonitasoft.engine.home;

import java.io.File;
import java.io.IOException;

/**
 * @author Charles Souillard
 */
class FolderMgr {


    private static Folder getFolder(final File baseFolder, final String... subSequentFolders) throws IOException {
        return new Folder(baseFolder, subSequentFolders);
    }

    private static Folder getFolder(final Folder baseFolder, final String... subSequentFolders) throws IOException {
        return new Folder(baseFolder, subSequentFolders);
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
        return getFolder(getServerFolder(bonitaHomeFolder), "temp");
    }

    public static Folder getPlatformInitWorkFolder(final File bonitaHomeFolder) throws IOException {
        return getFolder(getWorkFolder(bonitaHomeFolder), "platform-init");
    }

    public static Folder getPlatformWorkFolder(final File bonitaHomeFolder) throws IOException {
        return getFolder(getWorkFolder(bonitaHomeFolder), "platform");
    }

    public static Folder getPlatformConfFolder(final File bonitaHomeFolder) throws IOException {
        return getFolder(getConfFolder(bonitaHomeFolder), "platform");
    }

    private static Folder getTenantsWorkFolder(final File bonitaHomeFolder) throws IOException {
        return getFolder(getWorkFolder(bonitaHomeFolder), "tenants");
    }

    private static Folder getTenantsConfFolder(final File bonitaHomeFolder) throws IOException {
        return getFolder(getConfFolder(bonitaHomeFolder), "tenants");
    }

    private static Folder getTenantsTempFolder(final File bonitaHomeFolder) throws IOException {
        return getFolder(getTempFolder(bonitaHomeFolder), "tenants");
    }

    public static Folder getTenantWorkFolder(final File bonitaHomeFolder, long tenantId) throws IOException {
        return getFolder(getTenantsWorkFolder(bonitaHomeFolder), Long.toString(tenantId));
    }

    public static Folder getTenantConfFolder(final File bonitaHomeFolder, long tenantId) throws IOException {
        return getFolder(getTenantsConfFolder(bonitaHomeFolder), Long.toString(tenantId));
    }
    public static Folder getTenantTempFolder(File bonitaHomeFolder, long tenantId) throws IOException {
        return getFolder(getTenantsTempFolder(bonitaHomeFolder), Long.toString(tenantId));
    }

    public static Folder getTenantTemplateWorkFolder(File bonitaHomeFolder) throws IOException {
        return getFolder(getTenantsWorkFolder(bonitaHomeFolder), "template");
    }

    public static Folder getTenantTemplateConfFolder(File bonitaHomeFolder) throws IOException {
        return getFolder(getTenantsConfFolder(bonitaHomeFolder), "template");
    }

    public static Folder getTenantWorkProcessesFolder(File bonitaHomeFolder, long tenantId) throws IOException {
        return getFolder(getTenantWorkFolder(bonitaHomeFolder, tenantId), "processes");
    }

    public static Folder getTenantTempProcessesFolder(File bonitaHomeFolder, long tenantId) throws IOException {
        return getFolder(getTenantTempFolder(bonitaHomeFolder, tenantId), "processes");
    }

    public static Folder getTenantWorkProcessFolder(File bonitaHomeFolder, long tenantId, long processId) throws IOException {
        return getFolder(getTenantWorkProcessesFolder(bonitaHomeFolder, tenantId), Long.toString(processId));
    }

    public static Folder getTenantTempProcessFolder(File bonitaHomeFolder, long tenantId, long processId) throws IOException {
        return getFolder(getTenantTempProcessesFolder(bonitaHomeFolder, tenantId), Long.toString(processId));
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

        // copy configuration file
        try {
            templateWorkFolder.copyTo(tenantWorkFolder);
            templateConfFolder.copyTo(tenantConfFolder);
            tenantTempFolder.create();
            getTenantWorkProcessesFolder(bonitaHomeFolder, tenantId).create();
        } catch (final IOException e) {
            deleteTenant(bonitaHomeFolder, tenantId);
            throw e;
        }
        final Folder tenantWorkProcessesFolder = getTenantWorkProcessesFolder(bonitaHomeFolder, tenantId);
        tenantWorkProcessesFolder.create();
    }

    public static void createTenantWorkProcessFolder(File bonitaHomeFolder, long tenantId, long processId) throws IOException {
        getTenantWorkProcessFolder(bonitaHomeFolder, tenantId, processId).create();
        getTenantWorkProcessClasspathFolder(bonitaHomeFolder, tenantId, processId).create();
        getTenantWorkProcessConnectorsFolder(bonitaHomeFolder, tenantId, processId).create();
        getTenantWorkProcessDocumentFolder(bonitaHomeFolder, tenantId, processId).create();
        getTenantWorkProcessUserFiltersFolder(bonitaHomeFolder, tenantId, processId).create();
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
        return getFolder(getTenantWorkProcessFolder(bonitaHomeFolder, tenantId, processId), "connectors");
    }

    public static Folder getTenantWorkSecurityFolder(File bonitaHomeFolder, long tenantId) throws IOException {
        return getFolder(getTenantWorkFolder(bonitaHomeFolder, tenantId), "security-scripts");
    }

    public static Folder getTenantWorkProcessUserFiltersFolder(File bonitaHomeFolder, long tenantId, long processId) throws IOException {
        return getFolder(getTenantWorkProcessFolder(bonitaHomeFolder, tenantId, processId), "userFilters");
    }
}
