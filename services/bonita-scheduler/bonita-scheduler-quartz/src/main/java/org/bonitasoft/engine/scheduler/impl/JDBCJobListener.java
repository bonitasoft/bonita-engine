/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.incident.Incident;
import org.bonitasoft.engine.incident.IncidentService;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorNotFoundException;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorReadException;
import org.bonitasoft.engine.scheduler.exception.jobLog.SJobLogCreationException;
import org.bonitasoft.engine.scheduler.exception.jobLog.SJobLogDeletionException;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobLog;
import org.bonitasoft.engine.scheduler.model.impl.SJobLogImpl;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public class JDBCJobListener extends AbstractJobListener {

    private final JobService jobService;

    private final IncidentService incidentService;

    public JDBCJobListener(final JobService jobService, final IncidentService incidentService) {
        super();
        this.jobService = jobService;
        this.incidentService = incidentService;
    }

    @Override
    public String getName() {
        return "JDBCJobListener";
    }

    @Override
    public void jobToBeExecuted(final JobExecutionContext context) {
        // nothing to do
    }

    @Override
    public void jobExecutionVetoed(final JobExecutionContext context) {
        // nothing to do
    }

    @Override
    public void jobWasExecuted(final JobExecutionContext context, final JobExecutionException jobException) {
        final JobDetail jobDetail = context.getJobDetail();
        if (isEmptyJob(context)) {
            return;
        }
        final Long jobDescriptorId = Long.valueOf((String) jobDetail.getJobDataMap().getWrappedMap().get("jobId"));
        try {
            if (jobException != null) {
                final List<SJobLog> jobLogs = getJobLogs(jobDescriptorId);
                if (!jobLogs.isEmpty()) {
                    updateJobLog(jobException, jobLogs);
                } else {
                    createJobLog(jobException, jobDescriptorId);
                }
            } else {
                cleanJobLogIfAny(jobDescriptorId);
                deleteJobIfNotScheduledAnyMore(jobDescriptorId);
            }
        } catch (final SBonitaException sbe) {
            final Long tenantId = Long.valueOf((String) jobDetail.getJobDataMap().getWrappedMap().get("tenantId"));
            final Incident incident = new Incident("An exception occurs during the job execution of the job descriptor" + jobDescriptorId, "", jobException,
                    sbe);
            incidentService.report(tenantId, incident);
        }
    }

    private boolean isEmptyJob(final JobExecutionContext context) {
        final Job instance = context.getJobInstance();
        if (instance != null && instance instanceof QuartzJob) {
            final QuartzJob job = (QuartzJob) instance;
            if (job.getBosJob() == null) {
                return true;
            }
        }
        return false;
    }

    private void createJobLog(final JobExecutionException jobException, final Long jobDescriptorId) throws SJobLogCreationException {
        final SJobLogImpl jobLog = new SJobLogImpl(jobDescriptorId);
        jobLog.setLastMessage(getStackTrace(jobException));
        jobLog.setRetryNumber(Long.valueOf(0));
        jobLog.setLastUpdateDate(System.currentTimeMillis());
        jobService.createJobLog(jobLog);
    }

    private void updateJobLog(final JobExecutionException jobException, final List<SJobLog> jobLogs) {
        final SJobLogImpl jobLog = (SJobLogImpl) jobLogs.get(0);
        jobLog.setLastMessage(getStackTrace(jobException));
        jobLog.setLastUpdateDate(System.currentTimeMillis());
        jobLog.setRetryNumber(jobLog.getRetryNumber() + 1);
    }

    private void deleteJobIfNotScheduledAnyMore(final Long jobDescriptorId) throws SJobDescriptorNotFoundException, SJobDescriptorReadException,
    SSchedulerException {
        final SJobDescriptor jobDescriptor = jobService.getJobDescriptor(jobDescriptorId);
        if (!getSchedulerService().isStillScheduled(jobDescriptor)) {
            getSchedulerService().delete(jobDescriptor.getJobName());
        }
    }

    private void cleanJobLogIfAny(final Long jobDescriptorId) throws SBonitaSearchException, SJobLogDeletionException {
        final List<SJobLog> jobLogs = getJobLogs(jobDescriptorId);
        if (!jobLogs.isEmpty()) {
            jobService.deleteJobLog(jobLogs.get(0));
        }
    }

    private List<SJobLog> getJobLogs(final long jobDescriptorId) throws SBonitaSearchException {
        final List<FilterOption> filters = new ArrayList<FilterOption>(2);
        filters.add(new FilterOption(SJobLog.class, "jobDescriptorId", jobDescriptorId));
        final OrderByOption orderByOption = new OrderByOption(SJobLog.class, "jobDescriptorId", OrderByType.ASC);
        final QueryOptions options = new QueryOptions(0, 1, Arrays.asList(orderByOption), filters, null);
        return jobService.searchJobLogs(options);
    }

    private String getStackTrace(final JobExecutionException jobException) {
        final StringWriter exceptionWriter = new StringWriter();
        jobException.printStackTrace(new PrintWriter(exceptionWriter));
        return exceptionWriter.toString();
    }

}
