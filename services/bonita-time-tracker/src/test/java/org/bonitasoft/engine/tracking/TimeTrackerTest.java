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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.tracking.memory.MemoryFlushEventListener;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TimeTrackerTest extends AbstractTimeTrackerTest {

    private TimeTracker tracker;

    @Mock
    private FlushThread flushThread;

    private static final TimeTrackerRecords REC = TimeTrackerRecords.EVALUATE_EXPRESSION;

    private static final TimeTrackerRecords REC1 = TimeTrackerRecords.EVALUATE_EXPRESSION_INCLUDING_CONTEXT;
    private static final TimeTrackerRecords REC2 = TimeTrackerRecords.EXECUTE_CONNECTOR_CALLABLE;
    private static final TimeTrackerRecords REC3 = TimeTrackerRecords.EXECUTE_CONNECTOR_DISCONNECT;
    private static final TimeTrackerRecords INACTIVATED_REC = TimeTrackerRecords.EXECUTE_CONNECTOR_OUTPUT_OPERATIONS;

    @After
    public void tearDown() {
        // Make sure to clean tracker threads by stopping tracker.
        if (this.tracker != null) {
            this.tracker.stop();
        }
    }

    TimeTracker createTimeTracker(final Clock clock,
            final List<FlushEventListener> listeners, final boolean enabled, final int maxSize,
            final int flushIntervalInSeconds, final TimeTrackerRecords rec) {
        return new TimeTracker(clock, enabled, listeners, maxSize, flushIntervalInSeconds, rec.name()) {

            @Override
            FlushThread createFlushThread() {
                return TimeTrackerTest.this.flushThread;
            }
        };
    }

    TimeTracker createTimeTracker(final boolean enabled, final List<FlushEventListener> flushEventListeners,
            final int maxSize,
            final int flushIntervalInSeconds, final TimeTrackerRecords... records) {
        final List<String> recordsAsString = new ArrayList<>();
        for (final TimeTrackerRecords record : records) {
            recordsAsString.add(record.name());
        }
        return new TimeTracker(enabled, flushEventListeners, maxSize, flushIntervalInSeconds,
                recordsAsString.toArray(new String[records.length])) {

            @Override
            FlushThread createFlushThread() {
                return TimeTrackerTest.this.flushThread;
            }
        };
    }

    @Test
    public void should_isTrackable_returns_false_if_not_started() {
        this.tracker = createTimeTracker(true, null, 10, 2, REC);
        assertFalse(this.tracker.isTrackable(REC));
    }

    @Test
    public void isTrackable() {
        when(this.flushThread.isStarted()).thenReturn(true);
        this.tracker = createTimeTracker(true, null, 10, 2, REC1, REC2);
        this.tracker.start();
        assertTrue(this.tracker.isTrackable(REC1));
        assertTrue(this.tracker.isTrackable(REC2));
        assertFalse(this.tracker.isTrackable(REC3));
        this.tracker.stop();
    }

    @Test
    public void trackRecords() {
        when(this.flushThread.isStarted()).thenReturn(true);
        this.tracker = createTimeTracker(true, null, 10, 2, REC1, REC2);
        this.tracker.start();
        this.tracker.track(REC1, "rec11Desc", 100);
        this.tracker.track(REC1, "rec12Desc", 200);
        this.tracker.track(REC2, "rec2Desc", 300);
        this.tracker.track(INACTIVATED_REC, "blabla", 1000);
        final Map<TimeTrackerRecords, List<Record>> records = mapRecords(this.tracker.getRecordsCopy());
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
        this.tracker.stop();
    }

    @Test
    public void should_not_track_when_not_enabled() {
        this.tracker = createTimeTracker(false, null, 10, 2, REC1, REC2);
        this.tracker.start();
        this.tracker.track(REC1, "rec11Desc", 100);
        this.tracker.track(REC1, "rec12Desc", 200);
        this.tracker.track(REC2, "rec2Desc", 300);
        this.tracker.track(INACTIVATED_REC, "blabla", 1000);

        assertTrue(this.tracker.getRecordsCopy().isEmpty());
    }

    @Test
    public void timestamp() throws Exception {
        when(this.flushThread.isStarted()).thenReturn(true);
        this.tracker = createTimeTracker(true, null, 10, 2, REC1);
        this.tracker.start();
        this.tracker.track(REC1, "desc2", 100);
        Thread.sleep(2);
        this.tracker.track(REC1, "desc2", 200);
        final Map<TimeTrackerRecords, List<Record>> records = mapRecords(this.tracker.getRecordsCopy());
        assertEquals(1, records.size());
        assertEquals(2, records.get(REC1).size());

        final List<Record> rec1s = records.get(REC1);

        assertTrue(rec1s.get(0).getTimestamp() < rec1s.get(1).getTimestamp());
        this.tracker.stop();
    }

    private Map<TimeTrackerRecords, List<Record>> mapRecords(final List<Record> records) {
        final Map<TimeTrackerRecords, List<Record>> result = new HashMap<>();
        if (records != null) {
            for (final Record record : records) {
                final TimeTrackerRecords name = record.getName();
                if (!result.containsKey(name)) {
                    result.put(name, new ArrayList<>());
                }
                result.get(name).add(record);
            }
        }
        return result;
    }

    @Test
    public void should_build_do_not_start_tracking() {
        this.tracker = createTimeTracker(true, null, 10, 2);
        assertFalse(this.flushThread.isStarted());
    }

    @Test
    public void should_start_flush_thread_if_thread_is_stopped() {
        doReturn(false).when(this.flushThread).isStarted();
        this.tracker = createTimeTracker(true, null, 10, 2);
        this.tracker.start();
        verify(this.flushThread).start();
    }

    @Test
    public void should_stop_flush_thread_is_running() {
        when(this.flushThread.isStarted()).thenReturn(true);
        this.tracker = createTimeTracker(true, null, 10, 2);
        this.tracker.start();
        this.tracker.stop();
        verify(this.flushThread).interrupt();
    }

    @Test
    public void should_not_stop_flush_thread_is_running() {
        this.tracker = createTimeTracker(true, null, 10, 2);
        this.tracker.stop();
        verify(this.flushThread, never()).interrupt();
    }

    @Test
    public void should_flush_ignore_flush_listeners_exceptions() throws Exception {
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

        Mockito.when(listener2.flush(ArgumentMatchers.any(FlushEvent.class))).thenThrow(new Exception());

        final List<FlushEventListener> listeners = new ArrayList<>();
        listeners.add(listener1);
        listeners.add(listener2);
        listeners.add(listener3);

        this.tracker = createTimeTracker(clock, listeners, true, 10, 1, REC);

        this.tracker.track(REC, "desc", 10);

        this.tracker.flush();

        verify(listener1, times(1)).flush(ArgumentMatchers.any(FlushEvent.class));
        verify(listener2, times(1)).flush(ArgumentMatchers.any(FlushEvent.class));
        verify(listener3, times(1)).flush(ArgumentMatchers.any(FlushEvent.class));
    }

    @Test
    public void should_flush_call_all_flush_listeners() throws Exception {
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

        this.tracker = createTimeTracker(clock, listeners, true, 10, 1, REC);

        this.tracker.track(REC, "desc", 10);

        this.tracker.flush();

        verify(listener1, times(1)).flush(ArgumentMatchers.any(FlushEvent.class));
        verify(listener2, times(1)).flush(ArgumentMatchers.any(FlushEvent.class));
    }

    @Test
    public void rollingRecords() {
        when(this.flushThread.isStarted()).thenReturn(true);
        this.tracker = createTimeTracker(true, null, 2, 2, REC);
        this.tracker.start();
        this.tracker.track(REC, "rec1", 100);
        assertEquals(1, this.tracker.getRecordsCopy().size());
        this.tracker.track(REC, "rec2", 100);
        assertEquals(2, this.tracker.getRecordsCopy().size());
        this.tracker.track(REC, "rec3", 100);
        assertEquals(2, this.tracker.getRecordsCopy().size());
        assertEquals("rec2", this.tracker.getRecordsCopy().get(0).getDescription());
        assertEquals("rec3", this.tracker.getRecordsCopy().get(1).getDescription());
        this.tracker.stop();
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

        this.tracker = createTimeTracker(true, flushEventListeners, 2, 2, REC);

        assertEquals(1, this.tracker.getActiveFlushEventListeners().size());
    }

    @Test
    public void testActivateListeners() {
        final FlushEventListener listener1 = mock(FlushEventListener.class);
        when(listener1.getName()).thenReturn("listener1");
        final FlushEventListener listener2 = mock(FlushEventListener.class);
        when(listener2.getName()).thenReturn("listener2");

        final List<FlushEventListener> flushEventListeners = new ArrayList<>();
        flushEventListeners.add(listener1);
        flushEventListeners.add(listener2);

        this.tracker = createTimeTracker(true, flushEventListeners, 2, 2, REC);

        this.tracker.activateFlushEventListener("listener2");
        verify(listener2, times(1)).activate();
    }

    @Test
    public void testDeactivateListeners() {
        final FlushEventListener listener1 = mock(FlushEventListener.class);
        when(listener1.getName()).thenReturn("listener1");

        final List<FlushEventListener> flushEventListeners = new ArrayList<>();
        flushEventListeners.add(listener1);

        this.tracker = createTimeTracker(true, flushEventListeners, 2, 2, REC);

        this.tracker.deactivateFlushEventListener("listener1");
        verify(listener1, times(1)).deactivate();
    }

    @Test
    public void testActivatedRecords() {
        this.tracker = createTimeTracker(true, null, 2, 2, REC);

        assertEquals(1, this.tracker.getActivatedRecords().size());

        this.tracker.activateRecord(REC2);
        assertEquals(2, this.tracker.getActivatedRecords().size());

        //no duplicate
        this.tracker.activateRecord(REC2);
        assertEquals(2, this.tracker.getActivatedRecords().size());

        this.tracker.deactivatedRecord(REC);
        assertEquals(1, this.tracker.getActivatedRecords().size());
    }

    @Test
    public void testFlushInterval() {
        this.tracker = createTimeTracker(true, null, 2, 1, REC);

        assertEquals(1000, this.tracker.getFlushIntervalInMS());

        this.tracker.setFlushIntervalInMS(111);
        assertEquals(111, this.tracker.getFlushIntervalInMS());

        this.tracker.setFlushIntervalInSeconds(10);
        assertEquals(10000, this.tracker.getFlushIntervalInMS());
    }

    @Test
    public void should_stop_tracking_interrupt_flush_thread_and_listeners() {
        final FlushEventListener listener1 = mock(FlushEventListener.class);
        when(listener1.getName()).thenReturn("listener1");
        when(listener1.isActive()).thenReturn(true);
        final FlushEventListener listener2 = mock(FlushEventListener.class);
        when(listener2.getName()).thenReturn("listener2");
        when(listener2.isActive()).thenReturn(true);

        final List<FlushEventListener> flushEventListeners = new ArrayList<>();
        flushEventListeners.add(listener1);
        flushEventListeners.add(listener2);

        this.tracker = createTimeTracker(true, flushEventListeners, 2, 2, REC);
        this.tracker.start();
        when(this.flushThread.isStarted()).thenReturn(true);

        this.tracker.stopTracking();
        verify(this.flushThread, times(1)).interrupt();
        verify(listener1, times(1)).notifyStopTracking();
        verify(listener2, times(1)).notifyStopTracking();
    }

    @Test
    public void should_start_tracking_start_flush_thread_and_listeners() {
        final FlushEventListener listener1 = mock(FlushEventListener.class);
        when(listener1.getName()).thenReturn("listener1");
        when(listener1.isActive()).thenReturn(true);
        final FlushEventListener listener2 = mock(FlushEventListener.class);
        when(listener2.getName()).thenReturn("listener2");
        when(listener2.isActive()).thenReturn(true);

        final List<FlushEventListener> flushEventListeners = new ArrayList<>();
        flushEventListeners.add(listener1);
        flushEventListeners.add(listener2);

        this.tracker = createTimeTracker(true, flushEventListeners, 2, 2, REC);
        when(this.flushThread.isStarted()).thenReturn(false);
        this.tracker.start();

        this.tracker.startTracking();

        verify(this.flushThread, times(2)).start();
        verify(listener1, times(2)).notifyStartTracking();
        verify(listener2, times(2)).notifyStartTracking();
    }

    @Test
    public void should_pause_resume_without_error() {

        //given
        this.tracker = new TimeTracker(true, new LinkedList<>(), 500, 1, (String[]) null);

        //when
        this.tracker.start();
        int i = 0;
        try {
            // Error may not occur on the first execution. I did reproduce it in 4 to 8 executions.
            // To make myself confident, I execute the test 100 times.
            // Current implementation is deterministic, but previous one was not. Hence the loop in the test.
            while (i < 100) {
                i++;
                this.tracker.pause();
                this.tracker.resume();
                //No errors expected
            }
        } finally {
            System.err.println("Test ended after iteration: " + i);
            this.tracker.stop();
        }
    }

    @Test
    public void should_start_method_be_reentrant() {

        //given
        this.tracker = new TimeTracker(true, new LinkedList<>(), 500, 1, (String[]) null);
        final Set<Thread> initialThreadSet = Thread.getAllStackTraces().keySet();
        this.tracker.start();
        //expect
        final Set<Thread> runningTimeTrackerThreadSet = Thread.getAllStackTraces().keySet();
        assertThat(initialThreadSet.size() + 1).isEqualTo(runningTimeTrackerThreadSet.size());

        //when
        this.tracker.start();

        //then
        final Set<Thread> stillRunningTimeTrackerThreadSet = Thread.getAllStackTraces().keySet();
        assertThat(runningTimeTrackerThreadSet).isEqualTo(stillRunningTimeTrackerThreadSet);
    }

    @Test
    public void isTracking_should_be_false_if_tracking_not_started() {
        //given
        this.tracker = new TimeTracker(true, new LinkedList<>(), 500, 1, (String[]) null);
        //when
        final boolean trackingStatus = this.tracker.isTracking();
        //then
        assertThat(trackingStatus).as("Tracking status must be FALSE if tracker has not yet been started.").isFalse();
    }

    @Test
    public void isTracking_should_be_true_if_tracking_is_started() {
        //given
        this.tracker = new TimeTracker(true, new LinkedList<>(), 500, 1, (String[]) null);
        try {
            this.tracker.start();

            //when
            final boolean trackingStatus = this.tracker.isTracking();

            //then
            assertThat(trackingStatus).as("Tracking status must be TRUE after start method has been called.").isTrue();
        } finally {
            this.tracker.stop();
        }
    }

    @Test
    public void should_not_leave_unused_threads_when_stopped() {
        //given
        this.tracker = new TimeTracker(true, new LinkedList<>(), 500, 1, (String[]) null);
        final Set<Thread> beforeTimeTrackerStartedThreadSet = Thread.getAllStackTraces().keySet();
        this.tracker.start();

        //when
        this.tracker.stop();

        //then
        final Set<Thread> afterTimeTrackerStoppedThreadSet = Thread.getAllStackTraces().keySet();
        afterTimeTrackerStoppedThreadSet.removeAll(beforeTimeTrackerStartedThreadSet);

        //There should be no more threads than the ones existing prior to tracker startup
        assertThat(afterTimeTrackerStoppedThreadSet).isEqualTo(Collections.emptySet());
    }

    @Test
    public void should_add_remove_listeners() {
        // create a tracker
        this.tracker = new TimeTracker(true, new LinkedList<>(), 500, 1);

        // no active listener
        assertThat(this.tracker.getActiveFlushEventListeners()).isEmpty();

        // add a new listener
        final FlushEventListener flushEvent = new MemoryFlushEventListener(true, 10);

        this.tracker.addFlushEventListener(flushEvent);
        assertThat(this.tracker.getActiveFlushEventListeners()).containsOnly(flushEvent);

        // remove the same listener
        this.tracker.removeFlushEventListener(flushEvent.getName());
        assertThat(this.tracker.getActiveFlushEventListeners()).isEmpty();
    }

    @Test
    public void should_have_one_listener_based_on_listener_name() {
        // create a tracker
        this.tracker = new TimeTracker(true, new LinkedList<>(), 500, 1);
        // add a new listener
        final FlushEventListener flushEvent1 = new MemoryFlushEventListener(true, 10);
        final FlushEventListener flushEvent2 = new MemoryFlushEventListener(true, 10);

        this.tracker.addFlushEventListener(flushEvent1);
        this.tracker.addFlushEventListener(flushEvent2);
        // must have only one time
        assertThat(this.tracker.getActiveFlushEventListeners()).containsOnlyOnce(flushEvent2);
    }
}
