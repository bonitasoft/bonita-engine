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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.STransactionException;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * Execute works using an ExecutorService
 * 
 * @author Charles Souillard
 * @author Baptiste Mesta
 */
public class ExecutorWorkService implements WorkService, RunnableListener {

    private final TransactionService transactionService;

    private final ExecutorService threadPoolExecutor;

    private final WorkSynchronizationFactory workSynchronizationFactory;

    private final ThreadLocal<AbstractWorkSynchronization> synchronizations = new ThreadLocal<AbstractWorkSynchronization>();

    private final TechnicalLoggerService loggerService;

    private final SessionAccessor sessionAccessor;

    private final SessionService sessionService;

    private final Map<Long, Queue<NotifyingRunnable>> runnables = new HashMap<Long, Queue<NotifyingRunnable>>();

    private final Set<Long> deactivated = new HashSet<Long>();

    public ExecutorWorkService(final TransactionService transactionService, final WorkSynchronizationFactory workSynchronizationFactory,
            final TechnicalLoggerService loggerService, final SessionAccessor sessionAccessor, final SessionService sessionService,
            final BonitaExecutorServiceFactory bonitaExecutorServiceFactory) {
        this.transactionService = transactionService;
        this.workSynchronizationFactory = workSynchronizationFactory;
        threadPoolExecutor = bonitaExecutorServiceFactory.createExecutorService();
        this.loggerService = loggerService;
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
    }

    @Override
    public void registerWork(final AbstractBonitaWork work) throws WorkRegisterException {
        final AbstractWorkSynchronization synchro = getContinuationSynchronization();
        synchro.addWork(work);
    }

    private synchronized AbstractWorkSynchronization getContinuationSynchronization() throws WorkRegisterException {
        AbstractWorkSynchronization synchro = synchronizations.get();
        if (synchro == null || synchro.isExecuted()) {
            synchro = workSynchronizationFactory.getWorkSynchronization(threadPoolExecutor, loggerService, sessionAccessor, sessionService, transactionService,
                    this);
            try {
                if (!transactionService.isTransactionActive()) {
                    throw new WorkRegisterException("Can't register work on not active transaction");
                }
                transactionService.registerBonitaSynchronization(synchro);
            } catch (final STransactionNotFoundException e) {
                throw new WorkRegisterException(e.getMessage(), e);
            } catch (STransactionException e) {
                throw new WorkRegisterException(e.getMessage(), e);
            }
            synchronizations.set(synchro);
        }
        return synchro;
    }

    @Override
    public void runnableRegistered(final NotifyingRunnable runnable) {
        final long tenantId = runnable.getTenantId();
        Queue<NotifyingRunnable> queue = runnables.get(tenantId);
        if (queue == null) {
            queue = createQueue(tenantId);
        }
        queue.add(runnable);
    }

    private synchronized Queue<NotifyingRunnable> createQueue(final long tenantId) {
        Queue<NotifyingRunnable> queue = runnables.get(tenantId);
        if (queue == null) {
            queue = new ConcurrentLinkedQueue<NotifyingRunnable>();
            runnables.put(tenantId, queue);
        }
        return queue;
    }

    @Override
    public void runnableStarted(final NotifyingRunnable runnable) {
    }

    @Override
    public void runnableDone(final NotifyingRunnable runnable) {
        runnables.get(runnable.getTenantId()).remove(runnable);
    }

    @Override
    public void stop(final Long tenantId) {
        deactivated.add(tenantId);
        final Queue<NotifyingRunnable> queue = runnables.get(tenantId);
        if (queue != null) {
            for (final NotifyingRunnable notifyingRunnable : queue) {
                notifyingRunnable.cancel();
            }
        }
    }

    @Override
    public void start(final Long tenantId) {
        deactivated.remove(tenantId);
    }

    public boolean isStopped(final long tenantId) {
        return deactivated.contains(tenantId);
    }

}
