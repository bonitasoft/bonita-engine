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

import java.util.List;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * 
 * 
 * Is a tenant service must be declared in tenant part
 * 
 * @author Baptsite Mesta
 * 
 */
public class IncidentServiceImpl implements IncidentService {

    private final List<IncidentHandler> handlers;

    private final TechnicalLoggerService logger;

    public IncidentServiceImpl(TechnicalLoggerService logger, List<IncidentHandler> handlers) {
        this.logger = logger;
        this.handlers = handlers;
    }

    @Override
    public void report(Incident incident) {
        for (IncidentHandler handler : handlers) {
            try {
                handler.handle(incident);
            } catch (Throwable t) {
                logger.log(getClass(), TechnicalLogSeverity.ERROR, "Unable to report an incident using the handler " + handler + " incident was " + incident);
            }
        }
    }

}
