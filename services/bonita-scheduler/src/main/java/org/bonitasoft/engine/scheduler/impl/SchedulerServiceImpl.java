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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SFireEventException;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.scheduler.*;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.scheduler.exception.jobLog.SJobLogDeletionException;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobParameter;
import org.bonitasoft.engine.scheduler.trigger.Trigger;
import org.bonitasoft.engine.service.ServicesResolver;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Yanyan Liu
 * @author Celine Souchet
 */
@Slf4j
public class SchedulerServiceImpl implements SchedulerService {

    private final SchedulerExecutor schedulerExecutor;

    private final JobService jobService;

    private final EventService eventService;

    private final SEvent schedulerStarted;

    private final SEvent schedulerStopped;

    private final SEvent jobFailed;

    private final SessionAccessor sessionAccessor;

    private final TransactionService transactionService;

    private final ServicesResolver servicesResolver;
    private PersistenceService persistenceService;

    /**
     * Create a new instance of scheduler service.
     */
    public SchedulerServiceImpl(final SchedulerExecutor schedulerExecutor, final JobService jobService,
            final EventService eventService, final TransactionService transactionService,
            final SessionAccessor sessionAccessor,
            final ServicesResolver servicesResolver, final PersistenceService persistenceService) {
        this.schedulerExecutor = schedulerExecutor;
        this.jobService = jobService;
        this.servicesResolver = servicesResolver;
        this.persistenceService = persistenceService;
        schedulerStarted = new SEvent(SCHEDULER_STARTED);
        schedulerStopped = new SEvent(SCHEDULER_STOPPED);
        jobFailed = new SEvent(JOB_FAILED);
        this.eventService = eventService;
        this.transactionService = transactionService;
        this.sessionAccessor = sessionAccessor;
        schedulerExecutor.setBOSSchedulerService(this);
    }

    @Override
    public void schedule(final SJobDescriptor jobDescriptor, final Trigger trigger) throws SSchedulerException {
        final SJobDescriptor createdJobDescriptor = createJobDescriptor(jobDescriptor, Collections.emptyList());
        internalSchedule(createdJobDescriptor, trigger);
    }

    @Override
    public void schedule(final SJobDescriptor jobDescriptor, final List<SJobParameter> parameters,
            final Trigger trigger) throws SSchedulerException {
        if (trigger == null) {
            throw new SSchedulerException("The trigger is null");
        }
        final SJobDescriptor createdJobDescriptor = createJobDescriptor(jobDescriptor, parameters);
        internalSchedule(createdJobDescriptor, trigger);
    }

    @Override
    public void executeAgain(final long jobDescriptorId, int delayInMillis) throws SSchedulerException {
        final SJobDescriptor jobDescriptor = jobService.getJobDescriptor(jobDescriptorId);
        schedulerExecutor.executeAgain(jobDescriptorId, getTenantIdAsString(), jobDescriptor.getJobName(),
                false, delayInMillis);
    }

    @Override
    public void retryJobThatFailed(long jobDescriptorId) throws SSchedulerException {
        final SJobDescriptor jobDescriptor = jobService.getJobDescriptor(jobDescriptorId);
        deleteFailedJobs(jobDescriptorId);
        schedulerExecutor.executeAgain(jobDescriptorId, getTenantIdAsString(), jobDescriptor.getJobName(),
                false, 0);
    }

    @Override
    public void retryJobThatFailed(final long jobDescriptorId, final List<SJobParameter> parameters)
            throws SSchedulerException {
        final SJobDescriptor jobDescriptor = jobService.getJobDescriptor(jobDescriptorId);
        jobService.setJobParameters(getTenantId(), jobDescriptor.getId(), parameters);
        deleteFailedJobs(jobDescriptorId);
        schedulerExecutor.executeAgain(jobDescriptorId, getTenantIdAsString(), jobDescriptor.getJobName(),
                false, 0);
    }

    private void deleteFailedJobs(long jobDescriptorId) throws SSchedulerException {
        try {
            jobService.deleteJobLogs(jobDescriptorId);
        } catch (SJobLogDeletionException | SBonitaReadException e) {
            throw new SSchedulerException("Unable to delete failed jobs logs", e);
        }
    }

    private SJobDescriptor createJobDescriptor(final SJobDescriptor sJobDescriptor,
            final List<SJobParameter> parameters) throws SSchedulerException {
        final long tenantId = getTenantId();
        try {
            final SJobDescriptor createdJobDescriptor = jobService.createJobDescriptor(sJobDescriptor, tenantId);
            jobService.createJobParameters(parameters, tenantId, createdJobDescriptor.getId());
            return createdJobDescriptor;
        } catch (final SBonitaException sbe) {
            throw new SSchedulerException(sbe);
        }
    }

    private void internalSchedule(final SJobDescriptor jobDescriptor, final Trigger trigger)
            throws SSchedulerException {
        final String tenantId = getTenantIdAsString();
        try {
            schedulerExecutor.schedule(jobDescriptor.getId(), tenantId, jobDescriptor.getJobName(), trigger,
                    false);
        } catch (final Throwable e) {
            log.error("", e);
            try {
                eventService.fireEvent(jobFailed);
            } catch (final SFireEventException e1) {
                log.error("", e1);
            }
            throw new SSchedulerException(e);
        }
    }

    private long getTenantId() throws SSchedulerException {
        final long tenantId;
        try {
            tenantId = sessionAccessor.getTenantId();
        } catch (final STenantIdNotSetException e) {
            throw new SSchedulerException(e);
        }
        return tenantId;
    }

    private String getTenantIdAsString() throws SSchedulerException {
        return String.valueOf(getTenantId());
    }

    @Override
    public boolean isStarted() throws SSchedulerException {
        return schedulerExecutor.isStarted();
    }

    @Override
    public boolean isStopped() throws SSchedulerException {
        return schedulerExecutor.isShutdown();
    }

    @Override
    public void start() throws SSchedulerException, SFireEventException {
        log.info("Start scheduler");
        schedulerExecutor.start();
        eventService.fireEvent(schedulerStarted);
    }

    @Override
    public void stop() throws SSchedulerException, SFireEventException {
        schedulerExecutor.shutdown();
        eventService.fireEvent(schedulerStopped);
    }

    @Override
    public void pauseJobs(final long tenantId) throws SSchedulerException {
        schedulerExecutor.pauseJobs(String.valueOf(tenantId));
    }

    @Override
    public void resumeJobs(final long tenantId) throws SSchedulerException {
        schedulerExecutor.resumeJobs(String.valueOf(tenantId));
    }

    @Override
    public boolean delete(final String jobName) throws SSchedulerException {
        final boolean delete = schedulerExecutor.delete(jobName, String.valueOf(getTenantId()));
        jobService.deleteJobDescriptorByJobName(jobName);
        return delete;
    }

    @Override
    public void deleteJobs() throws SSchedulerException {
        schedulerExecutor.deleteJobs(String.valueOf(getTenantId()));
        jobService.deleteAllJobDescriptors();
    }

    @Override
    public List<String> getJobs() throws SSchedulerException {
        return schedulerExecutor.getJobs(String.valueOf(getTenantId()));
    }

    @Override
    public List<String> getAllJobs() throws SSchedulerException {
        return schedulerExecutor.getAllJobs();
    }

    /**
     * Get the persisted job from the database inside its own transaction.
     *
     * @return the newly created job
     * @throws SSchedulerException if the job cannot be created successfully
     */
    StatelessJob getPersistedJob(final JobIdentifier jobIdentifier) throws SSchedulerException {
        try {
            sessionAccessor.setTenantId(jobIdentifier.getTenantId());
            return transactionService.executeInTransaction(new PersistedJobCallable(jobIdentifier));
        } catch (final Exception e) {
            throw new SSchedulerException(e);
        } finally {
            sessionAccessor.deleteTenantId();
        }
    }

    private class PersistedJobCallable implements Callable<JobWrapper> {

        private final JobIdentifier jobIdentifier;

        PersistedJobCallable(final JobIdentifier jobIdentifier) {
            this.jobIdentifier = jobIdentifier;
        }

        @Override
        public JobWrapper call() throws Exception {
            final SJobDescriptor sJobDescriptor = jobService.getJobDescriptor(jobIdentifier.getId());
            if (sJobDescriptor == null) {
                throw new SObjectNotFoundException(String
                        .format("The job %s does not exist anymore. It might be already executed", jobIdentifier));
            }
            final String jobClassName = sJobDescriptor.getJobClassName();
            final Class<?> jobClass = Class.forName(jobClassName);
            final StatelessJob statelessJob = (StatelessJob) jobClass.newInstance();

            Map<String, Serializable> parameters = jobService.getJobParameters(jobIdentifier.getId())
                    .stream()
                    .collect(Collectors.toMap(SJobParameter::getKey, SJobParameter::getValue));
            parameters.put(StatelessJob.JOB_DESCRIPTOR_ID, jobIdentifier.getId());
            statelessJob.setAttributes(parameters);
            if (servicesResolver != null) {
                servicesResolver.injectServices(jobIdentifier.getTenantId(), statelessJob);
            }
            return new JobWrapper(jobIdentifier, statelessJob, jobIdentifier.getTenantId(), eventService,
                    sessionAccessor, transactionService, persistenceService, jobService);
        }
    }

    @Override
    public void pause() throws SBonitaException {
        pauseJobs(getTenantId());
    }

    @Override
    public void resume() throws SBonitaException {
        resumeJobs(getTenantId());
    }

    @Override
    public void rescheduleErroneousTriggers() throws SSchedulerException {
        schedulerExecutor.rescheduleErroneousTriggers();
    }

    @Override
    public Date rescheduleJob(final String triggerName, final String groupName, final Date triggerStartTime)
            throws SSchedulerException {
        return schedulerExecutor.rescheduleJob(triggerName, groupName, triggerStartTime);
    }

    @Override
    public boolean isExistingJob(final String jobName) throws SSchedulerException {
        return schedulerExecutor.isExistingJob(jobName, String.valueOf(getTenantId()));
    }

    @Override
    public boolean mayFireAgain(String groupName, String jobName) throws SSchedulerException {
        return schedulerExecutor.mayFireAgain(groupName, jobName);
    }
}
