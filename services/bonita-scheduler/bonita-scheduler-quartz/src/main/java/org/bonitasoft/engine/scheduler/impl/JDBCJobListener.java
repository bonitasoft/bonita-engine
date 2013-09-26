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
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobLog;
import org.bonitasoft.engine.scheduler.model.impl.SJobLogImpl;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class JDBCJobListener extends AbstractJobListener {

    private final JobService jobService;

    private final TechnicalLoggerService logger;

    public JDBCJobListener(final JobService jobService, final TechnicalLoggerService logger) {
        super();
        this.jobService = jobService;
        this.logger = logger;
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
        final Long jobDescriptorId = (Long) jobDetail.getJobDataMap().getWrappedMap().get("jobId");
        try {
            if (jobException != null) {
                final List<SJobLog> jobLogs = getJobLogs(jobDescriptorId);
                if (!jobLogs.isEmpty()) {
                    final SJobLogImpl jobLog = (SJobLogImpl) jobLogs.get(0);
                    jobLog.setLastMessage(getStackTrace(jobException));
                    jobLog.setLastUpdateDate(System.currentTimeMillis());
                    jobLog.setRetryNumber(jobLog.getRetryNumber() + 1);
                } else {
                    final SJobLogImpl jobLog = new SJobLogImpl(jobDescriptorId);
                    jobLog.setLastMessage(getStackTrace(jobException));
                    jobLog.setRetryNumber(Long.valueOf(0));
                    jobLog.setLastUpdateDate(System.currentTimeMillis());
                    jobService.createJobLog(jobLog);
                }
            } else {
                final List<SJobLog> jobLogs = getJobLogs(jobDescriptorId);
                if (!jobLogs.isEmpty()) {
                    jobService.deleteJobLog(jobLogs.get(0));
                }
                final SJobDescriptor jobDescriptor = jobService.getJobDescriptor(jobDescriptorId);
                if (!getSchedulerService().isStillScheduled(jobDescriptor)) {
                    getSchedulerService().delete(jobDescriptor.getJobName());
                }
            }
        } catch (final SBonitaException e) {
            logger.log(getClass(), TechnicalLogSeverity.ERROR, "Unable to handle the job completion", e);
        }
    }

    private List<SJobLog> getJobLogs(final long jobDescriptorId) throws SBonitaSearchException {
        final List<FilterOption> filters = new ArrayList<FilterOption>(2);
        filters.add(new FilterOption(SJobLog.class, "jobDescriptorId", jobDescriptorId));
        final QueryOptions options = new QueryOptions(0, 1, null, filters, null);
        return jobService.searchJobLogs(options);
    }

    private String getStackTrace(final JobExecutionException jobException) {
        final StringWriter exceptionWriter = new StringWriter();
        jobException.printStackTrace(new PrintWriter(exceptionWriter));
        return exceptionWriter.toString();
    }

}
