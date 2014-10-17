/**
 * Copyright (C) 2013-2014 BonitaSoft S.A.
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
 *
 * @since 6.1
 */
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
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.scheduler.AbstractBonitaPlatormJobListener;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.SchedulerExecutor;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.StatelessJob;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorNotFoundException;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorReadException;
import org.bonitasoft.engine.scheduler.exception.jobLog.SJobLogCreationException;
import org.bonitasoft.engine.scheduler.exception.jobLog.SJobLogUpdatingException;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobLog;
import org.bonitasoft.engine.scheduler.model.impl.SJobLogImpl;

/**
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public class JDBCJobListener extends AbstractBonitaPlatormJobListener {

    private static final long serialVersionUID = -5060516371371295271L;

    private final JobService jobService;

    private final IncidentService incidentService;

    private final TechnicalLoggerService logger;

    private final SchedulerService schedulerService;

    private final SchedulerExecutor schedulerExecutor;

    public JDBCJobListener(final SchedulerService schedulerService, final JobService jobService, final SchedulerExecutor schedulerExecutor,
            final IncidentService incidentService, final TechnicalLoggerService logger) {
        super();
        this.schedulerService = schedulerService;
        this.jobService = jobService;
        this.schedulerExecutor = schedulerExecutor;
        this.incidentService = incidentService;
        this.logger = logger;
    }

    @Override
    public String getName() {
        return "JDBCJobListener";
    }

    @Override
    public void jobToBeExecuted(final Map<String, Serializable> context) {
        final Long jobDescriptorId = (Long) context.get(JOB_DESCRIPTOR_ID);
        if (jobDescriptorId == null) {
            return;
        }
        try {
            final SJobDescriptor sJobDescriptor = jobService.getJobDescriptor(jobDescriptorId);
            if (sJobDescriptor == null) {
                if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.WARNING)) {
                    logger.log(this.getClass(), TechnicalLogSeverity.WARNING,
                            "No job descriptor found with id '" + jobDescriptorId + "'. It was probably deleted during the scheduler executed it.");
                }

                final String jobName = (String) context.get(JOB_NAME);
                final Long tenantId = (Long) context.get(TENANT_ID);
                schedulerExecutor.delete(jobName, String.valueOf(tenantId));
            }
        } catch (final SBonitaException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.WARNING)) {
                logger.log(this.getClass(), TechnicalLogSeverity.WARNING,
                        "An exception occurs during the check of the existence of the job descriptor '" + jobDescriptorId + "'.", e);
            }
        }
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
        try {
            if (jobDescriptorId != null) {
                if (jobException != null) {
                    final List<SJobLog> jobLogs = jobService.getJobLogs(jobDescriptorId, 0, 1);
                    if (!jobLogs.isEmpty()) {
                        updateJobLog(jobException, jobLogs);
                    } else {
                        createJobLog(jobException, jobDescriptorId);
                    }
                } else {
                    jobService.deleteJobLogs(jobDescriptorId);
                    deleteJobIfNotScheduledAnyMore(jobDescriptorId);
                }
            } else if (logger.isLoggable(getClass(), TechnicalLogSeverity.WARNING)) {
                logger.log(getClass(), TechnicalLogSeverity.WARNING, "An exception occurs during the job execution: " + jobException);
            }
        } catch (final SBonitaException sbe) {
            final Long tenantId = (Long) context.get(TENANT_ID);
            final Incident incident = new Incident("An exception occurs during the job execution of the job descriptor " + jobDescriptorId, "", jobException,
                    sbe);
            incidentService.report(tenantId, incident);
        }
    }

    private void createJobLog(final Exception jobException, final Long jobDescriptorId) throws SJobLogCreationException {
        final SJobLogImpl jobLog = new SJobLogImpl(jobDescriptorId);
        jobLog.setLastMessage(getStackTrace(jobException));
        jobLog.setRetryNumber(Long.valueOf(0));
        jobLog.setLastUpdateDate(System.currentTimeMillis());
        jobService.createJobLog(jobLog);
    }

    private void updateJobLog(final Exception jobException, final List<SJobLog> jobLogs) throws SJobLogUpdatingException {
        final SJobLog jobLog = jobLogs.get(0);
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField("lastMessage", getStackTrace(jobException));
        descriptor.addField("lastUpdateDate", System.currentTimeMillis());
        descriptor.addField("retryNumber", jobLog.getRetryNumber() + 1);
        jobService.updateJobLog(jobLog, descriptor);
    }

    private void deleteJobIfNotScheduledAnyMore(final Long jobDescriptorId) throws SJobDescriptorNotFoundException, SJobDescriptorReadException,
            SSchedulerException {
        try {
            final SJobDescriptor jobDescriptor = jobService.getJobDescriptor(jobDescriptorId);
            if (!schedulerService.isStillScheduled(jobDescriptor)) {
                schedulerService.delete(jobDescriptor.getJobName());
            }
        } catch (final SJobDescriptorNotFoundException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                final StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("jobDescriptor with id");
                stringBuilder.append(jobDescriptorId);
                stringBuilder.append(" already deleted, ignore it");
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, stringBuilder.toString());
            }
        }
    }

    private String getStackTrace(final Exception jobException) {
        final StringWriter exceptionWriter = new StringWriter();
        jobException.printStackTrace(new PrintWriter(exceptionWriter));
        return exceptionWriter.toString();
    }

}
