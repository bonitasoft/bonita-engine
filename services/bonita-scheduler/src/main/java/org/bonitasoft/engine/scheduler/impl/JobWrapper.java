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
package org.bonitasoft.engine.scheduler.impl;

import java.io.Serializable;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.commons.exceptions.SRetryableException;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SFireEventException;
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

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
@Slf4j
public class JobWrapper implements StatelessJob {

    private static final long serialVersionUID = 7145451610635400449L;

    private final StatelessJob statelessJob;

    private final long tenantId;

    private final SEvent jobExecuting;

    private final SEvent jobCompleted;

    private final EventService eventService;

    private final JobIdentifier jobIdentifier;

    private final SessionAccessor sessionAccessor;

    private final TransactionService transactionService;

    private final PersistenceService persistenceService;

    private final JobService jobService;

    public JobWrapper(final JobIdentifier jobIdentifier, final StatelessJob statelessJob, final long tenantId,
            final EventService eventService, final SessionAccessor sessionAccessor,
            final TransactionService transactionService, PersistenceService persistenceService, JobService jobService) {
        this.jobIdentifier = jobIdentifier;
        this.sessionAccessor = sessionAccessor;
        this.statelessJob = statelessJob;

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

            log.debug("Start execution of {}", statelessJob.getName());
            statelessJob.execute();
            //make sure hibernate flush everything we did before going back to quartz code
            persistenceService.flushStatements();

            log.debug("Finished execution of {}", statelessJob.getName());
        } catch (final SRetryableException e) {
            throw e;
        } catch (final Throwable e) {
            handleFailure(e);
            //throw an exception: if it's a "one shot" timer it should delete the timer trigger only if job succeeded (see TimerEventTriggerJobListener)
            throw new SJobExecutionException(e);
        } finally {
            if (eventService.hasHandlers(JOB_COMPLETED, null)) {
                jobCompleted.setObject(this);
                eventService.fireEvent(jobCompleted);
            }
        }
    }

    void handleFailure(Throwable e) {
        log.error("Error while executing job " + jobIdentifier + " : " + e.getMessage(), e);
        try {
            registerFailInAnOtherThread(e, jobIdentifier);
            transactionService.setRollbackOnly();
        } catch (STransactionException | STransactionNotFoundException e1) {
            log.error(
                    "Unable to rollback transaction after fail on job  " + jobIdentifier.getId(), e);
        }
    }

    private void registerFailInAnOtherThread(final Throwable jobException, final JobIdentifier jobIdentifier)
            throws STransactionNotFoundException {
        transactionService.registerBonitaSynchronization(new BonitaTransactionSynchronization() {

            @Override
            public void afterCompletion(final int txState) {
                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            sessionAccessor.setTenantId(jobIdentifier.getTenantId());
                            transactionService.executeInTransaction(() -> {
                                jobService.logJobError(jobException, jobIdentifier.getId());
                                return null;
                            });
                        } catch (Exception e) {
                            log.error(
                                    "Error while registering the error for the job " + jobIdentifier.getId(), e);
                            log.error("job exception was ", jobException);
                        }
                        sessionAccessor.deleteTenantId();
                    }
                }, "Job error handler");
                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    log.error(
                            "Thread to log error on job " + jobIdentifier.getId() + " interrupted", e);
                }

            }
        });
    }

    @Override
    public void setAttributes(final Map<String, Serializable> attributes) throws SJobConfigurationException {
        statelessJob.setAttributes(attributes);
    }

    public StatelessJob getStatelessJob() {
        return statelessJob;
    }

}
