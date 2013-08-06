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

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.sessionaccessor.TenantIdNotSetException;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.transaction.TransactionState;

public abstract class AbstractWorkSynchronization implements BonitaTransactionSynchronization {

    private final Collection<AbstractBonitaWork> works;

    protected final ExecutorService executorService;

    private boolean executed = false;

    protected final TechnicalLoggerService loggerService;

    private final SessionAccessor sessionAccessor;

    private final SessionService sessionService;

    private final TransactionService transactionService;

    private long tenantId;

    private final ExecutorWorkService threadPoolWorkService;

    public AbstractWorkSynchronization(final ExecutorWorkService threadPoolWorkService, final ExecutorService executorService,
            final TechnicalLoggerService loggerService, final SessionAccessor sessionAccessor, final SessionService sessionService,
            final TransactionService transactionService) {
        super();
        this.threadPoolWorkService = threadPoolWorkService;
        this.executorService = executorService;
        this.loggerService = loggerService;
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
        this.transactionService = transactionService;
        works = new HashSet<AbstractBonitaWork>();
        try {
            tenantId = sessionAccessor.getTenantId();
        } catch (final TenantIdNotSetException e) {
            tenantId = -1l;// we are not in a tenant
        }
    }

    public long getTenantId() {
        return tenantId;
    }

    public void addWork(final AbstractBonitaWork work) {
        works.add(work);
    }

    @Override
    public void beforeCommit() {
        // NOTHING
    }

    @Override
    public void afterCompletion(final TransactionState transactionStatus) {
        if (TransactionState.COMMITTED == transactionStatus) {
            for (final AbstractBonitaWork work : works) {
                work.setTechnicalLogger(loggerService);
                work.setSessionAccessor(sessionAccessor);
                work.setSessionService(sessionService);
                work.setTransactionService(transactionService);
                work.setTenantId(tenantId);
            }
            if (!threadPoolWorkService.isStopped(tenantId)) {
                executeRunnables(works);
            }
        }
        executed = true;
    }

    protected abstract void executeRunnables(Collection<AbstractBonitaWork> works);

    public boolean isExecuted() {
        return executed;
    }
}
