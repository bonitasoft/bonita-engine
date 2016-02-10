package org.bonitasoft.engine.tracking.memory;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.tracking.AbstractFlushEventListener;
import org.bonitasoft.engine.tracking.FlushEvent;
import org.bonitasoft.engine.tracking.FlushEventListenerResult;

public class MemoryFlushEventListener extends AbstractFlushEventListener {

    private final int maxSize;
    private DayRecord dayRecord;

    public MemoryFlushEventListener(final boolean activateAtStart, final TechnicalLoggerService logger, final int maxSize) {
        super(activateAtStart, logger);
        this.maxSize = maxSize;
    }



    @Override
    public synchronized FlushEventListenerResult flush(final FlushEvent flushEvent) throws Exception {

        if (flushEvent.getRecords().size() == 0) {
            return new FlushEventListenerResult(flushEvent);
        }

        log(TechnicalLogSeverity.DEBUG, "Reusing csv file: " + "FlushEvent received with " + flushEvent.getRecords().size() + " records.");

        final long flushTime = flushEvent.getFlushTime();
        if (this.dayRecord == null || !this.dayRecord.isExpectedDayKey(flushTime)) {
            this.dayRecord = new DayRecord(flushTime, maxSize);
        }
        this.dayRecord.addRecords(flushEvent.getRecords());

        log(TechnicalLogSeverity.INFO, "Adding '" + flushEvent.getRecords().size() + "' records to DayRecord with dayKey '" + this.dayRecord.getDayKey() + "'");

        return new FlushEventListenerResult(flushEvent);
    }



    @Override
    public String getStatus() {
        String status = super.getStatus() + "\n";
        if (this.dayRecord == null) {
            status += "No DayRecord registered in memory.";
        } else {
            status += ", dayRecord: " + this.dayRecord.toString();
        }
        return status;
    }

    @Override
    public void notifyStopTracking() {
        this.dayRecord = null;
    }

    @Override
    public void notifyStartTracking() {
        //nothing to do
    }

    public synchronized DayRecord getDayRecord() {
        return this.dayRecord;
    }

    public synchronized void clear() {
        this.dayRecord = null;
    }

}