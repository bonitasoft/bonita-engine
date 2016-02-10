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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.quartz.JobKey.jobKey;
import static org.quartz.impl.matchers.GroupMatcher.jobGroupEquals;
import static org.quartz.impl.matchers.GroupMatcher.jobGroupStartsWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.AbstractBonitaPlatformJobListener;
import org.bonitasoft.engine.scheduler.AbstractBonitaTenantJobListener;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.scheduler.trigger.OneShotTrigger;
import org.bonitasoft.engine.scheduler.trigger.RepeatTrigger;
import org.bonitasoft.engine.scheduler.trigger.Trigger;
import org.bonitasoft.engine.scheduler.trigger.Trigger.MisfireRestartPolicy;
import org.bonitasoft.engine.scheduler.trigger.UnixCronTrigger;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.ListenerManager;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;

@RunWith(MockitoJUnitRunner.class)
public class QuartzSchedulerExecutorTest {

    private static final String JOB_NAME = "jobName";

    private static final String GROUP_NAME = "groupName";

    private static final long JOB_ID = 1L;

    @Mock
    private BonitaSchedulerFactory schedulerFactory;

    @Mock
    private TransactionService transactionService;

    @Mock
    private SessionAccessor sessionAccessor;

    @Mock
    private TechnicalLoggerService logger;

    @Mock
    private SchedulerServiceImpl schedulerService;

    @Mock
    private Scheduler scheduler;

    @Mock
    private JobDetail jobDetail;

    @Mock
    private org.quartz.Trigger trigger;

    @Mock
    private GroupMatcher<TriggerKey> groupMatcher;

    private QuartzSchedulerExecutor quartzSchedulerExecutor;

    @Before
    public void before() throws Exception {
        when(schedulerFactory.getScheduler()).thenReturn(scheduler);
        when(scheduler.getListenerManager()).thenReturn(mock(ListenerManager.class));

        quartzSchedulerExecutor = initQuartzScheduler(false);
    }

    private QuartzSchedulerExecutor initQuartzScheduler(final boolean useOptimization) throws SSchedulerException {
        final QuartzSchedulerExecutor quartz = new QuartzSchedulerExecutor(schedulerFactory, transactionService, sessionAccessor, logger, useOptimization);
        quartz.initializeScheduler();
        quartz.start();
        return quartz;
    }

    @After
    public void after() throws Exception {
        quartzSchedulerExecutor.shutdown();
    }

    private Set<JobKey> newSet(final JobKey... jobKeys) {
        final HashSet<JobKey> set = new HashSet<JobKey>();
        set.addAll(asList(jobKeys));
        return set;
    }

    private class TestRepeatTrigger implements RepeatTrigger {

        private final MisfireRestartPolicy misfireRestartPolicy;

        private final String triggerName;

        private final int interval;

        private final int count;

        public TestRepeatTrigger(final String triggerName, final int interval, final int count, final MisfireRestartPolicy misfireRestartPolicy) {
            this.triggerName = triggerName;
            this.interval = interval;
            this.count = count;
            this.misfireRestartPolicy = misfireRestartPolicy;
        }

        public TestRepeatTrigger(final String triggerName, final int interval, final int count) {
            this.triggerName = triggerName;
            this.interval = interval;
            this.count = count;
            misfireRestartPolicy = MisfireRestartPolicy.ONE;
        }

        @Override
        public Date getStartDate() {
            return new Date();
        }

        @Override
        public int getPriority() {
            return -1;
        }

        @Override
        public String getName() {
            return triggerName;
        }

        @Override
        public MisfireRestartPolicy getMisfireHandlingPolicy() {
            return misfireRestartPolicy;
        }

        @Override
        public long getInterval() {
            return interval;
        }

        @Override
        public int getCount() {
            return count;
        }
    }

    @Test(expected = SSchedulerException.class)
    public void schedule_should_check_RepeatTrigger_with_bad_interval_param() throws Exception {
        final int badInterval = -1;
        final RepeatTrigger trigger = new TestRepeatTrigger("triggerName", badInterval, 1);

        quartzSchedulerExecutor.schedule(1l, "2", JOB_NAME, trigger, true);
    }

    @Test(expected = SSchedulerException.class)
    public void schedule_should_check_RepeatTrigger_with_bad_name_param() throws Exception {
        final String badTriggerName = null;
        final RepeatTrigger trigger = new TestRepeatTrigger(badTriggerName, 1, 1);

        quartzSchedulerExecutor.schedule(1l, "2", JOB_NAME, trigger, true);
    }

    @Test(expected = SSchedulerException.class)
    public void schedule_should_check_RepeatTrigger_with_bad_count_param() throws Exception {
        final int badCount = -2;
        final RepeatTrigger trigger = new TestRepeatTrigger("triggerName", 1, badCount);

        quartzSchedulerExecutor.schedule(1l, "2", JOB_NAME, trigger, true);
    }

    @Test
    public void pauseTriggers_should_pause_jobs_for_a_given_tenant() throws Exception {
        quartzSchedulerExecutor.pauseJobs("123");

        final GroupMatcher<TriggerKey> groupEquals = GroupMatcher.triggerGroupEquals(String.valueOf(123l));
        verify(scheduler, times(1)).pauseTriggers(groupEquals);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = SSchedulerException.class)
    public void pauseJobs_should_throw_exception_if_error_occurs() throws Exception {
        doThrow(SchedulerException.class).when(scheduler).pauseTriggers(any(GroupMatcher.class));

        quartzSchedulerExecutor.pauseJobs("123");
    }

    @Test
    public void resumeJobs_should_resume_jobs_for_a_given_tenant() throws Exception {
        quartzSchedulerExecutor.resumeJobs("123");

        final GroupMatcher<TriggerKey> groupEquals = GroupMatcher.triggerGroupEquals(String.valueOf(123l));
        verify(scheduler, times(1)).resumeTriggers(groupEquals);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = SSchedulerException.class)
    public void resumeJobs_should_throw_exception_if_error_occurs_when_resuming_jobs() throws Exception {
        doThrow(SchedulerException.class).when(scheduler).resumeTriggers(any(GroupMatcher.class));

        quartzSchedulerExecutor.resumeJobs("123");
    }

    @Test
    public void getQuartzTrigger_should_getQuartzTrigger_with_restart_ALL_have_a_ignore_misfire_policy() {
        // given
        final Date triggerEndTime = new Date(System.currentTimeMillis() + 10000);
        final UnixCronTrigger unixCronTrigger = new UnixCronTrigger("MyTrigger", triggerEndTime, "0/5 * * * * ??", MisfireRestartPolicy.ALL);

        // when
        final CronTrigger quartzTrigger = (CronTrigger) quartzSchedulerExecutor.getQuartzTrigger(unixCronTrigger, "MyJob", "12");

        // then
        assertEquals(CronTrigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY, quartzTrigger.getMisfireInstruction());
    }

    @Test
    public void getQuartzTrigger_should_getQuartzTrigger_with_restart_NONE_have_a_do_nothing_misfire_policy() {
        // given
        final Date triggerEndTime = new Date(System.currentTimeMillis() + 10000);
        final UnixCronTrigger unixCronTrigger = new UnixCronTrigger("MyTrigger", triggerEndTime, "0/5 * * * * ??", MisfireRestartPolicy.NONE);

        // when
        final CronTrigger quartzTrigger = (CronTrigger) quartzSchedulerExecutor.getQuartzTrigger(unixCronTrigger, "MyJob", "12");

        // then
        assertEquals(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING, quartzTrigger.getMisfireInstruction());
    }

    @Test
    public void getQuartzTrigger_should_getQuartzTrigger_with_restart_ONE_have_a_fire_once_misfire_policy() {
        // given
        final Date triggerEndTime = new Date(System.currentTimeMillis() + 10000);
        final UnixCronTrigger unixCronTrigger = new UnixCronTrigger("MyTrigger", triggerEndTime, "0/5 * * * * ??", MisfireRestartPolicy.ONE);

        // when
        final CronTrigger quartzTrigger = (CronTrigger) quartzSchedulerExecutor.getQuartzTrigger(unixCronTrigger, "MyJob", "12");

        // then
        assertEquals(CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW, quartzTrigger.getMisfireInstruction());
    }

    @Test
    public void delete_job_should_delete_job_and_interrup_for_a_given_name_and_group() throws Exception {
        // Given
        final JobKey jobKey = jobKey("aName", "aGroup");

        // When
        quartzSchedulerExecutor.delete(jobKey.getName(), jobKey.getGroup());

        // Then
        verify(scheduler).deleteJob(jobKey);
    }

    @Test(expected = SSchedulerException.class)
    public void delete_should_throw_exception_when_error_occurs_in_job_deletion() throws Exception {
        final JobKey job = jobKey("aName", "aGroup");
        when(scheduler.deleteJob(job)).thenThrow(new SchedulerException());

        quartzSchedulerExecutor.delete(job.getName(), job.getGroup());
    }

    @Test
    public void deleteJobs_should_delete_jobs_for_a_given_job_group() throws Exception {
        final String groupName = "aGroup";
        final JobKey toBeDeleted = jobKey("job1", groupName);
        final JobKey toBeDeletedAlso = jobKey("job2", groupName);
        final JobKey notToBeDeleted = jobKey("job1", "anotherGroup");
        when(scheduler.getJobKeys(jobGroupEquals(groupName))).thenReturn(newSet(toBeDeleted, toBeDeletedAlso));
        when(scheduler.getJobKeys(not(eq(jobGroupEquals(groupName))))).thenReturn(newSet(notToBeDeleted));

        quartzSchedulerExecutor.deleteJobs(groupName);

        verify(scheduler).deleteJob(toBeDeleted);
        verify(scheduler).deleteJob(toBeDeletedAlso);
        verify(scheduler, never()).deleteJob(notToBeDeleted);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = SSchedulerException.class)
    public void deleteJobs_should_throw_exception_when_error_occurs_in_jobs_deletion() throws Exception {
        doThrow(SchedulerException.class).when(scheduler).getJobKeys(any(GroupMatcher.class));

        quartzSchedulerExecutor.deleteJobs("aGroupName");
    }

    @Test
    public void getJobs_should_get_job_names_for_a_given_group_name() throws Exception {
        final String groupName = "aGroup";
        final JobKey toBeRetrieved = jobKey("job1", groupName);
        final JobKey toBeRetrievedAlso = jobKey("job2", groupName);
        final JobKey notToBeRetrieved = jobKey("job1", "anotherGroup");
        when(scheduler.getJobKeys(jobGroupEquals(groupName))).thenReturn(newSet(toBeRetrieved, toBeRetrievedAlso));
        when(scheduler.getJobKeys(not(eq(jobGroupEquals(groupName))))).thenReturn(newSet(notToBeRetrieved));

        final List<String> jobs = quartzSchedulerExecutor.getJobs(groupName);

        assertThat(jobs).containsOnly(toBeRetrieved.getName(), toBeRetrievedAlso.getName());
    }

    @Test(expected = SSchedulerException.class)
    public void getJobs_should_throw_exception_when_error_occurs_on_job_names_fetching() throws Exception {
        final String groupName = "aGroup";
        doThrow(SchedulerException.class).when(scheduler).getJobKeys(jobGroupEquals(groupName));

        quartzSchedulerExecutor.getJobs(groupName);
    }

    @Test
    public void getAllJobs_should_get_job_names_for_all_group_name() throws Exception {
        final JobKey job1 = jobKey("job1", "aGroup");
        final JobKey job2 = jobKey("job2", "aGroup");
        final JobKey job3 = jobKey("job3", "anotherGroup");
        doReturn(newSet(job1, job2, job3)).when(scheduler).getJobKeys(jobGroupStartsWith(""));

        final List<String> jobs = quartzSchedulerExecutor.getAllJobs();

        assertThat(jobs).containsOnly(job1.getName(), job2.getName(), job3.getName());
    }

    @SuppressWarnings("unchecked")
    @Test(expected = SSchedulerException.class)
    public void getAllJobs_should_throw_exception_when_error_occurs_on_all_job_names_fetching() throws Exception {
        doThrow(SchedulerException.class).when(scheduler).getJobKeys(any(GroupMatcher.class));

        quartzSchedulerExecutor.getAllJobs();
    }

    @Test(expected = SSchedulerException.class)
    public void start_should_not_start_twice() throws Exception {
        // given
        doReturn(true).when(scheduler).isStarted();

        // when
        quartzSchedulerExecutor.start();
    }

    @Test
    public void schedule_disallowConcurrentExecution() throws Exception {
        scheduleJob(quartzSchedulerExecutor, true, 0);
    }

    @Test
    public void schedule_allowConcurrentExecution() throws Exception {
        scheduleJob(quartzSchedulerExecutor, false, 0);
    }

    @Test
    public void schedule_with_optimisation() throws Exception {
        final QuartzSchedulerExecutor executor = quartzSchedulerExecutor = initQuartzScheduler(true);

        scheduleJob(executor, true, 1);
    }

    private void scheduleJob(final QuartzSchedulerExecutor executor, final boolean disallowConcurrentExecution, final int expectedOptimizationCall)
            throws Exception {
        // when
        executor.schedule(1l, "2", JOB_NAME, new OneShotTrigger("oneShot", new Date(System.currentTimeMillis())), disallowConcurrentExecution);

        // then
        verify(scheduler).scheduleJob(any(JobDetail.class), any(org.quartz.Trigger.class));
        verify(transactionService, times(expectedOptimizationCall)).registerBonitaSynchronization(any(BonitaTransactionSynchronization.class));
    }

    @Test(expected = SSchedulerException.class)
    public void schedule_with_exception() throws Exception {
        // given
        doThrow(SchedulerException.class).when(scheduler).scheduleJob(any(JobDetail.class), any(org.quartz.Trigger.class));

        // when
        quartzSchedulerExecutor.schedule(1l, "2", JOB_NAME, new OneShotTrigger("oneShot", new Date(System.currentTimeMillis())), true);

        // then exception

    }

    @Test
    public void executeNow_should_schedule_job() throws Exception {
        quartzSchedulerExecutor.executeNow(1L, "2", JOB_NAME, false);

        verify(scheduler, times(1)).addJob(any(JobDetail.class), anyBoolean());
        verify(scheduler, times(1)).triggerJob(any(JobKey.class));

    }

    @Test(expected = SSchedulerException.class)
    public void executeNow_with_exception() throws Exception {
        doThrow(SchedulerException.class).when(scheduler).triggerJob(any(JobKey.class));

        quartzSchedulerExecutor.executeNow(1l, "2", JOB_NAME, false);
    }

    @Test
    public void schedule_should_use_tenant_id_as_group_in_job_details() throws Exception {
        final String tenantId = "3";
        final Trigger trigger = new TestRepeatTrigger("trigger", 1, 1, MisfireRestartPolicy.NONE);

        quartzSchedulerExecutor.schedule(10L, tenantId, "myJob", trigger, true);

        final ArgumentCaptor<JobDetail> jobDetailCaptor = ArgumentCaptor.forClass(JobDetail.class);
        verify(scheduler, times(1)).scheduleJob(jobDetailCaptor.capture(), any(org.quartz.Trigger.class));
        final String group = jobDetailCaptor.getValue().getKey().getGroup();
        assertThat(group).isEqualTo(String.valueOf(tenantId));
    }

    @Test
    public void schedule_should_store_tenant_id_in_jobDataMap() throws Exception {
        final String tenantId = "3";
        final Trigger trigger = new TestRepeatTrigger("trigger", 1, 1, MisfireRestartPolicy.NONE);

        quartzSchedulerExecutor.schedule(10L, tenantId, "myJob", trigger, true);

        final ArgumentCaptor<JobDetail> jobDetailCaptor = ArgumentCaptor.forClass(JobDetail.class);
        verify(scheduler, times(1)).scheduleJob(jobDetailCaptor.capture(), any(org.quartz.Trigger.class));
        final JobDataMap dataMap = jobDetailCaptor.getValue().getJobDataMap();
        assertThat(dataMap.get("tenantId")).isEqualTo(tenantId);
    }

    @Test
    public void executeNow_should_store_tenant_id_in_jobDataMap() throws Exception {
        final String tenantId = "3";

        quartzSchedulerExecutor.executeNow(10L, tenantId, "myJob", true);

        final ArgumentCaptor<JobDetail> jobDetailCaptor = ArgumentCaptor.forClass(JobDetail.class);
        verify(scheduler, times(1)).addJob(jobDetailCaptor.capture(), anyBoolean());
        final JobDataMap dataMap = jobDetailCaptor.getValue().getJobDataMap();
        assertThat(dataMap.get("tenantId")).isEqualTo(tenantId);
        verify(scheduler, times(1)).triggerJob(any(JobKey.class));
    }

    @Test
    public void executeNow_should_use_tenant_id_as_group_in_job_details() throws Exception {
        final String tenantId = "3";

        quartzSchedulerExecutor.executeNow(10L, tenantId, "myJob", true);

        final ArgumentCaptor<JobDetail> jobDetailCaptor = ArgumentCaptor.forClass(JobDetail.class);
        verify(scheduler, times(1)).addJob(jobDetailCaptor.capture(), anyBoolean());
        final String group = jobDetailCaptor.getValue().getKey().getGroup();
        assertThat(group).isEqualTo(String.valueOf(tenantId));
        verify(scheduler, times(1)).triggerJob(any(JobKey.class));
    }

    @Test(expected = SSchedulerException.class)
    public void executeAgain_should_throw_exception() throws Exception {
        // given
        doThrow(Exception.class).when(scheduler).getJobDetail(any(JobKey.class));

        // when
        quartzSchedulerExecutor.executeAgain(JOB_ID, GROUP_NAME, JOB_NAME, true);

        // then exception
    }

    @Test
    public void executeAgain_should_schedule_job_with_job_detail() throws Exception {
        // given
        doReturn(null).when(scheduler).getJobDetail(any(JobKey.class));

        // when
        quartzSchedulerExecutor.executeAgain(JOB_ID, GROUP_NAME, JOB_NAME, true);

        // then
        verify(scheduler, times(0)).scheduleJob(any(org.quartz.Trigger.class));
        verify(scheduler, times(1)).scheduleJob(any(JobDetail.class), any(org.quartz.Trigger.class));
    }

    @Test
    public void executeAgain_should_schedule_job_without_job_detail() throws Exception {
        // given
        doReturn(jobDetail).when(scheduler).getJobDetail(any(JobKey.class));

        // when
        quartzSchedulerExecutor.executeAgain(JOB_ID, GROUP_NAME, JOB_NAME, true);

        // then
        verify(scheduler, times(1)).scheduleJob(any(org.quartz.Trigger.class));
        verify(scheduler, times(0)).scheduleJob(any(JobDetail.class), any(org.quartz.Trigger.class));
    }

    @Test
    public void executeAgain_using_optimization() throws Exception {
        // given
        quartzSchedulerExecutor = initQuartzScheduler(true);
        doReturn(jobDetail).when(scheduler).getJobDetail(any(JobKey.class));

        // when
        quartzSchedulerExecutor.executeAgain(JOB_ID, GROUP_NAME, JOB_NAME, true);

        // then
        verify(transactionService, times(1)).registerBonitaSynchronization(any(BonitaTransactionSynchronization.class));
    }

    @Test(expected = SSchedulerException.class)
    public void is_still_scheduled_should_throw_exception() throws Exception {
        // given
        doThrow(SchedulerException.class).when(scheduler).getTriggersOfJob(any(JobKey.class));

        // when
        quartzSchedulerExecutor.isStillScheduled(GROUP_NAME, JOB_NAME);

        // then exception
    }

    @Test
    public void is_still_scheduled_should_be_false_when_no_more_fire_time() throws Exception {
        check_is_still_scheduled_response(null, false);
    }

    @Test
    public void is_still_scheduled_should_be_true_when_have_fire_time() throws Exception {
        check_is_still_scheduled_response(new Date(), true);
    }

    private void check_is_still_scheduled_response(final Date nextFireTime, final boolean expectedResponse) throws SchedulerException, SSchedulerException {
        // given
        doReturn(nextFireTime).when(trigger).getNextFireTime();
        final ArrayList<org.quartz.Trigger> triggerList = new ArrayList<org.quartz.Trigger>();
        triggerList.add(trigger);
        doReturn(triggerList).when(scheduler).getTriggersOfJob(any(JobKey.class));

        // when
        final boolean stillScheduled = quartzSchedulerExecutor.isStillScheduled(GROUP_NAME, JOB_NAME);

        // then exception
        assertThat(stillScheduled).as("should return true").isEqualTo(expectedResponse);
    }

    @Test
    public void is_still_scheduled_should_be_false() throws Exception {
        // given
        doReturn(new ArrayList<org.quartz.Trigger>()).when(scheduler).getTriggersOfJob(any(JobKey.class));

        // when
        final boolean stillScheduled = quartzSchedulerExecutor.isStillScheduled(GROUP_NAME, JOB_NAME);

        // then exception
        assertThat(stillScheduled).as("should return false").isFalse();
    }

    @Test(expected = SSchedulerException.class)
    public void rescheduleErroneousTriggers_should_throw_exception() throws Exception {
        // given
        doThrow(SchedulerException.class).when(scheduler).getTriggerGroupNames();

        // when
        quartzSchedulerExecutor.rescheduleErroneousTriggers();

        // then exception

    }

    @Test
    public void rescheduleErroneousTriggers_should_pause_and_resume_trigger() throws Exception {
        check_reschedule(TriggerState.ERROR, 1);

    }

    @Test
    public void rescheduleErroneousTriggers_should_not_reschedule() throws Exception {
        check_reschedule(TriggerState.COMPLETE, 0);

    }

    private void check_reschedule(final TriggerState triggerState, final int expectedNumberOfInvocations) throws SchedulerException, SSchedulerException {
        final List<String> groupNames = new ArrayList<String>();
        final Set<TriggerKey> triggerKeys = new HashSet<TriggerKey>();
        groupNames.add(GROUP_NAME);
        final TriggerKey triggerKey = new TriggerKey("name");
        triggerKeys.add(triggerKey);
        final GroupMatcher<TriggerKey> triggerGroupEquals = GroupMatcher.triggerGroupEquals(GROUP_NAME);

        // given
        doReturn(triggerState).when(scheduler).getTriggerState(triggerKey);
        doReturn(groupNames).when(scheduler).getTriggerGroupNames();
        doReturn(triggerKeys).when(scheduler).getTriggerKeys(triggerGroupEquals);

        // when
        quartzSchedulerExecutor.rescheduleErroneousTriggers();

        // then
        verify(scheduler, times(expectedNumberOfInvocations)).pauseTrigger(any(TriggerKey.class));
        verify(scheduler, times(expectedNumberOfInvocations)).resumeTrigger(any(TriggerKey.class));
    }

    @Test(expected = SSchedulerException.class)
    public void is_started_should_throw_exception() throws Exception {
        // given
        doThrow(SchedulerException.class).when(scheduler).isStarted();

        // when
        quartzSchedulerExecutor.isStarted();

        // then exception
    }

    @Test
    public void is_started_should_be_true_when_scheduler_is_stated() throws Exception {
        checkIsStarted(true, true, false);

    }

    @Test
    public void is_started_should_be_false_when_scheduler_is_shutdown() throws Exception {
        checkIsStarted(false, false, true);

    }

    @Test
    public void is_started_should_be_false_when_scheduler_is_in_transitionnal_state() throws Exception {
        checkIsStarted(false, true, true);

    }

    private void checkIsStarted(final boolean expectedResponse, final boolean schedulerStartStatus, final boolean schedulerSchutdownStatus)
            throws SchedulerException, SSchedulerException {
        // given
        doReturn(schedulerStartStatus).when(scheduler).isStarted();
        doReturn(schedulerSchutdownStatus).when(scheduler).isShutdown();

        // when
        final boolean started = quartzSchedulerExecutor.isStarted();

        // then exception
        assertThat(started).isEqualTo(expectedResponse);
    }

    @Test(expected = SSchedulerException.class)
    public void is_shutdown_should_throw_exception() throws Exception {
        // given
        doThrow(SchedulerException.class).when(scheduler).isShutdown();

        // when
        quartzSchedulerExecutor.isShutdown();

        // then exception

    }

    @Test
    public void is_shutdown_should_be_true() throws Exception {
        checkIsSchutdown(true);

    }

    @Test
    public void is_shutdown_should_be_false() throws Exception {
        checkIsSchutdown(true);

    }

    private void checkIsSchutdown(final boolean expectedResponse) throws SchedulerException, SSchedulerException {
        // given
        doReturn(expectedResponse).when(scheduler).isShutdown();

        // when
        final boolean started = quartzSchedulerExecutor.isShutdown();

        // then exception
        assertThat(started).isEqualTo(expectedResponse);
    }

    @Test(expected = SSchedulerException.class)
    public void rescheduleJob_should_throw_exception_when_rescheduleJob_failed() throws Exception {
        // Given
        final org.quartz.Trigger trigger = mock(org.quartz.Trigger.class);
        doReturn(TriggerBuilder.newTrigger()).when(trigger).getTriggerBuilder();
        doReturn(trigger).when(scheduler).getTrigger(any(TriggerKey.class));
        doThrow(SchedulerException.class).when(scheduler).rescheduleJob(any(TriggerKey.class), any(org.quartz.Trigger.class));

        // When
        quartzSchedulerExecutor.rescheduleJob("triggerName", "groupName", new Date());
    }

    @Test
    public void rescheduleJob_should_rescheduleJob() throws Exception {
        // Given
        final org.quartz.Trigger trigger = mock(org.quartz.Trigger.class);
        doReturn(TriggerBuilder.newTrigger()).when(trigger).getTriggerBuilder();
        doReturn(trigger).when(scheduler).getTrigger(any(TriggerKey.class));

        // When
        quartzSchedulerExecutor.rescheduleJob("triggerName", "groupName", new Date());

        // Then
        verify(scheduler).rescheduleJob(any(TriggerKey.class), any(org.quartz.Trigger.class));
    }

    @Test(expected = SSchedulerException.class)
    public void addJobListener_should_throw_exception_when_addJobListener_failed_for_tenant() throws Exception {
        // Given
        final List<AbstractBonitaTenantJobListener> jobListeners = Collections.emptyList();
        final String groupName = "groupName";
        final ListenerManager listenerManager = mock(ListenerManager.class);
        doReturn(listenerManager).when(scheduler).getListenerManager();
        doThrow(SchedulerException.class).when(listenerManager).addJobListener(any(JobListener.class), eq(GroupMatcher.<JobKey> groupEquals(groupName)));

        // When
        quartzSchedulerExecutor.addJobListener(jobListeners, groupName);
    }

    @Test
    public void addJobListener_should_addJobListener_for_tenant() throws Exception {
        // Given
        final List<AbstractBonitaTenantJobListener> jobListeners = Collections.emptyList();
        final String groupName = "groupName";
        final ListenerManager listenerManager = mock(ListenerManager.class);
        doReturn(listenerManager).when(scheduler).getListenerManager();

        // When
        quartzSchedulerExecutor.addJobListener(jobListeners, groupName);

        // Then
        verify(listenerManager).addJobListener(any(JobListener.class), eq(GroupMatcher.<JobKey> groupEquals(groupName)));
    }

    @Test(expected = SSchedulerException.class)
    public void addJobListener_should_throw_exception_when_addJobListener_failed_for_platform() throws Exception {
        // Given
        final List<AbstractBonitaPlatformJobListener> jobListeners = Collections.emptyList();
        final ListenerManager listenerManager = mock(ListenerManager.class);
        doReturn(listenerManager).when(scheduler).getListenerManager();
        doThrow(SchedulerException.class).when(listenerManager).addJobListener(any(JobListener.class));

        // When
        quartzSchedulerExecutor.addJobListener(jobListeners);
    }

    @Test
    public void addJobListener_should_addJobListener_for_platform() throws Exception {
        // Given
        final List<AbstractBonitaPlatformJobListener> jobListeners = Collections.emptyList();
        final ListenerManager listenerManager = mock(ListenerManager.class);
        doReturn(listenerManager).when(scheduler).getListenerManager();

        // When
        quartzSchedulerExecutor.addJobListener(jobListeners);

        // Then
        verify(listenerManager).addJobListener(any(JobListener.class));
    }

    @Test(expected = SSchedulerException.class)
    public void initializeScheduler_should_throw_exception_when_getJobListener_failed() throws Exception {
        // Given
        doThrow(SchedulerException.class).when(schedulerFactory).getScheduler();

        // When
        quartzSchedulerExecutor.initializeScheduler();
    }

    @Test(expected = SSchedulerException.class)
    public void isExistingJob_should_throw_exception_when_getJobDetail_failed() throws Exception {
        // Given
        doThrow(SchedulerException.class).when(scheduler).getJobDetail(any(JobKey.class));

        // When
        quartzSchedulerExecutor.isExistingJob("name", "group");
    }

    @Test(expected = SSchedulerException.class)
    public void isExistingJob_should_throw_exception_when_scheduler_is_null() throws Exception {
        // Given
        quartzSchedulerExecutor.shutdown();
        when(schedulerFactory.getScheduler()).thenReturn(null);
        quartzSchedulerExecutor.initializeScheduler();

        try {
            // When
            quartzSchedulerExecutor.isExistingJob("name", "group");
        } finally {
            when(schedulerFactory.getScheduler()).thenReturn(scheduler);
            quartzSchedulerExecutor.initializeScheduler();
            quartzSchedulerExecutor.start();
        }
    }

    @Test
    public void isExistingJob_should_return_false_if_doesnt_exist_in_quartz() throws Exception {
        // Given
        doReturn(null).when(scheduler).getJobDetail(any(JobKey.class));

        // When
        final boolean existingJob = quartzSchedulerExecutor.isExistingJob("name", "group");

        // Then
        assertFalse(existingJob);
    }

    @Test
    public void isExistingJob_should_return_true_if_exists_in_quartz() throws Exception {
        // Given
        doReturn(mock(JobDetail.class)).when(scheduler).getJobDetail(any(JobKey.class));

        // When
        final boolean existingJob = quartzSchedulerExecutor.isExistingJob("name", "group");

        // Then
        assertTrue(existingJob);
    }

    @Test(expected = SSchedulerException.class)
    public void shutdown_should_throw_exception_when_scheduler_failed() throws Exception {
        // Given
        doThrow(SchedulerException.class).when(scheduler).shutdown(true);

        try {
            // When
            quartzSchedulerExecutor.shutdown();
        } finally {
            doNothing().when(scheduler).shutdown(true);
        }
    }

}
