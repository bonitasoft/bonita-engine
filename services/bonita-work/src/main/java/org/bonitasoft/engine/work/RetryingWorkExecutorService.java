/**
 * Copyright (C) 2020 Bonitasoft S.A.
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

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.bonitasoft.engine.commons.time.EngineClock;
import org.bonitasoft.engine.log.technical.TechnicalLogger;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.work.audit.WorkExecutionAuditor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Baptiste Mesta.
 */
@Component("workExecutorService")
public class RetryingWorkExecutorService implements WorkExecutorService, WorkExecutionCallback {

    public static final String NUMBER_OF_WORKS_RETRIED = "bonita.bpmengine.work.retried";

    private final TechnicalLogger logger;
    private final EngineClock engineClock;
    private final int maxRetry;
    private final WorkExecutionAuditor workExecutionAuditor;
    private int delay;
    private final double delayFactor;
    private final ExceptionRetryabilityEvaluator exceptionRetryabilityEvaluator;
    private final AtomicLong retriedWorks = new AtomicLong();
    private final BonitaExecutorServiceFactory bonitaExecutorServiceFactory;
    private final long workTerminationTimeout;
    private BonitaExecutorService executor;

    public RetryingWorkExecutorService(BonitaExecutorServiceFactory bonitaExecutorServiceFactory,
            @Qualifier("tenantTechnicalLoggerService") TechnicalLoggerService loggerService,
            EngineClock engineClock,
            @Value("${bonita.tenant.work.terminationTimeout}") long workTerminationTimeout,
            @Value("${bonita.tenant.work.maxRetry}") int maxRetry,
            @Value("${bonita.tenant.work.retry.delay}") int delay,
            @Value("${bonita.tenant.work.retry.factor}") double delayFactor,
            ExceptionRetryabilityEvaluator exceptionRetryabilityEvaluator,
            WorkExecutionAuditor workExecutionAuditor,
            MeterRegistry meterRegistry,
            @Value("${tenantId}") long tenantId) {
        this.bonitaExecutorServiceFactory = bonitaExecutorServiceFactory;
        this.logger = loggerService.asLogger(RetryingWorkExecutorService.class);
        this.engineClock = engineClock;
        this.workTerminationTimeout = workTerminationTimeout;
        this.maxRetry = maxRetry;
        this.delay = delay;
        this.delayFactor = delayFactor;
        this.exceptionRetryabilityEvaluator = exceptionRetryabilityEvaluator;
        this.workExecutionAuditor = workExecutionAuditor;
        Gauge.builder(NUMBER_OF_WORKS_RETRIED, retriedWorks, AtomicLong::get)
                .tag("tenant", String.valueOf(tenantId)).baseUnit("works")
                .description("Works currently waiting for execution that have been retried at least once")
                .register(meterRegistry);
    }

    @Override
    public void onSuccess(WorkDescriptor work) {
        if (work.getRetryCount() > 0) {
            logger.info("Work {} was successfully retried after {} tries.", work, work.getRetryCount());
            retriedWorks.decrementAndGet();
        }
        logger.debug("Completed work {}", work);
        workExecutionAuditor.notifySuccess(work);
    }

    @Override
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
        switch (exceptionRetryabilityEvaluator.evaluateRetryability(thrown)) {
            case NOT_RETRYABLE:
                if (thrown instanceof SWorkPreconditionException) {
                    logger.warn("Work was not executed because preconditions were not met, {} : {}", work,
                            thrown.getMessage());
                    decrementRetryCounterIfNeeded(work);
                } else {
                    handleFailure(work, bonitaWork, context, thrown);
                }
                break;
            case UNCERTAIN_COMPLETION_OF_COMMIT:
                // Do the same as retryable but add a warning log
                logger.warn(
                        "Work {} will be retried but the issue happened during the commit. We are uncertain that the "
                                +
                                "commit was really completed. If the retry fails with a SWorkPreconditionException it might indicate that "
                                +
                                "the work was already completed and you will need to restart your platform to trigger potential subsequent works.");
            case RETRYABLE:
                if (work.getRetryCount() < maxRetry) {
                    incrementRetryCounterIfNeeded(work);
                    retry(work, thrown);
                } else {
                    logger.warn("Work {} has encountered an {} and has already been retried {} times. " +
                            "No more retries will be attempted, see what is the failure in subsequent logs.",
                            work, thrown.getClass().getCanonicalName(), maxRetry);
                    handleFailure(work, bonitaWork, context, thrown);
                }
                break;
            default:
                throw new IllegalStateException(
                        "Unexpected value: " + exceptionRetryabilityEvaluator.evaluateRetryability(thrown));
        }
    }

    public void handleFailure(WorkDescriptor work, BonitaWork bonitaWork, Map<String, Object> context,
            Throwable thrown) {
        decrementRetryCounterIfNeeded(work);
        try {
            bonitaWork.handleFailure(thrown, context);
        } catch (Exception e) {
            logger.warn("Work failed with error {}", work, e);
        }
    }

    private void incrementRetryCounterIfNeeded(WorkDescriptor work) {
        if (work.getRetryCount() == 0) {
            retriedWorks.incrementAndGet();
        }
    }

    private void decrementRetryCounterIfNeeded(WorkDescriptor work) {
        if (work.getRetryCount() > 0) {
            retriedWorks.decrementAndGet();
        }
    }

    private void retry(WorkDescriptor work, Throwable thrown) {
        long delayInMillis = getDelayInMillis(work.getRetryCount());
        Instant mustBeExecutedAfter = engineClock.now().plusMillis(delayInMillis);
        work.incrementRetryCount();
        work.mustBeExecutedAfter(mustBeExecutedAfter);
        logger.warn("Work {} encountered an {} but it can be retried. Will retry. Attempt {} on {} " +
                "with a delay of {} ms", work.getDescription(), thrown.getClass().getCanonicalName(),
                work.getRetryCount(), maxRetry, delayInMillis);
        logger.debug("Exception encountered during retry", thrown);
        execute(work);
    }

    private long getDelayInMillis(int retryCount) {
        //we multiply by the delay each time
        // first time is delay
        // second time is delay * delayFactor
        // third time is delay * delayFactor * delayFactor
        //and so on
        double factor = Math.pow(delayFactor, retryCount);
        return Math.round(delay * factor);
    }

    //For testing purpose
    int getDelay() {
        return delay;
    }

    //For testing purpose
    void setDelay(int delay) {
        this.delay = delay;
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

    @Override
    public synchronized void stop() {
        // we don't throw exception just stop it and log if something happens
        try {
            if (isStopped()) {
                return;
            }
            bonitaExecutorServiceFactory.unbind();
            shutdownExecutor();
            awaitTermination();
        } catch (final SWorkException e) {
            if (e.getCause() != null) {
                logger.warn(e.getMessage(), e.getCause());
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
        bonitaExecutorServiceFactory.unbind();
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
                throw new SWorkException(format("Waited termination of all work %ds but all tasks were not finished",
                        workTerminationTimeout));
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
}
