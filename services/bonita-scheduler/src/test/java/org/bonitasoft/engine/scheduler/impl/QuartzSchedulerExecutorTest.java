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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.quartz.JobKey.jobKey;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.scheduler.BonitaJobListener;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.scheduler.trigger.OneShotTrigger;
import org.bonitasoft.engine.scheduler.trigger.Trigger;
import org.bonitasoft.engine.scheduler.trigger.Trigger.MisfireRestartPolicy;
import org.bonitasoft.engine.scheduler.trigger.UnixCronTrigger;
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

    private static final long JOB_ID = 1L;

    @Mock
    private BonitaSchedulerFactory schedulerFactory;

    @Mock
    private TransactionService transactionService;

    @Mock
    private Scheduler scheduler;
    @Mock
    private ListenerManager listenerManager;

    private final JobDetail jobDetail = JobBuilder.newJob(ConcurrentQuartzJob.class).withIdentity(JOB_NAME).build();

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
                useOptimization);
        quartz.start();
        return quartz;
    }

    private Set<JobKey> newSet(final JobKey... jobKeys) {
        final HashSet<JobKey> set = new HashSet<>();
        set.addAll(asList(jobKeys));
        return set;
    }

    @Test
    public void pauseTriggers_should_pause_jobs() throws Exception {
        quartzSchedulerExecutor.pauseJobs();

        verify(scheduler).pauseTriggers(GroupMatcher.anyTriggerGroup());
    }

    @SuppressWarnings("unchecked")
    @Test(expected = SSchedulerException.class)
    public void pauseJobs_should_throw_exception_if_error_occurs() throws Exception {
        doThrow(SchedulerException.class).when(scheduler).pauseTriggers(any(GroupMatcher.class));

        quartzSchedulerExecutor.pauseJobs();
    }

    @Test
    public void resumeJobs_should_resume_jobs() throws Exception {
        quartzSchedulerExecutor.resumeJobs();

        verify(scheduler).resumeTriggers(GroupMatcher.anyTriggerGroup());
    }

    @SuppressWarnings("unchecked")
    @Test(expected = SSchedulerException.class)
    public void resumeJobs_should_throw_exception_if_error_occurs_when_resuming_jobs() throws Exception {
        doThrow(SchedulerException.class).when(scheduler).resumeTriggers(any(GroupMatcher.class));

        quartzSchedulerExecutor.resumeJobs();
    }

    @Test
    public void getQuartzTrigger_should_getQuartzTrigger_with_restart_ALL_have_a_ignore_misfire_policy() {
        // given
        final Date triggerEndTime = new Date(System.currentTimeMillis() + 10000);
        final UnixCronTrigger unixCronTrigger = new UnixCronTrigger("MyTrigger", triggerEndTime, "0/5 * * * * ??",
                MisfireRestartPolicy.ALL);

        // when
        final CronTrigger quartzTrigger = (CronTrigger) quartzSchedulerExecutor.getQuartzTrigger(unixCronTrigger,
                "MyJob");

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
                "MyJob");

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
                "MyJob");

        // then
        assertEquals(CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW, quartzTrigger.getMisfireInstruction());
    }

    @Test
    public void delete_job_should_pause_and_delete_job_for_a_given_name() throws Exception {
        // Given
        final JobKey jobKey = jobKey("aName");

        // When
        quartzSchedulerExecutor.delete(jobKey.getName());

        // Then
        verify(scheduler).pauseJob(jobKey);
        verify(scheduler).deleteJob(jobKey);
    }

    @Test
    public void delete_should_throw_exception_when_error_occurs_in_job_deletion() throws Exception {
        final JobKey job = jobKey("aName");
        when(scheduler.deleteJob(job)).thenThrow(new SchedulerException());

        assertThatExceptionOfType(SSchedulerException.class)
                .isThrownBy(() -> quartzSchedulerExecutor.delete(job.getName()))
                .withCauseInstanceOf(SchedulerException.class);
    }

    @Test
    public void deleteJobs_should_delete_jobs() throws Exception {
        final JobKey toBeDeleted = jobKey("job1");
        final JobKey toBeDeletedAlso = jobKey("job2");
        when(scheduler.getJobKeys(GroupMatcher.anyJobGroup())).thenReturn(newSet(toBeDeleted, toBeDeletedAlso));

        quartzSchedulerExecutor.deleteJobs();

        verify(scheduler).deleteJob(toBeDeleted);
        verify(scheduler).deleteJob(toBeDeletedAlso);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = SSchedulerException.class)
    public void deleteJobs_should_throw_exception_when_error_occurs_in_jobs_deletion() throws Exception {
        doThrow(SchedulerException.class).when(scheduler).getJobKeys(any(GroupMatcher.class));

        quartzSchedulerExecutor.deleteJobs();
    }

    @Test
    public void getJobs_should_get_job_names() throws Exception {
        final JobKey toBeRetrieved = jobKey("job1");
        final JobKey toBeRetrievedAlso = jobKey("job2");
        when(scheduler.getJobKeys(GroupMatcher.anyJobGroup())).thenReturn(newSet(toBeRetrieved, toBeRetrievedAlso));

        final List<String> jobs = quartzSchedulerExecutor.getJobs();

        assertThat(jobs).containsOnly(toBeRetrieved.getName(), toBeRetrievedAlso.getName());
    }

    @Test
    public void getJobs_should_throw_exception_when_error_occurs_on_job_names_fetching() throws Exception {
        doThrow(SchedulerException.class).when(scheduler).getJobKeys(GroupMatcher.anyJobGroup());

        assertThatExceptionOfType(SSchedulerException.class)
                .isThrownBy(() -> quartzSchedulerExecutor.getJobs())
                .withCauseInstanceOf(SchedulerException.class);
    }

    @Test
    public void getJobs_should_get_job_names_for_all_group_name() throws Exception {
        final JobKey job1 = jobKey("job1", Scheduler.DEFAULT_GROUP);
        final JobKey job2 = jobKey("job2", Scheduler.DEFAULT_GROUP);
        final JobKey job3 = jobKey("job3", "anotherGroup");
        doReturn(newSet(job1, job2, job3)).when(scheduler).getJobKeys(GroupMatcher.anyJobGroup());

        final List<String> jobs = quartzSchedulerExecutor.getJobs();

        assertThat(jobs).containsOnly(job1.getName(), job2.getName(), job3.getName());
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
        executor.schedule(1L, JOB_NAME, new OneShotTrigger("oneShot", new Date(System.currentTimeMillis())),
                disallowConcurrentExecution);

        // then
        verify(scheduler).scheduleJob(any(JobDetail.class), any(org.quartz.Trigger.class));
        verify(transactionService, times(expectedOptimizationCall))
                .registerBonitaSynchronization(any(BonitaTransactionSynchronization.class));
    }

    @Test
    public void schedule_with_exception() throws Exception {
        // given
        doThrow(SchedulerException.class).when(scheduler).scheduleJob(any(JobDetail.class),
                any(org.quartz.Trigger.class));

        // when and then exception
        assertThatExceptionOfType(SSchedulerException.class)
                .isThrownBy(() -> quartzSchedulerExecutor.schedule(1L, JOB_NAME,
                        new OneShotTrigger("oneShot", new Date(System.currentTimeMillis())), true))
                .withCauseInstanceOf(SchedulerException.class);
    }

    @Test
    public void schedule_should_use_default_group_in_job_details() throws Exception {
        final Trigger trigger = new OneShotTrigger("trigger", new Date(), 1, MisfireRestartPolicy.NONE);

        quartzSchedulerExecutor.schedule(10L, "myJob", trigger, true);

        final ArgumentCaptor<JobDetail> jobDetailCaptor = ArgumentCaptor.forClass(JobDetail.class);
        verify(scheduler).scheduleJob(jobDetailCaptor.capture(), any(org.quartz.Trigger.class));
        final String group = jobDetailCaptor.getValue().getKey().getGroup();
        assertThat(group).isEqualTo(Scheduler.DEFAULT_GROUP);
    }

    @Test
    public void executeAgain_should_schedule_job_when_no_more_job_is_registered() throws Exception {
        // given
        doReturn(null).when(scheduler).getJobDetail(any(JobKey.class));

        // when
        quartzSchedulerExecutor.executeAgain(JOB_ID, JOB_NAME, true, 5000);

        // then: create a trigger and a job details
        verify(scheduler, never()).scheduleJob(any(org.quartz.Trigger.class));
        verify(scheduler).scheduleJob(any(JobDetail.class), any(org.quartz.Trigger.class));
        verify(scheduler, never()).rescheduleJob(any(org.quartz.TriggerKey.class), any(org.quartz.Trigger.class));
    }

    @Test
    public void executeAgain_should_schedule_job_when_job_do_not_have_a_trigger_anymore() throws Exception {
        // given
        doReturn(jobDetail).when(scheduler).getJobDetail(any(JobKey.class));
        doReturn(Collections.emptyList()).when(scheduler).getTriggersOfJob(any(JobKey.class));

        // when
        quartzSchedulerExecutor.executeAgain(JOB_ID, JOB_NAME, true, 5000);

        // then: create a new trigger to execute it
        verify(scheduler).scheduleJob(any(org.quartz.Trigger.class));
        verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(org.quartz.Trigger.class));
        verify(scheduler, never()).rescheduleJob(any(org.quartz.TriggerKey.class), any(org.quartz.Trigger.class));
    }

    @Test
    public void executeAgain_should_schedule_job_when_job_have_no_trigger_that_may_not_fire_again() throws Exception {
        // given
        doReturn(jobDetail).when(scheduler).getJobDetail(any(JobKey.class));
        doReturn(asList(triggerThatMayFireAgain(), triggerThatMayFireAgain())).when(scheduler)
                .getTriggersOfJob(any(JobKey.class));

        // when
        quartzSchedulerExecutor.executeAgain(JOB_ID, JOB_NAME, true, 5000);

        // then: create a new trigger to execute it
        verify(scheduler).scheduleJob(any(org.quartz.Trigger.class));
        verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(org.quartz.Trigger.class));
        verify(scheduler, never()).rescheduleJob(any(org.quartz.TriggerKey.class), any(org.quartz.Trigger.class));
    }

    private SimpleTriggerImpl triggerThatMayFireAgain() {
        SimpleTriggerImpl simpleTrigger = (SimpleTriggerImpl) TriggerBuilder.newTrigger()
                .withIdentity(JOB_NAME).build();
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
        quartzSchedulerExecutor.executeAgain(JOB_ID, JOB_NAME, true, 5000);

        // then: create a new trigger to execute it
        verify(scheduler, never()).scheduleJob(any(org.quartz.Trigger.class));
        verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(org.quartz.Trigger.class));
        verify(scheduler).rescheduleJob(any(org.quartz.TriggerKey.class), any(org.quartz.Trigger.class));
    }

    private org.quartz.Trigger triggerThatMayNotFireAgain() {
        return TriggerBuilder.newTrigger().withIdentity(JOB_NAME).build();
    }

    @Test
    public void executeAgain_should_schedule_job_when_job_have_a_trigger_that_may_not_fire_again() throws Exception {
        // given
        doReturn(jobDetail).when(scheduler).getJobDetail(any(JobKey.class));
        doReturn(singletonList(triggerThatMayNotFireAgain())).when(scheduler).getTriggersOfJob(any(JobKey.class));

        // when
        quartzSchedulerExecutor.executeAgain(JOB_ID, JOB_NAME, true, 5000);

        // then: update the trigger ( reschedule )
        verify(scheduler, never()).scheduleJob(any(org.quartz.Trigger.class));
        verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(org.quartz.Trigger.class));
        verify(scheduler).rescheduleJob(any(org.quartz.TriggerKey.class), any(org.quartz.Trigger.class));
    }

    @Test
    public void executeAgain_should_schedule_job_when_job_have_a_trigger_that_may_fire_again() throws Exception {
        // given
        doReturn(jobDetail).when(scheduler).getJobDetail(any(JobKey.class));
        doReturn(singletonList(triggerThatMayFireAgain()))
                .when(scheduler).getTriggersOfJob(any(JobKey.class));

        // when
        quartzSchedulerExecutor.executeAgain(JOB_ID, JOB_NAME, true, 5000);

        // then: create a new trigger to execute it
        verify(scheduler).scheduleJob(any(org.quartz.Trigger.class));
        verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(org.quartz.Trigger.class));
        verify(scheduler, never()).rescheduleJob(any(org.quartz.TriggerKey.class), any(org.quartz.Trigger.class));
    }

    @Test
    public void executeAgain_using_optimization() throws Exception {
        // given
        quartzSchedulerExecutor = initQuartzScheduler(true);
        doReturn(jobDetail).when(scheduler).getJobDetail(any(JobKey.class));

        // when
        quartzSchedulerExecutor.executeAgain(JOB_ID, JOB_NAME, true, 5000);

        // then
        verify(transactionService).registerBonitaSynchronization(any(BonitaTransactionSynchronization.class));
    }

    @Test
    public void mayFireAgain_should_return_true_when_some_trigger_may_fire_again() throws Exception {
        when(trigger1.mayFireAgain()).thenReturn(false);
        when(trigger2.mayFireAgain()).thenReturn(true);
        doReturn(asList(trigger1, trigger2)).when(scheduler).getTriggersOfJob(any(JobKey.class));

        boolean mayFireAgain = quartzSchedulerExecutor.mayFireAgain(JOB_NAME);

        assertThat(mayFireAgain).isTrue();
    }

    @Test
    public void mayFireAgain_should_return_false_when_no_trigger_may_fire_again() throws Exception {
        when(trigger1.mayFireAgain()).thenReturn(false);
        when(trigger2.mayFireAgain()).thenReturn(false);
        doReturn(asList(trigger1, trigger2)).when(scheduler).getTriggersOfJob(any(JobKey.class));

        boolean mayFireAgain = quartzSchedulerExecutor.mayFireAgain(JOB_NAME);

        assertThat(mayFireAgain).isFalse();
    }

    @Test
    public void rescheduleErroneousTriggers_should_throw_exception() throws Exception {
        // given
        doThrow(SchedulerException.class).when(scheduler).getTriggerKeys(GroupMatcher.anyTriggerGroup());

        // when and then exception
        assertThatExceptionOfType(SSchedulerException.class)
                .isThrownBy(() -> quartzSchedulerExecutor.rescheduleErroneousTriggers())
                .withCauseInstanceOf(SchedulerException.class);
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
        final Set<TriggerKey> triggerKeys = new HashSet<>();
        final TriggerKey triggerKey = new TriggerKey("name");
        triggerKeys.add(triggerKey);

        // given
        doReturn(triggerState).when(scheduler).getTriggerState(triggerKey);
        doReturn(triggerKeys).when(scheduler).getTriggerKeys(GroupMatcher.anyTriggerGroup());

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

    @Test
    public void is_shutdown_should_throw_exception() throws Exception {
        // given
        doThrow(SchedulerException.class).when(scheduler).isShutdown();

        // when and then exception
        assertThatExceptionOfType(SSchedulerException.class)
                .isThrownBy(() -> quartzSchedulerExecutor.isShutdown())
                .withCauseInstanceOf(SchedulerException.class);
    }

    @Test
    public void is_shutdown_should_be_true() throws Exception {
        doReturn(true).when(scheduler).isShutdown();

        final boolean started = quartzSchedulerExecutor.isShutdown();

        assertThat(started).isTrue();
    }

    @Test
    public void is_shutdown_should_be_false() throws Exception {
        doReturn(false).when(scheduler).isShutdown();

        boolean started = quartzSchedulerExecutor.isShutdown();

        assertThat(started).isFalse();
    }

    @Test
    public void rescheduleJob_should_throw_exception_when_rescheduleJob_failed() throws Exception {
        // Given
        final org.quartz.Trigger trigger = mock(org.quartz.Trigger.class);
        doReturn(TriggerBuilder.newTrigger()).when(trigger).getTriggerBuilder();
        doReturn(trigger).when(scheduler).getTrigger(any(TriggerKey.class));
        doThrow(SchedulerException.class).when(scheduler).rescheduleJob(any(TriggerKey.class),
                any(org.quartz.Trigger.class));

        // When
        assertThatExceptionOfType(SSchedulerException.class)
                .isThrownBy(() -> quartzSchedulerExecutor.rescheduleJob("triggerName", new Date()))
                .withCauseInstanceOf(SchedulerException.class);
    }

    @Test
    public void rescheduleJob_should_rescheduleJob() throws Exception {
        // Given
        final org.quartz.Trigger trigger = mock(org.quartz.Trigger.class);
        doReturn(TriggerBuilder.newTrigger()).when(trigger).getTriggerBuilder();
        doReturn(trigger).when(scheduler).getTrigger(any(TriggerKey.class));

        // When
        quartzSchedulerExecutor.rescheduleJob("triggerName", new Date());

        // Then
        verify(scheduler).rescheduleJob(any(TriggerKey.class), any(org.quartz.Trigger.class));
    }

    @Test
    public void isExistingJob_should_throw_exception_when_getJobDetail_failed() throws Exception {
        // Given
        doThrow(SchedulerException.class).when(scheduler).getJobDetail(any(JobKey.class));

        // When
        assertThatExceptionOfType(SSchedulerException.class)
                .isThrownBy(() -> quartzSchedulerExecutor.isExistingJob("name"))
                .withCauseInstanceOf(SchedulerException.class);
    }

    @Test
    public void isExistingJob_should_throw_exception_when_scheduler_is_shutdown() throws Exception {
        // Given
        doReturn(true).when(scheduler).isShutdown();

        // When
        assertThatExceptionOfType(SSchedulerException.class)
                .isThrownBy(() -> quartzSchedulerExecutor.isExistingJob("name"))
                .withMessage("The scheduler is not started");
    }

    @Test
    public void isExistingJob_should_return_false_if_doesnt_exist_in_quartz() throws Exception {
        // Given
        doReturn(null).when(scheduler).getJobDetail(any(JobKey.class));

        // When
        final boolean existingJob = quartzSchedulerExecutor.isExistingJob("name");

        // Then
        assertFalse(existingJob);
    }

    @Test
    public void isExistingJob_should_return_true_if_exists_in_quartz() throws Exception {
        // Given
        doReturn(mock(JobDetail.class)).when(scheduler).getJobDetail(any(JobKey.class));

        // When
        final boolean existingJob = quartzSchedulerExecutor.isExistingJob("name");

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
        quartzSchedulerExecutor = new QuartzSchedulerExecutor(schedulerFactory, transactionService, false);
        quartzSchedulerExecutor.setJobListeners(asList(listener1, listener2));

        quartzSchedulerExecutor.start();

        verify(listenerManager).addJobListener(argThat((quartzListener) -> ((QuartzJobListener) quartzListener)
                .getBonitaJobListeners().containsAll(asList(listener1, listener2))));
    }

}
