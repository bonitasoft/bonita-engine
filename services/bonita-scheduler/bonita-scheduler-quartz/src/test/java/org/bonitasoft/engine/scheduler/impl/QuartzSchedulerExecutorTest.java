package org.bonitasoft.engine.scheduler.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.quartz.JobKey.jobKey;
import static org.quartz.impl.matchers.GroupMatcher.jobGroupEquals;
import static org.quartz.impl.matchers.GroupMatcher.jobGroupStartsWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.scheduler.trigger.OneShotTrigger;
import org.bonitasoft.engine.scheduler.trigger.Trigger.MisfireRestartPolicy;
import org.bonitasoft.engine.scheduler.trigger.UnixCronTrigger;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;

@RunWith(MockitoJUnitRunner.class)
public class QuartzSchedulerExecutorTest {

    @Mock
    private BonitaSchedulerFactory schedulerFactory;

    @Mock
    private ReadSessionAccessor sessionAccessor;

    @Mock
    private TransactionService transactionService;

    @Mock
    private SchedulerServiceImpl schedulerService;

    @Mock
    private Scheduler scheduler;

    @Mock
    private STenantIdNotSetException sTenantIdNotSetException;

    private QuartzSchedulerExecutor quartzSchedulerExecutor;

    @Before
    public void before() throws Exception {
        when(schedulerFactory.getScheduler()).thenReturn(scheduler);

        quartzSchedulerExecutor = initQuartzScheduler(false);
    }

    private QuartzSchedulerExecutor initQuartzScheduler(final boolean useOptimization) throws SSchedulerException {
        final QuartzSchedulerExecutor quartz = new QuartzSchedulerExecutor(schedulerFactory, new ArrayList<AbstractJobListener>(), sessionAccessor,
                transactionService,
                useOptimization);
        quartz.setBOSSchedulerService(schedulerService);
        quartz.start();
        return quartz;
    }

    @After
    public void after() throws Exception {
        quartzSchedulerExecutor.shutdown();
    }

    @Test
    public void test_delete_job_delete_it_based_on_name_and_tenant_id() throws Exception {
        when(sessionAccessor.getTenantId()).thenReturn(1l);

        quartzSchedulerExecutor.delete("timerjob");

        verify(scheduler).deleteJob(eq(new JobKey("timerjob", "1")));
    }

    @Test
    public void should_pauseJobs_of_tenan_pause_group_of_jobs() throws Exception {
        quartzSchedulerExecutor.pauseJobs(123l);

        final GroupMatcher<TriggerKey> groupEquals = GroupMatcher.triggerGroupEquals(String.valueOf(123l));
        verify(scheduler, times(1)).pauseTriggers(groupEquals);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = SSchedulerException.class)
    public void should_pauseJobs_of_tenant_pause_group_of_jobs_when_quartz_throw_exception() throws Exception {
        doThrow(SchedulerException.class).when(scheduler).pauseTriggers((GroupMatcher<TriggerKey>) any());

        quartzSchedulerExecutor.pauseJobs(123l);
    }

    @Test
    public void should_resumeJobs_of_tenan_pause_group_of_jobs() throws Exception {
        quartzSchedulerExecutor.resumeJobs(123l);

        final GroupMatcher<TriggerKey> groupEquals = GroupMatcher.triggerGroupEquals(String.valueOf(123l));
        verify(scheduler, times(1)).resumeTriggers(groupEquals);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = SSchedulerException.class)
    public void should_resumeJobs_of_tenant_pause_group_of_jobs_when_quartz_throw_exception() throws Exception {
        doThrow(SchedulerException.class).when(scheduler).resumeTriggers((GroupMatcher<TriggerKey>) any());

        quartzSchedulerExecutor.resumeJobs(123l);
    }

    @Test
    public void should_getQuartzTrigger_with_restart_ALL_have_a_ignore_misfire_policy() {
        // given
        final Date triggerEndTime = new Date(System.currentTimeMillis() + 10000);
        final UnixCronTrigger unixCronTrigger = new UnixCronTrigger("MyTrigger", triggerEndTime, "0/5 * * * * ??", MisfireRestartPolicy.ALL);

        // when
        final CronTrigger quartzTrigger = (CronTrigger) quartzSchedulerExecutor.getQuartzTrigger(unixCronTrigger, "MyJob", "12");

        // then
        assertEquals(CronTrigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY, quartzTrigger.getMisfireInstruction());
    }

    @Test
    public void should_getQuartzTrigger_with_restart_NONE_have_a_do_nothing_misfire_policy() {
        // given
        final Date triggerEndTime = new Date(System.currentTimeMillis() + 10000);
        final UnixCronTrigger unixCronTrigger = new UnixCronTrigger("MyTrigger", triggerEndTime, "0/5 * * * * ??", MisfireRestartPolicy.NONE);

        // when
        final CronTrigger quartzTrigger = (CronTrigger) quartzSchedulerExecutor.getQuartzTrigger(unixCronTrigger, "MyJob", "12");

        // then
        assertEquals(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING, quartzTrigger.getMisfireInstruction());
    }

    @Test
    public void should_getQuartzTrigger_with_restart_ONE_have_a_fire_once_misfire_policy() {
        // given
        final Date triggerEndTime = new Date(System.currentTimeMillis() + 10000);
        final UnixCronTrigger unixCronTrigger = new UnixCronTrigger("MyTrigger", triggerEndTime, "0/5 * * * * ??", MisfireRestartPolicy.ONE);

        // when
        final CronTrigger quartzTrigger = (CronTrigger) quartzSchedulerExecutor.getQuartzTrigger(unixCronTrigger, "MyJob", "12");

        // then
        assertEquals(CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW, quartzTrigger.getMisfireInstruction());
    }

    @Test
    public void deleteJobs_delete_only_job_of_tenant() throws Exception {
        // given
        doReturn(123l).when(sessionAccessor).getTenantId();
        final HashSet<JobKey> jobKeyList = getJoblist1();
        final HashSet<JobKey> jobKeyList2 = getJoblist2();

        doReturn(jobKeyList).when(scheduler).getJobKeys(jobGroupEquals(123 + ""));
        doReturn(jobKeyList2).when(scheduler).getJobKeys(not(eq(jobGroupEquals(123 + ""))));

        // when
        quartzSchedulerExecutor.deleteJobs();

        // then
        verify(scheduler).deleteJob(jobKey("job1", "123"));
        verify(scheduler).deleteJob(jobKey("job2", "123"));
        verify(scheduler, never()).deleteJob(jobKey("job3", "123"));
        verify(scheduler, never()).deleteJob(jobKey("job4", "123"));
        verify(scheduler, never()).deleteJob(jobKey("job3", "124"));
        verify(scheduler, never()).deleteJob(jobKey("job4", "124"));
    }

    @Test(expected = SSchedulerException.class)
    public void deleteJob_with_scheduler_exception() throws Exception {
        //given
        doThrow(SchedulerException.class).when(scheduler).deleteJob(any(JobKey.class));
        //when
        quartzSchedulerExecutor.delete("jobName");
        //then
    }

    @Test(expected = SSchedulerException.class)
    public void deleteJob_with_tenant_exception() throws Exception {
        //given

        doThrow(sTenantIdNotSetException).when(sessionAccessor).getTenantId();
        //when
        quartzSchedulerExecutor.delete("jobName");
        //then exception
    }

    @SuppressWarnings("unchecked")
    @Test(expected = SSchedulerException.class)
    public void deleteJobs_with_scheduler_exception() throws Exception {
        //given
        doReturn(123l).when(sessionAccessor).getTenantId();
        doThrow(SchedulerException.class).when(scheduler).getJobKeys(any(GroupMatcher.class));

        //when
        quartzSchedulerExecutor.deleteJobs();

        //then exception
    }

    @Test(expected = SSchedulerException.class)
    public void deleteJobs_with_tenant_exception() throws Exception {
        //        given
        doReturn(123l).when(sessionAccessor).getTenantId();
        doThrow(sTenantIdNotSetException).when(sessionAccessor).getTenantId();

        //when
        quartzSchedulerExecutor.deleteJobs();

        //then exception
    }

    @Test
    public void getJobs() throws Exception {
        // given
        doReturn(123l).when(sessionAccessor).getTenantId();
        final HashSet<JobKey> jobKeyList = getJoblist1();
        final HashSet<JobKey> jobKeyList2 = getJoblist2();

        doReturn(jobKeyList).when(scheduler).getJobKeys(jobGroupEquals(123 + ""));
        doReturn(jobKeyList2).when(scheduler).getJobKeys(not(eq(jobGroupEquals(123 + ""))));

        // when
        final List<String> jobs = quartzSchedulerExecutor.getJobs();

        // then
        assertThat(jobs).as("should have 2 jobs").hasSameSizeAs(jobKeyList);
    }

    @Test
    public void getAllJobsJobs() throws Exception {
        // given
        doReturn(123l).when(sessionAccessor).getTenantId();
        final HashSet<JobKey> jobKeyListAll = getJobListListAll();

        doReturn(jobKeyListAll).when(scheduler).getJobKeys(jobGroupStartsWith(""));

        // when
        final List<String> jobs = quartzSchedulerExecutor.getAllJobs();

        // then
        assertThat(jobs).as("should have alljobs jobs").hasSize(5);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = SSchedulerException.class)
    public void getAllJobsJobs_with_scheduler_exception() throws Exception {
        // given
        doThrow(SchedulerException.class).when(scheduler).getJobKeys(any(GroupMatcher.class));

        // when
        quartzSchedulerExecutor.getAllJobs();

        // then exception

    }

    @Test(expected = SSchedulerException.class)
    public void getJobs_with_tenant_exception() throws Exception {
        // given
        doReturn(123l).when(sessionAccessor).getTenantId();
        final HashSet<JobKey> jobKeyList = getJoblist1();
        final HashSet<JobKey> jobKeyList2 = getJoblist2();

        doReturn(jobKeyList).when(scheduler).getJobKeys(jobGroupEquals(123 + ""));
        doReturn(jobKeyList2).when(scheduler).getJobKeys(not(eq(jobGroupEquals(123 + ""))));
        doThrow(sTenantIdNotSetException).when(sessionAccessor).getTenantId();

        // when
        final List<String> jobs = quartzSchedulerExecutor.getJobs();

        // then
        assertThat(jobs).as("should have 2 jobs").hasSameSizeAs(jobKeyList);
    }

    @Test(expected = SSchedulerException.class)
    public void getJobs_with_scheduler_exception() throws Exception {
        // given
        doReturn(123l).when(sessionAccessor).getTenantId();
        final HashSet<JobKey> jobKeyList = getJoblist1();
        final HashSet<JobKey> jobKeyList2 = getJoblist2();

        doReturn(jobKeyList2).when(scheduler).getJobKeys(not(eq(jobGroupEquals(123 + ""))));
        doThrow(SchedulerException.class).when(scheduler).getJobKeys(jobGroupEquals(123 + ""));

        // when
        final List<String> jobs = quartzSchedulerExecutor.getJobs();

        // then
        assertThat(jobs).as("should have 2 jobs").hasSameSizeAs(jobKeyList);
    }

    @Test(expected = SSchedulerException.class)
    public void should_not_start_twice() throws Exception {
        // given
        doReturn(true).when(scheduler).isStarted();

        // when
        quartzSchedulerExecutor.start();

        // then exception

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
        //given
        final QuartzSchedulerExecutor executor =
                quartzSchedulerExecutor = initQuartzScheduler(true);

        //when
        scheduleJob(executor, true, 1);

    }

    @Test(expected = SSchedulerException.class)
    public void schedule_with_exception() throws Exception {
        //given
        doThrow(SchedulerException.class).when(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));

        //when
        quartzSchedulerExecutor.schedule(1l, 2l, "jobName", new OneShotTrigger("oneShot", new Date(System.currentTimeMillis())), true);

        //then exception

    }

    @Test
    public void executeNow_should_schedule_job() throws Exception {
        final long jobId = 1l;
        final long tenantId = 2l;
        final String jobName = "jobName";
        final boolean disallowConcurrentExecution = false;

        //when
        quartzSchedulerExecutor.executeNow(jobId, tenantId, jobName, disallowConcurrentExecution);

        //then
        verify(scheduler, times(1)).addJob(any(JobDetail.class), anyBoolean());
        verify(scheduler, times(1)).triggerJob(any(JobKey.class));

    }

    @Test(expected = SSchedulerException.class)
    public void executeNow_with_exception() throws Exception {
        final long jobId = 1l;
        final long tenantId = 2l;
        final String jobName = "jobName";
        final boolean disallowConcurrentExecution = false;
        //given
        doThrow(SchedulerException.class).when(scheduler).triggerJob(any(JobKey.class));

        //when
        quartzSchedulerExecutor.executeNow(jobId, tenantId, jobName, disallowConcurrentExecution);

        //then exception

    }

    private void scheduleJob(final QuartzSchedulerExecutor executor, final boolean disallowConcurrentExecution, final int expectedOptimizationCall)
            throws Exception {

        //when
        executor.schedule(1l, 2l, "jobName", new OneShotTrigger("oneShot", new Date(System.currentTimeMillis())), disallowConcurrentExecution);

        //then
        verify(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
        verify(transactionService, times(expectedOptimizationCall)).registerBonitaSynchronization(any(BonitaTransactionSynchronization.class));
    }

    private HashSet<JobKey> getJoblist1() {
        final HashSet<JobKey> jobKeyList = new HashSet<JobKey>();
        jobKeyList.add(new JobKey("job1", "123"));
        jobKeyList.add(new JobKey("job2", "123"));
        return jobKeyList;
    }

    private HashSet<JobKey> getJoblist2() {
        final HashSet<JobKey> jobKeyList2 = new HashSet<JobKey>();
        jobKeyList2.add(new JobKey("job3", "124"));
        jobKeyList2.add(new JobKey("job4", "124"));
        jobKeyList2.add(new JobKey("job5", "124"));
        return jobKeyList2;
    }

    private HashSet<JobKey> getJobListListAll() {
        final HashSet<JobKey> jobKeyListAll = new HashSet<JobKey>();
        jobKeyListAll.add(new JobKey("job1", "123"));
        jobKeyListAll.add(new JobKey("job2", "123"));
        jobKeyListAll.add(new JobKey("job3", "124"));
        jobKeyListAll.add(new JobKey("job4", "124"));
        jobKeyListAll.add(new JobKey("job5", "124"));
        return jobKeyListAll;
    }
}
