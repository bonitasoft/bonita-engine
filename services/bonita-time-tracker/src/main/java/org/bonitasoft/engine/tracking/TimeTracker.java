package org.bonitasoft.engine.tracking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.bonitasoft.engine.commons.TenantLifecycleService;

public class TimeTracker implements TenantLifecycleService {

    private Set<String> activatedRecords;

    private final FlushThread flushThread;

    // private List<? extends FlushEventListener> flushEventListeners;
    private final List<FlushEventListener> flushEventListeners;

    private final TechnicalLoggerService logger;

    /**
     * the first queue contains only the "will be flush" event.
     */
    private final ConcurrentLinkedQueue<Record> recordsInQueue;
    /**
     * The queue contains all the last N records
     * NB: to avoid the synchronization, it may be a better idea to create one EventFlusher, and then the Event Flusher keep the list.
     */
    private final Queue<Record> allRecords;

    /**
     * the marker to indicate if the flush is activated or not
     */
    private boolean started;

    private final boolean startFlushThread;

    private String msgTimeTracker = "";

    public TimeTracker(
            final TechnicalLoggerService logger,
            final boolean startFlushThread,
            final List<FlushEventListener> flushEventListeners,
            final int maxSize,
            final int flushIntervalInSeconds,
            final String... activatedRecords) {
        this(logger, new ThreadSleepClockImpl(), startFlushThread, flushEventListeners, maxSize, flushIntervalInSeconds, activatedRecords);
    }

    public TimeTracker(
            final TechnicalLoggerService logger,
            final Clock clock,
            final boolean startFlushThread,
            final List<FlushEventListener> flushEventListeners,
            final int maxSize,
            final int flushIntervalInSeconds,
            final String... activatedRecords) {

        this.startFlushThread = startFlushThread;
        started = false;
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
        if (!startFlushThread) {
            msgTimeTracker = "FLUSH NOT ALLOWED TO START. Check parameter[startFlushThread] in service;";
        }

        flushThread = new FlushThread(clock, flushIntervalInSeconds, this, logger);

        // records = new CircularFifoQueue<Record>(maxSize);
        recordsInQueue = new ConcurrentLinkedQueue<Record>();
        allRecords = new CircularFifoQueue<Record>(maxSize);
    }

    public boolean isTrackable(final String recordName) {
        return started && activatedRecords.contains(recordName);
    }

    public void track(final String recordName, final String recordDescription, final long duration) {
        if (isTrackable(recordName)) {
            final long timestamp = System.currentTimeMillis();
            final Record record = new Record(timestamp, recordName, recordDescription, duration);
            track(record);
        }
    }

    public void track(final Record record) {

        if (isTrackable(record.getName())) {
            if (logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
                logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Tracking record: " + record);
            }
            // TODO needs a synchro?
            recordsInQueue.add(record);

            // Evolution : keep a reference to a Flusher, and ask him. Then, the synchonisation is not needed
            synchronized (allRecords) {
                allRecords.add(record);
            }
        }
    }

    /**
     * flush is call by only one thread, from the FlushTread class
     *
     * @return
     */
    public List<FlushResult> flush() {

        if (logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(getClass(), TechnicalLogSeverity.INFO, "Flushing...");
        }
        final List<Record> listRecords = new ArrayList<Record>();
        Record oneRecord = null;
        do
        {
            oneRecord = recordsInQueue.poll();
            if (oneRecord != null) {
                listRecords.add(oneRecord);
            }
        } while (oneRecord != null);

        if (logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
            logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Flushing...");
        }

        final List<FlushResult> flushResults = new ArrayList<FlushResult>();

        // build the flushEvent array from the queue
        if (listRecords.size() > 0 && flushEventListeners != null)
        {
            final FlushEvent flushEvent = new FlushEvent(listRecords);

            for (final FlushEventListener listener : flushEventListeners) {
                try {
                    flushResults.add(listener.flush(flushEvent));
                } catch (final Exception e) {
                    if (logger.isLoggable(getClass(), TechnicalLogSeverity.WARNING)) {
                        logger.log(getClass(), TechnicalLogSeverity.WARNING, "Exception while flushing: " + flushEvent + " on listener " + listener);
                    }
                }
            }

            if (logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
                logger.log(getClass(), TechnicalLogSeverity.INFO, "Flush finished: " + flushEvent);
            }
        }
        return flushResults;
    }

    /**
     * get all the records.
     * Evolution ? : keep a reference to a Flusher, and ask him. Then, the synchonisation is not needed
     */
    public List<Record> getRecords() {
        return Arrays.asList(allRecords.toArray(new Record[] {}));
    }

    @Override
    public void start() {
        if (logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(getClass(), TechnicalLogSeverity.INFO, "Starting TimeTracker..." + startFlushThread + " && " + !flushThread.isAlive()
                    + "] listEventListener[" + flushEventListeners.size() + "]");
        }

        if (startFlushThread && !flushThread.isAlive()) {
            flushThread.start();
        }
        started = true;

        if (logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(getClass(), TechnicalLogSeverity.INFO, "TimeTracker started.");
        }
    }

    @Override
    public void stop() {
        if (logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(getClass(), TechnicalLogSeverity.INFO, "Stopping TimeTracker...");
        }
        flushThread.askStop = true;
        if (flushThread.isAlive()) {
            flushThread.interrupt();
        }
        started = false;
        if (logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(getClass(), TechnicalLogSeverity.INFO, "TimeTracker stopped.");
        }
    }

    @Override
    public void pause() {
        // nothing to do as this service is not for production, we don't want to spend time on this
    }

    @Override
    public void resume() {
        // nothing to do as this service is not for production, we don't want to spend time on this
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isFlushThreadAlive() {
        return flushThread.isAlive();
    }

    public long getFlushIntervalInSeconds()
    {
        return flushThread.getFlushIntervalInSeconds();
    }

    public void setFlushIntervalInSeconds(final long flushIntervalInSeconds)
    {
        flushThread.setFlushIntervalInSeconds(flushIntervalInSeconds);
    }

    /**
     * return null if the flush is never made
     *
     * @return
     */
    public Date getLastFlushExecutionTime()
    {
        return flushThread.getLastFlushExecutionTime();
    }

    public String getFlushMsgState()
    {
        return msgTimeTracker
                + (activatedRecords.size() == 0 ? "NoRecordActivated;" : activatedRecords.size() + " events recorded;")
                + (flushThread.isAlive() ? "Flush Alive;" : "Flush not started;")
                + flushThread.getMsgState();
    }

    /**
     * return the list of different activated record.
     * Different string have constants, and can be found at org.bonitasoft.engine.tracking.TimeTrackerRecords
     *
     * @return
     * @see org.bonitasoft.engine.tracking.TimeTrackerRecords
     */
    public Set<String> getActivatedRecords()
    {
        return activatedRecords;
    }

    /**
     * Set the list of different string.
     *
     * @param activatedRecords
     * @see org.bonitasoft.engine.tracking.TimeTrackerRecords
     * @see
     */
    public void setActivatedRecords(final Set<String> activatedRecords)
    {
        this.activatedRecords = activatedRecords;
    }

    /**
     * Add a new register event
     *
     * @param flushEventListener
     */
    public void registerListener(final FlushEventListener flushEventListener)
    {
        flushEventListeners.add(flushEventListener);
    }

    public void unRegisterListener(final FlushEventListener flushEventListener)
    {
        flushEventListeners.remove(flushEventListener);
    }

    public List<FlushEventListener> getListEventListener()
    {
        return flushEventListeners;
    }

    public String getListEventListenerSt()
    {
        final StringBuffer result = new StringBuffer();
        for (final FlushEventListener eventListener : flushEventListeners)
        {
            result.append(eventListener.toString() + ";");
        }
        return result.toString();
    }

}
