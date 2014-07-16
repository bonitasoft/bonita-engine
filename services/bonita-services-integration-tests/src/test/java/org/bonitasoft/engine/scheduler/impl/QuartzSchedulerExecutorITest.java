package org.bonitasoft.engine.scheduler.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.CommonServiceTest;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.platform.STenantNotFoundException;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.builder.SJobDescriptorBuilderFactory;
import org.bonitasoft.engine.scheduler.builder.SJobParameterBuilderFactory;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.scheduler.job.IncrementItselfJob;
import org.bonitasoft.engine.scheduler.job.IncrementVariableJobWithMultiTenancy;
import org.bonitasoft.engine.scheduler.job.ReleaseWaitersJob;
import org.bonitasoft.engine.scheduler.job.VariableStorage;
import org.bonitasoft.engine.scheduler.job.VariableStorageByTenant;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobParameter;
import org.bonitasoft.engine.scheduler.trigger.OneExecutionTrigger;
import org.bonitasoft.engine.scheduler.trigger.OneShotTrigger;
import org.bonitasoft.engine.scheduler.trigger.RepeatXTimesTrigger;
import org.bonitasoft.engine.scheduler.trigger.Trigger;
import org.bonitasoft.engine.scheduler.trigger.Trigger.MisfireRestartPolicy;
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
    public void testExecuteOnceAJob() throws Exception {
        IncrementItselfJob.reset();
        final Date now = new Date();
        final SJobDescriptor jobDescriptor = BuilderFactory.get(SJobDescriptorBuilderFactory.class)
                .createNewInstance(IncrementItselfJob.class.getName(), "IncrementVariableJob").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobName", "testExecuteOnceAJob").done());
        final Trigger trigger = new OneExecutionTrigger("events", now, 10, MisfireRestartPolicy.NONE);

        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();
        waitForIncrementJobToBeExecutedXTimes(1, 5000);
        Thread.sleep(500);
        assertEquals(1, IncrementItselfJob.getValue());
    }

    private void waitForIncrementJobToBeExecutedXTimes(final int x, final int timeout) throws InterruptedException {
        assertTrue("Job was not executed " + x + " time(s)", new WaitForIncrementJobToHaveValue(timeout, x).waitFor());
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

    @Test(expected = SSchedulerException.class)
    public void testCannotUseTheSameTriggerWithTwoJobs() throws Throwable {
        final Date now = new Date();
        final String variableName = "myVar";
        final SJobDescriptor jobDescriptor1 = BuilderFactory.get(SJobDescriptorBuilderFactory.class)
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob1").done();
        final List<SJobParameter> parameters1 = new ArrayList<SJobParameter>();
        parameters1.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobName", "1").done());
        parameters1.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("variableName", variableName).done());
        parameters1.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final SJobDescriptor jobDescriptor2 = BuilderFactory.get(SJobDescriptorBuilderFactory.class)
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob2").done();
        final List<SJobParameter> parameters2 = new ArrayList<SJobParameter>();
        parameters2.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobName", "2").done());
        parameters2.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("variableName", variableName).done());
        parameters2.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger1 = new RepeatXTimesTrigger("trigger1", now, 10, 1000, 100);
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor1, parameters1, trigger1);
        try {
            schedulerService.schedule(jobDescriptor2, parameters2, trigger1);
        } finally {
            getTransactionService().complete();
        }
    }

    @Test(expected = SSchedulerException.class)
    public void testCannotUseTheSameTriggerNameAndGroupWithTwoJobs() throws Throwable {
        final Date now = new Date();
        final String variableName = "myVar";
        final SJobDescriptor jobDescriptor1 = BuilderFactory.get(SJobDescriptorBuilderFactory.class)
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob1").done();
        final List<SJobParameter> parameters1 = new ArrayList<SJobParameter>();
        parameters1.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobName", "1").done());
        parameters1.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("variableName", variableName).done());
        parameters1.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final SJobDescriptor jobDescriptor2 = BuilderFactory.get(SJobDescriptorBuilderFactory.class)
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob2").done();
        final List<SJobParameter> parameters2 = new ArrayList<SJobParameter>();
        parameters2.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobName", "2").done());
        parameters2.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("variableName", variableName).done());
        parameters2.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger1 = new RepeatXTimesTrigger("trigger1", now, 10, 1000, 100);
        final Trigger trigger2 = new RepeatXTimesTrigger("trigger1", now, 5, 80, 1000);
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor1, parameters1, trigger1);
        try {
            schedulerService.schedule(jobDescriptor2, parameters2, trigger2);
        } finally {
            getTransactionService().complete();
        }
    }

    @Test(expected = SSchedulerException.class)
    public void testCannotUseTheSameJobNameInTheSameGroup() throws Throwable {
        final Date now = new Date();
        final SJobDescriptor jobDescriptor1 = BuilderFactory.get(SJobDescriptorBuilderFactory.class)
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob").done();
        final List<SJobParameter> parameters1 = new ArrayList<SJobParameter>();
        parameters1.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobName", "1").done());
        parameters1.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("variableName", "first").done());
        parameters1.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final SJobDescriptor jobDescriptor2 = BuilderFactory.get(SJobDescriptorBuilderFactory.class)
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob").done();
        final List<SJobParameter> parameters2 = new ArrayList<SJobParameter>();
        parameters2.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobName", "2").done());
        parameters2.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("variableName", "second").done());
        parameters2.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger1 = new RepeatXTimesTrigger("trigger1", now, 10, 1000, 100);
        final Trigger trigger2 = new RepeatXTimesTrigger("trigger2", now, 5, 80, 1000);
        getTransactionService().begin();

        schedulerService.schedule(jobDescriptor1, parameters1, trigger1);
        try {
            schedulerService.schedule(jobDescriptor2, parameters2, trigger2);
        } finally {
            getTransactionService().complete();
        }
    }

    @Test
    public void testCanUseAJobWithANullGroup() throws Exception {
        final Date now = new Date();
        final SJobDescriptor jobDescriptor = BuilderFactory.get(SJobDescriptorBuilderFactory.class)
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobName", "testDoNotExecuteAFutureJob").done());
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("variableName", "first").done());
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger1 = new RepeatXTimesTrigger("testCanUseAJobWithANullGroup", now, 10, 1000, 100);
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger1);
        getTransactionService().complete();
    }

    @Test(expected = SSchedulerException.class)
    public void testCannotUseAOneShotTriggerWithANullName() throws Throwable {
        final Date now = new Date();
        final SJobDescriptor jobDescriptor = BuilderFactory.get(SJobDescriptorBuilderFactory.class)
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobName", "testDoNotExecuteAFutureJob").done());
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("variableName", "first").done());
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger = new OneExecutionTrigger(null, now, 10);
        getTransactionService().begin();
        try {
            schedulerService.schedule(jobDescriptor, parameters, trigger);
        } finally {
            getTransactionService().complete();
        }
    }

    @Test
    public void testCanUseAOneShotTriggerWithANullGroup() throws Exception {
        final Date now = new Date();
        final SJobDescriptor jobDescriptor = BuilderFactory.get(SJobDescriptorBuilderFactory.class)
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobName", "testCanUseAOneShotTriggerWithANullGroup").done());
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("variableName", "first").done());
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger = new OneExecutionTrigger("oneshot", now, 10);
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();
    }

    @Test
    public void testCanUseARepeatTriggerWithANullGroup() throws Exception {
        final Date now = new Date();
        final SJobDescriptor jobDescriptor = BuilderFactory.get(SJobDescriptorBuilderFactory.class)
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobName", "testCanUseARepeatTriggerWithANullGroup").done());
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("variableName", "first").done());
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger1 = new RepeatXTimesTrigger("trig", now, 10, 1000, 100);
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger1);
        getTransactionService().complete();
    }

    @Test(expected = SSchedulerException.class)
    public void testCannotUseACronTriggerWithANullName() throws Throwable {
        final Date now = new Date();
        final SJobDescriptor jobDescriptor = BuilderFactory.get(SJobDescriptorBuilderFactory.class)
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobName", "1").done());
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("variableName", "first").done());
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger = new UnixCronTrigger(null, now, 10, "0/1 * * * * ?");
        getTransactionService().begin();
        try {
            schedulerService.schedule(jobDescriptor, parameters, trigger);
        } finally {
            getTransactionService().complete();
        }
    }

    @Test
    public void testCanUseACronTriggerWithANullGroup() throws Exception {
        final Date now = new Date();
        final SJobDescriptor jobDescriptor = BuilderFactory.get(SJobDescriptorBuilderFactory.class)
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobName", "testCanUseACronTriggerWithANullGroup").done());
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("variableName", "first").done());
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger = new UnixCronTrigger("events", now, 10, "0/1 * * * * ?");
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();
    }

    @Test
    public void testExecuteAJobInACron() throws Exception {
        final String jobName = "IncrementItselfJob";
        final Date now = new Date();
        final SJobDescriptor jobDescriptor = BuilderFactory.get(SJobDescriptorBuilderFactory.class)
                .createNewInstance(IncrementItselfJob.class.getName(), jobName).done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobName", jobName).done());
        final Trigger trigger = new UnixCronTrigger("events", now, 10, "0/1 * * * * ?");

        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();

        final int value = IncrementItselfJob.getValue();

        Thread.sleep(1500);

        final int newValue = IncrementItselfJob.getValue();
        final int delta = newValue - value;
        assertTrue("expected 1,2 or 3 executions in 1.5 seconds, got: " + delta, delta == 1 || delta == 2 || delta == 3);
    }

    @Test
    public void testDoNotThrowAnExceptionWhenDeletingAnUnknownGroupOfJobs() throws Exception {
        getTransactionService().begin();
        final boolean deleted = schedulerService.delete("MyJob");
        getTransactionService().complete();
        assertFalse(deleted);
    }

    @Test
    public void testMultiTenancy() throws Exception {
        VariableStorageByTenant.clearAll();
        IncrementVariableJobWithMultiTenancy.setSessionAccessor(getSessionAccessor());
        final Date now = new Date(System.currentTimeMillis() + 10000000);
        final String variableName = "testMultiTenancy";
        final SJobDescriptor jobDescriptor = BuilderFactory.get(SJobDescriptorBuilderFactory.class)
                .createNewInstance(IncrementVariableJobWithMultiTenancy.class.getName(), "IncrementVariableJob").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobName", "testExecuteOnceAJob").done());
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("variableName", variableName).done());
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger = new OneExecutionTrigger("events", now, 10);
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();
        Thread.sleep(2000);
        final long defaultTenant = getTenantIdFromSession();
        assertNull(VariableStorageByTenant.getInstance(defaultTenant).getVariableValue(variableName));
        assertNull(VariableStorageByTenant.getInstance(tenant1).getVariableValue(variableName));

        List<String> jobs = schedulerService.getJobs();
        assertNotNull(jobs);
        assertEquals(1, jobs.size());

        // change tenant
        getTransactionService().begin();
        changeToTenant1();

        jobs = schedulerService.getJobs();
        assertEquals(0, jobs.size());

        changeToDefaultTenant();
        TestUtil.createSessionOn(getSessionAccessor(), getSessionService(), defaultTenant);
        jobs = schedulerService.getJobs();
        getTransactionService().complete();
        assertEquals(1, jobs.size());
    }

    @Test
    public void testMultiTenantJobs() throws Exception {
        VariableStorageByTenant.clearAll();
        IncrementVariableJobWithMultiTenancy.setSessionAccessor(getSessionAccessor());
        final Date now = new Date(System.currentTimeMillis());
        final String variableName = "testMultiTenancy";
        final SJobDescriptor jobDescriptor = BuilderFactory.get(SJobDescriptorBuilderFactory.class)
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJobWithMultiTenancy", "IncrementVariableJob").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobName", "testExecuteOnceAJob").done());
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("variableName", variableName).done());
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger = new OneExecutionTrigger("events", now, 10);

        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();
        Thread.sleep(2000);

        final long defaultTenant = getTenantIdFromSession();
        assertNotNull(VariableStorageByTenant.getInstance(defaultTenant).getVariableValue(variableName));
        assertNull(VariableStorageByTenant.getInstance(tenant1).getVariableValue(variableName));

        getTransactionService().begin();
        final List<String> jobs = schedulerService.getJobs();
        getTransactionService().complete();
        assertEquals(0, jobs.size());// job id completed
    }

    private long getTenantIdFromSession() throws Exception {
        return getSessionAccessor().getTenantId();
    }

    private void changeToTenant1() throws STenantNotFoundException, Exception {
        final long tenant1 = getPlatformService().getTenantByName("tenant1").getId();
        TestUtil.createSessionOn(getSessionAccessor(), getSessionService(), tenant1);
    }

    protected void changeToDefaultTenant() throws STenantNotFoundException, Exception {
        final long defaultTenant = getPlatformService().getTenantByName("default").getId();
        TestUtil.createSessionOn(getSessionAccessor(), getSessionService(), defaultTenant);
    }

    @Test
    public void testCannotDeleteAGroupOfJobsFromAnotherTenant() throws Exception {
        final String jobName1 = "MyJob1";
        final String jobName2 = "MyJob2";
        final Date now = new Date();
        final SJobDescriptor jobDescriptor1 = BuilderFactory.get(SJobDescriptorBuilderFactory.class)
                .createNewInstance(IncrementItselfJob.class.getName(), "IncrementVariableJob1").done();
        final List<SJobParameter> parameters1 = new ArrayList<SJobParameter>();
        parameters1.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobName", jobName1).done());
        final Trigger trigger1 = new RepeatXTimesTrigger("event1", now, 10, 1000, 100);
        final SJobDescriptor jobDescriptor2 = BuilderFactory.get(SJobDescriptorBuilderFactory.class)
                .createNewInstance(IncrementItselfJob.class.getName(), "IncrementVariableJob2").done();
        final List<SJobParameter> parameters2 = new ArrayList<SJobParameter>();
        parameters2.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobName", jobName2).done());
        final Trigger trigger2 = new RepeatXTimesTrigger("event2", now, 10, 1000, 100);
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor1, parameters1, trigger1);
        getTransactionService().complete();
        Thread.sleep(50);

        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor2, parameters2, trigger2);
        getTransactionService().complete();
        Thread.sleep(500);

        // change tenant
        final long defaultTenant = getTenantIdFromSession();
        getTransactionService().begin();
        changeToTenant1();
        getTransactionService().complete();

        getTransactionService().begin();
        schedulerService.deleteJobs();
        getTransactionService().complete();

        Thread.sleep(200);

        getTransactionService().begin();
        final WaitForIncrementJobToHaveValue wf = new WaitForIncrementJobToHaveValue(1000, IncrementItselfJob.getValue() + 2);
        final boolean waitFor = wf.waitFor();
        getTransactionService().complete();

        assertTrue(waitFor);

        TestUtil.createSessionOn(getSessionAccessor(), getSessionService(), defaultTenant);

    }

    @Test
    public void testOnlyGetTenantJobs() throws Exception {
        final Date now = new Date();
        final String variableName = "myVar";
        final SJobDescriptor jobDescriptor1 = BuilderFactory.get(SJobDescriptorBuilderFactory.class)
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "1").done();
        final List<SJobParameter> parameters1 = new ArrayList<SJobParameter>();
        parameters1.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobName", "1").done());
        parameters1.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("variableName", variableName).done());
        parameters1.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final SJobDescriptor jobDescriptor2 = BuilderFactory.get(SJobDescriptorBuilderFactory.class)
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "2").done();
        final List<SJobParameter> parameters2 = new ArrayList<SJobParameter>();
        parameters2.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobName", "2").done());
        parameters2.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("variableName", variableName).done());
        parameters2.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger1 = new RepeatXTimesTrigger("trigger1", now, 10, 1000, 100);
        final Trigger trigger2 = new RepeatXTimesTrigger("trigger2", now, 10, 1000, 100);
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor1, parameters1, trigger1);
        getTransactionService().complete();
        Thread.sleep(50);

        getTransactionService().begin();
        // change tenant
        final long defaultTenant = getTenantIdFromSession();
        changeToTenant1();
        schedulerService.schedule(jobDescriptor2, parameters2, trigger2);
        getTransactionService().complete();

        getTransactionService().begin();
        Thread.sleep(200);

        List<String> jobs = schedulerService.getJobs();
        assertNotNull(jobs);
        assertEquals(1, jobs.size());
        assertEquals("2", jobs.get(0));

        TestUtil.createSessionOn(getSessionAccessor(), getSessionService(), defaultTenant);

        jobs = schedulerService.getJobs();
        assertNotNull(jobs);
        assertEquals(1, jobs.size());
        assertEquals("1", jobs.get(0));

        schedulerService.deleteJobs();

        // delete tenant1 groups
        changeToTenant1();
        schedulerService.deleteJobs();

        TestUtil.createSessionOn(getSessionAccessor(), getSessionService(), defaultTenant);
        getTransactionService().complete();
    }

    // test is too long and just test quartz...
    // @Test
    // public void should_pause_and_resume_jobs_of_a_tenant_reexecute_all_missed_job() throws Exception {
    // IncrementItselfJob.reset();
    // final String jobName = "ReleaseWaitersJob";
    // Date now = new Date();
    // SJobDescriptor jobDescriptor = BuilderFactory.get(SJobDescriptorBuilderFactory.class)
    // .createNewInstance(IncrementItselfJob.class.getName(), jobName + "1").done();
    // List<SJobParameter> parameters = new ArrayList<SJobParameter>();
    // parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobName", jobName).done());
    // parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("jobKey", "1").done());
    // Trigger trigger = new UnixCronTrigger("events", now, 10, "0/1 * * * * ?", MisfireRestartPolicy.ALL);
    //
    // // trigger it
    // getTransactionService().begin();
    // schedulerService.schedule(jobDescriptor, parameters, trigger);
    // getTransactionService().complete();
    // // pause
    // getTransactionService().begin();
    // schedulerService.pauseJobs(defaultTenantId);
    // getTransactionService().complete();
    // Thread.sleep(2000);
    // // resume after 3s
    // getTransactionService().begin();
    // schedulerService.resumeJobs(defaultTenantId);
    // getTransactionService().complete();
    // Thread.sleep(2000);
    //
    // // there should be more than 5 execution after 5sec because 3 jobs had to execute themselves before
    // assertTrue(IncrementItselfJob.getValue() > 2);
    // }

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
