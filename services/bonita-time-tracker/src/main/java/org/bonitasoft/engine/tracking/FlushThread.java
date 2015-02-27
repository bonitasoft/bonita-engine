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
