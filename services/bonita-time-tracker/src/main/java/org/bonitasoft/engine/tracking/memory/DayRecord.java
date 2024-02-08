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
package org.bonitasoft.engine.tracking.memory;

import java.util.ArrayList;
import java.util.Calendar;
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
        return this.dayKey;
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

    public void addRecords(final List<Record> newRecords) {
        this.records.addAll(newRecords);
    }

    @Override
    public String toString() {
        return "DayRecord{" +
                "dayKey='" + this.dayKey + '\'' +
                ", records.size=" + this.records.size() +
                '}';
    }
}
