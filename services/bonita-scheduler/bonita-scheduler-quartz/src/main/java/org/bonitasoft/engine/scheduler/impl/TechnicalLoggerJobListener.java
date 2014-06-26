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
 **
 * @since 6.1
 */
package org.bonitasoft.engine.scheduler.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.StatelessJob;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

/**
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class TechnicalLoggerJobListener extends AbstractJobListener {

    private static final String jobToBeFiredMessage = "Job FIRED : group=''{1}'', name=''{0}'', class=''{5}'', data=''{6}'', triggerGroup=''{4}'', triggerName=''{3}'', at=''{2, date,HH:mm:ss MM/dd/yyyy}''";

    private static final String jobSuccessMessage = "Job COMPLETED : group=''{1}'', name=''{0}'', class=''{4}'', data=''{5}'', at=''{2, date,HH:mm:ss MM/dd/yyyy}'', reports=''{3}''";

    private static final String jobFailedMessage = "Job FAILED : group=''{1}'', name=''{0}'', class=''{4}'', data=''{5}'', at=''{2, date,HH:mm:ss MM/dd/yyyy}'', reports=''{3}''";

    private static final String jobWasVetoedMessage = "Job VETOED : group=''{1}'', name=''{0}'', class=''{5}'', triggerGroup=''{4}'', triggerName=''{3}'', at=''{2, date,HH:mm:ss MM/dd/yyyy}''";

    private final TechnicalLoggerService logger;

    private final boolean trace;

    private final boolean warning;

    public TechnicalLoggerJobListener(final TechnicalLoggerService logger) {
        this.logger = logger;
        trace = logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE);
        warning = logger.isLoggable(this.getClass(), TechnicalLogSeverity.WARNING);
    }

    @Override
    public String getName() {
        return "TechnicalLoggerJobListener";
    }

    @Override
    public void jobToBeExecuted(final JobExecutionContext context) {
        if (trace) {
            final Trigger trigger = context.getTrigger();
            final TriggerKey triggerKey = trigger.getKey();
            final JobDetail jobDetail = context.getJobDetail();
            final JobKey jobKey = jobDetail.getKey();
            final String jobType = getJobType(context.getJobInstance());

            final Object[] args = { jobKey.getName(), jobKey.getGroup(), new java.util.Date(), triggerKey.getName(), triggerKey.getGroup(), jobType,
                    getJobDataValueAndType(jobDetail), trigger.getPreviousFireTime(), trigger.getNextFireTime(), Integer.valueOf(context.getRefireCount()) };
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, MessageFormat.format(jobToBeFiredMessage, args));
        }
    }

    @Override
    public void jobExecutionVetoed(final JobExecutionContext context) {
        if (trace) {
            final Trigger trigger = context.getTrigger();
            final TriggerKey triggerKey = trigger.getKey();
            final JobDetail jobDetail = context.getJobDetail();
            final JobKey jobKey = jobDetail.getKey();
            final String jobType = getJobType(context.getJobInstance());

            final Object[] args = { jobKey.getName(), jobKey.getGroup(), new java.util.Date(), triggerKey.getName(), triggerKey.getGroup(), jobType,
                    trigger.getPreviousFireTime(), trigger.getNextFireTime(), Integer.valueOf(context.getRefireCount()) };
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, MessageFormat.format(jobWasVetoedMessage, args));
        }
    }

    @Override
    public void jobWasExecuted(final JobExecutionContext context, final JobExecutionException jobException) {
        final Trigger trigger = context.getTrigger();
        final TriggerKey triggerKey = trigger.getKey();
        final JobDetail jobDetail = context.getJobDetail();
        final JobKey jobKey = jobDetail.getKey();
        final String jobType = getJobType(context.getJobInstance());

        if (jobException != null) {
            if (warning) {
                final Object[] args = new Object[] { jobKey.getName(), jobKey.getGroup(), new java.util.Date(), jobException.getMessage(), jobType,
                        getJobDataValueAndType(jobDetail), triggerKey.getName(), triggerKey.getGroup(), trigger.getPreviousFireTime(),
                        trigger.getNextFireTime(), Integer.valueOf(context.getRefireCount()) };
                logger.log(this.getClass(), TechnicalLogSeverity.WARNING, MessageFormat.format(jobFailedMessage, args), jobException);
            }
        } else {
            if (trace) {
                final Object[] args = new Object[] { jobKey.getName(), jobKey.getGroup(), new java.util.Date(), String.valueOf(context.getResult()), jobType,
                        getJobDataValueAndType(jobDetail), triggerKey.getName(), triggerKey.getGroup(), trigger.getPreviousFireTime(),
                        trigger.getNextFireTime(), Integer.valueOf(context.getRefireCount()) };
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, MessageFormat.format(jobSuccessMessage, args));
            }
        }
    }

    private String getJobType(final Job job) {
        final String jobType;
        final Class<? extends Job> jobClass = job.getClass();
        if (QuartzJob.class.isAssignableFrom(jobClass)) {
            final StatelessJob bosJob = ((QuartzJob) job).getBosJob();
            if (bosJob != null) {
                if (bosJob instanceof JobWrapper) {
                    jobType = ((JobWrapper) bosJob).getStatelessJob().getClass().getName();
                } else {
                    jobType = bosJob.getClass().getName();
                }
            } else {
                return "null";
            }
        } else {
            jobType = jobClass.getName();
        }
        return jobType;
    }

    private List<JobDataValueAndType> getJobDataValueAndType(final JobDetail jobDetail) {
        final Set<Entry<String, Object>> entries = jobDetail.getJobDataMap().getWrappedMap().entrySet();
        final List<JobDataValueAndType> jobDatas = new ArrayList<JobDataValueAndType>(entries.size());
        for (final Entry<String, Object> entry : entries) {
            jobDatas.add(new JobDataValueAndType(entry));
        }
        return jobDatas;
    }

    private class JobDataValueAndType {

        private final String key;

        private final Object value;

        final String classOfValue;

        public JobDataValueAndType(final Entry<String, Object> jobData) {
            key = jobData.getKey();
            value = jobData.getValue();
            classOfValue = value.getClass().getName();
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder("{'");
            builder.append(key).append("'='").append(value).append("', class='").append(classOfValue).append("'}");
            return builder.toString();
        }
    }

}
