package org.bonitasoft.engine.handler;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
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

    @InjectMocks
    private SchedulerServiceRestartHandler handler;

    @Test
    public void executeHandleCallsSchedulerServiceToRescheduleErroneousTriggers() throws SBonitaException {
        handler.execute();

        verify(schedulerService).rescheduleErroneousTriggers();
    }

    @Test(expected = SBonitaException.class)
    public void executeHandleThrowsExceptionIfSchedulerServiceThrowsException() throws SBonitaException {
        doThrow(new SSchedulerException("failed")).when(schedulerService).rescheduleErroneousTriggers();

        handler.execute();
    }

}
