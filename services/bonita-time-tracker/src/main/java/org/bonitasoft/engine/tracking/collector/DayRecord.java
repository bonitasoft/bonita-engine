package org.bonitasoft.engine.tracking.collector;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.bonitasoft.engine.tracking.Record;

/**
 * @author Charles Souillard
 */
public class DayRecord {

    private final String dayKey;
    private final List<Record> records = new ArrayList<>();


    public DayRecord(final long timestamp) {
        this.dayKey = getDayKey(timestamp);
    }

    public String getDayKey() {
        return dayKey;
    }

    public List<Record> getRecords() {
        return records;
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
