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

import java.util.List;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * @author Baptsite Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class IncidentServiceImpl implements IncidentService {

    private final List<IncidentHandler> handlers;

    private final TechnicalLoggerService logger;

    public IncidentServiceImpl(final TechnicalLoggerService logger, final List<IncidentHandler> handlers) {
        this.logger = logger;
        this.handlers = handlers;
    }

    @Override
    public void report(final long tenantId, final Incident incident) {
        for (final IncidentHandler handler : handlers) {
            try {
                handler.handle(tenantId, incident);
            } catch (final Exception t) {
                logger.log(getClass(), TechnicalLogSeverity.ERROR, "Unable to report an incident using the handler " + handler + " incident was " + incident);
            }
        }
    }

}
