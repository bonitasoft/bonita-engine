/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

public class TimeTrackerTest extends AbstractTimeTrackerTest {

    private TimeTracker tracker;

    @After
    public void after() {

        if (tracker != null) {
            assertFalse(tracker.isStarted());
        }
    }

    @Test
    public void should_isTrackable_returns_false_if_not_started() {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        tracker = new TimeTracker(logger, false, null, 10, 2, "rec");
        assertFalse(tracker.isTrackable("rec"));
    }

    @Test
    public void isTrackable() {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        tracker = new TimeTracker(logger, false, null, 10, 2, "rec1", "rec2");
        tracker.start();
        assertTrue(tracker.isTrackable("rec1"));
        assertTrue(tracker.isTrackable("rec2"));
        assertFalse(tracker.isTrackable("rec3"));
        tracker.stop();
    }

    @Test
    public void trackRecords() {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        tracker = new TimeTracker(logger, false, null, 10, 2, "rec1", "rec2");
        tracker.start();
        tracker.track("rec1", "rec11Desc", 100);
        tracker.track("rec1", "rec12Desc", 200);
        tracker.track("rec2", "rec2Desc", 300);
        tracker.track("inactivatedRec", "blabla", 1000);
        final Map<String, List<Record>> records = mapRecords(tracker.getRecords());
        assertEquals(2, records.size());
        assertEquals(2, records.get("rec1").size());
        assertEquals(1, records.get("rec2").size());

        final List<Record> rec1s = records.get("rec1");

        final Record rec11 = rec1s.get(0);
        assertEquals("rec1", rec11.getName());
        assertEquals("rec11Desc", rec11.getDescription());
        assertEquals(100L, rec11.getDuration());

        final Record rec12 = rec1s.get(1);
        assertEquals("rec1", rec12.getName());
        assertEquals("rec12Desc", rec12.getDescription());
        assertEquals(200L, rec12.getDuration());

        final List<Record> rec2s = records.get("rec2");
        final Record rec2 = rec2s.get(0);
        assertEquals("rec2", rec2.getName());
        assertEquals("rec2Desc", rec2.getDescription());
        assertEquals(300L, rec2.getDuration());
        tracker.stop();
    }

    @Test
    public void timestamp() throws Exception {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        tracker = new TimeTracker(logger, false, null, 10, 2, "rec1");
        tracker.start();
        tracker.track("rec1", "desc2", 100);
        Thread.sleep(2);
        tracker.track("rec1", "desc2", 200);
        final Map<String, List<Record>> records = mapRecords(tracker.getRecords());
        assertEquals(1, records.size());
        assertEquals(2, records.get("rec1").size());

        final List<Record> rec1s = records.get("rec1");

        assertTrue(rec1s.get(0).getTimestamp() < rec1s.get(1).getTimestamp());
        tracker.stop();
    }

    private Map<String, List<Record>> mapRecords(final List<Record> records) {
        final Map<String, List<Record>> result = new HashMap<String, List<Record>>();
        if (records != null) {
            for (final Record record : records) {
                final String name = record.getName();
                if (!result.containsKey(name)) {
                    result.put(name, new ArrayList<Record>());
                }
                result.get(name).add(record);
            }
        }
        return result;
    }

    @Test
    public void should_build_do_not_start_flush_thread() {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        tracker = new TimeTracker(logger, false, null, 10, 2);
        assertFalse(tracker.isFlushThreadAlive());
    }

    @Test
    public void should_start_start_flush_thread_if_startFlushThread_is_true() {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        tracker = new TimeTracker(logger, true, null, 10, 2);
        tracker.start();
        assertTrue(tracker.isFlushThreadAlive());
        tracker.stop();
    }

    @Test
    public void should_start_do_not_start_flush_thread_if_startFlushThread_is_false() {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        tracker = new TimeTracker(logger, false, null, 10, 2);
        tracker.start();
        assertFalse(tracker.isFlushThreadAlive());
        tracker.stop();
    }

    @Test
    public void should_stop_stop_flush_thread_if_running() {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        tracker = new TimeTracker(logger, false, null, 10, 2);
        tracker.start();
        tracker.stop();
        assertFalse(tracker.isFlushThreadAlive());
    }

    @Test
    public void should_stop_stop_silently_if_flush_thread_is_running() {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        tracker = new TimeTracker(logger, false, null, 10, 2);
        tracker.start();
        tracker.stop();
    }

    @Test
    public void should_flush_ignore_flush_listeners_exceptions() throws Exception {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final Clock clock = mock(Clock.class);

        final FlushEventListener listener1 = mock(FlushEventListener.class);
        final FlushEventListener listener2 = mock(FlushEventListener.class);
        final FlushEventListener listener3 = mock(FlushEventListener.class);

        Mockito.when(listener2.flush(Mockito.any(FlushEvent.class))).thenThrow(new Exception());

        final List<FlushEventListener> listeners = new ArrayList<FlushEventListener>();
        listeners.add(listener1);
        listeners.add(listener2);
        listeners.add(listener3);

        tracker = new TimeTracker(logger, clock, false, listeners, 10, 1, "rec");

        tracker.track("rec", "desc", 10);

        tracker.flush();

        verify(listener1, times(1)).flush(Mockito.any(FlushEvent.class));
        verify(listener2, times(1)).flush(Mockito.any(FlushEvent.class));
        verify(listener3, times(1)).flush(Mockito.any(FlushEvent.class));
    }

    @Test
    public void should_flush_call_all_flush_listeners() throws Exception {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final Clock clock = mock(Clock.class);

        final FlushEventListener listener1 = mock(FlushEventListener.class);
        final FlushEventListener listener2 = mock(FlushEventListener.class);

        final List<FlushEventListener> listeners = new ArrayList<FlushEventListener>();
        listeners.add(listener1);
        listeners.add(listener2);

        tracker = new TimeTracker(logger, clock, false, listeners, 10, 1, "rec");

        tracker.track("rec", "desc", 10);

        tracker.flush();

        verify(listener1, times(1)).flush(Mockito.any(FlushEvent.class));
        verify(listener2, times(1)).flush(Mockito.any(FlushEvent.class));
    }

    @Test
    public void rollingRecords() {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        tracker = new TimeTracker(logger, false, null, 2, 2, "rec");
        tracker.start();
        tracker.track("rec", "rec1", 100);
        assertEquals(1, tracker.getRecords().size());
        tracker.track("rec", "rec2", 100);
        assertEquals(2, tracker.getRecords().size());
        tracker.track("rec", "rec3", 100);
        assertEquals(2, tracker.getRecords().size());
        assertEquals("rec2", tracker.getRecords().get(0).getDescription());
        assertEquals("rec3", tracker.getRecords().get(1).getDescription());
        tracker.stop();
    }
}
