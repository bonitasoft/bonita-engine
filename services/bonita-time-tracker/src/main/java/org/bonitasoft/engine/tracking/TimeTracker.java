package org.bonitasoft.engine.tracking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

public class TimeTracker {

    private final Set<String> activatedRecords;

    private final FlushThread flushThread;

    private final List<? extends FlushEventListener> flushEventListeners;

    private final TechnicalLoggerService logger;

    private final Queue<Record> records;

    public TimeTracker(
            final TechnicalLoggerService logger,
            final boolean startFlushThread,
            final List<? extends FlushEventListener> flushEventListeners,
            final int maxSize,
            final int flushIntervalInSeconds,
            final String... activatedRecords) {
        this(logger, new ThreadSleepClockImpl(), startFlushThread, flushEventListeners, maxSize, flushIntervalInSeconds * 1000, activatedRecords);
    }

    public TimeTracker(
            final TechnicalLoggerService logger,
            final Clock clock,
            final boolean startFlushThread,
            final List<? extends FlushEventListener> flushEventListeners,
            final int maxSize,
            final int flushIntervalInSeconds,
            final String... activatedRecords) {
        super();
        this.logger = logger;
        this.flushEventListeners = flushEventListeners;
        if (activatedRecords == null || activatedRecords.length == 0) {
            this.activatedRecords = Collections.emptySet();
        } else {
            this.activatedRecords = new HashSet<String>(Arrays.asList(activatedRecords));
        }
        if (activatedRecords != null && activatedRecords.length > 0 && logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            this.logger.log(getClass(), TechnicalLogSeverity.INFO,
                    "Time tracker is activated for some records. This may not be used in production as performances may be strongly impacted: "
                            + activatedRecords);
        }
        this.flushThread = new FlushThread(clock, flushIntervalInSeconds, this, logger);

        this.records = new CircularFifoQueue<Record>(maxSize);

        if (startFlushThread) {
            startFlushThread();
        }
        Runtime.getRuntime().addShutdownHook(new FlushThreadShutdownHook(this.flushThread));
    }

    public boolean isTrackable(final String recordName) {
        return this.activatedRecords.contains(recordName);
    }

    public void track(final String recordName, final String recordDescription, final long duration) {
        final long timestamp = System.currentTimeMillis();
        final Record record = new Record(timestamp, recordName, recordDescription, duration);
        track(record);
    }

    public void track(final Record record) {
        if (isTrackable(record.getName())) {
            if (this.logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
                logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Tracking record: " + record);
            }
            // TODO needs a synchro?
            records.add(record);
        }
    }

    public void startFlushThread() {
        if (this.logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(getClass(), TechnicalLogSeverity.INFO, "Starting TimeTracker FlushThread...");
        }
        if (this.flushThread.isAlive()) {
            throw new RuntimeException("FlushThread is already running");
        }
        this.flushThread.start();
        if (this.logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(getClass(), TechnicalLogSeverity.INFO, "TimeTracker FlushThread started.");
        }
    }

    public void stopFlushThread() {
        if (this.logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(getClass(), TechnicalLogSeverity.INFO, "Stopping TimeTracker FlushThread...");
        }
        if (!this.flushThread.isAlive()) {
            throw new RuntimeException("FlushThread is not running");
        }
        this.flushThread.interrupt();
        if (this.logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(getClass(), TechnicalLogSeverity.INFO, "TimeTracker FlushThread stopped.");
        }
    }

    public List<FlushResult> flush() throws IOException {
        if (this.logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(getClass(), TechnicalLogSeverity.INFO, "Flushing...");
        }
        final List<Record> records = getRecords();

        if (logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
            logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Flushing...");
        }

        final FlushEvent flushEvent = new FlushEvent(records);

        final List<FlushResult> flushResults = new ArrayList<FlushResult>();
        if (this.flushEventListeners != null) {
            for (final FlushEventListener listener : this.flushEventListeners) {
                try {
                    flushResults.add(listener.flush(flushEvent));
                } catch (Exception e) {
                    if (this.logger.isLoggable(getClass(), TechnicalLogSeverity.WARNING)) {
                        logger.log(getClass(), TechnicalLogSeverity.WARNING, "Exception while flushing: " + flushEvent + " on listener " + listener);
                    }
                }
            }
        }
        if (this.logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(getClass(), TechnicalLogSeverity.INFO, "Flush finished: " + flushEvent);
        }
        return flushResults;
    }

    public List<Record> getRecords() throws IOException {
        return Arrays.asList(this.records.toArray(new Record[] {}));
    }

}
