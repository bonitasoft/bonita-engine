package org.bonitasoft.engine.handler;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SchedulerServiceRestartHandlerTest {

    @Mock
    private SchedulerService schedulerService;

    @Mock
    private TechnicalLoggerService technicalLoggerService;

    @InjectMocks
    private SchedulerServiceRestartHandler handler;

    @Test
    public void executeHandleCallsSchedulerServiceToRescheduleErroneousTriggers() throws SBonitaException {
        handler.execute();

        verify(schedulerService).rescheduleErroneousTriggers();
    }

    @Test
    public void executeHandleShouldLogItsJob() throws SBonitaException {
        handler.execute();

        verify(technicalLoggerService).log(eq(SchedulerServiceRestartHandler.class), any(TechnicalLogSeverity.class), anyString());
    }

    @Test(expected = SBonitaException.class)
    public void executeHandleThrowsExceptionIfSchedulerServiceThrowsException() throws SBonitaException {
        doThrow(new SSchedulerException("failed")).when(schedulerService).rescheduleErroneousTriggers();

        handler.execute();
    }

}
