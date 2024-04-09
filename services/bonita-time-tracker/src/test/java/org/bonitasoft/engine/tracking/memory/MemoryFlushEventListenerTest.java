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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.bonitasoft.engine.tracking.FlushEvent;
import org.bonitasoft.engine.tracking.Record;
import org.bonitasoft.engine.tracking.TimeTrackerRecords;
import org.junit.Test;

/**
 * @author Charles Souillard
 */
public class MemoryFlushEventListenerTest {

    private static final TimeTrackerRecords REC = TimeTrackerRecords.EVALUATE_EXPRESSION;

    @Test
    public void should_day_record_keep_all_records_of_subsequent_flush() throws Exception {
        final MemoryFlushEventListener listener = new MemoryFlushEventListener(true, 10);
        final Record rec1 = new Record(System.currentTimeMillis(), REC, "rec1Desc", 100);
        listener.flush(new FlushEvent(System.currentTimeMillis(), Arrays.asList(rec1)));
        assertEquals(1, listener.getDayRecord().getRecordsCopy().size());
        final Record rec2 = new Record(System.currentTimeMillis(), REC, "rec2Desc", 200);
        listener.flush(new FlushEvent(System.currentTimeMillis(), Arrays.asList(rec2)));
        assertEquals(2, listener.getDayRecord().getRecordsCopy().size());
    }

    @Test
    public void should_day_record_never_exceed_maxSize() throws Exception {

        final MemoryFlushEventListener listener = new MemoryFlushEventListener(true, 1);
        final Record rec1 = new Record(System.currentTimeMillis(), REC, "rec1Desc", 100);
        listener.flush(new FlushEvent(System.currentTimeMillis(), Arrays.asList(rec1)));
        assertEquals(1, listener.getDayRecord().getRecordsCopy().size());
        final Record rec2 = new Record(System.currentTimeMillis(), REC, "rec2Desc", 200);
        listener.flush(new FlushEvent(System.currentTimeMillis(), Arrays.asList(rec2)));
        assertEquals(1, listener.getDayRecord().getRecordsCopy().size());
    }
}
