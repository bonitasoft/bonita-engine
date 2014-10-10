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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.scheduler.AbstractBonitaJobListener;
import org.bonitasoft.engine.scheduler.AbstractBonitaTenantJobListener;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

/**
 * @author Celine Souchet
 */
public class TenantQuartzJobListener extends AbstractQuartzJobListener {

    private final List<AbstractBonitaTenantJobListener> bonitaJobListeners;

    private final String groupName;

    public TenantQuartzJobListener(final List<AbstractBonitaTenantJobListener> bonitaJobListeners, final String groupName) {
        this.bonitaJobListeners = bonitaJobListeners;
        this.groupName = groupName;
    }

    @Override
    public String getName() {
        return "TenantQuartzJobListener_" + groupName + "_" + System.currentTimeMillis();
    }

    @Override
    public void jobToBeExecuted(final JobExecutionContext context) {
        final JobDetail jobDetail = context.getJobDetail();
        final Trigger trigger = context.getTrigger();
        final TriggerKey triggerKey = trigger.getKey();
        final JobKey jobKey = jobDetail.getKey();

        final Map<String, Serializable> mapContext = new HashMap<String, Serializable>();
        mapContext.put(AbstractBonitaJobListener.BOS_JOB, getBosJob(context));
        mapContext.put(AbstractBonitaJobListener.JOB_DESCRIPTOR_ID, getJobDescriptorId(jobDetail));
        mapContext.put(AbstractBonitaJobListener.TENANT_ID, getTenantId(jobDetail));
        mapContext.put(AbstractBonitaJobListener.JOB_TYPE, getJobType(context.getJobInstance()));
        mapContext.put(AbstractBonitaJobListener.JOB_NAME, jobKey.getName());
        mapContext.put(AbstractBonitaJobListener.JOB_GROUP, jobKey.getGroup());
        mapContext.put(AbstractBonitaJobListener.TRIGGER_NAME, triggerKey.getName());
        mapContext.put(AbstractBonitaJobListener.TRIGGER_GROUP, triggerKey.getGroup());
        mapContext.put(AbstractBonitaJobListener.TRIGGER_PREVIOUS_FIRE_TIME, trigger.getPreviousFireTime());
        mapContext.put(AbstractBonitaJobListener.TRIGGER_NEXT_FIRE_TIME, trigger.getNextFireTime());
        mapContext.put(AbstractBonitaJobListener.REFIRE_COUNT, Integer.valueOf(context.getRefireCount()));
        mapContext.put(AbstractBonitaJobListener.JOB_DATAS, (Serializable) getJobDataValueAndType(jobDetail));
        mapContext.put(AbstractBonitaJobListener.JOB_RESULT, String.valueOf(context.getResult()));

        for (final AbstractBonitaTenantJobListener abstractBonitaTenantJobListener : bonitaJobListeners) {
            abstractBonitaTenantJobListener.jobToBeExecuted(mapContext);
        }
    }

    @Override
    public void jobExecutionVetoed(final JobExecutionContext context) {
        final JobDetail jobDetail = context.getJobDetail();
        final Trigger trigger = context.getTrigger();
        final TriggerKey triggerKey = trigger.getKey();
        final JobKey jobKey = jobDetail.getKey();

        final Map<String, Serializable> mapContext = new HashMap<String, Serializable>();
        mapContext.put(AbstractBonitaJobListener.BOS_JOB, getBosJob(context));
        mapContext.put(AbstractBonitaJobListener.JOB_DESCRIPTOR_ID, getJobDescriptorId(jobDetail));
        mapContext.put(AbstractBonitaJobListener.TENANT_ID, getTenantId(jobDetail));
        mapContext.put(AbstractBonitaJobListener.JOB_TYPE, getJobType(context.getJobInstance()));
        mapContext.put(AbstractBonitaJobListener.JOB_NAME, jobKey.getName());
        mapContext.put(AbstractBonitaJobListener.JOB_GROUP, jobKey.getGroup());
        mapContext.put(AbstractBonitaJobListener.TRIGGER_NAME, triggerKey.getName());
        mapContext.put(AbstractBonitaJobListener.TRIGGER_GROUP, triggerKey.getGroup());
        mapContext.put(AbstractBonitaJobListener.TRIGGER_PREVIOUS_FIRE_TIME, trigger.getPreviousFireTime());
        mapContext.put(AbstractBonitaJobListener.TRIGGER_NEXT_FIRE_TIME, trigger.getNextFireTime());
        mapContext.put(AbstractBonitaJobListener.REFIRE_COUNT, Integer.valueOf(context.getRefireCount()));
        mapContext.put(AbstractBonitaJobListener.JOB_DATAS, (Serializable) getJobDataValueAndType(jobDetail));
        mapContext.put(AbstractBonitaJobListener.JOB_RESULT, String.valueOf(context.getResult()));

        for (final AbstractBonitaTenantJobListener abstractBonitaTenantJobListener : bonitaJobListeners) {
            abstractBonitaTenantJobListener.jobExecutionVetoed(mapContext);
        }
    }

    @Override
    public void jobWasExecuted(final JobExecutionContext context, final JobExecutionException jobException) {
        final JobDetail jobDetail = context.getJobDetail();
        final Trigger trigger = context.getTrigger();
        final TriggerKey triggerKey = trigger.getKey();
        final JobKey jobKey = jobDetail.getKey();

        final Map<String, Serializable> mapContext = new HashMap<String, Serializable>();
        mapContext.put(AbstractBonitaJobListener.BOS_JOB, getBosJob(context));
        mapContext.put(AbstractBonitaJobListener.JOB_DESCRIPTOR_ID, getJobDescriptorId(jobDetail));
        mapContext.put(AbstractBonitaJobListener.TENANT_ID, getTenantId(jobDetail));
        mapContext.put(AbstractBonitaJobListener.JOB_TYPE, getJobType(context.getJobInstance()));
        mapContext.put(AbstractBonitaJobListener.JOB_NAME, jobKey.getName());
        mapContext.put(AbstractBonitaJobListener.JOB_GROUP, jobKey.getGroup());
        mapContext.put(AbstractBonitaJobListener.TRIGGER_NAME, triggerKey.getName());
        mapContext.put(AbstractBonitaJobListener.TRIGGER_GROUP, triggerKey.getGroup());
        mapContext.put(AbstractBonitaJobListener.TRIGGER_PREVIOUS_FIRE_TIME, trigger.getPreviousFireTime());
        mapContext.put(AbstractBonitaJobListener.TRIGGER_NEXT_FIRE_TIME, trigger.getNextFireTime());
        mapContext.put(AbstractBonitaJobListener.REFIRE_COUNT, Integer.valueOf(context.getRefireCount()));
        mapContext.put(AbstractBonitaJobListener.JOB_DATAS, (Serializable) getJobDataValueAndType(jobDetail));
        mapContext.put(AbstractBonitaJobListener.JOB_RESULT, String.valueOf(context.getResult()));

        for (final AbstractBonitaTenantJobListener abstractBonitaTenantJobListener : bonitaJobListeners) {
            abstractBonitaTenantJobListener.jobWasExecuted(mapContext, jobException);
        }
    }

}
