/**
 * Copyright (C) 2011, 2014 BonitaSoft S.A.
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

import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerKey.triggerKey;
import static org.quartz.impl.matchers.GroupMatcher.jobGroupEquals;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bonitasoft.engine.scheduler.SchedulerExecutor;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.scheduler.trigger.CronTrigger;
import org.bonitasoft.engine.scheduler.trigger.RepeatTrigger;
import org.bonitasoft.engine.scheduler.trigger.Trigger;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.transaction.TransactionState;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.ListenerManager;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.core.QuartzScheduler;
import org.quartz.impl.matchers.GroupMatcher;

/**
 * @author Matthieu Chaffotte
 * @author Yanyan Liu
 */
public class QuartzSchedulerExecutor implements SchedulerExecutor {

    private Scheduler scheduler;

    private final BonitaSchedulerFactory schedulerFactory;

    private final ReadSessionAccessor sessionAccessor;

    private final TransactionService transactionService;

    private final boolean useOptimization;

    private QuartzScheduler quartzScheduler;

    private final List<AbstractJobListener> jobListeners;

    public QuartzSchedulerExecutor(final BonitaSchedulerFactory schedulerFactory, final List<AbstractJobListener> jobListeners,
            final ReadSessionAccessor sessionAccessor, final TransactionService transactionService, final boolean useOptimization) {
        this.sessionAccessor = sessionAccessor;
        this.transactionService = transactionService;
        this.useOptimization = useOptimization;
        this.schedulerFactory = schedulerFactory;
        this.jobListeners = jobListeners;
    }

    @Override
    public void schedule(final long jobId, final long tenantId, final String jobName, final Trigger trigger, final boolean disallowConcurrentExecution)
            throws SSchedulerException {
        try {
            checkSchedulerState();
            final JobDetail jobDetail = getJobDetail(jobId, tenantId, jobName, disallowConcurrentExecution);
            final JobKey jobKey = jobDetail.getKey();
            final org.quartz.Trigger quartzTrigger = getQuartzTrigger(trigger, jobKey.getName(), jobKey.getGroup());
            scheduler.scheduleJob(jobDetail, quartzTrigger);
            if (useOptimization) {
                transactionService.registerBonitaSynchronization(new NotifyQuartzOfNewTrigger(trigger.getStartDate().getTime(), quartzScheduler));
            }
        } catch (final Exception e) {
            throw new SSchedulerException(e);
        }
    }

    private final class NotifyQuartzOfNewTrigger implements BonitaTransactionSynchronization {

        private final long time;

        private final QuartzScheduler quartzScheduler;

        public NotifyQuartzOfNewTrigger(final long time, final QuartzScheduler quartzScheduler) {
            super();
            this.time = time;
            this.quartzScheduler = quartzScheduler;
        }

        @Override
        public void beforeCommit() {
            // NOTHING
        }

        @Override
        public void afterCompletion(final TransactionState txState) {
            if (TransactionState.COMMITTED.equals(txState)) {
                if (quartzScheduler != null) {
                    quartzScheduler.getSchedulerSignaler().signalSchedulingChange(time);
                }
            }
        }
    }

    private JobDetail getJobDetail(final long jobId, final long tenantId, final String jobName, final boolean disallowConcurrentExecution) {
        Class<? extends QuartzJob> jobClass = null;
        if (disallowConcurrentExecution) {
            jobClass = NonConcurrentQuartzJob.class;
        } else {
            jobClass = ConcurrentQuartzJob.class;
        }
        final JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName, String.valueOf(tenantId)).build();
        jobDetail.getJobDataMap().put("tenantId", String.valueOf(tenantId));
        jobDetail.getJobDataMap().put("jobId", String.valueOf(jobId));
        jobDetail.getJobDataMap().put("jobName", jobName);
        return jobDetail;
    }

    @Override
    public void executeNow(final long jobId, final long tenantId, final String jobName, final boolean disallowConcurrentExecution) throws SSchedulerException {
        try {
            checkSchedulerState();
            final JobDetail jobDetail = getJobDetail(jobId, tenantId, jobName, disallowConcurrentExecution);
            scheduler.addJob(jobDetail, true);
            scheduler.triggerJob(jobDetail.getKey());
        } catch (final Exception e) {
            throw new SSchedulerException(e);
        }
    }

    @Override
    public void executeAgain(final long jobId, final long tenantId, final String jobName, final boolean disallowConcurrentExecution) throws SSchedulerException {
        try {
            final JobDetail jobDetail2 = scheduler.getJobDetail(new JobKey(jobName, String.valueOf(tenantId)));
            final org.quartz.Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("OneShotTrigger" + UUID.randomUUID().getLeastSignificantBits(), String.valueOf(tenantId))
                    .forJob(jobName, String.valueOf(tenantId)).startNow().build();
            if (jobDetail2 == null) {
                final JobDetail jobDetail = getJobDetail(jobId, tenantId, jobName, disallowConcurrentExecution);
                scheduler.scheduleJob(jobDetail, trigger);
            } else {
                scheduler.scheduleJob(trigger);
            }
            if (useOptimization) {
                transactionService.registerBonitaSynchronization(new NotifyQuartzOfNewTrigger(System.currentTimeMillis(), quartzScheduler));
            }
        } catch (final Exception e) {
            throw new SSchedulerException(e);
        }
    }

    org.quartz.Trigger getQuartzTrigger(final Trigger trigger, final String jobName, final String tenantId) {
        final TriggerBuilder<? extends org.quartz.Trigger> triggerBuilder;
        final TriggerBuilder<org.quartz.Trigger> base = TriggerBuilder.newTrigger().forJob(jobName, tenantId).withIdentity(trigger.getName(), tenantId)
                .startNow();
        if (trigger instanceof CronTrigger) {
            final CronTrigger cronTrigger = (CronTrigger) trigger;
            final CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cronTrigger.getExpression());
            switch (cronTrigger.getMisfireHandlingPolicy()) {
                case NONE:
                    cronScheduleBuilder.withMisfireHandlingInstructionDoNothing();
                    break;
                case ALL:
                    cronScheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires();
                    break;
                case ONE:
                    cronScheduleBuilder.withMisfireHandlingInstructionFireAndProceed();
                    break;
                default:
                    throw new IllegalStateException();
            }
            triggerBuilder = base.withSchedule(cronScheduleBuilder).endAt(cronTrigger.getEndDate());
        } else if (trigger instanceof RepeatTrigger) {
            final RepeatTrigger repeatTrigger = (RepeatTrigger) trigger;
            final SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(repeatTrigger.getInterval())
                    .withRepeatCount(repeatTrigger.getCount()).withMisfireHandlingInstructionIgnoreMisfires();
            triggerBuilder = base.withSchedule(scheduleBuilder).startAt(repeatTrigger.getStartDate());
            switch (repeatTrigger.getMisfireHandlingPolicy()) {
                case NONE:
                    scheduleBuilder.withMisfireHandlingInstructionNextWithRemainingCount();
                    break;
                case ALL:
                    scheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires();
                    break;
                case ONE:
                    scheduleBuilder.withMisfireHandlingInstructionNowWithRemainingCount();
                    break;
                default:
                    throw new IllegalStateException();
            }
        } else {
            triggerBuilder = base.startAt(trigger.getStartDate());
        }
        return triggerBuilder.withPriority(trigger.getPriority()).build();
    }

    @Override
    public boolean isStarted() throws SSchedulerException {
        try {
            return scheduler != null && scheduler.isStarted();
        } catch (final SchedulerException e) {
            throw new SSchedulerException(e);
        }
    }

    @Override
    public boolean isShutdown() throws SSchedulerException {
        try {
            return scheduler != null && scheduler.isShutdown();
        } catch (final SchedulerException e) {
            throw new SSchedulerException(e);
        }
    }

    @Override
    public void start() throws SSchedulerException {
        try {
            if (!isShutdown()) {
                if (isStarted()) {
                    throw new SSchedulerException("The scheduler is already started.");
                }
                // shutdown();
            }
            scheduler = schedulerFactory.getScheduler();

            final ListenerManager listenerManager = scheduler.getListenerManager();
            for (final AbstractJobListener jobListener : jobListeners) {
                listenerManager.addJobListener(jobListener);
            }
            scheduler.start();

            try {
                if (useOptimization) {
                    final Field quartzSchedulerField = scheduler.getClass().getDeclaredField("sched");
                    quartzSchedulerField.setAccessible(true);
                    quartzScheduler = (QuartzScheduler) quartzSchedulerField.get(scheduler);
                }
            } catch (final Exception t) {
                // this is an optimization, we do not want it to make the system failing
                t.printStackTrace();
            }

        } catch (final SchedulerException e) {
            throw new SSchedulerException(e);
        }
    }

    @Override
    public void shutdown() throws SSchedulerException {
        try {
            checkSchedulerState();
            scheduler.shutdown(true);
        } catch (final SchedulerException e) {
            throw new SSchedulerException(e);
        }
    }

    private void checkSchedulerState() throws SSchedulerException {
        if (scheduler == null) {
            throw new SSchedulerException("The scheduler is not started");
        }
    }

    @Override
    public void reschedule(final String triggerName, final Trigger newTrigger) throws SSchedulerException {
        try {
            final String tenantId = String.valueOf(getTenantIdFromSession());
            if (triggerName == null) {
                throw new SSchedulerException("The trigger name is null");
            } else if (tenantId == null) {
                throw new SSchedulerException("The trigger group name is null");
            }
            checkSchedulerState();
            final org.quartz.Trigger quartzTrigger = getQuartzTrigger(newTrigger, triggerName, tenantId);
            final org.quartz.Trigger trigger = scheduler.getTrigger(triggerKey(triggerName, tenantId));
            if (trigger == null) {
                throw new SSchedulerException("No trigger found with name: " + triggerName + " and tenant: " + tenantId);
            }
            scheduler.rescheduleJob(trigger.getKey(), quartzTrigger);
        } catch (final Exception e) {
            throw new SSchedulerException(e);
        }
    }

    @Override
    public boolean delete(final String jobName) throws SSchedulerException {
        try {
            checkSchedulerState();
            final String tenantId = String.valueOf(getTenantIdFromSession());
            return scheduler.deleteJob(jobKey(jobName, tenantId));
        } catch (final SchedulerException e) {
            throw new SSchedulerException(e);
        } catch (final STenantIdNotSetException e) {
            throw new SSchedulerException(e);
        }
    }

    @Override
    public void deleteJobs() throws SSchedulerException {
        try {
            checkSchedulerState();
            final String tenantId = String.valueOf(getTenantIdFromSession());
            final Set<JobKey> jobNames = scheduler.getJobKeys(jobGroupEquals(tenantId));
            for (final JobKey jobKey : jobNames) {
                delete(jobKey.getName());
            }
        } catch (final SchedulerException e) {
            throw new SSchedulerException(e);
        } catch (final STenantIdNotSetException e) {
            throw new SSchedulerException(e);
        }
    }

    @Override
    public List<String> getJobs() throws SSchedulerException {
        try {
            checkSchedulerState();
            final String tenantId = String.valueOf(getTenantIdFromSession());
            final Set<JobKey> jobKeys = scheduler.getJobKeys(jobGroupEquals(tenantId));
            final List<String> jobsNames = new ArrayList<String>(jobKeys.size());
            for (final JobKey jobKey : jobKeys) {
                jobsNames.add(jobKey.getName());
            }
            return jobsNames;
        } catch (final SchedulerException e) {
            throw new SSchedulerException(e);
        } catch (final STenantIdNotSetException e) {
            throw new SSchedulerException(e);
        }
    }

    @Override
    public List<String> getAllJobs() throws SSchedulerException {
        try {
            checkSchedulerState();
            final Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupStartsWith(""));
            final List<String> jobsNames = new ArrayList<String>(jobKeys.size());
            for (final JobKey jobKey : jobKeys) {
                jobsNames.add(jobKey.getName());
            }
            return jobsNames;
        } catch (final SchedulerException e) {
            throw new SSchedulerException(e);
        }
    }

    @Override
    public void setBOSSchedulerService(final SchedulerServiceImpl schedulerService) {
        schedulerFactory.setBOSSchedulerService(schedulerService);
        for (final AbstractJobListener jobListener : jobListeners) {
            jobListener.setBOSSchedulerService(schedulerService);
        }
    }

    private long getTenantIdFromSession() throws STenantIdNotSetException {
        return sessionAccessor.getTenantId();
    }

    @Override
    public boolean isStillScheduled(final long tenantId, final String jobName) throws SSchedulerException {
        boolean stillScheduled = false;
        try {
            final List<? extends org.quartz.Trigger> triggers = scheduler.getTriggersOfJob(new JobKey(jobName, String.valueOf(tenantId)));
            final Iterator<? extends org.quartz.Trigger> iterator = triggers.iterator();
            while (!stillScheduled && iterator.hasNext()) {
                final org.quartz.Trigger trigger = iterator.next();
                if (trigger.getNextFireTime() != null) {
                    stillScheduled = true;
                }
            }
            return stillScheduled;
        } catch (final SchedulerException e) {
            throw new SSchedulerException(e);
        }
    }

    @Override
    public void rescheduleErroneousTriggers() throws SSchedulerException {
        checkSchedulerState();
        try {
            final List<String> triggerGroupNames = scheduler.getTriggerGroupNames();
            for (final String triggerGroupName : triggerGroupNames) {
                final Set<TriggerKey> triggerKeys = scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(triggerGroupName));
                for (final TriggerKey triggerKey : triggerKeys) {
                    final TriggerState triggerState = scheduler.getTriggerState(triggerKey);
                    if (TriggerState.ERROR.equals(triggerState)) {
                        scheduler.pauseTrigger(triggerKey);
                        scheduler.resumeTrigger(triggerKey);
                    }

                }
            }
        } catch (final SchedulerException e) {
            throw new SSchedulerException(e);
        }
    }

    @Override
    public void pauseJobs(final long tenantId) throws SSchedulerException {
        final GroupMatcher<TriggerKey> groupEquals = GroupMatcher.triggerGroupEquals(String.valueOf(tenantId));
        try {
            scheduler.pauseTriggers(groupEquals);
        } catch (final SchedulerException e) {
            throw new SSchedulerException("Unable to put jobs of tenant " + tenantId + " in pause", e);
        }
    }

    @Override
    public void resumeJobs(final long tenantId) throws SSchedulerException {
        final GroupMatcher<TriggerKey> groupEquals = GroupMatcher.triggerGroupEquals(String.valueOf(tenantId));
        try {
            scheduler.resumeTriggers(groupEquals);
        } catch (final SchedulerException e) {
            throw new SSchedulerException("Unable to put jobs of tenant " + tenantId + " in pause", e);
        }
    }

}
