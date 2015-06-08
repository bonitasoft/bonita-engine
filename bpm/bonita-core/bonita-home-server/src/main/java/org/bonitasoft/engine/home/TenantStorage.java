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
import java.util.logging.FileHandler;

import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.io.IOUtil;

/**
 * @author Baptiste Mesta
 */
public class TenantStorage {
    private final BonitaHomeServer bonitaHomeServer;

    public TenantStorage(BonitaHomeServer bonitaHomeServer) {

        this.bonitaHomeServer = bonitaHomeServer;
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


    public void storeSecurityScript(long tenantId, String scriptFileContent, String fileName, String[] folders) throws IOException, BonitaHomeNotSetException {
        Folder current = FolderMgr.getTenantWorkSecurityFolder(getBonitaHomeFolder(), tenantId);
        for (String folder : folders) {
            current = new Folder(current, folder);
            current.create();
        }
        final File fileToWrite = current.newFile(fileName);
        org.bonitasoft.engine.commons.io.IOUtil.writeFile(fileToWrite, scriptFileContent);
    }

    public File getSecurityScriptsFolder(long tenantId) throws BonitaHomeNotSetException, IOException {
        return FolderMgr.getTenantWorkSecurityFolder(getBonitaHomeFolder(), tenantId).getFile();
    }

    public FileHandler getIncidentFileHandler(long tenantId) throws BonitaHomeNotSetException, IOException {
        final File incidentFile = FolderMgr.getTenantWorkFolder(getBonitaHomeFolder(), tenantId).getFile("incidents.log");
        return new FileHandler(incidentFile.getAbsolutePath());

    }

    public File getBonitaHomeFolder() throws BonitaHomeNotSetException {
        return bonitaHomeServer.getBonitaHomeFolder();
    }
}
