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

import static org.quartz.JobKey.jobKey;
import static org.quartz.impl.matchers.GroupMatcher.jobGroupEquals;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.AbstractBonitaPlatformJobListener;
import org.bonitasoft.engine.scheduler.AbstractBonitaTenantJobListener;
import org.bonitasoft.engine.scheduler.SchedulerExecutor;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.scheduler.trigger.CronTrigger;
import org.bonitasoft.engine.scheduler.trigger.RepeatTrigger;
import org.bonitasoft.engine.scheduler.trigger.Trigger;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
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
 * @author Celine Souchet
 */
public class QuartzSchedulerExecutor implements SchedulerExecutor {

    private Scheduler scheduler;

    private final BonitaSchedulerFactory schedulerFactory;

    private final TransactionService transactionService;

    private final SessionAccessor sessionAccessor;

    private final TechnicalLoggerService logger;

    private final boolean useOptimization;

    private QuartzScheduler quartzScheduler;

    public QuartzSchedulerExecutor(final BonitaSchedulerFactory schedulerFactory, final TransactionService transactionService,
            final SessionAccessor sessionAccessor, final TechnicalLoggerService logger, final boolean useOptimization) {
        this.transactionService = transactionService;
        this.sessionAccessor = sessionAccessor;
        this.logger = logger;
        this.useOptimization = useOptimization;
        this.schedulerFactory = schedulerFactory;
    }

    @Override
    public void setBOSSchedulerService(final SchedulerServiceImpl schedulerService) {
        schedulerFactory.setBOSSchedulerService(schedulerService);
    }

    @Override
    public void schedule(final long jobId, final String groupName, final String jobName, final Trigger trigger, final boolean disallowConcurrentExecution)
            throws SSchedulerException {
        try {
            checkSchedulerState();
            final JobDetail jobDetail = getJobDetail(jobId, groupName, jobName, disallowConcurrentExecution);
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

    private JobDetail getJobDetail(final long jobId, final String groupName, final String jobName, final boolean disallowConcurrentExecution) {
        Class<? extends AbstractQuartzJob> jobClass = null;
        if (disallowConcurrentExecution) {
            jobClass = NonConcurrentQuartzJob.class;
        } else {
            jobClass = ConcurrentQuartzJob.class;
        }
        final JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName, groupName).build();
        jobDetail.getJobDataMap().put("tenantId", groupName);
        jobDetail.getJobDataMap().put("jobId", String.valueOf(jobId));
        jobDetail.getJobDataMap().put("jobName", jobName);
        return jobDetail;
    }

    @Override
    public void executeNow(final long jobId, final String groupName, final String jobName, final boolean disallowConcurrentExecution)
            throws SSchedulerException {
        try {
            checkSchedulerState();
            final JobDetail jobDetail = getJobDetail(jobId, groupName, jobName, disallowConcurrentExecution);
            scheduler.addJob(jobDetail, true);
            scheduler.triggerJob(jobDetail.getKey());
        } catch (final Exception e) {
            throw new SSchedulerException(e);
        }
    }

    @Override
    public void executeAgain(final long jobId, final String groupName, final String jobName, final boolean disallowConcurrentExecution)
            throws SSchedulerException {
        try {
            final JobDetail jobDetail2 = scheduler.getJobDetail(new JobKey(jobName, String.valueOf(groupName)));
            final org.quartz.Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("OneShotTrigger" + UUID.randomUUID().getLeastSignificantBits(), String.valueOf(groupName))
                    .forJob(jobName, String.valueOf(groupName)).startNow().build();
            if (jobDetail2 == null) {
                final JobDetail jobDetail = getJobDetail(jobId, groupName, jobName, disallowConcurrentExecution);
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
            return scheduler != null && scheduler.isStarted() && !scheduler.isShutdown();
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
            if(scheduler.isShutdown()){
                initializeScheduler();
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

    protected void checkSchedulerState() throws SSchedulerException {
        if (scheduler == null) {
            throw new SSchedulerException("The scheduler is not started");
        }
    }

    @Override
    public boolean delete(final String jobName, final String groupName) throws SSchedulerException {
        try {
            checkSchedulerState();
            final JobKey jobKey = jobKey(jobName, groupName);
            scheduler.pauseJob(jobKey);
            return scheduler.deleteJob(jobKey);
        } catch (final SchedulerException e) {
            throw new SSchedulerException(e);
        }
    }

    @Override
    public void deleteJobs(final String groupName) throws SSchedulerException {
        try {
            checkSchedulerState();
            final Set<JobKey> jobNames = scheduler.getJobKeys(jobGroupEquals(groupName));
            for (final JobKey jobKey : jobNames) {
                scheduler.pauseJob(jobKey);
                scheduler.deleteJob(jobKey);
            }
        } catch (final SchedulerException e) {
            throw new SSchedulerException(e);
        }
    }

    @Override
    public boolean isExistingJob(final String jobName, final String groupName) throws SSchedulerException {
        try {
            checkSchedulerState();
            final JobKey jobKey = jobKey(jobName, groupName);
            return scheduler.getJobDetail(jobKey) != null;
        } catch (final SchedulerException e) {
            throw new SSchedulerException(e);
        }
    }

    @Override
    public List<String> getJobs(final String groupName) throws SSchedulerException {
        try {
            checkSchedulerState();
            final Set<JobKey> jobKeys = scheduler.getJobKeys(jobGroupEquals(groupName));
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
    public boolean isStillScheduled(final String groupName, final String jobName) throws SSchedulerException {
        boolean stillScheduled = false;
        try {
            final List<? extends org.quartz.Trigger> triggers = scheduler.getTriggersOfJob(new JobKey(jobName, groupName));
            for (final org.quartz.Trigger trigger : triggers) {
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
                for (final TriggerKey triggerKey : scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(triggerGroupName))) {
                    if (TriggerState.ERROR.equals(scheduler.getTriggerState(triggerKey))) {
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
    public void pauseJobs(final String groupName) throws SSchedulerException {
        final GroupMatcher<TriggerKey> groupEquals = GroupMatcher.triggerGroupEquals(groupName);
        try {
            scheduler.pauseTriggers(groupEquals);
        } catch (final SchedulerException e) {
            throw new SSchedulerException("Unable to put jobs of tenant " + groupName + " in pause", e);
        }
    }

    @Override
    public void resumeJobs(final String groupName) throws SSchedulerException {
        final GroupMatcher<TriggerKey> groupEquals = GroupMatcher.triggerGroupEquals(groupName);
        try {
            scheduler.resumeTriggers(groupEquals);
        } catch (final SchedulerException e) {
            throw new SSchedulerException("Unable to put jobs of tenant " + groupName + " in pause", e);
        }
    }

    @Override
    public Date rescheduleJob(final String triggerName, final String groupName, final Date triggerStartTime) throws SSchedulerException {
        final TriggerKey triggerKey = new TriggerKey(triggerName, groupName);
        try {
            final org.quartz.Trigger oldTrigger = scheduler.getTrigger(triggerKey);
            final org.quartz.Trigger newTrigger = oldTrigger.getTriggerBuilder().startAt(triggerStartTime).build();
            return scheduler.rescheduleJob(triggerKey, newTrigger);
        } catch (final SchedulerException e) {
            throw new SSchedulerException("Can't get the trigger " + triggerKey, e);
        }
    }

    @Override
    public void addJobListener(final List<AbstractBonitaTenantJobListener> jobListeners, final String groupName) throws SSchedulerException {
        try {
            scheduler.getListenerManager().addJobListener(new TenantQuartzJobListener(jobListeners, groupName, sessionAccessor, transactionService, logger),
                    GroupMatcher.<JobKey> groupEquals(groupName));
        } catch (final SchedulerException e) {
            throw new SSchedulerException(e);
        }
    }

    @Override
    public void addJobListener(final List<AbstractBonitaPlatformJobListener> jobListeners) throws SSchedulerException {
        try {
            final ListenerManager listenerManager = scheduler.getListenerManager();
            listenerManager.addJobListener(new PlatformQuartzJobListener(jobListeners));
        } catch (final SchedulerException e) {
            throw new SSchedulerException(e);
        }
    }

    @Override
    public void initializeScheduler() throws SSchedulerException {
        try {
            scheduler = schedulerFactory.getScheduler();
        } catch (final SchedulerException e) {
            throw new SSchedulerException(e);
        }
    }
}
