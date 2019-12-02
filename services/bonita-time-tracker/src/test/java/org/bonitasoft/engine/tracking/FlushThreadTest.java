/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.tracking;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.Test;

public class FlushThreadTest {

    @SuppressWarnings("unchecked")
    @Test
    public void should_flush_thread_flush_until_interruption() throws Exception {
        final TimeTracker timeTracker = mock(TimeTracker.class);
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final Clock clock = mock(Clock.class);
        final FlushResult flushResult = new FlushResult(System.currentTimeMillis(), new ArrayList<>());

        when(timeTracker.getClock()).thenReturn(clock);
        when(timeTracker.getLogger()).thenReturn(logger);
        when(timeTracker.getFlushIntervalInMS()).thenReturn(0L);
        when(timeTracker.flush()).thenReturn(flushResult);
        when(clock.sleep(anyLong())).thenReturn(true).thenReturn(true).thenReturn(true)
                .thenThrow(InterruptedException.class);

        final FlushThread flushThread = new FlushThread(timeTracker);
        flushThread.start();
        // wait max 1 minute to not freeze CI in case of a bug
        flushThread.join(60000);
        verify(timeTracker, times(3)).flush();
        verify(clock, times(4)).sleep(anyLong());
    }

    @Test
    public void should_flush_thread_flush_on_latest_flush_interval() throws Exception {
        final TimeTracker timeTracker = mock(TimeTracker.class);
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        when(timeTracker.getLogger()).thenReturn(logger);
        final FlushThread flushThread = new FlushThread(timeTracker);
        final long now = 10;
        final long lastFlushTimestamp = 9;

        when(timeTracker.getFlushIntervalInMS()).thenReturn(4L);
        assertEquals(3, flushThread.getSleepTime(now, lastFlushTimestamp));

        when(timeTracker.getFlushIntervalInMS()).thenReturn(3L);
        assertEquals(2, flushThread.getSleepTime(now, lastFlushTimestamp));
    }

    @Test
    public void should_last_flush_timestamp_move_to_now() {
        final TimeTracker timeTracker = mock(TimeTracker.class);
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        when(timeTracker.getLogger()).thenReturn(logger);
        final FlushThread flushThread = new FlushThread(timeTracker);
        final long now = 10;

        when(timeTracker.flush()).thenThrow(RuntimeException.class);
        final long lastFlushTimestamp = flushThread.flush(now);
        assertEquals(now, lastFlushTimestamp);
    }

}
