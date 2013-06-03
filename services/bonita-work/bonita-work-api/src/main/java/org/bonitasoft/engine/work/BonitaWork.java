/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.work;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Baptiste Mesta
 */
public abstract class BonitaWork implements Runnable {

    private TechnicalLoggerService loggerService;

    private TransactionService transactionService;

    private SessionService sessionService;

    private SessionAccessor sessionAccessor;

    private long tenantId;

    protected abstract void work() throws SBonitaException;

    @Override
    public void run() {
        SSession session = null;
        try {
            session = createSession();// FIXME get the technical user of the tenant
            sessionAccessor.setSessionInfo(session.getId(), session.getTenantId());// FIXME do that in the session service?
            work();
        } catch (final SBonitaException e) {
            loggerService.log(getClass(), TechnicalLogSeverity.ERROR, "Error while executing work", e);
        } catch (final Throwable e) {
            loggerService.log(getClass(), TechnicalLogSeverity.ERROR, "Unexpected error while executing work", e);
        } finally {
            if (session != null) {
                try {
                    sessionAccessor.deleteSessionId();
                    sessionService.deleteSession(session.getId());
                } catch (final SSessionNotFoundException e) {
                    loggerService.log(this.getClass(), TechnicalLogSeverity.ERROR, e);// FIXME
                }
            }
        }
    }

    private SSession createSession() throws SBonitaException {
        SSession session = null;
        try {
            transactionService.begin();
            session = sessionService.createSession(tenantId, "scheduler");
        } catch (final SBonitaException e) {
            transactionService.setRollbackOnly();
            throw e;
        } finally {
            transactionService.complete();
        }
        return session;
    }

    public void setTechnicalLogger(final TechnicalLoggerService loggerService) {
        this.loggerService = loggerService;
    }

    public void setSessionAccessor(final SessionAccessor sessionAccessor) {
        this.sessionAccessor = sessionAccessor;
    }

    public void setSessionService(final SessionService sessionService) {
        this.sessionService = sessionService;
    }

    public void setTenantId(final long tenantId) {
        this.tenantId = tenantId;
    }

    public void setTransactionService(final TransactionService transactionService) {
        this.transactionService = transactionService;
    }
}
