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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.bonitasoft.engine.commons.TenantLifecycleService;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

public class TimeTracker implements TenantLifecycleService {

    private final Set<TimeTrackerRecords> activatedRecords;
    private FlushThread flushThread;
    private final Map<String, FlushEventListener> flushEventListeners;
    private final TechnicalLoggerService logger;
    private final Queue<Record> records;
    private final Clock clock;

    private long flushIntervalInMS;
    private boolean startTracking = false;

    private boolean serviceStarted;
    private long lastFlushTimestamp = 0L;

    public TimeTracker(
            final TechnicalLoggerService logger,
            final boolean startTracking,
            final List<FlushEventListener> flushEventListeners,
            final int maxSize,
            final int flushIntervalInSeconds,
            final String... activatedRecords) {
        this(logger, new ThreadSleepClockImpl(), startTracking, flushEventListeners, maxSize,
                flushIntervalInSeconds * 1000, activatedRecords);
    }

    public TimeTracker(
            final TechnicalLoggerService logger,
            final Clock clock,
            final boolean startTracking,
            final List<FlushEventListener> flushEventListeners,
            final int maxSize,
            final int flushIntervalInMS,
            final String... activatedRecords) {
        super();
        this.startTracking = startTracking;
        this.clock = clock;
        this.flushIntervalInMS = flushIntervalInMS;
        this.records = new CircularFifoQueue<>(maxSize);
        this.serviceStarted = false;
        this.logger = logger;
        this.flushEventListeners = new ConcurrentHashMap<>();
        if (flushEventListeners != null) {
            for (final FlushEventListener flushEventListener : flushEventListeners) {
                final String name = flushEventListener.getName();
                if (this.flushEventListeners.containsKey(name)) {
                    log(TechnicalLogSeverity.ERROR, "Duplicate entry for flushEventListener with name: " + name);
                }
                this.flushEventListeners.put(name, flushEventListener);
            }
        }

        if (activatedRecords == null || activatedRecords.length == 0) {
            this.activatedRecords = Collections.emptySet();
        } else {
            this.activatedRecords = new HashSet<>();
            for (final String activatedRecord : activatedRecords) {
                this.activatedRecords.add(TimeTrackerRecords.valueOf(activatedRecord));
            }
        }
        log(TechnicalLogSeverity.INFO, getStatus());
    }

    /**
     * get the list of Active Listener
     */
   public List<FlushEventListener> getActiveFlushEventListeners() {
        final List<FlushEventListener> active = new ArrayList<>();
        for (final FlushEventListener flushEventListener : this.flushEventListeners.values()) {
            if (flushEventListener.isActive()) {
                active.add(flushEventListener);
            }
        }
        return active;
    }
   /**
    * return all Event Listeners, active or not
    */
   public List<FlushEventListener> getFlushEventListeners() {
       return new ArrayList(this.flushEventListeners.values());
   }

    /**
     * reference a new flushEventListener. The key of the reference is the flushEventListener.name().
     * If a listener exist with this name, it will be replaced.
     */
    public void addFlushEventListener(final FlushEventListener flushEventListener) {
        this.flushEventListeners.put(flushEventListener.getName(), flushEventListener);
    }

    /**
     * remove a flush event listener
     */
    public void removeFlushEventListener(final String flushEventListenerName) {
        this.flushEventListeners.remove(flushEventListenerName);
    }

    public boolean activateFlushEventListener(final String flushEventListenerName) {
        final FlushEventListener flushEventListener = this.flushEventListeners.get(flushEventListenerName);
        if (flushEventListener == null) {
            return false;
        }
        flushEventListener.activate();
        return true;
    }

    public boolean deactivateFlushEventListener(final String flushEventListenerName) {
        final FlushEventListener flushEventListener = this.flushEventListeners.get(flushEventListenerName);
        if (flushEventListener == null) {
            return false;
        }
        flushEventListener.deactivate();
        return true;
    }

    public void activateRecord(final TimeTrackerRecords activatedRecord) {
        this.activatedRecords.add(activatedRecord);
    }

    public void deactivatedRecord(final TimeTrackerRecords activatedRecord) {
        this.activatedRecords.remove(activatedRecord);
    }

    public Set<TimeTrackerRecords> getActivatedRecords() {
        return Collections.unmodifiableSet(this.activatedRecords);
    }

    public void startTracking() {
        if (!this.serviceStarted) {
            log(TechnicalLogSeverity.WARNING, "Cannot start Time tracker tracking because service is not started.");
            return;
        }
        this.startTracking = true;
        internalStartTracking();
    }

    public void stopTracking() {
        this.startTracking = false;
        internalStopTracking();
    }

    FlushThread createFlushThread() {
        return new FlushThread(this);
    }

    private void internalStartTracking() {
        if (this.startTracking) {
            log(TechnicalLogSeverity.WARNING, "Starting Time tracker tracking...");
            this.flushThread = createFlushThread();
            this.flushThread.start();
            for (final FlushEventListener listener : getActiveFlushEventListeners()) {
                listener.notifyStartTracking();
            }
            log(TechnicalLogSeverity.WARNING,
                    "Time tracker tracking is activated. This may not be used in production as performances may be strongly impacted.");
        }
    }

    private void internalStopTracking() {
        if (isTracking()) {
            log(TechnicalLogSeverity.WARNING, "Stopping Time tracker tracking...");
            if (this.flushThread.isStarted()) {
                this.flushThread.interrupt();
                try {
                    // Wait for the thread to die
                    this.flushThread.join();
                } catch (final InterruptedException e) {
                    // We want this thread to be interrupted. No need to do extra work here, we are in the desired state.
                }
            }
            for (final FlushEventListener listener : getActiveFlushEventListeners()) {
                listener.notifyStopTracking();
            }
            log(TechnicalLogSeverity.WARNING, "Time tracker tracking is deactivated.");
        }
    }

    public boolean isTracking() {
        return this.flushThread != null && this.flushThread.isStarted();
    }

    public long getFlushIntervalInMS() {
        return this.flushIntervalInMS;
    }

    public void setFlushIntervalInSeconds(final long flushIntervalInSeconds) {
        this.flushIntervalInMS = flushIntervalInSeconds * 1000;
    }

    public void setFlushIntervalInMS(final long flushIntervalInMS) {
        this.flushIntervalInMS = flushIntervalInMS;
    }

    public Clock getClock() {
        return this.clock;
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
        sb.append(isTracking());
        sb.append("\n");

        sb.append("  - flushIntervalInSeconds: ");
        sb.append(this.flushIntervalInMS);
        sb.append("\n");

        sb.append("  - activatedRecords: ");
        for (final TimeTrackerRecords activatedRecord : this.activatedRecords) {
            sb.append(activatedRecord.name());
            sb.append(" ");
        }
        sb.append("\n");

        sb.append("  - flushEventListeners: ");
        for (final FlushEventListener flushEventListener : this.flushEventListeners.values()) {
            sb.append(flushEventListener.getStatus());
            sb.append(" ");
        }
        sb.append("\n");

        sb.append("  - records.size: ");
        sb.append(this.records.size());
        sb.append("\n");

        sb.append("  - last flush occurrence: ");
        sb.append(new Date(this.lastFlushTimestamp).toString());
        sb.append("\n");

        sb.append("\n");
        sb.append("-----");
        return sb.toString();
    }

    public boolean isTrackable(final TimeTrackerRecords recordName) {
        return isTracking() && this.activatedRecords.contains(recordName);
    }

    public TechnicalLoggerService getLogger() {
        return this.logger;
    }

    public void track(final TimeTrackerRecords recordName, final String recordDescription, final long duration) {
        if (!isTrackable(recordName)) {
            return;
        }
        final long timestamp = System.currentTimeMillis();
        final Record record = new Record(timestamp, recordName, recordDescription, duration);
        log(TechnicalLogSeverity.DEBUG, "Tracking record: " + record);
        synchronized (this) {
            this.records.add(record);
        }
    }

    void log(final TechnicalLogSeverity severity, final String message) {
        if (this.logger.isLoggable(getClass(), severity)) {
            this.logger.log(getClass(), severity, message);
        }
    }

    public FlushResult flush() {
        log(TechnicalLogSeverity.INFO, "Flushing...");
        this.lastFlushTimestamp = System.currentTimeMillis();
        final List<FlushEventListenerResult> flushEventListenerResults = new ArrayList<>();
        final FlushResult flushResult = new FlushResult(this.lastFlushTimestamp, flushEventListenerResults);
        final List<Record> records;
        synchronized (this) {
            records = getRecordsCopy();
            clearRecords();
        }
        final FlushEvent flushEvent = new FlushEvent(this.lastFlushTimestamp, records);

        flushListeners(flushEvent, flushEventListenerResults);
        log(TechnicalLogSeverity.INFO, "Flush finished: " + flushEvent);
        return flushResult;
    }

    void flushListeners(final FlushEvent flushEvent, final List<FlushEventListenerResult> flushEventListenerResults) {
        if (this.flushEventListeners == null) {
            return;
        }
        for (final FlushEventListener listener : getActiveFlushEventListeners()) {
            try {
                final FlushEventListenerResult flushEventListenerResult = listener.flush(flushEvent);
                flushEventListenerResults.add(flushEventListenerResult);
            } catch (final Exception e) {
                log(TechnicalLogSeverity.WARNING,
                        "Exception while flushing: " + flushEvent + " on listener " + listener);
            }
        }
    }

    public List<Record> getRecordsCopy() {
        return Arrays.asList(this.records.toArray(new Record[this.records.size()]));
    }

    public void clearRecords() {
        this.records.clear();
    }

    @Override
    public void start() {
        if (this.serviceStarted) {
            return;
        }
        log(TechnicalLogSeverity.INFO, "Starting TimeTracker...");
        this.serviceStarted = true;
        internalStartTracking();
        log(TechnicalLogSeverity.INFO, "TimeTracker started.");
    }

    @Override
    public void stop() {
        if (!this.serviceStarted) {
            return;
        }
        log(TechnicalLogSeverity.INFO, "Stopping TimeTracker...");
        this.serviceStarted = false;
        internalStopTracking();
        log(TechnicalLogSeverity.INFO, "TimeTracker stopped.");
    }

    @Override
    public void pause() {
        stop();
    }

    @Override
    public void resume() {
        start();
    }

}
