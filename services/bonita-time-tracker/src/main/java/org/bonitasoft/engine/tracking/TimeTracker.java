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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.bonitasoft.engine.commons.TenantLifecycleService;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

public class TimeTracker implements TenantLifecycleService {

    private final boolean trackingEnabled;
    private final long flushIntervalInSeconds;
    private final Set<String> activatedRecords;
    private final FlushThread flushThread;
    private final List<? extends FlushEventListener> flushEventListeners;
    private final TechnicalLoggerService logger;
    private final Queue<Record> records;
    private boolean started;
    private long lastFlushTimestamp = 0L;

    public TimeTracker(
            final TechnicalLoggerService logger,
            final boolean trackingEnabled,
            final List<? extends FlushEventListener> flushEventListeners,
            final int maxSize,
            final int flushIntervalInSeconds,
            final String... activatedRecords) {
        this(logger, new ThreadSleepClockImpl(), trackingEnabled, flushEventListeners, maxSize, flushIntervalInSeconds * 1000, activatedRecords);
    }

    public TimeTracker(
            final TechnicalLoggerService logger,
            final Clock clock,
            final boolean trackingEnabled,
            final List<? extends FlushEventListener> flushEventListeners,
            final int maxSize,
            final int flushIntervalInSeconds,
            final String... activatedRecords) {
        super();
        this.flushIntervalInSeconds = flushIntervalInSeconds;
        this.trackingEnabled = trackingEnabled;
        records = new CircularFifoQueue<>(maxSize);
        started = false;
        this.logger = logger;
        this.flushEventListeners = flushEventListeners;
        if (activatedRecords == null || activatedRecords.length == 0) {
            this.activatedRecords = Collections.emptySet();
        } else {
            this.activatedRecords = new HashSet<>(Arrays.asList(activatedRecords));
        }
        flushThread = createFlushThread(logger, clock, flushIntervalInSeconds);
        if (trackingEnabled && logger.isLoggable(getClass(), TechnicalLogSeverity.WARNING)) {
            this.logger.log(getClass(), TechnicalLogSeverity.WARNING,
                    "Time tracker is activated. This may not be used in production as performances may be strongly impacted.");
        }
        if (logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            this.logger.log(getClass(), TechnicalLogSeverity.INFO,
                    getStatus());
        }
    }

    public String getStatus() {
        final StringBuilder sb = new StringBuilder();
        sb.append("-----");
        sb.append("\n");

        sb.append("Time Tracker '");
        sb.append(this.getClass().getName());
        sb.append("':");
        sb.append("\n");

        sb.append("  - trackingEnabled: ");
        sb.append(trackingEnabled);
        sb.append("\n");

        sb.append("  - flushIntervalInSeconds: ");
        sb.append(flushIntervalInSeconds);
        sb.append("\n");

        sb.append("  - activatedRecords: ");
        for (String activatedRecord : activatedRecords) {
            sb.append(activatedRecord);
            sb.append(" ");
        }
        sb.append("\n");

        sb.append("  - flushEventListeners: ");
        for (FlushEventListener flushEventListener : flushEventListeners) {
            sb.append(flushEventListener.getStatus());
            sb.append(" ");
        }
        sb.append("\n");

        sb.append("  - records.size: ");
        sb.append(records.size());
        sb.append("\n");

        sb.append("  - last flush occurrence: ");
        sb.append(new Date(lastFlushTimestamp).toString());
        sb.append("\n");

        sb.append("\n");
        sb.append("-----");
        return sb.toString();
    }
    
    FlushThread createFlushThread(TechnicalLoggerService logger, Clock clock, int flushIntervalInSeconds) {
        return new FlushThread(clock, flushIntervalInSeconds, this, logger);
    }

    public boolean isTrackable(final String recordName) {
        return trackingEnabled && started && activatedRecords.contains(recordName);
    }

    public void track(final String recordName, final String recordDescription, final long duration) {
        if (!isTrackable(recordName)) {
            return;
        }
        final long timestamp = System.currentTimeMillis();
        final Record record = new Record(timestamp, recordName, recordDescription, duration);
        debug(TechnicalLogSeverity.DEBUG, "Tracking record: " + record);
        // TODO needs a synchro?
        records.add(record);
    }

    void debug(TechnicalLogSeverity debug, String message) {
        if (logger.isLoggable(getClass(), debug)) {
            logger.log(getClass(), debug, message);
        }
    }

    public List<FlushResult> flush() {
        if (!trackingEnabled) {
            return Collections.emptyList();
        }
        debug(TechnicalLogSeverity.INFO, "Flushing...");
        lastFlushTimestamp = System.currentTimeMillis();
        final List<Record> records = getRecords();
        final FlushEvent flushEvent = new FlushEvent(records);
        final List<FlushResult> flushResults = new ArrayList<>();
        flushListeners(flushEvent, flushResults);
        debug(TechnicalLogSeverity.INFO, "Flush finished: " + flushEvent);
        return flushResults;
    }

    void flushListeners(FlushEvent flushEvent, List<FlushResult> flushResults) {
        if (flushEventListeners == null) {
            return;
        }
        for (final FlushEventListener listener : flushEventListeners) {
            flushListener(flushEvent, flushResults, listener);
        }
    }

    void flushListener(FlushEvent flushEvent, List<FlushResult> flushResults, FlushEventListener listener) {
        try {
            flushResults.add(listener.flush(flushEvent));
        } catch (final Exception e) {
            debug(TechnicalLogSeverity.WARNING, "Exception while flushing: " + flushEvent + " on listener " + listener);
        }
    }

    public List<Record> getRecords() {
        return Arrays.asList(records.toArray(new Record[records.size()]));
    }

    @Override
    public void start() {
        if (!trackingEnabled) {
            return;
        }
        debug(TechnicalLogSeverity.INFO, "Starting TimeTracker...");
        startFlushThread();
        started = true;
        debug(TechnicalLogSeverity.INFO, "TimeTracker started.");
    }

    void startFlushThread() {
        if (flushThread != null && !flushThread.isStarted()) {
            flushThread.start();
        }
    }

    @Override
    public void stop() {
        if (!trackingEnabled) {
            return;
        }
        debug(TechnicalLogSeverity.INFO, "Stopping TimeTracker...");
        interruptFlushThread();
        started = false;
        debug(TechnicalLogSeverity.INFO, "TimeTracker stopped.");
    }

    void interruptFlushThread() {
        if (flushThread != null && flushThread.isStarted()) {
            flushThread.interrupt();
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

}
