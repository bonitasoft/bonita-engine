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
import static org.bonitasoft.engine.commons.ExceptionUtils.printLightWeightStacktrace;
import static org.bonitasoft.engine.commons.ExceptionUtils.printRootCauseOnly;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.bonitasoft.engine.commons.time.EngineClock;
import org.bonitasoft.engine.incident.Incident;
import org.bonitasoft.engine.incident.IncidentService;
import org.bonitasoft.engine.log.technical.TechnicalLogger;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.work.audit.WorkExecutionAuditor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.stereotype.Component;

/**
 * @author Baptiste Mesta.
 */
@Component
@ConditionalOnSingleCandidate(WorkExecutorService.class)
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
    private final IncidentService incidentService;
    private final long tenantId;
    public int numberOfFramesToLogInExceptions = 3;
    private Random random = new Random();

    public RetryingWorkExecutorService(BonitaExecutorServiceFactory bonitaExecutorServiceFactory,
            TechnicalLoggerService loggerService,
            EngineClock engineClock,
            @Value("${bonita.tenant.work.terminationTimeout}") long workTerminationTimeout,
            @Value("${bonita.tenant.work.maxRetry}") int maxRetry,
            @Value("${bonita.tenant.work.retry.delay}") int delay,
            @Value("${bonita.tenant.work.retry.factor}") double delayFactor,
            ExceptionRetryabilityEvaluator exceptionRetryabilityEvaluator,
            WorkExecutionAuditor workExecutionAuditor,
            MeterRegistry meterRegistry,
            IncidentService incidentService,
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
        this.incidentService = incidentService;
        this.tenantId = tenantId;
        Gauge.builder(NUMBER_OF_WORKS_RETRIED, retriedWorks, AtomicLong::get)
                .tag("tenant", String.valueOf(tenantId)).baseUnit("works")
                .description("Works currently waiting for execution that have been retried at least once")
                .register(meterRegistry);
    }

    @Value("${bonita.tenant.work.exceptionsNumberOfFrameToLog:3}")
    public void setNumberOfFramesToLogInExceptions(int numberOfFramesToLogInExceptions) {
        this.numberOfFramesToLogInExceptions = numberOfFramesToLogInExceptions;
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
                logger.warn("Tried to execute the work, but it was unable to acquire a lock {}",
                        bonitaWork.getDescription(), thrown);
            }
            execute(work);
            return;
        }
        logger.debug("Work {} failed because of ", work, thrown);
        switch (exceptionRetryabilityEvaluator.evaluateRetryability(thrown)) {
            case NOT_RETRYABLE:
                if (thrown instanceof SWorkPreconditionException) {
                    logger.warn("Work was not executed because preconditions were not met, {} : {}",
                            bonitaWork.getDescription(), thrown.getMessage());
                    decrementRetryCounterIfNeeded(work);
                } else {
                    logger.warn("Work {} failed. The element will be marked as failed. Exception is: {}",
                            bonitaWork.getDescription(),
                            printLightWeightStacktrace(thrown, numberOfFramesToLogInExceptions));
                    handleFailure(work, bonitaWork, context, thrown);
                }
                break;
            case UNCERTAIN_COMPLETION_OF_COMMIT:
                // Do the same as retryable but add a warning log
                logger.warn(
                        "Work {} has failed and will be retried but the issue happened during the commit. We are uncertain that the "
                                +
                                "commit was really completed. If the retry fails with a SWorkPreconditionException it might indicate that "
                                +
                                "the work was already completed and the recovery mechanism will restart it. No manual action is required.",
                        bonitaWork.getDescription());
            case RETRYABLE:
                if (work.getRetryCount() < maxRetry) {
                    long delayInMillis = getDelayInMillis(work.getRetryCount());
                    logger.warn(
                            "Work {} failed because of {}. It will be retried. Attempt {} of {} with a delay of {} ms",
                            bonitaWork.getDescription(),
                            printRootCauseOnly(thrown),
                            work.getRetryCount() + 1,
                            maxRetry, delayInMillis);
                    incrementRetryCounterIfNeeded(work);
                    retry(work, delayInMillis);
                } else {
                    logger.warn("Work {} failed. It has already been retried {} times. " +
                            "No more retries will be attempted, it will be marked as failed. Exception is: {}",
                            bonitaWork.getDescription(), maxRetry,
                            printLightWeightStacktrace(thrown, numberOfFramesToLogInExceptions));
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
            if (bonitaWork.canBeRecoveredByTheRecoveryMechanism()) {
                logger.warn("Work {} failed and we were not able to mark the element as failed. " +
                        "However, the element can be recovered by the recovery mechanism, it will be recovered automatically "
                        +
                        "the next time the recovery is executed. We were not able to mark it as failed because of {} ",
                        bonitaWork.getDescription(), printLightWeightStacktrace(e, numberOfFramesToLogInExceptions));
                logger.debug("Unable to put the element as failed because:", e);
            } else {
                logger.warn(
                        "Work {} failed and we were not able to mark the element as failed. An incident will be reported. "
                                +
                                "We were not able to mark it as failed because of {}",
                        bonitaWork.getDescription(), printLightWeightStacktrace(e, numberOfFramesToLogInExceptions));
                incidentService.report(tenantId,
                        new Incident(bonitaWork.getDescription(), bonitaWork.getRecoveryProcedure(), thrown, e));
            }
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

    private void retry(WorkDescriptor work, long delayInMillis) {
        Instant mustBeExecutedAfter = engineClock.now().plusMillis(delayInMillis);
        work.incrementRetryCount();
        work.mustBeExecutedAfter(mustBeExecutedAfter);
        execute(work);
    }

    private long getDelayInMillis(int retryCount) {
        //we multiply by the delay each time
        // first time is delay
        // second time is delay * delayFactor
        // third time is delay * delayFactor * delayFactor
        // and so on
        // Also add a random scatter delay to avoid retry "waves"
        // when several jobs fail & are retried at exactly the same time, causing further fails...
        // see RUNTIME-302 for more details
        double factor = Math.pow(delayFactor, retryCount);
        return Math.round((delay * factor) * (random.nextFloat() + 1));
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
