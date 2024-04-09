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
package org.bonitasoft.engine.tracking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlushThread extends Thread {

    private static final Logger log = LoggerFactory.getLogger(FlushThread.class);
    private final TimeTracker timeTracker;

    public FlushThread(final TimeTracker timeTracker) {
        super("Bonita-TimeTracker-FlushThread");
        this.timeTracker = timeTracker;
    }

    @Override
    public void run() {
        log.info("Starting " + getName() + "...");
        long lastFlushTimestamp = System.currentTimeMillis();
        while (true) {
            final long now = System.currentTimeMillis();
            try {
                final long sleepTime = getSleepTime(now, lastFlushTimestamp);
                log.debug("FlushThread: sleeping for: " + sleepTime + "ms");
                this.timeTracker.getClock().sleep(sleepTime);
            } catch (final InterruptedException e) {
                // Make sure to propagate the interruption to cleanly stop the current thread.
                Thread.currentThread().interrupt();
                break;
            }
            lastFlushTimestamp = flush(now);
        }
        log.info(getName() + " stopped.");
    }

    long getSleepTime(final long now, final long lastFlushTimestamp) throws InterruptedException {
        final long flushDuration = now - lastFlushTimestamp;
        return this.timeTracker.getFlushIntervalInMS() - flushDuration;
    }

    long flush(final long now) {
        try {
            final FlushResult flushResult = this.timeTracker.flush();
            return flushResult.getFlushTime();
        } catch (final Exception e) {
            log.warn("Exception caught while flushing: " + e.getMessage(), e);
        }
        return now;
    }

    public boolean isStarted() {
        return isAlive();
    }

}
