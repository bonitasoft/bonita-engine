/**
 * Copyright (C) 2014 BonitaSoft S.A.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.bonitasoft.engine.scheduler.StatelessJob;
import org.bonitasoft.engine.scheduler.model.SJobData;
import org.bonitasoft.engine.scheduler.model.impl.SJobDataImpl;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobListener;

/**
 * @author Celine Souchet
 */
public abstract class AbstractQuartzJobListener implements JobListener {

    protected Long getJobDescriptorId(final JobDetail jobDetail) {
        return Long.valueOf((String) jobDetail.getJobDataMap().getWrappedMap().get("jobId"));
    }

    protected Long getTenantId(final JobDetail jobDetail) {
        return Long.valueOf((String) jobDetail.getJobDataMap().getWrappedMap().get("tenantId"));
    }

    protected StatelessJob getBosJob(final JobExecutionContext context) {
        final Job instance = context.getJobInstance();
        if (instance != null && instance instanceof AbstractQuartzJob) {
            final AbstractQuartzJob job = (AbstractQuartzJob) instance;
            return job.getBosJob();
        }
        return null;
    }

    protected List<SJobData> getJobDataValueAndType(final JobDetail jobDetail) {
        final Set<Entry<String, Object>> entries = jobDetail.getJobDataMap().getWrappedMap().entrySet();
        final List<SJobData> jobDatas = new ArrayList<SJobData>(entries.size());
        for (final Entry<String, Object> entry : entries) {
            jobDatas.add(new SJobDataImpl(entry));
        }
        return jobDatas;
    }

    protected String getJobType(final Job job) {
        final String jobType;
        final Class<? extends Job> jobClass = job.getClass();
        if (AbstractQuartzJob.class.isAssignableFrom(jobClass)) {
            final StatelessJob bosJob = ((AbstractQuartzJob) job).getBosJob();
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
}
