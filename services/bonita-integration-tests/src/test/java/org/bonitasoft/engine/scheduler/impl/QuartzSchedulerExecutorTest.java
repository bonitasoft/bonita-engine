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
import org.bonitasoft.engine.platform.STenantNotFoundException;
import org.bonitasoft.engine.platform.model.builder.STenantBuilder;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.builder.SJobParameterBuilder;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.scheduler.job.IncrementItselfJob;
import org.bonitasoft.engine.scheduler.job.IncrementVariableJobWithMultiTenancy;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobParameter;
import org.bonitasoft.engine.scheduler.trigger.OneExecutionTrigger;
import org.bonitasoft.engine.scheduler.trigger.RepeatXTimesTrigger;
import org.bonitasoft.engine.scheduler.trigger.Trigger;
import org.bonitasoft.engine.scheduler.trigger.UnixCronTrigger;
import org.bonitasoft.engine.test.util.PlatformUtil;
import org.bonitasoft.engine.test.util.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class QuartzSchedulerExecutorTest extends CommonServiceTest {

    public final String DEFAULT_TENANT_STATUS = "DEACTIVATED";

    private static final SchedulerService schedulerService;

    private static STenantBuilder tenantBuilder;

    private long tenant1;

    private final VariableStorage storage = VariableStorage.getInstance();

    static {
        schedulerService = getServicesBuilder().buildSchedulerService();
        tenantBuilder = getServicesBuilder().buildTenantBuilder();
    }

    @Before
    public void setUp() throws Exception {
        tenant1 = PlatformUtil.createTenant(getTransactionService(), getPlatformService(), tenantBuilder, "tenant1", PlatformUtil.DEFAULT_CREATED_BY,
                PlatformUtil.DEFAULT_TENANT_STATUS);
        TestUtil.startScheduler(schedulerService);
        getTransactionService().begin();
        changeToDefaultTenant();
        getTransactionService().complete();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        TestUtil.closeTransactionIfOpen(getTransactionService());
        TestUtil.stopScheduler(schedulerService, getTransactionService());
        storage.clear();
        PlatformUtil.deleteTenant(getTransactionService(), getPlatformService(), tenant1);
    }

    /**
     * @author Baptiste Mesta
     */
    private final class WaitForIncrementJobToHaveValue extends WaitFor {

        private final int value;

        /**
         * @param timeout
         * @param value
         */
        private WaitForIncrementJobToHaveValue(final int timeout, final int value) {
            super(10, timeout);
            this.value = value;
        }

        @Override
        boolean check() {
            return IncrementItselfJob.getValue() == value;
        }
    }

    public abstract class WaitFor {

        private final int timeout;

        private final int repeatEach;

        public WaitFor(final int repeatEach, final int timeout) {
            assertTrue("timeout is not big enough", repeatEach < timeout);
            this.repeatEach = repeatEach;
            this.timeout = timeout;
        }

        public boolean waitFor() throws InterruptedException {
            final long limit = new Date().getTime() + timeout;
            while (new Date().getTime() < limit) {
                Thread.sleep(repeatEach);
                if (check()) {
                    return true;
                }
            }
            return check();
        }

        abstract boolean check();

    }

    @Test
    public void testCheckShutdownScheduler() throws Exception {
        schedulerService.stop();
        assertTrue(schedulerService.isStopped());
    }

    @Test
    public void testCheckShutdownSchedulerTwice() throws Exception {
        schedulerService.stop();
        schedulerService.stop();
    }

    @Test
    public void testCanRestartTheSchedulerAfterShutdown() throws Exception {
        schedulerService.stop();
        assertTrue(schedulerService.isStopped());
        schedulerService.start();
        assertTrue(schedulerService.isStarted());
    }

    @Test(expected = SSchedulerException.class)
    public void testCannotStartASchedulerWhichIsAlreadyStarted() throws Exception {
        schedulerService.start();
    }

    @Test(expected = SSchedulerException.class)
    public void testExecuteAJobWithANullTrigger() throws Exception {
        final String variableName = "myVar";

        final SJobDescriptor jobDescriptor = schedulerService.getJobDescriptorBuilder()
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        final SJobParameterBuilder jobParameterBuilder = schedulerService.getJobParameterBuilder();
        parameters.add(jobParameterBuilder.createNewInstance("jobName", "job").done());
        parameters.add(jobParameterBuilder.createNewInstance("variableName", variableName).done());
        parameters.add(jobParameterBuilder.createNewInstance("throwExceptionAfterNIncrements", -1).done());

        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, null);
        getTransactionService().complete();
    }

    @Test
    public void testExecuteOnceAJob() throws Exception {
        IncrementItselfJob.reset();
        final Date now = new Date();
        final SJobDescriptor jobDescriptor = schedulerService.getJobDescriptorBuilder()
                .createNewInstance(IncrementItselfJob.class.getName(), "IncrementVariableJob").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        final SJobParameterBuilder jobParameterBuilder = schedulerService.getJobParameterBuilder();
        parameters.add(jobParameterBuilder.createNewInstance("jobName", "testExecuteOnceAJob").done());
        final Trigger trigger = new OneExecutionTrigger("events", now, 10);

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

    @Ignore("This test is too long, should be deported to less frequently run test suite")
    @Test
    public void testExecuteAVeryOldJob() throws Exception {
        final Date epoch = new Date(0);
        final String variableName = "testExecuteAVeryOldJob";
        final SJobDescriptor jobDescriptor = schedulerService.getJobDescriptorBuilder()
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        final SJobParameterBuilder jobParameterBuilder = schedulerService.getJobParameterBuilder();
        parameters.add(jobParameterBuilder.createNewInstance("jobName", "testExecuteAVeryOldJob").done());
        parameters.add(jobParameterBuilder.createNewInstance("variableName", variableName).done());
        parameters.add(jobParameterBuilder.createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger = new OneExecutionTrigger("events", epoch, 10);

        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();
        final WaitFor waitFor = new WaitFor(300, 100000) {

            @Override
            boolean check() {
                final boolean equals = Integer.valueOf(1).equals(storage.getVariableValue(variableName));
                return equals;
            }
        };
        assertTrue("variable not updated", waitFor.waitFor());
    }

    @Test
    public void testDoNotExecuteAFutureJob() throws Exception {
        final Date future = new Date(System.currentTimeMillis() + 10000000);
        final String variableName = "myVar";
        final SJobDescriptor jobDescriptor = schedulerService.getJobDescriptorBuilder()
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        final SJobParameterBuilder jobParameterBuilder = schedulerService.getJobParameterBuilder();
        parameters.add(jobParameterBuilder.createNewInstance("jobName", "testDoNotExecuteAFutureJob").done());
        parameters.add(jobParameterBuilder.createNewInstance("variableName", variableName).done());
        parameters.add(jobParameterBuilder.createNewInstance("throwExceptionAfterNIncrements", -1).done());
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
        final SJobDescriptor jobDescriptor1 = schedulerService.getJobDescriptorBuilder()
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob1").done();
        final List<SJobParameter> parameters1 = new ArrayList<SJobParameter>();
        final SJobParameterBuilder jobParameterBuilder = schedulerService.getJobParameterBuilder();
        parameters1.add(jobParameterBuilder.createNewInstance("jobName", "1").done());
        parameters1.add(jobParameterBuilder.createNewInstance("variableName", variableName).done());
        parameters1.add(jobParameterBuilder.createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final SJobDescriptor jobDescriptor2 = schedulerService.getJobDescriptorBuilder()
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob2").done();
        final List<SJobParameter> parameters2 = new ArrayList<SJobParameter>();
        parameters2.add(jobParameterBuilder.createNewInstance("jobName", "2").done());
        parameters2.add(jobParameterBuilder.createNewInstance("variableName", variableName).done());
        parameters2.add(jobParameterBuilder.createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger1 = new RepeatXTimesTrigger("trigger1", now, 10, 1000, 100);
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor1, parameters1, trigger1);
        schedulerService.schedule(jobDescriptor2, parameters2, trigger1);
        getTransactionService().complete();
    }

    @Test(expected = SSchedulerException.class)
    public void testCannotUseTheSameTriggerNameAndGroupWithTwoJobs() throws Throwable {
        final Date now = new Date();
        final String variableName = "myVar";
        final SJobDescriptor jobDescriptor1 = schedulerService.getJobDescriptorBuilder()
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob1").done();
        final List<SJobParameter> parameters1 = new ArrayList<SJobParameter>();
        final SJobParameterBuilder jobParameterBuilder = schedulerService.getJobParameterBuilder();
        parameters1.add(jobParameterBuilder.createNewInstance("jobName", "1").done());
        parameters1.add(jobParameterBuilder.createNewInstance("variableName", variableName).done());
        parameters1.add(jobParameterBuilder.createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final SJobDescriptor jobDescriptor2 = schedulerService.getJobDescriptorBuilder()
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob2").done();
        final List<SJobParameter> parameters2 = new ArrayList<SJobParameter>();
        parameters2.add(jobParameterBuilder.createNewInstance("jobName", "2").done());
        parameters2.add(jobParameterBuilder.createNewInstance("variableName", variableName).done());
        parameters2.add(jobParameterBuilder.createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger1 = new RepeatXTimesTrigger("trigger1", now, 10, 1000, 100);
        final Trigger trigger2 = new RepeatXTimesTrigger("trigger1", now, 5, 80, 1000);
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor1, parameters1, trigger1);
        schedulerService.schedule(jobDescriptor2, parameters2, trigger2);
        getTransactionService().complete();
    }

    @Test(expected = SSchedulerException.class)
    public void testCannotUseTheSameJobNameInTheSameGroup() throws Throwable {
        final Date now = new Date();
        final SJobDescriptor jobDescriptor1 = schedulerService.getJobDescriptorBuilder()
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob").done();
        final List<SJobParameter> parameters1 = new ArrayList<SJobParameter>();
        final SJobParameterBuilder jobParameterBuilder = schedulerService.getJobParameterBuilder();
        parameters1.add(jobParameterBuilder.createNewInstance("jobName", "1").done());
        parameters1.add(jobParameterBuilder.createNewInstance("variableName", "first").done());
        parameters1.add(jobParameterBuilder.createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final SJobDescriptor jobDescriptor2 = schedulerService.getJobDescriptorBuilder()
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob").done();
        final List<SJobParameter> parameters2 = new ArrayList<SJobParameter>();
        parameters2.add(jobParameterBuilder.createNewInstance("jobName", "2").done());
        parameters2.add(jobParameterBuilder.createNewInstance("variableName", "second").done());
        parameters2.add(jobParameterBuilder.createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger1 = new RepeatXTimesTrigger("trigger1", now, 10, 1000, 100);
        final Trigger trigger2 = new RepeatXTimesTrigger("trigger2", now, 5, 80, 1000);
        getTransactionService().begin();

        schedulerService.schedule(jobDescriptor1, parameters1, trigger1);
        schedulerService.schedule(jobDescriptor2, parameters2, trigger2);
        getTransactionService().complete();
    }

    @Test(expected = SSchedulerException.class)
    public void testCannotUseAJobWithANullName() throws Exception {
        final Date now = new Date();
        final SJobDescriptor jobDescriptor = schedulerService.getJobDescriptorBuilder()
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", null).done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        final SJobParameterBuilder jobParameterBuilder = schedulerService.getJobParameterBuilder();
        parameters.add(jobParameterBuilder.createNewInstance("jobName", null).done());
        parameters.add(jobParameterBuilder.createNewInstance("variableName", "first").done());
        parameters.add(jobParameterBuilder.createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger1 = new RepeatXTimesTrigger("trigger1", now, 10, 1000, 100);
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger1);
        getTransactionService().complete();
    }

    @Test
    public void testCanUseAJobWithANullGroup() throws Exception {
        final Date now = new Date();
        final SJobDescriptor jobDescriptor = schedulerService.getJobDescriptorBuilder()
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        final SJobParameterBuilder jobParameterBuilder = schedulerService.getJobParameterBuilder();
        parameters.add(jobParameterBuilder.createNewInstance("jobName", "testDoNotExecuteAFutureJob").done());
        parameters.add(jobParameterBuilder.createNewInstance("variableName", "first").done());
        parameters.add(jobParameterBuilder.createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger1 = new RepeatXTimesTrigger("testCanUseAJobWithANullGroup", now, 10, 1000, 100);
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger1);
        getTransactionService().complete();
    }

    @Test(expected = SSchedulerException.class)
    public void testCannotUseAOneShotTriggerWithANullName() throws Throwable {
        final Date now = new Date();
        final SJobDescriptor jobDescriptor = schedulerService.getJobDescriptorBuilder()
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        final SJobParameterBuilder jobParameterBuilder = schedulerService.getJobParameterBuilder();
        parameters.add(jobParameterBuilder.createNewInstance("jobName", "testDoNotExecuteAFutureJob").done());
        parameters.add(jobParameterBuilder.createNewInstance("variableName", "first").done());
        parameters.add(jobParameterBuilder.createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger = new OneExecutionTrigger(null, now, 10);
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();
    }

    @Test
    public void testCanUseAOneShotTriggerWithANullGroup() throws Exception {
        final Date now = new Date();
        final SJobDescriptor jobDescriptor = schedulerService.getJobDescriptorBuilder()
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        final SJobParameterBuilder jobParameterBuilder = schedulerService.getJobParameterBuilder();
        parameters.add(jobParameterBuilder.createNewInstance("jobName", "testCanUseAOneShotTriggerWithANullGroup").done());
        parameters.add(jobParameterBuilder.createNewInstance("variableName", "first").done());
        parameters.add(jobParameterBuilder.createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger = new OneExecutionTrigger("oneshot", now, 10);
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();
    }

    @Test(expected = SSchedulerException.class)
    public void testCannotUseARepeatTriggerWithANullName() throws Throwable {
        final Date now = new Date();
        final SJobDescriptor jobDescriptor = schedulerService.getJobDescriptorBuilder()
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        final SJobParameterBuilder jobParameterBuilder = schedulerService.getJobParameterBuilder();
        parameters.add(jobParameterBuilder.createNewInstance("jobName", "1").done());
        parameters.add(jobParameterBuilder.createNewInstance("variableName", "first").done());
        parameters.add(jobParameterBuilder.createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger1 = new RepeatXTimesTrigger(null, now, 10, 1000, 100);
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger1);
        getTransactionService().complete();
    }

    @Test
    public void testCanUseARepeatTriggerWithANullGroup() throws Exception {
        final Date now = new Date();
        final SJobDescriptor jobDescriptor = schedulerService.getJobDescriptorBuilder()
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        final SJobParameterBuilder jobParameterBuilder = schedulerService.getJobParameterBuilder();
        parameters.add(jobParameterBuilder.createNewInstance("jobName", "testCanUseARepeatTriggerWithANullGroup").done());
        parameters.add(jobParameterBuilder.createNewInstance("variableName", "first").done());
        parameters.add(jobParameterBuilder.createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger1 = new RepeatXTimesTrigger("trig", now, 10, 1000, 100);
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger1);
        getTransactionService().complete();
    }

    @Test(expected = SSchedulerException.class)
    public void testCannotUseACronTriggerWithANullName() throws Throwable {
        final Date now = new Date();
        final SJobDescriptor jobDescriptor = schedulerService.getJobDescriptorBuilder()
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        final SJobParameterBuilder jobParameterBuilder = schedulerService.getJobParameterBuilder();
        parameters.add(jobParameterBuilder.createNewInstance("jobName", "1").done());
        parameters.add(jobParameterBuilder.createNewInstance("variableName", "first").done());
        parameters.add(jobParameterBuilder.createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger = new UnixCronTrigger(null, now, 10, "0/1 * * * * ?");
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();
    }

    @Test
    public void testCanUseACronTriggerWithANullGroup() throws Exception {
        final Date now = new Date();
        final SJobDescriptor jobDescriptor = schedulerService.getJobDescriptorBuilder()
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        final SJobParameterBuilder jobParameterBuilder = schedulerService.getJobParameterBuilder();
        parameters.add(jobParameterBuilder.createNewInstance("jobName", "testCanUseACronTriggerWithANullGroup").done());
        parameters.add(jobParameterBuilder.createNewInstance("variableName", "first").done());
        parameters.add(jobParameterBuilder.createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger = new UnixCronTrigger("events", now, 10, "0/1 * * * * ?");
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();
    }

    @Test
    public void testExecuteSeveralTimesAJob() throws Exception {
        final String jobName = "IncrementVariableJob1";
        final Date now = new Date();
        final SJobDescriptor jobDescriptor = schedulerService.getJobDescriptorBuilder().createNewInstance(IncrementItselfJob.class.getName(), jobName).done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        final SJobParameterBuilder jobParameterBuilder = schedulerService.getJobParameterBuilder();
        parameters.add(jobParameterBuilder.createNewInstance("jobName", jobName).done());
        final Trigger trigger = new RepeatXTimesTrigger("events", now, 10, 3, 100);

        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();

        IncrementItselfJob.reset();
        final WaitForIncrementJobToHaveValue wf = new WaitForIncrementJobToHaveValue(1000, IncrementItselfJob.getValue() + 1);
        assertTrue(wf.waitFor());
        Thread.sleep(500);
        assertFalse(wf.waitFor());
    }

    @Test(expected = SSchedulerException.class)
    public void testCannotUseRepeatTriggerDueToNegativeCount() throws Throwable {
        final Date now = new Date();
        final String variableName = "myVar";
        final SJobDescriptor jobDescriptor = schedulerService.getJobDescriptorBuilder()
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob1").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        final SJobParameterBuilder jobParameterBuilder = schedulerService.getJobParameterBuilder();
        parameters.add(jobParameterBuilder.createNewInstance("jobName", "job").done());
        parameters.add(jobParameterBuilder.createNewInstance("variableName", variableName).done());
        parameters.add(jobParameterBuilder.createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger = new RepeatXTimesTrigger("events", now, 10, -2, 100);
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();
    }

    @Test
    public void testCanUseRepeatTriggerDueToInfiniteCount() throws Exception {
        final Date now = new Date();
        final String variableName = "myVar";

        final SJobDescriptor jobDescriptor = schedulerService.getJobDescriptorBuilder()
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        final SJobParameterBuilder jobParameterBuilder = schedulerService.getJobParameterBuilder();
        parameters.add(jobParameterBuilder.createNewInstance("jobName", "job").done());
        parameters.add(jobParameterBuilder.createNewInstance("variableName", variableName).done());
        parameters.add(jobParameterBuilder.createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger = new RepeatXTimesTrigger("events", now, 10, -1, 100);
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();
    }

    @Test(expected = SSchedulerException.class)
    public void testCannotUseRepeatTriggerDueToZeroInterval() throws Throwable {
        final Date now = new Date();
        final String variableName = "myVar";
        final SJobDescriptor jobDescriptor = schedulerService.getJobDescriptorBuilder()
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        final SJobParameterBuilder jobParameterBuilder = schedulerService.getJobParameterBuilder();
        parameters.add(jobParameterBuilder.createNewInstance("jobName", "job").done());
        parameters.add(jobParameterBuilder.createNewInstance("variableName", variableName).done());
        parameters.add(jobParameterBuilder.createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger = new RepeatXTimesTrigger("events", now, 10, 1000, 0);
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();
    }

    @Test(expected = SSchedulerException.class)
    public void testCannotUseRepeatTriggerDueToNegativeInterval() throws Throwable {
        final Date now = new Date();
        final String variableName = "myVar";
        final SJobDescriptor jobDescriptor = schedulerService.getJobDescriptorBuilder()
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        final SJobParameterBuilder jobParameterBuilder = schedulerService.getJobParameterBuilder();
        parameters.add(jobParameterBuilder.createNewInstance("jobName", "job").done());
        parameters.add(jobParameterBuilder.createNewInstance("variableName", variableName).done());
        parameters.add(jobParameterBuilder.createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger = new RepeatXTimesTrigger("events", now, 10, 1000, -1);
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();
    }

    @Test
    public void testCannotUseRepeatTriggerDueToIntervalOfOneMS() throws Exception {
        final Date now = new Date();
        final String variableName = "myVar";
        final SJobDescriptor jobDescriptor = schedulerService.getJobDescriptorBuilder()
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        final SJobParameterBuilder jobParameterBuilder = schedulerService.getJobParameterBuilder();
        parameters.add(jobParameterBuilder.createNewInstance("jobName", "job").done());
        parameters.add(jobParameterBuilder.createNewInstance("variableName", variableName).done());
        parameters.add(jobParameterBuilder.createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger = new RepeatXTimesTrigger("events", now, 10, 1000, 1);
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();
    }

    @Test
    public void testCanDefineNegativePriorityOfAJob() throws Exception {
        final Date now = new Date();
        final String variableName = "testCanDefineNegativePriorityOfAJob";
        final SJobDescriptor jobDescriptor = schedulerService.getJobDescriptorBuilder()
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        final SJobParameterBuilder jobParameterBuilder = schedulerService.getJobParameterBuilder();
        parameters.add(jobParameterBuilder.createNewInstance("jobName", "job").done());
        parameters.add(jobParameterBuilder.createNewInstance("variableName", variableName).done());
        parameters.add(jobParameterBuilder.createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger = new OneExecutionTrigger("events", now, -10);
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();
    }

    @Test
    public void testExecuteAJobInACron() throws Exception {
        final String jobName = "IncrementItselfJob";
        final Date now = new Date();
        final SJobDescriptor jobDescriptor = schedulerService.getJobDescriptorBuilder().createNewInstance(IncrementItselfJob.class.getName(), jobName).done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        final SJobParameterBuilder jobParameterBuilder = schedulerService.getJobParameterBuilder();
        parameters.add(jobParameterBuilder.createNewInstance("jobName", jobName).done());
        final Trigger trigger = new UnixCronTrigger("events", now, 10, "0/1 * * * * ?");

        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();

        final int value = IncrementItselfJob.getValue();

        Thread.sleep(2000);

        final int newValue = IncrementItselfJob.getValue();
        final int delta = newValue - value;
        assertTrue("expected 1 or 2, 3 execution in 2 seconds, got: " + delta, delta == 1 || delta == 2 || delta == 3);
    }

    @Test
    @Ignore("Rewrite it so that it is more tolerant on Quartz imprecision (see test above)")
    public void testExecuteAJobInACronAndStopIt() throws Exception {
        final Date now = new Date();
        final String variableName = "testExecuteAJobInACronAndStopIt";
        final SJobDescriptor jobDescriptor = schedulerService.getJobDescriptorBuilder()
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        final SJobParameterBuilder jobParameterBuilder = schedulerService.getJobParameterBuilder();
        parameters.add(jobParameterBuilder.createNewInstance("jobName", "job").done());
        parameters.add(jobParameterBuilder.createNewInstance("variableName", variableName).done());
        parameters.add(jobParameterBuilder.createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger = new UnixCronTrigger("events", now, 10, "0/1 * * * * ?", new Date(now.getTime() + 2000));

        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();

        Integer value;
        final int timeout = 5000;
        final Date time = new Date();
        do {
            value = (Integer) storage.getVariableValue(variableName);
            if (value == null) {
                value = 0;
            }
            Thread.sleep(50);
        } while (time.getTime() + timeout > System.currentTimeMillis() && value != 3);

        assertEquals(Integer.valueOf(3), value);
        Thread.sleep(2000);
        assertEquals(3, storage.getVariableValue(variableName));
    }

    /*
     * @Test(expected = SSchedulerException.class)
     * public void testCannotExecuteACronTriggerDueToExpressionMisconfiguration() throws Throwable {
     * final Date now = new Date();
     * final String variableName = "myVar";
     * final SJobDescriptor jobDescriptor = schedulerService.getJobDescriptorBuilder()
     * .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "IncrementVariableJob").done();
     * final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
     * final JobParameterBuilder jobParameterBuilder = schedulerService.getJobParameterBuilder();
     * parameters.add(jobParameterBuilder.createNewInstance("jobName", "job").done());
     * parameters.add(jobParameterBuilder.createNewInstance("variableName", variableName).done());
     * parameters.add(jobParameterBuilder.createNewInstance("throwExceptionAfterNIncrements", -1).done());
     * final Trigger trigger = new UnixCronTrigger("events", now, 10, "1 * * * * * *");
     * getTransactionService().begin();
     * schedulerService.schedule(jobDescriptor, parameters, trigger);
     * getTransactionService().complete();
     * }
     * @Test
     * public void testDoNotThrowAnExceptionWhenDeletingAnUnknownJob() throws Exception {
     * final boolean deleted = schedulerService.delete("MyJob");
     * assertFalse(deleted);
     * }
     */
    @Test
    public void testDeleteAJob() throws Exception {
        final String jobName = "testDeleteAJob";
        final Date now = new Date();
        final SJobDescriptor jobDescriptor = schedulerService.getJobDescriptorBuilder().createNewInstance(IncrementItselfJob.class.getName(), jobName).done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        final SJobParameterBuilder jobParameterBuilder = schedulerService.getJobParameterBuilder();
        parameters.add(jobParameterBuilder.createNewInstance("jobName", jobName).done());
        final Trigger trigger = new RepeatXTimesTrigger("events", now, 10, 1000, 100);

        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();

        final WaitForIncrementJobToHaveValue wf = new WaitForIncrementJobToHaveValue(1200, IncrementItselfJob.getValue() + 1);
        assertTrue(wf.waitFor());
        getTransactionService().begin();
        schedulerService.delete(jobName);
        getTransactionService().complete();
        // may be in execution so the last job is running
        Thread.sleep(200);
        IncrementItselfJob.reset();
        Thread.sleep(500);
        assertTrue(0 == IncrementItselfJob.getValue());
    }

    @Test
    public void testDoNotThrowAnExceptionWhenDeletingAnUnknownGroupOfJobs() throws Exception {
        getTransactionService().begin();
        final boolean deleted = schedulerService.delete("MyJob");
        getTransactionService().complete();
        assertFalse(deleted);
    }

    @Test
    public void testDeleteAGroupOfJobs() throws Exception {
        final Date now = new Date();

        final SJobDescriptor jobDescriptor1 = schedulerService.getJobDescriptorBuilder().createNewInstance(IncrementItselfJob.class.getName(), "job1").done();
        final List<SJobParameter> parameters1 = new ArrayList<SJobParameter>();
        final SJobParameterBuilder jobParameterBuilder = schedulerService.getJobParameterBuilder();
        parameters1.add(jobParameterBuilder.createNewInstance("jobName", "1").done());

        final SJobDescriptor jobDescriptor2 = schedulerService.getJobDescriptorBuilder().createNewInstance(IncrementItselfJob.class.getName(), "job2").done();
        final List<SJobParameter> parameters2 = new ArrayList<SJobParameter>();
        parameters2.add(jobParameterBuilder.createNewInstance("jobName", "2").done());

        final Trigger trigger1 = new RepeatXTimesTrigger("trigger1", now, 10, 10000, 1000);
        final Trigger trigger2 = new RepeatXTimesTrigger("trigger2", now, 10, 10000, 1000);

        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor1, parameters1, trigger1);
        getTransactionService().complete();
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor2, parameters2, trigger2);
        assertEquals(2, schedulerService.getJobs().size());
        getTransactionService().complete();
        getTransactionService().begin();
        schedulerService.deleteJobs();
        assertEquals(0, schedulerService.getJobs().size());
        getTransactionService().complete();
    }

    @Test
    public void testMultiTenancy() throws Exception {
        VariableStorage.clearAll();
        IncrementVariableJobWithMultiTenancy.setSessionAccessor(getSessionAccessor());
        final Date now = new Date(System.currentTimeMillis() + 10000000);
        final String variableName = "testMultiTenancy";
        final SJobDescriptor jobDescriptor = schedulerService.getJobDescriptorBuilder()
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJobWithMultiTenancy", "IncrementVariableJob").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        final SJobParameterBuilder jobParameterBuilder = schedulerService.getJobParameterBuilder();
        parameters.add(jobParameterBuilder.createNewInstance("jobName", "testExecuteOnceAJob").done());
        parameters.add(jobParameterBuilder.createNewInstance("variableName", variableName).done());
        parameters.add(jobParameterBuilder.createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger = new OneExecutionTrigger("events", now, 10);
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();
        Thread.sleep(2000);
        final long defaultTenant = getTenantIdFromSession();
        assertNull(VariableStorage.getInstance(defaultTenant).getVariableValue(variableName));
        assertNull(VariableStorage.getInstance(tenant1).getVariableValue(variableName));

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
        VariableStorage.clearAll();
        IncrementVariableJobWithMultiTenancy.setSessionAccessor(getSessionAccessor());
        final Date now = new Date(System.currentTimeMillis());
        final String variableName = "testMultiTenancy";
        final SJobDescriptor jobDescriptor = schedulerService.getJobDescriptorBuilder()
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJobWithMultiTenancy", "IncrementVariableJob").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        final SJobParameterBuilder jobParameterBuilder = schedulerService.getJobParameterBuilder();
        parameters.add(jobParameterBuilder.createNewInstance("jobName", "testExecuteOnceAJob").done());
        parameters.add(jobParameterBuilder.createNewInstance("variableName", variableName).done());
        parameters.add(jobParameterBuilder.createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final Trigger trigger = new OneExecutionTrigger("events", now, 10);

        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();
        Thread.sleep(2000);

        final long defaultTenant = getTenantIdFromSession();
        assertNotNull(VariableStorage.getInstance(defaultTenant).getVariableValue(variableName));
        assertNull(VariableStorage.getInstance(tenant1).getVariableValue(variableName));

        getTransactionService().begin();
        final List<String> jobs = schedulerService.getJobs();
        getTransactionService().complete();
        assertEquals(0, jobs.size());// job id completed
    }

    private long getTenantIdFromSession() throws Exception {
        return getSessionAccessor().getTenantId();
    }

    @Test
    public void testCannotDeleteAJobFromAnotherTenant() throws Exception {
        final Date now = new Date();
        final String jobName = "IncrementItselfJob";
        final SJobDescriptor jobDescriptor = schedulerService.getJobDescriptorBuilder()
                .createNewInstance(IncrementItselfJob.class.getName(), "IncrementItselfJob").done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();
        final SJobParameterBuilder jobParameterBuilder = schedulerService.getJobParameterBuilder();
        parameters.add(jobParameterBuilder.createNewInstance("jobName", "job").done());
        final Trigger trigger1 = new RepeatXTimesTrigger("events", now, 10, 100, 100);

        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor, parameters, trigger1);
        getTransactionService().complete();

        Thread.sleep(200);

        final WaitForIncrementJobToHaveValue wf = new WaitForIncrementJobToHaveValue(1000, IncrementItselfJob.getValue() + 1);
        assertTrue(wf.waitFor());

        getTransactionService().begin();
        // change tenant
        final long defaultTenant = getTenantIdFromSession();
        changeToTenant1();

        schedulerService.delete(jobName);

        TestUtil.createSessionOn(getSessionAccessor(), getSessionService(), defaultTenant);
        getTransactionService().complete();

        // may be in execution so the last job is running
        Thread.sleep(500);
        assertFalse(wf.waitFor());
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
        final SJobDescriptor jobDescriptor1 = schedulerService.getJobDescriptorBuilder()
                .createNewInstance(IncrementItselfJob.class.getName(), "IncrementVariableJob1").done();
        final List<SJobParameter> parameters1 = new ArrayList<SJobParameter>();
        final SJobParameterBuilder jobParameterBuilder = schedulerService.getJobParameterBuilder();
        parameters1.add(jobParameterBuilder.createNewInstance("jobName", jobName1).done());
        final Trigger trigger1 = new RepeatXTimesTrigger("event1", now, 10, 1000, 100);
        final SJobDescriptor jobDescriptor2 = schedulerService.getJobDescriptorBuilder()
                .createNewInstance(IncrementItselfJob.class.getName(), "IncrementVariableJob2").done();
        final List<SJobParameter> parameters2 = new ArrayList<SJobParameter>();
        parameters2.add(jobParameterBuilder.createNewInstance("jobName", jobName2).done());
        final Trigger trigger2 = new RepeatXTimesTrigger("event2", now, 10, 1000, 100);
        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor1, parameters1, trigger1);
        getTransactionService().complete();
        Thread.sleep(50);

        getTransactionService().begin();
        schedulerService.schedule(jobDescriptor2, parameters2, trigger2);
        getTransactionService().complete();
        Thread.sleep(500);

        getTransactionService().begin();
        // change tenant
        final long defaultTenant = getTenantIdFromSession();
        changeToTenant1();

        schedulerService.deleteJobs();
        Thread.sleep(200);
        final WaitForIncrementJobToHaveValue wf = new WaitForIncrementJobToHaveValue(1000, IncrementItselfJob.getValue() + 2);
        assertTrue(wf.waitFor());

        TestUtil.createSessionOn(getSessionAccessor(), getSessionService(), defaultTenant);
        getTransactionService().complete();
    }

    @Test
    public void testOnlyGetTenantJobs() throws Exception {
        final Date now = new Date();
        final String variableName = "myVar";
        final SJobDescriptor jobDescriptor1 = schedulerService.getJobDescriptorBuilder()
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "1").done();
        final List<SJobParameter> parameters1 = new ArrayList<SJobParameter>();
        final SJobParameterBuilder jobParameterBuilder = schedulerService.getJobParameterBuilder();
        parameters1.add(jobParameterBuilder.createNewInstance("jobName", "1").done());
        parameters1.add(jobParameterBuilder.createNewInstance("variableName", variableName).done());
        parameters1.add(jobParameterBuilder.createNewInstance("throwExceptionAfterNIncrements", -1).done());
        final SJobDescriptor jobDescriptor2 = schedulerService.getJobDescriptorBuilder()
                .createNewInstance("org.bonitasoft.engine.scheduler.job.IncrementVariableJob", "2").done();
        final List<SJobParameter> parameters2 = new ArrayList<SJobParameter>();
        parameters2.add(jobParameterBuilder.createNewInstance("jobName", "2").done());
        parameters2.add(jobParameterBuilder.createNewInstance("variableName", variableName).done());
        parameters2.add(jobParameterBuilder.createNewInstance("throwExceptionAfterNIncrements", -1).done());
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

}
