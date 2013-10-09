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
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.work.BonitaWork;

/**
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public class FailureHandlingBonitaWork extends WrappingBonitaWork {

    private static final long serialVersionUID = 1L;

    protected transient TechnicalLoggerService loggerService;

    private transient SessionAccessor sessionAccessor;

    public FailureHandlingBonitaWork(final BonitaWork work) {
        super(work);
    }

    protected void logIncident(final Throwable cause, final Throwable exceptionWhenHandlingFailure) {
        final Incident incident = new Incident(getDescription(), getRecoveryProcedure(), cause, exceptionWhenHandlingFailure);
        final IncidentService incidentService = getTenantAccessor().getIncidentService();
        incidentService.report(getTenantId(), incident);
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
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        loggerService = tenantAccessor.getTechnicalLoggerService();
        sessionAccessor = tenantAccessor.getSessionAccessor();
        context.put(TENANT_ACCESSOR, tenantAccessor);
        try {
            sessionAccessor.setTenantId(getTenantId());

            if (loggerService.isLoggable(getClass(), TechnicalLogSeverity.TRACE)) {
                loggerService.log(getClass(), TechnicalLogSeverity.TRACE, "Starting work: " + getDescription());
            }
            getWrappedWork().work(context);
        } catch (final Throwable e) {
            // Edge case we cannot manage
            loggerService.log(getClass(), TechnicalLogSeverity.WARNING, "A work failed, The failure will be handled, work is:  " + getDescription());
            loggerService.log(getClass(), TechnicalLogSeverity.WARNING, "Exception was:" + e.getMessage(), e);

            try {
                getWrappedWork().handleFailure(e, context);
            } catch (final Throwable e1) {
                loggerService.log(getClass(), TechnicalLogSeverity.ERROR, "Unexpected error while executing work " + getDescription()
                        + ". You may consider restarting the system. This will restart all works.", e);
                loggerService.log(getClass(), TechnicalLogSeverity.ERROR, "Unable to handle the failure ", e);
                logIncident(e, e1);
            }

        } finally {
            sessionAccessor.deleteTenantId();
        }
    }

}
