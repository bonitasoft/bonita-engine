package org.bonitasoft.engine.scheduler.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.CommonServiceTest;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.trigger.OneShotTrigger;
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

    @Test
    public void testLogWhenScheduleAndExecuteAJob() throws Exception {
        getTransactionService().begin();
        final Date now = new Date();
        final Trigger trigger = new OneShotTrigger("logevents", now, 10);
        final SJobDescriptor jobDescriptor = schedulerService.getJobDescriptorBuilder()
                .createNewInstance("org.bonitasoft.engine.scheduler.job.DoNothingJob", "DoNothingJob").setDescription("a job that does nothing").done();
        schedulerService.schedule(jobDescriptor, trigger);
        getTransactionService().complete();

        // query schedule
        getTransactionService().begin();

        // FIXME: this can be improve: there are other action_types to be retrieved
        final List<FilterOption> filters = new ArrayList<FilterOption>(1);
        filters.add(createFilterOption("actionType", JOB_CREATED));
        final List<SQueriableLog> logs = queriableLoggerService.searchLogs(new QueryOptions(0, 10, null, filters, null));
        checkRetrievedLog(logs, SQueriableLog.STATUS_OK, JOB_CREATED);

        getTransactionService().complete();
    }

    private FilterOption createFilterOption(final String fieldName, final String fieldValue) {
        return new FilterOption(SQueriableLog.class, fieldName, fieldValue);
    }

    private void checkRetrievedLog(final List<SQueriableLog> logs, final int state, final String actionType) {
        assertEquals(1, logs.size());
        assertEquals(state, logs.get(0).getActionStatus());
        assertEquals(actionType, logs.get(0).getActionType());
    }

    @Test
    public void testNotLogIfTransactionIsRolledBack() throws Exception {
        // schedule
        getTransactionService().begin();

        final Date now = new Date();
        final SJobDescriptor jobDescriptor = schedulerService.getJobDescriptorBuilder()
                .createNewInstance("org.bonitasoft.engine.scheduler.job.ThrowsExceptionJob.ThrowsExceptionJob()", "ThrowExceptionJob")
                .setDescription("a job that throws exception").done();
        final Trigger trigger = new OneShotTrigger("eventsLog", now, 10);
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
