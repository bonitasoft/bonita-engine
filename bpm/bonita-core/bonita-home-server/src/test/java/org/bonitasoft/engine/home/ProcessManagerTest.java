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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.bonitasoft.engine.commons.io.IOUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class ProcessManagerTest {

    @Mock
    private BonitaHomeServer bonitaHomeServer;


    @InjectMocks
    @Spy
    private ProcessManager processManager;


    @Test
    public void storeConnectorFileShouldEnsureParentFoldersExist() throws Exception {
        final String home = System.getProperty("java.io.tmpdir") + File.separator + "home-test";
        final File homeFile = new File(home);
        doReturn(homeFile).when(bonitaHomeServer).getBonitaHomeFolder();
        final String oldBonitaHome = System.setProperty(BonitaHome.BONITA_HOME, home);
        final long tenantId = 44L;
        final long processId = 5421L;
        final File connectorFolder = FolderMgr.getTenantWorkProcessConnectorsFolder(BonitaHomeServer.getInstance().getBonitaHomeFolder(), tenantId, processId)
                .getFile();

        connectorFolder.mkdirs();

        try {
            final String resourceName = "src/net/dummy/MyConnectorImpl.java";
            processManager.storeConnectorFile(tenantId, processId, resourceName, "someBytes".getBytes());

            verify(processManager).createFoldersIfNecessary(new File(connectorFolder, resourceName).getParentFile());

        } finally {
            if (oldBonitaHome != null) {
                System.setProperty(BonitaHome.BONITA_HOME, oldBonitaHome);
            }
            IOUtil.deleteDir(homeFile);
        }
    }

}