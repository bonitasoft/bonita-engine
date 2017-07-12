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
package org.bonitasoft.engine.scheduler.impl;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SFireEventException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.JobIdentifier;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.StatelessJob;
import org.bonitasoft.engine.scheduler.exception.SJobConfigurationException;
import org.bonitasoft.engine.scheduler.exception.SJobExecutionException;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.STransactionException;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.transaction.TransactionState;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class JobWrapper implements StatelessJob {

    private static final long serialVersionUID = 7145451610635400449L;

    private final StatelessJob statelessJob;

    private final TechnicalLoggerService logger;

    private final long tenantId;

    private final SEvent jobExecuting;

    private final SEvent jobCompleted;

    private final EventService eventService;

    private final JobIdentifier jobIdentifier;

    private final SessionAccessor sessionAccessor;

    private final TransactionService transactionService;

    private final PersistenceService persistenceService;

    private final JobService jobService;

    public JobWrapper(final JobIdentifier jobIdentifier, final StatelessJob statelessJob, final TechnicalLoggerService logger, final long tenantId,
                      final EventService eventService, final SessionAccessor sessionAccessor, final TransactionService transactionService, PersistenceService persistenceService, JobService jobService) {
        this.jobIdentifier = jobIdentifier;
        this.sessionAccessor = sessionAccessor;
        this.statelessJob = statelessJob;
        this.logger = logger;

        this.tenantId = tenantId;
        this.eventService = eventService;
        this.transactionService = transactionService;
        this.persistenceService = persistenceService;
        this.jobService = jobService;
        jobExecuting = new SEvent(JOB_EXECUTING);
        jobCompleted = new SEvent(JOB_COMPLETED);
    }


    @Override
    public String getName() {
        return jobIdentifier.getJobName();
    }

    @Override
    public String getDescription() {
        return statelessJob.getDescription();
    }

    @Override
    public void execute() throws SJobExecutionException, SFireEventException {
        try {
            sessionAccessor.setTenantId(tenantId);
            if (eventService.hasHandlers(JOB_EXECUTING, null)) {
                jobExecuting.setObject(this);
                eventService.fireEvent(jobExecuting);
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
                logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "Start execution of " + statelessJob.getName());
            }
            statelessJob.execute();
            //make sure hibernate flush everything we did before going back to quartz code
            persistenceService.flushStatements();
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
                logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "Finished execution of " + statelessJob.getName());
            }

        } catch (final SFireEventException | SJobExecutionException e) {
            handleFailure(e);
            throw e;
        } catch (Exception e) {
            handleFailure(e);
            throw new SJobExecutionException(e);
        } finally {
            if (eventService.hasHandlers(JOB_COMPLETED, null)) {
                jobCompleted.setObject(this);
                eventService.fireEvent(jobCompleted);
            }
        }
    }

    void handleFailure(Exception e) {
        logFailedJob(e);
        try {
            registerFailInAnOtherThread(e, jobIdentifier);
            transactionService.setRollbackOnly();
        } catch (STransactionException | STransactionNotFoundException e1) {
            logger.log(getClass(), TechnicalLogSeverity.ERROR, "Unable to rollback transaction after fail on job  " + jobIdentifier.getId(), e);
        }
    }

    private void registerFailInAnOtherThread(final Exception jobException, final JobIdentifier jobIdentifier) throws STransactionNotFoundException {
        transactionService.registerBonitaSynchronization(new BonitaTransactionSynchronization() {
            @Override
            public void beforeCommit() {

            }

            @Override
            public void afterCompletion(TransactionState txState) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            sessionAccessor.setTenantId(jobIdentifier.getTenantId());
                            transactionService.executeInTransaction(new Callable<Object>() {
                                @Override
                                public Object call() throws Exception {
                                    jobService.logJobError(jobException, jobIdentifier.getId());
                                    return null;
                                }
                            });
                        } catch (Exception e) {
                            logger.log(getClass(), TechnicalLogSeverity.ERROR, "Error while registering the error for the job " + jobIdentifier.getId(), e);
                            logger.log(getClass(), TechnicalLogSeverity.ERROR, "job exception was ", jobException);
                        }
                        sessionAccessor.deleteTenantId();
                    }
                }, "Job error handler");
                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    logger.log(getClass(), TechnicalLogSeverity.ERROR, "Thread to log error on job " + jobIdentifier.getId() + " interrupted", e);
                }

            }
        });
    }

    private void logFailedJob(final Exception e) {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.ERROR)) {
            logger.log(this.getClass(), TechnicalLogSeverity.ERROR, "Error while executing job " + jobIdentifier + " : " + e.getMessage(), e);
        }
    }

    @Override
    public void setAttributes(final Map<String, Serializable> attributes) throws SJobConfigurationException {
        statelessJob.setAttributes(attributes);
    }

    public StatelessJob getStatelessJob() {
        return statelessJob;
    }

}
