package org.bonitasoft.engine.tracking.memory;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Arrays;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
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
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final MemoryFlushEventListener listener = new MemoryFlushEventListener(true, logger, 10);
        final Record rec1 = new Record(System.currentTimeMillis(), REC, "rec1Desc", 100);
        listener.flush(new FlushEvent(System.currentTimeMillis(), Arrays.asList(rec1)));
        assertEquals(1, listener.getDayRecord().getRecordsCopy().size());
        final Record rec2 = new Record(System.currentTimeMillis(), REC, "rec2Desc", 200);
        listener.flush(new FlushEvent(System.currentTimeMillis(), Arrays.asList(rec2)));
        assertEquals(2, listener.getDayRecord().getRecordsCopy().size());
    }

    @Test
    public void should_day_record_never_exceed_maxSize() throws Exception {
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final MemoryFlushEventListener listener = new MemoryFlushEventListener(true, logger, 1);
        final Record rec1 = new Record(System.currentTimeMillis(), REC, "rec1Desc", 100);
        listener.flush(new FlushEvent(System.currentTimeMillis(), Arrays.asList(rec1)));
        assertEquals(1, listener.getDayRecord().getRecordsCopy().size());
        final Record rec2 = new Record(System.currentTimeMillis(), REC, "rec2Desc", 200);
        listener.flush(new FlushEvent(System.currentTimeMillis(), Arrays.asList(rec2)));
        assertEquals(1, listener.getDayRecord().getRecordsCopy().size());
    }
}
