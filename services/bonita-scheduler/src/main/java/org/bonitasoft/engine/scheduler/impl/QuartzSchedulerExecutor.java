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

import static org.quartz.JobKey.jobKey;
import static org.quartz.impl.matchers.GroupMatcher.jobGroupEquals;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.transaction.Status;

import org.bonitasoft.engine.commons.ExceptionUtils;
import org.bonitasoft.engine.scheduler.BonitaJobListener;
import org.bonitasoft.engine.scheduler.SchedulerExecutor;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.scheduler.trigger.CronTrigger;
import org.bonitasoft.engine.scheduler.trigger.Trigger;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.TransactionService;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.ListenerManager;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.core.QuartzScheduler;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthieu Chaffotte
 * @author Yanyan Liu
 * @author Celine Souchet
 */
public class QuartzSchedulerExecutor implements SchedulerExecutor {

    private Logger logger = LoggerFactory.getLogger(QuartzSchedulerExecutor.class);
    private Scheduler scheduler;

    private final BonitaSchedulerFactory schedulerFactory;

    private final TransactionService transactionService;

    private final SessionAccessor sessionAccessor;

    private final boolean useOptimization;

    private QuartzScheduler quartzScheduler;

    private List<BonitaJobListener> jobListeners = new ArrayList<>();

    public QuartzSchedulerExecutor(final BonitaSchedulerFactory schedulerFactory,
            final TransactionService transactionService,
            final SessionAccessor sessionAccessor, final boolean useOptimization) {
        this.transactionService = transactionService;
        this.sessionAccessor = sessionAccessor;
        this.useOptimization = useOptimization;
        this.schedulerFactory = schedulerFactory;
    }

    // autowired
    public void setJobListeners(List<BonitaJobListener> jobListeners) {
        this.jobListeners = jobListeners;
    }

    @Override
    public void setBOSSchedulerService(final SchedulerServiceImpl schedulerService) {
        schedulerFactory.setBOSSchedulerService(schedulerService);
    }

    @Override
    public void schedule(final long jobId, final String groupName, final String jobName, final Trigger trigger,
            final boolean disallowConcurrentExecution)
            throws SSchedulerException {
        try {
            checkSchedulerState();
            final JobDetail jobDetail = createJobDetails(jobId, groupName, jobName, disallowConcurrentExecution);
            final JobKey jobKey = jobDetail.getKey();
            final org.quartz.Trigger quartzTrigger = getQuartzTrigger(trigger, jobKey.getName(), jobKey.getGroup());
            scheduler.scheduleJob(jobDetail, quartzTrigger);
            if (useOptimization) {
                transactionService.registerBonitaSynchronization(
                        new NotifyQuartzOfNewTrigger(trigger.getStartDate().getTime(), quartzScheduler));
            }
        } catch (final Exception e) {
            throw new SSchedulerException(e);
        }
    }

    private static final class NotifyQuartzOfNewTrigger implements BonitaTransactionSynchronization {

        private final long time;

        private final QuartzScheduler quartzScheduler;

        public NotifyQuartzOfNewTrigger(final long time, final QuartzScheduler quartzScheduler) {
            super();
            this.time = time;
            this.quartzScheduler = quartzScheduler;
        }

        @Override
        public void afterCompletion(final int txState) {
            if (Status.STATUS_COMMITTED == txState) {
                if (quartzScheduler != null) {
                    quartzScheduler.getSchedulerSignaler().signalSchedulingChange(time);
                }
            }
        }
    }

    private JobDetail createJobDetails(final long jobId, final String groupName, final String jobName,
            final boolean disallowConcurrentExecution) {
        Class<? extends AbstractQuartzJob> jobClass;
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
    public void executeAgain(final long jobId, final String groupName, final String jobName,
            final boolean disallowConcurrentExecution, int delayInMillis)
            throws SSchedulerException {
        checkSchedulerState();
        try {
            JobDetail jobDetail = scheduler.getJobDetail(new JobKey(jobName, String.valueOf(groupName)));
            if (jobDetail == null) {
                logger.debug(
                        "Re-execute job {} named {} of group {}, there was no quartz job and no triggers left (one shot triggered that failed and was deleted)",
                        jobId, jobName, groupName);
                // The quartz job itself was deleted because the trigger that failed was the only one and was a one shot trigger
                scheduler.scheduleJob(createJobDetails(jobId, groupName, jobName, disallowConcurrentExecution),
                        createOneShotTrigger(groupName, jobName, delayInMillis));
            } else {
                List<? extends org.quartz.Trigger> triggersOfJob = scheduler.getTriggersOfJob(jobDetail.getKey());
                // We retrieve the first trigger that will not fire again
                Optional<? extends org.quartz.Trigger> firstTriggerThatWillNotFire = triggersOfJob.stream()
                        .filter(t -> !t.mayFireAgain()).findFirst();
                if (firstTriggerThatWillNotFire.isPresent()) {
                    // if there is one, we reschedule the job by replacing it
                    logger.debug(
                            "Re-execute job {} named {} of group {}, reuse existing trigger {} because it will not fire again."
                                    +
                                    "(most likely a one shot trigger that failed and was not correctly deleted)",
                            jobId, jobName, groupName, firstTriggerThatWillNotFire.get());
                    scheduler.rescheduleJob(firstTriggerThatWillNotFire.get().getKey(),
                            createOneShotTrigger(groupName, jobName, delayInMillis));
                } else {
                    // in the other case we create a new trigger to schedule the job (it means other triggers are likely cron triggers)
                    logger.debug("Re-execute job {} named {} of group {}, create a new trigger for that. " +
                            "(The job that failed was most likely triggered by a cron trigger)", jobId, jobName,
                            groupName);
                    scheduler.scheduleJob(createOneShotTrigger(groupName, jobName, delayInMillis));
                }
            }
            if (useOptimization) {
                transactionService.registerBonitaSynchronization(
                        new NotifyQuartzOfNewTrigger(System.currentTimeMillis(), quartzScheduler));
            }
        } catch (final Exception e) {
            throw new SSchedulerException(e);
        }
    }

    private org.quartz.Trigger createOneShotTrigger(String groupName, String jobName, int delayInMillis) {
        return TriggerBuilder.newTrigger()
                .withIdentity("OneShotTrigger" + UUID.randomUUID().getLeastSignificantBits(), String.valueOf(groupName))
                .forJob(jobName, String.valueOf(groupName))
                .startAt(new Date(Instant.now().plusMillis(delayInMillis).toEpochMilli())).build();
    }

    org.quartz.Trigger getQuartzTrigger(final Trigger trigger, final String jobName, final String tenantId) {
        final TriggerBuilder<? extends org.quartz.Trigger> triggerBuilder;
        final TriggerBuilder<org.quartz.Trigger> base = TriggerBuilder.newTrigger().forJob(jobName, tenantId)
                .withIdentity(trigger.getName(), tenantId)
                .startNow();
        if (trigger instanceof CronTrigger) {
            final CronTrigger cronTrigger = (CronTrigger) trigger;
            final CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder
                    .cronSchedule(cronTrigger.getExpression());
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
            if (isStarted()) {
                throw new SSchedulerException("The scheduler is already started.");
            }
            if (scheduler == null || scheduler.isShutdown()) {
                try {
                    scheduler = schedulerFactory.getScheduler();
                } catch (final SchedulerException e) {
                    throw new SSchedulerException(e);
                }
            }

            scheduler.start();
            addListeners();
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
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown(true);
            }
        } catch (final SchedulerException e) {
            throw new SSchedulerException(e);
        }
    }

    private void checkSchedulerState() throws SSchedulerException {
        try {
            if (scheduler == null || scheduler.isShutdown()) {
                throw new SSchedulerException("The scheduler is not started");
            }
            if (!transactionService.isTransactionActive()) {
                throw new SSchedulerException("The scheduler cannot be used without opening a transaction first");
            }
        } catch (SchedulerException e) {
            throw new SSchedulerException("The scheduler is not started", e);
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
    public boolean mayFireAgain(final String groupName, final String jobName) throws SSchedulerException {
        try {
            checkSchedulerState();
            List<? extends org.quartz.Trigger> triggersOfJob = scheduler
                    .getTriggersOfJob(new JobKey(jobName, groupName));
            return triggersOfJob.stream()
                    .anyMatch(org.quartz.Trigger::mayFireAgain);
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
                for (final TriggerKey triggerKey : scheduler
                        .getTriggerKeys(GroupMatcher.triggerGroupEquals(triggerGroupName))) {
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
        checkSchedulerState();
        final GroupMatcher<TriggerKey> groupEquals = GroupMatcher.triggerGroupEquals(groupName);
        try {
            scheduler.pauseTriggers(groupEquals);
        } catch (final SchedulerException e) {
            throw new SSchedulerException("Unable to put jobs of tenant " + groupName + " in pause", e);
        }
    }

    @Override
    public void resumeJobs(final String groupName) throws SSchedulerException {
        checkSchedulerState();
        final GroupMatcher<TriggerKey> groupEquals = GroupMatcher.triggerGroupEquals(groupName);
        try {
            scheduler.resumeTriggers(groupEquals);
        } catch (final SchedulerException e) {
            throw new SSchedulerException("Unable to resume jobs of tenant " + groupName, e);
        }
    }

    @Override
    public Date rescheduleJob(final String triggerName, final String groupName, final Date triggerStartTime)
            throws SSchedulerException {
        checkSchedulerState();
        final TriggerKey triggerKey = new TriggerKey(triggerName, groupName);
        try {
            final org.quartz.Trigger oldTrigger = scheduler.getTrigger(triggerKey);
            final org.quartz.Trigger newTrigger = oldTrigger.getTriggerBuilder().startAt(triggerStartTime).build();
            Date date = scheduler.rescheduleJob(triggerKey, newTrigger);
            if (useOptimization) {
                try {
                    transactionService.registerBonitaSynchronization(
                            new NotifyQuartzOfNewTrigger(triggerStartTime.getTime(), quartzScheduler));
                } catch (STransactionNotFoundException e) {
                    logger.error("Unable to register synchronization to optimize Quartz rescheduling, "
                            + ExceptionUtils.printLightWeightStacktrace(e));
                }
            }
            return date;
        } catch (final SchedulerException e) {
            throw new SSchedulerException("Can't get the trigger " + triggerKey, e);
        }
    }

    // For tests only
    public Set<String> getPausedTriggerGroups() throws SSchedulerException {
        try {
            return quartzScheduler.getPausedTriggerGroups();
        } catch (SchedulerException e) {
            throw new SSchedulerException(e);
        }
    }

    private void addListeners() throws SSchedulerException {
        try {
            final ListenerManager listenerManager = scheduler.getListenerManager();
            listenerManager.addJobListener(new QuartzJobListener(jobListeners, sessionAccessor));
        } catch (final SchedulerException e) {
            throw new SSchedulerException(e);
        }
    }

}
