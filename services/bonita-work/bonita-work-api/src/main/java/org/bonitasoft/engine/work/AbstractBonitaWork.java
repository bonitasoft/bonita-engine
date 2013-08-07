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
package org.bonitasoft.engine.work;

import java.io.Serializable;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public abstract class AbstractBonitaWork implements Runnable, Serializable {

    private static final long serialVersionUID = -3346630968791356467L;

    protected TechnicalLoggerService loggerService;

    private SessionService sessionService;

    private SessionAccessor sessionAccessor;

    private long tenantId;

    protected TransactionService transactionService;

    public AbstractBonitaWork() {
        super();
    }

    @Override
    public final void run() {
        SSession session = null;
        try {
            session = sessionService.createSession(tenantId, "workservice");
            sessionAccessor.setSessionInfo(session.getId(), session.getTenantId());

            loggerService.log(getClass(), TechnicalLogSeverity.DEBUG, "Starting work: " + getDescription());
            if (isTransactional()) {
                workInTransaction();
            } else {
                work();
            }
        } catch (final SBonitaException e) {
            handleError(e);
        } catch (final Exception e) {
            // Edge case we cannot manage
            loggerService.log(getClass(), TechnicalLogSeverity.ERROR,
                    "Unexpected error while executing work. You may consider restarting the system. This will restart all works.", e);
        } finally {
            if (session != null) {
                try {
                    sessionAccessor.deleteSessionId();
                    sessionService.deleteSession(session.getId());
                } catch (final SSessionNotFoundException e) {
                    loggerService.log(this.getClass(), TechnicalLogSeverity.DEBUG, e);
                }
            }
        }
    }

    protected abstract boolean isTransactional();

    protected abstract String getDescription();

    protected abstract void work() throws Exception;

    protected void workInTransaction() throws Exception {
        final Callable<Void> runWork = new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                work();
                return null;
            }
        };

        // Call the method work() wrapped in a transaction.
        transactionService.executeInTransaction(runWork);
    }

    protected void handleError(final SBonitaException e) {
        throw new IllegalStateException("Must be implemented in sub-classes to handle Set Failed, or log severe message with procedure to restart.", e);
    }

    protected long getTenantId() {
        return tenantId;
    }

    public void setTenantId(final long tenantId) {
        this.tenantId = tenantId;
    }

    public void setTechnicalLogger(TechnicalLoggerService loggerService) {
        this.loggerService = loggerService;
    }

    public void setSessionService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    public void setSessionAccessor(SessionAccessor sessionAccessor) {
        this.sessionAccessor = sessionAccessor;
    }

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

}
