/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

import static java.lang.String.format;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bonitasoft.engine.log.technical.TechnicalLogger;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.monitoring.ObservableExecutor;

/**
 * @author Baptiste Mesta
 */
public class WorkExecutorServiceImpl implements WorkExecutorService, WorkExecutionCallback, ObservableExecutor {

    private BonitaExecutorServiceFactory bonitaExecutorServiceFactory;
    private BonitaExecutorService executor;
    private long workTerminationTimeout;
    private final TechnicalLogger logger;

    public WorkExecutorServiceImpl(BonitaExecutorServiceFactory bonitaExecutorServiceFactory, TechnicalLoggerService loggerService, long workTerminationTimeout) {
        this.bonitaExecutorServiceFactory = bonitaExecutorServiceFactory;
        logger = loggerService.asLogger(WorkExecutorServiceImpl.class);
        this.workTerminationTimeout = workTerminationTimeout;
    }

    @Override
    public void execute(WorkDescriptor work) {
        if (!isStopped()) {
            logger.debug("Submitted work {}", work);
            executor.submit(work);
        } else {
            logger.debug("Ignored work submission (service stopped) {}", work);
        }
    }

    public void onSuccess(WorkDescriptor work) {
        logger.debug("Completed work {}", work);
    }

    public void onFailure(WorkDescriptor work, BonitaWork bonitaWork, Map<String, Object> context, Throwable thrown) {
        if (thrown instanceof LockException) {
            if (thrown instanceof LockTimeoutException) {
                //Can happen frequently, only log in debug
                logger.debug("Tried to execute the work, but it was unable to acquire a lock {}", work);
            } else {
                //Caused
                logger.warn("Tried to execute the work, but it was unable to acquire a lock " + work, thrown);
            }
            execute(work);
            return;
        }
        if (thrown instanceof SWorkPreconditionException) {
            logger.info("Work was not executed because preconditions where not met, {} : {}", work, thrown.getMessage());
            return;
        }
        try {
            bonitaWork.handleFailure(thrown, context);
        } catch (Exception e) {
            logger.warn("Work failed with error " + work, e);
        }
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
                logger.warn(e.getMessage(),e.getCause());
            } else {
                logger.warn(e.getMessage());
            }
        }
    }

    @Override
    public synchronized void start() {
        if (isStopped()) {
            executor = bonitaExecutorServiceFactory.createExecutorService(this);
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
                throw new SWorkException(format("Waited termination of all work %ds but all tasks were not finished", workTerminationTimeout));
            }
        } catch (final InterruptedException e) {
            throw new SWorkException("Interrupted while stopping the work service", e);
        }
        executor = null;
    }

    private void shutdownExecutor() {
        executor.shutdownAndEmptyQueue();
        logger.info("Stopped executor service");
    }

    public boolean isStopped() {
        return executor == null;
    }

    @Override
    public void notifyNodeStopped(String nodeName) {
    }

    @Override
    public long getPendings() {
        if (executor == null) {
            return 0;
        }
        return executor.getPendings();
    }

    @Override
    public long getRunnings() {
        if (executor == null) {
            return 0;
        }
        return executor.getRunnings();
    }

    @Override
    public long getExecuted() {
        if (executor == null) {
            return 0;
        }
        return executor.getExecuted();
    }
}
