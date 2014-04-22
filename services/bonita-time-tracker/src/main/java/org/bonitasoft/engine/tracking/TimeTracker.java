package org.bonitasoft.engine.tracking;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

public class TimeTracker {

    private final Set<String> activatedRecords;

    private final PrinterThread printerThread;

    private final RecordKeeper recordKeeper;

    private final File outputFolderFile;

    private final List<FlushEventListener> flushSubscribers;

    private final TechnicalLoggerService logger;

    public TimeTracker(
            final TechnicalLoggerService logger,
            final boolean startPrinterThread,
            final List<FlushEventListener> flushSubscribers,
            final int maxSize,
            final int flushIntervalInSeconds,
            final String outputFolder,
            final String csvSeparator,
            final String... activatedRecords) {
        super();
        this.logger = logger;
        this.flushSubscribers = flushSubscribers;
        outputFolderFile = new File(outputFolder);
        if (!outputFolderFile.exists()) {
            throw new RuntimeException("Output folder does not exist: " + outputFolder);
        }
        if (!outputFolderFile.isDirectory()) {
            throw new RuntimeException("Output folder is not a directory: " + outputFolder);
        }
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
        this.recordKeeper = new RecordKeeper(logger, maxSize, csvSeparator, outputFolder, this.flushSubscribers);
        this.printerThread = new PrinterThread(flushIntervalInSeconds, recordKeeper);
        if (startPrinterThread) {
            startPrinterThread();
        }
        Runtime.getRuntime().addShutdownHook(new PrinterThreadShutdownHook(this.printerThread));
    }

    private static class PrinterThreadShutdownHook extends Thread {

        private final PrinterThread printerThread;

        public PrinterThreadShutdownHook(final PrinterThread printerThread) {
            super();
            this.printerThread = printerThread;
        }

        @Override
        public void run() {
            if (this.printerThread.isAlive()) {
                this.printerThread.interrupt();
            }

        }
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
            this.recordKeeper.record(record);
        }
    }

    public void startPrinterThread() {
        if (this.logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(getClass(), TechnicalLogSeverity.INFO, "Starting TimeTracker PrinterThread...");
        }
        if (this.printerThread.isAlive()) {
            throw new RuntimeException("PrinterThread is already running");
        }
        this.printerThread.start();
        if (this.logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(getClass(), TechnicalLogSeverity.INFO, "TimeTracker PrinterThread started.");
        }
    }

    public void stopPrinterThread() {
        if (this.logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(getClass(), TechnicalLogSeverity.INFO, "Stopping TimeTracker PrinterThread...");
        }
        if (!this.printerThread.isAlive()) {
            throw new RuntimeException("PrinterThread is not running");
        }
        this.printerThread.interrupt();
        if (this.logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(getClass(), TechnicalLogSeverity.INFO, "TimeTracker PrinterThread stopped.");
        }
    }

    public FlushEvent flush() throws IOException {
        if (this.logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(getClass(), TechnicalLogSeverity.INFO, "Flushing...");
        }
        final FlushEvent flushEvent = this.recordKeeper.flush();
        if (this.logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(getClass(), TechnicalLogSeverity.INFO, "Flush finished: " + flushEvent);
        }
        return flushEvent;
    }

    public List<Record> getRecords() throws IOException {
        return this.recordKeeper.cloneRecords();
    }

}
