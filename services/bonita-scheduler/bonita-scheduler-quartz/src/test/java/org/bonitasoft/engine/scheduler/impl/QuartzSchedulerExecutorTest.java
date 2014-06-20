package org.bonitasoft.engine.scheduler.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.quartz.JobKey.jobKey;
import static org.quartz.impl.matchers.GroupMatcher.jobGroupEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.scheduler.trigger.Trigger.MisfireRestartPolicy;
import org.bonitasoft.engine.scheduler.trigger.UnixCronTrigger;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.quartz.CronTrigger;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
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

    private QuartzSchedulerExecutor quartzSchedulerExecutor;

    @Before
    public void before() throws Exception {
        final boolean useOptimizations = false;
        quartzSchedulerExecutor = new QuartzSchedulerExecutor(schedulerFactory, new ArrayList<AbstractJobListener>(), sessionAccessor, transactionService,
                useOptimizations);
        quartzSchedulerExecutor.setBOSSchedulerService(schedulerService);
        when(schedulerFactory.getScheduler()).thenReturn(scheduler);
        quartzSchedulerExecutor.start();
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

        GroupMatcher<TriggerKey> groupEquals = GroupMatcher.triggerGroupEquals(String.valueOf(123l));
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

        GroupMatcher<TriggerKey> groupEquals = GroupMatcher.triggerGroupEquals(String.valueOf(123l));
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
        Date triggerEndTime = new Date(System.currentTimeMillis() + 10000);
        UnixCronTrigger unixCronTrigger = new UnixCronTrigger("MyTrigger", triggerEndTime, "0/5 * * * * ??", MisfireRestartPolicy.ALL);

        // when
        CronTrigger quartzTrigger = (CronTrigger) quartzSchedulerExecutor.getQuartzTrigger(unixCronTrigger, "MyJob", "12");

        // then
        assertEquals(CronTrigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY, quartzTrigger.getMisfireInstruction());
    }

    @Test
    public void should_getQuartzTrigger_with_restart_NONE_have_a_do_nothing_misfire_policy() {
        // given
        Date triggerEndTime = new Date(System.currentTimeMillis() + 10000);
        UnixCronTrigger unixCronTrigger = new UnixCronTrigger("MyTrigger", triggerEndTime, "0/5 * * * * ??", MisfireRestartPolicy.NONE);

        // when
        CronTrigger quartzTrigger = (CronTrigger) quartzSchedulerExecutor.getQuartzTrigger(unixCronTrigger, "MyJob", "12");

        // then
        assertEquals(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING, quartzTrigger.getMisfireInstruction());
    }

    @Test
    public void should_getQuartzTrigger_with_restart_ONE_have_a_fire_once_misfire_policy() {
        // given
        Date triggerEndTime = new Date(System.currentTimeMillis() + 10000);
        UnixCronTrigger unixCronTrigger = new UnixCronTrigger("MyTrigger", triggerEndTime, "0/5 * * * * ??", MisfireRestartPolicy.ONE);

        // when
        CronTrigger quartzTrigger = (CronTrigger) quartzSchedulerExecutor.getQuartzTrigger(unixCronTrigger, "MyJob", "12");

        // then
        assertEquals(CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW, quartzTrigger.getMisfireInstruction());
    }

    @Test
    public void deleteJobs_delete_only_job_of_tenant() throws Exception {
        // given
        doReturn(123l).when(sessionAccessor).getTenantId();
        HashSet<JobKey> jobKeyList = new HashSet<JobKey>();
        jobKeyList.add(new JobKey("job1", "123"));
        jobKeyList.add(new JobKey("job2", "123"));
        HashSet<JobKey> jobKeyList2 = new HashSet<JobKey>();
        jobKeyList2.add(new JobKey("job3", "124"));
        jobKeyList2.add(new JobKey("job4", "124"));

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
}
