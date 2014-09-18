package org.bonitasoft.engine.scheduler.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.CommonServiceTest;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.platform.exception.STenantNotFoundException;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.builder.SJobDescriptorBuilderFactory;
import org.bonitasoft.engine.scheduler.builder.SJobParameterBuilderFactory;
import org.bonitasoft.engine.scheduler.job.IncrementItselfJob;
import org.bonitasoft.engine.scheduler.job.ReleaseWaitersJob;
import org.bonitasoft.engine.scheduler.job.VariableStorage;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobParameter;
import org.bonitasoft.engine.scheduler.trigger.OneExecutionTrigger;
import org.bonitasoft.engine.scheduler.trigger.OneShotTrigger;
import org.bonitasoft.engine.scheduler.trigger.Trigger;
import org.bonitasoft.engine.scheduler.trigger.UnixCronTrigger;
import org.bonitasoft.engine.test.util.PlatformUtil;
import org.bonitasoft.engine.test.util.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class QuartzSchedulerExecutorITest extends CommonServiceTest {

    public final String DEFAULT_TENANT_STATUS = "DEACTIVATED";

    private static final SchedulerService schedulerService;

    private long defaultTenantId;

    private long tenant1;

    private final VariableStorage storage = VariableStorage.getInstance();

    static {
        schedulerService = getServicesBuilder().buildSchedulerService();
    }

    @Before
    public void setUp() throws Exception {
        tenant1 = PlatformUtil.createTenant(getTransactionService(), getPlatformService(), "tenant1", PlatformUtil.DEFAULT_CREATED_BY,
                PlatformUtil.DEFAULT_TENANT_STATUS);

        TestUtil.startScheduler(schedulerService);

        getTransactionService().begin();
        defaultTenantId = getPlatformService().getTenantByName("default").getId();
        changeToDefaultTenant();
        getTransactionService().complete();
    }

    @After
    public void stopScheduler() throws Exception {
        TestUtil.stopScheduler(schedulerService, getTransactionService());
        storage.clear();
        PlatformUtil.deleteTenant(getTransactionService(), getPlatformService(), tenant1);
    }

    @Test
    public void testCanRestartTheSchedulerAfterShutdown() throws Exception {
        schedulerService.stop();
        assertTrue(schedulerService.isStopped());
        schedulerService.start();
        assertTrue(schedulerService.isStarted());
    }

    @Test
    public void testDoNotExecuteAFutureJob() throws Exception {
        final Date future = new Date(System.currentTimeMillis() + 10000000);
        final String variableName = "myVar";
        final SJobDescriptor jobDescriptor = BuilderFactory.get(SJobDescriptorBuilderFactory.class)
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobName", "testDoNotExecuteAFutureJob").done());
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("variableName", variableName).done());
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger = new OneExecutionTrigger("events", future, 10);
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();
        Thread.sleep(200);
        assertNull(storage.getVariableValue(variableName));
    }

    @Test
    public void testExecuteAJobInACron() throws Exception {
        // given
        final String jobName = "IncrementItselfJob";
        IncrementItselfJob.reset();
        final SJobDescriptor jobDescriptor = BuilderFactory.get(SJobDescriptorBuilderFactory.class)
                .createNewInstance(IncrementItselfJob.class.getName(), jobName).done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobName", jobName).done());
        final Date now = new Date();
        final Trigger trigger = new UnixCronTrigger("events", now, 10, "0/1 * * * * ?");

        //when
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();
        Thread.sleep(2500);

        //then
        final List<Date> executionDates = IncrementItselfJob.getExecutionDates();
        assertThat(executionDates).as("should have triggered job").isNotEmpty();
        Date previousDate = null;
        for (final Date date : executionDates) {
            if (previousDate != null) {
                assertThat(date).as("should date diff be equal to cron interval").isAfter(previousDate);
            }
            previousDate = date;
        }
    }

    @Test
    public void testDoNotThrowAnExceptionWhenDeletingAnUnknownJob() throws Exception {
        getTransactionService().begin();
        final boolean deleted = schedulerService.delete("MyJob");
        getTransactionService().complete();
        assertFalse(deleted);
    }

    private void changeToTenant1() throws STenantNotFoundException, Exception {
        final long tenant1 = getPlatformService().getTenantByName("tenant1").getId();
        TestUtil.createSessionOn(getSessionAccessor(), getSessionService(), tenant1);
    }

    protected void changeToDefaultTenant() throws STenantNotFoundException, Exception {
        final long defaultTenant = getPlatformService().getTenantByName("default").getId();
        TestUtil.createSessionOn(getSessionAccessor(), getSessionService(), defaultTenant);
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
        final String jobName = "ReleaseWaitersJob";
        Date now = new Date();
        SJobDescriptor jobDescriptor = BuilderFactory.get(SJobDescriptorBuilderFactory.class)
                .createNewInstance(ReleaseWaitersJob.class.getName(), jobName + "1").done();
        List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobName", jobName).done());
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobKey", "1").done());
        Trigger trigger = new UnixCronTrigger("events", now, 10, "0/1 * * * * ?");

        // trigger it
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();
        ReleaseWaitersJob.waitForJobToExecuteOnce();

        // pause
        getTransactionService().begin();
        schedulerService.pauseJobs(defaultTenantId);
        getTransactionService().complete();
        Thread.sleep(100);
        ReleaseWaitersJob.checkNotExecutedDuring(1500);

        // trigger the job in an other tenant
        getTransactionService().begin();
        changeToTenant1();
        getTransactionService().complete();
        now = new Date(System.currentTimeMillis() + 100);
        jobDescriptor = BuilderFactory.get(SJobDescriptorBuilderFactory.class).createNewInstance(ReleaseWaitersJob.class.getName(), jobName + "2").done();
        parameters = new ArrayList<SJobParameter>();
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobName3", jobName).done());
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobKey", "3").done());
        trigger = new OneShotTrigger("events3", now, 10);
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();
        ReleaseWaitersJob.waitForJobToExecuteOnce();

        getTransactionService().begin();
        changeToDefaultTenant();
        getTransactionService().complete();
        // schedule on same group
        now = new Date(System.currentTimeMillis() + 100);
        jobDescriptor = BuilderFactory.get(SJobDescriptorBuilderFactory.class).createNewInstance(ReleaseWaitersJob.class.getName(), jobName + "2").done();
        parameters = new ArrayList<SJobParameter>();
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobName2", jobName).done());
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobKey", "2").done());
        trigger = new OneShotTrigger("events2", now, 10);
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();

        ReleaseWaitersJob.checkNotExecutedDuring(1500);

        // resume
        getTransactionService().begin();
        schedulerService.resumeJobs(defaultTenantId);
        getTransactionService().complete();

        ReleaseWaitersJob.waitForJobToExecuteOnce();
    }

}
