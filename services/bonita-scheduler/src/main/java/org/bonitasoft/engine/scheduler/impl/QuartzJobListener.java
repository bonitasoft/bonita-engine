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

import static org.bonitasoft.engine.scheduler.BonitaJobListener.BOS_JOB;
import static org.bonitasoft.engine.scheduler.BonitaJobListener.JOB_DATAS;
import static org.bonitasoft.engine.scheduler.BonitaJobListener.JOB_DESCRIPTOR_ID;
import static org.bonitasoft.engine.scheduler.BonitaJobListener.JOB_GROUP;
import static org.bonitasoft.engine.scheduler.BonitaJobListener.JOB_NAME;
import static org.bonitasoft.engine.scheduler.BonitaJobListener.JOB_RESULT;
import static org.bonitasoft.engine.scheduler.BonitaJobListener.JOB_TYPE;
import static org.bonitasoft.engine.scheduler.BonitaJobListener.REFIRE_COUNT;
import static org.bonitasoft.engine.scheduler.BonitaJobListener.TENANT_ID;
import static org.bonitasoft.engine.scheduler.BonitaJobListener.TRIGGER_GROUP;
import static org.bonitasoft.engine.scheduler.BonitaJobListener.TRIGGER_NAME;
import static org.bonitasoft.engine.scheduler.BonitaJobListener.TRIGGER_NEXT_FIRE_TIME;
import static org.bonitasoft.engine.scheduler.BonitaJobListener.TRIGGER_PREVIOUS_FIRE_TIME;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.scheduler.BonitaJobListener;
import org.bonitasoft.engine.scheduler.StatelessJob;
import org.bonitasoft.engine.scheduler.model.SJobData;
import org.bonitasoft.engine.scheduler.model.impl.SJobDataImpl;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

@Slf4j
public class QuartzJobListener implements JobListener {

    private final SessionAccessor sessionAccessor;
    private final List<BonitaJobListener> bonitaJobListeners;

    QuartzJobListener(final List<BonitaJobListener> bonitaJobListeners,
            final SessionAccessor sessionAccessor) {
        this.bonitaJobListeners = bonitaJobListeners;
        this.sessionAccessor = sessionAccessor;
    }

    @Override
    public String getName() {
        return "QuartzJobListener";
    }

    List<BonitaJobListener> getBonitaJobListeners() {
        return bonitaJobListeners;
    }

    @Override
    public void jobToBeExecuted(final JobExecutionContext context) {
        final Map<String, Serializable> mapContext = buildMapContext(context);
        inTenantSession(mapContext, () -> {
            for (final BonitaJobListener abstractBonitaTenantJobListener : bonitaJobListeners) {
                abstractBonitaTenantJobListener.jobToBeExecuted(mapContext);
            }
            return null;
        });
    }

    @Override
    public void jobExecutionVetoed(final JobExecutionContext context) {
        final Map<String, Serializable> mapContext = buildMapContext(context);
        inTenantSession(mapContext, () -> {
            for (final BonitaJobListener abstractBonitaTenantJobListener : bonitaJobListeners) {
                abstractBonitaTenantJobListener.jobExecutionVetoed(mapContext);
            }
            return null;
        });
    }

    @Override
    public void jobWasExecuted(final JobExecutionContext context, final JobExecutionException jobException) {
        final Map<String, Serializable> mapContext = buildMapContext(context);
        inTenantSession(mapContext, () -> {
            for (final BonitaJobListener abstractBonitaTenantJobListener : bonitaJobListeners) {
                abstractBonitaTenantJobListener.jobWasExecuted(mapContext, jobException);
            }
            return null;
        });
    }

    private void inTenantSession(Map<String, Serializable> context, Callable<Void> callable) {
        Long tenantId = ((Long) context.get(BonitaJobListener.TENANT_ID));
        if (tenantId != null) {
            sessionAccessor.setTenantId(tenantId);
        }
        try {
            callable.call();
        } catch (Throwable e) {
            log.warn("Unable to execute job listener", e);
        }
    }

    private Long getJobDescriptorId(final JobDetail jobDetail) {
        return Long.valueOf((String) jobDetail.getJobDataMap().getWrappedMap().get("jobId"));
    }

    protected Long getTenantId(final JobDetail jobDetail) {
        return Long.valueOf((String) jobDetail.getJobDataMap().getWrappedMap().get("tenantId"));
    }

    private StatelessJob getBosJob(final JobExecutionContext context) {
        final Job instance = context.getJobInstance();
        if (instance instanceof AbstractQuartzJob job) {
            return job.getBosJob();
        }
        return null;
    }

    private List<SJobData> getJobDataValueAndType(final JobDetail jobDetail) {
        final Set<Map.Entry<String, Object>> entries = jobDetail.getJobDataMap().getWrappedMap().entrySet();
        final List<SJobData> jobDatas = new ArrayList<SJobData>(entries.size());
        for (final Map.Entry<String, Object> entry : entries) {
            jobDatas.add(new SJobDataImpl(entry));
        }
        return jobDatas;
    }

    private String getJobType(final Job job) {
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

    private Map<String, Serializable> buildMapContext(final JobExecutionContext context) {
        final JobDetail jobDetail = context.getJobDetail();
        final Trigger trigger = context.getTrigger();
        final TriggerKey triggerKey = trigger.getKey();
        final JobKey jobKey = jobDetail.getKey();

        final Map<String, Serializable> mapContext = new HashMap<>();
        mapContext.put(BOS_JOB, getBosJob(context));
        mapContext.put(JOB_DESCRIPTOR_ID, getJobDescriptorId(jobDetail));
        mapContext.put(TENANT_ID, getTenantId(jobDetail));
        mapContext.put(JOB_TYPE, getJobType(context.getJobInstance()));
        mapContext.put(JOB_NAME, jobKey.getName());
        mapContext.put(JOB_GROUP, jobKey.getGroup());
        mapContext.put(TRIGGER_NAME, triggerKey.getName());
        mapContext.put(TRIGGER_GROUP, triggerKey.getGroup());
        mapContext.put(TRIGGER_PREVIOUS_FIRE_TIME, trigger.getPreviousFireTime());
        mapContext.put(TRIGGER_NEXT_FIRE_TIME, trigger.getNextFireTime());
        mapContext.put(REFIRE_COUNT, context.getRefireCount());
        mapContext.put(JOB_DATAS, (Serializable) getJobDataValueAndType(jobDetail));
        mapContext.put(JOB_RESULT, String.valueOf(context.getResult()));
        return mapContext;
    }
}
