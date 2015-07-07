package org.bonitasoft.engine.tracking.memory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Queue;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.bonitasoft.engine.tracking.Record;

/**
 * @author Charles Souillard
 */
public class DayRecord {

    private final String dayKey;
    private final Queue<Record> records;


    public DayRecord(final long timestamp, final int maxSize) {
        this.dayKey = getDayKey(timestamp);
        this.records = new CircularFifoQueue(maxSize);
    }

    public String getDayKey() {
        return dayKey;
    }

    public List<Record> getRecordsCopy() {
        return new ArrayList<>(this.records);
    }

    public boolean isExpectedDayKey(final long timestamp) {
        final String expectedDayKey = getDayKey(timestamp);
        return this.dayKey.equals(expectedDayKey);
    }

    private String getDayKey(final long timestamp) {
        final GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(timestamp);
        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH) + 1;
        final int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

        return String.valueOf(year) + String.valueOf(month) + String.valueOf(dayOfMonth);
    }


    public void addRecords(List<Record> newRecords) {
        this.records.addAll(newRecords);
    }

    @Override
    public String toString() {
        return "DayRecord{" +
                "dayKey='" + dayKey + '\'' +
                ", records.size=" + records.size() +
                '}';
    }
}
