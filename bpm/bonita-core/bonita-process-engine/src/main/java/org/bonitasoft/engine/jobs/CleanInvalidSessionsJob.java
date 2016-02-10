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
package org.bonitasoft.engine.jobs;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.scheduler.exception.SJobExecutionException;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.session.SessionService;

/**
 * @author Elias Ricken de Medeiros
 */
public class CleanInvalidSessionsJob extends InternalJob {

    private static final long serialVersionUID = 2448120492184242153L;

    @Override
    public String getName() {
        return "CleanInvalidSessionsJob";
    }

    @Override
    public String getDescription() {
        return "Clean all invalid sessions";
    }

    @Override
    public void execute() throws SJobExecutionException {
        try {
            final SessionService sessionService = getTenantServiceAccessor().getSessionService();
            sessionService.cleanInvalidSessions();
        } catch (final Exception e) {
            throw new SJobExecutionException("Unable to clean invalid sessions", e);
        }
    }

    @Override
    public void setAttributes(final Map<String, Serializable> attributes) {

    }

}
