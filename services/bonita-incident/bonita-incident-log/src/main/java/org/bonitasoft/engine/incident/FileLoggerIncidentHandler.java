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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.home.BonitaHomeServer;

/**
 * Report incident in a log file located inside the bonita home
 * Is a tenant service must be declared in tenant part
 * 
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class FileLoggerIncidentHandler implements IncidentHandler {


    private final Map<Long, Logger> loggers;

    public FileLoggerIncidentHandler() {
        loggers = new HashMap<Long, Logger>(2);
    }

    @Override
    public void handle(final long tenantId, final Incident incident) {
        try {
            final Logger logger = getLogger(tenantId);
            logger.log(Level.SEVERE, "An incident occurred: " + incident.getDescription());
            logger.log(Level.SEVERE, "Exception was", incident.getCause());
            logger.log(Level.SEVERE, "We were unable to handle the failure on the elements because of", incident.getExceptionWhenHandlingFailure());
            final String recoveryProcedure = incident.getRecoveryProcedure();
            if (recoveryProcedure != null && !recoveryProcedure.isEmpty()) {
                logger.log(Level.SEVERE, "Procedure to recover: " + recoveryProcedure);
            }
        } catch (final SecurityException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        } catch (BonitaHomeNotSetException e) {
            e.printStackTrace();
        }
    }

    protected Logger getLogger(final long tenantId) throws SecurityException, IOException, BonitaHomeNotSetException {
        Logger logger = loggers.get(tenantId);
        if (logger == null) {
            logger = Logger.getLogger("INCIDENT" + tenantId);
            final FileHandler fh = BonitaHomeServer.getInstance().getIncidentFileHandler(tenantId);
            logger.addHandler(fh);
            final SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            loggers.put(tenantId, logger);
        }
        return logger;
    }

}
