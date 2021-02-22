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

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
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

import org.bonitasoft.engine.log.technical.TechnicalLoggerSLF4JImpl;
import org.bonitasoft.engine.scheduler.BonitaJobListener;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.scheduler.trigger.OneShotTrigger;
import org.bonitasoft.engine.scheduler.trigger.Trigger;
import org.bonitasoft.engine.scheduler.trigger.Trigger.MisfireRestartPolicy;
import org.bonitasoft.engine.scheduler.trigger.UnixCronTrigger;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.ListenerManager;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.SimpleTriggerImpl;

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
    private Scheduler scheduler;
    @Mock
    private ListenerManager listenerManager;

    private JobDetail jobDetail = JobBuilder.newJob(ConcurrentQuartzJob.class).withIdentity(JOB_NAME, GROUP_NAME)
            .build();

    @Mock
    private org.quartz.Trigger trigger1;
    @Mock
    private org.quartz.Trigger trigger2;

    private QuartzSchedulerExecutor quartzSchedulerExecutor;

    @Before
    public void before() throws Exception {
        when(schedulerFactory.getScheduler()).thenReturn(scheduler);
        when(scheduler.getListenerManager()).thenReturn(listenerManager);
        when(transactionService.isTransactionActive()).thenReturn(true);
        quartzSchedulerExecutor = initQuartzScheduler(false);
    }

    private QuartzSchedulerExecutor initQuartzScheduler(final boolean useOptimization) throws SSchedulerException {
        final QuartzSchedulerExecutor quartz = new QuartzSchedulerExecutor(schedulerFactory, transactionService,
                sessionAccessor, new TechnicalLoggerSLF4JImpl(), useOptimization);
        quartz.start();
        return quartz;
    }

    private Set<JobKey> newSet(final JobKey... jobKeys) {
        final HashSet<JobKey> set = new HashSet<>();
        set.addAll(asList(jobKeys));
        return set;
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
        final UnixCronTrigger unixCronTrigger = new UnixCronTrigger("MyTrigger", triggerEndTime, "0/5 * * * * ??",
                MisfireRestartPolicy.ALL);

        // when
        final CronTrigger quartzTrigger = (CronTrigger) quartzSchedulerExecutor.getQuartzTrigger(unixCronTrigger,
                "MyJob", "12");

        // then
        assertEquals(CronTrigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY, quartzTrigger.getMisfireInstruction());
    }

    @Test
    public void getQuartzTrigger_should_getQuartzTrigger_with_restart_NONE_have_a_do_nothing_misfire_policy() {
        // given
        final Date triggerEndTime = new Date(System.currentTimeMillis() + 10000);
        final UnixCronTrigger unixCronTrigger = new UnixCronTrigger("MyTrigger", triggerEndTime, "0/5 * * * * ??",
                MisfireRestartPolicy.NONE);

        // when
        final CronTrigger quartzTrigger = (CronTrigger) quartzSchedulerExecutor.getQuartzTrigger(unixCronTrigger,
                "MyJob", "12");

        // then
        assertEquals(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING, quartzTrigger.getMisfireInstruction());
    }

    @Test
    public void getQuartzTrigger_should_getQuartzTrigger_with_restart_ONE_have_a_fire_once_misfire_policy() {
        // given
        final Date triggerEndTime = new Date(System.currentTimeMillis() + 10000);
        final UnixCronTrigger unixCronTrigger = new UnixCronTrigger("MyTrigger", triggerEndTime, "0/5 * * * * ??",
                MisfireRestartPolicy.ONE);

        // when
        final CronTrigger quartzTrigger = (CronTrigger) quartzSchedulerExecutor.getQuartzTrigger(unixCronTrigger,
                "MyJob", "12");

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
        when(scheduler.getJobKeys(jobGroupEquals(groupName))).thenReturn(newSet(toBeRetrieved, toBeRetrievedAlso));

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

    private void scheduleJob(final QuartzSchedulerExecutor executor, final boolean disallowConcurrentExecution,
            final int expectedOptimizationCall)
            throws Exception {
        // when
        executor.schedule(1l, "2", JOB_NAME, new OneShotTrigger("oneShot", new Date(System.currentTimeMillis())),
                disallowConcurrentExecution);

        // then
        verify(scheduler).scheduleJob(any(JobDetail.class), any(org.quartz.Trigger.class));
        verify(transactionService, times(expectedOptimizationCall))
                .registerBonitaSynchronization(any(BonitaTransactionSynchronization.class));
    }

    @Test(expected = SSchedulerException.class)
    public void schedule_with_exception() throws Exception {
        // given
        doThrow(SchedulerException.class).when(scheduler).scheduleJob(any(JobDetail.class),
                any(org.quartz.Trigger.class));

        // when
        quartzSchedulerExecutor.schedule(1l, "2", JOB_NAME,
                new OneShotTrigger("oneShot", new Date(System.currentTimeMillis())), true);

        // then exception

    }

    @Test
    public void schedule_should_use_tenant_id_as_group_in_job_details() throws Exception {
        final String tenantId = "3";
        final Trigger trigger = new OneShotTrigger("trigger", new Date(), 1, MisfireRestartPolicy.NONE);

        quartzSchedulerExecutor.schedule(10L, tenantId, "myJob", trigger, true);

        final ArgumentCaptor<JobDetail> jobDetailCaptor = ArgumentCaptor.forClass(JobDetail.class);
        verify(scheduler, times(1)).scheduleJob(jobDetailCaptor.capture(), any(org.quartz.Trigger.class));
        final String group = jobDetailCaptor.getValue().getKey().getGroup();
        assertThat(group).isEqualTo(String.valueOf(tenantId));
    }

    @Test
    public void schedule_should_store_tenant_id_in_jobDataMap() throws Exception {
        final String tenantId = "3";
        final Trigger trigger = new OneShotTrigger("trigger", new Date(), 1, MisfireRestartPolicy.NONE);

        quartzSchedulerExecutor.schedule(10L, tenantId, "myJob", trigger, true);

        final ArgumentCaptor<JobDetail> jobDetailCaptor = ArgumentCaptor.forClass(JobDetail.class);
        verify(scheduler, times(1)).scheduleJob(jobDetailCaptor.capture(), any(org.quartz.Trigger.class));
        final JobDataMap dataMap = jobDetailCaptor.getValue().getJobDataMap();
        assertThat(dataMap.get("tenantId")).isEqualTo(tenantId);
    }

    @Test
    public void executeAgain_should_schedule_job_when_no_more_job_is_registered() throws Exception {
        // given
        doReturn(null).when(scheduler).getJobDetail(any(JobKey.class));

        // when
        quartzSchedulerExecutor.executeAgain(JOB_ID, GROUP_NAME, JOB_NAME, true, 5000);

        // then: create a trigger and a job details
        verify(scheduler, times(0)).scheduleJob(any(org.quartz.Trigger.class));
        verify(scheduler, times(1)).scheduleJob(any(JobDetail.class), any(org.quartz.Trigger.class));
        verify(scheduler, times(0)).rescheduleJob(any(org.quartz.TriggerKey.class), any(org.quartz.Trigger.class));
    }

    @Test
    public void executeAgain_should_schedule_job_when_job_do_not_have_a_trigger_anymore() throws Exception {
        // given
        doReturn(jobDetail).when(scheduler).getJobDetail(any(JobKey.class));
        doReturn(Collections.emptyList()).when(scheduler).getTriggersOfJob(any(JobKey.class));

        // when
        quartzSchedulerExecutor.executeAgain(JOB_ID, GROUP_NAME, JOB_NAME, true, 5000);

        // then: create a new trigger to execute it
        verify(scheduler, times(1)).scheduleJob(any(org.quartz.Trigger.class));
        verify(scheduler, times(0)).scheduleJob(any(JobDetail.class), any(org.quartz.Trigger.class));
        verify(scheduler, times(0)).rescheduleJob(any(org.quartz.TriggerKey.class), any(org.quartz.Trigger.class));
    }

    @Test
    public void executeAgain_should_schedule_job_when_job_have_no_trigger_that_may_not_fire_again() throws Exception {
        // given
        doReturn(jobDetail).when(scheduler).getJobDetail(any(JobKey.class));
        doReturn(asList(triggerThatMayFireAgain(), triggerThatMayFireAgain())).when(scheduler)
                .getTriggersOfJob(any(JobKey.class));

        // when
        quartzSchedulerExecutor.executeAgain(JOB_ID, GROUP_NAME, JOB_NAME, true, 5000);

        // then: create a new trigger to execute it
        verify(scheduler, times(1)).scheduleJob(any(org.quartz.Trigger.class));
        verify(scheduler, times(0)).scheduleJob(any(JobDetail.class), any(org.quartz.Trigger.class));
        verify(scheduler, times(0)).rescheduleJob(any(org.quartz.TriggerKey.class), any(org.quartz.Trigger.class));
    }

    private SimpleTriggerImpl triggerThatMayFireAgain() {
        SimpleTriggerImpl simpleTrigger = (SimpleTriggerImpl) TriggerBuilder.newTrigger()
                .withIdentity(JOB_NAME, GROUP_NAME).build();
        simpleTrigger.setNextFireTime(new Date());
        return simpleTrigger;
    }

    @Test
    public void executeAgain_should_reschedule_job_when_job_have_at_least_one_trigger_may_not_fire_again()
            throws Exception {
        // given
        doReturn(jobDetail).when(scheduler).getJobDetail(any(JobKey.class));
        doReturn(asList(triggerThatMayFireAgain(), triggerThatMayNotFireAgain())).when(scheduler)
                .getTriggersOfJob(any(JobKey.class));

        // when
        quartzSchedulerExecutor.executeAgain(JOB_ID, GROUP_NAME, JOB_NAME, true, 5000);

        // then: create a new trigger to execute it
        verify(scheduler, times(0)).scheduleJob(any(org.quartz.Trigger.class));
        verify(scheduler, times(0)).scheduleJob(any(JobDetail.class), any(org.quartz.Trigger.class));
        verify(scheduler, times(1)).rescheduleJob(any(org.quartz.TriggerKey.class), any(org.quartz.Trigger.class));
    }

    private org.quartz.Trigger triggerThatMayNotFireAgain() {
        return TriggerBuilder.newTrigger().withIdentity(JOB_NAME, GROUP_NAME).build();
    }

    @Test
    public void executeAgain_should_schedule_job_when_job_have_a_trigger_that_may_not_fire_again() throws Exception {
        // given
        doReturn(jobDetail).when(scheduler).getJobDetail(any(JobKey.class));
        doReturn(singletonList(triggerThatMayNotFireAgain())).when(scheduler).getTriggersOfJob(any(JobKey.class));

        // when
        quartzSchedulerExecutor.executeAgain(JOB_ID, GROUP_NAME, JOB_NAME, true, 5000);

        // then: update the trigger ( reschedule )
        verify(scheduler, times(0)).scheduleJob(any(org.quartz.Trigger.class));
        verify(scheduler, times(0)).scheduleJob(any(JobDetail.class), any(org.quartz.Trigger.class));
        verify(scheduler, times(1)).rescheduleJob(any(org.quartz.TriggerKey.class), any(org.quartz.Trigger.class));
    }

    @Test
    public void executeAgain_should_schedule_job_when_job_have_a_trigger_that_may_fire_again() throws Exception {
        // given
        doReturn(jobDetail).when(scheduler).getJobDetail(any(JobKey.class));
        doReturn(singletonList(triggerThatMayFireAgain()))
                .when(scheduler).getTriggersOfJob(any(JobKey.class));

        // when
        quartzSchedulerExecutor.executeAgain(JOB_ID, GROUP_NAME, JOB_NAME, true, 5000);

        // then: create a new trigger to execute it
        verify(scheduler, times(1)).scheduleJob(any(org.quartz.Trigger.class));
        verify(scheduler, times(0)).scheduleJob(any(JobDetail.class), any(org.quartz.Trigger.class));
        verify(scheduler, times(0)).rescheduleJob(any(org.quartz.TriggerKey.class), any(org.quartz.Trigger.class));
    }

    @Test
    public void executeAgain_using_optimization() throws Exception {
        // given
        quartzSchedulerExecutor = initQuartzScheduler(true);
        doReturn(jobDetail).when(scheduler).getJobDetail(any(JobKey.class));

        // when
        quartzSchedulerExecutor.executeAgain(JOB_ID, GROUP_NAME, JOB_NAME, true, 5000);

        // then
        verify(transactionService, times(1)).registerBonitaSynchronization(any(BonitaTransactionSynchronization.class));
    }

    @Test
    public void mayFireAgain_should_return_true_when_some_trigger_may_fire_again() throws Exception {
        when(trigger1.mayFireAgain()).thenReturn(false);
        when(trigger2.mayFireAgain()).thenReturn(true);
        doReturn(asList(trigger1, trigger2)).when(scheduler).getTriggersOfJob(any(JobKey.class));

        boolean mayFireAgain = quartzSchedulerExecutor.mayFireAgain(GROUP_NAME, JOB_NAME);

        assertThat(mayFireAgain).isTrue();
    }

    @Test
    public void mayFireAgain_should_return_false_when_no_trigger_may_fire_again() throws Exception {
        when(trigger1.mayFireAgain()).thenReturn(false);
        when(trigger2.mayFireAgain()).thenReturn(false);
        doReturn(asList(trigger1, trigger2)).when(scheduler).getTriggersOfJob(any(JobKey.class));

        boolean mayFireAgain = quartzSchedulerExecutor.mayFireAgain(GROUP_NAME, JOB_NAME);

        assertThat(mayFireAgain).isFalse();
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

    private void check_reschedule(final TriggerState triggerState, final int expectedNumberOfInvocations)
            throws SchedulerException, SSchedulerException {
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

    private void checkIsStarted(final boolean expectedResponse, final boolean schedulerStartStatus,
            final boolean schedulerSchutdownStatus)
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
        doReturn(true).when(scheduler).isShutdown();

        final boolean started = quartzSchedulerExecutor.isShutdown();

        assertThat(started).isEqualTo(true);

    }

    @Test
    public void is_shutdown_should_be_false() throws Exception {
        doReturn(false).when(scheduler).isShutdown();

        boolean started = quartzSchedulerExecutor.isShutdown();

        assertThat(started).isEqualTo(false);

    }

    @Test(expected = SSchedulerException.class)
    public void rescheduleJob_should_throw_exception_when_rescheduleJob_failed() throws Exception {
        // Given
        final org.quartz.Trigger trigger = mock(org.quartz.Trigger.class);
        doReturn(TriggerBuilder.newTrigger()).when(trigger).getTriggerBuilder();
        doReturn(trigger).when(scheduler).getTrigger(any(TriggerKey.class));
        doThrow(SchedulerException.class).when(scheduler).rescheduleJob(any(TriggerKey.class),
                any(org.quartz.Trigger.class));

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
    public void isExistingJob_should_throw_exception_when_getJobDetail_failed() throws Exception {
        // Given
        doThrow(SchedulerException.class).when(scheduler).getJobDetail(any(JobKey.class));

        // When
        quartzSchedulerExecutor.isExistingJob("name", "group");
    }

    @Test
    public void isExistingJob_should_throw_exception_when_scheduler_is_null() throws Exception {
        // Given
        quartzSchedulerExecutor.shutdown();

        try {
            // When
            quartzSchedulerExecutor.isExistingJob("name", "group");
        } finally {
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

        quartzSchedulerExecutor.shutdown();
    }

    @Test
    public void should_register_listeners_on_start() throws Exception {
        BonitaJobListener listener1 = mock(BonitaJobListener.class);
        BonitaJobListener listener2 = mock(BonitaJobListener.class);
        quartzSchedulerExecutor = new QuartzSchedulerExecutor(schedulerFactory, transactionService, sessionAccessor,
                new TechnicalLoggerSLF4JImpl(), false);
        quartzSchedulerExecutor.setJobListeners(asList(listener1, listener2));

        quartzSchedulerExecutor.start();

        verify(listenerManager).addJobListener(argThat((quartzListener) -> ((QuartzJobListener) quartzListener)
                .getBonitaJobListeners().containsAll(asList(listener1, listener2))));
    }

}
