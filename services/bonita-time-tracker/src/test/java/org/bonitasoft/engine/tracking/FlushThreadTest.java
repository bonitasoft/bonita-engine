package org.bonitasoft.engine.tracking;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.Test;

public class FlushThreadTest {

    @SuppressWarnings("unchecked")
    @Test
    public void should_flush_thread_flush_until_interruption() throws Exception {
        final TimeTracker timeTracker = mock(TimeTracker.class);
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final long flushIntervalInMilliSeconds = 10;

        final Clock clock = mock(Clock.class);
        when(clock.sleep(flushIntervalInMilliSeconds)).thenReturn(true).thenReturn(true).thenReturn(true).thenThrow(InterruptedException.class);
        final FlushThread flushThread = new FlushThread(clock, flushIntervalInMilliSeconds, timeTracker, logger);
        flushThread.start();
        // wait max 1 minute to not freeze CI in case of a bug
        flushThread.join(60000);
        verify(timeTracker, times(3)).flush();
        verify(clock, times(4)).sleep(flushIntervalInMilliSeconds);
    }

}
