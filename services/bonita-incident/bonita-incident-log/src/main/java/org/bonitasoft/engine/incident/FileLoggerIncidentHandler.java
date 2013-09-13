/**
 * Copyright (C) 2013 BonitaSoft S.A.
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

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.home.BonitaHomeServer;

/**
 * 
 * Report incident in a log file located inside the bonita home
 * 
 * 
 * Is a tenant service must be declared in tenant part
 * 
 * @author Baptiste Mesta
 * 
 */
public class FileLoggerIncidentHandler implements IncidentHandler {

    private final Logger logger;

    public FileLoggerIncidentHandler(final String logFile, final long tenantId) throws BonitaHomeNotSetException, SecurityException, IOException {
        String tenantFolder = BonitaHomeServer.getInstance().getTenantFolder(tenantId);
        logger = Logger.getLogger("INCIDENT");
        FileHandler fh;
        fh = new FileHandler(tenantFolder + File.separatorChar + logFile);
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);

    }

    @Override
    public void handle(final Incident incident) {
        logger.log(Level.SEVERE, "An incident occurred: " + incident.getDescription());
        logger.log(Level.SEVERE, "Exception was", incident.getCause());
        logger.log(Level.SEVERE, "We were unable to handle the failure on the elements because of", incident.getExceptionWhenHandlingFailure());
        String recoveryProcedure = incident.getRecoveryProcedure();
        if (recoveryProcedure != null && !recoveryProcedure.isEmpty()) {
            logger.log(Level.SEVERE, "Procedure to recover: " + recoveryProcedure);
        }
    }
}
