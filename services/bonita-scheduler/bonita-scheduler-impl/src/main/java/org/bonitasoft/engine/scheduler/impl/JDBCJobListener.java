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

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.incident.Incident;
import org.bonitasoft.engine.incident.IncidentService;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.scheduler.AbstractBonitaPlatformJobListener;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.SchedulerExecutor;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.StatelessJob;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorReadException;
import org.bonitasoft.engine.scheduler.exception.jobLog.SJobLogCreationException;
import org.bonitasoft.engine.scheduler.exception.jobLog.SJobLogUpdatingException;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobLog;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public class JDBCJobListener extends AbstractBonitaPlatformJobListener {

    private static final long serialVersionUID = -5060516371371295271L;

    private final JobService jobService;

    private final IncidentService incidentService;

    private final TechnicalLoggerService logger;

    private final JobLogCreator jobLogCreator;

    private final SchedulerService schedulerService;

    private final SchedulerExecutor schedulerExecutor;

    private final SessionAccessor sessionAccessor;

    private final TransactionService transactionService;

    public JDBCJobListener(final SchedulerService schedulerService, final JobService jobService, final SchedulerExecutor schedulerExecutor,
            final SessionAccessor sessionAccessor, final TransactionService transactionService, final IncidentService incidentService,
            final TechnicalLoggerService logger, final JobLogCreator jobLogCreator) {
        super();
        this.schedulerService = schedulerService;
        this.jobService = jobService;
        this.schedulerExecutor = schedulerExecutor;
        this.sessionAccessor = sessionAccessor;
        this.transactionService = transactionService;
        this.incidentService = incidentService;
        this.logger = logger;
        this.jobLogCreator = jobLogCreator;
    }

    @Override
    public String getName() {
        return "JDBCJobListener";
    }

    @Override
    public void jobToBeExecuted(final Map<String, Serializable> context) {
        final Long jobDescriptorId = (Long) context.get(JOB_DESCRIPTOR_ID);
        final Long tenantId = (Long) context.get(TENANT_ID);
        if (isSessionRelated(jobDescriptorId, tenantId)) {
            deleteRelatedJob(context, jobDescriptorId, tenantId);
        }
    }

    private void deleteRelatedJob(final Map<String, Serializable> context, final Long jobDescriptorId, final Long tenantId) {
        try {
            // Set the tenant id, because the jobService is a tenant service and need a session to use the tenant persistence service. But, a job listener runs not in a session.
            sessionAccessor.setTenantId(tenantId);
            final SJobDescriptor sJobDescriptor = jobService.getJobDescriptor(jobDescriptorId);
            if (sJobDescriptor == null) {
                deleteJob(context, jobDescriptorId, tenantId);
            }
        } catch (final SBonitaException e) {
            logWarningWhenExceptionOccurs("check of the existence of the job descriptor '" + jobDescriptorId + "'.", e);
        } finally {
            cleanSession();
        }
    }

    private void logWarningWhenExceptionOccurs(final String message, final Exception e) {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.WARNING)) {
            logger.log(this.getClass(), TechnicalLogSeverity.WARNING, "An exception occurs during the " + message, e);
        }
    }

    private void deleteJob(final Map<String, Serializable> context, final Long jobDescriptorId, final Long tenantId) throws SSchedulerException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.WARNING)) {
            logger.log(this.getClass(), TechnicalLogSeverity.WARNING,
                    "No job descriptor found with id '" + jobDescriptorId + "'. It was probably deleted during the scheduler executed it.");
        }

        final String jobName = (String) context.get(JOB_NAME);
        schedulerExecutor.delete(jobName, String.valueOf(tenantId));
    }

    @Override
    public void jobExecutionVetoed(final Map<String, Serializable> context) {
        // nothing to do
    }

    @Override
    public void jobWasExecuted(final Map<String, Serializable> context, final Exception jobException) {
        final StatelessJob bosJob = (StatelessJob) context.get(BOS_JOB);
        if (bosJob == null) {
            return;
        }

        final Long jobDescriptorId = (Long) context.get(JOB_DESCRIPTOR_ID);
        final Long tenantId = (Long) context.get(TENANT_ID);
        if (isSessionRelated(jobDescriptorId, tenantId)) {
            performPostExecutionActions(jobException, jobDescriptorId, tenantId);
        } else {
            logWarningWhenExceptionOccurs("job execution.", jobException);
        }
    }

    private boolean isSessionRelated(final Long jobDescriptorId, final Long tenantId) {
        return isNotNullOrEmpty(jobDescriptorId) && isNotNullOrEmpty(tenantId);
    }

    private void performPostExecutionActions(final Exception jobException, final Long jobDescriptorId, final Long tenantId) {
        // Set the tenant id, because the jobService is a tenant service and need a session to use the tenant persistence service. But, a job listener runs not in a session.
        sessionAccessor.setTenantId(tenantId);
        try {
            if (jobException != null) {
                setJobLog(jobException, jobDescriptorId);
            } else {
                jobService.deleteJobLogs(jobDescriptorId);
                deleteJobIfNotScheduledAnyMore(jobDescriptorId);
            }
        } catch (final SBonitaException sbe) {
            final Incident incident = new Incident("An exception occurs during the job execution of the job descriptor " + jobDescriptorId, "",
                    jobException, sbe);
            incidentService.report(tenantId, incident);
        } finally {
            cleanSession();
        }
    }

    private void setJobLog(final Exception jobException, final Long jobDescriptorId) throws SBonitaReadException, SJobLogUpdatingException,
            SJobLogCreationException, SJobDescriptorReadException {
        final List<SJobLog> jobLogs = jobService.getJobLogs(jobDescriptorId, 0, 1);
        if (!jobLogs.isEmpty()) {
            updateJobLog(jobException, jobLogs);
        } else {
            jobLogCreator.createJobLog(jobException, jobDescriptorId);
        }
    }

    private boolean isNotNullOrEmpty(final Long id) {
        return id != null && id != 0;
    }

    private void cleanSession() {
        try {
            transactionService.registerBonitaSynchronization(new BonitaTransactionSynchronizationImpl(sessionAccessor));
        } catch (final STransactionNotFoundException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.WARNING)) {
                logger.log(this.getClass(), TechnicalLogSeverity.WARNING, e);
            }
        }
    }

    private void updateJobLog(final Exception jobException, final List<SJobLog> jobLogs) throws SJobLogUpdatingException {
        final SJobLog jobLog = jobLogs.get(0);
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField("lastMessage", getStackTrace(jobException));
        descriptor.addField("lastUpdateDate", System.currentTimeMillis());
        descriptor.addField("retryNumber", jobLog.getRetryNumber() + 1);
        jobService.updateJobLog(jobLog, descriptor);
    }

    private void deleteJobIfNotScheduledAnyMore(final Long jobDescriptorId) throws SSchedulerException {
        final SJobDescriptor jobDescriptor = jobService.getJobDescriptor(jobDescriptorId);
        if (jobDescriptor != null && !schedulerService.isStillScheduled(jobDescriptor)) {
            schedulerService.delete(jobDescriptor.getJobName());
        } else if (jobDescriptor == null && logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("jobDescriptor with id");
            stringBuilder.append(jobDescriptorId);
            stringBuilder.append(" already deleted, ignore it");
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, stringBuilder.toString());
        }
    }

    private String getStackTrace(final Exception jobException) {
        final StringWriter exceptionWriter = new StringWriter();
        jobException.printStackTrace(new PrintWriter(exceptionWriter));
        return exceptionWriter.toString();
    }

}
