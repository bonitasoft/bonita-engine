/**
 * Copyright (C) 2019 Bonitasoft S.A.
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

import org.bonitasoft.engine.exception.BonitaHomeNotSetException;

/**
 * Handles tenant related files of the bonita home, mainly bdm and security scripts
 *
 * @author Baptiste Mesta
 */
public class TenantStorage {

    TenantStorage() {
    }

    private void createFolders(Folder current) {
        if (!current.exists()) {
            current.getFile().mkdirs();
        }
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
