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
package org.bonitasoft.engine.execution.work;

import java.util.Map;

import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
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

    public FailureHandlingBonitaWork(final BonitaWork work) {
        super(work);
    }

    protected void logIncident(final Exception cause, final Exception exceptionWhenHandlingFailure) {
        final Incident incident = new Incident(getDescription(), getRecoveryProcedure(), cause, exceptionWhenHandlingFailure);
        final IncidentService incidentService = getTenantAccessor().getIncidentService();
        incidentService.report(getTenantId(), incident);
    }

    TenantServiceAccessor getTenantAccessor() {
        try {
            return TenantServiceSingleton.getInstance(getTenantId());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void work(final Map<String, Object> context) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TechnicalLoggerService loggerService = tenantAccessor.getTechnicalLoggerService();
        final SessionAccessor sessionAccessor = tenantAccessor.getSessionAccessor();
        context.put(TENANT_ACCESSOR, tenantAccessor);
        try {
            sessionAccessor.setTenantId(getTenantId());

            if (loggerService.isLoggable(getClass(), TechnicalLogSeverity.TRACE)) {
                loggerService.log(getClass(), TechnicalLogSeverity.TRACE, "Starting work: " + getDescription());
            }
            getWrappedWork().work(context);
        } catch (final SExpressionEvaluationException e) {
            // To do before log, because we want to set the context of the exception.
            handleFailureWrappedWork(loggerService, e, context);
        } catch (final Exception e) {
            handleFailure(e, context);
        } finally {
            sessionAccessor.deleteTenantId();
        }
    }

    @Override
    public void handleFailure(final Exception e, final Map<String, Object> context) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TechnicalLoggerService loggerService = tenantAccessor.getTechnicalLoggerService();
        // final Edge case we cannot manage
        if (loggerService.isLoggable(getClass(), TechnicalLogSeverity.ERROR)) {
            loggerService.log(getClass(), TechnicalLogSeverity.ERROR, "The work [" + getDescription() + "] failed. The failure will be handled.");
        }
        // To do before log, because we want to set the context of the exception.
        handleFailureWrappedWork(loggerService, e, context);
    }

    private void logException(final TechnicalLoggerService loggerService, final Throwable e) {
        if (loggerService.isLoggable(getClass(), TechnicalLogSeverity.ERROR)) {
            final StringBuilder logBuilder = new StringBuilder(e.getClass().getName());
            logBuilder.append(" : \"");
            final String message = e.getMessage();
            if (message == null || message.isEmpty()) {
                logBuilder.append("No message");
            } else {
                logBuilder.append(message);
            }
            logBuilder.append("\"");
            loggerService.log(getClass(), TechnicalLogSeverity.ERROR, logBuilder.toString(), e);
        }
    }

    private void handleFailureWrappedWork(final TechnicalLoggerService loggerService, final Exception e, final Map<String, Object> context) {
        try {
            getWrappedWork().handleFailure(e, context);
            logException(loggerService, e);
        } catch (final Exception e1) {
            loggerService.log(getClass(), TechnicalLogSeverity.ERROR, "Unexpected error while executing work [" + getDescription() + "]"
                    + ". You may consider restarting the system. This will restart all works.", e);
            loggerService.log(getClass(), TechnicalLogSeverity.ERROR, "Unable to handle the failure. ", e1);
            logIncident(e, e1);
        }
    }

}
