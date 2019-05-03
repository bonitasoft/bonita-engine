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

import org.bonitasoft.engine.scheduler.JobIdentifier;
import org.bonitasoft.engine.scheduler.StatelessJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Wraps a Bonita job.
 *
 * @author Matthieu Chaffotte
 * @author Baptsite Mesta
 * @author Celine Souchet
 */
public abstract class AbstractQuartzJob implements org.quartz.Job {

    private StatelessJob bosJob;
    private SchedulerServiceImpl schedulerService;
    private JobDetail jobDetail;

    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException {
        try {
            final JobIdentifier jobIdentifier = getJobIdentifier(jobDetail.getJobDataMap());
            bosJob = retrieveJob(jobIdentifier);
            bosJob.execute();
        } catch (final Throwable e) {
            throw new JobExecutionException(e);
        }
    }

    private StatelessJob retrieveJob(JobIdentifier jobIdentifier) throws JobExecutionException {
        try {
            return schedulerService.getPersistedJob(jobIdentifier);
        } catch (final Throwable t) {
            throw new JobExecutionException("unable to create the BOS job", t);
        }
    }

    private JobIdentifier getJobIdentifier(JobDataMap jobDataMap) {
        final long tenantId = Long.parseLong((String) jobDataMap.get("tenantId"));
        final long jobId = Long.parseLong((String) jobDataMap.get("jobId"));
        final String jobName = (String) jobDataMap.get("jobName");
        return new JobIdentifier(jobId, tenantId, jobName);
    }

    StatelessJob getBosJob() {
        return bosJob;
    }

    void setSchedulerService(SchedulerServiceImpl schedulerService) {
        this.schedulerService = schedulerService;
    }

    void setJobDetails(JobDetail jobDetail) {
        this.jobDetail = jobDetail;
    }

    JobDetail getJobDetail() {
        return jobDetail;
    }

    SchedulerServiceImpl getSchedulerService() {
        return schedulerService;
    }
}
