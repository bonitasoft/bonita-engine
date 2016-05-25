/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.FileHandler;

import org.bonitasoft.engine.exception.BonitaHomeNotSetException;

/**
 * Handles tenant related files of the bonita home, mainly bdm and security scripts
 *
 * @author Baptiste Mesta
 */
public class TenantStorage {

    static final String INCIDENT_LOG_FOLDER_PROPERTY = "org.bonitasoft.engine.incident.folder";
    public static final String INCIDENTS_LOG_FILENAME = "incidents.log";

    TenantStorage() {
    }

    private void createFolders(Folder current) {
        if (!current.exists()) {
            current.getFile().mkdirs();
        }
    }

    public FileHandler getIncidentFileHandler(long tenantId) throws BonitaHomeNotSetException, IOException {
        final String incidentLogFolder = System.getProperty(INCIDENT_LOG_FOLDER_PROPERTY);
        if (incidentLogFolder != null) {
            final Path tenantFolder = Paths.get(incidentLogFolder).resolve("tenants").resolve("" + tenantId);
            createFolders(new Folder(tenantFolder.toFile()));
            return getFileHandler(tenantFolder.resolve(INCIDENTS_LOG_FILENAME).toString());
        } else {
            Folder tenantWorkFolder = getTenantTempFolder(tenantId);
            final File incidentFile = tenantWorkFolder.getFile(INCIDENTS_LOG_FILENAME);
            return getFileHandler(incidentFile.getAbsolutePath());
        }
    }

    FileHandler getFileHandler(String incidentFileAbsolutePath) throws IOException {
        return new FileHandler(incidentFileAbsolutePath);
    }

    public File getProfileMD5(long tenantId) throws BonitaHomeNotSetException, IOException {
        Folder tenantWorkFolder = getTenantTempFolder(tenantId);
        return tenantWorkFolder.getFile("profiles.md5");

    }

    Folder getTenantTempFolder(long tenantId) throws IOException, BonitaHomeNotSetException {
        Folder tenantWorkFolder = FolderMgr.getTenantTempFolder(tenantId);
        createFolders(tenantWorkFolder);
        return tenantWorkFolder;
    }

}
