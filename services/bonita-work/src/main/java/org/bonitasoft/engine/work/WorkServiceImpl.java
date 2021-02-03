/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import org.bonitasoft.engine.commons.time.EngineClock;
import org.bonitasoft.engine.log.technical.TechnicalLogger;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Directly calls the WorkExecutorService
 *
 * @author Charles Souillard
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
@Service("workService")
public class WorkServiceImpl implements WorkService {

    private final UserTransactionService transactionService;
    private final TechnicalLogger log;
    private final SessionAccessor sessionAccessor;
    private final WorkExecutorService workExecutorService;
    private final EngineClock engineClock;
    private int workDelayOnMultipleXAResource;

    public WorkServiceImpl(UserTransactionService transactionService,
            TechnicalLoggerService loggerService,
            SessionAccessor sessionAccessor,
            WorkExecutorService workExecutorService,
            EngineClock engineClock,
            @Value("${bonita.tenant.work.${bdm.db.vendor}.delayOnMultipleXAResource:0}") int workDelayOnMultipleXAResource) {
        this.transactionService = transactionService;
        this.log = loggerService.asLogger(WorkServiceImpl.class);
        this.sessionAccessor = sessionAccessor;
        this.workExecutorService = workExecutorService;
        this.engineClock = engineClock;
        this.workDelayOnMultipleXAResource = workDelayOnMultipleXAResource;
    }

    @Override
    public void registerWork(WorkDescriptor workDescriptor) throws SWorkRegisterException {
        if (isStopped()) {
            log.warn("Tried to register work {}, but the work service is stopped.", workDescriptor);
            return;
        }
        workDescriptor.setRegistrationDate(engineClock.now());
        log.debug("Registering work {}", workDescriptor);
        createAndRegisterNewSynchronization(workDescriptor);
        log.debug("Work registered");
    }

    private WorkSynchronization createAndRegisterNewSynchronization(WorkDescriptor workDescriptor)
            throws SWorkRegisterException {
        WorkSynchronization synchro = new WorkSynchronization(transactionService, workExecutorService, sessionAccessor,
                workDescriptor, workDelayOnMultipleXAResource);
        try {
            transactionService.registerBonitaSynchronization(synchro);
        } catch (final STransactionNotFoundException e) {
            throw new SWorkRegisterException(e.getMessage(), e);
        }
        return synchro;
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
        if (workDelayOnMultipleXAResource > 0) {
            log.info(
                    "The property 'delayOnMultipleXAResource' is set to {} ms, that delay will be added when triggering works from a transaction having multiple XA resources. This is the case when the BDM is updated.",
                    workDelayOnMultipleXAResource);
        }
    }

    @Override
    public synchronized void pause() throws SWorkException {
    }

    @Override
    public synchronized void resume() {
        start();
    }
}
