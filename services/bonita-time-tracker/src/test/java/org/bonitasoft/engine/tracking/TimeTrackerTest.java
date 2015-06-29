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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TimeTrackerTest extends AbstractTimeTrackerTest {

    private TimeTracker tracker;
    @Mock
    private FlushThread flushThread;
    @Mock
    private TechnicalLoggerService logger;

    private static final TimeTrackerRecords REC = TimeTrackerRecords.EVALUATE_EXPRESSION;
    private static final TimeTrackerRecords REC1 = TimeTrackerRecords.EVALUATE_EXPRESSION_INCLUDING_CONTEXT;
    private static final TimeTrackerRecords REC2 = TimeTrackerRecords.EXECUTE_CONNECTOR_CALLABLE;
    private static final TimeTrackerRecords REC3 = TimeTrackerRecords.EXECUTE_CONNECTOR_DISCONNECT;
    private static final TimeTrackerRecords INACTIVATED_REC = TimeTrackerRecords.EXECUTE_CONNECTOR_OUTPUT_OPERATIONS;

    TimeTracker createTimeTracker(TechnicalLoggerService logger, Clock clock, List<FlushEventListener> listeners, boolean enabled, int maxSize,
            int flushIntervalInSeconds, TimeTrackerRecords rec) {
        return new TimeTracker(logger, clock, enabled, listeners, maxSize, flushIntervalInSeconds, rec.name()) {

            @Override
            FlushThread createFlushThread() {
                return flushThread;
            }
        };
    }

    TimeTracker createTimeTracker(boolean enabled, List<FlushEventListener> flushEventListeners, int maxSize,
            int flushIntervalInSeconds, TimeTrackerRecords... records) {
        final List<String> recordsAsString = new ArrayList<>();
        for (TimeTrackerRecords record : records) {
            recordsAsString.add(record.name());
        }
        return new TimeTracker(logger, enabled, flushEventListeners, maxSize, flushIntervalInSeconds, recordsAsString.toArray(new String[records.length])) {

            @Override
            FlushThread createFlushThread() {
                return flushThread;
            }
        };
    }

    @Test
    public void should_isTrackable_returns_false_if_not_started() {
        tracker = createTimeTracker(true, null, 10, 2, REC);
        assertFalse(tracker.isTrackable(REC));
    }

    @Test
    public void isTrackable() {
        when(flushThread.isStarted()).thenReturn(true);
        tracker = createTimeTracker(true, null, 10, 2, REC1, REC2);
        tracker.start();
        assertTrue(tracker.isTrackable(REC1));
        assertTrue(tracker.isTrackable(REC2));
        assertFalse(tracker.isTrackable(REC3));
        tracker.stop();
    }

    @Test
    public void trackRecords() {
        when(flushThread.isStarted()).thenReturn(true);
        tracker = createTimeTracker(true, null, 10, 2, REC1, REC2);
        tracker.start();
        tracker.track(REC1, "rec11Desc", 100);
        tracker.track(REC1, "rec12Desc", 200);
        tracker.track(REC2, "rec2Desc", 300);
        tracker.track(INACTIVATED_REC, "blabla", 1000);
        final Map<TimeTrackerRecords, List<Record>> records = mapRecords(tracker.getRecordsCopy());
        assertEquals(2, records.size());
        assertEquals(2, records.get(REC1).size());
        assertEquals(1, records.get(REC2).size());

        final List<Record> rec1s = records.get(REC1);

        final Record rec11 = rec1s.get(0);
        assertEquals(REC1, rec11.getName());
        assertEquals("rec11Desc", rec11.getDescription());
        assertEquals(100L, rec11.getDuration());

        final Record rec12 = rec1s.get(1);
        assertEquals(REC1, rec12.getName());
        assertEquals("rec12Desc", rec12.getDescription());
        assertEquals(200L, rec12.getDuration());

        final List<Record> rec2s = records.get(REC2);
        final Record rec2 = rec2s.get(0);
        assertEquals(REC2, rec2.getName());
        assertEquals("rec2Desc", rec2.getDescription());
        assertEquals(300L, rec2.getDuration());
        tracker.stop();
    }

    @Test
    public void should_not_track_when_not_enabled() {
        tracker = createTimeTracker(false, null, 10, 2, REC1, REC2);
        tracker.start();
        tracker.track(REC1, "rec11Desc", 100);
        tracker.track(REC1, "rec12Desc", 200);
        tracker.track(REC2, "rec2Desc", 300);
        tracker.track(INACTIVATED_REC, "blabla", 1000);

        assertTrue(tracker.getRecordsCopy().isEmpty());
    }

    @Test
    public void timestamp() throws Exception {
        when(flushThread.isStarted()).thenReturn(true);
        tracker = createTimeTracker(true, null, 10, 2, REC1);
        tracker.start();
        tracker.track(REC1, "desc2", 100);
        Thread.sleep(2);
        tracker.track(REC1, "desc2", 200);
        final Map<TimeTrackerRecords, List<Record>> records = mapRecords(tracker.getRecordsCopy());
        assertEquals(1, records.size());
        assertEquals(2, records.get(REC1).size());

        final List<Record> rec1s = records.get(REC1);

        assertTrue(rec1s.get(0).getTimestamp() < rec1s.get(1).getTimestamp());
        tracker.stop();
    }

    private Map<TimeTrackerRecords, List<Record>> mapRecords(final List<Record> records) {
        final Map<TimeTrackerRecords, List<Record>> result = new HashMap<>();
        if (records != null) {
            for (final Record record : records) {
                final TimeTrackerRecords name = record.getName();
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
        tracker = createTimeTracker(true, null, 10, 2);
        verifyZeroInteractions(flushThread);
    }

    @Test
    public void should_start_flush_thread_if_thread_is_stopped() {
        doReturn(false).when(flushThread).isStarted();
        tracker = createTimeTracker(true, null, 10, 2);
        tracker.start();
        verify(flushThread).start();
    }

    @Test
    public void should_not_start_flush_thread_if_thread_is_started() {
        doReturn(true).when(flushThread).isStarted();
        tracker = createTimeTracker(true, null, 10, 2);
        tracker.start();
        verify(flushThread, never()).start();
    }

    @Test
    public void should_stop_flush_thread_is_running() {
        when(flushThread.isStarted()).thenReturn(true);
        tracker = createTimeTracker(true, null, 10, 2);
        tracker.start();
        tracker.stop();
        verify(flushThread).interrupt();
    }

    @Test
    public void should_not_stop_flush_thread_is_running() {
        doReturn(false).when(flushThread).isStarted();
        tracker = createTimeTracker(true, null, 10, 2);
        tracker.stop();
        verify(flushThread, never()).interrupt();
    }

    @Test
    public void should_flush_ignore_flush_listeners_exceptions() throws Exception {
        when(flushThread.isStarted()).thenReturn(true);
        final Clock clock = mock(Clock.class);

        final FlushEventListener listener1 = mock(FlushEventListener.class);
        when(listener1.getName()).thenReturn("listener1");
        when(listener1.isActive()).thenReturn(true);
        final FlushEventListener listener2 = mock(FlushEventListener.class);
        when(listener2.getName()).thenReturn("listener2");
        when(listener2.isActive()).thenReturn(true);
        final FlushEventListener listener3 = mock(FlushEventListener.class);
        when(listener3.getName()).thenReturn("listener3");
        when(listener3.isActive()).thenReturn(true);

        Mockito.when(listener2.flush(Mockito.any(FlushEvent.class))).thenThrow(new Exception());

        final List<FlushEventListener> listeners = new ArrayList<>();
        listeners.add(listener1);
        listeners.add(listener2);
        listeners.add(listener3);

        tracker = createTimeTracker(logger, clock, listeners, true, 10, 1, REC);

        tracker.track(REC, "desc", 10);

        tracker.flush();

        verify(listener1, times(1)).flush(Mockito.any(FlushEvent.class));
        verify(listener2, times(1)).flush(Mockito.any(FlushEvent.class));
        verify(listener3, times(1)).flush(Mockito.any(FlushEvent.class));
    }

    @Test
    public void should_flush_call_all_flush_listeners() throws Exception {
        when(flushThread.isStarted()).thenReturn(true);
        final Clock clock = mock(Clock.class);

        final FlushEventListener listener1 = mock(FlushEventListener.class);
        when(listener1.getName()).thenReturn("listener1");
        when(listener1.isActive()).thenReturn(true);
        final FlushEventListener listener2 = mock(FlushEventListener.class);
        when(listener2.getName()).thenReturn("listener2");
        when(listener2.isActive()).thenReturn(true);

        final List<FlushEventListener> listeners = new ArrayList<>();
        listeners.add(listener1);
        listeners.add(listener2);

        tracker = createTimeTracker(logger, clock, listeners, true, 10, 1, REC);

        tracker.track(REC, "desc", 10);

        tracker.flush();

        verify(listener1, times(1)).flush(Mockito.any(FlushEvent.class));
        verify(listener2, times(1)).flush(Mockito.any(FlushEvent.class));
    }

    @Test
    public void rollingRecords() {
        when(flushThread.isStarted()).thenReturn(true);
        tracker = createTimeTracker(true, null, 2, 2, REC);
        tracker.start();
        tracker.track(REC, "rec1", 100);
        assertEquals(1, tracker.getRecordsCopy().size());
        tracker.track(REC, "rec2", 100);
        assertEquals(2, tracker.getRecordsCopy().size());
        tracker.track(REC, "rec3", 100);
        assertEquals(2, tracker.getRecordsCopy().size());
        assertEquals("rec2", tracker.getRecordsCopy().get(0).getDescription());
        assertEquals("rec3", tracker.getRecordsCopy().get(1).getDescription());
        tracker.stop();
    }

    @Test
    public void testGetActiveListeners() {
        final FlushEventListener listener1 = mock(FlushEventListener.class);
        when(listener1.getName()).thenReturn("listener1");
        when(listener1.isActive()).thenReturn(true);
        final FlushEventListener listener2 = mock(FlushEventListener.class);
        when(listener2.getName()).thenReturn("listener2");
        when(listener2.isActive()).thenReturn(false);

        final List<FlushEventListener> flushEventListeners = new ArrayList<>();
        flushEventListeners.add(listener1);
        flushEventListeners.add(listener2);

        tracker = createTimeTracker(true, flushEventListeners, 2, 2, REC);

        assertEquals(1, tracker.getActiveFlushEventListeners().size());
    }

    @Test
    public void testActivateListeners() {
        final FlushEventListener listener1 = mock(FlushEventListener.class);
        when(listener1.getName()).thenReturn("listener1");
        when(listener1.isActive()).thenReturn(true);
        final FlushEventListener listener2 = mock(FlushEventListener.class);
        when(listener2.getName()).thenReturn("listener2");
        when(listener2.isActive()).thenReturn(false);

        final List<FlushEventListener> flushEventListeners = new ArrayList<>();
        flushEventListeners.add(listener1);
        flushEventListeners.add(listener2);

        tracker = createTimeTracker(true, flushEventListeners, 2, 2, REC);

        tracker.activateFlushEventListener("listener2");
        verify(listener2, times(1)).activate();
    }

    @Test
    public void testDeactivateListeners() {
        final FlushEventListener listener1 = mock(FlushEventListener.class);
        when(listener1.getName()).thenReturn("listener1");
        when(listener1.isActive()).thenReturn(true);

        final List<FlushEventListener> flushEventListeners = new ArrayList<>();
        flushEventListeners.add(listener1);

        tracker = createTimeTracker(true, flushEventListeners, 2, 2, REC);

        tracker.deactivateFlushEventListener("listener1");
        verify(listener1, times(1)).deactivate();
    }

    @Test
    public void testActivatedRecords() {
        tracker = createTimeTracker(true, null, 2, 2, REC);

        assertEquals(1, tracker.getActivatedRecords().size());

        tracker.activateRecord(REC2);
        assertEquals(2, tracker.getActivatedRecords().size());

        //no duplicate
        tracker.activateRecord(REC2);
        assertEquals(2, tracker.getActivatedRecords().size());

        tracker.deactivatedRecord(REC);
        assertEquals(1, tracker.getActivatedRecords().size());
    }
}
