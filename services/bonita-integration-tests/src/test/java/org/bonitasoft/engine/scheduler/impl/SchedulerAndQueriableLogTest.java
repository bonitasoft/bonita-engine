package org.bonitasoft.engine.scheduler.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.CommonServiceTest;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.builder.SJobDescriptorBuilderFactory;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.trigger.OneExecutionTrigger;
import org.bonitasoft.engine.scheduler.trigger.Trigger;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.bonitasoft.engine.test.util.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SchedulerAndQueriableLogTest extends CommonServiceTest {

    private static final String JOB_EXECUTED = "JOB_EXECUTED";

    private static final String JOB_CREATED = "JOB_CREATED";

    private final int SLEEP_TIME = 1000;

    private static SchedulerService schedulerService;

    private static QueriableLoggerService queriableLoggerService;

    static {
        queriableLoggerService = getServicesBuilder().buildQueriableLogger("syncQueriableLoggerService");
        schedulerService = getServicesBuilder().buildSchedulerService();
    }

    @Before
    public void setUp() throws Exception {
        TestUtil.startScheduler(schedulerService);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        TestUtil.closeTransactionIfOpen(getTransactionService());
        TestUtil.stopScheduler(schedulerService, getTransactionService());
    }

    private FilterOption createFilterOption(final String fieldName, final String fieldValue) {
        return new FilterOption(SQueriableLog.class, fieldName, fieldValue);
    }

    @Test
    public void testNotLogIfTransactionIsRolledBack() throws Exception {
        // schedule
        getTransactionService().begin();

        final Date now = new Date();
        final SJobDescriptor jobDescriptor = BuilderFactory.get(SJobDescriptorBuilderFactory.class)
                .createNewInstance("org.bonitasoft.engine.scheduler.job.ThrowsExceptionJob.ThrowsExceptionJob()", "ThrowExceptionJob")
                .setDescription("a job that throws exception").done();
        final Trigger trigger = new OneExecutionTrigger("eventsLog", now, 10);
        schedulerService.schedule(jobDescriptor, trigger);
        getTransactionService().setRollbackOnly();
        getTransactionService().complete();

        Thread.sleep(SLEEP_TIME);

        // query log schedule
        getTransactionService().begin();
        List<FilterOption> filters = new ArrayList<FilterOption>(2);
        filters.add(createFilterOption("actionType", JOB_CREATED));
        filters.add(createFilterOption("actionScope", "exception:exception"));
        List<SQueriableLog> logs = queriableLoggerService.searchLogs(new QueryOptions(0, 10, null, filters, null));
        assertEquals(0, logs.size());
        getTransactionService().complete();

        Thread.sleep(SLEEP_TIME);

        // query execute job
        getTransactionService().begin();

        filters = new ArrayList<FilterOption>(2);
        filters.add(createFilterOption("actionType", JOB_EXECUTED));
        filters.add(createFilterOption("actionScope", "exception:exception"));
        logs = queriableLoggerService.searchLogs(new QueryOptions(0, 10, null, filters, null));
        assertEquals(0, logs.size());
        getTransactionService().complete();
    }

}
