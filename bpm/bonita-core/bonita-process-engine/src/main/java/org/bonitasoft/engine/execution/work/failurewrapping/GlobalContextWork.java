/**
 * Copyright (C) 2014 Bonitasoft S.A.
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
package org.bonitasoft.engine.execution.work.failurewrapping;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.work.BonitaWork;

/**
 * @author Aurelien Pupier
 * 
 */
public class GlobalContextWork extends TxInHandleFailureWrappingWork {

    private static final long serialVersionUID = -6043722230605068850L;

    private static String hostname = null;

    private static boolean hostnameResolutionAlreadyTried;

    public GlobalContextWork(BonitaWork work) {
        super(work);
    }

    @Override
    protected void setExceptionContext(SBonitaException sBonitaException, Map<String, Object> context) {
        sBonitaException.setThreadId(retrieveThreadId());
        sBonitaException.setTenantID(getTenantId());
        fillHostnameContextForException(sBonitaException, context);
    }

    long retrieveThreadId() {
        return Thread.currentThread().getId();
    }

    private void fillHostnameContextForException(SBonitaException be, final Map<String, Object> context) {
        if (hostname == null && !hostnameResolutionAlreadyTried) {
            hostnameResolutionAlreadyTried = true;
            try {
                hostname = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                technicalDebugLog(e, context);
            }
        }
        if (hostname != null) {
            be.setHostname(hostname);
        }
    }

    private void technicalDebugLog(final Throwable throwableToLog, final Map<String, Object> context) {
        TechnicalLoggerService technicalLogger = getTenantAccessor(context).getTechnicalLoggerService();
        if (technicalLogger != null && technicalLogger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
            technicalLogger.log(this.getClass(), TechnicalLogSeverity.DEBUG, throwableToLog);
        }
    }

}
