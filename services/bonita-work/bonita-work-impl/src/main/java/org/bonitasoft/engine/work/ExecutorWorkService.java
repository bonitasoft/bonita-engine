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
package org.bonitasoft.engine.work;

import java.util.concurrent.TimeUnit;

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

    private final Object getSynchroLock = new Object();

    private final TransactionService transactionService;

    private final WorkSynchronizationFactory workSynchronizationFactory;

    private final ThreadLocal<AbstractWorkSynchronization> synchronizations = new ThreadLocal<AbstractWorkSynchronization>();

    private final TechnicalLoggerService loggerService;

    private final SessionAccessor sessionAccessor;

    private final BonitaExecutorServiceFactory bonitaExecutorServiceFactory;

    private BonitaExecutorService executor;

    private final int workTerminationTimeout;

    /**
     * @param transactionService
     * @param workSynchronizationFactory
     * @param loggerService
     * @param sessionAccessor
     * @param bonitaExecutorServiceFactory
     * @param workTerminationTimeout
     *        time in secondes to wait for works to finish
     */
    public ExecutorWorkService(final TransactionService transactionService, final WorkSynchronizationFactory workSynchronizationFactory,
            final TechnicalLoggerService loggerService, final SessionAccessor sessionAccessor, final BonitaExecutorServiceFactory bonitaExecutorServiceFactory,
            final int workTerminationTimeout) {
        this.transactionService = transactionService;
        this.workSynchronizationFactory = workSynchronizationFactory;
        this.loggerService = loggerService;
        this.sessionAccessor = sessionAccessor;
        this.bonitaExecutorServiceFactory = bonitaExecutorServiceFactory;
        this.workTerminationTimeout = workTerminationTimeout;
    }

    @Override
    public void registerWork(final BonitaWork work) throws SWorkRegisterException {
        if (isStopped()) {
            logExecutorStateWarn(work);
            return;
        }
        final AbstractWorkSynchronization synchro = getContinuationSynchronization();
        if (synchro != null) {
            loggerService.log(getClass(), TechnicalLogSeverity.DEBUG, "Registered work " + work.getDescription());
            synchro.addWork(work);
        }
    }

    private void logExecutorStateWarn(final BonitaWork work) {
        loggerService.log(getClass(), TechnicalLogSeverity.WARNING, "Tried to register work " + work.getDescription()
                + ", but the work service is stopped.");
    }

    @Override
    public void executeWork(final BonitaWork work) throws SWorkRegisterException {
        if (isStopped()) {
            logExecutorStateWarn(work);
            return;
        }
        try {
            work.setTenantId(sessionAccessor.getTenantId());
        } catch (final STenantIdNotSetException e) {
            throw new SWorkRegisterException("Unable to read tenant id from session.", e);
        }
        executor.submit(work);
    }

    private AbstractWorkSynchronization getContinuationSynchronization() throws SWorkRegisterException {
        synchronized (getSynchroLock) {
            AbstractWorkSynchronization synchro = synchronizations.get();
            if (synchro == null) {
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
    }

    @Override
    public boolean isStopped() {
        // the executor must handle elements when it's shutting down
        return executor == null;
    }

    @Override
    public synchronized void stop() {
        // we don't throw exception just stop it and log if something happens
        try {
            if (isStopped()) {
                return;
            }
            shutdownExecutor();
            awaitTermination();
        } catch (final SWorkException e) {
            if (e.getCause() != null) {
                loggerService.log(getClass(), TechnicalLogSeverity.WARNING, e.getMessage(), e.getCause());
            } else {
                loggerService.log(getClass(), TechnicalLogSeverity.WARNING, e.getMessage());
            }
        }
    }

    @Override
    public synchronized void start() {
        if (isStopped()) {
            executor = bonitaExecutorServiceFactory.createExecutorService();
        }
    }

    @Override
    public synchronized void pause() throws SWorkException {
        if (isStopped()) {
            return;
        }
        shutdownExecutor();
        // completely clear the queue because it's a global pause
        executor.clearAllQueues();
        awaitTermination();
    }

    @Override
    public synchronized void resume() {
        start();
    }

    private void awaitTermination() throws SWorkException {
        try {
            if (!executor.awaitTermination(workTerminationTimeout, TimeUnit.SECONDS)) {
                throw new SWorkException("Waited termination of all work " + workTerminationTimeout + "s but all tasks were not finished");
            }
        } catch (final InterruptedException e) {
            throw new SWorkException("Interrupted while stopping the work service", e);
        }
        executor = null;
    }

    private void shutdownExecutor() {
        executor.shutdownAndEmptyQueue();
        loggerService.log(getClass(), TechnicalLogSeverity.INFO, "Stopped executor service");
    }

    @Override
    public void notifyNodeStopped(final String nodeName) {
        if (!isStopped()) {
            executor.notifyNodeStopped(nodeName);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeSynchronization() {
        synchronizations.remove();
    }
}
