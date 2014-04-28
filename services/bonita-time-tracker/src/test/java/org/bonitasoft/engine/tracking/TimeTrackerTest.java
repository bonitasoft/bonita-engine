package org.bonitasoft.engine.tracking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.Test;

public class TimeTrackerTest {

    @Test
    public void testIsTrackable() throws Exception {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final TimeTracker tracker = new TimeTracker(logger, false, null, 10, 2, System.getProperty("java.io.tmpdir"), ";", "rec1", "rec2");
        assertTrue(tracker.isTrackable("rec1"));
        assertTrue(tracker.isTrackable("rec2"));
        assertFalse(tracker.isTrackable("rec3"));
    }

    @Test
    public void testTrackRecords() throws Exception {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final TimeTracker tracker = new TimeTracker(logger, false, null, 10, 2, System.getProperty("java.io.tmpdir"), ";", "rec1", "rec2");
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
        final TimeTracker tracker = new TimeTracker(logger, false, null, 10, 2, System.getProperty("java.io.tmpdir"), ";", "rec1");
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
    public void should_fail_if_enabled_and_output_folder_unknown() throws Exception {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        new TimeTracker(logger, true, null, 10, 2, "unknownFolder", ";");
    }

    @Test(expected = RuntimeException.class)
    public void should_not_fail_if_not_enabled_and_output_folder_unknown() throws Exception {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        new TimeTracker(logger, false, null, 10, 2, "unknownFolder", ";");
    }

    @Test(expected = RuntimeException.class)
    public void testOutputFilePathNotAFolder() throws Exception {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final File file = new File(System.getProperty("java.io.tmpdir"), "test.txt");
        file.createNewFile();
        new TimeTracker(logger, false, null, 10, 2, file.getAbsolutePath(), ";");
    }

    @Test(expected = RuntimeException.class)
    public void should_throw_an_exception_on_stopPrinterThread_if_not_started() throws Exception {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final TimeTracker tracker = new TimeTracker(logger, false, null, 10, 2, "unknownFolder", ";");
        tracker.stopPrinterThread();
    }

    @Test(expected = RuntimeException.class)
    public void should_throw_an_exception_on_startPrinterThread_if_already_started_at_build_time() throws Exception {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final TimeTracker tracker = new TimeTracker(logger, true, null, 10, 2, "unknownFolder", ";");
        try {
            tracker.startPrinterThread();
        } finally {
            tracker.stopPrinterThread();
        }
    }

    @Test
    public void testFlush() throws Exception {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final Record rec1 = new Record(System.currentTimeMillis(), "rec", "rec1Desc", 100);
        final Record rec2 = new Record(System.currentTimeMillis(), "rec", "rec2Desc", 200);
        final TimeTracker tracker = new TimeTracker(logger, false, null, 10, 2, System.getProperty("java.io.tmpdir"), ";", "rec");

        tracker.track(rec1);
        tracker.track(rec2);

        final File csvFile = tracker.flush().getOutputFile();
        final List<List<String>> csvValues = CSVUtil.readCSV(true, csvFile, ";");
        assertEquals(2, csvValues.size());
        checkCSVRecord(rec1, csvValues.get(0));
        checkCSVRecord(rec2, csvValues.get(1));

        final List<Record> records = tracker.flush().getRecords();
        assertEquals(2, records.size());
        checkRecord(rec1, records.get(0));
        checkRecord(rec2, records.get(1));
    }

    private void checkRecord(final Record expected, final Record actual) {
        assertEquals(expected.getTimestamp(), actual.getTimestamp());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getDuration(), actual.getDuration());
    }

    private void checkCSVRecord(final Record record, final List<String> csvValues) {
        // timestamp, year, month, day, hour, minute, second, milisecond, duration, name, description]
        assertEquals(11, csvValues.size());

        final long timestamp = record.getTimestamp();
        final GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(timestamp);
        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH) + 1;
        final int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        final int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
        final int minute = cal.get(Calendar.MINUTE);
        final int second = cal.get(Calendar.SECOND);
        final int milisecond = cal.get(Calendar.MILLISECOND);

        assertEquals(timestamp, Long.valueOf(csvValues.get(0)).longValue());
        assertEquals(year, Integer.valueOf(csvValues.get(1)).intValue());
        assertEquals(month, Integer.valueOf(csvValues.get(2)).intValue());
        assertEquals(dayOfMonth, Integer.valueOf(csvValues.get(3)).intValue());
        assertEquals(hourOfDay, Integer.valueOf(csvValues.get(4)).intValue());
        assertEquals(minute, Integer.valueOf(csvValues.get(5)).intValue());
        assertEquals(second, Integer.valueOf(csvValues.get(6)).intValue());
        assertEquals(milisecond, Integer.valueOf(csvValues.get(7)).intValue());

        assertEquals(record.getDuration(), Long.valueOf(csvValues.get(8)).longValue());
        assertEquals(record.getName(), csvValues.get(9));
        assertEquals(record.getDescription(), csvValues.get(10));

    }

    @Test
    public void testPrinterThread() throws Exception {
        final Record rec1 = new Record(System.currentTimeMillis(), "rec", "rec1Desc", 100);
        final Record rec2 = new Record(System.currentTimeMillis(), "rec", "rec2Desc", 200);
        final Object monitor = new Object();
        final List<FlushEventListener> listeners = new ArrayList<FlushEventListener>();
        listeners.add(new FlushEventListener() {

            @Override
            public void flush(FlushEvent flushEvent) throws Exception {
                final File csvFile = flushEvent.getOutputFile();
                final List<List<String>> records = CSVUtil.readCSV(true, csvFile, ";");
                assertEquals(2, records.size());
                synchronized (monitor) {
                    monitor.notify();
                }
            }
        });
        final int flushIntervalInSeconds = 1;
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final TimeTracker tracker = new TimeTracker(logger, true, listeners, 10, flushIntervalInSeconds, System.getProperty("java.io.tmpdir"), ";", "rec");
        final long startTime = System.currentTimeMillis();
        final long maxWait = flushIntervalInSeconds * 1000 * 1;
        tracker.track(rec1);
        tracker.track(rec2);
        synchronized (monitor) {
            monitor.wait(maxWait);
        }
        final long endTime = System.currentTimeMillis();
        assertTrue((endTime - startTime) < maxWait);
        tracker.stopPrinterThread();
    }

    @Test
    public void testRollingRecords() throws Exception {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final TimeTracker tracker = new TimeTracker(logger, false, null, 2, 2, System.getProperty("java.io.tmpdir"), ";", "rec");

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
