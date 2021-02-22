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

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.bonitasoft.engine.scheduler.impl.JobThatMayThrowErrorOrJobException.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.job.ReleaseWaitersJob;
import org.bonitasoft.engine.scheduler.job.VariableStorage;
import org.bonitasoft.engine.scheduler.model.SFailedJob;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobParameter;
import org.bonitasoft.engine.scheduler.trigger.OneShotTrigger;
import org.bonitasoft.engine.scheduler.trigger.Trigger;
import org.bonitasoft.engine.scheduler.trigger.UnixCronTrigger;
import org.bonitasoft.engine.scheduler.trigger.UnixCronTriggerForTest;
import org.bonitasoft.engine.test.util.TestUtil;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.bonitasoft.engine.util.FunctionalMatcher;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SchedulerServiceIT extends CommonBPMServicesTest {

    private SchedulerService schedulerService;
    private JobService jobService;
    private UserTransactionService userTransactionService;
    private final VariableStorage storage = VariableStorage.getInstance();

    @Before
    public void before() throws Exception {
        schedulerService = getTenantAccessor().getSchedulerService();
        userTransactionService = getTenantAccessor().getUserTransactionService();
        jobService = getTenantAccessor().getJobService();
        TestUtil.stopScheduler(schedulerService, getTransactionService());
        TestUtil.startScheduler(schedulerService);
        getTenantAccessor().getSessionAccessor().setTenantId(getDefaultTenantId());
    }

    @After
    public void after() throws Exception {
        storage.clear();
        userTransactionService.executeInTransaction(() -> {
            schedulerService.deleteJobs();
            return null;
        });
    }

    @Test
    public void canRestartTheSchedulerAfterShutdown() throws Exception {
        schedulerService.stop();
        assertTrue(schedulerService.isStopped());
        schedulerService.start();
        assertTrue(schedulerService.isStarted());
    }

    @Test
    public void doNotExecuteAFutureJob() throws Exception {
        final Date future = new Date(System.currentTimeMillis() + 10000000);
        final String variableName = "myVar";
        final SJobDescriptor jobDescriptor = SJobDescriptor.builder()
                .jobClassName("org.bonitasoft.engine.scheduler.job.IncrementVariableJob")
                .jobName("IncrementVariableJob").build();
        final List<SJobParameter> parameters = new ArrayList<>();
        parameters.add(SJobParameter.builder().key("jobName").value("testDoNotExecuteAFutureJob").build());
        parameters.add(SJobParameter.builder().key("variableName").value(variableName).build());
        parameters.add(SJobParameter.builder().key("throwExceptionAfterNIncrements").value(-1).build());
        final Trigger trigger = new OneShotTrigger("events", future, 10);
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();
        Thread.sleep(200);
        assertNull(storage.getVariableValue(variableName));
    }

    @Test
    public void doNotThrowAnExceptionWhenDeletingAnUnknownJob() throws Exception {
        getTransactionService().begin();
        final boolean deleted = schedulerService.delete("MyJob");
        getTransactionService().complete();
        assertFalse(deleted);
    }

    /*
     * We must ensure that:
     * * pause only jobs of the current tenant
     * * trigger new job are not executed
     * * resume the jobs resume it really
     * *
     */
    @Test
    public void pause_and_resume_jobs_of_a_tenant() throws Exception {
        long tenantForJobTest1 = createTenant("tenantForJobTest1");
        long tenantForJobTest2 = createTenant("tenantForJobTest2");

        changeTenant(tenantForJobTest1);
        final String jobName = "ReleaseWaitersJob";
        Date now = new Date();
        SJobDescriptor jobDescriptor = SJobDescriptor.builder()
                .jobClassName(ReleaseWaitersJob.class.getName()).jobName(jobName + "1").build();
        List<SJobParameter> parameters = new ArrayList<>();
        parameters.add(SJobParameter.builder().key("jobName").value(jobName).build());
        parameters.add(SJobParameter.builder().key("jobKey").value("1").build());
        Trigger trigger = new UnixCronTriggerForTest("events", now, 10, "0/1 * * * * ?");

        // trigger it
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();
        ReleaseWaitersJob.waitForJobToExecuteOnce();

        // pause
        getTransactionService().begin();
        schedulerService.pauseJobs(sessionAccessor.getTenantId());
        getTransactionService().complete();
        Thread.sleep(100);
        ReleaseWaitersJob.checkNotExecutedDuring(1500);

        // trigger the job in an other tenant
        changeTenant(tenantForJobTest2);
        now = new Date(System.currentTimeMillis() + 100);
        jobDescriptor = SJobDescriptor.builder()
                .jobClassName(ReleaseWaitersJob.class.getName()).jobName(jobName + "2").build();
        parameters = new ArrayList<>();
        parameters.add(SJobParameter.builder().key("jobName3").value(jobName).build());
        parameters.add(SJobParameter.builder().key("jobKey").value("3").build());
        trigger = new OneShotTrigger("events3", now, 10);
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();
        ReleaseWaitersJob.waitForJobToExecuteOnce();

        //cleanup
        after();
        changeTenant(tenantForJobTest1);
        after();
        changeTenant(getDefaultTenantId());
    }

    @Test
    public void should_be_able_to_list_job_that_failed_because_of_an_Error() throws Exception {
        // schedule a job that throws an Error
        schedule(jobDescriptor(JobThatMayThrowErrorOrJobException.class, "MyJob"),
                new OneShotTrigger("triggerJob", new Date(System.currentTimeMillis() + 100)),
                singletonMap(TYPE, ERROR));

        //we have failed job
        List<SFailedJob> failedJobs = await().until(() -> inTx(() -> jobService.getFailedJobs(0, 100)), hasSize(1));
        assertThat(failedJobs).hasOnlyOneElementSatisfying(f -> assertThat(f.getLastMessage()).contains("an Error"));
    }

    @Test
    public void should_be_able_to_restart_a_job_that_failed_because_of_a_SJobExecutionException() throws Exception {
        // schedule a job that throws a SJobExecutionException
        schedule(jobDescriptor(JobThatMayThrowErrorOrJobException.class, "MyJob"),
                new OneShotTrigger("triggerJob", new Date(System.currentTimeMillis() + 100)),
                singletonMap(TYPE, JOBEXCEPTION));
        SJobDescriptor persistedJobDescriptor = getFirstPersistedJob();

        //we have failed job
        List<SFailedJob> failedJobs = await().until(() -> inTx(() -> jobService.getFailedJobs(0, 100)), hasSize(1));
        assertThat(failedJobs)
                .hasOnlyOneElementSatisfying(f -> assertThat(f.getLastMessage()).contains("a Job exception"));

        //small sleep because quartz do not always immediately delete the associated trigger (done in the quartz Thread)
        // because of that it can cause issues when rescheduling (Foreign key violation)
        Thread.sleep(500);
        //reschedule the job: no more exception
        inTx(() -> {
            schedulerService.retryJobThatFailed(persistedJobDescriptor.getId(),
                    toJobParameterList(singletonMap(TYPE, NO_EXCEPTION)));
            return null;
        });
        await().until(() -> storage.getVariableValue("nbSuccess", 0).equals(1));
    }

    @Test
    public void should_be_able_to_restart_a_cron_job_that_failed_because_of_a_SJobExecutionException()
            throws Exception {
        // schedule a job that throws a SJobExecutionException
        schedule(jobDescriptor(JobThatMayThrowErrorOrJobException.class, "MyJob"),
                new UnixCronTrigger("triggerJob", new Date(System.currentTimeMillis() + 100), "* * * * * ?"),
                singletonMap(TYPE, JOBEXCEPTION));
        SJobDescriptor persistedJobDescriptor = getFirstPersistedJob();

        //ensure there is more than one failure: i.e. cron is still triggering new jobs
        await().until(() -> storage.getVariableValue("nbJobException", 0), isGreaterThan(1));
        //wait a little because the failure is registered later...
        Callable<List<SFailedJob>> getFailedJobs = () -> {
            try {
                return inTx(() -> jobService.getFailedJobs(0, 100));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
        List<SFailedJob> sFailedJobs = await().until(getFailedJobs, new BaseMatcher<List<SFailedJob>>() {

            @Override
            public boolean matches(Object item) {
                List<SFailedJob> list = (List<SFailedJob>) item;
                return list.size() == 1 && list.get(0).getNumberOfFailures() > 1;
            }

            @Override
            public void describeTo(Description description) {

            }
        });

        assertThat(sFailedJobs).hasSize(1);
        //ensure we trace the number of failure
        assertThat(sFailedJobs.get(0).getNumberOfFailures()).isGreaterThan(1);

        //reschedule the job: no more exception
        inTx(() -> {
            schedulerService.retryJobThatFailed(persistedJobDescriptor.getId(),
                    toJobParameterList(singletonMap(TYPE, NO_EXCEPTION)));
            return null;
        });

        //ensure there is more than one success: i.e. cron is still triggering new jobs
        await().until(() -> storage.getVariableValue("nbSuccess", 0), isGreaterThan(1));
        //ensure no more failed job is present
        assertThat(inTx(() -> jobService.getFailedJobs(0, 100))).isEmpty();
    }

    @Test
    public void should_keep_a_failed_job_when_failing_once() throws Exception {
        // schedule a job that throws a SJobExecutionException
        schedule(jobDescriptor(JobThatMayThrowErrorOrJobException.class, "MyJob"),
                new UnixCronTrigger("triggerJob", new Date(System.currentTimeMillis() + 100), "* * * * * ?"),
                singletonMap(TYPE, FAIL_ONCE));
        SJobDescriptor persistedJobDescriptor = getFirstPersistedJob();

        //this job fail only the first time
        await().until(() -> storage.getVariableValue("nbJobException", 0), isGreaterThan(0));
        await().until(() -> storage.getVariableValue("nbSuccess", 0), isGreaterThan(0));

        List<SFailedJob> sFailedJobs = inTx(() -> jobService.getFailedJobs(0, 100));

        assertThat(sFailedJobs).hasSize(1);
        //ensure we trace the number of failure
        assertThat(sFailedJobs.get(0).getNumberOfFailures()).isEqualTo(1);

        //reschedule the job: no more exception
        inTx(() -> {
            schedulerService.retryJobThatFailed(persistedJobDescriptor.getId(),
                    toJobParameterList(singletonMap(TYPE, NO_EXCEPTION)));
            return null;
        });

        //ensure there is more than one success: i.e. cron is still triggering new jobs
        await().until(() -> storage.getVariableValue("nbSuccess", 0), isGreaterThan(1));
        //ensure no more failed job is present
        assertThat(inTx(() -> jobService.getFailedJobs(0, 100))).isEmpty();
    }

    @Test
    public void should_let_quartz_retry_a_job_that_failed_because_of_a_SRetryableException() throws Exception {
        // schedule one shot job that throws a SJobExecutionException
        schedule(jobDescriptor(JobThatMayThrowErrorOrJobException.class, "MyJob"),
                new OneShotTrigger("triggerJob", new Date(System.currentTimeMillis() + 100)),
                singletonMap(TYPE, FAIL_ONCE_WITH_RETRYABLE));
        SJobDescriptor persistedJobDescriptor = getFirstPersistedJob();

        //this job fail once and is immediately retried, even if its a one shot job
        await().until(() -> storage.getVariableValue("nbJobException", 0), isGreaterThan(0));
        await().until(() -> storage.getVariableValue("nbSuccess", 0), isGreaterThan(0));

        List<SFailedJob> sFailedJobs = inTx(() -> jobService.getFailedJobs(0, 100));

        //no error traced, it was retried
        assertThat(sFailedJobs).hasSize(0);
    }

    private FunctionalMatcher<Integer> isGreaterThan(int i) {
        return t -> t > i;
    }

    private SJobDescriptor getFirstPersistedJob() throws Exception {
        return inTx(() -> jobService.searchJobDescriptors(new QueryOptions(0, 1))).get(0);
    }

    private <T> T inTx(Callable<T> callable) throws Exception {

        return userTransactionService.executeInTransaction(() -> {
            getTenantAccessor().getSessionAccessor().setTenantId(getDefaultTenantId());
            return callable.call();
        });
    }

    private SJobDescriptor jobDescriptor(Class<?> jobClass, String jobName) {
        return SJobDescriptor.builder()
                .jobClassName(jobClass.getName()).jobName(jobName).build();
    }

    private void schedule(SJobDescriptor jobDescriptor, Trigger trigger, Map<String, Serializable> parameters)
            throws Exception {
        List<SJobParameter> parametersList = toJobParameterList(parameters);
        inTx(() -> {
            schedulerService.schedule(jobDescriptor, parametersList, trigger);
            return null;
        });
    }

    private List<SJobParameter> toJobParameterList(Map<String, Serializable> parameters) {
        return parameters.entrySet().stream()
                .map(e -> SJobParameter.builder().key(e.getKey()).value(e.getValue()).build())
                .collect(Collectors.toList());
    }

}
