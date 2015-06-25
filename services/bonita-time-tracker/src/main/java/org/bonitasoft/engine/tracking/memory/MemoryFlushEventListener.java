package org.bonitasoft.engine.tracking.memory;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.tracking.FlushEvent;
import org.bonitasoft.engine.tracking.FlushEventListener;
import org.bonitasoft.engine.tracking.FlushEventListenerResult;

public class MemoryFlushEventListener implements FlushEventListener {

    private final boolean activateAtStart;
    private final int maxSize;
    private DayRecord dayRecord;

    private final TechnicalLoggerService logger;

    public MemoryFlushEventListener(final boolean activateAtStart, final TechnicalLoggerService logger, final int maxSize) {
        this.activateAtStart = activateAtStart;
        this.maxSize = maxSize;
        this.logger = logger;
    }



    @Override
    public synchronized FlushEventListenerResult flush(final FlushEvent flushEvent) throws Exception {

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
    public boolean activateAtStart() {
        return activateAtStart;
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