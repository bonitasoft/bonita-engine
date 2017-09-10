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

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.UserTransactionService;

/**
 * Directly calls the WorkExecutorService
 *
 * @author Charles Souillard
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class WorkServiceImpl implements WorkService {

    private final Object getSynchroLock = new Object();

    private final UserTransactionService transactionService;

    private final ThreadLocal<WorkSynchronization> synchronizations = new ThreadLocal<>();

    private final TechnicalLoggerService loggerService;

    private final SessionAccessor sessionAccessor;

    private final WorkExecutorService workExecutorService;
    private WorkFactory workFactory;

    /**
     * @param transactionService
     * @param loggerService
     * @param sessionAccessor
     * @param workExecutorService
     */
    public WorkServiceImpl(final UserTransactionService transactionService,
                           final TechnicalLoggerService loggerService, final SessionAccessor sessionAccessor,
                           WorkExecutorService workExecutorService, WorkFactory workFactory) {
        this.transactionService = transactionService;
        this.loggerService = loggerService;
        this.sessionAccessor = sessionAccessor;
        this.workExecutorService = workExecutorService;
        this.workFactory = workFactory;
    }

    @Override
    public void registerWork(WorkDescriptor workDescriptor) throws SWorkRegisterException {
        if (isStopped()) {
            logExecutorStateWarn(workDescriptor);
            return;
        }
        final WorkSynchronization synchro = getContinuationSynchronization();
        if (synchro != null) {
            loggerService.log(getClass(), TechnicalLogSeverity.DEBUG, "Registered work " + workDescriptor);
            synchro.addWork(workDescriptor);
        }
    }

    private void logExecutorStateWarn(final WorkDescriptor work) {
        loggerService.log(getClass(), TechnicalLogSeverity.WARNING, "Tried to register work " + work
                + ", but the work service is stopped.");
    }

    private WorkSynchronization getContinuationSynchronization() throws SWorkRegisterException {
        synchronized (getSynchroLock) {
            WorkSynchronization synchro = synchronizations.get();
            if (synchro == null) {
                synchro = new WorkSynchronization(workExecutorService, sessionAccessor, this);
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
        return workExecutorService.isStopped();
    }

    @Override
    public synchronized void stop() {

    }

    @Override
    public synchronized void start() {
    }

    @Override
    public synchronized void pause() throws SWorkException {
    }

    @Override
    public synchronized void resume() {
        start();
    }

    void removeSynchronization() {
        synchronizations.remove();
    }
}
