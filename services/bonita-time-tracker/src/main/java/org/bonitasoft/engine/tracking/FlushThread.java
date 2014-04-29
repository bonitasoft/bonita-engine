package org.bonitasoft.engine.tracking;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

public class FlushThread extends Thread {

    private final TimeTracker timeTracker;

    private final long flushIntervalInMilliSeconds;

    private final Clock clock;

    private final TechnicalLoggerService logger;

    public FlushThread(final Clock clock, final long flushIntervalInMilliSeconds, final TimeTracker timeTracker, final TechnicalLoggerService logger) {
        super("TimeTracker-FlushThread");
        this.clock = clock;
        this.logger = logger;
        this.flushIntervalInMilliSeconds = flushIntervalInMilliSeconds;
        this.timeTracker = timeTracker;
    }

    @Override
    public void run() {
        if (this.logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(getClass(), TechnicalLogSeverity.INFO, "Starting " + this.getName() + "...");
        }
        while (true) {
            try {
                clock.sleep(flushIntervalInMilliSeconds);
            } catch (InterruptedException e) {
                break;
            }
            try {
                this.timeTracker.flush();
            } catch (Exception e) {
                if (this.logger.isLoggable(getClass(), TechnicalLogSeverity.WARNING)) {
                    this.logger.log(getClass(), TechnicalLogSeverity.WARNING, "Exception caught while flushing: " + e.getMessage(), e);
                }
            }
        }
        if (this.logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(getClass(), TechnicalLogSeverity.INFO, this.getName() + " stopped.");
        }
    }

}
