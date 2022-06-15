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
package org.bonitasoft.engine.incident;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Report incident in a log file located inside the bonita home
 * Is a tenant service must be declared in tenant part
 *
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */

public class FileLoggerIncidentHandler implements IncidentHandler {

    Logger logger = LoggerFactory.getLogger(FileLoggerIncidentHandler.class);

    public FileLoggerIncidentHandler() {
    }

    @Override
    public void handle(final long tenantId, final Incident incident) {
        Marker INCIDENT = MarkerFactory.getMarker("INCIDENT");
        logger.error(INCIDENT, "An incident on tenant id {} occurred: {}", tenantId, incident.getDescription());
        logger.error(INCIDENT, "Exception was:", incident.getCause());
        logger.error(INCIDENT, "We were unable to handle the failure on the elements because of",
                incident.getExceptionWhenHandlingFailure());
        final String recoveryProcedure = incident.getRecoveryProcedure();
        if (recoveryProcedure != null && !recoveryProcedure.isEmpty()) {
            logger.error(INCIDENT, "Procedure to recover: " + recoveryProcedure);
        }
    }

}
