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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.io.PropertiesManager;

/**
 * @author Baptiste Mesta
 */
public class TenantManager {
    private final BonitaHomeServer bonitaHomeServer;

    public TenantManager(BonitaHomeServer bonitaHomeServer) {
        this.bonitaHomeServer = bonitaHomeServer;
    }

    public void createTenant(final long tenantId) throws BonitaHomeNotSetException, IOException {
        FolderMgr.createTenant(bonitaHomeServer.getBonitaHomeFolder(), tenantId);
        //put the right id in tenant properties file
        final File file = FolderMgr.getTenantWorkFolder(bonitaHomeServer.getBonitaHomeFolder(), tenantId).getFile("bonita-tenant-id.properties");
        //maybe a replace is better?
        final Properties tenantProperties = new Properties();
        tenantProperties.put("tenantId", String.valueOf(tenantId));
        PropertiesManager.saveProperties(tenantProperties, file);
    }

    public void deleteTenant(final long tenantId) throws BonitaHomeNotSetException, IOException {
        FolderMgr.deleteTenant(bonitaHomeServer.getBonitaHomeFolder(), tenantId);
    }

    public File getTenantWorkFile(long tenantId, String fileName) throws BonitaHomeNotSetException, IOException {
        return FolderMgr.getTenantWorkFolder(bonitaHomeServer.getBonitaHomeFolder(), tenantId).getFile(fileName);
    }

    public void modifyTechnicalUser(long tenantId, String userName, String password) throws IOException, BonitaHomeNotSetException {
        final Folder workFolder = FolderMgr.getTenantWorkFolder(bonitaHomeServer.getBonitaHomeFolder(), tenantId);
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

}
