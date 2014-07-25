package org.bonitasoft.engine.scheduler.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.CommonServiceTest;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.platform.STenantNotFoundException;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.builder.SJobDescriptorBuilderFactory;
import org.bonitasoft.engine.scheduler.builder.SJobParameterBuilderFactory;
import org.bonitasoft.engine.scheduler.job.ThrowsExceptionJob;
import org.bonitasoft.engine.scheduler.job.VariableStorage;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobLog;
import org.bonitasoft.engine.scheduler.model.SJobParameter;
import org.bonitasoft.engine.scheduler.trigger.OneExecutionTrigger;
import org.bonitasoft.engine.scheduler.trigger.Trigger;
import org.bonitasoft.engine.test.util.PlatformUtil;
import org.bonitasoft.engine.test.util.TestUtil;
import org.junit.After;
import org.junit.Before;

public class JobTest extends CommonServiceTest {

    public final String DEFAULT_TENANT_STATUS = "DEACTIVATED";

    private static final SchedulerService schedulerService;

    private static final JobService JOB_SERVICE;

    private long tenant1;

    private final VariableStorage storage = VariableStorage.getInstance();

    static {
        schedulerService = getServicesBuilder().buildSchedulerService();
        JOB_SERVICE = getServicesBuilder().getInstanceOf(JobService.class);
    }

    protected void changeToDefaultTenant() throws STenantNotFoundException, Exception {
        final long defaultTenant = getPlatformService().getTenantByName("default").getId();
        TestUtil.createSessionOn(getSessionAccessor(), getSessionService(), defaultTenant);
    }

    @Before
    public void setUp() throws Exception {
        tenant1 = PlatformUtil.createTenant(getTransactionService(), getPlatformService(), "tenant1", PlatformUtil.DEFAULT_CREATED_BY,
                PlatformUtil.DEFAULT_TENANT_STATUS);
        TestUtil.startScheduler(schedulerService);
        getTransactionService().begin();
        changeToDefaultTenant();
        final QueryOptions options = new QueryOptions(0, 1, null, Collections.<FilterOption> emptyList(), null);
        final List<SJobDescriptor> jobDescriptors = JOB_SERVICE.searchJobDescriptors(options);
        assertTrue(jobDescriptors.isEmpty());
        final List<SJobParameter> jobParameters = JOB_SERVICE.searchJobParameters(options);
        assertTrue(jobParameters.isEmpty());
        final List<SJobLog> jobLogs = JOB_SERVICE.searchJobLogs(options);
        assertTrue(jobLogs.isEmpty());
        getTransactionService().complete();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        getTransactionService().begin();
        schedulerService.deleteJobs();
        getTransactionService().complete();
        TestUtil.stopScheduler(schedulerService, getTransactionService());
        storage.clear();
        PlatformUtil.deleteTenant(getTransactionService(), getPlatformService(), tenant1);
    }

    // @Test: ignored before we find why it fails.? see https://bonitasoft.atlassian.net/browse/BS-9398 for the stack trace to anaylyse.
    public void retryingAFailedCronJobShouldCleanJobLogsAndDeleteJobDescriptorIfNotRecurrent() throws Exception {
        final SJobDescriptor jobDescriptor = BuilderFactory.get(SJobDescriptorBuilderFactory.class)
                .createNewInstance(ThrowsExceptionJob.class.getName(), "ThrowExceptionJob").done();

        final SJobParameter parameter = BuilderFactory.get(SJobParameterBuilderFactory.class)
                .createNewInstance(ThrowsExceptionJob.THROW_EXCEPTION, Boolean.TRUE).done();
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>(1);
        parameters.add(parameter);

        getTransactionService().begin();
        final Date now = new Date(System.currentTimeMillis() + 500);
        final Trigger trigger = new OneExecutionTrigger("logevents", now, 10);
        schedulerService.schedule(jobDescriptor, parameters, trigger);
        getTransactionService().complete();

        Thread.sleep(800);
        getTransactionService().begin();
        final List<FilterOption> filters = new ArrayList<FilterOption>(1);
        filters.add(new FilterOption(SJobDescriptor.class, "jobClassName", ThrowsExceptionJob.class.getName()));
        final QueryOptions queryOptions = new QueryOptions(0, 1, null, filters, null);
        List<SJobDescriptor> jobDescriptors = JOB_SERVICE.searchJobDescriptors(queryOptions);
        assertEquals(1, jobDescriptors.size());

        final long jobDescriptorId = jobDescriptors.get(0).getId();
        final QueryOptions options = new QueryOptions(0, 1, null, Arrays.asList(new FilterOption(SJobLog.class, "jobDescriptorId", jobDescriptorId)), null);
        List<SJobLog> jobLogs = JOB_SERVICE.searchJobLogs(options);
        try {
            assertEquals(1, jobLogs.size());
        } catch (final AssertionError e) {
            getTransactionService().complete();
            Thread.sleep(800);
            getTransactionService().begin();
            jobLogs = JOB_SERVICE.searchJobLogs(options);
            assertEquals(1, jobLogs.size());
        }
        getTransactionService().complete();

        getTransactionService().begin();
        parameters.clear();
        parameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance(ThrowsExceptionJob.THROW_EXCEPTION, Boolean.FALSE).done());
        schedulerService.executeAgain(jobDescriptorId, parameters);
        getTransactionService().complete();

        Thread.sleep(800);
        getTransactionService().begin();
        jobLogs = JOB_SERVICE.searchJobLogs(options);
        jobDescriptors = JOB_SERVICE.searchJobDescriptors(queryOptions);
        try {
            assertEquals(0, jobDescriptors.size());
        } catch (final AssertionError e) {
            getTransactionService().complete();
            Thread.sleep(800);
            getTransactionService().begin();
            jobDescriptors = JOB_SERVICE.searchJobDescriptors(queryOptions);
            jobLogs = JOB_SERVICE.searchJobLogs(options);
            assertEquals(0, jobDescriptors.size());
        }

        assertEquals(0, jobLogs.size());
        getTransactionService().complete();
    }

}
