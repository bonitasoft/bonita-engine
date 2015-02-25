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

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.JobIdentifier;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.simpl.SimpleJobFactory;
import org.quartz.spi.TriggerFiredBundle;

/**
 * Job factory that inject the transaction service
 * Must modify this to inject the configuration service instead
 * 
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public final class TransactionalSimpleJobFactory extends SimpleJobFactory {

    private final SchedulerServiceImpl schedulerService;

    private final TechnicalLoggerService logger;

    public TransactionalSimpleJobFactory(final SchedulerServiceImpl schedulerService, final TechnicalLoggerService logger) {
        this.schedulerService = schedulerService;
        this.logger = logger;
    }

    @Override
    public Job newJob(final TriggerFiredBundle bundle, final Scheduler scheduler) throws SchedulerException {
        final Job newJob = super.newJob(bundle, scheduler);
        if (newJob instanceof AbstractQuartzJob) {
            final AbstractQuartzJob quartzJob = (AbstractQuartzJob) newJob;
            final JobDataMap jobDataMap = bundle.getJobDetail().getJobDataMap();
            final Long tenantId = Long.valueOf((String) jobDataMap.get("tenantId"));
            final Long jobId = Long.valueOf((String) jobDataMap.get("jobId"));
            final String jobName = (String) jobDataMap.get("jobName");
            final JobIdentifier jobIdentifier = new JobIdentifier(jobId, tenantId, jobName);
            try {
                quartzJob.setBosJob(schedulerService.getPersistedJob(jobIdentifier));
            } catch (final Throwable t) {
                if (isInternalCronJob(jobName)) {
                    logExecutionIgnored(bundle, jobName, t);
                    quartzJob.setBosJob(null);
                } else {
                    throw new org.quartz.SchedulerException("unable to create the BOS job", t);
                }
            }
            return quartzJob;
        }
        // FIXME a job that is not a BOS job was scheduled... not possible
        return newJob;
    }

    private void logExecutionIgnored(final TriggerFiredBundle bundle, final String jobName, final Throwable t) {
        if (logger.isLoggable(getClass(), TechnicalLogSeverity.WARNING)) {
            final StringBuilder stb = new StringBuilder();
            stb.append("Unable to create the BOS job '");
            stb.append(jobName);
            stb.append("'. Ignoring this execution. Next fire time is: ");
            stb.append(bundle.getNextFireTime());
            logger.log(getClass(), TechnicalLogSeverity.WARNING, stb.toString());
        }
        if (logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
            logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Unable to crete the BOS job due to: ", t);
        }
    }

    private boolean isInternalCronJob(final String jobName) {
        final List<String> internalCronJobs = Arrays.asList("BPMEventHandling", "CleanInvalidSessions", "InsertBatchLogsJob");
        return internalCronJobs.contains(jobName);
    }

}
