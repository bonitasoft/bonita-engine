package org.bonitasoft.engine.scheduler.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
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
        boolean useOptimizations = false;
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

        verify(scheduler, times(1)).deleteJob(eq(new JobKey("timerjob", "1")));
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
}
