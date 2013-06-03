package org.bonitasoft.engine.scheduler.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.builders.SEventBuilder;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.SLogBuilder;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.scheduler.SSchedulerException;
import org.bonitasoft.engine.scheduler.SchedulerExecutor;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.Trigger;
import org.bonitasoft.engine.scheduler.builder.JobLogBuilder;
import org.bonitasoft.engine.scheduler.builder.SchedulerBuilderAccessor;
import org.bonitasoft.engine.scheduler.builder.SchedulerLogBuilder;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;

public class SchedulerImplTest {

    SchedulerService schedulerService;

    SchedulerExecutor schedulerExecutor;

    @Before
    public void setUp() throws Exception {
        schedulerExecutor = mock(SchedulerExecutor.class);
        SchedulerBuilderAccessor builderAccessor = mock(SchedulerBuilderAccessor.class);
        QueriableLoggerService queriableLogService = mock(QueriableLoggerService.class);
        TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        EventService eventService = mock(EventService.class);
        Recorder recorder = mock(Recorder.class);
        ReadPersistenceService readPersistenceService = mock(ReadPersistenceService.class);
        TransactionService transactionService = mock(TransactionService.class);
        SessionAccessor sessionAccessor = mock(SessionAccessor.class);
        SessionService sessionService = mock(SessionService.class);
        JobTruster jobTruster = mock(JobTruster.class);

        SEventBuilder sEventBuilder = mock(SEventBuilder.class);
        when(eventService.getEventBuilder()).thenReturn(sEventBuilder);
        when(sEventBuilder.createNewInstance(anyString())).thenReturn(sEventBuilder);
        when(sEventBuilder.createInsertEvent(anyString())).thenReturn(sEventBuilder);
        when(sEventBuilder.setObject(any(Object.class))).thenReturn(sEventBuilder);

        JobLogBuilder jobLogBuilder = mock(JobLogBuilder.class);
        when(builderAccessor.getJobLogBuilder()).thenReturn(jobLogBuilder);

        SchedulerLogBuilder schedulerLogBuilder = mock(SchedulerLogBuilder.class);
        when(builderAccessor.getSchedulerLogBuilder()).thenReturn(schedulerLogBuilder);

        SLogBuilder sLogBuilder = mock(SLogBuilder.class);

        when(schedulerLogBuilder.createNewInstance()).thenReturn(sLogBuilder);

        SQueriableLog sQueriableLog = mock(SQueriableLog.class);
        when(jobLogBuilder.done()).thenReturn(sQueriableLog);

        when(jobLogBuilder.createNewInstance()).thenReturn(sLogBuilder);
        when(sLogBuilder.actionStatus(anyInt())).thenReturn(sLogBuilder);
        when(sLogBuilder.severity(any(SQueriableLogSeverity.class))).thenReturn(sLogBuilder);
        when(sLogBuilder.rawMessage(anyString())).thenReturn(sLogBuilder);

        when(queriableLogService.isLoggable(anyString(), any(SQueriableLogSeverity.class))).thenReturn(false);

        schedulerService = new SchedulerImpl(schedulerExecutor, builderAccessor, queriableLogService, logger, eventService, recorder, readPersistenceService,
                transactionService, sessionAccessor, sessionService, jobTruster);
    }

    @Test
    public void testIsStarted() throws Exception {
        when(schedulerExecutor.isStarted()).thenReturn(true);
        assertTrue(schedulerService.isStarted());
    }

    @Test
    public void testIsShutDown() throws Exception {
        when(schedulerExecutor.isShutdown()).thenReturn(false);
        assertFalse(schedulerService.isShutdown());
    }

    @Test(expected = SSchedulerException.class)
    public void testCannotScheduleANullJob() throws Exception {
        Trigger trigger = mock(Trigger.class);
        schedulerService.schedule(null, trigger);
    }

}
