/**
 * Copyright (C) 2012, 2014 BonitaSoft S.A.
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

import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.bonitasoft.engine.commons.Pair;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * Execute works using an ExecutorService
 * 
 * @author Charles Souillard
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class ExecutorWorkService implements WorkService {

    private static final int TIMEOUT = Integer.valueOf(System.getProperty("bonita.work.termination.timeout", "15"));

    private final TransactionService transactionService;

    private ExecutorService executor;

    private final WorkSynchronizationFactory workSynchronizationFactory;

    private final ThreadLocal<AbstractWorkSynchronization> synchronizations = new ThreadLocal<AbstractWorkSynchronization>();

    private final TechnicalLoggerService loggerService;

    private final SessionAccessor sessionAccessor;

    private final BonitaExecutorServiceFactory bonitaExecutorServiceFactory;

    private Queue<Runnable> queue;

    public ExecutorWorkService(final TransactionService transactionService, final WorkSynchronizationFactory workSynchronizationFactory,
            final TechnicalLoggerService loggerService, final SessionAccessor sessionAccessor, final BonitaExecutorServiceFactory bonitaExecutorServiceFactory) {
        this.transactionService = transactionService;
        this.workSynchronizationFactory = workSynchronizationFactory;
        this.loggerService = loggerService;
        this.sessionAccessor = sessionAccessor;
        this.bonitaExecutorServiceFactory = bonitaExecutorServiceFactory;
    }

    @Override
    public void registerWork(final BonitaWork work) throws SWorkRegisterException {
        if (isStopped()) {
            loggerService.log(getClass(), TechnicalLogSeverity.WARNING, "Tried to register work " + work.getDescription()
                    + ", but the work service is stopped");
            return;
        }
        final AbstractWorkSynchronization synchro = getContinuationSynchronization(work);
        if (synchro != null) {
            synchro.addWork(work);
        }
    }

    @Override
    public void executeWork(final BonitaWork work) throws SWorkRegisterException {
        if (isStopped()) {
            loggerService.log(getClass(), TechnicalLogSeverity.WARNING, "Tried to register work " + work.getDescription()
                    + ", but the work service is stopped");
            return;
        }

        try {
            work.setTenantId(sessionAccessor.getTenantId());
        } catch (STenantIdNotSetException e) {
            throw new SWorkRegisterException("Unable to read tenant id from session.", e);
        }
        this.executor.submit(work);
    }

    private synchronized AbstractWorkSynchronization getContinuationSynchronization(final BonitaWork work) throws SWorkRegisterException {
        if (executor == null || executor.isShutdown()) {
            loggerService.log(getClass(), TechnicalLogSeverity.INFO, "Tried to register work " + work.getDescription()
                    + " but the work service is shutdown. work will be restarted with the node");
            return null;
        }
        AbstractWorkSynchronization synchro = synchronizations.get();
        if (synchro == null || synchro.isExecuted()) {
            synchro = workSynchronizationFactory.getWorkSynchronization(executor, loggerService, sessionAccessor, this);
            try {
                transactionService.registerBonitaSynchronization(synchro);
            } catch (final STransactionNotFoundException e) {
                throw new SWorkRegisterException(e.getMessage(), e);
            }
            synchronizations.set(synchro);
        }
        return synchro;
    }

    @Override
    public boolean isStopped() {
        return executor == null || executor.isShutdown();
    }

    @Override
    public void stop() {
        // we don't throw exception just stop it and log if something happend
        try {
            stopWithException();
        } catch (SWorkException e) {
            loggerService.log(getClass(), TechnicalLogSeverity.WARNING, e.getMessage());
        } catch (TimeoutException e) {
            loggerService.log(getClass(), TechnicalLogSeverity.WARNING, e.getMessage());
        }
    }

    @Override
    public void start() {
        if (isStopped()) {
            Pair<ExecutorService, Queue<Runnable>> createExecutorService = bonitaExecutorServiceFactory.createExecutorService();
            executor = createExecutorService.getLeft();
            queue = createExecutorService.getRight();
        }
    }

    @Override
    public void pause() throws TimeoutException, SWorkException {
        stopWithException();
    }

    private void stopWithException() throws TimeoutException, SWorkException {
        if (isStopped()) {
            return;
        }
        executor.shutdown();
        queue.clear();
        try {
            if (!executor.awaitTermination(TIMEOUT, TimeUnit.SECONDS)) {
                throw new TimeoutException("Waited termination of all work " + TIMEOUT + "s but all tasks were not finished");
            }
        } catch (InterruptedException e) {
            throw new SWorkException("Interrupted while pausing the work service", e);
        }
        executor = null;
        queue = null;
    }

    @Override
    public void resume() {
        start();
    }
}
