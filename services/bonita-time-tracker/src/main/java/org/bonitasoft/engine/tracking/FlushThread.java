/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.tracking;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

public class FlushThread extends Thread {

    private final TimeTracker timeTracker;

    private final TechnicalLoggerService logger;

    public FlushThread(final TimeTracker timeTracker) {
        super("Bonita-TimeTracker-FlushThread");
        this.logger = timeTracker.getLogger();
        this.timeTracker = timeTracker;
    }

    @Override
    public void run() {
        log(TechnicalLogSeverity.INFO, "Starting " + this.getName() + "...");
        long lastFlushTimestamp = System.currentTimeMillis();
        while (true) {
            final long now = System.currentTimeMillis();
            try {
                final long sleepTime = getSleepTime(now, lastFlushTimestamp);
                log(TechnicalLogSeverity.DEBUG, "FlushThread: sleeping for: " + sleepTime + "ms");
                this.timeTracker.getClock().sleep(sleepTime);
            } catch (InterruptedException e) {
                // Make sure to propagate the interruption to cleanly stop the current thread.
                Thread.currentThread().interrupt();
                break;
            }
            lastFlushTimestamp = flush(now);
        }
        log(TechnicalLogSeverity.INFO, this.getName() + " stopped.");
    }

    long getSleepTime(final long now, final long lastFlushTimestamp) throws InterruptedException {
        final long flushDuration = now - lastFlushTimestamp;
        return this.timeTracker.getFlushIntervalInMS() - flushDuration;
    }

    long flush(final long now) {
        try {
            final FlushResult flushResult = this.timeTracker.flush();
            return flushResult.getFlushTime();
        } catch (Exception e) {
            log(TechnicalLogSeverity.WARNING, "Exception caught while flushing: " + e.getMessage(), e);
        }
        return now;
    }

    void log(TechnicalLogSeverity severity, String message) {
        if (logger.isLoggable(getClass(), severity)) {
            logger.log(getClass(), severity, message);
        }
    }

    void log(TechnicalLogSeverity severity, String message, Exception e) {
        if (logger.isLoggable(getClass(), severity)) {
            logger.log(getClass(), severity, message, e);
        }
    }

    public boolean isStarted() {
        return isAlive();
    }

}
