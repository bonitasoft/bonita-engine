package org.bonitasoft.engine.scheduler.impl;

import static java.util.Arrays.asList;
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
import java.util.Set;

import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.scheduler.trigger.OneShotTrigger;
import org.bonitasoft.engine.scheduler.trigger.RepeatTrigger;
import org.bonitasoft.engine.scheduler.trigger.Trigger;
import org.bonitasoft.engine.scheduler.trigger.Trigger.MisfireRestartPolicy;
import org.bonitasoft.engine.scheduler.trigger.UnixCronTrigger;
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
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;

@RunWith(MockitoJUnitRunner.class)
public class QuartzSchedulerExecutorTest {

    @Mock
    private BonitaSchedulerFactory schedulerFactory;

    @Mock
    private TransactionService transactionService;

    @Mock
    private SchedulerServiceImpl schedulerService;

    @Mock
    private Scheduler scheduler;

    private QuartzSchedulerExecutor quartzSchedulerExecutor;

    @Before
    public void before() throws Exception {
        when(schedulerFactory.getScheduler()).thenReturn(scheduler);

        quartzSchedulerExecutor = initQuartzScheduler(false);
    }

    private QuartzSchedulerExecutor initQuartzScheduler(final boolean useOptimization) throws SSchedulerException {
        final QuartzSchedulerExecutor quartz = new QuartzSchedulerExecutor(schedulerFactory, new ArrayList<AbstractJobListener>(), transactionService, useOptimization);
        quartz.setBOSSchedulerService(schedulerService);
        quartz.start();
        return quartz;
    }

    @After
    public void after() throws Exception {
        quartzSchedulerExecutor.shutdown();
    }

    private Set<JobKey> newSet(JobKey... jobKeys) {
		HashSet<JobKey> set = new HashSet<JobKey>();
		set.addAll(asList(jobKeys));
		return set;
	}
    
    private class TestRepeatTrigger implements RepeatTrigger {
    	private MisfireRestartPolicy misfireRestartPolicy;
		private String triggerName;
		private int interval;
		private int count;

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
			this.misfireRestartPolicy = MisfireRestartPolicy.ONE;
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
    public void should_check_RepeatTrigger_with_bad_interval_param() throws Exception {
		int badInterval = -1;
        RepeatTrigger trigger = new TestRepeatTrigger("triggerName", badInterval, 1);

        quartzSchedulerExecutor.schedule(1l, "2", "jobName", trigger, true);
    }

    @Test(expected = SSchedulerException.class)
    public void should_check_RepeatTrigger_with_bad_name_param() throws Exception {
    	String badTriggerName = null;
    	RepeatTrigger trigger = new TestRepeatTrigger(badTriggerName, 1, 1);

    	quartzSchedulerExecutor.schedule(1l, "2", "jobName", trigger, true);
    }

    @Test(expected = SSchedulerException.class)
    public void should_check_RepeatTrigger_with_bad_count_param() throws Exception {
    	int badCount = -2;
    	RepeatTrigger trigger = new TestRepeatTrigger("triggerName", 1, badCount);

    	quartzSchedulerExecutor.schedule(1l, "2", "jobName", trigger, true);
    }

    @Test
    public void should_pause_jobs_for_a_given_tenant() throws Exception {
        quartzSchedulerExecutor.pauseJobs("123");

        final GroupMatcher<TriggerKey> groupEquals = GroupMatcher.triggerGroupEquals(String.valueOf(123l));
        verify(scheduler, times(1)).pauseTriggers(groupEquals);
    }

    @SuppressWarnings("unchecked")
	@Test(expected = SSchedulerException.class)
    public void should_throw_exception_if_error_occurs_when_pausing_jobs() throws Exception {
        doThrow(SchedulerException.class).when(scheduler).pauseTriggers(any(GroupMatcher.class));

        quartzSchedulerExecutor.pauseJobs("123");
    }

    @Test
    public void should_resume_jobs_for_a_given_tenant() throws Exception {
        quartzSchedulerExecutor.resumeJobs("123");

        final GroupMatcher<TriggerKey> groupEquals = GroupMatcher.triggerGroupEquals(String.valueOf(123l));
        verify(scheduler, times(1)).resumeTriggers(groupEquals);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = SSchedulerException.class)
    public void should_throw_exception_if_error_occurs_when_resuming_jobs() throws Exception {
        doThrow(SchedulerException.class).when(scheduler).resumeTriggers(any(GroupMatcher.class));

        quartzSchedulerExecutor.resumeJobs("123");
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
	public void should_delete_job_for_a_given_name_and_group() throws Exception {
    	JobKey job = jobKey("aName", "aGroup");
	
	    quartzSchedulerExecutor.delete(job.getName(), job.getGroup());
	
	    verify(scheduler).deleteJob(job);
	}

	@Test(expected = SSchedulerException.class)
	public void should_throw_exception_when_error_occurs_in_job_deletion() throws Exception {
		JobKey job = jobKey("aName", "aGroup");
	    when(scheduler.deleteJob(job)).thenThrow(new SchedulerException());

	    quartzSchedulerExecutor.delete(job.getName(), job.getGroup());
	}

	@Test
    public void should_delete_jobs_for_a_given_job_group() throws Exception {
		String groupName = "aGroup";
		JobKey toBeDeleted = jobKey("job1", groupName);
		JobKey toBeDeletedAlso = jobKey("job2", groupName);
		JobKey notToBeDeleted = jobKey("job1", "anotherGroup");
        when(scheduler.getJobKeys(jobGroupEquals(groupName))).thenReturn(newSet(toBeDeleted, toBeDeletedAlso));
        when(scheduler.getJobKeys(not(eq(jobGroupEquals(groupName))))).thenReturn(newSet(notToBeDeleted));

        quartzSchedulerExecutor.deleteJobs(groupName);

        verify(scheduler).deleteJob(toBeDeleted);
        verify(scheduler).deleteJob(toBeDeletedAlso);
        verify(scheduler, never()).deleteJob(notToBeDeleted);
    }
	
	@SuppressWarnings("unchecked")
	@Test(expected = SSchedulerException.class)
	public void should_throw_exception_when_error_occurs_in_jobs_deletion() throws Exception {
	    doThrow(SchedulerException.class).when(scheduler).getJobKeys(any(GroupMatcher.class));
	
	    quartzSchedulerExecutor.deleteJobs("aGroupName");
	}

	@Test
    public void should_get_job_names_for_a_given_group_name() throws Exception {
    	String groupName = "aGroup";
		JobKey toBeRetrieved = jobKey("job1", groupName);
		JobKey toBeRetrievedAlso = jobKey("job2", groupName);
		JobKey notToBeRetrieved = jobKey("job1", "anotherGroup");
		when(scheduler.getJobKeys(jobGroupEquals(groupName))).thenReturn(newSet(toBeRetrieved, toBeRetrievedAlso));
        when(scheduler.getJobKeys(not(eq(jobGroupEquals(groupName))))).thenReturn(newSet(notToBeRetrieved));

        List<String> jobs = quartzSchedulerExecutor.getJobs(groupName);

        assertThat(jobs).containsOnly(toBeRetrieved.getName(), toBeRetrievedAlso.getName());
    }

	@Test(expected = SSchedulerException.class)
	public void should_throw_exception_when_error_occurs_on_job_names_fetching() throws Exception {
		String groupName = "aGroup";
	    doThrow(SchedulerException.class).when(scheduler).getJobKeys(jobGroupEquals(groupName));

	    quartzSchedulerExecutor.getJobs(groupName);
	}

	@Test
    public void should_get_job_names_for_all_group_name() throws Exception {
		JobKey job1 = jobKey("job1", "aGroup");
		JobKey job2 = jobKey("job2", "aGroup");
		JobKey job3 = jobKey("job3", "anotherGroup");
        doReturn(newSet(job1, job2, job3)).when(scheduler).getJobKeys(jobGroupStartsWith(""));

        List<String> jobs = quartzSchedulerExecutor.getAllJobs();

        assertThat(jobs).containsOnly(job1.getName(), job2.getName(), job3.getName());
    }

    @SuppressWarnings("unchecked")
    @Test(expected = SSchedulerException.class)
    public void should_throw_exception_when_error_occurs_on_all_job_names_fetching() throws Exception {
        doThrow(SchedulerException.class).when(scheduler).getJobKeys(any(GroupMatcher.class));

        quartzSchedulerExecutor.getAllJobs();
    }

    @Test(expected = SSchedulerException.class)
    public void should_not_start_twice() throws Exception {
        doReturn(true).when(scheduler).isStarted();

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
	
	    //when
	    executor.schedule(1l, "2", "jobName", new OneShotTrigger("oneShot", new Date(System.currentTimeMillis())), disallowConcurrentExecution);
	
	    //then
	    verify(scheduler).scheduleJob(any(JobDetail.class), any(org.quartz.Trigger.class));
	    verify(transactionService, times(expectedOptimizationCall)).registerBonitaSynchronization(any(BonitaTransactionSynchronization.class));
	}

	@Test(expected = SSchedulerException.class)
    public void schedule_with_exception() throws Exception {
        //given
        doThrow(SchedulerException.class).when(scheduler).scheduleJob(any(JobDetail.class), any(org.quartz.Trigger.class));

        //when
        quartzSchedulerExecutor.schedule(1l, "2", "jobName", new OneShotTrigger("oneShot", new Date(System.currentTimeMillis())), true);

        //then exception

    }

    @Test
    public void executeNow_should_schedule_job() throws Exception {
        quartzSchedulerExecutor.executeNow(1L, "2", "jobName", false);

        verify(scheduler, times(1)).addJob(any(JobDetail.class), anyBoolean());
        verify(scheduler, times(1)).triggerJob(any(JobKey.class));

    }

    @Test(expected = SSchedulerException.class)
    public void executeNow_with_exception() throws Exception {
        doThrow(SchedulerException.class).when(scheduler).triggerJob(any(JobKey.class));

        quartzSchedulerExecutor.executeNow(1l, "2", "jobName", false);
    }

    @Test
    public void schedule_should_use_tenant_id_as_group_in_job_details() throws Exception {
        final String tenantId = "3";
        final Trigger trigger = new TestRepeatTrigger("trigger", 1, 1,  MisfireRestartPolicy.NONE);

        quartzSchedulerExecutor.schedule(10L, tenantId, "myJob", trigger, true);

        final ArgumentCaptor<JobDetail> jobDetailCaptor = ArgumentCaptor.forClass(JobDetail.class);
        verify(scheduler, times(1)).scheduleJob(jobDetailCaptor.capture(), any(org.quartz.Trigger.class));
        final String group = jobDetailCaptor.getValue().getKey().getGroup();
        assertThat(group).isEqualTo(String.valueOf(tenantId));
    }


    @Test
    public void schedule_should_store_tenant_id_in_jobDataMap() throws Exception {
        final String tenantId = "3";
        final Trigger trigger = new TestRepeatTrigger("trigger", 1, 1,  MisfireRestartPolicy.NONE);

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

}
