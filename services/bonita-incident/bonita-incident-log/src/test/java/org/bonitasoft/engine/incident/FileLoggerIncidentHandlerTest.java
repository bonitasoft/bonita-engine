/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.incident;

import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.home.TenantStorage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BonitaHomeServer.class)
public class FileLoggerIncidentHandlerTest {

	@Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Mock
    private BonitaHomeServer bonitaHomeServer;
    @Mock
    private TenantStorage tenantStorage;
	
    @Test
    public void should_write_into_file_when_handle_incident() throws Exception {
        mockStatic(BonitaHomeServer.class);

    	//Given
    	long tenantId =1;
        final File logFolder = temporaryFolder.newFolder("logFolder");
        @SuppressWarnings("unused")
        final File tenantLogFolder = temporaryFolder.newFolder("logFolder" + File.separatorChar + tenantId);
        final File incidentFile = new File(tenantLogFolder, "incident.log");
        final FileHandler fh = new FileHandler(incidentFile.getAbsolutePath());

        when(BonitaHomeServer.getInstance()).thenReturn(bonitaHomeServer);
        when(bonitaHomeServer.getTenantStorage()).thenReturn(tenantStorage);
        doReturn(fh).when(tenantStorage).getIncidentFileHandler(tenantId);

    	final Incident incident = new Incident("test", "recovery", new Exception("an unexpected exception"), new Exception("unable to handle failure"));
        FileLoggerIncidentHandler fileLoggerIncidentHandler = new FileLoggerIncidentHandler();
        
    	//When
        fileLoggerIncidentHandler.handle(tenantId, incident);

        String content = org.bonitasoft.engine.io.IOUtil.read(incidentFile);
        
        // Close the open stream to allow to delete the logfile on the file system
        Logger logger = fileLoggerIncidentHandler.getLogger(tenantId);
        Handler[] handlers = logger.getHandlers();
        for (Handler handler : handlers) {
        	handler.close();
		}
        
        //Then
        assertTrue("File content is: " + content, content.contains("An incident occurred: test"));
        assertTrue("File content is: " + content, content.contains("an unexpected exception"));
        assertTrue("File content is: " + content, content.contains("unable to handle failure"));
        assertTrue("File content is: " + content, content.contains("Procedure to recover: recovery"));
        
    }

}
