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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Baptsite Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */

public class IncidentServiceImpl implements IncidentService {

    private Logger logger = LoggerFactory.getLogger(IncidentServiceImpl.class);
    private final List<IncidentHandler> handlers;

    public IncidentServiceImpl(final List<IncidentHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public void report(final long tenantId, final Incident incident) {
        for (final IncidentHandler handler : handlers) {
            try {
                handler.handle(tenantId, incident);
            } catch (final Exception t) {
                logger.error("Unable to report an incident using the handler " + handler + " incident was " + incident);
            }
        }
    }

}
