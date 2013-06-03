/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
import org.bonitasoft.engine.scheduler.SSchedulerException;
import org.quartz.Job;
import org.quartz.JobDetail;
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
 */
public final class TransactionalSimpleJobFactory extends SimpleJobFactory {

    private final SchedulerImpl schedulerService;

    public TransactionalSimpleJobFactory(final SchedulerImpl schedulerService) {
        this.schedulerService = schedulerService;
    }

    @Override
    public Job newJob(final TriggerFiredBundle bundle, final Scheduler scheduler) throws SchedulerException {
        final Job newJob = super.newJob(bundle, scheduler);
        if (newJob instanceof QuartzJob) {
            final QuartzJob quartzJob = (QuartzJob) newJob;
            final JobDetail jobDetail = bundle.getJobDetail();
            final JobIdentifier jobIdentifier = (JobIdentifier) jobDetail.getJobDataMap().get("jobIdentifier");
            try {
                quartzJob.setBOSJob(schedulerService.getPersistedJob(jobIdentifier));
            } catch (final SSchedulerException e) {
                throw new org.quartz.SchedulerException("unable to create the BOS job", e);
            }
            // FIXME what to do with that
            // if (jobTruster.isTrusted(jobIdentifier)) {
            // try {
            // final Method method = jobIdentifier.getClass().getMethod("setQueriableLoggerService", QueriableLoggerService.class);
            // try {
            // method.invoke(jobIdentifier, logService);
            // } catch (final IllegalArgumentException e) {
            // e.printStackTrace();
            // } catch (final IllegalAccessException e) {
            // e.printStackTrace();
            // } catch (final InvocationTargetException e) {
            // e.printStackTrace();
            // }
            // } catch (final SecurityException e) {
            // e.printStackTrace();
            // } catch (final NoSuchMethodException e) {
            // e.printStackTrace();
            // }
            // }

            return quartzJob;
        }
        // FIXME a job that is not a BOS job was scheduled... not possible
        return newJob;
    }

}
