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
package org.bonitasoft.engine.execution.work;

import java.util.Map;

import org.bonitasoft.engine.incident.Incident;
import org.bonitasoft.engine.incident.IncidentService;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.work.BonitaWork;

/**
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public class FailureHandlingBonitaWork extends WrappingBonitaWork {

    private static final long serialVersionUID = 1L;

    protected transient TechnicalLoggerService loggerService;

    private transient SessionService sessionService;

    private transient SessionAccessor sessionAccessor;

    public FailureHandlingBonitaWork(final BonitaWork work) {
        super(work);
    }

    protected void logIncident() {
        Incident incident = new Incident(getDescription(), getRecoveryProcedure());
        IncidentService incidentService = getTenantAccessor().getIncidentService();
        incidentService.report(incident);
    }

    protected TenantServiceAccessor getTenantAccessor() {
        try {
            return TenantServiceSingleton.getInstance(getTenantId());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void work(final Map<String, Object> context) {
        TenantServiceAccessor tenantAccessor = getTenantAccessor();
        loggerService = tenantAccessor.getTechnicalLoggerService();
        sessionAccessor = tenantAccessor.getSessionAccessor();
        sessionService = tenantAccessor.getSessionService();
        context.put(TENANT_ACCESSOR, tenantAccessor);
        SSession session = null;
        try {
            session = sessionService.createSession(getTenantId(), "workservice");
            sessionAccessor.setSessionInfo(session.getId(), session.getTenantId());

            if (loggerService.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
                loggerService.log(getClass(), TechnicalLogSeverity.DEBUG, "Starting work: " + getDescription());
            }
            getWrappedWork().work(context);
        } catch (final Exception e) {
            // Edge case we cannot manage
            loggerService.log(getClass(), TechnicalLogSeverity.WARNING,
                    "A work failed, The failure will be handled, work is:  " + getDescription());
            loggerService.log(getClass(), TechnicalLogSeverity.WARNING,
                    "Exception was:" + e.getMessage());
            if (loggerService.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
                loggerService.log(getClass(), TechnicalLogSeverity.DEBUG, e);
            }
            try {
                getWrappedWork().handleFailure(e, context);;
            } catch (Exception e1) {
                loggerService.log(getClass(), TechnicalLogSeverity.ERROR,
                        "Unexpected error while executing work " + getDescription() + ". You may consider restarting the system. This will restart all works.",
                        e);
                loggerService.log(getClass(), TechnicalLogSeverity.ERROR, "Unable to handle the failure ", e);
                logIncident();
            }

        } finally {
            if (session != null) {
                try {
                    sessionAccessor.deleteSessionId();
                    sessionService.deleteSession(session.getId());
                } catch (final SSessionNotFoundException e) {
                    if (loggerService.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
                        loggerService.log(this.getClass(), TechnicalLogSeverity.DEBUG, e);
                    }
                }
            }
        }
    }

}
