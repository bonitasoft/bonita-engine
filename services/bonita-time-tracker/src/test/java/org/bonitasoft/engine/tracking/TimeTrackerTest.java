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
import org.junit.Test;
import org.mockito.Mockito;

public class TimeTrackerTest extends AbstractTimeTrackerTest {

    @Test
    public void testIsTrackable() throws Exception {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final TimeTracker tracker = new TimeTracker(logger, false, null, 10, 2, "rec1", "rec2");
        assertTrue(tracker.isTrackable("rec1"));
        assertTrue(tracker.isTrackable("rec2"));
        assertFalse(tracker.isTrackable("rec3"));
    }

    @Test
    public void testTrackRecords() throws Exception {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final TimeTracker tracker = new TimeTracker(logger, false, null, 10, 2, "rec1", "rec2");
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
    }

    @Test
    public void testTimestamp() throws Exception {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final TimeTracker tracker = new TimeTracker(logger, false, null, 10, 2, "rec1");
        tracker.track("rec1", "desc2", 100);
        Thread.sleep(2);
        tracker.track("rec1", "desc2", 200);
        final Map<String, List<Record>> records = mapRecords(tracker.getRecords());
        assertEquals(1, records.size());
        assertEquals(2, records.get("rec1").size());

        final List<Record> rec1s = records.get("rec1");

        assertTrue(rec1s.get(0).getTimestamp() < rec1s.get(1).getTimestamp());
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

    @Test(expected = RuntimeException.class)
    public void should_throw_an_exception_on_stopFlushThread_if_not_started() throws Exception {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final TimeTracker tracker = new TimeTracker(logger, false, null, 10, 2);
        tracker.stopFlushThread();
    }

    @Test(expected = RuntimeException.class)
    public void should_throw_an_exception_on_startFlushThread_if_already_started_at_build_time() throws Exception {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final TimeTracker tracker = new TimeTracker(logger, true, null, 10, 2);
        try {
            tracker.startFlushThread();
        } finally {
            tracker.stopFlushThread();
        }
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

        final TimeTracker tracker = new TimeTracker(logger, clock, false, listeners, 10, 1, "rec");

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

        final TimeTracker tracker = new TimeTracker(logger, clock, false, listeners, 10, 1, "rec");

        tracker.track("rec", "desc", 10);

        tracker.flush();

        verify(listener1, times(1)).flush(Mockito.any(FlushEvent.class));
        verify(listener2, times(1)).flush(Mockito.any(FlushEvent.class));

    }

    @Test
    public void testRollingRecords() throws Exception {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final TimeTracker tracker = new TimeTracker(logger, false, null, 2, 2, "rec");

        tracker.track("rec", "rec1", 100);
        assertEquals(1, tracker.getRecords().size());
        tracker.track("rec", "rec2", 100);
        assertEquals(2, tracker.getRecords().size());
        tracker.track("rec", "rec3", 100);
        assertEquals(2, tracker.getRecords().size());
        assertEquals("rec2", tracker.getRecords().get(0).getDescription());
        assertEquals("rec3", tracker.getRecords().get(1).getDescription());
    }
}
