package org.bonitasoft.engine.tracking.collector;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Logger;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.tracking.FlushEvent;
import org.bonitasoft.engine.tracking.FlushEventListener;
import org.bonitasoft.engine.tracking.FlushEventListenerResult;
import org.bonitasoft.engine.tracking.Record;

public class MemoryFlushEventListener implements FlushEventListener {

    private final int maxSize;
    private DayRecord dayRecord;

    public MemoryFlushEventListener(final int maxSize) {
        this.maxSize = maxSize;
    }
    
    @Override
    public synchronized FlushEventListenerResult flush(final TechnicalLoggerService logger, final FlushEvent flushEvent) throws Exception {

        if (flushEvent.getRecords().size() == 0) {
            return new FlushEventListenerResult(flushEvent);
        }

        if (logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
            logger.log(getClass(), TechnicalLogSeverity.DEBUG, "FlushEvent received with " + flushEvent.getRecords().size() + " records.");
        }

        final long flushTime = flushEvent.getFlushTime();
        if (this.dayRecord == null || !this.dayRecord.isExpectedDayKey(flushTime)) {
            this.dayRecord = new DayRecord(flushTime, maxSize);
        }
        this.dayRecord.addRecords(flushEvent.getRecords());

        if (logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(getClass(), TechnicalLogSeverity.INFO, "Adding '" + flushEvent.getRecords().size()
                    + "' records to DayRecord with dayKey '" + this.dayRecord.getDayKey() + "'");
        }
        return new FlushEventListenerResult(flushEvent);
    }



    @Override
    public String getStatus() {
        String status = getName() + ": ";
        if (this.dayRecord == null) {
            status += "No DayRecord registered in memory.";
        } else {
            status += ", dayRecord: " + this.dayRecord.toString();
        }
        return status;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void notifyStopTracking() {
        this.dayRecord = null;
    }

    public synchronized DayRecord getDayRecord() {
        return this.dayRecord;
    }

    public synchronized void clear() {
        this.dayRecord = null;
    }

}