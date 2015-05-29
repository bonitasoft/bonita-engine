package org.bonitasoft.engine.tracking;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

public class FlushThread extends Thread {

    private final TimeTracker timeTracker;

    private long flushIntervalInSeconds;

    private Date currentDate = null;

    private final Clock clock;

    private final TechnicalLoggerService logger;

    public boolean askStop;

    private String msgState = "";

    public FlushThread(final Clock clock, final long flushIntervalInSeconds,
            final TimeTracker timeTracker, final TechnicalLoggerService logger) {
        super("TimeTracker-FlushThread");
        this.clock = clock;
        this.logger = logger;
        this.flushIntervalInSeconds = flushIntervalInSeconds;
        this.timeTracker = timeTracker;
    }

    public Date getLastFlushExecutionTime() {
        return currentDate;
    }

    public long getFlushIntervalInSeconds() {
        return flushIntervalInSeconds;
    }

    public void setFlushIntervalInSeconds(long flushIntervalInSeconds) {
        this.flushIntervalInSeconds = flushIntervalInSeconds;
    }

    public String getMsgState() {
        return msgState;
    }

    @Override
    public void run() {

        msgState = "Started;";
        if (this.logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(getClass(), TechnicalLogSeverity.INFO, "Starting "
                    + this.getName() + "...");
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        askStop = false;
        while (true) {
            msgState += "Sleep " + flushIntervalInSeconds + " s;";
            try {
                clock.sleep(flushIntervalInSeconds * 1000);
            } catch (InterruptedException e) {
                break;
            }
            currentDate = new Date();
            msgState = ""; // reset
            msgState += "Wakeup at " + sdf.format(currentDate) + " ;";

            try {
                this.timeTracker.flush();
            } catch (Exception e) {
                if (this.logger.isLoggable(getClass(),
                        TechnicalLogSeverity.WARNING)) {
                    this.logger.log(
                            getClass(),
                            TechnicalLogSeverity.WARNING,
                            "Exception caught while flushing: "
                                    + e.getMessage(), e);
                }
                msgState += "Error flush:" + e.toString() + " ;";

            }
            if (askStop) {
                msgState += "AskStop;";
                if (this.logger.isLoggable(getClass(),
                        TechnicalLogSeverity.INFO)) {
                    logger.log(getClass(), TechnicalLogSeverity.INFO,
                            this.getName() + " ASK STOP.");
                }
                return;
            }
        }
        if (this.logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(getClass(), TechnicalLogSeverity.INFO, this.getName()
                    + " stopped.");
        }
    }

}
