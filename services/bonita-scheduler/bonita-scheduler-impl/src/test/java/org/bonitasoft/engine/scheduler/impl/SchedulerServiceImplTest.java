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
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.SLogBuilder;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.JobTruster;
import org.bonitasoft.engine.scheduler.SchedulerExecutor;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.builder.SJobQueriableLogBuilder;
import org.bonitasoft.engine.scheduler.builder.SSchedulerBuilderAccessor;
import org.bonitasoft.engine.scheduler.builder.SSchedulerQueriableLogBuilder;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorCreationException;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.trigger.Trigger;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;

public class SchedulerServiceImplTest {

    SchedulerService schedulerService;

    SchedulerExecutor schedulerExecutor;

    JobService jobService;

    @Before
    public void setUp() {
        schedulerExecutor = mock(SchedulerExecutor.class);
        jobService = mock(JobService.class);

        SSchedulerBuilderAccessor builderAccessor = mock(SSchedulerBuilderAccessor.class);
        QueriableLoggerService queriableLogService = mock(QueriableLoggerService.class);
        TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        EventService eventService = mock(EventService.class);
        TransactionService transactionService = mock(TransactionService.class);
        SessionAccessor sessionAccessor = mock(SessionAccessor.class);
        SessionService sessionService = mock(SessionService.class);
        JobTruster jobTruster = mock(JobTruster.class);

        SEventBuilder sEventBuilder = mock(SEventBuilder.class);
        when(eventService.getEventBuilder()).thenReturn(sEventBuilder);
        when(sEventBuilder.createNewInstance(anyString())).thenReturn(sEventBuilder);
        when(sEventBuilder.createInsertEvent(anyString())).thenReturn(sEventBuilder);
        when(sEventBuilder.setObject(any(Object.class))).thenReturn(sEventBuilder);

        SJobQueriableLogBuilder jobLogBuilder = mock(SJobQueriableLogBuilder.class);
        when(builderAccessor.getSJobQueriableLogBuilder()).thenReturn(jobLogBuilder);

        SSchedulerQueriableLogBuilder schedulerLogBuilder = mock(SSchedulerQueriableLogBuilder.class);
        when(builderAccessor.getSSchedulerQueriableLogBuilder()).thenReturn(schedulerLogBuilder);

        SLogBuilder sLogBuilder = mock(SLogBuilder.class);
        when(schedulerLogBuilder.createNewInstance()).thenReturn(sLogBuilder);

        SQueriableLog sQueriableLog = mock(SQueriableLog.class);
        when(jobLogBuilder.done()).thenReturn(sQueriableLog);

        when(jobLogBuilder.createNewInstance()).thenReturn(sLogBuilder);
        when(sLogBuilder.actionStatus(anyInt())).thenReturn(sLogBuilder);
        when(sLogBuilder.severity(any(SQueriableLogSeverity.class))).thenReturn(sLogBuilder);
        when(sLogBuilder.rawMessage(anyString())).thenReturn(sLogBuilder);

        when(queriableLogService.isLoggable(anyString(), any(SQueriableLogSeverity.class))).thenReturn(false);

        schedulerService = new SchedulerServiceImpl(schedulerExecutor, builderAccessor, jobService, queriableLogService, logger, eventService,
                transactionService, sessionAccessor, sessionService, jobTruster);
    }

    @Test
    public void isStarted() throws Exception {
        when(schedulerExecutor.isStarted()).thenReturn(true);
        assertTrue(schedulerService.isStarted());
    }

    @Test
    public void isShutDown() throws Exception {
        when(schedulerExecutor.isShutdown()).thenReturn(false);
        assertFalse(schedulerService.isShutdown());
    }

    @Test(expected = SSchedulerException.class)
    public void cannotScheduleANullJob() throws Exception {
        Trigger trigger = mock(Trigger.class);
        when(jobService.createJobDescriptor(any(SJobDescriptor.class), any(Long.class))).thenThrow(new SJobDescriptorCreationException(""));
        schedulerService.schedule(null, trigger);
    }

}
